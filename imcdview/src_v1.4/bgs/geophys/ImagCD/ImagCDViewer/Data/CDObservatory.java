/*
 * CDObservatory.java
 *
 * Created on 29 April 2002, 22:35
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.File;

/**
 * Class that scans and holds information about what data is
 * available in a single observatory sub-directory on an
 * intermagnet CD.
 *
 * @author  smf
 * @version 
 */
public class CDObservatory extends Object 
{

  // public members
  /** code for baseline values text file */
  public static final int FILE_BLV_TEXT =       1;
  /** code for the baseline values plot file */
  public static final int FILE_BLV_PLOT =       2;
  /** code for the readme file */
  public static final int FILE_README =         3;
  /** code for the index file */
  public static final int FILE_DKA =            4;
  /** code for the historic yearly means text file */
  public static final int FILE_YEAR_MEAN =      5;
  /** code for the related countries map plot -
   * THIS FILE LIVES IN THE ASSOCIATED COUNTY OBJECT -
   * this code is only for use with the GetFile() method */
  public static final int FILE_COUNTRY_MAP =    6;
  /** code for the related countries information plot -
   * THIS FILE LIVES IN THE ASSOCIATED COUNTY OBJECT -
   * this code is only for use with the GetFile() method */
  public static final int FILE_COUNTRY_INFO =   7;
  /** code for the related countries readme file -
   * THIS FILE LIVES IN THE ASSOCIATED COUNTY OBJECT -
   * this code is only for use with the GetFile() method */
  public static final int FILE_COUNTRY_README = 8;
  
  /** code for missing numeric data */
  public static final int MISSING_DATA =     999999;
  
  // private members
  private File base_dir;             // base directory (full path)
  private int year;                  // year for this data
  private String obsy_code;          // code for the observatory (in uppercase)
  private File month_files [];       // list of available months (null if unavailable)
  private File zip_month_files [];   // list of available compressed months (null if unavailable)
  private File blv_text_file;        // baseline text file or null
  private File blv_plot_file;        // baseline plot file or null
  private File readme_file;          // readme file or null
  private File dka_file;             // index text (dka) file or null
  private File year_mean_file;       // text file of historic yearly mean values
  
  // private details about an observatory - these are filled in
  // after the observatory is created, so could be missing
  private String obsy_name;          // free text name
  private double latitude;
  private double longitude;
  private int altitude;              // altitude in metres
  private CDCountry country;

  /*******************************************************************
   * Create a new CDObservatory
   *
   * @param base_dir the base directory for this observatory in the
   *                 CD file structure
   * @param year the year for this data
   * @param error_list a list of errors which this object can add to
   *******************************************************************/  
  public CDObservatory (File base_dir, int year, CDErrorList error_list)
  {
    int count, n_months;
    String name, year_string, year_long_string;
    File files [];

    // flag all files as missing
    month_files = new File [12];
    zip_month_files = new File [12];
    for (count=0; count<12; count++)
    {
      month_files[count] = null;
      zip_month_files[count] = null;
    }
    blv_text_file = null;
    blv_plot_file = null;
    readme_file = null;
    dka_file = null;
    year_mean_file = null;

    // flag all details as missing
    obsy_name = null;
    latitude = MISSING_DATA;
    longitude = MISSING_DATA;
    altitude = MISSING_DATA;
    country = null;

    // extract the observatory code, in uppercase, and store the
    // base directory and year
    obsy_code = base_dir.getName().toUpperCase();
    this.base_dir = base_dir;
    this.year = year;

    // get a two digit year string
    if ((year % 100) < 10) year_string = "0" + Integer.toString (year % 100);
    else year_string = Integer.toString (year % 100);
    // get a four digit year string
    year_long_string = Integer.toString(year);
    
    // scan the files in the observatory sub-directory
    files = base_dir.listFiles();
    for (count=0; count<files.length; count++)
    {
      // do all testing in uppercase
      name = files[count].getName().toUpperCase ();
      
      // ignore some files
      if (name.equals ("BASELINE.ADJ") || name.equals (obsy_code + ".PCX") ||
          name.equals (obsy_code + year_string + "PCX.BLV"))
      {
        error_list.Add (CDErrorList.INFORMATION_MESSAGE, "Ignoring file", files[count], year);
        continue;
      }
      
      // is it a readme file
      if (name.equals ("README." + obsy_code)) readme_file = files [count];
      else if (name.equals ("READ_ME." + obsy_code)) readme_file = files [count];
      // is it a baseline values file, text or plot
      else if (name.equals (obsy_code + "_BLV.PCX")) blv_plot_file = files [count];
      else if (name.equals (obsy_code + "_BLV.GIF")) blv_plot_file = files [count];
      else if (name.equals (obsy_code + "_BLV.PNG")) blv_plot_file = files [count];
      else if (name.equals (obsy_code + year_string + ".BLV")) blv_text_file = files [count];
      else if (name.equals (obsy_code + year_long_string + ".BLV")) blv_text_file = files [count];
      // is it a data file
      else if (name.equals (obsy_code + year_string + "JAN.BIN")) month_files [0] = files [count];
      else if (name.equals (obsy_code + year_string + "FEB.BIN")) month_files [1] = files [count];
      else if (name.equals (obsy_code + year_string + "MAR.BIN")) month_files [2] = files [count];
      else if (name.equals (obsy_code + year_string + "APR.BIN")) month_files [3] = files [count];
      else if (name.equals (obsy_code + year_string + "MAY.BIN")) month_files [4] = files [count];
      else if (name.equals (obsy_code + year_string + "JUN.BIN")) month_files [5] = files [count];
      else if (name.equals (obsy_code + year_string + "JUL.BIN")) month_files [6] = files [count];
      else if (name.equals (obsy_code + year_string + "AUG.BIN")) month_files [7] = files [count];
      else if (name.equals (obsy_code + year_string + "SEP.BIN")) month_files [8] = files [count];
      else if (name.equals (obsy_code + year_string + "OCT.BIN")) month_files [9] = files [count];
      else if (name.equals (obsy_code + year_string + "OKT.BIN")) month_files [9] = files [count];
      else if (name.equals (obsy_code + year_string + "NOV.BIN")) month_files [10] = files [count];
      else if (name.equals (obsy_code + year_string + "DEC.BIN")) month_files [11] = files [count];
      // is it a compressed data file
      else if (name.equals (obsy_code + year_string + "JAN.ZIP")) zip_month_files [0] = files [count];
      else if (name.equals (obsy_code + year_string + "FEB.ZIP")) zip_month_files [1] = files [count];
      else if (name.equals (obsy_code + year_string + "MAR.ZIP")) zip_month_files [2] = files [count];
      else if (name.equals (obsy_code + year_string + "APR.ZIP")) zip_month_files [3] = files [count];
      else if (name.equals (obsy_code + year_string + "MAY.ZIP")) zip_month_files [4] = files [count];
      else if (name.equals (obsy_code + year_string + "JUN.ZIP")) zip_month_files [5] = files [count];
      else if (name.equals (obsy_code + year_string + "JUL.ZIP")) zip_month_files [6] = files [count];
      else if (name.equals (obsy_code + year_string + "AUG.ZIP")) zip_month_files [7] = files [count];
      else if (name.equals (obsy_code + year_string + "SEP.ZIP")) zip_month_files [8] = files [count];
      else if (name.equals (obsy_code + year_string + "OCT.ZIP")) zip_month_files [9] = files [count];
      else if (name.equals (obsy_code + year_string + "NOV.ZIP")) zip_month_files [10] = files [count];
      else if (name.equals (obsy_code + year_string + "DEC.ZIP")) zip_month_files [11] = files [count];
      // is it a text index file
      else if (name.equals (obsy_code + year_string + "K.DKA")) dka_file = files [count];
      else if (name.equals (obsy_code + year_string + ".DKA")) dka_file = files [count];
      // is it a yearly mean text file
      else if (name.equals ("YEARMEAN." + obsy_code)) year_mean_file = files [count];
      // otherwise it shouldn't be here
      else error_list.Add (CDErrorList.WARNING_MESSAGE, "Unrecognised file found", files[count], year);
    }

    // check that all compulsory files are present
    n_months = 0;
    for (count=0; count<12; count++)
    {
      if ((month_files[count] != null) || (zip_month_files[count] != null)) n_months ++;
    }
    if (n_months <= 0)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No monthly data files in directory", base_dir, year);
    if (readme_file == null)
      error_list.Add (CDErrorList.WARNING_MESSAGE, "No observatory readme file in directory", base_dir, year);
  }

  /********************************************************************
   * Set the ancilliary details about an observatory
   *
   * @params obsy_name the observstory name
   * @params latitude latitude for the observatory
   * @params longitude longiutde for the observatory
   * @params altitude altitude in metres at the observatory
   ********************************************************************/
  public void SetExtraDetails (String obsy_name, double latitude, double longitude, int altitude)
  {
    this.obsy_name = obsy_name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.altitude = altitude;
  }
  
  /********************************************************************
   * Set the country details for the observatory
   *
   * @param country the country information
   ********************************************************************/
  public void SetCountry (CDCountry country)
  {
    this.country = country;
  }

  /********************************************************************
   * Get the observatory code for this observatory
   *
   * @return the observatory code
   ********************************************************************/
  public String GetObservatoryCode ()
  {
    return obsy_code;
  }
  
  /********************************************************************
   * Get the name for this observatory
   *
   * @return the observatory name
   ********************************************************************/
  public String GetObservatoryName ()
  {
    return obsy_name;
  }

  /********************************************************************
   * Get the latitude for this observatory
   *
   * @return the latitude
   ********************************************************************/
  public double GetLatitude ()
  {
    return latitude;
  }
  
  /********************************************************************
   * Get the longitude for this observatory
   *
   * @return the longitude
   ********************************************************************/
  public double GetLongitude ()
  {
    return longitude;
  }

  /********************************************************************
   * Get the altitude for this observatory
   *
   * @return the altitude
   ********************************************************************/
  public int GetAltitude ()
  {
    return altitude;
  }

  /********************************************************************
   * Get the country for this observatory
   *
   * @return the country
   ********************************************************************/
  public CDCountry GetCountry ()
  {
    return country;
  }

  /********************************************************************
   * Get the normal or compressed month file details
   *
   * @param month the month (1 to 12)
   * @param zip_flag if true return the zip file, otherwise
   *                 return the plain file
   * @return the file details or null if there is no such file
   ********************************************************************/
  public File GetMonthFile (int month, boolean zip_flag)
  {
    if (month < 1 || month > 12) return null;
    if (zip_flag) return zip_month_files [month -1];
    return month_files [month -1];
  }
  
  /********************************************************************
   * Get the requested file - this routine can return files from the
   * associated country as well as observatory files 
   *
   * @param type one of the file type codes defined in this class
   * @return the file details or null if the file does not exist
   ********************************************************************/
  public File GetFile (int type)
  {
    switch (type)
    {
    case FILE_BLV_TEXT: 
        return blv_text_file;
    case FILE_BLV_PLOT: 
        return blv_plot_file;
    case FILE_README: 
        return readme_file;
    case FILE_DKA: 
        return dka_file;
    case FILE_YEAR_MEAN: 
        return year_mean_file;
    case FILE_COUNTRY_MAP:
        if (country != null) return country.map_plot;
        break;
    case FILE_COUNTRY_INFO:
        if (country != null) return country.agency_plot;
        break;
    case FILE_COUNTRY_README:
        if (country != null) return country.readme_text;
        break;
    }
    return null;
  }

  /********************************************************************
   * Get the year for the data
   *
   * @return the year
   ********************************************************************/
  public int GetYear () { return year; }

}
