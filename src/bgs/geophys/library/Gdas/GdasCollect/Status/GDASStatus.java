/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

import bgs.geophys.library.Gdas.GdasCollect.Config.GDASAddressConfig;
import bgs.geophys.library.Gdas.GdasCollect.Config.GDASConfig;
import bgs.geophys.library.Gdas.GdasCollect.Config.ScriptConfig;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author smf
 */
public class GDASStatus 
implements StatusAlarm
{

    /** name of this GDAS system */
    private String gdas_name;
    /** station code for this GDAS system */
    private String station_code;
    /** number of this GDAS system */
    private int gdas_number;
    /** delay between collections */
    private int collect_delay;
    /** delay between collections, in minutes */
    private int mins_collect_delay;
    /** are minute means being calculated ?? */
    private boolean mean_calc_enabled;
    
    /** directory for MRRD files */
    private String mrrd_dir;
    
    /** time collection thread started */
    private Date collection_started_date;
    /** time of last collection */
    private Date last_collection_date;
    /** amount of fluxgate data collected on last collection (in seconds) */
    private int amount_fg_data_collected;
    /** amount of proton data collected on last collection (in seconds) */
    private int amount_pr_data_collected;
    /** most recently received fluxgate 1-second data */
    private MRRDFile mrrd_fg_1s;
    /** most recently received proton 1-second data */
    private MRRDFile mrrd_pr_1s;
    /** most recently calculated fluxgate 1-minute data */
    private MRRDFile mrrd_fg_1m;
    /** most recently calculated proton 1-minute data */
    private MRRDFile mrrd_pr_1m;
    /** new MRRD data/time for fluxgate 1-second data */
    private Date new_sec_fg_mrrd_date;
    /** new MRRD data/time for proton 1-second data */
    private Date new_sec_pr_mrrd_date;
    /** new MRRD data/time for fluxgate 1-minute data */
    private Date new_min_fg_mrrd_date;
    /** new MRRD data/time for proton 1-minute data */
    private Date new_min_pr_mrrd_date;
    /** status of the collection thread */
    private String collection_thread_status;
    
    private Vector<GDASAddressStatus> address_status_list;
    private Vector<ScriptStatus> script_status_list;
    
    /** build a new status object from the configuration */
    public GDASStatus (String mrrd_dir, GDASConfig gdas_config)
    {
        int count;
        GDASAddressConfig gdas_address_config;
        GDASAddressStatus gdas_address_status;

        gdas_name = gdas_config.toString();
        station_code = gdas_config.getGdasStationCode();
        gdas_number = gdas_config.getGdasNumber();
        collect_delay = gdas_config.getCollectDelay();
        mins_collect_delay = collect_delay / 60;
        if (mins_collect_delay <= 0) mins_collect_delay = 1;
        this.mrrd_dir = mrrd_dir;
        mean_calc_enabled = gdas_config.isMeanCalculationEnabled();
        
        collection_started_date = new Date ();
        last_collection_date = new Date (0l);
        amount_fg_data_collected = 0;
        amount_pr_data_collected = 0;
        
        mrrd_fg_1s = new MRRDFile (mrrd_dir, gdas_config.getGdasStationCode(), gdas_config.getGdasNumber(), MRRDFile.MRRD_TYPE_FLUXGATE_SECOND);
        mrrd_pr_1s = new MRRDFile (mrrd_dir, gdas_config.getGdasStationCode(), gdas_config.getGdasNumber(), MRRDFile.MRRD_TYPE_PROTON_SECOND);
        mrrd_fg_1m = new MRRDFile (mrrd_dir, gdas_config.getGdasStationCode(), gdas_config.getGdasNumber(), MRRDFile.MRRD_TYPE_FLUXGATE_MINUTE);
        mrrd_pr_1m = new MRRDFile (mrrd_dir, gdas_config.getGdasStationCode(), gdas_config.getGdasNumber(), MRRDFile.MRRD_TYPE_PROTON_MINUTE);
        new_sec_fg_mrrd_date = null;
        new_sec_pr_mrrd_date = null;
        new_min_fg_mrrd_date = null;
        new_min_pr_mrrd_date = null;
        collection_thread_status = "Idle";
        
        // iterate over GDAS systems, creating address status objects
        address_status_list = new Vector<GDASAddressStatus> ();
        for (count=0; count<gdas_config.getNAddresses(); count++)
        {
            gdas_address_config = gdas_config.getAddress(count);
            gdas_address_status = new GDASAddressStatus (station_code, gdas_number, gdas_address_config);
            gdas_address_config.setGdasAddressStatus(gdas_address_status);
            address_status_list.add (gdas_address_status);
        }
        
        script_status_list = new Vector<ScriptStatus> ();
    }
    
    /** copy the details of this status to the given
     * object - don't copy lists of addresses or scripts */
    public void copy (GDASStatus from)
    {
        this.gdas_name = from.gdas_name;
        this.station_code = from.station_code;
        this.gdas_number = from.gdas_number;
        this.collect_delay = from.collect_delay;
        this.mins_collect_delay = from.mins_collect_delay;
        this.mrrd_dir = from.mrrd_dir;
        this.mean_calc_enabled = from.mean_calc_enabled;
        this.collection_started_date = from.collection_started_date;
        this.last_collection_date = from.last_collection_date;
        this.amount_fg_data_collected = from.amount_fg_data_collected;
        this.amount_pr_data_collected = from.amount_pr_data_collected;
        this.mrrd_fg_1s = from.mrrd_fg_1s;
        this.mrrd_pr_1s = from.mrrd_pr_1s;
        this.mrrd_fg_1m = from.mrrd_fg_1m;
        this.mrrd_pr_1m = from.mrrd_pr_1m;
        this.new_sec_fg_mrrd_date = from.new_sec_fg_mrrd_date;
        this.new_sec_pr_mrrd_date = from.new_sec_pr_mrrd_date;
        this.new_min_fg_mrrd_date = from.new_min_fg_mrrd_date;
        this.new_min_pr_mrrd_date = from.new_min_pr_mrrd_date;
        this.collection_thread_status = from.collection_thread_status;
    }
            
    /** add a script status object */
    public void addScriptStatus (ScriptConfig script_config)
    {
        ScriptStatus script_status;
        
        script_status = new ScriptStatus (mrrd_dir, script_config, station_code, gdas_number);
        script_status_list.add (script_status);
    }
    
    // read methods for status variables
    public String getGdasName () { return gdas_name; }
    public boolean isGdasNameAlarm () { return false; }
    public String getStationCode () { return station_code; }
    public boolean isStationCodeAlarm () { return false; }
    public int getGdasNumber () { return gdas_number; }
    public boolean isGdasNumberAlarm () { return false; }
    public int getCollectDelay () { return collect_delay; }
    public boolean isCollectDelayAlarm () { return false; }
    public Date getCollectionStartedDate () { return collection_started_date; }
    public boolean isCollectionStartedDateAlarm () { return false; }
    public Date getLastCollectionDate () { return last_collection_date; }
    public boolean isLastCollectionDateAlarm () 
    {
        Date now = new Date ();
        if ((now.getTime() - last_collection_date.getTime()) > ((long) collect_delay * 5000l)) return true;
        return false; 
    }
    public int getAmountFgDataCollected () { return amount_fg_data_collected; }
    public boolean isAmountFgDataCollectedAlarm () 
    {
        if (amount_fg_data_collected <= 0) return true;
        return false; 
    }
    public int getAmountPrDataCollected () { return amount_pr_data_collected; }
    public boolean isAmountPrDataCollectedAlarm () { return false; }
    public int getNAddressStatus () { return address_status_list.size(); }
    public GDASAddressStatus getAddressStatus (int index) { return address_status_list.get (index); }
    public GDASAddressStatus findAddressStatus (String address_name)
    {
        int count;
        GDASAddressStatus address_status;
        
        for (count=0; count<address_status_list.size(); count++)
        {
            address_status = address_status_list.get(count);
            if (address_status.getAddressName().equals(address_name))
                return address_status;
        }
        return null;
        
    }
    public int getNScriptStatus () { return script_status_list.size(); }
    public ScriptStatus getScriptStatus (int index) { return script_status_list.get (index); }
    public ScriptStatus findScriptStatus (String script_name)
    {
        int count;
        ScriptStatus script_status;
        
        for (count=0; count<script_status_list.size(); count++)
        {
            script_status = script_status_list.get(count);
            if (script_status.getScriptName().equals(script_name))
                return script_status;
        }
        return null;
    }
    public MRRDFile getMRRDFg1s () { return mrrd_fg_1s; }
    public boolean isMRRDFg1sAlarm () 
    {
        Date now = new Date ();
        if ((now.getTime() - mrrd_fg_1s.getMRRD().getTime()) > ((long) collect_delay * 5000l)) return true;
        return false; 
    }
    public MRRDFile getMRRDPr1s () { return mrrd_pr_1s; }
    public boolean isMRRDPr1sAlarm ()
    {
        Date now = new Date ();
        if ((now.getTime() - mrrd_pr_1s.getMRRD().getTime()) > ((long) collect_delay * 5000l)) return true;
        return false; 
    }
    public MRRDFile getMRRDFg1m () { return mrrd_fg_1m; }
    public boolean isMRRDFg1mAlarm () 
    {
        if (! mean_calc_enabled) return false;
        Date now = new Date ();
        if ((now.getTime() - mrrd_fg_1m.getMRRD().getTime()) > ((long) mins_collect_delay * 300000l)) return true;
        return false; 
    }
    public MRRDFile getMRRDPr1m () { return mrrd_pr_1m; }
    public boolean isMRRDPr1mAlarm ()
    {
        if (! mean_calc_enabled) return false;
        Date now = new Date ();
        if ((now.getTime() - mrrd_pr_1m.getMRRD().getTime()) > ((long) mins_collect_delay * 300000l)) return true;
        return false; 
    }
    public Date getNewMRRDFg1sDate () { return new_sec_fg_mrrd_date; }
    public Date getNewMRRDPr1sDate () { return new_sec_pr_mrrd_date; }
    public Date getNewMRRDFg1mDate () { return new_min_fg_mrrd_date; }
    public Date getNewMRRDPr1mDate () { return new_min_pr_mrrd_date; }
    public String getCollectionThreadStatus () { return collection_thread_status; }
    
    // write methods for status variables
    public void setLastCollectionDate (Date date) { last_collection_date = date; }
    public void setAmountFgDataCollected (int amount) { amount_fg_data_collected = amount; }
    public void setAmountPrDataCollected (int amount) { amount_pr_data_collected = amount; }
    public void resetCollectionStartedDate () { collection_started_date = new Date (); }
    public void setNewMRRDFg1sDate (Date new_sec_fg_mrrd_date) 
    {
        this.new_sec_fg_mrrd_date = new_sec_fg_mrrd_date; 
    }
    public void setNewMRRDPr1sDate (Date new_sec_pr_mrrd_date) 
    {
        this.new_sec_pr_mrrd_date = new_sec_pr_mrrd_date; 
    }
    public void setNewMRRDFg1mDate (Date new_min_fg_mrrd_date) 
    {
        this.new_min_fg_mrrd_date = new_min_fg_mrrd_date; 
    }
    public void setNewMRRDPr1mDate (Date new_min_pr_mrrd_date) 
    {
        this.new_min_pr_mrrd_date = new_min_pr_mrrd_date; 
    }
    public void setCollectionThreadStatus (String collection_thread_status) { this.collection_thread_status = collection_thread_status; }

    public boolean equals (GDASStatus o) 
    {
        if (o == null) return false;
        if (gdas_name == null)
        {
            if (o.gdas_name == null) return true;
            return false;
        }
        if (o.gdas_name == null) return false;
        return gdas_name.equals(o.gdas_name); 
    }
    
    public int compareTo (GDASStatus o)
    {
        if (o == null) return +1;
        if (gdas_name == null)
        {
            if (o.gdas_name == null) return 0;
            return -1;
        }
        if (o.gdas_name == null) return +1;
        return gdas_name.compareTo(o.gdas_name); 
    }
    
    public boolean isAlarmed() 
    {
        int count;
        GDASAddressStatus addr_status;
        ScriptStatus script_status;

        if (isAmountFgDataCollectedAlarm()) return true;
        if (isAmountPrDataCollectedAlarm()) return true;
        if (isCollectDelayAlarm()) return true;
        if (isCollectionStartedDateAlarm()) return true;
        if (isGdasNameAlarm()) return true;
        if (isGdasNumberAlarm()) return true;
        if (isLastCollectionDateAlarm()) return true;
        if (isMRRDFg1mAlarm()) return true;
        if (isMRRDFg1sAlarm()) return true;
        if (isMRRDPr1mAlarm()) return true;
        if (isMRRDPr1sAlarm()) return true;
        if (isStationCodeAlarm()) return true;
        for (count=0; count<address_status_list.size(); count++)
        {
            addr_status = address_status_list.get(count);
            if (addr_status.isAlarmed()) return true;
        }
        for (count=0; count<script_status_list.size(); count++)
        {
            script_status = script_status_list.get(count);
            if (script_status.isAlarmed()) return true;
        }
        return false;
    }

    public String displayText() 
    {
        return gdas_name;
    }
}
