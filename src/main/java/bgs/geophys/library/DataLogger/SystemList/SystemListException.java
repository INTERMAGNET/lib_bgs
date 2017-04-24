/*
 * SystemListException.java
 *
 * Created on 31 May 2004, 16:35
 */

package bgs.geophys.library.DataLogger.SystemList;

/**
 *
 * @author  Administrator
 */
public class SystemListException extends java.lang.Exception 
{
    
    /**
     * Creates a new instance of <code>SystemListException</code> without detail message.
     */
    public SystemListException() 
    {
    }
    
    
    /**
     * Constructs an instance of <code>SystemListException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SystemListException(String msg) 
    {
        super(msg);
    }
}
