/*
 * ImageFileSelector.java
 *
 * Created on 18 February 2003, 12:51
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
 * Class to display an image (including PCX) in a dialog with buttons to select
 * the year for the file.
 *
 * @author  fmc
 */
public class ImageFileSelector extends JDialog
implements ActionListener, WindowListener
{
  private ImageViewerPanel image_panel;
  private JButton prev_button, next_button, print_button, close_button;
  private JComboBox select_year;
  private Vector year_list;
  private int n_years, file_type_code, current_year_index, disp_year_index;
  private String obsy_code;

  // objects used for printing
  private DocFlavor print_doc_flavor;
  private PrintRequestAttributeSet print_request_attr_set;
  private PrintService default_print_service;
  private PrintService [] print_service_array;

 /********************************************************************
  * creates a new ImageFileSelector dialog.
  *
  * @param owner - the owner of this dialog
  * @param file_type_code - the type of file to be displayed. This
  *        code comes from the CDObservatory object.
  * @param obsy_code - the observatory code for the file required
  * @param current_year - the year for the file required
  ********************************************************************/
  public ImageFileSelector (Frame owner, int file_type_code,
                            String obsy_code, int current_year)
  {
    super (owner, "Image Viewer");

    this.file_type_code = file_type_code;
    this.obsy_code = obsy_code;

    // display components
    createComponents (current_year);
  }

 /********************************************************************
  * creates a new ImageFileSelector dialog.
  *
  * @param owner - the owner of this dialog
  * @param file_type_code - the type of file to be displayed. This
  *        code comes from the CDObservatory object.
  * @param obsy_code - the observatory code for the file required
  * @param current_year - the year for the file required
  ********************************************************************/
  public ImageFileSelector (Dialog owner, int file_type_code,
                            String obsy_code, int current_year)
  {
    super (owner, "Image Viewer");

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

    // set up printing
    print_doc_flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    print_request_attr_set = new HashPrintRequestAttributeSet ();
    default_print_service = PrintServiceLookup.lookupDefaultPrintService ();
    print_service_array = PrintServiceLookup.lookupPrintServices (print_doc_flavor, print_request_attr_set);

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

    // create empty image panel
    image_panel = new ImageViewerPanel ();

    // create scroll pane to contain image
    scroll_pane = new JScrollPane (image_panel);
    scroll_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

    // add close button
    close_button = new JButton ("Close");
    close_button.addActionListener (this);
    close_panel.add (close_button);
    getRootPane().setDefaultButton (close_button);
    
    updateDisplay ();
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    this.pack ();
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
      print_service = ServiceUI.printDialog (null, 200, 200, print_service_array, default_print_service, print_doc_flavor, print_request_attr_set);
      if (print_service != null)
      {
        print_job = print_service.createPrintJob();
        doc_attr_set = new HashDocAttributeSet();
        document = new SimpleDoc (image_panel, print_doc_flavor, doc_attr_set);
        try 
        {
          print_job.print (document, print_request_attr_set); 
          default_print_service = print_service;
        }
        catch (PrintException pe)
        {
          JOptionPane.showMessageDialog (this, pe.toString ());
        }
      }
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
    File imagefile;
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
          imagefile = null;
      }
      else
      {
        // since files are in all segments, take first observatory object
        observatory = (CDObservatory) obsy_list.elementAt (0);
        imagefile = observatory.GetFile (file_type_code);
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
      try { image_panel.update (imagefile); }
      catch (ImageViewerException e)
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
      case CDObservatory.FILE_BLV_PLOT:
        if (observatory == null) this.setTitle ("Image Viewer");
        else this.setTitle ("Image Viewer: Baseline value plot for " +
                            observatory.GetObservatoryName () + " " + current_year);
        break;
      case CDObservatory.FILE_COUNTRY_MAP:
        if (observatory == null || observatory.GetCountry () == null) this.setTitle ("Image Viewer");
        else this.setTitle ("Image Viewer: Country map for " +
                            observatory.GetCountry().country_name + " " + current_year);
        break;
      case CDObservatory.FILE_COUNTRY_INFO:
        if (observatory == null || observatory.GetCountry () == null) this.setTitle ("Image Viewer");
        else this.setTitle ("Image Viewer: Institute details for " +
                            observatory.GetCountry().country_name + " " + current_year);
        break;
      default:
        this.setTitle ("Image Viewer");
        break;
      }
    }
  }
  
  public void windowActivated(WindowEvent e) {  }
  public void windowClosed(WindowEvent e) {  }
  public void windowClosing(WindowEvent e)  { closeDialog (e); }
  public void windowDeactivated(WindowEvent e) {  }
  public void windowDeiconified(WindowEvent e) {  }
  public void windowIconified(WindowEvent e) {  }
  public void windowOpened(WindowEvent e) {  }
  
  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) 
  {
    setVisible(false);
    dispose();
  }
  
}