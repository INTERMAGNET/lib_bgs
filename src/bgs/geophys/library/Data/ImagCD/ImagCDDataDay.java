/*
 * DataDay.java
 *
 * Created on 24 October 2003, 22:34
 */

package bgs.geophys.library.Data.ImagCD;

import java.io.*;

import bgs.geophys.library.File.*;
import bgs.geophys.library.Misc.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.ZipInputStream;

/**
 * Holds one day of IMAG CD data
 * @author  Administrator
 */
public class ImagCDDataDay 
{
    
    /** value used to flag missing data (except K-indices) */
    public static final int MISSING_DATA    = 999999;
    
    /** value used to flag a missing K index */
    public static final int MISSING_K_INDEX = 999;
    
    /** value used to set a missing numeric header field */
    public static final int MISSING_HEADER_FIELD = -99999;
    
    // other constants
    public final static int N_MINUTE_MEAN_VALUES = 1440;
    public final static int N_HOURLY_MEAN_VALUES = 24;
    public final static int N_DAILY_MEAN_VALUES  = 1;
    public final static int N_K_INDICES = 8;
    public final static int N_COMPONENTS = 4;
  
    // private member variables - raw data
    private byte header [];              // the header information - 16 words = 64 bytes
    private int minute_data [] [];       // organised as [comp (0-3)] [minute (0-1439)]
    private int hour_data [] [];         // organised as [comp (0-3)] [hour (0-23)]
    private int day_data [];             // organised as [comp (0-3)]
    private int k_index_data [];         // organised as [index-for-3-hours (0-7)]
    private byte trailer [];             // 4 words = 16 bytes of reserved space

    // private member variables - interpreted data
    private String station_id;
    private int year, day_number;
    private int colatitude;             // times 1000
    private int longitude;              // times 1000
    private int elevation;              // in meters
    private String recorded_elements;   // the vector orientation of the data in this file: HDZF, XYZF etc
    private String institute_code;      // code for the originating institute
    private int d_conversion;           // factor to change 0.1 minutes to 0.1 nT
    private String quality_code;        // code for data's quality standard
    private String instrument_code;     // code for recording sensor
    private int k9_limit;               // k9-limit used to calculate K-numbers
    private int sample_period;          // sample rate at observatory, in mSec
    private String sensor_orientation;  // orientation of sensor: HDZF, XYZF, DIF and other
    private Date publication_date;      // version of the data as a date - only year and month will be set
    private String format_version;      // version of the data format
    
    // private members - formatters
    private SimpleDateFormat version_date_format;

    /** Creates a new instance of DataDay */
    public ImagCDDataDay() 
    {
        // the sizes of the arrays are important - they are used to control reading from the file
        header = new byte [64];
        minute_data = new int [4] [1440];
        hour_data = new int [4] [24];
        day_data = new int [4];
        k_index_data = new int [8];
        trailer = new byte [16];
       
        version_date_format = new SimpleDateFormat ("yyMM");
        version_date_format.setTimeZone (DateUtils.gmtTimeZone);
        
        emptyData ();
    }

    /** Creates a new instance of DataDay */
    public ImagCDDataDay (int year, int day_number) 
    {
        this ();
        
        setYear (year);
        setDayNumber (day_number);
    }
    
    /** fill this object with empty data */
    public void emptyData ()
    {
        int count;
       
        // set the raw data
        for (count=0; count<header.length; count++) header[count] = 0;
        for (count=0; count<minute_data[0].length; count++) minute_data[0][count] = minute_data[1][count] = minute_data[2][count] = minute_data[3][count]= MISSING_DATA;
        for (count=0; count<hour_data[0].length; count++) hour_data[0][count] = hour_data[1][count] = hour_data[2][count] = hour_data[3][count] = MISSING_DATA;
        day_data[0] = day_data[1] = day_data[2] = day_data[3] = MISSING_DATA;
        for (count=0; count<k_index_data.length; count++) k_index_data[count] = MISSING_K_INDEX;
        for (count=0; count<trailer.length; count++) trailer[count] = 0;
        
        // set the interpreted data
        station_id = "";
        year = day_number = MISSING_HEADER_FIELD;
        colatitude = longitude = elevation = MISSING_HEADER_FIELD;
        recorded_elements = "";
        institute_code = "";
        d_conversion = MISSING_HEADER_FIELD;
        quality_code = "";
        instrument_code = "";
        k9_limit = MISSING_HEADER_FIELD;
        sample_period = MISSING_HEADER_FIELD;
        sensor_orientation = "";
        publication_date = null;
        format_version = "";
    }
        
    /** read data into this object from a file
     * @param file_stream the stream of data from the file 
     * @throws DataException if there is an error - the exception contains
     *         a message describing the problem */
    public void loadFromFile (InputStream file_stream)
    throws ImagCDDataException
    {
        int count, component, length;
        long position;
        byte data_buffer [];
        String error_message;
        
        error_message = "Unknown error";
        try
        {
            // read the header
            error_message = "Error reading header";
            count = FileUtils.readFully(file_stream, header);
            if (count != header.length) throw new ImagCDDataException ("File does not contain enough data");
            
            // interpret the header
            error_message = "Error interpreting header";
            interpretHeader ();

            // read the data
            length = 1440 * 4;
            data_buffer = new byte [length];
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error reading component " + Integer.toString (component +1) + " minute mean data";
                count = FileUtils.readFully(file_stream, data_buffer, 0, length);
                if (count != length) throw new ImagCDDataException ("File does not contain enough data");
                for (count=0; count<1440; count++) minute_data [component] [count] = Utils.bytesToInt (data_buffer, count * 4);
            }
            length = 24 * 4;
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error reading component " + Integer.toString (component +1) + " hourly mean data";
                count = FileUtils.readFully(file_stream, data_buffer, 0, length);
                if (count != length) throw new ImagCDDataException ("File does not contain enough data");
                for (count=0; count<24; count++) hour_data [component] [count] = Utils.bytesToInt (data_buffer, count * 4);
            }
            length = 4;
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error reading component " + Integer.toString (component +1) + " daily mean data";
                count = FileUtils.readFully(file_stream, data_buffer, 0, length);
                if (count != length) throw new ImagCDDataException ("File does not contain enough data");
                day_data [component] = Utils.bytesToInt (data_buffer, 0);
            }
            length = 8 * 4;
            error_message = "Error reading component K index data";
            count = FileUtils.readFully(file_stream, data_buffer, 0, length);
            if (count != length) throw new ImagCDDataException ("File does not contain enough data");
            for (count=0; count<8; count++) k_index_data [count] = Utils.bytesToInt (data_buffer, count * 4);
            
            // read the trailer
            error_message = "Error reading reserved data at end of K indices";
            count = FileUtils.readFully(file_stream, trailer);
            if (count != trailer.length) throw new ImagCDDataException ("File does not contain enough data");
        }
        // error handler - try to tell the user where the problem lies
        catch (Exception e)
        {
            try
            {
                if (file_stream instanceof FileInputStream)
                {
                    position = ((FileInputStream) file_stream).getChannel().position();
                    error_message += " at or near byte " + Long.toString (position);
                }
            }
            catch (Exception e2) { }
            throw new ImagCDDataException (error_message);
        }
    }

    /** write data from this object into a file
     * @param file_stream the stream of data to the file 
     * @throws DataException if there is an error - the exception contains
     *         a message describing the problem */
    public void writeToFile (OutputStream file_stream)
    throws ImagCDDataException
    {
        int count, component;
        long position;
        byte byte_data [];
        String error_message;
        
        error_message = "Unknown error";
        try
        {
            // write the header
            error_message = "Error writing header";
            file_stream.write(header);
            
            // write the data
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error writing component " + Integer.toString (component +1) + " minute mean data";
                for (count=0; count<1440; count++)
                {
                    byte_data = Utils.intToBytes (minute_data [component] [count]);
                    file_stream.write (byte_data);
                }
            }
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error writing component " + Integer.toString (component +1) + " hourly mean data";
                for (count=0; count<24; count++)
                {
                    byte_data = Utils.intToBytes (hour_data [component] [count]);
                    file_stream.write (byte_data);
                }
            }
            for (component = 0; component < 4; component ++)
            {
                error_message = "Error writing component " + Integer.toString (component +1) + " daily mean data";
                byte_data = Utils.intToBytes (day_data [component]);
                file_stream.write (byte_data);
            }
            error_message = "Error writing component K index data";
            for (count=0; count<8; count++)
            {
                byte_data = Utils.intToBytes (k_index_data [count]);
                file_stream.write (byte_data);
            }
            
            // write the trailer
            error_message = "Error writing reserved data at end of K indices";
            file_stream.write(trailer);
        }
        // error handler - try to tell the user where the problem lies
        catch (Exception e)
        {
            try
            {
                if (file_stream instanceof FileOutputStream)
                {
                    position = ((FileOutputStream) file_stream).getChannel().position();
                    error_message += " at or near byte " + Long.toString (position);
                }
            }
            catch (Exception e2) { }
            throw new ImagCDDataException (error_message);
        }
    }

    /** a method to extract the header or trailer as a hex dump */
    public String getHeaderHexDump (int offset, int length)
    {
        return Utils.hexDumpBytes (header, offset, length);
    }
    public String getTrailerHexDump (int offset, int length)
    {
        return Utils.hexDumpBytes (trailer, offset, length);
    }

    // methods to set raw data
    public void setHeaderByte (byte b, int count)                   
    { 
        header [count] = b; 
        interpretHeader ();
    }
    public void setMinuteData (int value, int component, int count) { minute_data [component] [count] = value; }
    public void setHourData (int value, int component, int count)   { hour_data [component] [count] = value; }
    public void setDayData (int value, int component)               { day_data [component] = value; }
    public void setKIndexData (int value, int count)                { k_index_data [count] = value; }
    public void setTrailerByte (byte b, int count)                  { trailer [count] = b; }
    
    // methods to set interpreted data
    public void setStationID (String station_id)
    {
        try
        {
          this.station_id = make4CharString (station_id);
          insertIntoHeader (this.station_id.getBytes("US-ASCII"), 0);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setYear (int year)                 
    { 
        this.year = year;
        insertIntoHeader (Utils.intToBytes ((this.year * 1000) + this.day_number), 4);
    }
    public void setDayNumber (int day_number)            
    { 
        this.day_number = day_number; 
        insertIntoHeader (Utils.intToBytes ((this.year * 1000) + this.day_number), 4);
    }
    public void setColatitude (int colatitude)           
    { 
        this.colatitude = colatitude; 
        insertIntoHeader (Utils.intToBytes (this.colatitude), 8);
    }
    public void setLongitude (int longitude)            
    { 
        this.longitude = longitude; 
        insertIntoHeader (Utils.intToBytes (this.longitude), 12);
    }
    public void setElevation (int elevation)            
    { 
        this.elevation = elevation; 
        insertIntoHeader (Utils.intToBytes (this.elevation), 16);
    }
    public void setRecordedElements (String recorded_elements)  
    { 
        try
        {
          this.recorded_elements = make4CharString (recorded_elements); 
          insertIntoHeader (this.recorded_elements.getBytes("US-ASCII"), 20);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setInstituteCode (String institute_code)     
    { 
        try
        {
          this.institute_code = make4CharString (institute_code); 
          insertIntoHeader (this.institute_code.getBytes("US-ASCII"), 24);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setDConversion (int d_conversion)          
    { 
        this.d_conversion = d_conversion; 
        insertIntoHeader (Utils.intToBytes (this.d_conversion), 28);
    }
    public void setQualityCode (String quality_code)       
    { 
        try
        {
          this.quality_code = make4CharString (quality_code); 
          insertIntoHeader (this.quality_code.getBytes("US-ASCII"), 32);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setInstrumentCode (String instrument_code)    
    { 
        try
        {
          this.instrument_code = make4CharString(instrument_code); 
          insertIntoHeader (this.instrument_code.getBytes("US-ASCII"), 36);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setK9Limit (int k9_limit)
    { 
        this.k9_limit = k9_limit; 
        insertIntoHeader (Utils.intToBytes (this.k9_limit), 40);
    }
    public void setSamplePeriod (int sample_period)         
    { 
        this.sample_period = sample_period; 
        insertIntoHeader (Utils.intToBytes (this.sample_period), 44);
    }
    public void setSensorOrientation (String sensor_orientation) 
    { 
        try
        {
          this.sensor_orientation = make4CharString (sensor_orientation); 
          insertIntoHeader (this.sensor_orientation.getBytes("US-ASCII"), 48);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setPublicationDate (Date publication_date) 
    {
        String string;
        
        try
        {
          this.publication_date = publication_date;
          if (this.publication_date == null)
              string = "    ";
          else
              string = version_date_format.format (publication_date);
          insertIntoHeader(string.getBytes("US-ASCII"), 52);
        }
        catch (Exception e) { interpretHeader(); }
    }
    public void setFormatVersion (String format_version) 
    {
        byte bytes [];
        
        bytes = new byte [4];
        if (format_version.equals("1.0")) bytes [0] = 0;
        else if (format_version.equals("1.1")) bytes [0] = 1;
        else if (format_version.equals("2.0")) bytes [0] = 2;
        else bytes [0] = 0;
        bytes [1] = bytes [2] = bytes [3] = 0;
        insertIntoHeader (bytes, 56);
    }
    
    // methods to get raw data
    public byte getHeaderByte (int count)               { return header [count]; }
    public int getMinuteData (int component, int count) { return minute_data [component] [count]; }
    public int getHourData (int component, int count)   { return hour_data [component] [count]; }
    public int getDayData (int component)               { return day_data [component]; }
    public int getKIndexData (int count)                { return k_index_data [count]; }
    public byte getTrailerByte (int count)              { return trailer [count] ; }
    
    // methods to get interpreted data
    public String getStationID ()         { return station_id; }
    public int getYear ()                 { return year; }
    public int getDayNumber ()            { return day_number; }
    public int getColatitude ()           { return colatitude; }
    public int getLongitude ()            { return longitude; }
    public int getElevation ()            { return elevation; }
    public String getRecordedElements ()  { return recorded_elements; }
    public String getInstituteCode ()     { return institute_code; }
    public int getDConversion ()          { return d_conversion; }
    public String getQualityCode ()       { return quality_code; }
    public String getInstrumentCode ()    { return instrument_code; }
    public int getK9Limit ()              { return k9_limit; }
    public int getSamplePeriod ()         { return sample_period; }
    public String getSensorOrientation () { return sensor_orientation; }
    /** get the publication date
     * @return the date of publication - may be null if the publication date has not been set */
    public Date getPublicationDate ()     { return publication_date; }
    public String getFormatVersion ()     { return format_version; }
    
    /** calculate hourly mean values from minute means
     * @param overwrite_existing set true to overwrite existing values
     *        set false to only fill in gaps (where possible)
     * @param max_missing_pc maximum missing samples (as a percentage)
     *        beyond which mean will be set missing */
    public void calculateHourlyMeans (boolean overwrite_existing, int max_missing_pc)
    {
        int hour_count, minute_count, comp_count, n_samps_reqd;
        int minute_end, sample_count, sample;
        long accumulator;
        
        n_samps_reqd = 60 - ((max_missing_pc * 60) / 100);
        for (comp_count=0; comp_count<N_COMPONENTS; comp_count++)
        {
            for (hour_count=0; hour_count<N_HOURLY_MEAN_VALUES; hour_count++)
            {
                if ((getHourData (comp_count, hour_count) == MISSING_DATA) || overwrite_existing)
                {
                    minute_end = (hour_count +1) * 60;
                    sample_count = 0;
                    accumulator = 0l;
                    for (minute_count=minute_end -60; minute_count<minute_end; minute_count++)
                    {
                        sample = getMinuteData(comp_count, minute_count);
                        if (sample != MISSING_DATA)
                        {
                            accumulator += sample;
                            sample_count ++;
                        }
                    }
                    if (sample_count >= n_samps_reqd)
                        setHourData ((int) (accumulator / (long) sample_count), comp_count, hour_count);
                }
            }
        }
    }
    
    /** calculate daily mean values from minute means
     * @param overwrite_existing set true to overwrite existing values
     *        set false to only fill in gaps (where possible)
     * @param max_missing_pc maximum missing samples (as a percentage)
     *        beyond which mean will be set missing */
    public void calculateDailyMeans (boolean overwrite_existing, int max_missing_pc)
    {
        int minute_count, comp_count, n_samps_reqd;
        int sample_count, sample;
        long accumulator;
        
        n_samps_reqd = 1440 - ((max_missing_pc * 1440) / 100);
        for (comp_count=0; comp_count<N_COMPONENTS; comp_count++)
        {
            if ((getDayData(comp_count) == MISSING_DATA) || overwrite_existing)
            {
                sample_count = 0;
                accumulator = 0l;
                for (minute_count=0; minute_count<N_MINUTE_MEAN_VALUES; minute_count++)
                {
                    sample = getMinuteData(comp_count, minute_count);
                    if (sample != MISSING_DATA)
                    {
                        accumulator += sample;
                        sample_count ++;
                    }
                }
                if (sample_count >= n_samps_reqd)
                    setDayData ((int) (accumulator / (long) sample_count), comp_count);
            }
        }
    }
        
    /** interpret the header and trailer from raw data to decoded */
    private void interpretHeader ()
    {
        int count;

        station_id = bytes2String (header, 0, 4, false);
        count = Utils.bytesToInt  (header, 4);
        year = count / 1000;
        day_number = count % 1000;
        colatitude = Utils.bytesToInt    (header, 8);
        longitude = Utils.bytesToInt     (header, 12);
        elevation = Utils.bytesToInt     (header, 16);
        recorded_elements = bytes2String (header, 20, 4, false);
        institute_code = bytes2String    (header, 24, 4, false);
        d_conversion = Utils.bytesToInt  (header, 28);
        quality_code = bytes2String      (header, 32, 4, false);
        instrument_code = bytes2String   (header, 36, 4, false);
        k9_limit = Utils.bytesToInt      (header, 40);
        sample_period = Utils.bytesToInt (header, 44);
        sensor_orientation = bytes2String(header, 48, 4, false);
        
        try
        {
            publication_date = version_date_format.parse (bytes2String (header, 52, 4, true));
        }
        catch (ParseException e)
        {
            publication_date = null;
        }
        
        switch (header [56])
        {
            case 0: format_version = "1.0"; break;
            case 1: format_version = "1.1"; break;
            case 2: format_version = "2.0"; break;
            default: format_version = "Unknown"; break;
        }
    }

    /** insert bytes into the header
     * @param bytes - the bytes to insert
     * @param offset - the starting point for the insertion */
    private void insertIntoHeader (byte bytes [], int offset)
    {
        int count;

        for (count=0; count<bytes.length; count++) header[count + offset] = bytes [count];
    }
    
    private void insertIntoTrailer (byte bytes[], int offset) {
        for ( int index=0; index<bytes.length; index++) trailer[index + offset] = bytes[index];
    }
    
    /** convert a sequence of bytes into a string - only converts that basic
     *  ASCII set between 32 (' ') and 127 ('~')
     * @param bytes - the bytes to convert
     * @param offset - the starting point to begin conversion in the byte array
     * @param length - the number of bytes to convert
     * @param stop_at_ctrl - if TRUE stop the conversion at non-printing chars
     *                       otherwise convert unprinting chars using '.'
     * @returns the string */
    private static String bytes2String (byte bytes [], int offset, int length, boolean stop_at_ctrl)
    {
        int count, position;
        String string;
        
        string = "";
        for (count=0; count<length; count++)
        {
            position = count + offset;
            if (bytes [position] < 32 || bytes [position] > 127)
            {
                if (stop_at_ctrl) return string;
                string = string + ".";
            }
            else string = string + ((char) bytes[position]);
        }
        return string;
    }
    
    /** make a string 4 characters long by truncating oversize strings and padding short ones 
     * @param string the string to convert
     * @return the 4 character string */
    private static String make4CharString (String string)
    {
        switch (string.length())
        {
            case 0: return "    ";
            case 1: return "   " + string;
            case 2: return "  " + string;
            case 3: return " " + string;
            case 4: return string;
        }
        return string.substring(string.length()-4, string.length());
    } 
}
