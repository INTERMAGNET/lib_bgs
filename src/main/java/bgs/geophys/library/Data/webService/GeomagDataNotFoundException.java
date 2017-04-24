/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Exception class that signals the Geomag data was not found in the data source
 *
 * @author sani
 */
public class GeomagDataNotFoundException extends GeomagDataAccessException{

    public GeomagDataNotFoundException() {
    }

    public GeomagDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeomagDataNotFoundException(Throwable cause) {
        super(cause);
    }

    public GeomagDataNotFoundException(String message) {
        super(message);
    }

}
