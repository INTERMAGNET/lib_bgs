/*
 * PlainData.java
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
public class PlainData extends GeomagDataFormat 
{
   
    // formatting objects
    private static DecimalFormat valueFormat;
    private static SimpleDateFormat dataDateFormat;
    
    // static initialisers - mainly creation of formatting objects
    static
    {
        valueFormat = new DecimalFormat ("######0.00", Utils.getScientificDecimalFormatSymbols());
        dataDateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS", DateUtils.english_date_format_symbols);
        dataDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    }
    
    /**
     * Creates a new instance of PlainData data by supplying all data
     * 
     * @param station_code the 'IAGA code' header field
     * @param comp_code the geomagnetic component codes
     * @param data_type the state of publication
     * @throws GeomagDataException if there was an error in the data - the
     *         messages associated with the exception will describe the problem
     */
    public PlainData (String station_code, String comp_code, String data_type)
    throws GeomagDataException
    {
        super (station_code, null, MISSING_HEADER_VALUE, 
               MISSING_HEADER_VALUE, MISSING_HEADER_VALUE,
               comp_code, data_type, null, null, null,
               null, null, true, -1);
    }
    
    /** Write the data in IAGA 2002 format to a new file
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int count;
        
        // check that there is some data to write
        if (getDataLength() < 1)
            throw new GeomagDataException ("No data to write");
        
        // write the header items
        writeString (os, 
                     GeoString.fix (getStationCode(), 3, true, false) + " " +
                     GeoString.fix (getComponentCodes(), 4, true, false) + " " +
                     GeoString.fix (getDataType(), 12, true, false) + " " +
                     GeoString.fix (Long.toString (getSamplePeriod() / 1000l), 5, true, false) + " " +
                     dataDateFormat.format (getStartDate()),
                     termType );
        
        // write the data items
        for (count=0; count<getDataLength(); count++) 
        {
             writeString (os,
                          GeoString.pad (valueFormat.format (getData(0, count, 99999.99, 88888.88)), 9, true) + " " +
                          GeoString.pad (valueFormat.format (getData(1, count, 99999.99, 88888.88)), 9, true) + " " +
                          GeoString.pad (valueFormat.format (getData(2, count, 99999.99, 88888.88)), 9, true) + " " +
                          GeoString.pad (valueFormat.format (getData(3, count, 99999.99, 88888.88)), 9, true),
                          termType);
        }
    }

    /**
     * generate an PlainData filename
     * 
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the file OR null if there is an error
     */
    public String makeFilename (String prefix, boolean force_lower_case)
    {
        return makeFilename (prefix, getStationCode (), force_lower_case);
    }
    

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new instance of PlainData data by reading it from a file
     * 
     * @param is the stream to read from
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if there was an error reading the file
     * @throws GeomagDataException if there was an error in the data format - the
     *         messages associated with the exception will describe the problem
     */
    public static PlainData read (InputStream is)
    throws FileNotFoundException, IOException, GeomagDataException
    {
        int count, line_number, data_count;
        long sample_period;
        Date date, startDate;
        String buffer;
        BufferedReader reader;
        StringTokenizer tokens;
        PlainData plain;

        // open the file
        reader = new BufferedReader (new InputStreamReader (is));

        // read the header line
        buffer = reader.readLine();
        if (buffer == null)
            throw new GeomagDataException ("Empty file");
        tokens = new StringTokenizer (buffer);
        if (tokens.countTokens() != 6)
            throw new GeomagDataException ("Incorrect number of fields in header");
        plain = new PlainData (tokens.nextToken(), tokens.nextToken(), tokens.nextToken());
        try
        {
            sample_period = Long.parseLong (tokens.nextToken());
            startDate = dataDateFormat.parse (tokens.nextToken() + " " + tokens.nextToken());
        }
        catch (NumberFormatException e)
        {
            throw new GeomagDataException ("Bad sample period in header");
        }
        catch (ParseException e)
        {
            throw new GeomagDataException ("Bad date in header");
        }
        
        line_number = 1;
        data_count = 0;
        while ((buffer = reader.readLine()) != null)
        {
            line_number ++;
            
            tokens = new StringTokenizer (buffer);
            if (tokens.countTokens() != 4) 
                throw new GeomagDataException ("Bad data at line number " + Integer.toString (line_number));
            
            date = new Date (startDate.getTime() + (sample_period * (long) data_count));
            try 
            { 
                plain.addData (date, sample_period,
                               99999.99, 88888.88,
                               Double.parseDouble (tokens.nextToken()),
                               Double.parseDouble (tokens.nextToken()),
                               Double.parseDouble (tokens.nextToken()),
                               Double.parseDouble (tokens.nextToken()));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad data at line number " + Integer.toString (line_number));
            }
            
            data_count ++;
        }
                
        // check for not enough data
        switch (data_count)
        {
        case 0:
            throw new GeomagDataException ("No data in file");
        case 1:
            throw new GeomagDataException ("File must contain more than a single sample");
        }
        
        return plain;
    }

    /** generate an IAGA 2002 filename
     * @param prefix the prefix for the name (including any directory)
     * @param station_code the IAGA station code
     * @param start_date the start date for the data 
     * @param data_type the data type code
     * @param sample_period the period between samples, in milliseconds
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the file OR null if there is an error */
    public static String makeFilename (String prefix,
                                       String station_code,
                                       boolean force_lower_case)
    {
        String filename;
        
        filename = station_code + ".dat";
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }
        
}
