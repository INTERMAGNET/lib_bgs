/*
 * PCXImage.java
 *
 * Created on 17 February 2003, 17:09
 */

package bgs.geophys.library.AWT;


import java.awt.*;
import java.awt.image.*;
import java.io.*;

import bgs.geophys.library.Misc.*;

/**
 * Class to hold PCX image data file
 *
 * @author  fmc
 */
public class PCXImage
{
  private boolean image_loaded = false;
  private Image image;
  private String error_message;
  private int x_size, y_size;

 /********************************************************************
  * creates a new PCXImage
  * @param - the file containing the image data
  ********************************************************************/
  public PCXImage(File filename)
  {
    image_loaded = readPCXFile (filename);
  }

 /********************************************************************
  * readPCXFile - read pcx file and store as an image
  *
  * @return true if read successfully, false otherwise
  ********************************************************************/
  public boolean readPCXFile (File filename)
  {
    final int HEADER_LENGTH = 128;
    final int PALETTE_LENGTH_16 = 48;
    final int PALETTE_LENGTH_256 = 768;

    final int COLOUR_PALETTE_16 = 0;
    final int COLOUR_PALETTE_256 = 1;
    final int RGB = 2;

    FileInputStream file_stream;
    DataInputStream data_stream;
    byte header_buffer [];
    byte palette_buffer [];
    byte bit_index [] = { (byte)0x80, (byte)0x40, (byte)0x20, (byte)0x10,
                          (byte)0x08, (byte)0x04, (byte)0x02, (byte)0x01 };
    int pcx, version, bits_per_pixel, n_planes, bytes_per_line_per_plane;
    int x_min, x_max, y_min, y_max, scan_length, format;
    int n, i, total_size, bit_no, palette_index;
    int red = 0, green = 0, blue = 0;
    int data_byte = 0;
    int plane = 0, n_lines = 0, repeat = 0, pixelcount = 0;
    int colour_planes [][];
    int index [];
    int pixels [];
    Color colour;
    boolean load_data = true;
    MemoryImageSource mis;

    // buffer for image header
    header_buffer = new byte [HEADER_LENGTH];

    // temporary length for palette buffer until actual length is known
    palette_buffer = new byte [0];

    // open file and create data stream
    try
    {
      file_stream = new FileInputStream (filename);
    }
    catch (FileNotFoundException e)
    {
      error_message = ("Could not open file " + filename.toString());
      return false;
    }
    data_stream = new DataInputStream (file_stream);

    // read header
    try
    {
      data_stream.read (header_buffer, 0, HEADER_LENGTH);
    }
    catch (IOException e)
    {
      error_message = "header error";
      return false;
    }

    // read required information from header...

    // confirm PCX format versions 2 to 5
    pcx = header_buffer [0];
    version = header_buffer [1];
    if (pcx != 10 || version < 2 || version > 5)
    {
      error_message = "not a valid PCX file";
      return false;
    }

    // ...bits per pixel
    bits_per_pixel = header_buffer [3];

    // dimensions of image
    x_min = Utils.bytesToInt (header_buffer [4], header_buffer [5], (byte)0, (byte)0);
    y_min = Utils.bytesToInt (header_buffer [6], header_buffer [7], (byte)0, (byte)0);
    x_max = Utils.bytesToInt (header_buffer [8], header_buffer [9], (byte)0, (byte)0);
    y_max = Utils.bytesToInt (header_buffer [10], header_buffer [11], (byte)0, (byte)0);

    x_size = x_max - x_min + 1;

    // length of scan line will always be an even number
    if (x_size %2  != 0) scan_length = x_size + 1;
    else scan_length = x_size;
    y_size = y_max - y_min + 1;

    // number of colour planes
    n_planes = header_buffer [65];
    // bytes per line (use this because x_max - x_min does not take into
    // account run-length encoding)
    bytes_per_line_per_plane = Utils.bytesToInt (header_buffer [66], header_buffer [67], (byte)0, (byte)0);

    // check that this format can be read
    if (bits_per_pixel == 1 && n_planes == 4)
    {
      //  1 bit per pixel, 4 planes image has 16 colour
      // palette in image header
      format = COLOUR_PALETTE_16;
      palette_buffer = new byte [PALETTE_LENGTH_16];

      // read palette
      for (n = 0; n < PALETTE_LENGTH_16; n++)
      {
        palette_buffer [n] = header_buffer [16 + n];
      }
    }
    else if (bits_per_pixel == 8 && n_planes == 1)
    {
      // 8 bits per pixel, 1 plane image has 256 colour
      // palette at end of image data
      format = COLOUR_PALETTE_256;
      palette_buffer = new byte [PALETTE_LENGTH_256];
    }
    else if (bits_per_pixel == 8 && n_planes == 3)
    {
      // 8 bits per pixel, 3 planes: planes represent
      // red, green and blue values for each pixel
      format = RGB;
    }
    else
    {
      error_message = "cannot read image with " + bits_per_pixel + " bits per pixel and "
                                                + n_planes + " colour_planes.";
      return false;
    }

    // set up storage buffers
    total_size = scan_length * y_size;
    // storage for each colour plane
    colour_planes = new int [n_planes][total_size];
    // next index into colour plane array
    index = new int [n_planes];
    // array of final image pixels
    pixels = new int [total_size];

    // set indices into colour_planes array
    for (n = 0; n < n_planes; n++)
    {
      index [n] = 0;
    }

    // read all image data into colour plane buffers
    while (load_data)
    {
      try
      {
        // if repeat is > 0 then the previous data value is being
        // repeated and a new data byte is not required
        if (repeat <= 0)
        {
          // read next data byte
          data_byte = data_stream.readByte ();
          data_byte = (data_byte >= 0) ? data_byte : (data_byte + 256);

          // if top 2 bits are set this is the repeat count
          if ((data_byte & 0xC0) == 0xC0)
          {
            // remove top bits and find number of times to repeat this
            repeat  = data_byte & 0x3F;

            // get next byte to repeat this many times
            data_byte = data_stream.readByte ();
            data_byte = (data_byte >= 0) ? data_byte : (data_byte + 256);
          }
          // this byte is the data byte, record once
          else repeat = 1;
        }

        while (repeat > 0)
        {
          // assign data byte to bufffer and increment counters
          colour_planes [plane] [index [plane]] = data_byte;
          repeat--;
          index [plane] ++;

          if (index [plane] % bytes_per_line_per_plane == 0)
          {
            // this plane is finished
            plane ++;
            if (plane >= n_planes)
            {
              // this line is finished
              plane = 0;
              n_lines ++;
              // coding break at end of each line (but not neccesarily
              // at end of each plane)
              if (repeat > 0)
              {
                error_message = "coding error at index " + index [plane];
                return false;
              }

              if (n_lines >= y_size)
              {
                // data is finished
                repeat = 0;
                load_data = false;
              }
            }
          }
        }
      }
      catch (IOException e)
      {
        error_message = "error reading image file at plane " + plane + " index " + index [plane];
        return false;
      }
    }

    // if 8 bit image with 1 plane then 256 colour palette will be at end of file
    if (format == COLOUR_PALETTE_256)
    {
      try
      {
        // spare bytes at bottom of image will be set to zero.
        // read spare bytes until start of palette - marked by byte value 12
        data_byte = 0;
        while (data_byte != 12)
          data_byte = data_stream.readByte();

        // read palette
        data_stream.read (palette_buffer, 0, PALETTE_LENGTH_256);
      }
      catch (IOException e)
      {
        error_message = "palette error";
        return false;
      }
    }

    // 16 colour image using the palette from the image header: this
    // is a one bit per pixel, 4 colour plane image. This index into
    // the palette is found by taking a bit from each of the
    // 4 colour planes, giving an integer between 0 and 15.
    if (format == COLOUR_PALETTE_16)
    {
      for (n = 0; n < index [0]; n ++)
      {
        for (bit_no = 1; bit_no <= 8; bit_no ++)
        {
          palette_index = 0;
          // get a bit from each plane and make an integer
          for (i = 0; i < n_planes; i++)
          {
            data_byte = colour_planes [i] [n] & bit_index [bit_no - 1];
            data_byte = data_byte >> (8 - bit_no);
            palette_index = palette_index | data_byte << i;
          }

          if (palette_index < 0 || palette_index >= PALETTE_LENGTH_16 / 3)
          {
            error_message = "palette error: index " + palette_index + " pixelcount " + pixelcount;
            return false;
          }

          // palette is ordered r, g, b, r, g, b...
          red = palette_buffer  [palette_index * 3];
          green = palette_buffer [palette_index * 3 + 1];
          blue = palette_buffer [palette_index * 3 + 2];

          red = (red >= 0) ? red : (red + 256);
          green = (green >= 0) ? green : (green + 256);
          blue = (blue >= 0) ? blue : (blue + 256);

          colour = new Color (red, green, blue);
          pixels [pixelcount] = colour.getRGB();
          pixelcount ++;
          if (pixelcount > total_size)
          {
            error_message = "Image has too many pixels: " + pixelcount;
            return false;
          }
        }
      }
    }
    else
    {
      for (n = 0; n < total_size; n++)
      {
        // 8 bit, 1 plane image: the bytes contain an index into
        // the colour palette.
        if (format == COLOUR_PALETTE_256)
        {
          // palette is organised r, g, b, r, g, b...
          red = palette_buffer [colour_planes [0][n] * 3];
          green = palette_buffer [colour_planes [0][n] * 3 + 1];
          blue = palette_buffer [colour_planes [0][n] * 3 + 2];
        }
        // 8 bit, 3 planes image: each plane holds red, green or blue
        // values
        else if (format == RGB)
        {
          red = colour_planes [0] [n];
          green = colour_planes [1] [n];
          blue = colour_planes [2] [n];
        }

        red = (red >= 0) ? red : (red + 256);
        green = (green >= 0) ? green : (green + 256);
        blue = (blue >= 0) ? blue : (blue + 256);
      
        colour = new Color (red, green, blue);
        pixels [n] = colour.getRGB();
      }
    }

    // make image from array of pixels
    mis = new MemoryImageSource (x_size, y_size, pixels, 0, scan_length);
    image = Toolkit.getDefaultToolkit().createImage (mis);
    return true;
  }

  public boolean isImageLoaded ()
  {
    return image_loaded;
  }

  public Image getImage ()
  {
    return image;
  }

  public String getErrorMessage ()
  {
    return error_message;
  }

  public int getHeight ()
  {
    return y_size;
  }

  public int getWidth ()
  {
    return x_size;
  }
}
