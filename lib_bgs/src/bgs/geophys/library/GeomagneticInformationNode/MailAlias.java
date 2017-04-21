/*
 * MailAlias.java
 *
 * Created on 04 November 2006, 10:22
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;

/**
 * A class that allows mail names to be translated according to a
 * database 'table'
 *
 * @author  smf
 */
public class MailAlias 
implements ConfigFileInterface
{
    
    /** inner class to hold a single row from the table */
    private class MailAliasFields
    {
        String from;        // the 'from' address to match agaist
        boolean case_ind;   // if true to case independant matching
        String to;          // the translated 'to' address
    }
    
    // private members - rows are static so we only load them once
    private static Vector<MailAliasFields> rows;    // the rows from the 'table'
    private static Date loadTime;
    
    // static 'constructor'
    static
    {
        rows = null;
        loadTime = null;
    }
    
    /** Creates a new instance of MailAlias
     * @throws ConfigFileException if there was an error reading the configuration file */
    public MailAlias() 
    throws ConfigFileException
    {
        if (StationDetails.isTableReloadNeeded(loadTime)) rows = null;
        if (rows == null)
        {
            rows = new Vector<MailAliasFields> ();
            ConfigFileReader.LoadConfigFile (GINUtils.makeDatabaseFilename ("mail_alias"), 
                                             this);
            loadTime = new Date ();
        }
    }
    
    /** translate a 'from' field  from a mail message to an
     * alias from the table - if no translation is available use
     * the original 'from' field
     * @param from the address to translate
     * @return the translated address */
    public String getMailAlias (String from)
    {
        int count;
        boolean found;
        String user, host, alias;
        StringTokenizer tokens;
        MailAliasFields fields;
        
        // split the mail address into user and host
        tokens = new StringTokenizer (from, "@");
        if (tokens.countTokens() <= 1)
        {
            user = from;
            host = "";
        }
        else
        {
            user = tokens.nextToken();
            host = tokens.nextToken();
            while (tokens.hasMoreTokens()) host += "@" + tokens.nextToken();
        
            // search the 'table' for a translation
            for (count=0, found=false; (count<rows.size()) && (! found); count++)
            {
                fields = (MailAliasFields) rows.get (count);
                if (fields.case_ind)
                {
                    if (host.equalsIgnoreCase(fields.from)) found = true;
                }
                else if (host.equals(fields.from)) found = true;
                if (found) host = fields.from;
            }
        }        
        
        // if the alias contains an '@', the original '@' must be changed to a '%'
        if (host.length() <= 0) alias = user;
        else if (host.indexOf('@') >= 0) alias = user + "%" + host;
        else alias = user + '@' + host;
        
        return alias;
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
        MailAliasFields fields;
        
        // do we need new rows in the table ??
        while (rows.size() <= row) rows.add (new MailAliasFields ());

        // add the field to the table
        fields = (MailAliasFields) rows.get (row);
        switch (column)
        {
        case 0:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Mail 'from' address must not be blank", file_line_number);
            fields.from = contents;
            break;
        case 1:
            fields.case_ind = ConfigFileReader.parseBoolean(contents);
            break;
        case 2:
            if (contents.length() <= 0)
                throw new ConfigFileException ("Mail 'to' address must not be blank", file_line_number);
            fields.to = contents;
            break;
        default:
            throw new ConfigFileException ("Too many fields", file_line_number);
        }            
    }
    
}
