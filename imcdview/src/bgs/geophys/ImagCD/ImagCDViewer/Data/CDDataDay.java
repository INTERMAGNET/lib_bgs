/*
 * CDDataDay.java
 *
 * Created on 11 February 2003, 11:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.*;
import java.text.*;
import java.util.*;

import bgs.geophys.library.Misc.*;

import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.library.Data.GeomagAbsoluteValue;

/**
 * Class to hold one day of data
 *
 * @author  fmc
 */

public class CDDataDay
{
  // constants
  public final static int N_MINUTE_MEAN_VALUES = 1440;
  public final static int N_HOURLY_MEAN_VALUES = 24;
  public final static int N_DAILY_MEAN_VALUES  = 1;
  public final static int N_K_INDICES = 8;
  public final static int MISSING_K_VALUE = 999;
  public final static int N_COMPONENTS = 4;
  public final static int MISSING_DATA_VALUE = 999999;
  public final static double MISSING_REAL_DATA = 99999.9;

  private String error_message;

  // all strings in file have 4 characters
  private final int INT_BUFFER_LENGTH = 4;

  private CDDataMonth month_data;
  private int month_index;           // index to this file in CDDataMonth
                                     // month files list
  private int day;                   // the day index of this file in CDDataMonth
  private int f_scalar_type;

  // header
  // Station ID code
  private String station_id;
  // start date in format yyyyddd (day of year)
  private int start_date;
  // co-latitude = (90-latitude)*1000
  private int co_latitude;
  // longitude
  private int longitude;
  // elevation in metres
  private int elevation;
  // component orientation
  private int orient_code;
  private String comp_orientation;
  // source
  private String source;
  // d conversion = (H/3438)*10000
  private int d_conversion;
  // data quality (for future use)
  private int data_quality;
  // instrumentation
  private String instrumentation;
  // K9 value
  private int k9_value;
  // sample rate in msec
  private int sample_rate;
  // sensor orientation
  private String sensor_orientation;
  // the publication date
  private Date publication_date;
  // the data format version number
  private String format_version;

  // minute mean data
  private GeomagAbsoluteValue minute_means [];
  // hourly mean data
  private GeomagAbsoluteValue hourly_means [] = new GeomagAbsoluteValue [N_HOURLY_MEAN_VALUES];
  // daily means
  private GeomagAbsoluteValue daily_means [] = new GeomagAbsoluteValue [N_DAILY_MEAN_VALUES];
  // K indices
  private Integer k_indices [] = new Integer [N_K_INDICES];

  // formatters
  private SimpleDateFormat pub_date_format;
  private SimpleDateFormat startDateFormat;
  
 /********************************************************************
  * creates a new CDDataDay object. The data is not read from
  * day_buffer unless required.
  * @param month_data - the CDDataMonth object this data is from
  * @param month_index - the month this data is from (0..11)
  * @param day - the day index for this data in CDDataMonth
  * @param day_buffer - an array of bytes containing the binary data
  *                     for this day
  * @throws IOException if there was an IO error
  * @throws CDException if there was a format error
  ********************************************************************/
  public CDDataDay (CDDataMonth month_data, int month_index,
                    int day, byte [] day_buffer)
  throws IOException, CDException
  {
    this.month_index = month_index;
    this.month_data = month_data;
    this.day = day;
    
    pub_date_format = new SimpleDateFormat ("yyMM");
    pub_date_format.setTimeZone(DateUtils.gmtTimeZone);
    startDateFormat = new SimpleDateFormat ("yyyyDDD");
    startDateFormat.setTimeZone(DateUtils.gmtTimeZone);
    
    // open file input stream
    readImagData (day_buffer);
  }

 /********************************************************************
  * readImagData - read binary data into CDDataDay object
  * @param day_buffer - a buffer containing the binary data for this
  *                     day
  * @throws IOException if there was an IO error
  * @throws CDException if there was a format error
  ********************************************************************/
  private void readImagData (byte [] day_buffer)
  throws IOException, CDException
  {
    ByteArrayInputStream byte_stream;
    DataInputStream data_stream;

    byte int_buffer [] = new byte [INT_BUFFER_LENGTH];
    int n, index, value;
    double data [] [];

    data = new double [N_COMPONENTS] [N_MINUTE_MEAN_VALUES];
    
    // convert byte array into an input stream
    byte_stream = new ByteArrayInputStream (day_buffer);
    data_stream = new DataInputStream (byte_stream);

    // read header block into variables
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) station_id = "";
    else station_id = new String (int_buffer);
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    start_date = Utils.bytesToInt (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    co_latitude = Utils.bytesToInt (int_buffer);
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    longitude = Utils.bytesToInt (int_buffer);
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    elevation = Utils.bytesToInt (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) comp_orientation = "";
    else comp_orientation = new String (int_buffer);
    if (comp_orientation.equalsIgnoreCase("XYZF"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
        f_scalar_type = month_data.getFScalarType();
    }
    else if (comp_orientation.equalsIgnoreCase("XYZG"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_XYZ;
        f_scalar_type = GeomagAbsoluteValue.COMPONENT_F_DIFF;
    }
    else if (comp_orientation.equalsIgnoreCase("HDZF"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
        f_scalar_type = month_data.getFScalarType();
    }
    else if (comp_orientation.equalsIgnoreCase("HDZG"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_HDZ;
        f_scalar_type = GeomagAbsoluteValue.COMPONENT_F_DIFF;
    }
    else if (comp_orientation.equalsIgnoreCase("DIFF"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
        f_scalar_type = month_data.getFScalarType();
    }
    else if (comp_orientation.equalsIgnoreCase("DIFG"))
    {
        orient_code = GeomagAbsoluteValue.ORIENTATION_DIF;
        f_scalar_type = GeomagAbsoluteValue.COMPONENT_F_DIFF;
    }
    else
        throw new CDException ("Bad orientation code: " + comp_orientation);
    
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) source = "";
    else source = new String (int_buffer);
     data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    d_conversion = Utils.bytesToInt (int_buffer);
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    data_quality = Utils.bytesToInt (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) instrumentation = "";
    else instrumentation = new String (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    k9_value = Utils.bytesToInt (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    sample_rate = Utils.bytesToInt (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) sensor_orientation = "";
    else sensor_orientation = new String (int_buffer);

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    if (int_buffer [0] == 0) publication_date = null;
    else
    {
        try { publication_date = pub_date_format.parse(new String (int_buffer)); }
        catch (ParseException e) { publication_date = null; }
    }

    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
    switch (int_buffer [0])
    {
        case 0: format_version = "1.0"; break;
        case 1: format_version = "1.1"; break;
        case 2: format_version = "2.0"; break;
        default: format_version = "Unknown"; break;
    }
    
    // 4 bytes not used
    data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);

    // read minute mean data
    for (n = 0; n < N_COMPONENTS; n++)
    {
        for (index = 0; index < N_MINUTE_MEAN_VALUES; index++)
        {
          data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
          value = Utils.bytesToInt (int_buffer);
          if (value == MISSING_DATA_VALUE)
            data [n] [index] = MISSING_REAL_DATA;
          else
            data [n] [index] = ((double) value) / 10.0;
        }
    }
    minute_means = new GeomagAbsoluteValue[N_MINUTE_MEAN_VALUES];
    for (index = 0; index < N_MINUTE_MEAN_VALUES; index++)
        minute_means [index] = new GeomagAbsoluteValue (data [0] [index], data [1] [index],
                                                        data [2] [index], data [3] [index],
                                                        f_scalar_type, MISSING_REAL_DATA, orient_code,
                                                        GeomagAbsoluteValue.ANGLE_MINUTES);

    // read hourly mean data
    for (n = 0; n < N_COMPONENTS; n++)
    {
        for (index = 0; index < N_HOURLY_MEAN_VALUES; index++)
        {
            data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
            value = Utils.bytesToInt (int_buffer);
            if (value == MISSING_DATA_VALUE)
              data [n] [index] = MISSING_REAL_DATA;
            else
              data [n] [index] = ((double) value) / 10.0;
        }
    }
    hourly_means = new GeomagAbsoluteValue[N_HOURLY_MEAN_VALUES];
    for (index = 0; index < N_HOURLY_MEAN_VALUES; index++)
        hourly_means [index] = new GeomagAbsoluteValue (data [0] [index], data [1] [index],
                                                        data [2] [index], data [3] [index],
                                                        f_scalar_type, MISSING_REAL_DATA, orient_code,
                                                        GeomagAbsoluteValue.ANGLE_MINUTES);

    // read daily means
    for (n = 0; n < N_COMPONENTS; n++)
    {
        for (index = 0; index < N_DAILY_MEAN_VALUES; index++)
        {
            data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
            value = Utils.bytesToInt (int_buffer);
            if (value == MISSING_DATA_VALUE)
              data [n] [index] = MISSING_REAL_DATA;
            else
              data [n] [index] = ((double) value) / 10.0;
        }
    }
    daily_means = new GeomagAbsoluteValue[N_DAILY_MEAN_VALUES];
    for (index = 0; index < N_DAILY_MEAN_VALUES; index++)
        daily_means [index] = new GeomagAbsoluteValue (data [0] [index], data [1] [index],
                                                       data [2] [index], data [3] [index],
                                                       f_scalar_type, MISSING_REAL_DATA, orient_code,
                                                       GeomagAbsoluteValue.ANGLE_MINUTES);

    // read k indices
    for (n=0; n < N_K_INDICES; n++)
    {
        data_stream.read (int_buffer, 0, INT_BUFFER_LENGTH);
        k_indices [n] = new Integer (Utils.bytesToInt (int_buffer));
        // note K index is multiplied by 10 in data file
        if(k_indices [n]<0 ||k_indices [n]>=100) k_indices[n]= MISSING_K_VALUE;
    }
  }

  public String getCompOrientation () { return comp_orientation; }
  public String getSource () { return source; }
  public int getDConversion () { return d_conversion; }
  public String getSensorOrientation () { return sensor_orientation; }
  public String getStationID() { return station_id; }
  public int getCoLatitude() { return co_latitude; }
  public int getLongitude() { return longitude; }
  public int getElevation() { return elevation; }
  public int getDataQuality() { return data_quality; }
  public String getInstrumentation() { return instrumentation; }
  public int getK9Value() { return k9_value; }
  public int getSampleRate() { return sample_rate; }
  public Date getPublicationDate() { return publication_date; }
  public String getFormatVersion() { return format_version; }
  
  public Date getStartDate ()
  {
    try
    {
      return startDateFormat.parse ("" + start_date);
    }
    catch (ParseException e)
    {
      return null;
    }
  }

  /********************************************************************
  * getMinuteMeans - get the minute mean data for this day
  * @return a 2D array containing minute mean data
  ********************************************************************/
  public GeomagAbsoluteValue [] getMinuteMeans ()
  {
    return minute_means;
  }

 /********************************************************************
  * getHourlyMeans - get the hourly mean data for this day
  * @return a 2D array containing hourly mean data
  ********************************************************************/
  public GeomagAbsoluteValue [] getHourlyMeans ()
  {
    return hourly_means;
  }

 /********************************************************************
  * getDailyMeans - get the daily mean data for this day
  * @return a 1D array containing daily mean data
  ********************************************************************/
  public GeomagAbsoluteValue [] getDailyMeans ()
  {
    return daily_means;
  }

 /********************************************************************
  * getKIndices - get the k indices for this day
  * @return a 1D array containing k indices
  ********************************************************************/
  public Integer [] getKIndices ()
  {
    return k_indices;
  }

}
