/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.File.FileUtils;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Misc.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A class for editing the header and trailer sections
 * in INTERMAGNET CD files. This class is more efficient (if you
 * just want to access the header/trailer) than reading/writing
 * the whole file.
 * 
 * This class can read zip and plain files, but can only write plain files.
 * 
 * @author smf
 */
public class ImagCDHeader 
{

    // the header fields
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
    private String publication_date;    // version of the data as a date in the form YYMM
    private String format_version;      // version of the data format
    private int reserved_1;
    
    // the trailer fields
    private int trailer_1;
    private int trailer_2;
    private int trailer_3;
    private int trailer_4;
    
    // other members
    private File imag_cd_file;          // the file to read / write from
    private boolean is_zip;             // true for a ZIP file
    private SimpleDateFormat pub_date_date_format;
    
    /** read the header from a file - the first header only is read
     * @param file the file to read
     * @param stop_at_ctrl when interpreting string fields, if there
     *        is a control character stop the coversion (true) or
     *        insert a '.' (false)
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws bgs.geophys.library.Data.ImagCD.ImagCDDataException
     */
    public ImagCDHeader (File file, boolean stop_at_ctrl)
    throws FileNotFoundException, IOException, ImagCDDataException
    {
        int count;
        long skip_req, skip_len;
        String string;
        byte header [], trailer [];
        InputStream input_stream;
        FileInputStream file_stream;
        ZipInputStream zip_stream;
        ZipEntry zip_entry;
        
        header = new byte [64];
        trailer = new byte [16];
        pub_date_date_format = new SimpleDateFormat ("yyMM");
        pub_date_date_format.setTimeZone (DateUtils.gmtTimeZone);
        pub_date_date_format.setLenient(false);
        this.imag_cd_file = file;
        
        // check for a zip file
        is_zip = false;
        string = file.getName();
        if (string.length() >= 3)
        {
            string = string.substring(string.length() -3);
            if (string.equalsIgnoreCase("zip")) is_zip = true;
        }
        
        // open the file
        file_stream = null;
        zip_stream = null;
        input_stream = file_stream = new FileInputStream (file);
        if (is_zip)
        {
            input_stream = zip_stream = new ZipInputStream (file_stream);
            zip_entry = zip_stream.getNextEntry ();
        }
        
        // read the first header
        FileUtils.readFully (input_stream, header);

        // move to the trailer
        skip_req = ImagCDFile.DAY_SIZE_BYTES - 80;
        skip_len = input_stream.skip (skip_req);
        if (skip_req != skip_len) throw new ImagCDDataException ("File does not contain enough data");
                
        // read the first trailer
        FileUtils.readFully (input_stream, trailer);
        
        // close the file
        try
        {
            if (zip_stream != null) zip_stream.close();
            if (file_stream != null) file_stream.close();
        }
        catch (IOException e) { }
        
        // interpret the header and trailer
        station_id = bytes2String        (header, 0, 4, stop_at_ctrl);
        count = Utils.bytesToInt         (header, 4);
        year = count / 1000;
        day_number = count % 1000;
        colatitude = Utils.bytesToInt    (header, 8);
        longitude = Utils.bytesToInt     (header, 12);
        elevation = Utils.bytesToInt     (header, 16);
        recorded_elements = bytes2String (header, 20, 4, stop_at_ctrl);
        institute_code = bytes2String    (header, 24, 4, stop_at_ctrl);
        d_conversion = Utils.bytesToInt  (header, 28);
        quality_code = bytes2String      (header, 32, 4, stop_at_ctrl);
        instrument_code = bytes2String   (header, 36, 4, stop_at_ctrl);
        k9_limit = Utils.bytesToInt      (header, 40);
        sample_period = Utils.bytesToInt (header, 44);
        sensor_orientation = bytes2String(header, 48, 4, stop_at_ctrl);
        publication_date = bytes2String  (header, 52, 4, stop_at_ctrl);
        switch (header [56])
        {
            case 0: format_version = "1.0"; break;
            case 1: format_version = "1.1"; break;
            case 2: format_version = "2.0"; break;
            case 3: format_version = "2.1"; break;
            default: format_version = "Unknown"; break;
        }
        reserved_1 = Utils.bytesToInt    (header, 60);
        
        trailer_1 = Utils.bytesToInt    (trailer, 0);
        trailer_2 = Utils.bytesToInt    (trailer, 4);
        trailer_3 = Utils.bytesToInt    (trailer, 8);
        trailer_4 = Utils.bytesToInt    (trailer, 12);
    }
    
    public ImagCDHeader (ImagCDHeader orig)
    {
        pub_date_date_format = new SimpleDateFormat ("yyMM");
        pub_date_date_format.setTimeZone (DateUtils.gmtTimeZone);
        pub_date_date_format.setLenient(false);
        
        this.station_id = new String (orig.station_id);
        this.year = orig.year;
        this.day_number = orig.day_number;
        this.colatitude = orig.colatitude;
        this.longitude = orig.longitude;
        this.elevation = orig.elevation;
        this.recorded_elements = new String (orig.recorded_elements);
        this.institute_code = new String (orig.recorded_elements);
        this.d_conversion = orig.d_conversion;
        this.quality_code = new String (orig.quality_code);
        this.instrument_code = new String (orig.instrument_code);
        this.k9_limit = orig.k9_limit;
        this.sample_period = orig.sample_period;
        this.sensor_orientation = new String (orig.sensor_orientation);
        this.publication_date = new String (orig.publication_date);
        this.format_version = new String (orig.format_version);
        this.reserved_1 = orig.reserved_1;
        this.trailer_1 = orig.trailer_1;
        this.trailer_2 = orig.trailer_2;
        this.trailer_3 = orig.trailer_3;
        this.trailer_4 = orig.trailer_4;
        this.imag_cd_file = new File (orig.imag_cd_file.getAbsolutePath());
    }
    
    /** write the header to a file - all header fields are written,
     * including the year/day number - the day number is incremented
     * from its's starting value for each day in the file */
    public void write ()
    throws FileNotFoundException, IOException
    {
        int n_days, day_count;
        byte header [], trailer [];
        RandomAccessFile file_ptr;
        GregorianCalendar cal;
        

        if (is_zip) throw new FileNotFoundException ("Writing zip files not supported");
        
        header = new byte [64];
        trailer = new byte [16];
        
        // find the number of days in the file
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        cal.set (GregorianCalendar.YEAR, year);
        cal.set (GregorianCalendar.DAY_OF_YEAR, day_number);
        n_days = DateUtils.daysInMonth(cal.get (GregorianCalendar.MONTH), year);
        
        // convert the static parts of the header and trailer
        insertInto (header, 0,  station_id);
        insertInto (header, 8,  Utils.intToBytes (colatitude));
        insertInto (header, 12, Utils.intToBytes (longitude));
        insertInto (header, 16, Utils.intToBytes (elevation));
        insertInto (header, 20, recorded_elements);
        insertInto (header, 24, institute_code);
        insertInto (header, 28, Utils.intToBytes (d_conversion));
        insertInto (header, 32, quality_code);
        insertInto (header, 36, instrument_code);
        insertInto (header, 40, Utils.intToBytes (k9_limit));
        insertInto (header, 44, Utils.intToBytes (sample_period));
        insertInto (header, 48, sensor_orientation);
        insertInto (header, 52, publication_date);
        if (format_version.equals("1.0")) header [56] = 0;
        else if (format_version.equals("1.1")) header [56] = 1;
        else if (format_version.equals("2.0")) header [56] = 2;
        else header [56] = 0;
        header [57] = header [58] = header [59] = 0;
        insertInto (header, 60, Utils.intToBytes (reserved_1));

        insertInto (trailer, 0,  Utils.intToBytes (trailer_1));
        insertInto (trailer, 4,  Utils.intToBytes (trailer_2));
        insertInto (trailer, 8,  Utils.intToBytes (trailer_3));
        insertInto (trailer, 12, Utils.intToBytes (trailer_4));

        // open the file
        file_ptr = new RandomAccessFile (imag_cd_file, "rw");
        
        // for each day ...
        for (day_count=0; day_count<n_days; day_count++)
        {
            // convert the year and day number
            insertInto (header, 4, Utils.intToBytes ((year * 1000) + day_number + day_count));
        
            // skip to and write the header
            file_ptr.seek (ImagCDFile.DAY_SIZE_BYTES * day_count);
            file_ptr.write (header);
            
            // skip to and write the trailer
            file_ptr.seek ((ImagCDFile.DAY_SIZE_BYTES * (day_count +1)) - 16);
            file_ptr.write (trailer);
        }        
        
        // close the file
        file_ptr.close();
    }

    public String getStationID() { return station_id; }
    public void setStationID(String station_id) 
    throws ImagCDDataException
    {
        if (station_id.length() < 3) throw new ImagCDDataException ("Station ID must be at least 3 characters long");
        if (station_id.length() > 4) throw new ImagCDDataException ("Station ID must be less than 5 characters long");
        this.station_id = station_id; 
    }
    public int getYear() { return year; }
    public void setYear(String year) 
    throws ImagCDDataException
    {
        try { setYear (Integer.parseInt(year)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Year must be a number"); }
    }
    public void setYear(int year) { this.year = year; }
    public int getDayNumber() { return day_number; }
    public void setDaynumber(String day_number) 
    throws ImagCDDataException
    {
        try { setDaynumber (Integer.parseInt(day_number)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Day number must be a number"); }
    }
    public void setDaynumber(int day_number) 
    throws ImagCDDataException
    { 
        if (day_number < 1 || day_number > 366) throw new ImagCDDataException ("Day number must be between 1 and 366");
        this.day_number = day_number; 
    }
    public int getColatitude() { return colatitude; }
    public void setColatitude (String colatitude)
    throws ImagCDDataException
    {
        try { setColatitude(Double.parseDouble(colatitude)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Colatitude must be a number"); }
    }
    public void setColatitude (double colatitude) 
    throws ImagCDDataException
    {
        setColatitude ((int) (colatitude * 1000.0)); 
    }
    public void setColatitude(int colatitude) 
    throws ImagCDDataException
    {
        if (colatitude != ImagCDDataDay.MISSING_HEADER_FIELD)
        {
            if (colatitude < 0 || colatitude > 180000) throw new ImagCDDataException ("Colatitude must be between 0 and 180 degrees");
        }
        this.colatitude = colatitude; 
    }
    public int getLongitude() { return longitude; }
    public void setLongitude (String longitude)
    throws ImagCDDataException
    {
        try { setLongitude(Double.parseDouble(longitude)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Longitude must be a number"); }
    }
    public void setLongitude (double longitude) 
    throws ImagCDDataException
    {
        setLongitude ((int) (longitude * 1000.0)); 
    }
    public void setLongitude(int longitude) 
    throws ImagCDDataException
    {
        if (longitude != ImagCDDataDay.MISSING_HEADER_FIELD)
        {
            if (longitude < -180000 || longitude > 360000) throw new ImagCDDataException ("Longitude must be between -180 and 360 degrees");
        }
        this.longitude = longitude; 
    }
    public int getElevation() { return elevation; }
    public void setElevation (String elevation)
    throws ImagCDDataException
    {
        try { setElevation(Integer.parseInt(elevation)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Elevation must be a number"); }
    }
    public void setElevation(int elevation) 
    throws ImagCDDataException
    {
        if (elevation != ImagCDDataDay.MISSING_HEADER_FIELD)
        {
            if (elevation < -1000 || elevation > 36000) throw new ImagCDDataException ("Elevation must be between -1000 and 36000 metres");
        }
        this.elevation = elevation; 
    }
    public String getRecordedElements() { return recorded_elements; }
    public void setRecordedElements(String recorded_elements) 
    throws ImagCDDataException
    {
        if (! recorded_elements.equalsIgnoreCase("HDZF") &&
            ! recorded_elements.equalsIgnoreCase("XYZF") &&
            ! recorded_elements.equalsIgnoreCase("DIFF") &&
            ! recorded_elements.equalsIgnoreCase("HDZG") &&
            ! recorded_elements.equalsIgnoreCase("XYZG"))
            throw new ImagCDDataException ("Recorded elements must be one of 'XYZF','HDZF','XYZG' or 'HDZG'");
        this.recorded_elements = recorded_elements; 
    }
    public String getInstituteCode() { return institute_code; }
    public void setInstituteCode(String institute_code) 
    throws ImagCDDataException
    { 
        if (institute_code.length() > 4) throw new ImagCDDataException ("Institude code cannot be longer than 4 characters");
        this.institute_code = institute_code; 
    }
    public int getDConversion() { return d_conversion; }
    public void setDConversion (String d_conversion)
    throws ImagCDDataException
    {
        try { setDConversion(Integer.parseInt (d_conversion)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("D conversion value must be a number"); }
        
    }
    public void setDConversion(int d_conversion) 
    throws ImagCDDataException
    {
        this.d_conversion = d_conversion; 
    }
    public String getQualityCode() { return quality_code; }
    public void setQualityCode(String quality_code) 
    throws ImagCDDataException
    {
        if (quality_code.length() > 4) throw new ImagCDDataException ("Quality code cannot be longer that 4 characters");
        this.quality_code = quality_code; 
    }
    public String getInstrumentCode() { return instrument_code; }
    public void setInstrumentCode(String instrument_code) 
    throws ImagCDDataException
    {
        if (instrument_code.length() > 4) throw new ImagCDDataException ("Instrument code cannot be longer that 4 characters");
        this.instrument_code = instrument_code; 
    }
    public int getK9Limit() { return k9_limit; }
    public void setK9Limit (String k9_limit)
    throws ImagCDDataException
    {
        try { setK9Limit(Integer.parseInt (k9_limit)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("K9 limit must be a number"); }
    }
    public void setK9Limit(int k9_limit) 
    throws ImagCDDataException
    {
        if (k9_limit != ImagCDDataDay.MISSING_HEADER_FIELD)
        {
            if (k9_limit < 0 || elevation > 80000) throw new ImagCDDataException ("K9 limit must be between 0 and 80000");
        }
        this.k9_limit = k9_limit; 
    }
    public int getSamplePeriod() { return sample_period; }
    public void setSamplePeriod(String sample_period) 
    throws ImagCDDataException
    {
        try { setSamplePeriod(Integer.parseInt (sample_period)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Sample period must be a number"); }
    }
    public void setSamplePeriod(int sample_period) 
    throws ImagCDDataException
    {
        if (sample_period != ImagCDDataDay.MISSING_HEADER_FIELD)
        {
            if (sample_period < 0 || sample_period > 60000) throw new ImagCDDataException ("Sample period must be between 0 and 60000");
        }
        this.sample_period = sample_period; 
    }
    public String getSensorOrientation() { return sensor_orientation; }
    public void setSensorOrientation(String sensor_orientation) 
    throws ImagCDDataException
    {
        if (sensor_orientation.length() > 4) throw new ImagCDDataException ("Sensor orientation code code cannot be longer that 4 characters");
        this.sensor_orientation = sensor_orientation; 
    }
    public String getPublicationDate() { return publication_date; }
    public void setPublicationDate(String publication_date) 
    throws ImagCDDataException
    {
        if (publication_date.length() > 4) throw new ImagCDDataException ("Publication date must be a number in the form YYMM (or be left blank)");
        if (publication_date.length() > 0)
        {
            try { pub_date_date_format.parse(publication_date); }
            catch (ParseException e) { throw new ImagCDDataException ("Publication date must be a number in the form YYMM (or leave blank)"); }
        }
        this.publication_date = publication_date; 
    }
    public String getFormatVersion () { return format_version; }
    public void setFormatVersion (String version)
    throws ImagCDDataException
    {
        if (version.equals("1.0")) format_version = version;
        else if (version.equals("1.1")) format_version = version;
        else if (version.equals("2.0")) format_version = version;
        else throw new ImagCDDataException ("Incorrect format version - should be 1.0, 1.1 or 2.0");
    }
    public int getReserved() { return reserved_1; }
    public void setReserved(String reserved_1)
    throws ImagCDDataException
    {
        try { setReserved (Integer.parseInt (reserved_1)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Header reserved word 1 must be a number"); }
    }
    public void setReserved(int reserved_1) { this.reserved_1 = reserved_1; }
    public int getTrailer1() { return trailer_1; }
    public void setTrailer1(String trailer_1)
    throws ImagCDDataException
    {
        try { setTrailer1 (Integer.parseInt (trailer_1)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Trailer reserved word 1 must be a number"); }
    }
    public void setTrailer1(int trailer_1) { this.trailer_1 = trailer_1; }
    public int getTrailer2() { return trailer_2; }
    public void setTrailer2(String trailer_2)
    throws ImagCDDataException
    {
        try { setTrailer2 (Integer.parseInt (trailer_2)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Trailer reserved word 2 must be a number"); }
    }
    public void setTrailer2(int trailer_2) { this.trailer_2 = trailer_2; }
    public int getTrailer3() { return trailer_3; }
    public void setTrailer3(String trailer_3)
    throws ImagCDDataException
    {
        try { setTrailer3 (Integer.parseInt (trailer_3)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Trailer reserved word 3 must be a number"); }
    }
    public void setTrailer3(int trailer_3) { this.trailer_3 = trailer_3; }
    public int getTrailer4() { return trailer_4; }
    public void setTrailer4(String trailer_4)
    throws ImagCDDataException
    {
        try { setTrailer4 (Integer.parseInt (trailer_4)); }
        catch (NumberFormatException e) { throw new ImagCDDataException ("Trailer reserved word 4 must be a number"); }
    }
    public void setTrailer4(int trailer_4) { this.trailer_4 = trailer_4; }
    public File getFile() { return imag_cd_file; }
    
    /** convert a sequence of bytes into a string - only converts that basic
     *  ASCII set between 32 (' ') and 127 ('~')
     * @param bytes - the bytes to convert
     * @param offset - the starting point to begin conversion in the byte array
     * @param length - the number of bytes to convert
     * @param stop_at_ctrl - if TRUE stop the conversion at non-printing chars
     *                       otherwise convert unprinting chars using '.'
     * @returns the string with leading / trailing whitespace trimmed */
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
        return string.trim();
    }
    
    /** insert a string into a header or trailer
     * @param buffer the header or trailer
     * @param offset the place to insert the string
     * @param string the string to insert */
    private void insertInto (byte buffer [], int offset, String string)
    {
        switch (string.length())
        {
            case 0: string = "    "; break;
            case 1: string = "   " + string; break;
            case 2: string = "  " + string; break;
            case 3: string = " " + string; break;
            case 4: break;
            default: string = string.substring(string.length()-4, string.length()); break;
        }
        insertInto (buffer, offset, string.getBytes());
    }
    
    /** insert a string into a header or trailer
     * @param buffer the header or trailer
     * @param offset the place to insert the string
     * @param bytes the bytes to insert */
    private void insertInto (byte buffer [], int offset, byte bytes [])
    {
        int count;
        
        for (count=0; count<4; count++)
            buffer [offset + count] = bytes [count];
    }
    
}
