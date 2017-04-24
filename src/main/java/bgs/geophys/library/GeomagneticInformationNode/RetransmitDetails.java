/*
 * RetransmitDetails.java
 *
 * Created on 04 November 2006, 11:26
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * A list of records the describe onward transmission of data.
 *
 * @author  smf
 */
public class RetransmitDetails 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    public class RetransmitDetailsFields
    implements Comparable<RetransmitDetailsFields>
    {
        /** 1.) 3-4 digit IAGA station code */
        String station_code;
        /** 2.) Method code - E for e-mail, F for ftp */
        String method;
        /** 3.) INTERNET host name for the destination */
        String host;
        /** 4.) Remote user name */
        String username;
        /** 5.) Remote password (not needed for e-mail transmission) */
        String password;
        /** 6.) Remote directory (only valid for ftp) */
        String directory;
        /** 7.) Data type code - see dictionary (DATA_TYPE) */
        String data_type;
        /** 8.) Data format code - see dictionary (DATA_FORMAT) */
        String format;
        /** 9.) Sample rate - in samples per day */
        int samps_per_day;
        /** 10.) Date transmissions are to start */
        int valid_from_day, valid_from_month, valid_from_year;
        /** 11.) Date transmissions are to end */
        int valid_to_day, valid_to_month, valid_to_year;
        /** 12.) Hour at which data is to be transmitted OR
          *      -1 for data to be re-transmitted as it is loaded to the database OR
          *      24 to transmit data every hour */
        int transmit_hour;
        /** 13.) Age of data to be sent (0 = today, 1 = yesterday, etc.) */
        int days_ago;
        /** 14.) Flag - if TRUE then use this record, otherwise ignore it */
        boolean in_use;
        
        /** implement compareTo to allow sorting 
         * @param o the object to compare with
         * @return -ve number if this object is less than the given object, +ve number
         *         if this objects is greater than the given object, 0 if the
         *         objects are equal. */
        public int compareTo(RetransmitDetailsFields o) { return station_code.compareTo (o.station_code); }
    }
        
    // private members - rows are static so we only load them once
    private static Vector<RetransmitDetailsFields> rows;    // the rows from the 'table'
    private static Date loadTime;
    
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of RetransmitDetails
     * @throws ConfigFileException if there was an error reading the configuration file */
    public RetransmitDetails() 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        if (rows == null)
        {
            rows = new Vector<RetransmitDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("retransmit"), 
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
    public RetransmitDetailsFields getRow (int index) { return rows.get (index); }
    
    /** find a row in the 'table' 
     * @param station_code the station to search for 
     * @return the station code OR null */
    public RetransmitDetailsFields findStation (String station_code)
    {
        int count;
        RetransmitDetailsFields fields;
        
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
    public int getNFields() { return 13; }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number beng set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions 
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        RetransmitDetailsFields fields;
        StringTokenizer tokens;
        
        // do we need new rows in the table ??
        while (rows.size() <= row) rows.add (new RetransmitDetailsFields ());

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
            if (contents.length() <= 0)
                throw new ConfigFileException ("Method must not be blank", file_line_number);
            fields.method = contents;
            break;
        case 2:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Host must not be blank", file_line_number);
            fields.host = contents;
            break;
        case 3:
            fields.username = contents;
            break;
        case 4:
            fields.password = contents;
            break;
        case 5:
            fields.directory = contents;
            break;
        case 6:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Data type must not be blank", file_line_number);
            fields.data_type = contents;
            break;
        case 7:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Format must not be blank", file_line_number);
            fields.format = contents;
            break;
        case 8:
            fields.samps_per_day = GINUtils.parseSampsPerDay (contents);
            if (fields.samps_per_day <= 0)
                throw new ConfigFileException ("Bad sample rate: " + contents, file_line_number);
            break;
        case 9:
            tokens = new StringTokenizer (contents);
            try
            {
                fields.valid_from_day = Integer.parseInt(tokens.nextToken());
                fields.valid_from_month = Integer.parseInt(tokens.nextToken());
                fields.valid_from_year = Integer.parseInt(tokens.nextToken());
            }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrect number format in from date field", e, file_line_number); }
            catch (NoSuchElementException e) { throw new ConfigFileException ("Missing date field in from date", e, file_line_number); }
            break;
        case 10:
            tokens = new StringTokenizer (contents);
            try
            {
                fields.valid_to_day = Integer.parseInt(tokens.nextToken());
                fields.valid_to_month = Integer.parseInt(tokens.nextToken());
                fields.valid_to_year = Integer.parseInt(tokens.nextToken());
            }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrect number format in to date field", e, file_line_number); }
            catch (NoSuchElementException e) { throw new ConfigFileException ("Missing date field in to date", e, file_line_number); }
            break;
        case 11:
            try { fields.transmit_hour = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrect number format for transmit hour", e, file_line_number); }
            break;
        case 12:
            try { fields.days_ago = Integer.parseInt(contents); }
            catch (NumberFormatException e) { throw new ConfigFileException ("Incorrect number format for days ago", e, file_line_number); }
            break;
        case 13:
            fields.in_use = ConfigFileReader.parseBoolean(contents);
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }            
    }
    
}
