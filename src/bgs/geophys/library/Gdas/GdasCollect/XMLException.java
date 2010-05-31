/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect;

import com.thoughtworks.xstream.core.BaseException;

/**
 *
 * @author smf
 */
public class XMLException extends Exception
{

    BaseException cause;
    
    public XMLException (BaseException cause)
    {
        this.cause = cause;
    }
    
    @Override
    public String getMessage ()
    {
        String msg;
        Throwable throwable;
        
        // try to find the message from the lowest level exception
        msg = cause.getMessage();
        throwable = cause.getCause();
        while (throwable != null)
        {
            if (throwable.getMessage() != null) msg = throwable.getMessage();
            throwable = throwable.getCause();
        }

        if (msg == null) msg = "Unknown cause";
        return msg;
    }
}
