/*
 * Iaga2002.java
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
public class Iaga2002 extends GeomagDataFormat 
{

    // NOTE: The IAGA-2002 spec does not define missing sample or component values
    // as a single value, as suggested below. The best interpretation of the spec
    // is to consider a value > 9999.0 with interger part all 9's (in effect,
    // 9999, 99999, or 999999) as a missing value. Similarly for missing component,
    // only replace the 9's with 8's.
    // Thus, the only accurate way to test for a missing sample or component is
    // to use the static methods isMissingSample(double) and isMissingComponent(double)

    /** code for a missing data sample */
    public static final double MISSING_DATA_SAMPLE = 99999.0;
    /** code for a missing component - is unrecorded component */
    public static final double MISSING_COMPONENT = 88888.0;

    public static boolean isMissingSample(double value) {
        if (value < 9999.0) return false;
        for(char digit : String.valueOf((int)value).toCharArray()) {
            if (digit != '9') return false;
        }
        return true;
    }
    
    public static boolean isMissingComponent(double value) {
        if (value < 8888.0) return false;
        for(char digit : String.valueOf((int)value).toCharArray()) {
            if (digit != '8') return false;
        }
        return true;
    }

    // these members hold the header in addition to those inherited from DataFormat
    private List<String> raw_header_lines;        // these are the unformatted comments
    
    // a count of the number of header lines (including the line that holds the column headings)
    private int n_header_lines;
    
    // a flag showing whether we need to swap the first two conponents when writing data
    boolean swap_hdzf_to_dhzf;

    /*
     * Caution! DecimalFormat and SimpleDateFormat classes are not thread safe
     */
    // formatting objects
    private DecimalFormat latFormat;
    private DecimalFormat longFormat;
    private DecimalFormat valueFormat;
    private static final SimpleDateFormat dataDateFormat;
    private SimpleDateFormat dataDateFormatIncDN;
    
    // static initialisers - mainly creation of formatting objects
    static
    {        
        dataDateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS", DateUtils.english_date_format_symbols);
        dataDateFormat.setTimeZone(DateUtils.gmtTimeZone);        
    }
    
    /** Creates a new instance of Iaga2002 by supplying all data
     * @param station_code the 'IAGA code' header field
     * @param station_name the 'Station name' header field
     * @param latitude the 'Geodetic latitude' header field
     * @param longitude the 'Geodetic longitude' header field
     * @param elevation the 'Elevation' header field
     * @param comp_code the 'Reported' header field
     * @param data_type the 'Data type' header field
     * @param institute_name the 'Source of Data' header field
     * @param sensor_orientation the 'Sensor orientation' header field
     * @param sample_period_string the 'Digital sampling' header field
     * @param interval_type the 'Data interval type' header field
     * @throws GeomagDataException if there was an error in the data - the
     *         messages associated with the exception will describe the problem */
    public Iaga2002 (String station_code, String station_name, 
                     double latitude, double longitude, double elevation, 
                     String comp_code, String data_type,
                     String institute_name,
                     String sensor_orientation, String sample_period_string, 
                     String interval_type, ArrayList<String> comments)
    throws GeomagDataException
    {
        super (station_code, station_name, latitude, longitude, elevation,
               comp_code, data_type, null, institute_name, sensor_orientation,
               sample_period_string, interval_type, true, -1);
        raw_header_lines = new ArrayList<String> ();
        for(int i=0;i<comments.size();i++){
            raw_header_lines.add(comments.get(i));
        }
        n_header_lines = 13 + raw_header_lines.size();
        swap_hdzf_to_dhzf = true;
        initFormatObjects();
    }

    public Iaga2002 (String station_code, String station_name,
                     double latitude, double longitude, double elevation,
                     String comp_code, String data_type,
                     String institute_name,
                     String sensor_orientation, String sample_period_string,
                     String interval_type)
    throws GeomagDataException
    {
        super (station_code, station_name, latitude, longitude, elevation,
               comp_code, data_type, null, institute_name, sensor_orientation,
               sample_period_string, interval_type, true, -1);
        raw_header_lines = new ArrayList<String> ();
        n_header_lines = 13;
        swap_hdzf_to_dhzf = true;
        initFormatObjects();
    }
    /**
     * Constructor with the same signature as the GeomagDataFormat parent class
     * Designed to give all subclasses of GeomagDataFormat a uniform constructor.
     * Delegates creation of a new instance of Iaga2002 to the appropriate Uaga2002 constructor
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
    public Iaga2002(String stationCode, String stationName, double latitude, double longitude, double elevation, String compCode, String dataType, String ginCode, String instituteName, String sensorOrientation, String samplePeriodString, String intervalType, boolean allowFourDigitSC, int blocksize) throws GeomagDataException {
        this(stationCode, stationName, latitude, longitude, elevation, compCode, dataType, instituteName, sensorOrientation, samplePeriodString, intervalType);
        n_header_lines = 13;
        initFormatObjects();
    }

    /** Add a line to the optional header records - the lines should not have a leading '#'
     * @param header the new header, which is added at the end of the exsiting records */
    public void addOptionalHeaderLine (String header)
    {
        raw_header_lines.add (header);
        n_header_lines ++;
    }

    /** get the total number of header lines (including the line that holds the column hedings
     * @return the number of heading lines
     */
    public int getNHeaderLines () { return n_header_lines; }

    /** get the number of raw header lines
     * @return the number of lines */
    public int getNExtraHeaderLines () { return raw_header_lines.size(); }
    
    /** get an individual raw header line
     * @return the line, including leading '#' */
    public String getExtraHeaderLine (int index) { return raw_header_lines.get (index); }
    
    /** get the unswapped component codes (if the first two were swapped
     * @return the component codes */
    public String getOriginalComponentCodes ()
    {
        if (swap_hdzf_to_dhzf && getComponentCodes().substring(0,2).equalsIgnoreCase("HD"))
            return "DH" + getComponentCodes().substring(2);
        return getComponentCodes();
    }
    
    /** Get the numerical value of the "Digital Sampling" header field in milliseconds.
     *  The value -1 is returned if the field was not specified in the header,
     *  while the value -2 is returned if there was an error in parsing the header field.
     *  
     *  @return the digital sampling period im milliseconds, or a negative number if an error occurred.
     */
    public int getInstrumentSamplingPeriod() {
        // First, check to see if the string is null or empty:
        if ( (this.getSamplePeriodString() == null) || (this.getSamplePeriodString().isEmpty()) )
            return -1;
        
        // Parse the string, using space characters as separators
        String[] samplePeriodStringTokens = this.getSamplePeriodString().split("[ ]+");
        
        // There should be two array elements - a value and a unit.
        if ( samplePeriodStringTokens.length != 2 )
            return -2;
        
        try {
            // Now return the sample period based on the parsed value and unit.
            // Currently, only units beginning with the string "second" or "millisecond"
            // are recognised.
            double samplePeriodValue = Double.parseDouble(samplePeriodStringTokens[0]);
            String samplePeriodUnit = samplePeriodStringTokens[1];
            if ( samplePeriodUnit.toLowerCase().startsWith("second") )
                return (int) (1000 * samplePeriodValue);
            else if ( samplePeriodUnit.toLowerCase().startsWith("millisecond") )
                return (int) samplePeriodValue;
            else return -2;
        } catch ( NumberFormatException e ) {
            return -2;
        }
        
    }

    /** Write the data in IAGA 2002 format to a new file
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int count, column1, column2;
        boolean do_swap;
        String comp_code, three_digit_code, four_digit_comp_code;
        Date date;
        char F_type;

        // do we need to swap from HDZF to DHZF
        if (swap_hdzf_to_dhzf && getComponentCodes().substring(0,2).equalsIgnoreCase("HD"))
            do_swap = true;
        else
            do_swap = false;
        
        // retrieve the component code
        if (do_swap) 
        {
            comp_code = "DH" + getComponentCodes().substring(2);
            column1 = 1;
            column2 = 0;
        }
        else
        {
            comp_code = getComponentCodes();
            column1 = 0;
            column2 = 1;
        }

        // get fourth component code if not available in the data

        four_digit_comp_code = GeoString.fix (comp_code, 4, false, true, '?');
        
        // check that there are at least 2 samples - without two samples
        // we can't work out the sample rate when reading the data
        if (getDataLength() < 2)
            throw new GeomagDataException ("Cannot write less than two samples");
        
        // write the header items
        writeString (os, formatMandatoryHeader ("Format", "IAGA-2002"), termType);
        writeString (os, formatMandatoryHeader ("Source of Data", getInstituteName()), termType);
        writeString (os, formatMandatoryHeader ("Station Name", getStationName()), termType);
        writeString (os, formatMandatoryHeader ("IAGA CODE", getStationCode()), termType);
        writeString (os, formatMandatoryHeader ("Geodetic Latitude", latFormat.format(getLatitude())), termType);
        writeString (os, formatMandatoryHeader ("Geodetic Longitude", longFormat.format (getLongitude())), termType);
        writeString (os, formatMandatoryHeader ("Elevation", Integer.toString ((int)getElevation())), termType);
        writeString (os, formatMandatoryHeader ("Reported", four_digit_comp_code), termType);
        writeString (os, formatMandatoryHeader ("Sensor Orientation", getSensorOrientation()), termType);
        writeString (os, formatMandatoryHeader ("Digital Sampling", getSamplePeriodString()), termType);
        writeString (os, formatMandatoryHeader ("Data Interval Type", getIntervalType()), termType);
        writeString (os, formatMandatoryHeader ("Data Type", getDataType()), termType);
        for (count=0; count<raw_header_lines.size(); count++)
            writeString (os, formatOptionalHeader (raw_header_lines.get (count)), termType);
        
        // write the title line
        three_digit_code = GeoString.fix (getStationCode(), 3, false, true, '?');
        writeString (os,
                     "DATE       TIME         DOY     " +
                     three_digit_code + four_digit_comp_code.substring(0, 1) + "      " +
                     three_digit_code + four_digit_comp_code.substring(1, 2) + "      " +
                     three_digit_code + four_digit_comp_code.substring(2, 3) + "      " +
                     three_digit_code + four_digit_comp_code.substring(3, 4) + "   |",
                     termType);
        
        
        // write the data items
        for (count=0; count<getDataLength(); count++) 
        {
             date = new Date (getStartDate().getTime() + ((long) count * getSamplePeriod()));
             writeString (os,
                          dataDateFormatIncDN.format (date) + "   " +
                          GeoString.pad (valueFormat.format (getData(column1, count, 99999.00, 88888.00)), 10, true) +
                          GeoString.pad (valueFormat.format (getData(column2, count, 99999.00, 88888.00)), 10, true) +
                          GeoString.pad (valueFormat.format (getData(2, count, 99999.00, 88888.00)), 10, true) +
                          GeoString.pad (valueFormat.format (getData(3, count, 99999.00, 88888.00)), 10, true),
                          termType);
        }
    }

    /** generate an IAGA 2002 filename
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the file OR null if there is an error */
    public String makeFilename (String prefix, boolean force_lower_case)
    {
        if (getStartDate() == null) return null;
        return makeFilename (prefix, getStationCode (), getStartDate (), 
                             getDataType(), getSamplePeriod(), force_lower_case);
    }
        
    /** allow HDZF to be swapped to DHZF on writing data */
    public void setSwapHDZFToDHZF (boolean swap_hdzf_to_dhzf) { this.swap_hdzf_to_dhzf = swap_hdzf_to_dhzf; }
    

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** get the name of the units corresponding to particular geomagnetic elements
     * @param element string containing the single element character, e.g. H, D, Z, X, Y, ...
     * @return the name of the units */
    public static String getUnits (String element)
    {
        if ("XYZHEVFSG".indexOf(element.toUpperCase()) >= 0)
            return "nT";
        else if ("DI".indexOf(element.toUpperCase()) >= 0)
            return "Minutes of arc";
        return "Unknown";
    }
    
    /** format a mandatory header line
     * @param name the name for the line
     * @param value the value of the line
     * @return the formatted string */
    public static String formatMandatoryHeader (String name, String value)
    {
        int count, end;
        char buffer [];
        
        buffer = new char [70];
        for (count=0; count<69; count++) buffer [count] = ' ';
        buffer [69] = '|';
        if (name.length() > 22) end = 22;
        else end = name.length();
        try { name.getChars(0, end, buffer, 1); }
        catch (IndexOutOfBoundsException e) { }
        if (value == null) value = "";
        if (value.length() > 45) end = 45;
        else end = value.length();
        try { value.getChars(0, end, buffer, 24); }
        catch (IndexOutOfBoundsException e) { }
        
        return new String (buffer);
    }

    /** format an optional header line
     * @param value the value of the line
     * @return the formatted string */
    public static String formatOptionalHeader (String value)
    {
        int count, end;
        char buffer [];
        
        buffer = new char [70];
        for (count=0; count<69; count++) buffer [count] = ' ';
        buffer [1] = '#';
        buffer [69] = '|';
        if (value.length() > 66) end = 66;
        else end = value.length();
        try { value.getChars(0, end, buffer, 3); }
        catch (IndexOutOfBoundsException e) { }

        return new String (buffer);
    }
    
    /** Creates a new instance of Iaga2002 data by reading it from a file
      * @param is the stream to read from
      * @throws FileNotFoundException if the file was not found
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    /** file with no elevation is allowed if check_elevation is false, otherwise error
     * if no elevation   */
    public static Iaga2002 read (InputStream is)
    throws FileNotFoundException, IOException, GeomagDataException
    {
        return read(is, true);
    }
    
    public static Iaga2002 read (InputStream is, boolean check_elevation)
    throws FileNotFoundException, IOException, GeomagDataException
    {
        int count, line_number, day_of_year, data_count, column1, column2;
        long sample_period;
        boolean readingHeaders, swap_dhzf_to_hdzf;
        double values [], latitude, longitude, elevation, value_store [];
        String buffer, header_name, header_value;
        String institute_name, station_name, station_code;
        String comp_code, sensor_orientation, sample_period_string;
        String interval_type, data_type;
        Date date, last_date;
        BufferedReader reader;
        StringTokenizer tokens;
        Iaga2002 iaga2002;
        List<String> raw_header_lines;

        // fill all values with nulls
        institute_name = station_name = station_code = null;
        comp_code = sensor_orientation = sample_period_string = null;
        interval_type = data_type = null;
        latitude = longitude = elevation = MISSING_HEADER_VALUE;
        raw_header_lines = new ArrayList<String> ();
        values = new double [4];
        value_store = new double [4];
        swap_dhzf_to_hdzf = false;
        
        // open the file
        reader = new BufferedReader (new InputStreamReader (is));
        
        // read the lines from the file
        line_number = 0;
        readingHeaders = true;
        data_count = 0;
        date = null;
        iaga2002 = null;
        last_date = null;
        while (readingHeaders && ((buffer = reader.readLine()) != null))
        {
            // Read the headers at the start of the file
            
            line_number ++;
            
            // check the length of the line
            if (buffer.length() != 70) throw new GeomagDataException ("Incorrect line length at line number " + Integer.toString (line_number));
            
            // work out what type of line this is - first check for compulsory
            // header lines, then optional headers, then the "DATE..." line
            // if the line does not fit any of these, it must be a data line
            header_name = buffer.substring(0, 24).trim();
            header_value = buffer.substring(24, 69).trim();
            if (header_name.equalsIgnoreCase("format")) {
                if (! header_value.equalsIgnoreCase("iaga-2002"))
                    throw new GeomagDataException("Unrecognised data format at line number " + Integer.toString(line_number));
            } else if (header_name.equalsIgnoreCase("source of data"))
                institute_name = header_value;
            else if (header_name.equalsIgnoreCase("station name"))
                station_name = header_value;
            else if (header_name.equalsIgnoreCase("iaga code"))
                station_code = header_value;
            else if (header_name.equalsIgnoreCase("geodetic latitude")) {
                try { latitude = Double.parseDouble(header_value); } catch (Exception e) { throw new GeomagDataException("Bad latitude at line number " + Integer.toString(line_number)); }
            } else if (header_name.equalsIgnoreCase("geodetic longitude")) {
                try { longitude = Double.parseDouble(header_value); } catch (Exception e) { throw new GeomagDataException("Bad longitude at line number " + Integer.toString(line_number)); }
            } else if (header_name.equalsIgnoreCase("elevation")) {
                try { elevation = Double.parseDouble(header_value); } catch (Exception e) { 
                    if(check_elevation)throw new GeomagDataException("Bad elevation at line number " + Integer.toString(line_number)); }
            } else if (header_name.equalsIgnoreCase("reported"))
                comp_code = header_value;
            else if (header_name.equalsIgnoreCase("sensor orientation"))
                sensor_orientation = header_value;
            else if (header_name.equalsIgnoreCase("digital sampling"))
                sample_period_string = header_value;
            else if (header_name.equalsIgnoreCase("data interval type"))
                interval_type = header_value;
            else if (header_name.equalsIgnoreCase("data type"))
                data_type = header_value;
            else if (header_name.length() <= 0) {
                // do nothing
            } else if (header_name.substring(0, 1).equals("#"))
                raw_header_lines.add(buffer.substring(2, 69).trim());
            else if (header_name.length() < 4) {
                // do nothing
            } else if (header_name.substring(0, 4).equalsIgnoreCase("date")) {
                // ignore the data header line - there's nothing new in it
                // but it shows that we are at the end of the headers, so
                // check all the header data and create the Iaga2002 object
                if (institute_name == null)
                    throw new GeomagDataException("'Source of Data' missing from header");
                if (station_name == null)
                    throw new GeomagDataException("'Station name' missing from header");
                if (latitude == MISSING_HEADER_VALUE)
                    throw new GeomagDataException("'Geodetic Latitude' missing from header");
                if (longitude == MISSING_HEADER_VALUE)
                    throw new GeomagDataException("'Geodetic Longitude' missing from header");
                if (elevation == MISSING_HEADER_VALUE)
                    if(check_elevation) throw new GeomagDataException("'Elevation' missing from header");
                if (comp_code == null)
                    throw new GeomagDataException("'Reported' code missing from header");
                if (sensor_orientation == null)
                    throw new GeomagDataException("'Sensor Orientation' missing from header");
                if (sample_period_string == null)
                    throw new GeomagDataException("'Digital Sampling' missing from header");
                if (interval_type == null)
                    throw new GeomagDataException("'Data Interval Type' missing from header");
                if (data_type == null)
                    throw new GeomagDataException("'Data Type' missing from header");
                
                // we are going to swap DHZF data to HDZF to make the internal reading consistent with all
                // other objects that are based on GeomagDataFormat
                if (comp_code.length() >= 2)
                {
                    if (comp_code.substring(0, 2).equalsIgnoreCase("DH"))
                    {
                        swap_dhzf_to_hdzf = true;
                        comp_code = "HD" + comp_code.substring(2);
                    }
                }
                        
                try {
                    iaga2002 = new Iaga2002(station_code, station_name,
                            latitude, longitude, elevation,
                            comp_code, data_type, institute_name,
                            sensor_orientation, sample_period_string,
                            interval_type);
                    iaga2002.setSwapHDZFToDHZF(swap_dhzf_to_hdzf);
                } catch (GeomagDataException e) { throw new GeomagDataException(e.getMessage() + " at line number " + Integer.toString(line_number)); }
                for (count=0; count<raw_header_lines.size(); count++)
                    iaga2002.addOptionalHeaderLine(raw_header_lines.get(count));
                
                readingHeaders = false;
            } else
                throw new GeomagDataException("Bad header line at line number " + Integer.toString(line_number));
        }
        // Finished reading headers        
        
        // Start reading data now
        if (swap_dhzf_to_hdzf)
        {
            column1 = 1;
            column2 = 0;
        }
        else
        {
            column1 = 0;
            column2 = 1;
        }
        while ((buffer = reader.readLine()) != null)
        {
            line_number ++;
            // check the length of the line
            if (buffer.length() != 70) throw new GeomagDataException ("Incorrect line length at line number " + Integer.toString (line_number));
            
            // this should be a data line - parse and extract the data fields
            last_date = date;
            tokens = new StringTokenizer(buffer);
            if (tokens.countTokens() < 7)
                throw new GeomagDataException("Bad data at line number " + Integer.toString(line_number));
            try { date = dataDateFormat.parse(tokens.nextToken() + " " + tokens.nextToken()); } catch (Exception e) { throw new GeomagDataException("Bad date or time at line number " + Integer.toString(line_number)); }
            tokens.nextToken(); // ignore the day of year field
            try {
                for (count = 0; count<4; count++) {
                    values [count] = Double.parseDouble(tokens.nextToken());
                    // adjust missing values
                    if (values [count] >= 99999.0) values [count] = 99999.0;
                    else if (values [count] >= 88888.0) values [count] = 88888.0;
                }
            } catch (Exception e) { throw new GeomagDataException("Bad component data at line number " + Integer.toString(line_number)); }
            
            // calculate and check the sample period
            try {
                switch (data_count ++) {
                    case 0:
                        // can't do anything yet so record the data
                        for (count = 0; count<4; count++) value_store [count] = values [count];
                        break;
                    case 1:
                        // calculate the sample period and record the first two data points
                        sample_period = date.getTime() - last_date.getTime();
                        iaga2002.addData(last_date, sample_period, 99999.0, 88888.0,
                                value_store [column1], value_store [column2],
                                value_store [2], value_store [3]);
                        iaga2002.addData(date, sample_period, 99999.0, 88888.0,
                                values [column1], values [column2],
                                values [2], values [3]);
                        break;
                    default:
                        sample_period = date.getTime() - last_date.getTime();
                        iaga2002.addData(date, sample_period, 99999.0, 88888.0,
                                values [column1], values [column2],
                                values [2], values [3]);
                        break;
                }
            } catch (GeomagDataException e) { throw new GeomagDataException(e.getMessage() + " at line number " + Integer.toString(line_number)); }
        }

        // check for not enough data
        switch (data_count)
        {
        case 0:
            throw new GeomagDataException ("No data in file");
        case 1:
            // try to guess the sample rate from the 'data interval type' header
            buffer = interval_type.toUpperCase();
            if (buffer.indexOf ("1-MINUTE") >= 0) sample_period = 60000l;
            else if (buffer.indexOf ("1-SECOND") >= 0) sample_period = 100l;
            else sample_period = -1;
            if (sample_period <= 0)
                throw new GeomagDataException ("Unknown sample rate: File must contain valid 'data interval type' header or more than a single sample");
            else if (date == null)
                throw new GeomagDataException ("Internal IAGA2002 error: missing sample date");
            else
                iaga2002.addData(date, sample_period, 99999.0, 88888.0,
                                 value_store [column1], value_store [column2],
                                 value_store [2], value_store [3]);
        }
        
        
        return iaga2002;
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
                                       String station_code, Date start_date,
                                       String data_type, long sample_period,
                                       boolean force_lower_case)
    {
        String file_ext, filename, data_type_code;
        String file_period;
        SimpleDateFormat date_format;
        
        
        // set up the parts of the filename that depend on the sample period
        if (sample_period <= 1000)               // second data
        {
            date_format = new SimpleDateFormat ("yyyyMMdd");
            file_period = "sec";
            file_ext = ".sec";
        }
        else if (sample_period <= 60000)         // minute data
        {
            date_format = new SimpleDateFormat ("yyyyMMdd");
            file_period = "min";
            file_ext = ".min";
        }
        else if (sample_period <= 3600000)       // hour data
        {
            date_format = new SimpleDateFormat ("yyyyMM");
            file_period = "hor";
            file_ext = ".hor";
        }
        else if (sample_period <= 86400000)      // day data
        {
            date_format = new SimpleDateFormat ("yyyy");
            file_period = "day";
            file_ext = ".day";            
        }
        else                                     // month data
        {
            date_format = new SimpleDateFormat ("yyyy");
            file_period = "mon";
            file_ext = ".mon";
        }
        date_format.setTimeZone(DateUtils.gmtTimeZone);
        
        // translate data type codes
        if (data_type.equalsIgnoreCase("Definitive")) data_type_code = "d";
        else if (data_type.equalsIgnoreCase("D")) data_type_code = "d";
        else if (data_type.equalsIgnoreCase("Quasi-Definitive")) data_type_code = "q";
        else if (data_type.equalsIgnoreCase("Q")) data_type_code = "q";
        else if (data_type.equalsIgnoreCase("Provisional")) data_type_code = "p";
        else if (data_type.equalsIgnoreCase("P")) data_type_code = "p";
        else if (data_type.equalsIgnoreCase("Adjusted")) data_type_code = "p";
        else if (data_type.equalsIgnoreCase("A")) data_type_code = "p";
        else if (data_type.equalsIgnoreCase("Variation")) data_type_code = "v";
        else if (data_type.equalsIgnoreCase("V")) data_type_code = "v";
        else if (data_type.equalsIgnoreCase("Reported")) data_type_code = "v";
        else if (data_type.equalsIgnoreCase("R")) data_type_code = "v";
        else data_type_code = "";
        
        filename = station_code + date_format.format (start_date) + data_type_code +file_period+ file_ext;
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }

    private void initFormatObjects(){
        latFormat = new DecimalFormat ("00.000", Utils.getScientificDecimalFormatSymbols());
        longFormat = new DecimalFormat ("000.000", Utils.getScientificDecimalFormatSymbols());
        valueFormat = new DecimalFormat ("######0.00", Utils.getScientificDecimalFormatSymbols());
        dataDateFormatIncDN = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS DDD", DateUtils.english_date_format_symbols);
        dataDateFormatIncDN.setTimeZone(DateUtils.gmtTimeZone);
    }
        
}
