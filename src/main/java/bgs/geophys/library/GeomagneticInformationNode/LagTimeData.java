/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A class to hold information on the real-time performance of an GIN
 * observatory. Data of real-time lag is recorded hourly on the GIN.
 * This class allows this data to be searched and read.
 * 
 * @author smf
 */
public class LagTimeData 
{

    /** a class to hold an individual lag time reading */
    public class LagTimeSample
    {
        private Date date;      // the date/time that the sample was recorded
        private long lag;       // the lag at that time (in milliseconds)
        public LagTimeSample (String record) throws GINDataException
        {
            StringTokenizer tokens;
            try
            {
                tokens = new StringTokenizer (record);
                tokens.nextToken();
                date = new Date (Long.parseLong(tokens.nextToken()) * 1000l);
                lag = date.getTime() - (Long.parseLong(tokens.nextToken()) * 1000l);
            }
            catch (NoSuchElementException e) { throw new GINDataException ("Error in data: " + record); }
            catch (NumberFormatException e) { throw new GINDataException ("Error in data: " + record); }
        }
        public Date getDate () { return date; }
        public long getLag () { return lag; }
        @Override
        public String toString ()
        {
            return date.toString() + ", lag=" + Long.toString (lag);
        }
    }
    
    // list of stations
    private StationDetails station_details_list;
    
    // the sample rate for this data (in samples per day)
    private int samps_per_day;
    
    // reference to station details for this station
    private StationDetails.StationDetailsFields sta_details;
    private String station_code;
    
    // start and end date for this data
    private Date start_date;
    private Date end_date;
    
    // minimum, medium and average lag time (in milliseconds)
    private long min_lag_time;
    private long max_lag_time;
    private long average_lag_time;
    
    // an array of lag time samples
    private Vector<LagTimeSample> samples;
    
    /** Creates an empty instance of LagTimeData
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param start_date the starting date for the data - the data will be
     *        on or after this date
     * @param end_date the ending date for the data - the data will be
     *        before this date
     * @param dummy to allow a second constructor (which really has the
     *        same parameter list) */
    public LagTimeData (String station_code, int samps_per_day, 
                        Date start_date, Date end_date, boolean dummy)
    {
        // store the parameters
        this.samps_per_day = samps_per_day;
        this.station_code = station_code;
        this.sta_details = null;
        this.start_date = start_date;
        this.end_date = end_date;
        samples = new Vector<LagTimeSample> ();
        min_lag_time = max_lag_time = average_lag_time = 0;
    }

    /** Creates a new instance of LagTimeData from data on disk
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param start_date the starting date for the data - the data will be
     *        on or after this date
     * @param end_date the ending date for the data - the data will be
     *        before this date
     * @throws GINDataException if there is an error loading the data
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public LagTimeData (String station_code, int samps_per_day, 
                        Date start_date, Date end_date)
    throws GINDataException, ConfigFileException, ParameterException
    {
        // load verifying objects
        station_details_list = new StationDetails ();
        
        // store the sample rate
        this.samps_per_day = samps_per_day;
        
        // check the station code and data type
        sta_details = station_details_list.findStation(station_code);
        if (sta_details == null)
            throw new ParameterException ("Station does not exist: " + station_code);
        this.station_code = sta_details.station_code;
        
        // record the parameters
        this.start_date = start_date;
        this.end_date = end_date;

        // create the data array
        samples = new Vector<LagTimeSample> ();
        min_lag_time = max_lag_time = average_lag_time = 0;
        
        // read the data
        readData ();
    }

    // reading routines
    public int getSampsPerDay() { return samps_per_day; }
    public String getStationCode () { return station_code; }
    public StationDetails.StationDetailsFields getStaDetails() { return sta_details; }
    public Date getStartDate() { return start_date; }
    public Date getEndDate() { return end_date; }
    public int getNSamples () { return samples.size(); }
    public LagTimeSample getSample (int index) { return samples.get(index); }
    public long getMinLagTime () { return min_lag_time; }
    public long getMaxLagTime () { return max_lag_time; }
    public long getAverageLagTime () { return average_lag_time; }
    

    // routine to read data from a data file
    private void readData ()
    throws GINDataException
    {
        String filename, line;
        BufferedReader reader;
        GregorianCalendar date;
        LagTimeSample sample;
        
        // catch file IO exceptions ...
        reader = null;
        filename = "";
        max_lag_time = average_lag_time = 0;
        min_lag_time = Long.MAX_VALUE;
        samples.removeAllElements();
        try
        {
            // for each year ...
            date = new GregorianCalendar ();
            for (date.setTime(getStartDate()); date.getTime().getTime() <= getEndDate().getTime(); date.add (GregorianCalendar.YEAR, 1))
            {
                // open the file
                filename = GINUtils.makeLagDataFilename(getStaDetails().station_code, getSampsPerDay(),date.getTime());
                try
                {
                    reader = new BufferedReader (new FileReader (filename));
            
                    // for each record ...
                    while ((line = reader.readLine()) != null)
                    {
                        sample = new LagTimeSample (line);
                        if (sample.getDate().getTime() >= getEndDate().getTime()) break;
                        else if (sample.getDate().getTime() >= getStartDate().getTime())
                        {
                            samples.add (sample);
                            average_lag_time += sample.getLag();
                            if (sample.getLag() > max_lag_time) max_lag_time = sample.getLag();
                            if (sample.getLag() < min_lag_time) min_lag_time = sample.getLag();
                        }
                    }
                
                    // close the file
                    reader.close();
                    reader = null;
                }
                catch (FileNotFoundException e) { }
            }
        }
        catch (ConfigFileException e) 
        { 
            throw new GINDataException ("Unable to find data file directory", e); 
        }
        catch (IOException e) 
        { 
            throw new GINDataException ("IO error: " + filename, e); 
        }
        
        if (samples.size() <= 0)
            min_lag_time = 0;
        else
            average_lag_time /= (long) samples.size();
        
    }
    
}
