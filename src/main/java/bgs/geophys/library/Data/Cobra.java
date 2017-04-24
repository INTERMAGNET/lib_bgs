/*
 * Cobra.java
 *
 * Created on 13 February 2004, 10:14
 */
package bgs.geophys.library.Data;

import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Misc.GeoString;

import java.util.*;
import java.text.*;
import java.io.*;
//import bgs.geophys.library.Data;

/**
 * To use this object to write a file:
 *      1.) Instantiate an object in the normal way
 *      2.) Call the addData() methods to insert the data
 *      3.) Call any of the write() methods
 *
 * To use this object to read a file call the static read() method
 *
 * @author  bba
 */
public class Cobra extends GeomagDataFormat 
{

    // formatting objects
    private static SimpleDateFormat headerDateFormat;
    private static SimpleDateFormat filenameDateFormat;

    // static initialisers - mainly creation of formatting objects
    static
    {
        headerDateFormat = new SimpleDateFormat ("HH DDD dd MM yyyy", DateUtils.english_date_format_symbols);
        headerDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        filenameDateFormat = new SimpleDateFormat ("DDD'h.m'yy", DateUtils.english_date_format_symbols);
        filenameDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    }
     
    /** create a Cobra format object from the station code
     * @param station_code the stationcode
     * @throws GeomagDataException if there was a fault */
    public Cobra (String station_code)
    throws GeomagDataException
    {
        super (station_code, null, 
               MISSING_HEADER_VALUE, MISSING_HEADER_VALUE, MISSING_HEADER_VALUE,
               "HDZF", "Definitive", null, null, null, null, null, false, -1);
    }

    /** create a Cobra format object from the file that it is found in -
     * the filename is the only place where the station code is held
     * @param file the file for the data
     * @throws GeomagDataException if there was a fault */
    public Cobra (File file)
    throws GeomagDataException
    {
        super (file.getName().substring(0, 2), null, 
               MISSING_HEADER_VALUE, MISSING_HEADER_VALUE, MISSING_HEADER_VALUE,
               "HDZF", "Definitive", null, null, null, null, null, false, -1);
    }
    
    /** Write data in COBRA format to an output stream
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int count, count2, index;
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
            // write the header
            writeString (os, headerDateFormat.format (date), termType);
            
            // write the data
            for (count2=0; count2<60; count2++)
            {
                index = count + count2;

                if ((count2 %2) == 0) term = "  ".getBytes();
                else term = termType;
                writeString (os, 
                             GeoString.formatAsInteger (getData (0, index, 99999.0, 88888.0), 6) +
                             GeoString.formatAsInteger (getData (0, index, 99999.0, 88888.0) * 10.0, 6) +
                             GeoString.formatAsInteger (getData (0, index, 99999.0, 88888.0), 6) +
                             GeoString.formatAsInteger (getData (0, index, 99999.0, 88888.0), 6),
                             term);
            }
        }
    }
    
     /** generate a COBRA filename
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
    
    /** Creates a new instance of COBRA data by reading it from a file
      * @param is the stream to read from
      * @param file the cobra file being read from
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    public static Cobra read (InputStream is, File file) 
    throws IOException, GeomagDataException
    {
        return read (is, new Cobra (file));        
    }
    
    /** Creates a new instance of COBRA data by reading it from a file
      * @param is the stream to read from
      * @param station_code the station code
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    public static Cobra read (InputStream is, String station_code) 
    throws IOException, GeomagDataException
    {
        return read (is, new Cobra (station_code));
    }
    
    /** generate a COBRA filename
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
        
        switch (station_code.length())
        {
            case 0: filename = "__"; break;
            case 1: filename = station_code + "_"; break;
            default: filename = station_code.substring(0, 2); break;
        }
        filename += filenameDateFormat.format (start_date);
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }

    
    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// private methods below here ////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** the real work of the read method is here */
    private static Cobra read (InputStream is, Cobra cobra) 
    throws IOException, GeomagDataException
    {
        int lineNumber, blockCounter, index;
        double c1 [], c2 [], c3 [], c4 [];
        BufferedReader reader;
        String buffer;
        StringTokenizer tokens;
        Date date;
        
        // buffer the stream
        reader = new BufferedReader (new InputStreamReader (is));

        // read the data
        lineNumber = 0;
        blockCounter = -1;
        c1 = new double [60];
        c2 = new double [60];
        c3 = new double [60];
        c4 = new double [60];
        date = null;
        while ((buffer = reader.readLine()) != null) 
        {
            lineNumber++;
            if (buffer.length() == 0) continue;

            // is this a header
            if (blockCounter ++ < 1)
            {
                // header - attempt to extract fields from the header
                if (buffer.length() != 17)
                    throw new GeomagDataException ("Bad header at line " + Integer.toString (lineNumber));
                try
                {
                    date = headerDateFormat.parse (buffer);
                }
                catch (ParseException e)
                {
                    throw new GeomagDataException ("Bad data in header at line number " + Integer.toString (lineNumber));
                }
            }
            else
            {
                // data - extract two sets of field readings
                tokens = new StringTokenizer (buffer);
                try
                {
                    index = blockCounter *2;
                    c1 [index] = Double.parseDouble (tokens.nextToken());
                    c2 [index] = Double.parseDouble (tokens.nextToken());
                    if (c2 [index] != 99999.0) c2 [index] /= 10.0;
                    c3 [index] = Double.parseDouble (tokens.nextToken());
                    c4 [index] = Double.parseDouble (tokens.nextToken());
                    index ++;
                    c1 [index] = Double.parseDouble (tokens.nextToken());
                    c2 [index] = Double.parseDouble (tokens.nextToken());
                    if (c2 [index] != 99999.0) c2 [index] /= 10.0;
                    c3 [index] = Double.parseDouble (tokens.nextToken());
                    c4 [index] = Double.parseDouble (tokens.nextToken());
                }
                catch (NoSuchElementException e)
                {
                    throw new GeomagDataException ("Missing data fields at line number " + Integer.toString (lineNumber));
                }
                catch (NumberFormatException e)
                {
                    throw new GeomagDataException ("Bad number in data at line number " + Integer.toString (lineNumber));
                }
            }
            
            // at the end of the block send the data to the Cobra object
            if (blockCounter == 30)
            {
                cobra.addData (date, 60000l, 99999.0, 99999.0, c1, c2, c3, c4);
                blockCounter = -1;
            }
        }
        
        // check for an incomplete end block
        if (blockCounter != -1)
            throw new GeomagDataException ("Incomplete final data block");
        
        return cobra;
    }                

}
