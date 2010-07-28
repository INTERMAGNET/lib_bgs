/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataMonth;
import bgs.geophys.library.Data.GeomagAbsoluteDifference;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;

/**
 * A class that holds data for displaying. The class holds data in the 
 * form of an array of GeomagAbsoluteValues, then for each trace to be 
 * displayed the class holds:
 * 1.) The code of the element to be plotted - one of the COMPONENT_xxx codes
 *     in GeomagAbsoluteValue
 * 2.) The colour for the trace and the vertical scale to use
 * 
 * @author smf
 */
public class DisplayData 
{

    // a class that holds details on a single trace
    private class DisplayDataTrace
    {
        private int comp_code;
        private Color colour;
        private double range;
        
        public DisplayDataTrace (int comp_code, Color colour, double range)
        {
            this.comp_code = comp_code;
            this.colour = colour;
            this.range = range;
        }
        public int getCompCode () { return comp_code; }
        public Color getColour () { return colour; }
        public double getRange () { return range; }
    }
    
    // private members
    private String time_units;
    private GeomagAbsoluteValue data [];
    private GeomagAbsoluteStats stats;
    private GeomagAbsoluteDifference diff_data [];
    private GeomagAbsoluteStats diff_stats;
    private Date start_date;
    private Vector<DisplayDataTrace> list;
    private Vector<String> data_error_messages;
    private Vector<String> diff_error_messages;
    private Vector<File> month_file_list;
    
    /** create an empty component list
     * @param start_date the start date for the data
     * @param time_units "minute", "hour" or "day"
     * @param data the data that is to be displayed
     * @param stats the statistics associated with the data
     * @param diff_data an optional array containing difference
     *        values between the main data set and a data set from
     *        another observatory for the same date / time 
     * @param diff_stats optional statistics to go with diff_data
     * @param month_data_list list of files used to read the main data set
     * @param diff_month_data_list list of files used to read the comparison data set
     * @param data_error_messages list of error messages from reading the main data
     * @param diff_error_messages list of error messages from reading the comparison data */
    public DisplayData (Date start_date, String time_units, GeomagAbsoluteValue data [], 
                        GeomagAbsoluteStats stats,
                        GeomagAbsoluteDifference diff_data [],
                        GeomagAbsoluteStats diff_stats,
                        Vector<CDDataMonth> month_data_list,
                        Vector<CDDataMonth> diff_month_data_list,
                        Vector<String> data_error_messages,
                        Vector<String> diff_error_messages)
    {
        int count;
        
        this.time_units = time_units;
        this.start_date = start_date;
        this.data = data;
        this.stats = stats;
        this.diff_data = diff_data;
        this.diff_stats = diff_stats;
        
        month_file_list = new Vector<File> ();
        for (count=0; count<month_data_list.size(); count++)
            month_file_list.add (month_data_list.get (count).getMonthFile());
        if (diff_month_data_list != null)
        {
            for (count=0; count<diff_month_data_list.size(); count++)
                month_file_list.add (diff_month_data_list.get (count).getMonthFile());
        }

        if (data_error_messages == null) this.data_error_messages = new Vector<String> ();
        else this.data_error_messages = data_error_messages;
        if (diff_error_messages == null) this.diff_error_messages = new Vector<String> ();
        else this.diff_error_messages = diff_error_messages;
        
        list = new Vector <DisplayDataTrace> ();
    }

    /** get the start date for the data */
    public Date getStartDate () { return start_date; }

    /** get the data that is to be displayed */
    public GeomagAbsoluteValue [] getData () { return data; }

    /** get the statistics associated with the data */
    public GeomagAbsoluteStats getStats () { return stats; }

    /** get the difference data */
    public GeomagAbsoluteDifference [] getDiffData () { return diff_data; }

    /** get the statistics associated with the difference data */
    public GeomagAbsoluteStats getDifferenceStats () { return diff_stats; }

    
    /** there may have been one or more error messages generated
     * when the data was loaded - this routine allows you to
     * access these messages */
    public int getNDataLoadErrors () { return data_error_messages.size(); }
    
    /** there may have been one or more error messages generated
     * when the data was loaded - this routine allows you to
     * access these messages */
    public String getDataLoadError (int index) { return data_error_messages.get (index); }

    /** there may have been one or more error messages generated
     * when the comparison data was loaded - this routine allows you to
     * access these messages */
    public int getNDiffLoadErrors () { return diff_error_messages.size(); }
    
    /** there may have been one or more error messages generated
     * when the comparison data was loaded - this routine allows you to
     * access these messages */
    public String getDiffLoadError (int index) { return diff_error_messages.get (index); }
    
    /** get the month files that this data was loaded from */
    public int getNDataFiles () { return month_file_list.size(); }
    
    /** get the month files that this data was loaded from */
    public File getDataFile (int index) { return month_file_list.get(index); }
    
    /** add a component
     * @param comp_code code for the component to display - one of the 
     *        GeomagAbsoluteValue.COMPONENT_xxx codes
     * @param colour the colour to display the trace in
     * @param range the range of data to display, -ve to autoscale */
    public void addComponent (int comp_code, Color trace_colour, double range)
    {
        list.add (new DisplayDataTrace (comp_code, trace_colour, range));
    }
    
    /** get the number of traces to display */
    public int getNTraces () { return list.size(); }
    
    /** get the code for the component to plot */
    public int getTraceCompCode (int index) { return list.get (index).getCompCode(); }
    
    /** get the colour for a trace */
    public Color getTraceColor (int index) { return list.get (index).getColour(); }

    /** get the range for a trace */
    public double getTraceRange (int index) { return list.get (index).getRange(); }
    
    /** get the name of the trace's component */
    public String getTraceCompName (int index, boolean is_diff)
    {
        if (data.length <= 0) return "Unknown";
        if (is_diff)
            return "d" + data[0].getComponentName (list.get(index).getCompCode()) + "/dt";
        return data[0].getComponentName (list.get(index).getCompCode());
    }
    
    /** get the units that the component is displayed in */
    public String getUnits (int index, boolean is_diff)
    {
        String units;
        
        if (data.length <= 0) units = "unk";
        else if (is_diff) units = data[0].getUnitName (getTraceCompCode(index), GeomagAbsoluteValue.ANGLE_MINUTES, true) + "/" + time_units;
        else units = data[0].getUnitName (getTraceCompCode(index), GeomagAbsoluteValue.ANGLE_MINUTES, true);
        return units;
    }
    
    /** get a formatter for displaying the data values */
    public DecimalFormat getFormatter (int index)
    {
        DecimalFormat formatter;
        
        if (data.length <= 0) formatter = new DecimalFormat ();
        else formatter = data[0].getFormatter(getTraceCompCode(index), GeomagAbsoluteValue.ANGLE_MINUTES);
        return formatter;
    }
    
    /** get the number of rows of data */
    public int getNRows ()
    {
        return data.length;
    }    
}
