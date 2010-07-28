/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Data.DataCache;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.DisplayData;
import bgs.geophys.library.Misc.DateUtils;
import javax.swing.JPanel;

/**
 * Abstract class containing methods common to all panels in the data viewer
 * 
 * @author smf
 */
public abstract class DataViewerPanel extends JPanel
{

    private String title = "";
    
    public String getTitle () { return title; }
    
    /** derived objects must implement the same update method */
    public abstract void update (String obsy_name, String compare_obsy_name,
                                 int year, int month, int day,
                                 DisplayData data_list, int time_period);
    
    /** make the title for the plot - the title is put in the 'title' field
     * @param start_title the start of the title
     * @param obsy_name observatory name
     * @param compare_obsy_name comparison observatory name (may be null)
     * @param use_compare_name must be true to use the compare name
     * @param year displayed date
     * @param month displayed date
     * @param day displayed date
     * @param view_type minute, hour or day */
    protected void makeTitle (String start_title, String obsy_name,
                              String compare_obsy_name, boolean use_compare_name,
                              int year, int month, int day, int view_type)
    {
        String join_name;
        
        if (compare_obsy_name == null) join_name = obsy_name;
        else if (! use_compare_name) join_name = obsy_name;
        else join_name = obsy_name + " & " + compare_obsy_name;
        
        switch (view_type)
        {
            case DataCache.MINUTE_MEANS:
                title = start_title + " Minute Mean Data for " +
                        join_name + " " + 
                        (day +1) + " " +
                        DateUtils.getMonthName(month, DateUtils.MONTH_UPPERFIRSTONLY, 3) + " " +
                        year;
                break;
            case DataCache.HOURLY_MEANS: 
                title = start_title + " Hourly Mean Data for " +
                        join_name + " " + 
                        DateUtils.getMonthName(month, DateUtils.MONTH_UPPERFIRSTONLY, 3) + " " +
                        year;
                break;
            case DataCache.DAILY_MEANS: 
                title = start_title + " Daily Mean Data for " +
                        join_name + " " + 
                        year;
                break;
            default:
                title = start_title + " Unknown Type of Data for " +
                        join_name + " " + 
                        (day +1) + " " +
                        DateUtils.getMonthName(month, DateUtils.MONTH_UPPERFIRSTONLY, 3) + " " +
                        year;
                break;
        }
    }
}

