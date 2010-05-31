/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Magnetogram;

import bgs.geophys.library.GeomagneticInformationNode.ConfigFileException;
import bgs.geophys.library.GeomagneticInformationNode.GINDataException;
import bgs.geophys.library.GeomagneticInformationNode.LagTimeData;
import bgs.geophys.library.GeomagneticInformationNode.ParameterException;
import bgs.geophys.library.GeomagneticInformationNode.StationDetails;
import bgs.geophys.library.Misc.DateUtils;
import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

/**
 * Create a chart displaying lag times as a real-time series for
 * one or more observatories
 * 
 * @author smf
 */
public class LagTimeChart 
{
    
    /** codes to indicate production observatories, test observatories
     * and all observatories */
    public enum ObservatorySet {PRODUCTION, TEST, ALL}
    
    private Date start_date;
    private Date end_date;
    private int sample_rate;
    private long threshold;
    private String lineTitle;
    private String barTitle;
    private String lineSubTitle;
    private String barSubTitle;
    private String xLineTitle;
    private String xBarTitle;
    private String yLineTitle;
    private String yBarTitle;
    private Vector<LagTimeData> data_array;
    private DateFormat title_date_format;
    
    /** create an empty lag time chart - stations must be added
     * latter
     * @param start_date the earliest lag time sample
     * @param end_date the latest lag time sample
     * @param sample_rate the sample rate in samples per day - 1440 or 86400
     * @param threshold lag time values larger than the threshold will
     *        be set to the threshold value - this allows a limit to
     *        the lag time size displayed in a graph, thereby preventing
     *        it from autoscaling and swamping other stations - values is
     *        given in minutes - set negative to turn off */
    public LagTimeChart (Date start_date, Date end_date, int sample_rate,
                         long threshold)
    {
        String date_title;
        
        this.start_date = start_date;
        this.end_date = end_date;
        this.sample_rate = sample_rate;
        this.threshold = threshold;
        
        // create titles
        title_date_format = DateFormat.getDateInstance(DateFormat.MEDIUM);
        title_date_format.setTimeZone(DateUtils.gmtTimeZone);
        date_title = title_date_format.format(start_date) + 
                     " to " + title_date_format.format(end_date);
        switch (sample_rate)
        {
            case 1440:
                lineTitle = "'Minute' data lag time " + date_title;
                barTitle = "Average 'minute' data lag time " + date_title;
                break;
            case 86400:
                lineTitle = "'Second' data lag time " + date_title;
                barTitle = "Average 'second' data lag time " + date_title;
                break;
            default:
                lineTitle = "Lag time for data at " + Float.toString (86400.0f / (float) sample_rate) + "Hz " + date_title;
                barTitle = "Average lag time for data at " + Float.toString (86400.0f / (float) sample_rate) + "Hz " + date_title;
                break;
        }
        if (threshold > 0)
        {
            lineSubTitle = "Lag times larger than " + Long.toString(threshold) + " will be displayed as " + Long.toString (threshold);
            barSubTitle = "Lag times larger than " + Long.toString(threshold) + " will be displayed as " + Long.toString (threshold);
        }
        else
        {
            lineSubTitle = null;
            barSubTitle = null;
        }
        xLineTitle = "Date";
        xBarTitle = "Station";
        yLineTitle = "Lag time (minutes)";
        yBarTitle = "Lag time (minutes)";
        
        data_array = new Vector<LagTimeData> ();
    }
    
    /** add all observatories to the chart
     * @param set all production, test or really all observatories */
    public void addAllObservatories (ObservatorySet set)
    {
        int count;
        boolean add_station;
        StationDetails station_details_list;
        StationDetails.StationDetailsFields station_details;
        
        try 
        {
            station_details_list = new StationDetails(StationDetails.ORDER_BY_STATION_CODE);
            for (count=0; count<station_details_list.getNRows(); count++)
            {
                station_details = station_details_list.getRow(count);
                add_station = false;
                switch (set)
                {
                    case ALL:
                        add_station = true;
                        break;
                    case PRODUCTION:
                        if (! station_details.test) add_station = true;
                        break;
                    case TEST:
                        if (station_details.test) add_station = true;
                        break;
                }
                if (add_station)
                    addObservatory(station_details.station_code);
            }
        }
        catch (ConfigFileException ex) { }
        
    }

    /** add the data from a station to the chart
     * @param station_code the station code */
    public void addObservatory (String obsy_code) 
    {
        LagTimeData data;

        try
        {
            data = new LagTimeData (obsy_code, sample_rate, start_date, end_date);
        }
        catch (GINDataException e) { data = null; }
        catch (ParameterException e) { data = null; }
        catch (ConfigFileException e) { data = null; }
        if (data == null)
            data = new LagTimeData (obsy_code, sample_rate, start_date, end_date, false);
        data_array.add (data);
    }
    
    /** add preloaded data from a station to the chart
     * @param data the preloaded data */
    public void addObservatory (LagTimeData data) 
    {
        data_array.add (data);
    }
    
    /** get the number of observatories being displayed */
    public int getNObservatories ()
    {
        return data_array.size();
    }
    
    /** create a chart showing a lag time line plot for each station
     * @return the chart */
    public JFreeChart createLineChart ()
    {
        int count, count2;
        long cnv, value;
        JFreeChart chart;
        XYPlot plot;
        XYItemRenderer r;
        XYLineAndShapeRenderer renderer;
        DateAxis axis;
        TimeSeriesCollection dataset;
        LagTimeData data;
        TimeSeries time_series;
        LagTimeData.LagTimeSample sample;
        
        cnv = 60000l;   // milliseconds to minutes
        
        // make time series for each of the data elements
        dataset = new TimeSeriesCollection();
        for (count=0; count<data_array.size(); count++)
        {
            data = data_array.get(count);
            time_series = new TimeSeries (makeSeriesTitle (data.getStationCode(), 
                                                           data.getAverageLagTime() / cnv),
                                          Minute.class);
            
            for (count2=0; count2<data.getNSamples(); count2++)
            {
                sample = data.getSample(count2);
                value = sample.getLag() / cnv;
                if (threshold > 0)
                {
                    if (value < 0) value = 0;
                    if (value > threshold) value = threshold;
                }
                time_series.add (new Minute (sample.getDate ()), value);
            }
            dataset.addSeries(time_series);
        }

        // create and configure the chart
        chart = ChartFactory.createTimeSeriesChart (lineTitle, xLineTitle, yLineTitle, dataset, true, true, false);
        if (lineSubTitle != null) chart.addSubtitle(new TextTitle(lineSubTitle));
        chart.setBackgroundPaint(Color.white);
        plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) 
        {
            renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(false);
        }
        axis = (DateAxis) plot.getDomainAxis();
        // axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
        return chart;
    }
    
    /** create a chart showing a lag time bar line for each station on a histogram plot 
     * @return the chart */
    public JFreeChart createBarChart ()
    {
        int count;
        long cnv, min_value, max_value, average_value;
        JFreeChart chart;
        LagTimeData data;
        LagTimeData.LagTimeSample sample;
        DefaultCategoryDataset dataset;
        CategoryPlot plot;
        CategoryAxis axis;
        ValueAxis axis2;
        LayeredBarRenderer renderer;
        
        cnv = 60000l;   // milliseconds to minutes
        
        // make dataset items for each of the data elements
        dataset = new DefaultCategoryDataset();
        for (count=0; count<data_array.size(); count++)
        {
            data = data_array.get(count);
            min_value = data.getMinLagTime() / cnv;
            average_value = data.getAverageLagTime() / cnv;
            max_value = data.getMaxLagTime() / cnv;
            if (threshold > 0)
            {
                if (min_value < 0) min_value = 0;
                if (min_value > threshold) min_value = threshold;
                if (average_value < 0) average_value = 0;
                if (average_value > threshold) average_value = threshold;
                if (max_value < 0) max_value = 0;
                if (max_value > threshold) max_value = threshold;
            }
            dataset.addValue (min_value,     "Minimum", data.getStationCode());
            dataset.addValue (average_value, "Average", data.getStationCode());
            dataset.addValue (max_value,     "Maximum", data.getStationCode());
        }

        // create and configure the chart
        chart = ChartFactory.createBarChart (barTitle, xBarTitle, yBarTitle, dataset, PlotOrientation.HORIZONTAL, true, false, false);
        if (barSubTitle != null) chart.addSubtitle(new TextTitle(barSubTitle));
        chart.setBackgroundPaint(Color.white);
        plot = chart.getCategoryPlot();
        
        // the next four lines make the bars layer on top of each other
        renderer = new LayeredBarRenderer();
        renderer.setDrawBarOutline(false);
        plot.setRenderer(renderer);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);

        axis = plot.getDomainAxis();
        axis2 = plot.getRangeAxis();
        plot.setRangeAxis(0, axis2);
        plot.setRangeAxis(1, axis2);
        plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        return chart;
    }
    
    public String getLineTitle () { return lineTitle; }
    public String getBarTitle () { return barTitle; }    
    public String getLineSubTitle () { return lineSubTitle; }
    public String getBarSubTitle () { return barSubTitle; }    
    public String getXLineTitle () { return xLineTitle; }
    public String getXBarTitle () { return xBarTitle; }
    public String getYLineTitle () { return yLineTitle; }
    public String getYBarTitle () { return yBarTitle; }
    public void setLineTitle (String lineTitle) { this.lineTitle = lineTitle; }
    public void setBarTitle (String barTitle) { this.barTitle = barTitle; }
    public void setLineSubTitle (String lineSubTitle) { this.lineSubTitle = lineSubTitle; }
    public void setBarSubTitle (String barSubTitle) { this.barSubTitle = barSubTitle; }
    public void setXLineTitle (String xLineTitle) { this.xLineTitle = xLineTitle; }
    public void setXBarTitle (String xBarTitle) { this.xBarTitle = xBarTitle; }
    public void setYLineTitle (String yLineTitle) { this.yLineTitle = yLineTitle; }
    public void setYBarTitle (String yBarTitle) { this.yBarTitle = yBarTitle; }
    
    
    private String makeSeriesTitle (String station_code, long average)
    {
        String string;
        
        string = station_code;
        if (average > 0)
            string += ", avg " + Long.toString (average) + " mins";
        else
            string += ", no data";
        return string;
    }
}

