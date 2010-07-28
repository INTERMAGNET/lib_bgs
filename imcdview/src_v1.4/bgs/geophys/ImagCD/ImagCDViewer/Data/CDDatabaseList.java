/*
 * DatabaseList.java
 *
 * Created on 27 April 2002, 08:53
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.File;
import java.util.Vector;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.File.*;
import bgs.geophys.library.Database.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;

/**
 * A class to hold details of where IMAG CD data can be found.
 * It also allows CD's to be copied to disk. This is the top
 * of the hierarchy of data storage. The hierarchy is:
 *
 * CDDatabaseList       All CD structures on a CPU
 *   CDDatabse          A single CD structure
 *     CDYear           A single year on a CD structure
 *       CDCountry      A country in a year directory
 *       CDObservatory  An observatory in a year directory
 *
 * CDObservatoryIterator (one is a member of this class) allows
 * you to iterate over observatories
 *
 * The CDUtils class holds general purpose codes and utilities
 * The CDException class handles error reporting for all these classes
 * The CDErrorList holds a list of errors found during parsing a database
 *
 * @author  smf
 * @version
 */
public class CDDatabaseList extends Object implements Runnable
{

  // private members
  private Vector<CDDatabase> databases;            // array of base IMAG directories
  private CDObservatoryIterator obsy_iterator;     // object to allow iteration over all loaded observatories
  private boolean obsy_iterator_needs_updating;    // if TRUE then the iterator must be recreated
  private CSVDatabaseTable country_table;          // database dictionary - list of countries
  private CSVDatabaseTable observatory_table;      // database dictionary - list of observatories
  private final String config_base_key_name = "data_source_";

  // private members used to communicate with the thread that
  // scans the filesystem for databases
  private File scan_start_dir;            // null to start with file system roots
  private File scan_current_dir;          // the point the scan has got to
  private Boolean scan_in_progress;       // used to stop multiple simultaneous scans
  private Boolean scan_stop_scanning;     // set this to true to force an end of the scanning thread
  private int scan_recurse_level;         // current recursion level
  private int scan_recurse_limit;         // maxmimum recursion level (after that scans automaically fail)
  private boolean scan_ignore_system_dirs;// true to ignore system directories during a scan
  
  /********************************************************************
   * Creates new DatabaseList from scratch
   *
   * @params country_table list of countries
   * @params observatory_table list of observatories
   ********************************************************************/
  public CDDatabaseList (CSVDatabaseTable country_table, CSVDatabaseTable observatory_table)
  {
    int count;
    String name, value;
    
    // store the tables
    this.country_table = country_table;
    this.observatory_table = observatory_table;
    
    // create an empty array
    databases = new Vector<CDDatabase> ();
    scan_in_progress = new Boolean (false);
    scan_current_dir = new File ("");
    obsy_iterator_needs_updating = true;

    // fill it from the previous serialized store
    count = 0;
    do
    {
      name = config_base_key_name + Integer.toString (count);
      value = (String) GlobalObjects.configuration.get (name);
      // add the database, silently ignoring it if it fails
      try { AddDatabase (new File (value), false); }
      catch (CDException e) { }
      catch (NullPointerException e) { }
      count ++;
    } while (value != null);
  }

  /********************************************************************
   * Creates new DatabaseList as a copy of an existing list
   *
   * @param list the existing list to copy
   * @params country_table list of countries
   * @params observatory_table list of observatories
   ********************************************************************/
  public CDDatabaseList (CDDatabaseList list, CSVDatabaseTable country_table, CSVDatabaseTable observatory_table)
  {
    int count;
    
    // store the tables
    this.country_table = country_table;
    this.observatory_table = observatory_table;

    // create an empty array
    databases = new Vector<CDDatabase> ();
    scan_in_progress = new Boolean (false);
    scan_current_dir = new File ("");
    obsy_iterator_needs_updating = true;

    // fill it from the existing list (without checking the base directories)
    for (count=0; count<list.GetNDatabases(); count++)
    {
      try
      {
        databases.add (list.GetDatabase(count));
      }
      catch (CDException e) { }
    }
    
  }

  /********************************************************************
   * reload the contents of all databases
   *
   * @throws CDException if accessed during a scan or if there was
   *         a loading error
   ********************************************************************/
  public void reload ()
  throws CDException
  {
    int count;
    CDDatabaseList new_list;
    
    new_list = new CDDatabaseList (this, country_table, observatory_table);
    databases.removeAllElements ();
    for (count=0; count<new_list.GetNDatabases(); count++)
      AddDatabase (new_list.GetDatabase(count).GetBaseDir (), false);
  }
  
  /********************************************************************
   * Add a database to the list of databases
   *
   * @param database the pre-loaded database to add
   * @return the database (may have been reloaded)
   * @throws CDException if accessed during a scan
   ********************************************************************/
  public CDDatabase AddDatabase (CDDatabase database)
  throws CDException
  {
      database = PrivateAddDatabase (database);
      SendToConfiguration ();
      return database;
  }
  
  /********************************************************************
   * Add a database to the list of databases
   *
   * @param base_dir the base directory for the database
   * @return the new database object
   * @throws CDException if accessed during a scan or if base_dir
   *         does not contain INTERMAGNET CD data
   ********************************************************************/
  public CDDatabase AddDatabase (File base_dir)
  throws CDException
  {
      return AddDatabase (base_dir, true);
  }
  
  /********************************************************************
   * Add a database to the list of databases
   *
   * @param base_dir the base directory for the database
   * @param modify_config if true update the configuration
   * @return the new database object
   * @throws CDException if accessed during a scan or if base_dir
   *         does not contain INTERMAGNET CD data
   ********************************************************************/
  private CDDatabase AddDatabase (File base_dir, boolean modify_config)
  throws CDException
  {
      
    CDDatabase database;
    
    // prevent access during a scan
    if (IsScanInProgress ())
      throw (new CDException ("Action prohibited during database scan"));
    
    // add the database
    database = PrivateAddDatabase (base_dir);

    // serialize this class to store the list for next time
    if (modify_config) SendToConfiguration ();
    
    return database;
  }

  /********************************************************************
   * Find the number of databases
   *
   * @return the number of databases
   ********************************************************************/
  public int GetNDatabases ()
  {
    return databases.size ();
  }

  /********************************************************************
   * Get the a database from the array
   *
   * @param index the number of the database to retrieve
   * @returns the database, or null for the end of the list
   * @throws CDException if accessed during a scan
   ********************************************************************/
  public CDDatabase GetDatabase (int index)
  throws CDException
  {
    // prevent access during a scan
    if (IsScanInProgress ())
      throw (new CDException ("Action prohibited during database scan"));
    
    return (CDDatabase) databases.get (index);
  }

  /********************************************************************
   * Get the longest errata file - the assumption is that the longest
   * file is the most complete.
   * @return the full path to the longest file, or null if no
   *         files are available
   ********************************************************************/
  public String GetLongestErrataFile ()
  {
    int count;
    long max_length;
    CDDatabase database;
    String errata_file;
    
    errata_file = null;
    max_length = 0;
    for (count=0; count<databases.size(); count++)
    {
      database = (CDDatabase) databases.get (count);
      if (database.GetErrataFileLength() > max_length)
      {
        max_length = database.GetErrataFileLength();
        errata_file = database.GetErrataFile();
      }
    }
    
    return errata_file;
    
  }
  
  /********************************************************************
   * Get the readme file from the database with the latest year directory -
   * the assumption is that the database with the latest year has the most
   * up to date readme file
   * @return the full path to the longest file, or null if no
   *         files are available
   ********************************************************************/
  public String GetLatestReadmeFile ()
  {
    int count, latest_year;
    CDDatabase database;
    String readme_file;
    
    readme_file = null;
    latest_year = 0;
    for (count=0; count<databases.size(); count++)
    {
      database = (CDDatabase) databases.get (count);
      if (database.GetReadmeFileLength() > 0)
      {
        if (database.GetLatestYear() > latest_year)
        {
          readme_file = database.GetReadmeFile();
          latest_year = database.GetLatestYear();
        }
      }
    }
    
    return readme_file;
    
  }
  
  /********************************************************************
   * Find the object the allow iteration over all observatories
   *
   * @return the iterator object
   ********************************************************************/
  public CDObservatoryIterator GetObservatoryIterator ()
  {
    if (obsy_iterator_needs_updating)
    {
        obsy_iterator = new CDObservatoryIterator (this);
        obsy_iterator.Sort ();
    }
    obsy_iterator_needs_updating = false;
    return obsy_iterator;
  }

  /******************************************************************
   * Remove a database from the list
   *
   * @param index the index of the database to remove
   * @throws CDException if accessed during a scan
   ******************************************************************/
  public void RemoveDatabase(int index) 
  throws CDException
  {
    // prevent access during a scan
    if (IsScanInProgress ())
      throw (new CDException ("Action prohibited during database scan"));
    
    if (index >= 0 && index < databases.size())
      databases.removeElementAt (index);

    // flag that we need to rebuild the observatory iterator
    obsy_iterator_needs_updating = true;

    // serialize this class to store the list for next time
    SendToConfiguration ();
  }

  /******************************************************************
   * Remove a database from the list
   *
   * @param database the database to remove
   * @throws CDException if accessed during a scan
   ******************************************************************/
  public void RemoveDatabase(CDDatabase database) 
  throws CDException
  {
    // prevent access during a scan
    if (IsScanInProgress ())
      throw (new CDException ("Action prohibited during database scan"));
    
    databases.removeElement(database);

    // flag that we need to rebuild the observatory iterator
    obsy_iterator_needs_updating = true;

    // serialize this class to store the list for next time
    SendToConfiguration ();
  }

  /******************************************************************
   * Start a thread that will scan the file system for IMAG CD databases.
   * While the thread is running all other functions in this class are
   * disabled, with the excepion of those that get the scan status or
   * call a stop to the scan. The sequence of events is call
   * ScanForDatabases() then wait until IsScanInProgress() returns
   * false. At any time you may call StopScan(). You can also call
   * GetCurrentScanDir() to find out what point the scan has reached.
   *
   * @param start_dir the start directory or null to scan the entire
   *        file system
   * @param recurse_limit limit the recursion into sub-directories to
   *        to amount shown
   * @param ignore_system_dirs true to ignore system directories
   * @throws CDException if accessed during a scan
   ******************************************************************/
  public void ScanForDatabases (File start_dir, int recurse_limit,
                                boolean ignore_system_dirs)
  throws CDException
  {
    // prevent access during a scan
    if (IsScanInProgress ())
      throw (new CDException ("Action prohibited during database scan"));
    
    // set up for the scan and start it
    scan_in_progress = new Boolean (true);
    scan_stop_scanning = new Boolean (false);
    scan_current_dir = new File ("");
    scan_start_dir = start_dir;
    scan_recurse_limit = recurse_limit;
    scan_ignore_system_dirs = ignore_system_dirs;
    new Thread(this).start();
  }
  
  /******************************************************************
   * Get the directory currently being scanned
   *
   * @returns current scan directory or null if none
   ******************************************************************/
  public File GetCurrentScanDir ()
  {
    String path;
    
    synchronized (scan_current_dir)
    {
      path = scan_current_dir.getAbsolutePath();
    }
    if (path.length () <= 0) return null;
    return new File (path);
  }

  /******************************************************************
   * Stop a scan
   ******************************************************************/
  public void StopScan ()
  {
    synchronized (scan_stop_scanning)
    {
      scan_stop_scanning = new Boolean (true);
    }
  }

  /******************************************************************
   * Find of wether a scan is in progress
   *
   * @return true if a scan is in progress
   ******************************************************************/
  public boolean IsScanInProgress ()
  {
    boolean ret_val;
    
    synchronized (scan_in_progress)
    {
      ret_val = scan_in_progress.booleanValue();
    }
    return ret_val;
  }

  /******************************************************************
   * Utility routine to send the current database list to the
   * configuration object.
   ******************************************************************/
  private void SendToConfiguration ()
  {
    int count;
    String name, value;
    
    // first delete any current database source keys
    count = 0;
    do
    {
      name = config_base_key_name + Integer.toString (count ++);
    } while (GlobalObjects.configuration.remove (name) != null);
    
    // now set the configuration
    for (count=0; count<GetNDatabases(); count++)
    {
      name = config_base_key_name + Integer.toString (count);
      try
      {
        value = GetDatabase(count).GetBaseDir().getPath ();
        GlobalObjects.configuration.put (name, value);
      }
      catch (CDException e) { }
    }
  }
  
  /******************************************************************
   ******************************************************************
   * Code below this point handles the creation of database lists
   * in the background
   ******************************************************************
   ******************************************************************/

  /******************************************************************
   * Run a new thread to scan the file system for databases
   ******************************************************************/
  public void run ()
  {
    try
    {
      scan_recurse_level = 0;
      if (scan_start_dir == null) StartScanning ();
      else StartScanning (scan_start_dir);
      synchronized (scan_in_progress)
      {
        scan_in_progress = new Boolean (false);
      }
      scan_current_dir = new File ("");

      // serialize this class to store the list for next time
      SendToConfiguration ();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
  
  /******************************************************************
   * Scan the file system for IMAG databases
   ******************************************************************/
  private void StartScanning ()
  {
    int count;
    File [] file_system_roots;
    
    file_system_roots = File.listRoots();
    for (count=0; count<file_system_roots.length; count++)
    {
      synchronized (scan_stop_scanning)
      {
        if (scan_stop_scanning.booleanValue()) break;
      }
      StartScanning (file_system_roots [count]);
    }
  }
  
  /******************************************************************
   * Scan the given directory (and its sub-directories recusrsively)
   * for IMAG databases, storing any that are found in this object.
   *
   * @param dir the directory to scan
   ******************************************************************/
  private void StartScanning (File dir)
  {
    boolean cd_data_found, scan_sub_dir;
    int count;
    String string, windows_match, unix_match;
    File files [];

    cd_data_found = false;
    synchronized (scan_current_dir)
    {
      scan_current_dir = dir;
    }
    if (! dir.isDirectory ()) return;
    if (scan_ignore_system_dirs)
    {
        unix_match = dir.getAbsolutePath();
        if (unix_match.length() < 3) windows_match = "";
        else windows_match = unix_match.substring(3).toLowerCase();
        if (unix_match.equals("/bin")) return;
        if (unix_match.equals("/bin/")) return;
        if (unix_match.equals("/dev")) return;
        if (unix_match.equals("/dev/")) return;
        if (unix_match.equals("/devices")) return;
        if (unix_match.equals("/devices/")) return;
        if (unix_match.equals("/etc")) return;
        if (unix_match.equals("/etc/")) return;
        if (unix_match.equals("/kernel")) return;
        if (unix_match.equals("/kernel/")) return;
        if (unix_match.equals("/platform")) return;
        if (unix_match.equals("/platform/")) return;
        if (unix_match.equals("/proc")) return;
        if (unix_match.equals("/proc/")) return;
        if (unix_match.equals("/sbin")) return;
        if (unix_match.equals("/sbin/")) return;
        if (unix_match.equals("/usr")) return;
        if (unix_match.equals("/usr/")) return;
        if (windows_match.equals("program files")) return;
        if (windows_match.equals("program files\\")) return;
        if (windows_match.equals("windows")) return;
        if (windows_match.equals("windows\\")) return;
        if (windows_match.equals("winnt")) return;
        if (windows_match.equals("winnt\\")) return;
    }
    try
    {
      files = dir.listFiles();
    }
    catch (SecurityException e) { return; }
    if (files == null) return;
    
    for (count=0; count<files.length; count++)
    {
      synchronized (scan_stop_scanning)
      {
        if (scan_stop_scanning.booleanValue()) break;
      }
      
      // we are only interested in directories
      try
      {
        if (files[count].isDirectory ())
        {
          scan_sub_dir = true;
          try
          {
            // search for IMAG CD directory names
            CDYear.TestYearDir(files[count]);
            scan_sub_dir = false;
            if (! cd_data_found)
            {
              PrivateAddDatabase (dir);
              cd_data_found = true;
            }
          }
          catch (CDException e) { }

          // if this wasn't an IMAG CD directory, process it recursively
          if (scan_sub_dir) 
          {
            if (scan_recurse_level < scan_recurse_limit)
            {
              scan_recurse_level ++;
              StartScanning (files[count]);
              scan_recurse_level --;
            }
          }
        }
      }
      catch (SecurityException e) { }
    }
    
  }  
  
  /********************************************************************
   * Private version of AddDatabase that does the real work
   *
   * @param base_dir the base directory for the database
   * @return the new database object or null if the database wasn't added
   *         (which does NOT indicate an error)
   * @throws CDException if base_dir does not contain INTERMAGNET CD data
   ********************************************************************/
  private CDDatabase PrivateAddDatabase (File base_dir)
  throws CDException
  {

    int count;
    CDDatabase database;

    // don't add databases that are already in the list
    for (count=0; count<databases.size (); count++)
    {
        if (base_dir.equals (((CDDatabase)databases.elementAt(count)).GetBaseDir ())) return null;
    }

    // users often specify a year directory as a database directory - correct for this
    // by checking if we have been given a year directory - if we have adjust
    // the base directory up one level in the directory tree
    try
    {
        CDYear.TestYearDir (base_dir);
        base_dir = base_dir.getParentFile();
    }
    catch (CDException e) { }
    
    // create the new database
    database = new CDDatabase (base_dir, country_table, observatory_table);
    return PrivateAddDatabase (database);
  }

  private CDDatabase PrivateAddDatabase (CDDatabase database)
  throws CDException
  {
    boolean found_new_table;
    int count, year_index;
    CDYear cd_year;
    File base_dir;

    base_dir = database.GetBaseDir();
    
    // are there new country or observatory database files in the new database
    found_new_table = false;
    for (year_index=0; year_index<database.GetNYears(); year_index++)
    {
        cd_year = database.GetYear(year_index);
        try
        {
            for (count=0; count<cd_year.GetNCountryDatabaseVersions(); count++)
            {
                if (GlobalObjects.addCountryDatabase(cd_year.GetCountryDatabseVersion(count)))
                    found_new_table = true;
            }
        }
        catch (Exception e) 
        {
            throw new CDException ("Error loading country database in year " + Integer.toString (cd_year.GetYear()));
        }
        try
        {
            for (count=0; count<cd_year.GetNObservatoryDatabaseVersions(); count++)
            {
                if (GlobalObjects.addObservatoryDatabase(cd_year.GetObservatoryDatabseVersion(count)))
                    found_new_table = true;
            }
        }
        catch (Exception e) 
        {
            throw new CDException ("Error loading observatory database in year " + Integer.toString (cd_year.GetYear()));
        }
    }

    // if new tables were found, reload the database using the new tables
    if (found_new_table)
    {
        country_table = GlobalObjects.country_table;
        observatory_table = GlobalObjects.observatory_table;
        database = new CDDatabase (base_dir, country_table, observatory_table);
    }
    
    // add the database to the list
    databases.add (database);
    
    
    // flag that we need to rebuild the observatory iterator
    obsy_iterator_needs_updating = true;
    
    return database;
  }
  
}
