/*
 * PlotPanel.java
 *
 * Created on 11 April 2002, 14:50
 */

/**
 * Panel for plotting data
 *
 * @author fmc
 * @version
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.DataCache;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.DisplayData;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.JFreeChart.JFreeChartUtils;
import bgs.geophys.library.Magnetogram.Magnetogram;
import bgs.geophys.library.Magnetogram.MagnetogramTrace;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.*;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


public class DataPlotPanel extends DataViewerPanel 
implements Printable
{

    protected static final long MARKER_NPOINT_THRESHOLD = 100l;
    
    protected JFreeChart chart;
    protected ChartPanel chart_panel;
    private long sample_period;
    private Magnetogram magnetogram;

    /********************************************************************
     * Creates a new empty instance of PlotPanel used for initialisation
     ********************************************************************/
    public DataPlotPanel ()
    {
        // show that there is nothing to plot
        chart = null;
        chart_panel = null;
        sample_period = -1;
    }
    
    /********************************************************************
     * update - update the display with new data and dimensions
     *
     * @param obsy_name observatory name
     * @param compare_obsy_name comparison observatory name (may be null)
     * @param year displayed date
     * @param month displayed date
     * @param day displayed date
     * @param data_list - the data to display
     * @param time_period - one of the codes from DataCache
     ********************************************************************/
    public void update (String obsy_name, String compare_obsy_name,
                        int year, int month, int day,
                        DisplayData data_list, int time_period)
    {
        int count;
        double range, mid_point;
        String trace_title, units;
        MagnetogramTrace trace;
        GeomagAbsoluteValue data [];
        GeomagAbsoluteStats stats;
        DecimalFormat formatter;
        
        // draw the magnetogram
        makeTitle ("Data Viewer: ", obsy_name, compare_obsy_name, false, year, month, day, time_period);
        magnetogram = new Magnetogram (getTitle ());
        magnetogram.setFontSizeMultiplier (ProgramOptionsDialog.getProgramFontSizePercent(true));
        if (data_list.getNDataLoadErrors() > 0) magnetogram.addSubtitle("There were errors reading the data. See the 'File reading errors' tab for details.");
        
        switch (time_period)
        {
        case DataCache.MINUTE_MEANS: sample_period = 60000l; break;
        case DataCache.HOURLY_MEANS: sample_period = 3600000l; break;
        case DataCache.DAILY_MEANS: sample_period = 86400000l; break;
        default: sample_period = 0l; break;
        }
        magnetogram.setTraceMarkers(Magnetogram.TraceMarkerType.ON_BELOW, sample_period * MARKER_NPOINT_THRESHOLD);
        
        data = data_list.getData();
        stats = data_list.getStats();
        for (count=0; count<data_list.getNTraces(); count++)
        {
            range = stats.getRange().getComponent (data_list.getTraceCompCode(count), GeomagAbsoluteValue.ANGLE_MINUTES);
            mid_point = stats.getMidPoint().getComponent (data_list.getTraceCompCode(count), GeomagAbsoluteValue.ANGLE_MINUTES);
            units = data_list.getUnits(count, false);
            formatter = data_list.getFormatter(count);
            if (stats.getNDataPoints(data_list.getTraceCompCode(count)) <= 0)
                trace_title = data_list.getTraceCompName(count, false) + ", no data available";
            else
                trace_title = data_list.getTraceCompName(count, false) + 
                              ", range " + formatter.format (range) + units +
                              ", mid-point " + formatter.format (mid_point) + units;
           
            trace = new MagnetogramTrace (trace_title, data_list.getStartDate(), 
                                          sample_period, data,
                                          data_list.getTraceCompCode(count),
                                          data_list.getTraceRange(count));
            trace.setTraceTitlePosition(MagnetogramTrace.TITLE_POSITION_INSIDE_TOP_LEFT);
            trace.setTraceColour(data_list.getTraceColor(count));
            trace.setMarkerUnits(units);
            magnetogram.addTrace(trace);
        }
            
        chart = magnetogram.makeChart(true);
        if (chart_panel == null)
        {
            chart_panel = createChartPanel (chart);
            this.setLayout (new BorderLayout ());
            this.add (chart_panel, "Center");
        }
        else
            chart_panel.setChart(chart);
            
    }

    protected static ChartPanel createChartPanel (JFreeChart chart)
    {
        ChartPanel cp;
        
        cp = new ChartPanel (chart, true, false, false, true, true);
        cp.setDisplayToolTips(true);
        cp.setMouseZoomable(true);
        cp.setFillZoomRectangle(false);
        return cp;
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
        return JFreeChartUtils.print (chart, g, pageFormat, pageIndex);
    }
    
    /*********************************************************************
     * save the chart to a file
     * @param file the name of the file to save to
     * @param title the subject for a PDF document
     * @param file_type the type of file to save
     * @param x_size the width of the plot
     * @param y_size the height of the plot
     *********************************************************************/
    public void save (File file, String title, String file_type,
                      double x_size, double y_size)
    {
        FileOutputStream out;
        Cursor old_cursor;

        old_cursor = this.getCursor();
        this.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JFreeChartUtils.save (this, chart, file, "INTERMAGNET", title, file_type, x_size, y_size);
        this.setCursor(old_cursor);
    }
    
}

