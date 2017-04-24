/*
 * DataException.java
 *
 * Created on 25 October 2003, 09:23
 */

package bgs.geophys.library.Data.ImagCD;

/**
 *
 * @author  Administrator
 */
public class ImagCDDataException extends java.lang.Exception 
{
    
    /**
     * Creates a new instance of <code>DataException</code> without detail message.
     */
    public ImagCDDataException() 
    {
    }
    
    
    /**
     * Constructs an instance of <code>DataException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ImagCDDataException(String msg) 
    {
        super(msg);
    }
}
