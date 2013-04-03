/*
 * GINData.java
 *
 * Created on 05 November 2006, 14:30
 */

package bgs.geophys.library.GeomagneticInformationNode;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import java.util.*;
import java.text.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Class to hold GIN time series data. Two constructors
 * are provided, one to read data from a file in the GIN internal binary
 * format, the other to create the object from data in arrays. A method
 * is provided to write the data to a GIN internal binary file.
 *
 * @author  smf
 */
public class GINData 
{

    /** inner class that holds statistics on a time series data set */
    public class DataStats
    {
        /** number of usable (non-null) values */
        public int count;
        /** minimum value in the set */
        public double min;
        /** maximum value in the set */
        public double max;
        /** average value of the set */
        public double average;
        /** earliest time (in seconds) since start of data */
        public int earliest_time;
        /** latest time (in seconds) since start of data */
        public int latest_time;
    }

    /** code for a mising data value - thie number is stored in the data files
     * to indicate missing data (as well as being used in programs that need to
     * flag missing values) - the value is MAXFLOAT from SUN values.h */
    public static double MISSING_DATA_FLAG = 3.40282346638528860e+38;
    
    /** number of bytes used by a record in a data file */
    public static int RECORD_LENGTH = 16;

    /** number of bytes in a data file */
    private int file_length;

    // member variables for verifying data */
    // list of stations
    private StationDetails station_details_list;
    // list of application details
    private AppDetails app_details_list;
    // GIN dictionary
    private GINDictionary gin_dictionary;
    
    // member variables that describe the time series data in this object
    // reference to station details for this station
    private StationDetails.StationDetailsFields sta_details;	
    // the sample rate for this data (in samples per day)
    private int samps_per_day;
    // the component orientation for this data
    private String components;
    // reference to application details for this station
    private AppDetails.AppDetailsFields app_details;	
    // start date / time for the data
    Date date;
    // type of data (description and code from dictionary segment DATA_TYPE)
    private String data_type_desc;
    private String data_type_code;
    // time series array - n data points of type GeomagAbsoluteValue
    private Collection<GeomagAbsoluteValue> data;
    // statistics for the 4 components
    private GINData.DataStats stats [];
    
    // flag to allow / prevent overwriting of existing data
    private static boolean allow_overwrite = true;
    
    /** Creates a new instance of GINData from data in memory
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param components the component orientation for this data
     * @param date the starting date for the data
     * @param data_type_desc the data type description for the data
     * @param data1 component 1 data
     * @param data2 component 2 data
     * @param data3 component 3 data
     * @param data4 component 4 data
     * @throws GINDataException if there is an error loading the data
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public GINData (String station_code, int samps_per_day, String components,
                    Date date, String data_type_desc,
                    double data1 [], double data2 [], double data3 [], double data4 [])
    throws GINDataException, ConfigFileException, ParameterException
    {        
        int orientation, count;
        String test_components;
        
        // load verifying objects
        station_details_list = new StationDetails ();
        app_details_list = new AppDetails ();
        gin_dictionary = new GINDictionary ();

        // store the sample rate
        this.samps_per_day = samps_per_day;
        file_length = (16 * samps_per_day) + 4;
        this.components = components;
        
        // check the data
        if ((date.getTime() % 1000) != 0)
            throw new ParameterException ("Data time must start on a second boundary");
        if (data1.length != data2.length || data1.length != data3.length || 
            data1.length != data4.length)
            throw new ParameterException ("Data arrays not the same length");
        sta_details = station_details_list.findStation (station_code);
        if (sta_details == null)
            throw new ParameterException ("Unknown station code: " + station_code);
        app_details = app_details_list.findStation (station_code);
        if (app_details == null)
            throw new ConfigFileException ("Missing application details for station code: " + station_code);
        data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
        if (data_type_code == null)
            throw new ParameterException ("Data type does not exist: " + data_type_desc);
        if (data_type_code.equalsIgnoreCase("j"))
            throw new ParameterException ("Data type 'j' not allowed here");

        if (components.length() != 4)
            throw new GINDataException ("Unrecognised component orientation: " + components);
        test_components = components.substring (0, 3);
        if (test_components.equalsIgnoreCase("HDZ"))
            orientation = GeomagAbsoluteValue.ORIENTATION_HDZ;
        else if (test_components.equalsIgnoreCase("XYZ"))
            orientation = GeomagAbsoluteValue.ORIENTATION_XYZ;
        else if (test_components.equalsIgnoreCase("DIF"))
            orientation = GeomagAbsoluteValue.ORIENTATION_DIF;
        else
            throw new GINDataException ("Unsupported component code: " + test_components);

        // record the data
        this.data_type_desc = data_type_desc;
        this.date = date;
        this.data = new ArrayList<GeomagAbsoluteValue> ();
        for (count=0; count<data1.length; count++)
        {
            data.add (new GeomagAbsoluteValue (data1[count], data2[count], data3[count], data4[count],
                                               GeomagAbsoluteValue.COMPONENT_F_SCALAR, MISSING_DATA_FLAG, 
                                               orientation, GeomagAbsoluteValue.ANGLE_DEGREES));
        }
        
        // initialise (but don't create) the statistics
        stats = new DataStats [4];
        stats [0] = stats [1] = stats [2] = stats [3] = null;
    }

    /** Creates a new instance of GINData from data on disk
     * @param station_code the station code
     * @param samps_per_day the sample rate in samples per day
     * @param date the starting date for the data
     * @param length the duration (in samples) for the data
     * @param data_type_desc the data type description for the data
     * @param allow_j if true allow the special data type j (adj-or-rep)
     * @throws GINDataException if there is an error loading the data
     * @throws ConfigFileException if there is an error with the configuration files
     * @throws ParameterException if there was a problem with the parameters */
    public GINData (String station_code, int samps_per_day, Date date, int length, String data_type_desc,
                    boolean allow_j)
    throws GINDataException, ConfigFileException, ParameterException
    {
        int total_data_points;
                        
        // load verifying objects
        station_details_list = new StationDetails ();
        app_details_list = new AppDetails ();
        gin_dictionary = new GINDictionary ();
        
        // store the sample rate
        this.samps_per_day = samps_per_day;
        file_length = (16 * samps_per_day) + 4;
        
        // check the station code and data type
        if ((date.getTime() % 1000) != 0)
            throw new ParameterException ("Data time must start on a second boundary");
        sta_details = station_details_list.findStation(station_code);
        if (sta_details == null)
            throw new ParameterException ("Station does not exist: " + station_code);
        data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
        if (data_type_code == null)
            throw new ParameterException ("Data type does not exist: " + data_type_desc);
        if ((! allow_j) && data_type_code.equalsIgnoreCase("j"))
            throw new ParameterException ("Data type 'j' not allowed here");
        // record the parameters
        this.data_type_desc = data_type_desc;
        this.date = date;
        
        // create the data array
        data = new ArrayList<GeomagAbsoluteValue> ();

        // handle the special adjusted or reported data type
        if (! data_type_code.equalsIgnoreCase("j")) readData (length);
        else
        {
            // try quasi-definitive data first
            data_type_desc = this.data_type_desc = "quasi-def";
            data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
            if (data_type_code == null)
                throw new GINDataException ("Missing code for " + data_type_desc + " data");
            readData (length);
            total_data_points = getStats (0).count + getStats(1).count +
                                getStats (2).count + getStats(3).count;
            if (total_data_points <= 0)
            {
                // try adjusted data next
                // re-create the data array first (because readData appends to it)
                data_type_desc = this.data_type_desc = "adjusted";
                data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, data_type_desc);
                if (data_type_code == null)
                    throw new GINDataException ("Missing code for " + data_type_desc + " data");
                data = new ArrayList<GeomagAbsoluteValue> ();
                readData (length);
                total_data_points = getStats (0).count + getStats(1).count +
                                    getStats (2).count + getStats(3).count;
                if (total_data_points <= 0)
                {
                    // no adjusted data - try reported
                    // re-create the data array first (because readData appends to it)
                    data_type_desc = this.data_type_desc = "reported";
                    data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, "reported");
                    if (data_type_code == null)
                        throw new GINDataException ("Missing code for " + data_type_desc + " data");
                    data = new ArrayList<GeomagAbsoluteValue> ();
                    readData (length);
                }
            }
        }
    }
    
    /** insert this data into the database
     * @param window time window for data:
     *        if -ve or 0 then ignore
     *        if +ve then this variable holds then number of days (including today) that
     *               the data can be time stamped for (eg. 1 - data must be today,
     *               2 - data must be today or yesterday, ...)
     * @param bad_writer file to write bad records to
     * @param use_modifiers if true apply any arithmetic modifiers present in the
     *        application_details table */
    public void insertData (int window, PrintStream bad_writer, boolean use_modifiers)
    {
        int sample_count, count;
        long date_count, date_inc, data_offset;
        boolean new_file, null_flags [];
        byte buffer [], test_components_bytes [];
        double samples [], read_samples [];
        char data_char;
        SimpleMathsEvaluator evaluators [];
        AppDetails.AppDetailsFields app_details;
        Date now_date, end_data_date, start_window_date;
        SimpleDateFormat date_format;
        ByteBuffer internal_reader;
        String filename, file_fault, general_fault, test_components;
        RandomAccessFile writer;
        FileLock file_lock;
        Iterator iterator;
        GeomagAbsoluteValue value;

        general_fault = file_fault = null;
        date_format = new SimpleDateFormat ("dd-MM-yyyy HH:mm:ss");
        date_format.setTimeZone (SimpleTimeZone.getTimeZone ("gmt"));

        // if the station is under test then override the given data_type
        if (sta_details.test)
        {
            data_type_code = gin_dictionary.find ("DATA_TYPE", GINDictionary.SEARCH_DATA_CASE_INDEPENDANT, "test");
            general_fault = "Missing code for test data";
        }
        
        // check that the flag is set to allow data to be loaded
        app_details = null;
        if (general_fault == null)
        {
            app_details = app_details_list.findStation(sta_details.station_code);
            if (app_details == null)
                general_fault = "No entry in application_details database";
            else if (! app_details.enter_data)
                general_fault = "Data entry disabled in application_details database";
        }
        
        // check the time window
        if ((general_fault == null) && (window >= 1))
        {
            // get the current date/time
            now_date = new Date ();
            end_data_date = new Date (date.getTime() + ((long) data.size() * 60000l));
            start_window_date = new Date (now_date.getTime() - (86400000l * (long) window));
            
            // check that the data is not in the future or before the window
            if (end_data_date.getTime() > now_date.getTime())
                general_fault = "Data time stamp in future (current time is "+
                                date_format.format (now_date) + ")";
            if (date.getTime() < start_window_date.getTime())
                general_fault = "Data time stamp before given window (current time is "+
                                date_format.format (now_date) + ")";
        }
 
        // check the data type - disallow the special data type 'j'
        if ((general_fault == null) && data_type_code.equalsIgnoreCase("j"))
            general_fault = "Data type 'j' not allowed here";

        // create an object to evaluate the modifiers
        evaluators = new SimpleMathsEvaluator [4];
        for (count=0; count<4; count++)
        {
            if (general_fault != null)
                evaluators [count] = new SimpleMathsEvaluator ();
            if (! use_modifiers)
                evaluators [count] = new SimpleMathsEvaluator ();
            else if (app_details.comp_mod [count].trim().length() <= 0)
                evaluators [count] = new SimpleMathsEvaluator ();
            else
            {
                try
                {
                    evaluators [count] = new SimpleMathsEvaluator (app_details.comp_mod [count]);
                }
                catch (ParseException e)
                {
                    general_fault = "Unable to parse modifier expression (from application_details table)";
                }
            }
        }

        // check the orientation
        null_flags = new boolean [4];
        if (general_fault == null)
        {
            if (components.length() != 4)
                general_fault = "Bad data components (string must be 4 characters long): " + components;
            else
            {
                for (count=0; count<4; count++)
                {
                    data_char = components.charAt(count);
                    if (data_char == 'N' || data_char == 'n') null_flags [count] = true;
                    else null_flags [count] = false;
                }
            }
        }
        else
        {
            for (count=0; count<4; count++) null_flags [count] = false;
        }
        
        
        // for each data point ...
        filename = "";
        writer = null;
        file_lock = null;
        buffer = new byte [RECORD_LENGTH];
        internal_reader = ByteBuffer.wrap(buffer);
        internal_reader.order (ByteOrder.BIG_ENDIAN);
        samples = new double [4];
        read_samples = new double [4];
        date_inc = 86400000 / samps_per_day;
        for (sample_count = 0, date_count = date.getTime(), iterator = data.iterator();
             iterator.hasNext(); 
             sample_count ++, date_count += date_inc)
        {
            // do we need a new file - a new file occurs on the first iteration
            // or at midnight
            if ((sample_count == 0) || ((date_count % 86400000) == 0)) new_file = true;
            else new_file = false;
        
            // generate the filename and open the file
            if (new_file)
            {
                // reset file fault flag
                if (general_fault != null) file_fault = general_fault;
                else file_fault = null;

                // generate the filename
                if (file_fault == null)
                {
                    try
                    {
                        filename = GINUtils.makeDataFilename (sta_details.station_code, samps_per_day,
                                                              data_type_code, new Date (date_count));
                    }
                    catch (ConfigFileException e) { file_fault = e.getMessage(); }
                }                        
                
                // close the previous file, open the next
                if (file_fault == null)
                {
                    if (file_lock != null) try { file_lock.release(); } catch (IOException e) { }
                    if (writer != null) try { writer.close (); } catch (IOException e) { }
                    try
                    {
                        writer = new RandomAccessFile (filename, "rw");
                        file_lock = writer.getChannel().lock();
                    }
                    catch (FileNotFoundException e) { file_fault = "Unexpected FileNotFoundException"; }
                    catch (IOException e) { file_fault = "IO Error"; }
                }

                // if the file is empty, fill it with missing data and the component code
                if (file_fault == null)
                {
                    try
                    {
                        if (writer.length() == 0)
                        {
                            internal_reader.rewind();
                            for (count=0; count<4; count++) 
                                internal_reader.putFloat((float) MISSING_DATA_FLAG);
                            for(data_offset=0; data_offset<samps_per_day; data_offset ++)
                                writer.write (buffer);
                            writer.write (components.getBytes());
                        }
                        else if (writer.length() == ((long) file_length))
                        {
                            writer.seek (file_length -4);
                            test_components_bytes = new byte [4];
                            writer.read (test_components_bytes);
                            test_components = new String (test_components_bytes);
                            if (! components.equals(test_components))
                                file_fault = "Mismatch in component orientation, " + components + " should be " + test_components;
                        }
                        else
                            file_fault = "Wrong size database file: " + filename;
                    }
                    catch (IOException e) { file_fault = "IO Error"; }
                }
                
            }
            
            // nothing more is needed for a missing data record
            value = (GeomagAbsoluteValue) iterator.next();
            if (null_flags [0]) samples [0] = MISSING_DATA_FLAG;
            else samples [0] = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (null_flags [1]) samples [1] = MISSING_DATA_FLAG;
            else samples [1] = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (null_flags [2]) samples [2] = MISSING_DATA_FLAG;
            else samples [2] = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (null_flags [3]) samples [3] = MISSING_DATA_FLAG;
            else samples [3] = value.getFScalar();
            if ((samples [0] != MISSING_DATA_FLAG) ||
                (samples [1] != MISSING_DATA_FLAG) ||
                (samples [2] != MISSING_DATA_FLAG) ||
                (samples [3] != MISSING_DATA_FLAG))
            {
                data_offset = time2position (date_count);
                
                // apply arithmetic modifiers
                for (count=0; count<4; count++)
                {
                    if (use_modifiers && (samples [count] != MISSING_DATA_FLAG))
                        samples [count] = evaluators[count].evaluate (samples [count]);
                }
                
                // read the data from the file and check it is not already loaded
                if ((file_fault == null) && (! allow_overwrite))
                {
                    try
                    {
                        writer.seek (data_offset * (long) RECORD_LENGTH);
                        readNextRecord (writer, read_samples);
                        for (count=0; count<4; count++)
                        {
                            if (read_samples[count] != MISSING_DATA_FLAG)
                                samples[count] = read_samples[count];
                        }
                    }
                    catch (IOException e) { file_fault = "IO Error"; }
                }
                
                // write the data to the file
                if (file_fault == null)
                {
                    try
                    {
                        // insert into byte buffer
                        internal_reader.rewind();
                        for (count=0; count<4; count++)
                            internal_reader.putFloat((float) samples [count]);
                        // insert byte buffer into file
                        writer.seek (data_offset * (long) RECORD_LENGTH);
                        writer.write (buffer);
                    }
                    catch (IOException e) { file_fault = "IO Error"; }
                }

                // if there was an error write the data to the log
                if (file_fault != null)
                {
                    bad_writer.println (sta_details.station_code + " " +
                                        Integer.toString (samps_per_day) + " " +
                                        components + " " +
                                        date_format.format (new Date (date_count)) + " " +
                                        data_type_code + " " +
                                        Double.toString (samples [0]) + " " +
                                        Double.toString (samples [1]) + " " +
                                        Double.toString (samples [2]) + " " +
                                        Double.toString (samples [3]) + "[" +
                                        file_fault + "]");
                }
            }
        }

        // shutdown file access
        try { if (file_lock != null) file_lock.release(); } catch (IOException e) { }
        try { if (writer != null) writer.close(); } catch (IOException e) { }
    }
    
    /** get statistics for a given component
     * @param component the component number (0 to 3)
     * @return the statistics
     * @throws GINDataException if component number is invalid
     * @throws ParameterException if there was a problem with the parameters */
    public DataStats getStats (int component)
    throws GINDataException, ParameterException
    {
        int count;
        GeomagAbsoluteValue value;
        double number;
        Iterator iterator;
        
        if (component < 0 || component > 3)
            throw new ParameterException ("component must be between 0 and 3");
        if (stats [component] == null)
        {
            stats [component] = new GINData.DataStats ();
            stats [component].count = 0;
            stats [component].min = Float.MAX_VALUE;
            stats [component].max = Float.MIN_VALUE;
            stats [component].average = 0;
            stats [component].earliest_time = Integer.MAX_VALUE;
            stats [component].latest_time = Integer.MIN_VALUE;
            for (iterator = data.iterator(), count = 0;
                 iterator.hasNext(); 
                 count ++)
            {
                value = (GeomagAbsoluteValue) iterator.next();
                switch (component)
                {
                    case 0:
                        number = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES);
                        break;
                    case 1:
                        number = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES);
                        break;
                    case 2:
                        number = value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES);
                        break;
                    case 3:
                        number = value.getFScalar();
                        break;
                    default:
                        number = MISSING_DATA_FLAG;
                        break;
                }
                if (number != MISSING_DATA_FLAG)
                {
                    stats [component].count ++;
                    if (number < stats [component].min)
                        stats [component].min = number;
                    if (number > stats [component].max)
                        stats [component].max = number;
                    stats [component].average += number;
                    if (stats [component].earliest_time == Integer.MAX_VALUE)
                        stats [component].earliest_time = index2time (count);
                    stats [component].latest_time = index2time (count);
                }
            }
            if (stats [component].count > 0)
                stats [component].average /= (float) stats [component].count;
            else
            {
                stats [component].min = MISSING_DATA_FLAG;
                stats [component].max = MISSING_DATA_FLAG;
                stats [component].average = MISSING_DATA_FLAG;
            }
        }
        return stats [component];
    }

    /** get length of data arrays
     * @return the length in samples */
    public int getDataLength () { return data.size(); }
    
    /** get all samples
     * @return the data samples as a collection of GeomagAbsoluteValues */
    public Collection<GeomagAbsoluteValue> getData ()
    {
        return data;
    }

    /** get the station details for the station asociated with this data 
     * @return the station details */
    public StationDetails.StationDetailsFields getStationDetails ()
    {
        return sta_details;
    }
    
    /** get the data type description for this data 
     * @return the data type name (e.g. "reported") */
    public String getDataTypeDescription ()
    {
        return data_type_desc;
    }

    /** get the data type code for this data 
     * @return the data type code (e.g. "r") */
    public String getDataTypeCode ()
    {
        return data_type_code;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // private code below here
    //////////////////////////////////////////////////////////////////////////////////////
    
    // routine to read data from a data file
    private void readData (int length)
    throws GINDataException
    {
        int sample_count, orientation;
        long date_count, data_offset, date_inc;
        boolean new_file;
        double samples [];
        byte test_components_bytes [];
        String filename, test_components;
        RandomAccessFile reader;
        FileLock file_lock;
        
        // catch file IO exceptions ...
        filename = "";
        reader = null;
        file_lock = null;
        samples = new double [4];
        date_inc = 86400000 / samps_per_day;
        components = null;
        orientation = GeomagAbsoluteValue.ORIENTATION_UNKNOWN;
        try
        {
            // for each data point ...
            for (sample_count = 0, date_count = date.getTime();
                 sample_count < length; sample_count ++, date_count += date_inc)
            {
                // do we need a new file - a new file occurs on the first iteration
                // or at midnight
                if ((sample_count == 0) || ((date_count % 86400000) == 0)) new_file = true;
                else new_file = false;
            
                // generate the filename and open the file
                if (new_file)
                {
                    // generate the filename
                    filename = GINUtils.makeDataFilename (sta_details.station_code, samps_per_day,
                                                          data_type_code, new Date (date_count));
                
                    // close the previous file, open the next
                    if (file_lock != null) file_lock.release();
                    if (reader != null) reader.close ();
                    try
                    {
                        // open and lock the file
                        reader = new RandomAccessFile (filename, "r");
                        file_lock = reader.getChannel().lock(0, file_length, true);
                        
                        // read and check the orientation of the data
                        reader.seek (file_length -4);
                        test_components_bytes = new byte [4];
                        reader.read (test_components_bytes);
                        test_components = new String (test_components_bytes);
                        if (components == null)
                        {
                            components = test_components.substring (0, 3);
                            if (components.equalsIgnoreCase("HDZ"))
                                orientation = GeomagAbsoluteValue.ORIENTATION_HDZ;
                            else if (components.equalsIgnoreCase("XYZ"))
                                orientation = GeomagAbsoluteValue.ORIENTATION_XYZ;
                            else if (components.equalsIgnoreCase("DIF"))
                                orientation = GeomagAbsoluteValue.ORIENTATION_DIF;
                            else
                                throw new GINDataException ("Unsupported component code: " + test_components);
                            components = test_components;
                        }
                        else if (! components.equals(new String (test_components)))
                            throw new GINDataException ("Orientation changes from " + components + " to " + test_components + " at " + date.toString());

                        // if needed seek to the first location
                        data_offset = time2position (date_count);
                        if (data_offset != 0)
                            reader.seek (data_offset * (long) RECORD_LENGTH);
                    }
                    catch (FileNotFoundException e)
                    {
                        reader = null;
                        file_lock = null;
                    }
                }
                
                // read the next sample
                readNextRecord (reader, samples);
                data.add (new GeomagAbsoluteValue (samples [0], samples [1],
                                                   samples [2], samples [3],
                                                   GeomagAbsoluteValue.COMPONENT_F_SCALAR,
                                                   MISSING_DATA_FLAG,
                                                   orientation,
                                                   GeomagAbsoluteValue.ANGLE_MINUTES));
            }
        }
        catch (ConfigFileException e) 
        { 
            throw new GINDataException ("Unable to find data file directory", e); 
        }
        catch (IOException e) 
        { 
            throw new GINDataException ("IO error: " + filename, e); 
        }
        finally
        {
            // shutdown file access
            try { if (file_lock != null) file_lock.release(); } catch (IOException e) { }
            try { if (reader != null) reader.close(); } catch (IOException e) { }
        }

        // initialise (but don't create) the statistics
        stats = new DataStats [4];
        stats [0] = stats [1] = stats [2] = stats [3] = null;
    }

    // read the next record from a stream
    private void readNextRecord (RandomAccessFile reader, double samples [])
    throws IOException
    {
        byte buffer [];
        ByteBuffer internal_reader;
        
        if (reader == null)
        {
            samples [0] = MISSING_DATA_FLAG;
            samples [1] = MISSING_DATA_FLAG;
            samples [2] = MISSING_DATA_FLAG;
            samples [3] = MISSING_DATA_FLAG;
        }
        else
        {
            buffer = new byte [RECORD_LENGTH];
            if (reader.read (buffer) != buffer.length)
                throw new IOException ("Data missing from file");
         
            // extract the floating point values from the buffer
            internal_reader = ByteBuffer.wrap(buffer);
            internal_reader.order (ByteOrder.BIG_ENDIAN);
            samples [0] = internal_reader.getFloat();
            samples [1] = internal_reader.getFloat();
            samples [2] = internal_reader.getFloat();
            samples [3] = internal_reader.getFloat();
        }
    }
    
    /** convert time of day to an index in a data file
     * @param date the time to convert (date info is ignored)
     * @return the index of the correcpsonding record in a data file */
    private int time2index (Date date) { return time2index (date.getTime()); }
    private int time2index (long date_count)
    {
        int sec_of_day;

        sec_of_day = (int) ((date_count % 86400000l) / 1000l);
        return (sec_of_day * samps_per_day) / 86400;
    }
    
    /** convert time of day to a byte offset in a data file
     * @param date the time to convert (date info is ignored)
     * @return the byte count to reach the corresponding record in a data file */
    private long time2position (Date date) { return (long) time2index (date) * (long) RECORD_LENGTH; }
    private long time2position (long date_count) { return (long) time2index (date_count) * (long) RECORD_LENGTH; }

    /** convert an index into a data file into a time
     * @param index_no the index
     * @return the time, in second of the day */
    private int index2time (int index_no)
    {
        return (index_no * 86400) / samps_per_day;
    }

}
