/*
 * MagnetogramTrace.java
 *
 * Created on 21 December 2006, 15:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Magnetogram;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Data.ImagCD.YearMeanIterator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.TextAnchor;

/**
 * A class that holds a single trace of data in a magnetogram (a trace is a 
 * single XY plot showing one component from the magnetic field)
 *
 * @author smf
 */
public class MagnetogramTrace 
{

    /** code for position of trace title */
    public static final int TITLE_POSITION_NONE = 0;
    /** code for position of trace title */
    public static final int TITLE_POSITION_OUTSIDE = 1;
    /** code for position of trace title */
    public static final int TITLE_POSITION_INSIDE_TOP_LEFT = 2;
    /** code for position of trace title */
    public static final int TITLE_POSITION_INSIDE_TOP_RIGHT = 3;
    /** code for position of trace title */
    public static final int TITLE_POSITION_INSIDE_BOTTOM_LEFT = 4;
    /** code for position of trace title */
    public static final int TITLE_POSITION_INSIDE_BOTTOM_RIGHT = 5;
    
    private TimeSeries time_series;
    private TimeSeries time_series2;
    private TimeZone gmt;
    private Date start_date;
    private long data_period;   // constructors must set either data_period or end_date
    private Date end_date;
    private Color trace_colour;
    private int trace_title_pos;
    private int axis_number_precision;
    private double data_min, data_max, data_range;
    private String title;
    private boolean has_range_axis;
    private boolean all_missing;
    private boolean show_markers;
    private String marker_units;
    private XYLineAndShapeRenderer renderer;
    private float font_size_multiplier;

    /** Create a magnetogram trace from an array of numbers
     * @param title the title for this trace 
     * @param start_date the start date for the data
     * @param data_period the period between samples, in milliseconds
     * @param data the data
     * @param missingDataValue number used to represent missing data
     * @param range the range of data to display, -ve for autoscale */
    public MagnetogramTrace (String title, Date start_date, long data_period, 
                             double data [], double missingDataValue,
                             double data_range)
    {
        int count;
        long ms_counter;
        
        gmt = TimeZone.getTimeZone("gmt");
        this.start_date = start_date;
        this.data_period = data_period;
        this.setTraceColour(null);
        if (title == null) this.trace_title_pos = TITLE_POSITION_NONE;
        else this.trace_title_pos = TITLE_POSITION_OUTSIDE;
        this.title = title;
        this.axis_number_precision = 1;
        this.has_range_axis = true;
        this.show_markers = false;
        this.marker_units = "";
        this.renderer = null;
        this.font_size_multiplier = 1.0f;
        
        // create the time series
        time_series = new TimeSeries(title, Millisecond.class);
        time_series2 = null;
        
        // put the data into the time series
        data_min = Double.MAX_VALUE;
        data_max = - Double.MAX_VALUE;
        for (count=0, ms_counter = start_date.getTime(); count<data.length; 
             count++, ms_counter += data_period)
        {
            if (data[count] == missingDataValue)
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         null);
            else
            {
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         new Double(data[count]));
                if (data[count] < data_min) data_min = data[count];
                if (data[count] > data_max) data_max = data[count];
            }
        }
        if (data_min >= data_max) 
        {
            data_range = -1.0;
            all_missing = true;
        }
        else all_missing = false;
        this.data_range = data_range;
        this.end_date = new Date (this.start_date.getTime() + this.getDataDuration());
    }    
    
    /** Create a magnetogram trace from an array geomagnetic data samples
     * @param title the title for this trace 
     * @param start_date the start date for the data
     * @param data_period the period between samples, in milliseconds
     * @param data the data - a collection of GeomagAbsoluteValue objects
     * @param component_code a code from GeomagAbsoluteValue indicating which component to plot
     * @param range the range of data to display, -ve for autoscale */
    public MagnetogramTrace (String title, Date start_date, long data_period, 
                             Collection data, int component_code,
                             double data_range)
    {
        long ms_counter;
        Iterator iterator;
        GeomagAbsoluteValue value;
        double number;
        
        gmt = TimeZone.getTimeZone("gmt");
        this.start_date = start_date;
        this.data_period = data_period;
        this.setTraceColour(null);
        if (title == null) this.trace_title_pos = TITLE_POSITION_NONE;
        else this.trace_title_pos = TITLE_POSITION_OUTSIDE;
        this.title = title;
        this.axis_number_precision = 1;
        this.has_range_axis = true;
        this.show_markers = false;
        this.marker_units = "";
        this.renderer = null;
        this.font_size_multiplier = 1.0f;
        
        // create the time series
        time_series = new TimeSeries(title, Millisecond.class);
        time_series2 = null;
        
        // put the data into the time series
        data_min = Double.MAX_VALUE;
        data_max = - Double.MAX_VALUE;
        for (iterator = data.iterator(), ms_counter = start_date.getTime();
             iterator.hasNext(); ms_counter += data_period)
        {
            value = (GeomagAbsoluteValue) iterator.next();
            number = value.getComponent (component_code, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (number == value.getMissingDataValue())
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         null);
            else
            {
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         new Double(number));
                if (number < data_min) data_min = number;
                if (number > data_max) data_max = number;
            }
        }
        if (data_min >= data_max)
        {
            data_range = -1.0;
            all_missing = true;
        }
        else all_missing = false;
        this.data_range = data_range;
        this.end_date = new Date (this.start_date.getTime() + this.getDataDuration());
    }    
    
    /** Create a magnetogram trace from an array geomagnetic data samples
     * @param title the title for this trace 
     * @param start_date the start date for the data
     * @param data_period the period between samples, in milliseconds
     * @param data the data - an array of GeomagAbsoluteValue objects
     * @param component_code a code from GeomagAbsoluteValue indicating which component to plot
     * @param range the range of data to display, -ve for autoscale */
    public MagnetogramTrace (String title, Date start_date, long data_period, 
                             GeomagAbsoluteValue data [], int component_code,
                             double data_range)
    {
        int count;
        long ms_counter;
        double number;
        double rangeOfValues; //used to check whether range has gone over
                              //180deg  in the D component JE 10.3.10
        
        gmt = TimeZone.getTimeZone("gmt");
        this.start_date = start_date;
        this.data_period = data_period;
        this.setTraceColour(null);
        if (title == null) this.trace_title_pos = TITLE_POSITION_NONE;
        else this.trace_title_pos = TITLE_POSITION_OUTSIDE;
        this.title = title;
        this.axis_number_precision = 1;
        this.has_range_axis = true;
        this.show_markers = false;
        this.marker_units = "";
        this.renderer = null;
        this.font_size_multiplier = 1.0f;
        
        // create the time series
        time_series = new TimeSeries(title, Millisecond.class);
        time_series2 = null;
        
        // put the data into the time series
        data_min = Double.MAX_VALUE;
        data_max = - Double.MAX_VALUE;
        for (count=0, ms_counter = start_date.getTime(); 
             count<data.length; 
             count++, ms_counter += data_period)
        {
            number = data[count].getComponent (component_code, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (number == data[count].getMissingDataValue())
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         null);
            else
            {
                time_series.addOrUpdate (new Millisecond(new Date (ms_counter), gmt),
                                         new Double(number));
                if (number < data_min) data_min = number;
                if (number > data_max) data_max = number;
            }
        }
        if (data_min >= data_max) 
        {
            data_range = -1.0;
            all_missing = true;
        }
        else all_missing = false;
        this.data_range = data_range;
        rangeOfValues = data_max - data_min;
        if(GeomagAbsoluteValue.COMPONENT_D == component_code && rangeOfValues>=180*60){
//            System.out.println("Need to reconcile data for flippage..."+this.title);
            reconcileAngles(component_code);  // this method adds 360 to negative angles for display purposes
//                                // it should only be called if the values of D are hovering
                                // around 180 and -180
        }
        this.end_date = new Date (this.start_date.getTime() + this.getDataDuration());
    }    

/*** add 360 to all negative values in a trace
 * only to be used if the D component has a range greater than 180
 * JE 10.3.10
 */
      private void reconcileAngles(int component_code)
      {
          
       Number val;
       double dval;
          if(component_code != GeomagAbsoluteValue.COMPONENT_D) return;
          for(int i=0;i<time_series.getItemCount();i++){
           val = time_series.getValue(i);
           dval = val.doubleValue();
           if(dval<0.0)dval += 360*60;
           time_series.update(i, dval);
          }
      }            
    /** Create a magnetogram trace from an array of annual mean values
     * @param title the title for this trace 
     * @param data the data from a year mean file
     * @param jump_data data, adjusted for jumps, or null for no jump data
     * @param component_code a code from GeomagAbsoluteValue indicating which component to plot
     * @param range the range of data to display, -ve for autoscale */
    public MagnetogramTrace (String title, YearMeanIterator data, 
                             YearMeanIterator jump_data, int component_code,
                             double data_range)
    {
        double number;
        Double plot_value;
        YearMean mean;
        Date date;
        int previousYear=0;
        int currentYear=0;
        int firstYear= 0;
        gmt = TimeZone.getTimeZone("gmt");
        start_date = end_date = null;
        this.data_period = -1;  // period is irregular
        this.setTraceColour(null);
        if (title == null) this.trace_title_pos = TITLE_POSITION_NONE;
        else this.trace_title_pos = TITLE_POSITION_OUTSIDE;
        this.title = title;
        this.axis_number_precision = 1;
        this.has_range_axis = true;
        this.show_markers = false;
        this.marker_units = "";
        this.renderer = null;
        this.font_size_multiplier = 1.0f;
        
        System.out.println("component d is:" +GeomagAbsoluteValue.COMPONENT_D);
        System.out.println("component i is:" +GeomagAbsoluteValue.COMPONENT_I);
        // create the 1st time series
        time_series = new TimeSeries(title, Day.class);
        
        // put the data into the time series
        data_min = Double.MAX_VALUE;
        data_max = - Double.MAX_VALUE;
        data.rewind();
        
        previousYear = (int) data.next().getYear()-1;
        data.rewind();
        while (data.hasNext())
        {
            mean = data.next();
            // if the next year is more than 1 from the last one,
            // need to put in the null value so that completely
            // missing years are not plotted JE 24.07.09
            date = mean.getDate ();
            currentYear = (int) mean.getYear();
            if ((previousYear+1) != currentYear){
             // send a null value to plot for currentYear-1
                time_series.addOrUpdate (time_series.getNextTimePeriod(), null);                
            }
                previousYear = currentYear;
            // JFreeChart cannot handle years earlier than 1900
            if (mean.getYear() >= 1900.0)
            {
                number = mean.getComponent (component_code);
                if (number == YearMean.MISSING_ELEMENT){
                    plot_value = null;
                }
                else
                {
                    plot_value = new Double (number);
                    if (number < data_min) data_min = number;
                    if (number > data_max) data_max = number;
                }
                date = mean.getDate ();
                if (start_date == null)
                    start_date = end_date = date;
                else
                {
                    if (date.before(start_date)) start_date = date;
                    if (date.after(end_date)) end_date = date;
                }
                time_series.addOrUpdate (new Day (date, gmt), plot_value);
            }
        }
        
        // add a trace for the jump data
        if (jump_data == null) time_series2 = null;
        else
        {
            time_series2 = new TimeSeries("", Day.class);
            jump_data.rewind();
            while (jump_data.hasNext())
            {
                mean = jump_data.next();
                // JFreeChart cannot handle years earlier than 1900
                if (mean.getYear() >= 1900.0)
                {
                    number = mean.getComponent (component_code);
                if (number == YearMean.MISSING_ELEMENT){
                    plot_value = null;
                    }
                else
                    {
                        plot_value = new Double (number);
                        if (number < data_min) data_min = number;
                        if (number > data_max) data_max = number;
                    }
                    date = mean.getDate ();
                    if (start_date == null)
                        start_date = end_date = date;
                    else
                    {
                        if (date.before(start_date)) start_date = date;
                        if (date.after(end_date)) end_date = date;
                    }
                    time_series2.addOrUpdate (new Day (date, gmt), plot_value);
                }
            }
        }
                
        // sort out the range
        if (start_date == null)
            start_date = end_date = new Date (0l);
        if (data_min >= data_max) 
        {
            data_range = -1.0;
            all_missing = true;
        }
        else all_missing = false;
        this.data_range = data_range;
         
    }    
   


    /** set the size of the font, relative to the default font size
     * @param font_size_multiplier_percent the amount to multiply by, as a percentage */
    public void setFontSizeMultiplier (int font_size_multiplier_percent)
    {
        font_size_multiplier = (float) font_size_multiplier_percent / 100.0f;
    }
    
    /** set the size of the font, relative to the default font size
     * @param font_size_multiplier the amount to multiply by, as a percentage */
    public void setFontSizeMultiplier (float font_size_multiplier)
    {
        this.font_size_multiplier = font_size_multiplier;
    }
    
    /** get the date/time of the first sample */
    public Date getStartDate () { return start_date; }
    
    /** get the number of samples of data */
    public int getLength () { return time_series.getItemCount(); }
    
    /** get the sample period, in milliseconds 
     * @return the sample period, -ve if the sample period is irregular */
    public long getDataPeriod () { return data_period; }
    
    /** get the duration that this trace covers */
    public long getDataDuration ()
    {
        if (data_period > 0)
            return data_period * (long) getLength ();
        return end_date.getTime() - start_date.getTime();
    }

    public Color getTraceColour() { return trace_colour; }
    public void setTraceColour(Color trace_colour) { this.trace_colour = trace_colour; }
    
    public boolean isAxisDisplayed () { return has_range_axis; }
    public void setAxisDisplayed (boolean has_range_axis) { this.has_range_axis = has_range_axis; }

    public int getTraceTitlePosition () { return trace_title_pos; }
    public void setTraceTitlePosition (int trace_title_pos) 
    {
        if (title != null) this.trace_title_pos = trace_title_pos; 
    }
    
    public int getAxisNumberPrecision () { return axis_number_precision; }
    public void setAxisNumberPrecision (int axis_number_precision) { this.axis_number_precision = axis_number_precision; }
    
    public boolean isShowMarkers () { return show_markers; }
    
    /** this routine is mainly for use by the Magnetogram object to
     * turn markes on and off dynamically - if you call it directly
     * then Magnetogram may override your call. Use Magnetogram.setTraceMarkers()
     * instead
     * @param show_markers true to show markers, false otherwise */
    public void setShowMarkers (boolean show_markers)
    {
        if (this.show_markers != show_markers)
        {
            this.show_markers = show_markers;
            if (renderer != null)
            {
                if (! show_markers)
                    renderer.setSeriesShapesVisible(0, false);
                else
                    renderer.setSeriesShapesVisible(0, true);
            }
        }
    }
    
    public String getMarkerUnits () { return marker_units; }
    public void setMarkerUnits (String marker_units) { this.marker_units = marker_units; }
    
    /** get an object that will plot this trace */
    public XYPlot getPlot ()
    {
        int count;
        float dash_array [];
        double centre;
        XYPlot plot;
        XYTextAnnotation annotation;
        NumberAxis range_axis;
        String number_prec_string;
        TimeSeriesCollection collection;
        BasicStroke stroke;
        
        // create the axis
        if (trace_title_pos == TITLE_POSITION_OUTSIDE)
            range_axis = new NumberAxis(title);
        else
            range_axis = new NumberAxis(null);
        range_axis.setAutoRangeIncludesZero(false);
        if (font_size_multiplier != 1.0f)
        {
            range_axis.setLabelFont (sizeFont (NumberAxis.DEFAULT_AXIS_LABEL_FONT));
            range_axis.setTickLabelFont (sizeFont (NumberAxis.DEFAULT_TICK_LABEL_FONT));
        }
        if (axis_number_precision <= 0)
            number_prec_string = "######";
        else
            number_prec_string = "######.";
        for (count=0; count<axis_number_precision; count++)
            number_prec_string += "0";
        range_axis.setNumberFormatOverride (new DecimalFormat (number_prec_string));
        if (data_range > 0.0)
        {
            centre = data_min + ((data_max - data_min) / 2.0);
            range_axis.setRange (centre - (data_range / 2.0), centre + (data_range / 2.0));
        }
        
        if (! has_range_axis)
            range_axis.setVisible(false);
        
        switch (trace_title_pos)
        {
        case TITLE_POSITION_INSIDE_TOP_LEFT:
            annotation = new XYTextAnnotation ("  " + title,
                                               new Millisecond (start_date).getMiddleMillisecond(),
                                               data_max);
            annotation.setTextAnchor (TextAnchor.TOP_LEFT);
            if (! time_series.isEmpty())
                annotation.setX ((double) time_series.getTimePeriod(0).getStart().getTime());
            if (data_range > 0.0) annotation.setY (range_axis.getUpperBound());
            else if (! all_missing) annotation.setY (data_max);
            else annotation.setY (1.0);
            break;
        case TITLE_POSITION_INSIDE_TOP_RIGHT:
            annotation = new XYTextAnnotation ("  " + title,
                                               new Millisecond (new Date (start_date.getTime() + getDataDuration())).getMiddleMillisecond(),
                                               data_max);
            annotation.setTextAnchor (TextAnchor.TOP_RIGHT);
            if (! time_series.isEmpty())
                annotation.setX ((double) time_series.getTimePeriod(time_series.getItemCount() -1).getStart().getTime());
            if (data_range > 0.0) annotation.setY (range_axis.getUpperBound());
            else if (! all_missing) annotation.setY (data_max);
            else annotation.setY (1.0);
            break;
        case TITLE_POSITION_INSIDE_BOTTOM_LEFT:
            annotation = new XYTextAnnotation ("  " + title,
                                               new Millisecond (start_date).getMiddleMillisecond(),
                                               data_min);
            annotation.setTextAnchor (TextAnchor.BOTTOM_LEFT);
            if (! time_series.isEmpty())
                annotation.setX ((double) time_series.getTimePeriod(0).getStart().getTime());
            if (data_range > 0.0) annotation.setY (range_axis.getLowerBound());
            else if (! all_missing) annotation.setY (data_min);
            else annotation.setY (0.0);
            break;
        case TITLE_POSITION_INSIDE_BOTTOM_RIGHT:
            annotation = new XYTextAnnotation ("  " + title,
                                               new Millisecond (new Date (start_date.getTime() + getDataDuration())).getMiddleMillisecond(),
                                               data_min);
            annotation.setTextAnchor (TextAnchor.BOTTOM_RIGHT);
            if (! time_series.isEmpty())
                annotation.setX ((double) time_series.getTimePeriod(time_series.getItemCount() -1).getStart().getTime());
            if (data_range > 0.0) annotation.setY (range_axis.getLowerBound());
            else if (! all_missing) annotation.setY (data_min);
            else annotation.setY (0.0);
            break;
        default:
            annotation = null;
            break;
        }
        if (annotation != null && font_size_multiplier != 1.0f)
            annotation.setFont (sizeFont (XYTextAnnotation.DEFAULT_FONT));

        // create the renderer and set it's attributes
        renderer = new XYLineAndShapeRenderer ();
        renderer.setSeriesShape (0, new Rectangle2D.Double (-0.5, -4.0, 1.0, 8.0));
        renderer.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator ("{2}" + marker_units, new SimpleDateFormat (""), new DecimalFormat ("#####0.0")));
        renderer.setSeriesShapesFilled(0, true);
        if (! show_markers)
        {
          renderer.setSeriesShapesVisible(0, false);
          renderer.setSeriesShapesVisible(1, false);
        }
        else
        {
          renderer.setSeriesShapesVisible(0, true);
          renderer.setSeriesShapesVisible(1, false);
        }
        dash_array = new float [2];
        dash_array [0] = 2.0f;
        dash_array [1] = 3.0f;
        stroke = new BasicStroke ();
        stroke = new BasicStroke (stroke.getLineWidth(), stroke.getEndCap(),
                                  stroke.getLineJoin(), stroke.getMiterLimit(),
                                  dash_array, 0.0f);
        renderer.setSeriesStroke(1, stroke);
        if (trace_colour != null) 
        {
            renderer.setSeriesPaint(0, trace_colour);
            renderer.setSeriesPaint(1, trace_colour);
        }

        // create the plot
        collection = new TimeSeriesCollection (time_series, gmt);
        if (time_series2 != null)
          collection.addSeries(time_series2);
        plot = new XYPlot (collection, null, range_axis, renderer);
        plot.setRangeAxisLocation (AxisLocation.BOTTOM_OR_LEFT);
        if (annotation != null)
            plot.addAnnotation (annotation);
        
        return plot;
    }
    
    private Font sizeFont (Font font)
    {
        return font.deriveFont ((float) font.getSize() * font_size_multiplier);
    }

}
