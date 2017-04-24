/**
 * DataFormat.java
 *
 * Created on 19 March 2004, 11:28
 */
package bgs.geophys.library.Data;

import java.util.*;
import java.io.*;

/**
 * Abstract class to provide those elements common to all geomagnetic data formats.
 * Some details:
 * 1.) This class provides all header fields for any format (except optional fields
 *     for IAGA 2002) - if a field is not used set it to null (for Strings)
 *     of MISSING_HEADER_VALUE for numbers
 * 2.) Latitude and longitude are held as decimal degrees
 *     Elevation is held as metres
 *     Field strengths are held as nanoTesla
 *     Field angles are held in minutes
 *     All values have to be converted appropriately when reading/writing
 *     to individual data formats.
 * 3.) All numeric values are double - all data is held as 4 component
 *     fixed length arrays. Classes based on this class should be immutable
 *     (ie once created the data they contain should not be changed).
 *     Codes are provided for missing values (gap) and missing components
 *     (not recorded) - this class only uses these internal codes, but it
 *     allows conversion to other codes
 * 4.) Additionally mean data and 'quiet day' flags can be associated with
 *     the main data.
 *
 * @author  smf
 */
public abstract class GeomagDataFormat_Old 
{

    /** code for a missing header value */
    public static final double MISSING_HEADER_VALUE = Double.MAX_VALUE;
    /** code for a missing data sample */
    public static final double MISSING_DATA_SAMPLE = 999999.0;
    /** code for a missing component - is unrecorded component */
    public static final double MISSING_COMPONENT = 888888.0;

    /** termination type code - UNIX record terminator */
    public static final byte TERM_TYPE_UNIX [];
    /** termination type code - Windows record terminator */
    public static final byte TERM_TYPE_WINDOWS [];
    /** termination type code - native record terminator */
    public static final byte TERM_TYPE_NATIVE [];
    /** termination type code - no termination */
    public static final byte TERM_TYPE_NONE [];
    
    // header fields
    private String stationCode;         // IAGA code - old two digit codes
                                        // can be converted by getIaga()
    private String stationName;         // Free text
    private double latitude;            // Decimal degrees
    private double longitude;           // Decimal degrees
    private double elevation;           // Decimal metres
    private String compCode;            // 3 or 4 digit component code
    private String dataType;            // Description of data type - reported, adjusted, ...
    private String ginCode;             // 3 digit code for associated INTERMAGNET GIN
    private String instituteName;       // name of the institute that runs the observatory
    private String sensorOrientation;   // original orientation of the vector sensor
    private String samplePeriodString;  // description of the original sample period
    private String intervalType;        // description of the data set sample period
     
    // these members hold the data
    private long samplePeriod;            // Time between samples, in milliseconds
    private Date startDate;               // Date/time of first sample
    private double data [] [];            // 4 components, nT or minutes
    private boolean all_missing;          // true until some non-missing data is added
    
    // these members hold additional data - means for each of the components
    // and a quiet day flag - these variables are only valid where blocksize is
    // not negative - once blocksize is set, data must be inserted in blocks that size
    private int blocksize;                // the size of blocks used to calculate means
    private ArrayList<Double> c1_means;
    private ArrayList<Double> c2_means;
    private ArrayList<Double> c3_means;
    private ArrayList<Double> c4_means;
    private ArrayList<Character> quietDayFlag;
    
    // these members hold details on the file that contains the data
    private File file;
    
    // static initialisors
    static
    {
        TERM_TYPE_UNIX = "\n".getBytes();
        TERM_TYPE_WINDOWS = "\r\n".getBytes();
        TERM_TYPE_NATIVE = System.getProperty("line.separator").getBytes();
        TERM_TYPE_NONE = "".getBytes();
    }

    /**
     * Creates a new instance of GeomagDataFormat - fill out the header values 
     * 
     * @param stationCode IAGA code - must be given IAGA code - old two digit codes
     *        will be converted
     * @param stationName free text station name, may be null
     * @param latitude the station latitude - if unknown set to MISSING_HEADER_VALUE
     * @param longitude the station longitude - if unknown set to MISSING_HEADER_VALUE
     * @param elevation the station elevation - if unknown set to MISSING_HEADER_VALUE
     * @param compCode 3 or 4 digit component code
     * @param dataType Description of data type or data type code -
     *        reported (R), adjusted (A), ...
     * @param ginCode 3 digit code for associated INTERMAGNET GIN
     * @param instituteName name of the institute that runs the observatory
     * @param sensorOrientation original orientation of the vector sensor
     * @param samplePeriodString description of the original sample period
     * @param intervalType description of the data set sample period
     * @param allowFourDigitSC true to allow 4 character station codes
     * @param blocksize the mandatory size of arrays that will be delivered
     *        to any of the addData methods - arrays must contain an integer
     *        multiple of blocksize elements - also means will be
     *        calculated if blocksize is not negative - set to -ve
     *        number to turn all this off.
     * @throws GeomagDataException if there are any faults
     */
    public GeomagDataFormat_Old (String stationCode, String stationName,
                             double latitude, double longitude, double elevation,
                             String compCode, String dataType, String ginCode,
                             String instituteName, String sensorOrientation,
                             String samplePeriodString, String intervalType,
                             boolean allowFourDigitSC, int blocksize)
    throws GeomagDataException
    {
        // check the station name
        if (stationCode == null)
            throw new GeomagDataException ("Missing station code");
        switch (stationCode.length())
        {
            case 2:
                this.stationCode = getIaga (stationCode);
                if (this.getStationCode() == null)
                    throw new GeomagDataException ("Unable to translate old style station code: " + stationCode);
                break;
            case 3:
                this.stationCode = stationCode;
                break;
            case 4:
                if (allowFourDigitSC)
                    this.stationCode = stationCode;
                else
                    throw new GeomagDataException ("Station code incorrect length");
                break;
            default:
                throw new GeomagDataException ("Station code incorrect length");
        }
        
        // record the other header values
        if (stationName == null) stationName = stationCode;
        else this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.compCode = compCode;
        if (this.compCode != null)
        {
            if (this.compCode.length() < 3 || this.compCode.length() > 4)
                throw new GeomagDataException ("Component code must contain 3 or 4 characters");
        }
        this.dataType = dataType;
        this.ginCode = ginCode;
        if (this.ginCode != null)
        {
            if (this.ginCode.length() != 3)
                throw new GeomagDataException ("GIN code must contain 3 characters");
        }
        this.instituteName = instituteName;
        this.sensorOrientation = sensorOrientation;
        this.samplePeriodString = samplePeriodString;
        this.intervalType = intervalType;
        
        // set the data to show it is empty
        samplePeriod = 0;
        startDate = null;
        data = new double [4] [];
        data [0] = data [1] = data [2] = data [3] = null;
        
        // set up the extra data
        this.blocksize = blocksize;
        c1_means = new ArrayList<Double> ();
        c2_means = new ArrayList<Double> ();
        c3_means = new ArrayList<Double> ();
        c4_means = new ArrayList<Double> ();
        quietDayFlag = new ArrayList<Character> ();
        
        // show that there is no associated file
        file = null;
        
        // show there is no non-null data
        all_missing = true;
    }
    
    /** add a single data sample - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param missingDataSample value used for missing data (which will be translated)
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent value used for missing component (which will be translated)
     *        if none use MISSING_COMPONENT
     * @param c1 data for the first component
     * @param c2 data for the first component
     * @param c3 data for the first component
     * @param c4 data for the first component
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         double missingDataSample, double missingComponent,
                         double c1, double c2, double c3, double c4)
    throws GeomagDataException
    {
        int startIndex;
        
        // sort out the timing details and the data arrays
        checkDataSizes (1, 1, 1, 1);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (1);
        
        // add the data
        insertData (0, startIndex, c1, missingDataSample, missingComponent);
        insertData (1, startIndex, c2, missingDataSample, missingComponent);
        insertData (2, startIndex, c3, missingDataSample, missingComponent);
        insertData (3, startIndex, c4, missingDataSample, missingComponent);
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** add data in the form of data arrays - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param missingDataSample value used for missing data (which will be translated)
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent value used for missing component (which will be translated)
     *        if none use MISSING_COMPONENT
     * @param c1 data for the first component
     * @param c2 data for the first component
     * @param c3 data for the first component
     * @param c4 data for the first component
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         double missingDataSample, double missingComponent,
                         double c1[], double c2[], double c3[], double c4[])
    throws GeomagDataException
    {
        int startIndex, index;
        
        // sort out the timing details and the data arrays
        checkDataSizes (c1.length, c2.length, c3.length, c4.length);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (c1.length);
        
        // add the data
        for (index=0; index<c1.length; index++)
        {
            insertData (0, startIndex + index, c1 [index], missingDataSample, missingComponent);
            insertData (1, startIndex + index, c2 [index], missingDataSample, missingComponent);
            insertData (2, startIndex + index, c3 [index], missingDataSample, missingComponent);
            insertData (3, startIndex + index, c4 [index], missingDataSample, missingComponent);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }

    /** add data in the form of data arrays - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param missingDataSample value used for missing data (which will be translated)
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent value used for missing component (which will be translated)
     *        if none use MISSING_COMPONENT
     * @param cData data for all components indexed by component, then sample - [comp][samp]
     * @param c1_multiplier amount to mutliply 1st component by the get nT/mins
     * @param c2_multiplier amount to mutliply 2nd component by the get nT/mins
     * @param c3_multiplier amount to mutliply 3rd component by the get nT/mins
     * @param c4_multiplier amount to mutliply 4th component by the get nT/mins
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         int missingDataSample, int missingComponent,
                         Integer cData [] [],
                         double c1_multiplier, double c2_multiplier,
                         double c3_multiplier, double c4_multiplier)
    throws GeomagDataException
    {
        int startIndex, index, componentCount;
        double value, multipliers [];
        
        // sort out the timing details and the data arrays
        if (cData.length != 4)
            throw new GeomagDataException ("Data must have 4 components");
        checkDataSizes (cData[0].length, cData[1].length, cData[2].length, cData[3].length);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (cData[0].length);
        multipliers = new double [4];
        multipliers [0] = c1_multiplier;
        multipliers [1] = c2_multiplier;
        multipliers [2] = c3_multiplier;
        multipliers [3] = c4_multiplier;
        
        // add the data
        for (index=0; index<cData[0].length; index++)
        {
            for (componentCount=0; componentCount<4; componentCount++)
            {
                if (cData [componentCount] [index] == missingDataSample)
                    value = MISSING_DATA_SAMPLE;
                else if (cData [componentCount] [index] == missingComponent)
                    value = MISSING_COMPONENT;
                else
                    value = cData [componentCount] [index].doubleValue() * multipliers [componentCount];
                insertData (componentCount, 
                            startIndex + index, 
                            value, 
                            MISSING_DATA_SAMPLE, 
                            MISSING_COMPONENT);
            }
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }

    /** add data in the form of data arrays - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param missingDataSample value used for missing data (which will be translated)
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent value used for missing component (which will be translated)
     *        if none use MISSING_COMPONENT
     * @param cData data for all components
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         double missingDataSample, double missingComponent,
                         double cData [] [])
    throws GeomagDataException
    {
        int startIndex, index, componentCount;
        
        // sort out the timing details and the data arrays
        if (cData.length != 4)
            throw new GeomagDataException ("Data must have 4 components");
        checkDataSizes (cData[0].length, cData[1].length, cData[2].length, cData[3].length);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (cData[0].length);
        
        // add the data
        for (index=0; index<cData[0].length; index++)
        {
            for (componentCount=0; componentCount<4; componentCount++)
                insertData (componentCount, 
                            startIndex + index, 
                            cData [componentCount] [index], 
                            missingDataSample, 
                            missingComponent);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }

    /** add data in the form of data arrays - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param missingDataSample value used for missing data (which will be translated)
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent value used for missing component (which will be translated)
     *        if none use MISSING_COMPONENT
     * @param c1 data for the first component
     * @param c2 data for the first component
     * @param c3 data for the first component
     * @param c4 data for the first component
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         double missingDataSample, double missingComponent,
                         Collection<Double> c1, Collection<Double> c2, 
                         Collection<Double> c3, Collection<Double> c4)
    throws GeomagDataException
    {
        int startIndex, index, componentCount;
        Iterator<Double> iter1, iter2, iter3, iter4;
        
        // sort out the timing details and the data arrays
        checkDataSizes (c1.size(), c2.size(), c3.size(), c4.size());
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (c1.size());
        
        // add the data
       for (index=startIndex, iter1=c1.iterator(), iter2=c2.iterator(),
                              iter3=c3.iterator(), iter4=c4.iterator();
            iter1.hasNext(); index++)
        {
            insertData (0, index, iter1.next().doubleValue(), missingDataSample, missingComponent);
            insertData (1, index, iter2.next().doubleValue(), missingDataSample, missingComponent);
            insertData (2, index, iter3.next().doubleValue(), missingDataSample, missingComponent);
            insertData (3, index, iter4.next().doubleValue(), missingDataSample, missingComponent);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** add data in the form of GeomagAbsoluteValues - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param c1 array of data
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         GeomagAbsoluteValue cData [])
    throws GeomagDataException
    {
        int startIndex, index, componentCount;
        double missingValue;
        
        // sort out the timing details and the data arrays
        checkDataSizes (cData.length, cData.length, cData.length, cData.length);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (cData.length);
        
        // add the data
       for (index=0; index<cData.length; index++)
       {
            missingValue = cData[index].getMissingDataValue();
            insertData (0, startIndex + index, cData[index].getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (1, startIndex + index, cData[index].getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (2, startIndex + index, cData[index].getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (3, startIndex + index, cData[index].getComponent(GeomagAbsoluteValue.COMPONENT_F_SCALAR, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** add data in the form of GeomagAbsoluteValues - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param cData collection of GeomagAbsoluteValue objects
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         Collection<GeomagAbsoluteValue> cData)
    throws GeomagDataException
    {
        int startIndex, index, componentCount;
        double missingValue;
        GeomagAbsoluteValue value;
        Iterator<GeomagAbsoluteValue> iterator;
        
        // sort out the timing details and the data arrays
        checkDataSizes (cData.size(), cData.size(), cData.size(), cData.size());
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (cData.size());
        
        // add the data
       for (index=0, iterator=cData.iterator(); index<cData.size(); index++)
       {
            value = iterator.next();
            missingValue = value.getMissingDataValue();
            insertData (0, startIndex + index, value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (1, startIndex + index, value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (2, startIndex + index, value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
            insertData (3, startIndex + index, value.getComponent(GeomagAbsoluteValue.COMPONENT_F_SCALAR, GeomagAbsoluteValue.ANGLE_MINUTES), missingValue, missingValue);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** add a number of missing data samples - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param n_samples the number of missing samples to add
     * @throws GeomagDataException if there is an error */
    public void addMissingData (Date startDate, long samplePeriod, int n_samples)
    throws GeomagDataException
    {
        int startIndex, count;
        
        // sort out the timing details and the data arrays
        checkDataSizes (n_samples, n_samples, n_samples, n_samples);
        checkNextSegment (startDate, samplePeriod);
        startIndex = makeNewDataArrays (n_samples);
        
        // add the data
        for (count=0; count<n_samples; count++)
        {
            insertMissingData (0, startIndex + count);
            insertMissingData (1, startIndex + count);
            insertMissingData (2, startIndex + count);
            insertMissingData (3, startIndex + count);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** Get the station code - will never be null */
    public String getStationCode() { return stationCode; }
    
    /** Get the station name - will never be null */
    public String getStationName() { return stationName; }
    
    /** Get the latitude - may be set to MISSING_HEADER_VALUE */
    public double getLatitude() { return latitude; }
    
    /** Get the longitude - may be set to MISSING_HEADER_VALUE */
    public double getLongitude() { return longitude; }
    
    /** Get the elevation - may be set to MISSING_HEADER_VALUE */
    public double getElevation() { return elevation; }

    /** Get the component codes - may be null */
    public String getComponentCodes() { return compCode; }
    
    /** Get the data type description - may be null */
    public String getDataType() { return dataType; }
    
    /** Get the INTERMAGNET GIN code - may be null */
    public String getGINCode() { return ginCode; }
    
    /** Get the institute name - may be null */
    public String getInstituteName () { return instituteName; }

    /** Get the institute name - may be null */
    public String getSamplePeriodString () { return samplePeriodString; }
    
    /** Get the institute name - may be null */
    public String getSensorOrientation () { return sensorOrientation; }

    /** Get the institute name - may be null */
    public String getIntervalType () { return intervalType; }

    /** Get the sample period - may be 0 if no data has been added */
    public long getSamplePeriod() { return samplePeriod; }

    /** get the start date - may be null if not data has been added */
    public Date getStartDate() { return startDate; }
    
    /** get the time associated with a data value
     * @param index the index into the data array (0 to n_samples-1)
     * @returns the date/time */
    public Date getDataSampleDate (int index)
    {
        int n_samples;
        
        n_samples = getDataLength();
        if (index < 0 || index >= n_samples) return null;
        return new Date (startDate.getTime() + ((long) index * samplePeriod));
    }
    
    /** Get the amount of data
     * @return the number of data samples */
    public int getDataLength () { return data [0].length; }
    
    /** get data, without translating missing samples and components
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1 */
    public double getData (int componentIndex, int index)
    {
        return data [componentIndex] [index];
    }

    /** get data, translating missing samples and components
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1
     * @param missingDataSample translate missing values to this value
     *        if none use MISSING_DATA_SAMPLE
     * @param missingComponent translate missing components to this value
     *        if none use MISSING_COMPONENT */
    public double getData (int componentIndex, int index,
                           double missingDataSample, double missingComponent)
    {
        double value;
        
        value = data [componentIndex] [index];
        if (value == MISSING_DATA_SAMPLE) value = missingDataSample;
        else if (value == MISSING_COMPONENT) value = missingComponent;
        return value;
    }
    
    /** Get the number of mean data points - also the number of
     * quiet day flags
     * @return the number of data samples */
    public int getDataMeanLength () { return c1_means.size(); }
    
    /** get mean data, without translating missing values
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1 
     * @return the mean value */
    public double getDataMean (int componentIndex, int index)
    {
        switch (componentIndex)
        {
            case 0: return c1_means.get(index).doubleValue();
            case 1: return c2_means.get(index).doubleValue();
            case 2: return c3_means.get(index).doubleValue();
            case 3: return c4_means.get(index).doubleValue();
        }
        throw new IndexOutOfBoundsException ();
    }

    /** get mean data, translating missing values
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1
     * @param missingDataSample translate missing values to this value
     *        if none use MISSING_DATA_SAMPLE */
    public double getDataMean (int componentIndex, int index,
                               double missingDataSample)
    {
        double value;
        
        value = getDataMean (componentIndex, index);
        if (value == MISSING_DATA_SAMPLE) value = missingDataSample;
        return value;
    }
    
    /** set data mean - for missing values use MISSING_DATA_SAMPLE
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1
     * @param value the value of the mean */
    public void setDataMean (int componentIndex, int index,
                             double value)
    {
        switch (componentIndex)
        {
            case 0: c1_means.set(index, new Double (value)); break;
            case 1: c2_means.set(index, new Double (value)); break;
            case 2: c3_means.set(index, new Double (value)); break;
            case 3: c4_means.set(index, new Double (value)); break;
            default: throw new IndexOutOfBoundsException ();
        }
    }
   
    /** get a quiet day flag 
     * @param index 0..n_data_samples-1 */
    public char getQuietDayFlag (int index)
    {
        return quietDayFlag.get (index).charValue();
    }
    
    /** set a quiet day flag
     * @param index 0..n_data_samples-1
     * @param value the new value */
    public void setQuietDayFlag (int index, char value)
    {
        quietDayFlag.set (index, new Character (value));
    }
    
    /** write to the normal file for the data type
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @throws FileNotFoundException if there was an error creating the file
     * @throws IOException if there was an error writing to the file */
    public void write (String prefix, boolean force_lower_case)
    throws FileNotFoundException, IOException
    {
        write (prefix, force_lower_case, TERM_TYPE_NATIVE);
    }
    
    /** write to the normal file for the data type
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws FileNotFoundException if there was an error creating the file
     * @throws IOException if there was an error writing to the file */
    public void write (String prefix, boolean force_lower_case, byte termType [])
    throws FileNotFoundException, IOException
    {
        String filename;
        FileOutputStream os;
        
        filename = makeFilename (prefix, force_lower_case);
        if (filename == null)
            throw new FileNotFoundException ("Error creating geomagnetic data file name");
        os = new FileOutputStream (filename);
        write (os, termType);
        os.close();
    }
    
    /** write to a specified file
     * @param file the file to write to
     * @throws FileNotFoundException if there was an error creating the file
     * @throws IOException if there was an error writing to the file */
    public void write (File file)
    throws FileNotFoundException, IOException
    {
        write (file, TERM_TYPE_NATIVE);
    }
    
    /** write to a specified file
     * @param file the file to write to
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws FileNotFoundException if there was an error creating the file
     * @throws IOException if there was an error writing to the file */
    public void write (File file, byte termType [])
    throws FileNotFoundException, IOException
    {
        FileOutputStream os;
        
        os = new FileOutputStream (file);
        write (os, termType);
        os.close();
    }
    
    /** write to an output stream
     * @param os the output stream to use
     * @throws IOException if there was an writing to the file */
    public void write (OutputStream os)
    throws IOException
    {
        write (os, TERM_TYPE_NATIVE);
    }
    
    /** all classes must implement a write method that writes to an output stream
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file */
    public abstract void write (OutputStream os, byte termType [])
    throws IOException;
    
    /** all classes must implement a makeFilename method that constructs
     * a filename
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @return the file OR null if there is an error */
    public abstract String makeFilename (String prefix, boolean force_lower_case);
    
    /** calculate a mean from a portion of one of the internal data arrays
     * @param componentIndex 0 to 3
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the average or MISSING_DATA_SAMPLE */
    public double calcMean (int componentIndex, int beginIndex, int endIndex, int maxMissingPercent)
    {
        return calcMean (data [componentIndex], beginIndex, endIndex,
                         MISSING_DATA_SAMPLE, MISSING_COMPONENT, maxMissingPercent);
    }
    
    /** is all the data in this object 'missing'
     * @returns true if no non-null data has been inserted */
    public boolean isAllMissing () { return all_missing; }
    
    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** convert old 2 digit station codes to IAGA codes - expand
     * this table as needed
     * @param oldCode the old 2 digit code
     * @return the IAGA code or null for not found */
    public static String getIaga (String oldCode)
    {
        if (oldCode.equalsIgnoreCase("as")) return "ASC";
        if (oldCode.equalsIgnoreCase("es")) return "ESK";
        if (oldCode.equalsIgnoreCase("ha")) return "HAD";
        if (oldCode.equalsIgnoreCase("le")) return "LER";
        if (oldCode.equalsIgnoreCase("ps")) return "PST";
        if (oldCode.equalsIgnoreCase("sb")) return "SBL";
        return null;
    }
    
    /** write strings to an output stream with the required form
     * of line termination
     * @param os the output stream
     * @param string the string to write
     * @param termType one of the TERM_TYPE_... codes above */
    public static void writeString (OutputStream os, String string, byte termType [])
    throws IOException
    {
        os.write (string.getBytes());
        os.write (termType);
    }
    
    /** calculate a mean from a portion of an array
     * @param array the data to calculate the mean from
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param missingValue value that indicates data point is missing
     * @param missingValue2 another value that indicates data point is missing
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the average or MISSING_DATA_SAMPLE */
    public static double calcMean (double array [], int beginIndex, int endIndex,
                                   double missingValue, double missingValue2,
                                   int maxMissingPercent)
    {
        int n_missing, n_points, n_total, index, missingPercent;
        double sum;
        
        sum = 0.0;
        n_missing = n_points = 0;
        for (index=beginIndex; index<endIndex; index++)
        {
            if (array [index] == missingValue) n_missing ++;
            else if (array [index] == missingValue2) n_missing ++;
            else
            {
                sum += array [index];
                n_points ++;
            }
        }
        n_total = n_missing + n_points;
        if (n_total <= 0) return MISSING_DATA_SAMPLE;
        missingPercent = (n_missing * 100) / n_total;
        if (missingPercent > maxMissingPercent) return MISSING_DATA_SAMPLE;
        return sum / (double) n_points;
    }

    /** calculate a mean from a portion of an array
     * @param array the data to calculate the mean from
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param missingValue value that indicates data point is missing
     * @param missingValue2 another value that indicates data point is missing
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the average or MISSING_DATA_SAMPLE */
    public static double calcMean (Double array [], int beginIndex, int endIndex,
                                   double missingValue, double missingValue2,
                                   int maxMissingPercent)
    {
        int n_missing, n_points, n_total, index, missingPercent;
        double sum;
        
        sum = 0.0;
        n_missing = n_points = 0;
        for (index=beginIndex; index<endIndex; index++)
        {
            if (array[index].doubleValue () == missingValue) n_missing ++;
            else if (array[index].doubleValue () == missingValue2) n_missing ++;
            else
            {
                sum += array[index].doubleValue ();
                n_points ++;
            }
        }
        n_total = n_missing + n_points;
        if (n_total <= 0) return MISSING_DATA_SAMPLE;
        missingPercent = (n_missing * 100) / n_total;
        if (missingPercent > maxMissingPercent) return MISSING_DATA_SAMPLE;
        return sum / (double) n_points;
    }

    /** calculate a mean from a portion of an array of GeomagAbsoluteValue objects
     * @param array the data to calculate the mean from
     * @param component the component to use - one of the GeomagAbsoluteValue.COMPONENT_ codes
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the average or MISSING_DATA_SAMPLE */
    public static double calcMean (GeomagAbsoluteValue array [], int component,
                                   int beginIndex, int endIndex,
                                   int maxMissingPercent)
    {
        int n_missing, n_points, n_total, index, missingPercent;
        double sum, value;
        
        sum = 0.0;
        n_missing = n_points = 0;
        for (index=beginIndex; index<endIndex; index++)
        {
            value = array [index].getComponent(component, GeomagAbsoluteValue.ANGLE_MINUTES);
            if (value == array [index].getMissingDataValue()) n_missing ++;
            else
            {
                sum += value;
                n_points ++;
            }
        }
        n_total = n_missing + n_points;
        if (n_total <= 0) return MISSING_DATA_SAMPLE;
        missingPercent = (n_missing * 100) / n_total;
        if (missingPercent > maxMissingPercent) return MISSING_DATA_SAMPLE;
        return sum / (double) n_points;
    }

    /** check a prefix before passing it to makeFilename
     * @param prefix the prefix to check
     * @param mustExist if true, the prefix must exist as a directory
     *                  if false, the prefix will be created as a directory
     * @param term if true ensure that the prefix is terminated with a
     *             file separation character
     * @returns the possibly modified prefix
     * @throws GeomagDataException if there are any errors */
    public static String checkPrefix (String prefix, boolean mustExist, boolean term)
    throws GeomagDataException
    {
        File dir;
        
        if (prefix == null) return "";
        dir = new File (prefix);
        if (dir.exists())
        {
            if (! dir.isDirectory())
                throw new GeomagDataException ("File already exists (and is not a directory): " + prefix);
            if (term && (! prefix.endsWith (File.separator)))
                prefix += File.separator;
        }
        else
        {
            if (mustExist)
                throw new GeomagDataException ("Directory does not exist: " + prefix);
            else if (! dir.mkdirs())
                throw new GeomagDataException ("Directory could not be created: " + prefix);
            if (term && (! prefix.endsWith (File.separator)))
                prefix += File.separator;
        }
        return prefix;
    }
    
    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// private code below here //////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** check that a set of array lengths are equal
     * @parmas len1 - length of the 1st data array
     * @parmas len2 - length of the 2nd data array
     * @parmas len3 - length of the 3rd data array
     * @parmas len4 - length of the 4th data array
     * @throws GeomagDataException if there is an error */
    private void checkDataSizes (int len1, int len2, int len3, int len4)
    throws GeomagDataException
    {
        if (len1 != len2 || len1 != len3 || len1 != len4)
            throw new GeomagDataException ("Data arrays must be the same length");
        if (blocksize > 0)
        {
            if ((len1 % blocksize) != 0)
                throw new GeomagDataException ("Data must be in blocks of length " + Integer.toString (blocksize));
        }
        
    }
    
    /** check that a given date/sample period forms the next sample
     * from any existing data - if no data has been recorded, sets
     * up the member variables startDate and samplePeriod
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @throws GeomagDataException if there is an error */
    private void checkNextSegment (Date startDate, long samplePeriod)
    throws GeomagDataException
    {
        long objectEnd;
        
        if (samplePeriod <= 0)
            throw new GeomagDataException ("Bad sample period");
        if (this.startDate == null)
        {
            this.startDate = new Date (startDate.getTime());
            this.samplePeriod = samplePeriod;
        }
        else if (this.samplePeriod != samplePeriod)
            throw new GeomagDataException ("Sample periods do not match");
        else
        {
            objectEnd = this.startDate.getTime() + ((long) data[0].length * samplePeriod);
            if (startDate.getTime() != objectEnd)
                throw new GeomagDataException ("Data segment not contiguous");
        }
    }
    
    /** make new data arrays, copy old data (if any) to the new arrays
     * @param addLength the additional length (added to any existing data) 
     * @return the index of the first new sample in the new data arrays */
    private int makeNewDataArrays (int addLength)
    {
        int oldLength, count, componentCount, retVal;
        double oldData [];
        
        if (data [0] == null) retVal = 0;
        else retVal = data [0].length;
        for (componentCount=0; componentCount<4; componentCount++)
        {
            if (data [componentCount] == null) oldLength = 0;
            else oldLength = data [componentCount].length;
            oldData = data [componentCount];
            data [componentCount] = new double [oldLength + addLength];
            for (count=0; count<oldLength; count++)
                data [componentCount] [count] = oldData [count];
        }
        return retVal;
    }
    
    /** put a data sample into one of the arrays
     * @param componentIndex the component (0-3)
     * @param index the array counter
     * @param value the data to insert
     * @param missingDataValue value to translate for missing data value
     * @param missingComponent value to translate for missing component */
    private void insertData (int componentIndex, int index, double value,
                             double missingDataSample, double missingComponent)
    {    
        if (value == missingDataSample)
            data [componentIndex] [index] = MISSING_DATA_SAMPLE;
        else if (value == missingComponent)
            data [componentIndex] [index] = MISSING_COMPONENT;
        else
        {
            all_missing = false;
            data [componentIndex] [index] = value;
        }
    }
    
    /** put a missing data sample into one of the arrays
     * @param componentIndex the component (0-3)
     * @param index the array counter */
    private void insertMissingData (int componentIndex, int index)
    {    
        data [componentIndex] [index] = MISSING_DATA_SAMPLE;
    }
    
    /** generate means for new blocks of data */
    private void addMeans ()
    {
        int n_total_means, n_current_means, mean_count;
        int sample_start, sample_end;
        
        // work out how many means need to be added - to find this:
        // 1.) get the number of raw data samples and divide by the blocksize
        //     this is the total number of means thre should be
        // 2.) get the number of means we currently have from one of the arrays
        // 3.) take the difference
        n_total_means = this.getDataLength() / blocksize;
        n_current_means = c1_means.size();

        // for each mean ...
        for (mean_count=n_current_means; mean_count<n_total_means; mean_count++)
        {
            // get the indices for the raw data
            sample_start = mean_count * blocksize;
            sample_end = sample_start + blocksize;
            
            // calculate and store the means
            c1_means.add (calcMean(0, sample_start, sample_end, 10));
            c2_means.add (calcMean(1, sample_start, sample_end, 10));
            c3_means.add (calcMean(2, sample_start, sample_end, 10));
            c4_means.add (calcMean(3, sample_start, sample_end, 10));
            
            // set an empty quiet day flag
            quietDayFlag.add (new Character (' '));
        }
    }
    
}
