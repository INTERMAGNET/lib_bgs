/*
 * Gauge.java
 *
 * Created on 14 January 2003, 20:45
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
public class CircleGauge extends Gauge
{

  /** minimum width of the gauge */
  public static final int MIN_WIDTH = 50;  
  /** minimum height of the gauge */
  public static final int MIN_HEIGHT = 50;  

  // private members
  private int start_angle;
  private int arc_angle;
  
  /** Creates new Gauge */
  public CircleGauge() 
  {
    Dimension dim;

    dim = new Dimension (MIN_WIDTH, MIN_HEIGHT);
    setMinimumSize (dim);
    setPreferredSize (dim);
    setSize (dim);
    
    start_angle = -90;
    arc_angle = 360;
  }

  /** Creates new Gauge
   * @param width the width of the gauge in pixels
   * @param height the height of the gauge in pixels
   * @param start_angle the starting angle for the gauge circle,
   *        for all angles 0 = 3 o'clock, +ve = anti-clockwize, -ve = clockwise
   * @param arc_angle the size of the arc (359 = full circle) */
  public CircleGauge (int width, int height, int start_angle, int arc_angle)
  {
    Dimension dim;
    
    dim = new Dimension (MIN_WIDTH, MIN_HEIGHT);
    setMinimumSize (dim);
    if (width < MIN_WIDTH) width = MIN_WIDTH;
    if (height < MIN_HEIGHT) height = MIN_HEIGHT;
    dim = new Dimension (width, height);    
    setPreferredSize (dim);
    setSize (dim);
    
    this.start_angle = start_angle;
    this.arc_angle = arc_angle;
  }
  
  /** override the paintComponent method to draw the gauge
   * @param g the graphcs context, which must not be (permanently) modified */
  protected void paintComponent (Graphics g)
  {
    int x, y, width, height, width_inc, height_inc, angle;
    int x1, y1, x2, y2, x3, y3, x_inc, y_inc, x_poly [], y_poly [];
    int start_value, value, count, font_size;
    boolean quit;
    String string;
    Dimension size;
    Color old_colour;
    Font old_font, font;
    FontMetrics font_metrics;
    Arc2D.Float arc_outer_calc, arc_inner_calc;
    LookAndFeel look_and_feel;
    
    x_poly = new int [4];
    y_poly = new int [4];

    
    // call super class
    super.paintComponent (g);

    // get information
    size = getSize ();
    old_colour = g.getColor ();
    old_font = g.getFont ();
    width_inc = size.width / 20;
    height_inc = size.height / 20;

    // set up drawing co-ordinates:
    // arc_outer_calc is an arc making the reference for the major tick mark ends and annotation text
    // arc_inner_calc is an arc on the inside of the tick marks / the outside of the gauge frame
    // x, y, width, height (at the end of the block) are the co-ordinates needed for an arc around
    //                     the outside of the gauge frame
    x = width_inc * 2;
    y = height_inc * 2;
    width = size.width - (width_inc * 4);
    height = size.height - (height_inc * 4);
    arc_outer_calc = new Arc2D.Float ((float) x, (float) y, (float) width, (float) height,
                                      (float) start_angle, (float) 0.0, Arc2D.CHORD);
    x += width_inc * 2;
    y += height_inc * 2;
    width -= width_inc * 4;
    height -= height_inc * 4;
    arc_inner_calc = new Arc2D.Float ((float) x, (float) y, (float) width, (float) height,
                                      (float) start_angle, (float) 0.0, Arc2D.CHORD);

    // draw the gauge tick marks
    if (getPaintTicks())
    {
      font_size = (height_inc * 5) / 4;
      if (font_size < 8) font_size = 8;
      if (font_size > 30) font_size = 30;
      font = new Font ("SansSerif", Font.BOLD, font_size);
      g.setFont (font);
      font_metrics = getFontMetrics (font);
      y_inc = font_metrics.getHeight () / 3;
      
      // draw the minor tick marks
      start_value = getMinimumValue ();
      count = Math.abs (start_value % getMinorTickSpacing ());
      if (count != 0) start_value += getMinorTickSpacing () - count;
      g.setColor (getGaugeFrameColour ());
      for (value = start_value; value <= getMaximumValue (); value += getMinorTickSpacing ())
      {
        // set the arc for this value
        arc_outer_calc.setAngleExtent ((float) this.value2angle (value));
        arc_inner_calc.setAngleExtent ((float) this.value2angle (value));

        // extract the tick mark positions
        x1 = (int) arc_outer_calc.getEndPoint().getX ();
        y1 = (int) arc_outer_calc.getEndPoint().getY ();
        x3 = (int) arc_inner_calc.getEndPoint().getX ();
        y3 = (int) arc_inner_calc.getEndPoint().getY ();
        x2 = x1 + (((x3 - x1) * 7) / 8);
        y2 = y1 + (((y3 - y1) * 7) / 8);
        g.drawLine (x2, y2, x3, y3);
      }

      // draw the major tick marks
      start_value = getMinimumValue ();
      count = Math.abs (start_value % getMajorTickSpacing ());
      if (count != 0) start_value += getMajorTickSpacing () - count;
      for (value = start_value; value <= getMaximumValue (); value += getMajorTickSpacing ())
      {
        // set the arc for this value
        arc_outer_calc.setAngleExtent ((float) this.value2angle (value));
        arc_inner_calc.setAngleExtent ((float) this.value2angle (value));

        // extract the tick mark positions
        x1 = (int) arc_outer_calc.getEndPoint().getX ();
        y1 = (int) arc_outer_calc.getEndPoint().getY ();
        x3 = (int) arc_inner_calc.getEndPoint().getX ();
        y3 = (int) arc_inner_calc.getEndPoint().getY ();
        x2 = x1 + (((x3 - x1) * 5) / 8);
        y2 = y1 + (((y3 - y1) * 5) / 8);
        g.setColor (getGaugeFrameColour ());
        g.drawLine (x2, y2, x3, y3);

        // draw the value at this point - don't redraw the mark if the gauge is a full circle
        if (value < getMaximumValue () || Math.abs (arc_angle) < 355)
        {
          string = Integer.toString (value);
          x_inc = font_metrics.stringWidth (string) / 2;
          g.setColor (getGaugeTextColour ());
          g.drawString (string, x1 - x_inc, y1 + y_inc);
        }
      }
    }

    // draw the gauge frame - a circle
    g.setColor (getGaugeFrameColour ());
    g.fillArc (x, y, width, height, start_angle, arc_angle);
    g.setColor (getBackground ());
    x += width_inc / 2;
    y += height_inc / 2;
    width -= width_inc;
    height -= height_inc;
    g.fillArc (x, y, width, height, start_angle, 360);

    // draw the value as a line from the centre of the circle
    // and an arc around the inside of the gauge frame
    if (getValueDisplayed ())
    {
      angle = value2angle (getValue ());

      // draw the arc
      g.setColor (getGaugeFillColour ());
      g.fillArc (x, y, width, height, start_angle, angle);

      // draw the pointer
      x1 = x_poly [0] = size.width / 2;
      y1 = y_poly [0] = size.height / 2;
      arc_inner_calc.setAngleExtent ((double) angle - 20.0);
      x2 = (int) arc_inner_calc.getEndPoint().getX ();
      y2 = (int) arc_inner_calc.getEndPoint().getY ();
      x_poly [1] = x1 - ((x1 - x2) / 5);
      y_poly [1] = y1 - ((y1 - y2) / 5);
      arc_inner_calc.setAngleExtent ((double) angle);
      x_poly [2] = (int) arc_inner_calc.getEndPoint().getX ();
      y_poly [2] = (int) arc_inner_calc.getEndPoint().getY ();
      arc_inner_calc.setAngleExtent ((double) angle + 20.0);
      x2 = (int) arc_inner_calc.getEndPoint().getX ();
      y2 = (int) arc_inner_calc.getEndPoint().getY ();
      x_poly [3] = x1 - ((x1 - x2) / 5);
      y_poly [3] = y1 - ((y1 - y2) / 5);
      g.setColor (getGaugePointerColour ());
      g.fillPolygon(x_poly, y_poly, 4);      
    }
    
    // restore the graphics context
    g.setColor (old_colour);
    g.setFont (old_font);
  }

  /** convert a gauge value to an angle
   * @param value the value to convert
   * @return the angle */
  private int value2angle (int value)
  {
    int angle;
    
    angle = ((value - getMinimumValue ()) * arc_angle ) / (getMaximumValue () - getMinimumValue ());
    return angle;
  }
  
}
