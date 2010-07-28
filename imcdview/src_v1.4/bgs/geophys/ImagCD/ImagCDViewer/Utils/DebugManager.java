/*
 * DebugManager.java
 *
 * Created on 27 October 2004, 16:35
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import java.util.*;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;

/**
 * Manage debugging operations. Allow messages to be recorded (and stored)
 * before the debug dialog is displayed.
 *
 * @author  smf
 */
public class DebugManager
{
     
    private boolean debug;
    private DebugDialog debug_dialog;
    Vector<String> module_name_store, description_store;

     /** Creates a new instance of DebugManager
      * @param debug set to true if debugging is enabled, false otherwise*/
     public DebugManager (boolean debug)
     {
         debug_dialog = null;
         this.debug = debug;
         module_name_store = new Vector<String> ();
         description_store = new Vector<String> ();
     }

    /** show the debug dialog (if debugging is enabled)
     * @param parent the parent frame (or null) */
    public void showDebugDialog (java.awt.Frame parent)
    {
        int count;
        
        if (debug)
        {
            if (debug_dialog == null)
            {
                debug_dialog = new DebugDialog (parent, false);
                for (count=0; count<module_name_store.size(); count++)
                    debug_dialog.logEvent((String) module_name_store.get (count),
                                          (String) description_store.get (count));
                module_name_store.removeAllElements();
                description_store.removeAllElements();
            }
            debug_dialog.setVisible (true);
        }
    }
     
    /** log a message to the debug window
     * @param module_name the name of the module logging the message
     * @param description the message */
    public void logDebugMessage (String module_name, String description) 
    {
        if (debug)
        {
            if (debug_dialog == null)
            {
                module_name_store.add (module_name);
                description_store.add (description);
            }
            else debug_dialog.logEvent(module_name, description);
        }
    }    
     
}
