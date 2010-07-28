/*
 * ExportDialog.java
 *
 * Created on 26 March 2007, 07:31
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatoryIterator;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDYear;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.ExportThread;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Swing.ProgressDialog;
import bgs.geophys.library.Swing.SwingUtils;
import java.io.IOException;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author  smf
 */
public class ExportDialog extends javax.swing.JFrame 
implements ExportThread.progressAndCancel
{
    
    // the currently selected start date
    //    private int selectedDay, selectedMonth, selectedYear;
    // the list of available years
    private Vector year_list;

    // what type of file to use (zip and/or unzip)
    private boolean use_zip;
    private boolean use_plain;
    
    // flags used with notifier to control display of messages
    private boolean show_warning_messages;
    private boolean show_error_messages;
    
    // variables for running data retrieval from a different thread
    private Thread export_data_thread;

    // separate dialog used to track progress of export
    private ProgressDialog progress;
    
    // the base directory of the data source to use (or null to use all data sources)
    private String base_dir;
    // the data source to use (or null to use all data sources)
    private CDDatabase database;
    
    // flag to control whether the year/month callbacks to anything
    private boolean do_date_updates;
  
    /** Creates a new instance of ExportDialog with an observatory code,
     *  data year and month for data file
     * @param obs_code the observatory to select (null for none)
     * @param year the data file year (-1 for no selected year)
     * @param month the data file month (-1 for no selected month)
     * @param use_zip - if true read from zip files, otherwise don't
     * @param use_plain - if true read from non-zip files, otherwise don't
     * @param base_dir - if the export should be restricted to one data source
     *        then set this to the path of the data source, otherwise set it to null */
    public ExportDialog (String obs_code, int year, int month, 
                         boolean use_zip, boolean use_plain, String base_dir)
    {
        this.use_zip = use_zip;
        this.use_plain = use_plain;
        initComponents();
        initComponents2 (obs_code, year, month, base_dir);
    }

    /** set up the components with the given observatory selected
     * @param obs_code - the observatory code to select (pass null for no obs code)
     * @param year - the selected year (pass in -1 for no year)
     * @param month - the selected month (pass in -1 for no month)
     * @param base_dir - if the export should be restricted to one data source
     *        then set this to the path of the data source, otherwise set it to null */
    private void initComponents2 (String obs_code, int year, int month, String base_dir)
    {
        int count, index, yr, yrIndex;
        CDObservatoryIterator obsy_iterator;
        CDObservatoryIterator.ObservatoryInfo info;
        DefaultListModel obs_list_model;
        
        do_date_updates = false;
        
        // find database (if any)
        if (base_dir == null)
            database = null;
        else
            database = CDMisc.findDataSource(base_dir);
        if (database == null)
            this.base_dir = null;
        else
            this.base_dir = base_dir;

        // set dialog title
        if (base_dir == null)
            this.setTitle("Export Data");
        else
            this.setTitle("Export Data [restricted to " + base_dir + "]");
        
        // build the list of observatories
        if (database == null)
            obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
        else
            obsy_iterator = database.GetObservatoryIterator();
        obs_list_model = new DefaultListModel ();
        index = -1;
        for (info = obsy_iterator.GetFirstObservatory(), count = 0; 
             info != null;  
             info = obsy_iterator.GetNextObservatory(), count ++)
        {
            obs_list_model.addElement (info.GetDisplayName ());
            if (obs_code != null && obs_code.equals (info.GetObservatoryCode ()))
                index = count;
        }
        ObservatoryList.setModel (obs_list_model);
        ObservatoryList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (index >= 0)
        {
            ObservatoryList.setSelectedIndex (index);
            ObservatoryList.ensureIndexIsVisible (index);
        }
        
        // create list of months, years and days
        StartMonthComboBox.removeAllItems ();
        for (count = 0; count < 12; count ++)
            StartMonthComboBox.addItem (DateUtils.getMonthName(count, DateUtils.MONTH_UPPERFIRSTONLY, -1));
        StartYearComboBox.removeAllItems ();
        if (database == null)
            year_list = CDMisc.makeYearList ();
        else
            year_list = CDMisc.makeYearList(database);
        yrIndex = 0;
        for (count = 0; count < year_list.size (); count++)
        {
            yr = ((CDYear)year_list.elementAt (count)).GetYear ();
            StartYearComboBox.addItem (new Integer (yr));
            if (yr == year) yrIndex = count;
        }
        buildDayList ();
        do_date_updates = true;
        
        // set initial selection of day, month, year
        StartDayComboBox.setSelectedIndex (0);
        if (month >= 0)
            StartMonthComboBox.setSelectedIndex (month);
        if (year >= 0)
            StartYearComboBox.setSelectedIndex (yrIndex);
        
        // make combo boxes a little bigger - hopefully fixes a bug on Linux
        this.pack ();
        SwingUtils.changeSize (StartDayComboBox, 120, 100);
        SwingUtils.changeSize (StartMonthComboBox, 120, 100);
        SwingUtils.changeSize (StartYearComboBox, 120, 100);

        // pack and display
        this.setIconImage(GlobalObjects.imag_icon.getImage());
        this.pack ();
        this.setVisible (true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        DataTypeButtonGroup = new javax.swing.ButtonGroup();
        DurationTypeButtonGroup = new javax.swing.ButtonGroup();
        FormatTypeButtonGroup = new javax.swing.ButtonGroup();
        FileLengthButtonGroup = new javax.swing.ButtonGroup();
        ZipButtonGroup = new javax.swing.ButtonGroup();
        ObservatoryLabel = new javax.swing.JLabel();
        ObservatoryListScrollPane = new javax.swing.JScrollPane();
        ObservatoryList = new javax.swing.JList();
        ObservatoryHelpLabel = new javax.swing.JLabel();
        Separator1 = new javax.swing.JSeparator();
        DataTypeLabel = new javax.swing.JLabel();
        DataTypePanel = new javax.swing.JPanel();
        MinuteDataRadioButton = new javax.swing.JRadioButton();
        HourlyDataRadioButton = new javax.swing.JRadioButton();
        DailyDataRadioButton = new javax.swing.JRadioButton();
        DataFormatLabel = new javax.swing.JLabel();
        DataFormatPanel = new javax.swing.JPanel();
        Iaga2002RadioButton = new javax.swing.JRadioButton();
        IntermagnetRadioButton = new javax.swing.JRadioButton();
        PlainTextRadioButton = new javax.swing.JRadioButton();
        WDCRadioButton = new javax.swing.JRadioButton();
        FileLengthLabel = new javax.swing.JLabel();
        FileLengthPanel = new javax.swing.JPanel();
        FileLengthDayRadioButton = new javax.swing.JRadioButton();
        FileLengthMonthRadioButton = new javax.swing.JRadioButton();
        FileLengthYearRadioButton = new javax.swing.JRadioButton();
        FileLengthInfiniteRadioButton = new javax.swing.JRadioButton();
        ZipLabel = new javax.swing.JLabel();
        ZipPanel = new javax.swing.JPanel();
        ZipLengthNoneRadioButton = new javax.swing.JRadioButton();
        ZipLengthDayRadioButton = new javax.swing.JRadioButton();
        ZipLengthMonthRadioButton = new javax.swing.JRadioButton();
        ZipLengthYearRadioButton = new javax.swing.JRadioButton();
        ZipLengthObservatoryRadioButton = new javax.swing.JRadioButton();
        ZipLengthAllDataRadioButton = new javax.swing.JRadioButton();
        GINCodeLabel = new javax.swing.JLabel();
        GINCodePanel = new javax.swing.JPanel();
        GINCodeTextField = new javax.swing.JTextField();
        GINCodeHelpLabel = new javax.swing.JLabel();
        Separator2 = new javax.swing.JSeparator();
        StartDateLabel = new javax.swing.JLabel();
        StartDatePanel = new javax.swing.JPanel();
        StartDayComboBox = new javax.swing.JComboBox();
        StartMonthComboBox = new javax.swing.JComboBox();
        StartYearComboBox = new javax.swing.JComboBox();
        DurationLabel = new javax.swing.JLabel();
        DurationPanel = new javax.swing.JPanel();
        DurationTextField = new javax.swing.JTextField();
        DayDurationRadioButton = new javax.swing.JRadioButton();
        MonthDurationRadioButton = new javax.swing.JRadioButton();
        YearDurationRadioButton = new javax.swing.JRadioButton();
        ErrorControlLabel = new javax.swing.JLabel();
        ErrorControlPanel = new javax.swing.JPanel();
        ShowErrorsCheckBox = new javax.swing.JCheckBox();
        ShowWarningsCheckBox = new javax.swing.JCheckBox();
        Separator3 = new javax.swing.JSeparator();
        ButtonPanel = new javax.swing.JPanel();
        WriteDataButton = new javax.swing.JButton();
        ExportOptionsButton = new javax.swing.JButton();
        CloseButton = new javax.swing.JButton();
        HelpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        ObservatoryLabel.setText("Observatory:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(ObservatoryLabel, gridBagConstraints);

        ObservatoryList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        ObservatoryListScrollPane.setViewportView(ObservatoryList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ObservatoryListScrollPane, gridBagConstraints);

        ObservatoryHelpLabel.setText("Use the SHIFT and CTRL keys to select more than one observatory.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ObservatoryHelpLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(Separator1, gridBagConstraints);

        DataTypeLabel.setText("Data type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(DataTypeLabel, gridBagConstraints);

        DataTypePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        DataTypeButtonGroup.add(MinuteDataRadioButton);
        MinuteDataRadioButton.setSelected(true);
        MinuteDataRadioButton.setText("Minute mean data");
        MinuteDataRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        MinuteDataRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        MinuteDataRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MinuteDataRadioButtonActionPerformed(evt);
            }
        });
        DataTypePanel.add(MinuteDataRadioButton);

        DataTypeButtonGroup.add(HourlyDataRadioButton);
        HourlyDataRadioButton.setText("Hourly mean data");
        HourlyDataRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        HourlyDataRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        HourlyDataRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HourlyDataRadioButtonActionPerformed(evt);
            }
        });
        DataTypePanel.add(HourlyDataRadioButton);

        DataTypeButtonGroup.add(DailyDataRadioButton);
        DailyDataRadioButton.setText("Daily mean data");
        DailyDataRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        DailyDataRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        DailyDataRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DailyDataRadioButtonActionPerformed(evt);
            }
        });
        DataTypePanel.add(DailyDataRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(DataTypePanel, gridBagConstraints);

        DataFormatLabel.setText("Data format:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(DataFormatLabel, gridBagConstraints);

        DataFormatPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        FormatTypeButtonGroup.add(Iaga2002RadioButton);
        Iaga2002RadioButton.setSelected(true);
        Iaga2002RadioButton.setText("IAGA 2002");
        Iaga2002RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Iaga2002RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        Iaga2002RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Iaga2002RadioButtonActionPerformed(evt);
            }
        });
        DataFormatPanel.add(Iaga2002RadioButton);

        FormatTypeButtonGroup.add(IntermagnetRadioButton);
        IntermagnetRadioButton.setText("IMV1.22");
        IntermagnetRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        IntermagnetRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        IntermagnetRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IntermagnetRadioButtonActionPerformed(evt);
            }
        });
        DataFormatPanel.add(IntermagnetRadioButton);

        FormatTypeButtonGroup.add(PlainTextRadioButton);
        PlainTextRadioButton.setText("Plain text");
        PlainTextRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        PlainTextRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        PlainTextRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlainTextRadioButtonActionPerformed(evt);
            }
        });
        DataFormatPanel.add(PlainTextRadioButton);

        FormatTypeButtonGroup.add(WDCRadioButton);
        WDCRadioButton.setText("WDC");
        WDCRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        WDCRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        WDCRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WDCRadioButtonActionPerformed(evt);
            }
        });
        DataFormatPanel.add(WDCRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(DataFormatPanel, gridBagConstraints);

        FileLengthLabel.setText("File length:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(FileLengthLabel, gridBagConstraints);

        FileLengthButtonGroup.add(FileLengthDayRadioButton);
        FileLengthDayRadioButton.setSelected(true);
        FileLengthDayRadioButton.setText("1 file/day");
        FileLengthDayRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        FileLengthDayRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FileLengthDayRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileLengthDayRadioButtonActionPerformed(evt);
            }
        });
        FileLengthPanel.add(FileLengthDayRadioButton);

        FileLengthButtonGroup.add(FileLengthMonthRadioButton);
        FileLengthMonthRadioButton.setText("1 file/month");
        FileLengthMonthRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        FileLengthMonthRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FileLengthMonthRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileLengthMonthRadioButtonActionPerformed(evt);
            }
        });
        FileLengthPanel.add(FileLengthMonthRadioButton);

        FileLengthButtonGroup.add(FileLengthYearRadioButton);
        FileLengthYearRadioButton.setText("1 file/year");
        FileLengthYearRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        FileLengthYearRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FileLengthYearRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileLengthYearRadioButtonActionPerformed(evt);
            }
        });
        FileLengthPanel.add(FileLengthYearRadioButton);

        FileLengthButtonGroup.add(FileLengthInfiniteRadioButton);
        FileLengthInfiniteRadioButton.setText("1 file/observatory");
        FileLengthInfiniteRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        FileLengthInfiniteRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FileLengthInfiniteRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileLengthInfiniteRadioButtonActionPerformed(evt);
            }
        });
        FileLengthPanel.add(FileLengthInfiniteRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(FileLengthPanel, gridBagConstraints);

        ZipLabel.setText("Group into zip:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(ZipLabel, gridBagConstraints);

        ZipPanel.setLayout(new java.awt.GridBagLayout());

        ZipButtonGroup.add(ZipLengthNoneRadioButton);
        ZipLengthNoneRadioButton.setSelected(true);
        ZipLengthNoneRadioButton.setText("Do not use zip file");
        ZipLengthNoneRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthNoneRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        ZipPanel.add(ZipLengthNoneRadioButton, gridBagConstraints);

        ZipButtonGroup.add(ZipLengthDayRadioButton);
        ZipLengthDayRadioButton.setText("1 zip/day");
        ZipLengthDayRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthDayRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ZipPanel.add(ZipLengthDayRadioButton, gridBagConstraints);

        ZipButtonGroup.add(ZipLengthMonthRadioButton);
        ZipLengthMonthRadioButton.setText("1 zip/month");
        ZipLengthMonthRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthMonthRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ZipPanel.add(ZipLengthMonthRadioButton, gridBagConstraints);

        ZipButtonGroup.add(ZipLengthYearRadioButton);
        ZipLengthYearRadioButton.setText("1 zip/year");
        ZipLengthYearRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthYearRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        ZipPanel.add(ZipLengthYearRadioButton, gridBagConstraints);

        ZipButtonGroup.add(ZipLengthObservatoryRadioButton);
        ZipLengthObservatoryRadioButton.setText("1 zip/observatory");
        ZipLengthObservatoryRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthObservatoryRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ZipPanel.add(ZipLengthObservatoryRadioButton, gridBagConstraints);

        ZipButtonGroup.add(ZipLengthAllDataRadioButton);
        ZipLengthAllDataRadioButton.setText("All data in 1 zip file");
        ZipLengthAllDataRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ZipLengthAllDataRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ZipPanel.add(ZipLengthAllDataRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ZipPanel, gridBagConstraints);

        GINCodeLabel.setText("GIN code:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(GINCodeLabel, gridBagConstraints);

        GINCodePanel.setLayout(new java.awt.BorderLayout(5, 0));

        GINCodeTextField.setColumns(5);
        GINCodePanel.add(GINCodeTextField, java.awt.BorderLayout.WEST);

        GINCodeHelpLabel.setText("(only used with Intermagnet data format)");
        GINCodePanel.add(GINCodeHelpLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(GINCodePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(Separator2, gridBagConstraints);

        StartDateLabel.setText("Start date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(StartDateLabel, gridBagConstraints);

        StartDatePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        StartDayComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        StartDatePanel.add(StartDayComboBox);

        StartMonthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        StartMonthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartMonthComboBoxActionPerformed(evt);
            }
        });
        StartDatePanel.add(StartMonthComboBox);

        StartYearComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        StartYearComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartYearComboBoxActionPerformed(evt);
            }
        });
        StartDatePanel.add(StartYearComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(StartDatePanel, gridBagConstraints);

        DurationLabel.setText("Duration:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(DurationLabel, gridBagConstraints);

        DurationPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        DurationTextField.setColumns(5);
        DurationTextField.setText("1");
        DurationPanel.add(DurationTextField);

        DurationTypeButtonGroup.add(DayDurationRadioButton);
        DayDurationRadioButton.setSelected(true);
        DayDurationRadioButton.setText("Days");
        DayDurationRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        DayDurationRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        DurationPanel.add(DayDurationRadioButton);

        DurationTypeButtonGroup.add(MonthDurationRadioButton);
        MonthDurationRadioButton.setText("Months");
        MonthDurationRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        MonthDurationRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        DurationPanel.add(MonthDurationRadioButton);

        DurationTypeButtonGroup.add(YearDurationRadioButton);
        YearDurationRadioButton.setText("Years");
        YearDurationRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        YearDurationRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        DurationPanel.add(YearDurationRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(DurationPanel, gridBagConstraints);

        ErrorControlLabel.setText("Error control:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(ErrorControlLabel, gridBagConstraints);

        ErrorControlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ShowErrorsCheckBox.setSelected(true);
        ShowErrorsCheckBox.setText("Show errors during export");
        ShowErrorsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ShowErrorsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ErrorControlPanel.add(ShowErrorsCheckBox);

        ShowWarningsCheckBox.setSelected(true);
        ShowWarningsCheckBox.setText("Show warnings during export");
        ShowWarningsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ShowWarningsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ErrorControlPanel.add(ShowWarningsCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ErrorControlPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(Separator3, gridBagConstraints);

        WriteDataButton.setText("Write data");
        WriteDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WriteDataButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(WriteDataButton);

        ExportOptionsButton.setText("Export options...");
        ExportOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportOptionsButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(ExportOptionsButton);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(CloseButton);

        HelpButton.setText("Help");
        HelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(HelpButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(ButtonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void HelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand ("show_help&from_export_window&Windows.html#Export");
    }//GEN-LAST:event_HelpButtonActionPerformed

    private void FileLengthInfiniteRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileLengthInfiniteRadioButtonActionPerformed
        checkZipGroup();
    }//GEN-LAST:event_FileLengthInfiniteRadioButtonActionPerformed

    private void FileLengthYearRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileLengthYearRadioButtonActionPerformed
        checkZipGroup();
    }//GEN-LAST:event_FileLengthYearRadioButtonActionPerformed

    private void FileLengthMonthRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileLengthMonthRadioButtonActionPerformed
        checkZipGroup();
    }//GEN-LAST:event_FileLengthMonthRadioButtonActionPerformed

    private void FileLengthDayRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileLengthDayRadioButtonActionPerformed
        checkZipGroup();
    }//GEN-LAST:event_FileLengthDayRadioButtonActionPerformed

    private void DailyDataRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DailyDataRadioButtonActionPerformed
        callDataFormatCallback(evt);
    }//GEN-LAST:event_DailyDataRadioButtonActionPerformed

    private void HourlyDataRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HourlyDataRadioButtonActionPerformed
        callDataFormatCallback(evt);
    }//GEN-LAST:event_HourlyDataRadioButtonActionPerformed

    private void MinuteDataRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MinuteDataRadioButtonActionPerformed
        callDataFormatCallback(evt);
    }//GEN-LAST:event_MinuteDataRadioButtonActionPerformed

    private void WDCRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WDCRadioButtonActionPerformed
      JOptionPane.showMessageDialog(null, "Known bug in Exporting to WDC format\n" +
              "See release notes in Help","Export Information Message", JOptionPane.INFORMATION_MESSAGE);
  
        // set up data type buttons
        MinuteDataRadioButton.setEnabled(true);
        HourlyDataRadioButton.setEnabled(true);
        if (DailyDataRadioButton.isSelected())
            HourlyDataRadioButton.setSelected(true);
        DailyDataRadioButton.setEnabled(false);

        // set up file length buttons
        FileLengthYearRadioButton.setEnabled(true);
        FileLengthInfiniteRadioButton.setEnabled(true);
        if (MinuteDataRadioButton.isSelected())
        {
            FileLengthMonthRadioButton.setEnabled(true);
            FileLengthMonthRadioButton.setSelected(true);
        }
        else
        {
            FileLengthYearRadioButton.setSelected(true);
            FileLengthMonthRadioButton.setEnabled(false);
        }
        FileLengthDayRadioButton.setEnabled(false);
        
        // set up zip group buttons
        checkZipGroup();
    }//GEN-LAST:event_WDCRadioButtonActionPerformed

    private void PlainTextRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlainTextRadioButtonActionPerformed
        // set up data type buttons
        MinuteDataRadioButton.setEnabled(true);
        HourlyDataRadioButton.setEnabled(true);
        DailyDataRadioButton.setEnabled(true);
        
        // set up file length buttons
        FileLengthDayRadioButton.setEnabled(true);
        FileLengthMonthRadioButton.setEnabled(true);
        FileLengthYearRadioButton.setEnabled(true);
        FileLengthInfiniteRadioButton.setEnabled(true);
        FileLengthInfiniteRadioButton.setSelected(true);
        
        // set up zip group buttons
        checkZipGroup();
    }//GEN-LAST:event_PlainTextRadioButtonActionPerformed

    private void IntermagnetRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IntermagnetRadioButtonActionPerformed
        // set up data type buttons
        MinuteDataRadioButton.setEnabled(true);
        MinuteDataRadioButton.setSelected(true);
        HourlyDataRadioButton.setEnabled(false);
        DailyDataRadioButton.setEnabled(false);
        
        // set up file length buttons
        FileLengthDayRadioButton.setEnabled(true);
        FileLengthMonthRadioButton.setEnabled(false);
        FileLengthYearRadioButton.setEnabled(false);
        FileLengthInfiniteRadioButton.setEnabled(false);
        FileLengthDayRadioButton.setSelected(true);
        
        // set up zip group buttons
        checkZipGroup();        
    }//GEN-LAST:event_IntermagnetRadioButtonActionPerformed

    private void Iaga2002RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Iaga2002RadioButtonActionPerformed
        // set up data type buttons
        MinuteDataRadioButton.setEnabled(true);
        HourlyDataRadioButton.setEnabled(true);
        DailyDataRadioButton.setEnabled(true);
        
        // set up file length buttons
        FileLengthYearRadioButton.setEnabled(true);
        FileLengthInfiniteRadioButton.setEnabled(true);
        if (MinuteDataRadioButton.isSelected())
        {
            FileLengthDayRadioButton.setEnabled(true);
            FileLengthMonthRadioButton.setEnabled(true);
            FileLengthDayRadioButton.setSelected(true);
        }
        else if (HourlyDataRadioButton.isSelected())
        {
            FileLengthMonthRadioButton.setEnabled(true);
            FileLengthMonthRadioButton.setSelected(true);
            FileLengthDayRadioButton.setEnabled(false);
        }
        else
        {
            FileLengthYearRadioButton.setSelected(true);
            FileLengthDayRadioButton.setEnabled(false);
            FileLengthMonthRadioButton.setEnabled(false);
        }
        
        // set up zip group buttons
        checkZipGroup();        
    }//GEN-LAST:event_Iaga2002RadioButtonActionPerformed

    private void StartYearComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartYearComboBoxActionPerformed
        if (do_date_updates)
        {
            if (year_list != null) buildDayList();
        }
    }//GEN-LAST:event_StartYearComboBoxActionPerformed

    private void StartMonthComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartMonthComboBoxActionPerformed
        if (do_date_updates)
        {
            if (year_list != null) buildDayList();
        }
    }//GEN-LAST:event_StartMonthComboBoxActionPerformed

    private void WriteDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WriteDataButtonActionPerformed
        int count, status, data_type_flag, duration, duration_units, data_format;
        int selectedDay, selectedMonth, selectedYear, fileLength, zipType;
        String obsName, string, gin_code;
        Object obsNames [];
        CDObservatoryIterator obsy_iterator;
        CDObservatoryIterator.ObservatoryInfo info, obsy_info [] = null;
        
        // get the selected observatories
        obsNames = ObservatoryList.getSelectedValues ();
        if (obsNames.length == 0)
        {
            JOptionPane.showMessageDialog (this, "Please select an observatory",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // go through the list of selected observatory names and build a
        // list of observatory info objects
        obsy_info = new CDObservatoryIterator.ObservatoryInfo [obsNames.length];
        if (database == null)
            obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
        else
            obsy_iterator = database.GetObservatoryIterator();
        for (count = 0; count < obsNames.length; count ++)
        {
            obsName = obsNames [count].toString ();
            // build the list of observatories
            for (info = obsy_iterator.GetFirstObservatory(); info != null;
                 info = obsy_iterator.GetNextObservatory())
            {
                // find observatory from string
                if (info.GetDisplayName ().equals (obsName)) obsy_info [count] = info;
            }
         }

        // get the currently entered values for date and time
        selectedDay = StartDayComboBox.getSelectedIndex () + 1;
        selectedMonth = StartMonthComboBox.getSelectedIndex () + 1;
        selectedYear = ((CDYear) year_list.elementAt (StartYearComboBox.getSelectedIndex ())).GetYear ();
            
        // get the units in which to write data
        if (MinuteDataRadioButton.isSelected ()) data_type_flag = ExportThread.PERIOD_MINUTE;
        else if (HourlyDataRadioButton.isSelected ()) data_type_flag = ExportThread.PERIOD_HOUR;
        else if (DailyDataRadioButton.isSelected ()) data_type_flag = ExportThread.PERIOD_DAY;
        else
        {
            JOptionPane.showMessageDialog (this, "Select a data type (minute mean, hourly mean, ...)",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;                
        }

        // get the units for duration
        if (DayDurationRadioButton.isSelected ()) duration_units = DateUtils.DURATION_TYPE_DAYS;
        else if (MonthDurationRadioButton.isSelected ()) duration_units = DateUtils.DURATION_TYPE_MONTHS;
        else if (YearDurationRadioButton.isSelected ()) duration_units = DateUtils.DURATION_TYPE_YEARS;
        else
        {
            JOptionPane.showMessageDialog (this, "Select a duration type (day, month, ...)",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;                
        }
            
        // get the data format
        if (Iaga2002RadioButton.isSelected()) data_format = ExportThread.FORMAT_IAGA2002;
        else if (IntermagnetRadioButton.isSelected()) data_format = ExportThread.FORMAT_INTERMAGNET;
        else if (PlainTextRadioButton.isSelected()) data_format = ExportThread.FORMAT_PLAIN_TEXT;
        else if (WDCRadioButton.isSelected()) data_format = ExportThread.FORMAT_WDC;
        else
        {
            JOptionPane.showMessageDialog (this, "Select a data format",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;                
        }
            
        // if the format is intermagnet, check the gin code
        if (data_format == ExportThread.FORMAT_INTERMAGNET)
        {
            gin_code = GINCodeTextField.getText().trim();
            if (gin_code.length() != 3)
            {
                JOptionPane.showMessageDialog (this, "A 3-digit GIN code must be entered for INTERMAGNET data export",
                                               "Error", JOptionPane.ERROR_MESSAGE);
                return;                
            }
        }
        else gin_code = null;
        
        // get the file length code
        if (FileLengthDayRadioButton.isSelected()) fileLength = DateUtils.DURATION_TYPE_DAYS;
        else if (FileLengthMonthRadioButton.isSelected()) fileLength = DateUtils.DURATION_TYPE_MONTHS;
        else if (FileLengthYearRadioButton.isSelected()) fileLength = DateUtils.DURATION_TYPE_YEARS;
        else if (FileLengthInfiniteRadioButton.isSelected()) fileLength = DateUtils.DURATION_TYPE_INFINITE;
        else
        {
            JOptionPane.showMessageDialog (this, "Select a file length",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;                
        }

        // get the zip type code
        if (ZipLengthNoneRadioButton.isSelected()) zipType = ExportThread.ZIP_NONE;
        else if (ZipLengthDayRadioButton.isSelected()) zipType = ExportThread.ZIP_DAILY;
        else if (ZipLengthMonthRadioButton.isSelected()) zipType = ExportThread.ZIP_MONTHLY;
        else if (ZipLengthYearRadioButton.isSelected()) zipType = ExportThread.ZIP_YEARLY;
        else if (ZipLengthObservatoryRadioButton.isSelected()) zipType = ExportThread.ZIP_OBSERVATORY;
        else if (ZipLengthAllDataRadioButton.isSelected()) zipType = ExportThread.ZIP_ALL;
        else
        {
            JOptionPane.showMessageDialog (this, "Select a zip grouping",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            return;                
        }
        
        // get the message control flags
        show_warning_messages = ShowWarningsCheckBox.isSelected();
        show_error_messages = ShowErrorsCheckBox.isSelected();

        // check what was typed in the duration box
        try
        {
            duration = Integer.parseInt (DurationTextField.getText ());
            if (duration <= 0)
            {
                JOptionPane.showMessageDialog (this, "Duration must be greater than zero", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog (this, "The value entered for duration must be a positive integer",
                                           "Error", JOptionPane.ERROR_MESSAGE);
            DurationTextField.requestFocusInWindow ();
            DurationTextField.selectAll ();
            return;
        }

        // select the save directory
        GlobalObjects.export_dir_chooser.setCurrentDirectory(GlobalObjects.export_output_dir);
        if (GlobalObjects.export_dir_chooser.showDialog (null, null) == JFileChooser.APPROVE_OPTION)
        {
            GlobalObjects.export_output_dir = GlobalObjects.export_dir_chooser.getSelectedFile ();

            // flag that the progress dialog is not available
            progress = null;
                
            // Exporting data is done from a separate thread.
            // This is to allow original thread to listen for Cancel
            // being pressed on progress dialog.
            try
            {
                export_data_thread = new Thread (new ExportThread (obsy_info, selectedDay,
                                                                   selectedMonth, selectedYear,
                                                                   data_type_flag, data_format, gin_code,
                                                                   duration, duration_units,
                                                                   fileLength, zipType,
                                                                   use_zip, use_plain, base_dir,
                                                                   this),
                                                                   "Export Data Thread");
                progress = new ProgressDialog (this, "Progress", "Initialising data export...", 
                                               "Total progress: ", "Current observatory: ", 
                                               true, true);
                export_data_thread.start ();
                
                // display progress dialog as modal (blocking) dialog - when
                // progress.setVisible() returns the export has completed
                progress.setLocationRelativeTo (this);
                progress.setVisible (true);
                progress = null;
            }
            catch (IOException except)
            {
                JOptionPane.showMessageDialog (this, except.getMessage (),
                                               "Error", JOptionPane.ERROR_MESSAGE);
                // next line added 01.04.09 JE
                progress.dispose();
                return;
            }
        }
    }//GEN-LAST:event_WriteDataButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog ();
    }//GEN-LAST:event_formWindowClosing

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        closeDialog ();
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void ExportOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportOptionsButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand("raise_export_options_dialog&from_export_window");
    }//GEN-LAST:event_ExportOptionsButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton CloseButton;
    private javax.swing.JRadioButton DailyDataRadioButton;
    private javax.swing.JLabel DataFormatLabel;
    private javax.swing.JPanel DataFormatPanel;
    private javax.swing.ButtonGroup DataTypeButtonGroup;
    private javax.swing.JLabel DataTypeLabel;
    private javax.swing.JPanel DataTypePanel;
    private javax.swing.JRadioButton DayDurationRadioButton;
    private javax.swing.JLabel DurationLabel;
    private javax.swing.JPanel DurationPanel;
    private javax.swing.JTextField DurationTextField;
    private javax.swing.ButtonGroup DurationTypeButtonGroup;
    private javax.swing.JLabel ErrorControlLabel;
    private javax.swing.JPanel ErrorControlPanel;
    private javax.swing.JButton ExportOptionsButton;
    private javax.swing.ButtonGroup FileLengthButtonGroup;
    private javax.swing.JRadioButton FileLengthDayRadioButton;
    private javax.swing.JRadioButton FileLengthInfiniteRadioButton;
    private javax.swing.JLabel FileLengthLabel;
    private javax.swing.JRadioButton FileLengthMonthRadioButton;
    private javax.swing.JPanel FileLengthPanel;
    private javax.swing.JRadioButton FileLengthYearRadioButton;
    private javax.swing.ButtonGroup FormatTypeButtonGroup;
    private javax.swing.JLabel GINCodeHelpLabel;
    private javax.swing.JLabel GINCodeLabel;
    private javax.swing.JPanel GINCodePanel;
    private javax.swing.JTextField GINCodeTextField;
    private javax.swing.JButton HelpButton;
    private javax.swing.JRadioButton HourlyDataRadioButton;
    private javax.swing.JRadioButton Iaga2002RadioButton;
    private javax.swing.JRadioButton IntermagnetRadioButton;
    private javax.swing.JRadioButton MinuteDataRadioButton;
    private javax.swing.JRadioButton MonthDurationRadioButton;
    private javax.swing.JLabel ObservatoryHelpLabel;
    private javax.swing.JLabel ObservatoryLabel;
    private javax.swing.JList ObservatoryList;
    private javax.swing.JScrollPane ObservatoryListScrollPane;
    private javax.swing.JRadioButton PlainTextRadioButton;
    private javax.swing.JSeparator Separator1;
    private javax.swing.JSeparator Separator2;
    private javax.swing.JSeparator Separator3;
    private javax.swing.JCheckBox ShowErrorsCheckBox;
    private javax.swing.JCheckBox ShowWarningsCheckBox;
    private javax.swing.JLabel StartDateLabel;
    private javax.swing.JPanel StartDatePanel;
    private javax.swing.JComboBox StartDayComboBox;
    private javax.swing.JComboBox StartMonthComboBox;
    private javax.swing.JComboBox StartYearComboBox;
    private javax.swing.JRadioButton WDCRadioButton;
    private javax.swing.JButton WriteDataButton;
    private javax.swing.JRadioButton YearDurationRadioButton;
    private javax.swing.ButtonGroup ZipButtonGroup;
    private javax.swing.JLabel ZipLabel;
    private javax.swing.JRadioButton ZipLengthAllDataRadioButton;
    private javax.swing.JRadioButton ZipLengthDayRadioButton;
    private javax.swing.JRadioButton ZipLengthMonthRadioButton;
    private javax.swing.JRadioButton ZipLengthNoneRadioButton;
    private javax.swing.JRadioButton ZipLengthObservatoryRadioButton;
    private javax.swing.JRadioButton ZipLengthYearRadioButton;
    private javax.swing.JPanel ZipPanel;
    // End of variables declaration//GEN-END:variables

    /////////////////////////////////////////////////////////////////////////////////
    // implementation of ExportThread.progressAndCancel
    /////////////////////////////////////////////////////////////////////////////////
    /** display progress of the export */
    public void showProgress (String message, String obsy,
                              int total_percent, int operation_percent)
    {
      if (progress != null)
      {
        progress.update (message);
        progress.updateTotal (total_percent, 
                              Integer.toString (total_percent) + "% (" + obsy + ")");
        progress.updateOperation (operation_percent);
      }
    }

    /** check for cancel of export */
    public boolean isCancelled ()
    {
        if (progress == null) return false;
        return progress.isCancelled();
    }
    
    /** show any error messages from the export thread */
    public void registerError (String message, int type)
    {
        int status;
        ExportMessageDialog dialog;
        
        switch (type)
        {
        case ExportThread.progressAndCancel.ERROR_WARNING:
            if (show_warning_messages)
            {
                if (progress != null)
                    dialog = new ExportMessageDialog (progress, true, "Export Warning", message, 
                                                      show_warning_messages, show_error_messages);
                else
                    dialog = new ExportMessageDialog (this, true, "Export Warning", message, 
                                                      show_warning_messages, show_error_messages);
            }
            else
                dialog = null;
            break;
        case ExportThread.progressAndCancel.ERROR_ERROR:
            if (show_error_messages)
            {
                if (progress != null)
                    dialog = new ExportMessageDialog (progress, true, "Export Error", message, 
                                                      show_warning_messages, show_error_messages);
                else
                    dialog = new ExportMessageDialog (this, true, "Export Error", message, 
                                                      show_warning_messages, show_error_messages);
            }
            else
                dialog = null;
            break;
        default:
            JOptionPane.showMessageDialog (this, message, "Fatal Export Error", JOptionPane.ERROR_MESSAGE);
            dialog = null;
            if (progress != null) progress.setCancelled();
        }
        if (dialog != null)
        {
            dialog.setVisible(true);
            show_warning_messages = dialog.isContinueShowingWarnings();
            show_error_messages = dialog.isContinueShowingErrors();
            if (dialog.isStopPressed() && (progress != null)) progress.setCancelled();
        }
    }

    /** notify that export is complete */
    public void allDone ()
    {
        if (progress != null) progress.setVisible (false);
    }

    /** set the use zip and use plain flags */
    public void setUseZipAndPlain (boolean use_zip, boolean use_plain)
    {
        this.use_zip = use_zip;
        this.use_plain = use_plain;
    }
    
    /** set the base directory of the database that the export 
     * is restricted to using - this call also deselects any selected
     * observatories and the current year/month
     * @param base_dir - if the export should be restricted to one data source
     *        then set this to the path of the data source, otherwise set it to null */
    public void setBaseDir (String base_dir)
    {
        // weed out situations where no change is required
        if (base_dir == null)
        {
            if (this.base_dir == null) return;
        }
        else if (base_dir.equals(this.base_dir)) return;
        
        // reload the dialog
        initComponents2 (null, -1, -1, base_dir);
    }
    
    /** programtically set a station code */
    public void setObservatory (String obs_code)
    {
        int count;
        String string;
        CDObservatoryIterator obsy_iterator;
        CDObservatoryIterator.ObservatoryInfo info;

        if (database == null)
            obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
        else
            obsy_iterator = database.GetObservatoryIterator();
        for (info = obsy_iterator.GetFirstObservatory(); 
             info != null;  
             info = obsy_iterator.GetNextObservatory())
        {
            if (info.GetObservatoryCode().equalsIgnoreCase(obs_code))
            {
                for (count=0; count<ObservatoryList.getModel().getSize(); count++)
                {
                    string = (String) ObservatoryList.getModel().getElementAt(count);
                    if (string.equalsIgnoreCase(info.GetDisplayName()))
                    {
                        ObservatoryList.setSelectedIndex (count);
                        ObservatoryList.ensureIndexIsVisible (count);
                        return;
                    }
                }
            }
        }
    }
    
    public void setDate (int year, int month)
    {
        int count;
        Integer year_element, month_element;
        
        for (count=0; count<StartYearComboBox.getModel().getSize(); count++)
        {
            year_element = (Integer) StartYearComboBox.getModel().getElementAt(count);
            if (year_element.intValue() == year)
            {
                StartYearComboBox.setSelectedIndex(count);
                if (month < 0 || month > 11) month = 0;
                StartMonthComboBox.setSelectedIndex(month);
                buildDayList ();
                return;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // private code below here
    /////////////////////////////////////////////////////////////////////////////////
    private void buildDayList ()
    {
        int n_days, n, selectedDay, selectedMonth, selectedYear;

        // rebuild the day list depending on the month and year selected
        selectedDay = StartDayComboBox.getSelectedIndex () + 1;
        selectedMonth = StartMonthComboBox.getSelectedIndex () + 1;
        n = StartYearComboBox.getSelectedIndex ();
        if (n < 0) selectedYear = 0;
        else selectedYear = ((CDYear) year_list.elementAt (n)).GetYear ();
        n_days = DateUtils.daysInMonth (selectedMonth -1, selectedYear);

        // rebuild day list
        StartDayComboBox.removeAllItems ();
        for (n = 0; n < n_days; n++)
        {
            StartDayComboBox.addItem (new Integer (n+1));
        }
        if (selectedDay > n_days || selectedDay <= 0) selectedDay = 1;
        StartDayComboBox.setSelectedIndex (selectedDay - 1);
    }
    
    /** Closes the dialog */
    private void closeDialog () 
    {
        setVisible(false);
        dispose();
    }
    
    /** check zip grouping is appropriate for file length */
    private void checkZipGroup ()
    {
        if (FileLengthDayRadioButton.isSelected())
        {
            ZipLengthDayRadioButton.setEnabled(true);
            ZipLengthMonthRadioButton.setEnabled(true);
            ZipLengthYearRadioButton.setEnabled(true);
        }
        else if (FileLengthMonthRadioButton.isSelected())
        {
            ZipLengthMonthRadioButton.setEnabled(true);
            ZipLengthYearRadioButton.setEnabled(true);
            if (ZipLengthDayRadioButton.isSelected())
                ZipLengthMonthRadioButton.setSelected (true);
            ZipLengthDayRadioButton.setEnabled(false);
        }
        else if (FileLengthYearRadioButton.isSelected())
        {
            ZipLengthYearRadioButton.setEnabled(true);
            if (ZipLengthDayRadioButton.isSelected() || 
                ZipLengthMonthRadioButton.isSelected())
                ZipLengthYearRadioButton.setSelected (true);
            ZipLengthDayRadioButton.setEnabled(false);
            ZipLengthMonthRadioButton.setEnabled(false);
        }
        else if (FileLengthInfiniteRadioButton.isSelected())
        {
            if (ZipLengthDayRadioButton.isSelected() || 
                ZipLengthMonthRadioButton.isSelected() ||
                ZipLengthYearRadioButton.isSelected())
                ZipLengthObservatoryRadioButton.setSelected (true);
            ZipLengthDayRadioButton.setEnabled(false);
            ZipLengthMonthRadioButton.setEnabled(false);
            ZipLengthYearRadioButton.setEnabled(false);
        }
    }

    private void callDataFormatCallback (java.awt.event.ActionEvent evt)
    {
        if (Iaga2002RadioButton.isSelected()) Iaga2002RadioButtonActionPerformed (evt);
        else if (IntermagnetRadioButton.isSelected()) IntermagnetRadioButtonActionPerformed (evt);
        else if (PlainTextRadioButton.isSelected()) PlainTextRadioButtonActionPerformed (evt);
        else if (WDCRadioButton.isSelected()) WDCRadioButtonActionPerformed (evt);        
    }
    
}
