/*
 * GINDataException.java
 *
 * Created on 01 November 2006, 15:17
 */

package bgs.geophys.library.GeomagneticInformationNode;

/**
 * All errors reported by the data handling routines use this exception
 *
 * @author  smf
 */
public class GINDataException extends Exception 
{
    
    // private members
    private Exception original_exception;
        
    /** create a data exception with a message
     * @param msg description of the exception */
    public GINDataException(String msg) 
    {
        super (msg); 
        original_exception = null;
    }
    
    /** create a data exception with a message and an original exception
     * @param msg description of the exception
     * @param original_exception the original exception */
    public GINDataException(String msg, Exception original_exception) 
    {
        super (msg); 
        this.original_exception = original_exception;
    }
    
    /** get the original exception that gave rise to this one 
     @return the excption or null for no exception*/
    public Exception getOriginalException () { return original_exception; }

}
