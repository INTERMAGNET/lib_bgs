/*
 * CollectionConfig.java
 *
 * Created on 11 November 2005, 14:36
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

import bgs.geophys.library.Gdas.GdasCollect.Status.CollectionStatus;
import bgs.geophys.library.Gdas.GdasCollect.XMLException;
import bgs.geophys.library.Gdas.GdasCollect.XStreamPlus;
import java.io.*;
import java.util.*;
import java.text.*;

import bgs.geophys.library.ssh.*;
import com.thoughtworks.xstream.core.BaseException;

/**
 * Class to hold a list of details for GDAS collection operations. Includes methods to
 * read XML encoded configurations from disk. This class uses other classes
 * in the package in the following hierarchy:
 * 
 * CollectionConfig - an entire configuration
 *   GDASConfig - a single GDAS system
 *     GDASName - it is convenient to have the station code and GDAS number,
 *                which make up the name, in a separate object
 *     GDASAddressConfig - the address of a GDAS system (on system may have multiple addresses)
 *   ScriptConfig - description of a script that is called when new data
 *                   is available
 *
 * @author  smf
 */
public class CollectionConfig
{

    /** list of different line termination types */
    public enum TerminationType {WINDOWS, UNIX, NATIVE}
    
    /** list of different file locking types */
    public enum FileLockingType {NONE, FILE_LEVEL, RECORD_LEVEL}
    
//    /** list of 1 second output formats */
//    public enum OutputFileFormat {HUNDRED_PT, ONE_PT}
    
    /** the file used to read/write the configuration - will
     * be null if the configuration has not ben read or written */
    public File config_file;
    
    /** configuration variable: the path to the directory where data is written - 
     * if being used on bgsobs this should be something like /users/bgsobs/data/second/reported/gdas/
     * since data is written below this as <year>/<station>/<file> */
    private String base_dir;
    /** configuration variable: code for the type of record termination */
    private TerminationType record_term_code;
    /** configuration variable: code for the type of output file format */
//    private OutputFileFormat output_file_format;    
    /** configuration variable: code for the type of record locking */    
    private FileLockingType file_lock_code;
    /** configuration variable: directory for log files (null = no log file) - only used by background processor */
    private String log_dir;
    /** configuration variable: if true write log to stderr */
    private boolean write_to_stderr;
    /** configuration variable: directory to write most-recent-retrieved-data files to - these are the files
     *  that hold the dates and times of the most recently retrieved data sample for each system */
    private String mrrd_dir;
    /** configuration variable: name of an ssh server to route all traffic through - must include
     *  username and password in the form user@host,password */
    private String ssh_server;
    /** configuration variable: true to use the ssh server (if it is non-blank) */
    private boolean use_ssh_server;
    /** configuration variable: true to kill any running scripts when program exits */
    private boolean kill_scripts_on_exit;
    /** configuration variable: array of GDAS systems to collect data from */    
    private Vector<GDASConfig> system_list;
    /** configuration variable: array of scripts to run when new data arrives */
    private Vector<ScriptConfig> script_list;
    
    /** a link to the associated status object */
    private CollectionStatus collection_status;
    
    // private members
    private XStreamPlus xstream_plus;

//    /** debugging */
//    public static void main (String args[])
//    {
//        String filename;
//        CollectionConfig config, config2;
//        CollectionStatus status, status2;
//        GDASConfig system;
//        GDASStatus sys_status;
//        ScriptConfig script;
//
//        try
//        {
//            filename = "c:\\Documents and Settings\\smf\\Desktop\\GdasCollectSettings.xml";
//            config = new CollectionConfig ();
//        
//            system = new GDASConfig ();
//            system.setGdasName (new GDASName ("Esk", 1));
//            system.addAddress (new GDASAddressConfig ("gdas1.nerc-eskdalemuir.ac.uk"));
//            system.addAddress (new GDASAddressConfig ("217.192.171.66"));
//            config.addSystem(system);
//        
//            script = new ScriptConfig ("New-minute-fg-data");
//            script.setScriptType(ScriptConfig.ScriptType.NEW_MINUTE_FLUXGATE_DATA);
//            script.addSystemName (new GDASName ("Esk", 1));
//            config.addScript (script);
//        
//            config.writeConfig(filename);
//            config2 = new CollectionConfig (filename);
//
//            
//            filename = "c:\\Documents and Settings\\smf\\Desktop\\GdasCollectStatus.xml";
//            status = new CollectionStatus (config);
//            status.writeStatusAsXML(filename);
//            status2 = new CollectionStatus ();
//            status2.readStatusFromXML(filename);
//        }
//        catch (Exception e) { e.printStackTrace(); }
//    }
    
    /** Creates a new instance of CollectionConfig with default settings */
    public CollectionConfig ()
    {
        setDefaults ();
        configureXML ();
        
        config_file = null;
    }

    /** Creates a new instance of CollectionConfig by parsing XML passed in a string
     * @param xml the string where the configuration is stored
     * @param check_bd - true to create and check that the base directory exists, false otherwise
     * @throws FileNotFoundException if the base data directory could not be accessed (and check_bd is true)
     * @throws ConfigException if the configuration file is incorrect
     * @throws XMLException if there is an XML parse error */
    public CollectionConfig (String xml, boolean check_bd)
    throws FileNotFoundException, ConfigException, XMLException
    {
        int count;
        
        // set default values
        setDefaults ();
        configureXML ();
        
        // parse XML data
        try { xstream_plus.fromXML(xml, this); }
        catch (BaseException e) { throw new XMLException (e); }
        checkConfig (check_bd);
        setAddressesInUse ();        
        
        config_file = null;
    }
    
    /** Creates a new instance of CollectionConfig by parsing a configuration file
     * @param file the file where the configuration is stored
     * @param check_bd - true to create and check that the base directory exists, false otherwise
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed (and check_bd is true)
     * @throws IOException if there was an IO error
     * @throws ConfigException if the configuration file is incorrect
     * @throws XMLException if there is an XML parse error */
    public CollectionConfig (File file, boolean check_bd)
    throws FileNotFoundException, IOException, ConfigException, XMLException
    {    
        int count;
    
        // set default values
        setDefaults ();
        configureXML ();
        
        // read XML data
        xstream_plus.XMLFromFile (this, file);
        checkConfig (check_bd);
        setAddressesInUse ();        
        
        // record the config file
        config_file = file;
    }
    
    /** get the configuration as an XML string
     * @throws XMLException if there is an XML creations error */
    public String getAsXML ()
    throws XMLException
    {
        String xml;
        
        try { xml = xstream_plus.toXML (this); }
        catch (BaseException e) { throw new XMLException (e); }
        return xml;
    }
    
    /** write the current configuration to the stored file
     * @throws FileNotFoundException if there is not stored file
     * @throws IOException if there was an IO error
     * @throws XMLException if there is an XML creation error */
    public void writeConfig ()
    throws FileNotFoundException, IOException, XMLException
    {
        if (config_file == null)
            throw new FileNotFoundException ("No file name has been set");
        xstream_plus.XMLToFile(this,config_file);
    }
    
    /** write the current configuration to file
     * @param file the file to write
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed
     * @throws IOException if there was an IO error
     * @throws XMLException if there is an XML creation error */
    public void writeConfig (File file)
    throws FileNotFoundException, IOException, XMLException
    {
        xstream_plus.XMLToFile(this, file);
        
        config_file = file;
    }

    /** get the number of GDAS systems */
    public int getNGDASSystems () { return system_list.size(); }
    
    /** get a specific configuration */
    public GDASConfig getGDASSystem (int index)
    {
        if (index < 0 || index > system_list.size()) return null;
        return system_list.get(index);
    }
    
    /** remove a configuration */
    public void removeSystem (int index)
    {
        if (index >= 0 && index < system_list.size()) system_list.remove(index);
    }
    
    /** remove a configuration */
    public boolean removeSystem (GDASConfig system)
    {
        return system_list.remove(system);
    }
    
    /** add a configuration */
    public void addSystem (GDASConfig details)
    throws NameInUseException
    {
        if (isGdasNameInUse (details.getGdasStationCode(), details.getGdasNumber()))
            throw new NameInUseException ("Name already exists: " + details.getGdasNameAsString());
        system_list.add (details);
        Collections.sort (system_list); 
    }
    
    /** replace a configuration */
    public void replaceSystem (GDASConfig old_details, GDASConfig new_details)
    throws NameInUseException
    {
        boolean found;
        NameInUseException niu_exception;

        found = system_list.remove(old_details);
        if (isGdasNameInUse (new_details.getGdasStationCode(), new_details.getGdasNumber()))
        {
            if (found) system_list.add (old_details);
            niu_exception = new NameInUseException ("Name already exists: " + new_details.getGdasNameAsString());
        }
        else
        {
            system_list.add (new_details);
            niu_exception = null;
        }
        Collections.sort (system_list); 
        if (niu_exception != null) throw niu_exception;
    }
    
    /** get a list of the systems */
    public Vector<GDASName> getAllGDASNames ()
    {
        int count;
        Vector<GDASName> ret_val;
        GDASConfig system;
        GDASName name;
        
        ret_val = new Vector<GDASName> ();
        for (count=0; count<system_list.size(); count++)
        {
            system = system_list.get (count);
            name = new GDASName (system.getGdasStationCode(), system.getGdasNumber());
            ret_val.add (name);
        }
        return ret_val;
    }
    
    /** get the number of new data scripts */
    public int getNScripts () { return script_list.size(); }
    
    /** get a specific script */
    public ScriptConfig getScript (int index) 
    {
        if (index < 0 || index > script_list.size()) return null;
        return script_list.get(index);
    }
    
    /** get all scripts */
    public Vector<ScriptConfig> getScripts ()
    {
        return script_list;
    }
    
    /** remove a script */
    public void removeScript (int index)
    {
        if (index >= 0 && index < script_list.size()) script_list.remove(index);
    }
    
    /** remove a script */
    public boolean removeScript (ScriptConfig script)
    {
        return script_list.remove(script);
    }
    
    /** add a configuration */
    public void addScript (ScriptConfig script)
    throws NameInUseException
    {
        if (isScriptNameInUse(script.getScriptName()))
            throw new NameInUseException ("Name already exists: " + script.getScriptName());
        script_list.add (script);
        Collections.sort (script_list); 
    }
    
    /** replace a script */
    public void replaceScript (ScriptConfig old_script, ScriptConfig new_script)
    throws NameInUseException
    {
        boolean found;
        NameInUseException niu_exception;

        found = script_list.remove(old_script);
        if (isScriptNameInUse (new_script.getScriptName()))
        {
            if (found) script_list.add (old_script);
            niu_exception = new NameInUseException ("Name already exists: " + new_script.getScriptName());
        }
        else
        {
            script_list.add (new_script);
            niu_exception = null;
        }
        Collections.sort (script_list); 
        if (niu_exception != null) throw niu_exception;
    }
    
    /** Getter for property base_dir.
     * @return Value of property base_dir. */
    public java.lang.String getBaseDataDirectory() { return base_dir; }

    /** Getter for property record_term_code.
     * @return Value of property record_term_code. */
    public TerminationType getRecordTerminationType() { return record_term_code; }
    
    /** Getter for property output_file_format code.
     * @return Value of property output_file_format code. */
//    public OutputFileFormat getOutputFileFormat() { return output_file_format; }

    /** Getter for property file_lock_code.
     * @return Value of property file_lock_code. */
    public FileLockingType getFileLockCode() { return file_lock_code; }

    /** Getter for property log_dir.
     * @return Value of property base_dir. */
    public java.lang.String getLogDirectory() { return log_dir; }

    /** Getter for property write_to_stderr.
     * @return Value of property write_to_stderr. */
    public boolean isWriteLogToStderr() { return write_to_stderr; }
    
    /** Getter for property mrrd_dir
     * @return Value of property mrrd_dir. */
    public java.lang.String getMRRDDirectory() { return mrrd_dir; }

    /** Getter for property ssh_server
     * @return Value of property ssh_server */
    public String getSSHServer () { return ssh_server; }
    
    /** Getter for property use_ssh_server
     * @return Value of property use_ssh_server */
    public boolean isUseSSHServer () { return use_ssh_server; }
    
    /** Getter for property kill_scripts_on_exit
     * @return Value of property kill_scripts_on_exit */
    public boolean isKillScriptsOnExit () { return kill_scripts_on_exit; }
    
    /** getter for property collection_status */
    public CollectionStatus getCollectionStatus () { return collection_status; }
    
    /** setter property for base_dir */
    public void setBaseDataDirectory (String base_dir) { this.base_dir = base_dir; }
    
    /** setter property for record_term_code */
    public void setRecordTerminationType (TerminationType record_term_code) { this.record_term_code = record_term_code; }
    
    /** setter property for output_file_format code */
//    public void setOutputFileFormat (OutputFileFormat ouput_file_fmt) { this.output_file_format = ouput_file_fmt; }
    
    /** setter property for file_lock_code */
    public void setFileLockCode (FileLockingType file_lock_code) { this.file_lock_code = file_lock_code; }
    
    /** setter property for log_dir */
    public void setLogDirectory (String log_dir) { this.log_dir = log_dir; }
    
    /** setter property for write_to_stderr flag */
    public void setWriteToStderr (boolean write_to_stderr) { this.write_to_stderr = write_to_stderr; }
    
    /** setter property for mrrd_dir */
    public void setMRRDDirectory (String mrrd_dir) { this.mrrd_dir = mrrd_dir; }

    /** setter fo property ssh_server */
    public void setSSHServer (String ssh_server) { this.ssh_server = ssh_server; }
    
    /** setter for property use_ssh_server */
    public void setUseSSHServer (boolean use_ssh_server) { this.use_ssh_server = use_ssh_server; }
    
    /** setter for property kill_scripts_on_exit */
    public void setKillScriptsOnExit (boolean kill_scripts_on_exit) { this.kill_scripts_on_exit = kill_scripts_on_exit; }
    
    /** setter for property collection_status */
    public void setCollectionStatus (CollectionStatus collection_status) { this.collection_status = collection_status; }
    
    /** get the current confiruration file name */
    public File getConfigFile () { return config_file; }
    
    /** method to check that a gdas name is not already in use */
    public boolean isGdasNameInUse (String station_code, int gdas_number) 
    {
        int count;
        GDASConfig system;
        
        for (count=0; count<system_list.size(); count++)
        {
            system = system_list.get(count);
            if (system.getGdasStationCode().equalsIgnoreCase(station_code) &&
                system.getGdasNumber() == gdas_number)
                return true;
        }
        
        return false;
    }
    
    /** method to check that a script name is not already in use */
    public boolean isScriptNameInUse (String script_name) 
    {
        int count;
        ScriptConfig script;
        
        for (count=0; count<script_list.size(); count++)
        {
            script = script_list.get(count);
            if (script.getScriptName().equalsIgnoreCase(script_name))
                return true;
        }
        
        return false;
    }
    
    /** what to show for this object */
    @Override
    public String toString ()
    {
        int count;
        String ret_val;
        GDASConfig system;
        
        ret_val = null;
        for (count=0; count<system_list.size(); count++)
        {
            system = system_list.get (count);
            if (count == 0) ret_val = system.toString();
            else ret_val = ret_val + " " + system.toString();
        }
        return ret_val;
    }
    
    /** check the configuration */
    public void checkConfig (boolean check_bd)
    throws ConfigException, FileNotFoundException
    {
        int count, count2;
        File dir;
        GDASConfig system, system2;
        GDASAddressConfig address;
        ScriptConfig script, script2;
        
        // check that the ssh string includes a user and password
        if (ssh_server.length() > 0) 
        {
            if (sshTunnel.findHostInHostname(ssh_server) == null)
                throw new ConfigException("Missing host name in SSH string");
            if (sshTunnel.findUsernameInHostname(ssh_server) == null)
                throw new ConfigException("Missing user name in SSH string");
            if (sshTunnel.findPasswordInHostname(ssh_server) == null)
                throw new ConfigException("Missing password in SSH string");
        }
                
        // check that the base data directory is accessible
        if (check_bd)
        {
            dir = new File (base_dir);
            dir.mkdirs();
            if (! dir.isDirectory()) throw new FileNotFoundException ("Unable to access directory " + dir.getPath());
        }
        
        // check the systems
        for (count=0; count<system_list.size(); count++)
        {
            system = system_list.get (count);
            
            // check the configuration
            if (system.isGdasNameEmpty())
                throw new ConfigException ("Missing station code or GDAS number - number of system in configuration file = " + Integer.toString (count));
            if (system.getNAddresses() <= 0)
                throw new ConfigException ("No host names given for GDAS " + system.getGdasNameAsString());
            if (system.getSdasHChannelIndex() < 0)
                throw new ConfigException ("Bad H channel index for GDAS " + system.getGdasNameAsString());
            if (system.getSdasDChannelIndex() < 0)
                throw new ConfigException ("Bad D channel index for GDAS " + system.getGdasNameAsString());
            if (system.getSdasZChannelIndex() < 0)
                throw new ConfigException ("Bad Z channel index for GDAS " + system.getGdasNameAsString());
            if (system.getSdasTChannelIndex() < 0)
                throw new ConfigException ("Bad T channel index for GDAS " + system.getGdasNameAsString());
            if (system.getSdasFChannelIndex() < 0)
                throw new ConfigException ("Bad F channel index for GDAS " + system.getGdasNameAsString());
            if (system.getWatchdogTimeout() < (system.getCollectDelay() *2))
                throw new ConfigException ("Bad watchdog timeout value for GDAS " + system.getGdasNameAsString());
            if (system.getMaximumDuration() < 300)
                throw new ConfigException ("Bad maximum duration for GDAS " + system.getGdasNameAsString());
            system.setMaximumDuration(system.getMaximumDuration()); // this call sets the maximum_duration_ms member correctly
            if (system.getCollectDelay() < 10)
                throw new ConfigException ("Bad IntraCollectionDelay for GDAS " + system.getGdasNameAsString());
            
            // check that the system names are unique
            for (count2=count+1; count2<system_list.size(); count2++)
            {
                system2 = system_list.get(count2);
                if (system.nameEquals(system2))
                    throw new ConfigException ("Duplicate station code / GDAS number " + system.getGdasNameAsString());
            }
            
            // check the addresses
            for (count2=0; count2<system.getNAddresses(); count2++)
            {
                address = system.getAddress(count2);
                
                // check the configuration
                if (address.getIpPort() < 1)
                    throw new ConfigException ("Bad IPPort for GDAS " + system.toString() + ", address " + address.toString());
                if (address.getConnectionCheckDelay() < 1)
                    throw new ConfigException ("Bad IntraTestDelay for GDAS " + system.toString() + ", address " + address.toString());
                if (address.getSdasConnectionShutdownPeriod() < 1)
                    throw new ConfigException ("Bad CollectionsPerConnection for GDAS " + system.toString() + ", address " + address.toString());
            }
        }
        
        // check the scripts
        for (count=0; count<script_list.size(); count++)
        {
            script = script_list.get (count);
            
            // check the configuration
            if (script.getScriptName() == null)
                throw new ConfigException ("Missing script name");
            if (script.getScriptName().length() <= 0)
                throw new ConfigException ("Missing script name");
            if (script.getScriptPath() == null)
                throw new ConfigException ("Missing script path for script " + script.toString());
            if (script.getScriptPath().length() <= 0)
                throw new ConfigException ("Missing script path for script " + script.toString());

            // check that the script names are unique
            for (count2=count+1; count2<script_list.size(); count2++)
            {
                script2 = script_list.get(count2);
                if (script.nameEquals (script2))
                    throw new ConfigException ("Duplicate script " + script.toString());
            }
        }

        // sort the list of systems and scripts
        Collections.sort (system_list); 
        Collections.sort (script_list); 
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // static code below here
    ////////////////////////////////////////////////////////////////////////////////////////
    
    /** get the default configuration filename, which will be in the directory pointed
     * to by $GDAS_CFG_DIR - GDAS_CFG_DIR may be an environment variable or a system
     * property (environment variable overrides system property)
     * @return the filename, which will be in the directory pointed to by $GDAS_CFG_DIR
     * @throws ParserConfigurationException if $GDAS_CFG_DIR does not exist */
    public static String makeDefaultConfigPathName ()
    throws ConfigException
    {
        String key_name, key_value;
        
        key_name = "GDAS_CFG_DIR";
        key_value = System.getenv (key_name);
        if (key_value == null)
            key_value = System.getProperty (key_name);
        if (key_value == null)
            throw new ConfigException ("Can't find environemnt variable or property: " + key_name);
        
        return key_value + File.separator + "GDASCollect.cfg";
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////
    // private code below here
    ////////////////////////////////////////////////////////////////////////////////////////

    /** call after fields have been set - this routine
     * then sets the address in use flag for each address */
    public void setAddressesInUse ()
    {
        int count;
        
        for (count=0; count<system_list.size(); count++)
        {
            system_list.get(count).setAddressesInUse();
        }
    }
    
    /** set default configuration */
    private void setDefaults ()
    {
        base_dir = ".";
        record_term_code = TerminationType.NATIVE;
        file_lock_code = FileLockingType.FILE_LEVEL;
//        output_file_format = OutputFileFormat.HUNDRED_PT;
        log_dir = null;
        write_to_stderr = false;
        mrrd_dir = ".";
        ssh_server = "";
        use_ssh_server = false;
        kill_scripts_on_exit = false;
        
        system_list = new Vector<GDASConfig> ();
        script_list = new Vector<ScriptConfig> ();
        
        collection_status = null;
    }
    
    private void configureXML ()
    {
        xstream_plus = XStreamPlus.makeXstreamPlus();
        
        // XML configuration for CollectionConfig
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                     "GDASCollectionConfiguration");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "xstream_plus",         "");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "collection_status",    "");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "config_file",          "");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "write_to_stderr",      "WriteLogToStderr");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "record_term_code",     "RecordTerminationCode");
//        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "ouput_file_format",    "OutputFileFormat");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "file_lock_code",       "FileLockCode");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "base_dir",             "BaseDirectory");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "log_dir",              "LogDirectory");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "mrrd_dir",             "MRRDDirectory");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "ssh_server",           "SSHServer");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "use_ssh_server",       "UseSSHServer");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "kill_scripts_on_exit", "KillScriptsOnExit");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "system_list",          "GdasList");
        xstream_plus.configField (CollectionConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_list",          "ScriptList");
        
        // XML configuration for GDASConfig
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                        "Gdas");        
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "max_duration_ms",         "");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "gdas_status",             "");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "sdas_h_chan_index",       "HChannel");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "sdas_d_chan_index",       "DChannel");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "sdas_z_chan_index",       "ZChannel");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "sdas_t_chan_index",       "TChannel");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "sdas_f_chan_index",       "FChannel");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "output_file_format",      "OutputFileFormat");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "watchdog_timeout",        "WatchdogTimeout");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "max_duration",            "MaximumTransferDuration");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "collect_delay",           "IntraCollectionDelay");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "enable_data_collection",  "EnableDataCollection");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "enable_mean_calculation", "EnableMeanCalculation");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "gdas_name",               "Name");
        xstream_plus.configField (GDASConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "address_list",            "AddressList");
        
        // XML configuration for GDASAddressConfig
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                          "Address");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "global_address_list",       "");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "gdas_address_status",       "");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "host_name",                 "HostName");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "ip_port",                   "IPPort");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "connection_check_delay",    "IntraTestDelay");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "sdas_conn_shutdown_period", "CollectionsPerConnection");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "socket_timeout",            "SocketTimeout");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "user_in_use",               "InUse");
        xstream_plus.configField (GDASAddressConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "hostname_check",            "HostnameCheck");
        
        // XML configuration for GDASName
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",              "SystemName");
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "station_code",  "StationCode");
        xstream_plus.configField (GDASName.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "gdas_no",       "GDASNumber");
    
        // XML configuration for ScriptConfig
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_CLASS_FIELD, "",                        "Script");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_OMIT_MEMBER,      "global_script_name_list", "");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_ATTRIBUTE,   "script_name",             "Name");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_type",             "Type");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "system_name_list",        "SystemNameList");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "unique_check",            "CheckForUniqueness");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "max_elapse_time",         "MaximumTimeAllowed");
        xstream_plus.configField (ScriptConfig.class, XStreamPlus.ACTION_MAKE_FIELD,       "script_path",             "ScriptPath");
    }
    
}
