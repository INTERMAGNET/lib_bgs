/*
 * ImageViewerPanel.java
 *
 * Created on 18 February 2003, 12:51
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.io.*;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.AWT.*;

/**
 * Panel to display an image (including PCX images)
 *
 * @author  fmc
 */
public class ImageViewerPanel extends JPanel implements Printable
{
    // put label on panel and display the image as label icon
    private JLabel label;
    private PCXImage pcx_image;
    private ImageIcon load_image;

    /********************************************************************
     * creates a new ImageViewerPanel
     * @param imagefile - the image to be displayed
     * @throws ImageViewerException if there was a problem loading the image
     ********************************************************************/
    public ImageViewerPanel (File imagefile)
    throws ImageViewerException
    {
        // create components
        label = new JLabel ();
        this.add (label);
        // display image
        this.update (imagefile);
    }

   /********************************************************************
    * creates a new empty ImageViewerPanel
    ********************************************************************/
    public ImageViewerPanel ()
    {
        // create components
        label = new JLabel ();
        this.add (label);
        pcx_image = null;
        load_image = null;
    }

   /********************************************************************
    * update - update the panel to display a new image
    * @param imagefile - the file to be displayed
    * @return - false if an error occurred, else true
    * @throws ImageViewerException if there was a problem loading the image
    ********************************************************************/
    public void update (File imagefile)
    throws ImageViewerException
    {
        int width, height;
        String filename;
    
        // check for null file
        if (imagefile == null)
        {
            label.setIcon (null);
            throw new ImageViewerException ("No file to display");
        }
        
        // load the image
        filename = imagefile.getPath();
        if (filename.toUpperCase().endsWith(".PCX"))
        {
            load_image = null;
            pcx_image = new PCXImage (imagefile);
            if (pcx_image.isImageLoaded ())
            {
                // add image to label
                width = pcx_image.getWidth ();
                height = pcx_image.getHeight ();
                label.setIcon (new ImageIcon (pcx_image.getImage ()));
            }
            else
            {
                label.setIcon (null);
                pcx_image = null;
                throw new ImageViewerException (pcx_image.getErrorMessage ());
            }
        }
        else
        {
            pcx_image = null;
            load_image = new ImageIcon (filename);
            width = load_image.getIconWidth ();
            height = load_image.getIconHeight ();
            if (width <= 0 || height <= 0)
            {
                label.setIcon (null);
                load_image = null;
                throw new ImageViewerException ("unable to load image");
            }
            label.setIcon (load_image);
        }
// next line removed because it cuts the bottom off the plot - SMF 7/3/2007
//        this.setPreferredSize(new Dimension (width + x_border, height + y_border));
    }

    /********************************************************************
     * This method draws on a printer in the same way as paintComponent
     * draws on a components
     *
     * @param g - the graphics context to use
     * @param pageFormat -
     * @param pageIndex -
     * @return Printable.NO_SUCH_PAGE on error, Printable.PAGE_EXISTS on
     *         success
     ********************************************************************/
    public int print (Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException
    {
        Graphics2D g2d;
        g2d = (Graphics2D) g;
        double xscale, yscale, scale;
    
        if (pageIndex != 0) return Printable.NO_SUCH_PAGE;

        // translate size into graphics coordinate system
        g2d.translate ((int)pageFormat.getImageableX (), (int)pageFormat.getImageableY ());

        // calculate scale factors and then find the minimum - this is because
        // we want the same scale factor to be used for x and y
        if (pcx_image != null)
        {
            xscale = pageFormat.getImageableWidth () / pcx_image.getWidth ();
            yscale = pageFormat.getImageableHeight () / pcx_image.getHeight ();
        }
        else if (load_image != null)
        {
            xscale = pageFormat.getImageableWidth () / load_image.getIconWidth ();
            yscale = pageFormat.getImageableHeight () / load_image.getIconHeight ();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No image has been loaded - there is nothing to print");
            return Printable.NO_SUCH_PAGE;
        }
        if (xscale <= yscale) scale = xscale;
        else scale = yscale;
        g2d.scale (scale, scale);

        // errors are handled by throwing an exception with a message
        try
        {
            this.paint (g2d);
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
