/*
 * DataViewerException.java
 *
 * Created on 18 October 2004, 10:18
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

/**
 *
 * @author  smf
 */
public class DataViewerException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>DataViewerException</code> without detail message.
     */
    public DataViewerException() {
    }
    
    
    /**
     * Constructs an instance of <code>DataViewerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DataViewerException(String msg) {
        super(msg);
    }
}
