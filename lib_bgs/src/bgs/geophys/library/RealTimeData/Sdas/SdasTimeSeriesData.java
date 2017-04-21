
package bgs.geophys.library.RealTimeData.Sdas;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

import com.jcraft.jzlib.*;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.RealTimeData.*;


/*****************************************************************
 * The SdasTimeSeriesData class is designed as a storage system for
 * integer time series data and its sttributes.
 *
 * @author  S. Flower
 * @version 0.0
 *****************************************************************/
public class SdasTimeSeriesData extends Object implements TimeSeriesData {

    // private instance variables
    private Date date;          // date / time of the start of the data
    private int duration;       // length of the data held in this time series (in mS)
    private int sampleRate;     // sample rate in samples / hour
    private double sampleRateHz; // sample rate in Hz
    private double samplePeriodHz;
    private SdasChannel channelDetails;
    private boolean someDataMissing;  // flag to indicate if some of the
                                      // data points in the cache contain
                                      // the missing data value
    private int data [];        // the data array

    /*******************************************************************
     * Create a new time series data object, allocating the memory
     * needed for the data
     *
     * @param d        the starting date and time for the data
     * @param dur      the length of the data in milliseconds
     * @param sr       the sample rate for the data in samples / hour
     * @param cd       the channel details
     *******************************************************************/
    public SdasTimeSeriesData (Date d, int dur, int sr, DataChannel cd)
    {
        long arrayLength;

        // store instance variables
        date = d;
        duration = dur;
        sampleRate = sr;
        channelDetails = (SdasChannel) cd;
        sampleRateHz = sr / 3600.0;
        samplePeriodHz = 1.0 / sampleRateHz;
        someDataMissing = true;

        // allocate memory for the array
        arrayLength = ((long) dur * (long) sr) / 3600000l;
        data = new int [(int) arrayLength];
    }

    /*******************************************************************
     * Create a new time series data object, based on an exisitng
     * object, allocating the memory needed for the data
     *
     * @param tsd      the existing time series object
     *******************************************************************/
    public SdasTimeSeriesData (TimeSeriesData tsd)
    {
      long arrayLength;

      // store instance variables
      date = tsd.GetStartDate();
      duration = (int) tsd.GetDuration();
      sampleRate = tsd.GetSampleRateSPH();
      channelDetails = (SdasChannel) tsd.GetChannelDetails();
      sampleRateHz = sampleRate / 3600.0;
      samplePeriodHz = 1.0 / sampleRateHz;
      someDataMissing = tsd.IsSomeDataMissing();

      // allocate memory for the array
      arrayLength = ((long) duration * (long) sampleRate) / 3600000l;
      data = new int [(int) arrayLength];
    }
    
    /*******************************************************************
     *
     * Create a new empty time series data object, only call before a
     *              copy routine...
     *
     *******************************************************************/
    public SdasTimeSeriesData ()
    {
        // Do nothing...
    }

    /*******************************************************************
     * Copy the contents of one time series to another
     *
     * @param source the object to copy
     *******************************************************************/
    public void copy (TimeSeriesData source)
    {
      int data_ptr [], count;
      
      date = new Date (source.GetStartDate ().getTime ());
      duration = (int) source.GetDuration ();
      sampleRate = source.GetSampleRateSPH ();
      sampleRateHz = source.GetSampleRateHz ();
      samplePeriodHz = 1.0 / sampleRateHz;
      channelDetails = new SdasChannel ((SdasChannel) source.GetChannelDetails ());
      someDataMissing = source.IsSomeDataMissing();
      data_ptr = source.GetData ();
      data = new int [data_ptr.length];
      for (count=0; count<data_ptr.length; count++) data [count] = data_ptr [count];
    }
    
    /*******************************************************************
     * Fill a time series data array from an input stream.
     *
     * @param inputStream the input stream
     * @param mode - the transfer mode for the data, either SdasNet.DATA_BINARY,
     *               SdasNet.DATA_ASCII or SdasNet.DATA_ZLIB
     *
     * @throws IOException If there is an IO error on the stream.
     *******************************************************************/
    public void FillFromStream (DataInputStream inputStream, int mode)
    throws IOException

    {
        int count, n, new_data, index, i;
        int out_count = 0;
        int zlib_status = JZlib.Z_OK;
        int data_length = data.length * 4;
        boolean read_more = true;
        boolean eos = false;
        String line, test_string = "";
        BufferedReader bufferedReader;
        ZStream d_stream;

        byte [] inbuffer = new byte [data_length * 2];
        byte [] compressed = new byte [data_length * 2];
        byte [] uncompressed = new byte [data_length];

        if (mode == SdasNetV0_3.DATA_ZLIB)
        {
          // initialise decompression
          d_stream = new ZStream ();
          zlib_status = d_stream.inflateInit ();
          if (zlib_status != JZlib.Z_OK)
            throw new ZStreamException ("Zlib error: " + JZlibErrToString (zlib_status));

          // set variables for decompression
          d_stream.avail_out = data_length;
          d_stream.next_out = uncompressed;
          d_stream.next_out_index = 0;
          d_stream.total_out = 0;

          index = 0;
          while (!eos)
          {
            // It is not possible to have while (inputStream.available () > 0)
            // at start of loop because it appears that data is not always
            // available even though more is required. read () will block until
            // it becomes available.
            // read as much data as possible into inbuffer
            new_data = inputStream.read (inbuffer);
            if (new_data < 0) throw new ZStreamException ("end of input stream");
            // copy into compressed buffer for decompression
            for (i = 0; i < new_data; i++)
              compressed [index + i] = inbuffer [i];
            index += new_data;
            // check if the last 5 characters in the compressed buffer match
            // the string "OKOK\n". This is sent only when zlib data is being
            // transferred in order to give this code an indication of when there
            // is no more data expected.
            if (index >= 5)
            {
              test_string = new String (compressed, index -5, 5);
              if (test_string.equals ("OKOK\n"))
              {
                eos = true;
                // decrement the number of bytes required for decompression
                index -= 5;
              }
            }
          }

          // decompress data in buffer
          d_stream.avail_in = index;
          d_stream.next_in = compressed;
          d_stream.next_in_index = 0;

          zlib_status = d_stream.inflate (JZlib.Z_SYNC_FLUSH);
          if (zlib_status != JZlib.Z_OK && zlib_status != JZlib.Z_STREAM_END)
            throw new ZStreamException ("JZlib error: " + JZlibErrToString (zlib_status));

          d_stream.inflateEnd ();

          if (d_stream.total_out != data_length)
            throw new ZStreamException ("JZlib error: incorrect number of bytes obtained in decompression " +
                                        data_length + " expected, " + d_stream.total_out + " obtained");

          // convert uncompressed data to integers
          for (count = 0; count < d_stream.total_out; count += 4)
          {
            data [out_count] = Utils.bytesToIntBE (uncompressed, count);
            out_count ++;
          }
        }

        else if (mode == SdasNetV0_3.DATA_BINARY)
        {
          for (count=0; count<data.length; count++)
          {
            data [count] = inputStream.readInt ();
          }
        }

        else if (mode == SdasNetV0_3.DATA_ASCII)
        {
          // data is separated by newline characters so
          // convert stream to BufferedReader to enable
          // reading of lines
          bufferedReader = new BufferedReader (new InputStreamReader (inputStream));
          for (count=0; count<data.length; count++)
          {
            line = bufferedReader.readLine ();
            try
            {
              // byte order is reversed for ascii data
              data [count] = Integer.parseInt (line);
            }
            catch (NumberFormatException e)
            {
              throw new IOException (e.toString ());
            }
          }
        }
    }

    /*******************************************************************
     * Trim the data array to a new date/duration. Only call this routine
     * after calling DoTimesOverlap to ensure that there is an
     * overlap between the current data and the new date/duration.
     *
     * @param newDate the new start date for this time series
     * @param newDuration the new duration for this time series
     *
     * @return true if data was removed, false otherwise
     *******************************************************************/
    public boolean TrimToDate (Date newDate, long newDuration)
    {
        
        int offset, nSamplesRemovedAtFront, nSamplesRemovedAtEnd, oldData [];
        int count, count2, nSamplesRemoved;

        // work out the new start date and the number of samples to
        // remove from the front of the data array
        offset = (int) (newDate.getTime () - date.getTime ());
        if (offset > 0)
        {
            nSamplesRemovedAtFront = (int) (((long) offset * (long) sampleRate) / 3600000l);
            date.setTime (date.getTime () + offset);
            duration -= offset;
        }
        else nSamplesRemovedAtFront = 0;
        
        // work out the new duration and the number of samples
        // to remove from the end of the array
        offset = (int) (GetEndDate ().getTime () - (newDate.getTime () + (long) newDuration));
        if (offset > 0)
        {
            nSamplesRemovedAtEnd = (int) (((long) offset * (long) sampleRate) / 3600000l);
            duration -= offset;
        }
        else nSamplesRemovedAtEnd = 0;

        // create the new array and move the data to it
        nSamplesRemoved = nSamplesRemovedAtFront + nSamplesRemovedAtEnd;
        if (nSamplesRemoved > 0)
        {
            oldData = data;
            data = new int [oldData.length - nSamplesRemoved];
            for (count=0, count2=nSamplesRemovedAtFront; count<oldData.length - nSamplesRemoved; count++, count2++)
                data [count] = oldData [count2];
        }
          
        // work out what to return
        if (nSamplesRemoved > 0) return true;
        return false;
        
    }
    
    /*******************************************************************
     * Merge the data from another time series before the data from this
     * time series.
     *
     * @param newData the data to merge
     *
     * @throws SDASNetException if the data is not contiguous or the
     *         sample rates are not the same
     *******************************************************************/
    public void MergeBefore (TimeSeriesData newData)
    throws SdasNetException
    {
        int oldData [], count, count2;
        
        // check that the time series can be merged
        if (GetSampleRateSPH () != newData.GetSampleRateSPH ())
            throw new SdasNetException (SdasNetException.SAMPLE_RATE_CHANGES, "Sample rate changes during data");
        if (! newData.IsContiguousBefore (this))
            throw new SdasNetException (SdasNetException.DATA_MUST_BE_CONTIGUOUS, "Data objects must be contiguous in order to be merged");

        // create the new array and move the data to it
        oldData = data;
        data = new int [oldData.length + ((SdasTimeSeriesData)newData).data.length];
        for (count=0; count<((SdasTimeSeriesData)newData).data.length; count++)
            data [count] = ((SdasTimeSeriesData)newData).data [count];
        for (count=0, count2=((SdasTimeSeriesData)newData).data.length; count<oldData.length; count++, count2++)
            data [count2] = oldData [count];
        date.setTime (date.getTime () - ((SdasTimeSeriesData)newData).duration);
        duration += ((SdasTimeSeriesData)newData).duration;
    }
    
    /*******************************************************************
     * Merge the data from another time series before the data from this
     * time series.
     *
     * @param newData the data to merge
     *
     * @throws SDASNetException if the data is not contiguous or the
     *         sample rates are not the same
     *******************************************************************/
    public void MergeAfter (TimeSeriesData newData)
    throws SdasNetException
    {
        int oldData [], count, count2;

        // check that the time series can be merged
        if (GetSampleRateSPH () != newData.GetSampleRateSPH ())
            throw new SdasNetException (SdasNetException.SAMPLE_RATE_CHANGES, "Sample rate changes during data");
        if (! IsContiguousBefore (newData))
            throw new SdasNetException (SdasNetException.DATA_MUST_BE_CONTIGUOUS, "Data objects must be contiguous in order to be merged");

        // create the new array and move the data to it
        oldData = data;
        data = new int [oldData.length + ((SdasTimeSeriesData)newData).data.length];
        for (count=0; count<oldData.length; count++)
            data [count] = oldData [count];
        for (count=0, count2=oldData.length; count<((SdasTimeSeriesData)newData).data.length; count++, count2++)
            data [count2] = ((SdasTimeSeriesData)newData).data [count];
        duration += ((SdasTimeSeriesData)newData).duration;
    }
    
    /********************************************************************
     * Get a reference to the data in this cache.
     *
     * @return the data array, which will be null if no data is loaded
     ********************************************************************/
    public int [] GetData ()
    {
        return data;
    }

    /*******************************************************************
     * Return the start date for this time series data object.
     *
     * @return the start date
     *******************************************************************/
    public Date GetStartDate ()
    {
        return new Date (date.getTime ());
    }
    
    /*******************************************************************
     * Return the duration for this time series data object.
     *
     * @return the duration in milliseconds
     *******************************************************************/
    public long GetDuration ()
    {
        return (long) duration;
    }
    
    /*******************************************************************
     * Return the end date for this time series data object.
     *
     * @return the end date
     *******************************************************************/
    public Date GetEndDate ()
    {
        return new Date (date.getTime () + duration);
    }
    
    /********************************************************************
     * Tests whether this object is contiguous and immediately before
     * the given object.
     *
     * @param testObj the object to test against
     * @return true if the objects are contiguous, false otherwise
     ********************************************************************/
    public boolean IsContiguousBefore (TimeSeriesData testObj)

    {
        if (GetEndDate().equals (((SdasTimeSeriesData)testObj).date)) return true;
        return false;
    }
    
    /********************************************************************
     * Calculates whether the time window for this time series intersects
     * the given time window.
     *
     * @param testDate the start of the time window to test against
     * @param testDuration the duration (in mS) of the time window to test against
     * @return true if the two windows intersect, false otherwise
     ********************************************************************/
    public boolean DoTimesOverlap (Date testDate, long testDuration)
    {
        long objStartTime, objEndTime, testStartTime, testEndTime;

        objStartTime = objEndTime = date.getTime ();
        objEndTime += duration;
        testStartTime = testEndTime = testDate.getTime ();
        testEndTime += testDuration;

        if (testEndTime < objStartTime) return false;
        if (testStartTime > objEndTime) return false;
        return true;
    }

    /*********************************************************************
     * Get the sample rate for this time series object (in samples / hour).
     *
     * @return the sample rate in samples per hour
     *********************************************************************/
    public int GetSampleRateSPH ()
    {
        return sampleRate;
    }

    /*********************************************************************
     * Get the sample rate for this time series object (in samples / second).
     *
     * @return the sample rate in Hz
     *********************************************************************/
    public double GetSampleRateHz ()
    {
        return sampleRateHz;
    }

    /*********************************************************************
     * Get the channel details associated with this time series.
     *
     * @return the channel details
     *********************************************************************/
    public DataChannel GetChannelDetails ()
    {
        return channelDetails;
    }

    /*********************************************************************
     * Set the flag to indicate if some data is missing in the cache
     *
     * @param the new boolean value for the flag
     *********************************************************************/
    public void SetSomeDataMissing (boolean miss)
    {
        someDataMissing = miss;
    }

    /*********************************************************************
     * Get the flag which indicates if some data is missing in the cache
     *
     * @return the flag
     *********************************************************************/
    public boolean IsSomeDataMissing ()
    {
        return someDataMissing;
    }
    
    /*********************************************************************
     * Get the error string associated with a particular JZlib error code
     *
     * @param the error code
     * @return the error string
     *********************************************************************/
    public String JZlibErrToString (int code)
    {
      String string;
      switch (code)
      {
        case JZlib.Z_OK:
          string = "JZlib status OK";
          break;
        case JZlib.Z_STREAM_END:
          string = "JZlib: end of stream";
          break;
        case JZlib.Z_NEED_DICT:
          string = "JZlib: need dictionary";
          break;
        case JZlib.Z_ERRNO:
          string = "JZlib: file system error";
          break;
        case JZlib.Z_STREAM_ERROR:
          string = "JZlib: data stream error";
          break;
        case JZlib.Z_DATA_ERROR:
          string = "JZlib: data error";
          break;
        case JZlib.Z_MEM_ERROR:
          string = "JZlib: memory error";
          break;
        case JZlib.Z_BUF_ERROR:
          string = "JZlib: buffer error";
          break;
        case JZlib.Z_VERSION_ERROR:
          string = "JZlib: version error";
          break;
        default:
          string = "JZlib: unknown error code " + code;
          break;
      }
      return string;
    }

}