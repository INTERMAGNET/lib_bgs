/*
 * DataPlotDiffPanel.java
 *
 * Created on 11 April 2002, 14:50
 */

/**
 * Panel for plotting 1st difference data
 *
 * @author fmc
 * @version
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Data.DataCache;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.ProgramOptionsDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.DisplayData;
import bgs.geophys.library.Data.GeomagAbsoluteDifference;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Magnetogram.Magnetogram;
import bgs.geophys.library.Magnetogram.MagnetogramTrace;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.*;

import java.util.Date;
import org.jfree.chart.JFreeChart;


public class DataPlotDiffPanel extends DataPlotPanel
{

    /********************************************************************
     * Creates a new empty instance of PlotPanel used for initialisation
     ********************************************************************/
    public DataPlotDiffPanel ()
    {
        // show that there is nothing to plot
        chart = null;
        chart_panel = null;
    }

    /********************************************************************
     * update - override to display difference data
     *
     * @param obsy_name observatory names
     * @param compare_obsy_name comparison observatory name (may be null)
     * @param year displayed date
     * @param month displayed date
     * @param day displayed date
     * @param data_list - the data to display
     * @param time_period - one of the codes from DataCache
     ********************************************************************/
    @Override
    public void update (String obsy_name, String compare_obsy_name,
                        int year, int month, int day,
                        DisplayData data_list, int time_period)
    {
        int count;
        long sample_period;
        String trace_title, units;
        double range;
        Magnetogram magnetogram;
        MagnetogramTrace trace;
        GeomagAbsoluteDifference data [];
        GeomagAbsoluteStats stats, diff_stats;
        DecimalFormat formatter;
        
        // draw the magnetogram
        makeTitle ("Difference Viewer: ", obsy_name, compare_obsy_name, false, year, month, day, time_period);
        magnetogram = new Magnetogram (getTitle());
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
        
        stats = data_list.getStats();
        data = stats.getFirstDifference();
        diff_stats = stats.getFirstDifferenceStats();
        for (count=0; count<data_list.getNTraces(); count++)
        {
            range = diff_stats.getRange().getComponent (data_list.getTraceCompCode(count), GeomagAbsoluteValue.ANGLE_MINUTES);
            units = data_list.getUnits(count, true);
            formatter = data_list.getFormatter(count);
            if (stats.getNDataPoints(data_list.getTraceCompCode(count)) <= 0)
                trace_title = data_list.getTraceCompName(count, true) + ", no data available";
            else
                trace_title = data_list.getTraceCompName(count, true) +
                              ", range " + formatter.format (range) + units;
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

}

