package bgs.geophys.library.Swing.Wizard;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;


/**
 * A descriptor class used to reference a Component panel for the Wizard, as
 * well as provide general rules as to how the panel should behave.
 */
public class WizardPanelDescriptor 
{
    static class FinishIdentifier {
        public static final String ID = "FINISH";
    }
    
    /**
     * Identifier returned by getNextPanelDescriptor() to indicate that this is the
     * last panel and the text of the 'Next' button should change to 'Finish'.
     */    
    public static final FinishIdentifier FINISH = new FinishIdentifier();
    
    private Wizard wizard;
    private Component targetPanel;
    private Object backIdentifier;
    private Object panelIdentifier;
    private Object nextIdentifier;
    private Vector<WizardListeners.WizardPanelChangeListener> panelChangeListeners;
    
    /**
     * Constructor to create a panel descriptor which knows its own
     * id and panel, as well as the ids for previous and next panels
     * @param previous_id ID for the previous panel
     * @param id Object-based identifier
     * @param next_id ID for the previous panel
     * @param panel A class which extends java.awt.Component that will be inserted as a
     * panel into the wizard dialog.
     */    
    public WizardPanelDescriptor(Object previous_id, Object id, Object next_id, Component panel) {
        backIdentifier = previous_id;
        panelIdentifier = id;
        nextIdentifier = next_id;
        targetPanel = panel;
        panelChangeListeners = new Vector<WizardListeners.WizardPanelChangeListener> ();
    }
    
    public void addPanelChangeListener (WizardListeners.WizardPanelChangeListener listener) {
        panelChangeListeners.add (listener);
    }
    
    public void removePanelChangeListener (WizardListeners.WizardPanelChangeListener listener) {
        panelChangeListeners.remove(listener);
    }
   
    /**
     * Returns to java.awt.Component that serves as the actual panel.
     * @return A reference to the java.awt.Component that serves as the panel
     */    
    public final Component getPanelComponent() {
        return targetPanel;
    }
    
    /**
     * Sets the panel's component as a class that extends java.awt.Component
     * @param panel java.awt.Component which serves as the wizard panel
     */    
    public final void setPanelComponent(Component panel) {
        targetPanel = panel;
    }
    
    /**
     * Returns the unique Object-based identifier for this panel descriptor.
     * @return The Object-based identifier
     */    
    public final Object getPanelDescriptorIdentifier() {
        return panelIdentifier;
    }

    /**
     * Sets the Object-based identifier for this panel. The identifier must be unique
     * from all the other identifiers in the panel.
     * @param id Object-based identifier for this panel.
     */    
    public final void setPanelDescriptorIdentifier(Object id) {
        panelIdentifier = id;
    }
    
    final void setWizard(Wizard w) {
        wizard = w;
    }
    
    /**
     * Returns a reference to the Wizard component.
     * @return The Wizard class hosting this descriptor.
     */    
    public final Wizard getWizard() {
        return wizard;
    }   

    /**
     * Returns a reference to the current WizardModel for this Wizard component.
     * @return The current WizardModel for this Wizard component.
     */    
    public WizardModel getWizardModel() {
        return wizard.getModel();
    }
    
    /**
     * Get the Object-based identifier of the panel that the
     * user should traverse to when the Next button is pressed. Note that this method
     * is only called when the button is actually pressed, so that the panel can change
     * the next panel's identifier dynamically at runtime if necessary. Return null if
     * the button should be disabled. Return FinishIdentfier if the button text
     * should change to 'Finish' and the dialog should end.
     * @return Object-based identifier.
     */    
    public Object getNextPanelDescriptor() {
        return nextIdentifier;
    }
    
    /** set the identifier of the panel that will be displayed when the
     * Next button is pressed. Note that this can be called at any time before
     * next is pressed
     * @param id the new identifier
     */
    public void setNextPanelDescriptor (Object id) {
        nextIdentifier = id;
    }

    /**
     * Get the Object-based identifier of the panel that the
     * user should traverse to when the Back button is pressed. Note that this method
     * is only called when the button is actually pressed, so that the panel can change
     * the previous panel's identifier dynamically at runtime if necessary. Return null if
     * the button should be disabled.
     * @return Object-based identifier
     */    
    public Object getBackPanelDescriptor() {
        return backIdentifier;
    }
    
    /** set the identifier of the panel that will be displayed when the
     * Back button is pressed. Note that this can be called at any time before
     * back is pressed
     * @param id the new identifier
     */
    public void setBackPanelDescriptor (Object id) {
        backIdentifier = id;
    }

    
    
    /**
     * Call listeners just before the panel is to be displayed.
     */    
    public void aboutToDisplayPanel(WizardEvents.WizardButtonEvent btn_evt) {
        Iterator<WizardListeners.WizardPanelChangeListener> i;
        WizardEvents events;
        WizardEvents.WizardPanelChangeEvent evt;
        
        events = new WizardEvents ();
        evt = events.makeWizardPanelChangeEvent(panelIdentifier);
        for (i=panelChangeListeners.iterator(); i.hasNext(); )
            i.next().aboutToDisplayPanel(evt, btn_evt);
    }
 
    /**
     * Call listeners when the panel itself is displayed.
     */    
    public void displayingPanel(WizardEvents.WizardButtonEvent btn_evt) {
        Iterator<WizardListeners.WizardPanelChangeListener> i;
        WizardEvents events;
        WizardEvents.WizardPanelChangeEvent evt;
        
        events = new WizardEvents ();
        evt = events.makeWizardPanelChangeEvent(panelIdentifier);
        for (i=panelChangeListeners.iterator(); i.hasNext(); )
            i.next().displayingPanel(evt, btn_evt);
    }
 
    /**
     * Call listeners just before the panel is to be hidden.
     * @return true if the panel can be hidden (and the next panel displayed)
     *         false to halt the panel change operations
     */    
    public boolean aboutToHidePanel(WizardEvents.WizardButtonEvent btn_evt) {
        Iterator<WizardListeners.WizardPanelChangeListener> i;
        WizardEvents events;
        WizardEvents.WizardPanelChangeEvent evt;
        boolean ret_val;
        
        events = new WizardEvents ();
        evt = events.makeWizardPanelChangeEvent(panelIdentifier);
        ret_val = true;
        for (i=panelChangeListeners.iterator(); i.hasNext(); )
        {
            if (! i.next().aboutToHidePanel(evt, btn_evt)) ret_val = false;
        }
        
        return ret_val;
    }    
    
}
