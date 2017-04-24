/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Exception class that signlas an error has occured while trying to retrieve data
 *
 * @author sani
 */
public class GeomagDataAccessException extends Exception{

    public GeomagDataAccessException() {
    }

    public GeomagDataAccessException(Throwable cause) {
        super(cause);
    }

    public GeomagDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeomagDataAccessException(String message) {
        super(message);
    }
    
}
