/*
 * PlotPanel.java
 *
 * Created on 11 April 2002, 14:50
 */

/**
 * Panel for plotting annual mean data. Also plots mean differences
 *
 * @author fmc
 * @version
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.ProgramOptionsDialog;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Data.ImagCD.YearMeanFile;
import bgs.geophys.library.Data.ImagCD.YearMeanFileStats;
import bgs.geophys.library.Data.ImagCD.YearMeanIterator;
import bgs.geophys.library.JFreeChart.JFreeChartUtils;
import bgs.geophys.library.Magnetogram.Magnetogram;
import bgs.geophys.library.Magnetogram.MagnetogramTrace;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import javax.swing.JPopupMenu;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;



public class AnnualMeanPlotPanel extends AnnualMeanViewerPanel 
implements Printable
{

    private JFreeChart chart;
    private ChartPanel chart_panel;
    private boolean show_diffs;

    /********************************************************************
     * Creates a new empty instance of PlotPanel used for initialisation
     ********************************************************************/
    public AnnualMeanPlotPanel (boolean show_diffs)
    {
        this.show_diffs = show_diffs;
        
        // show that there is nothing to plot
        chart = null;
        chart_panel = null;
    }
    
    /********************************************************************
     * update - update the display with new data and dimensions
     *
     * @param obsy_name observatory name
     * @param year the year the annual mean data file was published
     * @param year_mean_file the data to display
     ********************************************************************/
    public void update (String obsy_name, int year, YearMeanFile year_mean_file,
                        AnnualMeanOptions options)
    {
        int count;
        boolean include_incomplete;
        double range, mid_point;
        String start_title;
        YearMeanIterator iterator, jump_iterator;
        YearMeanFileStats stats;
        YearMean.YearMeanType mean_type;
        String trace_title;
        Magnetogram magnetogram;
        MagnetogramTrace trace;
        DecimalFormat formatter;
        AnnualMeanOptions.AnnualMeanTraceOptions trace_options;
        
        // calculate statistics
        switch (options.getDisplayType())
        {
            case ALL_DAYS:
                mean_type = YearMean.YearMeanType.ALL_DAYS;
                include_incomplete = false;
                break;
            case ALL_DAYS_PLUS_INCOMPLETE:
                mean_type = YearMean.YearMeanType.ALL_DAYS;
                include_incomplete = true;
                break;
            case DISTURBED_DAYS:
                mean_type = YearMean.YearMeanType.DISTURBED_DAYS;
                include_incomplete = false;
                break;
            case QUIET_DAYS:
                mean_type = YearMean.YearMeanType.QUIET_DAYS;
                include_incomplete = false;
                break;
            default:
                mean_type = YearMean.YearMeanType.UNKNOWN;
                include_incomplete = false;
                break;
        }
        iterator = year_mean_file.Iterator (mean_type, YearMeanIterator.IncludeJumps.NO_JUMPS, include_incomplete, false);
        stats = new YearMeanFileStats (iterator);
        if (show_diffs)
        {
            iterator = new YearMeanIterator (stats.getFirstDifference());
            jump_iterator = null;
        }
        else if (options.isShowJumps())
            jump_iterator = year_mean_file.Iterator (mean_type, YearMeanIterator.IncludeJumps.APPLY_JUMPS, include_incomplete, true);
        else
            jump_iterator = null;
                
        // draw the magnetogram
        if (show_diffs)
            start_title = "Annual Mean Difference Viewer";
        else
            start_title = "Annual Mean Data Viewer";
        makeTitle (start_title, options, obsy_name, year);
        magnetogram = new Magnetogram (getTitle ());
        magnetogram.addSubtitle(getSubTitle());
        magnetogram.setFontSizeMultiplier (ProgramOptionsDialog.getProgramFontSizePercent(true));
        magnetogram.addSubtitle("Dashed lines show annual means adjusted by jump values");
        if (options.isMarkMeans())
            magnetogram.setTraceMarkers(Magnetogram.TraceMarkerType.ON, 0l);

        // for each component
        formatter = new DecimalFormat ("#####0");
        for (count=0; count<options.getNComponentsShown(); count++)
        {
            trace_options = options.getComponentOptions(count);
            range = stats.getRange().getComponent(trace_options.getComponentCode());
            mid_point = stats.getMidPoint().getComponent(trace_options.getComponentCode());
            if (stats.getNDataPoints (trace_options.getComponentCode()) <= 0)
                trace_title = trace_options.getTraceName(true, show_diffs) + ", no data available";
            else
            {
                trace_title = trace_options.getTraceName(true, show_diffs) + 
                              ", range " + formatter.format (range) + trace_options.getTraceUnits(show_diffs) +
                              ", mid-point " + formatter.format (mid_point) + trace_options.getTraceUnits(show_diffs);
                trace = new MagnetogramTrace (trace_title, iterator, jump_iterator, 
                                              trace_options.getComponentCode(),
                                              trace_options.getRange());
                trace.setTraceTitlePosition(MagnetogramTrace.TITLE_POSITION_INSIDE_TOP_LEFT);
                trace.setTraceColour(trace_options.getTraceColor());
                trace.setMarkerUnits(trace_options.getTraceUnits(show_diffs));
                magnetogram.addTrace(trace);
            }
        }
            
        chart = magnetogram.makeChart(true);
        if (chart_panel == null)
        {
            chart_panel = new ChartPanel (chart, true, false, false, true, true);
            this.setLayout (new BorderLayout ());
            this.add (chart_panel, "Center");
            chart_panel.setDisplayToolTips(true);
            chart_panel.setMouseZoomable(true);
            chart_panel.setFillZoomRectangle(false);
            chart_panel.setZoomAroundAnchor(true);
        }
        else
            chart_panel.setChart(chart);
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
    public int print (Graphics g, PageFormat pageFormat, int pageIndex) 
    throws PrinterException
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

