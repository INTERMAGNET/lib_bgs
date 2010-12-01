/*
 * MRRDFile.java
 *
 * Created on 11 November 2005, 17:23
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * A class to hold the data and time of the Most Recently Retrieved Data 
 * (MRRD) from a GDAS system. A number of different MRRDs are supported - 
 * there are one second and one minute proton and fluxgate data and
 * script execution. A note on script execution - there needs to be an
 * MRRD file for each station:gdas_no combination, so that script
 * failure can be properly handled
 *
 * @author  smf
 */
public class MRRDFile 
{
    /** code for the type of proton/fluxgate MRRD: one second fluxgate data */
    public static final int MRRD_TYPE_FLUXGATE_SECOND = 1;
    /** code for the type of proton/fluxgate MRRD: one second proton data */
    public static final int MRRD_TYPE_PROTON_SECOND = 2;
    /** code for the type of proton/fluxgate MRRD: one minute fluxgate data */
    public static final int MRRD_TYPE_FLUXGATE_MINUTE = 3;
    /** code for the type of proton/fluxgate MRRD: one minute proton data */
    public static final int MRRD_TYPE_PROTON_MINUTE = 4;

    
    // date/time of most recently retrieved data
    private Date mrrd;
    
    private File mrrd_file;
    private SimpleDateFormat date_time_format;
    
    /** Creates a new instance of MRRDFile for proton and fluxgate data
     * @param mrrd_dir the directory where the mrrd files are stored
     * @param system_name the name of the system
     * @param system_number the number of the system
     * @param type one of the MRRD_TYPE_... codes */
    public MRRDFile (String mrrd_dir, String system_name, 
                     int system_number, int type) 
    {
        commonInitialisation (mrrd_dir + File.separator + system_name + "_" +
                              Integer.toString (system_number) + "_", type);
    }

    /** Creates a new instance of MRRDFile for a script
     * @param mrrd_dir the directory where the mrrd files are stored
     * @param script_name the name of the script
     * @param station_code the station code for the system
     * @param gdas_number the GDAS number for the system
     * @param type one of the MRRD_TYPE_... codes */
    public MRRDFile (String mrrd_dir, String script_name,
                     String station_code, int gdas_number, int type) 
    {
        commonInitialisation (mrrd_dir + File.separator + station_code + "_" + 
                              Integer.toString (gdas_number) + "_" + 
                              script_name, type);
    }

    private void commonInitialisation (String prefix, int type)
    {
        Date start_date;
        int force_period;
        long time_ms;
        boolean create_file;
        String type_string;
        
        date_time_format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

        // set the type used in the filename stringm also set force_period
        // to the period (in seconds) that the 
        // MRRD should be forced to e.g. for minute data, set to 
        // 60, for 10-second data set to 10 - to not use set -ve */
        switch (type)
        {
        case MRRD_TYPE_FLUXGATE_SECOND:
            type_string = "fgs"; 
            force_period = -1;
            break;
        case MRRD_TYPE_PROTON_SECOND:   
            type_string = "prs"; 
            force_period = -1;
            break;
        case MRRD_TYPE_FLUXGATE_MINUTE: 
            type_string = "fgm"; 
            force_period = 60;
            break;
        case MRRD_TYPE_PROTON_MINUTE:   
            type_string = "prm"; 
            force_period = 60;
            break;
        default:                        
            type_string = "unk"; 
            force_period = -1;
            break;
        }
        mrrd_file = new File (prefix + type_string + ".mrrd");
        create_file = false;
        if (! read()) create_file = true;
        if (create_file)
        {
            start_date = new Date ();
            time_ms = start_date.getTime ();
            time_ms -= time_ms % 60000;                 // start on the nearest whole minute
            start_date = new Date (time_ms - 60000);    // start one minute ago
            mrrd = new Date (time_ms);
        }
        if (force_period > 0)
        {
            time_ms = mrrd.getTime();
            time_ms -= time_ms % ((long) force_period * 1000l);
            mrrd.setTime (time_ms);
        }
        write ();        
    }
    
    public Date getMRRD () { return mrrd; }
    
    public boolean updateMRRD (Date new_date)
    {
        mrrd = new_date;
        return write ();
    }
    
    public boolean incrementMRRD (long millisecs)
    {
        return updateMRRD (new Date (mrrd.getTime() + millisecs));
    }
    
    /** read the MRRD data from file
     * @return true if the file could be read */
    private boolean read ()
    {
        BufferedReader reader;
        boolean ret_val;

        ret_val = true;
        reader = null;
        try
        {
            reader = new BufferedReader (new FileReader (mrrd_file));
            mrrd = date_time_format.parse(reader.readLine());
        }
        catch (FileNotFoundException e) { ret_val = false; }
        catch (IOException e) { ret_val = false; }
        catch (ParseException e) { ret_val = false; }
        catch (NumberFormatException e) { ret_val = false; }
        
        try
        {
            if (reader != null) reader.close ();
        }
        catch (IOException e) { }
        
        return ret_val;
    }

    /** write the MRRD data from file
     * @return true if the file could be read */
    private boolean write ()
    {
        PrintStream writer;
        boolean ret_val;

        ret_val = true;
        writer = null;
        try
        {
            writer = new PrintStream (new FileOutputStream (mrrd_file));
            writer.println (date_time_format.format(mrrd));
            if (writer.checkError()) ret_val = false;
        }
        catch (FileNotFoundException e) { ret_val = false; }

        if (writer != null) writer.close ();
        
        return ret_val;
    }
    
}
