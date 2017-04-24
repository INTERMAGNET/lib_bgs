/*
 * AppDetails.java
 *
 * Created on 01 November 2006, 23:16
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * class that holds the application details 'table'
 *
 * @author  smf
 */
public class AppDetails 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    public class AppDetailsFields
    implements Comparable<AppDetailsFields>
    {
        /** 1.) 3-4 digit IAGA station code */
        public String station_code;
        /** 2.) Flag - 1 = allow automatic data entry, 0 = disallow */
        public boolean enter_data;
        /** 3.) Flag - 1 = include in automatic stack plots, 0 = don't include */
        public boolean stack_plot;
        /** 4.) Flag - 1 = archive data before deleting, 0 = dont archive */
        public boolean archive;
        /** 5.) Flag - 1 = delete data on expirey, 0 = don't delete */
        public boolean delete;
        /** 6.) Flag - 1 = make data available for WWW, 0 = hide from www */
        public boolean www;
        /** 7.) Handshake - number of days to wait before sending a report
         *      on the available data (0 = today, 1 = yesterday, etc.)
         *      To turn off the handshake application enter -1 */
        public int handshake;
        /** 8, 9, 10, 11.) List of arithmetic operators and constants to apply to
        /*                 4 components when data is entered */
        public String comp_mod [];
        
        /** implement compareTo to allow sorting 
         * @param o the object to compare with
         * @return -ve number if this object is less than the given object, +ve number
         *         if this objects is greater than the given object, 0 if the
         *         objects are equal. */
        public int compareTo(AppDetailsFields o) { return station_code.compareTo (o.station_code); }
    }
    
    // private members - rows are static so we only load them once
    private static Vector<AppDetailsFields> rows;    // the rows from the 'table'
    private static Date loadTime;
    
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of AppDetails
     * @throws ConfigFileException if there was an error reading the configuration file */
    public AppDetails() 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        if (rows == null)
        {
            rows = new Vector<AppDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("application_details"), 
                                             this);
            Collections.sort (rows);
            loadTime = new Date ();
        }
    }
    
    /** get the number of rows in the 'table' 
     * @return the number of rows */
    public int getNRows () { return rows.size(); }
    
    /** get a row from the 'table'
     * @param index The row number to return (0..n_rows-1)
     * @return The details for this row
     */
    public AppDetailsFields getRow (int index) { return rows.get (index); }
    
    /** find a row in the 'table' 
     * @param station_code the station to search for 
     * @return the station code OR null */
    public AppDetailsFields findStation (String station_code)
    {
        int count;
        AppDetailsFields fields;
        
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
    public int getNFields() { return 11;  }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number beng set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions 
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        AppDetailsFields fields;
        
        // do we need new rows in the table ??
        while (rows.size() <= row)
        {
            fields = new AppDetailsFields ();
            fields.comp_mod = new String [4];
            rows.add (fields);
        }

        // add the field to the table
        fields = rows.get (row);
        switch (column)
        {
        case 0:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Station code must not be blank", file_line_number);
            fields.station_code = contents;
            break;
        case 1:
            fields.enter_data = ConfigFileReader.parseBoolean(contents);
            break;
        case 2:
            fields.stack_plot = ConfigFileReader.parseBoolean(contents);
            break;
        case 3:
            fields.archive = ConfigFileReader.parseBoolean(contents);
            break;
        case 4:
            fields.delete = ConfigFileReader.parseBoolean(contents);
            break;
        case 5:
            fields.www = ConfigFileReader.parseBoolean(contents);
            break;
        case 6:
            try { fields.handshake = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrect number format for handshake", e, file_line_number); }
            break;
        case 7:
        case 8:
        case 9:
        case 10:
            fields.comp_mod [column -7] = contents;
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }            
    }
    
}
