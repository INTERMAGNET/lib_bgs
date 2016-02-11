
package bgs.geophys.library.RealTimeData.Earthworm;

import java.util.*;
import java.io.*;
import java.text.*;
import bgs.geophys.library.RealTimeData.*;


/**************************************************************************
 * The TimeSeriesData class is designed as a storage system for integer
 *                      time series data and its attributes.
 *
 * @author S.Flower, Iestyn Evans
 *************************************************************************/
public class EWTimeSeriesData extends Object implements TimeSeriesData {
    // Constants
    static final int TRACE_STA_LEN = 7;
    static final int TRACE_NET_LEN = 9;
    static final int TRACE_CHAN_LEN = 9;
    static final int SIZE_OF_DOUBLE = 8;
    static final int GAP_VALUE = 0x79797979;
    
    // private instance variables
    private Date date;          // date / time of the start of the data
    private long duration;     // length of the data held in this time series (in mS)
    private Date endDate;      // Provided for convenience
    private double sampleRate;   // sample rate in samples / hour
    private double samplePeriodHz;
    private EWChannel channelDetails;
    private boolean someDataMissing;  // flag to indicate if some of the
                                      // data points in the cache contain
                                      // the missing data value
    
    private int[] data;       // the data structure
    private int dataIndex;              // The index that the array has been filled to
    
    // Additional varaibles needed to allow the combining of EWTracePacket
    // and EWTimeSeriesData
    private String packetDataType;
    private int packetNoSamp;
    private byte[] packetBinaryHeader;            // Holds binary data from a raw request.
    private int packetPinNo;
    private String packetSiteName;
    private String packetChannelName;
    private String packetNetworkID;
    private String packetQuality;
    private Date packetDate;
    private Date packetEndDate;
    private double packetSampleRate;
    private int responseExpectedSamples;
    private long responseExpectedDuration;
    
    /**************************************************************************
     * Create a new time series data object, only use before a copy operation.
     *
     *************************************************************************/
    public EWTimeSeriesData()
    {
            dataIndex = 0;
    }
    
    /**************************************************************************
     * Create a new time series data object, next fill using fillFromStream().
     *
     * @param channel The channel data for the time series.
     *************************************************************************/
    public EWTimeSeriesData(DataChannel channel)
    {
        channelDetails = (EWChannel) channel;
        
        // Create some empty objects for safety
            packetDataType = "";
            packetSiteName = "";
            packetChannelName = "";
            packetNetworkID = "";
            packetQuality = "";
            dataIndex = 0;
    }
        
    /**************************************************************************
     * Create a new time series data object, based on an exisitng object,
     *                      allocating the memory needed for the data.
     *
     * @param tsd The existing time series object.
     *************************************************************************/
    public EWTimeSeriesData(EWTimeSeriesData tsd)
    {
      long arrayLength;

      // store instance variables
      date = tsd.GetStartDate();
      duration = tsd.GetDuration();
      endDate = tsd.GetEndDate();
      sampleRate = tsd.GetSampleRateHz();
      channelDetails = (EWChannel) tsd.GetChannelDetails();
      samplePeriodHz = 1.0 / sampleRate;
      someDataMissing = tsd.IsSomeDataMissing();
      
      // allocate memory for the array
      data = new int [tsd.data.length];
    }
    
    /**************************************************************************
     * Copy the contents of one time series to another
     *
     * @param source the object to copy
     *************************************************************************/
    public void copy (TimeSeriesData source)
    {
      int int_data_ptr [], count;
      
      date = source.GetStartDate ();
      duration = source.GetDuration ();
      endDate = source.GetEndDate();
      sampleRate = source.GetSampleRateHz ();
      samplePeriodHz = 1.0 / sampleRate;
      channelDetails = new EWChannel ((EWChannel) source.GetChannelDetails ());
      someDataMissing = source.IsSomeDataMissing();
      
      int_data_ptr = ((EWTimeSeriesData)source).data;
      
      data = new int [int_data_ptr.length];
      for (count=0; count<((EWTimeSeriesData)source).dataIndex; count++) data [count] = int_data_ptr [count];
      dataIndex = ((EWTimeSeriesData)source).dataIndex;
      
    }
    
    /**************************************************************************
     * Fill a time series data array from an input stream located at the start
     *                      of a Trace_Buf structure.
     *
     * @param inputStream The input stream which must contain binary integer
     *                      data.
     *
     * @throws IOException If there is an IO error on the stream.
     *
     * @return The number of bytes read from the DataInputStream.
     **************************************************************************/
    public void FillFromStream (DataInputStream inputStream, String hDataType, int bytesToRead, long requestDuration)
    throws IOException, EWNetException
    {
        // Make space for data
        responseExpectedSamples = 0;            // At the moment we don't know the
                                                // sample rate so can't make a guess
        responseExpectedDuration = requestDuration; // The duration from the request
        
        data = new int[(int)bytesToRead /2];    // At the moment all we know are
                                                // the number of bytes to read. At
                                                // worst the data is 2 bytes to an
                                                // integer so we have enough room
                                                // for all the data coming. However
                                                // there may be gap data not taken into
                                                // acount....
        
        int bytesRead = 0;                      // Number of bytes read so far
        int packetsRead = 0;                    // Number of packets read so far

        while (bytesRead < bytesToRead) 
        {    
            // Loop while there are still packets to read
            
            // Read in the packet header
            bytesRead += getTrace_Header(inputStream, hDataType);
            
            if (packetsRead == 0)
            {
                // Here if the first packet...
                
                // Check that enough room for data has been set aside
                sampleRate = packetSampleRate; // This sets the sample rate for the series
                responseExpectedSamples =  (int)(responseExpectedDuration /(1000 * sampleRate));
                if (responseExpectedSamples > data.length) {
                    // We are expecting more samples than our original guess
                    data = new int[(int)(responseExpectedSamples * 1.1)]; // Adding a little extra space for safety
                }

                // Create new Date objects
                date = new Date(packetDate.getTime());
                endDate = new Date(packetEndDate.getTime());
                duration = endDate.getTime() - date.getTime();
                
                // Note: if there is gap data at the start of the request
                // eg we have asked for data earlier than the available data
                // this code will not pick it up. Instead perhaps the GetEWData
                // function should deal with it...
            }
            else
            {
                // Here if not the first packet...
                
                // Check that the sample rate is the same
                if (sampleRate != packetSampleRate)
                    throw new EWNetException(EWNetException.SAMPLE_RATE_CHANGES, "The sample rate changes between packets.");
                
                // Check wether gap data needs to be inserted
                if (!IsContiguousBefore(packetDate)) {
                    addGapData(packetDate.getTime() - endDate.getTime() - (long)(samplePeriodHz * 1000), GAP_VALUE);
                }
                // Update the Series times / duration to reflect the new packet
                endDate.setTime(packetEndDate.getTime());
                duration = endDate.getTime() - date.getTime();
            }
            
            // Add the new data...
            bytesRead += getTraceData(inputStream);
            packetsRead++;
        }
    }

    /**************************************************************************
     * Trim the data array to a new date/duration. Only call this routine
     *                      after calling DoTimesOverlap to ensure that there
     *                      is an overlap between the current data and the new
     *                      date/duration.
     *
     * @param newDate The new start date for this time series.
     * @param newDuration The new duration for this time series.
     *
     * @return true if data was removed, false otherwise.
     *************************************************************************/
    public boolean TrimToDate (Date newDate, long newDuration)
    {
        // This function may need some tweaking...
        
        long offset;
        int nSamplesRemovedAtFront, nSamplesRemovedAtEnd, olddata [];
        int count, count2, nSamplesRemoved;

        // work out the new start date and the number of samples to
        // remove from the front of the data array
        offset = newDate.getTime() - date.getTime();
        if (offset > 0.0)
        {
            nSamplesRemovedAtFront = (int) (offset * sampleRate / 1000);
            date.setTime(date.getTime() + ((long) (nSamplesRemovedAtFront / sampleRate * 1000))) ;
            duration -= offset;
        }
        else nSamplesRemovedAtFront = 0;
        
        // work out the new duration and the number of samples
        // to remove from the end of the array
        offset = (endDate.getTime() - (newDate.getTime() + newDuration));
        if (offset > 0)
        {
            nSamplesRemovedAtEnd = (int) (offset * sampleRate / 1000);
            endDate.setTime(endDate.getTime() - ((long) (nSamplesRemovedAtEnd / sampleRate * 1000 )));
            duration -= offset;
        }
        else nSamplesRemovedAtEnd = 0;

        duration = endDate.getTime() - date.getTime();

        nSamplesRemoved = nSamplesRemovedAtFront + nSamplesRemovedAtEnd;        
        
        
        // Move data in the array to reflect the changes
        
        if (nSamplesRemoved > 0)
        {
            for (count=0, count2=nSamplesRemovedAtFront; count<dataIndex - nSamplesRemoved; count++, count2++)
                data [count] = data [count2];
        }
        dataIndex -= nSamplesRemoved;

        // work out what to return
        if (nSamplesRemoved > 0) return true;
        return false;
    }
    
    /**************************************************************************
     * Merge the data from another time series before the data from this
     *                      time series.
     *
     * @param newData The data to merge.
     *
     * @throws EWNetException If the data is not contiguous or the
     *                      sample rates are not the same.
     *************************************************************************/
    public void MergeBefore (TimeSeriesData newData)
    throws EWNetException
    {
        EWTimeSeriesData newEWData = (EWTimeSeriesData) newData;
        int olddata [], count, count2; 
        
        // check that the time series can be merged
        if (GetSampleRateHz () != newData.GetSampleRateHz ())
            throw new EWNetException (EWNetException.SAMPLE_RATE_CHANGES, "Sample rate changes during data");
        if (! newData.IsContiguousBefore (this))
            throw new EWNetException (EWNetException.DATA_MUST_BE_CONTIGUOUS, "Data objects must be contiguous in order to be merged");

        if (dataIndex + newEWData.dataIndex >= data.length)
        {
            // create the new array and move the data to it
            olddata = data;
            data = new int [(int)((dataIndex + newEWData.dataIndex) * 1.1)];
            for (count=0; count<newEWData.dataIndex; count++)
                data [count] = newEWData.data [count];
            for (count=0, count2=newEWData.dataIndex; count<dataIndex; count++, count2++)
                data [count2] = olddata [count];
        }
        else
        {
            // Move existing data through the array and add new data at the front
         for (count=dataIndex + newEWData.dataIndex - 1; count>=newEWData.dataIndex; count--)
             data[count] = data[count - newEWData.dataIndex];
         for (count=0; count<newEWData.dataIndex; count++)
             data[count] = newEWData.data[count];
        }
            
        dataIndex += newEWData.dataIndex;
        
        date.setTime(newEWData.date.getTime());
        duration = endDate.getTime() - date.getTime();
    }
    
    /**************************************************************************
     * Merge the data from another time series before the data from this
     *                      time series.
     *
     * @param newData The data to merge.
     *
     * @throws EWNetException if the data is not contiguous or the
     *                      sample rates are not the same.
     **************************************************************************/
    public void MergeAfter (TimeSeriesData newData)
    throws EWNetException
    {
        int olddata [], count, count2;
        EWTimeSeriesData newEWData = (EWTimeSeriesData) newData;
        
        
        // check that the time series can be merged
        if (GetSampleRateHz () != newEWData.GetSampleRateHz ())
            throw new EWNetException (EWNetException.SAMPLE_RATE_CHANGES, "Sample rate changes during data");
        if (! IsContiguousBefore (newData))
            throw new EWNetException (EWNetException.DATA_MUST_BE_CONTIGUOUS, "Data objects must be contiguous in order to be merged");

        
        if (dataIndex + newEWData.dataIndex >= data.length)
        {
            // create the new array and move the data to it
            olddata = data;
            data = new int [(int)((dataIndex + newEWData.dataIndex) * 1.1)];
            for (count=0; count<dataIndex; count++)
                data [count] = olddata [count]; 
            for (count=0, count2=dataIndex; count<newEWData.dataIndex; count++, count2++)
                data [count2] = newEWData.data [count];
            dataIndex += newEWData.dataIndex;
        }
        else
        {
            for (count=0; count<newEWData.dataIndex; count++)
            {
                data[dataIndex] = newEWData.data[count];
                dataIndex++;
            }
        }
        
        endDate.setTime(newEWData.endDate.getTime());
        duration = endDate.getTime() - date.getTime();
    }
    
    /**************************************************************************
     * Get a reference to the data in this cache.
     *
     * @return The data array, which will be null if no data is loaded.
     *************************************************************************/
    public int [] GetData ()
    {
        // Perhaps return a resized array containing only samples???
        
        int[] resizedData = new int[dataIndex];
        
        // This is used for safety - hopefully it shouldn't happen
        if (dataIndex > data.length)
            return data;
        
        for (int count = 0; count < dataIndex; count++)
        {
            resizedData[count] = data[count];
        }
        
        
        return resizedData;
    }

    /**************************************************************************
     * Return the number of samples held in the object
     *
     * @return The number of samples.
     *************************************************************************/
    public int getNumOfSamples()
    {
        return dataIndex;
    }
    
    /**************************************************************************
     * Return the start date for this time series data object.
     *
     * @return The start date.
     **************************************************************************/
    public Date GetStartDate ()
    {
        return date;
    }
    
    /**************************************************************************
     * Return the duration for this time series data object.
     *
     * @return The duration in milliseconds.
     *************************************************************************/
    public long GetDuration ()
    {
        return duration;
    }
    
    /**************************************************************************
     * Return the end date for this time series data object.
     *
     * @return The end date.
     *************************************************************************/
    public Date GetEndDate ()
    {
        return endDate;
    }
    
    /**************************************************************************
     * Tests whether this object is contiguous and immediately before
     *                      the given object.
     *
     * @param testObj The object to test against.
     * @return True if the objects are contiguous, false otherwise.
     *************************************************************************/
    public boolean IsContiguousBefore (TimeSeriesData testObj)
    {
        // Note:
        // For non integer sample rates a sample may 'slip' a few milliseconds
        // this will need to be taken into account. For the moment as far as this
        // function is concerned the Series are contiguous if there is no more than
        // a difference of 1 samples length in either direction between the times.
        // Fixing this sample 'slip' will need to be done by another function after
        // this test has taken place...
        
        // Debug Stuff
        /*
        DateFormat df = DateFormat.getDateTimeInstance();
        System.out.println("Sample Rate: " + sampleRate);
        System.out.println(df.format(endDate));
        System.out.println(df.format(testObj.date));*/
        EWTimeSeriesData testEWObj = (EWTimeSeriesData) testObj;
        
        if (testEWObj.date.getTime() < endDate.getTime()) return false;
        else if (testEWObj.date.getTime() > endDate.getTime() + (2 * samplePeriodHz * 1000)) return false;
        return true;
    }

    /**************************************************************************
     * Tests whether this object is contiguous and immediately before
     *                      the given time.
     *
     * @param testObj The time to test against.
     * @return True if the times are contiguous, false otherwise.
     *************************************************************************/
    private boolean IsContiguousBefore (Date testDate)
    {
        if (testDate.getTime() < endDate.getTime())
        {
            System.err.println(testDate.getTime());
            return false;
        }
        else if (testDate.getTime() > endDate.getTime() + (2 * samplePeriodHz * 1000)) return false;
        return true;
    }    
    
    /**************************************************************************
     * Calculates whether the time window for this time series intersects
     *                      the given time window.
     *
     * @param testDate The start of the time window to test against.
     * @param testDuration The duration (in mS) of the time window to test
     *                      against.
     * @return True if the two windows intersect, false otherwise.
     **************************************************************************/
    public boolean DoTimesOverlap (Date testDate, long testDuration)
    {
        if ((testDate.getTime() + testDuration) < date.getTime()) return false;
        if (testDate.getTime() > GetEndDate().getTime()) return false;
        return true;
    }

    /*********************************************************************
     * Get the sample rate for this time series object (in samples / hour).
     *
     * @return the sample rate in samples per hour
     *********************************************************************/
    public int GetSampleRateSPH ()
    {
        return (int)(sampleRate * 60 * 60);
    }
    
    /**************************************************************************
     * Get the sample rate for this time series object (in samples / second).
     *
     * @return The sample rate in samples per second.
     *************************************************************************/
    public double GetSampleRateHz ()
    {
        return sampleRate;
    }

    /**************************************************************************
     * Get the channel details associated with this time series.
     *
     * @return The channel details.
     **************************************************************************/
    public DataChannel GetChannelDetails ()
    {
        return channelDetails;
    }

    /**************************************************************************
     * Set the flag to indicate if some data is missing in the cache.
     *
     * @param The new boolean value for the flag.
     *************************************************************************/
    public void SetSomeDataMissing (boolean miss)
    {
        someDataMissing = miss;
    }

    /**************************************************************************
     * Get the flag which indicates if some data is missing in the cache.
     *
     * @return The flag.
     **************************************************************************/
    public boolean IsSomeDataMissing ()
    {
        return someDataMissing;
    }
    
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     *   Trace packet manipulation and related functions
     *
     *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!/
    
        
    /**************************************************************************
     * Returns a filled in EWTracePacket class from a valid DataInputStream.
     *
     * @param dInStream A valid DataInputStream located at the start of a
     *                  TRACE_HEADER packet.
     * @param dataType The expected format of the data as given by the ASCII
     *                  header.
     *
     * @return The size in bytes of the header.
     *
     * @throws IOException
     * @throws EWNetException If the sample rate changes between packets.
     *************************************************************************/
    private int getTrace_Header(DataInputStream dInStream, String hDataType)
    throws IOException, EWNetException
    {
        double tempSampleRate;
        
        
        // Use hDataType to decode the binary header data
        
        // Calculate information about datatype
        String dataFormat = String.valueOf(hDataType.charAt(0));
        int integerSize = Integer.parseInt(String.valueOf(hDataType.charAt(1)));
        
        // Calculate the size in bytes of the binary header
        int headerSize = 7 + TRACE_STA_LEN + TRACE_NET_LEN + TRACE_CHAN_LEN + (3 * SIZE_OF_DOUBLE) + (2 * integerSize);

       // Create new byte array to store the header and read in the header
        packetBinaryHeader = new byte[headerSize];
        for (int i = 0; i < headerSize; i++)
        {
            packetBinaryHeader[i] = dInStream.readByte();
        }
        
        // If intel byte order swap the bytes for the first 5 fields
        // Order is int, int, double, double, double
        if (dataFormat.toLowerCase().compareTo("i") == 0)
        {
            int index = 0;
            swapBytes(index, integerSize);
            index += integerSize;
            swapBytes(index, integerSize);
            index += integerSize;
            
            swapBytes(index, SIZE_OF_DOUBLE);
            index += SIZE_OF_DOUBLE;
            swapBytes(index, SIZE_OF_DOUBLE);
            index += SIZE_OF_DOUBLE;
             swapBytes(index, SIZE_OF_DOUBLE);
            index += SIZE_OF_DOUBLE;     
        }
        
        // Create new DataInputStream from the Binary Header Array
        DataInputStream headerStream = new DataInputStream(
                new ByteArrayInputStream(packetBinaryHeader));
        
        // Empty strings for new values
        packetSiteName = "";
        packetChannelName = "";
        packetNetworkID = "";
        packetQuality = "";
        packetDataType = "";
        
        
        // Read pinNo (int)
        if (integerSize == 4) {
            packetPinNo = headerStream.readInt();
        }
        else if (integerSize == 2) {
            packetPinNo = (int)headerStream.readShort();
        }
 
        // Read nSamp (int)
        if (integerSize == 4) {
            packetNoSamp = headerStream.readInt();        
        }
        else if (integerSize == 2) {
            packetNoSamp = headerStream.readInt();
        }
            
        // Read startTime (double)
        packetDate = new Date((long)(headerStream.readDouble() * 1000));        
        
        // Read endTime (double)
        packetEndDate = new Date((long)(headerStream.readDouble() * 1000));
        
        // Read sampRate (double)
        packetSampleRate = headerStream.readDouble();
        
        // Read siteName (char[7])
        for (int i = 0; i < TRACE_STA_LEN; i++)
        {
            packetSiteName += (char)headerStream.readByte();
        }
        
        // Read networkID (char[9])
        for (int i = 0; i < TRACE_NET_LEN; i++)
        {
            packetNetworkID += (char)headerStream.readByte();
        }        
        
        // Read channelName (char[9])
        for (int i = 0; i < TRACE_CHAN_LEN; i++)
        {
            packetChannelName += (char)headerStream.readByte();
        }
        
        // Read dataType (char[3])
        for (int i = 0; i < 3; i++)
        {
            packetDataType += (char)headerStream.readByte();
        }        
        
        // Read quality (char[2])
        for (int i = 0; i < 2; i++)
        {
            packetQuality += (char)headerStream.readByte();
        }
        
        // Read padding (char[2])
        // Do not add this to the class!!
        for (int i = 0; i < 2; i++)
        {
            headerStream.readByte();
        }        
        
        // Close streams and free memory
        headerStream.close();
        packetBinaryHeader = null;
        
        // Return filled in packet details
        return headerSize;
    }
    
    /**************************************************************************
     * Changes ordering of bytes from Intel to Sparc.
     *                      Note: This is applyed to the class level binary
     *                      header array - only call if sure this is correct.
     *
     * @param start The index of the first byte to swap
     * @param length The length of data to swap
     *************************************************************************/    
    private void swapBytes(int start, int size)
    {
        byte temp;
        int j = start + size -1;
        
        for (int i = start; i < j; i++, j--)
        {
            temp = packetBinaryHeader[i];
            packetBinaryHeader[i] = packetBinaryHeader[j];
            packetBinaryHeader[j] = temp;
        }        
    }
    
    /**************************************************************************
     * Changes ordering of bytes from Intel to Sparc.
     *                          Note: Use only for small arrays.
     *
     * @param data The binary array to swap the order of.
     * @param length The length of data to swap.
     *
     * @return The reordered byte array.
     *************************************************************************/
    private byte[] swapBytes(byte[] data, int size)
    {
        // Use this for small byte arrays only
        // A lot of copying is done
        byte[] swapped = new byte[size];
        
        for (int i = 0; i < size; i++)
        {
            swapped[size - 1 - i] = data[i];
        }
        return swapped;
    }
    
    /**************************************************************************
     * Gathers the data contained after a TRACE_HEADER packet from a valid
     *                      DataInputStream.
     *
     * @param dInStream A valid DataInputStream located at the end of the 
     *                      given TRACE_HEADER packet.
     * 
     * @return the size in bytes of the read data.
     *
     * @throws IOException.
     * @throws Exception If an unsupported datatype is used.
     *************************************************************************/
    private int getTraceData(DataInputStream dInStream)
    throws IOException, EWNetException
    {
        // Calculate number of bytes to be read etc...
        int bytesPerSample = Integer.parseInt(String.valueOf(packetDataType.charAt(1)));
        int bytesOfData = packetNoSamp * bytesPerSample;
        
        // Loop for the number of samples in packet
        for (int i = 0; i < packetNoSamp; i++)
        {
            byte[] packetByteData = new byte[bytesPerSample];
            // Loop for the size of a sample
            for (int j = 0; j < bytesPerSample; j++)
            {
                // Add byte to array
                packetByteData[j] = dInStream.readByte();
            }
            // Process the sample
if (dataIndex < 0)
{
  System.out.println (Integer.toString (dataIndex));
}
            data[dataIndex] = getIntData(packetByteData, packetDataType);
            dataIndex++;
        }

        // Return the number of bytes read in the stream
        return bytesOfData;
    }

    /**************************************************************************
     * Gathers a single integer from the start of a binary array using the 
     *                      dataType variable.
     *
     * @param data The binary array containing the data to be extracted.
     * @param dataType The dataType (CSS) of the binary data (eg i2, s4).
     * 
     * @throws EWNetException An integer cannot be read from the array
     *                      probably the array is too small for the given
     *                      dataType.
     *
     * @return the 'decoded' integer.
     *************************************************************************/
    private int getIntData(byte[] data, String dataType)
    throws IOException, EWNetException
    {
        // Works for 4 byte and 2 byte integers (currently all that is needed).
        // In the future may need rewriting for other types.
        
        // Get the number of bytes per integer
        int size = Integer.parseInt(String.valueOf(dataType.charAt(1)));
        
        // Determine wether the bytes need to be re-ordered
        if (String.valueOf(dataType.charAt(0)).toLowerCase().compareTo("i") == 0)
        {
            data = swapBytes(data,size);
        }
    
        // Extract the integer
        DataInputStream dInStream = new DataInputStream(
                new ByteArrayInputStream(data));
        if (size == 4) {
            return dInStream.readInt();
        }
        else if (size == 2) {
            return (int) dInStream.readShort();
        }
        else {
            // There is a problem here - at the time of writing
            // only int and short are supported by WaveServer however
            // at somepoint others maybe used.
            // For the time being throw an exception
            throw new EWNetException(EWNetException.UNSUPPORTED_DATA_TYPE, "Integer data size not supported: [" + size + "].");
        }
    }
    
    /**************************************************************************
     * Adds gap data to the end of the time series using the given value for
     *                      the duration specified.
     *
     * @param gapDuration The duration that the data is to cover.
     * @param gapValue The integer value used to represent gap data.
     *************************************************************************/    
    public void addGapData(long gapDuration, int gapValue)
    {
        int noOfGapSamples = (int)((gapDuration / 1000) * sampleRate);

        long actualGapDuration = (long) (noOfGapSamples * 1000 * samplePeriodHz);
        duration += actualGapDuration;
        endDate.setTime(endDate.getTime() + actualGapDuration);
        
        if ((noOfGapSamples + dataIndex) >= data.length)
        {
            // Resize array and move data into it...
            int[] oldData = data;
            data = new int[(int)((noOfGapSamples + dataIndex) * 1.1)];
            
            for (int count = 0; count < dataIndex; count++)
            {
                data[count] = oldData[count];
            }
            oldData = null;
        }
        
        for (int count = 0; count < noOfGapSamples; count++)
        {
            data[count + dataIndex] = gapValue;
        }
        dataIndex += noOfGapSamples;
    }
    
    /**************************************************************************
     * Adds gap data to the beginning of the time series.
     *
     * @param start The time at which the series should now start.
     *************************************************************************/    
    public void addGapDataFront(Date start)
    {
        int count;
        int count2;
        
        int noOfGapSamples = (int)(((date.getTime() - start.getTime()) / 1000) * sampleRate);

        long actualGapDuration = (long) (noOfGapSamples * 1000 * samplePeriodHz);
        duration += actualGapDuration;
        date.setTime(date.getTime() - actualGapDuration);
        
        if (noOfGapSamples + dataIndex > data.length)
        {
            // Resize array and move old data into it
            int[] oldData = data;
            data = new int[(int)((noOfGapSamples + dataIndex) * 1.1)];
            for (count = 0; count < dataIndex; count++)
                data[count + noOfGapSamples] = oldData[count];
        }
        else
        {
            for (count = dataIndex + noOfGapSamples - 1; count >= noOfGapSamples; count--)
                data[count] = data[count - noOfGapSamples];
        }
        
        for (count = 0; count < noOfGapSamples; count++)
            data[count] = GAP_VALUE;
        
        dataIndex += noOfGapSamples;       
    }
    
}
