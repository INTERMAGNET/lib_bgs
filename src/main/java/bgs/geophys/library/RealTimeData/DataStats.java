/*
 * Stats.java
 *
 * Created on 17 August 2003, 17:16
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;

/***************************************************************************
 * Stats is an interface for classes used to hold a single set of
 * statistics from a data system
 *
 * @author  Iesyn Evans
 **************************************************************************/
public interface DataStats {
    
    // publicly accessible methods - replaces original properties
    
    /***************************************************************************
     * Gets the channel number for these stats
     *
     * @return the channel number
     **************************************************************************/
    public int GetChannel();
    
    /***************************************************************************
     * Sets the channel number for these stats
     *
     * @param channel The channel number
     **************************************************************************/
    public void SetChannel(int channel);    
    
    /***************************************************************************
     * Gets the date for these stats
     *
     * @return the date
     **************************************************************************/    
    public Date GetDate();

    /***************************************************************************
     * Sets the date for these stats
     *
     * @param date The date
     **************************************************************************/        
    public void SetDate(Date date);
    
    /***************************************************************************
     * Gets time type
     *
     * @return time type - TRUE for hourly stats, FALSE for daily stats
     **************************************************************************/    
    public boolean GetHourly();
    
    /***************************************************************************
     * Sets time type 
     *
     * @param hourly The time type - TRUE for hourly stats, FALSE for daily stats 
     **************************************************************************/    
    public void SetHourly(boolean hourly);    
    
    /***************************************************************************
     * Gets the number of points used to make these statistics
     *
     * @return the number of points
     **************************************************************************/    
    public int GetNPoints();
    
    /***************************************************************************
     * Sets the number of points used to make these statistics
     *
     * @param n_points The number of points
     **************************************************************************/    
    public void SetNPoints(int n_points);

    /***************************************************************************
     * Gets the number of points missing
     *
     * @return the number of points
     **************************************************************************/    
    public int GetNMissing();
    
    /***************************************************************************
     * Sets the number of points missing
     *
     * @param n_missing The number of points
     **************************************************************************/    
    public void SetNMissing(int n_missing);

    /***************************************************************************
     * Gets the minimum value 
     *
     * @return the minimum value
     **************************************************************************/    
    public int GetMinVal();

    /***************************************************************************
     * Sets the minimum value
     *
     * @param min_val The minimum value
     **************************************************************************/    
    public void SetMinVal(int min_val);
    
    /***************************************************************************
     * Gets the maximum value
     *
     * @return the maximum value
     **************************************************************************/    
    public int GetMaxVal();

    /***************************************************************************
     * Sets the maximum value
     *
     * @param max_val The maximum value
     **************************************************************************/    
    public void SetMaxVal(int max_val);

    /***************************************************************************
     * Gets the average
     *
     * @return the average
     **************************************************************************/    
    public double GetAverage();

    /***************************************************************************
     * Sets the average
     *
     * @param average The average
     **************************************************************************/    
    public void SetAverage(double average);

    /***************************************************************************
     * Gets the standard deviation
     *
     * @return the standard deviation
     **************************************************************************/    
    public double GetStandardDev();

    /***************************************************************************
     * Sets the standard deviation
     *
     * @param standard_dev The standard deviation
     **************************************************************************/    
    public void SetStandardDev(double standard_dev);
    
}
