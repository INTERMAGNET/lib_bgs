/*
 * TextFileSelector.java
 *
 * Created on 14 February 2003, 11:41
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.swing.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * Dialog to display contents of a text file with buttons to select
 * the year for the file.
 *
 * @author  fmc
 */
public class TextFileSelector extends JDialog
implements ActionListener, WindowListener
{
  private Component owner;
  private TextViewerPanel text_panel;
  private JButton prev_button, next_button, print_button, close_button, save_button;
  private JComboBox select_year;
  private Vector year_list;
  private int n_years, file_type_code, current_year_index, disp_year_index;
  private String obsy_code;
  private File textfile;

 /********************************************************************
  * creates a new TextFileSelector dialog.
  *
  * @param owner - the owner of this dialog
  * @param file_type_code - the type of file to be displayed. This
  *        code comes from the CDObservatory object.
  * @param obsy_code - the observatory code for the file required
  * @param current_year - the year for the file required
  ********************************************************************/
  public TextFileSelector (Frame owner, int file_type_code,
                           String obsy_code, int current_year)
  {
    super (owner, "Text Viwer");

    this.file_type_code = file_type_code;
    this.obsy_code = obsy_code;

    // display components
    createComponents (current_year);
  }

 /********************************************************************
  * creates a new TextFileSelector dialog.
  *
  * @param owner - the owner of this dialog
  * @param file_type_code - the type of file to be displayed. This
  *        code comes from the CDObservatory object.
  * @param obsy_code - the observatory code for the file required
  * @param current_year - the year for the file required
  ********************************************************************/
  public TextFileSelector (Dialog owner, int file_type_code,
                           String obsy_code, int current_year)
  {
    super (owner, "Text Viwer");

    this.file_type_code = file_type_code;
    this.obsy_code = obsy_code;

    // display components
    createComponents (current_year);
  }
  
  /** set up the components */
  private void createComponents (int current_year)
  {
    Container content_pane;
    JScrollPane scroll_pane;
    JPanel control_panel, select_panel, close_panel;
    int n;

    this.setIconImage(GlobalObjects.imag_icon.getImage());

    // make list of available years for this observatory
    year_list = CDMisc.makeYearList (obsy_code);
    n_years = year_list.size ();

    // set the current year index to be the most recent year or
    // the specified year
    current_year_index = -1;
    disp_year_index = -2;
    for (n = 0; n < n_years; n++)
    {
      if (((Integer)year_list.elementAt (n)).intValue () == current_year)
        current_year_index = n;
    }
    if (current_year_index < 0) current_year_index = n_years - 1;

    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout(new BorderLayout ());

    // create empty text panel
    text_panel = new TextViewerPanel ();

    // create scrolling pane for text area
    scroll_pane = new JScrollPane (text_panel);
    scroll_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll_pane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    content_pane.add ("Center", scroll_pane);

    // add panels for buttons
    control_panel = new JPanel();
    content_pane.add ("South", control_panel);

    // set layout and add more panels for buttons
    control_panel.setLayout (new GridLayout (2, 1));

    select_panel = new JPanel ();
    select_panel.setLayout (new GridLayout (1, 5, 5, 5));
    control_panel.add (select_panel);

    close_panel = new JPanel ();
    control_panel.add (close_panel);

    // add control buttons
    select_panel.add (new JLabel (""));
    prev_button = new JButton ("Previous Year");
    prev_button.addActionListener (this);
    select_panel.add (prev_button);

    select_year = new JComboBox ();
    for (n = 0; n < n_years; n++)
      select_year.addItem (year_list.elementAt (n).toString ());
    select_year.addActionListener (this);
    select_panel.add (select_year);

    next_button = new JButton ("Next Year");
    next_button.addActionListener (this);
    select_panel.add (next_button);
    select_panel.add (new JLabel (""));

    // add print button
    print_button = new JButton ("Print");
    print_button.addActionListener (this);
    close_panel.add (print_button);

    // add save button
    save_button = new JButton ("Save");
    save_button.addActionListener (this);
    close_panel.add (save_button);

    // add close button
    close_button = new JButton ("Close");
    close_button.addActionListener (this);
    close_panel.add (close_button);
    getRootPane().setDefaultButton (close_button);
    
    updateDisplay ();
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    this.pack ();
    
    // make dialog slightly shorter than pack height so that
    // close button is not hidden behind toolbar
    this.setSize (this.getWidth (), this.getHeight () - 50);
    this.setVisible (true);
  }

  // listener for action events
  public void actionPerformed (ActionEvent actionEvent)
  {
    Object source;
    DocPrintJob print_job;
    DocAttributeSet doc_attr_set;
    Doc document;
    PrintService print_service;

    source = actionEvent.getSource();
    if (source == close_button)
    {
      closeDialog (null);
    }
    else if (source == print_button)
    {
      GlobalObjects.print_canvas.doPrint (text_panel);
    }
    else if (source == save_button)
    {
      GlobalObjects.querySaveFile (this, textfile);
    }
    else if (source == prev_button)
    {
      current_year_index --;
      if (current_year_index < 0) current_year_index = 0;
    }
    else if (source == next_button)
    {
      current_year_index ++;
      if (current_year_index > n_years - 1) current_year_index = n_years - 1;
    }
    else if (source == select_year)
    {
      current_year_index = select_year.getSelectedIndex ();
    }

    updateDisplay ();
  }

 /********************************************************************
  * updateDisplay - update the display after a new date is
  * selected
  ********************************************************************/
  private void updateDisplay ()
  {
    int n, current_year;
    CDObservatory observatory;
    Vector obsy_list;
    Cursor old_cursor;

    // check that year has changed
    if (current_year_index != disp_year_index)
    {
      current_year = ((Integer)year_list.elementAt (current_year_index)).intValue ();

      // get list of observatories for this year
      obsy_list = CDMisc.getObservatoryList (obsy_code, current_year);
      if (obsy_list == null)
      {
          observatory = null;
          textfile = null;
      }
      else
      {
        // since files are in all segments, take first observatory object
        observatory = (CDObservatory) obsy_list.elementAt (0);
        textfile = observatory.GetFile (file_type_code);
      }
      
      // enable and disable components according to the year selected
      if (current_year_index == 0) prev_button.setEnabled (false);
      else prev_button.setEnabled (true);
      if (current_year_index == n_years - 1) next_button.setEnabled (false);
      else next_button.setEnabled (true);

      // set year displayed in drop-down list - remove action listener first
      select_year.removeActionListener (this);
      select_year.setSelectedIndex (current_year_index);
      select_year.addActionListener (this);

      // show busy cursor
      old_cursor = getCursor ();
      setCursor (GlobalObjects.wait_cursor);

      // load new data
      try { text_panel.update (textfile); }
      catch (TextViewerException e)
      {
        setCursor (old_cursor);
        JOptionPane.showMessageDialog (this, e.getMessage ());
      }

      // reset cursor
      setCursor (old_cursor);

      // set flag for displayed year
      disp_year_index = current_year_index;

      // set dialog title
      switch (file_type_code)
      {
      case CDObservatory.FILE_BLV_TEXT:
        if (observatory == null) this.setTitle ("Text Viewer");
        else this.setTitle ("Text Viewer: Baseline values for " +
                            observatory.GetObservatoryName () + " " + current_year);
        break;
      case CDObservatory.FILE_DKA:
        if (observatory == null) this.setTitle ("Text Viewer");
        else this.setTitle ("Text Viewer: K index text file for " +
                            observatory.GetObservatoryName () + " " + current_year);
        break;
      case CDObservatory.FILE_COUNTRY_README:
        if (observatory == null || observatory.GetCountry () == null) this.setTitle ("Text Viewer");
        else this.setTitle ("Text Viewer: Country information for " +
                            observatory.GetCountry().country_name + " " + current_year);
        break;
      case CDObservatory.FILE_YEAR_MEAN:
        if (observatory == null) this.setTitle ("Text Viewer");
        else this.setTitle ("Text Viewer: Annual means for " +
                            observatory.GetObservatoryName () + " " + current_year);
        break;
      case CDObservatory.FILE_README:
        if (observatory == null) this.setTitle ("Text Viewer");
        else this.setTitle ("Text Viewer: Observatory information for " +
                            observatory.GetObservatoryName () + " " + current_year);
        break;
      default:
        this.setTitle ("Text Viewer");
        break;
      }
    }
  }
  
  /* methods for handling window events */
  public void windowActivated (WindowEvent e) { }
  public void windowClosed (WindowEvent e) { }
  public void windowClosing (WindowEvent e) { closeDialog (e); }
  public void windowDeactivated (WindowEvent e) { }
  public void windowDeiconified (WindowEvent e) { }
  public void windowIconified (WindowEvent e) { }
  public void windowOpened (WindowEvent e) { }
  
  private void closeDialog (WindowEvent e)
  {
    setVisible (false);
    dispose ();
  }
}
