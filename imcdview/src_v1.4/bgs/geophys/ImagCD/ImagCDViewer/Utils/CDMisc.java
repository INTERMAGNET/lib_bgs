/*
 * Utils.java
 *
 * Created on 02 June 2002, 16:11
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.Database.*;
import bgs.geophys.library.File.*;
import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;

/**
 * Utilities - mainly subroutines without another home
 * @author  Simon
 * @version
 */
public class CDMisc extends Object
{

 /********************************************************************
  * findData - get data for the given month and year from an array of
  *            observatory objects. If -1 is passed as the month
  *            index all months are required for this year. Tries to
  *            get zipped data if no unzipped data is available.
  *
  * @param obsy_code - the observatory required
  * @param year - the year required
  * @param month - the month index required (0..11) OR
  *                -1 if all months are required for this year
  *                -2 to find the first available month
  * @param use_zip - if true read from zip files, otherwise don't
  * @param use_plain - if true read from non-zip files, otherwise don't
  * @param base_dir - the base directory that identifies the data source
  *        to look in - to look in all data sources, set this to null
  * @return an array of CDDataMonth objects. The array will have 12
  *         elements if one year of data was requested, otherwise
  *         it will have 1. If no data is available for a month, the
  *         CDDataMonth object for this element will be null.
  ********************************************************************/
  public static CDDataMonth [] findData (String obsy_code, int year, int month,
                                         boolean use_zip, boolean use_plain,
                                         String base_dir)
  {
    final int N_MONTHS = 12;
    CDObservatory observatory;
    CDDataMonth m_data;
    CDDatabase database;
    CDDataMonth [] data_array = null;
    int obs_count, month_count, n;
    Vector obsy_list;
    
    // initialise return variable
    if (month == -1)
    {
      // data storage for one year of data
      data_array = new CDDataMonth [N_MONTHS];
      for (n = 0; n < N_MONTHS; n++)
        data_array [n] = null;
    }
    else
    {
      // data storage for one month of data
      data_array = new CDDataMonth [1];
      data_array [0] = null;
    }

    // find the list of observatory objects for this year
    if (base_dir == null)
      obsy_list = getObservatoryList (obsy_code, year);
    else
    {
      database = findDataSource(base_dir);
      if (database == null) return data_array;
      obsy_list = getObservatoryList (obsy_code, year, database);
    }
    if (obsy_list == null) return data_array;
            
    // get data from observatory objects in list
    for (obs_count = 0; obs_count < obsy_list.size (); obs_count++)
    {
      observatory = (CDObservatory) obsy_list.elementAt (obs_count);
      
      // what do we want to load
      switch (month)
      {
      case -1:
        // load the entire year
        for (month_count = 0; month_count < N_MONTHS; month_count++)
        {
          // try loading unzipped data first
          if (use_plain)
          {
            m_data = new CDDataMonth (month_count+1, observatory, false);
            if (m_data.hasDataFile ()) data_array [month_count] = m_data;
          }
          // try loading zipped data, if needed
          if ((data_array [month_count] == null) && use_zip)
          {
            m_data = new CDDataMonth (month_count+1, observatory, true);
            if (m_data.hasDataFile ()) data_array [month_count] = m_data;
          }
        }
        break;
      case -2:
        // load the first available month
        for (month_count = 0; month_count < N_MONTHS; month_count++)
        {
          // try loading unzipped data first
          if (use_plain)
          {
            m_data = new CDDataMonth (month_count+1, observatory, false);
            if (m_data.hasDataFile ()) data_array [0] = m_data;
          }
          // try loading zipped data, if needed
          if ((data_array [0] == null) && use_zip)
          {
            // try loading zipped data
            m_data = new CDDataMonth (month_count+1, observatory, true);
            if (m_data.hasDataFile ()) data_array [0] = m_data;
          }
          if (data_array [0] != null) break;
        }
        break;
      default:
        // load the given month - try loading unzipped data first
        if (use_plain)
        {
          m_data = new CDDataMonth (month+1, observatory, false);
          if (m_data.hasDataFile ()) data_array [0] = m_data;
        }
        // try loading zipped data, if needed
        if ((data_array [0] == null) && use_zip)
        {
          m_data = new CDDataMonth (month+1, observatory, true);
          if (m_data.hasDataFile ()) data_array [0] = m_data;
        }
        break;
      }
    }
    return data_array;
  }

 /********************************************************************
  * makeYearList - get a list of all the available data years from all
  *                observatories and all data sources
  *
  * @return - a Vector of CDYear objects
  ********************************************************************/
  public static Vector<CDYear> makeYearList ()
  {
      int count, count2, new_year, count3;
      boolean found;
      CDDatabaseList database_list;
      CDDatabase database;
      Vector<CDYear> year_list;

      // initialise objects
      year_list = new Vector<CDYear> ();

      database_list = GlobalObjects.database_list;
      for (count = 0; count < database_list.GetNDatabases (); count ++)
      {
          try
          {
              database = database_list.GetDatabase (count);
          }
          catch (CDException e)
          {
              SwingUtils.ExceptionHandler (e);
              return null;
          }

          for (count2 = 0; count2 < database.GetNYears (); count2 ++)
          {
              new_year = database.GetYear (count2).GetYear ();

              // have we seen this year before
              found = false;
              for (count3=0; count3<year_list.size(); count3++)
              {
                  if (year_list.get(count3).GetYear() == new_year)
                  {
                      found = true;
                      break;
                  }
              }
              
              // no - add it
              if (! found) year_list.add (database.GetYear(count2));
          }
      }

      // sort the list
      Collections.sort (year_list);
      
      return year_list;
  }
  
 /********************************************************************
  * makeYearList - get a list of all the available data years from all
  *                observatories in the given data source
  *
  * @param database - the data source to search
  * @return - a Vector of CDYear objects
  ********************************************************************/
  public static Vector<CDYear> makeYearList (CDDatabase database)
  {
      int count;
      Vector<CDYear> year_list;
      
      year_list = new Vector<CDYear> ();
      for (count = 0; count < database.GetNYears (); count ++)
          year_list.add (database.GetYear(count));
      Collections.sort (year_list);
      
      return year_list;
  }
  
 /********************************************************************
  * makeYearList - makes a list of the years for which data is
  *                available for the given observatory from all data sources
  * @param obsy_code - the code for the required observatory
  * @return a vector of Integer years.
  ********************************************************************/
  public static Vector<Integer> makeYearList (String obsy_code)
  {
    CDObservatoryIterator obsy_iterator;
    CDObservatoryIterator.ObservatoryInfo info;
    CDObservatoryIterator.ObservatoryInfo obsy_info = null;
    Vector<Integer> year_list;
    boolean found = false;
    int n, i, yr, n_years = 0;

    // initialise objects
    obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
    year_list = new Vector<Integer> ();

    // get observatory info from the code passed
    for (info = obsy_iterator.GetFirstObservatory(); info != null;
         info = obsy_iterator.GetNextObservatory())
    {
      // find observatory from observatory code
      if (info.GetObservatoryCode ().equals (obsy_code))
      {
        obsy_info = info;
        break;
      }
    }

    // observatory not found
    if (obsy_info == null)
    {
      return null;
    }

    // find the required segment(s) of data
    for (n = 0; n < obsy_info.GetNDataSegments (); n ++)
    {
      yr = obsy_info.GetObservatoryData (n).GetYear ();
      // go through each year to see if this one has been seen before
      for (i = 0; i < n_years; i++)
      {
        if (((Integer) year_list.elementAt (i)).equals (new Integer (yr)))
        {
          // this year has been seen before
          found = true;
          break;
        }
      }
      if (!found)
      {
        // new year, add to list
        n_years ++;
        year_list.add (new Integer (yr));
      }
      found = false;
    }
    
    // put years into correct order
    Collections.sort (year_list);
    return year_list;
  }

 /********************************************************************
  * makeYearList - makes a list of the years for which data is
  *                available for the given observatory in the given data source
  * @param obsy_code - the code for the required observatory
  * @param database - the data source to search
  * @return a vector of Integer years.
  ********************************************************************/
  public static Vector<Integer> makeYearList (String obsy_code, CDDatabase database)
  {
      int year_count, obsy_count, count;
      boolean found;
      CDYear cd_year;
      CDObservatory cd_obsy;
      Vector<Integer> year_list = null;
    
      for (year_count=0; year_count<database.GetNYears(); year_count++)
      {
          cd_year = database.GetYear(year_count);
          for (obsy_count=0; obsy_count<cd_year.GetNObservatories(); obsy_count++)
          {
              cd_obsy = cd_year.GetObservatory(obsy_count);
              if (cd_obsy.GetObservatoryCode().equalsIgnoreCase(obsy_code))
              {
                  if (year_list == null)
                      year_list = new Vector<Integer> ();
                
                  // check if the year has been recorded already
                  found = false;
                  for (count=0; count<year_list.size(); count++)
                  {
                      if (year_list.get(count).intValue() == cd_year.GetYear())
                      {
                          found = true;
                          break;
                      }
                  }
                  if (! found)
                      year_list.add (new Integer (cd_year.GetYear()));
              }
          }
      }
              
      Collections.sort (year_list);
      return year_list;
  }
  
  /***************************************************************************
   * Make a list of years for which there exist observatory files of the 
   * given type anywhere in the database list
   * @param obsy_code the code for the observatory
   * @param type the type of file - any of the type codes in CDObservatory
   * @return an array of files (may be 0 length)
   **************************************************************************/
  public static Vector<Integer> makeYearList (String obsy_code, int type)
  {
      int database_count, year_count, observatory_count, count;
      boolean found;
      CDDatabase database;
      CDYear year;
      CDObservatory observatory;
      File file;
      Vector<Integer> year_list;
      
      year_list = new Vector<Integer> ();
      
      // for each database ...
      try
      {
          for (database_count=0; database_count<GlobalObjects.database_list.GetNDatabases(); database_count++)
          {
              database = GlobalObjects.database_list.GetDatabase (database_count);
          
              // for each year ...
              for (year_count=0; year_count<database.GetNYears(); year_count++)
              {
                  year = database.GetYear (year_count);
                
                  // for each observatory ...
                  for (observatory_count=0; observatory_count<year.GetNObservatories(); observatory_count++)
                  {
                      observatory = year.GetObservatory (observatory_count);
                      if (observatory.GetObservatoryCode().equalsIgnoreCase(obsy_code))
                      {
                          // does the file exist ???
                          if (observatory.GetFile (type) != null)
                          {
                              // check if this year is already in the list, if not add it
                              found = false;
                              for (count=0; count<year_list.size(); count++)
                              {
                                  if (year_list.get(count).intValue() == year.GetYear())
                                  {
                                      found = true;
                                      break;
                                  }
                              }
                              if (! found) year_list.add (new Integer (year.GetYear()));
                          }
                      }
                  }
              }
          }
      }
      catch (CDException e) { }

      // put years into correct order
      Collections.sort (year_list);
      return year_list;
  }
  
  /***************************************************************************
   * Make a list of years for which there exist observatory files of the 
   * given type in the given data source
   * @param obsy_code the code for the observatory
   * @param type the type of file - any of the type codes in CDObservatory
   * @param database - the data source to search
   * @return an array of files (may be 0 length)
   **************************************************************************/
  public static Vector<Integer> makeYearList (String obsy_code, int type, CDDatabase database)
  {
      int year_count, observatory_count;
      Vector<Integer> year_list;
      CDYear year;
      CDObservatory observatory;
      
      year_list = new Vector<Integer> ();
      
      // for each year ...
      for (year_count=0; year_count<database.GetNYears(); year_count++)
      {
          year = database.GetYear (year_count);
                
          // for each observatory ...
          for (observatory_count=0; observatory_count<year.GetNObservatories(); observatory_count++)
          {
              observatory = year.GetObservatory (observatory_count);
              if (observatory.GetObservatoryCode().equalsIgnoreCase(obsy_code))
              {
                  // does the file exist ???
                  if (observatory.GetFile (type) != null)
                  {
                      year_list.add (new Integer (year.GetYear()));
                  }
              }
          }
      }
      
      Collections.sort (year_list);
      return year_list;
  }
  
 /********************************************************************
  * getObservatoryList - get a list of the CDObservatory objects for the
  *                      given observatory and year in all data sources
  *
  * @param obsy_code - the code for the required observatory
  * @param year - the year required
  * @return - a Vector of CDObservatory objects
  ********************************************************************/
  public static Vector<CDObservatory> getObservatoryList (String obsy_code, int year)
  {
    CDObservatoryIterator obsy_iterator;
    CDObservatoryIterator.ObservatoryInfo info;
    CDObservatoryIterator.ObservatoryInfo obsy_info = null;
    Vector<CDObservatory> obsy_list = null;
    int n;

    // initialise objects
    obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();

    // get observatory info from the code passed
    for (info = obsy_iterator.GetFirstObservatory(); info != null;
         info = obsy_iterator.GetNextObservatory())
    {
      // find observatory from observatory code
      if (info.GetObservatoryCode ().equals (obsy_code))
      {
        obsy_info = info;
        break;
      }
    }

    if (obsy_info != null)
    {
      // find the required segment(s) of data
      for (n = 0; n < obsy_info.GetNDataSegments (); n ++)
      {
        if (obsy_info.GetObservatoryData (n).GetYear () == year)
        {
          if (obsy_list == null)
          {
            // initialise return value
            obsy_list = new Vector<CDObservatory> ();
          }
          obsy_list.add (obsy_info.GetObservatoryData (n));
        }
      }
    }
    return obsy_list;
  }

 /********************************************************************
  * getObservatoryList - get a list of the CDObservatory objects for the
  *                      given observatory and year in the given data source
  *
  * @param obsy_code - the code for the required observatory
  * @param year - the year required
  * @param data_source - the data source to search
  * @return - a Vector of CDObservatory objects
  ********************************************************************/
  public static Vector<CDObservatory> getObservatoryList (String obsy_code, int year,
                                                          CDDatabase data_source)
  {
    int year_count, obsy_count;
    Vector<CDObservatory> obsy_list = null;
    CDYear cd_year;
    CDObservatory cd_obsy;
    
    for (year_count=0; year_count<data_source.GetNYears(); year_count++)
    {
      cd_year = data_source.GetYear(year_count);
      if (cd_year.GetYear() == year)
      {
        for (obsy_count=0; obsy_count<cd_year.GetNObservatories(); obsy_count++)
        {
          cd_obsy = cd_year.GetObservatory(obsy_count);
          if (cd_obsy.GetObservatoryCode().equalsIgnoreCase(obsy_code))
          {
            if (obsy_list == null)
              obsy_list = new Vector<CDObservatory> ();
            obsy_list.add (cd_obsy);
          }
        }
      }
    }
    
    return obsy_list;
  }  
  
 /********************************************************************
  * getObservatoryName - find the observatory name from the code
  *
  * @param obsy_code - the code for the required observatory
  * @return - the name
  ********************************************************************/
  public static String getObservatoryName (String obsy_code)
  {
    Vector obsy_list;
    Vector year_list;
    CDObservatory obsy;
    String name;
    int year;

    year_list = makeYearList (obsy_code);
    year = ((Integer)year_list.elementAt (year_list.size () -1)).intValue ();
    obsy_list = getObservatoryList (obsy_code, year);

    obsy = (CDObservatory) obsy_list.elementAt (0);
    name = obsy.GetObservatoryName ();
    return name;
  }
  
 /********************************************************************
  * find a data source from its base directory
  * 
  * @param base_dir the base directory
  * @return the data source or null
  ********************************************************************/
  public static CDDatabase findDataSource (String base_dir)
  {
    int count;
    CDDatabaseList db_list;
    CDDatabase database;
    File base_path;
    
    base_path = new File (base_dir);
    db_list = GlobalObjects.database_list;
    try
    {
      for (count=0; count<db_list.GetNDatabases(); count++)
      {
        database = db_list.GetDatabase(count);
        if (base_path.equals(database.GetBaseDir())) return database;
      }
    }
    catch (CDException e) { }
    return null;
  }
    
}

