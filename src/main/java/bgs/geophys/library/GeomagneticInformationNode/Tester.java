/*
 * Tester.java
 *
 * Created on 09 November 2006, 17:16
 */

package bgs.geophys.library.GeomagneticInformationNode;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.DateUtils;
import java.util.*;

/**
 *
 * @author  smf
 */
public class Tester 
{
    
    public static void main (String args [])
    {
        Properties sys_props;
        
        try
        {
            // force properties to contain reference to GIN file location
            sys_props = System.getProperties();
            sys_props.setProperty ("GIN_READ_ONLY", "1");
//            sys_props.setProperty("GIN_DATA_DIR",    "\\\\mhlf\\gin\\test\\data");
//            sys_props.setProperty("GIN_SYS_TAB_DIR", "\\\\mhlf\\gin\\test\\data\\system_tables");
            sys_props.setProperty("GIN_DATA_DIR",    "\\\\mhlf\\e_gin\\data");
            sys_props.setProperty("GIN_SYS_TAB_DIR", "\\\\mhlf\\e_gin\\data\\system_tables");
//            sys_props.setProperty("GIN_DATA_DIR",    "c:\\data");
//            sys_props.setProperty("GIN_SYS_TAB_DIR", "c:\\data\\system_tables");
            
            // test access to the stations database
            displayStations ();
            
            // get available data statistics
            displayAvailableData ();
            
            // get most recent data
            displayMRD ();
            
            // try to read a data file
            displayData ();
        }
        catch (ConfigFileException e)
        {
            if (e.getLineNumber() >= 0)
                System.err.println ("Error in config file at line number " + Integer.toString (e.getLineNumber()) + ":");
            else
                System.err.println ("Error in config file:");
            if (e.getMessage() == null)
            {
                System.err.println ("    Unknown error, stack trace follows");
                e.printStackTrace();
            }
            else
                System.err.println ("    " + e.getMessage());
        }
        catch (GINDataException e)
        {
            if (e.getMessage() == null)
            {
                System.err.println ("Unknown data error, stack trace follows");
                e.printStackTrace();
            }
            else
                System.err.println ("Data error: " + e.getMessage());
        }
        catch (ParameterException e)
        {
            if (e.getMessage() == null)
            {
                System.err.println ("Unknown data error, stack trace follows");
                e.printStackTrace();
            }
            else
                System.err.println ("Data error: " + e.getMessage());
        }
    }
    
    public static void displayStations ()
    throws ConfigFileException
    {
        int count;
        StationDetails station_details;
        StationDetails.StationDetailsFields station_details_fields;
        
        station_details = new StationDetails (StationDetails.ORDER_BY_STATION_CODE);
        for (count=0; count<station_details.getNRows (); count++)
        {
            station_details_fields = station_details.getRow (count);
            System.out.println (Integer.toString (count +1) + " " + station_details_fields.station_code + "  " + "[" + station_details_fields.station_name + "]");
        }
    }
    
    public static void displayAvailableData ()
    throws ConfigFileException, GINDataException, ParameterException
    {
        int count, end;
        GINUtils.AvailableDataDetails av_data_details;
        
        System.out.println ("AVD:");
        av_data_details = GINUtils.getAvailableData("esk", 1440, "adj-or-rep", null, null);
        System.out.println (av_data_details.start_date.toString());
        System.out.println (av_data_details.day_counts.length);
        end = av_data_details.day_counts.length;
        if (end > 10) end = 10;
        for (count=0; count<end; count++)
            System.out.println (av_data_details.day_counts[count]);
    }
    
    public static void displayData ()
    throws ConfigFileException, GINDataException, ParameterException
    {
        int count;
        GINData minute_data;
        Date date;
        Collection data;
        GeomagAbsoluteValue value;
        Iterator iterator;
        GregorianCalendar calendar;
        
        System.out.println ("Data:");
        calendar = new GregorianCalendar (2007, 7, 3, 0, 0, 0);
        calendar.setTimeZone (DateUtils.gmtTimeZone);
        date = calendar.getTime();
        minute_data = new GINData ("esk", 1440, date, 10, "adj-or-rep", true);
        data = minute_data.getData();
        for (count=0, iterator=data.iterator(); 
             count<minute_data.getDataLength(); count++)
        {
            value = (GeomagAbsoluteValue) iterator.next ();
            System.out.println (Integer.toString (count) + " " +
                                value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_1, GeomagAbsoluteValue.ANGLE_MINUTES) + " " +
                                value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_2, GeomagAbsoluteValue.ANGLE_MINUTES) + " " +
                                value.getComponent(GeomagAbsoluteValue.COMPONENT_NATIVE_3, GeomagAbsoluteValue.ANGLE_MINUTES) + " " +
                                value.getFScalar());
        }
    }
    
    public static void displayMRD ()
    throws ConfigFileException, GINDataException, ParameterException
    {
        Date date;
        
        date = GINUtils.findMRD("esk", 1440, "adj-or-rep");
        System.out.println ("MRD:");
        System.out.println (date.toString());
    }
}
