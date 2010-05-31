/*
 * SatDetails.java
 *
 * Created on 04 November 2006, 10:59
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * Class to hold descriptions of Meteosat satallite channels
 *
 * @author  smf
 */
public class SatDetails 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    public class SatDetailsFields
    implements Comparable<SatDetailsFields>
    {
        /** 1.) 3-4 digit IAGA station code */
        String station_code;
        /** 2.) Hexadecimal channel identifier */
        int channel_id;
        /** 3.) Number of characters to strip from the front of a METEOSAT message */
        int front_strip;
        /** 4.) Number of characters to strip from the back of a METEOSAT message
          *     Length of message = front_strip + (126 * 5) + back_strip */
        int rear_strip;
        /** 5.) Flag - if TRUE swap all bytes in the message */
        boolean swap_bytes;
        /** 6.) Date/time and Lat/Long nibble ordering in 3 byte fields */
        int swap_dates [];
        /** 7.) Flag - if TRUE swap the field value data bytes (not the header) */
        boolean swap_fvals;
        /** 8.) Flag - if TRUE treat numeric data as signed (otherwise unsigned) */
        boolean sign;
        /** 9.) Minute on which the data should be stamped */
        int stamp_min;
        /** 10.) Second on which the data should be stamped */
        int stamp_sec;
        
        /** implement compareTo to allow sorting 
         * @param o the object to compare with
         * @return -ve number if this object is less than the given object, +ve number
         *         if this objects is greater than the given object, 0 if the
         *         objects are equal. */
        public int compareTo(SatDetailsFields o) { return station_code.compareTo (o.station_code); }
    }
    
    // private members - rows are static so we only load them once
    private static Vector<SatDetailsFields> rows;    // the rows from the 'table'
    private static Date loadTime;
    
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of SatDetails
     * @throws ConfigFileException if there was an error reading the configuration file */
    public SatDetails() 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        if (rows == null)
        {
            rows = new Vector<SatDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("satelliet_details"), 
                                             this);
            Collections.sort (rows);
            loadTime = new Date ();
        }
    }
    
    /** get the number of rows in the 'table' 
     * @return the number of rows */
    public int getNRows () { return rows.size(); }
    
    /** get a row from the 'table'
     * @param index the number of the row (0 .. n_rows-1)
     * @return the fields in the row */
    public SatDetailsFields getRow (int index) { return (SatDetailsFields) rows.get (index); }
    
    /** find a row in the 'table' 
     * @param station_code the station to search for 
     * @return the station code OR null */
    public SatDetailsFields findStation (String station_code)
    {
        int count;
        SatDetailsFields fields;
        
        for (count=0; count<rows.size(); count++)
        {
            fields = (SatDetailsFields) rows.get (count);
            if (fields.station_code.equalsIgnoreCase (station_code)) return fields;
        }
        return null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // methods below here implement ConfigFileInterface to load data from the file
    //////////////////////////////////////////////////////////////////////////////////////
    
    /** implementation of ConfigFileInterface: get the number of fields in the 'table'
     * @return The number of fields in each row */
    public int getNFields() { return 10; }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number beng set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions 
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        int number;
        SatDetailsFields fields;
        
        // do we need new rows in the table ??
        while (rows.size() <= row) rows.add (new SatDetailsFields ());

        // add the field to the table
        fields = (SatDetailsFields) rows.get (row);
        switch (column)
        {
        case 0:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Station code must not be blank", file_line_number);
            fields.station_code = contents;
            break;
        case 1:
            try { fields.channel_id = Integer.parseInt(contents, 16); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for channel id", e, file_line_number); }
            break;
        case 2:
            try { fields.front_strip = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for 'front strip'", e, file_line_number); }
            break;
        case 3:
            try { fields.rear_strip = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for 'rear strip'", e, file_line_number); }
            break;
        case 4:
            fields.swap_bytes = ConfigFileReader.parseBoolean(contents);
            break;
        case 5:
            if (contents.length() != 6)
                throw new ConfigFileException ("Date/time nibble re-ordering must contain 6 digits", file_line_number);
            fields.swap_dates = new int [6];
            try { number = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for date/time re-ordering", e, file_line_number); }
            fields.swap_dates [0] = (number / 100000) %10;
            fields.swap_dates [1] = (number / 10000) %10;
            fields.swap_dates [2] = (number / 1000) %10;
            fields.swap_dates [3] = (number / 100) %10;
            fields.swap_dates [4] = (number / 10) %10;
            fields.swap_dates [5] = number % 10;
            break;
        case 6:
            fields.swap_fvals = ConfigFileReader.parseBoolean(contents);
            break;
        case 7:
            fields.sign = ConfigFileReader.parseBoolean(contents);
            break;
        case 8:
            try { fields.stamp_min = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for 'stamp minute'", e, file_line_number); }
            break;
        case 9:
            try { fields.stamp_sec = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrent number format for 'stamp second'", e, file_line_number); }
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }            
    }
    
}
