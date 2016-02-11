/***************************************************************
 * SdasStats.java
 * SdasStats is used to hold a single set of statistics from 
 * an SDAS system
 *
 * Created on 06 January 2003, 12:14
 **************************************************************/

package bgs.geophys.library.RealTimeData.Sdas;

import java.util.*;
import bgs.geophys.library.RealTimeData.*;


public class SdasStats extends Object implements DataStats
{
    // private fields
    /** the SDAS channel number for these stats */
    private int channel;
    /** the date for these stats */
    private Date date;
    /** TRUE for hourly stats, FALSE for daily stats */
    private boolean hourly;
    /** the number of points used to make these statistics */
    private int n_points;
    /** the number of points missing */
    private int n_missing;
    /** the minimum value */
    private int min_val;
    /** the maximum value */
    private int max_val;
    /* the average */
    private double average;
    /* the standard deviation */
    private double standard_dev;
    
    /*************************************************************
     * Create a new SdasStats object.
     *
     * @param channel the SDAS channel number
     * @param date the date for the stats
     * @param hourly TRUE for hourly stats, FALSE for daily
     * @param n_points the number of points used to construct the stats
     * @param n_missing the number of missing points
     * @param min_val the minimum value
     * @param max_val the maximum value
     * @param average the average
     * @param standard_dev the standard deviation
     *************************************************************/
    public SdasStats (int channel, Date date, boolean hourly,
                      int n_points, int n_missing, int min_val, int max_val,
                      double average, double standard_dev)
    {
        this.channel = channel;
        this.date = date;
        this.hourly = hourly;
        this.n_points = n_points;
        this.n_missing = n_missing;
        this.min_val = min_val;
        this.max_val = max_val;
        this.average = average;
        this.standard_dev = standard_dev;
    }

    /*************************************************************
     * Create a new SdasStats object from string based data.
     *
     * @param channel the SDAS channel number
     * @param date the date for the stats
     * @param hourly TRUE for hourly stats, FALSE for daily
     * @param n_points the number of points used to construct the stats
     * @param n_missing the number of missing points
     * @param min_val the minimum value
     * @param max_val the maximum value
     * @param average the average
     * @param standard_dev the standard deviation
     * @throws NumberFormatException is the strings contain bad data
     *************************************************************/
    public SdasStats (int channel, Date date, boolean hourly,
                      String n_points, String n_missing, String min_val,
                      String max_val, String average, String standard_dev)
    throws NumberFormatException
    {
        this.channel = channel;
        this.date = date;
        this.hourly = hourly;
        this.n_points = Integer.parseInt (n_points);
        this.n_missing = Integer.parseInt (n_missing);
        this.min_val = Integer.parseInt (min_val);
        this.max_val = Integer.parseInt (max_val);
        this.average = Double.parseDouble (average);
        this.standard_dev = Double.parseDouble (standard_dev);
    }

        /***************************************************************************
     * Gets the channel number for these stats
     *
     * @return the channel number
     **************************************************************************/
    public int GetChannel()
    {
        return channel;
    }
    
    /***************************************************************************
     * Sets the channel number for these stats
     *
     * @param channel The channel number
     **************************************************************************/
    public void SetChannel(int channel)
    {
        this.channel = channel;
    }
    
    /***************************************************************************
     * Gets the date for these stats
     *
     * @return the date
     **************************************************************************/    
    public Date GetDate()
    {
        return date;
    }

    /***************************************************************************
     * Sets the date for these stats
     *
     * @param date The date
     **************************************************************************/        
    public void SetDate(Date date)
    {
        this.date = date;
    }
    
    /***************************************************************************
     * Gets time type
     *
     * @return time type - TRUE for hourly stats, FALSE for daily stats
     **************************************************************************/    
    public boolean GetHourly()
    {
        return hourly;
    }
    
    /***************************************************************************
     * Sets time type 
     *
     * @param hourly The time type - TRUE for hourly stats, FALSE for daily stats 
     **************************************************************************/    
    public void SetHourly(boolean hourly)
    {
        this.hourly = hourly;
    }
    
    /***************************************************************************
     * Gets the number of points used to make these statistics
     *
     * @return the number of points
     **************************************************************************/    
    public int GetNPoints()
    {
        return n_points;
    }
    
    /***************************************************************************
     * Sets the number of points used to make these statistics
     *
     * @param n_points The number of points
     **************************************************************************/    
    public void SetNPoints(int n_points)
    {
        this.n_points = n_points;
    }

    /***************************************************************************
     * Gets the number of points missing
     *
     * @return the number of points
     **************************************************************************/    
    public int GetNMissing()
    {
        return n_missing;
    }
    
    /***************************************************************************
     * Sets the number of points missing
     *
     * @param n_missing The number of points
     **************************************************************************/    
    public void SetNMissing(int n_missing)
    {
        this.n_missing = n_missing;
    }

    /***************************************************************************
     * Gets the minimum value 
     *
     * @return the minimum value
     **************************************************************************/    
    public int GetMinVal()
    {
        return min_val;
    }

    /***************************************************************************
     * Sets the minimum value
     *
     * @param min_val The minimum value
     **************************************************************************/    
    public void SetMinVal(int min_val)
    {
        this.min_val = min_val;
    }
    
    /***************************************************************************
     * Gets the maximum value
     *
     * @return the maximum value
     **************************************************************************/    
    public int GetMaxVal()
    {
        return max_val;
    }

    /***************************************************************************
     * Sets the maximum value
     *
     * @param max_val The maximum value
     **************************************************************************/    
    public void SetMaxVal(int max_val)
    {
        this.max_val = max_val;
    }

    /***************************************************************************
     * Gets the average
     *
     * @return the average
     **************************************************************************/    
    public double GetAverage()
    {
        return average;
    }

    /***************************************************************************
     * Sets the average
     *
     * @param average The average
     **************************************************************************/    
    public void SetAverage(double average)
    {
        this.average = average;
    }

    /***************************************************************************
     * Gets the standard deviation
     *
     * @return the standard deviation
     **************************************************************************/    
    public double GetStandardDev()
    {
        return standard_dev;
    }

    /***************************************************************************
     * Sets the standard deviation
     *
     * @param standard_dev The standard deviation
     **************************************************************************/    
    public void SetStandardDev(double standard_dev)
    {
        this.standard_dev = standard_dev;
    }
    
    
}