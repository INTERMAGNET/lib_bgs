package bgs.geophys.library.Data.webService;

import bgs.geophys.library.LogConfig.LogConfig;
import bgs.geophys.library.Data.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Ewan Dawson
 */
public class GeomagTimeSeries extends TreeSet<GeomagDataPoint> {

    private static final Logger LOGGER = LogConfig.getLogger(GeomagTimeSeriesFactory.class);
// previous logging used this configuration
//        logger.addAppender(new ConsoleAppender(new PatternLayout("%p: %m %n")));
// this should be replaced with a log4j2.xml config file somewhere in the classpath
    
    private GeomagMetadata metadata;    
    private Components componentsOrientation = Components.ORIENTATION_UNKNOWN;
    private Components defaultOrientation = Components.ORIENTATION_UNKNOWN;
    

    public GeomagTimeSeries(GeomagMetadata metadata) {
        this.metadata = metadata;
    }

    public <T extends Class> GeomagDataFormat toGeomagDataFormat(T gdfClass, DataInterval intervalType) {
        Constructor[] constructors = gdfClass.getConstructors();

        Object[] initargs = {metadata.getStationCode(),metadata.getStationName(),
            metadata.getLatitude(),metadata.getLongitude(),metadata.getElevation(),
            metadata.getCompCode(),metadata.getDataType(),metadata.getGinCode(),
            metadata.getInstituteName(),metadata.getSensorOrientation(),metadata.getSamplePeriodString(),
            metadata.getIntervalType(),metadata.getAllowFourDigitSC(),metadata.getBlockSize()};
       GeomagDataFormat dataFormat = null;
       for(int i=0; i<constructors.length;i++){           
           try{               
               dataFormat = (GeomagDataFormat)constructors[i].newInstance(initargs);
               LOGGER.debug("GeomagDataFormat object created successfully");
           }catch(IllegalArgumentException e){
                
           }catch(InstantiationException e){
               LOGGER.error(e.getMessage(), e);
           }catch(IllegalAccessException e){
               LOGGER.error(e.getMessage(), e);
           }catch(InvocationTargetException e){
               LOGGER.error(e.getMessage(), e);
           }catch(ExceptionInInitializerError e){
               LOGGER.error(e.getMessage(), e);
           }           
       }
       insertData(dataFormat, intervalType.period());
       return dataFormat;
    }

    

    

    

    public Components getDefaultOrientation() {
        return defaultOrientation;
    }

    public void setDefaultOrientation(Components defaultOrientation) {
        this.defaultOrientation = defaultOrientation;
    }

    public Components getComponentsOrientation() {
        return componentsOrientation;
    }

    public void setComponentsOrientation(Components componentsOrientation) {
        this.componentsOrientation = componentsOrientation;
    }

    public GeomagMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(GeomagMetadata metadata) {
        this.metadata = metadata;
    }
     
    private void insertData(GeomagDataFormat dataObject, long period){

         int size = this.size();
         double[] c1 = new double[size];
         double[] c2 = new double[size];
         double[] c3 = new double[size];
         double[] c4 = new double[size];
         Date[] date = new Date[size];
         Components components;
         if(getComponentsOrientation()!=Components.ORIENTATION_UNKNOWN){
            components = getComponentsOrientation();
         }else{
             components = getDefaultOrientation();
         }

         int j=0;         
         
        for(Iterator i=this.iterator();i.hasNext();){
            GeomagDataPoint dataPoint = (GeomagDataPoint)i.next();
            date[j] = dataPoint.getTime();
            GeomagAbsoluteValue absoluteValue = dataPoint.getValue();
            components.setValue(absoluteValue);
            c1[j] = components.getComponent1();
            c2[j] = components.getComponent2();
            c3[j] = components.getComponent3();
            c4[j] = components.getComponent4();
            if(c1[j]>=99999.0 || c2[j]>=99999.0 || c3[j]>=99999.0 || c4[j]>=99999.0){
                c1[j] = 99999.0;
                c2[j] = 99999.0;
                c3[j] = 99999.0;
                c4[j] = 99999.0;
            }
            j++;
        }
        
         try{             
             dataObject.addData(date[0], period, 99999.0,
                 88888.0, c1, c2, c3, c4);
         }catch(GeomagDataException e){
            LOGGER.error(e.getMessage(), e);
         }
     }

}
