/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDF 
{

    /** constant value used to indicate missing data */
    public static final double MISSING_DATA_VALUE = 99999.0;
    
    // private member data - the CDF global attributes
    private String title;
    private String format_description;
    private String format_version;
    private String terms_of_use;
    private String institution;
    private String source;
    private String history;
    private String references;
    private String observatory_name;
    private String iaga_code;
    private double latitude;
    private double longitude;
    private double elevation;
    private String vector_sens_orient;
    private IMCDFBaselineType baseline_type;
    private IMCDFPublicationState pub_state;
    private IMCDFStandardsConformance standards_conformance;
    private Date pub_date;

    // private member data - the field element and temperature data arrays
    private ImagCDFVariable temperatures [];
    private ImagCDFVariable elements [];
    
    /** read an ImagCDF file
     * @param file the CDF file
     * @throws CDFException if there is an error */
    public ImagCDF (File file)
    throws CDFException, ParseException
    {
        int count, n_elements, n_temperatures;
        String string;
        ImagCDFLowLevel cdf;
        IMCDFVariableType field_var_type, temperature_var_type;
        
        // check that the CDF libraries are available
        string = ImagCDFLowLevel.checkNativeLib("");
        if (string != null) throw new CDFException (string);
        
        // open the CDF file
        cdf = new ImagCDFLowLevel (file, ImagCDFLowLevel.CDFOpenType.CDFOpen, ImagCDFLowLevel.CDFCompressType.None);

        // find the number of geomagnetic field elements and the number of temperature
        // arrays in the file and set up the array objcts to receive them
        field_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement);
        n_elements = 0;
        while (cdf.isVariableExist (field_var_type.getCDFFileVariableName(n_elements))) n_elements ++;
        temperature_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.Temperature);
        n_temperatures = 0;
        while (cdf.isVariableExist (temperature_var_type.getCDFFileVariableName(n_temperatures))) n_temperatures ++;
        elements = new ImagCDFVariable [n_elements];
        temperatures = new ImagCDFVariable [n_temperatures];
        
        // get global metadata
        title =                                               cdf.getGlobalAttributeString("Title");
        format_description =                                  cdf.getGlobalAttributeString("FormatDescription");
        format_version =                                      cdf.getGlobalAttributeString("FormatVersion");
        terms_of_use =                                        cdf.getGlobalAttributeString("TermsOfUse");
        institution =                                         cdf.getGlobalAttributeString("Institution");
        source =                                              cdf.getGlobalAttributeString("Source");
        history =                                             cdf.getGlobalAttributeString("History");
        references =                                          cdf.getGlobalAttributeString("References");
        observatory_name =                                    cdf.getGlobalAttributeString("ObservatoryName");
        iaga_code =                                           cdf.getGlobalAttributeString("IagaCode");
        latitude =                                            cdf.getGlobalAttributeDouble("Latitude");
        longitude =                                           cdf.getGlobalAttributeDouble("Longitude");
        elevation =                                           cdf.getGlobalAttributeDouble("Elevation");
        vector_sens_orient =                                  cdf.getGlobalAttributeString("VectorSensOrient");
        baseline_type =                 new IMCDFBaselineType (cdf.getGlobalAttributeString("BaselineType"));
        pub_state =                 new IMCDFPublicationState (cdf.getGlobalAttributeString("PublicationState"));
        standards_conformance = new IMCDFStandardsConformance (cdf.getGlobalAttributeString("StandardsConformance"));
        pub_date =                 ImagCDFLowLevel.parseDate (cdf.getGlobalAttributeString("PublicationDate"));

        // get variables and their metadata
        for (count=0; count<n_elements; count++)
            elements [count] = new ImagCDFVariable(cdf, field_var_type, count);
        for (count=0; count<n_temperatures; count++)
            temperatures [count] = new ImagCDFVariable(cdf, temperature_var_type, count);

        // check data and metadata
        if (! title.equalsIgnoreCase             ("Geomagnetic observatory data")) throw new CDFException ("Format Error");
        if (! format_description.equalsIgnoreCase("INTERMAGNET CDF Format"))       throw new CDFException ("Format Error");
        if (! format_version.equalsIgnoreCase    ("1.0"))                          throw new CDFException ("Format Error");
        checkDataElements();
    }

    /** create an ImagCDF object from data and metadata (prior to writing to a file)
     * @param institution name of the institution
     * @param source set to one of "institute", "intermagnet" or "wdc"
     * @param history description of the history of the data
     * @param references URL of relevance, e.g. www.intermagnet.org
     * @param observatory_name Full name of the observatory
     * @param iaga_code IAGA code of the observatory
     * @param latitude Geographic latitude of the observing position
     * @param longitude Geographic longitude of the observing position
     * @param elevation Height of the observing position above sea level
     * @param vector_sens_orient the orientation of the vector sensor (which may differ from that of the elements reported in the data)
     * @param baseline_type the type of baseline that has been applied to the data
     * @param pub_state the amount of editing that has been done to the data
     * @param standards_conformance describes any standards that the data conforms to
     * @param pub_date the date the data was published
     * @param elements the geomagnetic data
     * @param temperatures corresponding temperature data (may be null)
     * @throws CDFException if the vector data elements don't have the same sample period or start date */
    public ImagCDF (String institution, String source, String history, String references,
                    String observatory_name, String iaga_code, double latitude,
                    double longitude, double elevation, String vector_sens_orient,
                    IMCDFBaselineType baseline_type, IMCDFPublicationState pub_state,
                    IMCDFStandardsConformance standards_conformance,
                    Date pub_date, 
                    ImagCDFVariable elements [], ImagCDFVariable temperatures [])
    throws CDFException
    {
        // constant metadata fields
        this.title = "Geomagnetic observatory data";
        this.format_description = "INTERMAGNET CDF Format";
        this.format_version = "1.0";
        this.terms_of_use = getINTERMAGNETTermsOfUse();
        
        // global metadata
        this.institution = institution;
        this.source = source;
        this.history = history;
        this.references = references;
        this.observatory_name = observatory_name;
        this.iaga_code = iaga_code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.vector_sens_orient = vector_sens_orient;
        this.baseline_type = baseline_type;
        this.pub_state = pub_state;
        this.standards_conformance = standards_conformance;
        this.pub_date = pub_date;
        
        // data arrays
        this.elements = elements;
        if (temperatures == null) this.temperatures = new ImagCDFVariable [0];
        else this.temperatures = temperatures;
        
        // check metadata and data
        checkDataElements();
    }

    /** write this data to a CDF file
     * @param cdf_file the CDF file to write into
     * @param compress true to compress the CDF file, FALSE not to compress
     * @param overwrite_existing true to overwrite any existing file, false to throw exception if file exists
     * @throws CDFException if there is an error */
    public void write (File cdf_file, boolean compress, boolean overwrite_existing)
    throws CDFException
    {
        int count;
        ImagCDFLowLevel cdf;
        
        cdf = new ImagCDFLowLevel (cdf_file, 
                                   overwrite_existing ? ImagCDFLowLevel.CDFOpenType.CDFForceCreate : ImagCDFLowLevel.CDFOpenType.CDFCreate,
                                   compress ? ImagCDFLowLevel.CDFCompressType.GZip6 : ImagCDFLowLevel.CDFCompressType.None);
        
        cdf.addGlobalAttribute ("Title",                checkForNull (title));
        cdf.addGlobalAttribute ("FormatDescription",    checkForNull (format_description));
        cdf.addGlobalAttribute ("FormatVersion",        checkForNull (format_version));
        cdf.addGlobalAttribute ("TermsOfUse",           checkForNull (terms_of_use));
        cdf.addGlobalAttribute ("Institution",          checkForNull (institution));
        cdf.addGlobalAttribute ("Source",               checkForNull (source));
        cdf.addGlobalAttribute ("History",              checkForNull (history));
        cdf.addGlobalAttribute ("References",           checkForNull (references));
        cdf.addGlobalAttribute ("ObservatoryName",      checkForNull (observatory_name));
        cdf.addGlobalAttribute ("IagaCode",             checkForNull (iaga_code));
        cdf.addGlobalAttribute ("Latitude",             new Double (latitude));
        cdf.addGlobalAttribute ("Longitude",            new Double (longitude));
        cdf.addGlobalAttribute ("Elevation",            new Double (elevation));
        cdf.addGlobalAttribute ("VectorSensOrient",     checkForNull (vector_sens_orient));
        cdf.addGlobalAttribute ("BaselineType",         baseline_type.toString());
        cdf.addGlobalAttribute ("PublicationState",     pub_state.toString());
        cdf.addGlobalAttribute ("StandardsConformance", standards_conformance.toString());
        cdf.addGlobalAttribute ("PublicationDate",      ImagCDFLowLevel.formatDate(pub_date));
        
        for (count=0; count<elements.length; count++)
            elements[count].write(cdf, count);
        for (count=0; count<temperatures.length; count++)
            temperatures[count].write(cdf, count);

        cdf.close ();
    }
    
    public String getTitle() { return title; }
    public String getFormatDescription() { return format_description; }
    public String getFormatVersion() { return format_version; }
    public String getTermsOfUse() { return terms_of_use; }
    public String getInstitution() { return institution; }
    public String getSource() { return source; }
    public String getHistory() { return history; }
    public String getReferences() { return references; }
    public String getObservatoryName() { return observatory_name; }
    public String getIagaCode() { return iaga_code; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getElevation() { return elevation; }
    public String getVectorSensorOrientation() { return vector_sens_orient; }
    public IMCDFBaselineType getBaselineType() {  return baseline_type; }
    public IMCDFPublicationState getPublicationState() { return pub_state; }
    public IMCDFStandardsConformance getStandardsConformance() { return standards_conformance; }
    public Date getPublicationDate() { return pub_date; }

    public int getNElements () { return elements.length; }
    public ImagCDFVariable getElement (int index) { return elements [index]; }
    
    /** finds the index of the first three vector elements
     * @return an array containing indices of elements, or null if there are less than thee vector elements  */
    public int [] findVectorElements ()
    {
        int count, indexes [], found_count;

        indexes = new int [3];
        for (count=found_count=0; count<elements.length; count++)
        {
            if (elements[count].isVectorGeomagneticData())
            {
                indexes [found_count ++] = count;
                if (found_count == indexes.length) return indexes;
            }
        }
        return null;
    }
    
    /** find the first scalar element
     * @return the element index or a -ve number if there is no scalar element in the file */
    public int findScalarElement ()
    {
        int count;

        for (count=0; count<elements.length; count++)
        {
            if (elements[count].isScalarGeomagneticData())
                return count;
        }
        return -1;
    }
    
    /** get the element code string corresponding to the given vector and scalar elements
     * @param vector_indices the indexes to the vector elements
     * @param scalar_index  the index to the scalar element OR -ve for no scalar index
     * @return the concatenated element code */
    public String getElementCodes (int vector_indices [], int scalar_index)
    {
        String codes;
        int count;

        codes = "";
        for (count=0; count<vector_indices.length; count++)
            codes += elements[vector_indices[count]].getElementRecorded();
        if (scalar_index >= 0)
            codes += elements [scalar_index].getElementRecorded();
        return codes;
    }
    
    public int getNTemperatures () { return temperatures.length; }
    public ImagCDFVariable getTemperature (int index) { return temperatures [index]; }
    
    /** generate an IMAG CDF filename 
     * @param prefix the prefix for the name (including any directory)
     * @param station_code the IAGA station code
     * @param start_date the start date for the data
     * @param pub_state the publication state code
     * @param baseline_type the baseline type code
     * @param sample_period the period between samples, in seconds
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the file OR null if there is an error */
    public static String makeFilename (String prefix,
                                       String station_code, Date start_date,
                                       IMCDFPublicationState pub_state,
                                       IMCDFBaselineType baseline_type, 
                                       double sample_period,
                                       boolean force_lower_case)
    {
        String filename, file_period;
        SimpleDateFormat date_format;


        // set up the parts of the filename that depend on the sample period
        if (sample_period <= 1.0)               // second data
        {
            date_format = new SimpleDateFormat ("yyyyMMdd");
            file_period = "sec";
        }
        else if (sample_period <= 60.0)         // minute data
        {
            date_format = new SimpleDateFormat ("yyyyMMdd");
            file_period = "min";
        }
        else if (sample_period <= 3600.0)       // hour data
        {
            date_format = new SimpleDateFormat ("yyyyMM");
            file_period = "hor";
        }
        else if (sample_period <= 86400.0)      // day data
        {
            date_format = new SimpleDateFormat ("yyyy");
            file_period = "day";
        }
        else                                     // month data
        {
            date_format = new SimpleDateFormat ("yyyy");
            file_period = "mon";
        }
        date_format.setTimeZone(TimeZone.getTimeZone("gmt"));

        filename = station_code + date_format.format (start_date) + 
                   pub_state.toString (false) + baseline_type.toString(false) +
                   file_period + ".cdf";
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }

    public static String getINTERMAGNETTermsOfUse ()
    {
        return "CONDITIONS OF USE FOR DATA PROVIDED THROUGH INTERMAGNET:\n" +
               "The data made available through INTERMAGNET are provided for\n" +
               "your use and are not for commercial use or sale or distribution\n" +
               "to third parties without the written permission of the institute\n" +
               "(http://www.intermagnet.org/Institutes_e.html) operating\n" +
               "the observatory. Publications making use of the data\n" +
               "should include an acknowledgment statement of the form given below.\n" +
               "A citation reference should be sent to the INTERMAGNET Secretary\n" +
               "(secretary@intermagnet.org) for inclusion in a publications list\n" +
               "on the INTERMAGNET website.\n" +
               "\n" +
               "     ACKNOWLEDGEMENT OF DATA FROM OBSERVATORIES\n" +
               "     PARTICIPATING IN INTERMAGNET\n" +
               "We offer two acknowledgement templates. The first is for cases\n" +
               "where data from many observatories have been used and it is not\n" + 
               "practical to list them all, or each of their operating institutes.\n" + 
               "The second is for cases where research results have been produced\n" + 
               "using a smaller set of observatories.\n" +
               "\n" +
               "     Suggested Acknowledgement Text (template 1)\n" +
               "The results presented in this paper rely on data collected\n" + 
               "at magnetic observatories. We thank the national institutes that\n" + 
               "support them and INTERMAGNET for promoting high standards of\n" + 
               "magnetic observatory practice (www.intermagnet.org).\n" +
               "\n" +
               "     Suggested Acknowledgement Text (template 2)\n" +
               "The results presented in this paper rely on the data\n" + 
               "collected at <observatory name>. We thank <institute name>,\n" + 
               "for supporting its operation and INTERMAGNET for promoting high\n" + 
               "standards of magnetic observatory practice (www.intermagnet.org).\n";
    }

    
    private String checkForNull (String str)
    {
        if (str == null) str = "Unknown";
        else if (str.length() <= 0) str = "Unknown";
        return str;
    }

    private void checkDataElements ()
    throws CDFException
    {
        int count;
        double sample_period;
        Date start_date;
        
        start_date = null;
        sample_period = -1;
        for (count=0; count<elements.length; count++)
        {
            if (elements[count].isVectorGeomagneticData())
            {
                // check start date is the same across all vector elements
                if (start_date == null) 
                    start_date = elements[count].getStartDate();
                else if (! elements[count].getStartDate().equals(start_date))
                    throw new CDFException ("Start date differs between geomagnetic vector elements");
                        
                // check sample rate is the same across all vector elements
                if (sample_period < 0.0) 
                    sample_period = elements[count].getSamplePeriod();
                else if (sample_period != elements[count].getSamplePeriod())
                    throw new CDFException ("Sample period differs between geomagnetic vector elements");
            }
        }
        
    }
}
