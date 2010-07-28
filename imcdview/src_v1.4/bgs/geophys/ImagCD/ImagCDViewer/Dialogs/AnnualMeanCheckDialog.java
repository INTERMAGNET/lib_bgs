/*
 * AnnualMeanCheckDialog.java
 *
 * Created on 29 January 2009, 15:46
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataDay;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataMonth;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatory;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.ImagCD.YearMeanFile;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Swing.SwingUtils;
import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  smf
 */
public class AnnualMeanCheckDialog extends javax.swing.JDialog 
{
    
    private String obsy_code;
    private int current_year_index;
    private boolean use_zip;
    private boolean use_plain;
    private Vector<Integer> year_list;
    private DefaultTableModel table_model;
    private boolean do_year_cb_action;
    private String base_dir;
    private CDDatabase database;
  
   /** creates a new AnnualMeanCheckDialog dialog.
    * @param parent the owner of this dialog
    * @param obsy_code the observatory code for the file required
    * @param year the year for the file required
    * @param use_zip if true read from zip files, otherwise don't
    * @param use_plain if true read from non-zip files, otherwise don't
    * @param base_dir - if the viewer should be restricted to one data source
    *        then set this to the path of the data source, otherwise set it to null */
    public AnnualMeanCheckDialog (java.awt.Frame parent, boolean modal,
                             String obsy_code, int year,
                             boolean use_zip, boolean use_plain, String base_dir) 
    {
        super(parent, modal);
        
        this.obsy_code = obsy_code;
        this.use_zip = use_zip;
        this.use_plain = use_plain;
        
        initComponents();
        initComponents2(year, base_dir);
    }
    
   /** creates a new AnnualMeanCheckDialog dialog.
    * @param parent the owner of this dialog
    * @param obsy_code the observatory code for the file required
    * @param year the year for the file required
    * @param use_zip if true read from zip files, otherwise don't
    * @param use_plain if true read from non-zip files, otherwise don't
    * @param base_dir - if the viewer should be restricted to one data source
    *        then set this to the path of the data source, otherwise set it to null */
    public AnnualMeanCheckDialog (java.awt.Dialog parent, boolean modal,
                             String obsy_code, int year,
                             boolean use_zip, boolean use_plain, String base_dir) 
    {
        super(parent, modal);
        
        this.obsy_code = obsy_code;
        this.use_zip = use_zip;
        this.use_plain = use_plain;
        
        initComponents();
        initComponents2(year, base_dir);
    }
    
    private void initComponents2 (int year, String base_dir)
    {
        int count;
        
        AnnualMeanScrollPane.setPreferredSize(AnnualMeanTable.getSize());
        SwingUtils.ResizeTableColumnWidths (AnnualMeanTable, 15, 300, 10, 17, 10, 9, 9, 9, 9, 9, 9, 9);
        
        do_year_cb_action = true;
        table_model = (DefaultTableModel) AnnualMeanTable.getModel();        
        
        if (base_dir == null)
            database = null;
        else
            database = CDMisc.findDataSource(base_dir);
        if (database == null)
            this.base_dir = null;
        else
            this.base_dir = base_dir;
        
        // make list of available years for this observatory
        do_year_cb_action = false;
        if (database == null)
            year_list = CDMisc.makeYearList (obsy_code);
        else
            year_list = CDMisc.makeYearList (obsy_code, database);
        current_year_index = -1;
        for (count = 0; count < year_list.size(); count++)
        {
            YearComboBox.addItem (year_list.elementAt (count).toString ());
            if (year == year_list.elementAt(count).intValue())
                current_year_index = count;
        }
        if (current_year_index < 0) current_year_index = year_list.size() - 1;
        do_year_cb_action = true;

        // display the current year
        updateDisplay ();
        
        // show the dialog
        getRootPane().setDefaultButton (CloseButton);
        this.pack ();
        this.setVisible (true);
    }

    /** updateDisplay - update the display after a new year is selected */
    public void updateDisplay ()
    {
        int n, current_year;
        String title;
        Cursor old_cursor;

        // get the year
        current_year = year_list.get (current_year_index).intValue();
        
        // set a busy cursor
        old_cursor = getCursor();
        setCursor(GlobalObjects.wait_cursor);

        // load the data
        while (table_model.getRowCount() > 0) table_model.removeRow(0);
        if (!loadData ())
            JOptionPane.showMessageDialog (this, "Error loading data");
        setCursor (old_cursor);

        // set dialogue 
        title = "Annual Mean Comparison: Data for " + CDMisc.getObservatoryName (obsy_code) + " " + current_year;
        if (base_dir != null)
            title += " [restricted to " + base_dir + "]";
        setTitle (title);
        StationLabel.setText ("<html><b>Station: " + obsy_code + "</html>");
        YearLabel.setText ("<html><b>Year: " + current_year + "</html>");

        // enable and disable components according to the year selected
        if (current_year == ((Integer)year_list.elementAt (0)).intValue ())
            PreviousYearButton.setEnabled (false);
        else PreviousYearButton.setEnabled (true);
        if (current_year == ((Integer)year_list.elementAt (year_list.size()-1)).intValue ())
            NextYearButton.setEnabled (false);
        else NextYearButton.setEnabled (true);

        // set year displayed in drop-down list - remove action listener first
        for (n = 0; n < year_list.size(); n++)
        {
            if (((Integer) year_list.elementAt (n)).intValue () == current_year)
            {
                do_year_cb_action = false;
                YearComboBox.setSelectedIndex (n);
                do_year_cb_action = true;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        StatusPanel = new javax.swing.JPanel();
        StationLabel = new javax.swing.JLabel();
        YearLabel = new javax.swing.JLabel();
        AnnualMeanScrollPane = new javax.swing.JScrollPane();
        AnnualMeanTable = new javax.swing.JTable();
        ControlPanel = new javax.swing.JPanel();
        KeyLabel = new javax.swing.JLabel();
        KeyLabel1 = new javax.swing.JLabel();
        KeyLabel2 = new javax.swing.JLabel();
        KeyLabel3 = new javax.swing.JLabel();
        KeyLabel4 = new javax.swing.JLabel();
        CloseButton = new javax.swing.JButton();
        YearSelectPanel = new javax.swing.JPanel();
        PreviousYearButton = new javax.swing.JButton();
        YearComboBox = new javax.swing.JComboBox();
        NextYearButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Annual mean comparison");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        StationLabel.setText("<html><b>Station: xxx</html>");
        StatusPanel.add(StationLabel);

        YearLabel.setText("<html><b>Year: xxxx</html>");
        StatusPanel.add(YearLabel);

        getContentPane().add(StatusPanel, java.awt.BorderLayout.NORTH);

        AnnualMeanTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Year", "Source", "Type", "D (min)", "I (min)", "H (nT)", "X (nT)", "Y (nT)", "Z (nT)", "F (nT)"
            }
        ));
        AnnualMeanTable.setFillsViewportHeight(true);
        AnnualMeanTable.setMinimumSize(new java.awt.Dimension(200, 50));
        AnnualMeanTable.setPreferredSize(new java.awt.Dimension(800, 100));
        AnnualMeanScrollPane.setViewportView(AnnualMeanTable);

        getContentPane().add(AnnualMeanScrollPane, java.awt.BorderLayout.CENTER);

        ControlPanel.setLayout(new java.awt.GridBagLayout());

        KeyLabel.setText("Key:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        ControlPanel.add(KeyLabel, gridBagConstraints);

        KeyLabel1.setText("<html>\nElements shown <u>underlined</u> are the ones used to calculate the mean.\n</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ControlPanel.add(KeyLabel1, gridBagConstraints);

        KeyLabel2.setText("<html>\nWhere elements are shown in <b>bold</b> there is a discrepancy between the elements.\n</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ControlPanel.add(KeyLabel2, gridBagConstraints);

        KeyLabel3.setText("<html>\nWhere elements are shown in <i>italics</i> it is not possible to check for a discrepancy.\n</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ControlPanel.add(KeyLabel3, gridBagConstraints);

        KeyLabel4.setText("<html>\n&nbsp;&nbsp;&nbsp;&nbsp;\nWhere a discrepancy is found, the elements are recalculated and displayed on the following line.\n</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ControlPanel.add(KeyLabel4, gridBagConstraints);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ControlPanel.add(CloseButton, gridBagConstraints);

        PreviousYearButton.setText("Previous Year");
        PreviousYearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreviousYearButtonActionPerformed(evt);
            }
        });
        YearSelectPanel.add(PreviousYearButton);

        YearComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YearComboBoxActionPerformed(evt);
            }
        });
        YearSelectPanel.add(YearComboBox);

        NextYearButton.setText("Next Year");
        NextYearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextYearButtonActionPerformed(evt);
            }
        });
        YearSelectPanel.add(NextYearButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ControlPanel.add(YearSelectPanel, gridBagConstraints);

        getContentPane().add(ControlPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void PreviousYearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreviousYearButtonActionPerformed
        current_year_index --;
        if (current_year_index < 0) current_year_index = 0;
        updateDisplay();
    }//GEN-LAST:event_PreviousYearButtonActionPerformed

    private void NextYearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextYearButtonActionPerformed
        current_year_index ++;
        if (current_year_index >= year_list.size()) current_year_index = year_list.size() -1;
        updateDisplay();        
    }//GEN-LAST:event_NextYearButtonActionPerformed

    private void YearComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YearComboBoxActionPerformed
        if (do_year_cb_action)
        {
            current_year_index = YearComboBox.getSelectedIndex();
            updateDisplay();
        }
    }//GEN-LAST:event_YearComboBoxActionPerformed
    
    private void closeDialog ()
    {
        this.setVisible(false);
        this.dispose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane AnnualMeanScrollPane;
    private javax.swing.JTable AnnualMeanTable;
    private javax.swing.JButton CloseButton;
    private javax.swing.JPanel ControlPanel;
    private javax.swing.JLabel KeyLabel;
    private javax.swing.JLabel KeyLabel1;
    private javax.swing.JLabel KeyLabel2;
    private javax.swing.JLabel KeyLabel3;
    private javax.swing.JLabel KeyLabel4;
    private javax.swing.JButton NextYearButton;
    private javax.swing.JButton PreviousYearButton;
    private javax.swing.JLabel StationLabel;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JComboBox YearComboBox;
    private javax.swing.JLabel YearLabel;
    private javax.swing.JPanel YearSelectPanel;
    // End of variables declaration//GEN-END:variables
    
   /** load data for the year specified by the flag current_year. 
    * @return - true if data was loaded, else false */
    private boolean loadData ()
    {
        int month_count, day_count, count, comp, current_year;
        int n_day_samples [], n_hour_samples [], n_min_samples [];
        boolean incomplete;
        double day_accumulator [], hour_accumulator [], min_accumulator [];
        CDDataMonth month_data [];
        CDDataDay day_data;
        GeomagAbsoluteValue mean, values [];
        String recorded_elements;
        Vector obsy_list;
        CDObservatory observatory;
        File os_file;
        YearMeanFile yearmean_file;
        YearMean year_mean;
  
        // get the year
        current_year = year_list.get (current_year_index).intValue();
        
        // retrieve one year of data
        month_data = CDMisc.findData (obsy_code, current_year, -1, use_zip, use_plain, base_dir);

        // find the yearmean file -
        // since files are in all segments, take first observatory object
        if (database == null)
            obsy_list = CDMisc.getObservatoryList (obsy_code, current_year);
        else
            obsy_list = CDMisc.getObservatoryList (obsy_code, current_year, database);
        if (obsy_list == null) observatory = null;
        else observatory = (CDObservatory) obsy_list.elementAt (0);
        if (observatory == null) os_file = null;
        else os_file = observatory.GetFile (CDObservatory.FILE_YEAR_MEAN);

        // load the means from the year mean file
        try
        {
            if (os_file != null)
            {
                yearmean_file = new YearMeanFile (os_file);
                for (count=0; count<yearmean_file.getNMeans(); count++)
                {
                    year_mean = yearmean_file.getMean(count);
                    if ((int) year_mean.getYear() == current_year &&
                        ((year_mean.getType() == YearMean.YearMeanType.ALL_DAYS) ||
                         (year_mean.getType() == YearMean.YearMeanType.INCOMPLETE)))
                        addMean (year_mean);
                }
            }
        }
        catch (FileNotFoundException e) { }
        catch (IOException e) { }
        catch (ParseException e) { }
            
        // initialise mean calculation
        recorded_elements = null;
        day_accumulator = new double [3];
        hour_accumulator = new double [3];
        min_accumulator = new double [3];
        n_day_samples = new int [3];
        n_hour_samples = new int [3];
        n_min_samples = new int [3];
        for (count=0; count<3; count++)
        {
            day_accumulator [count] = hour_accumulator [count] = min_accumulator [count] = 0.0;
            n_day_samples [count] = n_hour_samples [count] = n_min_samples [count];
        }

        // calculate means from data
        for (month_count = 0; month_count < 12; month_count++)
        {
            for (day_count = 0; day_count < DateUtils.daysInMonth(month_count, current_year); day_count++)
            {
                // get the day data
                if (month_data [month_count] == null) day_data = null;
                else day_data = month_data [month_count].getDayData (day_count);
                
                // check the orientation
                if (day_data != null)
                {
                    if (recorded_elements == null)
                        recorded_elements = day_data.getCompOrientation();
                    else if (! recorded_elements.equalsIgnoreCase(day_data.getCompOrientation()))
                        day_data = null;
                }
                    
                // accumulate means
                if (day_data != null)
                {
                    values = day_data.getMinuteMeans();
                    for (count=0; count<values.length; count++)
                        accumulate_mean (values[count].getNativeComponents (false, GeomagAbsoluteValue.ANGLE_MINUTES),
                                         values[count].getMissingDataValue(),
                                         min_accumulator, n_min_samples);
                    values = day_data.getHourlyMeans();
                    for (count=0; count<values.length; count++)
                        accumulate_mean (values[count].getNativeComponents (false, GeomagAbsoluteValue.ANGLE_MINUTES),
                                         values[count].getMissingDataValue(),
                                         hour_accumulator, n_hour_samples);
                    values = day_data.getDailyMeans();
                    for (count=0; count<values.length; count++)
                        accumulate_mean (values[count].getNativeComponents (false, GeomagAbsoluteValue.ANGLE_MINUTES),
                                         values[count].getMissingDataValue(),
                                         day_accumulator, n_day_samples);
                }
            }
            
            // hand back used objects for garbage collection
            month_data [month_count] = null;
            day_data = null;
            values = null;
        }
        
        // finalise and post the means
        mean = finalise_mean (min_accumulator, n_min_samples, recorded_elements);
        incomplete = is_incomplete(1440 * DateUtils.daysInYear(current_year), n_min_samples);
        addMean ((double) current_year + 0.5, mean, "Minute mean data", incomplete);
        mean = finalise_mean (hour_accumulator, n_hour_samples, recorded_elements);
        incomplete = is_incomplete(24 * DateUtils.daysInYear(current_year), n_hour_samples);
        addMean ((double) current_year + 0.5, mean, "Hourly mean data", incomplete);
        mean = finalise_mean (day_accumulator, n_day_samples, recorded_elements);
        incomplete = is_incomplete(DateUtils.daysInYear(current_year), n_day_samples);
        addMean ((double) current_year + 0.5, mean, "Daily mean data", incomplete);
        
        return true;
    }

    private void accumulate_mean (double values [],
                                  double missing,
                                  double accumulator [],
                                  int n_samples [])
    {
        int comp;
        
        for (comp=0; comp<3; comp++)
        {
            if (values [comp] != missing)
            {
                accumulator [comp] += values [comp];
                n_samples [comp] ++;
            }
        }
    }
    
    private GeomagAbsoluteValue finalise_mean (double accumulator [],
                                               int n_samples [],
                                               String elements)
    {
        int orientation;
        GeomagAbsoluteValue mean;
        
        orientation = setOrientation(elements);
        if (orientation == GeomagAbsoluteValue.ORIENTATION_UNKNOWN)
            return null;
        if (n_samples [0] <= 0 || n_samples [1] <= 0 || n_samples [2] <= 0)
            return null;
        mean = new GeomagAbsoluteValue (accumulator [0] / (double) n_samples [0],
                                        accumulator [1] / (double) n_samples [1],
                                        accumulator [2] / (double) n_samples [2],
                                        YearMean.MISSING_ELEMENT,
                                        orientation, 
                                        GeomagAbsoluteValue.ANGLE_MINUTES);
        return mean;
    }
    
    private boolean is_incomplete (int expected_n_samples, 
                                   int n_samples [])
    {
        int threshold;
        
        threshold = expected_n_samples - ((10 * expected_n_samples) / 100);
        if (n_samples [0] < threshold) return true;
        if (n_samples [1] < threshold) return true;
        if (n_samples [2] < threshold) return true;
        return false;
    }

    private void addMean (double year, GeomagAbsoluteValue mean, String source_type, boolean incomplete)
    {
        String elements, mean_type;

        if (mean != null)
        {
            switch (mean.getNativeOrientation())
            {
                case GeomagAbsoluteValue.ORIENTATION_DIF: elements = "diff"; break;
                case GeomagAbsoluteValue.ORIENTATION_HDZ: elements = "hdzf"; break;
                case GeomagAbsoluteValue.ORIENTATION_XYZ: elements = "xyzf"; break;
                default: elements = "Unk"; break;
            }
            if (incomplete) mean_type = "Incomplete";
            else mean_type = "All days";
            addMean (year, mean.getX (), mean.getY (), mean.getZ (),
                     mean.getH (), mean.getDMinutes(), mean.getIMinutes(),
                     mean.getF (), source_type, mean_type, elements, true);
        }
    }
    
    private void addMean (YearMean mean)
    {
        boolean check_for_discrepancy;
        
        switch (mean.getType())
        {
            case ALL_DAYS: check_for_discrepancy = true; break;
            case DISTURBED_DAYS: check_for_discrepancy = true; break;
            case QUIET_DAYS: check_for_discrepancy = true; break;
            case INCOMPLETE: check_for_discrepancy = true; break;
            default: check_for_discrepancy = false; break;
        }
        addMean (mean.getYear(), mean.getX (), mean.getY (), mean.getZ (),
                 mean.getH (), mean.getD (), mean.getI (), mean.getF (),
                 "Annual means file",  mean.getTypeName(),
                 mean.getRecordedElements(), check_for_discrepancy);
    }
    
    private void addMean (double year, double x, double y, double z,
                          double h, double d, double i, double f,
                          String source_type, String mean_type,
                          String elements,
                          boolean check_for_discrepancy)
    {
        boolean discrepancy;
        int orientation;
        GeomagAbsoluteValue abs_val;
        String row [];

        // create an absolute value from the data
        abs_val = null;
        if (check_for_discrepancy)
        {
            orientation = setOrientation (elements);
            switch (orientation)
            {
                case GeomagAbsoluteValue.ORIENTATION_HDZ:
                    abs_val = new GeomagAbsoluteValue (h, d, z, YearMean.MISSING_ELEMENT, orientation, GeomagAbsoluteValue.ANGLE_MINUTES);
                    break;
                case GeomagAbsoluteValue.ORIENTATION_XYZ:
                    abs_val = new GeomagAbsoluteValue (x, y, z, YearMean.MISSING_ELEMENT, orientation, GeomagAbsoluteValue.ANGLE_MINUTES);
                    break;
                case GeomagAbsoluteValue.ORIENTATION_DIF:
                    abs_val = new GeomagAbsoluteValue (d, i, f, YearMean.MISSING_ELEMENT, orientation, GeomagAbsoluteValue.ANGLE_MINUTES);
                    break;
                default:
                    check_for_discrepancy = false;
                    break;
            }
        }
        
        // use the absolute value to check for a discrepancy
        discrepancy = false;
        if (check_for_discrepancy)
        {
            if (Math.abs (d - abs_val.getDMinutes()) > 0.05) discrepancy = true;
            if (Math.abs (i - abs_val.getIMinutes()) > 0.05) discrepancy = true;
            if (Math.abs (x - abs_val.getX()) > 0.5) discrepancy = true;
            if (Math.abs (y - abs_val.getY()) > 0.5) discrepancy = true;
            if (Math.abs (z - abs_val.getZ()) > 0.5) discrepancy = true;
            if (Math.abs (h - abs_val.getH()) > 0.5) discrepancy = true;
            if (Math.abs (f - abs_val.getF()) > 0.5) discrepancy = true;
        }
        
        // format the mean and insert the mean in the table
        row = new String [10];
        row [0] = String.format ("%8.3f", year);
        row [1] = source_type;
        row [2] = mean_type;
        row [3] = format (String.format ("%.1f", d), "d", elements, check_for_discrepancy, discrepancy);
        row [4] = format (String.format ("%.1f", i), "i", elements, check_for_discrepancy, discrepancy);
        row [5] = format (String.format ("%.0f", h), "h", elements, check_for_discrepancy, discrepancy);
        row [6] = format (String.format ("%.0f", x), "x", elements, check_for_discrepancy, discrepancy);
        row [7] = format (String.format ("%.0f", y), "y", elements, check_for_discrepancy, discrepancy);
        row [8] = format (String.format ("%.0f", z), "z", elements, check_for_discrepancy, discrepancy);
        row [9] = format (String.format ("%.0f", f), "f", elements, check_for_discrepancy, discrepancy);
        table_model.addRow(row);
        
        // if there was a discrepancy, add another row showing the elements without a discrepancy
        if (discrepancy)
        {
            row [0] = "";
            row [1] = "Element correction";
            row [2] = mean_type;
            row [3] = format (String.format ("%.1f", abs_val.getDMinutes()), "d", elements, true, false);
            row [4] = format (String.format ("%.1f", abs_val.getIMinutes()), "i", elements, true, false);
            row [5] = format (String.format ("%.0f", abs_val.getH()),        "h", elements, true, false);
            row [6] = format (String.format ("%.0f", abs_val.getX()),        "x", elements, true, false);
            row [7] = format (String.format ("%.0f", abs_val.getY()),        "y", elements, true, false);
            row [8] = format (String.format ("%.0f", abs_val.getZ()),        "z", elements, true, false);
            row [9] = format (String.format ("%.0f", abs_val.getF()),        "f", elements, true, false);
            table_model.addRow(row);
        }
        
    }

    private String format(String value, String ecode, String elements, boolean check_for_discrepancy, boolean discrepancy) 
    {
        boolean underline, bold, italic;
        String string;
        
        if (elements.toLowerCase().contains(ecode.toLowerCase())) underline = true;
        else underline = false;
        
        if (check_for_discrepancy) italic = false;
        else italic = true;
        
        if (discrepancy) bold = true;
        else bold = false;
        
        string = "<html>";
        if (bold) string += "<b>";
        if (italic) string += "<i>";
        if (underline) string += "<u>";
        string += value;
        if (underline) string += "</u>";
        if (italic) string += "</i>";
        if (bold) string += "</b>";
        string += "</html>";
        return string;
    }

    public int setOrientation (String elements)
    {
        if (elements == null) return GeomagAbsoluteValue.ORIENTATION_UNKNOWN;
        if (elements.equalsIgnoreCase("hdz")) return GeomagAbsoluteValue.ORIENTATION_HDZ;
        if (elements.equalsIgnoreCase("hdzf")) return GeomagAbsoluteValue.ORIENTATION_HDZ;
        if (elements.equalsIgnoreCase("xyz")) return GeomagAbsoluteValue.ORIENTATION_XYZ;
        if (elements.equalsIgnoreCase("xyzf")) return GeomagAbsoluteValue.ORIENTATION_XYZ;
        if (elements.equalsIgnoreCase("dif")) return GeomagAbsoluteValue.ORIENTATION_DIF;
        if (elements.equalsIgnoreCase("diff")) return GeomagAbsoluteValue.ORIENTATION_DIF;
        return GeomagAbsoluteValue.ORIENTATION_UNKNOWN;
    }
}
