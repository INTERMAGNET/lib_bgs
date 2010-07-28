/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.Import;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorDialog.CloseOptions;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.ImportThread;
import bgs.geophys.library.Swing.Wizard.Wizard;
import bgs.geophys.library.Swing.Wizard.WizardEvents;
import bgs.geophys.library.Swing.Wizard.WizardEvents.WizardButtonEvent;
import bgs.geophys.library.Swing.Wizard.WizardEvents.WizardPanelChangeEvent;
import bgs.geophys.library.Swing.Wizard.WizardListeners;
import bgs.geophys.library.Swing.Wizard.WizardPanelDescriptor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author smf
 */
public class ImportWizard 
implements WizardListeners.WizardPanelChangeListener,
           WizardListeners.WizardButtonListener,
           ActionListener,
           WindowListener
{

    // private members
    private Wizard wizard;
    private WizardPanelDescriptor descs [];
    private Timer timer;
    private ImportThread import_thread;
    private ImportStep1 import_step_1;
    private ImportStep2 import_step_2;
    private ImportStep3 import_step_3;
    private ImportStep4 import_step_4;
    private ImportStep5 import_step_5;
    private ImportStep6 import_step_6;
    private boolean phase_1_complete;
    private boolean phase_2_complete;
    private boolean phase_3_complete;
    private String last_panel_id;
        
    /* create a new Import Wizard */
    public ImportWizard ()
    {
        int count;
        String ids [];
        
        // create the object that will do all the work
        import_thread = new ImportThread ();
        
        // create the ids, descriptions and panels
        ids = new String [6];
        ids [0] = "INTRO_PANEL";
        ids [1] = "SELECT_OPTIONS_PANEL";
        ids [2] = "VALIDATE_FILES_PANEL";
        ids [3] = "CONVERT_DATA_PANEL";
        ids [4] = "INSTALL_DATA_PANEL";
        ids [5] = "SUMMARY_PANEL";
        last_panel_id = ids [ids.length -1];
        descs = new WizardPanelDescriptor [ids.length];
        import_step_1 = new ImportStep1 ();
        import_step_2 = new ImportStep2 ();
        import_step_3 = new ImportStep3 ();
        import_step_4 = new ImportStep4 ();
        import_step_5 = new ImportStep5 ();
        import_step_6 = new ImportStep6 ();
        descs [0] = new WizardPanelDescriptor (null ,  ids[0], ids[1], import_step_1);
        descs [1] = new WizardPanelDescriptor (ids[0] ,ids[1], ids[2], import_step_2);
        descs [2] = new WizardPanelDescriptor (ids[1] ,ids[2], ids[3], import_step_3);
        descs [3] = new WizardPanelDescriptor (ids[2] ,ids[3], ids[4], import_step_4);
        descs [4] = new WizardPanelDescriptor (ids[3] ,ids[4], ids[5], import_step_5);
        descs [5] = new WizardPanelDescriptor (ids[4] ,ids[5], WizardPanelDescriptor.FINISH, import_step_6);
        
        // create the wizard and add the steps
        wizard = new Wizard(GlobalObjects.top_frame);
        wizard.setBackButtonText("Back");
        wizard.setNextButtonText("Next");
        wizard.setFinishButtonText("Finish");
        wizard.setCancelButtonText("Cancel");
        wizard.getDialog().setTitle("Import Data Wizard");
        for (count=0; count<descs.length; count++)
        {
            wizard.registerWizardPanel(ids[count], descs[count]);
            descs[count].addPanelChangeListener(this);
        }
        wizard.addButtonListener(this);
        wizard.setCurrentPanel(ids[0]);
        
        // capture window events
        wizard.addWindowListener(this);
        
        // create and start a timer
        timer = new Timer (200, this);
        timer.start();
    }
    
    /** show the wizard in a modal dailog
     * @return 0 if wizard completed, 1 for cancel, 2 for error */
    public int showModal ()
    {
        int ret_val;
        
        ret_val = wizard.showModalDialog();
        return ret_val;
    }
    
    /** show the wizard in a non-modal dialog */
    public void showNonModal ()
    {
        wizard.showNonModalDialog();
    }
    
    /** handle events where a new panel is about to display in the wizard */
    public void aboutToDisplayPanel(WizardPanelChangeEvent evt) 
    {
        int panel_index;
        
        // which panel is about to display
        panel_index = findPanel (evt.getPanelID());
        switch (panel_index)
        {
        case 0: // the introduction panel
            break;
            
        case 1: // the options selection panel
            break;
            
        case 2: // the file list validation panel
            // start the import thread pahse 1 - disable buttons while the thread is running
            wizard.setBackButtonEnabled(false);
            wizard.setNextFinishButtonEnabled(false);
            phase_1_complete = false;
            import_step_3.removeAllGeomagFiles();
            import_thread.startPhase1 (import_step_2.getSelectedFile(),
                                       import_step_2.isIncludeSubDirs(),
                                       import_step_2.isUseCDFormat(),
                                       import_step_2.isUseIMFFormat(),
                                       import_step_2.isUseIAGA2002Format(),
                                       import_step_2.isUseKIndexFormat());
            break;
            
        case 3: // the data conversion panel
            // start the import thread phase 2 - disable buttons while the thread is running
            wizard.setBackButtonEnabled(false);
            wizard.setNextFinishButtonEnabled(false);
            phase_2_complete = false;
            import_step_4.clearErrors();
            import_step_4.clearFiles();
            import_step_4.setPercentComplete (0);
            import_step_4.enableEditButtons(false);
            import_thread.startPhase2 (import_step_2.isMakeHourlyMeans(), 
                                       import_step_2.isMakeDailyMeans(),
                                       import_step_2.getMaxMissingPC());
            break;
            
        case 4: // the file installation panel
            break;
            
        case 5: // the summary panel
            // start the import thread phase 3 - disable buttons while the thread is running
            wizard.setBackButtonEnabled(false);
            wizard.setNextFinishButtonEnabled(false);
            phase_3_complete = false;
            import_step_6.removeAllResults();
            import_step_6.setPercentComplete (0);
            import_thread.startPhase3 (import_step_5.getTargetDir(),
                                       import_step_5.isOverwrite(),
                                       import_step_5.isZipFiles(),
                                       import_step_5.addDataSource());
            break;
        }
    }

    /** handle events where a new panel is displayed in the wizard */
    public void displayingPanel(WizardPanelChangeEvent evt) 
    {
        int panel_index;
        
        // which panel has been displayed
        panel_index = findPanel (evt.getPanelID());
        switch (panel_index)
        {
        case 0: // the introduction panel
            break;
            
        case 1: // the options selection panel
            break;
            
        case 2: // the file list validation panel
            import_step_3.setChosenDirectory(import_step_2.getSelectedFile());
            break;
            
        case 3: // the data conversion panel
            import_step_4.setTmpDirectory(import_thread.getTmpDir());
            break;
            
        case 4: // the file installation panel
            break;
            
        case 5: // the summary panel
            break;
        }
    }

    /** handle events where a panel is about to be hidden in the wizard */
    public boolean aboutToHidePanel(WizardPanelChangeEvent evt) 
    {
        int panel_index;
        
        // which panel is about to be hidden
        panel_index = findPanel (evt.getPanelID());
        switch (panel_index)
        {
        case 0: // the introduction panel
            break;
            
        case 1: // the options selection panel
            break;
            
        case 2: // the file list validation panel
            import_step_3.updateGeomagFilesFromUI();
            break;
        
        case 3: // the data conversion panel
            if (import_step_4.isEditsPending())
                JOptionPane.showMessageDialog (import_step_4,
                                               "<html>You have made changes to one or more file or header dialogs.<br>" +
                                               "Please close these dialogs before proceeding.</html>",
                                               "Editors have unsaved changes",
                                               JOptionPane.INFORMATION_MESSAGE);
            else
                import_step_4.closeEditors();
            break;
            
        case 4: // the file installation panel
            break;
            
        case 5: // the summary panel
            wizard.setCancelButtonEnabled(true);
            break;
        }
        
        // default - allow process to continue
        return true;
    }

    /** handle events where forward/next/cancel wizard buttons are pressed */
    public void buttonClicked(WizardButtonEvent evt) 
    {
        switch (evt.getButtonCode())
        {
            case WizardEvents.BUTTON_CANCEL:
                import_thread.abort();
                break;
        }
    }

    /** an action performed handler for the timer */
    public void actionPerformed(ActionEvent e) 
    {
        int count;
        String string;
        
        // update messages from the background thread
        while ((string = import_thread.getMessageFromThread()) != null)
            import_step_3.setStatusMessage(string);
        
        // what phase is the import thread in
        switch (import_thread.getPhase())
        {
            case 0: // not started
                break;
                
            case 1: // listing files
                // check if we have finished
                if (! phase_1_complete)
                {
                    // has the phase completed
                    if (! import_thread.isThreadAlive())
                    {
                        // yes - enable the wizard's buttons
                        wizard.setBackButtonEnabled(true);
                        if (import_thread.isPhase1Successful())
                            wizard.setNextFinishButtonEnabled(true);
                        
                        // add new geomag files to the table
                        for (count=0; count<import_thread.getNGeomagFiles(); count++)
                            import_step_3.addGeomagFile(import_thread.getGeomagFile(count));
                        
                        phase_1_complete = true;
                    }
                }
                break;
                
            case 2: // converting data
                // show percent complete
                import_step_4.setPercentComplete(import_thread.getPhase2PercentComplete());
                
                // check if we have finished
                if (! phase_2_complete)
                {
                    // has the phase completed
                    if (! import_thread.isThreadAlive())
                    {
                        // yes - enable the wizard's buttons
                        wizard.setBackButtonEnabled(true);
                        if (import_thread.isPhase2Successful())
                            wizard.setNextFinishButtonEnabled(true);
                        import_step_4.enableEditButtons(true);
                        
                        // show errors
                        if (import_thread.getNConversionSummaryLines() <= 0)
                            import_step_4.addToErrors("Conversion completed with no errors");
                        else
                        {
                            for (count=0; count<import_thread.getNConversionSummaryLines(); count++)
                                import_step_4.addToErrors(import_thread.getConversionSummaryLine(count));
                        }
                        
                        // show files
                        if (import_thread.getNImagCDFiles() <= 0)
                            import_step_4.setShowNoFiles();
                        else
                        {
                            for (count=0; count<import_thread.getNImagCDFiles(); count++)
                                import_step_4.addToFiles (import_thread.getImagCDFile(count));
                        }
                        
                        phase_2_complete = true;
                    }
                }
                break;
                
            case 3: // installing data files
                // show percent complete
                import_step_6.setPercentComplete(import_thread.getPhase3PercentComplete());
                
                // check if we have finished
                if (! phase_3_complete)
                {
                    // has the phase completed
                    if (! import_thread.isThreadAlive())
                    {
                        // yes - enable the wizard's buttons, except cancel (it's too late now)
                        wizard.setBackButtonEnabled(true);
                        wizard.setNextFinishButtonEnabled(true);
                        wizard.setCancelButtonEnabled(false);
                        
                        // show details of installation
                        for (count=0; count<import_thread.getNInstallResults(); count++)
                            import_step_6.addResult (import_thread.getInstallResult(count));
                        
                        phase_3_complete = true;
                    }
                }
                break;
        }
    }

    /** find the index of a panel from its ID */
    private int findPanel (Object id)
    {
        int count;
        String panel_id;

        if (id == null) return -1;
        panel_id = (String) id;
        for (count=0; count<descs.length; count++)
        {
            if (panel_id.equals((String) descs[count].getPanelDescriptorIdentifier()))
                return count;
        }
        return -1;
    }

    public JDialog getWizardDialog () { return wizard.getDialog(); }
    
    public void windowOpened(WindowEvent e) { }
    public void windowClosing(WindowEvent e) { closeWizard (); }
    public void windowClosed(WindowEvent e) { closeWizard (); }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    
    public void closeWizard ()
    {
        import_step_4.closeEditors();
        import_thread.cleanpUp();
    }
}
