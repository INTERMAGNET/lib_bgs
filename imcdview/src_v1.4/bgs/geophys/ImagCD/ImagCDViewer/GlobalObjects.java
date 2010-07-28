/*
 * GlobalObjects.java
 *
 * Created on 02 June 2002, 16:19
 */

package bgs.geophys.ImagCD.ImagCDViewer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;
import java.net.URL;
import bgs.geophys.library.File.*;
import bgs.geophys.library.Database.*;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Swing.ImageSaverDialog;
import bgs.geophys.library.Swing.PrintCanvas;
import bgs.geophys.library.Swing.SwingUtils;
import bgs.geophys.library.Swing.UIFontModifier;

/**
 * This class holds a set of objects needed by the whole system. These objects are
 * held as static, so that any other object can gain access to them by instantiating
 * this object.
 *
 * Objects that need to be persistant can be put in the configuration hashtable. They
 * will be saved at the end of the program and loaded at the next invocation.
 *
 * @author  Simon
 * @version
 */
public class GlobalObjects extends Object
{

  // an inner class that can handle ComponentListener events
  private static class ComponentListenerHandler
  implements ComponentListener
  {
    public void componentHidden(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentResized(ComponentEvent e) 
    {
      if (e.getSource().equals(top_frame))
      {
        configuration.put ("WorldMapSizeWidth", Integer.toString (e.getComponent().getWidth()));
        configuration.put ("WorldMapSizeHeight", Integer.toString (e.getComponent().getHeight()));
      }
    }
  }
    
  // public members - data items
  /** the list of IMAG CD data sources */
  public static CDDatabaseList database_list;
  /** the program's configuration is a public Hashtable - users may write
   * what they like into it and it will be saved when the program ends - this
   * object does no syntax checking on the contents of the table */
  public static Properties configuration;
  /** a list of versions of the country database file */
  public static FileVersion country_table_versions;
  /** a list of all known countries */
  public static CSVDatabaseTable country_table;
  /** a list of versions of the observatory database file */
  public static FileVersion observatory_table_versions;
  /** a list of observatories - this list contains all known observatories,
   * not just the INTERMAGNET ones */
  public static CSVDatabaseTable observatory_table;
  /** a list of versions of the scalar F database file */
  public static FileVersion scalar_f_table_versions;
  /** a list of scalar F flags - this lists which observatories provided
   * scalar F during which years */
  public static CSVDatabaseTable scalar_f_table;

  /** the most recent version of the country database file, as distributed with the
   * software - THIS NEEDS TO BE UPDATED each time a new version of the file is created */
  public static final int COUNTRY_TABLE_VERSION_NO = 102;
  /** the most recent version of the observatory database file, as distributed with the
   * software - THIS NEEDS TO BE UPDATED each time a new version of the file is created */
  public static final int OBSERVATORY_TABLE_VERSION_NO = 102;
  /** the most recent version of the scalar F database file, as distributed with the
   * software - THIS NEEDS TO BE UPDATED each time a new version of the file is created */
  public static final int SCALAR_F_TABLE_VERSION_NO = 100;
  
  // public members - user interface items
  /** the frame for the top level window - also includes the map */
  public static MainWin top_frame;
  /** the object the interprets the commands that drive the program */
  public static CommandInterpreter command_interpreter;
  /** the object responsible for printing other objects */
  public static PrintCanvas print_canvas;
  /** an object that can change parts of the UI's font */
  public static UIFontModifier ui_font_modifier;

  /** an object used to manage debug information */
  public static DebugManager debug_manager;

  // public members - program icons
  /** icon to represent a filesystem */
  public static ImageIcon filesystem_icon;
  /** icon to represent a folder */
  public static ImageIcon folder_icon;
  /** icon to represent a bad folder */
  public static ImageIcon folder_bad_icon;
  /** icon to represent an open folder */
  public static ImageIcon open_folder_icon;
  /** icon to represent a file */
  public static ImageIcon file_icon;
  /** icon to represent a bad file */
  public static ImageIcon file_bad_icon;
  /** icon to represent a plot */
  public static ImageIcon plot_icon;
  /** icon for intermagnet */
  public static ImageIcon imag_icon;
  /** icon for 'first' */
  public static ImageIcon first_icon;
  /** icon for 'previous' */
  public static ImageIcon previous_icon;
  /** icon for 'next' */
  public static ImageIcon next_icon;
  /** icon for 'last' */
  public static ImageIcon last_icon;
  /** icon showing a tick */
  public static ImageIcon tick_icon;
  /** icon showing a cross */
  public static ImageIcon cross_icon;
  /* icon to depict export */
  public static ImageIcon export_icon;
  /* icon to depict a table */
  public static ImageIcon table_icon;
  /* icon to depict a check */
  public static ImageIcon check_icon;
  /** treat map of the world as an icon */
  public static ImageIcon world_map;
  /* warning folder icon */
  public static ImageIcon folder_warn_icon;
  /* warning file icon */
  public static ImageIcon file_warn_icon;
  /* warning  icon */
  public static ImageIcon warn_icon;
  /* wait icon */
  public static ImageIcon hour_glass_icon;

  // public members - mouse pointers (cursors)
  public static Cursor default_cursor;
  public static Cursor wait_cursor;
  
  // public members - file related things
  /** the directory used to store configuration data */
  public static File config_dir;
  /** the file holding the release notes (or null) */
  public static URL release_notes_url;
  /** the directory where exported data will be written */
  public static File export_output_dir;
  /** the file chooser for data sources - this is stored here so that
   * the program remembers where the user was the last time the file
   * chooser was used */
  public static JFileChooser data_source_dir_chooser;
  /** the file chooser for exported data - this is stored here so that
   * the program remembers where the user was the last time the file
   * chooser was used */
  public static JFileChooser export_dir_chooser;
  /** the file chooser for general saving (e.g. text files) */
  public static JFileChooser general_save_file_chooser;
  
  // a handler for component resize events - this copes with capturing
  // window sizes to the configuration file
  private static ComponentListenerHandler component_listener_handler;
  
  // the name of the configuration file
  private static String config_file_name;

  // headers for the database files
  private static String country_table_header;
  private static String observatory_table_header;
  private static String scalar_f_table_header;

  // the welcome dialog
  private static WelcomeDialog welcome_dialog;
  private static Date welcome_dialog_date;

  
  // initialise the static members
  static
  {
    int width, height, count;
    long min_welcome_display_time;
    String string, error_message;
    FileInputStream input_stream;
    File file, dest_file;
    URL url;
    Vector<String> fields;


    // show the welcome dialog before doing anything else
    welcome_dialog = new WelcomeDialog (null);
    welcome_dialog.setProgress (0);
    welcome_dialog.setVisible (true);
    welcome_dialog_date = new Date ();
    
    // initialise variables ...
    error_message = null;
    export_output_dir = null;
    
    // read the configuration from disk - must be second as it is needed by most objects
    config_dir = new File (FileNameConstants.getConfigDir (FileNameConstants.INTERMAGNET_PACKAGE_NAME));
    config_file_name = FileNameConstants.getConfigFileName (FileNameConstants.INTERMAGNET_PACKAGE_NAME, 
                                                            FileNameConstants.IMCD_VIEWER_OPTIONS_FILENAME);
    component_listener_handler = new GlobalObjects.ComponentListenerHandler ();
    configuration = new Properties ();
    try
    {
      input_stream = new FileInputStream (config_file_name);
      configuration.load (input_stream);
      input_stream.close ();
    }
    catch (FileNotFoundException e)    { configuration = new Properties (); }
    catch (IOException e)              { configuration = new Properties (); }
    welcome_dialog.setProgress (5);
    
    // get export directory from configuration
    string = configuration.getProperty("ExportOutputDirectory");
    if (string != null)
        export_output_dir = new File (string);
    
    // create and initialise the UIFontModifier
    ui_font_modifier = new UIFontModifier ();
    ui_font_modifier.changeFontSize(ProgramOptionsDialog.getProgramFontSizePercent(true), ProgramOptionsDialog.getMinFontSize());
    
    // create the debug manager
    if (System.getProperty("debug") == null)
        debug_manager = new DebugManager (false);
    else
        debug_manager = new DebugManager (true);
    
    /* create the command interpreter
     * CommandInterpreter is used as a reference to allow the program to find data files and
     * icons. IT IS VERY IMPORTANT that the class file for CommandInterpreter is at the base
     * of the directory tree for the application - the data files and tables must live
     * in subdirectories of CommandInterpreter's class file directory, otherwise they won't be
     * found. */
    command_interpreter = new CommandInterpreter ();
    welcome_dialog.setProgress (10);

    // create the list of database table versions - this is the list stored in the
    // users configuration directory - all files with correctly formatted names will
    // be put in the list - the most recent version will be used
    try
    {
        country_table_versions = new FileVersion (config_dir, "ctr", ".csv", true);
        observatory_table_versions = new FileVersion (config_dir, "obs", ".csv", true);
        scalar_f_table_versions = new FileVersion (config_dir, "scf", ".csv", true);
    }
    catch (IOException e) { error_message = "Configuration directory is invalid: " + config_dir.toString(); }
    welcome_dialog.setProgress (20);

    // check if we need to copy the versions of the database tables that come
    // with the software to the configuration directory
    if (error_message == null)
    {
        try
        {
            file = new File (country_table_versions.makeFileName (COUNTRY_TABLE_VERSION_NO));
            if (country_table_versions.findFile (file, false) < 0)
            {
                url = command_interpreter.getClass().getResource("Tables/" + file.getName());
                if (url != null)
                {
                    dest_file = new File (config_dir.getAbsolutePath() + File.separator + file.getName ());
                    FileUtils.copy(url, dest_file);
                    country_table_versions.addFile (dest_file);
                }
            }
            file = new File (observatory_table_versions.makeFileName (OBSERVATORY_TABLE_VERSION_NO));
            if (observatory_table_versions.findFile (file, false) < 0)
            {
                url = command_interpreter.getClass().getResource("Tables/" + file.getName());
                if (url != null)
                {
                    dest_file = new File (config_dir.getAbsolutePath() + File.separator + file.getName ());
                    FileUtils.copy(url, dest_file);
                    observatory_table_versions.addFile (dest_file);
                }
            }
            file = new File (scalar_f_table_versions.makeFileName (SCALAR_F_TABLE_VERSION_NO));
            if (scalar_f_table_versions.findFile (file, false) < 0)
            {
                url = command_interpreter.getClass().getResource("Tables/" + file.getName());
                if (url != null)
                {
                    dest_file = new File (config_dir.getAbsolutePath() + File.separator + file.getName ());
                    FileUtils.copy(url, dest_file);
                    scalar_f_table_versions.addFile (dest_file);
                }
            }
        }
        catch (Exception e) 
        {
            error_message = "Unable to copy database tables from software distribution"; 
        }
    }
    welcome_dialog.setProgress (30);
    
    // check that there is at least one version of each of the database files
    if (error_message == null)
    {
        if (country_table_versions.getNVersions() < 1 || 
            observatory_table_versions.getNVersions() < 1 ||
            scalar_f_table_versions.getNVersions() < 1)
            error_message = "Unable to find database tables";
    }
    
    // get the database tables
    if (error_message == null)
    {
        file = country_table_versions.getFile (country_table_versions.getNVersions() -1);
        try
        {
            country_table_header = "country,STRING,code_3_digit,STRING,IMAG_code,String";
            country_table = new CSVDatabaseTable (country_table_header, file,
                                                  CSVDatabaseTable.CSV_TOP_ROW_IGNORE);
        }
        catch (Exception e) { error_message = "Unable to load country database: " + file.toString(); }
    }
    if (error_message == null)
    {
        file = observatory_table_versions.getFile (observatory_table_versions.getNVersions() -1);
        try
        {
            observatory_table_header = "code,STRING,name,STRING,latitude,DOUBLE,longitude,DOUBLE,altitude,INT,country_code,STRING,valid_start,INT,valid_end,INT";
            observatory_table = new CSVDatabaseTable (observatory_table_header, file,
                                                      CSVDatabaseTable.CSV_TOP_ROW_IGNORE);
        }
        catch (Exception e) { error_message = "Unable to load observatory database: " + file.toString(); }
    }
    if (error_message == null)
    {
        file = scalar_f_table_versions.getFile (scalar_f_table_versions.getNVersions() -1);
        try
        {
            // build the header description from the file header - the first column lists
            // observatories, subsequent columns contain year data
            scalar_f_table_header = "code,STRING";
            fields = CSVDatabaseTable.readHeader(file);
            for (count=1; count<fields.size(); count++)
                scalar_f_table_header = scalar_f_table_header + "," + fields.get(count) + ",STRING";
            scalar_f_table = new CSVDatabaseTable (scalar_f_table_header, file,
                                                   CSVDatabaseTable.CSV_TOP_ROW_IGNORE);
        }
        catch (Exception e) { error_message = "Unable to load scalar F database: " + file.toString(); }
    }
    welcome_dialog.setProgress (35);
    
    // create the list of IMAG CD data sources
    if (error_message == null)
    {
        database_list = new CDDatabaseList (country_table, observatory_table);
    }
    welcome_dialog.setProgress (75);
    
    // get the program icons
    if (error_message == null)
    {
        filesystem_icon =  new ImageIcon(command_interpreter.getClass().getResource("Icons/FileSystem.gif"));
        folder_icon =      new ImageIcon(command_interpreter.getClass().getResource("Icons/Directory.gif"));
        folder_bad_icon =  new ImageIcon(command_interpreter.getClass().getResource("Icons/DirectoryBad.gif"));
        open_folder_icon = new ImageIcon(command_interpreter.getClass().getResource("Icons/OpenDirectory.gif"));
        file_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/File.gif"));
        file_bad_icon =    new ImageIcon(command_interpreter.getClass().getResource("Icons/FileBad.gif"));
        plot_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/Plot.gif"));
        imag_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/Imag.gif"));
        first_icon =       new ImageIcon(command_interpreter.getClass().getResource("Icons/first.png"));
        previous_icon =    new ImageIcon(command_interpreter.getClass().getResource("Icons/previous.png"));
        next_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/next.png"));
        last_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/last.png"));
        tick_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/tick.png"));
        cross_icon =       new ImageIcon(command_interpreter.getClass().getResource("Icons/cross.png"));
        export_icon =      new ImageIcon(command_interpreter.getClass().getResource("Icons/export.gif"));
        table_icon =       new ImageIcon(command_interpreter.getClass().getResource("Icons/table.png"));
        check_icon =       new ImageIcon(command_interpreter.getClass().getResource("Icons/check.png"));
        world_map =        new ImageIcon(command_interpreter.getClass().getResource("Icons/world_map.gif"));
//        folder_warn_icon = new ImageIcon(command_interpreter.getClass().getResource("Icons/DirectoryWarn.gif"));
//        file_warn_icon =   new ImageIcon(command_interpreter.getClass().getResource("Icons/FileWarn.gif"));
//        warn_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/QuestionMark.gif"));
        folder_warn_icon = new ImageIcon(command_interpreter.getClass().getResource("Icons/warndir.GIF"));
        file_warn_icon =   new ImageIcon(command_interpreter.getClass().getResource("Icons/warnfile.GIF"));
        warn_icon =        new ImageIcon(command_interpreter.getClass().getResource("Icons/QMARK2.GIF"));
        hour_glass_icon =  new ImageIcon(command_interpreter.getClass().getResource("Icons/HourGlass.png"));
        if (filesystem_icon == null || folder_icon == null || folder_bad_icon == null ||
            open_folder_icon == null || file_icon == null || file_bad_icon == null || plot_icon == null ||
            imag_icon == null || first_icon == null || previous_icon == null ||
            next_icon == null || last_icon == null || tick_icon == null || cross_icon == null ||
            export_icon == null || table_icon == null || check_icon == null ||
            world_map == null || folder_warn_icon == null || file_warn_icon == null ||
            warn_icon == null || hour_glass_icon == null) error_message = "Unable to load icon files";
    }
    welcome_dialog.setProgress (80);

    // get the release notes
    if (error_message == null)
    {
        release_notes_url = command_interpreter.getClass().getResource("ReleaseNotes.txt");
    }
    welcome_dialog.setProgress (85);
    
    // load the mouse pointers
    if (error_message == null)
    {
        default_cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        wait_cursor = new Cursor(Cursor.WAIT_CURSOR);
    }
    welcome_dialog.setProgress (90);

    // if there was an error, then show it and bail out
    if (error_message != null)
    {
        JOptionPane.showMessageDialog(null, error_message, "System Error", JOptionPane.ERROR_MESSAGE);
        System.exit (1);
    }
    
    // create the print canvas
    print_canvas = new PrintCanvas();
    
    // create file choosers - this must be done AFTER icons have been loaded
    data_source_dir_chooser = new JFileChooser ();
    data_source_dir_chooser.setDialogTitle ("Select a directory containing INTERMAGNET data");
    data_source_dir_chooser.setApproveButtonText ("Select directory");
    data_source_dir_chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    data_source_dir_chooser.setMultiSelectionEnabled (false);
    data_source_dir_chooser.setFileView (new IconFileView ());
    SwingUtils.configureFileChooser (data_source_dir_chooser, "Directory name:", "", false);
    export_dir_chooser = new JFileChooser ();
    export_dir_chooser.setDialogTitle ("Select a directory for exported files");
    export_dir_chooser.setApproveButtonText ("Select directory");
    export_dir_chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    export_dir_chooser.setMultiSelectionEnabled (false);
    SwingUtils.configureFileChooser (export_dir_chooser, "Directory name:", "", false);
    general_save_file_chooser = new JFileChooser ();
    general_save_file_chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
    general_save_file_chooser.setMultiSelectionEnabled (false);
            
    // wait until the welcome dialog has been up 
    // for a minimum time (measured in milliseconds)
    try
    {
        min_welcome_display_time = Long.parseLong (configuration.getProperty ("minWelcomeDisplayTime", "3000"));
    }
    catch (NumberFormatException e)
    {
        min_welcome_display_time = 3000l;
    }
    if (min_welcome_display_time < 0l) min_welcome_display_time = 0;
    if (min_welcome_display_time > 10000l) min_welcome_display_time = 10000l;
    while ((new Date().getTime() - welcome_dialog_date.getTime()) < min_welcome_display_time)
    {
        try {Thread.sleep(100l); }
        catch (InterruptedException e) { }
    }
    configuration.setProperty ("minWelcomeDisplayTime", Long.toString (min_welcome_display_time));
    
    // set up the main window frame and make it visible
    top_frame = new MainWin ();
    try
    {
      string = (String) configuration.get ("WorldMapSizeWidth");
      width = Integer.parseInt (string);
      string = (String) configuration.get ("WorldMapSizeHeight");
      height = Integer.parseInt (string);
      if (width >= MainWin.MIN_WIDTH && height >= MainWin.MIN_HEIGHT) top_frame.setSize(width, height);
    }
    catch (NullPointerException e) { }
    catch (NumberFormatException e) { }
    top_frame.addComponentListener (component_listener_handler);
    top_frame.setVisible (true);
    
    // NBNBNB - once you get here the Swing event handler is running - no
    // further access to Swing components is allowed from this thread, instead
    // access must be through the AWT event handling thread - this can be achieved
    // using SwingUtilities.invokeLater to run a portion of code (see below)
    SwingUtilities.invokeLater(new Runnable ()
    {
        public void run ()
        {
            String key, string;
            boolean show_new_user_info;
            NewUserDialog new_user_dialog;
            Dimension top_frame_size, dialog_size;
            Point top_frame_pos, dialog_pos;
            
            // remove the welcome dialog
            welcome_dialog.setProgress (100);
            welcome_dialog.setVisible (false);
            welcome_dialog.dispose();

            // show the debug manager dialog (if any)
            debug_manager.showDebugDialog(top_frame);
    
            // Show the new user help ??
            key = "ShowNewUserInfo";
            string = (String) configuration.get (key);
            if (string == null) show_new_user_info = true;
            else if (string.equals("Y")) show_new_user_info = true;
            else show_new_user_info = false;
            if (show_new_user_info)
            {
                new_user_dialog = new NewUserDialog (top_frame, false, key);
                top_frame_pos = top_frame.getLocation();
                top_frame_size = top_frame.getSize ();
                dialog_size = new_user_dialog.getSize ();
                dialog_pos = new Point (top_frame_pos.x + (top_frame_size.width - dialog_size.width) / 2,
                                        top_frame_pos.y + (top_frame_size.height - dialog_size.height) / 2);
                if (dialog_pos.x < 0) dialog_pos.x = 0;
                if (dialog_pos.y < 0) dialog_pos.y = 0;
                new_user_dialog.setVisible(true);
                new_user_dialog.setLocation (dialog_pos);
            }
        }
    });
  }

  /** prevent GlobalObjects from being instantiated */
  private GlobalObjects () { }
  
  /** Write the configuration to disk */
  public static void writeConfiguration ()
  {
    FileOutputStream output_stream;

    if (export_output_dir != null)
        configuration.setProperty("ExportOutputDirectory", export_output_dir.getAbsolutePath());
    
    try
    {
      output_stream = new FileOutputStream (config_file_name, false);
      configuration.store(output_stream, "INTERMAGNET CD Browser Configuration");
      output_stream.close ();
    }
    catch (FileNotFoundException e) { }
    catch (IOException e) { }
  }

  /** redraw the map in the main window */
  public static void redrawMap ()
  {
      top_frame.redrawMap ();
  }

  /** add a country database file to the configuration directory - if it has a higher
   * version number than the current version, reload it
   * @param src_file the new file
   * @returns true if a new table was loaded
   * @throws FileNotFoundException if the file could not be found 
   * @throws SecurityException if the file could not be opened
   * @throws IOException if an IO error occurred
   * @throws CSVDatabaseException if the file could not be loaded */
  public static boolean addCountryDatabase (File src_file)
  throws FileNotFoundException, SecurityException, IOException, CSVDatabaseException
  {
      boolean ret_val;
      CSVDatabaseTable new_table;
      File dest_file, current_version_file, new_version_file;

      ret_val = false;
      if (country_table_versions.findFile (src_file, false) < 0)
      {
          current_version_file = country_table_versions.getFile (country_table_versions.getNVersions() -1);
          dest_file = new File (config_dir, src_file.getName().toLowerCase());
          FileUtils.copy (src_file, dest_file);
          country_table_versions.addFile (dest_file);
          new_version_file = country_table_versions.getFile (country_table_versions.getNVersions() -1);
          if (current_version_file != new_version_file)
          {
              new_table = new CSVDatabaseTable (country_table_header, new_version_file,
                                                CSVDatabaseTable.CSV_TOP_ROW_IGNORE);
              country_table = new_table;
              ret_val = true;
          }
      }
      
      return ret_val;
  }

  /** add an observatory database file to the configuration directory - if it has a higher
   * version number than the current version, reload it
   * @param src_file the new file
   * @returns true if a new table was loaded
   * @throws FileNotFoundException if the file could not be found 
   * @throws SecurityException if the file could not be opened
   * @throws IOException if an IO error occurred
   * @throws CSVDatabaseException if the file could not be loaded */
  public static boolean addObservatoryDatabase (File src_file)
  throws FileNotFoundException, SecurityException, IOException, CSVDatabaseException
  {
      boolean ret_val;
      CSVDatabaseTable new_table;
      File dest_file, current_version_file, new_version_file;

      ret_val = false;
      if (observatory_table_versions.findFile (src_file, false) < 0)
      {
          current_version_file = observatory_table_versions.getFile (observatory_table_versions.getNVersions() -1);
          dest_file = new File (config_dir, src_file.getName().toLowerCase());
          FileUtils.copy (src_file, dest_file);
          observatory_table_versions.addFile (dest_file);
          new_version_file = observatory_table_versions.getFile (observatory_table_versions.getNVersions() -1);
          if (current_version_file != new_version_file)
          {
              new_table = new CSVDatabaseTable (observatory_table_header, new_version_file,
                                                CSVDatabaseTable.CSV_TOP_ROW_IGNORE);
              observatory_table = new_table;
              ret_val = true;
          }
      }
      
      return ret_val;
  }

  /** get the expected type of F for a given observatory and year
   * @param obsy_code iaga code
   * @param year code
   * @return one of the following codes (defined in GeomagAbsoluteValue):
   *         COMPONENT_F - expect F data calculated from the variometer
   *         COMPONENT_F_SCALAR - expect F data from an indpendant instrument
   *         COMPONENT_F_DIFF - expect a delta F value */
  public static int getExpectedFType (String obsy_code, int year)
  {
      int row_idx, col_idx;
      String code;
      
      // find and translate the code in the database
      if (DataOptionsDialog.isUseFDatabase())
      {
          try
          {
              row_idx = scalar_f_table.FindFirst (0, obsy_code, true);
              col_idx = scalar_f_table.FindColumn (Integer.toString(year));
              code = scalar_f_table.GetStringData (row_idx, col_idx);
              if (code.equalsIgnoreCase("S")) return GeomagAbsoluteValue.COMPONENT_F_SCALAR;
              if (code.equalsIgnoreCase("V")) return GeomagAbsoluteValue.COMPONENT_F;
              if (code.equalsIgnoreCase("D")) return GeomagAbsoluteValue.COMPONENT_F_DIFF;
          }
          catch (CSVDatabaseException e) { }
      }
      
      // use configuration rules to determine F type
      switch (DataOptionsDialog.getNonDatabaseRuleCode())
      {
          case ALL_DELTA:
              return GeomagAbsoluteValue.COMPONENT_F_DIFF;
          case ALL_VECTOR:
              return GeomagAbsoluteValue.COMPONENT_F;
          case USE_YEAR_RULES:
              if (year <= DataOptionsDialog.getVectorBeforeYear()) 
                  return GeomagAbsoluteValue.COMPONENT_F;
              if (year >= DataOptionsDialog.getDeltaAfterYear()) 
                  return GeomagAbsoluteValue.COMPONENT_F_DIFF;
              return GeomagAbsoluteValue.COMPONENT_F_SCALAR;
      }

      // default - data is scalar
      return GeomagAbsoluteValue.COMPONENT_F_SCALAR;
  }
  
  public static ImageSaverDialog createImageSaverDialog ()
  {
      String dir, raster_size_x, raster_size_y, paper_size_x, paper_size_y, file_type;
      
      dir = configuration.getProperty("ImageSaveDirectory", System.getProperty("user.home", "."));
      raster_size_x = configuration.getProperty ("ImageRasterSizeX", "640");
      raster_size_y = configuration.getProperty ("ImageRasterSizeY", "480");
      paper_size_x = configuration.getProperty ("ImagePaperSizeX", "29.7");
      paper_size_y = configuration.getProperty ("ImagePaperSizeY", "21.0");
      file_type = configuration.getProperty ("ImageType", "PDF");
      
      return new ImageSaverDialog (dir, raster_size_x, raster_size_y,
                                   paper_size_x, paper_size_y, file_type,
                                   true);
  }
  
  public static void persistImageSaverOptions (ImageSaverDialog isd)
  {
      configuration.setProperty ("ImageSaveDirectory", isd.getStoredDir());
      configuration.setProperty ("ImageRasterSizeX", isd.getStoredRasterSizeX());
      configuration.setProperty ("ImageRasterSizeY", isd.getStoredRasterSizeY());
      configuration.setProperty ("ImagePaperSizeX", isd.getStoredPaperSizeX());
      configuration.setProperty ("ImagePaperSizeY", isd.getStoredPaperSizeY());
      configuration.setProperty ("ImageType", isd.getStoredFileType());
  }
  
  public static void querySaveFile (Component parent, File file)
  {
      File selected_file;
      String errmsg;
      
      if (file != null)
      {
          selected_file = general_save_file_chooser.getSelectedFile();
          if (selected_file != null) selected_file = selected_file.getParentFile();
          if (selected_file != null) selected_file = new File (selected_file, file.getName());
          if (selected_file != null) general_save_file_chooser.setSelectedFile(file);
          if (general_save_file_chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
          {
              errmsg = null;
              try
              {
                  selected_file = general_save_file_chooser.getSelectedFile();
                  FileUtils.copy (file, selected_file);
              }
              catch (FileNotFoundException e)
              {
                  errmsg = "Unable to find file to copy from: " + file.getAbsolutePath();
              }
              catch (IOException e)
              {
                  errmsg = "Error copying file";
              }
              catch (SecurityException e)
              {
                  errmsg = "Unable to open file: " + file.getAbsolutePath();
              }
              if (errmsg != null)
                  JOptionPane.showMessageDialog(parent, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
          }        
      }
  }
  
  public static Properties checkpointConfig ()
  {
      Properties saved_config;
      Enumeration<?> e;
      String name;
      
      saved_config = new Properties ();
      e = configuration.propertyNames ();
      
      while (e.hasMoreElements())
      {
          name = (String) e.nextElement();
          saved_config.setProperty(name, configuration.getProperty(name));
      }
              
      return saved_config;
  }
  
}
