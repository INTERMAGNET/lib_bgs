/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Exception class that signlas an I/O error has occured while trying to read 
 * Geomag data from a  data source
 *
 * @author sani
 */
public class GeomagDataIOException extends GeomagDataAccessException{

    public GeomagDataIOException() {
    }

    public GeomagDataIOException(String message) {
        super(message);
    }

    public GeomagDataIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeomagDataIOException(Throwable cause) {
        super(cause);
    }

}
