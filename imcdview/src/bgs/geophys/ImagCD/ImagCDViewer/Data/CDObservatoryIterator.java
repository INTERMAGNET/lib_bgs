/*
 * CDObservatoryIterator.java
 *
 * Created on 19 May 2003, 10:06
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.util.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * Iterate through the loaded databases
 * @author  smf
 */
public class CDObservatoryIterator extends Object 
{

    /** inner class to hold data about a single observatory */
    public class ObservatoryInfo extends Object
    implements Comparable<ObservatoryInfo>
    {
        private String obsy_code;               // copy of the observatory code for easy access
        private double latitude;                // copy of observatory position data for easy access
        private double longitude;               // MAY BE MISSING (set to CDObservatory.MISSING_DATA)
        private int display_x;                  // X position on the map, may be MISSING
        private int display_y;                  // Y position on the map, may be MISSING
        private CDCountry country;              // copy of country data for the observatory for easy access
                                                // MAY BE NULL if a country could not be found
        private Vector<CDObservatory> observatory_data_list;   // array of CDObservatory objects
        private String display_name;            // the name that the user will be shown for this observatory
        
        
        /** construct an observatory info object
         * @param observatory_data the first observatory data object
         * @param the country for the observatory (may be NULL) */
        public ObservatoryInfo (CDObservatory observatory_data)
        {
            String string;
            
            obsy_code = observatory_data.GetObservatoryCode ();
            latitude = observatory_data.GetLatitude ();
            longitude = observatory_data.GetLongitude ();
            display_x = display_y = CDObservatory.MISSING_DATA;
            country = observatory_data.GetCountry ();
            observatory_data_list = new Vector<CDObservatory> ();
            observatory_data_list.add (observatory_data);
            string = observatory_data.GetObservatoryName ();
            if (string != null)
                display_name = observatory_data.GetObservatoryCode () + " (" + string + ")";
            else
                display_name = observatory_data.GetObservatoryCode ();
        }
        
        public void Add (CDObservatory observatory_data) { observatory_data_list.add (observatory_data); }
        public void SetDisplayPosition (int x, int y) { display_x = x; display_y = y; }
        public String GetObservatoryCode () { return obsy_code; }
        public double GetLatitude () { return latitude; }
        public double GetLongitude () { return longitude; }
        public int GetDisplayX () { return display_x; }
        public int GetDisplayY () { return display_y; }
        public String GetDisplayName () { return display_name; }
        public int GetNDataSegments () { return observatory_data_list.size(); }
        public CDObservatory GetObservatoryData (int count) { return (CDObservatory) observatory_data_list.get (count); }
        public CDCountry GetObservatoryCountry () { return country; }
        
        public int compareTo(ObservatoryInfo o) 
        {
            return display_name.compareTo (o.display_name);
        }
        
    }
    
    // private members
    private Vector<ObservatoryInfo> observatory_list;        // list of observatories, created at instantiation
    private int count_index;                // index used to count through the list
    
    /** Creates a new instance of CDObservatoryIterator
     * @param database_list the current list of databases */
    public CDObservatoryIterator (CDDatabaseList database_list) 
    {
        int database_count;
        CDDatabase database;
        
        // create the vector of observatories
        observatory_list = new Vector<ObservatoryInfo> ();
        count_index = 0;
        
        // for each database ...
        try
        {
            for (database_count=0; database_count<database_list.GetNDatabases(); database_count++)
            {
                database = database_list.GetDatabase(database_count);
                AddDatabase (database);
            }
        }
        catch (CDException e) { SwingUtils.ExceptionHandler (e); }
    }
    
    /** Creates a new instance of CDObservatoryIterator
     * @param database_list the current list of databases */
    public CDObservatoryIterator (CDDatabase database) 
    {
        // create the vector of observatories
        observatory_list = new Vector<ObservatoryInfo> ();
        count_index = 0;
        
        AddDatabase (database);
    }
    
    /** Add an observatory object to the list
     * observatory the observatory to add */
    public void Add (CDObservatory observatory)
    {
        boolean found;
        int list_count;
        ObservatoryInfo observatory_element;
        CDCountry country;
        
        // is the observatory in the list already??
        found = false;
        for (list_count=0; list_count<observatory_list.size(); list_count++)
        {
            observatory_element = (ObservatoryInfo) observatory_list.get (list_count);
            if (observatory.GetObservatoryCode().equals (observatory_element.GetObservatoryCode ()))
            {
                observatory_element.Add (observatory);
                found = true;
                break;
            }
        }
        
        // if the observatory was not in the list, create a new element
        if (! found)
        {
            observatory_element = new ObservatoryInfo (observatory);
            observatory_list.add (observatory_element);
        }
    }

    /** sort the list of observatories */
    public void Sort ()
    {
        Collections.sort (observatory_list);
    }
    
    /** get the first observatory in the list
     * @return the observatory */
    public ObservatoryInfo GetFirstObservatory ()
    {
        count_index = 0;
        return GetNextObservatory ();
    }

    /** get the next observatory in the list
     * @return the observatory */
    public ObservatoryInfo GetNextObservatory ()
    {
        if (count_index < observatory_list.size ())
            return (ObservatoryInfo) observatory_list.get (count_index ++);
        return null;
    }
    
    /** find the number of observatories in this iterator
     * @return the number of observatories */
    public int GetNObservatories () { return observatory_list.size (); }
    
    private void AddDatabase (CDDatabase database)
    {
        int year_count, observatory_count;
        CDYear year;
        CDObservatory observatory;
        
        // for each year of data ...
        for (year_count=0; year_count<database.GetNYears(); year_count++)
        {
            year = database.GetYear (year_count);
                
            // for each observatory
            for (observatory_count=0; observatory_count<year.GetNObservatories(); observatory_count++)
            {
                observatory = year.GetObservatory (observatory_count);
                Add (observatory);
            }
        }
    }
}
