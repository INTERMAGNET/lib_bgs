/*
 * CSVInterpreter.java
 *
 * Created on 06 December 2005, 10:27
 */

package bgs.geophys.library.Database;

import java.util.*;

/**
 * A class to interpret a row of CSV data
 *
 * @author  Administrator
 */
public class CSVInterpreter 
{
    
    // a list of the fields that were found
    private Vector<String> fields;
    
    // an index into the field list for retun of the next field
    private int field_index;
    
    /** Creates a new instance of CSVInterpreter */
    public CSVInterpreter (String string) 
    {
        boolean quit, quoted, quit2;
        char character;
        int input_ptr, column_ptr, quote_count, input_length;
        StringBuffer buffer;

        // create the file list
        fields = new Vector<String> ();
        
        // until we run out of string to process...
        input_ptr = column_ptr = 0;
        input_length = string.length();
        buffer = new StringBuffer ();
        for (quit=false; (! quit) && (input_ptr < input_length); )
        {
            // extract the next field
            buffer.setLength (0);
            quote_count = 0;
            if (string.charAt (input_ptr) == '"')
            {
                quoted = true;
                input_ptr ++;
            }
            else quoted = false;
            for (quit2=false; (! quit2) && (input_ptr < input_length); )
            {
                character = string.charAt (input_ptr ++);
                switch (character)
                {
                case '"':
                    quote_count ++;
                    if (quote_count == 2)
                    {
                        buffer.append ('"');
                        quote_count = 0;
                    }
                    break;
                case ',':
                    if (quoted)
                    {
                        if (quote_count > 0) quit2 = true;
                        else buffer.append (',');
                    }
                    else quit2 = true;
                    break;
                default:
                    while (quote_count > 0)
                    {
                        buffer.append ('"');
                        quote_count --;
                    }
                    buffer.append (character);
                    break;
                }
            }
      
            // check that there is something to process
            if (buffer.length() <= 0)
            {
                // add an empty field
                fields.add ("");
            }
            else
            {
                // add this field
                fields.add (new String (buffer));
            }
        }
        
        // set the return index to 0
        field_index = 0;
    }

    /** does this object have more elements to return
     * @returns true if there are more elements */
    public boolean hasMoreElements ()
    {
        if (field_index < fields.size()) return true;
        return false;
    }
    
    /** how many elements are there left to return
     * @return the number of elements left */
    public int countElements ()
    {
        return fields.size() - field_index;
    }

    /** get the next element
     * @return the next string field */
    public String nextElement ()
    throws NoSuchElementException
    {
        if (! hasMoreElements()) throw new NoSuchElementException ();
        return (String) fields.get (field_index ++);
    }
    
}
