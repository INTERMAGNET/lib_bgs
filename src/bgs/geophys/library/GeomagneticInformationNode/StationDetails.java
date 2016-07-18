/*
 * StationDetails.java
 *
 * Created on 01 November 2006, 19:00
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * class that holds the station details 'table'
 *
 * @author  smf
 */
public class StationDetails 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    public class StationDetailsFields
    implements Comparable<StationDetailsFields>
    {
        /** 1.) 3-4 digit IAGA station code */
        public String station_code;
        /** 2.) Reserved (was component orientation) */
        public String reserved;
        /** 3.) Free text Observatory name */
        public String station_name;
        /** 4.) Observatory colatitude */
        public float colatitude;
        /** 5.) Observatory longitude */    
        public float longitude;
        /** 6.) Observatory 2 digit country code */
        public String country;
        /** 7.) Number of days reported (or test) data to hold */
        public int rhold_len;
        /** 8.) Number of days adjusted data to hold */
        public int ahold_len;
        /** 9.) Number of days definitive data to hold */
        public int dhold_len;
        /** 10.) Annual mean H at observatory (for converting d nT to D min.arc.) */
        public float ann_mean_h;
        /** 11.) Flag - if 0 observatory is production - if 1 observatory is test */
        public boolean test;
        
        /** implement compareTo to allow sorting - the sort order is determined
         * by the parent objects 'order' variable, which can be set with 
         * the method reOrder() 
         * @param o the object to compare with
         * @return -ve number if this object is less than the given object, +ve number
         *         if this objects is greater than the given object, 0 if the
         *         objects are equal. */
        public int compareTo (StationDetailsFields o)
        {
            int ret_val;

            switch (order)
            {
            case ORDER_BY_STATION_CODE:
                return station_code.compareTo(o.station_code);
            case ORDER_BY_COLATITUDE:
                if (colatitude < o.colatitude) return -1;
                if (colatitude > o.colatitude) return 1;
                return 0;
            case ORDER_BY_LONGITUDE:
                if (longitude < o.longitude) return -1;
                if (longitude > o.longitude) return 1;
                return 0;
            }
            return 0;
        }
        
    }

    // codes for ordering the stations
    /** code to leave the stations unordered */
    public final static int ORDER_BY_NONE		= 0;
    /** code to order the stations by station code */
    public final static int ORDER_BY_STATION_CODE	= 1;
    /** code to order the stations by colatitude */
    public final static int ORDER_BY_COLATITUDE         = 2;
    /** code to order the stations by longitude */
    public final static int ORDER_BY_LONGITUDE          = 3;
    
    /** This code is only for internal use - it defines that amount of time
     * after which a loaded database will be reloaded. All the database loading
     * objects hold their data in static objects, so that the database can be
     * accessed many times, but is only loaded once. For most applications
     * this is fine. But long-lived applications will miss any updates to
     * the database files, so the static data is reloaded after the time
     * given in this constant (time is in seconds) */
    private final static int TABLE_RELOAD_TIMEOUT = 600;
    
    // private members - orig_rows are static so we only load them once, but rows
    // (which are copied from orig_order) and order are separate for each instatiation
    private static Vector<StationDetailsFields> orig_rows;    // the rows from the 'table', unordered
    private static Date loadTime;
    private Vector<StationDetailsFields> rows;                // the ordered rows from the 'table'
    private int order;      // the current order of the rows in the 'table'
    
    // static 'constructor'
    static
    {
        orig_rows = null;
        loadTime = null;
    }
    
    /** Creates a new un-ordered instance of StationDetails
     * @throws ConfigFileException if there was an error reading the configuration file */
    public StationDetails () 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) orig_rows = null;
        if (orig_rows == null)
        {
            orig_rows = new Vector<StationDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("station_details"), 
                                             this);
            loadTime = new Date ();
        }
        rows = new Vector<StationDetailsFields> (orig_rows);
        this.order = ORDER_BY_NONE;
    }
    
    /** Creates a new instance of StationDetails
     * @param order initial order for the stations
     * @throws ConfigFileException if there was an error reading the configuration file */
    public StationDetails (int order) 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) orig_rows = null;
        if (orig_rows == null)
        {
            orig_rows = new Vector<StationDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("station_details"), 
                                             this);
            loadTime = new Date ();
        }
        rows = new Vector<StationDetailsFields> (orig_rows);
        this.order = ORDER_BY_NONE;
        reOrder (order);
    }

    /** re-order the stations 
     * @param order the new order (one of the order codes) */
    public void reOrder (int order)
    {
        if ((this.order != order) && (order != ORDER_BY_NONE))
        {
            this.order = order;
            Collections.sort (rows);
        }
    }
    
    /** get the number of rows in the 'table'
     * @return the number of rows */
    public int getNRows () { return rows.size(); }
    
    /** get a row from the 'table'
     * @param index the number of the row (0 .. n_rows-1)
     * @return the fields in the row */
    public StationDetailsFields getRow (int index) { return rows.get (index); }
    
    /** find a row in the 'table' 
     * @param station_code the station to search for 
     * @return the station code OR null */
    public StationDetailsFields findStation (String station_code)
    {
        int count;
        StationDetailsFields fields;
        
        for (count=0; count<rows.size(); count++)
        {
            fields = rows.get (count);
            if (fields.station_code.equalsIgnoreCase (station_code)) return fields;
        }
        return null;
    }

    
    //////////////////////////////////////////////////////////////////////////////////////
    // methods below here implement ConfigFileInterface to load data from the file
    //////////////////////////////////////////////////////////////////////////////////////
    
    /** implementation of ConfigFileInterface: get the number of fields in the 'table'
     * @return The number of fields in each row */
    public int getNFields() { return 11; }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number being set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        StationDetailsFields fields;
        
        // do we need new rows in the table ??
        while (orig_rows.size() <= row) orig_rows.add (new StationDetailsFields ());

        // add the field to the table
        fields = orig_rows.get (row);
        switch (column)
        {
        case 0:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Station code must not be blank", file_line_number);
            fields.station_code = contents;
            break;
        case 1:
            fields.reserved = contents;
            break;
        case 2:
            fields.station_name = contents;
            break;
        case 3:
            try { fields.colatitude = Float.parseFloat(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for colatitude", e, file_line_number); }
            break;
        case 4:
            try { fields.longitude = Float.parseFloat(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for longitude", e, file_line_number); }
            break;
        case 5:
            fields.country = contents;
            break;
        case 6:
            try { fields.rhold_len = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for number of days reported data to hold", e, file_line_number); }
            break;
        case 7:
            try { fields.ahold_len = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for number of days adjusted data to hold", e, file_line_number); }
            break;
        case 8:
            try { fields.dhold_len = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for number of days definitive data to hold", e, file_line_number); }
            break;
        case 9:
            try { fields.ann_mean_h = Float.parseFloat(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for annual mean H", e, file_line_number); }
        case 10:
            fields.test = ConfigFileReader.parseBoolean(contents);
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // static methods below here
    //////////////////////////////////////////////////////////////////////////////////////
    
    /** determine if a table needs to be reloaded 
     * @param date the date/time at which the table was originally loaded
     *        (which may be null)
     * @return true if the table needs to be reloaded */
    public static boolean isTableReloadNeeded (Date date)
    {
        if (date == null) return true;
        if (new Date().getTime() > (date.getTime() + TABLE_RELOAD_TIMEOUT)) return true;
        return false;
    }
    
}
