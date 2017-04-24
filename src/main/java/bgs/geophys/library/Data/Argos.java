/*
 * Argos.java
 *
 * Created on 13 February 2004, 10:14
 */
package bgs.geophys.library.Data;

import bgs.geophys.library.Misc.DateUtils;

import java.util.*;
import java.text.*;
import java.io.*;

/**
 *
 * @author  bba
 */
public class Argos extends GeomagDataFormat 
{

    /** a class that holds the extra data that is associated with
     * each hourly data block */
    public static class ExtraData
    {
        private int tapeWriteMinute;
        private int tapeWriteSecond;
        private int rangeIndex;
        private double meanX;
        private double meanY;
        private double meanZ;
        private double temperature;
        private int voltageReference;
        private int threeHourRangeIndex;
        private int tapeBlockNumber;
        
        public ExtraData (int tapeWriteMinute, int tapeWriteSecond,
                          int rangeIndex, double meanX, double meanY, double meanZ,
                          double temperature, int voltageReference,
                          int threeHourRangeIndex, int tapeBlockNumber)
        {
            this.tapeWriteMinute = tapeWriteMinute;
            this.tapeWriteSecond = tapeWriteSecond;
            this.rangeIndex = rangeIndex;
            this.meanX = meanX;
            this.meanY = meanY;
            this.meanZ = meanZ;
            this.temperature = temperature;
            this.voltageReference = voltageReference;
            this.threeHourRangeIndex = threeHourRangeIndex;
            this.tapeBlockNumber = tapeBlockNumber;
        }

        public int getTapeWriteMinute() { return tapeWriteMinute; }
        public int getTapeWriteSecond() { return tapeWriteSecond; }
        public int getRangeIndex() { return rangeIndex; }
        public double getMeanX() { return meanX; }
        public double getMeanY() { return meanY; }
        public double getMeanZ() { return meanZ; }
        public double getTemperature() { return temperature; }
        public int getVoltageReference() { return voltageReference; }
        public int getThreeHourRangeIndex() { return threeHourRangeIndex; }
        public int getTapeBlockNumber() { return tapeBlockNumber; }
    }
    
    // formatting objects
    private static SimpleDateFormat headerDateFormat;
    private static SimpleDateFormat filenameDateFormat;

    // static initialisers - mainly creation of formatting objects
    static
    {
        headerDateFormat = new SimpleDateFormat ("yyyyDDDHH", DateUtils.english_date_format_symbols);
        headerDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        filenameDateFormat = new SimpleDateFormat ("MMM'.a'yy", DateUtils.english_date_format_symbols);
        filenameDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    }

    // a store for the extra data
    private ArrayList<ExtraData> extraData;
    
    /** create an Argos format object from the station code
     * @param station_code the station code
     * @throws GeomagDataException if there was a fault */
    public Argos (String station_code)
    throws GeomagDataException
    {
        super (station_code, null, 
               MISSING_HEADER_VALUE, MISSING_HEADER_VALUE, MISSING_HEADER_VALUE,
               "XYZF", "Reported", null, null, null, null, null, false, -1);
        
        extraData = new ArrayList<ExtraData> ();
    }

    /** add extra data - this can only be added after component
     * data has been added with one of the addData() routines. The
     * date/time for the extra data must place the extra data
     * within the date/time/duration of the component data
     * @param date the date/time for the data (to the nearest hour)
     * @param data the extra data to add
     * @throws GeomagDataException if there is an error */
    public void addExtraData (Date date, ExtraData data)
    throws GeomagDataException
    {
        int index, end_index;
        
        // check if the date is valid
        index = (int) ((getStartDate().getTime() - date.getTime()) / 3600000l);
        end_index = getDataLength() / 60;
        if (index < 0 || index >= end_index)
            throw new GeomagDataException ("Cannot add extra data with timestamp outside existing component data");
        
        // do we need to add new elements to the array
        while (index >= extraData.size())
            extraData.add(null);
        
        // add this element to the array
        extraData.set (index, data);
    }
    
    /** get the extra data associated with the given date/hour
     * @param date the date to get extra data for
     * @return the extra data or null if no data exists
     * @throws GeomagDataException if there is an error */
    public ExtraData getExtraData (Date date)
    throws GeomagDataException
    {
        int index, end_index;
        
        // check if the date is valid
        index = (int) ((getStartDate().getTime() - date.getTime()) / 3600000l);
        end_index = getDataLength() / 60;
        if (index < 0 || index >= end_index)
            throw new GeomagDataException ("Cannot add extra data with timestamp outside existing component data");
        
        // return the extra data
        if (index < extraData.size()) return extraData.get (index);
        return null;
    }
    
    /** Write data in Argos format to an output stream - this has not
     * been implemented, so always throws an exception
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        throw new IOException ("Writing ARGOS files is not supported");
    }
    
    /** generate an ARGOS filename
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
    
    /** read an ARGOS file
      * @param is the stream to read from
      * @param year the year for the data (can be obtained from the file name)
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    private static Argos read (InputStream is, int year)
    throws IOException, GeomagDataException
    {
        int index, count, vector_index, scalar_index;
        double x [], y [], z [], f [], avX, avY, avZ, avF;
        LineNumberReader reader;
        String buffer, yearString, dateString, temperatureString, station_code;
        Date date;
        ExtraData extraData;
        Argos argos;
        
        // buffer the stream
        reader = new LineNumberReader (new InputStreamReader (is));

        // read the data
        x = new double [60];
        y = new double [60];
        z = new double [60];
        f = new double [60];
        date = null;
        yearString = Integer.toString (year);
        argos = null;
        while ((buffer = readNextRecord (reader)) != null)
        {
            // extract header fields from this record
            try
            {
                switch (buffer.charAt(0))
                {
                    // annual mean average field values are for 1980
                    case '1': station_code = "LER"; avX = 14812.0; avY = -2039.0; avZ = 47858; avF = 50139; break;
                    case '2': station_code = "ESK"; avX = 17110.0; avY = -2513.0; avZ = 45788; avF = 48939; break;
                    case '3': station_code = "HAD"; avX = 19154.0; avY = -2600.0; avZ = 43768; avF = 47848; break;
                    default:
                        throw new GeomagDataException ("Bad station code in record ending at line number " + Integer.toString (reader.getLineNumber()));
                }
                dateString = yearString + buffer.substring(1, 6);
                date = headerDateFormat.parse (dateString);
                temperatureString = buffer.substring (1003, 1007).replace('D', '.');
                extraData = new ExtraData (Integer.parseInt   (buffer.substring (   6,    8)),
                                           Integer.parseInt   (buffer.substring (   8,   10)),
                                           Integer.parseInt   (buffer.substring ( 987,  988)),
                                           Double.parseDouble (buffer.substring ( 988,  993)),
                                           Double.parseDouble (buffer.substring ( 993,  998)),
                                           Double.parseDouble (buffer.substring ( 998, 1003)),
                                           Double.parseDouble (temperatureString),
                                           Integer.parseInt   (buffer.substring (1007, 1011)),
                                           Integer.parseInt   (buffer.substring (1011, 1012)),
                                           Integer.parseInt   (buffer.substring (1012, 1015)));
            }
            catch (ParseException e)
            {
                throw new GeomagDataException ("Bad date for record ending at line number " + Integer.toString (reader.getLineNumber()));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in header of record ending at line number " + Integer.toString (reader.getLineNumber()));
            }
            
            // extract data from this record
            try
            {
                for (count=0; count<60; count++)
                {
                    vector_index = 10 + (count * 12);
                    scalar_index = 730 + (count * 4);
                    x [count] = getField (buffer, vector_index, avX);
                    y [count] = getField (buffer, vector_index, avY);
                    z [count] = getField (buffer, vector_index, avZ);
                    f [count] = getField (buffer, scalar_index, avF);
                }
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in data of record ending at line number " + Integer.toString (reader.getLineNumber()));
            }
            
            // if this is the first record, create a new Argos object
            if (argos == null) argos = new Argos (station_code);
            else if (! argos.getStationCode().equals(station_code))
                throw new GeomagDataException ("Station code changes in record ending at line number " + Integer.toString (reader.getLineNumber()));

            // add the data
            argos.addData (date, 60000l, MISSING_DATA_SAMPLE, MISSING_COMPONENT,
                           x, y, z, f);
            argos.addExtraData (date, extraData);
        }
        
        return argos;
    }                
    
    /** generate an ARGOS format filename
     * @param prefix the path to the ARGOS format root directory
     * @param station_code the three letter station code
     * @param start_date the start date fot the data
     * @param force_lower_case set true to force the filename to lower case
     * @return the file name */
    public static String makeFilename(String prefix, String station_code,
                                      Date start_date, boolean force_lower_case) 
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
    //////////////////////// private methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    private static String readNextRecord (LineNumberReader reader)
    throws IOException, GeomagDataException
    {
        int n_lines, line_length, count;
        String buffer, line;
        
        // read the next line, ignoring blank lines
        line = reader.readLine();
        if (line == null) return null;
        while (line.length() == 0)
        {
            line = reader.readLine();
            if (line == null) return null;
        }
        
        // at the start of the file there may be a line with the observatory
        // name and the month / year - if so ignore the line
        if (line.length() < 30) line = reader.readLine();
        if (line == null) return null;
        while (line.length() == 0)
        {
            line = reader.readLine();
            if (line == null) return null;
        }
        
        // the record may be broken into multiple lines - if so these should be
        // an integer dividend of the record length (1024)
        line_length = line.length();
        n_lines = 1024 / line_length;
        if (line_length * n_lines != 1024)
            throw new GeomagDataException ("Bad line length in data at line number " + reader.getLineNumber());
        
        // read the remaining lines
        buffer = line;
        for (count=1; count<n_lines; count++)
        {
            line = reader.readLine();
            if (line == null)
                throw new GeomagDataException ("Incomplete record at end of file");
            while (line.length() == 0)
            {
                line = reader.readLine();
                if (line == null)
                    throw new GeomagDataException ("Incomplete record at end of file");
            }
            
            if (line.length() != line_length)
                throw new GeomagDataException ("Incorrect line length at line number " + reader.getLineNumber());
            buffer += line;
        }
        
        return buffer;
    }

    private static double getField (String buffer, int index, double average)
    throws NumberFormatException
    {
        double value, skip, baseline;
        String string;
        
        string = buffer.substring (index, index + 4);
        if (string.equals("9999")) return MISSING_DATA_SAMPLE;

        value = Double.parseDouble (string);
        
        if (average < 0)
        {
            skip = Math.abs (average % 10000);
            baseline = average + skip;
            if (skip < 5000)
            {
                if (value < (skip + 5000)) value = (- value) + baseline;
                else value = (- value) + baseline + 10000;
            }
            else
            {
                if (value < (skip - 5000)) value = (- value) + baseline - 10000;
                else value = (- value) + baseline;
            }
            
        }
        else
        {
            skip = average % 10000;
            baseline = average - skip;
            if (skip < 5000)
            {
                if (value < (skip + 5000)) value += baseline;
                else value += baseline - 10000;
            }
            else
            {
                if (value < (skip - 5000)) value += baseline + 10000;
                else value += baseline;
            }
        }

        return value;
    }
    
}
