/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.JFreeChart;

import bgs.geophys.library.Magnetogram.Magnetogram;
import bgs.geophys.library.Swing.ImageSaverDialog;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.jfree.chart.JFreeChart;

/**
 * Some utilities when working with JFreeChart
 * 
 * @author smf
 */
public class JFreeChartUtils 
{
    /********************************************************************
     * This method draws on a printer in the same way as paintComponent
     * draws on a component. It is designed as a simple method of printing
     * charts for objects that implement the AWT printable interface.
     * @param chart the chart to print
     * @param g the graphics context to use - from Printable.print
     * @param pageFormat - from Printable.print
     * @param pageIndex - from Printable.print
     * @return Printable.NO_SUCH_PAGE on error, Printable.PAGE_EXISTS on
     *         success
     ********************************************************************/
    public static int print (JFreeChart chart, Graphics g, PageFormat pageFormat, int pageIndex) 
    throws PrinterException
    {
        int x, y, width, height;

        if (pageIndex != 0) return Printable.NO_SUCH_PAGE;
        width = (int) pageFormat.getImageableWidth();
        height = (int) pageFormat.getImageableHeight ();
        
        // translate size into graphics coordinate system
        x = (int) pageFormat.getImageableX ();
        y = (int) pageFormat.getImageableY ();
        g.translate (x, y);

        // errors are handler by throwing an exception with a message
        Magnetogram.drawChart (chart, (Graphics2D) g, new Rectangle (width, height));

        // normal successful return
        return Printable.PAGE_EXISTS;
    }
    
    /*********************************************************************
     * Save a chart to a file
     * @param chart the chart to print
     * @param file the name of the file to save to
     * @param author the author for a PDF document
     * @param title the subject for a PDF document
     * @param file_type the type of file to save
     * @param x_size the width of the plot
     * @param y_size the height of the plot
     *********************************************************************/
    public static void save (JFreeChart chart, File file, String author, String title, 
                             String file_type,
                             double x_size, double y_size)
    throws IOException
    {
        FileOutputStream out;

        out = new FileOutputStream (file);
        if (file_type.equalsIgnoreCase ("pdf"))
            Magnetogram.drawChart (chart, author, title, x_size, y_size, out);
        else
            Magnetogram.drawChart (chart, (int) x_size, (int) y_size, file_type, out);
        
        try { out.close(); }
        catch (IOException e) { }
    }
    
    /** version of save where exceptions are handled by displaying the exception
     * message to the user */
    public static void save (Component dialog_parent,
                             JFreeChart chart, File file, String author, String title, 
                             String file_type,
                             double x_size, double y_size)
    {
        String errmsg;
        
        try
        {
            save (chart, file, author, title, file_type, x_size, y_size);
        }
        catch (IOException e)
        {
            if (e.getMessage() != null)
                errmsg = e.getMessage();
            else
                errmsg = "There was an error wiriting to " + file.getName();
            JOptionPane.showMessageDialog (dialog_parent, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
            file.delete();
        }
    }
    

}
