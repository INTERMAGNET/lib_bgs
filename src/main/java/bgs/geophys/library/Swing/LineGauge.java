/*
 * LineGauge.java
 *
 * Created on 25 January 2003, 21:01
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author  Administrator
 * @version 
 */
public class LineGauge extends Gauge
{

  /** minimum width of the gauge */
  public static final int MIN_WIDTH = 200;
  /** minimum height of the gauge */
  public static final int MIN_HEIGHT = 40;

  /** Creates new LineGauge */
  public LineGauge() 
  {
    Dimension dim;

    dim = new Dimension (MIN_WIDTH, MIN_HEIGHT);
    setMinimumSize (dim);
    setPreferredSize (dim);
    setSize (dim);
  }

  /** Creates new Gauge
   * @param width the width of the gauge in pixels
   * @param height the height of the gauge in pixels
   */
  public LineGauge (int width, int height)
  {
    Dimension dim;
    
    dim = new Dimension (MIN_WIDTH, MIN_HEIGHT);
    setMinimumSize (dim);
    if (width < MIN_WIDTH) width = MIN_WIDTH;
    if (height < MIN_HEIGHT) height = MIN_HEIGHT;
    dim = new Dimension (width, height);    
    setPreferredSize (dim);
    setSize (dim);
  }

  /** override the paintComponent method to draw the gauge
   * @param g the graphcs context, which must not be (permanently) modified
   */
  protected void paintComponent (Graphics g)
  {
    int x, y, width_inc, height_inc, rect_width, rect_height, count, range;
    int start_value, value, orig_x, orig_y, x_inc, y_inc, width, height;
    int poly_x [], poly_y [];
    boolean quit;
    String string;
    Dimension size;
    Color old_colour;
    Font old_font, font;
    FontMetrics font_metrics;

    poly_x = new int [3];
    poly_y = new int [3];

    
    // call super class
    super.paintComponent (g);

    // get information
    size = getSize ();
    old_colour = g.getColor ();
    old_font = g.getFont ();
    width_inc = size.width / 20;
    height_inc = size.height / 20;
    range = getMaximumValue () - getMinimumValue ();
    orig_x = width_inc * 2;
    orig_y = height_inc;
    rect_width = size.width - (width_inc * 4);
    rect_height = height_inc * 3;

    // draw the current position
    width = height = 0;
    if (getValueDisplayed ())
    {
      width = ((getValue () - getMinimumValue ()) * (rect_width +1)) / range;
      height = rect_height -1;
      g.setColor (getGaugeFillColour ());
      g.fillRoundRect (orig_x -5, orig_y +1, width +5, height, 5, 5);
    }
    
    // draw the rectangle for the gauge
    g.setColor (getGaugeFrameColour ());
    g.drawRoundRect (orig_x -5, orig_y, rect_width +10, rect_height, 5, 5);

    // draw the current position pointer
    if (getValueDisplayed ())
    {
      poly_x [0] = orig_x + width;
      poly_y [0] = orig_y +1 + height;
      poly_x [1] = poly_x [0] - width_inc;
      poly_x [2] = poly_x [0] + width_inc;
      poly_y [1] = poly_y [2] = poly_y [0] - (height_inc * 4);
      g.setColor (getGaugePointerColour ());
      g.fillPolygon(poly_x, poly_y, 3);
    }
    
    // add the tick marks
    if (getPaintTicks ())
    {
      font = new Font ("SansSerif", Font.BOLD, height_inc * 6);
      g.setFont (font);
      g.setColor (getGaugeFrameColour ());

      // draw the minor tick marks
      start_value = getMinimumValue ();
      count = Math.abs (start_value % getMinorTickSpacing ());
      if (count != 0) start_value += getMinorTickSpacing () - count;
      y = orig_y + rect_height;
      for (value = start_value; value <= getMaximumValue (); value += getMinorTickSpacing ())
      {
        x = orig_x + (((value - getMinimumValue ()) * rect_width) / range);
        g.drawLine (x, y, x, y + height_inc);
      }

      // draw the major tick marks
      start_value = getMinimumValue ();
      count = Math.abs (start_value % getMajorTickSpacing ());
      if (count != 0) start_value += getMajorTickSpacing () - count;
      y = orig_y + rect_height;
      font_metrics = getFontMetrics (font);
      y_inc = font_metrics.getHeight () + (height_inc * 2);
      for (value = start_value; value <= getMaximumValue (); value += getMajorTickSpacing ())
      {
        x = orig_x + (((value - getMinimumValue ()) * rect_width) / range);
        g.setColor (getGaugeFrameColour ());
        g.drawLine (x, y, x, y + height_inc + height_inc);
        string = Integer.toString (value);
        x_inc = font_metrics.stringWidth (string) / 2;
        g.setColor (getGaugeTextColour ());
        g.drawString (string, x - x_inc, y + y_inc);
      }
    }
    
    // restore the graphics context
    g.setColor (old_colour);
    g.setFont (old_font);
  }    

}
