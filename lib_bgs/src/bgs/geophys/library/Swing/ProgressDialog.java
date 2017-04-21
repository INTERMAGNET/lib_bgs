/*
 * ProgressDialog.java
 *
 * Created on 24 January 2003, 14:05
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 * ProgressDialog - dialogue box which displays a message and a "Cancel" button,
 *                  for use when a task is expected to take a long time.
 *                  This dialogue and the task should be run from separate threads in
 *                  the following way:
 * 1) Create dialog. Initial message is passed to the constructor. Set visible.
 * 2) Start a new thread. The run() method for this thread should run a method 
 *    which carries out the long task.
 * 3) During the long task, check whether the cancel button on this dialog has been pressed using
 *    the isCancelled() method. This dialog's message can be changed using the update() method.
 * 5) If task is cancelled or finished, join threads and hide dialog.
 *
 * @author  fmc
 * @version 
 */


public class ProgressDialog extends JDialog implements ActionListener
{
  private JPanel contentPanel;
  private JLabel icon_label;
  private JLabel progressLabel;
  private JLabel completionLabel;
  private JLabel totalProgressLabel;
  private JProgressBar totalProgress;
  private JLabel operationProgressLabel;
  private JProgressBar operationProgress;  
  private JButton cancelButton;
  private JButton closeButton;
  private boolean cancelled = false;
  private int maxWidth;
  private Date startDate;
  private Date lastDate;

  /** Create a new progress dialog
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (String message, String totalProgressMessage, 
                         String operationProgressMessage, boolean showCompletionTime)
  {
    setTitle ("Progress");
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    
    
  /** Create a new progress dialog
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, boolean showCompletionTime)
  {
    setTitle (title);
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /**Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JFrame owner, String title, String message, 
                         String totalProgressMessage, String operationProgressMessage,
                         boolean showCompletionTime)
  {
    super (owner, title);
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /**Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JDialog owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, boolean showCompletionTime)
  {
    super (owner, title);
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /** Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param modal true for a modal dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JFrame owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, boolean modal, boolean showCompletionTime)
  {
    super (owner, title, modal);
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /** Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param modal true for a modal dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JDialog owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, boolean modal, boolean showCompletionTime)
  {
    super (owner, title, modal);
    createComponents (null, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    
  
  /** Create a new progress dialog
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (String message, String totalProgressMessage, 
                         String operationProgressMessage, Icon icon, boolean showCompletionTime)
  {
    setTitle ("Progress");
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    
    
  /** Create a new progress dialog
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (String title, String message, String totalProgressMessage, 
                         String operationProgressMessage,  Icon icon, boolean showCompletionTime)
  {
    setTitle (title);
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /**Create a new progress dialog
   * @param icon and icon for the dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JFrame owner, String title, String message, 
                         String totalProgressMessage, String operationProgressMessage, Icon icon, 
                         boolean showCompletionTime)
  {
    super (owner, title);
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /**Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JDialog owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, Icon icon, boolean showCompletionTime)
  {
    super (owner, title);
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /** Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param modal true for a modal dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JFrame owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, Icon icon, boolean modal, boolean showCompletionTime)
  {
    super (owner, title, modal);
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /** Create a new progress dialog
   * @param owner - the dialog owner
   * @param title - the dialog title
   * @param message - the message to display on the dialog
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param icon and icon for the dialog
   * @param modal true for a modal dialog
   * @param showCompletionTime if true show amount of time until finish - this only
   *        works if you regularly update the total percent figure */
  public ProgressDialog (JDialog owner, String title, String message, String totalProgressMessage, 
                         String operationProgressMessage, Icon icon, boolean modal, boolean showCompletionTime)
  {
    super (owner, title, modal);
    createComponents (icon, message, totalProgressMessage, operationProgressMessage, showCompletionTime);
  }    

  /** set up the components
   * @param icon an icon for the dialog (may be null)
   * @param message the initial message
   * @param totalProgressMessage label for the total progress bar - null = no bar
   * @param operationProgressMessage label for the operation progress bar - null = no bar
   * @param showCompletionTime if true show amount of time till finished */
  private void createComponents (Icon icon,
                                 String message, String totalProgressMessage,
                                 String operationProgressMessage, 
                                 boolean showCompletionTime)
  {
    int rowCount;
    GridBagLayout layout;

    setResizable (false);
    
    setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
    
    // add components to dialogue
    layout = new GridBagLayout ();
    contentPanel = (JPanel) getContentPane();
    contentPanel.setLayout (layout);
    rowCount = 0;

    // create the progress bars even if the label messages are null, but don't add them
    // to the dialog - that way they can be set without a null pointer exception
    if (icon != null)
    {
        icon_label = new JLabel (icon);
        SwingUtils.addToGridBag (icon_label, contentPanel, 0, rowCount, 2, 1, 1.0, 1.0, GridBagConstraints.WEST);
        rowCount ++;
    }
    if (totalProgressMessage == null) totalProgressLabel = new JLabel ();
    else totalProgressLabel = new JLabel (totalProgressMessage);
    totalProgress = new JProgressBar (0, 100);
    totalProgress.setValue (0);
    totalProgress.setStringPainted(true);
    totalProgress.setString ("0%");
    if (totalProgressMessage != null)
    {
        SwingUtils.addToGridBag (totalProgressLabel, contentPanel, 0, rowCount, 1, 1, 1.0, 1.0, GridBagConstraints.NONE);
        SwingUtils.addToGridBag (totalProgress,      contentPanel, 1, rowCount, 1, 1, 1.0, 1.0, GridBagConstraints.NONE);
        rowCount ++;
    }
    if (operationProgressMessage == null) operationProgressLabel = new JLabel ();
    else operationProgressLabel = new JLabel (operationProgressMessage);
    operationProgress = new JProgressBar (0, 100);
    operationProgress.setValue (0);
    operationProgress.setStringPainted(true);
    operationProgress.setString ("0%");
    if (operationProgressMessage != null)
    {
        SwingUtils.addToGridBag (operationProgressLabel, contentPanel, 0, rowCount, 1, 1, 1.0, 1.0, GridBagConstraints.NONE);
        SwingUtils.addToGridBag (operationProgress,      contentPanel, 1, rowCount, 1, 1, 1.0, 1.0, GridBagConstraints.NONE);
        rowCount ++;
    }
    
    progressLabel = new JLabel (message);
    maxWidth = progressLabel.getSize().width;
    SwingUtils.addToGridBag (progressLabel, contentPanel, 0, rowCount, 2, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);
    rowCount ++;
    
    if (showCompletionTime)
    {
        completionLabel = new JLabel ("Time remaining: Unknown");
        SwingUtils.addToGridBag (completionLabel, contentPanel, 0, rowCount, 2, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);
        rowCount ++;
    }
    
    cancelButton = new JButton ("Cancel");
    SwingUtils.addToGridBag (cancelButton, contentPanel, 0, rowCount, 2, 1, 1.0, 1.0, GridBagConstraints.NONE);
    cancelButton.addActionListener (this);
 
//    closeButton = new JButton ("Close");
//    SwingUtils.addToGridBag (closeButton, contentPanel, 2, rowCount, 2, 1, 1.0, 1.0, GridBagConstraints.NONE);
//    closeButton.addActionListener (this);
    
    getRootPane().setDefaultButton (cancelButton);

    pack ();
 //   this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    startDate = lastDate = new Date ();
  }

  /** change the message displayed on this dialog
   * @param message - the string to display */
  public void update (String message)
  {
    int width;

    if (! cancelled)
    {
      progressLabel.setText(message);
      width = progressLabel.getGraphics().getFontMetrics().stringWidth(message);
      if (width > maxWidth)
      {
        maxWidth = width;
        this.pack ();
      }
    }
  }
  
  /** update the total progress bar 
   * @param percentComplete the percentage that has been completed (0..100) */
  public void updateTotal (int percentComplete)
  {
      updateTotal (percentComplete, Integer.toString (percentComplete) + "%");      
  }
   
  /** update the total progress bar 
   * @param percentComplete the percentage that has been completed (0..100)
   * @param message a message to display in the progress bar */
  public void updateTotal (int percentComplete, String message)
  {
      String string;
      Date date;
      long time_elapsed, total_time, time_remaining;
      
    if (! cancelled)
    {
      totalProgress.setValue (percentComplete);
      totalProgress.setString (message);
      
      date = new Date ();
      if ((date.getTime() - lastDate.getTime()) > 1000l)
      {
          lastDate = date;
          if (percentComplete <= 0)
          {
              time_elapsed = time_remaining = total_time = 0l;
          }
          else
          {
              time_elapsed = date.getTime() - startDate.getTime();
              total_time = (time_elapsed * 100l) / (long) percentComplete;
              time_remaining = total_time - time_elapsed;
          }
          if (time_elapsed < 3000l) string = "Time remaining: Unknown";
          else if (time_remaining > 3600000l) string = "Time remaining: " + Long.toString (time_remaining / 3600000l) + " hour(s)";
          else if (time_remaining > 60000l) string = "Time remaining: " + Long.toString (time_remaining / 60000l) + " minute(s)";
          else string = "Time remaining: " + Long.toString (time_remaining / 1000l) + " second(s)";
          completionLabel.setText (string);
      }
    }
  }
   
  /** update the total progress bar 
   * @param percentComplete the percentage that has been completed (0..100) */
  public void updateOperation (int percentComplete)
  {
      updateOperation (percentComplete, Integer.toString (percentComplete) + "%");
  }
   
  /** update the current operation progress bar 
   * @param percentComplete the percentage that has been completed (0..100)
   * @param message a message to display in the progress bar */
  public void updateOperation (int percentComplete, String message)
  {
    if (! cancelled)
    {
      operationProgress.setValue (percentComplete);
      operationProgress.setString (message);
    }
  }
   
  /** catch events from the cancel button*/
  public void actionPerformed(java.awt.event.ActionEvent actionEvent)
  {
    if (actionEvent.getSource() == cancelButton)
    {
      // set flag to indicate button has been pressed
      completionLabel.setText ("Time remaining:");
      update ("Cancelling...");
      cancelled = true;
    }
//      if (actionEvent.getSource() == closeButton)
//    {
//      update ("Cancelling...");
//      cancelled = true;
//      // get rid of progress listener
//      this.dispose();
//    }
  }

  /** check flag to find out if Cancel button has been pressed
   * @return - true if button has been pressed, else false. */
  public boolean isCancelled ()
  {
    return cancelled;
  }
  
  /** programatically force cancelation */
  public void setCancelled ()
  {
      completionLabel.setText ("Time remaining:");
      update ("Cancelling...");
      cancelled = true;
  }
  
}
