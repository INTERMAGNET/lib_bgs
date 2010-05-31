/*
 * Magnetogram.java
 *
 * Created on 03 December 2006, 11:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Magnetogram;

import bgs.geophys.library.Swing.SwingUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;

/**
 * To use this object:
 *  1.) Instanitiate a copy
 *  2.) Create some MagntogramTraces and add them
 *  3.) Call one of the createPlot methods
 * @author smf
 */
public class Magnetogram 
implements AxisChangeListener
{

    /** F type code (for axis title): F type not known */
    public static final int F_TYPE_NONE = 0;
    /** F type code (for axis title): F calculated from vector data */
    public static final int F_TYPE_VECTOR = 1;
    /** F type code (for axis title): F measured */
    public static final int F_TYPE_SCALAR = 2;
    /** F type code (for axis title): Difference between scalar and vector */
    public static final int F_TYPE_DIFFERENCE = 3;
    
    /** enumeration of the possible types of trace marker */
    public enum TraceMarkerType { OFF, ON, ON_BELOW }
    
    private String title;                   // main title
    private String time_axis_title;         // title for time axis
    private ArrayList<String> subtitles;    // array of subtitles (placed below main title)
    private TimeZone gmt;                   // everything will be plotted in UTC
    private Locale locale;
    private TraceMarkerType trace_marker_type;
    private long trace_marker_duration;
    private float font_size_multiplier;
        
    private ArrayList<MagnetogramTrace> trace_list;   // list of time series objects to be plotted
    
    /** Creates a new instance of Magnetogram
     * @param title the main title for the magnetogram */
    public Magnetogram (String title) 
    {
        this.title = title;
        this.subtitles = new ArrayList<String> ();
        trace_list = new ArrayList<MagnetogramTrace> ();
        gmt = TimeZone.getTimeZone("gmt");
        locale = Locale.UK;
        time_axis_title = null;
        trace_marker_type = TraceMarkerType.OFF;
        font_size_multiplier = 1.0f;
    }

    /** add a trace to the magnetogram */
    public void addTrace (MagnetogramTrace trace)
    {
        trace.setFontSizeMultiplier(font_size_multiplier);
        trace_list.add (trace);
    }
    
    public void setTimeAxisTitle (String title)
    {
        this.time_axis_title = title;
    }
    
    /** set the conditions under which the chart will display markers
     * at the locations of each of the data points
     * @param trace_marker_type code showing wether traces are on, off
     *        or automatically switched on when the displayed length is
     *        below a given threshold
     * @param trace_marker_duration the length threshold for
     *        trace_marker_type == ON_BELOW */
    public void setTraceMarkers (TraceMarkerType trace_marker_type,
                                 long trace_marker_duration)
    {
        this.trace_marker_type = trace_marker_type;
        this.trace_marker_duration = trace_marker_duration;
    }
    
    /** set the size of the font, relative to the default font size
     * @param font_size_multiplier_percent the amount to multiply by, as a percentage */
    public void setFontSizeMultiplier (int font_size_multiplier_percent)
    {
        setFontSizeMultiplier ((float) font_size_multiplier_percent / 100.0f);
    }
    
    /** set the size of the font, relative to the default font size
     * @param font_size_multiplier the amount to multiply by, as a percentage */
    public void setFontSizeMultiplier (float font_size_multiplier)
    {
        int count;
        Iterator<MagnetogramTrace> iterator;
        
        this.font_size_multiplier = font_size_multiplier;
        
        for (iterator = trace_list.iterator(), count = 0; 
            iterator.hasNext(); count ++)
            iterator.next().setFontSizeMultiplier(font_size_multiplier);
    }
    
    /** create the magnetogram - unless you
     * want the chart object, don't use this method, use one of the createPlot
     * methods insted.
     * @param has_time_axis true for a time axis
     * @return the chart object */
    public JFreeChart makeChart (boolean has_time_axis)
    {
        int count;
        long duration_ms;
        String axis_title, start_date_string, end_date_string;
        SimpleDateFormat date_format, doy_format;
        CombinedDomainXYPlot plot;
        XYItemRenderer renderer;
        NumberAxis range_axis;
        JFreeChart chart;
        DateAxis time_axis;
        Iterator<MagnetogramTrace> iterator;
        MagnetogramTrace trace;
        Date start_date, end_date, last_sample_date;
        TextTitle text_title;

        // create the time axis - calculate the span of all the time series
        start_date = end_date = null;
        for (iterator = trace_list.iterator(), count = 0; 
             iterator.hasNext(); count ++)
        {
            trace = iterator.next();
            if (count == 0)
            {
                start_date = trace.getStartDate();
                end_date = new Date (trace.getStartDate().getTime() + trace.getDataDuration());
            }
            else
            {
                if (trace.getStartDate().getTime() < start_date.getTime())
                    start_date = trace.getStartDate ();
                if (trace.getStartDate().getTime() + trace.getDataDuration() > end_date.getTime())
                    end_date = new Date (trace.getStartDate().getTime() + trace.getDataDuration());
            }
        }
        
        // check for no traces
        if (start_date == null || end_date == null)
        {
            time_axis = new DateAxis ("No data", gmt, locale);
            duration_ms = 0l;
        }
        else
        {
            duration_ms = end_date.getTime() - start_date.getTime();
            doy_format = new SimpleDateFormat ("DDD");
            doy_format.setTimeZone(gmt);
            last_sample_date = new Date (end_date.getTime() -1);

            if (this.time_axis_title != null)
                axis_title = this.time_axis_title;
            else if (duration_ms < 300000l)
            {
                date_format = new SimpleDateFormat ("dd-MMM-yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date) + 
                                    " (day number " + doy_format.format (start_date) +")";
                end_date_string = date_format.format (last_sample_date) + 
                                  " (day number " + doy_format.format (last_sample_date) +")";
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Time in seconds (UTC) for " + start_date_string;
                else
                    axis_title = "Time in seconds (UTC) from " + start_date_string + " to " + end_date_string;
            }
            else if (duration_ms < 7200000l)
            {
                date_format = new SimpleDateFormat ("dd-MMM-yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date) + 
                                    " (day number " + doy_format.format (start_date) +")";
                end_date_string = date_format.format (last_sample_date) + 
                                  " (day number " + doy_format.format (last_sample_date) +")";
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Time in minutes (UTC) for " + start_date_string;
                else
                    axis_title = "Time in minutes (UTC) from " + start_date_string + " to " + end_date_string;
            }
            else if (duration_ms < 192800000l)
            {
                date_format = new SimpleDateFormat ("dd-MMM-yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date) + 
                                    " (day number " + doy_format.format (start_date) +")";
                end_date_string = date_format.format (last_sample_date) + 
                                  " (day number " + doy_format.format (last_sample_date) +")";
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Time in hours (UTC) for " + start_date_string;
                else
                    axis_title = "Time in hours (UTC) from " + start_date_string + " to " + end_date_string;
            }
            else if (duration_ms < 5400000000l)
            {
                date_format = new SimpleDateFormat ("dd-MMM-yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date) + 
                                    " (day number " + doy_format.format (start_date) +")";
                end_date_string = date_format.format (last_sample_date) + 
                                  " (day number " + doy_format.format (last_sample_date) +")";
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Days";
                else
                    axis_title = "Days from " + start_date_string + " to " + end_date_string;
            }
            else if (duration_ms < 63158400000l)
            {
                date_format = new SimpleDateFormat ("MMM-yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date);
                end_date_string = date_format.format (last_sample_date);
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Months";
                else
                    axis_title = "Months from " + start_date_string + " to " + end_date_string;
            }
            else
            {
                date_format = new SimpleDateFormat ("yyyy");
                date_format.setTimeZone(gmt);
                start_date_string = date_format.format (start_date);
                end_date_string = date_format.format (last_sample_date);
                if (start_date_string.equalsIgnoreCase(end_date_string))
                    axis_title = "Years";
                else
                    axis_title = "Years from " + start_date_string + " to " + end_date_string;
            }
            
     
            time_axis = new DateAxis (axis_title, gmt, locale);
            time_axis.setAutoRange(true);
            time_axis.setLowerMargin(0.0);   // force the axis to stop at the same place as the data
            time_axis.setUpperMargin(0.0);
            time_axis.setTickMarksVisible(true);
            time_axis.addChangeListener(this);
        
            if (! has_time_axis)
                time_axis.setVisible(false);
        }
        if (font_size_multiplier != 1.0f)
        {
            time_axis.setLabelFont (sizeFont (DateAxis.DEFAULT_AXIS_LABEL_FONT));
            time_axis.setTickLabelFont (sizeFont (DateAxis.DEFAULT_TICK_LABEL_FONT));
        }
                
        // create the main plot
        plot = new CombinedDomainXYPlot(time_axis);
        plot.setGap(4.0);   // the gap between the trace panels
        plot.setOrientation(PlotOrientation.VERTICAL);
        
        // add the traces
        for (iterator = trace_list.iterator(), count = 0; 
            iterator.hasNext(); count ++)
        {
            trace = (MagnetogramTrace) iterator.next();
            switch (trace_marker_type)
            {
                case OFF:
                    trace.setShowMarkers(false);
                    break;
                case ON:
                    trace.setShowMarkers(true);
                    break;
                case ON_BELOW:
                    if (duration_ms < trace_marker_duration)
                        trace.setShowMarkers(true);
                    else
                        trace.setShowMarkers(false);
            }
//            trace.getPlot().configureRangeAxes();
            plot.add (trace.getPlot (), 1);
        }
        
//      AxisSpace axisSpace = new AxisSpace();
//      axisSpace.setTop(0.1);
//      axisSpace.setBottom(0.1);
//      plot.setFixedRangeAxisSpace(axisSpace); 
    

        // create a new chart containing the overlaid plot and no legend...
        chart = new JFreeChart (title, 
                                sizeFont (JFreeChart.DEFAULT_TITLE_FONT),
                                plot, false);
        for (count=0; count<subtitles.size(); count++)
        {
            text_title = new TextTitle (subtitles.get(count));
            text_title.setFont (sizeFont (TextTitle.DEFAULT_FONT));
            chart.addSubtitle (text_title);
        }
        chart.setBorderVisible (true);
        
        return chart;
    }
    
    /** draw a previously created magnetogram 
     * @param chart a magnetogram previously created with makeChart()
     * @param graphics the object to draw on
     * @param rectangle the area to draw in */
    public static void drawChart (JFreeChart chart, Graphics2D graphics, Rectangle2D rectangle)
    {
        chart.draw (graphics, rectangle);
    }
    
    /** create the magnetogram 
     * @param has_time_axis true for a time axis
     * @param graphics the object to draw on
     * @param rectangle the area to draw in */
    public void createPlot (boolean has_time_axis, Graphics2D graphics, Rectangle2D rectangle)
    {
        drawChart (makeChart (has_time_axis), graphics, rectangle);
    }
    
    /** draw a previously created magnetogram 
     * @param chart a magnetogram previously created with makeChart()
     * @param x_size size of the plot in pixels, x direction
     * @param y_size size of the plot in pixels, y direction
     * @return a buffered image containing the magnetogram */
    public static BufferedImage drawChart (JFreeChart chart, int x_size, int y_size)
    {
        return chart.createBufferedImage (x_size, y_size);
    }
    
    /** create the magnetogram
     * @param has_time_axis true for a time axis
     * @param x_size size of the plot in pixels, x direction
     * @param y_size size of the plot in pixels, y direction
     * @return a buffered image containing the magnetogram */
    public BufferedImage createPlot (boolean has_time_axis, int x_size, int y_size)
    {
        return drawChart (makeChart (has_time_axis), x_size, y_size);
    }
    
    /** draw a previously created magnetogram 
     * @param chart a magnetogram previously created with makeChart()
     * @param x_size size of the plot in pixels, x direction
     * @param y_size size of the plot in pixels, y direction
     * @param format the format for the image to be written in - as at Java 1.5
     *        the options are "png" or "jpg"
     * @param out the stream to write the data to
     * @throws IOException if there was an IO error */
    public static void drawChart (JFreeChart chart, int x_size, int y_size,
                                  String format, OutputStream out)
    throws IOException
    {
        if (! SwingUtils.writeImage (drawChart (chart, x_size, y_size), format, out))
            throw new IOException ("Image format not supported: " + format);
    }
    
    /** create the magnetogram
     * @param has_time_axis true for a time axis
     * @param x_size size of the plot in pixels, x direction
     * @param y_size size of the plot in pixels, y direction
     * @param format the format for the image to be written in - as at Java 1.5
     *        the options are "png" or "jpg"
     * @param out the stream to write the data to
     * @throws IOException if there was an IO error */
    public void createPlot (boolean has_time_axis, int x_size, int y_size, 
                            String format, OutputStream out)
    throws IOException
    {
        drawChart (makeChart(has_time_axis), x_size, y_size, format, out);
    }
    
    /** draw a previously created magnetogram as a PDF
     * @param chart a magnetogram previously created with makeChart()
     * @param author the PDF author metadata
     * @param subject the PDF subject metadata
     * @param x_size size of the plot in centimetres, x direction
     * @param y_size size of the plot in centimetres, y direction
     * @param out the stream to write the PDF to
     * @throws IOException if there was an IO error */
    public static void drawChart (JFreeChart chart, String author, String subject,
                                  double x_size, double y_size, OutputStream out) 
    throws IOException
    {
        com.lowagie.text.Rectangle pagesize;
        Document document;
        PdfWriter pdfWriter;
        PdfContentByte pdfContentByte;
        PdfTemplate pdfTemplate;
        Graphics2D g2;
        Rectangle2D r2D;
        int width, height;

        try
        {
            // all measurement units in the PDF library are in points (1/72 inch)
            // http://itextdocs.lowagie.com/tutorial/general/faq/index.html#measurements
            width = (int) ((x_size / 2.54) * 72.0);
            height =(int) ((y_size / 2.54) * 72.0);
            pagesize = new com.lowagie.text.Rectangle(width, height);
            document = new Document(pagesize, 50, 50, 50, 50);
            pdfWriter = PdfWriter.getInstance(document, out);
            document.addAuthor(author);
            document.addSubject(subject);
            document.open();
            pdfContentByte = pdfWriter.getDirectContent();
            pdfTemplate = pdfContentByte.createTemplate(width, height);
            g2 = pdfTemplate.createGraphics(width, height, new DefaultFontMapper ());
            r2D = new Rectangle2D.Double(0, 0, width, height);
            drawChart(chart, g2, r2D);
            g2.dispose();
            pdfContentByte.addTemplate(pdfTemplate, 0, 0);
            document.close();
        }
        catch (DocumentException e)
        {
            throw new IOException ("Document error");
        }
    }

    /** create the magnetogram as an INTERMAGNET PDF
     * @param has_time_axis true for a time axis
     * @param x_size size of the plot in centimetres, x direction
     * @param y_size size of the plot in centimetres, y direction
     * @param out the stream to write the PDF to
     * @throws IOException if there was an IO error */
    public void createPlot (boolean has_time_axis,
                            double x_size, double y_size, OutputStream out)
    throws IOException
    {
        String subject;
        
        if (title == null) subject = "Magnetogram";
        else if (title.length() <= 0) subject = "Magnetogram";
        else subject = title;
        drawChart (makeChart(has_time_axis), "INTERMAGNET", subject, x_size, y_size, out);
    }
    
    /** create the magnetogram as a PDF
     * @param has_time_axis true for a time axis
     * @param author the PDF author metadata
     * @param subject the PDF subject metadata
     * @param x_size size of the plot in centimetres, x direction
     * @param y_size size of the plot in centimetres, y direction
     * @param out the stream to write the PDF to
     * @throws IOException if there was an IO error */
    public void createPlot (boolean has_time_axis, String author, String subject,
                            double x_size, double y_size, OutputStream out)
    throws IOException
    {
        drawChart (makeChart(has_time_axis), author, subject, x_size, y_size, out);
    }
    
    public String getTitle() { return title;  }
    public void setTitle(String title) { this.title = title; }
    
    public int getNSubtitles () { return subtitles.size(); }
    public String getSubtitle (int index) { return subtitles.get (index); }
    public void addSubtitle (String subtitle) { subtitles.add (subtitle); }
    public String removeSubtitle (int index)
    {
        if (index < 0) return null;
        if (index >= subtitles.size()) return null;
        return subtitles.remove(index);
    }
        
    /** find the number of traces */
    public int getNTraces () { return trace_list.size(); }
    
    /** routine to generate a title string from an orientation code
     * @param componentCode one of the standard geomagnetic component codes
     * @param magnetic_units string describing the physical units for mangetic field values
     * @param angle_units string describing the physical units for mangetic angles
     * @param f_type one of the F_TYPE_... codes 
     * @param long_names true for long names, false for short
     * @return the title string */
    public static String createAxisTitle (char componentCode,
                                          String magnetic_units,
                                          String angle_units,
                                          int f_type,
                                          boolean long_names)
    {
        String name;
        
        switch (componentCode)
        {
            case 'H':
            case 'h': 
                if (long_names) name = "Horizontal intensity, H";
                else name = "H";
                break;
            case 'D':
            case 'd':
                if (long_names) name = "Declination, D";
                else name = "D";
                break;
            case 'Z':
            case 'z':
                if (long_names) name = "Vertical intensity, Z";
                else name = "Z";
                break;
            case 'X':
            case 'x':
                if (long_names) name = "Geographic north intensity, X";
                else name = "X";
                break;
            case 'Y':
            case 'y':
                if (long_names) name = "Geographic east intensity, Y";
                else name = "Y";
                break;
            case 'I':
            case 'i':
                if (long_names) name = "Inclination, I";
                else name = "I";
                break;
            case 'F':
            case 'f': 
                if (long_names) name = "Total intensity, F";
                else name = "F";
                break;
            default:
                name = "Unknown";
                break;
        }
        
        switch (componentCode)
        {
            case 'H':
            case 'h':
            case 'Z':
            case 'z':
            case 'X':
            case 'x':
            case 'Y':
            case 'y': return name + " (" + magnetic_units + ")";
            case 'I':
            case 'i':
            case 'D':
            case 'd': return name + " (" + angle_units + ")";
            case 'F':
            case 'f': 
                switch (f_type)
                {
                    case F_TYPE_DIFFERENCE:
                        return name + " (difference - " + magnetic_units + ")";
                    case F_TYPE_SCALAR:
                        return name + " (scalar - " + magnetic_units + ")";
                    case F_TYPE_VECTOR:
                        return name + " (vector - " + magnetic_units + ")";
                }
                return name + " (" + magnetic_units + ")";
        }
        return "Unknown";
    }
    
    /** routine to generate a title string from an orientation code
     * @param componentCode one of the standard geomagnetic component codes
     * @param magnetic_units string describing the physical units for mangetic field values
     * @param angle_units string describing the physical units for mangetic angles
     * @param f_type one of the F_TYPE_... codes 
     * @return the title string */
    public static String createAxisTitle (char componentCode,
                                          String magnetic_units,
                                          String angle_units,
                                          int f_type)
    {
        return createAxisTitle (componentCode, magnetic_units, angle_units, f_type, false);
    }
    
    /** routine to generate a title string from an orientation code
     * using default units ("nT" and "minutes" or arc)
     * @param componentCode one of the standard geomagnetic component codes
     * @param f_type one of the F_TYPE_... codes
     * @return the title string */
    public static String createAxisTitle (char componentCode,
                                          int f_type)
    {
        return createAxisTitle (componentCode, "nt", "minutes", f_type);
    }
    
    /** a colour table used for different trace types
     * @param componentCode one of the standard geomagnetic component codes
     * @param f_type one of the F_TYPE_... codes
     * @return the colour for the trace */
    public static Color getTraceColour (char componentCode,
                                        int f_type)
    {
        switch (componentCode)
        {
            case 'H':
            case 'h': return Color.RED;
            case 'D':
            case 'd': return Color.GREEN;
            case 'Z':
            case 'z': return Color.BLUE;
            case 'X':
            case 'x': return Color.RED;
            case 'Y':
            case 'y': return Color.GREEN;
            case 'I':
            case 'i': return Color.CYAN;
            case 'F':
            case 'f': 
                switch (f_type)
                {
                    case F_TYPE_DIFFERENCE:
                        return Color.YELLOW;
                    case F_TYPE_SCALAR:
                        return Color.ORANGE;
                    case F_TYPE_VECTOR:
                        return Color.PINK;
                }
        }
        return Color.BLACK;
    }
    
    /** test whether a component represents an angle or a field strength
     * @param componentCode the code for the component
     * @return true for an angle, false for a field strength */
    public static boolean isAngle (char componentCode)
    {
        switch (componentCode)
        {
            case 'D':
            case 'd':
            case 'I':
            case 'i': return true;
        }
        return false;
    }    
    
    /** respond the changes in the magneotgram axis - this is used
     * to calculate the number of points in the trace and hence
     * to set/hide markers dynamically
     * @param evt the change event */
    public void axisChanged(AxisChangeEvent evt) 
    {
        ValueAxis axis;
        
        if ((evt.getType() == ChartChangeEventType.GENERAL) && 
            (evt.getAxis() != null) &&
            (trace_marker_type == TraceMarkerType.ON_BELOW))
        {
            if (evt.getAxis() instanceof ValueAxis)
            {
                axis = (ValueAxis) evt.getAxis();
                if (axis.getRange().getLength() < trace_marker_duration)
                    this.setShowMarkers(true);
                else
                    this.setShowMarkers(false);
            }
        }
    }
    
    private void setShowMarkers (boolean show_markers)
    {
        Iterator<MagnetogramTrace> iterator;
        MagnetogramTrace trace;

        for (iterator = trace_list.iterator(); iterator.hasNext(); )
        {
            trace = iterator.next();
            trace.setShowMarkers(show_markers);
        }
    }
    
    private Font sizeFont (Font font)
    {
        return font.deriveFont ((float) font.getSize() * font_size_multiplier);
    }
    
//    public static void writeChartAsPDF(OutputStream out,
//                                       JFreeChart chart,
//                                       int width,
//                                       int height,
//                                       FontMapper mapper) 
//    throws IOException, DocumentException
//    {
//        Rectangle pagesize = new Rectangle(width, height);
//        Document document = new Document(pagesize, 50.0, 50.0, 50.0, 50.0);
//        PdfWriter writer = PdfWriter.getInstance(document, out);
//        document.addAuthor("Edinburgh INTERMAGNET GIN");
//        document.addSubject("Magnetogram");
//        document.open();
//        PdfContentByte cb = writer.getDirectContent();
//        PdfTemplate tp = cb.createTemplate(width, height);
//        Graphics2D g2 = tp.createGraphics(width, height, mapper);
//        Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
//        chart.draw(g2, r2D);
//        g2.dispose();
//        cb.addTemplate(tp, 0, 0);
//        document.close();
//    }
}
