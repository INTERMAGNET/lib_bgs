/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import bgs.geophys.library.Threads.ProcessMonitor;
import gsfc.nssdc.cdf.CDFException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author smf
 */
public class ImagCDFIT {

    private static final String TEST_IAGA_CODE = "ABC";
    private static final IMCDFPublicationLevel TEST_PUB_LEVEL = new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);
    private static final Date TEST_PUB_DATE = new Date (1419120000000l);
    private static final String TEST_OBSERVATORY_NAME = "Test Observatory";
    private static final double TEST_LATITUDE = 53.5;
    private static final double TEST_LONGITUDE = 179.4;
    private static final double TEST_ELEVATION = -199.1;
    private static final String TEST_INSTITUTE = "Test Institute";
    private static final String TEST_VECTOR_SENS_ORIENT = "PBQ";
    private static final IMCDFStandardLevel TEST_STANDARD_LEVEL = new IMCDFStandardLevel (IMCDFStandardLevel.StandardLevel.PARTIAL);
    private static final IMCDFStandardName TEST_STANDARD_NAME = new IMCDFStandardName (IMCDFStandardName.StandardName.INTERMAGNET_1_SECOND);
    private static final String TEST_STANDARD_VERSION = "1.0";
    private static final String TEST_PARTIAL_STAND_DESC = "IMO1, IMO2, IMO99";
    private static final String TEST_SOURCE = "Source of the data";
    private static final String TEST_UNIQUE_IDENTIFIER = "DOI/10.1.2.3";
    private static final String TEST_PARENT_IDENTIFIERS [] = {"DOI/11.1.2.3", "DOI/12.5.6"};
    private static final URL TEST_REFERENCE_LINKS [];
    
    private static final Date TEST_TS_START_DATE = new Date (TEST_PUB_DATE.getTime() - 864000000);
    
    private static final int TEST_VECTOR_DATA_LENGTH = 100;
    private static final int TEST_SCALAR_DATA_LENGTH = 10;
    private static final int TEST_TEMPERATURE_DATA_LENGTH = 5;

    private static final double TEST_VECTOR_SAMPLE_PERIOD_SECS = 1.0;
    private static final double TEST_SCALAR_SAMPLE_PERIOD_SECS = 10.0;
    private static final double TEST_TEMPERATURE_SAMPLE_PERIOD_SECS = 20.0;
    
    private static final String TEST_VECTOR_TS_NAME = "GeomagneticVectorTimes";
    private static final String TEST_SCALAR_TS_NAME = "GeomagneticScalarTimes";
    private static final String TEST_TEMPERATURE_TS_NAME = "Temperature1Times";
    
    private static final String TEST_GEOMAG_FIELD_NAME_BASE = "GeomagneticFieldElement";
    private static final String TEST_GEOMAG_ELEMENT_NAMES [] = {"X", "Y", "Z", "S"};
    private static final String TEST_TEMPERATURE_NAME_BASE = "Temperature"; 
    
    private static final double TEST_VECTOR_MIN_VAL =   -90000.0;
    private static final double TEST_VECTOR_MAX_VAL =    90000.0;
    private static final double TEST_SCALAR_MIN_VAL =        0.0;
    private static final double TEST_SCALAR_MAX_VAL =    90000.0;
    private static final double TEST_TEMPERATURE_MIN_VAL = -50.0;
    private static final double TEST_TEMPERATURE_MAX_VAL =  80.0;
    private static final double TEST_FILL_VAL =          99999.0;
    
    private static final String TEST_GEOMAG_UNITS = "nT";
    private static final String TEST_TEMPERATURE_UNITS = "Celcius";

    // folder for tests - gets deleted after each test
    @Rule
    public TemporaryFolder temp_folder = new TemporaryFolder ();
    
    static
    {
        URL urls [];
        try
        {
             urls = new URL [] {new URL("http://test.com/first"), new URL ("ftp://test.ftp/second")};
        }
        catch (MalformedURLException e)
        {
            urls = null;
        }
        if (urls == null)
            TEST_REFERENCE_LINKS = null;
        else
        {
            TEST_REFERENCE_LINKS = new URL [urls.length];
            for (int count=0; count<urls.length; count++)
                TEST_REFERENCE_LINKS [count] = urls [count];
        }
    }
    
    public ImagCDFIT() {
    }

    /**
     * Test of write method, of class ImagCDF.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        
        // create a synthetic CDF object
        ImagCDF imag_cdf = buildImagCDF();
        
        // write it to the temporary folder
        System.out.println("   Writing CDF file with synthetic data...");
        File cdf_file = temp_folder.newFile("ImagCDFTestWrite.cdf");
        imag_cdf.write (cdf_file, true, true);
        assertEquals (cdf_file.exists(), true);
        
        // dump the CDF file to an ASCII output using cdfdump and capture the output
        System.out.println("   Dumping CDF to ASCII...");
        ProcessMonitor pm = new ProcessMonitor ("cdfdump -r 1,1000 " + cdf_file.getAbsolutePath());
        pm.startProcess();
        while (pm.isProcessAlive())
        { 
            try { Thread.sleep(100); }
            catch (InterruptedException e) { }
        }
        List<String> calculated_lines = readLines (new StringReader (pm.getStdoutData()));

        // get what should be the same data from a file stored in the class structure
        System.out.println("   Comparing CDF dump files...");
        InputStream in = this.getClass().getResourceAsStream("files/tst_cdf.dump");
        List<String> checked_lines = readLines (new InputStreamReader (in));

        // test that the two outputs are the same
        assertEquals (checked_lines.size(), calculated_lines.size());
        int n_lines = checked_lines.size();
        if (n_lines > calculated_lines.size()) n_lines = calculated_lines.size();
        for (int count=0; count<n_lines; count++)
        {
            switch (count)
            {
                case 0:     // name of CDF file
                    // don't check these line(s)
                    break;
                default:
                    assertEquals ("Data file line " + Integer.toString (count+1), checked_lines.get(count), calculated_lines.get(count));
                    break;
            }
        }
    }

    /**
     * Test of reading an ImagCDF, which is performed in one of the class constructors
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        
        // create a synthetic CDF object
        ImagCDF imag_cdf_orig = buildImagCDF();
        
        // write it to the temporary folder
        System.out.println("   Writing CDF file with synthetic data...");
        File cdf_file = temp_folder.newFile("ImagCDFTestWrite.cdf");
        imag_cdf_orig.write (cdf_file, true, true);
        assertEquals (cdf_file.exists(), true);
        
        // read it from the temporary folder
        System.out.println("   Reading CDF data back from file...");
        ImagCDF imag_cdf_copy = new ImagCDF (cdf_file);
        
        // compare the original and the copy read in
        assertEquals (imag_cdf_orig.getElementsRecorded(),                      imag_cdf_copy.getElementsRecorded());
        assertEquals (imag_cdf_orig.getElevation(),                             imag_cdf_copy.getElevation(), 0.001);
        assertEquals (imag_cdf_orig.getFormatDescription(),                     imag_cdf_copy.getFormatDescription());
        assertEquals (imag_cdf_orig.getFormatVersion(),                         imag_cdf_copy.getFormatVersion());
        assertEquals (imag_cdf_orig.getIagaCode(),                              imag_cdf_copy.getIagaCode());
        assertEquals (imag_cdf_orig.getInstitution(),                           imag_cdf_copy.getInstitution());
        assertEquals (imag_cdf_orig.getLatitude(),                              imag_cdf_copy.getLatitude(), 0.001);
        assertEquals (imag_cdf_orig.getLongitude(),                             imag_cdf_copy.getLongitude(), 0.001);
        assertEquals (imag_cdf_orig.getNElements(),                             imag_cdf_copy.getNElements());
        assertEquals (imag_cdf_orig.getNTemperatures(),                         imag_cdf_copy.getNTemperatures());
        assertEquals (imag_cdf_orig.getObservatoryName(),                       imag_cdf_copy.getObservatoryName());
        assertEquals (imag_cdf_orig.getParentIdentifiers().length,              imag_cdf_copy.getParentIdentifiers().length);
        for (int count=0; count<imag_cdf_orig.getParentIdentifiers().length; count++)
            assertEquals (imag_cdf_orig.getParentIdentifiers()[count],          imag_cdf_copy.getParentIdentifiers()[count]);
        assertEquals (imag_cdf_orig.getPartialStandDesc(),                      imag_cdf_copy.getPartialStandDesc());
        assertEquals (imag_cdf_orig.getPublicationDate().getTime(),             imag_cdf_copy.getPublicationDate().getTime());
        assertEquals (imag_cdf_orig.getPublicationLevel().toString(),           imag_cdf_copy.getPublicationLevel().toString());
        assertEquals (imag_cdf_orig.getReferenceLinks().length,                 imag_cdf_copy.getReferenceLinks ().length);
        for (int count=0; count<imag_cdf_orig.getReferenceLinks().length; count++)
            assertEquals (imag_cdf_orig.getReferenceLinks()[count].toString(),  imag_cdf_copy.getReferenceLinks()[count].toString());
        assertEquals (imag_cdf_orig.getSource(),                                imag_cdf_copy.getSource());
        assertEquals (imag_cdf_orig.getStandardLevel().toString(),              imag_cdf_copy.getStandardLevel().toString());
        assertEquals (imag_cdf_orig.getStandardName().toString(),               imag_cdf_copy.getStandardName().toString());
        assertEquals (imag_cdf_orig.getStandardVersion(),                       imag_cdf_copy.getStandardVersion());
        assertEquals (imag_cdf_orig.getTermsOfUse(),                            imag_cdf_copy.getTermsOfUse());
        assertEquals (imag_cdf_orig.getTitle(),                                 imag_cdf_copy.getTitle());
        assertEquals (imag_cdf_orig.getUniqueIdentifier(),                      imag_cdf_copy.getUniqueIdentifier());
        assertEquals (imag_cdf_orig.getVectorSensorOrientation(),               imag_cdf_copy.getVectorSensorOrientation());

        for (int count=0; count<imag_cdf_orig.getNElements(); count++)
        {
            ImagCDFVariable element_orig = imag_cdf_orig.getElement(count);
            ImagCDFVariable element_copy = imag_cdf_copy.getElement(count);
            assertEquals(element_orig.getData().length,             element_copy.getData().length);
            assertEquals(element_orig.getDataLength(),              element_copy.getDataLength());
            assertEquals(element_orig.getDepend0(),                 element_copy.getDepend0());
            assertEquals(element_orig.getElementRecorded(),         element_copy.getElementRecorded());
            assertEquals(element_orig.getFieldName(),               element_copy.getFieldName());
            assertEquals(element_orig.getFillValue(),               element_copy.getFillValue(), 0.001);
            assertEquals(element_orig.getUnits(),                   element_copy.getUnits());
            assertEquals(element_orig.getValidMaximum(),            element_copy.getValidMaximum(), 0.001);
            assertEquals(element_orig.getValidMinimum(),            element_copy.getValidMinimum(), 0.001);
            assertEquals(element_orig.getVariableType().toString(), element_copy.getVariableType().toString());
            
            ImagCDFVariableTS ts_orig = imag_cdf_orig.findTimeStamps(element_orig);
            ImagCDFVariableTS ts_copy = imag_cdf_copy.findTimeStamps(element_copy);
            assertEquals(ts_orig.getNSamples(),                     ts_copy.getNSamples());
            assertEquals(ts_orig.getSamplePeriod(),                 ts_copy.getSamplePeriod(), 0.001);
            assertEquals(ts_orig.getStartDate().getTime(),          ts_copy.getStartDate().getTime());
            assertEquals(ts_orig.getTimeStamps().length,            ts_copy.getTimeStamps().length);
            assertEquals(ts_orig.getVarName(),                      ts_copy.getVarName());
        }
        
        for (int count=0; count<imag_cdf_orig.getNTemperatures(); count++)
        {
            ImagCDFVariable temp_orig = imag_cdf_orig.getTemperature(count);
            ImagCDFVariable temp_copy = imag_cdf_copy.getTemperature(count);
            assertEquals(temp_orig.getData().length,             temp_copy.getData().length);
            assertEquals(temp_orig.getDataLength(),              temp_copy.getDataLength());
            assertEquals(temp_orig.getDepend0(),                 temp_copy.getDepend0());
            assertEquals(temp_orig.getElementRecorded(),         temp_copy.getElementRecorded());
            assertEquals(temp_orig.getFieldName(),               temp_copy.getFieldName());
            assertEquals(temp_orig.getFillValue(),               temp_copy.getFillValue(), 0.001);
            assertEquals(temp_orig.getUnits(),                   temp_copy.getUnits());
            assertEquals(temp_orig.getValidMaximum(),            temp_copy.getValidMaximum(), 0.001);
            assertEquals(temp_orig.getValidMinimum(),            temp_copy.getValidMinimum(), 0.001);
            assertEquals(temp_orig.getVariableType().toString(), temp_copy.getVariableType().toString());
        }
    }

    /**
     * Test of findTimeStamps method, of class ImagCDF.
     */
    @Test
    public void testFindTimeStamps() throws Exception {
        System.out.println("findTimeStamps");
        
        // create a synthetic CDF object
        ImagCDF imag_cdf = buildImagCDF();
        
        // find the time stamps
        assertEquals (imag_cdf.findTimeStamps(imag_cdf.getElement(0)).getVarName(),     "GeomagneticVectorTimes");
        assertEquals (imag_cdf.findTimeStamps(imag_cdf.getElement(1)).getVarName(),     "GeomagneticVectorTimes");
        assertEquals (imag_cdf.findTimeStamps(imag_cdf.getElement(2)).getVarName(),     "GeomagneticVectorTimes");
        assertEquals (imag_cdf.findTimeStamps(imag_cdf.getElement(3)).getVarName(),     "GeomagneticScalarTimes");
        assertEquals (imag_cdf.findTimeStamps(imag_cdf.getTemperature(0)).getVarName(), "Temperature1Times");
    }

    /**
     * Test of findVectorTimeStamps method, of class ImagCDF.
     */
    @Test
    public void testFindVectorTimeStamps() throws Exception {
        System.out.println("findVectorTimeStamps");
        
        // create a synthetic CDF object
        ImagCDF imag_cdf = buildImagCDF();

        // check name of vector times variable
        assertEquals (imag_cdf.findVectorTimeStamps().getVarName(), "GeomagneticVectorTimes");
    }

    /**
     * Test of findVectorElements method, of class ImagCDF.
     */
    @Test
    public void testFindVectorElements() throws Exception {
        System.out.println("findVectorElements");

        // create a synthetic CDF object
        ImagCDF imag_cdf = buildImagCDF();

        // check vector elements
        assertEquals (imag_cdf.findVectorElements().length, 3);
        assertEquals (imag_cdf.findVectorElements()[0], 0);
        assertEquals (imag_cdf.findVectorElements()[1], 1);
        assertEquals (imag_cdf.findVectorElements()[2], 2);
    }

    /**
     * Test of findScalarElement method, of class ImagCDF.
     */
    @Test
    public void testFindScalarElement() throws Exception {
        System.out.println("findScalarElement");
        
        // create a synthetic CDF object
        ImagCDF imag_cdf = buildImagCDF();

        // check scalar elements
        assertEquals (imag_cdf.findScalarElement(), 3);
    }

    /**
     * Test of makeFilename method, of class ImagCDF.
     */
    @Test
    public void testMakeFilename() {
        System.out.println("makeFilename");
        
        assertEquals (ImagCDF.makeFilename("abc", ImagCDF.FilenameSamplePeriod.SECOND, TEST_PUB_DATE, TEST_PUB_LEVEL), "abc_20141221_000000_pt1s_1.cdf");
    }


    // build an ImagCDF from syntehtic data for testing
    private ImagCDF buildImagCDF ()
    throws CDFException
    {
        ImagCDFVariableTS time_stamps [] = new ImagCDFVariableTS [3];
        time_stamps [0] = new ImagCDFVariableTS (TEST_TS_START_DATE, TEST_VECTOR_SAMPLE_PERIOD_SECS, TEST_VECTOR_DATA_LENGTH, TEST_VECTOR_TS_NAME);
        time_stamps [1] = new ImagCDFVariableTS (TEST_TS_START_DATE, TEST_SCALAR_SAMPLE_PERIOD_SECS, TEST_SCALAR_DATA_LENGTH, TEST_SCALAR_TS_NAME);
        time_stamps [2] = new ImagCDFVariableTS (TEST_TS_START_DATE, TEST_TEMPERATURE_SAMPLE_PERIOD_SECS, TEST_TEMPERATURE_DATA_LENGTH, TEST_TEMPERATURE_TS_NAME);
        
        ImagCDFVariable elements [] = new ImagCDFVariable [TEST_GEOMAG_ELEMENT_NAMES.length];
        for (int count=0; count<TEST_GEOMAG_ELEMENT_NAMES.length -1; count++)
        {
            double data [] = new double [TEST_VECTOR_DATA_LENGTH];
            for (int count2=0; count2<data.length; count2 ++)
                data[count2] = Math.sin ((double) count2 * 2.0 * Math.PI / (double) data.length) * 100.0 * (count +1);
            elements [count] = new ImagCDFVariable (new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement),
                                                    TEST_GEOMAG_FIELD_NAME_BASE + TEST_GEOMAG_ELEMENT_NAMES [count], TEST_VECTOR_MIN_VAL,
                                                    TEST_VECTOR_MAX_VAL, TEST_GEOMAG_UNITS, TEST_FILL_VAL, TEST_VECTOR_TS_NAME, 
                                                    TEST_GEOMAG_ELEMENT_NAMES [count], data);
        }
        double data [] = new double [TEST_SCALAR_DATA_LENGTH];
        for (int count2=0; count2<data.length; count2 ++)
            data[count2] = Math.sin ((double) count2 * 2.0 * Math.PI / (double) data.length);
        int count = TEST_GEOMAG_ELEMENT_NAMES.length -1;
        elements [count] = new ImagCDFVariable (new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement),
                                                TEST_GEOMAG_FIELD_NAME_BASE + TEST_GEOMAG_ELEMENT_NAMES [count], TEST_SCALAR_MIN_VAL,
                                                TEST_SCALAR_MAX_VAL, TEST_GEOMAG_UNITS, TEST_FILL_VAL, TEST_SCALAR_TS_NAME, 
                                                TEST_GEOMAG_ELEMENT_NAMES [count], data);
        
        ImagCDFVariable temperatures [] = new ImagCDFVariable [1];
        data = new double [TEST_TEMPERATURE_DATA_LENGTH];
        for (int count2=0; count2<data.length; count2 ++)
            data[count2] = Math.sin ((double) count2 * 2.0 * Math.PI / (double) data.length);
        temperatures [0] = new ImagCDFVariable (new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.Temperature),
                                                TEST_TEMPERATURE_NAME_BASE + count, TEST_TEMPERATURE_MIN_VAL,
                                                TEST_TEMPERATURE_MAX_VAL, TEST_TEMPERATURE_UNITS, TEST_FILL_VAL, TEST_TEMPERATURE_TS_NAME, 
                                                "1", data);
        
        ImagCDF imag_cdf = new ImagCDF (TEST_IAGA_CODE, TEST_PUB_LEVEL, TEST_PUB_DATE, TEST_OBSERVATORY_NAME,
                                        TEST_LATITUDE, TEST_LONGITUDE, TEST_ELEVATION,
                                        TEST_INSTITUTE, TEST_VECTOR_SENS_ORIENT, TEST_STANDARD_LEVEL, TEST_STANDARD_NAME, 
                                        TEST_STANDARD_VERSION, TEST_PARTIAL_STAND_DESC, TEST_SOURCE, TEST_UNIQUE_IDENTIFIER,
                                        TEST_PARENT_IDENTIFIERS, TEST_REFERENCE_LINKS,
                                        elements, temperatures, time_stamps);
        return imag_cdf;
    }
    
    private List<String> readLines (Reader reader)
    throws IOException
    {
        List<String> lines = new ArrayList<> ();
        BufferedReader br =  new BufferedReader (reader);
        String line;
        while ((line = br.readLine()) != null) lines.add(line);
        br.close();
        return lines;
    }
}
