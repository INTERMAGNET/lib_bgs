/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.signal;

import java.util.Observable;
import java.util.Vector;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * A class to help with signal handling. To use the class, register as an
 * observer, wait to be called, something like this:
 *
 *  try
 *  {
 *    UserSignalHandler sig_hand;
 *    sig_hand = new UserSignalHandler ();
 *    sig_hand.addObserver(this);
 *    sig_hand.handleSignal("INT");
 *   }
 *   catch (IllegalArgumentException e)
 *   {
 *     System.err.println (e.getMsg ());
 *   }
 * @author smf
 */
public class UserSignalHandler extends Observable
implements SignalHandler

{
    private class NamedHandler
    {
        public Signal signal;
        public SignalHandler handler;
        public NamedHandler (Signal signal, SignalHandler handler)
        {
            this.signal = signal;
            this.handler = handler;
        }
    }
    
    private SignalFinder signal_finder;
    private Vector<NamedHandler> old_handlers;
    
    public UserSignalHandler ()
    {
        super ();
        signal_finder = new SignalFinder ();
        old_handlers = new Vector<NamedHandler> ();
    }
    
    public void finalize ()
    {
        NamedHandler handler;
        
        while (old_handlers.size() > 0)
        {
            handler = old_handlers.remove(old_handlers.size() -1);
            Signal.handle (handler.signal, handler.handler);
        }
    }
            
    public void handleSignal (String name)
    throws IllegalArgumentException
    {
        Signal signal;
        SignalHandler old_handler;
        
        if (! signal_finder.findSignal(name)) throw new IllegalArgumentException ("Unable to catch signal " + name);
        signal = new Signal (name);
        old_handler = Signal.handle (signal, this);
        if (old_handler != SignalHandler.SIG_DFL && old_handler != SignalHandler.SIG_IGN)
            old_handlers.add (new NamedHandler (signal, old_handler));
    }

    public void handle (Signal signal) 
    {
        this.notifyObservers (signal.getName());
    }
    
}
