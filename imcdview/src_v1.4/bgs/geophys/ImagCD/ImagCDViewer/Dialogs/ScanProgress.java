/*
 * ScanProgress.java
 *
 * Created on 05 June 2002, 21:11
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.File;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * A dialog that shows progress in scanning and registers users cancel requests.
 *
 * @author  Simon
 * @version 
 */
public class ScanProgress extends JDialog
implements ActionListener
{

  // private members
  private JLabel current_dir_label;
  private boolean cancel_requested;
  private Timer timer;
  private CDDatabaseList database_list;
  private Frame frame_owner;
  private Dialog dialog_owner;
  private boolean dialog_closed;
  private int error_list_severity;
  
  /** Creates new ScanProgress. This dialog is modal. Since the constructor
   * calls show, the constructor will block until the scan completes
   * @param owner the owner of this dialog
   * @param database_list the database list that will run the scan
   * @param start_dir directory to start scan (null to use file roots)
   * @param recurse_limit limit the recursion into sub-directories to
   *        to amount shown
   * @param ignore_system_dirs true to ignore system directories
   * @param error_list_severity the severity code for error display*/
  public ScanProgress(Frame owner, CDDatabaseList database_list, File start_dir, 
                      int recurse_limit, boolean ignore_system_dirs, int error_list_severity) 
  {
    // create the dialog (as a modal dialog)
    super (owner, "Scan Progress", true);
    this.database_list = database_list;
    this.frame_owner = owner;
    this.dialog_owner = null;
    this.error_list_severity = error_list_severity;
    createComponents (start_dir, recurse_limit, ignore_system_dirs);
  }

  /** Creates new ScanProgress. This dialog is modal. Since the constructor
   * calls show, the constructor will block until the scan completes
   * @param owner the owner of this dialog
   * @param database_list the database list that will run the scan
   * @param start_dir directory to start scan (null to use file roots)
   * @param recurse_limit limit the recursion into sub-directories to
   *        to amount shown
   * @param ignore_system_dirs true to ignore system directories
   * @param error_list_severity the severity code for error display*/
  public ScanProgress(Dialog owner, CDDatabaseList database_list, File start_dir, 
                      int recurse_limit, boolean ignore_system_dirs, int error_list_severity) 
  {
    // create the dialog (as a modal dialog)
    super (owner, "Scan Progress", true);
    this.database_list = database_list;
    this.frame_owner = null;
    this.dialog_owner = owner;
    this.error_list_severity = error_list_severity;
    createComponents (start_dir, recurse_limit, ignore_system_dirs);
  }

  /** set up the components
   * @param start_dir directory to start scan (null to use file roots)
   * @param recurse_limit limit the recursion into sub-directories to
   *        to amount shown
   * @param ignore_system_dirs true to ignore system directories */
  private void createComponents (File start_dir, int recurse_limit, boolean ignore_system_dirs)
  {
    // declare local variables
    Container content_pane;

    // initialise members
    cancel_requested = false;
    dialog_closed = false;
    
    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout (new GridBagLayout ());

    // create a text field that will show the user what has been selected
    current_dir_label = new JLabel ();

    // add the components to the dialog
    SwingUtils.addToGridBag (new JLabel ("Scanning in: "),                        content_pane, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);
    SwingUtils.addToGridBag (current_dir_label,                                   content_pane, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH);
    SwingUtils.addToGridBag (SwingUtils.createButton ("Stop scan", "stop", this), content_pane, 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);

    // start the database scan and the timer (must be in that order)
    try
    {
      this.database_list.ScanForDatabases (start_dir, recurse_limit, ignore_system_dirs);
    }
    catch (CDException e) { }
    timer = new Timer (200, this);
    timer.start ();

    // set the dialogs size and show it
    setSize (600, 100);
    setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    setVisible (true);
  }

  /** Catch actions in this frame - specifically timer and cancel button push events
   * @param event object describing the event */
  public void actionPerformed (ActionEvent event)
  {
    boolean done_scanning;

    done_scanning = false;
    if (event.getSource () == timer)
    {
      if (database_list.IsScanInProgress ())
        current_dir_label.setText (database_list.GetCurrentScanDir().getAbsolutePath());
      else
        done_scanning = true;
    }
    else
    {
      database_list.StopScan ();
      while (database_list.IsScanInProgress ()) { }
      done_scanning = true;
    }

    if (done_scanning) closeDialog ();
  }
  
  private void closeDialog () 
  {
    int database_count;
    CDDatabase database;
    CDErrorList error_list;

    // make sure that this only happens once
    if (! dialog_closed)
    {
      // check the error lists for all databases
      try
      {
        for (database_count=0; database_count<database_list.GetNDatabases(); database_count++)
        {
          database = database_list.GetDatabase(database_count);
          error_list = database.GetErrorList ();
          if (error_list.GetNErrorMessages(error_list_severity) > 0)
          {
            if (frame_owner != null)
              new ErrorListDisplay (frame_owner, false, error_list, error_list_severity);
            else
              new ErrorListDisplay (dialog_owner, false, error_list, error_list_severity);
          }
        }
      }
      catch (CDException e) { SwingUtils.ExceptionHandler (e); }
      
      // close the dialog
      setVisible(false);
      dispose();
    }
    dialog_closed = true;
  }
}
