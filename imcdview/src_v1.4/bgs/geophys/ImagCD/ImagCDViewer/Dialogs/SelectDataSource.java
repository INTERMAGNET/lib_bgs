/*
 * SelectDataSource.java
 *
 * Created on 15 October 2004, 10:32
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import javax.swing.filechooser.FileView;

/**
 *
 * @author  smf
 */
public class SelectDataSource extends javax.swing.JDialog 
{

    /** a class to hold a database - the database can have one of
     * three states - not loaded (the 'database' is just a path reference);
     * disabled (loaded, but not in use); loaded */
    enum databaseState { NOT_LOADED, DISABLED, LOADED }
    private class databaseWrapper
    implements IconListCellRenderer.IconTeller, Comparable<databaseWrapper>
    {
        private String path;
        private CDDatabase database;
        private boolean in_use;
        public databaseWrapper (String path)
        {
            this.path = path;
            this.database = null;
            this.in_use = false;
        }
        public databaseWrapper (CDDatabase database, boolean in_use)
        {
            this.path = database.GetBaseDir().getAbsolutePath();
            this.database = database;
            this.in_use = in_use;
        }
        public databaseState getState ()
        {
            if (database == null) return databaseState.NOT_LOADED;
            if (! in_use) return databaseState.DISABLED;
            return databaseState.LOADED;
        }
        public void setInUse (boolean in_use) { this.in_use = in_use; }
        public void setDatabase (CDDatabase database, boolean in_use)
        {
            this.database = database;
            this.in_use = in_use;
        }
        public CDDatabase getDatabase () { return database; }
        public String getPath () { return path; }
        /** method used to display the object */
        @Override
        public String toString () 
        {
            if (! in_use) return path + " (disabled)";
            return path; 
        }
        /** method used to get the icon associated with the database */
        public ImageIcon getIcon (boolean is_selected) 
        {
            if (getState () == databaseState.LOADED)
            {
                if (is_selected) return GlobalObjects.open_folder_icon;
                return GlobalObjects.folder_icon;
            }
            return GlobalObjects.folder_bad_icon;
        }
        /** method used to sort objects */
        public int compareTo(SelectDataSource.databaseWrapper o) 
        {
          return path.compareToIgnoreCase(o.path);
        }
    }
    
    // private members
    private CDDatabaseList database_list;         // copy of the main program's list of IMAG CD data sources
//    private Vector<databaseWrapper> wrapper_list; // the list displayed to the user
    private DefaultListModel bd_list_contents;    //  contents of the base directory list
    private Vector<DataSourceListener> data_source_listeners;         // list of callbacks when new data sources are chosen
    private Frame frame_owner;                    // who owns this dialog
    private Dialog dialog_owner;                  // who owns this dialog
    private final String config_base_key_name = "disabled_data_source_";

    /** Creates new form SelectDataSource */
    public SelectDataSource(java.awt.Frame parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        setupComponents ();
        this.frame_owner = parent;
        this.dialog_owner = null;
    }

    /** Creates new form SelectDataSource */
    public SelectDataSource(java.awt.Dialog parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        setupComponents ();
        this.frame_owner = null;
        this.dialog_owner = parent;
    }
    
    /** get the current severity for display of messages
     * @return a CDErrorList severity code */
    public int getDisplaySeverity ()
    {
        if (ErrorRadioButton.isSelected()) 
            return bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.ERROR_MESSAGE;
        if (WarningRadioButton.isSelected())
            return bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.WARNING_MESSAGE;
        return bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.INFORMATION_MESSAGE;
    }
    
    /** set up the components */
    private void setupComponents ()
    {
        int count, n_databases, severity;
        String name, value;
        Vector<databaseWrapper> wrapper_list;
        
        // set the severity radio buttons - the default severity for the program is set here
        try { severity = Integer.parseInt ((String) GlobalObjects.configuration.get ("ErrorListSeverity")); }
        catch (Exception e) { severity = bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.ERROR_MESSAGE; }
        switch (severity)
        {
        case bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.ERROR_MESSAGE:
            ErrorRadioButton.setSelected(true);
            break;
        case bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList.WARNING_MESSAGE:
            WarningRadioButton.setSelected(true);
            break;
        default:
            InformationRadioButton.setSelected (true);
            break;
        }
        
        // set the scan recurse limits
        ScanRecurseSpinner.setModel (new SpinnerNumberModel (5, 3, 100, 1));

        // create the list of listeners
        data_source_listeners = new Vector<DataSourceListener> ();

        // get the database list
        database_list = new CDDatabaseList (GlobalObjects.database_list, GlobalObjects.country_table, GlobalObjects.observatory_table);
        
        // make the database wrapper object, for displaying databases
        n_databases = database_list.GetNDatabases ();
        wrapper_list = new Vector<SelectDataSource.databaseWrapper> ();
        try
        {
            for (count=0; count<n_databases; count++)
                wrapper_list.add (new databaseWrapper (database_list.GetDatabase(count), true));
        } catch (Exception e) { }
        
        // pull any non-loaded databases from the configuration
        count = 0;
        do
        {
            name = config_base_key_name + Integer.toString (count ++);
            value = (String) GlobalObjects.configuration.get (name);
            if (value != null)
                wrapper_list.add (new databaseWrapper (value));
        } while (value != null);

        Collections.sort (wrapper_list);

        // fill the list of current data sources
        bd_list_contents = new DefaultListModel ();
        for (count=0; count<wrapper_list.size(); count++)
            bd_list_contents.addElement (wrapper_list.get(count));
        DataSourceList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        DataSourceList.setCellRenderer (new IconListCellRenderer (GlobalObjects.open_folder_icon, GlobalObjects.folder_icon));
        DataSourceList.setModel(bd_list_contents);

        DataSourceListValueChanged (null);
        getRootPane().setDefaultButton (OKButton);
        setVisible (true);
    }

    /** Add an object to be called when the data sources are chosen
     * @param data_source_listener the object that will be called */
    public void addDataSourceListener (DataSourceListener data_source_listener)
    {
        data_source_listeners.addElement (data_source_listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        SeverityButtonGroup = new javax.swing.ButtonGroup();
        ListPane = new javax.swing.JPanel();
        DataSourceListScrollPane = new javax.swing.JScrollPane();
        DataSourceList = new javax.swing.JList();
        ListButtonPane = new javax.swing.JPanel();
        AddButton = new javax.swing.JButton();
        RemoveButton = new javax.swing.JButton();
        DisableButton = new javax.swing.JButton();
        ActionPane = new javax.swing.JPanel();
        SeverityPane = new javax.swing.JPanel();
        SeverityLabel = new javax.swing.JLabel();
        InformationRadioButton = new javax.swing.JRadioButton();
        WarningRadioButton = new javax.swing.JRadioButton();
        ErrorRadioButton = new javax.swing.JRadioButton();
        ScanPane = new javax.swing.JPanel();
        Separator2 = new javax.swing.JSeparator();
        ScanLabel = new javax.swing.JLabel();
        StartScanDirText = new javax.swing.JTextField();
        ScanRecurseLabel = new javax.swing.JLabel();
        ScanPane2 = new javax.swing.JPanel();
        ScanRecurseSpinner = new javax.swing.JSpinner();
        ScanIgnoreSystemDirs = new javax.swing.JCheckBox();
        ScanButton = new javax.swing.JButton();
        Separator3 = new javax.swing.JSeparator();
        ButtonPane = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        HelpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Data Sources");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        ListPane.setLayout(new java.awt.BorderLayout());

        DataSourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                DataSourceListValueChanged(evt);
            }
        });
        DataSourceListScrollPane.setViewportView(DataSourceList);

        ListPane.add(DataSourceListScrollPane, java.awt.BorderLayout.CENTER);

        AddButton.setText("Add...");
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });
        ListButtonPane.add(AddButton);

        RemoveButton.setText("Remove");
        RemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveButtonActionPerformed(evt);
            }
        });
        ListButtonPane.add(RemoveButton);

        DisableButton.setText("Disable");
        DisableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisableButtonActionPerformed(evt);
            }
        });
        ListButtonPane.add(DisableButton);

        ListPane.add(ListButtonPane, java.awt.BorderLayout.SOUTH);

        getContentPane().add(ListPane, java.awt.BorderLayout.NORTH);

        ActionPane.setLayout(new java.awt.BorderLayout());

        SeverityPane.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        SeverityLabel.setText("View error list at severity:");
        SeverityPane.add(SeverityLabel);

        SeverityButtonGroup.add(InformationRadioButton);
        InformationRadioButton.setText("Information");
        SeverityPane.add(InformationRadioButton);

        SeverityButtonGroup.add(WarningRadioButton);
        WarningRadioButton.setText("Warning");
        SeverityPane.add(WarningRadioButton);

        SeverityButtonGroup.add(ErrorRadioButton);
        ErrorRadioButton.setText("Error");
        SeverityPane.add(ErrorRadioButton);

        ActionPane.add(SeverityPane, java.awt.BorderLayout.NORTH);

        ScanPane.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ScanPane.add(Separator2, gridBagConstraints);

        ScanLabel.setText("Start scan in directory:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ScanPane.add(ScanLabel, gridBagConstraints);

        StartScanDirText.setColumns(30);
        StartScanDirText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartScanDirTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ScanPane.add(StartScanDirText, gridBagConstraints);

        ScanRecurseLabel.setText("Limit depth of directory search:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ScanPane.add(ScanRecurseLabel, gridBagConstraints);

        ScanPane2.add(ScanRecurseSpinner);

        ScanIgnoreSystemDirs.setSelected(true);
        ScanIgnoreSystemDirs.setText("Ignore system directories");
        ScanIgnoreSystemDirs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ScanIgnoreSystemDirs.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ScanPane2.add(ScanIgnoreSystemDirs);

        ScanButton.setText("Scan");
        ScanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScanButtonActionPerformed(evt);
            }
        });
        ScanPane2.add(ScanButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ScanPane.add(ScanPane2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ScanPane.add(Separator3, gridBagConstraints);

        ActionPane.add(ScanPane, java.awt.BorderLayout.CENTER);

        OKButton.setText("OK");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        ButtonPane.add(OKButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });
        ButtonPane.add(CancelButton);

        HelpButton.setText("Help");
        HelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpButtonActionPerformed(evt);
            }
        });
        ButtonPane.add(HelpButton);

        ActionPane.add(ButtonPane, java.awt.BorderLayout.SOUTH);

        getContentPane().add(ActionPane, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveButtonActionPerformed
        int index;
        databaseWrapper wrapper;
        
        index = DataSourceList.getSelectedIndex ();
        if (index > -1)
        {
            try
            {
                wrapper = (databaseWrapper) bd_list_contents.remove(index);
                if (wrapper.getState() != databaseState.NOT_LOADED)
                    database_list.RemoveDatabase (wrapper.getDatabase());
            }
            catch (CDException e) { }
        }

    }//GEN-LAST:event_RemoveButtonActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        Cursor old_cursor, sd_old_cursor;
        File dir;

        // run the file chooser
        if (GlobalObjects.data_source_dir_chooser.showDialog (null, null) == JFileChooser.APPROVE_OPTION)
        {
            dir = GlobalObjects.data_source_dir_chooser.getSelectedFile();

            // show a busy cursor
            sd_old_cursor = GlobalObjects.data_source_dir_chooser.getCursor();
            old_cursor = getCursor ();
            setCursor(GlobalObjects.wait_cursor);
            GlobalObjects.data_source_dir_chooser.setCursor(GlobalObjects.wait_cursor);
            
            loadData (dir, null);
        
            GlobalObjects.data_source_dir_chooser.setCursor(sd_old_cursor);
            setCursor(old_cursor);
        }       
    }//GEN-LAST:event_AddButtonActionPerformed

    private void ScanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScanButtonActionPerformed
        int n_databases, count;
        String string, err_msg;
        File start_dir;
        Vector<databaseWrapper> wrapper_list;        
        
        // get the users scan start directory
        string = StartScanDirText.getText ();
        err_msg = null;
        if (string == null) start_dir = null;
        else if (string.length() <= 0) start_dir = null;
        else
        {
            start_dir = new File (string);
            if (! start_dir.exists()) err_msg = "The start directory does not exist: " + string;
            else if (! start_dir.isDirectory()) err_msg = "The start file is not a directory: " + string;
        }
        
        if (err_msg != null)
        {
            if (frame_owner != null)
                JOptionPane.showMessageDialog (frame_owner, err_msg, "Error with start directory",
                                               JOptionPane.ERROR_MESSAGE);
            else
                JOptionPane.showMessageDialog (dialog_owner, err_msg, "Error with start directory",
                                               JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            // create a scan progress dialog which will run the database scan
            new ScanProgress (this, database_list, start_dir, 
                              ((Integer) ScanRecurseSpinner.getValue()).intValue(), 
                              ScanIgnoreSystemDirs.isSelected(),
                              getDisplaySeverity());
      
            // reload the database list to the on screen list
            wrapper_list = new Vector<databaseWrapper> ();
            n_databases = database_list.GetNDatabases ();
            try
            {
                for (count=0; count<n_databases; count++)
                    wrapper_list.add (new databaseWrapper (database_list.GetDatabase(count), true));
            } catch (Exception e) { }
            Collections.sort (wrapper_list);
            bd_list_contents.removeAllElements ();
            for (count=0; count<wrapper_list.size(); count++)
                bd_list_contents.addElement (wrapper_list.get(count));
        }
    }//GEN-LAST:event_ScanButtonActionPerformed

    private void HelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand("show_help&from_data_source_window&DataSources.html");
    }//GEN-LAST:event_HelpButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        closeDialog (null);
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        DataSourceListener data_source_listener;
        int count, n_disabled;
        databaseWrapper wrapper;
        String name, value;
        
        // remove current non-loaded databases from configuration
        count = 0;
        do
        {
            name = config_base_key_name + Integer.toString (count ++);
            value = (String) GlobalObjects.configuration.get (name);
            if (value != null)
                GlobalObjects.configuration.remove(name);
        } while (value != null);
        
        // put all non-loaded databases to the confiugration
        n_disabled = 0;
        for (count=0; count<bd_list_contents.size(); count++)
        {
            wrapper = (databaseWrapper) bd_list_contents.get(count);
            if (wrapper.getState() != databaseState.LOADED)
            {
                name = config_base_key_name + Integer.toString (n_disabled ++);
                GlobalObjects.configuration.put (name, wrapper.getPath());
            }
        }
        
        for (count=0; count<data_source_listeners.size(); count++)
        {
            data_source_listener = (DataSourceListener) data_source_listeners.elementAt (count);
            data_source_listener.dataSourcesChosen (database_list);
        }
        GlobalObjects.redrawMap ();
        closeDialog (null);
    }//GEN-LAST:event_OKButtonActionPerformed

    private void StartScanDirTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartScanDirTextActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_StartScanDirTextActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        GlobalObjects.configuration.put ("ErrorListSeverity", Integer.toString (getDisplaySeverity()));

        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void DisableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisableButtonActionPerformed
        int index;
        databaseWrapper wrapper;
        Cursor old_cursor;
        
        index = DataSourceList.getSelectedIndex ();
        if (index > -1)
        {
            wrapper = (databaseWrapper) bd_list_contents.get(index);
            switch (wrapper.getState())
            {
                case DISABLED:
                    try
                    {
                        wrapper.setDatabase(database_list.AddDatabase(wrapper.getDatabase()), true);
                    }
                    catch (CDException e) { }
                    break;
                case LOADED:
                    wrapper.setInUse(false);
                    try
                    {
                        database_list.RemoveDatabase(wrapper.getDatabase());
                    }
                    catch (CDException e) { }
                    break;
                case NOT_LOADED:
                    old_cursor = getCursor ();
                    setCursor(GlobalObjects.wait_cursor);

                    loadData (new File (wrapper.getPath()), wrapper);
                    
                    setCursor(old_cursor);
            }
            DataSourceList.repaint();
            DataSourceListValueChanged(null);
        }
    }//GEN-LAST:event_DisableButtonActionPerformed

    private void DataSourceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_DataSourceListValueChanged
        databaseWrapper wrapper;
        int index;
        
        // set the enabled / disabled button according to the state of this database
        index = DataSourceList.getSelectedIndex();
        if (index >= 0)
        {
            DisableButton.setEnabled(true);
            RemoveButton.setEnabled(true);
            wrapper = (databaseWrapper) bd_list_contents.get(index);
            if (wrapper.getState() == databaseState.LOADED)
                DisableButton.setText("Disable");
            else
                DisableButton.setText("Enable");
        }
        else
        {
            DisableButton.setEnabled(false);
            RemoveButton.setEnabled(false);
            DisableButton.setText("Disable");
        }
                
    }//GEN-LAST:event_DataSourceListValueChanged

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ActionPane;
    private javax.swing.JButton AddButton;
    private javax.swing.JPanel ButtonPane;
    private javax.swing.JButton CancelButton;
    private javax.swing.JList DataSourceList;
    private javax.swing.JScrollPane DataSourceListScrollPane;
    private javax.swing.JButton DisableButton;
    private javax.swing.JRadioButton ErrorRadioButton;
    private javax.swing.JButton HelpButton;
    private javax.swing.JRadioButton InformationRadioButton;
    private javax.swing.JPanel ListButtonPane;
    private javax.swing.JPanel ListPane;
    private javax.swing.JButton OKButton;
    private javax.swing.JButton RemoveButton;
    private javax.swing.JButton ScanButton;
    private javax.swing.JCheckBox ScanIgnoreSystemDirs;
    private javax.swing.JLabel ScanLabel;
    private javax.swing.JPanel ScanPane;
    private javax.swing.JPanel ScanPane2;
    private javax.swing.JLabel ScanRecurseLabel;
    private javax.swing.JSpinner ScanRecurseSpinner;
    private javax.swing.JSeparator Separator2;
    private javax.swing.JSeparator Separator3;
    private javax.swing.ButtonGroup SeverityButtonGroup;
    private javax.swing.JLabel SeverityLabel;
    private javax.swing.JPanel SeverityPane;
    private javax.swing.JTextField StartScanDirText;
    private javax.swing.JRadioButton WarningRadioButton;
    // End of variables declaration//GEN-END:variables

    private void loadData (File dir, databaseWrapper wrapper)
    {
        CDDatabase database;
        CDErrorList error_list;
        int severity;

        // attempt to load data
        try
        {
            database = database_list.AddDatabase (dir);
                
            if (database == null)
            {
                if (frame_owner != null)
                    JOptionPane.showMessageDialog (frame_owner,
                                                   "This database is already in the list: " + dir.toString(), "Error adding directory",
                                                   JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog (dialog_owner,
                                                   "This database is already in the list: " + dir.toString(), "Error adding directory",
                                                   JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                // the base directory may be changed during loading
                dir = database.GetBaseDir();
                error_list = database.GetErrorList ();
                severity = getDisplaySeverity();
                if (error_list.GetNErrorMessages(severity) > 0)
                {
                    if (frame_owner != null)
                        new ErrorListDisplay (frame_owner, false, error_list, severity);
                    else
                        new ErrorListDisplay (dialog_owner, false, error_list, severity);
                }
                if (wrapper == null)
                {
                    wrapper = new databaseWrapper (database, true);
                    bd_list_contents.addElement (wrapper);
                }
                else 
                    wrapper.setDatabase(database, true);
            }
        }
        catch (CDException e)
        {
            if (frame_owner != null)
                JOptionPane.showMessageDialog (frame_owner,
                                               e.getMessage (), "Error Adding Directory", JOptionPane.ERROR_MESSAGE);
            else
                JOptionPane.showMessageDialog (dialog_owner,
                                               e.getMessage (), "Error Adding Directory", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
