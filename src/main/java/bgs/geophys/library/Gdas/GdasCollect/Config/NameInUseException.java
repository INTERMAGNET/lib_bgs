/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

/**
 * An exception that is thrown when a script name already exists
 *
 * @author smf
 */
public class NameInUseException extends Exception
{
    public NameInUseException (String msg) { super (msg); }
}
