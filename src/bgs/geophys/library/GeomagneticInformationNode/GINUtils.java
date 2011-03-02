/*
 * GINUtils.java
 *
 * Created on 05 November 2006, 10:50
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Utilities (static methods) for various GIN operations.
 *
 * @author  smf
 */
public class GINUtils 
{

    /** inner class to hold return values from getAvailableData */
    public static class AvailableDataDetails
    {
        /** first date data is available */
        public Date start_date;     
        /** count of data points available for each day */
        public int day_counts [];   
    }
    
    /** return code for gin_access_level() - access level not known */
    public static final int GIN_UNKNOWN_ACCESS = -1;
    /** return code for gin_access_level() - no access allowed */
    public static final int GIN_NO_ACCESS = 0;
    /** return code for gin_access_level() - write access allowed */
    public static final int GIN_WRITE_ACCESS = 1;
    /** return code for gin_access_level() - read access allowed */
    public static final int GIN_READ_ACCESS = 2;
    
    // cached copy of the access level
    private static int gin_access_level = GIN_UNKNOWN_ACCESS;

    /** Creates a new instance of GINUtils */
    public GINUtils() 
    {
    }
    
    /** re-order the date/time and number fields of an intermagnet packet
     * @param packet the (126 byte) intermagnet packet
     * @param sat_details satellite details for this station */
    public static void reorderIntermagnet (byte packet [], 
                                           SatDetails.SatDetailsFields sat_details)
    {
        int count;
        byte nibble [], save;

        nibble = new byte [6];
        
        // swap all bytes ??
        if (sat_details.swap_bytes)
        {
            for (count=0; count<126; count += 2)
            {
                save = packet [count];
                packet [count] = packet [count +1];
                packet [count +1] = save;
            }
        }

        // re-order the date/time
        nibble [0] = (byte) (((int) packet [0] & 0xf0) >> 4);
        nibble [1] = (byte)  ((int) packet [0] & 0x0f);
        nibble [2] = (byte) (((int) packet [1] & 0xf0) >> 4);
        nibble [3] = (byte)  ((int) packet [1] & 0x0f);
        nibble [4] = (byte) (((int) packet [2] & 0xf0) >> 4);
        nibble [5] = (byte)  ((int) packet [2] & 0x0f);
        packet [0] = (byte) (((int) nibble [sat_details.swap_dates[0]] << 4) |
	                      (int) nibble [sat_details.swap_dates[1]]);
        packet [1] = (byte) (((int) nibble [sat_details.swap_dates[2]] << 4) |
	                      (int) nibble [sat_details.swap_dates[3]]);
        packet [2] = (byte) (((int) nibble [sat_details.swap_dates[4]] << 4) |
	                      (int) nibble [sat_details.swap_dates[5]]);
        
        // re-order the lat/long
        nibble [0] = (byte) (((int) packet [ 9] & 0xf0) >> 4);
        nibble [1] = (byte)  ((int) packet [ 9] & 0x0f);
        nibble [2] = (byte) (((int) packet [10] & 0xf0) >> 4);
        nibble [3] = (byte)  ((int) packet [10] & 0x0f);
        nibble [4] = (byte) (((int) packet [11] & 0xf0) >> 4);
        nibble [5] = (byte)  ((int) packet [11] & 0x0f);
        packet [ 9] = (byte) (((int) nibble [sat_details.swap_dates[0]] << 4) |
	                       (int) nibble [sat_details.swap_dates[1]]);
        packet [10] = (byte) (((int) nibble [sat_details.swap_dates[2]] << 4) |
	                       (int) nibble [sat_details.swap_dates[3]]);
        packet [11] = (byte) (((int) nibble [sat_details.swap_dates[4]] << 4) |
	                       (int) nibble [sat_details.swap_dates[5]]);

        // re-order the numeric data ??
        if (sat_details.swap_fvals)
        {
            for (count=30; count<126; count += 2)
            {
                save = packet [count];
                packet [count] = packet [count +1];
                packet [count +1] = save;
            }
        }
    }

    /** Generate the name for a database file
     * @param database - the filename for the database (e.g. "station_details", etc.)
     * @return the file name
     * @throws ConfigFileException if there was an error */
    public static String makeDatabaseFilename (String database)
    throws ConfigFileException
    {
        String dir;

        // get the system tables directory
        dir = getDirName ("GIN_SYS_TAB_DIR");
        if (dir == null) throw new ConfigFileException ("Unable to find directory name for system tables");

        // construct the filename
        return dir + File.separator + database + ".gdb";
    }
    
    /** create a filename for a GIN data file
     * @param station_code IAGA station code
     * @param samps_per_day the sample rate in samples per day
     * @param data_type the data type
     * @param date the date for the data
     * @return the file name
     * @throws ConfigFileException if there was an error */
    public static String makeDataFilename (String station_code, int samps_per_day, 
                                           String data_type, Date date)
    throws ConfigFileException
    {
        int dir_len;
        String dir, filename;
        SimpleDateFormat year_format, month_day_format;
        File file;

        // get the data directory
        dir = getDirName ("GIN_DATA_DIR");
        if (dir == null) throw new ConfigFileException ("Unable to find directory name for data directories");

        // construct the observatory directory name and check it exists
        filename = dir + File.separator + station_code.toLowerCase();
        file = new File (filename);
        if (! file.exists())
        {
            if (getGinAccessLevel() == GIN_WRITE_ACCESS) 
            {
                if (! file.mkdir())
                    throw new ConfigFileException ("Error creating directory: " + filename);
            }
        }
        else if (! file.isDirectory())
            throw new ConfigFileException ("File is not a directory: " + filename);

        // construct the sample rate directory name and check it exists
        filename += File.separator + make_period_dir_name(samps_per_day);
        file = new File (filename);
        if (! file.exists())
        {
            if (getGinAccessLevel() == GIN_WRITE_ACCESS)
            {
                if (! file.mkdir())
                    throw new ConfigFileException ("Error creating directory: " + filename);
            }
        }
        else if (! file.isDirectory())
            throw new ConfigFileException ("File is not a directory: " + filename);
        
        // construct the year / data type directory name and check it exists
        year_format = new SimpleDateFormat ("yyyy");
        year_format.setTimeZone(SimpleTimeZone.getTimeZone("gmt"));
        filename += File.separator + year_format.format (date) + "_" + data_type.toLowerCase();
        file = new File (filename);
        if (! file.exists())
        {
            if (getGinAccessLevel() == GIN_WRITE_ACCESS)
            {
                if (! file.mkdir())
                    throw new ConfigFileException ("Error creating directory: " + filename);
            }
        }
        else if (! file.isDirectory())
            throw new ConfigFileException ("File is not a directory: " + filename);

        // construct the filename
        month_day_format = new SimpleDateFormat ("MM_dd");
        month_day_format.setTimeZone(SimpleTimeZone.getTimeZone("gmt"));
        return filename + File.separator + month_day_format.format (date) + ".gin";
    }
    
    /** create a filename for a GIN lag data file
     * @param station_code IAGA station code
     * @param samps_per_day the sample rate in samples per day
     * @param date the date for the data
     * @return the file name
     * @throws ConfigFileException if there was an error */
    public static String makeLagDataFilename (String station_code, int samps_per_day, Date date)
    throws ConfigFileException
    {
        String dir, filename;
        SimpleDateFormat year_format;

        // get the data directory
        dir = getDirName ("GIN_DATA_DIR");
        if (dir == null) throw new ConfigFileException ("Unable to find directory name for data directories");

        // construct the file name
        year_format = new SimpleDateFormat ("yyyy");
        year_format.setTimeZone(SimpleTimeZone.getTimeZone("gmt"));
        filename = dir + File.separator + 
                   station_code.toLowerCase() + File.separator + 
                   make_period_dir_name(samps_per_day) + File.separator + 
                   year_format.format (date) + ".mrd";
        return filename;
    }
    
    /** Delete geomagnetic data from the GIN. This routine deletes data in 24 hour batches.
     * @param station_code code of station to delete
     * @param samps_per_day the sample rate in samples per day
     * @param data_type_desc type of data to delete
     * @param start_date start date of data to delete from
     * @param n_days number of days to delete
     * @throws GINDataException ifthere was en error
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public static void deleteGeomagData (String station_code, int samps_per_day, String data_type_desc,
                                         Date start_date, int n_days)
    throws GINDataException, ConfigFileException, ParameterException
    {
        int day_count;
        String data_type_code, filename, msg;
        StationDetails station_details_list;
        StationDetails.StationDetailsFields sta_details;
        GINDictionary gin_dictionary;
        Date date;
        File file, year_dir, sr_dir, obsy_dir, last_year_dir;
        
        // load the station details and the dictionary
        station_details_list = new StationDetails ();
        gin_dictionary = new GINDictionary ();

        // check the station code and data type
        sta_details = station_details_list.findStation(station_code);
        if (sta_details == null)
            throw new ParameterException ("Unknown station code: " + station_code);
        data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
        if (data_type_code == null)
            throw new ParameterException ("Unknown data type: " + data_type_desc);
        if (data_type_code.equalsIgnoreCase("j"))
            throw new ParameterException ("Special data type J not allowed here");
        
        // for each day to delete ...
        last_year_dir = obsy_dir = sr_dir = year_dir = null;
        for (date=new Date (start_date.getTime()), day_count=0;
             day_count<n_days; date=new Date (date.getTime() + 86400000), day_count++)
        {
            // create the filename for this day
            try
            {
                file = new File (makeDataFilename(station_code, samps_per_day, data_type_code, date));
                year_dir = file.getParentFile();
                if (year_dir != null) sr_dir = year_dir.getParentFile();
                if (sr_dir != null) obsy_dir = sr_dir.getParentFile();
            }
            catch (ConfigFileException e)
            {
                throw new GINDataException ("Unable to create filename", e);
            }
            
            // check if the file exists and delete it - also attempt to delete
            // the parent directory
            if (file.exists())
            {
                if (! file.delete()) msg = "Not all files deleted";
                if (! year_dir.equals(last_year_dir))
                {
                    if (last_year_dir != null)
                    {
                        if (last_year_dir.list().length <= 0)
                            last_year_dir.delete();
                    }
                    last_year_dir = year_dir;
                }
            }
        }

        // attempt to delete directories
        if (year_dir != null)
        {
            if (year_dir.list().length <= 0) year_dir.delete();
        }
        if (sr_dir != null)
        {
            if (sr_dir.list().length <= 0) sr_dir.delete();
        }
        if (obsy_dir != null)
        {
            if (obsy_dir.list().length <= 0) obsy_dir.delete();
        }
    }
    
    /** find the amount of data available for
     * a given station / data type
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param data_type_desc the data type
     * @param start_date the start date OR null to use earliest available
     * @param end_date the end date OR null to use latest available 
     * @return the details OR null if no data available
     * @throws GINDataException if there was an error
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public static AvailableDataDetails getAvailableData (String station_code,
                                                         int samps_per_day,
                                                         String data_type_desc,
                                                         Date start_date,
                                                         Date end_date)
    throws GINDataException, ConfigFileException, ParameterException
    {
        int n_days, day_count;
        boolean ignore_data;
        AvailableDataDetails av_data_details;
        StationDetails station_details_list;
        StationDetails.StationDetailsFields sta_details;
        GINDictionary gin_dictionary;
        String data_type_code, r_dt_desc, r_dt_code, a_dt_desc, a_dt_code, q_dt_desc, q_dt_code;
        Date date, start_date2, end_date2, start_date3, end_date3;
        GINData gin_data;
        
        // load the station details and the dictionary
        station_details_list = new StationDetails ();
        gin_dictionary = new GINDictionary ();
        
        // check the station code and data type
        sta_details = station_details_list.findStation(station_code);
        if (sta_details == null)
            throw new ParameterException ("Unknown station code: " + station_code);
        data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
        if (data_type_code == null)
            throw new ParameterException ("Unknown data type: " + data_type_desc);
        
        // retrieve reported and adjusted data type codes
        if (data_type_code.equalsIgnoreCase("j"))
        {
            r_dt_desc = "reported";
            a_dt_desc = "adjusted";
            q_dt_desc = "quasi-def";
            r_dt_code =  gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, r_dt_desc);
            a_dt_code =  gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, a_dt_desc);
            q_dt_code =  gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, q_dt_desc);
            if (r_dt_code == null)
                throw new ParameterException ("Unknown data type: " + r_dt_desc);
            if (a_dt_code == null)
                throw new ParameterException ("Unknown data type: " + a_dt_desc);
            if (q_dt_code == null)
                throw new ParameterException ("Unknown data type: " + q_dt_desc);
        }
        else r_dt_desc = a_dt_desc = q_dt_desc = r_dt_code = a_dt_code = q_dt_code = null;
        
        // do we need to find the start date ??
        if (start_date == null)
        {
            if (data_type_code.equalsIgnoreCase("j"))
            {
                start_date = findMinOrMax (station_code, samps_per_day, q_dt_desc, q_dt_code, false, false);
                start_date2 = findMinOrMax (station_code, samps_per_day, a_dt_desc, a_dt_code, false, false);
                start_date3 = findMinOrMax (station_code, samps_per_day, r_dt_desc, r_dt_code, false, false);
                if (start_date == null)
                {
                    start_date = start_date2;
                    if (start_date == null) start_date = start_date3;
                    if (start_date == null) return null;
                }
                if (start_date2 != null)
                {
                    if (start_date2.getTime() < start_date.getTime())
                        start_date = start_date2;
                }
                if (start_date3 != null)
                {
                    if (start_date3.getTime() < start_date.getTime())
                        start_date = start_date3;
                }
            }
            else start_date = findMinOrMax (station_code, samps_per_day, data_type_desc, data_type_code, false, false);
        }
        
        // do we need to find the end date ??
        if (end_date == null)
        {
            if (data_type_code.equalsIgnoreCase("j"))
            {
                end_date = findMinOrMax (station_code, samps_per_day, q_dt_desc, q_dt_code, true, false);
                end_date2 = findMinOrMax (station_code, samps_per_day, a_dt_desc, a_dt_code, true, false);
                end_date3 = findMinOrMax (station_code, samps_per_day, r_dt_desc, r_dt_code, true, false);
                if (end_date == null)
                {
                    end_date = end_date2;
                    if (end_date == null) end_date = end_date3;
                    if (end_date == null) return null;
                }
                if (end_date2 != null)
                {
                    if (end_date2.getTime() > end_date.getTime())
                        end_date = end_date2;
                }
                if (end_date3 != null)
                {
                    if (end_date3.getTime() > end_date.getTime())
                        end_date = end_date3;
                }
            }
            else end_date = findMinOrMax (station_code, samps_per_day, data_type_desc, data_type_code, true, false);
        }

        // set up the return structure
        n_days = (int) ((end_date.getTime() - start_date.getTime()) / 86400000l) +1;
        if (n_days < 1) n_days = 1;
        av_data_details = new AvailableDataDetails ();
        av_data_details.start_date = start_date;
        av_data_details.day_counts = new int [n_days];
        
        // for each day...
        for (date=new Date (start_date.getTime()), day_count=0;
             day_count < n_days; date.setTime(date.getTime() + 86400000l), day_count ++)
        {
            // load the data and get the statistics
            gin_data = new GINData (station_code, samps_per_day, date, samps_per_day, data_type_desc, true);
            av_data_details.day_counts[day_count] = gin_data.getStats(0).count;
            if (gin_data.getStats(1).count > av_data_details.day_counts[day_count])
                av_data_details.day_counts[day_count] = gin_data.getStats(1).count;
            if (gin_data.getStats(2).count > av_data_details.day_counts[day_count])
                av_data_details.day_counts[day_count] = gin_data.getStats(2).count;
            if (gin_data.getStats(3).count > av_data_details.day_counts[day_count])
                av_data_details.day_counts[day_count] = gin_data.getStats(3).count;
        }
        
        // return the details
        return av_data_details;
    }

    /** find the date/time of the most recent data available for
     * a given station / data type
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param data_type_desc the data type 
     * @return the date or null if no data was found
     * @throws GINDataException if there was an error
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public static Date findMRD (String station_code, int samps_per_day, String data_type_desc)
    throws GINDataException, ConfigFileException, ParameterException
    {
        StationDetails station_details_list;
        StationDetails.StationDetailsFields sta_details;
        GINDictionary gin_dictionary;
        String data_type_code;
        Date date, date2, date3;
        
        // load the station details and the dictionary
        station_details_list = new StationDetails ();
        gin_dictionary = new GINDictionary ();
        
        // check the station code and data type
        sta_details = station_details_list.findStation(station_code);
        if (sta_details == null)
            throw new ParameterException ("Unknown station code: " + station_code);
        data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
        if (data_type_code == null)
            throw new ParameterException ("Unknown data type: " + data_type_desc);

        // process special data type code
        if (data_type_code.equalsIgnoreCase("j"))
        {
            date = findMRD (station_code, samps_per_day, "quasi-def");
            date2 = findMRD (station_code, samps_per_day, "adjusted");
            date3 = findMRD (station_code, samps_per_day, "reported");
            if (date == null) date = date2;
            if (date == null) date = date3;
            if (date2 != null)
            {
                if (date2.getTime() > date.getTime()) date = date2;
            }
            if (date3 != null)
            {
                if (date3.getTime() > date.getTime()) date = date3;
            }
        }
        else
            date = findMinOrMax (station_code, samps_per_day, data_type_desc, data_type_code, 
                                 true, true);
        return date;
    }

    /** get the level of access that the caller has to the GIN data - you
     * can override the probing by setting GIN_READ_ONLY system property
     * @return one of the access level codes */
    public static int getGinAccessLevel ()
    {
        String dir;
        File dir_file;

        if (gin_access_level == GIN_UNKNOWN_ACCESS)
        {
            if (System.getProperty("GIN_READ_ONLY") != null)
                gin_access_level = GIN_READ_ACCESS;
            else
            {
                dir = getDirName ("GIN_DATA_DIR");
                if (dir == null) gin_access_level = GIN_NO_ACCESS;
                else
                {
                    dir_file = new File (dir);
                    if (dir_file.canWrite()) gin_access_level = GIN_WRITE_ACCESS;
                    else if (dir_file.canRead()) gin_access_level = GIN_READ_ACCESS;
                    else gin_access_level = GIN_NO_ACCESS;
                }
            }
        }
        return gin_access_level;
    }

    
    //////////////////////////////////////////////////////////////////////////////////////
    // private code below here
    //////////////////////////////////////////////////////////////////////////////////////
    
    /** get a directory - will use getenv in v5 - currently used properties */
    private static String getDirName (String env_name)
    {
        // return System.getenv ("GIN_SYS_TAB_DIR");
        return System.getProperty(env_name);
    }

    /** find the date of the earliest or latest data file for
     *  a given station and data type code
     * @param station_code the station to search for
     * @param samps_per_day the sample rate in samples per day
     * @param data_type_desc the data type description
     * @param data_type_code the data type code - doesn't support data type code 'j'
     * @param max true to search for the maximum, false to search for the minimum
     * @param examine_file true to examine the contents of the file and include
     *        this information in the returned date, false to just look at file names
     * @return the date of the earliest or latest data OR null if the
     *         date cannot be found
     * @throws ParameterException if there was a problem with the parameters */
    private static Date findMinOrMax (String station_code, int samps_per_day, String data_type_desc,
                                      String data_type_code, 
                                      boolean max, boolean examine_file)
    throws ParameterException
    {
        int min_year, max_year, day_inc, n_days, count, day;
        int start_year, end_year, inc_year, year, earliest_time, latest_time;
        String dir_name, filename;
        StringTokenizer tokens;
        File obsy_dir, min_year_dir, max_year_dir, year_dir, files [], file;
        GregorianCalendar calendar;
        Date date;
        GINData gin_data;
        GINData.DataStats stats;
        
        dir_name = getDirName ("GIN_DATA_DIR");
        if (dir_name == null) return null;

        // construct the observatory directory and sample rate name and list its contents
        // to find the minimum and maximum years
        min_year = Integer.MAX_VALUE;
        max_year = Integer.MIN_VALUE;
        dir_name += File.separator + station_code.toLowerCase() + File.separator + make_period_dir_name(samps_per_day);
        obsy_dir = new File (dir_name);
        files = obsy_dir.listFiles();
        for (count=0; count<files.length; count++)
        {
            try
            {
                tokens = new StringTokenizer (files[count].getName(), "_");
                year = Integer.parseInt(tokens.nextToken());
                if (data_type_code.equalsIgnoreCase(tokens.nextToken()))
                {
                    if (year < min_year)
                    {
                        min_year = year;
                        min_year_dir = files [count];
                    }
                    if (year > max_year)
                    {
                        max_year = year;
                        max_year_dir = files [count];
                    }
                }
            }
            catch (NoSuchElementException e) { }
            catch (NumberFormatException e) { }                
        }
        
        // check for no years
        if (min_year >= Integer.MAX_VALUE) return null;
        
        // for each year...
        if (max)
        {
            start_year = max_year;
            end_year = min_year;
            inc_year = -1;
        }
        else
        {
            start_year = min_year;
            end_year = max_year;
            inc_year = 1;
        }
        for (year=start_year; year<=end_year; year++)
        {
            // create variables to iterate over the days
            if (max)
            {
                calendar = new GregorianCalendar (year, 11, 31);
                day_inc = -1;
            }
            else
            {
                calendar = new GregorianCalendar (year, 0, 1);
                day_inc = 1;
            }
            calendar.setTimeZone(SimpleTimeZone.getTimeZone("gmt"));
            if (calendar.isLeapYear(year)) n_days = 366;
            else n_days = 365;
            for (day=0; day<n_days; day++)
            {
                // create the filename for this day and see if it exists
                date = calendar.getTime();
                try
                {
                    filename = makeDataFilename (station_code, samps_per_day, data_type_code, date);
                    file = new File (filename);
                    if (file.exists()) 
                    {
                        // do we want to examine the file contents
                        if (! examine_file) return date;
                        
                        // load the file
                        try
                        {
                            gin_data = new GINData (station_code, samps_per_day, date, samps_per_day, data_type_desc, false);
                            earliest_time = Integer.MAX_VALUE;
                            latest_time = - Integer.MAX_VALUE;
                            for (count=0; count<4; count++)
                            {
                                stats = gin_data.getStats(count);
                                if (stats.count > 0)
                                {
                                    if (stats.earliest_time < earliest_time)
                                        earliest_time = stats.earliest_time;
                                    if (stats.latest_time > latest_time)
                                        latest_time = stats.latest_time;
                                }
                            }
                            if (max)
                            {
                                if (latest_time != - Integer.MAX_VALUE)
                                    return new Date (date.getTime() + (long) (latest_time * 1000));
                            }
                            else
                            {
                                if (earliest_time != Integer.MAX_VALUE)
                                    return new Date (date.getTime() + (long) (earliest_time * 1000));
                            }
                        }
                        catch (GINDataException e) { }
                    }
                }
                catch (ConfigFileException e) { }
                
                // increment the calendar
                calendar.add (calendar.DAY_OF_MONTH, day_inc);
            }
            
            
            filename = dir_name + File.separator + 
                       Integer.toString (year) + "_" + data_type_code;
        }
        
        // no files were found
        return null;
    }
    
    /* convert a sample period to a directory name (for use
     * in the data file path name
     * @param samps_per_day the sample rate in samples per day
     * @return the directory name used to represent the sample period */
    public static String make_period_dir_name (int samps_per_day)
    {
        String name;
  
        switch (samps_per_day)
        {
        case 86400: name = "second";    break;
        case 8640:  name = "10second";  break;
        case 1440:  name = "minute";    break;
        case 24:    name = "hour";      break;
        case 1:     name = "day";       break;
        default: name = Integer.toString (samps_per_day) + "PerDay"; break;
        }
        return name;
    }

    /** parse the samples per day field - as well as numeric values, this routine accepts
     * the shorthand 'minute' (and acronyms) for '1440' and
     * 'second' (and acroynms) for  '86400. This routine restricts the sample rates to a valid set (currently
     * 1440 and 86400).
     * @param spd the samples per day, as given by the user
     * @return samples per day or -ve for an error */
    public static int parseSampsPerDay (String spd)
    {

        int samps_per_day;

        /* parse the string */
        if (spd.equalsIgnoreCase("minutes") || spd.equalsIgnoreCase("minute") ||
            spd.equalsIgnoreCase("min") || spd.equalsIgnoreCase("m"))
            samps_per_day = 1440;
        else if (spd.equalsIgnoreCase("seconds") || spd.equalsIgnoreCase("second") ||
                 spd.equalsIgnoreCase("sec") || spd.equalsIgnoreCase("s"))
            samps_per_day = 86400;
        else
        {
            try 
            {
                samps_per_day = Integer.parseInt (spd); 
                if (samps_per_day <= 0) samps_per_day = -1;
            }
            catch (NumberFormatException e) { samps_per_day = -1; }
        }

        /* restrict to valid sample rates */
        switch (samps_per_day)
        {
        case 1440:
        case 86400:
          break;
        default:
            samps_per_day = -1;
            break;
        }

        return samps_per_day;
    }

    /** get a title for the samples per day
     * @param samps_per_day number of samples per day
     * @returns the title */
    public static String getSampleRateTitle (int samps_per_day)
    {
        String ret_val;

        switch (samps_per_day)
        {
        case 86400: ret_val = "second";    break;
        case 8640:  ret_val = "10-second"; break;
        case 1440:  ret_val = "minute";    break;
        case 24:    ret_val = "hour";      break;
        case 1:     ret_val = "day";       break;
        default: ret_val = Integer.toString (samps_per_day) + "PerDay"; break;
        }

        return ret_val;

    }
    
    /** this routine sets system properties that control:
     * 1.) where the GIN data is found
     * 2.) where the logs are put
     * The system properties are:
     *      GIN_DATA_DIR        The directory holding the GIN data
     *      GIN_SYS_TAB_DIR     The directory holding GIN configuration files
     *      GIN_LOG_DIR         The directory where log files should be written
     * For each of these properties the following hierarchy will be used to
     * try and retrieve the property information:
     * 1.) If the property is already set (e.g. using -D... options when the
     *     servlet container is started) this value is used.
     * 2.) If an environment variable of the same name is present, the
     *     variable is read and the property set
     * 3.) As a last attempt, a default hardcoded value is used - this will
     *     only work on a PC for development (using SAMBA links to the GIN data) */
    public static void setProperties ()
    {
        Properties sys_props;
        
        sys_props = System.getProperties();
        
        // do we need a GIN data directory
        if (sys_props.getProperty("GIN_DATA_DIR") == null) 
        {
            // yes - try the environment
            if (System.getenv("GIN_DATA_DIR") != null)
                sys_props.setProperty("GIN_DATA_DIR",    System.getenv("GIN_DATA_DIR"));
            // otherwise default to local disk or SAMBA share
            else if (new File("c:\\data\\gin_data").exists())
            {
                // if we are working through mhlf, we can't write data
                sys_props.setProperty("GIN_DATA_DIR",    "c:\\data\\gin_data");
                sys_props.setProperty("GIN_READ_ONLY",   "1");
            }
            else if (new File("\\\\mhlf\\e_gin\\data").exists())
            {
                // if we are working through mhlf, we can't write data
                sys_props.setProperty("GIN_DATA_DIR",    "\\\\mhlf\\e_gin\\data");
                sys_props.setProperty("GIN_READ_ONLY",   "1");
            }
        }
        
        // do we need a GIN system tables directory
        if (sys_props.getProperty("GIN_SYS_TAB_DIR") == null)
        {
            // yes - try the environment
            if (System.getenv("GIN_SYS_TAB_DIR") != null)
                sys_props.setProperty("GIN_SYS_TAB_DIR", System.getenv("GIN_SYS_TAB_DIR"));
            // otherwise default to local disk or SAMBA share
            else if (new File("c:\\data\\gin_data\\system_tables").exists())
                sys_props.setProperty("GIN_SYS_TAB_DIR", "c:\\data\\gin_data\\system_tables");
            else if (new File("\\\\mhlf\\e_gin\\data\\system_tables").exists())
                sys_props.setProperty("GIN_SYS_TAB_DIR", "\\\\mhlf\\e_gin\\data\\system_tables");
        }
        
        // do we need a log direcotry ??
        if (sys_props.getProperty("GIN_LOG_DIR") == null) 
        {
            // yes - try the environment
            if (System.getenv("GIN_LOG_DIR") != null)
                sys_props.setProperty("GIN_LOG_DIR",    System.getenv("GIN_LOG_DIR"));
            // otherwise default to something simple
            else if (new File("c:\\").exists())
                sys_props.setProperty("GIN_LOG_DIR", "c:\\");
        }
    }
    
    
}
