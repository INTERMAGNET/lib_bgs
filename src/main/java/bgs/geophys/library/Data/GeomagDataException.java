/*
 * GeomagDataException.java
 *
 * Created on 29 January 2007, 10:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import java.io.IOException;

/** an exception that is thrown when there are errors with the data */
public class GeomagDataException extends IOException
{
    public GeomagDataException (String msg) { super (msg); }
}
