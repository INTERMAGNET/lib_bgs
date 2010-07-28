/*
 * CommandInterpreter.java
 *
 * Created on 12 June 2002, 11:28
 */

package bgs.geophys.ImagCD.ImagCDViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer.DataViewer;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer.DataViewerException;
import bgs.geophys.library.Swing.MemoryViewDialog;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import bgs.geophys.library.Swing.*;
import bgs.geophys.library.File.*;
import bgs.geophys.library.Net.*;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer.AnnualMeanViewer;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer.AnnualMeanViewerException;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorPanel.FileLoadException;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.Import.ImportWizard;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.ImagCD.ImagCDViewer.Menus.*;
import bgs.geophys.library.Data.ImagCD.BLVException;

/**
 * The main program command interpreter lives here. This object also implements the
 * ActionListener interface, so that other objects can use it to process menu
 * commands, etc. To do this, the menu's ActionCommand must be set to a string
 * that is recognised by the command interpreter.
 *
 * This object is used as a reference to allow the program to find data files and
 * icons. IT IS VERY IMPORTANT that the class file for this object is at the base
 * of the directory tree for the application - the data files and tables must live
 * in subdirectories of this object's class file directory, otherwise they won't be
 * found. See GlobalObjects for details.
 *
 * @author  Simon
 * @version 
 */
public class CommandInterpreter extends Object 
implements ActionListener, DataSourceListener, Runnable, ComponentListener
{
    
  // private members - dialogs
  private SelectDataSource data_source_dialog;
  private Explorer explorer_dialog;
  private ExportDialog export_dialog;
  private CatalogueDialog catalogue_dialog;
  private DataOptionsDialog data_options_dialog;
  private PlotOptionsDialog plot_options_dialog;
  private Vector<DataViewer> data_viewer_list;
  private Vector<AnnualMeanViewer> annual_mean_viewer_list;
  private Vector<BLVFrame> baseline_viewer_list;
  private MemoryViewDialog memory_view_dialog;
  private CtryObsyListViewerDialog config_view_dialog;
  private ImportWizard import_wizard;
  private ProgramOptionsDialog program_options_dialog;
  private ExportOptionsDialog export_options_dialog;
  
  private BrowserControl browser_control;

  // action being performed by second thread
  private final static int COPY   = 0;
  private final static int MOVE   = 1;
  private final static int DELETE = 2;

  // thread for copying and deleting files
  private Thread second_thread;
  // the action being carried out by second thread
  // (either copy or delete)
  private int action;
  // flag used to end recursion in copyDirStructure and delDirStructure
  private static boolean status = true;
  // global variables needed to pass parameters to second thread
  // run method for copying and deleting
  private File [] dirArray;
  private File destDir;

  /** Creates new CommandInterpreter */
  public CommandInterpreter ()
  {
    data_viewer_list = new Vector<DataViewer> ();
    annual_mean_viewer_list = new Vector<AnnualMeanViewer> ();
    baseline_viewer_list = new Vector<BLVFrame> ();
    browser_control = new BrowserControl ();
    import_wizard = null;
  }

  /** Interpret command strings - all (major) commands for the program pass through this
   * command interpreter. One simple way of working with Swing buttons is to attach a String
   * that is retrieved when the button is pressed. This routine processes the actions
   * for these strings (and any others)
   * @param args the command to process, arguments separated by '&'
   * @return true if the command could be interpreted, false otherwise
   *         the user is notified if there are any problems */
  public boolean interpretCommand (String args)
  {
    String command, str, page, token_array [], report_fields [], obsy_code, url_string, anchor, base_dir;
    StringTokenizer url_tokens;
    LookAheadStringTokenizer arg_tokens;
    boolean ret_val, flag, use_plain, use_zip;
    int count, yr, mon, choice, width, height, index, code;
    Integer year = null;
    File dir, file;
    JFileChooser fc;
    Cursor old_cursor;
    DataViewer data_viewer;
    TextViewerDialog text_viewer;
    TextFileSelector text_selector;
    ImageViewerDialog image_viewer;
    ImageFileSelector image_selector;
    KIndexViewer k_index_viewer;
    AnnualMeanCheckDialog mean_viewer;
    Frame parent_frame;
    Dialog parent_dialog;
    Component parent_component;
    FaultReport fault_report;
    URL url, full_url;
    AnnualMeanViewer annual_mean_viewer;
    BLVFrame baseline_viewer;
    CDDataMonth data_month_list [];
    NewExporterDialog newExporterDialog;

    
    ret_val = true;
    old_cursor = null;
    parent_frame = null;
    parent_dialog = null;
    parent_component = null;

    // send a copy of the command to the debug dialog
    GlobalObjects.debug_manager.logDebugMessage ("Command interpreter",  args);
    
    // process the command
    try
    {
      // split command into its arguments
      arg_tokens = new LookAheadStringTokenizer (args, "&");
      command = arg_tokens.nextToken ();
      
      // the first argument may be a 'from_' window - in which case extract it
      // at the end of this block of code:
      //   if the 'from window' was a dialog, parent_dialog contains the dialog
      //   else if the 'from window' was a frame, parent_frame contains the frame
      //   else parent_frame contain the top frame
      parent_frame = findParentFrame (arg_tokens);
      if (parent_frame == null)
      {
          parent_dialog = findParentDialog (arg_tokens);
          if (parent_dialog == null) parent_frame = GlobalObjects.top_frame;
      }
      if (parent_frame != null) parent_component = parent_frame;
      else parent_component = parent_dialog;

      // set a busy cursor
      if (parent_dialog != null)
      {
          old_cursor = parent_dialog.getCursor();
          parent_dialog.setCursor(GlobalObjects.wait_cursor);
      }
      else
      {
          old_cursor = parent_frame.getCursor();
          parent_frame.setCursor(GlobalObjects.wait_cursor);
      }

      // process individual commands
      if (command.equals ("raise_data_source_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable (data_source_dialog))
        {
          if (parent_dialog != null)
              data_source_dialog = new SelectDataSource (parent_dialog, false);
          else
              data_source_dialog = new SelectDataSource (parent_frame, false);
          data_source_dialog.addDataSourceListener (this);
          data_source_dialog.setLocationRelativeTo(parent_component);
        }
        data_source_dialog.setVisible (true);
      }

      else if (command.equals ("send_fault_report"))
      {
        report_fields = new String [3];
        report_fields [0] = "What part of the sofware were you using: ";
        report_fields [1] = "What data were you accessing: ";
        report_fields [2] = "Any other information: ";
        fault_report = new FaultReport (null, false, 
                                        AboutDialog.PROGRAM_NAME + " Version " + AboutDialog.formatVersionNumber(),
                                        "smf@bgs.ac.uk", report_fields, null);
        fault_report.setVisible (true);
      }
      
      else if (command.equals ("show_external_web_page"))
      {
          page = arg_tokens.nextToken();
          str = null;
          try
          {
              if (! browser_control.isBrowserAvailable ()) str = "Unable to find web browser";
              else browser_control.displayURL(page);
          }
          catch (InterruptedException e) { str = "Unable to display page " + page; }
          catch (IOException e) { str = "Unable to display page " + page; }
          if (str != null) JOptionPane.showMessageDialog (parent_component, str, "Error With Browser", JOptionPane.ERROR_MESSAGE);
      }
      
      else if (command.equals ("show_help"))
      {
          url_string = arg_tokens.nextToken();
          url_tokens = new StringTokenizer (url_string, "#");
          page = url_tokens.nextToken();
          if (url_tokens.hasMoreTokens())
              anchor = url_tokens.nextToken();
          else
              anchor = null;
          url = GlobalObjects.command_interpreter.getClass().getResource("Help/" + page);
          if (url == null)
              JOptionPane.showMessageDialog (parent_component, "Unable to load help file: " + page,
                                             "Installation Error", JOptionPane.ERROR_MESSAGE);
          else 
          {
              try 
              {
                  if (anchor == null) full_url = url;
                  else full_url = new URL (url.toExternalForm() + "#" + anchor); 
              }
              catch (MalformedURLException e) { full_url = url; }
              if (parent_dialog != null)
                  new HTMLViewer (parent_dialog, false, full_url).setVisible(true);
              else
                  new HTMLViewer (parent_frame, false, full_url).setVisible(true);
          }
      }

      else if (command.equals ("view_longest_errata_list"))
      {
          str = GlobalObjects.database_list.GetLongestErrataFile();
          if (str == null)
              JOptionPane.showMessageDialog (parent_component, "No errata files were found in any of the current databases",
                                             "No Errata Lists", JOptionPane.ERROR_MESSAGE);
          else
          {
              if (parent_dialog != null)
                  text_viewer = new TextViewerDialog (parent_dialog, new File (str), "Errata List", GlobalObjects.imag_icon);
              else
                  text_viewer = new TextViewerDialog (parent_frame, new File (str), "Errata List", GlobalObjects.imag_icon);
              text_viewer.setVisible (true);
          }
      }
      
      else if (command.equals ("view_latest_cd_readme"))
      {
          str = GlobalObjects.database_list.GetLatestReadmeFile();
          if (str == null)
              JOptionPane.showMessageDialog (parent_component, "No README files were found in any of the current databases",
                                             "No README Files", JOptionPane.ERROR_MESSAGE);
          else
          {
              if (parent_dialog != null)
                  text_viewer = new TextViewerDialog (parent_dialog, new File (str), "CD Information", GlobalObjects.imag_icon);
              else
                  text_viewer = new TextViewerDialog (parent_frame, new File (str), "CD Information", GlobalObjects.imag_icon);
              text_viewer.setVisible (true);
          }
      }
      
      else if (command.equals ("copy_data_source"))
      {
        dir = new File (arg_tokens.nextToken ());
        try
        {
          if (arg_tokens.hasMoreTokens ())
          {
            year = new Integer (Integer.parseInt (arg_tokens.nextToken ()));
          }

          dirArray = getDirArray (dir, year);
          if (dirArray != null)
          {
            // choose destination directory
            fc = new JFileChooser (dir);
            fc.setApproveButtonText ("Select");
            fc.setDialogTitle ("Select destintion for database copy");
            fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);

            choice = fc.showSaveDialog (parent_component);
            if (choice == JFileChooser.APPROVE_OPTION)
            {
              destDir = fc.getSelectedFile ();
            }
            else destDir = null;

            if (destDir != null)
            {
              // set the flag to indicate what second thread does
              action = COPY;

              // start new thread for copy
              second_thread = new Thread (this, "Copy thread");
              second_thread.start ();
            }
          }
        }
        catch (CDException e)
        {
          SwingUtils.ExceptionHandler (e);
          ret_val = false;
        }
      }

      else if (command.equals ("move_data_source"))
      {
        dir = new File (arg_tokens.nextToken ());
        try
        {
          if (arg_tokens.hasMoreTokens ())
          {
            year = new Integer (Integer.parseInt (arg_tokens.nextToken ()));
          }

          dirArray = getDirArray (dir, year);
          if (dirArray != null)
          {
            // choose destination directory
            fc = new JFileChooser (dir);
            fc.setApproveButtonText ("Select");
            fc.setDialogTitle ("Select destination for database move");
            fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);

            choice = fc.showSaveDialog (parent_component);
            if (choice == JFileChooser.APPROVE_OPTION)
            {
              destDir = fc.getSelectedFile ();
            }
            else destDir = null;

            if (destDir != null)
            {
              // set the flag to indicate what second thread does
              action = MOVE;

              // start new thread for copy
              second_thread = new Thread (this, "Move thread");
              second_thread.start ();
            }
          }
        }
        catch (CDException e)
        {
          SwingUtils.ExceptionHandler (e);
          ret_val = false;
        }
      }

      else if (command.equals ("delete_data_source"))
      {
        dir = new File (arg_tokens.nextToken ());
        try
        {
          if (arg_tokens.hasMoreTokens ())
          {
            year = new Integer (Integer.parseInt (arg_tokens.nextToken ()));
          }

          dirArray = getDirArray (dir, year);

          // check that deletion is really wanted
          // build a list of the directories that will be deleted
          str = dirArray [0].getAbsolutePath () + "\n";
          for (count = 1; count < dirArray.length; count ++)
            str += dirArray [count].getAbsolutePath () + "\n";
          choice = JOptionPane.showConfirmDialog (parent_component,
                                                  "Do you want to delete these directories:\n" + str,
                                                  "Confirm deletion", JOptionPane.YES_NO_OPTION);
          if (choice == JOptionPane.YES_OPTION)
          {
            if (dirArray != null)
            {
              // set the flag to indicate what second thread does
              action = DELETE;
              // start new thread for copy
              second_thread = new Thread (this, "Delete thread");
              second_thread.start ();
            }
          }
        }
        catch (CDException e)
        {
          SwingUtils.ExceptionHandler (e);
          ret_val = false;
        }
      }
    
      else if (command.equals ("raise_explorer_dialog"))
      {
        if (! SwingUtils.isFrameDisplayable(explorer_dialog))
        {
          explorer_dialog = null;   // release memory before creating a new dialog
          explorer_dialog = new Explorer ();
          explorer_dialog.addComponentListener (this);
          explorer_dialog.setLocationRelativeTo (parent_component);
          try
          {
            str = (String) GlobalObjects.configuration.get ("ExplorerSizeWidth");
            width = Integer.parseInt (str);
            str = (String) GlobalObjects.configuration.get ("ExplorerSizeHeight");
            height = Integer.parseInt (str);
            if (width >= Explorer.MIN_WIDTH && height >= Explorer.MIN_HEIGHT) explorer_dialog.setSize(width, height);
          }
          catch (NullPointerException e) { }
          catch (NumberFormatException e) { }
        }
        explorer_dialog.setVisible (true);
      }

      else if (command.equals ("raise_memory_view_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable(memory_view_dialog))
        {
          if (parent_dialog != null)
            memory_view_dialog = new MemoryViewDialog (parent_dialog, false);
          else
            memory_view_dialog = new MemoryViewDialog (parent_frame, false);
          memory_view_dialog.addComponentListener (this);
          memory_view_dialog.setLocationRelativeTo (parent_component);
        }
        memory_view_dialog.setVisible (true);
      }

      else if (command.equals ("raise_config_view_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable(config_view_dialog))
        {
          if (parent_dialog != null)
            config_view_dialog = new CtryObsyListViewerDialog (parent_dialog, false);
          else
            config_view_dialog = new CtryObsyListViewerDialog (parent_frame, false);
          config_view_dialog.addComponentListener (this);
          config_view_dialog.setLocationRelativeTo (parent_component);
        }
        config_view_dialog.setVisible (true);
      }
      
      else if (command.equals ("raise_export_options_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable(export_options_dialog))
        {
          if (parent_dialog != null)
            export_options_dialog = new ExportOptionsDialog (parent_dialog, false);
          else
            export_options_dialog = new ExportOptionsDialog (parent_frame, false);
          export_options_dialog.setLocationRelativeTo (parent_component);
        }
        export_options_dialog.setVisible (true);
      }
      else if (command.equals("raise export agreement dialog"))
      {
        if (parent_dialog != null)
          newExporterDialog = new NewExporterDialog(parent_dialog,false,"DataAcknowledgementAgreement",
                                                                               "ShowExportAgreementCheckBox");
        else
          newExporterDialog = new NewExporterDialog(parent_frame,false,"DataAcknowledgementAgreement",
                                                                               "ShowExportAgreementCheckBox");
        newExporterDialog.setVisible(true);
      }        
      else if (command.equals ("raise_export_dialog"))
      {
        if (GlobalObjects.configuration.getProperty("ShowExportAgreementCheckBox") == null ||
            GlobalObjects.configuration.getProperty("ShowExportAgreementCheckBox").contains("Y"))
        {
          if (parent_dialog != null)
            newExporterDialog = new NewExporterDialog(parent_dialog,true,"DataAcknowledgementAgreement",
                                                                                   "ShowExportAgreementCheckBox");
          else
            newExporterDialog = new NewExporterDialog(parent_frame,true,"DataAcknowledgementAgreement",
                                                                                   "ShowExportAgreementCheckBox");
          newExporterDialog.setVisible(true);
        }
        if(GlobalObjects.configuration.getProperty("DataAcknowledgementAgreement").equalsIgnoreCase("Accepted"))
        {
          // extract the parameters
          token_array = new String [arg_tokens.countTokens()];
          for (count=0; count<token_array.length; count++) token_array [count] = arg_tokens.nextToken();
          switch (token_array.length)
          {
            case 2:
              obsy_code = null;
              yr = -1;
              mon = -1;
              use_zip = Boolean.valueOf (token_array[0]).booleanValue();
              use_plain = Boolean.valueOf (token_array[1]).booleanValue();
              base_dir = null;
              break;
            case 3:
              obsy_code = token_array [0];
              yr = mon = -1;
              use_zip = Boolean.valueOf (token_array[1]).booleanValue();
              use_plain = Boolean.valueOf (token_array[2]).booleanValue();
              base_dir = null;
              break;
            case 5:
              obsy_code = token_array [0];
              yr = Integer.parseInt (token_array [1]);
              mon = Integer.parseInt (token_array [2]);
              use_zip = Boolean.valueOf (token_array[3]).booleanValue();
              use_plain = Boolean.valueOf (token_array[4]).booleanValue();
              base_dir = null;
              break;
            case 6:
              obsy_code = token_array [0];
              yr = Integer.parseInt (token_array [1]);
              mon = Integer.parseInt (token_array [2]);
              use_zip = Boolean.valueOf (token_array[3]).booleanValue();
              use_plain = Boolean.valueOf (token_array[4]).booleanValue();
              base_dir = token_array [5];
              break;
            default:
              throw new NoSuchElementException ();
          }
          
          // if the dialog is already displayed, then set its parameters
          if (SwingUtils.isFrameDisplayable (export_dialog) )
          {
            export_dialog.setUseZipAndPlain(use_zip, use_plain);
            export_dialog.setBaseDir(base_dir);
            if (obsy_code != null) export_dialog.setObservatory(obsy_code);
            if (yr >= 0) export_dialog.setDate (yr, mon);
          }
          else
          {
            // create a new dialog
            export_dialog = new ExportDialog (obsy_code, yr, mon,
                                              use_zip, use_plain, base_dir);
            export_dialog.setLocationRelativeTo (parent_component);
          }
          export_dialog.setVisible (true);
        }
      }
      
      else if (command.equals ("raise_data_options_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable (data_options_dialog))
        {
          if (parent_dialog != null)
            data_options_dialog = new DataOptionsDialog(parent_dialog, false);
          else
            data_options_dialog = new DataOptionsDialog(parent_frame, false);
          data_options_dialog.setLocationRelativeTo (parent_component);
        }
        data_options_dialog.setVisible (true);
      }

      else if (command.equals ("raise_catalogue_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable (catalogue_dialog))
        {
          if (parent_dialog != null)
            catalogue_dialog = new CatalogueDialog (parent_dialog, false);
          else
            catalogue_dialog = new CatalogueDialog (parent_frame, false);
          catalogue_dialog.setLocationRelativeTo (parent_component);
        }
        catalogue_dialog.setVisible (true);
      }
      
      else if (command.equals ("raise_plot_options_dialog"))
      {
          if (! SwingUtils.isDialogDisplayable (plot_options_dialog))
          {
            if (parent_dialog != null)
              plot_options_dialog = new PlotOptionsDialog (parent_dialog, false);
            else
              plot_options_dialog = new PlotOptionsDialog (parent_frame, false);
            plot_options_dialog.setLocationRelativeTo (parent_component);
          }
          plot_options_dialog.setVisible (true);
      }
      
      else if (command.equals ("new_plot_options"))
      {
        purgeDataViewerList ();
        for (count=0; count<data_viewer_list.size(); count++)
        {
          data_viewer = (DataViewer) data_viewer_list.get (count);
          try
          {
            data_viewer.updateDisplay (false, true);
          }
          catch (DataViewerException e)
          {
            JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
          }
        }
        for (count=0; count<annual_mean_viewer_list.size(); count++)
        {
          annual_mean_viewer = (AnnualMeanViewer) annual_mean_viewer_list.get (count);
          try
          {
            annual_mean_viewer.updateDisplay (false);
          }
          catch (AnnualMeanViewerException e)
          {
            JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
      
      else if (command.equals ("zoom_plot_in"))
      {
          if (SwingUtils.isDialogDisplayable (plot_options_dialog)) plot_options_dialog.ZoomWithDialog(-1, true);
          else PlotOptionsDialog.ZoomWithoutDialog (-1, true);
      }

      else if (command.equals ("zoom_plot_out"))
      {
          if (SwingUtils.isDialogDisplayable (plot_options_dialog)) plot_options_dialog.ZoomWithDialog (1, true);
          else PlotOptionsDialog.ZoomWithoutDialog (1, true);
      }

      else if (command.equals ("zoom_plot"))
      {
          index = Integer.parseInt (arg_tokens.nextToken ());
          if (SwingUtils.isDialogDisplayable (plot_options_dialog)) plot_options_dialog.ZoomWithDialog (index, false);
          else PlotOptionsDialog.ZoomWithoutDialog (index, false);
      }

      else if (command.equals("import_data"))
      {
          if (import_wizard == null) flag = true;
          else if (SwingUtils.isDialogDisplayable(import_wizard.getWizardDialog())) flag = false;
          else flag = true;
          if (flag)
          {
              import_wizard = new ImportWizard ();
              import_wizard.showNonModal();
          }
          else import_wizard.getWizardDialog().setVisible(true);
      }
      
      else if (command.equals ("reload_data_from_source"))
      {
        try
        {
          GlobalObjects.database_list.reload ();
        }
        catch (CDException e)
        {
          JOptionPane.showMessageDialog (parent_component, e.toString(), "Error Reloading Data", JOptionPane.ERROR_MESSAGE);
        }
        if (SwingUtils.isFrameDisplayable (explorer_dialog)) explorer_dialog.reload ();
      }

      else if (command.equals ("raise_program_options_dialog"))
      {
        if (! SwingUtils.isDialogDisplayable(program_options_dialog))
        {
          if (parent_dialog != null)
            program_options_dialog = new ProgramOptionsDialog (parent_dialog, false);
          else
            program_options_dialog = new ProgramOptionsDialog (parent_frame, false);
          program_options_dialog.addComponentListener (this);
          program_options_dialog.setLocationRelativeTo (parent_component);
        }
        program_options_dialog.setVisible (true);
      }
      else if (command.equals ("new_program_options"))
      {
          // nothing to do - the font size (currently program options only field)
          // is not updated until the program restarts
      }
      
      else if (command.equals ("show_release_notes"))
      {
          if (GlobalObjects.release_notes_url != null)
          {
              if (parent_dialog != null)
                  new TextViewerDialog (parent_dialog,
                                        GlobalObjects.release_notes_url,
                                        "Release Notes",
                                        GlobalObjects.imag_icon).setVisible(true);
              else
                  new TextViewerDialog (parent_frame,
                                        GlobalObjects.release_notes_url,
                                        "Release Notes",
                                        GlobalObjects.imag_icon).setVisible(true);
          }
      }
      
      else if (command.equals ("annual_mean_viewer"))
      {
        try
        {
          obsy_code = arg_tokens.nextToken ();
          yr = Integer.parseInt (arg_tokens.nextToken ());
          if (arg_tokens.hasMoreTokens())
            base_dir = arg_tokens.nextToken();
          else 
            base_dir = null;
          annual_mean_viewer = new AnnualMeanViewer (obsy_code, yr, base_dir);
          purgeAnnualMeanViewerList ();
          annual_mean_viewer_list.add (annual_mean_viewer);
          annual_mean_viewer.setVisible (true);
        }
        catch (AnnualMeanViewerException e)
        {
          JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
        }
      }

      else if (command.equals ("text_file_viewer"))
      {
        file = new File (arg_tokens.nextToken ());
        str = arg_tokens.nextToken ();
        if (parent_dialog != null)
          text_viewer = new TextViewerDialog (parent_dialog, file, str,
                                              GlobalObjects.imag_icon);
        else
          text_viewer = new TextViewerDialog (parent_frame, file, str,
                                              GlobalObjects.imag_icon);
        text_viewer.setVisible (true);
      }

      else if (command.equals ("text_file_selector"))
      {
        code = Integer.parseInt (arg_tokens.nextToken ());
        obsy_code = arg_tokens.nextToken ();
        yr = Integer.parseInt (arg_tokens.nextToken ());
        if (parent_dialog != null)
          text_selector = new TextFileSelector (parent_dialog, code, obsy_code, yr);
        else
          text_selector = new TextFileSelector (parent_frame, code, obsy_code, yr);
        text_selector.setVisible (true);
      }

      else if (command.equals ("image_file_viewer"))
      {
        file = new File (arg_tokens.nextToken ());
        str = arg_tokens.nextToken ();
        if (parent_dialog != null)
          image_viewer = new ImageViewerDialog (parent_dialog, file, str,
                                                GlobalObjects.imag_icon);
        else
          image_viewer = new ImageViewerDialog (parent_frame, file, str,
                                                GlobalObjects.imag_icon);
        image_viewer.setVisible (true);
      }

      else if (command.equals ("image_file_selector"))
      {
        code = Integer.parseInt (arg_tokens.nextToken ());
        obsy_code = arg_tokens.nextToken ();
        yr = Integer.parseInt (arg_tokens.nextToken ());
        if (parent_dialog != null)
          image_selector = new ImageFileSelector (parent_dialog, code,
                                                  obsy_code, yr);
        else
          image_selector = new ImageFileSelector (parent_frame, code,
                                                  obsy_code, yr);
        image_selector.setVisible (true);
      }

      else if (command.equals ("baseline_viewer"))
      {
        purgeBaselineViewerList();
        baseline_viewer = null;
        obsy_code = arg_tokens.nextToken();
        yr = Integer.parseInt (arg_tokens.nextToken());
        if (arg_tokens.hasMoreTokens())
          base_dir = arg_tokens.nextToken();
        else 
          base_dir = null;
        try
        {
          baseline_viewer = new BLVFrame (obsy_code, yr, base_dir);
          baseline_viewer.setVisible (true);
          baseline_viewer_list.add (baseline_viewer);
        }
        catch(BLVException e)
        {
          JOptionPane.showMessageDialog(parent_component,
                        e.getMessage(),
                        "Error Message", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
        }
      }
      
      else if (command.equals ("k_index_viewer"))
      {
          obsy_code = arg_tokens.nextToken ();
          yr = Integer.parseInt (arg_tokens.nextToken ());
          use_zip = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          use_plain = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          if (arg_tokens.hasMoreTokens())
            base_dir = arg_tokens.nextToken();
          else 
            base_dir = null;
          if (parent_dialog != null)
            k_index_viewer = new KIndexViewer (parent_dialog,
                                               obsy_code, yr, use_zip, use_plain, base_dir);
          else
            k_index_viewer = new KIndexViewer (parent_frame,
                                               obsy_code, yr, use_zip, use_plain, base_dir);
          k_index_viewer.setVisible (true);
      }
      
      else if (command.equals ("mean_viewer"))
      {
          obsy_code = arg_tokens.nextToken ();
          yr = Integer.parseInt (arg_tokens.nextToken ());
          use_zip = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          use_plain = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          if (arg_tokens.hasMoreTokens())
            base_dir = arg_tokens.nextToken();
          else 
            base_dir = null;
          if (parent_dialog != null)
            mean_viewer = new AnnualMeanCheckDialog (parent_dialog,
                                                     false, obsy_code, yr,
                                                     use_zip, use_plain, base_dir);
          else
            mean_viewer = new AnnualMeanCheckDialog (parent_frame,
                                                     false, obsy_code, yr,
                                                     use_zip, use_plain, base_dir);
          mean_viewer.setVisible (true);
      }
      
      else if (command.equals ("view_data"))
      {
        try
        {
          obsy_code = arg_tokens.nextToken ();
          yr = Integer.parseInt (arg_tokens.nextToken ());
          mon = Integer.parseInt (arg_tokens.nextToken ());
          use_zip = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          use_plain = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          if (arg_tokens.hasMoreTokens())
            base_dir = arg_tokens.nextToken();
          else 
            base_dir = null;
          data_viewer = new DataViewer (obsy_code, yr, mon,
                                        use_zip, use_plain, base_dir);
          purgeDataViewerList ();
          data_viewer_list.add (data_viewer);
          data_viewer.setVisible (true);
        }
        catch (DataViewerException e)
        {
          JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
        }
      }
      
      else if (command.equals("view_binary"))
      {
          obsy_code = arg_tokens.nextToken ();
          yr = Integer.parseInt (arg_tokens.nextToken ());
          mon = Integer.parseInt (arg_tokens.nextToken ());
          use_zip = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          use_plain = Boolean.valueOf (arg_tokens.nextToken ()).booleanValue();
          if (arg_tokens.hasMoreTokens())
            base_dir = arg_tokens.nextToken();
          else 
            base_dir = null;
          data_month_list = CDMisc.findData (obsy_code, yr, mon, use_zip, use_plain, base_dir);
          if (data_month_list[0] == null)
              JOptionPane.showMessageDialog(parent_component, "Can't find data file", "Error loading data", JOptionPane.ERROR_MESSAGE);
          else
          {
              try
              {
                  if (parent_dialog != null)
                    new ImagCDFileEditorDialog (parent_dialog,
                                                false,
                                                data_month_list[0].getMonthFile(),
                                                false);
                  else
                    new ImagCDFileEditorDialog (parent_frame,
                                                false,
                                                data_month_list[0].getMonthFile(),
                                                false);
              }
              catch (FileLoadException e)
              {
                  JOptionPane.showMessageDialog(parent_component, "Can't load data file", "Error loading data", JOptionPane.ERROR_MESSAGE);
              }
          }
      }
      
      else if (command.equals("search_for_obsy"))
      {
          obsy_code = arg_tokens.nextToken();
          if (! GlobalObjects.top_frame.showObsyPopup (obsy_code))
              JOptionPane.showMessageDialog (GlobalObjects.top_frame, 
                                             "Can't find observatory " + obsy_code,
                                             "Error", JOptionPane.ERROR_MESSAGE);
      }

      else if (command.equals ("help_about"))
      {
          if (parent_dialog != null)
              new AboutDialog (parent_dialog, false, this).setVisible(true);
          else
              new AboutDialog (parent_frame, false, this).setVisible(true);
      }
      
      else if (command.equals ("program_exit"))
      {
        // if there is an import wizard running, shut it down
        if (import_wizard != null) import_wizard.closeWizard();
        // save the configuration
        GlobalObjects.writeConfiguration ();
        // exit
        System.exit (0);
      }
    
      else 
      {
          ret_val = false;
          JOptionPane.showMessageDialog(parent_component, "Unrecognised command: " + command, "Internal Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    catch (NoSuchElementException e)    
    {
        ret_val = false; 
        JOptionPane.showMessageDialog(parent_component, "Badly formed command line: " + args, "Internal Error", JOptionPane.ERROR_MESSAGE);
    }
    catch (NumberFormatException e)     
    { 
        ret_val = false; 
        JOptionPane.showMessageDialog(parent_component, "Badly formed command line: " + args, "Internal Error", JOptionPane.ERROR_MESSAGE);
    }
    catch (IndexOutOfBoundsException e) 
    {
        ret_val = false; 
        JOptionPane.showMessageDialog(parent_component, "Badly formed command line: " + args, "Internal Error", JOptionPane.ERROR_MESSAGE);
    }
    catch (ImageViewerException e)
    {
        ret_val = false; 
        JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading image", JOptionPane.ERROR_MESSAGE);
    }
    catch (TextViewerException e)
    {
        ret_val = false; 
        JOptionPane.showMessageDialog(parent_component, e.getMessage(), "Error loading text file", JOptionPane.ERROR_MESSAGE);
    }

    // set the default cursor
    if (old_cursor != null)
    {
        if (parent_dialog != null)
            parent_dialog.setCursor(old_cursor);
        else
            parent_frame.setCursor(old_cursor);
    }
    
    return ret_val;
  }

  /** catch action events for other objects - the actions must have an
   * ActionCommand string set
   * @param e the object that describes the event */
  public void actionPerformed(ActionEvent e)
  {
    interpretCommand (e.getActionCommand ());
  }

  /** run method for the second thread - this is used to carry out
   * copying using the method copyDirStructure and deletion using
   * the method deleteDirStructure. Both of these methods take care
   * of a ProgressDialog which can be used to cancel the copy/delete.
   * This method joins the threads once the copy/delete has completed.
   */
  public void run ()
  {
    Thread executing_thread;
    int n;
    ProgressDialog progress;

    // work out which thread to start
    executing_thread = Thread.currentThread();
    progress = null;
    if (executing_thread == second_thread)
    {
      if (action == COPY)
      {
        progress = new ProgressDialog (GlobalObjects.top_frame,
                                       "Copy progress",
                                       "Copying " + dirArray [0].getAbsolutePath () + 
                                       " to " + destDir.getAbsolutePath (),
                                       null, null, false);
        progress.setLocationRelativeTo (GlobalObjects.top_frame);
        progress.setVisible (true);

        for (n = 0; n < dirArray.length; n++)
          status = copyDirStructure (dirArray [n], destDir, progress);
      }
      else if (action == MOVE)
      {
        progress = new ProgressDialog (GlobalObjects.top_frame,
                                       "Move progress",
                                       "Copying " + dirArray [0].getAbsolutePath () + 
                                       " to " + destDir.getAbsolutePath (),
                                       null, null, false);
        progress.setLocationRelativeTo (GlobalObjects.top_frame);
        progress.setVisible (true);
        
        for (n = 0; n < dirArray.length; n++)
        {
          status = copyDirStructure (dirArray [n], destDir, progress);
          // only delete if copy has completed successfully
          if (status) status = deleteDirStructure (dirArray [n], progress);
        }
      }
      else if (action == DELETE)
      {
        progress = new ProgressDialog (GlobalObjects.top_frame,
                                       "Deletion progress",
                                       "Deleting " + dirArray [0].getAbsolutePath (),
                                       null, null, false);
        progress.setLocationRelativeTo (GlobalObjects.top_frame);
        progress.setVisible (true);

        for (n = 0; n < dirArray.length; n++)
        {
          status = deleteDirStructure (dirArray [n], progress);
        }
      }

      // the copy or delete has now finished - hide
      // the progress dialog and end the second thread
      try
      {
        if (progress != null) progress.setVisible (false);
        progress = null;
        if (second_thread != null) second_thread.join (2000);
      }
      catch (InterruptedException e)
      {
      }

      // force a database reload when deletion or move has completed
      if (status && (action == MOVE || action == DELETE))
        this.interpretCommand ("reload_data_from_source");
    }
  }
  
  /** Called when a list of data sources is chosen
   * @param list the new list of data sources  */
  public void dataSourcesChosen(CDDatabaseList list) 
  {
    GlobalObjects.database_list = list;
    if (SwingUtils.isFrameDisplayable (explorer_dialog)) explorer_dialog.reload ();
  }


  /**
   * copyDirStructure - Copy a directory structure and all files
   * from sourceDir to destDir. This method is run from a separate
   * thread and uses a ProgressDialog to display status and enable
   * a user to stop the copy. This method is called recursively to
   * copy the contents of each sub-directory. If there is an error
   * at any stage of the recursion, false will be returned by the
   * outside loop of the recursion.
   *
   * @param sourceDir the source directory as a File object
   * @param destDir the destination directory as a File object
   * @param progress the progress dialog
   * @return true if successful
   */
  private boolean copyDirStructure (File sourceDir, File destDir, ProgressDialog progress)
  {
    File [] fileList;
    File file, newDir;
    int fileCount, n;

    if (sourceDir == null || destDir == null) return false;

    if (sourceDir.isDirectory ())
    {
      // make a directory in the destination dir with same name as source dir
      newDir = new File (destDir, sourceDir.getName ());
      if (newDir.exists ())
      {
        JOptionPane.showMessageDialog (GlobalObjects.top_frame,
                                       "Directory " + newDir.getAbsolutePath () + " already exists",
                                       "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      progress.update ("Making directory " + newDir.getAbsolutePath ());
      if (!newDir.mkdir ())
      {
        JOptionPane.showMessageDialog (GlobalObjects.top_frame,
                                       "Error creating directory " + newDir.getAbsolutePath (),
                                       "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

      // go through contents of directory and make new directory structure
      fileList = sourceDir.listFiles ();
      fileCount = fileList.length;
    
      for (n = 0; n < fileCount; n ++)
      {
        file = fileList [n];
        if (file.isDirectory ())
          status = copyDirStructure (file, newDir, progress);
        else if (file.isFile ())
          status = copyFile (file, newDir, progress);
        if (!status) return false;
      }
    }
    else return copyFile (sourceDir, destDir, progress);
    
    // check if progress has been cancelled
    if (progress.isCancelled ()) return false;
 
    return true;
  }

  /** copyFile - Copy a file to the destination directory. This
   * method updates a progress dialog.
   *
   * @param sourceFile the source file as a File object
   * @param destDir the destination directory as a File object
   * @param progress the progress dialog
   * @return true if successful
   */
  private boolean copyFile (File sourceFile, File destDir, ProgressDialog progress)
  {
    int count;
    DataInputStream inputStream;
    FileOutputStream outputStream;
    File destFile;
    byte [] buffer = new byte [8192];
    boolean eof = false;

    destFile = new File (destDir, sourceFile.getName ());
    progress.update ("Copying file " + sourceFile.getAbsolutePath () +
                     " to " + destDir.getAbsolutePath ());
    try
    {
      inputStream = new DataInputStream (new FileInputStream (sourceFile));
      outputStream = new FileOutputStream (destFile);
    }
    catch (FileNotFoundException e)
    {
      JOptionPane.showMessageDialog (GlobalObjects.top_frame,
                                     "Error copying file " + sourceFile.getAbsolutePath (),
                                     "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // read bytes of the source file into buffer and write to the destination file
    try
    {
      for (count = inputStream.read (buffer); count >= 0; count = inputStream.read (buffer))
      {
        // check if progress has been cancelled
        if (progress.isCancelled ()) return false;
        
        // write to output stream
        outputStream.write (buffer, 0, count);
      }

      // close streams
      inputStream.close ();
      outputStream.flush ();
      outputStream.close ();
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog (GlobalObjects.top_frame,
                                     "Error copying file " + sourceFile.getAbsolutePath (),
                                     "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  /**
   * delete a directory structure
   *
   * @param dir the directory to delete
   * @param progress the progress dialog
   * @return true if successful
   */
  private boolean deleteDirStructure (File dir, ProgressDialog progress)
  {
    File [] fileList;
    File file;
    int fileCount, n;

    if (dir == null) return false;

    if (dir.isDirectory ())
    {
      // go through contents of directory and delete
      fileList = dir.listFiles ();
      fileCount = fileList.length;

      for (n = 0; n < fileCount; n ++)
      {
        file = fileList [n];
        if (file.isDirectory ())
          status = deleteDirStructure (file, progress);
        else if (file.isFile ())
        {
          progress.update ("Deleting file " + file.getAbsolutePath ());
          status = file.delete ();
        }
        if (!status)
        {
          JOptionPane.showMessageDialog (GlobalObjects.top_frame,
                                         "Error deleting " + file.getAbsolutePath (),
                                         "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
    }

    // delete the directory
    progress.update ("Deleting directory " + dir.getAbsolutePath ());
    dir.delete ();

    // check if progress has been cancelled
    if (progress.isCancelled ()) return false;

    return true;
  }

  /* getDirArray - get an array of directories which contain the path
   *               for the given base directory and year. If year is
   *               null, the array will contain all the databases
   *               in the given base directory
   * @param base_dir the base directory for the database
   * @param year the year for the database or null if all years in this
   *             directory are required
   * @return an array of File objects containing the path to each database
   */
  private File [] getDirArray (File base_dir, Integer year) throws CDException
  {
    int count, count2;
    CDDatabase database;
    File [] dirList = null;

    // go through the databases until we find the path to the
    // database with the right base directory and year
    for (count = 0; count < GlobalObjects.database_list.GetNDatabases (); count ++)
    {
      database = GlobalObjects.database_list.GetDatabase (count);
      if (database.GetBaseDir ().equals (base_dir))
      {
        // no year passed - this is a copy of all
        // the databases in this directory
        if (year == null)
        {
          dirList = new File [database.GetNYears ()];
        }
        else dirList = new File [1];

        // make a list of base dirs for copy
        for (count2 = 0; count2 < database.GetNYears (); count2 ++)
        {
          if (year == null)
          {
            dirList [count2] = database.GetYear (count2).GetBaseDir ();
          }
          else if (database.GetYear (count2).GetYear () == year.intValue ())
          {
            dirList [0] = database.GetYear (count2).GetBaseDir ();
            break;
          }
        }
      }
    }
    return dirList;
  }
  
  public void componentHidden(ComponentEvent e) { }
  public void componentMoved(ComponentEvent e) { }
  public void componentShown(ComponentEvent e) { }
  public void componentResized(ComponentEvent e) 
  {
    if (e.getSource().equals(explorer_dialog))
    {
      GlobalObjects.configuration.put ("ExplorerSizeWidth", Integer.toString (e.getComponent().getWidth()));
      GlobalObjects.configuration.put ("ExplorerSizeHeight", Integer.toString (e.getComponent().getHeight()));
    }
  }

  private void purgeDataViewerList ()
  {
      int count;
      DataViewer data_viewer;
      
      for (count=0; count<data_viewer_list.size(); count++)
      {
        data_viewer = (DataViewer) data_viewer_list.get (count);
        if (! SwingUtils.isFrameDisplayable(data_viewer))
        {
          data_viewer_list.removeElementAt(count);
          count --;
        }
      }
  }

  private void purgeAnnualMeanViewerList ()
  {
      int count;
      AnnualMeanViewer annual_mean_viewer;
      
      for (count=0; count<annual_mean_viewer_list.size(); count++)
      {
        annual_mean_viewer = (AnnualMeanViewer) annual_mean_viewer_list.get (count);
        if (! SwingUtils.isFrameDisplayable(annual_mean_viewer))
        {
          annual_mean_viewer_list.removeElementAt(count);
          count --;
        }
      }
  }

  private void purgeBaselineViewerList ()
  {
      int count;
      BLVFrame baseline_viewer;
      
      for (count=0; count<baseline_viewer_list.size(); count++)
      {
        baseline_viewer = (BLVFrame) baseline_viewer_list.get (count);
        if (! SwingUtils.isFrameDisplayable(baseline_viewer))
        {
          baseline_viewer_list.removeElementAt(count);
          count --;
        }
      }
  }
  
  private Frame findParentFrame (LookAheadStringTokenizer tokens)
  {
      Frame frame;
      String name, fdv_prefix, famv_prefix, fbv_prefix;
      int id, count;
      DataViewer data_viewer;
      AnnualMeanViewer annual_mean_viewer;
      BLVFrame baseline_viewer;
      
      frame = null;
      try 
      {
          name = tokens.peekToken(); 

          fdv_prefix = "from_data_viewer_";
          famv_prefix = "from_annual_mean_viewer_";
          fbv_prefix = "from_baseline_viewer_";
          
          if (name.equalsIgnoreCase ("from_main_window"))
              frame = GlobalObjects.top_frame;
          else if (name.equalsIgnoreCase ("from_export_window"))
              frame = export_dialog;
          else if (name.equalsIgnoreCase ("from_explorer_window"))
              frame = explorer_dialog;
          else if (name.startsWith (fdv_prefix))
          {
              purgeDataViewerList();
              id = Integer.parseInt (name.substring(fdv_prefix.length()));
              for (count=0; count<data_viewer_list.size(); count++)
              {
                  data_viewer = data_viewer_list.get (count);
                  if (id == data_viewer.getUniqueID())
                      frame = data_viewer;
              }
          }
          else if (name.startsWith (famv_prefix))
          {
              purgeAnnualMeanViewerList();
              id = Integer.parseInt (name.substring(famv_prefix.length()));
              for (count=0; count<annual_mean_viewer_list.size(); count++)
              {
                  annual_mean_viewer = annual_mean_viewer_list.get (count);
                  if (id == annual_mean_viewer.getUniqueID())
                      frame = annual_mean_viewer;
              }
          }
          else if (name.startsWith (fbv_prefix))
          {
              purgeBaselineViewerList();
              id = Integer.parseInt (name.substring(fbv_prefix.length()));
              for (count=0; count<baseline_viewer_list.size(); count++)
              {
                  baseline_viewer = baseline_viewer_list.get (count);
                  if (id == baseline_viewer.getUniqueID())
                      frame = baseline_viewer;
              }
          }
      }
      catch (NoSuchElementException e) { }

      if (frame != null) 
      {
          tokens.nextToken();
          if (! SwingUtils.isFrameDisplayable(frame)) frame = null;
      }
      return frame;
  }
  
  private Dialog findParentDialog (LookAheadStringTokenizer tokens)
  {
      Dialog dialog;
      String name;
      
      dialog = null;
      try 
      {
          name = tokens.peekToken(); 

          if (name.equalsIgnoreCase ("from_catalog_window"))
              dialog = catalogue_dialog;
          else if (name.equalsIgnoreCase ("from_data_options_window"))
              dialog = data_options_dialog;
          else if (name.equalsIgnoreCase ("from_data_source_window"))
              dialog = data_source_dialog;
      }
      catch (NoSuchElementException e) { }
      
      if (dialog != null)
      {
          tokens.nextToken();
          if (! SwingUtils.isDialogDisplayable(dialog)) dialog = null;
      }
      
      return dialog;
  }
    
}

