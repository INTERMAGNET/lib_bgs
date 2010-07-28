/*
 * CDYear.java
 *
 * Created on 29 April 2002, 23:15
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.File;
import java.util.Vector;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.Database.*;

import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * Class that scans and holds information about what data is
 * available in a single year sub-directory on an intermagnet CD.
 *
 * @author  smf
 * @version 
 */
public class CDYear
implements Comparable<CDYear>
{

  // public members
  /** code for the readme file */
  public static final int FILE_README =       1;
  /** code for the all observatories map file */
  public static final int FILE_ALL_MAP =       2;
  /** code for the introduction screen plot */
  public static final int FILE_INTRO =         3;
  /** code for the all countries map file */
  public static final int FILE_ALL_COUNTRIES = 4;

  // private members
  private File base_dir;             // base directory (full path)
  private int year;                  // year for this data
  private Vector<CDCountry> countries;           // array of CDCountry details
  private Vector<CDObservatory> observatories;       // array of CDObservatory details
  private File readme_text;          // readme file for entire CD
  private File all_map_file;         // map plot of all observatories
  private File intro_screen;         // introduction screen (plot)
  private File all_countries_plot;   // plot of contributing countries
  private CSVDatabaseTable country_table;     // list of countries
  private CSVDatabaseTable observatory_table; // list of observatories
  private String two_digit_year, four_digit_year;
  private Vector<File> country_databases; // versions of the country database
  private Vector<File> observatory_databases; // versions of the observatory database
  
  /*******************************************************************
   * Create a new CDYear
   *
   * @param base_dir the base directory for this year in the
   *                 CD file structure
   * @param year the year for this data
   * @params country_table list of countries
   * @params observatory_table list of observatories
   * @param error_list a list of errors which this object can add to
   *******************************************************************/  
  public CDYear (File base_dir, int year, CSVDatabaseTable country_table, CSVDatabaseTable observatory_table, CDErrorList error_list)
  {
    int count, code_idx, name_idx, row_idx, lat_idx, long_idx, alt_idx, iso3_idx;
    int syear_idx, eyear_idx, start_year, end_year, count2, country_code_idx;
    boolean quit, quit2;
    String name, obsy_country_code;
    File files [];
    CDObservatory observatory;
    CDCountry country;

    // store the tables
    this.country_table = country_table;
    this.observatory_table = observatory_table;

    // store the base directory and year
    this.base_dir = base_dir;
    this.year = year;

    // flag eveything as missing
    countries = new Vector<CDCountry> ();
    observatories = new Vector<CDObservatory> ();
    readme_text = null;
    all_map_file = null;
    intro_screen = null;
    all_countries_plot = null;
    country_databases = new Vector<File> ();
    observatory_databases = new Vector<File> ();

    // get two and four digit year strings
    if ((year % 100) < 10) two_digit_year = "0" + Integer.toString (year % 100);
    else two_digit_year = Integer.toString (year % 100);
    four_digit_year = Integer.toString (year);
    
    // scan the files in the year sub-directory
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();

      // ignore some files
      if (name.equals("IMAG23.EXE") || name.equals("IMAG22.EXE") || 
          name.equals("GMAG.CFG") || name.equals("OBS_LIST.DOC"))
      {
        error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Ignoring file", files[count], year);
        continue;
      }
      
      // the remaining contents should be sub-directories
      if (! files[count].isDirectory()) error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
      // is it a map directory
      else if (name.equals (four_digit_year + "MAPS")) load_maps_dir (files[count], error_list);
      // is it the country information directory
      else if (name.equals ("CTRY_INF")) load_ctry_dir (files[count], error_list);
      // is it the observatory information directory
      else if (name.equals ("OBSY_INF")) load_obsy_dir (files[count], error_list);
      // is it the new 'CSV' tables directory
      else if (name.equals ("CSV")) load_csv_dir (files[count], error_list);
      // otherwise it should be an observatory sub-directory
      else
      {
        observatory = new CDObservatory (files[count], year, error_list);
        observatories.add (observatory);
      }
    }

    // check that all files are present
    if (readme_text == null)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No readme.all in ctry_inf sub-directory", base_dir, year);
    if (all_map_file == null)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No all.pcx, all.gif or all.png in maps sub-directory", base_dir, year);
    if (intro_screen == null)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No intro.pcx, intro.gif or intro.png in ctry_inf sub-directory", base_dir, year);
    if (all_countries_plot == null)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No allsrn.pcx, allsrn.gif or allsrn.png in ctry_inf sub-directory", base_dir, year);
    if (countries.size () <= 0)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No country maps or text files in directory", base_dir, year);
    for (count=0; count<countries.size(); count++)
    {
      country = (CDCountry) countries.get (count);
      if (country.map_plot == null)
        error_list.Add (CDErrorList.WARNING_MESSAGE, "Missing observatory map for country " + country.country_code, year);
      if (country.agency_plot == null)
        error_list.Add (CDErrorList.WARNING_MESSAGE, "Missing agency plot for country " + country.country_code, year);
      if (country.readme_text == null)
        error_list.Add (CDErrorList.WARNING_MESSAGE, "Missing readme file for country " + country.country_code,year);
    }

    // find the indexes of the columns that we want from the country table
    try
    {
      code_idx = country_table.FindColumn ("IMAG_code");
      name_idx = country_table.FindColumn ("country");
      iso3_idx = country_table.FindColumn ("code_3_digit");
    
      // find a record in the country table to match each country from the CD
      for (count=0; count<countries.size(); count++)
      {
        country = (CDCountry) countries.get (count);
        try
        {
          row_idx = country_table.FindFirst (code_idx, country.country_code, true);
          if (row_idx < 0) 
            row_idx = country_table.FindFirst (iso3_idx, country.country_code, true);
          country.country_name = country_table.GetStringData (row_idx, name_idx);
          country.iso3digit_code = country_table.GetStringData (row_idx, iso3_idx);
        }
        catch (CSVDatabaseException e) {error_list.Add (CDErrorList.ERROR_MESSAGE, "Can't find IMAG country code " + country.country_code + " in country database table", year); }
       }
    }
    catch (CSVDatabaseException e) {error_list.Add (CDErrorList.ERROR_MESSAGE, "Internal Error: Unable to find columns in country_table", year); }

    // find the indexes of the columns that we want from the observatory table
    try
    {
      code_idx = observatory_table.FindColumn ("code");
      name_idx = observatory_table.FindColumn ("name");
      lat_idx = observatory_table.FindColumn ("latitude");
      long_idx = observatory_table.FindColumn ("longitude");
      alt_idx = observatory_table.FindColumn ("altitude");
      country_code_idx = observatory_table.FindColumn ("country_code");
      syear_idx = observatory_table.FindColumn ("valid_start");
      eyear_idx = observatory_table.FindColumn ("valid_end");

      // for each observsatory ...
      for (count=0; count<observatories.size(); count++)
      {
        observatory = (CDObservatory) observatories.get (count);
        
        // find a record in the observatory table to match each observatory from the CD
        try
        {
          for (quit=false, row_idx = observatory_table.FindFirst (code_idx, observatory.GetObservatoryCode(), true);
               ! quit; row_idx = observatory_table.FindNext ())
          {
            start_year = observatory_table.GetIntData (row_idx, syear_idx);
            end_year = observatory_table.GetIntData (row_idx, eyear_idx);
            quit = true;
            if (start_year != 0 && year < start_year) quit = false;
            if (end_year != 0 && year > end_year) quit = false;
            if (quit)
            {
              observatory.SetExtraDetails (observatory_table.GetStringData (row_idx, name_idx),
                                           observatory_table.GetDoubleData (row_idx, lat_idx),
                                           observatory_table.GetDoubleData (row_idx, long_idx),
                                           observatory_table.GetIntData (row_idx, alt_idx));
              obsy_country_code = observatory_table.GetStringData (row_idx, country_code_idx);
              
              // attempt to relate this observatory to its country
              quit2 = false;
              for (count2=0; count2<countries.size(); count2++)
              {
                country = (CDCountry) countries.get (count2);
                if (country.country_code.equalsIgnoreCase(obsy_country_code))
                  quit2 = true;
                else if (country.iso3digit_code != null)
                {
                  if (country.iso3digit_code.equalsIgnoreCase (obsy_country_code))
                    quit2 = true;
                }
                if (quit2)
                {
                  observatory.SetCountry (country);
                  break;
                }
              }
              if (! quit2) error_list.Add (CDErrorList.WARNING_MESSAGE, "Can't find country for observatory code " + observatory.GetObservatoryCode() + " (need CCCsrn.pcx, CCCsrn.gif, CCCsrn.png or readme.CCC files in ctry_inf sub-directory)", year);
            }
          }
        }
        catch (CSVDatabaseException e) {error_list.Add (CDErrorList.ERROR_MESSAGE, "Can't find observatory code " + observatory.GetObservatoryCode() + " in observatory database table", year); }

      }
    }
    catch (CSVDatabaseException e) {error_list.Add (CDErrorList.ERROR_MESSAGE, "Internal Error: Unable to find columns in observatory_table", year); }

    error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Completed processing", year);
    
  }

  /******************************************************************
   * Return the year this object represents
   *
   * @return the year
   ******************************************************************/
  public int GetYear ()
  {
    return year;
  }
  
  /******************************************************************
   * Return the base directory for this CDYear object
   *
   * @return the base directory as a File
   ******************************************************************/
  public File GetBaseDir ()
  {
    return base_dir;
  }

  /******************************************************************
   * Get the number of countries in this year
   *
   * @return the number of contries, which may be 0
   ******************************************************************/
  public int GetNCountries ()
  {
    return countries.size();
  }

  /******************************************************************
   * Get a country object
   *
   * @param index number of the country object (0..n_countries)
   * @return the country details
   ******************************************************************/
  public CDCountry GetCountry (int index)
  {
    return (CDCountry) countries.elementAt(index);
  }

  /******************************************************************
   * Find a country from its country code
   *
   * @param code the country to find
   * @param the country OR null if it doesn't exist
   ******************************************************************/
  public CDCountry FindCountry (String code)
  {
    int count;
    CDCountry country;
    
    for (count=0; count<GetNCountries(); count++)
    {
      country = GetCountry (count);
      if (code.equals (country.country_code)) return country;
    }
    return null;
  }
  
  /******************************************************************
   * Get the number of observatories in this year
   *
   * @return the number of observatories, which may be 0
   ******************************************************************/
  public int GetNObservatories ()
  {
    return observatories.size();
  }

  /******************************************************************
   * Get an observatory object
   *
   * @param index number of the observatory object (0..n_observatories)
   * @return the observatory details
   ******************************************************************/
  public CDObservatory GetObservatory (int index)
  {
    return (CDObservatory) observatories.elementAt(index);
  }

  /********************************************************************
   * Get the requested file
   *
   * @param type one of the file type codes defined in this class
   * @return the file details or null if the file does not exist
   ********************************************************************/
  public File GetFile (int type)
  {
    switch (type)
    {
    case FILE_README: return readme_text;
    case FILE_ALL_MAP: return all_map_file;
    case FILE_INTRO: return intro_screen;
    case FILE_ALL_COUNTRIES: return all_countries_plot;
    }
    return null;
  }

  /*********************************************************************
   * Get the number of versions of the country database file on the disk
   *
   * @return the number 
   ********************************************************************/
  public int GetNCountryDatabaseVersions ()
  {
    return country_databases.size();
  }

  /********************************************************************
   * Get file that contains a version of the country database. Note
   * that the order in which these files are returned is random - 
   * it is simply the order they were found on disk (they are not
   * sorted into version number order
   ********************************************************************/
  public File GetCountryDatabseVersion (int index)
  {
    return (File) country_databases.elementAt(index);
  }
  
  /*********************************************************************
   * Get the number of versions of the observatory database file on the disk
   *
   * @return the number 
   ********************************************************************/
  public int GetNObservatoryDatabaseVersions ()
  {
    return observatory_databases.size();
  }

  /********************************************************************
   * Get file that contains a version of the observatory database. Note
   * that the order in which these files are returned is random - 
   * it is simply the order they were found on disk (they are not
   * sorted into version number order
   ********************************************************************/
  public File GetObservatoryDatabseVersion (int index)
  {
    return (File) observatory_databases.elementAt(index);
  }
  
  /********************************************************************
   * Test if a directory appears to be an INTERMAGNET CD 
   * year directory
   *
   * @param dir the directory test
   * @return the year
   * @throws CDException if the directory is not a CD year dir
   ********************************************************************/
  public static int TestYearDir (File dir)
  throws CDException
  {
    int year;
    String name;
    File test_file;
    boolean found_year;
    

    // check if the directory name is in the form mag<yyyy>
    found_year = true;
    year = 0;
    name = dir.getName ();
    if (! name.regionMatches (true, 0, "mag", 0, 3)) throw (new CDException ("Bad CD year directory"));
    try
    {
      year = Integer.parseInt (name.substring (3));
    } catch (NumberFormatException e) 
    { throw (new CDException ("Bad CD year directory")); }

    // check that there is a CTRY_INF and a yyyyMAPS directory
    // this check needs to be done for upper and lower case
    // (as far as possible) becuase some OS (e.g. Linux) show the
    // CD contents in lower case
    test_file = new File (dir, "CTRY_INF");
    if (! test_file.exists ())
    {
      test_file = new File (dir, "ctry_inf");
      if (! test_file.exists ())
      {
        test_file = new File (dir, "Ctry_inf");
        if (! test_file.exists ())
          throw (new CDException ("Bad CD year directory"));
      }
    }
    test_file = new File (dir, Integer.toString (year) + "MAPS");
    if (! test_file.exists ())
    {
      test_file = new File (dir, Integer.toString (year) + "maps");
      if (! test_file.exists ())
      {
        test_file = new File (dir, Integer.toString (year) + "Maps");
        if (! test_file.exists ())
          throw (new CDException ("Bad CD year directory"));
      }
    }

    return year;
    
  }

  /******************************************************************
   * Load the contry maps directory
   *
   * @param base_dir the base directory for the country maps
   * @param error_list a list of errors which this object can add to
   ******************************************************************/
  private void load_maps_dir (File base_dir, CDErrorList error_list)
  {
    int count;
    String name, code;
    File files [];
    CDCountry country;
    
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();

      // ignore some files
      if (name.equals("INTRO" + two_digit_year + ".PCX"))
      {
        error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Ignoring file", files[count], year);
        continue;
      }
      
      // there should only be plain files here, also
      // all files should be of type pcx, gif or png
      if ((! files[count].isFile()) ||
          ((! name.endsWith(".PCX")) && (! name.endsWith(".GIF")) && (! name.endsWith(".PNG"))) || 
          (name.length() <= 4))
        error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
      // find the special all.pcx file
      else if (name.equals ("ALL.PCX")) all_map_file = files [count];
      else if (name.equals ("ALL.GIF")) all_map_file = files [count];
      else if (name.equals ("ALL.PNG")) all_map_file = files [count];
      // process a country file
      else
      {
        // get the country code and add the map file to it
        code = name.substring (0, name.length() - 4);
        country = FindCountry (code);
        if (country == null) country = add_country (code);
        country.map_plot = files [count];
      }
    }
  }
  
  /******************************************************************
   * Load the country information directory
   *
   * @param base_dir the base directory for the country information
   * @param error_list a list of errors which this object can add to
   ******************************************************************/
  private void load_ctry_dir (File base_dir, CDErrorList error_list)
  {
    int count;
    boolean file_ok;
    String name, code;
    File files [];
    CDCountry country;
    
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();
      file_ok = true;

      // ignore some files
      if (name.equals("CTRYLIST.IDX") || name.equals("CTRYLIST.IDY") ||
          name.equals("INTRO00A.PCX") || name.equals("INTRO00B.PCX") ||
          name.equals("INTRO2.PCX") || name.equals("INTRO1.PCX") ||
          name.equals("README.BAK"))
      {
        error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Ignoring file", files[count], year);
        continue;
      }
      
      // there should only be plain files here
      if (! files[count].isFile()) file_ok = false;
      // find the special intro.pcx, readme.all and allsrn.pcx file
      else if (name.equals ("INTRO.PCX")) intro_screen = files [count];
      else if (name.equals ("INTRO.GIF")) intro_screen = files [count];
      else if (name.equals ("INTRO.PNG")) intro_screen = files [count];
      else if (name.equals ("README.ALL")) readme_text = files [count];
      else if (name.equals ("ALLSRN.PCX")) all_countries_plot = files [count];
      else if (name.equals ("ALLSRN.GIF")) all_countries_plot = files [count];
      else if (name.equals ("ALLSRN.PNG")) all_countries_plot = files [count];
      // process readme files
      else if (name.startsWith("README.") && (name.length() > 7))
      {
        // get the country code and the readme file to it
        code = name.substring (7);
        country = FindCountry (code);
        if (country == null) country = add_country (code);
        country.readme_text = files [count];
      }
      // process plots of agency information
      else if ((name.endsWith("SRN.PCX") || name.endsWith("SRN.GIF") || name.endsWith("SRN.PNG")) && 
               (name.length() > 7))
      {
        // get the country code and add the agency plot to it
        code = name.substring (0, name.length() - 7);
        country = FindCountry (code);
        if (country == null) country = add_country (code);
        country.agency_plot = files [count];
      }
      // process new style country lists
      else if (name.startsWith("CTR_V")) country_databases.add (files[count]);
      // anything else shouldn't be there
      else file_ok = false;
      
      // was the file processed OK??
      if (! file_ok) error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
    }
  }

  /******************************************************************
   * Load the observatory information directory
   *
   * @param base_dir the base directory for the observatory information
   * @param error_list a list of errors which this object can add to
   ******************************************************************/
  private void load_obsy_dir (File base_dir, CDErrorList error_list)
  {
    int count;
    boolean file_ok;
    String name, code;
    File files [];
    
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();
      file_ok = true;

      // ignore some files
      if (name.endsWith("OBSDAT.DBF"))
      {
        error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Ignoring file", files[count], year);
        continue;
      }
      
      // there should only be plain files here
      if (! files[count].isFile()) file_ok = false;
      // process new style observatory lists
      else if (name.startsWith("OBS_V")) observatory_databases.add (files[count]);
      // anything else shouldn't be there
      else file_ok = false;
      
      // was the file processed OK??
      if (! file_ok) error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
    }
  }

  /******************************************************************
   * Load the 'CSV' directory
   *
   * @param base_dir the base directory for the observatory information
   * @param error_list a list of errors which this object can add to
   ******************************************************************/
  private void load_csv_dir (File base_dir, CDErrorList error_list)
  {
    int count;
    boolean file_ok;
    String name, code;
    File files [];
    
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();
      file_ok = true;

      // there should only be plain files here
      if (! files[count].isFile()) file_ok = false;
      // process new style country lists
      else if (name.startsWith("CTR_V")) country_databases.add (files[count]);
      // process new style observatory lists
      else if (name.startsWith("OBS_V")) observatory_databases.add (files[count]);
      // anything else shouldn't be there
      else file_ok = false;
      
      // was the file processed OK??
      if (! file_ok) error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
    }
  }

  /******************************************************************
   * Add a country to the country list
   *
   * @param code the code for the country
   * @return the country
   ******************************************************************/
  private CDCountry add_country (String code)
  {
    CDCountry country;
    
    country = new CDCountry (code);
    countries.add (country);
    return country;
    
  }

  /** compare with another CDYear to allow sorting */
  public int compareTo(CDYear other) 
  {
    if (this.year < other.year) return -1;
    if (this.year > other.year) return +1;
    return 0;
  }

}
