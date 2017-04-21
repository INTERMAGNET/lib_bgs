/*
 * ParameterException.java
 *
 * Created on 29 November 2006, 12:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.GeomagneticInformationNode;

/**
 * Handle conditions where the callers parameters are incorrect (and could be fixed)
 * @author smf
 */
public class ParameterException extends Exception 
{
    
    /** Creates a new instance of ParameterException */
    public ParameterException(String msg) {
        super (msg);
    }
    
}
