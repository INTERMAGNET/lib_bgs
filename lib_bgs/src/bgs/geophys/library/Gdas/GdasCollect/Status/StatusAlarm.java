/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Status;

/**
 * An interface that status objects must implement to offer
 * alarm services
 * 
 * @author smf
 */
public interface StatusAlarm 
{
    public boolean isAlarmed ();       // return true if the status object is in an alarm state
    public String displayText ();   // return the text to be displayed for this object
}
