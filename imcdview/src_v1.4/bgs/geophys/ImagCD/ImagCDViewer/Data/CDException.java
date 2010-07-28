/*
 * IMCDException.java
 *
 * Created on 27 April 2002, 09:05
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

/**
 * Pass messages about problems processing IMAG CD data
 * @author  smf
 * @version 
 */
public class CDException extends Exception {

    /**
     * Creates new <code>IMCDException</code> without detail message.
     */
    public CDException() {
    }


    /**
     * Constructs an <code>IMCDException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CDException(String msg) {
        super(msg);
    }
}

