/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

import java.util.Properties;

/**
 * Class that represents all metadata needed by GeomagDataFormat objects.
 * This class is a wrapper around a java.util.Properties object and provides
 * a defined interface for setting and getting the properties in the
 * java.util.Properties object.
 *
 * @author sani
 */
public class GeomagMetadata {

    private Properties properties;
    private String stationCode = "stationCode";
    private String stationName = "stationName";
    private String latitude = "latitude";
    private String longitude = "longitude";
    private String elevation = "elevation";
    private String compCode = "compCode";
    private String dataType = "dataType";
    private String ginCode = "ginCode";
    private String instituteName = "instituteName";
    private String sensorOrientation = "sensorOrientation";
    private String samplePeriodString = "samplePeriodString";
    private String intervalType = "intervalType";
    private String allowFourDigitSC = "allowFourDigitSC";
    private String blockSize = "blockSize";
    private String[] allProperties = {"stationCode","stationName","latitude",
        "longitude","elevation","compCode","dataType","ginCode","instituteName",
        "sensorOrientation","samplePeriodString","intervalType","allowFourDigitSC",
        "blockSize"};

    /**
     * Creates a default GeomagMetada
     */
    public GeomagMetadata() {
        properties = new Properties();
        for(int i=0; i<allProperties.length;i++){
            properties.setProperty(allProperties[i], "");
        }
    }

    /**
     * Creates a GeomagMetadata object using the given properties object
     * @param defaultProperties The properties object to use for creating the
     * Geomag metadata
     */
    public GeomagMetadata(Properties defaultProperties){
        properties = new Properties(defaultProperties);
    }

    public boolean getAllowFourDigitSC() {
        if(properties.getProperty(allowFourDigitSC)==null || properties.getProperty(allowFourDigitSC).length()==0){
            return false;
        }else{
            return Boolean.valueOf(properties.getProperty(elevation));
        }
    }

    public void setAllowFourDigitSC(boolean allowFourDigitSC) {
        properties.setProperty(this.allowFourDigitSC, String.valueOf(allowFourDigitSC));
    }    

    public int getBlockSize() {
        if(properties.getProperty(blockSize)==null || properties.getProperty(blockSize).length()==0){
            return 0;
        }else{
            return Integer.valueOf(properties.getProperty(blockSize));
        }
    }

    public void setBlockSize(int blockSize) {
       properties.setProperty(this.blockSize, String.valueOf(blockSize));
    }

    public String getCompCode() {
        return properties.getProperty(compCode);
    }

    public void setCompCode(String compCode) {
        properties.setProperty(this.compCode, checkNull(compCode));
    }

    public String getDataType() {
        return properties.getProperty(dataType);
    }

    public void setDataType(String dataType) {
        properties.setProperty(this.dataType, checkNull(dataType));
    }

    public double getElevation() {
        if(properties.getProperty(elevation)==null || properties.getProperty(elevation).length()==0){
            return 0;
        }else{
            return Double.valueOf(properties.getProperty(elevation));
        }
    }

    public void setElevation(double elevation) {
        properties.setProperty(this.elevation, String.valueOf(elevation));
    }

    public String getGinCode() {
        return properties.getProperty(ginCode);
    }

    public void setGinCode(String ginCode) {
        properties.setProperty(this.ginCode, checkNull(ginCode));
    }

    public String getInstituteName() {
        return properties.getProperty(instituteName);
    }

    public void setInstituteName(String instituteName) {
        properties.setProperty(this.instituteName, checkNull(instituteName));
    }

    public String getIntervalType() {
        return properties.getProperty(intervalType);
    }

    public void setIntervalType(String intervalType) {
        properties.setProperty(this.intervalType, checkNull(intervalType));
    }

    public double getLatitude() {
        if(properties.getProperty(latitude)==null || properties.getProperty(latitude).length()==0){
            return 0;
        }else{
            return Double.valueOf(properties.getProperty(latitude));
        }
    }

    public void setLatitude(double latitude) {
       properties.setProperty(this.latitude, String.valueOf(latitude));
    }

    public double getLongitude() {
        if(properties.getProperty(longitude)==null || properties.getProperty(longitude).length()==0){
            return 0;
        }else{
            return Double.valueOf(properties.getProperty(longitude));
        }
    }

    public void setLongitude(double longitude) {
        properties.setProperty(this.longitude, String.valueOf(longitude));
    }    

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getSamplePeriodString() {
        return properties.getProperty(samplePeriodString);
    }

    public void setSamplePeriodString(String samplePeriodString) {
        properties.setProperty(this.samplePeriodString, checkNull(samplePeriodString));
    }

    public String getSensorOrientation() {
        return properties.getProperty(sensorOrientation);
    }

    public void setSensorOrientation(String sensorOrientation) {
        properties.setProperty(this.sensorOrientation, checkNull(sensorOrientation));
    }

    public String getStationCode() {
        return properties.getProperty(stationCode);
    }

    public void setStationCode(String stationCode) {
        properties.setProperty(this.stationCode, checkNull(stationCode));
    }

    public String getStationName() {
        return properties.getProperty(stationName);
    }

    public void setStationName(String stationName) {
        properties.setProperty(this.stationName, checkNull(stationName));
    }

    private String checkNull(String s){
        if(s==null){
            return "";
        }else{
            return s;
        }
    }

}
