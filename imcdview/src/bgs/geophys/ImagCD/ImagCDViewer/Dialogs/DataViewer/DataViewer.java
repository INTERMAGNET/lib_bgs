/*
 * DataViewer.java
 *
 * Created on 04 March 2003, 16:39
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import java.util.*;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.library.Data.GeomagAbsoluteDifference;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Swing.JComboSpinner;
import java.text.SimpleDateFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class to display data. The class contains tabbed panes containing
 * a plot panel to view data graphically, and a table to view
 * numeric values.
 *
 * @author  fmc
 */
public class DataViewer extends JFrame 
implements ActionListener, WindowListener, ChangeListener
{
  // constants
  private static final int N_MONTHS          = 12;
  private static final int MAX_N_DAYS        = 366;

  // components
  private JButton copy_button, print_button, close_button, plot_options_button, zoom_help_button;
  private JButton prev_day, next_day, prev_month, next_month, prev_year, next_year, save_button;
  private JComboBox select_day, select_month, select_year;
  private JRadioButton minute_means, hourly_means, daily_means;
  private JCheckBox compare_check_box;
  private JComboBox compare_combo_box;
  private ObsyByDistance compare_list;
  private ObsyByDistance.ObsyInfoWithDistance compare_obsy;
  private String obsy_code;
  private JComboSpinner scale_spinner;
    

  // the top level tabbed pane and its contents
  private JTabbedPane tabbed_pane;
  private DataPlotPanel plot_panel;
  private DataPlotDiffPanel diff_panel;
  private DataPlotComparePanel compare_panel;
  private FileMetadataPanel file_metadata_panel;
  private DataTablePanel data_table_panel;
  private FileErrorPanel file_error_panel;
  
  // counters and flags
  private int n_years;
  private int current_view, disp_view;
  // the current selected date
  private int current_day, current_month, current_year_index;
  // the current displayed date
  private int disp_day, disp_month, disp_year_index;
  // what type of file to use (zip andor unzip)
  private boolean use_zip;
  private boolean use_plain;
  // the base directory of the data source to use (or null to use all data sources)
  private String base_dir;
  // the data source to use (or null to use all data sources)
  private CDDatabase database;

  // data storage
  private DataCache data_cache;
  private DataCache diff_data_cache;
  private Vector<Integer> year_list;
  private GregorianCalendar start_calendar;
  private DisplayData data_list;

  // objects used for printing
  private DocFlavor print_doc_flavor;
  private PrintRequestAttributeSet print_request_attr_set;

 /********************************************************************
  * Create new DataViewer
  *
  * @param obsy_code - the 3 letter code for the observatory
  * @param year - the year of the data to be displayed
  * @param month - the index of the month data to be displayed, from
  *                0 to 11.
  * @param use_zip - if true read from zip files, otherwise don't
  * @param use_plain - if true read from non-zip files, otherwise don't
  * @param base_dir - if the viewer should be restricted to one data source
  *        then set this to the path of the data source, otherwise set it to null
  * @throws DataViewerException if data could not be loaded
  ********************************************************************/
  public DataViewer (String obsy_code, int year, int month,
                     boolean use_zip, boolean use_plain, String base_dir)
  throws DataViewerException
  {
    int count;
    
    data_list = null;
      
    // create the frame
    this.obsy_code = obsy_code;
    this.use_zip = use_zip;
    this.use_plain = use_plain;
    this.data_cache = new DataCache ();
    this.diff_data_cache = null;

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

    // make list of available years for this observatory
    if (database == null)
        year_list = CDMisc.makeYearList(obsy_code);
    else
        year_list = CDMisc.makeYearList (obsy_code, database);
    n_years = year_list.size ();
    current_year_index = -1;
    for (count=0; count<n_years; count++)
    {
        if (year == year_list.get(count).intValue())
            current_year_index = count;
    }
    if (current_year_index < 0) current_year_index = n_years -1;
    disp_year_index = -1;
    
    // create a list of observatories ordered by distance from this observatory
    if (database == null)
        compare_list = new ObsyByDistance (obsy_code);
    else
        compare_list = new ObsyByDistance (obsy_code, database);

    // initially show minute means
    current_view = DataCache.MINUTE_MEANS;
    current_day = 0;
    current_month = month;
    if (current_month < 0) current_month = 0;
    if (current_month > 11) current_month = 11;
 
    
    // create dialog
    createComponents ();
    updateDisplay (true, false);
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    this.setIconImage(GlobalObjects.imag_icon.getImage());
    this.pack ();
    this.setVisible (true);
    
    assignUniqueID();
  }

  /** variables to define a unique ID for this viewer */
  private static int id_counter = 0;
  private int unique_id;
  private void assignUniqueID () { unique_id = id_counter ++; }
  public int getUniqueID () { return unique_id; }

  /** set up the components */
  private void createComponents ()
  {
    Container content_pane;
    JPanel control_panel, select_panel, close_panel;
    JPanel radio_panel, prev_panel, list_panel, next_panel, comp_panel;
    JPanel empty1, empty2;
    ButtonGroup duration;
    JLabel label;
    int n, index;

    // set the layout (use the default)
    content_pane = getContentPane();
    content_pane.setLayout(new BorderLayout ());

    // set up tabbed pane and add the tabs
    tabbed_pane = new JTabbedPane (JTabbedPane.BOTTOM);
    plot_panel = new DataPlotPanel ();
    diff_panel = new DataPlotDiffPanel ();
    compare_panel = new DataPlotComparePanel ();
    data_table_panel = new DataTablePanel ();
    file_metadata_panel = new FileMetadataPanel ();
    file_error_panel = new FileErrorPanel ();
    tabbed_pane.add ("Plot Data", plot_panel);
    tabbed_pane.add ("Plot Differences", diff_panel);
    tabbed_pane.add ("View Data", data_table_panel);
    tabbed_pane.add ("Metadata", file_metadata_panel);
    tabbed_pane.addChangeListener(this);

    // add tabbed pane to display
    content_pane.add ("Center", tabbed_pane);

    // add panels for controls
    control_panel = new JPanel();
    content_pane.add ("South", control_panel);
    control_panel.setLayout(new BorderLayout ());

    // add empty panel to improve layout
    empty1 = new JPanel ();
    control_panel.add ("West", empty1);
    empty1.add (new JLabel ("   "));

    // panel for date selection buttons
    select_panel = new JPanel ();
    select_panel.setLayout (new GridBagLayout ());
    control_panel.add ("Center", select_panel);

    // add empty panel to improve layout
    empty2 = new JPanel ();
    control_panel.add ("West", empty2);
    empty2.add (new JLabel ("   "));

    // panel for radio buttons
    radio_panel = new JPanel ();
    radio_panel.setLayout (new GridLayout (3, 1, 5, 5));
    radio_panel.setBorder(BorderFactory.createEtchedBorder());
    SwingUtils.addToGridBag (radio_panel, select_panel, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);

    // panel for "previous" buttons
    prev_panel = new JPanel ();
    prev_panel.setLayout (new GridLayout (3, 2, 5, 5));
    SwingUtils.addToGridBag (prev_panel, select_panel, 1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);

    // panel for drop-down lists
    list_panel = new JPanel ();
    list_panel.setLayout (new GridLayout (3, 3, 5, 5));
    SwingUtils.addToGridBag (list_panel, select_panel, 2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);

    // panel for "next" buttons
    next_panel = new JPanel ();
    next_panel.setLayout (new GridLayout (3, 2, 5, 5));
    SwingUtils.addToGridBag (next_panel, select_panel, 3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);

    // panel for comparison observatory
    comp_panel = new JPanel ();
    comp_panel.setLayout (new GridLayout (3, 1, 5, 5));
    comp_panel.setBorder(BorderFactory.createEtchedBorder());
    SwingUtils.addToGridBag (comp_panel, select_panel, 4, 0, 1, 1, 1.0, 1.0, GridBagConstraints.HORIZONTAL);
    
    // panel for close button
    close_panel = new JPanel ();
    control_panel.add ("South", close_panel);

    // add radio buttons to panel
    duration = new ButtonGroup();
    minute_means = new JRadioButton ("Minute mean data");
    duration.add (minute_means);
    if (current_view == DataCache.MINUTE_MEANS)
      minute_means.setSelected (true);
    minute_means.addActionListener (this);
    radio_panel.add (minute_means);

    hourly_means = new JRadioButton ("Hourly mean data");
    duration.add (hourly_means);
    if (current_view == DataCache.HOURLY_MEANS)
      hourly_means.setSelected (true);
    hourly_means.addActionListener (this);
    radio_panel.add (hourly_means);

    daily_means = new JRadioButton ("Daily mean data");
    duration.add (daily_means);
    if (current_view == DataCache.DAILY_MEANS)
      daily_means.setSelected (true);
    daily_means.addActionListener (this);
    radio_panel.add (daily_means);

    // add buttons to prev_panel
    prev_day = new JButton ("Previous day");
    prev_day.addActionListener(this);
    prev_panel.add (new JLabel (""));
    prev_panel.add (prev_day);
    prev_month = new JButton ("Previous month");
    prev_month.addActionListener(this);
    prev_panel.add (new JLabel (""));
    prev_panel.add (prev_month);
    prev_year = new JButton ("Previous year");
    prev_year.addActionListener(this);
    prev_panel.add (new JLabel (""));
    prev_panel.add (prev_year);

    // add lists to list_panel
    // add menu with list of days
    label = new JLabel ("Day:");
    label.setHorizontalAlignment (SwingConstants.RIGHT);
    list_panel.add (label);
    select_day = new JComboBox ();
    list_panel.add (select_day);
    select_day.addActionListener (this);
    // empty label to fill last column
    list_panel.add (new JLabel (""));

    // add menu with list of months
    label = new JLabel ("Month:");
    label.setHorizontalAlignment (SwingConstants.RIGHT);
    list_panel.add (label);
    select_month = new JComboBox ();
    for (n = 0; n < N_MONTHS; n++)
      select_month.addItem (DateUtils.getMonthName(n, DateUtils.MONTH_UPPERCASE, 3) + "        ");
    list_panel.add (select_month);
    select_month.addActionListener (this);
    // empty label to fill last column
    list_panel.add (new JLabel (""));

    // add menu with list of years
    label = new JLabel ("Year:");
    label.setHorizontalAlignment (SwingConstants.RIGHT);
    list_panel.add (label);
    select_year = new JComboBox ();
    for (n = 0; n < n_years; n++)
      select_year.addItem (((Integer) year_list.elementAt (n)).toString () + "        ");
    list_panel.add (select_year);
    select_year.addActionListener (this);
    // empty label to fill last column
    list_panel.add (new JLabel (""));

    // add buttons to next_panel
    next_day = new JButton ("Next day");
    next_day.addActionListener(this);
    next_panel.add (next_day);
    next_panel.add (new JLabel (""));
    next_month = new JButton ("Next month");
    next_month.addActionListener(this);
    next_panel.add (next_month);
    next_panel.add (new JLabel (""));
    next_year = new JButton ("Next year");
    next_year.addActionListener(this);
    next_panel.add (next_year);
    next_panel.add (new JLabel (""));
    
    // add list to compare panel
    compare_check_box = new JCheckBox ("Comparison on", false);
    compare_check_box.addActionListener(this);
    compare_combo_box = new JComboBox (compare_list.getObservatoryList());
    compare_combo_box.addActionListener(this);
    comp_panel.add (new JLabel ("Compare data with observatory"));
    comp_panel.add (compare_check_box);
    comp_panel.add (compare_combo_box);
    
    // add scale / zoom components
    close_panel.add (new JLabel ("Scale:"));
    scale_spinner = (JComboSpinner) PlotOptionsDialog.getZoomComboBox(true);
    close_panel.add (scale_spinner);
    zoom_help_button = new JButton ("Help on Zooming...");
    zoom_help_button.addActionListener (this);
    close_panel.add (zoom_help_button);
    // add plot options button
    plot_options_button = new JButton ("Plot Options...");
    plot_options_button.setActionCommand ("PlotOptions");
    plot_options_button.addActionListener (this);
    close_panel.add (plot_options_button);
    // add copy button
    copy_button = new JButton ("Copy to clipboard");
    copy_button.setEnabled(false);
    copy_button.setActionCommand ("Copy");
    copy_button.addActionListener (data_table_panel.getExcelAdapter());
    close_panel.add (copy_button);
    // add print button
    print_button = new JButton ("Print");
    print_button.addActionListener (this);
    close_panel.add (print_button);
    // add save button
    save_button = new JButton ("Save");
    save_button.addActionListener (this);
    close_panel.add (save_button);
    // button to close dialog
    close_button = new JButton ("Close");
    close_button.addActionListener (this);
    close_panel.add (close_button);
    getRootPane().setDefaultButton (close_button);
  }

  // listener for action events
  public void actionPerformed (ActionEvent actionEvent)
  {
    int index;
    boolean always_reload;
    Object source;
    DocPrintJob print_job;
    DocAttributeSet doc_attr_set;
    Doc document;
    PrintService print_service;
    ImageSaverDialog save_dialog;

    // get event source
    source = actionEvent.getSource();
    // the first block of if statements service events that don't need data
    // to be reloadaded and redisplayed
    if (source == close_button)
    {
      closeDialog(null);
    }
    else if (source == print_button)
    {
      if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
        GlobalObjects.print_canvas.doPrint (plot_panel);
      else if (diff_panel.equals(tabbed_pane.getSelectedComponent()))
        GlobalObjects.print_canvas.doPrint (diff_panel);
      else if (compare_panel.equals(tabbed_pane.getSelectedComponent()))
        GlobalObjects.print_canvas.doPrint (compare_panel);
    }
    else if (source == save_button)
    {
        save_dialog = GlobalObjects.createImageSaverDialog();
        if (save_dialog.runModal(this) == JFileChooser.APPROVE_OPTION)
        {
            GlobalObjects.persistImageSaverOptions(save_dialog);
            if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
                plot_panel.save (save_dialog.getChosenFile(), plot_panel.getTitle(), save_dialog.getChosenFileType(), 
                                 save_dialog.getImageSizeX(), save_dialog.getImageSizeY());
            else if (diff_panel.equals(tabbed_pane.getSelectedComponent()))
                diff_panel.save (save_dialog.getChosenFile(), diff_panel.getTitle(), save_dialog.getChosenFileType(),
                                 save_dialog.getImageSizeX(), save_dialog.getImageSizeY());
            else if (compare_panel.equals(tabbed_pane.getSelectedComponent()))
                compare_panel.save (save_dialog.getChosenFile(), compare_panel.getTitle(), save_dialog.getChosenFileType(),
                                    save_dialog.getImageSizeX(), save_dialog.getImageSizeY());
        }
    }
    else if (source == zoom_help_button)
    {
        GlobalObjects.command_interpreter.interpretCommand ("show_help&from_data_viewer_" + Integer.toString (getUniqueID()) + "&Zoom.html");
    }
    else if (source == plot_options_button)
    {
        GlobalObjects.command_interpreter.interpretCommand ("raise_plot_options_dialog&from_data_viewer_" + Integer.toString (getUniqueID()));
    }
    else
    {
      // the second block of if statements service events that do need data
      // to be reloadaded and redisplayed
      always_reload = false;
      if (source == compare_check_box)
      {
        if (compare_check_box.isSelected())
        {
          compare_obsy = (ObsyByDistance.ObsyInfoWithDistance) compare_combo_box.getSelectedItem();
          tabbed_pane.insertTab ("Plot Comparison With " + compare_obsy.getObservatoryCode(), null, compare_panel, null, 2);
          tabbed_pane.setSelectedComponent(compare_panel);
        }
        else
        {
          tabbed_pane.remove (compare_panel);
          compare_obsy = null;
          diff_data_cache = null;
        }
      }
      else if (source == compare_combo_box)
      {
        if (compare_check_box.isSelected())
        {
          compare_obsy = (ObsyByDistance.ObsyInfoWithDistance) compare_combo_box.getSelectedItem();
          tabbed_pane.setTitleAt(tabbed_pane.indexOfComponent(compare_panel), "Comparison with " + compare_obsy.getObservatoryCode());
          always_reload = true;
        }
      }
      else if (source == prev_day)
      {
        current_day --;
        // check if day is in previous month
        if (current_day < 0)
        {
          current_month --;
          if (current_month < 0)
          {
            // current_year will never fall beyond earliest
            // available year because button would be disabled
            current_month = N_MONTHS-1;
            current_year_index --;
            if (current_year_index <0) current_year_index = 0;
          }
          // reset day index to max days in this month
          current_day = DateUtils.daysInMonth (current_month, getCurrentYear())-1;
        }
      }
      else if (source == next_day)
      {
        current_day ++;
        // check if day is in next month
        if (current_day > (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
        {
          current_day = 0;
          current_month ++;
          if (current_month >= N_MONTHS)
          {
            current_month = 0;
            current_year_index ++;
            if (current_year_index >= n_years) current_year_index = n_years -1;
          }
        }
      }
      else if (source == prev_month)
      {
        current_month --;
        if (current_month < 0)
        {
          current_month = N_MONTHS-1;
          current_year_index --;
          if (current_year_index < 0) current_year_index = 0;
        }
        // check that day index is not too high for this month
        if (current_day >= (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
          current_day = DateUtils.daysInMonth (current_month, getCurrentYear())-1;
      }
      else if (source == next_month)
      {
        current_month ++;
        if (current_month >= N_MONTHS)
        {
          current_month = 0;
          current_year_index ++;
          if (current_year_index >= n_years) current_year_index = n_years -1;
        }
        // check that day index is not too high for this month
        if (current_day >= (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
          current_day = DateUtils.daysInMonth (current_month, getCurrentYear())-1;
      }
      else if (source == prev_year)
      {
        current_year_index --;
        if (current_year_index < 0) current_year_index = 0;
      }
      else if (source == next_year)
      {
        current_year_index ++;
        if (current_year_index >= n_years) current_year_index = n_years -1;
      }
      else if (source == select_day)
      {
        current_day = select_day.getSelectedIndex ();
      }
      else if (source == select_month)
      {
        current_month = select_month.getSelectedIndex ();
        // check that day index is valid
        if (current_day >= (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
          current_day = DateUtils.daysInMonth (current_month, getCurrentYear()) -1;
      }
      else if (source == select_year)
      {
        current_year_index = select_year.getSelectedIndex();
        // check that day index is valid for this year and month
        if (current_day >= (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
          current_day = DateUtils.daysInMonth (current_month, getCurrentYear())-1;
      }
      else if (source == minute_means)
      {
        current_view = DataCache.MINUTE_MEANS;
      }
      else if (source == hourly_means)
      {
        current_view = DataCache.HOURLY_MEANS;
      }
      else if (source == daily_means)
      {
        current_view = DataCache.DAILY_MEANS;
      }

      try
      {
        updateDisplay (false, always_reload);
      }
      catch (DataViewerException e)
      {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

 /********************************************************************
  * updateDisplay - update display after a new date is selected
  *                 or new options are given by the user
  * @param search_for_month - if true, search for the first available month
  * @param always_reload - if true, always reload the cache and redisplay
  *                        if false, only reload if date/view has changed
  * @throws DataViewerException if the data cannot be loaded
  ********************************************************************/
  public void updateDisplay (boolean search_for_month, boolean always_reload)
  throws DataViewerException
  {
    int n, extra_pc, count;
    boolean reload;
    String time_units;
    Cursor old_cursor;
    GeomagAbsoluteValue data [], diff_data [];
    GeomagAbsoluteDifference compare_diff_data [];
    GeomagAbsoluteStats compare_diff_stats;

    compare_diff_data = null;
    compare_diff_stats = null;
    old_cursor = null;
    try { extra_pc = Integer.parseInt(GlobalObjects.configuration.getProperty("PlotExtraWidth", "0")); }
    catch (NumberFormatException e) { extra_pc = 0; }
    
    // check if new data is required
    if (always_reload) reload = true;
    else if ((current_year_index != disp_year_index) || (current_month != disp_month) ||
             (current_day != disp_day) || (current_view != disp_view)) reload = true;
    else reload = false;
    if (reload)
    {
      // set a busy cursor
      old_cursor = getCursor();
      setCursor(GlobalObjects.wait_cursor);

      // load data
      if (! data_cache.loadData (obsy_code, current_day, current_month, getCurrentYear(),
                                 current_view, extra_pc, use_zip, use_plain, search_for_month, base_dir))
      {
        // reset currently displayed data
        current_day = disp_day;
        current_month = disp_month;
        current_year_index = disp_year_index;
        current_view = disp_view;

        // reset cursor
        setCursor (old_cursor);
        
        // or send message to display panel?
        throw new DataViewerException ("No data is available for this date.");
      }
      // When I looked at this code, the next line was in use (not commented), the
      // second line wsa out of use (commented) - I'm not sure why this next line was here
      // as current_month is set in response to user interaction in other parts of the code.
      // current_month should reflect the month that the user has chosen - this will
      // sometimes be different from the cache's start month when the user has extended
      // the plot beyond 1 month or 1 year (using the extension option in plot options)
      // this assignement was causing a bug in those circumstances
      // current_month = data_cache.getMonthData().get(0).getMonthIndex() -1;
      //// current_month = data_cache.getStart_month();
      // ***
      // next line added 8.03.2010 - sometimes only the second half of the year is available
      // or there may be the first data file missing for another reason.
      if (current_month<data_cache.getStart_month())current_month=data_cache.getStart_month();
    }

    // check if comparison data needs to be loaded
    if ((reload || (diff_data_cache == null)) && (compare_obsy != null))
    {
        // set a busy cursor
        if (old_cursor == null)
        {
            old_cursor = getCursor();
            setCursor(GlobalObjects.wait_cursor);
        }
        
        // if this is the first call that uses difference data, then create the cache
        if (diff_data_cache == null) diff_data_cache = new DataCache();
        
        // load the data
        if (diff_data_cache.loadData (compare_obsy.getObservatoryCode(), current_day, current_month, getCurrentYear(),
                                      current_view, extra_pc, use_zip, use_plain, search_for_month, base_dir))
        {
            data = data_cache.getStoredData();
            diff_data = diff_data_cache.getStoredData();
            if (data.length == diff_data.length)
            {
                compare_diff_data = new GeomagAbsoluteDifference [data_cache.getStoredData().length];
                for (count=0; count<compare_diff_data.length; count++)
                    compare_diff_data [count] = new GeomagAbsoluteDifference (data [count], diff_data [count]);
                compare_diff_stats = new GeomagAbsoluteStats(compare_diff_data);
            }
        }
    }
    
    // restore the cursor
    if (old_cursor != null) setCursor(old_cursor);
    
    // set title to show current date
    updateTitle ();
    switch (current_view)
    {
      case DataCache.MINUTE_MEANS:
        start_calendar = new GregorianCalendar (getCurrentYear(), current_month, current_day);
        time_units = "minute";
        break;
      case DataCache.HOURLY_MEANS:
        start_calendar = new GregorianCalendar (getCurrentYear(), current_month, 1);
        time_units = "hour";
        break;
      case DataCache.DAILY_MEANS:
        start_calendar = new GregorianCalendar (getCurrentYear(), 0, 1);
        time_units = "day";
        break;
      default:
        start_calendar = new GregorianCalendar (1970, 0, 1);
        time_units = "unknown";
        break;
    }
    start_calendar.setTimeZone(DateUtils.gmtTimeZone);

    // build list of days for the new month and year
    if (current_month != disp_month || current_year_index != disp_year_index)
    {
      // stop events being fired while menu is being built
      select_day.removeActionListener (this);
      select_day.removeAllItems ();
      for (n = 0; n < DateUtils.daysInMonth(current_month, getCurrentYear()); n++)
        select_day.addItem (Integer.toString (n+1) + "        ");
      // add action listener again
      select_day.addActionListener (this);
    }

    // check if date is first available
    if (current_year_index <= 0)
    {
      prev_year.setEnabled (false);
      if (current_month == 0)
      {
        prev_month.setEnabled (false);
        if (current_day == 0)
          prev_day.setEnabled (false);
        else prev_day.setEnabled (true);
      }
      else
      {
        prev_month.setEnabled (true);
        prev_day.setEnabled (true);
      }
    }
    else
    {
      prev_year.setEnabled (true);
      prev_month.setEnabled (true);
      prev_day.setEnabled (true);
    }

    // check if date is last available
    if (current_year_index >=  (n_years-1))
    {
      next_year.setEnabled (false);
      if (current_month == N_MONTHS-1)
      {
        next_month.setEnabled (false);
        if (current_day == (DateUtils.daysInMonth (current_month, getCurrentYear())-1))
          next_day.setEnabled (false);
        else next_day.setEnabled (true);
      }
      else
      {
        next_month.setEnabled (true);
        next_day.setEnabled (true);
      }
    }
    else
    {
      next_year.setEnabled (true);
      next_month.setEnabled (true);
      next_day.setEnabled (true);
    }

    // all "day" elements are unavailable
    if (current_view != DataCache.MINUTE_MEANS)
    {
      prev_day.setEnabled (false);
      select_day.setEnabled (false);
      select_day.setSelectedIndex(0);
      next_day.setEnabled (false);
    }
    else
      select_day.setEnabled (true);

    // all "month" elements are unavailable
    if (current_view == DataCache.DAILY_MEANS)
    {
      prev_month.setEnabled (false);
      select_month.setEnabled (false);
      select_month.setSelectedIndex(0);
      next_month.setEnabled (false);
    }
    else
      select_month.setEnabled (true);

    // set indices on drop-down menus. Remove action listeners
    // first to stop events being fired
    select_day.removeActionListener (this);
    select_month.removeActionListener (this);
    select_year.removeActionListener (this);
    if (select_day.getItemCount() > current_day) select_day.setSelectedIndex (current_day);
    if (select_month.getItemCount() > current_month) select_month.setSelectedIndex (current_month);
    for (n = 0; n < n_years; n++)
    {
      if (n == current_year_index)
      {
        select_year.setSelectedIndex (n);
        break;
      }
    }
    select_day.addActionListener (this);
    select_month.addActionListener (this);
    select_year.addActionListener (this);

    // set selected radio buttons - remove action listeners first
    // to stop events being fired
    minute_means.removeActionListener (this);
    hourly_means.removeActionListener (this);
    daily_means.removeActionListener (this);
    if (current_view == DataCache.MINUTE_MEANS)
      minute_means.setSelected (true);
    else if (current_view == DataCache.HOURLY_MEANS)
      hourly_means.setSelected (true);
    else if (current_view == DataCache.DAILY_MEANS)
      daily_means.setSelected (true);
    
    minute_means.addActionListener (this);
    hourly_means.addActionListener (this);
    daily_means.addActionListener (this);

    // make a list of the geomangetic components to be displayed
    if (diff_data_cache == null)
        data_list = new DisplayData (data_cache.getStartDate(), time_units,
                                     data_cache.getStoredData(), 
                                     data_cache.getStoredStats(), null,
                                     null, data_cache.getMonthData(), null, 
                                     data_cache.getErrorMessages(), null);
    else
        data_list = new DisplayData (data_cache.getStartDate(), time_units,
                                     data_cache.getStoredData(), 
                                     data_cache.getStoredStats(), compare_diff_data,
                                     compare_diff_stats, data_cache.getMonthData(),  diff_data_cache.getMonthData(),
                                     data_cache.getErrorMessages(), diff_data_cache.getErrorMessages());
    if (GlobalObjects.configuration.getProperty("PlotNativeOrCalculated", "Recorded").equalsIgnoreCase("Calculated"))
    {
        // component codes are from the configuration
        if (PlotOptionsDialog.isShownFromConfiguration("X"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_X, 
                                    PlotOptionsDialog.getColourFromConfiguration ("X"),
                                    PlotOptionsDialog.getScaleFromConfiguration("X"));
        if (PlotOptionsDialog.isShownFromConfiguration("Y"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_Y, 
                                    PlotOptionsDialog.getColourFromConfiguration ("Y"),
                                    PlotOptionsDialog.getScaleFromConfiguration("Y"));
        if (PlotOptionsDialog.isShownFromConfiguration("Z"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_Z, 
                                    PlotOptionsDialog.getColourFromConfiguration ("Z"),
                                    PlotOptionsDialog.getScaleFromConfiguration("Z"));
        if (PlotOptionsDialog.isShownFromConfiguration("F"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F, 
                                    PlotOptionsDialog.getColourFromConfiguration ("F"),
                                    PlotOptionsDialog.getScaleFromConfiguration("F"));
        if (PlotOptionsDialog.isShownFromConfiguration("H"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_H, 
                                    PlotOptionsDialog.getColourFromConfiguration ("H"),
                                    PlotOptionsDialog.getScaleFromConfiguration("H"));
        if (PlotOptionsDialog.isShownFromConfiguration("D"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_D, 
                                    PlotOptionsDialog.getColourFromConfiguration ("D"),
                                    PlotOptionsDialog.getScaleFromConfiguration("D"));
        if (PlotOptionsDialog.isShownFromConfiguration("I"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_I, 
                                    PlotOptionsDialog.getColourFromConfiguration ("I"),
                                    PlotOptionsDialog.getScaleFromConfiguration("I"));
        if (PlotOptionsDialog.isShownFromConfiguration("FScalar"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F_SCALAR, 
                                    PlotOptionsDialog.getColourFromConfiguration ("FScalar"),
                                    PlotOptionsDialog.getScaleFromConfiguration("FScalar"));
        if (PlotOptionsDialog.isShownFromConfiguration("FDiff"))
            data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F_DIFF, 
                                    PlotOptionsDialog.getColourFromConfiguration ("FDiff"),
                                    PlotOptionsDialog.getScaleFromConfiguration("FDiff"));
    }
    else
    {
        for (n=0; n<data_cache.getNComponents(); n++)
        {
            switch (data_cache.getComponentCode(n))
            {
                case GeomagAbsoluteValue.COMPONENT_X:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_X, 
                                            PlotOptionsDialog.getColourFromConfiguration ("X"),
                                            PlotOptionsDialog.getScaleFromConfiguration("X"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_Y:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_Y, 
                                            PlotOptionsDialog.getColourFromConfiguration ("Y"),
                                            PlotOptionsDialog.getScaleFromConfiguration("Y"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_Z:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_Z, 
                                            PlotOptionsDialog.getColourFromConfiguration ("Z"),
                                            PlotOptionsDialog.getScaleFromConfiguration("Z"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_F:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F, 
                                            PlotOptionsDialog.getColourFromConfiguration ("F"),
                                            PlotOptionsDialog.getScaleFromConfiguration("F"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_H:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_H, 
                                            PlotOptionsDialog.getColourFromConfiguration ("H"),
                                            PlotOptionsDialog.getScaleFromConfiguration("H"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_D:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_D, 
                                            PlotOptionsDialog.getColourFromConfiguration ("D"),
                                            PlotOptionsDialog.getScaleFromConfiguration("D"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_I:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_I, 
                                            PlotOptionsDialog.getColourFromConfiguration ("I"),
                                            PlotOptionsDialog.getScaleFromConfiguration("I"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_F_SCALAR:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F_SCALAR, 
                                            PlotOptionsDialog.getColourFromConfiguration ("FScalar"),
                                            PlotOptionsDialog.getScaleFromConfiguration("FScalar"));
                    break;
                case GeomagAbsoluteValue.COMPONENT_F_DIFF:
                    data_list.addComponent (GeomagAbsoluteValue.COMPONENT_F_DIFF, 
                                            PlotOptionsDialog.getColourFromConfiguration ("FDiff"),
                                            PlotOptionsDialog.getScaleFromConfiguration("FDiff"));
                    break;
            }
        }
    }
    
    // show the data
    installErrorPane();
    updateVisiblePanel ();

    // set the scale combo box
    PlotOptionsDialog.setZoomComboBoxSelectedItem(scale_spinner, false);
    
    // set the currently displayed data flags
    disp_day = current_day;
    disp_month = current_month;
    disp_year_index = current_year_index;
    disp_view = current_view;
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
    
    // make sure that data gets deallocated
    data_cache = null;
    data_list = null;
  }

  /** captures switching between tabbed panes */
  public void stateChanged(ChangeEvent e) 
  {
    updateVisiblePanel();
  }

  /** update the panel that is currently visible */
  private void updateVisiblePanel ()  
  {
    int current_year;
    String compare_obsy_name;
    
    updateTitle();

    current_year = getCurrentYear();
    if (diff_data_cache == null) compare_obsy_name = null;
    else compare_obsy_name = diff_data_cache.getObsyName();
    
    if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(false);
      print_button.setEnabled(true);
      save_button.setEnabled(true);
      if (data_list!= null)
          plot_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
    else if (diff_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(false);
      print_button.setEnabled(true);
      save_button.setEnabled(true);
      if (data_list!= null)
          diff_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
    else if (data_table_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(true);
      print_button.setEnabled(false);
      save_button.setEnabled(false);
      if (data_list!= null)
          data_table_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
    else if (compare_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(false);
      print_button.setEnabled(true);
      save_button.setEnabled(true);
      if (data_list!= null)
          compare_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
    else if (file_metadata_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(false);
      print_button.setEnabled(false);
      save_button.setEnabled(false);
      if (data_list != null)
          file_metadata_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
    else if (file_error_panel.equals(tabbed_pane.getSelectedComponent()))
    {
      copy_button.setEnabled(false);
      print_button.setEnabled(false);
      save_button.setEnabled(false);
      if (data_list != null)
          file_error_panel.update (data_cache.getObsyName(), compare_obsy_name, current_year, current_month, current_day, data_list, current_view);
    }
  }

  private void installErrorPane ()
  {
      int count;
      boolean found;
      Component component;
      
      // if there are no errors, then remove the error pane
      if (data_list.getNDataLoadErrors() <= 0)
      {
          tabbed_pane.remove (file_error_panel);
      }
      else
      {
          // add the error pane if it's not already there
          found = false;
          for (count=0; count<tabbed_pane.getTabCount(); count++)
          {
              component = tabbed_pane.getTabComponentAt(count);
              if (component != null)
              {
                  if (component.equals(file_error_panel))
                      found = true;
              }
          }
          if (! found)
              tabbed_pane.add ("<html><b><p color=red>File reading errors</html>", file_error_panel);
      }
  }
  
  private void updateTitle ()
  {
    int current_year;
    String read_errors, end_title, restricted;

    current_year = getCurrentYear();
    switch (current_view)
    {
      case DataCache.MINUTE_MEANS:
        end_title = "Viewer: Minute Mean Data for " + data_cache.getObsyName() + " " + 
                    (current_day +1) + " " +
                    DateUtils.getMonthName(current_month, DateUtils.MONTH_UPPERFIRSTONLY, 3) + " " +
                    current_year;
        break;
      case DataCache.HOURLY_MEANS:
        end_title = "Viewer: Hourly Mean Data for " + data_cache.getObsyName() + " " +
                    DateUtils.getMonthName(current_month, DateUtils.MONTH_UPPERFIRSTONLY, 3) + " " +
                    current_year;
        break;
      case DataCache.DAILY_MEANS:
        end_title = "Viewer: Daily Mean Data for " + data_cache.getObsyName() +
                    " " + current_year;
        break;
      default:
        end_title = "Viewer";
        break;
    }

    // show if data is restricted to one source
    if (base_dir == null)
        restricted = "";
    else
        restricted = " [restricted to " + base_dir + "]";
    
    // show if there are errors
    if (data_list == null) read_errors = "";
    else if (data_list.getNDataLoadErrors() <= 0) read_errors = "";
    else read_errors = " (read errors)";
    
    this.setTitle ("Data " + end_title + restricted + read_errors);
  }
  
  private int getCurrentYear () { return year_list.get (current_year_index).intValue(); }
}
