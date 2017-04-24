/*
 * ConfigFileException.java
 *
 * Created on 01 November 2006, 15:17
 */

package bgs.geophys.library.GeomagneticInformationNode;

/**
 * All errors reported by the config file routines use this exception
 *
 * @author  smf
 */
public class ConfigFileException extends Exception 
{
    
    // private members
    private Exception original_exception;
    private int line_number;
        
    /** create a configuration file exception with a message
     * @param msg description of the exception */
    public ConfigFileException (String msg) 
    {
        super (msg); 
        original_exception = null;
        line_number = -1;
    }
    
    /** create a configuration file exception with a message and an original exception
     * @param msg description of the exception
     * @param original_exception the original exception */
    public ConfigFileException (String msg, Exception original_exception) 
    {
        super (msg); 
        this.original_exception = original_exception;
        this.line_number = -1;
    }
    
    /** create a configuration file exception with a message and a line number
     * @param msg description of the exception
     * @param line_number the number of the line being processed in the config file */
    public ConfigFileException (String msg, int line_number) 
    {
        super (msg); 
        this.original_exception = null;
        this.line_number = line_number;
    }
    
    /** create a configuration file exception with a message, an original exception
     * and a line number
     * @param msg description of the exception
     * @param original_exception the original exception
     * @param file_line_number the number of the line being processed in the config file
     */
    public ConfigFileException (String msg, Exception original_exception, int file_line_number) 
    {
        super (msg); 
        this.original_exception = original_exception;
        this.line_number = line_number;
    }
    
    /** get the line number associated with this exception (may be -ve for no line number) 
     * @return the line number */
    public int getLineNumber () { return line_number; }
    
    /** get the original exception that gave rise to this one 
     @return the excption or null for no exception*/
    public Exception getOriginalException () { return original_exception; }

}
