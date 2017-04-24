/*
 * Gauge.java
 *
 * Created on 26 January 2003, 09:14
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The base class for Gauges
 *
 * @author  Administrator
 * @version 
 */
public abstract class Gauge extends JComponent 
{

  // private members
  private int max_value;
  private int min_value;
  private int current_value;
  private boolean show_value;
  private int large_tick_interval, small_tick_interval;
  private boolean show_tick_marks;
  
  // colours for various parts of the gauge
  private Color gauge_frame_colour;
  private Color gauge_fill_colour;
  private Color gauge_pointer_colour;
  private Color gauge_text_colour;

  /** Creates new Gauge */
  public Gauge() 
  {
    min_value = current_value = 0;
    max_value = 100;
    show_value = false;
    large_tick_interval = 20;
    small_tick_interval = 5;
    show_tick_marks = true;
  
    gauge_frame_colour = Color.black;
    gauge_fill_colour = Color.green;
    gauge_pointer_colour = Color.red;
    gauge_text_colour = Color.black;
  }

  /** set the maximum / minimum data values for the gauge
   * @param min_value the minimum value
   * @param max_value the maximum value */
  public void setScale (int min_value, int max_value)
  {
    if (max_value <= min_value) max_value = min_value +1;
    this.min_value = min_value;
    this.max_value = max_value;
    if (current_value < min_value) current_value = min_value;
    if (current_value > max_value) current_value = max_value;
    repaint ();
  }

  /** set the current value for the gauge
   * @param current_value the current value */
  public void setValue (int current_value)
  {
    if (current_value < min_value) current_value = min_value;
    if (current_value > max_value) current_value = max_value;
    this.current_value = current_value;
    show_value = true;
    repaint ();
  }

  /** get the maximum value */
  public int getMaximumValue () { return max_value; }
  
  /** get the minimum value */
  public int getMinimumValue () { return min_value; }
  
  /** get the current value */
  public int getValue () { return current_value; }
  
  /** turn the current value off */
  public void noValue ()
  {
    if (show_value != false) repaint ();
    show_value = false;
  }

  /** find out whether the valus is being displayed
   * @return true if the value is being displayed */
  public boolean getValueDisplayed () { return show_value; }
  
  /** turn the tick marks on/off
   * @param on the new setting */
  public void setPaintTicks (boolean on)
  {
    if (show_tick_marks != on) repaint ();
    show_tick_marks = on;
  }

  /** get the tick mark status
   * @return true of tick marks are bieng displayed */
  public boolean getPaintTicks () { return show_tick_marks; }

  /** set the major tick mark interval
    * @param spacing the spacing between major tick marks */
  public void setMajorTickSpacing (int spacing)
  {
    if (spacing != large_tick_interval) repaint ();
    large_tick_interval = spacing;
  }

  /** get the distance between major tick marks
   * @return the distance */
  public int getMajorTickSpacing () { return large_tick_interval; }

  /** set the minor tick mark interval
    * @param spacing the spacing between minor tick marks */
  public void setMinorTickSpacing (int spacing)
  {
    if (spacing != small_tick_interval) repaint ();
    small_tick_interval = spacing;
  }

  /** get the distance between minor tick marks
   * @return the distance */
  public int getMinorTickSpacing () { return small_tick_interval; }

  /** set the gauge frame colour
   * @param colour the new colour */
  public void setGaugeFrameColour (Color colour)
  {
    if (! colour.equals (gauge_frame_colour)) repaint ();
    gauge_frame_colour = colour;
  }

  /** get the gauge frame colour
   * @returns the colour */
  public Color getGaugeFrameColour () { return gauge_frame_colour; }
  
  /** set the gauge fill colour
   * @param colour the new colour */
  public void setGaugeFillColour (Color colour)
  {
    if (! colour.equals (gauge_fill_colour)) repaint ();
    gauge_fill_colour = colour;
  }

  /** get the gauge fill colour
   * @returns the colour */
  public Color getGaugeFillColour () { return gauge_fill_colour; }

  /** set the gauge pointer colour
   * @param colour the new colour */
  public void setGaugePointerColour (Color colour)
  {
    if (! colour.equals (gauge_pointer_colour)) repaint ();
    gauge_pointer_colour = colour;
  }

  /** get the gauge pointer colour
   * @returns the colour */
  public Color getGaugePointerColour () { return gauge_pointer_colour; }

  /** set the gauge text colour
   * @param colour the new colour */
  public void setGaugeTextColour (Color colour)
  {
    if (! colour.equals (gauge_text_colour)) repaint ();
    gauge_text_colour = colour;
  }

  /** get the gauge text colour
   * @returns the colour */
  public Color getGaugeTextColour () { return gauge_text_colour; }
  
}
