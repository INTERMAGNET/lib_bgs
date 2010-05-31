/*
 * TextViewerException.java
 *
 * Created on 23 March 2005, 10:40
 */

package bgs.geophys.library.Swing;

/**
 *
 * @author  smf
 */
public class TextViewerException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>TextViewerException</code> without detail message.
     */
    public TextViewerException() {
    }
    
    
    /**
     * Constructs an instance of <code>TextViewerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TextViewerException(String msg) {
        super(msg);
    }
}
