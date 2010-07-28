/*
 * KIndexPlotPanel.java
 *
 * Created on 10 September 2003, 15:09
 */

/**
 *
 * @author  fmc
 */
package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.*;


public class KIndexPlotPanel extends JPanel implements Printable
{
  // plotting constants
  final static Color backColour = Color.white;
  final static Color foreColour = Color.black;
  final static Color traceColour = Color.blue;
  final static Color axisColour = Color.lightGray;
  final static Color textColour = Color.darkGray;

  private final static int N_MONTHS = 12;
  private final static int MAX_N_DAYS = 31;
  private final static int MAX_K_VALUE = 10;
  private final static int N_K_VALUES = 8;
  private final static int MISSING_K_VALUE = 999;
  private final static String months []  = { "JAN", "FEB", "MAR", "APR",
                                             "MAY", "JUN", "JUL", "AUG",
                                             "SEP", "OCT", "NOV", "DEC" };

  // data storage
  private Integer [] [] k_indices;
  private String title;

  /********************************************************************
   * Creates a new empty instance of KIndexPlotPanel 
   ********************************************************************/
  public KIndexPlotPanel ()
  {
    k_indices = null;
    title = null;
  }

  /********************************************************************
   * update the display with new data
   *
   * @param k - the 2D array of Integer k indices
   * @param title - title for this plot, used for printing
   ********************************************************************/
  public void update (Integer [][] k, String title)
  {
    this.k_indices = k;
    this.title = title;
    // force panel to repaint
    repaint ();
  }

  // Override the paintComponent method in JPanel in order to draw on the panel
  public void paintComponent (Graphics g)
  {
    // Call the paintComponent method in JPanel as a start
    super.paintComponent(g);
    int width, height;

    // get size of panel
    width = this.getWidth ();
    height = this.getHeight ();

    // do drawing
    plotData (g, width, height, false);
  }
  
  /********************************************************************
   * Plot data, either on panel or printer
   *
   * @param g - the graphics context to draw on
   * @param xmax - the width of the drawing area
   * @param ymax - the height of the drawing area
   * @param show_title - if true then draw the title at the top of the
   *                     display - this is used when printing
   ********************************************************************/
  private void plotData (Graphics g, int xmax, int ymax, boolean show_title)
  {
    super.paintComponent (g);
    Graphics2D g2d;
    Font font;
    FontMetrics fontMetrics;
    int linepos, textpos, textheight, tracebase;
    int n, i, k;
    int ticksize = 3;
    int xmin = 0, ymin = 0, plot_xmin = 0, plot_ymin = 0;
    int plot_xmax, plot_ymax;
    float xgap, kgap;
    DecimalFormat markFormat = new DecimalFormat ("00");
    String timeString;

    // To use the graphics class with the higher resolution, convert our
    //  graphics object to the 2D class
    g2d = (Graphics2D) g;

    // Set the panel attributes
    setBackground (backColour);
    setForeground (foreColour);
    g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    font = new Font ("Monospaced", Font.PLAIN, 12);
    g2d.setFont (font);
    fontMetrics = g2d.getFontMetrics();
    textheight = fontMetrics.getHeight ();

    // if a title is required then draw this and calculate new plot area size
    if (show_title)
    {
      ymin = fontMetrics.getHeight ();
      g2d.setPaint (foreColour);
      g2d.drawString (title, 0, ymin - ticksize);
    }

    // find widest month name
    for (n = 0; n < N_MONTHS; n++)
    {
      i = fontMetrics.stringWidth (months [n]) + ticksize;
      if (i > plot_xmin) plot_xmin = i;
    }
    plot_xmin = plot_xmin + xmin + ticksize;
    plot_xmax = xmax;
    plot_ymin = ymin;
    plot_ymax = ymax - textheight - ticksize;

    // draw border
    g2d.setPaint (foreColour);
    g2d.draw (new Rectangle2D.Double (xmin+1, ymin, xmax - xmin - 1, ymax - ymin - 1));

    // draw axes
    // draw day marks and titles
    for (n = 0; n < MAX_N_DAYS; n++)
    {
      // draw tick marks at intervals of 1 day
      g2d.setPaint (axisColour);
      linepos = (n * (plot_xmax - plot_xmin) / MAX_N_DAYS) + plot_xmin;
      g2d.draw (new Line2D.Double (linepos, plot_ymax,
                                   linepos, plot_ymax + ticksize));
      if (n % 5 == 0)
      {
        // draw mark lines every 5 days
        g2d.draw (new Line2D.Double (linepos, plot_ymin,
                                     linepos, plot_ymax));
        if (n == 0)
        {
          // axis title
          g2d.setPaint (textColour);
          g2d.drawString ("DAYS", linepos, ymax - ticksize);
        }
        else
        {
          // mark title
          g2d.setPaint (textColour);
          timeString = new String (markFormat.format (n));
          textpos = linepos - ((plot_xmax - plot_xmin) / (MAX_N_DAYS * 2)) - (fontMetrics.stringWidth (timeString) / 2);
          g2d.drawString (timeString, textpos, ymax - ticksize);
        }
      }
    }
    for (n = 0; n < N_MONTHS; n++)
    {
      // month lines
      g2d.setPaint (axisColour);
      linepos = ((n + 1) * (plot_ymax - plot_ymin) / N_MONTHS) + plot_ymin;
      g2d.draw (new Line2D.Double (plot_xmin, linepos, plot_xmax, linepos));

      // draw tick marks at intervals of 2 on k index scale
      for (i = 2; i <= MAX_K_VALUE; i += 2)
      {
        linepos = (int)((n + (float)i/MAX_K_VALUE) * (plot_ymax - plot_ymin) / N_MONTHS) + plot_ymin;
        g2d.draw (new Line2D.Double (plot_xmin - ticksize, linepos, plot_xmin, linepos));
      }

      // draw month names
      g2d.setPaint (textColour);
      textpos = (int)((n + 0.5) * (plot_ymax - plot_ymin)) / N_MONTHS + (textheight / 2) + plot_ymin;
      g2d.drawString (months [n], ticksize, textpos);
    }

    // plot data
    xgap = (float)(plot_xmax - plot_xmin) / (float)(MAX_N_DAYS * N_K_VALUES);
    kgap = (float)(plot_ymax - plot_ymin) / (float)(N_MONTHS * MAX_K_VALUE);
    for (n = 0; n < N_MONTHS; n++)
    {
      tracebase = ((n + 1) * (plot_ymax - plot_ymin) / N_MONTHS) + plot_ymin;

      g2d.setPaint (traceColour);
      for (i = 0; i < N_K_VALUES * MAX_N_DAYS; i++)
      {
        if ((k_indices [n] [i] != null) && 
            (k_indices [n][i].intValue () != MISSING_K_VALUE))
        {
          // k index is multiplied by 10 in data, divide to get value
          k = k_indices [n] [i].intValue() / 10;
          linepos = (int) (plot_xmin + (i * xgap));
          g2d.draw (new Line2D.Double (linepos, tracebase,
                                       linepos, (int)(tracebase - (k * kgap))));
        }
      }
    }
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
    int x, y, width, height;

    if (pageIndex != 0) return Printable.NO_SUCH_PAGE;
    width = (int) pageFormat.getImageableWidth();
    height = (int) pageFormat.getImageableHeight ();
    // translate size into graphics coordinate system
    x = (int) pageFormat.getImageableX ();
    y = (int) pageFormat.getImageableY ();
    g.translate (x, y);

    // errors are handler by throwing an exception with a message
    try
    {
      plotData (g, width, height, true);
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
