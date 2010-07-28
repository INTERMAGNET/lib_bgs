/*
 * KIndexViewer.java
 *
 * Created on 14 February 2003, 11:41
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.library.Misc.DateUtils;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import java.util.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * Display K indices as a histogram
 *
 * @author  fmc
 */
public class KIndexViewer extends JDialog 
implements ActionListener, WindowListener
{
  // constants
  private static final int N_MONTHS = 12;
  private static final int MAX_N_DAYS = 31;
  private static final int N_K_VALUES = 8;

  // global variables
  private KIndexPlotPanel plot_panel;
  private JButton prev_button, next_button, print_button, close_button;
  private JComboBox select_year;
  private Vector<Integer> year_list;
  private int n_years, current_year_index;
  private Integer [] [] k_indices;
  private String obsy_code;
  private String title;

  // what type of file to use (zip and/or unzip)
  private boolean use_zip;
  private boolean use_plain;
  private String base_dir;

  // objects used for printing
  private DocFlavor print_doc_flavor;
  private PrintRequestAttributeSet print_request_attr_set;
  private PrintService default_print_service;
  private PrintService [] print_service_array;

 /********************************************************************
  * creates a new KIndexViewer dialog.
  *
  * @param obsy_code - the observatory code for the file required
  * @param year - the year for the file required
  * @param use_zip - if true read from zip files, otherwise don't
  * @param use_plain - if true read from non-zip files, otherwise don't
  * @param base_dir - if the viewer should be restricted to one data source
  *        then set this to the path of the data source, otherwise set it to null
  ********************************************************************/
  public KIndexViewer (Frame owner, String obsy_code, int year,
                       boolean use_zip, boolean use_plain,
                       String base_dir)
  {
    super (owner, "K Index Viewer");
    
    title = "K Index Viewer";
    this.obsy_code = obsy_code;
    this.use_zip = use_zip;
    this.use_plain = use_plain;
    
    // display components
    createComponents (year, base_dir);
  }

 /********************************************************************
  * creates a new KIndexViewer dialog.
  *
  * @param obsy_code - the observatory code for the file required
  * @param year - the year for the file required
  * @param use_zip - if true read from zip files, otherwise don't
  * @param use_plain - if true read from non-zip files, otherwise don't
  * @param base_dir - if the viewer should be restricted to one data source
  *        then set this to the path of the data source, otherwise set it to null
  ********************************************************************/
  public KIndexViewer (Dialog owner, String obsy_code, int year,
                       boolean use_zip, boolean use_plain,
                       String base_dir)
  {
    super (owner, "K Index Viewer");
    
    title = "K Index Viewer";
    this.obsy_code = obsy_code;
    this.use_zip = use_zip;
    this.use_plain = use_plain;
    
    // display components
    createComponents (year, base_dir);
  }

  /** set up the components */
  private void createComponents (int year, String base_dir)
  {
    Container content_pane;
    JPanel control_panel, select_panel, close_panel;
    int n;

    int count;
    CDDatabase database;
    
    k_indices = new Integer [N_MONTHS] [MAX_N_DAYS * N_K_VALUES];

    if (base_dir == null)
        database = null;
    else
        database = CDMisc.findDataSource(base_dir);
    if (database == null)
        this.base_dir = null;
    else
        this.base_dir = base_dir;
    
    // set up printing
    print_doc_flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    print_request_attr_set = new HashPrintRequestAttributeSet ();
    default_print_service = PrintServiceLookup.lookupDefaultPrintService ();
    print_service_array = PrintServiceLookup.lookupPrintServices (print_doc_flavor, print_request_attr_set);

    // make list of available years for this observatory
    if (database == null)
        year_list = CDMisc.makeYearList (obsy_code);
    else
        year_list = CDMisc.makeYearList (obsy_code, database);
    n_years = year_list.size ();
    current_year_index = -1;
    for (count=0; count<n_years; count++)
    {
        if (year == year_list.get (count).intValue())
            current_year_index = count;
    }
    if (current_year_index < 0) current_year_index = n_years -1;

    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout(new BorderLayout ());

    // create empty text panel
    plot_panel = new KIndexPlotPanel ();
    plot_panel.setPreferredSize (new Dimension (600, 500));
    content_pane.add ("Center", plot_panel);

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
    getRootPane().setDefaultButton (close_button);

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
    
    updateDisplay ();
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    this.pack ();
    this.setVisible (true);
  }

 /********************************************************************
  * loadData - load data for the year specified by the flag
  *            current_year. The data is loaded into the the array
  *            k_indices
  *
  * @return - true if data was loaded, else false
  ********************************************************************/
  private boolean loadData ()
  {
    int n, i, k, index, current_year;
    CDDataMonth month_data [];
    CDDataDay day_data;
    Integer k_array [], missing_k_array [];
    
    k_array = new Integer [N_K_VALUES];
    missing_k_array = new Integer [N_K_VALUES];
    for (n=0; n<N_K_VALUES; n++) missing_k_array [n] = new Integer (999);

    // get the year
    current_year = year_list.get (current_year_index).intValue();
    
    // retrieve one year of data
    month_data = CDMisc.findData (obsy_code, current_year, -1, use_zip, use_plain, base_dir);

    // get k values from data
    index = 0;
    for (n = 0; n < N_MONTHS; n++)
    {
      for (i = 0; i < MAX_N_DAYS; i++)
      {
        if (i >= DateUtils.daysInMonth(n, current_year)) day_data = null;
        else if (month_data [n] == null) day_data = null;
        else day_data = month_data [n].getDayData (i);
        if (day_data == null) k_array = missing_k_array;
        else k_array = day_data.getKIndices ();
        for (k = 0; k < N_K_VALUES; k++)
        {
          k_indices [n] [index] = k_array [k];
          index ++;
        }
        
        // free the day data because it uses a lot of memory
        day_data = null;
      }
      // free the month data because it uses a lot of memory
      month_data[n] = null;
      index = 0;
    }
    return true;
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
        document = new SimpleDoc (plot_panel, print_doc_flavor, doc_attr_set);
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
      if (current_year_index >= n_years) current_year_index = n_years -1;
    }
    else if (source == select_year)
    {
      current_year_index = select_year.getSelectedIndex();
    }

    updateDisplay ();
  }

 /********************************************************************
  * updateDisplay - update the display after a new year is
  * selected
  ********************************************************************/
  private void updateDisplay ()
  {
    int n, current_year;
    String name;
    Cursor old_cursor;

    // get the year
    current_year = year_list.get (current_year_index).intValue();
    
    // set a busy cursor
    old_cursor = getCursor();
    setCursor(GlobalObjects.wait_cursor);

    // find the file to display
    if (!loadData ())
      JOptionPane.showMessageDialog (this, "Error loading data");

    setCursor (old_cursor);

    // set dialogue title
    title = "K Index Viewer: Data for " + CDMisc.getObservatoryName (obsy_code) + " " + current_year;
    if (base_dir != null)
        title += " [restricted to " + base_dir + "]";
    this.setTitle (title);

    // enable and disable components according to the year selected
    if (current_year == ((Integer)year_list.elementAt (0)).intValue ())
      prev_button.setEnabled (false);
    else prev_button.setEnabled (true);
    if (current_year == ((Integer)year_list.elementAt (n_years-1)).intValue ())
      next_button.setEnabled (false);
    else next_button.setEnabled (true);

    // set year displayed in drop-down list - remove action listener first
    for (n = 0; n < n_years; n++)
    {
      if (((Integer) year_list.elementAt (n)).intValue () == current_year)
      {
        select_year.removeActionListener (this);
        select_year.setSelectedIndex (n);
        select_year.addActionListener (this);
      }
    }

    // update plot panel
    plot_panel.update (k_indices, title);
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
