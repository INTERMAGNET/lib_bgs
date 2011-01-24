/**
 * DataFormat.java
 *
 * Created on 19 March 2004, 11:28
 */
package bgs.geophys.library.Data;

import java.util.*;
import java.io.*;
import bgs.geophys.library.Misc.PreciseDecimal;

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
public abstract class GeomagDataFormat 
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
    private long samplePeriod;               // Time between samples, in milliseconds
    private Date startDate;                  // Date/time of first sample
    private ArrayList<double []> sampleData; // arrays of 4 component data
                                             // in nT or minutes or arc
    private boolean all_missing;             // true until some non-missing data is added
    
    // these members hold additional data - means for each of the components
    // and a quiet day flag - these variables are only valid where blocksize is
    // not negative - once blocksize is set, data must be inserted in blocks that size
    private int blocksize;                   // the size of blocks used to calculate means
    private ArrayList<double []> meanData;   // arrays of 4 component data
                                             // in nT or minutes or arc
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
    public GeomagDataFormat (String stationCode, String stationName,
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
            // intermagnet has a 'G' for the fourth component, which needs to be put
            // back to an F
            if (this.compCode.length()==4 && this.compCode.substring(3).contentEquals("G")){
                this.compCode = this.compCode.substring(0,3).concat("F");
            }
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
        sampleData = new ArrayList<double []> ();
        
        // set up the extra data
        this.blocksize = blocksize;
        meanData = new ArrayList<double []> ();
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
        // sort out the timing details and the data arrays
        checkDataSizes (1, 1, 1, 1);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
        addData (c1, c2, c3, c4, missingDataSample, missingComponent);
        
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
        int index;
        
        // sort out the timing details and the data arrays
        checkDataSizes (c1.length, c2.length, c3.length, c4.length);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
        for (index=0; index<c1.length; index++)
            addData (c1 [index], c2 [index], c3 [index], c4 [index],
                     missingDataSample, missingComponent);
        
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
        int index;
        
        // sort out the timing details and the data arrays
        if (cData.length != 4)
            throw new GeomagDataException ("Data must have 4 components");
        checkDataSizes (cData[0].length, cData[1].length, cData[2].length, cData[3].length);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
        for (index=0; index<cData[0].length; index++)
        {
            addData (cData [0] [index].intValue (), cData [1] [index].intValue (),
                     cData [2] [index].intValue (), cData [3] [index].intValue (),
                     c1_multiplier, c2_multiplier, c3_multiplier, c4_multiplier,
                     missingDataSample, missingComponent);
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
        int index;
        
        // sort out the timing details and the data arrays
        if (cData.length != 4)
            throw new GeomagDataException ("Data must have 4 components");
        checkDataSizes (cData[0].length, cData[1].length, cData[2].length, cData[3].length);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
        for (index=0; index<cData[0].length; index++)
        {
            addData (cData [0] [index], cData [1] [index],
                     cData [2] [index], cData [3] [index], 
                     missingDataSample, missingComponent);
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
        int index;
        Iterator<Double> iter1, iter2, iter3, iter4;
        
        // sort out the timing details and the data arrays
        checkDataSizes (c1.size(), c2.size(), c3.size(), c4.size());
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
       for (iter1=c1.iterator(), iter2=c2.iterator(), iter3=c3.iterator(), iter4=c4.iterator();
            iter1.hasNext();)
        {
            addData (iter1.next().doubleValue(), iter2.next().doubleValue(), 
                     iter3.next().doubleValue(), iter4.next().doubleValue(), 
                     missingDataSample, missingComponent);
        }
        
        // calculate and store means
        if (blocksize > 0) addMeans ();
    }
    
    /** add data in the form of GeomagAbsoluteValues - data may only be added,
     * not taken away - if the startDate/samplePeriod in subsequent
     * calls does not match the existing data, an exception is thrown
     * @param startDate the start date for the data
     * @param samplePeriod the period between samples, in milliseconds 
     * @param fourthElement what to insert in the fourth element - one of
     *        GeomagAbsoluteValue.COMPONENT_F, GeomagAbsoluteValue.COMPONENT_F_SCALAR,
     *        GeomagAbsoluteValue.COMPONENT_F_DIFF
     * @param c1 array of data
     * @throws GeomagDataException if there is an error */
    public void addData (Date startDate, long samplePeriod,
                         int fourthElement,
                         GeomagAbsoluteValue cData [])
    throws GeomagDataException
    {
        int index;
        GeomagAbsoluteValue value;
        
        // sort out the timing details and the data arrays
        checkDataSizes (cData.length, cData.length, cData.length, cData.length);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
       for (index=0; index<cData.length; index++)
       {
            value = cData[index];
            addData (value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(fourthElement, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getMissingDataValue(),
                     value.getMissingDataValue());
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
        addData (startDate, samplePeriod, GeomagAbsoluteValue.COMPONENT_F_SCALAR, cData);
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
        int index;
        GeomagAbsoluteValue value;
        Iterator<GeomagAbsoluteValue> iterator;
        
        // sort out the timing details and the data arrays
        checkDataSizes (cData.size(), cData.size(), cData.size(), cData.size());
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
       for (index=0, iterator=cData.iterator(); index<cData.size(); index++)
       {
            value = iterator.next();
            addData (value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getComponent(GeomagAbsoluteValue.COMPONENT_F_SCALAR, GeomagAbsoluteValue.ANGLE_MINUTES),
                     value.getMissingDataValue(),
                     value.getMissingDataValue());
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
        int count;
        
        // sort out the timing details and the data arrays
        checkDataSizes (n_samples, n_samples, n_samples, n_samples);
        checkNextSegment (startDate, samplePeriod);
        
        // add the data
        for (count=0; count<n_samples; count++)
        {
            addMissingData();
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
    public int getDataLength () { return sampleData.size(); }
    
    /** get data, without translating missing samples and components
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1 */
    public double getData (int componentIndex, int index)
    {
        double [] data;
        
        data = sampleData.get (index);
        return data [componentIndex];
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
        double [] data;
        
        data = sampleData.get (index);
        value = data [componentIndex];
        if (value == MISSING_DATA_SAMPLE) value = missingDataSample;
        else if (value == MISSING_COMPONENT) value = missingComponent;
        return value;
    }
    
    /** calculate a set of means from the raw data
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the averages or MISSING_DATA_SAMPLE */
    public double [] calcMean (int maxMissingPercent)
    {
        return calcMean (0, getDataLength(), maxMissingPercent);
    }
    
    /** calculate a set of means from a portion of the raw data
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the averages or MISSING_DATA_SAMPLE */
    public double [] calcMean (int beginIndex, int endIndex, int maxMissingPercent)
    {
        int n_missing [], n_total, index, missingPercent, componentCount;
        PreciseDecimal sum[], data[];
        
        sum = new PreciseDecimal [4];
        n_missing = new int [4];
        sum [0] = sum [1] = sum [2] = sum [3] = PreciseDecimal.ZERO;
        n_missing [0] = n_missing [1] = n_missing [2] = n_missing [3] = 0;
        n_total = endIndex - beginIndex;
        
        for (index=beginIndex; index<endIndex; index++)
        {
            data = PreciseDecimal.arrayFromDouble(sampleData.get (index));
            for (componentCount = 0; componentCount < 4; componentCount ++)
            {
                if (data [componentCount].compareTo(new PreciseDecimal(MISSING_DATA_SAMPLE)) == 0)
                    n_missing [componentCount] ++;
                else if (data [componentCount].compareTo(new PreciseDecimal(MISSING_COMPONENT)) == 0)
                    n_missing [componentCount] ++;
                else 
                    sum [componentCount] = PreciseDecimal.add(sum [componentCount], data [componentCount]);
            }
        }
        
        for (componentCount = 0; componentCount < 4; componentCount ++)
        {
            if (n_total <= 0) 
                sum [componentCount] = new PreciseDecimal(MISSING_DATA_SAMPLE);
            else
            {
                missingPercent = (n_missing [componentCount] * 100) / n_total;
                if (missingPercent > maxMissingPercent) sum [componentCount] = new PreciseDecimal(MISSING_DATA_SAMPLE);
                else sum [componentCount] = PreciseDecimal.divide(sum [componentCount], (double) n_total - n_missing [componentCount]);
            }
        }
        
        return PreciseDecimal.arrayToDouble(sum);
    }
    
    /** calculate a set of medians from the raw data
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the averages or MISSING_DATA_SAMPLE */
    public double [] calcMedian (int maxMissingPercent)
    {
        return calcMedian (0, getDataLength(), maxMissingPercent);
    }
    
    /** calculate a set of medians from a portion of the raw data
     * @param beginIndex the index of the first sample
     * @param endIndex the index of the point after the last sample
     * @param maxMissingPercent if there are more missing data points than this
     *        percentage, the routine will return missingValue
     * @return the averages or MISSING_DATA_SAMPLE */
    public double [] calcMedian (int beginIndex, int endIndex, int maxMissingPercent)
    {
        int n_missing [], n_total, index, missingPercent, componentCount;
        double min [], max [], data [], median [];
        
        min = new double [4];
        max = new double [4];
        median = new double [4];
        n_missing = new int [4];
        min [0] = min [1] = min [2] = min [3] = Double.MAX_VALUE;
        max [0] = max [1] = max [2] = max [3] = - Double.MAX_VALUE;
        n_missing [0] = n_missing [1] = n_missing [2] = n_missing [3] = 0;
        n_total = endIndex - beginIndex;
        
        for (index=beginIndex; index<endIndex; index++) 
        {
            data = sampleData.get (index);
            for (componentCount = 0; componentCount < 4; componentCount ++)
            {
                if (data [componentCount] == MISSING_DATA_SAMPLE)
                    n_missing [componentCount] ++;
                else if (data [componentCount] == MISSING_COMPONENT)
                    n_missing [componentCount] ++;
                else 
                {
                    if (data [componentCount] > max [componentCount])
                        max [componentCount] = data [componentCount];
                    if (data [componentCount] < min [componentCount])
                        min [componentCount] = data [componentCount];
                }
            }
        }
        
        for (componentCount = 0; componentCount < 4; componentCount ++)
        {
            if (n_total <= 0)
                median [componentCount] = MISSING_DATA_SAMPLE;
            else
            {
                missingPercent = (n_missing [componentCount] * 100) / (n_total+1); // total points is one extra JE 20.02.11
                if (missingPercent > maxMissingPercent) 
                    median [componentCount] = MISSING_DATA_SAMPLE;
                else 
                    median [componentCount] = min [componentCount] +
                                              ((max [componentCount] - min [componentCount]) / 2.0);
            }
        }
        
        return median;
    }
    
    /** Get the number of mean data points - also the number of
     * quiet day flags
     * @return the number of data samples */
    public int getDataMeanLength () { return meanData.size(); }
    
    /** get mean data, without translating missing values
     * @param componentIndex the component number (0-3)
     * @param index 0..n_data_samples-1 
     * @return the mean value */
    public double getDataMean (int componentIndex, int index)
    {
        double data [];

        data = meanData.get (index);
        return data [componentIndex];
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
        double data [];

        data = meanData.get (index);
        data [componentIndex] = value;
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
    
    public enum IntervalType {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        MONTH,
        BAD_INTERVAL
    }
    
    public IntervalType getIntervalTypeEnum() {
        long samplePeriod = getSamplePeriod();
        if ( samplePeriod <= 0 ) return IntervalType.BAD_INTERVAL;
        if ( samplePeriod < 60000 ) return IntervalType.SECOND;
        if ( samplePeriod < 3600000 ) return IntervalType.MINUTE;
        if ( samplePeriod < 86400000 ) return IntervalType.HOUR;
        if ( samplePeriod < 2419200000L ) return IntervalType.DAY;
        return IntervalType.MONTH;
    }
    
    public enum DurationType {
        FRAGMENT_OR_UNKNOWN, SECOND, MINUTE, HOUR, DAY, MONTH, YEAR
    }
    
    /** Get the duration of the data present
     *  @returns the duration in milliseconds
     */
    public long getDuration() {
        return getSamplePeriod() * getDataLength();
    }
    
    /** Get the duration of the data present, represented as a calendar
     *  duration of type DurationType.  If the duration is not exactly
     *  a second, minute, hour, day, month or year, the duration returned
     *  will be FRAGMENT_OR_UNKNOWN
     *  @returns the calendar duration as a DurationType 
     */
    public DurationType getDurationType() {
        
        // Create a new calendar from the start date of the dataset
        GregorianCalendar startCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        startCal.setTime(getStartDate());
        
        // Compute the end date of the data set
        GregorianCalendar duration = (GregorianCalendar) startCal.clone();
        duration.add(Calendar.MILLISECOND, (int) getDuration());
        
        // Compute the end date were the duration one second
        GregorianCalendar second = (GregorianCalendar) startCal.clone();
        second.add(Calendar.SECOND, 1);
        
        // Compute the end date were the duration one minute
        GregorianCalendar minute = (GregorianCalendar) startCal.clone();
        minute.add(Calendar.MINUTE, 1);
        
        // Compute the end date were the duraton one hour
        GregorianCalendar hour = (GregorianCalendar) startCal.clone();
        hour.add(Calendar.HOUR, 1);
        
        // Compute the end date were the duration one day
        GregorianCalendar day = (GregorianCalendar) startCal.clone();
        day.add(Calendar.DAY_OF_YEAR, 1);
        
        // Compute the end date were the duration one month
        GregorianCalendar month = (GregorianCalendar) startCal.clone();
        month.add(Calendar.MONTH, 1);
        
        // Compute the end date were the duration one year
        GregorianCalendar year = (GregorianCalendar) startCal.clone();
        year.add(Calendar.YEAR, 1);
        
        // Compare with the actual duration to determine the value to return
        if ( duration.compareTo(second) == 0 ) return DurationType.SECOND;
        else if ( duration.compareTo(minute) == 0 ) return DurationType.MINUTE;
        else if ( duration.compareTo(hour) == 0 ) return DurationType.HOUR;
        else if ( duration.compareTo(day) == 0 ) return DurationType.DAY;
        else if ( duration.compareTo(month) == 0 ) return DurationType.MONTH;
        else if ( duration.compareTo(year) == 0 ) return DurationType.YEAR;
        else return DurationType.FRAGMENT_OR_UNKNOWN;
        
    }
    
    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// private code below here //////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** write strings to an output stream with the required form
     * of line termination
     * @param os the output stream
     * @param string the string to write
     * @param termType one of the TERM_TYPE_... codes above */
    protected static void writeString (OutputStream os, String string, byte termType [])
    throws IOException
    {
        os.write (string.getBytes());
        os.write (termType);
    }
    
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
            objectEnd = this.startDate.getTime() + ((long) sampleData.size() * samplePeriod);
            if (startDate.getTime() != objectEnd)
                throw new GeomagDataException ("Data segment not contiguous");
        }
    }
    
    /** add a data sample into one of the arrays, translating missing data codes
     * @param c1 the 1st component of the data to insert
     * @param c2 the 2nd component of the data to insert
     * @param c3 the 3rd component of the data to insert
     * @param c4 the 4th component of the data to insert
     * @param missingDataValue value to translate for missing data value
     * @param missingComponent value to translate for missing component */
    private void addData (double c1, double c2,
                          double c3, double c4,
                          double missingDataSample, double missingComponent)
    {    
        int count;
        double data [];
        
        // insert data into storage format
        data = new double [4];
        data [0] = c1;
        data [1] = c2;
        data [2] = c3;
        data [3] = c4;
        
        // translate missing sample codes
        for (count=0; count<4; count++)
        {
            if (data [count] == missingDataSample)
                data [count] = MISSING_DATA_SAMPLE;
            else if (data [count] == missingComponent)
                data [count] = MISSING_COMPONENT;
            else
                all_missing = false;
        }
        
        // insert the data
        sampleData.add (data);
    }
    
    /** add a data sample into one of the arrays, translating missing data codes
     * @param c1 the 1st component of the data to insert
     * @param c2 the 2nd component of the data to insert
     * @param c3 the 3rd component of the data to insert
     * @param c4 the 4th component of the data to insert
     * @param c1_multiplier amount to multiple c1 by
     * @param c2_multiplier amount to multiple c2 by
     * @param c3_multiplier amount to multiple c3 by
     * @param c4_multiplier amount to multiple c4 by
     * @param missingDataValue value to translate for missing data value
     * @param missingComponent value to translate for missing component */
    private void addData (int c1, int c2,
                          int c3, int c4,
                          double c1_multiplier, double c2_multiplier,
                          double c3_multiplier, double c4_multiplier,
                          int missingDataSample, int missingComponent)
    {    
        int count, int_data [];
        double data [];
        
        // insert data into storage format
        int_data = new int [4];
        int_data [0] = c1;
        int_data [1] = c2;
        int_data [2] = c3;
        int_data [3] = c4;
        data = new double [4];
        
        // translate missing sample codes
        for (count=0; count<4; count++)
        {
            if (int_data [count] == missingDataSample)
                data [count] = MISSING_DATA_SAMPLE;
            else if (int_data [count] == missingComponent)
                data [count] = MISSING_COMPONENT;
            else
            {
                data [count] = (double) int_data [count] * c1_multiplier;
                all_missing = false;
            }
        }
        
        // insert the data
        sampleData.add (data);
    }
    
    /** put a missing data sample into one of the arrays */
    private void addMissingData ()
    {    
        double data [];
        
        data = new double [4];
        data [0] = MISSING_DATA_SAMPLE;
        data [1] = MISSING_DATA_SAMPLE;
        data [2] = MISSING_DATA_SAMPLE;
        data [3] = MISSING_DATA_SAMPLE;
        sampleData.add (data);
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
        n_current_means = meanData.size();

        // for each mean ...
        for (mean_count=n_current_means; mean_count<n_total_means; mean_count++)
        {
            // get the indices for the raw data
            sample_start = mean_count * blocksize;
            sample_end = sample_start + blocksize;
            
            // calculate and store the means
            meanData.add (calcMean (sample_start, sample_end, 10));
            
            // set an empty quiet day flag
            quietDayFlag.add (new Character (' '));
        }
    }

}
