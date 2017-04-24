/*
 * ImageViewerException.java
 *
 * Created on 14 March 2005, 11:31
 */

package bgs.geophys.library.Swing;

/**
 *
 * @author  smf
 */
public class ImageViewerException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>ImageViewerException</code> without detail message.
     */
    public ImageViewerException() {
    }
    
    
    /**
     * Constructs an instance of <code>ImageViewerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ImageViewerException(String msg) {
        super(msg);
    }
}
