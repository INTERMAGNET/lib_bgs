/*
 * TimeSeriesDataCache.java
 *
 * Created on 25 August 2003, 18:07
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;
import java.io.*;

import bgs.geophys.library.Misc.*;

/*****************************************************************
 * The TimeSeriesDataCache object provides loading, storing and
 * retrieval facilities for TimeSeriesData objects. The
 * TimeSeriesDataObjects are treated as segments of a continuous
 * data record. Note that for multi-channel operations you need
 * multiple TimeSeriesDataCache objects - each object stores the
 * data for a single channel only.
 *
 * @author  S. Flower
 * @version 0.0
 *****************************************************************/
public class TimeSeriesDataCache extends Object {

    private int channelIndex;       // the EW channel that this cache holds data for
    private TimeSeriesData cache;   // the cache of time series data
    private AnnotationString annotation_string;
    private Range range;            // statistics for the data segment

    /*******************************************************************
     * Create a new TimeSeriesDataCache
     *
     * @param ci the index of the EW channel that this object 
     *           will cache data for
     *******************************************************************/
    public TimeSeriesDataCache(int ci)
    {
        channelIndex = ci;
        cache = null;
        annotation_string = null;
    }

    /*******************************************************************
     * Copy the contents of one cache to another
     *
     * @param source the object to copy
     *******************************************************************/
    public void copy (TimeSeriesDataCache source)
    {
      TimeSeriesData source_cache;
      AnnotationString source_annotation;
      
      channelIndex = source.GetChannelIndex ();
      source_cache = source.GetCache ();
      if (source_cache == null) cache = null;
      else
      {
        if (cache == null)
        {
            Class cacheClass = source_cache.getClass();
            try
            {
                cache = (TimeSeriesData) cacheClass.newInstance();
            }
            catch (Exception e)
            {
                // bail - may cause problems...
                System.err.println(e.getMessage());
                return;
                //throw e;
            }
        }
        cache.copy (source_cache);
      }
      source_annotation = source.GetAnnotation ();
      if (source_annotation != null)
      {
          annotation_string = new AnnotationString (source_annotation);
      }
      
      if (source.range != null) range = new Range (source.range);
    }

    /*******************************************************************
     * Loads the cache with the data for the requested channel, date/time
     * and duration. Current data segments that are no longer needed
     * are destroyed. New data segments are created and loaded.
     *
     * @param sdasNet  the connection to an SDAS server
     * @param date     the start date for the data
     * @param duration the duration for the data in mS
     *
     * @return true if data was added or removed, false otherwise
     *
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood
     *                              or the sample rate / channel details
     *                              change during the given time window.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     *******************************************************************/
    public boolean LoadCache (DataNet net, Date date, int duration)
    throws DataNetException, IOException
    {
        int count;
        long seg_duration;
        boolean new_data_found = false;
        String string;
        TimeSeriesData tsdBefore, tsdAfter;

        // check if we can delete the current cache completely
        if (cache != null)
        {
            if (! cache.DoTimesOverlap (date, duration))
            {
              cache = null;
              new_data_found = true;
            }
        }
        
        // if the cache is empty or if some data is missing
        // from the existing cache, load the entire data set.
        // if the time since the last data was loaded is shorter than the sample rate
        // then load the whole segment - this will stop errors occuring when
        // loading minute data alongside data with a higher sample rate.

        if (cache == null || cache.IsSomeDataMissing () ||
           ((cache.GetStartDate ().getTime () - date.getTime ()) < (cache.GetSampleRateHz () * 1000)))
        {
            try
            {
                cache = net.GetData (channelIndex, date, duration);
                new_data_found = true;
            }
            catch (DataNetException e)
            {
//                if (e.isDataMissingCode())
//                {
//                    cache = null;
//                }
//                else
//                {
                    throw e;
//                }
            }
        }
        else
        {
            // remove the unwanted data from the cache
            if (cache.TrimToDate (date, duration)) new_data_found = true;
        
            // load data needed before and after the cache
            tsdAfter = tsdBefore = null;
            seg_duration = cache.GetStartDate ().getTime () - date.getTime ();
            if (seg_duration > 0)
            {
                try
                {
                    tsdBefore = net.GetData (channelIndex, date, (int) seg_duration);
                }
                catch (DataNetException e)
                {
                    if (e.isDataMissingCode())
                    {
                        tsdBefore = null;
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
            seg_duration = (long) duration - (cache.GetEndDate ().getTime () - date.getTime ());
            if (seg_duration > 0)
            {
                try
                {
                    tsdAfter = net.GetData(channelIndex, cache.GetEndDate(), (int) seg_duration);
                }
                catch (DataNetException e)
                {
                    if (e.isDataMissingCode())
                    {
                        tsdAfter = null;
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        
            // merge the before and after segments into the main cache
            if (tsdBefore != null)
            {
              cache.MergeBefore (tsdBefore);
              new_data_found = true;
            }
            if (tsdAfter != null)
            {
              cache.MergeAfter (tsdAfter);
              new_data_found = true;
            }
            tsdAfter = tsdBefore = null;
        }

        // calculate statsitics for this data
        if (cache == null) annotation_string = null;
        else
        {
            range = new Range (DataNet.MISSING_DATA_VALUE);
            range.calc_range (cache.GetData ());
            // use the values calculated in range to find out if there
            // are any data points missing from the cache
            // this will be used on the next load to find decide if
            // the entire data should be reloaded
            if (range.get_n_valid_points () != cache.GetData ().length)
              cache.SetSomeDataMissing (true);
            else
              cache.SetSomeDataMissing (false);

            // if an annotation string has already been set, then recreate it
            // using the new data we have just loaded
            if (annotation_string != null)
            {
                string = annotation_string.GetRawString ();
                
                annotation_string = new AnnotationString (string, range, 
                                                          cache.GetChannelDetails().GetChannel(),
                                                          cache.GetChannelDetails().GetName(),
                                                          cache.GetChannelDetails().GetType(),
                                                          cache.GetChannelDetails().GetUnits());
            }
        }
        
        // finally run the garbage collector to make sure memory is returned to the pool
        System.gc ();
        
        return new_data_found;
    }

    /********************************************************************
     * Set the annotation for this channel. This function must be called
     * after LoadCache() to set the annotation for the channel.
     *
     * @param string the annotation (from the user's dialog)
     ********************************************************************/
    public void SetAnnotation (String string)
    
    {

      boolean set_string;
      
      if (cache == null) set_string = false;
      else if (annotation_string == null) set_string = true;
      else if (string.equals (annotation_string.GetRawString())) set_string=  false;
      else set_string = true;
      
      if (set_string)
      
        annotation_string = new AnnotationString (string, range, 
                                                  cache.GetChannelDetails().GetChannel(),
                                                  cache.GetChannelDetails().GetName(),
                                                  cache.GetChannelDetails().GetType(),
                                                  cache.GetChannelDetails().GetUnits());
      
    }
    
    /********************************************************************
     * Get this caches' start date, which may be different to the
     * requested start date if data was unavailable.
     *
     * @return the start date
     ********************************************************************/
    public Date GetStartDate ()
    {
        return cache.GetStartDate ();
    }
    
    /********************************************************************
     * Get this caches' duration, which may be different to the
     * requested duration if data was unavailable.
     *
     * @return the duration in milliseconds
     ********************************************************************/
    public long GetDuration ()
    {
        return cache.GetDuration ();
    }

    /********************************************************************
     * Get this caches' end date, which may be different to the
     * requested end date if data was unavailable.
     *
     * @return the start date
     ********************************************************************/
    public Date GetEndDate ()
    {
        return cache.GetEndDate ();
    }
    
    /*********************************************************************
     * Get this caches' sample rate.
     *
     * @return the sample rate in Hz
     *********************************************************************/
    public double GetSampleRateHz ()
    {
        return cache.GetSampleRateHz ();
    }

    /********************************************************************
     * Get a reference to the data in this cache.
     *
     * @return the data array, which will be null if no data is loaded
     ********************************************************************/
    public int [] GetData ()
    {
        if (cache == null) return null;
        return cache.GetData ();
    }

    /********************************************************************
     * Report whether data has been loaded into this timer series.
     *
     * @return true if data has ben loaded, false otherwise
     ********************************************************************/
    public boolean IsDataLoaded ()
    {
        if (cache == null) return false;
        return true;
    }

    /********************************************************************
     * Get a reference to the annotation object for this data.
     *
     * @return the annotation object, which may be null if SetAnnotation
     *         has not been called
     ********************************************************************/
    public AnnotationString GetAnnotation ()
    {
        return annotation_string;
    }
    
    /********************************************************************
     * Get a reference to the range object for this data.
     *
     * @return the range object
     ********************************************************************/
    public Range GetRange ()
    {
        return range;
    }

    /********************************************************************
     * Get a the channel index this data is for.
     *
     * @return the channel index
     ********************************************************************/
    public int GetChannelIndex ()
    {
        return channelIndex;
    }

    /********************************************************************
     * Get the underlying data cache.
     *
     * @return the channel index
     ********************************************************************/
    public TimeSeriesData GetCache ()
    {
        return cache;
    }
}
