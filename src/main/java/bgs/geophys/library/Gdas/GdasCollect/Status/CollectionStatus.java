/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

import bgs.geophys.library.Gdas.GdasCollect.Config.CollectionConfig;
import bgs.geophys.library.Gdas.GdasCollect.Config.GDASConfig;
import bgs.geophys.library.Gdas.GdasCollect.Config.GDASName;
import bgs.geophys.library.Gdas.GdasCollect.Config.ScriptConfig;
import bgs.geophys.library.Gdas.GdasCollect.GdasCollectVersion;
import bgs.geophys.library.Gdas.GdasCollect.XMLException;
import bgs.geophys.library.Gdas.GdasCollect.XStreamPlus;
import bgs.geophys.library.Threads.ProcessMonitor;
import bgs.geophys.library.Threads.TimedProcessMonitor;
import com.thoughtworks.xstream.core.BaseException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Class to hold the status of GDAS collection operations. Includes methods to
 * transmit and receive the status as an XML stream. This class uses other classes
 * in the package in the following hierarchy:
 * 
 * CollectionStauts - status of the entire syste,
 *   GDASStatus - status of a single GDAS system
 *     GDASAddressStatus - status of a particular conection to a GDAS system
 *     ScriptStatus - status of a particular script that is run to process new data
 *                    for this GDAS system
 *
 * @author smf
 */
public class CollectionStatus 
implements StatusAlarm
{
    /** codes for the state of collection operations */
    public enum CollectionState
    {
        COLLECTION_UNKNOWN, COLLECTION_STOPPED, COLLECTION_STARTING,
        COLLECTION_RUNNING, COLLECTION_STOPPING
    }
    
    // copies of configuration fields
    private String config_filename;
    private String base_dir;
    private String ip_address;
    private String log_dir;
    private String mrrd_dir;
    private String ssh_server;
    private CollectionState collection_state;

    // the collection program's version number
    private String version_no;

    // the transfer rate of data to disk (in samples / mSec)
    private double disk_transfer_rate;
    private double dtr_sd;
    private int dtr_npoints;

    // an array of status data, 1 record for each GDAS system */
    private Vector<GDASStatus> gdas_status_list;

    // an object used to serialise this data as XML
    private XStreamPlus xstream_plus;
    
    /** build an empty status object */
    public CollectionStatus ()
    {
        config_filename = "";
        base_dir = "";
        ip_address = "";
        log_dir = "";
        mrrd_dir = "";
        ssh_server = "";
        collection_state = CollectionStatus.CollectionState.COLLECTION_UNKNOWN;
        version_no = GdasCollectVersion.VERSION_NO;

        disk_transfer_rate = -1.0;
        dtr_sd = 0.0;
        dtr_npoints = 0;
        gdas_status_list = new Vector<GDASStatus> ();
        configureXML ();
    }
    
    /** build an status object from XML data
     * @throws XMLException if there is an XML parse error */
    public CollectionStatus (String xml)
    throws XMLException
    {
        configureXML ();
        
        try { xstream_plus.fromXML(xml, this); }
        catch (BaseException e) { throw new XMLException (e); }
    }
    
    /** build a new status object from the configuration */
    public CollectionStatus (CollectionConfig config)
    {
        int count, count2;
        GDASConfig gdas_config;
        GDASStatus gdas_status;
        ScriptConfig script_config;
        
        version_no = GdasCollectVersion.VERSION_NO;

        disk_transfer_rate = -1.0;
        dtr_sd = 0.0;
        dtr_npoints = 0;
        gdas_status_list = new Vector<GDASStatus> ();
        configureXML ();
        
        // create copies of config fields
        if (config.getConfigFile() == null) config_filename = "";
        else config_filename = config.getConfigFile().getAbsolutePath();
        base_dir = config.getBaseDataDirectory();
        try { ip_address = InetAddress.getLocalHost().toString(); }
        catch (UnknownHostException e) { ip_address = ""; }
        log_dir = config.getLogDirectory();
        mrrd_dir = config.getMRRDDirectory();
        if (config.isUseSSHServer()) ssh_server = config.getSSHServer();
        else ssh_server = "";
        collection_state = CollectionStatus.CollectionState.COLLECTION_UNKNOWN;
        
        // iterate over GDAS systems, creating status objects
        config.setCollectionStatus(this);
        for (count=0; count<config.getNGDASSystems(); count++)
        {
            gdas_config = config.getGDASSystem(count);
            gdas_status = new GDASStatus (config.getMRRDDirectory(), gdas_config);
            gdas_config.setGdasStatus(gdas_status);
            gdas_status_list.add (gdas_status);

            // iterate over scripts, creating status objects
            for (count2=0; count2<config.getNScripts(); count2++)
            {
                script_config = config.getScript(count2);
                if (script_config.isRunOn(gdas_config.getGdasStationCode(), gdas_config.getGdasNumber()))
                    gdas_status.addScriptStatus (script_config);
            }
        }
    }    
    
    /** copy the details of this collection status to the given
     * object - don't copy the list of status objects */
    public void copy (CollectionStatus from)
    {
        this.config_filename = from.config_filename;
        this.base_dir = from.base_dir;
        this.ip_address = from.ip_address;
        this.log_dir = from.log_dir;
        this.mrrd_dir = from.mrrd_dir;
        this.ssh_server = from.ssh_server;
        this.collection_state = from.collection_state;
        this.version_no = from.version_no;
        this.disk_transfer_rate = from.disk_transfer_rate;
        this.dtr_sd = from.dtr_sd;
        this.dtr_npoints = from.dtr_npoints;
    }
    
    /** add a GDAS system */
    public void addGdas (GDASStatus gdas_status) { gdas_status_list.add (gdas_status); }
    
    /** remove a GDAS system */
    public void removeGdas (GDASStatus gdas_status) { gdas_status_list.remove (gdas_status); }
    
    /** convert status to XML 
     * @return the XML as a string
     * @throws XMLException if there is an XML creation error */
    public String getAsXML ()
    throws XMLException
    {
        String string;
        try { string = xstream_plus.toXML (this); }
        catch (BaseException e) { throw new XMLException (e); }
        return string;
    }
    
    /** convert status to XML, checking that the XML is stable (ie that 
     * none of the nested objects have been alterred by another thread) -
     * don't call this if you are not using threads - it will be
     * inefficient - call getAsXML() instead
     * @return the XML as a string
     * @throws XMLException if there is an XML creation error */
    public String getAsStableXML ()
    throws XMLException
    {
        String xml1, xml2;
        
        xml2 = getAsXML();
        do
        {
            xml1 = xml2;
            xml2 = getAsXML();
        } while (xml1.equals(xml2));
        return xml1;
    }
    
    /** write XML status to a file
     * @param filename the name of the file to write */
    public void writeStatusAsXML (String filename) 
    throws FileNotFoundException, IOException, XMLException
    {
        xstream_plus.XMLToFile (this, filename);
    }
    
    /** read XML status from a file
     * @param filename the name of the file to read */
    public void readStatusFromXML (String filename) 
    throws FileNotFoundException, IOException, XMLException
    {
        xstream_plus.XMLFromFile (this, filename);
    }
    
    /** get the number of GDAS systems */
    public int getNGDASSystems () { return gdas_status_list.size(); }
    
    /** get a specific configuration */
    public GDASStatus getGDASSystem (int index)
    {
        if (index < 0 || index > gdas_status_list.size()) return null;
        return gdas_status_list.get(index);
    }

    // readers for config properties
    public String getConfigFilename () { return config_filename; }
    public boolean isConfigFilenameAlarm () { return false; }
    public String getBaseDir () { return base_dir; }
    public boolean isBaseDirAlarm () { return false; }
    public String getIPAddress () { return ip_address; }
    public boolean isIPAddressAlarm () { return false; }
    public boolean isIPPortAlarm () { return false; }
    public String getLogDir () { return log_dir; }
    public boolean isLogDirAlarm () { return false; }
    public String getMRRDDir () { return mrrd_dir; }
    public boolean isMRRDDirAlarm () { return false; }
    public String getSSHServer () { return ssh_server; }
    public boolean isSSHServerAlarm () { return false; }
    public CollectionStatus.CollectionState getCollectionState () { return collection_state; }
    public boolean isCollectionPausedAlarm () 
    {
        if (collection_state != CollectionStatus.CollectionState.COLLECTION_RUNNING)
            return true;
        return false;
    }
    public String getVersionNumber ()
    {
        if (version_no == null) return "Unknown";
        return version_no;
    }
    public boolean isVersionNumberAlarm ()
    {
        if (version_no == null) return true;
        if (version_no.equals(GdasCollectVersion.VERSION_NO)) return false;
        return true;
    }
    public double getDiskTransferRate () { return disk_transfer_rate; }
    public double getDiskTransferRateSD () { return dtr_sd; }
    public int getDiskTransferRateNPoints () { return dtr_npoints;}
    public boolean isDiskTransferRateAlarm () { return false; }


    // writers for status properties
    public void setCollectionState (CollectionState collection_state) {this.collection_state = collection_state; }
    public void setDiskTransferRate (double disk_transfer_rate, double dtr_sd, int dtr_n_points)
    {
        this.disk_transfer_rate = disk_transfer_rate;
        this.dtr_sd = dtr_sd;
        this.dtr_npoints = dtr_n_points;
    }
    
    /** find a system by its name */
    public GDASStatus findGDAS (String gdas_name)
    {
        int count;
        GDASStatus gdas_status;
        
        for (count=0; count<gdas_status_list.size(); count ++)
        {
            gdas_status = gdas_status_list.get (count);
            if (gdas_status.getGdasName().equals(gdas_name))
                return gdas_status;
        }
        return null;
    }
    
    /** find an address by its name */
    public GDASAddressStatus findGdasAddress (String address_name)
    {
        int count, count2;
        GDASStatus gdas_status;
        GDASAddressStatus address_status;
        
        for (count=0; count<gdas_status_list.size(); count ++)
        {
            gdas_status = gdas_status_list.get (count);
            for (count2=0; count2<gdas_status.getNAddressStatus(); count2++)
            {
                address_status = gdas_status.getAddressStatus(count2);
                if (address_status.getAddressName().equals(address_name))
                    return address_status;
            }
        }
        return null;        
    }
    
    /** find a script by its name */
    public ScriptStatus findGdasScript (String station_code, int gdas_number, String script_name)
    {
        int count, count2;
        GDASStatus gdas_status;
        ScriptStatus script_status;
        
        for (count=0; count<gdas_status_list.size(); count ++)
        {
            gdas_status = gdas_status_list.get (count);
            for (count2=0; count2<gdas_status.getNScriptStatus(); count2++)
            {
                script_status = gdas_status.getScriptStatus(count2);
                if (script_status.getStationCode().equals(station_code) &&
                    script_status.getGdasNumber() == gdas_number &&
                    script_status.getScriptName().equals(script_name))
                    return script_status;
            }
        }
        return null;        
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // private code below here
    ////////////////////////////////////////////////////////////////////////////////////////

    private void configureXML ()
    {
        xstream_plus = XStreamPlus.makeXstreamPlus();
        
        // XML configuration for CollectionStatus
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                  "GdasCollectionStatus");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_OMIT_MEMBER,      "xstream_plus",      "");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "config_filename",   "CS_ConfigFilename");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "base_dir",          "CS_BaseDir");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "ip_address",        "CS_IPAddress");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "log_dir",           "CS_LogDir");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_dir",          "CS_MRRDDir");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "ssh_server",        "CS_SSHServer");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "collection_paused", "CS_CollectionPaused");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "version_no",        "CS_VersionNumber");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "disk_transfer_rate","CS_DiskTransferRate");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "dtr_sd",            "CS_DTRSD");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "dtr_npoints",       "CS_DTRNPoints");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "gdas_status_list",  "CS_GdasList");
        xstream_plus.configField (CollectionStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_list",       "CS_ScriptList");

        // XML configuration for GDASStatus
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                         "Gdas");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "gdas_name",                "GD_GdasName");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "station_code",             "GD_StationCode");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "gdas_number",              "GD_GdasNumber");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "collect_delay",            "GD_CollectDelay");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mins_collect_delay",       "GD_MinsCollectDelay");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_dir",                 "GD_MRRDDir");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "collection_started_date",  "GD_CollectionStartedDate");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "last_collection_date",     "GD_LastCollectionDate");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "amount_fg_data_collected", "GD_AmountFgDataCollected");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "amount_pr_data_collected", "GD_AmountPrDataCollected");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_fg_1s",               "GD_MRRDFg1s");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_pr_1s",               "GD_MRRDPr1s");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_fg_1m",               "GD_MRRDFg1m");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_pr_1m",               "GD_MRRDPr1m");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "new_sec_fg_mrrd_date",     "GD_NewMMRDDateFg1s");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "new_sec_pr_mrrd_date",     "GD_NewMMRDDatePr1s");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "new_min_fg_mrrd_date",     "GD_NewMMRDDateFg1m");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "new_min_pr_mrrd_date",     "GD_NewMMRDDatePr1m");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "collection_thread_status", "GD_CollectionThreadStatus");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "address_status_list",      "GD_AddressList");
        xstream_plus.configField (GDASStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_status_list",       "GD_SystemScriptList");
        
        // XML configuration for GDASAddressStatus
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                     "Address");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "station_code",         "AD_StationCode");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "gdas_number",          "AD_dasNumber");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "address_name",         "AD_AddressName");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "in_use",               "AD_InUse");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "collection_count",     "AD_CollectionCount");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "last_open_date",       "AD_LastOpenDate");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "last_collection_date", "AD_LastCollectionDate");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "last_failure_date",    "AD_LastFailureDate");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "last_failure_details", "AD_LastFailureDetails");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "net_transfer_rate",    "AD_NetTransferRate");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "ntr_sd",               "AD_NTRSD");
        xstream_plus.configField (GDASAddressStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "ntr_npoints",          "AD_NTRNPoints");
        
        // XML configuration for MRRDFile
        xstream_plus.configField (MRRDFile.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                 "MRRD");
        xstream_plus.configField (MRRDFile.class, XStreamPlus.ACTION_OMIT_MEMBER,      "date_time_format", "");        
        xstream_plus.configField (MRRDFile.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd",             "MR_MRRDDate");
        xstream_plus.configField (MRRDFile.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_file",        "MR_MRRDFile");
    
        // XML configuration for ScriptStatus
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                      "Script");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "process_list",          "SC_ProcessList");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_name",           "SC_ScriptName");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_file_fg_1s",       "SC_MRRDFileFg1s");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_file_pr_1s",       "SC_MRRDFilePr1s");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_file_fg_1m",       "SC_MRRDFileFg1m");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_file_pr_1m",       "SC_MRRDFilePr1m");
        xstream_plus.configField (ScriptStatus.class, XStreamPlus.ACTION_MAKE_FIELD,       "most_recent_exit_code", "SC_MostRecentExitCode");
        
        // XML configuration for GDASName
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",              "SystemName");
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "station_code",  "SN_StationCode");
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "gdas_no",       "SN_GDASNumber");

        // XML configuration for ProcessMonitor
        // NBNBNB - monitor_thread is a Thread objecct, which XStream cannot serialise, so it MUST be omitted
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                     "ProcessMonitor");
        xstream_plus.configField (ProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,           "process_id_counter",   "");
        xstream_plus.configField (ProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,           "process",              "");
        xstream_plus.configField (ProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,           "monitor_thread",       "");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,      "process_id_counter",   "");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,      "process",              "");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_OMIT_MEMBER,      "monitor_thread",       "");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "start_date",           "PM_StartDate");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "command",              "PM_Command");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "stdout_data",          "PM_StdoutData");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "stderr_data",          "PM_StderrData");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "stdin_data",           "PM_StdinData");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "exit_value",           "PM_ExitValue");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "stored_io_exceptions", "PM_StoredIOExceptions");
        xstream_plus.configField (TimedProcessMonitor.class, XStreamPlus.ACTION_MAKE_FIELD,       "process_id",           "PM_ProcessID");
    }

    public boolean isAlarmed() 
    {
        int count;
        GDASStatus gdas_status;

        if (isVersionNumberAlarm()) return true;
        if (isBaseDirAlarm()) return true;
        if (isConfigFilenameAlarm()) return true;
        if (isIPAddressAlarm()) return true;
        if (isIPPortAlarm()) return true;
        if (isLogDirAlarm()) return true;
        if (isMRRDDirAlarm()) return true;
        if (isSSHServerAlarm()) return true;
        if (isCollectionPausedAlarm()) return true;
        if (isDiskTransferRateAlarm()) return true;
        for (count=0; count<gdas_status_list.size(); count++)
        {
            gdas_status = gdas_status_list.get(count);
            if (gdas_status.isAlarmed()) return true;
        }
        return false;
    }

    public String displayText() 
    {
        return "Data collection";
    }

}
