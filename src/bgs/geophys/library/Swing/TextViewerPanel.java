/*
 * TextViewerPanel.java
 *
 * Created on 14 February 2003, 11:41
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.io.*;

/**
 * Panel to display contents of a text file.
 *
 * @author  fmc
 */
public class TextViewerPanel extends JPanel implements Printable
{
  // constants
  static final int BUFFER_SIZE = 50000;

  // add text area to panel and display the
  // text in text area
  private JTextArea text_area;
  private boolean file_loaded = false;

 /********************************************************************
  * creates a new TextViewerPanel
  * @param textfile - the file to be displayed
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerPanel (File textfile)
  throws TextViewerException
  {
    // create components
    text_area = new JTextArea ();
    text_area.setFont (new Font ("MonoSpaced", Font.PLAIN, text_area.getFont().getSize()));
    text_area.setEditable (false);
    this.add (text_area);
    // display file
    this.update (textfile);
  }

 /********************************************************************
  * creates a new TextViewerPanel
  * @param reader - stream to the file to be displayed
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerPanel (Reader reader)
  throws TextViewerException
  {
    // create components
    text_area = new JTextArea ();
    text_area.setFont (new Font ("MonoSpaced", Font.PLAIN, text_area.getFont().getSize()));
    text_area.setEditable (false);
    this.add (text_area);
    // display file
    this.update (reader);
  }

 /********************************************************************
  * creates a new empty TextViewerPanel
  ********************************************************************/
  public TextViewerPanel ()
  {
    // create components
    text_area = new JTextArea ();
    text_area.setFont (new Font ("MonoSpaced", Font.PLAIN, text_area.getFont().getSize()));
    text_area.setEditable (false);
    this.add (text_area);
  }

 /********************************************************************
  * update - update the panel to display a new file
  * @param textfile - the file to be displayed
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public void update (File textfile)
  throws TextViewerException
  {
    String string;
    int width, height;

    
    try
    {
      FileReader file_reader = new FileReader (textfile);
      text_area.read(file_reader, "Text file");
      file_reader.close();
    }
    catch (Exception e)
    {
        text_area.setText("");
        if (textfile == null) string = "No file to display";
        else string = "Error reading file " + textfile.toString ();
        if (e.getMessage() != null) string += ": " + e.getMessage ();
        throw new TextViewerException (string);
    }

  }

 /********************************************************************
  * update - update the panel to display a new file
  * @param reader - the stream to read from
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public void update (Reader reader)
  throws TextViewerException
  {
    String string;
    int width, height;

    
    try
    {
      text_area.read(reader, "Text file");
    }
    catch (IOException e)
    {
        text_area.setText("");
        string = "Error reading file ";
        if (e.getMessage() != null) string += ": " + e.getMessage ();
        throw new TextViewerException (string);
    }

  }

  /********************************************************************
   * This method draws on a printer in the same way as paintComponent
   * draws on a component
   *
   * @param g - the graphics context to use
   * @param pageFormat -
   * @param pageIndex -
   * @return Printable.NO_SUCH_PAGE on error, Printable.PAGE_EXISTS on
   *         success
   ********************************************************************/
  public int print (Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException
  {
    int x, y, page_width, page_height, total_n_lines, text_area_width;
    int font_height, n_lines_per_page, n_pages, height_per_page;
    int last_line_printed, n_lines_left;
    double scale = 1;
    Graphics2D g2d = (Graphics2D) g;

    // translate origin into graphics coordinate system
    x = (int) pageFormat.getImageableX ();
    y = (int) pageFormat.getImageableY ();
    g2d.translate (x, y);
    
    // get page size
    page_height = (int) pageFormat.getImageableHeight ();
    page_width = (int) pageFormat.getImageableWidth ();

    // set text area variables
    text_area_width = text_area.getWidth ();
    font_height = g2d.getFontMetrics ().getHeight ();
    total_n_lines = text_area.getHeight () / font_height;

    // set scaling if line length won't fit onto width of page
    // and find new font size
    if (text_area_width > page_width)
    {
      scale = (double)page_width / (double)text_area_width;
      font_height = Math.round ((float)(g2d.getFontMetrics().getHeight() * scale) + 0.5f);
    }
    else scale = 1;

    // calculate number of pages and height of each page in order to set clipping
    n_lines_per_page = Math.round ((float)(page_height / font_height) - 0.5f);
    n_pages = Math.round ((float)(total_n_lines / n_lines_per_page) + 0.5f);
    height_per_page = n_lines_per_page * font_height;

    if (pageIndex >= n_pages)
    {
      return NO_SUCH_PAGE;
    }

    // set the origin of each page
    g2d.translate (0, -height_per_page * pageIndex);
    // Set graphics clipping to contain the portion for each page
    g2d.setClip (0, (height_per_page * pageIndex),
                 text_area_width, height_per_page);
    // do scaling
    g2d.scale (scale, scale);

    try
    {
      text_area.paint (g2d);
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(this, e.getMessage());
      return Printable.NO_SUCH_PAGE;
    }
    // normal successful return
    return Printable.PAGE_EXISTS;
  }
}
