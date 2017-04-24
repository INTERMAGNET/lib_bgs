/*
 * ContactDetails.java
 *
 * Created on 04 November 2006, 10:05
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * Contact details for each observatory connected to the GIN
 * @author  smf
 */
public class ContactDetails 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    public class ContactDetailsFields
    implements Comparable<ContactDetailsFields>
    {
        /** 1.) 3-4 digit IAGA station code */
        String station_code;
        /** 2.) Name of contact */
        String name;
        /** 3.) Telephone number for contact */
        String telephone;
        //** 4.) Fax number of contact */
        String fax;
        /** 5.) Mail address for the contact */
        String mail_id;
        
        /** implement compareTo to allow sorting 
         * @param o the object to compare with
         * @return -ve number if this object is less than the given object, +ve number
         *         if this objects is greater than the given object, 0 if the
         *         objects are equal. */
        public int compareTo(ContactDetailsFields o) { return station_code.compareTo (o.station_code); }
    }
    
    // private members - rows are static so we only load them once
    private static Vector<ContactDetailsFields> rows;    // the rows from the 'table'
    private static Date loadTime;
    
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of ContactDetails
     * @throws ConfigFileException if there was an error reading the configuration file */
    public ContactDetails() 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        if (rows == null)
        {
            rows = new Vector<ContactDetailsFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("contact_details"), 
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
    public ContactDetailsFields getRow (int index) { return rows.get (index); }
    
    /** find a row in the 'table' 
     * @param station_code the station to search for 
     * @return the contact details OR null */
    public ContactDetailsFields findStation (String station_code)
    {
        int count;
        ContactDetailsFields fields;
        
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
    public int getNFields() { return 5; }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number beng set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions 
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        ContactDetailsFields fields;
        
        // do we need new rows in the table ??
        while (rows.size() <= row) rows.add (new ContactDetailsFields ());

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
            fields.name = contents;
            break;
        case 2:
            fields.fax = contents;
            break;
        case 3:
            fields.telephone = contents;
            break;
        case 4:
            fields.mail_id = contents;
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }
    }
    
}
