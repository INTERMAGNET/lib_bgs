/*
 * GDASConfig.java
 *
 * Created on 22 November 2005, 14:11
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

import bgs.geophys.library.Gdas.GdasCollect.Status.GDASStatus;
import java.util.*;

/**
 * This class holds configuration details for a single GDAS system. Note that
 * the name for the system is not changeable - to get a new name for a system
 * you need to create a new object.
 * 
 * @author  Administrator
 */
public class GDASConfig implements Comparable<GDASConfig>
{
   /** list of 1 second output formats */
    public enum OutputFileFormat {HUNDRED_PT, ONE_PT}
    
    /**configuration variable: the name of the system */
    private GDASName gdas_name;
    /** configuration variable: a list of addresses by which the host can be reached - the addresses are used
     * in the order in the list to try to retrieve data - if the 1st fails the next is
     * used, etc. */
    private Vector<GDASAddressConfig> address_list;
    /** configuration variable: the channel index for one second h data */
    private int sdas_h_chan_index;
    /** configuration variable: the channel index for one second d data */
    private int sdas_d_chan_index;
    /** configuration variable: the channel index for one second z data */
    private int sdas_z_chan_index;
    /** configuration variable: the channel index for one second temperature data */
    private int sdas_t_chan_index;
    /** configuration variable: the channel index for ten second f data */
    private int sdas_f_chan_index;
    /** configuration variable: the output format of the data */
    private OutputFileFormat output_file_format;
    /** configuration variable: the amount of time (in seconds) after which a thread which has not collected new data or
     * produced an error will be said to be dead (and will be restarted) */    
    private int watchdog_timeout;
    /** configuration variable: the maximum duration for a single data transfer (in seconds) */
    private int max_duration;
    private long max_duration_ms;   // copy of the maximum duration in milliseconds
    /** configuration variable: the delay between automatic collections, in seconds */
    private int collect_delay;
    /** configuration variable: flag to enable data collection */
    private boolean enable_data_collection;
    /** configuration variable: flag to enable minute-mean calculation */
    private boolean enable_mean_calculation;

    /** a link to the associated status object */
    private GDASStatus gdas_status;
    
    /** Creates a new instance of GDASConfig with default (missing) values */
    public GDASConfig ()
    {
        gdas_name = new GDASName ();
        address_list = new Vector<GDASAddressConfig> ();
        sdas_h_chan_index = 0;
        sdas_d_chan_index = 1;
        sdas_z_chan_index = 2;
        sdas_t_chan_index = 3;
        sdas_f_chan_index = 4;
        output_file_format = OutputFileFormat.HUNDRED_PT;
        watchdog_timeout = 900;
        max_duration = 3600;
        max_duration_ms = (long) max_duration * 1000l;
        this.collect_delay = 60;
        enable_data_collection = true;
        enable_mean_calculation = false;
        gdas_status = null;
    }
            
    /** Creates a new instance of GDASConfig with a given name */
    public GDASConfig (GDASName name)
    {
        this ();
        gdas_name = new GDASName (name);
    }

    /** Creates a new instance of GDASConfig with a given name */
    public GDASConfig (String station_code, int gdas_number)
    {
        this ();
        gdas_name = new GDASName (station_code, gdas_number);
    }
    
    /** Creates a new instance of GDASConfig as a copy of another */
    public GDASConfig (GDASConfig o)
    {
        this.gdas_name = o.gdas_name;
        this.address_list = o.address_list;
        this.sdas_h_chan_index = o.sdas_h_chan_index;
        this.sdas_d_chan_index = o.sdas_d_chan_index;
        this.sdas_z_chan_index = o.sdas_z_chan_index;
        this.sdas_t_chan_index = o.sdas_t_chan_index;
        this.sdas_f_chan_index = o.sdas_f_chan_index;
        this.output_file_format = o.output_file_format;
        this.watchdog_timeout = o.watchdog_timeout;
        this.max_duration = o.max_duration;
        this.max_duration_ms = (long) max_duration * 1000l;
        this.collect_delay = o.collect_delay;
        this.enable_data_collection = o.enable_data_collection;
        this.enable_mean_calculation = o.enable_mean_calculation;
        this.gdas_status = o.gdas_status;
    }
    
    /** Creates a new instance of GDASConfig as a copy of another, but with a new name */
    public GDASConfig (GDASConfig o, GDASName name)
    {
        this (o);
        this.gdas_name = new GDASName (name);
    }
    
    /** Creates a new instance of GDASConfig as a copy of another, but with a new name */
    public GDASConfig (GDASConfig o, String station_code, int gdas_number)
    {
        this (o);
        this.gdas_name = new GDASName (station_code, gdas_number);
    }
    
    /** show this config as a string */
    @Override
    public String toString ()
    {
        return format (gdas_name, 
                       enable_data_collection, enable_mean_calculation);
    }
    
    /** format a GDAS system string */
    public static String format (GDASName system_name,
                                 boolean enable_data_collection,
                                 boolean enable_mean_calculation)
    {
        if (system_name.isEmpty()) return "Unknown";
        return format (system_name.toString(), enable_data_collection, enable_mean_calculation);
    }
    
    /** format a GDAS system string */
    public static String format (String system_name,
                                 boolean enable_data_collection,
                                 boolean enable_mean_calculation)
    {
        if (system_name.length() <= 0) return "Unknown";
        if (! enable_data_collection)
            return system_name + " (disabled)";
        if (! enable_mean_calculation)
            return system_name + " (no means)";
        return system_name;
    }
    
    /** format a GDAS system string */
    public String format ()
    {
        return format (this.gdas_name,
                       this.enable_data_collection, this.enable_mean_calculation);
    }
    
    /** implementation of the comparable interface */
    public int compareTo (GDASConfig o) 
    {
        return gdas_name.compareTo(o.gdas_name);
    }

    // read methods for configuration variables - don't give direct
    // access to gdas_name so that it can't be set without checking
    public String getGdasStationCode () { return gdas_name.getStationCode(); }
    public int getGdasNumber () { return gdas_name.getGdasNumber(); }
    public boolean isGdasNameEmpty () { return gdas_name.isEmpty(); }
    public String getGdasNameAsString () { return gdas_name.toString(); }
    public int getNAddresses () { return address_list.size (); }
    public GDASAddressConfig getAddress (int index) { return address_list.get (index); }
    public int getSdasHChannelIndex () { return sdas_h_chan_index; }
    public int getSdasDChannelIndex () { return sdas_d_chan_index; }
    public int getSdasZChannelIndex () { return sdas_z_chan_index; }
    public int getSdasTChannelIndex () { return sdas_t_chan_index; }
    public int getSdasFChannelIndex () { return sdas_f_chan_index; }
    public OutputFileFormat getOutputFileFormat() { return output_file_format; }
    public int getWatchdogTimeout () { return watchdog_timeout; }
    public int getMaximumDuration () { return max_duration; }
    public long getMaximumDurationMs () { return max_duration_ms; }
    public int getCollectDelay() { return collect_delay; }
    public boolean isDataCollectionEnabled () { return enable_data_collection; }
    public boolean isMeanCalculationEnabled () { return enable_mean_calculation; }
    public GDASStatus getGdasStatus () { return gdas_status; }

    public void removeAddress (int index) { address_list.remove(index); }
    public void removeAddress (Object obj) { address_list.remove(obj); }
    public void addAddress (GDASAddressConfig new_address) { address_list.add (new_address); }
    public void setSdasHChannelIndex (int sdas_h_chan_index) { this.sdas_h_chan_index = sdas_h_chan_index; }
    public void setSdasDChannelIndex (int sdas_d_chan_index) { this.sdas_d_chan_index = sdas_d_chan_index; }
    public void setSdasZChannelIndex (int sdas_z_chan_index) { this.sdas_z_chan_index = sdas_z_chan_index; }
    public void setSdasTChannelIndex (int sdas_t_chan_index) { this.sdas_t_chan_index = sdas_t_chan_index; }
    public void setSdasFChannelIndex (int sdas_f_chan_index) { this.sdas_f_chan_index = sdas_f_chan_index; }
    public void setOutputFileFormat (OutputFileFormat output_file_format) { this.output_file_format = output_file_format; }
    public void setWatchdogTimeout (int watchdog_timeout) { this.watchdog_timeout = watchdog_timeout; }
    public void setMaximumDuration (int max_duration) 
    { 
        this.max_duration = max_duration; 
        max_duration_ms = (long) max_duration * 1000l;
    }
    public void setCollectDelay (int collect_delay) { this.collect_delay = collect_delay; }
    public void setDataCollectionEnabled (boolean flag) { enable_data_collection = flag; }
    public void setMeanCalculationEnabled (boolean flag) { enable_mean_calculation = flag; }
    public void setGdasStatus (GDASStatus gdas_status) { this.gdas_status = gdas_status; }

    /** check if the name is equal to the name in another object */
    public boolean nameEquals (GDASConfig o)
    {
        return gdas_name.equals(o.gdas_name);
    }
    
    /** call after fields have been set - this routine
     * then sets the address in use flag for each address */
    public void setAddressesInUse ()
    {
        int count;
        
        for (count=0; count<address_list.size(); count++)
        {
            address_list.get(count).setAddressInUse();
        }
    }

}
