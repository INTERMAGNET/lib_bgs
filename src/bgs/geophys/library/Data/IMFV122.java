/*
 * IMFV122.java
 *
 * Created on 13 November 2003, 21:59
 */
package bgs.geophys.library.Data;

import bgs.geophys.library.Misc.DateUtils;
import java.util.*;
import java.text.*;
import java.io.*;

import bgs.geophys.library.Misc.GeoString;
import bgs.geophys.library.Misc.Utils;

/**
 * To use this object to write a file:
 *      1.) Instantiate an object in the normal way
 *      2.) Call the addData() methods to insert the data
 *      3.) Call any of the write() methods
 *
 * To use this object to read a file call the static read() method
 *
 * @author  Administrator
 */
public class IMFV122 extends GeomagDataFormat 
{
    // private member variables
    private boolean useDecbasFlag;

    // formatting objects
    private static SimpleDateFormat headerDateFormat;
    private static SimpleDateFormat filenameDateFormat;
    private static SimpleDateFormat doyDateFormat;
    private static SimpleDateFormat hourDateFormat;
    private static DecimalFormat number4digits;
    private static DecimalFormat number3digits;
    private static DecimalFormat number2digits;
    private static DecimalFormat signedNumber6digits;

    // static initialisers - mainly creation of formatting objects
    static
    {
        headerDateFormat = new SimpleDateFormat ("MMMddyy", DateUtils.english_date_format_symbols);
        headerDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        filenameDateFormat = new SimpleDateFormat ("MMMddyy", DateUtils.english_date_format_symbols);
        filenameDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        doyDateFormat = new SimpleDateFormat ("DDD", DateUtils.english_date_format_symbols);
        doyDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        hourDateFormat = new SimpleDateFormat ("HH", DateUtils.english_date_format_symbols);
        hourDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        signedNumber6digits = new DecimalFormat ("'0'00000;-00000", Utils.getScientificDecimalFormatSymbols());
        number4digits = new DecimalFormat ("0000", Utils.getScientificDecimalFormatSymbols());
        number3digits = new DecimalFormat ("000", Utils.getScientificDecimalFormatSymbols());
        number2digits = new DecimalFormat ("00", Utils.getScientificDecimalFormatSymbols());
    }
     
    /** Creates a new instance of IMFV122 by supplying all data
     * @param station_code the IAGA station code
     * @param ginCode 3 digit code for the GIN
     * @param latitude the geodetic latitude (degrees)
     * @param longitude the geodetic longitude (degrees)
     * @param compCode the components in this data
     * @param data_type the INTERMAGNET data type
     * @throws GeomagDataException if there was an error in the data - the
     *         messages associated with the exception will describe the problem */
    public IMFV122 (String stationCode, String ginCode,
                    double latitude, double longitude, 
                    String compCode, String dataType)
    throws GeomagDataException 
    {
        super (stationCode, null, latitude, longitude, MISSING_HEADER_VALUE,
               compCode, dataType, ginCode, null, null, null, null, false, -1);

        if (compCode.charAt(1) == 'd' || compCode.charAt(1) == 'D')
            useDecbasFlag = true;
        else
            useDecbasFlag = false;
    }

    /**
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
    public IMFV122(String stationCode, String stationName, double latitude, double longitude, double elevation, String compCode, String dataType, String ginCode, String instituteName, String sensorOrientation, String samplePeriodString, String intervalType, boolean allowFourDigitSC, int blocksize) throws GeomagDataException {
        this(stationCode, ginCode, latitude, longitude, compCode, dataType);
    }



    /** should the declination baseline be used ?? */
    public boolean useDecbas () { return useDecbasFlag; }
    
    /** Write data in IMFV1.22 format to an output stream
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int count, count2, index;
        double c1, c2, c3, c4, decbas, min, max;
        byte term [];
        Date date;
        
        // check that there is some data to write
        if (getDataLength() <= 0)
            throw new GeomagDataException ("No data to write");
        
        // check that data is one minute period
        if (getSamplePeriod() != 60000l)
            throw new GeomagDataException ("Data must have sample period of one minute");
        
        // check that there are an integer number of data blocks
        if ((getStartDate().getTime() % 3600000l) != 0)
            throw new GeomagDataException ("Data must start at beginning of an hour");
        if ((getDataLength() %60) != 0)
            throw new GeomagDataException ("Incomplete data (must be in blocks of 60)");
        
        // for each hourly block of data ...
        for (count=0, date = getStartDate(); 
             count<getDataLength(); 
             count += 60, date = new Date (date.getTime() + 3600000l))
        {
            // calculate declination baseline
            if (useDecbasFlag)
            {
                min = Double.MAX_VALUE;
                max = - Double.MAX_VALUE;
                for (count2=0; count2<60; count2++)
                {
                    c2 = getData (1, count + count2, 99999.0, 88888.0);
                    if (c2 < 88888.0)
                    {
                        if (c2 < min) min = c2;
                        if (c2 > max) max = c2;
                    }
                }
                if (min == Double.MAX_VALUE) decbas = 0.0;
                else if (max > 9000.0) decbas = min + ((max - min) / 2);
                else if (min < -9000.0) decbas = max + ((min - max) / 2);
                else decbas = 0.0;
            }
            else decbas = 0.0;

            // write the header
            writeString (os,
                         GeoString.fix (getStationCode(), 3, true, false) + " " +
                         headerDateFormat.format (date).toUpperCase() + " " +
                         doyDateFormat.format (date) + " " +
                         hourDateFormat.format (date) + " " +
                         GeoString.fix (getComponentCodes(), 4, true, false).toUpperCase() + " " +
                         GeoString.fix (getDataType(), 1, true, false).toUpperCase() + " " +
                         GeoString.fix (getGINCode(), 3, true, false).toUpperCase() + " " +
                         number4digits.format ((90.0 - getLatitude()) * 10.0) + number4digits.format(getLongitude() * 10.0) + " " +
                         signedNumber6digits.format (decbas * 10.0) + " " +
                         "RRRRRRRRRRRRRRRR",
                         termType);

            // write the data
            for (count2=0; count2<60; count2++)
            {
                index = count + count2;

                c1 = getData (0, index, 999999.9, 888888.8) * 10.0;
                c2 = getData (1, index, 999999.9, 888888.8) * 10.0;
                c3 = getData (2, index, 999999.9, 888888.8) * 10.0;
                c4 = getData (3, index, 99999.9, 88888.8) * 10.0;
                if ((c2 < 888888.0) && useDecbasFlag)
                    c2 = ((c2 / 10.0) - decbas) * 100.0;

                if ((count2 %2) == 0) term = "  ".getBytes();
                else term = termType;
                writeString (os, 
                             GeoString.formatAsInteger(c1, 7) + " " +
                             GeoString.formatAsInteger(c2, 7) + " " +
                             GeoString.formatAsInteger(c3, 7) + " " +
                             GeoString.formatAsInteger(c4, 6),
                             term);
            }
        }
    }

    /** generate an IMFV1.22 filename
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @return the file OR null if there is an error */
    public String makeFilename (String prefix, boolean force_lower_case)
    {
        if (getStartDate() == null) return null;
        return makeFilename (prefix, getStationCode (), getStartDate (), force_lower_case);
    }
    
    
    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** Creates a new instance of IMFV122 data by reading it from a file
      * @param is the stream to read from
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    public static IMFV122 read (InputStream is) 
    throws IOException, GeomagDataException
    {
        int count, line_number, index, hour, colalong, testColalong, count2;
        double c1 [], c2 [], c3 [], c4 [], decbas, longitude, latitude;
        String headerBuffer, dataBuffer, testStationCode, testCompCode;
        String testDataType, testGinCode;
        Date startDate;
        BufferedReader reader;
        StringTokenizer tokens;
        IMFV122 imfV122;

        // wrap a buffered reader round the input stream
        reader = new BufferedReader (new InputStreamReader (is));

        // create storage for a block of data
        c1 = new double [60];
        c2 = new double [60];
        c3 = new double [60];
        c4 = new double [60];
        
        // read the blocks from the file
        line_number = 0;
        imfV122 = null;
        startDate = null;
        colalong = 0;
        while ((headerBuffer = reader.readLine()) != null)
        {
            line_number ++;
            
            // check for blank line
            if (headerBuffer.trim().length() == 0) continue;
            
            // attempt to extract fields from the header
            tokens = new StringTokenizer (headerBuffer);
            try
            {
                // extract header fields
                testStationCode = tokens.nextToken();                        // station code
                startDate = headerDateFormat.parse (tokens.nextToken());     // date
                tokens.nextToken();                                          // day of year (ignore)
                hour = Integer.parseInt (tokens.nextToken ());               // hour
                testCompCode = tokens.nextToken();                           // component code
                testDataType = tokens.nextToken();                           // data type
                testGinCode = tokens.nextToken();                            // gin code    
                testColalong = Integer.parseInt(tokens.nextToken());         // colatitude and longitude                
                decbas = Double.parseDouble(tokens.nextToken()) / 10.0;      // declination baseline (stored in minutes)

                // calculate date for this block
                startDate = new Date (startDate.getTime() + ((long) hour * 3600000l));

                // do checking not performed by super class
                if (testCompCode.length() != 4)
                    throw new StreamCorruptedException ("Bad component code at line number " + Integer.toString (line_number));
                if (testDataType.length() != 1)
                    throw new StreamCorruptedException ("Bad data type code at line number " + Integer.toString (line_number));
                
                // if this is the first header, create the IMFV122 object
                if (imfV122 == null)
                {
                    colalong = testColalong;
                    longitude = ((double) colalong % 10000) / 10.0;
                    latitude = 90.0 - (((double) colalong / 10000.0) / 10.0);
                    try
                    {
                        imfV122 = new IMFV122 (testStationCode, testGinCode, latitude,
                                               longitude, testCompCode, testDataType);
                    }
                    catch (GeomagDataException e)
                    {
                        throw new GeomagDataException (e.getMessage () + " at line number " + Integer.toString (line_number));
                    }
                }
                else
                {
                    // check this header against the first one
                    if (! imfV122.getStationCode().equalsIgnoreCase (testStationCode) ||
                        ! imfV122.getComponentCodes().equals (testCompCode) ||
                        ! imfV122.getDataType().equalsIgnoreCase (testDataType) ||
                        ! imfV122.getGINCode().equalsIgnoreCase (testGinCode) ||
                        testColalong != colalong)
                        throw new GeomagDataException ("Header metadata does not match previous header(s) at line number " + Integer.toString (line_number));
                }
                
            }
            catch (NoSuchElementException e)
            {
                throw new GeomagDataException ("Missing header fields at line number " + Integer.toString (line_number));
            }
            catch (ParseException e)
            {
                throw new GeomagDataException ("Bad data in header at line number " + Integer.toString (line_number));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in header at line number " + Integer.toString (line_number));
            }
            
            // the next 30 lines are data fields
            for (count=0; count<30; count++)
            {
                dataBuffer = reader.readLine();
                line_number ++;
                if (dataBuffer == null)
                    throw new GeomagDataException ("Missing data at end of file");
                    
                tokens = new StringTokenizer (dataBuffer);
                try
                {
                    for (count2=0; count2<2; count2++)
                    {
                        index = (count * 2) + count2;
                        c1 [index] = Double.parseDouble(tokens.nextToken()) / 10.0;
                        c2 [index] = Double.parseDouble(tokens.nextToken()) / 10.0;
                        c3 [index] = Double.parseDouble(tokens.nextToken()) / 10.0;
                        c4 [index] = Double.parseDouble(tokens.nextToken()) / 10.0;
                        
                        // adjust missing data values - only needed for 1st three components
                        // also add declination baseline
                        if (c1 [index] >= 99999.0) c1 [index] = 99999.0;
                        else if (c1 [index] >= 88888.0) c1 [index] = 88888.0;
              //TODO wait for confirmation from Chris that this needs doing
              //may need doing for all 4 components
                        if ((c2 [index] >= 99999.0 && c2[index]<=100000.0) ||
                            (c2 [index] >= 999999.0 && c2[index]<=1000000.0)   ) c2 [index] = 99999.0;
                        else if ((c2 [index] >= 88888.0 && c2[index]<=88889.0) ||
                            (c2 [index] >= 888888.0 && c2[index]<=888889.0)   ) c2 [index] = 88888.0;
                        else if (imfV122.useDecbas()) c2 [index] = (c2 [index] / 10.0) + decbas;
                        if (c3 [index] >= 99999.0) c3 [index] = 99999.0;
                        else if (c3 [index] >= 88888.0) c3 [index] = 88888.0;
                        // do fourth component too - not sure why it wasn't done before
                        if (c4 [index] >= 99999.0) c4 [index] = 99999.0;
                        else if (c4 [index] >= 88888.0) c4 [index] = 88888.0;

                    }
                }
                catch (NumberFormatException e)
                {
                    throw new StreamCorruptedException ("Number format error at line " + Integer.toString (line_number));
                }
                catch (NoSuchElementException e)
                {
                    throw new StreamCorruptedException ("Missing data field(s) at line " + Integer.toString (line_number));
                }
            }
            
            // add the block of data
            try
            {
                imfV122.addData(startDate, 60000, 99999.0, 88888.0, c1, c2, c3, c4);
            }
            catch (GeomagDataException e)
            {
                throw new GeomagDataException (e.getMessage () + " at line number " + Integer.toString (line_number));
            }
        }
        
        return imfV122;
    }
    
    /** generate an IMFV1.22 filename
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
        
        filename = filenameDateFormat.format (start_date) + "." + station_code;
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }
}
