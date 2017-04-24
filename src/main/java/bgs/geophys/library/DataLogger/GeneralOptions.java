/*
 * GeneralOptions.java
 *
 * Created on 19 January 2004, 15:35
 */

package bgs.geophys.library.DataLogger;

import java.io.*;
import java.net.*;

import bgs.geophys.library.File.*;
import bgs.geophys.library.Swing.*;
import bgs.geophys.library.Misc.*;
import bgs.geophys.library.DataLogger.SystemList.*;

/**
 * This object holds options used by LogMon, SDASCtrl and ViewTrace
 * @author  smf
 */
public class GeneralOptions implements Serializable
{

    // the options that this object holds
    private boolean use_system_list;
    private URL system_list_update_address;
    private boolean use_data_compression;
    private boolean use_channel_names;
    private boolean show_start_dialog;

    // this object also holds a copy of the system list
    private SystemList system_list;
    
    /** Creates a new instance of GeneralOptions */
    public GeneralOptions() 
    {
        // set default values for the options
        use_system_list = false;
        if (NetworkUtils.isInBgsNetwork())
        {
            try
            {
                system_list_update_address = new URL ("http://sg.nmh.ac.uk/configs/rt_system_list.xml");
                use_system_list = true;
            }
            catch (MalformedURLException e)
            {
                system_list_update_address = null;
            }
        }
        else system_list_update_address = null;
        use_data_compression = true;
        use_channel_names = false;
        show_start_dialog = true;

        // read the options from disk
        read ();
        
        // if required, load the system list
        if (use_system_list)
        {
            system_list = new SystemList ();
            if (! system_list.readLocal ()) system_list = null;
        }
        else system_list = null;
    }

    /** Set and store the properties for this object
     * @param use_system_list if true the program should use the localc copy of the list of remote system
     * @param system_list_update_address if not null, update the local copy of the system list from this address
     * @param use_data_compression if true attempt to compress data before transferring it
     * @param use_channel_names if true use names for channels as well as numbers
     * @param show_start_dialog to show help information when programs start */
    public boolean update (boolean use_system_list, URL system_list_update_address,
                           boolean use_data_compression, boolean use_channel_names,
                           boolean show_start_dialog)
    {
      boolean ret_val;  
      SystemList new_system_list;
      

      // attempt to update system list
      ret_val = true;
      if (use_system_list && (system_list_update_address != null))
      {
          new_system_list = new SystemList ();
          if (! new_system_list.update (system_list_update_address)) ret_val = false;
          else if (! new_system_list.writeLocal()) ret_val = false;
          if (ret_val) system_list = new_system_list;
      }
          
      // set the new options
      if (ret_val)
      {
          this.use_system_list = use_system_list;
          this.system_list_update_address = system_list_update_address;
          this.use_data_compression = use_data_compression;
          this.use_channel_names = use_channel_names;
          this.show_start_dialog = show_start_dialog;

          // write options to disk
          if (! write ()) ret_val = false;
      }
      
      return ret_val;
    }
      
    /** Getter for property use_system_list.
     * @return Value of property use_system_list. */
    public boolean getUseSystemList()          { return use_system_list; }
    
    /** Getter for property system_list_update_address.
     * @return Value of property system_list_update_address - may be NULL. */
    public URL getSystemListUpdateAddress ()  { return system_list_update_address; }
    
    /** Getter for property use_data_compression.
     * @return Value of property use_data_compression. */
    public boolean getUseDataCompression()     { return use_data_compression; }

    /** Getter for property use_channel_names
     * @return Value of property use_channel_names. */
    public boolean getUseChannelNames()        { return use_channel_names; }

    /** Getter for property show_start_dialog
     * @return Value of property show_start_dialog */
    public boolean getShowStartDialog ()       { return show_start_dialog; }
    
    /** Getter for property system_list
     * @return Value of property system_list which may be null. */
    public SystemList getSystemList()       { return system_list; }
    
    /* read options from disk 
     * @return true if options were read OK */
    public boolean read ()
    {
        boolean ret_val;
        String options_filename, string;
        BufferedReader buffered_reader;
        
        options_filename = FileNameConstants.getConfigFileName(FileNameConstants.DATALOGGER_PACKAGE_NAME, 
                                                               FileNameConstants.DATALOGGER_OPTIONS_FILENAME);

        // load options
        ret_val = true;
        buffered_reader = null;
        try
        {
            buffered_reader = new BufferedReader (new InputStreamReader (new FileInputStream (new File (options_filename))));
            
            if (buffered_reader.readLine().equalsIgnoreCase("true")) use_system_list = true;
            else use_system_list = false;
            string = buffered_reader.readLine();
            if (string.length() <= 0) system_list_update_address = null;
            else system_list_update_address = new URL (string);
            if (buffered_reader.readLine().equalsIgnoreCase("true")) use_data_compression = true;
            else use_data_compression = false;
            if (buffered_reader.readLine().equalsIgnoreCase("true")) use_channel_names = true;
            else use_channel_names = false;
            if (buffered_reader.readLine().equalsIgnoreCase("true")) show_start_dialog = true;
            else show_start_dialog = false;
        }
        catch (Exception e) { ret_val = false; }

        try { if (buffered_reader != null) buffered_reader.close (); } catch (Exception e2) { }
        
        return ret_val;
    }

    /* write options to disk 
     * @return true if options were read OK */
    public boolean write ()
    {
        boolean ret_val;
        String options_filename;
        BufferedWriter buffered_writer;

        options_filename = FileNameConstants.getConfigFileName(FileNameConstants.DATALOGGER_PACKAGE_NAME, 
                                                               FileNameConstants.DATALOGGER_OPTIONS_FILENAME);

        // write options
        ret_val = true;
        buffered_writer = null;
        try
        {
          buffered_writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (new File (options_filename))));

          if (use_system_list) buffered_writer.write ("true");
          else buffered_writer.write ("false");
          buffered_writer.newLine ();
          if (system_list_update_address != null) buffered_writer.write (system_list_update_address.toExternalForm());
          buffered_writer.newLine ();
          if (use_data_compression) buffered_writer.write ("true");
          else buffered_writer.write ("false");
          buffered_writer.newLine ();
          if (use_channel_names) buffered_writer.write ("true");
          else buffered_writer.write ("false");
          buffered_writer.newLine ();
          if (show_start_dialog) buffered_writer.write ("true");
          else buffered_writer.write ("false");
          buffered_writer.newLine ();
        }
        catch (Exception e) { ret_val = false; }

        try { if (buffered_writer != null) buffered_writer.close (); } catch (Exception e2) { }
        
        return ret_val;
    }
    
}
