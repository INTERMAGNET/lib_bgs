/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

import bgs.geophys.library.Gdas.GdasCollect.Config.GDASAddressConfig;
import java.util.Date;

/**
 *
 * @author smf
 */
public class GDASAddressStatus 
implements StatusAlarm
{

    /** name of the station using this address */
    private String station_code;
    /** number of the GDAS system using this address */
    private int gdas_number;
    /** name of this address */
    private String address_name;
    /** IP address */
    private String address;
    /** IP port */
    private int ip_port;
    
    /** number of collections made since connection was opened */
    private int collection_count;
    /** date connection last opened successfully */
    private Date last_open_date;
    /** date data last retrieved successfully */
    private Date last_collection_date;
    /** date connection last failed to open or collect */
    private Date last_failure_date;
    /** last failure details */
    private String last_failure_details;
    
    /** build an empty status object */
    public GDASAddressStatus ()
    {
        station_code = null;
        gdas_number = -1;
        address_name = null;
        
        this.collection_count = 0;
        this.last_open_date = new Date (0l);
        this.last_failure_date = new Date (0l);
        this.last_collection_date = new Date (0l);
        this.last_failure_details = "";
    }
    
    /** build a new status object from the configuration */
    public GDASAddressStatus (String station_code, int gdas_number, GDASAddressConfig gdas_address_config)
    {
        this.station_code = station_code;
        this.gdas_number = gdas_number;
        address_name = gdas_address_config.toString();
        
        address = gdas_address_config.getHostName();
        ip_port = gdas_address_config.getIpPort();
        
        this.collection_count = 0;
        this.last_open_date = new Date (0l);
        this.last_failure_date = new Date (0l);
        this.last_collection_date = new Date (0l);
        this.last_failure_details = "";
    }
    
    /** copy the details of this address status to the given
     * object */
    public void copy (GDASAddressStatus from)
    {
        this.station_code = from.station_code;
        this.gdas_number = from.gdas_number;
        this.address_name = from.address_name;
        this.collection_count = from.collection_count;
        this.last_open_date = from.last_open_date;
        this.last_collection_date = from.last_collection_date;
        this.last_failure_date = from.last_failure_date;
        this.last_failure_details = from.last_failure_details;
    }
    
    // read methods for status variables
    public String getStationCode () { return station_code; }
    public boolean isStationCodeAlarm () { return false; }
    public int getGdasNumber () { return gdas_number; }
    public boolean isGdasNumberAlarm () { return false; }
    public String getAddressName () { return address_name; }
    public boolean isAddressNameAlarm () { return false; }
    public String getHostName () { return address; }
    public boolean isHostNameAlarm () { return false; }
    public int getIPPort () { return ip_port; }
    public boolean isIPPortAlarm () { return false; }
    public int getCollectionCount () { return collection_count; }
    public boolean isCollectionCountAlarm () { return false; }
    public Date getLastOpenDate () { return last_open_date; }
    public boolean isLastOpenDateAlarm () { return false; }
    public Date getLastCollectionDate () { return last_collection_date; }
    public boolean isLastCollectionDataAlarm () { return false; }
    public Date getLastFailureDate () { return last_failure_date; }
    public boolean isLastFailureDateAlarm () 
    {
        if (last_failure_date.getTime() > last_open_date.getTime()) return true;
        return false; 
    }
    public String getLastFailureDetails () { return last_failure_details; }
    public boolean isLastFailureDetailsAlarm ()
    {
        if (last_failure_date.getTime() > last_open_date.getTime()) return true;
        return false; 
    }
    
    // write methods for status variables
    public void clearCollectionCount () { collection_count = 0; }
    public void incrementCollectionCount () { collection_count ++; }
    public void setLastOpenDate (Date date) { last_open_date = date; }
    public void setLastCollectionDate (Date date) { last_collection_date = date; }
    public void setLastFailureDate (Date date) { last_failure_date = date; }
    public void setLastFailureDetails (String details) { last_failure_details = details; }

    public boolean equals (GDASAddressStatus o) 
    {
        if (o == null) return false;
        if (address_name == null)
        {
            if (o.address_name == null) return true;
            return false;
        }
        if (o.address_name == null) return false;
        return address_name.equals(o.address_name); 
    }
    
    public int compareTo (GDASAddressStatus o) 
    {
        if (o == null) return +1;
        if (address_name == null)
        {
            if (o.address_name == null) return 0;
            return -1;
        }
        if (o.address_name == null) return +1;
        return address_name.compareTo(o.address_name); 
    }
    
    public boolean isAlarmed() 
    {
        if (isAddressNameAlarm()) return true;
        if (isCollectionCountAlarm()) return true;
        if (isHostNameAlarm()) return true;
        if (isIPPortAlarm()) return true;
        if (isLastCollectionDataAlarm()) return true;
        if (isLastFailureDateAlarm()) return true;
        if (isLastFailureDetailsAlarm()) return true;
        if (isLastOpenDateAlarm()) return true;
        return false;
    }

    public String displayText() 
    {
        return address_name;
    }
}
