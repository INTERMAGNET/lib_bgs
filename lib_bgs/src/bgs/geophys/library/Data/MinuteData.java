/*
 * MinuteData.java
 *
 * Created on 07 September 2007, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class can be used to store minute-mean geomagnetic data, and is useful
 * for converting minute mean data to hourly mean data.
 * Hourly means are automatically computed by GeomagDataException base class
 * as the data are added.
 * Data must be added in blocks of 60.
 *
 * @author Ewan Dawson (ewan@bgs.ac.uk)
 */
public class MinuteData  extends GeomagDataFormat {
    
    /**
     * Creates a new instance of MinuteData
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
     * @throws GeomagDataException if there are any faults
     */
    public MinuteData(String stationCode, String stationName,
                      double latitude, double longitude, double elevation,
                      String compCode, String dataType, String ginCode,
                      String instituteName, String sensorOrientation,
                      String samplePeriodString, String intervalType,
                      boolean allowFourDigitSC) throws GeomagDataException {
        
        super (stationCode, stationName, latitude, 
               longitude, elevation,
               compCode, dataType, ginCode, instituteName, sensorOrientation,
               samplePeriodString, intervalType, allowFourDigitSC, 60);
    }
    
    /***************************************************************
     * Dummy method stubs to comply with GeomagDataFormat interface 
     ***************************************************************/
    
    /**
     * Method stub to comply with base class GeomagDataFormat interface.
     * No functionality implemented.
     * @return null
     * @param dummyString This is a dummy method. No point calling it!
     * @param dummyBool This is a dummy method. No point calling it!
     */
     public String makeFilename (String dummyString, boolean dummyBool) {
         // Do nothing - simply return null.
         return null;
     }
     
     /**
     * Method stub to comply with base class GeomagDataFormat interface.
     * No functionality implemented.
     * @param dummyOS This is a dummy method. No point calling it!
     * @param dummyByteArray This is a dummy method. No point calling it!
     * @throws java.io.IOException This is a dummy method. No point calling it!
     */
    public void write (OutputStream dummyOS, byte dummyByteArray []) throws IOException {
        // Do nothing
        return;
    }
}
