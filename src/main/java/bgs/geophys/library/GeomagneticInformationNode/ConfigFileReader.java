/*
 * ConfigFileReader.java
 *
 * Created on 01 November 2006, 12:54
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Read plain text ASCII configuration files. These are files in rows/columns where
 *   Row are separated by newlines
 *   Columns and separated by spaces -
 *      Unless a string is quoted using single or double quotes
 *   Blank lines and lines starting with '#' are ignored
 *   The caller to LoadConfigFile determines the legal contents of each field and
 *      the number of fields. Each row must have the same number of fields
 *   Strings may contain a number of escape sequences:
 *	\r - carriage return (13)
 *	\n - new-line (10)
 *	\a - alert (bell)
 *	\b - backspace
 *	\f - form-feed
 *	\t - tab
 *	\v - vertical tab
 *	\", /', /` - escaped (literal) quote characters
 *	\ - a backslash
 *	\? - a question mark
 *      \  - a space
 *
 * @author  smf
 */
public class ConfigFileReader 
{

    /** Fills a ConfigFile with the contents of a configuration file
     * @param filename the name of the file to be read 
     * @param contents an object that will be filled with the contents of the file 
     * @throws ConfigFileException if there was an error */
    public static void LoadConfigFile (String filename, ConfigFileInterface contents) 
    throws ConfigFileException
    {
        int row, column;
        String raw_line, trimmed_line, field_string;
        LineNumberReader reader;
        QuotedStringTokenizer tokens;
        
        // attempt to open the file
        try { reader = new LineNumberReader (new FileReader (filename)); }
        catch (FileNotFoundException e) { throw new ConfigFileException ("File not found: " + filename, e); }
        
        // for each line read from the file ...
        row = 0;
        try
        {
            while ((raw_line = reader.readLine()) != null)
            {
                // remove leading and trailing whitespace and check for an empty line
                trimmed_line = raw_line.trim();
                if (trimmed_line.length() <= 0) continue;
            
                // check for a coment line
                if (trimmed_line.charAt(0) == '#') continue;

                // split the data into tokens
                column = 0;
                for (tokens = new QuotedStringTokenizer (trimmed_line, " \t");
                     tokens.hasMoreTokens(); )
                {
                    field_string = tokens.nextToken ();
                
                    // process escape sequences in the field string
                    field_string = field_string.replaceAll ("\\\\a", "\007");
                    field_string = field_string.replaceAll ("\\\\b", "\b");
                    field_string = field_string.replaceAll ("\\\\f", "\f");
                    field_string = field_string.replaceAll ("\\\\n", "\n");
                    field_string = field_string.replaceAll ("\\\\r", "\r");
                    field_string = field_string.replaceAll ("\\\\t", "\t");
                    field_string = field_string.replaceAll ("\\\\v", "\013");
                    field_string = field_string.replaceAll ("\\\\\"", "\"");
                    field_string = field_string.replaceAll ("\\\\\'", "\'");
                    field_string = field_string.replaceAll ("\\\\`", "`");
                    field_string = field_string.replaceAll ("\\\\\\?", "?");
                    field_string = field_string.replaceAll ("\\\\\\\\", "\\");
                    field_string = field_string.replaceAll ("\\\\,", ",");
                    field_string = field_string.replaceAll ("\\\\,", " ");

                    // process this field
                    contents.setField (row, column, field_string, reader.getLineNumber());
                
                    // update column counter
                    column ++;
                }

                // check the number of columns was correct
                if (column != contents.getNFields())
                    throw new ConfigFileException ("Incorrect number of columns", reader.getLineNumber());
            
                // update row counter
                row ++;
            }
        }
        catch (IOException e) { throw new ConfigFileException ("IO error", e, reader.getLineNumber()); }        
    }
    
    /** convert a string to boolean (Boolean.parseBoolean() doesn't work properly 
     * @param string the string to convert
     * @return is string is "1", "t", "true", "y" or "yes" (case independant)
     *         returns true, otherwise returns false */
    public static boolean parseBoolean (String string)
    {
        if (string.equals("1")) return true;
        if (string.equalsIgnoreCase ("t")) return true;
        if (string.equalsIgnoreCase ("true")) return true;
        if (string.equalsIgnoreCase ("y")) return true;
        if (string.equalsIgnoreCase ("yes")) return true;
        return false;
    }
}
