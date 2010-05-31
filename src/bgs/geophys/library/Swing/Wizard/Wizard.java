package bgs.geophys.library.Swing.Wizard;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Original code from: http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 * 
 * This class implements a basic wizard dialog, where the programmer can
 * insert one or more Components to act as panels. These panels can be navigated
 * through arbitrarily using the 'Next' or 'Back' buttons, or the dialog itself
 * can be closed using the 'Cancel' button. Note that even though the dialog
 * uses a CardLayout manager, the order of the panels is not linear. Each panel
 * determines at runtime what its next and previous panel will be.
 */
public class Wizard extends WindowAdapter implements PropertyChangeListener {

    /** Indicates that the 'Finish' button was pressed to close the dialog. */
    public static final int FINISH_RETURN_CODE = 0;
    /** Indicates that the 'Cancel' button was pressed to close the dialog, or
      * the user pressed the close box in the corner of the window. */
    public static final int CANCEL_RETURN_CODE = 1;
    /** Indicates that the dialog closed due to an internal error. */    
    public static final int ERROR_RETURN_CODE = 2;
        
    /** The String-based action command for the 'Next' button. */
    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    /** The String-based action command for the 'Back' button. */
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    /** The String-based action command for the 'Cancel' button.*/
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";
        
    private WizardModel wizardModel;
    private WizardController wizardController;
    private JDialog wizardDialog;
        
    private JPanel cardPanel;
    private CardLayout cardLayout;            
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;
    
    private int returnCode;
    
    private Object default_back_label;
    private Object default_next_label;
    private Object default_finish_label;
    private Object default_cancel_label;
    
    private Icon default_back_icon;
    private Icon default_next_icon;
    private Icon default_finish_icon;
    private Icon default_cancel_icon;
    
    
    /**
     * Default constructor. This method creates a new WizardModel object and passes it
     * into the overloaded constructor.
     */    
    public Wizard() 
    {
        this((Frame)null);
    }
    
    /**
     * This method accepts a java.awt.Dialog object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Dialog object that is the owner of this dialog.
     */    
    public Wizard(Dialog owner) 
    {
        wizardModel = new WizardModel();
        wizardDialog = new JDialog(owner);
        initComponents();
    }
 
    /**
     * This method accepts a java.awt.Frame object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Frame object that is the owner of the javax.swing.JDialog.
     */    
    public Wizard(Frame owner) 
    {
        wizardModel = new WizardModel();
        wizardDialog = new JDialog(owner);         
        initComponents();
    }
    
    /**
     * Returns an instance of the JDialog that this class created. This is useful in
     * the event that you want to change any of the JDialog parameters manually.
     * @return The JDialog instance that this class created.
     */    
    public JDialog getDialog() 
    {
        return wizardDialog;
    }
    
    public void addButtonListener (WizardListeners.WizardButtonListener listener) 
    {
        wizardController.addButtonListener(listener);
    }
    
    public void removeButtonListener (WizardListeners.WizardButtonListener listener) 
    {
        wizardController.removeButtonListener(listener);
    }
    
    /**
     * Returns the owner of the generated javax.swing.JDialog.
     * @return The owner (java.awt.Frame or java.awt.Dialog) of the javax.swing.JDialog generated
     * by this class.
     */    
    public Component getOwner() 
    {
        return wizardDialog.getOwner();
    }
    
    /**
     * Sets the title of the generated javax.swing.JDialog.
     * @param s The title of the dialog.
     */    
    public void setTitle(String s) 
    {
        wizardDialog.setTitle(s);
    }
    
    /**
     * Returns the current title of the generated dialog.
     * @return The String-based title of the generated dialog.
     */    
    public String getTitle() 
    {
        return wizardDialog.getTitle();
    }
    
    /**
     * Sets the modality of the generated javax.swing.JDialog.
     * @param b the modality of the dialog
     */    
    public void setModal(boolean b) 
    {
        wizardDialog.setModal(b);
    }
    
    /**
     * Returns the modality of the dialog.
     * @return A boolean indicating whether or not the generated javax.swing.JDialog is modal.
     */    
    public boolean isModal() 
    {
        return wizardDialog.isModal();
    }
    
    /**
     * Convienence method that displays a modal wizard dialog and blocks until the dialog
     * has completed.
     * @return Indicates how the dialog was closed. Compare this value against the RETURN_CODE
     * constants at the beginning of the class.
     */    
    public int showModalDialog() 
    {
        
        wizardDialog.setModal(true);
        wizardDialog.pack();
        wizardDialog.setVisible(true);
        
        return returnCode;
    }
    
    /**
     * Convienence method that displays a non-modal wizard dialog
     */    
    public void showNonModalDialog() 
    {
        wizardDialog.setModal(false);
        wizardDialog.pack();
        wizardDialog.setVisible(true);
    }
    
    /**
     * Returns the current model of the wizard dialog.
     * @return A WizardModel instance, which serves as the model for the wizard dialog.
     */    
    public WizardModel getModel() 
    {
        return wizardModel;
    }
    
    /**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     * @param id An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param panel The WizardPanelDescriptor object which contains helpful information about the panel.
     */    
    public void registerWizardPanel(Object id, WizardPanelDescriptor panel) 
    {
        //  Add the incoming panel to our JPanel display that is managed by
        //  the CardLayout layout manager.
        cardPanel.add(panel.getPanelComponent(), id);
        
        //  Set a callback to the current wizard.
        panel.setWizard(this);
        
        //  Place a reference to it in the model. 
        wizardModel.registerPanel(id, panel);
    }  
    
    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param id The Object-based identifier of the panel to be displayed.
     */    
    public void setCurrentPanel(Object id) 
    {
        boolean continue_to_next;
        
        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close
        //  the dialog.
        if (id == null) close(ERROR_RETURN_CODE);

        // get the panel being displayed - check that it can be closed
        continue_to_next = true;
        WizardPanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
        {
            if (! oldPanelDescriptor.aboutToHidePanel()) continue_to_next = false;
        }
        
        if (continue_to_next)
        {
            // set the next panel
            wizardModel.setCurrentPanel(id);
            wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();
        
            //  Show the panel in the dialog.
            cardLayout.show(cardPanel, id.toString());
            wizardModel.getCurrentPanelDescriptor().displayingPanel();        
        }
    }
    
    /** find the panel currently displayed 
     * @return the current panel's ID*/
    public Object getCurrentPanel ()
    {
        return wizardModel.getCurrentPanelDescriptor().getPanelDescriptorIdentifier();
    }
    
    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (evt.getPropertyName().equals(WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY)) {
            wizardController.resetButtonsToPanelRules(); 
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY)) {            
            nextButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_TEXT_PROPERTY)) {            
            backButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_TEXT_PROPERTY)) {            
            cancelButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY)) {            
            nextButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ENABLED_PROPERTY)) {            
            backButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY)) {            
            cancelButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY)) {            
            nextButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ICON_PROPERTY)) {            
            backButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ICON_PROPERTY)) {            
            cancelButton.setIcon((Icon)evt.getNewValue());
        }
    }
    
    /**
     * Retrieves the last return code set by the dialog.
     * @return An integer that identifies how the dialog was closed. See the *_RETURN_CODE
     * constants of this class for possible values.
     */    
    public int getReturnCode() 
    {
        return returnCode;
    }
    
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */  
    public boolean isBackButtonEnabled() 
    {
        return wizardModel.getBackButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setBackButtonEnabled(boolean newValue) 
    {
        wizardModel.setBackButtonEnabled(new Boolean(newValue));
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean isNextFinishButtonEnabled() 
    {
        return wizardModel.getNextFinishButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setNextFinishButtonEnabled(boolean newValue) 
    {
        wizardModel.setNextFinishButtonEnabled(new Boolean(newValue));
    }
 
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean isCancelButtonEnabled() 
    {
        return wizardModel.getCancelButtonEnabled().booleanValue();
    }

    /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setCancelButtonEnabled(boolean newValue) 
    {
        wizardModel.setCancelButtonEnabled(new Boolean(newValue));
    }
    
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */    
    void close(int code) 
    {
        returnCode = code;
        wizardDialog.dispose();
    }
    
    /**
     * This method initializes the components for the wizard dialog: it creates a JDialog
     * as a CardLayout panel surrounded by a small amount of space on each side, as well
     * as three buttons at the bottom.
     */
    private void initComponents() 
    {
        setButtonDefaults ();
        
        wizardModel.addPropertyChangeListener(this);       
        wizardController = new WizardController(this);       

        wizardDialog.getContentPane().setLayout(new BorderLayout());
        wizardDialog.addWindowListener(this);
                
        //  Create the outer wizard panel, which is responsible for three buttons:
        //  Next, Back, and Cancel. It is also responsible a JPanel above them that
        //  uses a CardLayout layout manager to display multiple panels in the 
        //  same spot.
        
        JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       

        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);
        
        backButton = new JButton();
        nextButton = new JButton();
        cancelButton = new JButton();
        
        backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

        backButton.addActionListener(wizardController);
        nextButton.addActionListener(wizardController);
        cancelButton.addActionListener(wizardController);
        
        //  Create the buttons with a separator above them, then place them
        //  on the east side of the panel with a small amount of space between
        //  the back and the next button, and a larger amount of space between
        //  the next button and the cancel button.
        
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        
        wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

    }
    
   /**
     * If the user presses the close box on the dialog's window, treat it
     * as a cancel.
     * @param WindowEvent The event passed in from AWT.
     */ 
    public void windowClosing(WindowEvent e) 
    {
        returnCode = CANCEL_RETURN_CODE;
    }

    public void addWindowListener (WindowListener listener) {
        wizardDialog.addWindowListener(listener);
    }
    
    public void removeWindowListener (WindowListener listener) {
        wizardDialog.removeWindowListener(listener);
    }

    /** set the text for the back button on all future panels - 
     * to set the text on the current panel as well, call 
     * WizardModel.setBackButtonText() */
    public void setBackButtonText (Object label) 
    { 
        default_back_label = label;
    }
    
    /** set the icon for the back button on all future panels - 
     * to set the icon on the current panel as well, call 
     * WizardModel.setBackButtonIcon() */
    public void setBackButtonIcon (Icon icon) 
    { 
        default_back_icon = icon;
    }
    
    /** set the text for the next button on all future panels - 
     * to set the text on the current panel as well, call 
     * WizardModel.setNextFinishButtonText() */
    public void setNextButtonText (Object label) 
    { 
        default_next_label = label;
    }
    
    /** set the icon for the next button on all future panels - 
     * to set the icon on the current panel as well, call 
     * WizardModel.setNextFinishButtonIcon() */
    public void setNextButtonIcon (Icon icon) 
    { 
        default_next_icon = icon;
    }
    

    /** set the text for the finish button on all future panels - 
     * to set the text on the current panel as well, call 
     * WizardModel.setNextFinishButtonText() */
    public void setFinishButtonText (Object label) 
    { 
        default_finish_label = label;
    }
    
    /** set the icon for the finish button on all future panels - 
     * to set the icon on the current panel as well, call 
     * WizardModel.setNextFinishButtonIcon() */
    public void setFinishButtonIcon (Icon icon) 
    { 
        default_finish_icon = icon;
    }

    /** set the text for the cancel button on all future panels - 
     * to set the text on the current panel as well, call 
     * WizardModel.setCancelButtonText() */
    public void setCancelButtonText (Object label) 
    { 
        default_cancel_label = label;
    }
    
    /** set the icon for the cancel button on all future panels - 
     * to set the icon on the current panel as well, call 
     * WizardModel.setCancelButtonIcon() */
    public void setCancelButtonIcon (Icon icon) 
    { 
        default_cancel_icon = icon;
    }
    
    public Object getBackButtonText () { return default_back_label; }
    public Icon getBackButtonIcon () { return default_back_icon; }
    public Object getNextButtonText () { return default_next_label; }
    public Icon getNextButtonIcon () { return default_next_icon; }
    public Object getFinishButtonText () { return default_finish_label; }
    public Icon getFinishButtonIcon () { return default_finish_icon; }
    public Object getCancelButtonText () { return default_cancel_label; }
    public Icon getCancelButtonIcon () { return default_cancel_icon; }
    
    private void setButtonDefaults ()
    {
        URL url;
        
        setBackButtonText("Back");
        setNextButtonText("Next");
        setFinishButtonText("Finish");
        setCancelButtonText("Cancel");
        
        try 
        {
            url = this.getClass().getResource("backIcon.gif");
            if (url == null) throw new ClassNotFoundException ();
            setBackButtonIcon (new ImageIcon(url));
            url = this.getClass().getResource("nextIcon.gif");
            if (url == null) throw new ClassNotFoundException ();
            setNextButtonIcon (new ImageIcon(url));
            url = this.getClass().getResource("finishIcon.gif");
            if (url == null) throw new ClassNotFoundException ();
            setFinishButtonIcon (new ImageIcon(url));
            url = this.getClass().getResource("cancelIcon.gif");
            if (url == null) throw new ClassNotFoundException ();
            setCancelButtonIcon (new ImageIcon(url));
        } 
        catch (ClassNotFoundException cnfe) 
        {
            setBackButtonIcon (null);
            setNextButtonIcon (null);
            setFinishButtonIcon (null);
            setCancelButtonIcon (null);
        }

    }

}
