/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

import bgs.geophys.library.Gdas.GdasCollect.Config.ScriptConfig;
import bgs.geophys.library.Threads.TimedProcessMonitor;
import java.util.Date;
import java.util.Vector;

/**
 * A class that holds status on a script. One of these objects should
 * be created for each script on each GDAS system.
 * 
 * @author smf
 */
public class ScriptStatus 
implements StatusAlarm
{

    /** the name of the script */
    private String script_name;
    
    /** the station code this script will be run for */
    private String station_code;
    
    /** the gdas number this script will be run for */
    private int gdas_number;
    
    /** the path to the script */
    private String script_path;
    
    /** a list of running processes */
    private Vector<TimedProcessMonitor> process_list;
    
    /** the most recent exit code - or -1 if script was killed */
    private int most_recent_exit_code;
    
    /** an MRRD file for this script on this GDAS system */
    private MRRDFile mrrd_file;

    /** build a new script status object from the configuration */
    public ScriptStatus (String mrrd_dir, ScriptConfig script_config,
                         String station_code, int gdas_number)
    {
        script_name = script_config.getScriptName();
        this.station_code = station_code;
        this.gdas_number = gdas_number;
        this.script_path = script_config.getScriptPath();
        
        process_list = new Vector<TimedProcessMonitor> ();
        most_recent_exit_code = 0;

        mrrd_file = new MRRDFile (mrrd_dir, script_config.getScriptName(),
                                  station_code, gdas_number);
    }
    
    /** copy the details of this status to the given
     * object */
    public void copy (ScriptStatus from)
    {
        this.script_name = from.script_name;
        this.station_code = from.station_code;
        this.gdas_number = from.gdas_number;
        this.process_list = from.process_list;
        this.most_recent_exit_code = from.most_recent_exit_code;
        this.mrrd_file = from.mrrd_file;
    }
    
    // read methods for status variables
    public String getScriptName () { return script_name; }
    public boolean isScriptNameAlarm () { return false; }
    public String getStationCode () { return station_code; }
    public boolean isStationCodeAlarm () { return false; }
    public int getGdasNumber () { return gdas_number;}
    public boolean isGdasNumberAlarm () { return false; }
    public String getScriptPath () { return script_path; }
    public boolean isScriptPathAlarm () { return false; }
    public int getNRunningProcesses () { return process_list.size(); }
    public boolean isNRunningProcessesAlarm () { return false; }
    public TimedProcessMonitor getRunningProcess (int index) { return process_list.get (index); }
    public boolean isRunningProcessAlarm (int index) { return false; }
    public Date getMRRDDate () { return mrrd_file.getMRRD(); }
    public boolean isMRRDDateAlarm () { return false; }
    public int getMostRecentExitCode () { return most_recent_exit_code; }
    public boolean isMostRecentExitCodeAlarm () 
    {
        if (most_recent_exit_code != 0) return true;
        return false; 
    }

    // write methods for status variables
    public void addProcess (TimedProcessMonitor process) { process_list.add (process); }
    public void removeProcess (TimedProcessMonitor process) { process_list.remove (process); }
    public void setMRRDDate (Date date) { mrrd_file.updateMRRD(date); }
    public void setMostRecentExitCode (int most_recent_exit_code) { this.most_recent_exit_code = most_recent_exit_code; }
    
    /** test if this object is equal to another */
    public boolean equals (ScriptStatus other)
    {
        if (other == null) return false;
        
        if (script_name == null)
        {
            if (other.script_name != null) return false;
        }
        else if (other.script_name == null) return false;
        else if (! this.script_name.equalsIgnoreCase(other.script_name)) return false;
        
        if (station_code == null)
        {
            if (other.station_code != null) return false;
        }
        else if (other.station_code == null) return false;
        else if (! this.station_code.equalsIgnoreCase(other.station_code)) return false;

        if (this.gdas_number != other.gdas_number) return false;
        
        return true;
    }
    
    /** compare this object to another */
    public int compareTo (ScriptStatus other)
    {
        int result;
        
        if (other == null) return +1;
        if (script_name == null)
        {
            if (other.script_name == null) return 0;
            return -1;
        }
        if (other.script_name == null) return +1;
        result = script_name.compareTo(other.script_name);
        if (result != 0) return result;
        
        if (station_code == null)
        {
            if (other.station_code == null) return 0;
            return -1;
        }
        if (other.station_code == null) return +1;
        result = station_code.compareTo(other.station_code);
        if (result != 0) return result;

        if (gdas_number < other.gdas_number) return -1;
        if (gdas_number > other.gdas_number) return +1;
        return 0;
    }
    
    /** transfer all processes to another script status object */
    public void transferProcessesTo (ScriptStatus other)
    {
        int count;
        TimedProcessMonitor pm;
        
        for (count=0; count<process_list.size(); count++)
        {
            pm = process_list.remove(count);
            other.addProcess(pm);
        }
    }

    public boolean isAlarmed() 
    {
        int count;
        
        if (isGdasNumberAlarm()) return true;
        if (isMRRDDateAlarm()) return true;
        if (isMostRecentExitCodeAlarm()) return true;
        if (isNRunningProcessesAlarm()) return true;
        for(count=0; count<getNRunningProcesses(); count++)
        {
            if (isRunningProcessAlarm(count)) return true;
        }
        if (isScriptNameAlarm()) return true;
        if (isScriptPathAlarm()) return true;
        if (isStationCodeAlarm()) return true;
        return false;
    }

    public String displayText() 
    {
        return script_name;
    }
    
}
