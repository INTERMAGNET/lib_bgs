/*
 * GINDictionary.java
 *
 * Created on 05 November 2006, 09:39
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * Class to hold the dictionary segments for the GIN. These are used to validate
 * user input. The dictionary is split into segments (e.g. country codes is a
 * segment, data type another...). Each row consists of:
 *  a segment name;
 *  a name;
 *  a value.
 * @author  smf
 */
public class GINDictionary 
implements ConfigFileInterface
{
    
    // inner class to hold a single row from the table
    private class DictionaryFields
    implements Comparable<DictionaryFields>
    {
        public String segment;  // the segment of the dictionary
        public String code;     // the code
        public String data;     // the full length string associated with the code
        
        public int compareTo(DictionaryFields o) 
        {
            int ret_val;
            
            ret_val = segment.compareTo (o.segment);
            if (ret_val == 0) ret_val = code.compareTo(o.code);
            return ret_val;
        }
        
    }
    
    // inner class to hold a pointer to each segment
    private class SegmentOffset
    {
        public String segment;          // name of this segment
        public int row_number;          // row at which it starts
        public int length;              // number of entries in the segment
    }
    
    /** search types for find() - case independant search by code, return the assocaited data */
    public static final int SEARCH_CODE_CASE_INDEPENDANT = 1;
    /** search types for find() - case dependant search by code, return the assocaited data */
    public static final int SEARCH_CODE_CASE_DEPENDANT = 2;
    /** search types for find() - case independant search for data, return the assocaited code */
    public static final int SEARCH_DATA_CASE_INDEPENDANT = 3;
    /** search types for find() - case dependant search for data, return the assocaited code */
    public static final int SEARCH_DATA_CASE_DEPENDANT = 4;
    
    /** return type for get() - return the code */
    public static final int GET_CODE = 1;
    /** return type for get() - return the data */
    public static final int GET_DATA = 2;
    
    // private members - rows are static so we only load them once
    private static Vector<DictionaryFields> rows;                // the rows from the 'table'
    private static Date loadTime;
    private static Vector<SegmentOffset> segment_offset_list;    // array of segment names and the offsets of their
                                                                 // first entry in the rows array
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of GINDictionary which will contain all dicitonary segments
     * @throws ConfigFileException if there was an error reading the configuration file */
    public GINDictionary() 
    throws ConfigFileException
    {
        int count;
        String last_segment;
        SegmentOffset offset;
        DictionaryFields fields;
        
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        
        // load the 'table' from the file
        if (rows == null)
        {
            rows = new Vector<DictionaryFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("dictionary"), 
                                             this);
        
            // sort it - the sort must arrange the entries by segment
            Collections.sort (rows);
        
            // work through the 'table' finding the segments
            last_segment = "";
            segment_offset_list = new Vector<SegmentOffset> ();
            offset = null; // just to keep the compiler from moaning!
            for (count=0; count<rows.size(); count++)
            {
                fields = rows.get (count);
                if (! fields.segment.equalsIgnoreCase(last_segment))
                {
                    offset = new SegmentOffset ();
                    offset.segment = last_segment = fields.segment;
                    offset.row_number = count;
                    offset.length = 1;
                    segment_offset_list.add (offset);
                    last_segment = fields.segment;
                }
                else offset.length += 1;
            }
            loadTime = new Date ();
        }
    }
    
    /** find things in the dictionary
     * @param segment the segment to search
     * @param search_type one of the search codes from this object
     * @param search_string the thing to search for
     * @return for search codes SEARCH_CODE... returns the asociated data, for
     *         search codes SEARCH_DATA... returns the associated code, if
     *         code or data cannot be found returns null */
    public String find (String segment, int search_type, String search_string)
    {
        int count;
        SegmentOffset offset;
        DictionaryFields fields;
        
        // find the segment
        offset = find_segment (segment);
        if (offset == null) return null;
        
        // find the item
        for (count=offset.row_number; count<(offset.row_number + offset.length); count ++)
        {
            fields = rows.get (count);
            switch (search_type)
            {
            case SEARCH_CODE_CASE_INDEPENDANT:
                if (fields.code.equalsIgnoreCase(search_string)) return fields.data;
                break;
            case SEARCH_CODE_CASE_DEPENDANT:
                if (fields.code.equals(search_string)) return fields.data;
                break;
            case SEARCH_DATA_CASE_INDEPENDANT:
                if (fields.data.equalsIgnoreCase(search_string)) return fields.code;
                break;
            case SEARCH_DATA_CASE_DEPENDANT:
                if (fields.data.equals(search_string)) return fields.code;
                break;
            }
        }
        
        return null;
    }
    
    /** get things from the dictionary
     * @param segment the segment to search
     * @return the number of rows in the segment */
    public int getNRows (String segment)
    {
        SegmentOffset offset;
        
        // find the segment
        offset = find_segment (segment);
        if (offset == null) return 0;
        return offset.length;
    }
    
    /** get things from the dictionary
     * @param segment the segment to search
     * @param get_type one of the get codes from this object
     * @param row_number the row number from the segment
     * @return for GET_CODE returns the asociated data, for
     *         GET_DATA returns the associated code, if
     *         row_number is out of range or segment does not
     *         exist returns null */
    public String get (String segment, int get_type, int row_number)
    {
        SegmentOffset offset;
        DictionaryFields fields;
        
        // find the segment
        offset = find_segment (segment);
        
        // get the data
        if (row_number >= 0 && row_number < offset.length && offset != null)
        {
            fields = rows.get (offset.row_number + row_number);
            switch (get_type)
            {
            case GET_CODE: return fields.code;
            case GET_DATA: return fields.data;
            }
        }
        return null;
    }

    /** find a segment
     * @param segment the segment to search for
     * @return the offset structure, or null if the segment does not exist */
    private SegmentOffset find_segment (String segment)
    {
        int count;
        SegmentOffset offset;
        
        for (count=0; count<segment_offset_list.size(); count++)
        {
            offset = segment_offset_list.get (count);
            if (offset.segment.equalsIgnoreCase(segment)) return offset;
        }
        return null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // methods below here implement ConfigFileInterface to load data from the file
    //////////////////////////////////////////////////////////////////////////////////////
    
    /** get the number of rows in the 'table' 
     * @return the number of rows */
    public int getNFields() { return 3; }
    
    /** implementation of ConfigFileInterface: fill the object with data from the file
     * @param row the row number beng set
     * @param column the column number being set
     * @param contents the value being set
     * @param file_line_number used in exceptions
     * @throws ConfigFileException if there is an error with the contents */
    public void setField(int row, int column, String contents, int file_line_number) 
    throws ConfigFileException 
    {
        DictionaryFields fields;
        
        // do we need new rows in the table ??
        while (rows.size() <= row) rows.add (new DictionaryFields ());

        // add the field to the table
        fields = rows.get (row);
        switch (column)
        {
        case 0:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Segment code must not be blank", file_line_number);
            fields.segment = contents;
            break;
        case 1:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Code must not be blank", file_line_number);
            fields.code = contents;
            break;
        case 2:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Data must not be blank", file_line_number);
            fields.data = contents;
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }            
    }
    
}
