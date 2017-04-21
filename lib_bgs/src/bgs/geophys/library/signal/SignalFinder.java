/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.signal;

import java.util.StringTokenizer;
import java.util.Vector;
import sun.misc.Signal;

/**
 * A class to enumerate and find signals
 * 
 * @author smf
 */
public class SignalFinder 
{
    private static final String test_list = "HUP INT QUIT ILL TRAP ABRT EMT FPE " +
            "KILL BUS SEGV SYS PIPE ALRM TERM USR1 USR2 CHLD PWR WINCH URG POLL " +
            "STOP TSTP CONT TTIN TTOU VTALRM PROF XCPU XFSZ WAITING LWP AIO IO " +
            "INFO THR BREAK FREEZE THAW CANCEL EMT";

    private Vector<String> signal_list;
    
    public SignalFinder ()
    {
        String signal_name;
        Signal signal;
        StringTokenizer tokens;

        // create a list of valid signals
        tokens = new StringTokenizer (test_list, " ");
        signal_list = new Vector<String> ();
        while (tokens.hasMoreTokens())
        {
            try
            {
                signal_name = tokens.nextToken();
                signal = new Signal (signal_name);
                signal_list.add (signal_name);
            }
            catch (Throwable t) { /* catches IllegalArgument and ClassNotFound */ }
        }
    }
    
    public int getNSignals () { return signal_list.size(); }
    public String getSignal (int index) { return signal_list.get (index); }
    
    public boolean findSignal (String name)
    {
        int count;
        
        for (count=0; count<getNSignals (); count++)
        {
            if (getSignal(count).equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
