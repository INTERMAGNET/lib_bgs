/*
 * WDC.java
 *
 * Created on 28 January 2007, 11:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import bgs.geophys.library.Maths.BGSMath;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Misc.GeoString;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class to read and write WDC minute or hourly data. 
 * 
 * To use this object to write a file:
 *      1.) Instantiate an object in the normal way
 *      2.) Call the addData() methods to insert the data
 *      3.) Optionally call the get/setDataMean to adjust the associated mean values
 *      4.) Call any of the write() methods
 *
 * To use this object to read a file call the static read() method
 *
 * The formats are from
 * http://web.dmi.dk/projects/wdcc1/format.html
 *
 *  1 -   6   Geographic Co-Latitude in 0.001 degree (0 to 180)
 *  7 -  12   East Geographic Longitude in 0.001 degree (0 to 360)
 * 13 -  14   Year      (98)
 * 15 -  16   Month   (01 - 12)
 * 17 -  18   Day     (01 - 31)
 * 19         Component of the field (H,E,Z)
 * 20 -  21   Hour    (00 - 23)
 * 22 -  24   Observatory's IAGA 3-letter code
 * 25         Origin of data (D - digital, A - digitized)
 * 26 -  34   Future Use (NOT USED HERE)
 * 35 -  40   1-st 1 minute average
 * 41 -  46   2-nd 1 minute average
 * ...          ...      .....
 * 388 - 394   60-th 1 minute average 
 * 394 - 400   Hourly mean value
 *
 * ORG    (data origin codes)
 * 
 *      A =    Alaskan meridian magnetometer chain (includes Canadian sites) for
 *             lMS
 *      C =    Canadian standard observatory network
 *      O =    point samples digitized from analog magnetograms
 *      F =    France
 *      G =    USGS standard observatory network (one station operated by NOAA)
 *      J =    Japan
 *      K =    US AFGL E-W sub-auroral zone magnetometer chain
 *      R =    Western Canadian meridian magnetometer chain operated for IMS
 *      T =    Lungping magnetic observatory, Taiwan.
 *      U =    E-W mid-latitude magnetometer chain operated for IMS
 *      V =    Variations only sent via NOAA GOES satellite relay
 *      W =    Eastern Canadian meridian magnetometer chain operated for IMS
 *
 * NBNBNB - there is an alternative description of bytes 25-27 - the
 * alternative description is used by this class when writing data
 *
 * 25       Arbitary (not used)
 * 26       Single Century digit - 8 for 1800, 9 for 1900, 0 for 2000
 * 27       'P' or 'D' for preliminary or definitive data
 *
 * @author smf
 */
public class WDCMinute extends GeomagDataFormat
{
    /*
     * Caution! SimpleDateFormat is not thread safe!
     */
    // formatting objects
    private SimpleDateFormat headerDateFormat;
    private SimpleDateFormat hourDateFormat;
    private SimpleDateFormat yearDateFormat;
    private static SimpleDateFormat headerInputDateFormat;
    private static SimpleDateFormat filenameDateFormat;

    
    // static initialisers - mainly creation of formatting objects
    static
    {        
        headerInputDateFormat = new SimpleDateFormat ("yyyyMMddHH", DateUtils.english_date_format_symbols);
        headerInputDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        filenameDateFormat = new SimpleDateFormat ("yyMM", DateUtils.english_date_format_symbols);
        filenameDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    }
     
    /**
     * Creates a new instance of WDC by supplying all data
     * 
     * @param station_code the IAGA station code
     * @param latitude the geodetic latitude (degrees)
     * @param longitude the geodetic longitude (degrees)
     * @param comp_code the components in this data
     * @param institute_code - can be D for digital data, A for analog data
     *        or one of the codes in the object description
     * @throws GeomagDataException if there was an error in the data - the
     *         messages associated with the exception will describe the problem
     */
    public WDCMinute (String station_code, double latitude, double longitude,
                      String comp_code, String institute_code)
    throws GeomagDataException
    {
        super (station_code, null, latitude, longitude, MISSING_HEADER_VALUE,
               comp_code, null, null, institute_code, null, null, null, false, 60);
        initFormatObjects();
    }

    /**
     * Constructor with the same signature as the GeomagDataFormat parent class
     * Designed to give all subclasses of GeomagDataFormat a uniform constructor.
     * Delegates creation of a new instance of WDC to the appropriate WDC constructor
     *
     * @param stationCode
     * @param stationName
     * @param latitude
     * @param longitude
     * @param elevation
     * @param compCode
     * @param dataType
     * @param ginCode
     * @param instituteName
     * @param sensorOrientation
     * @param samplePeriodString
     * @param intervalType
     * @param allowFourDigitSC
     * @param blocksize
     * @throws bgs.geophys.library.Data.GeomagDataException
     */
    public WDCMinute(String stationCode, String stationName, double latitude, double longitude, double elevation, String compCode, String dataType, String ginCode, String instituteName, String sensorOrientation, String samplePeriodString, String intervalType, boolean allowFourDigitSC, int blocksize) throws GeomagDataException {
        this(stationCode, latitude, longitude, compCode, instituteName);
        initFormatObjects();
    }
    
    /** Write data in WDC format to an output stream
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int count, n_data_points, componentCount, recordCount, index;
        int nComponents, blockCount, nBlocks;
        double value, multiplier;
        Date date;
        String colatitudeString, longitudeString, dateString, hourString;
        String centuryString, componentCode;
        
        // check that there is some data to write
        if (getDataLength() <= 0)
            throw new GeomagDataException ("No data to write");

        // check that data is one minute period
        if (getSamplePeriod() != 60000l)
            throw new GeomagDataException ("Data must have sample period of one minute");
        
        // check that there are complete days to write
        if ((getStartDate().getTime() % 86400000l) != 0)
            throw new GeomagDataException ("Data must start at beginning of day");
        if ((getDataLength() % 1440) != 0)
            throw new GeomagDataException ("Incomplete data (must be in blocks of 1440)");
        
        // get header strings
        value = getLatitude();
        if (value == MISSING_HEADER_VALUE) value = 0.0;
        colatitudeString = GeoString.formatAsInteger((90.0 - value) * 1000.0, 6);
        
        value = getLongitude();
        if (value == MISSING_HEADER_VALUE) value = 0.0;
        while (value < 0.0) value += 360.0;
        longitudeString = GeoString.formatAsInteger (value * 1000.0, 6);
        
        nComponents = getComponentCodes().length();
        if (nComponents > 4) nComponents = 4;
        
        // write a day at a time - 24 records for each component
        // a 'block' is 24 records, a record in 60 samples, a block contains 1440 samples
        n_data_points = getDataLength();
        nBlocks = n_data_points / 1440;
        for (blockCount=0; blockCount<nBlocks; blockCount++)
        {
            date = new Date (getStartDate().getTime() + ((long) blockCount * 86400000l));
            dateString = headerDateFormat.format (date);
            
            // for each component...
            for (componentCount=0; componentCount<nComponents; componentCount ++)
            {
                componentCode = getComponentCodes().substring(componentCount, componentCount +1);
                if (componentCode.equalsIgnoreCase("D")) multiplier = 10.0;
                else if (componentCode.equalsIgnoreCase("I")) multiplier = 10.0;
                else multiplier = 1.0;
                        
                // for each record...
                for (recordCount=0; recordCount<24; recordCount++)
                {
                    date = new Date (getStartDate().getTime() + (((long) blockCount * 86400000l) + ((long) recordCount * 3600000l)));
                    centuryString = yearDateFormat.format (date);
                    hourString = hourDateFormat.format (date);
                    
                    // write the start of the record
                    writeString (os, colatitudeString, TERM_TYPE_NONE);
                    writeString (os, longitudeString, TERM_TYPE_NONE);
                    writeString (os, dateString, TERM_TYPE_NONE);
                    writeString (os, componentCode, TERM_TYPE_NONE);
                    writeString (os, hourString, TERM_TYPE_NONE);
                    writeString (os, getStationCode(), TERM_TYPE_NONE);
                    writeString (os, " ", TERM_TYPE_NONE);
                    writeString (os, centuryString.substring(1, 2), TERM_TYPE_NONE);
                    writeString (os, "D       ", TERM_TYPE_NONE);
                    
                    // write the 60 data values
                    for (count=0, index=(blockCount * 1440) + (recordCount * 60); 
                         count<60; 
                         count++, index++)
                    {
                        value = getData (componentCount, index, 999999.0, 888888.0);
                        if (value < 888888.0) value *= multiplier;
                        writeString (os, GeoString.formatAsInteger (value, 6, BGSMath.ROUND_HALF_EVEN), TERM_TYPE_NONE);
                    }
                    
                    // write the mean and end of record
                    index = (blockCount * 24) + recordCount;
                    value = getDataMean (componentCount, index, 999999.0);
                    if (value < 999999.0) value *= multiplier;
                    writeString (os, GeoString.formatAsInteger (value, 6, BGSMath.ROUND_HALF_EVEN), termType);
                }
            }
        }
    }

     /** generate a WDC minute filename
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @return the file OR null if there is an error */
    public String makeFilename(String prefix, boolean force_lower_case) 
    {
        if (getStartDate() == null) return null;
        return makeFilename (prefix, getStationCode (), getStartDate (), force_lower_case);
    }
    
    
    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** Creates a new instance of WDC minute data by reading it from a file
      * @param is the stream to read from
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    public static WDCMinute read (InputStream is) 
    throws IOException, GeomagDataException
    {
        int lineNumber, year, count, componentIndex, index;
        double testLatitude, testLongitude, latitude, longitude, value, multiplier;
        byte component, componentCodes [];
        Date startDate [], date;
        String record;
        BufferedReader reader;
        WDCMinute WdcMinute;
        String dateString, origin, testOriginString;
        String stationCode, testStationCode;
        ArrayList<Double> c1, c2, c3, c4, dataPtr;
        ArrayList<Double> c1Mean, c2Mean, c3Mean, c4Mean, meanPtr;
        
        // open the file
        reader = new BufferedReader (new InputStreamReader (is));
        
        // create arrays
        componentCodes = new byte [4];
        startDate = new Date [4];
        for (count=0; count<4; count++)
        {
            componentCodes [count] = ' ';
            startDate [count] = null;
        }
        c1 = new ArrayList<Double> ();
        c2 = new ArrayList<Double> ();
        c3 = new ArrayList<Double> ();
        c4 = new ArrayList<Double> ();
        c1Mean = new ArrayList<Double> ();
        c2Mean = new ArrayList<Double> ();
        c3Mean = new ArrayList<Double> ();
        c4Mean = new ArrayList<Double> ();
        
        // read records from the file
        lineNumber = 0;
        stationCode = null;
        latitude = longitude = 0.0;
        origin = null;
        while ((record = reader.readLine()) != null)
        {
            lineNumber ++;
            
            // records must be 400 bytes long
            if (record.length() != 400)
                throw new GeomagDataException ("Record length incorrect at line number " + Integer.toString (lineNumber));
            
            // split out the header fields
            try
            {
                testLatitude = Double.parseDouble (record.substring (0, 6));
                testLongitude = Double.parseDouble (record.substring (6, 12));
                year = Integer.parseInt (record.substring (12, 14));
                if (year >= 60) 
                    dateString = "19" + record.substring (12, 18) + record.substring (19, 21);
                else 
                    dateString = "20" + record.substring (12, 18) + record.substring (19, 21);
                date = headerInputDateFormat.parse(dateString);
                component = (byte) record.charAt (18);
                testStationCode = record.substring (21, 24);
                testOriginString = record.substring (24, 25);
            }
            catch (ParseException e)
            {
                throw new GeomagDataException ("Bad data in header (columns 1 - 34) at line number " + Integer.toString (lineNumber));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in header (columns 1 - 34) at line number " + Integer.toString (lineNumber));
            }
            
            // is this the first record
            if (stationCode == null)
            {
                // yes - fill out the header variables
                stationCode = testStationCode;
                latitude = testLatitude;
                longitude = testLongitude;
                origin = testOriginString;
            }
            else
            {
                // no - check the header variables
                if (! stationCode.equalsIgnoreCase(testStationCode))
                    throw new GeomagDataException ("Station code changes at line number " + Integer.toString (lineNumber) + ": " + stationCode + " / " + testStationCode);
                if (latitude != testLatitude)
                    throw new GeomagDataException ("Latitude changes at line number " + Integer.toString (lineNumber));
                if (longitude != testLongitude)
                    throw new GeomagDataException ("Longitude changes at line number " + Integer.toString (lineNumber));
            }
            
            // work out which component to put the data in
            switch (component)
            {
                case 'X': componentIndex = 0; multiplier = 1.0; dataPtr = c1; meanPtr = c1Mean; break;
                case 'Y': componentIndex = 1; multiplier = 1.0; dataPtr = c2; meanPtr = c2Mean; break;
                case 'Z': componentIndex = 2; multiplier = 1.0; dataPtr = c3; meanPtr = c3Mean; break;
                case 'H': componentIndex = 0; multiplier = 1.0; dataPtr = c1; meanPtr = c1Mean; break;
                case 'D': componentIndex = 1; multiplier = 0.1; dataPtr = c2; meanPtr = c2Mean; break;
                case 'I': componentIndex = 1; multiplier = 0.1; dataPtr = c2; meanPtr = c2Mean; break;
                case 'F': componentIndex = 3; multiplier = 1.0; dataPtr = c4; meanPtr = c4Mean; break;
                default:
                    throw new GeomagDataException ("Bad component code at line number " + Integer.toString (lineNumber));
            }
            if (componentCodes[componentIndex] == ' ')
                componentCodes[componentIndex] = component;
            else if (componentCodes[componentIndex] != component)
                throw new GeomagDataException ("Component code changes at line number " + Integer.toString (lineNumber));
            
            // check the times are contiguous
            if (startDate [componentIndex] == null)
                startDate [componentIndex] = date;
            else if (startDate[componentIndex].getTime () !=
                     (date.getTime() + ((long) dataPtr.size() * 60000l)))
                throw new GeomagDataException ("Non-contiguous date/time at line number " + Integer.toString (lineNumber));
            
            // extract the data and mean
            try
            {
                for (count=0; count<60; count++)
                {
                    index = 34 + (count * 6);
                    value = Double.parseDouble (record.substring (count, count+6));
                    if (value < 88888.0) value *= multiplier;
                    dataPtr.add (new Double (value));
                }
                value = Double.parseDouble(record.substring(393, 399));
                if (value < 88888.0) value *= multiplier;
                meanPtr.add (new Double (value));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in data at line number " + Integer.toString (lineNumber));
            }
        }

        // check there was some data and fill missing F with blank data
        if (componentCodes [0] == ' ')
            throw new GeomagDataException ("Missing X or H component in data file");
        if (componentCodes [1] == ' ')
            throw new GeomagDataException ("Missing Y or D component in data file");
        if (componentCodes [2] == ' ')
            throw new GeomagDataException ("Missing Z component in data file");
        if (componentCodes [3] == ' ')
        {
            startDate[3] = startDate[0];
            for (count=0; count<c1.size(); count++)
                c4.add (new Double (88888.0));
        }
        
        // check start dates are the same - also
        // check all components data are the same length - this could be changed
        // to tack missing values to the end of the short arrays - if you do
        // this remember to also add values to the means array
        for (count=1; count<4; count++)
        {
            if (startDate [0].getTime() != startDate [count].getTime())
                throw new GeomagDataException ("Components 1 and " + Integer.toString(count) + " start at different times");
        }
        if (c1.size() != c2.size())
            throw new GeomagDataException ("Components 1 and 2 have different amounts of data");
        if (c1.size() != c3.size())
            throw new GeomagDataException ("Components 1 and 3 have different amounts of data");
        if (c1.size() != c4.size())
            throw new GeomagDataException ("Components 1 and 4 have different amounts of data");
        
        // create the WDCMinute object and add data to it
        WdcMinute = new WDCMinute (stationCode, latitude, longitude, 
                                   new String (componentCodes), origin);
        WdcMinute.addData (startDate[0], 60000l, 999999.0, 888888.0,
                           c1, c2, c3, c4);
        
        // sort out the mean values
        for (count=0; count<c1Mean.size(); count++)
        {
            WdcMinute.setDataMean(0, count, c1Mean.get(count).doubleValue());
            WdcMinute.setDataMean(1, count, c2Mean.get(count).doubleValue());
            WdcMinute.setDataMean(2, count, c3Mean.get(count).doubleValue());
            WdcMinute.setDataMean(3, count, c4Mean.get(count).doubleValue());
        }
        
        return WdcMinute;
    }
    
    /** generate a WDC minute filename
     * @param prefix the prefix for the name (including any directory)
     * @param station_code the IAGA station code
     * @param start_date the start date for the data 
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the filename */
    public static String makeFilename (String prefix,
                                       String station_code, Date start_date,
                                       boolean force_lower_case)
    {
        String filename;
        
        filename = station_code + filenameDateFormat.format (start_date) + "M.WDC";
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }

    private void initFormatObjects(){
        headerDateFormat = new SimpleDateFormat ("yyMMdd", DateUtils.english_date_format_symbols);
        headerDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        hourDateFormat = new SimpleDateFormat ("HH", DateUtils.english_date_format_symbols);
        hourDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        yearDateFormat = new SimpleDateFormat ("yyyy", DateUtils.english_date_format_symbols);
        yearDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    }

}
