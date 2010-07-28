/*
 * Database.java
 *
 * Created on 27 April 2002, 09:23
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.File;
import java.util.Vector;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.Database.*;
import bgs.geophys.library.File.FileUtils;
import java.io.FileNotFoundException;

/**
 * Hold data that defines an IMAG CD database.
 *
 * @author  smf
 * @version
 */
public class CDDatabase extends Object
{

  // private members
  private File base_dir;          // base directory (full path)
  private Vector<CDYear> year_list;       // array of CDYear objects that this database covers
  private CSVDatabaseTable country_table;     // list of countries
  private CSVDatabaseTable observatory_table; // list of observatories
  private CDErrorList error_list; // list of errors found during parsing
  private File errata_file;       // the file ERRATA/ERRATA.TXT from the CD
  private long errata_file_length;
  private File readme_file;       // the README.TXT file at the base of the CD directory structure
  private long readme_file_length;
  private int earliest_year;      // the earliest year held in this database
  private int latest_year;        // the latest year held in this database
  private CDObservatoryIterator obsy_iterator;     // object to allow iteration over all loaded observatories
  private boolean obsy_iterator_needs_updating;    // if TRUE then the iterator must be recreated

  /********************************************************************
   * Creates new Database
   *
   * @param base_dir the base directory
   * @params country_table list of countries
   * @params observatory_table list of observatories
   * @throws CDException if the given base dir contains no
   *         INTERMAGNET data
   ********************************************************************/
  public CDDatabase(File base_dir, CSVDatabaseTable country_table, CSVDatabaseTable observatory_table)
  throws CDException
  {

    int count, year;
    File files [];
    File errataDirectory;

    // store the tables
    this.country_table = country_table;
    this.observatory_table = observatory_table;
    
    // store the base directory, initialise the year array and the error_list
    this.base_dir = base_dir;
    year_list = new Vector<CDYear> ();
    error_list = new CDErrorList ();
    earliest_year = 99999;

    if (! base_dir.isDirectory ()) 
        throw (new CDException ("Bad directory - " + base_dir.getAbsolutePath() + " is not a directory"));
    try
    {
      // list the files in the directory
      files = base_dir.listFiles();

      // traverse the list, counting the IMAG CD year directories
      if (files != null)
      {
        for (count=0; count<files.length; count++)
        {
          if (files[count].isDirectory ())
          {
            try
            {
              // search for IMAG CD directory names
              year = CDYear.TestYearDir(files[count]);
              year_list.add (new CDYear (files[count], year, country_table, observatory_table, error_list));
              if (earliest_year == 99999)
                earliest_year = latest_year = year;
              else
              {
                if (year > latest_year) latest_year = year;
                if (year < earliest_year) earliest_year = year;
              }
            }
            catch (CDException e) { }
          }
        }
      }
      
      // check there are some year directories
      if (year_list.size () <= 0) 
          throw (new CDException (base_dir.getAbsolutePath() + " does not contain INTERMAGNET data (ctry_inf and <yyyy>maps are missing)"));

      // get the database's errata and readme files, allow case sensitive
           // errata_file = new File (base_dir.toString() + File.separator + "ERRATA" + File.separator + "ERRATA.TXT");
           // readme_file = new File (base_dir.toString() + File.separator + "README.TXT");
    try{
      errataDirectory = FileUtils.FindFileIgnoreCase(base_dir, "ERRATA");
      errata_file = FileUtils.FindFileIgnoreCase(errataDirectory,"ERRATA.TXT");
      errata_file_length = errata_file.length ();
      }catch(FileNotFoundException e){
        errata_file_length = 0;
        }
      
    try{
      readme_file = FileUtils.FindFileIgnoreCase(base_dir, "README.TXT");
      readme_file_length = readme_file.length ();
      }catch(FileNotFoundException e){
        readme_file_length = 0;
        }
      
    }  
    catch (SecurityException e) { }

    obsy_iterator_needs_updating = true;
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

  /********************************************************************
   * Get the base directory for this database
   *
   * @return the base directory
   ********************************************************************/
  public File GetBaseDir ()
  {
    return base_dir;
  }

  /********************************************************************
   * Get the number of years covered by this database
   *
   * @return the number of years
   ********************************************************************/
  public int GetNYears ()
  {
    return year_list.size ();
  }

  /********************************************************************
   * Get the year specified by the given index
   *
   * @param index the index of the required year
   * @return the year object
   ********************************************************************/
  public CDYear GetYear (int index)
  {
    return (CDYear) year_list.elementAt (index);
  }

  /********************************************************************
   * Check if the given year is available in the database.
   *
   * @param test_year the year to test for
   * @return true if the year is available
   ********************************************************************/
  public boolean IsYearAvailable (int test_year)
  {
    int count;

    for (count=0; count<year_list.size (); count++)
    {
      if (((CDYear) year_list.elementAt(count)).GetYear() == test_year)
        return true;
    }
    return false;
  }

  /** get the list of errors that was created when this database was loaded
   * @return the error list */
  public CDErrorList GetErrorList () { return error_list; }

  /** get the length of the errata file
   * @returns the length, 0 indicates no file */
  public long GetErrataFileLength () { return errata_file_length; }

  /** get the name of the errata file
   * @returns the full path to the file */
  public String GetErrataFile () { return errata_file.toString (); }
  
  /** get the length of the readme file
   * @returns the length, 0 indicates no file */
  public long GetReadmeFileLength () { return readme_file_length; }
  
  /** get the name of the readme file
   * @returns the full path to the file */
  public String GetReadmeFile () { return readme_file.toString (); }

  /** get the earliest year in this database */
  public int GetEarliestYear () { return earliest_year; }  

  /** get the latest year in this database */
  public int GetLatestYear () { return latest_year; }

    private File FindFileIgnoreCase(File base_dir, String string) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
  
}
