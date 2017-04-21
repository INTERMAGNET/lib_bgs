/*
 * TimeSeriesData.java
 *
 * Created on 17 August 2003, 17:16
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;
import java.io.*;


/*****************************************************************
 * The TimeSeriesData class is designed as a storage system for
 * integer time series data and its attributes.
 *
 * @author  Iestyn Evans
 *****************************************************************/
public interface TimeSeriesData {
    
    /*******************************************************************
     * Copy the contents of one time series to another
     *
     * @param source the object to copy
     *******************************************************************/
    public void copy (TimeSeriesData source);
    
    /*******************************************************************
     * Fill a time series data array from an input stream.
     *
     * @param inputStream the input stream which must contain binary
     *                    integer data
     *
     * @throws IOException If there is an IO error on the stream.
     *******************************************************************/
   // public void FillFromStream (DataInputStream inputStream);
    
    /*******************************************************************
     * Trim the data array to a new date/duration.
     *
     * @param newDate the new start date for this time series
     * @param newDuration the new duration for this time series
     *
     * @return true if data was removed, false otherwise
     *******************************************************************/
    public boolean TrimToDate (Date newDate, long newDuration);
    
    /*******************************************************************
     * Merge the data from another time series before the data from this
     * time series.
     *
     * @param newData the data to merge
     *******************************************************************/
    public void MergeBefore (TimeSeriesData newData)
    throws DataNetException;
    
    /*******************************************************************
     * Merge the data from another time series before the data from this
     * time series.
     *
     * @param newData the data to merge
     *******************************************************************/
    public void MergeAfter (TimeSeriesData newData)
    throws DataNetException;
    
    /********************************************************************
     * Get the data in this cache.
     *
     * @return a data array, which will be null if no data is loaded
     ********************************************************************/
    public int [] GetData ();
    
    /*******************************************************************
     * Return the start date for this time series data object.
     *
     * @return the start date
     *******************************************************************/
    public Date GetStartDate ();
    
    /*******************************************************************
     * Return the duration for this time series data object.
     *
     * @return the duration in milliseconds
     *******************************************************************/
    public long GetDuration ();
    
    /*******************************************************************
     * Return the end date for this time series data object.
     *
     * @return the end date
     *******************************************************************/
    public Date GetEndDate ();

    /********************************************************************
     * Tests whether this object is contiguous and immediately before
     * the given object.
     *
     * @param testObj the object to test against
     * @return true if the objects are contiguous, false otherwise
     ********************************************************************/
    public boolean IsContiguousBefore (TimeSeriesData testObj);
    
    /********************************************************************
     * Calculates whether the time window for this time series intersects
     * the given time window.
     *
     * @param testDate the start of the time window to test against
     * @param testDuration the duration (in mS) of the time window to test against
     * @return true if the two windows intersect, false otherwise
     ********************************************************************/
    public boolean DoTimesOverlap (Date testDate, long testDuration);    

    /*********************************************************************
     * Get the sample rate for this time series object (in samples / hour).
     *
     * @return the sample rate in samples per hour
     *********************************************************************/
    public int GetSampleRateSPH ();
    
    /*********************************************************************
     * Get the sample rate for this time series object (in samples / second).
     *
     * @return the sample rate in Hz
     *********************************************************************/
    public double GetSampleRateHz ();
    
    /*********************************************************************
     * Get the channel details associated with this time series.
     *
     * @return the channel details
     *********************************************************************/
    public DataChannel GetChannelDetails ();
    
    /*********************************************************************
     * Set the flag to indicate if some data is missing in the cache
     *
     * @param the new boolean value for the flag
     *********************************************************************/
    public void SetSomeDataMissing (boolean miss);
    
    /*********************************************************************
     * Get the flag which indicates if some data is missing in the cache
     *
     * @return the flag
     *********************************************************************/
    public boolean IsSomeDataMissing ();    
    
}
