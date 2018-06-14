/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing.Wizard;

/**
 * A holding class for listener interfaces used by the wizard.
 * 
 * @author smf
 */
public class WizardListeners
{

    /** An interface that provides methods that are called when a wizard
     * changes state from one panel to another */
    public interface WizardPanelChangeListener 
    {
        /** Called just before the panel is to be displayed.
         * @param evt a description of the window change event
         * @param btn_evt the button event that caused the window change (null if there wasn't a button event) */
        public void aboutToDisplayPanel (WizardEvents.WizardPanelChangeEvent evt, 
                                         WizardEvents.WizardButtonEvent btn_evt);
        
        /** Called when the panel itself is displayed.
         * @param evt a description of the window change event
         * @param btn_evt the button event that caused the window change (null if there wasn't a button event) */
        public void displayingPanel (WizardEvents.WizardPanelChangeEvent evt, 
                                     WizardEvents.WizardButtonEvent btn_evt);
 
        /** Called just before the panel is to be hidden. This method provides
         * and opportunity to prevent a panel to panel transition
         * @param evt a description of the window change event
         * @param btn_evt the button event that caused the window change (null if there wasn't a button event)
         * @return true to allow the transition operation to continue, false to prevent it */
        public boolean aboutToHidePanel (WizardEvents.WizardPanelChangeEvent evt, 
                                         WizardEvents.WizardButtonEvent btn_evt);
    }

    /** an interface that forward ActionEvents from any of the
     * wizards buttons - the code for the button is in the event message */
    public interface WizardButtonListener
    {
        /** called when a button is clicked */
        public void buttonClicked (WizardEvents.WizardButtonEvent evt);
    }
    
}

