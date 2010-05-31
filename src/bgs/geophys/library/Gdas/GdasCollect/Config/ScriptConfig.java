/*
 * ScriptConfig.java
 *
 * Created on 22 November 2005, 14:11
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

import java.util.*;

/**
 * This class configures the process of running a script when new data
 * is available. Note that
 * the name for the script is not changeable - to get a new name for a
 * script you need to create a new object.
 * 
 * @author  Administrator
 */
public class ScriptConfig implements Comparable<ScriptConfig>
{

    /** list of different events that can cause a script to run */
    public enum ScriptType {NEW_RAW_FLUXGATE_DATA, NEW_RAW_PROTON_DATA,
                            NEW_RAW_DATA, NEW_MINUTE_DATA, NEW_DATA}
    
    /** list of options for ensuring that only one copy of a script is running */
    public enum UniqueCheck {NO_CHECK, DONT_RUN_NEW_PROC, KILL_OLD_PROC}
    
    /** configuration variable: the name of the script, which must be unique */
    private String script_name;
    /** configuration variable: run on new second or minute data */
    private ScriptType script_type;
    /** configuration variable: array of system names that this
     * script applies to - if this list is empty, the script applies
     * to all GDAS systems */
    private Vector<GDASName> system_name_list;
    /** configuration variable: set to one of:
     * NO_CHECK - no check is made to ensure that other copies are not running
     * DONT_RUN_NEW_PROC - if a script of this type/station/gdas# is already running, 
     *                     don't run another
     * KILL_OLD_PROC - if a script of this type/station/gdas# is already running, 
     *                 kill it befor running a new one */
    private UniqueCheck unique_check;
    /** configuration variable: if +ve, controls the maximum amount of time
     *  the script will be allowed to run (in seconds) */
    private int max_elapse_time;
    /** configuration variable: path to script that will be run */
    private String script_path;
    
    /** Creates a new instance of ScriptConfig with default (missing) values */
    public ScriptConfig () 
    {
        script_name = "";
        script_type = ScriptType.NEW_DATA;
        system_name_list = new Vector<GDASName> ();
        unique_check = UniqueCheck.NO_CHECK;
        max_elapse_time = -1;
        script_path = null;
    }
    
    /** Creates a new instance of ScriptConfig with a given name */
    public ScriptConfig (String script_name) 
    {
        this ();
        this.script_name = new String (script_name);
    }
    
    /** Creates a new instance of GDASConfig as a copy of another */
    public ScriptConfig (ScriptConfig o)
    {
        this.script_name = "";
        this.script_type = ScriptType.NEW_DATA;
        this.system_name_list = new Vector<GDASName> ();
        this.unique_check = UniqueCheck.NO_CHECK;
        this.max_elapse_time = -1;
        this.script_path = null;
    }
    
    /** Creates a new instance of GDASConfig as a copy of another, with a new name */
    public ScriptConfig (ScriptConfig o, String script_name)
    {
        this (o);
        this.script_name = new String (script_name);
    }
    
    /** show this config as a string */
    @Override
    public String toString ()
    {
        return script_name;
    }
        
    /** implementation of the comparable interface */
    public int compareTo (ScriptConfig o) 
    {
        return script_name.compareTo(o.script_name);
    }

    /** check if the name is equal to the name in another object */
    public boolean nameEquals (ScriptConfig o)
    {
        return script_name.equals(o.script_name);
    }
    
    // read methods for configuration variables
    public String getScriptName () { return script_name; }
    public ScriptType getScriptType () { return script_type; }
    public int getNSystemNames () { return system_name_list.size(); }
    public GDASName getSystemName (int count) { return system_name_list.get (count); }
    public UniqueCheck getUniqueCheck () { return unique_check; }
    public int getMaxElapseTime () { return max_elapse_time; }
    public String getScriptPath () { return script_path; }
    public boolean isRunOn (String station_code, int gdas_number)
    {
        GDASName test_name;
        
        test_name = new GDASName (station_code, gdas_number);
        return isRunOn (test_name); 
    }
    public boolean isRunOn (GDASName test_name)
    {
        int count;
        GDASName name;
        
        if (system_name_list.size() <= 0) return true;
        for (count=0; count<system_name_list.size(); count++)
        {
            name = system_name_list.get(count);
            if (name.equals(test_name)) return true;
        }
        return false;
    }

    public void setScriptType (ScriptType script_type) { this.script_type = script_type; }
    public void setUniqueCheck (UniqueCheck unique_check) { this.unique_check = unique_check; }
    public void setMaxElapseTime (int max_elapse_time) { this.max_elapse_time = max_elapse_time; }
    public void setScriptPath (String script_path) { this.script_path = script_path; }
    public void addSystemName (GDASName name) { system_name_list.add (name); }
    public void removeSystemName (int index) { system_name_list.remove (index); }
    public void removeSystemName (GDASName name) { system_name_list.remove (name); }

    /** check if data of a certain type matches the type that causes this
     * script to run
     * @param data_type the type of new data - must be one of NEW_MINUTE_DATA,
     *        NEW_RAW_FLUXGATE_DATA or NEW_RAW_PROTON_DATA -
     *        other data types are ignored */
    public boolean isDataRightType (ScriptType data_type)
    {
        switch (data_type)
        {
            case NEW_MINUTE_DATA:
                switch (script_type)
                {
                    case NEW_MINUTE_DATA:
                    case NEW_DATA:               
                        return true;
                }
                break;
            case NEW_RAW_FLUXGATE_DATA:
                switch (script_type)
                {
                    case NEW_RAW_FLUXGATE_DATA:
                    case NEW_RAW_DATA:
                    case NEW_DATA:               
                        return true;
                }
                break;
            case NEW_RAW_PROTON_DATA:
                switch (script_type)
                {
                    case NEW_RAW_PROTON_DATA:
                    case NEW_RAW_DATA:
                    case NEW_DATA:               
                        return true;
                }
                break;
        }
        return false;
    }
    
}
