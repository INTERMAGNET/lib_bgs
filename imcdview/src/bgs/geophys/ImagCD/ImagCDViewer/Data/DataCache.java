/*
 * DataCache.java
 *
 * Created on 28 February 2007, 16:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * 
 * 11.03.09 extra field start_month added for the index of first month stored, JE 
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.DateUtils;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Cache minute, hourly or daily data for plotting. Minute data is in 24 hour chunks,
 * hourly data in monthly chunks, daily data in yearly chunks.
 * 
 * The 'D conversion' factor used to be calcualted by this object, but is no longer
 * used. The calculation used to be:
 *      If mean (from data) of H available, then D-conv = mean-H / 3438.0
 *      else if D-conv available from file, then D-conv = cached_day_data_array[0].getDConversion();
 *      else (in an emergencgy) D-conv = 5000.0;
 *
 * @author smf
 */
public class DataCache 
{
    
    /** code for data period: minute means */
    public static final int MINUTE_MEANS = 1;
    /** code for data period: hourly means */
    public static final int HOURLY_MEANS = 2;
    /** code for data period: daily means */
    public static final int DAILY_MEANS  = 3;
    
    // private members
    private String obsy_code;                       // code for the observatory for last loaded data
    private String obsy_name;                       // name of the observatory for last loaded data
    private Vector<Integer> comp_list;              // component codes for last loaded data (as array of codes from GeomagAbsoluteValue.COMPONENT_xxx)
    private Vector<CDDataMonth> cached_month_data;  // cached monthly file
    private GeomagAbsoluteValue [] stored_data;     // array of 4 component data as last loaded
    private GeomagAbsoluteStats stored_stats;       // stats on stored data
    private Date start_date;                        // start date of the stored data
    private int start_month;                        // first month of stored data  !added JE 11.3.09
    private Vector<String> error_messages;          // list of load errors
    
    /** Creates a new instance of DataCache */
    public DataCache () 
    {
        cached_month_data = new Vector<CDDataMonth> ();
        error_messages = new Vector<String> ();
        clearCache ();
    }
    
    /** Load data into the cache
     * @param obsy_code the observatory code to load data for
     * @param start_day the first day (0..30) (only used for minute data)
     * @param start_month the first month (0..11) (only used for month data)
     * @param start_year the first year
     * @param data_period one of the data period codes (MINUTE, HOURLY, DAILY)
     * @param extra_pc percent extra data to display at the start and end of
     *        the main data - e.g. for minute data, which displays 1 day, 50%
     *        would show a total of two days, an extra half at the start and end -
     *        valid values from 0 to 50.
     * @param use_zip if TRUE use ZIPPED data files
     * @param use_plain if TRUE use plain data files
     * @param search_for_month - if true, search for the first available month
     * @param base_dir - the base directory that identifies the data source
     *        to look in - to look in all data sources, set this to null
     * @return true if some data loaded, false if no data found */
    public boolean loadData (String obsy_code,
                             int start_day, int start_month, int start_year, 
                             int data_period, int extra_pc,
                             boolean use_zip, boolean use_plain,
                             boolean search_for_month, String base_dir)
    {
        int day, month, year, sample_period, count;
        int last_month, start_index, end_index, time_diff;
        int basic_n_samples, extra_n_samples, total_n_samples, samps_per_day;
        int basic_n_secs, extra_n_secs, total_n_secs, sample_count;
        String string;
        GeomagAbsoluteValue data [];
        GregorianCalendar start_calendar, end_calendar, calendar;
        Vector<CDDataMonth> new_month_cache;
        CDDataMonth data_month, month_data_array [];
        CDDataDay data_day;
        
        // clear the error message list
        error_messages.removeAllElements();
        
        // if this is a new observatory, clear the cache
        if (this.obsy_code == null) clearCache ();
        else if (! this.obsy_code.equals(obsy_code)) clearCache ();
        this.obsy_code = obsy_code;
        
        // if search_for_month is true, find the first month
        if (search_for_month)
        {
            month_data_array = CDMisc.findData (obsy_code, start_year, -2, use_zip, use_plain, base_dir);
            if (month_data_array[0] == null)
            {
                clearCache();
                error_messages.add ("No data is available from this observatory at this time");
                return false;
            }
            
            start_month = month_data_array[0].getMonthIndex() - 1;  //can this go?
            this.setStart_month(month_data_array[0].getMonthIndex() - 1);
        }
       
        
        // work out the following:
        //   samps_per_day - the number of samples in a day
        //   sample_period - the sample period in seconds
        //   basic_n_samples - the number of samples without extra_pc
        //   extra_n_samples - the number of extra samples needed at each end
        //   total_n_samples - the number of samples with extra_pc
        //   basic_n_secs - the seconds duration without extra_pc        
        //   extra_n_secs - the extra seconds duration at each end
        //   total_n_secs - the seconds duration with extra_pc
        // also zero unused parts of the given date
        switch (data_period)
        {
        case MINUTE_MEANS:
            samps_per_day = CDDataDay.N_MINUTE_MEAN_VALUES;
            sample_period = 60;
            basic_n_samples = samps_per_day;
            extra_n_samples = (extra_pc * basic_n_samples) / 100;
            total_n_samples = basic_n_samples + (extra_n_samples * 2);
            break;
        case HOURLY_MEANS:
            start_day = 0;
            samps_per_day = CDDataDay.N_HOURLY_MEAN_VALUES;
            sample_period = 3600;
            basic_n_samples = samps_per_day * DateUtils.daysInMonth (start_month, start_year);
            extra_n_samples = (extra_pc * basic_n_samples) / 100;
            total_n_samples = basic_n_samples + (extra_n_samples * 2);
            break;
        case DAILY_MEANS:
            start_day = start_month = 0;
            samps_per_day = CDDataDay.N_DAILY_MEAN_VALUES;
            sample_period = 86400;
            basic_n_samples = samps_per_day * DateUtils.daysInYear (start_year);
            extra_n_samples = (extra_pc * basic_n_samples) / 100;
            total_n_samples = basic_n_samples + (extra_n_samples * 2);
            break;
        default: // to keep compiler happpy
            samps_per_day = sample_period = 0;
            basic_n_samples = extra_n_samples = total_n_samples = 0;
            basic_n_secs = extra_n_secs = total_n_secs = 0;
            break;
        }    
        basic_n_secs = basic_n_samples * sample_period;
        extra_n_secs = extra_n_samples * sample_period;
        total_n_secs = total_n_samples * sample_period;
        
        // find the date/time of the first sample and the sample after the last sample
        start_calendar = new GregorianCalendar (DateUtils.gmtTimeZone);
        start_calendar.set (start_year, start_month, start_day +1, 0, 0, 0);
        start_calendar.set (GregorianCalendar.MILLISECOND, 0);
        start_calendar.add (GregorianCalendar.SECOND, - extra_n_secs);
        end_calendar = (GregorianCalendar) start_calendar.clone();
        end_calendar.add (GregorianCalendar.SECOND, total_n_secs);
        
        // retrieve the component data a day at a time, either by loading new months,
        // or retrieveing old ones from the cache -
        // start the calendar counter at the beginning of the day
        new_month_cache = new Vector<CDDataMonth> ();
        stored_data = new GeomagAbsoluteValue [total_n_samples];
        last_month = -1;
        data_month = null;
        sample_count = 0;
        calendar = (GregorianCalendar) start_calendar.clone();
        calendar.set (GregorianCalendar.HOUR_OF_DAY, 0);
        calendar.set (GregorianCalendar.MINUTE, 0);
        calendar.set (GregorianCalendar.SECOND, 0);
        while (calendar.compareTo (end_calendar) < 0)
        {
            // which day are we working on?
            year = calendar.get (GregorianCalendar.YEAR);
            month = calendar.get (GregorianCalendar.MONTH);
            day = calendar.get (GregorianCalendar.DAY_OF_MONTH);
            
            // retrieve the appropriate month object
            if (last_month != month)
            {
                // is the month in the cache - if not load it
                data_month = findMonthInCache (year, month);
                if (data_month == null)
                {
                    month_data_array = CDMisc.findData (obsy_code, year, month, use_zip, use_plain, base_dir);
                    data_month = month_data_array [0];
                }
                else cached_month_data.remove(data_month);
                if (data_month != null) new_month_cache.add (data_month);
                last_month = month;
            }
            
            // get data for minute and hourly means
            if (data_period != DAILY_MEANS) 
            {
                // get the day data
                if (data_month == null) data_day = null;
                else data_day = data_month.getDayData (day -1);

                // work out where to start / end when copying the day data
                // this calculation is not needed for daily means
                time_diff = (int) ((start_calendar.getTimeInMillis() - calendar.getTimeInMillis()) / 1000l);
                if (time_diff > 0) start_index = time_diff / sample_period;
                else start_index = 0;
                time_diff = (int) ((end_calendar.getTimeInMillis() - calendar.getTimeInMillis()) / 1000l);
                end_index = time_diff / sample_period;
                if (end_index > samps_per_day) end_index = samps_per_day;

                // extract the samples, or fill in with missing values
                if (data_day == null)
                {
                    for (count=start_index; count<end_index; count++)
                        stored_data [sample_count ++] = new GeomagAbsoluteValue ();
                }
                else
                {
                    switch (data_period)
                    {
                    case MINUTE_MEANS: data = data_day.getMinuteMeans(); break;
                    case HOURLY_MEANS: data = data_day.getHourlyMeans(); break;
                    default: data = null; break;
                    }
                    for (count=start_index; count<end_index; count++)
                        stored_data [sample_count ++] = data [count];
                }
            }
            else
            {
                // get daily mean data
                if (data_month == null) data = null;
                else data = data_month.getDailyMeans();
                
                // copy daily mean data
                if (data == null)
                    stored_data [sample_count ++] = new GeomagAbsoluteValue ();
                else
                    stored_data [sample_count ++] = data [day -1];
            }

            // update the calendar counter
            calendar.add (GregorianCalendar.DAY_OF_MONTH, 1);
        }
        
        // delete the old cache of month files and copy the new month file cache to the old
        cached_month_data.removeAllElements();
        for (count=0; count<new_month_cache.size(); count++)
            cached_month_data.add (new_month_cache.get (count));
        
        // is all the data missing
        if (cached_month_data.size() <= 0)
        {
            clearCache();
            error_messages.add ("No data is available from this observatory at this time");
            return false;
        }

        // store metadata
        data_month = cached_month_data.get(0);
        obsy_name = data_month.getObservatoryData().GetObservatoryName();
        comp_list = makeComponentList (data_month.getCompOrientation(), start_year);
        start_date = start_calendar.getTime();
        
        // create statistics
        stored_stats = new GeomagAbsoluteStats (stored_data);
        
        // create error message list
        for (count=0; count<cached_month_data.size(); count++)
        {
            string = cached_month_data.get(count).getLoadErrmsg();
            if (string != null) error_messages.add (string);
        }
        
        return true;
    }

    public String getObsyCode() { return obsy_code; }
    public String getObsyName() { return obsy_name; }
    public int getNComponents () 
    {
        if (comp_list == null) return 0;
        return comp_list.size(); 
    }
    public int getComponentCode (int index) { return comp_list.get (index).intValue(); }
    public GeomagAbsoluteValue [] getStoredData () { return stored_data; }
    public GeomagAbsoluteStats getStoredStats () { return stored_stats; }
    public Date getStartDate () { return start_date; }
    public Vector<CDDataMonth> getMonthData () { return cached_month_data; }
    
    /** there may have been one or more error messages generated
     * when the data was loaded - this routine allows you to
     * access these messages */
    public Vector<String> getErrorMessages ()
    {
        return error_messages;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // private code below here
    ////////////////////////////////////////////////////////////////////////////
        
    /** list the traces used in this data in the form of codes from
     * the GeomagAbsoluteValue objects */
    private Vector<Integer> makeComponentList (String comps, int year)
    {
        int i, n;
        boolean found;
        Integer new_comp;
        Vector<Integer> list;
        
        if (comps == null) comps = "";
        list = new Vector<Integer> ();
        for (n=0; n<comps.length(); n++)
        {
            switch (comps.charAt(n))
            {
                case 'X': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_X); break;
                case 'Y': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_Y); break;
                case 'Z': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_Z); break;
                case 'H': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_H); break;
                case 'D': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_D); break;
                case 'I': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_I); break;
                case 'F': new_comp = new Integer (GlobalObjects.getExpectedFType(obsy_code, year)); break;
                case 'G': new_comp = new Integer (GeomagAbsoluteValue.COMPONENT_F_DIFF); break;
                default: new_comp = null; break;
            }
            if (new_comp != null)
            {
                found = false;
                for (i=0; i<list.size(); i++)
                {
                    if (list.get(i).intValue() == new_comp.intValue())
                        found = true;
                }
                if (! found) list.add (new_comp);
            }
        }
        return list;
    }
    
    private void clearCache ()
    {
        obsy_code = obsy_name = null;
        comp_list = null;
        stored_data = null;
        stored_stats = null;
        start_date = null;
        cached_month_data.removeAllElements();
    }

    /** find the month object in the cache
     * @param year the year
     * @param month the month (0..11)
     * @return the month object */
    private CDDataMonth findMonthInCache (int year, int month)
    {
        int count;
        CDDataMonth data_month;
        
        month ++;
        for (count=0; count<cached_month_data.size(); count++)
        {
            data_month = cached_month_data.get (count);
            if ((data_month.getObservatoryData().GetYear() == year) &&
                (data_month.getMonthIndex() == month)) return data_month;
        }
        
        return null;
    }

    public int getStart_month() {
        return start_month;
    }

    public void setStart_month(int start_month) {
        this.start_month = start_month;
    }

}
