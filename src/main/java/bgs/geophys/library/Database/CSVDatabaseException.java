/*
 * DatabaseException.java
 *
 * Created on 09 May 2002, 17:03
 */

package bgs.geophys.library.Database;

/**
 *
 * @author  smf
 * @version 
 */
public class CSVDatabaseException extends Exception {

    /**
     * Creates new <code>DatabaseException</code> without detail message.
     */
    public CSVDatabaseException() {
    }


    /**
     * Constructs an <code>DatabaseException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CSVDatabaseException(String msg) {
        super(msg);
    }
}

