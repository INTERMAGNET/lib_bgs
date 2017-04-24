/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing.Wizard;

import java.awt.event.ActionEvent;

/**
 * A holding class for event classes used by the wizard.
 * 
 * @author smf
 */
public class WizardEvents
{

    /////////////////////////////////////////////////////////////////////
    // panel change events
    /////////////////////////////////////////////////////////////////////
    
    /** to create a WizardPanelChangeEvent, instantiate the events
     * class, then call this method
     * @param id the id for the panel concerned
     * @return the event
     */
    public WizardPanelChangeEvent makeWizardPanelChangeEvent (Object id)
    {
        return new WizardPanelChangeEvent (id);
    }
    
    /** A class that describes the change between panels in a wizard
     * 'Next' or 'Back' operations */
    public class WizardPanelChangeEvent 
    {
        private Object id;
    
        public WizardPanelChangeEvent (Object id) { this.id = id; }
        public Object getPanelID () { return id; }
    }


    ///////////////////////////////////////////////////////////////////////
    // wizard button events
    ///////////////////////////////////////////////////////////////////////
    
    public static final int BUTTON_BACK = 1;
    public static final int BUTTON_NEXT = 2;
    public static final int BUTTON_CANCEL = 3;
        
    /** to create a WizardButtonEvent, instantiate the events
     * class, then call this method
     * @param button_code the code for the button concerned
     * @param evt the original action event from the button
     * @return the event
     */
    public WizardButtonEvent makeWizardButtonEvent (int button_code, ActionEvent evt)
    {
        return new WizardButtonEvent (button_code, evt);
    }
    
    /** A class that describes forwarded button events from the
     * 'Next', 'Back' or 'Cancel' button */
    public class WizardButtonEvent 
    {
        private int button_code;
        private ActionEvent evt;
    
        public WizardButtonEvent (int button_code, ActionEvent evt) { this.button_code = button_code; this.evt = evt; }
        public int getButtonCode () { return button_code; }
        public ActionEvent getRootEvent () { return evt; }
    }
    
}
