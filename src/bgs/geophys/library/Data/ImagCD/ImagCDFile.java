/*
 * ImagBinaryCDFile.java
 *
 * Created on 24 October 2003, 20:27
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.File.ClasspathFileInterface;
import java.io.*;
import java.util.*;

import bgs.geophys.library.Misc.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A class to hold a single INTERMAGNET CD binary data file
 * @author  Administrator
 */
public class ImagCDFile 
{
    /** the size of one days data, in bytes */
    public static final int DAY_SIZE_BYTES  = 23552;

    // private member variables
    private ImagCDDataDay data [];          // array of raw data
    private int month, year;                // the month (0..11) and year for this data
    protected int n_days;                   // the number of days in this month
    private File open_file;                 // the file data was last read/written to/from
    private boolean file_changed;           // the user has changed but not saved
    private boolean file_zipped;            // the file read was zipped

    // support for a variable that holds the day currently displayed to the user 
    // (1 .. data_file.n_days) this variable does not need to be used - it is
    // a convenience for those users who want it
    private int day_displayed;
    
    /** Creates a new instance of ImagBinaryCDFile */
    public ImagCDFile() 
    {
        int count;
        
        data = new ImagCDDataDay [31];
        for (count=0; count<data.length; count++) data[count] = new ImagCDDataDay ();
        month = year = 0;
        n_days = 31;
        open_file = null;
        file_changed = file_zipped = false;
        day_displayed = 1;
    }

    /** Creates a new instance of ImagBinaryCDFile, setting the file date
     * @param year the year for the data
     * @param month the month for the data (0..11) */
    public ImagCDFile (int year, int month) 
    {
        int count, day_number;
        
        n_days = DateUtils.daysInMonth(month, year);
        data = new ImagCDDataDay [n_days];
        day_number = 0;
        for (count=0; count<month; count++)
            day_number += DateUtils.daysInMonth(count, year);
        for (count=0; count<data.length; count++) 
            data[count] = new ImagCDDataDay (year, day_number + count +1);
        this.month = month;
        this.year = year;
        open_file = null;
        file_changed = file_zipped = false;
        day_displayed = 1;
    }

    /** fill this data object with blank data */
    public void emptyData ()
    {
        int count;
        
        for (count=0; count<data.length; count++) data[count].emptyData ();
        month = year = 0;
        open_file = null;
        file_changed = file_zipped = false;
        day_displayed = 1;
    }
    
    /** load this data object from a file - automatically copes
     * with zipped data
     * @param file the file containing IMAG binary data
     * @return an error message OR null if the data was read OK */
    public String loadFromFile (File file)
    {
        String errmsg;
        
        
        // clear all fields
        emptyData ();
        n_days = 31;
        open_file = file;
 
        // attempt to load plain data first, then zipped data
        errmsg = loadFile(file, false);
        if (errmsg != null) 
        {
            errmsg = loadFile(file, true);
            if (errmsg != null) file_zipped = true;
        }
        return errmsg;
    }
    
    /** load this data object from a classpath file - automatically copes
     * with zipped data
     * @param file the classpath file containing IMAG binary data
     * @return an error message OR null if the data was read OK */
    public String loadFromFile (ClasspathFileInterface file)
    {
        String errmsg;
        
        
        // clear all fields
        emptyData ();
        n_days = 31;
        open_file = null;
 
        // attempt to load plain data first, then zipped data
        errmsg = loadFile(file, false);
        if (errmsg != null) 
        {
            errmsg = loadFile(file, true);
            if (errmsg != null) file_zipped = true;
        }
        return errmsg;
    }
    
    /** write this data object to a file
     * @param file the file to write to
     * @return an error message OR null if the data was written OK */
    public String writeToFile (File file)
    {
        return writeToFile (file, false);
    }
    
    /** write this data object to a file
     * @param file the file to write to
     * @return an error message OR null if the data was written OK */
    public String writeToFile (File file, boolean file_zipped)
    {
        String string;
        File old_open_file;
        boolean old_file_zipped;
        
        old_open_file = open_file;
        old_file_zipped = this.file_zipped;
        open_file = file;
        this.file_zipped = file_zipped;
        string = writeToFile ();
        if (string != null)
        {
            open_file = old_open_file;
            this.file_zipped = old_file_zipped;
        }
        return string;
    }
    
    /** write this data object to a file
     * @return an error message OR null if the data was written OK */
    public String writeToFile ()
    {
        String msg;
        
        // check that the data needs to be written
        if (! file_changed) return null;
        
        // check a file is associated with the data
        if (open_file == null) return "Data does not have a file name";
        
        // write the file
        msg = writeFile (file_zipped);
        return msg;
    }
    
    /** flag that the file has been changed */
    public void setFileChanged () { file_changed = true; }

    /** get the requested day of data
     * @param day - the day to get (1-getNDays())
     * @return the data day */
    public ImagCDDataDay getDataDay (int day) { return data [day-1]; }
    
    /** get the month for this data file
     * @return the month (0..11) */
    public int getMonth () { return month; }
    
    /** get the year for this data file
     * @return the year */
    public int getYear () { return year; }

    /** get the number of days in this data file
     * @return the number of days */
    public int getNDays () { return n_days; }
    
    /** get the start date of the data in this file
     * @return the start date
     */
    public Date getStartDate () {
        GregorianCalendar calendar = new GregorianCalendar (TimeZone.getTimeZone("GMT"));
        calendar.set (Calendar.MILLISECOND, 0);
        calendar.set (Calendar.SECOND, 0);
        calendar.set (Calendar.MINUTE, 0);
        calendar.set (Calendar.HOUR_OF_DAY, 0);
        calendar.set (Calendar.DAY_OF_MONTH, 1);
        calendar.set (Calendar.MONTH, this.getMonth());
        calendar.set (Calendar.YEAR, this.getYear());
        return calendar.getTime ();
    }
    
    /** get the end date of the data in this file
     *  this is not the date of the last data record,
     *  rather this the data start date + 1 month
     * @return the end date (start date + 1 month)
     */
    public Date getEndDate () {
        GregorianCalendar calendar = new GregorianCalendar (TimeZone.getTimeZone("GMT"));
        calendar.setTime(getStartDate());
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }
    
    /** get the last file read/written
     * @return the file - null for no current file */
    public File getOpenFile () { return open_file; }

    /** find out if the file has been altered since the last load/save
     * @return true if the file has changed */
    public boolean getFileChanged () { return file_changed; }
    
    /* find out whether the last file read was zipped or not */
    public boolean isFileZipped () { return file_zipped; }
    
    /** utility routine to set the station code for each day in the file 
     * @param station_id the station code */
    public void setStationId (String station_id)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setStationID(station_id);
    }
    
    /** utility routine to set the colatitude for each day in the file 
     * @param colatitude the colatitude */
    public void setColatitude (int colatitude)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setColatitude(colatitude);
    }
    
    /** utility routine to set the longitude for each day in the file 
     * @param longitude the longitude */
    public void setLongitude (int longitude)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setLongitude(longitude);
    }
    
    /** utility routine to set the elevation for each day in the file 
     * @param elevation the elevation */
    public void setElevation (int elevation)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setElevation(elevation);
    }
    
    /** utility routine to set the component code for each day in the file 
     * @param comps the component code */
    public void setRecordedElements (String comps)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setRecordedElements(comps);
    }
    /** utility routine to get the component code for the first day in the file 
     * @param comps the component code */
    public String getRecordedElements ()
    {
       return data [0].getRecordedElements();
    }
    
    /** utility routine to set the code for the institute for each day in the file 
     * @param inst the institute code */
    public void setInstituteCode (String inst)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setInstituteCode(inst);
    }
    
    /** utility routine to set the D conversion factor for each day in the file 
     * @param dconv the D conversion factor */
    public void setDConversion (int dconv)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setDConversion(dconv);
    }
    
    /** utility routine to set the code for the quality for each day in the file 
     * @param qual the quality code */
    public void setQualityCode (String qual)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setQualityCode(qual);
    }
    
    /** utility routine to set the code for the instrument for each day in the file 
     * @param instr the instrument code */
    public void setInstrumentCode (String instr)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setInstrumentCode(instr);
    }
    
    /** utility routine to set the K9 value for each day in the file 
     * @param k9 the K9 value in nT */
    public void setK9Limit (int k9)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setK9Limit(k9);
    }
    
    /** utility routine to set the original instrument sample period for each day in the file 
     * @param sp the sample period, in mS */
    public void setSamplePeriod (int sp)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setSamplePeriod(sp);
    }
    
    /** utility routine to set the code for the instrument orientation for each day in the file 
     * @param code the instrument orientation code */
    public void setSensorOrientation (String code)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setSensorOrientation(code);
    }

    /** utility routine to set the version for each day in the file 
     * @param pub_date the version number as a year and month - other parts of the date (.e.g hour) may be set, but will be ignored */
    public void setPublicationDate (Date pub_date)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setPublicationDate(pub_date);
    }
    
    /** utility routine to set the format version for each day in the file 
     * @param format_version the version number as a string, e.g. "1.0", "1.1", "2.0" */
    public void setFormatVersion (String format_version)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setFormatVersion (format_version);
    }
    
    /** utility routine to set a header byte for each day in the file 
     * @param value the vale to set the byte to
     * @param index the header byte to set (0 to 63) */
    public void setHeaderByte(byte value, int index)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setHeaderByte(value, index);
    }
    
    /** utility routine to set a trailer byte for each day in the file 
     * @param value the vale to set the byte to
     * @param index the trailer byte to set (0 to 15) */
    public void setTrailerByte(byte value, int index)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].setTrailerByte(value, index);
    }

    /** get the day displayed
      * @return the day displayed */
    public int getDayDisplayed () { return day_displayed; }

    /** increment the day displayed
      * @return the new day displayed */
    public int incDayDisplayed () 
    { 
      day_displayed ++; 
      if (day_displayed > n_days) day_displayed = n_days;
      return day_displayed;
    }

    /** decrement the day displayed
      * @return the new day displayed */
    public int decDayDisplayed () 
    { 
      day_displayed --; 
      if (day_displayed < 1) day_displayed = 1;
      return day_displayed;
    }
    
    /** set the day displayed to the first day 
     * @return the new day displayed */
    public int setDayDisplayedToFirst ()
    {
        day_displayed = 1;
        return day_displayed;
    }

    /** set the day displayed to the last possible day 
     * @return the new day displayed */
    public int setDayDisplayedToLast ()
    {
        day_displayed = n_days;
        return day_displayed;
    }

    /** calculate hourly mean values from minute means
     * @param overwrite_existing set true to overwrite existing values
     *        set false to only fill in gaps (where possible)
     * @param max_missing_pc maximum missing samples (as a percentage)
     *        beyond which mean will be set missing */
    public void calculateHourlyMeans (boolean overwrite_existing, int max_missing_pc)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].calculateHourlyMeans(overwrite_existing, max_missing_pc);
    }
    
    /** calculate daily mean values from minute means
     * @param overwrite_existing set true to overwrite existing values
     *        set false to only fill in gaps (where possible)
     * @param max_missing_pc maximum missing samples (as a percentage)
     *        beyond which mean will be set missing */
    public void calculateDailyMeans (boolean overwrite_existing, int max_missing_pc)
    {
        int day;
        for (day=0; day<n_days; day++) data [day].calculateDailyMeans(overwrite_existing, max_missing_pc);
    }
    
    
    ////////////////////////////////////////////////////////////////////
    // private code below here
    ////////////////////////////////////////////////////////////////////
    private String loadFile (File file, boolean is_zipped)
    {
        String error_message;
        InputStream input_stream;
        FileInputStream file_stream;
        ZipInputStream zip_stream;
        ZipEntry zip_entry;

        file_stream = null;
        zip_stream = null;
        error_message = null;
        
        // open the file
        try
        {
            input_stream = file_stream = new FileInputStream (file);
            if (is_zipped)
            {
                input_stream = zip_stream = new ZipInputStream (file_stream);
                zip_entry = zip_stream.getNextEntry ();
            }
            
            // load the data
            loadStream(input_stream);
            
        }
        catch (FileNotFoundException e)
        {
            error_message = "Could not open file " + file.toString();
        }
        catch (IOException e)
        {
            error_message = e.getMessage();
            if (error_message == null) error_message = "IO error with file " + file.toString ();
        }
        catch (ImagCDDataException e)
        {
            error_message = e.getMessage();
        }
        
        // close the file
        try
        {
            if (zip_stream != null) zip_stream.close();
            if (file_stream != null) file_stream.close();
        }
        catch (IOException e) { }
        
        return error_message;
    }
    
    private String loadFile (ClasspathFileInterface cp_file, boolean is_zipped)
    {
        String error_message;
        InputStream input_stream;
        ZipInputStream zip_stream;
        ZipEntry zip_entry;

        zip_stream = null;
        error_message = null;
        
        // open the file and load the data
        try
        {
            input_stream = cp_file.openInputStream ();
            if (is_zipped)
            {
                zip_stream = new ZipInputStream (input_stream);
                zip_entry = zip_stream.getNextEntry ();
                loadStream (zip_stream);
            }
            else
                loadStream(input_stream);
        }
        catch (FileNotFoundException e)
        {
            error_message = "Could not open file " + cp_file.getAbsolutePath ();
        }
        catch (IOException e)
        {
            error_message = e.getMessage();
            if (error_message == null) error_message = "IO error with file " + cp_file.getAbsolutePath ();
        }
        catch (ImagCDDataException e)
        {
            error_message = e.getMessage();
        }
        
        // close the file
        try
        {
            if (zip_stream != null) zip_stream.close();
        }
        catch (IOException e) { }
        
        return error_message;
    }
    
    private void loadStream (InputStream input_stream) 
    throws ImagCDDataException
    {
        GregorianCalendar calendar;
        int day, dayno;
        
        // read the first day of data and check that it is the first day of the month
        data [0].loadFromFile (input_stream);
        calendar = new GregorianCalendar (TimeZone.getTimeZone("GMT"));
        try
        {
            calendar.set (GregorianCalendar.HOUR_OF_DAY, 12);
            calendar.set (GregorianCalendar.MINUTE, 0);
            calendar.set (GregorianCalendar.SECOND, 0);
            calendar.set (GregorianCalendar.YEAR, data [0].getYear());
            dayno = data [0].getDayNumber();
            calendar.set (GregorianCalendar.DAY_OF_YEAR, dayno);
            year = calendar.get (GregorianCalendar.YEAR);
            month = calendar.get (GregorianCalendar.MONTH);
            day = calendar.get (GregorianCalendar.DAY_OF_MONTH);
            if (day != 1) throw new ImagCDDataException ("First day in file must be first day of month, not day " + Integer.toString (day));
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new ImagCDDataException ("Error with day number or year in first day of data");
        }

        // read the rest of the days for this month
        for (day=2; day<=31; day++)
        {
            dayno ++;

            // update the calendar to check that this day is required
            try
            {
                calendar.set (GregorianCalendar.DAY_OF_YEAR, dayno);
                if (calendar.get (GregorianCalendar.MONTH) != month) n_days = day -1;
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                n_days = day -1;
            }
            if (n_days < 31) break;

            // get the data for this day
            data [day-1].loadFromFile (input_stream);
            if (data [day-1].getYear() != year) throw new ImagCDDataException ("Data for day " + Integer.toString (day) + " is for a different year to the previous days");
            if (data [day-1].getDayNumber() != dayno) throw new ImagCDDataException ("Data for day " + Integer.toString (day) + " has the wrong day number (" + Integer.toString (data [day-1].getDayNumber()) + " should be " + Integer.toString (dayno) + ")");
        }
    }

    private String writeFile (boolean is_zipped)
    {
        int day, dayno;
        String msg;
        OutputStream output_stream;
        FileOutputStream file_stream;
        ZipOutputStream zip_stream;
        ZipEntry zip_entry;

        // open the file
        file_stream = null;
        zip_stream = null;
        msg = null;
        try
        {
            output_stream = file_stream = new FileOutputStream (open_file);
            if (is_zipped)
            {
                output_stream = zip_stream = new ZipOutputStream (new BufferedOutputStream (file_stream));    
                zip_entry = new ZipEntry (open_file.getName());
                zip_stream.putNextEntry (zip_entry);
            }
                        
            // write each of the days for this month
            for (day=1; day<=n_days; day++) data [day-1].writeToFile (output_stream);
            
            // mark that the file is not changed
            file_changed = false;
        }
        catch (FileNotFoundException e)
        {
            msg = "Could not open file " + open_file.toString();
        }
        catch (IOException e)
        {
            msg = e.getMessage();
            if (msg == null) msg = "IO error with file " + open_file.toString ();
        }
        catch (ImagCDDataException e)
        {
            msg = e.getMessage();
        }

        // close the file
        try
        {
            if (zip_stream != null) zip_stream.close();
            if (file_stream != null) file_stream.close();
        }
        catch (IOException e) { }
        
        return msg;
    }

}

