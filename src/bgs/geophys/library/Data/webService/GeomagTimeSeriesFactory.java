/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.GeomagDataFormat;
import bgs.geophys.library.Data.GeomagDataPoint;
import bgs.geophys.library.Misc.DateUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author sani
 */
public class GeomagTimeSeriesFactory {
    
    private SimpleDateFormat DATA_DATE_FORMAT;
    private Logger logger;

    public GeomagTimeSeriesFactory() {
        DATA_DATE_FORMAT = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
        DATA_DATE_FORMAT.setTimeZone(DateUtils.gmtTimeZone);
        logger = Logger.getLogger(GeomagTimeSeries.class);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%p: %m %n")));
    }

    /**
     * Creates a GeomagTime Series object from a given List data store, metadata
     * and components orientation.
     *
     * @param dataStore A List object that contains the data.
     * @param GMmetadata A GeomagMetadata object that contains the metadata.
     * @param defaultOrientation A GeomagAbsoluteValue constant representing
     * the orientation of the data components.
     * @return Geomag time series
     */
   public GeomagTimeSeries getGeomagTimeSeries(List dataStore, GeomagMetadata GMmetadata, Components defaultOrientation) {
        GeomagTimeSeries geomagTS = new GeomagTimeSeries(GMmetadata);
        geomagTS.setDefaultOrientation(defaultOrientation);
        for(Iterator i=dataStore.iterator();i.hasNext();){
            String[] values = (String[])i.next();
            try{
                Date date1 = DATA_DATE_FORMAT.parse(values[0]+" "+values[1]);
                double[] components = new double[4];
                int j=0;
                for(int k=3;k<values.length;k++){
                    components[j] = Double.parseDouble(values[k]);
                    if (components[j] >= 99999.0) components[j] = 99999.0;
                    else if (components[j] >= 88888.0) components[j] = 88888.0;
                    j++;
                }
                GeomagAbsoluteValue gmav = new GeomagAbsoluteValue(components[1],
                        components[0],components[2],components[3],GeomagAbsoluteValue.COMPONENT_F_SCALAR,
                        88888.0, defaultOrientation.componentsCode(),GeomagAbsoluteValue.ANGLE_MINUTES);
                GeomagDataPoint gmdp = new GeomagDataPoint(date1, gmav);
                geomagTS.add(gmdp);

            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return geomagTS;
    }

    /**
     * Creates a GeomagTime Series object from a given GeomagDataFormat
     * and components orientation.
     *
     * @param dataStore A List object that contains the data.
     * @param GMmetadata A GeomagMetadata object that contains the metadata.
     * @param componentsOrientation A GeomagAbsoluteValue constant representing
     * the orientation of the data components.
     * @return Geomag time series
     */
    public GeomagTimeSeries getGeomagTimeSeries(GeomagDataFormat gdf, int componentsOrientation){
        return getGeomagTimeSeries(gdf, null, componentsOrientation );
    }

    /**
     * Creates a GeomagTime Series object from a given GeomagDataFormat, metadata
     * and components orientation.
     *
     * @param dataStore A List object that contains the data.
     * @param GMmetadata A GeomagMetadata object that contains the metadata.
     * @param componentsOrientation A GeomagAbsoluteValue constant representing
     * the orientation of the data components.
     * @return Geomag time series
     */
    public GeomagTimeSeries getGeomagTimeSeries(GeomagDataFormat gdf, GeomagMetadata gdfMetadata, int componentsOrientation) {
        if(!(gdf.getStationName()==null || gdf.getStationName().length()==0)){
            gdfMetadata.setStationName(gdf.getStationName());
        }
        if(gdf.getLatitude()!=0){
            gdfMetadata.setLatitude(gdf.getLatitude());
        }
        if(gdf.getLongitude()!=0){
            gdfMetadata.setLongitude(gdf.getLongitude());
        }
        if(gdf.getElevation()!=0){
            gdfMetadata.setElevation(gdf.getLongitude());
        }
        if(!(gdf.getDataType()==null || gdf.getDataType().length()==0)){
            gdfMetadata.setDataType(gdf.getDataType());
        }
        if(!(gdf.getInstituteName()==null || gdf.getInstituteName().length()==0)){
            gdfMetadata.setInstituteName(gdf.getInstituteName());       }
        if(!(gdf.getSensorOrientation()==null || gdf.getSensorOrientation().length()==0)){
            gdfMetadata.setSensorOrientation(gdf.getSensorOrientation());
        }
        if(!(gdf.getSamplePeriodString()==null || gdf.getSamplePeriodString().length()==0)){
            gdfMetadata.setSamplePeriodString(gdf.getSamplePeriodString());
        }
        if(!(gdf.getIntervalType()==null || gdf.getIntervalType().length()==0)){
            gdfMetadata.setIntervalType(gdf.getIntervalType());
        }
        GeomagTimeSeries geomagTS = new GeomagTimeSeries(gdfMetadata);
        // Not finished, need to write method to populate GeomagTimeSeries object
        // from GeomagDataFormat object.
        return geomagTS;
    }

}
