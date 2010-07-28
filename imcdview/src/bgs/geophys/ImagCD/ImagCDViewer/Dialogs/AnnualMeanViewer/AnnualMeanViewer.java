/*
 * AnnualMeanViewer.java
 *
 * Created on 20 February 2009, 14:44
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatory;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.PlotOptionsDialog;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Data.ImagCD.YearMeanFile;
import bgs.geophys.library.Swing.ImageSaverDialog;
import bgs.geophys.library.Swing.JComboSpinner;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author  smf
 */
public class AnnualMeanViewer extends javax.swing.JFrame 
{

    // observatory details
    private String obsy_code;
    private String obsy_name;
    private AnnualMeanPlotPanel plot_panel;
    private AnnualMeanPlotPanel plot_diff_panel;
    private AnnualMeanTablePanel data_table_panel;
  
    // counters and flags
    private int n_years;
    // the current selected year
    private int current_year_index;
    // the current displayed year
    private int disp_year_index;
    // set during initialisation
    private boolean is_initialising;

    // data storage
    private File annual_mean_file;
    private Vector<Integer> year_list;
    private YearMeanFile year_mean_file;
    private AnnualMeanOptions options;
    private CDDatabase database;
    
    // extra components
    private JComboSpinner scale_spinner;
    
   /** Create new DataViewer
    * @param obsy_code - the 3 letter code for the observatory
    * @param year - the year of the data to be displayed
    * @param base_dir - if the viewer should be restricted to one data source
    *        then set this to the path of the data source, otherwise set it to null
    * @throws AnnualMeanViewerException if the data cannot be loaded */
    public AnnualMeanViewer(String obsy_code, int year, String base_dir)
    throws AnnualMeanViewerException
    {
        int count;

        is_initialising = true;
        year_mean_file = null;
        options = new AnnualMeanOptions();
      
        this.obsy_code = obsy_code;
        this.obsy_name = obsy_code;
        if (base_dir == null)
            database = null;
        else
            database = CDMisc.findDataSource(base_dir);
        setTitle ();

        // get list of available years for this observatory
        if (database == null)
            year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_YEAR_MEAN);
        else
            year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_YEAR_MEAN, database);
        n_years = year_list.size ();
        if (n_years <= 0)
            throw new AnnualMeanViewerException ("No annual mean data files exist for " + obsy_code);
    
        // create dialog
        initComponents();
        scale_spinner = (JComboSpinner) PlotOptionsDialog.getZoomComboBox(true);
        ScalePanel.add (scale_spinner);
        
        // set up year selection
        select_year.removeAllItems();
        current_year_index = -1;
        for (count=0; count<n_years; count++)
        {
            if (year == year_list.get(count).intValue())
                current_year_index = count;
            select_year.addItem(year_list.get(count).toString());
        }
        if (current_year_index < 0) current_year_index = n_years -1;
        disp_year_index = -1;
        select_year.setSelectedIndex(current_year_index);
        getRootPane().setDefaultButton (close_button);
        this.setIconImage(GlobalObjects.imag_icon.getImage());
        ShowJumpsCheckBox.setSelected(options.isShowJumps());
        MarkMeansCheckBox.setSelected(options.isMarkMeans());
        switch (options.getDisplayType())
        {
            case ALL_DAYS_PLUS_INCOMPLETE: mean_type.setSelectedIndex(1); break;
            case QUIET_DAYS: mean_type.setSelectedIndex(2); break;
            case DISTURBED_DAYS: mean_type.setSelectedIndex(3); break;
            default: mean_type.setSelectedIndex(0); break;
        }
        
        // fill tabs
        plot_panel = new AnnualMeanPlotPanel (false);
        plot_diff_panel = new AnnualMeanPlotPanel (true);
        data_table_panel = new AnnualMeanTablePanel ();
        tabbed_pane.add ("Plot Data", plot_panel);
        tabbed_pane.add ("Plot Differences", plot_diff_panel);
        tabbed_pane.add ("View Data", data_table_panel);

        // fill the display and finalise the window
        is_initialising = false;
        updateDisplay (true);
        this.pack ();
        this.setVisible (true);
        
        assignUniqueID();
    }
    
    /** variables to define a unique ID for this viewer */
    private static int id_counter = 0;
    private int unique_id;
    private void assignUniqueID () { unique_id = id_counter ++; }
    public int getUniqueID () { return unique_id; }

    public void updateDisplayAndHandleErrors (boolean always_reload)
    {
        String msg;
        
        if (! is_initialising)
        {
            try
            {
                updateDisplay(always_reload);
            }
            catch (AnnualMeanViewerException e)
            {
                if (e.getMessage() == null)
                    msg = "Error loading data";
                else
                    msg = e.getMessage();
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
   /********************************************************************
    * updateDisplay - update display after a new date is selected
    *                 or new options are given by the user
    * @param always_reload - if true, always reload the cache and redisplay
    *                        if false, only reload if date/view has changed
    * @throws AnnualMeanViewerException if the data cannot be loaded
    ********************************************************************/
    public void updateDisplay (boolean always_reload)
    throws AnnualMeanViewerException
    {
        boolean reload;
        Cursor old_cursor;
        Vector<CDObservatory> cd_obsy_list;
        CDObservatory cd_obsy;

        old_cursor = null;
    
        // check if new data is required
        if (always_reload) reload = true;
        else if (current_year_index != disp_year_index) reload = true;
        else reload = false;
        if (reload)
        {
            // set a busy cursor
            old_cursor = getCursor();
            setCursor(GlobalObjects.wait_cursor);

            // get the full path to the file containing annual means for this observatory and year
            if (database == null)
                cd_obsy_list = CDMisc.getObservatoryList (obsy_code, getCurrentYear());
            else
                cd_obsy_list = CDMisc.getObservatoryList (obsy_code, getCurrentYear(), database);
            if (cd_obsy_list.size() <= 0)
                throw new AnnualMeanViewerException ("No annual mean data is present for " + obsy_code + " in " + Integer.toString (getCurrentYear ()));
            cd_obsy = cd_obsy_list.get(0);
            obsy_name = cd_obsy.GetObservatoryName();
            setTitle ();
            annual_mean_file = cd_obsy.GetFile (CDObservatory.FILE_YEAR_MEAN);        
            if (annual_mean_file == null)
            {
                setCursor (old_cursor);
                throw new AnnualMeanViewerException ("No annual mean data is present for " + obsy_code + " in " + Integer.toString (getCurrentYear ()));
            }
        
            // load the file
            try
            {
                year_mean_file = new YearMeanFile (annual_mean_file);
            }
            catch (FileNotFoundException e) 
            { 
                setCursor (old_cursor);
                throw new AnnualMeanViewerException ("Annual mean data file cannot be read: " + annual_mean_file.getAbsolutePath());
            }
            catch (IOException e) 
            { 
                setCursor (old_cursor);
                throw new AnnualMeanViewerException ("Error reading annual mean data file: " + annual_mean_file.getAbsolutePath());
            }
            catch (ParseException e) 
            { 
                setCursor (old_cursor);
                throw new AnnualMeanViewerException ("Bad data in annual mean data file: " + annual_mean_file.getAbsolutePath());
            }
        }

        // restore the cursor
        if (old_cursor != null) setCursor(old_cursor);
    
        // show the data
        updateVisiblePanel ();

        // set the scale combo box
        PlotOptionsDialog.setZoomComboBoxSelectedItem(scale_spinner, false);
        
        // set the currently displayed data flags
        disp_year_index = current_year_index;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbed_pane = new javax.swing.JTabbedPane();
        OptionsPanel = new javax.swing.JPanel();
        MiscPanel = new javax.swing.JPanel();
        YearLabel = new javax.swing.JLabel();
        select_year = new javax.swing.JComboBox();
        DummyLabel1 = new javax.swing.JLabel();
        MeanTypeLabel = new javax.swing.JLabel();
        mean_type = new javax.swing.JComboBox();
        DummyLabel2 = new javax.swing.JLabel();
        ShowJumpsCheckBox = new javax.swing.JCheckBox();
        DummyLabel3 = new javax.swing.JLabel();
        MarkMeansCheckBox = new javax.swing.JCheckBox();
        ButtonsPanel = new javax.swing.JPanel();
        ScalePanel = new javax.swing.JPanel();
        ScaleLabel = new javax.swing.JLabel();
        ZoomHelpButton = new javax.swing.JButton();
        plot_options_button = new javax.swing.JButton();
        copy_button = new javax.swing.JButton();
        print_button = new javax.swing.JButton();
        save_button = new javax.swing.JButton();
        close_button = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        tabbed_pane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbed_pane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbed_paneStateChanged(evt);
            }
        });
        getContentPane().add(tabbed_pane, java.awt.BorderLayout.CENTER);

        OptionsPanel.setLayout(new java.awt.GridBagLayout());

        YearLabel.setText("Year data recorded in:");
        MiscPanel.add(YearLabel);

        select_year.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Year", "Year", "Year" }));
        select_year.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_yearActionPerformed(evt);
            }
        });
        MiscPanel.add(select_year);

        DummyLabel1.setText("<html>&nbsp;&nbsp;&nbsp;</html>");
        MiscPanel.add(DummyLabel1);

        MeanTypeLabel.setText("Type of mean:");
        MiscPanel.add(MeanTypeLabel);

        mean_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Means calculated from all days", "Means calculated from all days (or possibly incomplete)", "Means calculated from quiet days only", "Means calculated from disturbed days only" }));
        mean_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mean_typeActionPerformed(evt);
            }
        });
        MiscPanel.add(mean_type);

        DummyLabel2.setText("<html>&nbsp;&nbsp;&nbsp;</html>");
        MiscPanel.add(DummyLabel2);

        ShowJumpsCheckBox.setText("Show jumps");
        ShowJumpsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowJumpsCheckBoxActionPerformed(evt);
            }
        });
        MiscPanel.add(ShowJumpsCheckBox);

        DummyLabel3.setText("<html>&nbsp;&nbsp;&nbsp;</html>");
        MiscPanel.add(DummyLabel3);

        MarkMeansCheckBox.setText("Mark means");
        MarkMeansCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MarkMeansCheckBoxActionPerformed(evt);
            }
        });
        MiscPanel.add(MarkMeansCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(MiscPanel, gridBagConstraints);

        ScaleLabel.setText("Scale:");
        ScalePanel.add(ScaleLabel);

        ButtonsPanel.add(ScalePanel);

        ZoomHelpButton.setText("Help on Zooming...");
        ZoomHelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZoomHelpButtonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(ZoomHelpButton);

        plot_options_button.setText("Plot Options");
        plot_options_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot_options_buttonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(plot_options_button);

        copy_button.setText("Copy to clipboard");
        copy_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copy_buttonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(copy_button);

        print_button.setText("Print");
        print_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                print_buttonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(print_button);

        save_button.setText("Save");
        save_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_buttonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(save_button);

        close_button.setText("Close");
        close_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                close_buttonActionPerformed(evt);
            }
        });
        ButtonsPanel.add(close_button);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        OptionsPanel.add(ButtonsPanel, gridBagConstraints);

        getContentPane().add(OptionsPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog ();
    }//GEN-LAST:event_formWindowClosing

    private void tabbed_paneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbed_paneStateChanged
        updateVisiblePanel();
    }//GEN-LAST:event_tabbed_paneStateChanged

    private void plot_options_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot_options_buttonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand ("raise_plot_options_dialog&from_annual_mean_viewer_" + Integer.toString (getUniqueID()));
    }//GEN-LAST:event_plot_options_buttonActionPerformed

    private void copy_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copy_buttonActionPerformed
        data_table_panel.getExcelAdapter().actionPerformed (new ActionEvent (evt.getSource(), evt.getID(), "Copy", evt.getWhen(), evt.getModifiers()));
    }//GEN-LAST:event_copy_buttonActionPerformed

    private void print_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_print_buttonActionPerformed
        if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
            GlobalObjects.print_canvas.doPrint(plot_panel);
        else if (plot_diff_panel.equals(tabbed_pane.getSelectedComponent()))
            GlobalObjects.print_canvas.doPrint(plot_diff_panel);
    }//GEN-LAST:event_print_buttonActionPerformed

    private void save_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_buttonActionPerformed
        ImageSaverDialog save_dialog;

        if (data_table_panel.equals(tabbed_pane.getSelectedComponent()))
            GlobalObjects.querySaveFile (this, annual_mean_file);
        else
        {
            save_dialog = GlobalObjects.createImageSaverDialog();
            if (save_dialog.runModal(this) == JFileChooser.APPROVE_OPTION)
            {
                GlobalObjects.persistImageSaverOptions(save_dialog);
                if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
                    plot_panel.save (save_dialog.getChosenFile(), plot_panel.getTitle(), save_dialog.getChosenFileType(), 
                                     save_dialog.getImageSizeX(), save_dialog.getImageSizeY());
                else if (plot_diff_panel.equals(tabbed_pane.getSelectedComponent()))
                    plot_diff_panel.save (save_dialog.getChosenFile(), plot_panel.getTitle(), save_dialog.getChosenFileType(), 
                                          save_dialog.getImageSizeX(), save_dialog.getImageSizeY());
            }
        }
    }//GEN-LAST:event_save_buttonActionPerformed

    private void close_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_close_buttonActionPerformed
        closeDialog ();
    }//GEN-LAST:event_close_buttonActionPerformed

    private void select_yearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_yearActionPerformed
        current_year_index = select_year.getSelectedIndex();
        updateDisplayAndHandleErrors(false);
    }//GEN-LAST:event_select_yearActionPerformed

    private void mean_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mean_typeActionPerformed
        switch (mean_type.getSelectedIndex())
        {
            case 0: options.setDisplayType(YearMean.YearMeanType.ALL_DAYS); break;
            case 1: options.setDisplayType(YearMean.YearMeanType.ALL_DAYS_PLUS_INCOMPLETE); break;
            case 2: options.setDisplayType(YearMean.YearMeanType.QUIET_DAYS); break;
            case 3: options.setDisplayType(YearMean.YearMeanType.DISTURBED_DAYS); break;
        }
        updateDisplayAndHandleErrors(false);
}//GEN-LAST:event_mean_typeActionPerformed

    private void ShowJumpsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowJumpsCheckBoxActionPerformed
        options.setShowJumps(ShowJumpsCheckBox.isSelected());
        updateDisplayAndHandleErrors(false);
    }//GEN-LAST:event_ShowJumpsCheckBoxActionPerformed

    private void MarkMeansCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MarkMeansCheckBoxActionPerformed
        options.setMarkMeans(MarkMeansCheckBox.isSelected());
        updateDisplayAndHandleErrors(false);
}//GEN-LAST:event_MarkMeansCheckBoxActionPerformed

    private void ZoomHelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZoomHelpButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand("show_help&from_annual_mean_viewer_" + Integer.toString (getUniqueID()) + "&Zoom.html");
}//GEN-LAST:event_ZoomHelpButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonsPanel;
    private javax.swing.JLabel DummyLabel1;
    private javax.swing.JLabel DummyLabel2;
    private javax.swing.JLabel DummyLabel3;
    private javax.swing.JCheckBox MarkMeansCheckBox;
    private javax.swing.JLabel MeanTypeLabel;
    private javax.swing.JPanel MiscPanel;
    private javax.swing.JPanel OptionsPanel;
    private javax.swing.JLabel ScaleLabel;
    private javax.swing.JPanel ScalePanel;
    private javax.swing.JCheckBox ShowJumpsCheckBox;
    private javax.swing.JLabel YearLabel;
    private javax.swing.JButton ZoomHelpButton;
    private javax.swing.JButton close_button;
    private javax.swing.JButton copy_button;
    private javax.swing.JComboBox mean_type;
    private javax.swing.JButton plot_options_button;
    private javax.swing.JButton print_button;
    private javax.swing.JButton save_button;
    private javax.swing.JComboBox select_year;
    private javax.swing.JTabbedPane tabbed_pane;
    // End of variables declaration//GEN-END:variables
    
    /** Closes the dialog */
    private void closeDialog () 
    {
        setVisible(false);
        dispose();
    
        // make sure that data gets deallocated
        year_mean_file = null;
    }
    
    /** update the panel that is currently visible */
    private void updateVisiblePanel ()  
    {
        int current_year;
    
        current_year = getCurrentYear();
        options.loadComponentsShown();
        if (plot_panel.equals(tabbed_pane.getSelectedComponent()))
        {
            copy_button.setEnabled(false);
            print_button.setEnabled(true);
            if (year_mean_file != null)
                plot_panel.update (obsy_name, current_year, year_mean_file, options);
        }
        else if (plot_diff_panel.equals(tabbed_pane.getSelectedComponent()))
        {
            copy_button.setEnabled(false);
            print_button.setEnabled(true);
            if (year_mean_file != null)
                plot_diff_panel.update (obsy_name, current_year, year_mean_file, options);
        }
        else if (data_table_panel.equals(tabbed_pane.getSelectedComponent()))
        {
            copy_button.setEnabled(true);
            print_button.setEnabled(false);
            if (year_mean_file != null)
                data_table_panel.update (obsy_name, current_year, year_mean_file, options);
        }
    }

    private int getCurrentYear () { return year_list.get (current_year_index).intValue(); }
    
    private void setTitle ()
    {
        if (database == null)
            this.setTitle ("Annual Mean Data For " + obsy_name);
        else
            this.setTitle ("Annual Mean Data For " + obsy_name + " [ restricted to " + database.GetBaseDir().getAbsolutePath() + "]");
    }
}
