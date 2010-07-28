
/*
 * CDDataMonth.java
 *
 * Created on 11 February 2003, 11:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import bgs.geophys.library.Misc.DateUtils;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.Utils;

 /********************************************************************
  * Class to hold contents of data file. Holds data as an array of
  * CDDataDay objects.
  *
  * @author  fmc
  ********************************************************************/

public class CDDataMonth
{
  // private variables
  private boolean data_loaded;
  private String error_message;
  private int expected_n_days, n_days;
  
  private CDObservatory observatory; // observatory for this month of data
  private int month_index;           // index to this file in CDObservatory
                                     // month files list
  private boolean zipped;            // true if file is in zipped format
  private int f_scalar_type;         // Expected type of F scalar data
  private File data_file;
  private CDDataDay day_data [];
  private GeomagAbsoluteValue daily_means [];
  private String comp_orientation;
  
  // private constants
  private static final int MAX_N_DAYS = 31;
  private static final int DAY_DATA_LENGTH = 23552;
  private static final int ZIP_BUFFER_LENGTH = 512;
  private static final int WORD_LENGTH = 4;

 /********************************************************************
  * creates a new CDDataMonth object
  * @param month_index - the month index of this data object (1..12)
  * @param observatory - the CDObservatory object this data is from
  * @param zipped - true if the data file is zipped, else false
  ********************************************************************/
  public CDDataMonth (int month_index, CDObservatory observatory, boolean zipped)
  {
      
    this.error_message = null;
    this.data_loaded = false;
    this.month_index = month_index;
    this.observatory = observatory;
    this.zipped = zipped;
    this.f_scalar_type = GlobalObjects.getExpectedFType(observatory.GetObservatoryCode(), observatory.GetYear());
    this.day_data = null;
    this.expected_n_days = DateUtils.daysInMonth(month_index -1, observatory.GetYear());
    this.n_days = 0;
    this.data_file = observatory.GetMonthFile (month_index, zipped);
    this.daily_means = null;
    this.comp_orientation = null;
  }

  /** if the data does not load correctly, this method may shed
    * some light on the problem 
    * @return description of the last load error, or null if the last
    *         attempt to load data was successful, or null if data
    *         has not yet ben loaded */
  public String getLoadErrmsg () { return error_message; }
  
 /********************************************************************
  * getMonthData - get a month of data. Reads the month file if it has
  *                not previously been read.
  * @return an array or CDDataDay objects or null if file not read
  *         correctly
  ********************************************************************/
  public CDDataDay [] getMonthData ()
  {
    if (!data_loaded)
    {
      if (zipped)
        data_loaded = readZippedMonthFile();
      else
        data_loaded = readMonthFile ();
    }

    return day_data;
  }

 /********************************************************************
  * getDayData - get one day of data. Reads the month file if it has
  *              not previously been read.
  * @param day - the day required
  * @return a CDDataDay object or null if not read correctly
  ********************************************************************/
  public CDDataDay getDayData (int day)
  {
    if (!data_loaded)
    {
      if (zipped)
        data_loaded = readZippedMonthFile();
      else
        data_loaded = readMonthFile ();
    }
    if (day_data == null) return null;
    if (day < 0 || day >= n_days) return null;
    return day_data [day];
  }

 /********************************************************************
  * getMonthFilename - get the name of the data file for this data.
  * @return a String containing the filename
  ********************************************************************/
  public String getMonthFilename ()
  {
    return data_file.toString();
  }
  
  public File getMonthFile ()
  {
      return data_file;
  }
  
  public int getFScalarType ()
  {
      return f_scalar_type;
  }
          
 /** this routine returns the daily mean data from each day file -
  * if the data has not already been loaded, the daily means
  * will be read direct from the file, which is more efficient than
  * reading the entire file, then getting cd CDDataDay objects.
  * @return an array of daily means or null if file not read correctly */
  public GeomagAbsoluteValue [] getDailyMeans ()
  {
      int count;
      
      // are the daily means already loaded?
      if (daily_means == null) 
      {
          // allocate space for the daily means
          daily_means = new GeomagAbsoluteValue [expected_n_days];
          
          // is the data already loaded
          if (data_loaded)
          {
              // load the daily means from the day data
              for (count=0; count<expected_n_days; count++)
                  daily_means[count] = day_data[count].getDailyMeans()[0];
          }
          else
          {
              // read the daily means
              if (! zipped)
              {
                  if (! readDailyMeans()) daily_means = null;
              }
              else
              {
                  if (! readZippedDailyMeans ()) daily_means = null;
              }
          }
      }
      
      return daily_means;
  }

  /********************************************************************
   * this routine returns the orientation for the data for this
   * month - if the daily_means have been loaded, the orientation
   * will come here, otherwise this call will prompt the entire
   * data file to be loaded.
   * @return a string containing the 4 character orientation code - will
   *         be null if the data cannot be loaded
   ********************************************************************/
   public String getCompOrientation ()
   {
     if (comp_orientation == null)
     {
       if (!data_loaded)
       {
         if (zipped)
           data_loaded = readZippedMonthFile();
         else
           data_loaded = readMonthFile ();
       }
       if (data_loaded) comp_orientation = day_data[0].getCompOrientation();
     }
     return comp_orientation;
   }
   
 /********************************************************************
  * getMonthIndex - get the index of this month, from 1 to 12
  * @return the month index
  ********************************************************************/
  public int getMonthIndex ()
  {
    return month_index;
  }

 /********************************************************************
  * getObservatoryData - get the CDObservatory object for this data
  * @return the CDObservatory object
  ********************************************************************/
  public CDObservatory getObservatoryData ()
  {
    return observatory;
  }

 /********************************************************************
  * hasDataFile - does a data file exist?
  * @return true if a data file exists, else false
  ********************************************************************/
  public boolean hasDataFile ()
  {
    if (this.data_file == null)
      return false;
    else
      return true;
  }

 /********************************************************************
  * getNDays - get the number of days in this month object
  * @return the number of days or 0 if data does not load correctly
  ********************************************************************/
  public int getNDays ()
  {
    if (!data_loaded)
    {
      if (zipped)
        data_loaded = readZippedMonthFile();
      else
        data_loaded = readMonthFile ();
    }
    return n_days;
  }
  
  
 /*-------------------------------------------------------------------
  *-------------------------------------------------------------------
  * ------------------- private code below here ----------------------
  *-------------------------------------------------------------------
  *-------------------------------------------------------------------*/
  
 /********************************************************************
  * readMonthFile - read data file into an array of CDDataDay objects
  *
  * @return true if read successfully, false otherwise
  ********************************************************************/
  private boolean readMonthFile ()
  {
    FileInputStream file_stream;
    boolean eof;
    byte read_buffer [] = new byte [DAY_DATA_LENGTH];
    int n, status;
    Vector<CDDataDay> day_buffer;

    day_buffer = new Vector<CDDataDay> ();
    error_message = null;
    try
    {
      file_stream = new FileInputStream (data_file);
    }
    catch (FileNotFoundException e)
    {
      error_message = "Could not open file " + data_file.toString();
      return false;
    }

    // read file
    // this loop will go false when end of file reached
    eof = false;
    this.n_days = 0;
    while (! eof)
    {
      try
      {
        status = file_stream.read (read_buffer, 0, DAY_DATA_LENGTH);
        if (status == -1)
        {
          // no more data
          eof = true;
          break;
        }
        else
        {
          // create day data
          try
          {
            day_buffer.add (new CDDataDay (this, month_index, n_days, read_buffer));
          }
          catch (IOException e) 
          { 
            error_message = "internal IO error: " + e.getMessage();
            return false;
          }
          catch (CDException e) 
          { 
            error_message = "format error in data file";
            return false;
          }
          n_days ++;
        }
      }
      catch (IOException e)
      {
        error_message = "error reading file";
        return false;
      }
    }
    
    // check that the number of days read is not too large or small to be valid
    if (n_days != expected_n_days)
    {
      error_message = "invalid number of days " + n_days;
      return false;
    }

    // create day_data array
    day_data = new CDDataDay [n_days];
    for (n = 0; n < n_days; n++) day_data [n] = day_buffer.get (n);
    return true;
  }
  
 /********************************************************************
  * readZippedMonthFile - read a zipped data file into an array of
  *                       CDDataDay objects
  *
  * @return true if read successfully, false otherwise
  ********************************************************************/
  private boolean readZippedMonthFile ()
  {
    ZipInputStream zip_stream;
    ZipEntry zip_entry;
    byte read_buffer [] = new byte [DAY_DATA_LENGTH];
    int n, status, file_length;
    int count = 0, byte_count = 0;
    Vector<CDDataDay> day_buffer;

    // initialise global variables
    day_buffer = new Vector<CDDataDay> ();
    error_message = null;
    n_days = 0;

    try
    {
      this.n_days = 0;
      
      // open stream for reading file
      zip_stream = new ZipInputStream (new FileInputStream (data_file));
      // get the file object from the zip stream
      zip_entry = zip_stream.getNextEntry ();
      // in ZipInputStream reading beyond the EOF gives an error so
      // find file length and don't read beyond it
      if (zip_entry != null)
      {
        file_length = (int) zip_entry.getSize ();
        while (byte_count < file_length)
        {
          // using ZipInputStream data can only be read to a maximum
          // buffer length of 512 so read data in chunks this size
          status = readZip (zip_stream, read_buffer, DAY_DATA_LENGTH);
          byte_count += status;
          
          // copy read data into buffer for this day
          try
          {
            day_buffer.add (new CDDataDay (this, month_index, n_days, read_buffer));
          }
          catch (IOException e) 
          { 
            error_message = "internal IO error: " + e.getMessage();
            return false;
          }
          catch (CDException e) 
          { 
            error_message = "format error in data file";
            return false;
          }
          
          n_days ++;
        }
      }
    }
    catch (IOException e)
    {
      error_message = "error reading file";
      return false;
    }

    // check that the number of days read is not too large or small to be valid
    if (n_days != expected_n_days)
    {
      error_message = "invalid number of days " + n_days;
      return false;
    }

    // create day_data array
    day_data = new CDDataDay [n_days];
    for (n = 0; n < n_days; n++) day_data [n] = day_buffer.get (n);
    return true;
  }

 /********************************************************************
  * readDailyMeans - read daily mean data directly from the data file
  * @return true if read successfully, false otherwise
  ********************************************************************/
  private boolean readDailyMeans ()
  {
    FileInputStream file_stream;
    byte read_buffer [];
    double comps [];
    int day_count, count, status, orient_code, value, fst;

    // initialise
    error_message = null;
    comps = new double [4];
    read_buffer = new byte [WORD_LENGTH *4];

    // open the data file
    try
    {
      file_stream = new FileInputStream (data_file);
    }
    catch (FileNotFoundException e)
    {
      error_message = "Could not open file " + data_file.toString();
      return false;
    }

    try
    {
      // skip to the orientation code
      count = 5 * WORD_LENGTH;
      if ((int) file_stream.skip ((long) count) != count)
      {
        error_message = "Could not manipulate data file " + data_file.toString();
        return false;
      }
        
      // read the orientation code
      status = file_stream.read (read_buffer, 0, WORD_LENGTH);
      if (status != WORD_LENGTH)
      {
        error_message = "Corrupt data file";
        return false;
      }
      comp_orientation = new String (read_buffer, 0, WORD_LENGTH);

      // set up parameters based on orientation code
      if (comp_orientation.equalsIgnoreCase("XYZF"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
        fst = getFScalarType();
      }
      else if (comp_orientation.equalsIgnoreCase("XYZG"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
        fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
      }
      else if (comp_orientation.equalsIgnoreCase("HDZF"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
        fst = getFScalarType();
      }
      else if (comp_orientation.equalsIgnoreCase("HDZG"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
        fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
      }
      else if (comp_orientation.equalsIgnoreCase("DIFF"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
        fst = getFScalarType();
      }
      else if (comp_orientation.equalsIgnoreCase("DIFG"))
      {
        orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
        fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
      }
      else
      {
        error_message = "Bad orientation code: " + comp_orientation;
        return false;
      }

      // for each daily mean ...
      for (day_count=0; day_count<expected_n_days; day_count++)
      {
        // skip to the next daily mean
        if (day_count == 0) count = DAY_DATA_LENGTH - (22 * WORD_LENGTH);
        else count = DAY_DATA_LENGTH - read_buffer.length;
        if ((int) file_stream.skip ((long) count) != count)
        {
          error_message = "Could not manipulate data file " + data_file.toString();
          return false;
        }

        // read the daily mean
        status = file_stream.read (read_buffer, 0, read_buffer.length);
        if (status != read_buffer.length)
        {
          error_message = "Invalid number of days " + day_count;
          return false;
        }
      
        // convert the daily mean
        for (count=0; count<4; count++)
        {
          value = Utils.bytesToInt (read_buffer, 4 * count);
          if (value == CDDataDay.MISSING_DATA_VALUE)
            comps [count] = CDDataDay.MISSING_REAL_DATA;
          else
            comps [count] = (double) value / 10.0;
        }
        daily_means [day_count] = new GeomagAbsoluteValue (comps [0], comps [1], comps [2], comps [3],
                                                           fst, CDDataDay.MISSING_REAL_DATA, orient_code,
                                                           GeomagAbsoluteValue.ANGLE_MINUTES);
      }
    }
    catch (IOException e)
    {
      error_message = "Error reading zip data file";
      return false;
    }

    return true;
  }

 /********************************************************************
  * readZippedDailyMeans - read daily mean data directly from the zipped data file
  * @return true if read successfully, false otherwise
  ********************************************************************/
  private boolean readZippedDailyMeans ()
  {
    byte read_buffer [];
    int day_count, count, status, orient_code, value, fst;
    double comps [];
    ZipInputStream zip_stream;
    ZipEntry zip_entry;
    
    // initialise
    error_message = null;
    comps = new double [4];
    read_buffer = new byte [WORD_LENGTH *4];

    try
    {
      // open stream for reading file
      zip_stream = new ZipInputStream (new FileInputStream (data_file));
      // get the file object from the zip stream
      zip_entry = zip_stream.getNextEntry ();
      // in ZipInputStream reading beyond the EOF gives an error so
      // find file length and don't read beyond it
      if (zip_entry != null)
      {
        // skip to the orientation code
        count = 5 * WORD_LENGTH;
        if ((int) zip_stream.skip ((long) count) != count)
        {
          error_message = "Could not manipulate zip data file " + data_file.toString();
          return false;
        }
        
        // read the orientation code
        status = readZip (zip_stream, read_buffer, WORD_LENGTH);
        if (status != WORD_LENGTH)
        {
          error_message = "Corrupt zip data file";
          return false;
        }
        comp_orientation = new String (read_buffer, 0, WORD_LENGTH);

        // set up parameters based on orientation code
        if (comp_orientation.equalsIgnoreCase("XYZF"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
          fst = getFScalarType();
        }
        else if (comp_orientation.equalsIgnoreCase("XYZG"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
          fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
        }
        else if (comp_orientation.equalsIgnoreCase("HDZF"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
          fst = getFScalarType();
        }
        else if (comp_orientation.equalsIgnoreCase("HDZG"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
          fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
        }
        else if (comp_orientation.equalsIgnoreCase("DIFF"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
          fst = getFScalarType();
        }
        else if (comp_orientation.equalsIgnoreCase("DIFG"))
        {
          orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
          fst = GeomagAbsoluteValue.COMPONENT_F_DIFF;
        }
        else
        {
          error_message = "Bad orientation code: " + comp_orientation;
          return false;
        }

        // for each daily mean ...
        for (day_count=0; day_count<expected_n_days; day_count++)
        {
          // skip to the next daily mean
          if (day_count == 0) count = DAY_DATA_LENGTH - (22 * WORD_LENGTH);
          else count = DAY_DATA_LENGTH - read_buffer.length;
          if ((int) zip_stream.skip ((long) count) != count)
          {
            error_message = "Could not manipulate zip data file " + data_file.toString();
            return false;
          }

          // read the daily mean
          status = readZip (zip_stream, read_buffer, read_buffer.length);
          if (status != read_buffer.length)
          {
            error_message = "Invalid number of days " + day_count;
            return false;
          }
      
          // convert the daily mean
          for (count=0; count<4; count++)
          {
              value = Utils.bytesToInt (read_buffer, 4 * count);
              if (value == CDDataDay.MISSING_DATA_VALUE)
                  comps [count] = CDDataDay.MISSING_REAL_DATA;
              else
                  comps [count] = (double) value / 10.0;
          }
          daily_means [day_count] = new GeomagAbsoluteValue (comps [0], comps [1], comps [2], comps [3],
                                                             fst, CDDataDay.MISSING_REAL_DATA, orient_code,
                                                             GeomagAbsoluteValue.ANGLE_MINUTES);
        }
      }
    }
    catch (IOException e)
    {
      error_message = "Error reading zip data file";
      return false;
    }

    return true;
  }

  // the zip file reads may take more than one attempt to complete
  // ZipStream seems to read in 512 byte chunks (and then stop)
  private int readZip (ZipInputStream zip_stream, byte buffer [], int length)
  throws IOException
  {
      int status, count;

      count = 0;
      do
      {
          status = zip_stream.read (buffer, count, length - count);
          if (status < 0) return -1;
          count += status;
      }
      while (count < length);
      
      return count;
  }  
}
