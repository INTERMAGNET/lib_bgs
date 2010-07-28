/*
 * ImportStep1.java
 *
 * Created on 27 December 2008, 10:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.Import;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDException;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author  smf
 */
public class ImportStep5 extends javax.swing.JPanel 
{

    private File new_source_dir;
    
    /** Creates new form ImportStep1 */
    public ImportStep5() 
    {
        int count;
        File file;
        String filename;
        
        initComponents();

        // populate the data source list and set it's default value
        filename = GlobalObjects.configuration.getProperty ("Import.DataSource");
        DataSourceComboBox.removeAllItems();
        for (count=0; count<GlobalObjects.database_list.GetNDatabases(); count++)
        {
            try 
            {
                file = GlobalObjects.database_list.GetDatabase(count).GetBaseDir();
                DataSourceComboBox.addItem (file);
                if (file.getAbsolutePath().equals(filename))
                {
                    DataSourceComboBox.setSelectedItem(file);
                }
            }
            catch (CDException ex) { }
        }

        // set remaining fields
        UseExistingDataSourceRadioButton.setSelected (ImportStep2.getFromConfig ("Import.UseExistingDataSource", true));
        MakeNewDataSourceRadioButton.setSelected (! UseExistingDataSourceRadioButton.isSelected());
        OverwriteFilesCheckBox.setSelected (ImportStep2.getFromConfig ("Import.OverwriteFiles", true));
        AddDataSourceCheckBox.setSelected (ImportStep2.getFromConfig ("Import.AddDataSource", true));
        new_source_dir = new File (GlobalObjects.configuration.getProperty ("Import.NewSourceDirectory", System.getProperty("user.home", ".")));
        DataSourceDirTextField.setText (new_source_dir.getAbsolutePath());
        ZipFlagCheckBox.setSelected (ImportStep2.getFromConfig("Import.ZipFiles", false));
        
        setExistingOrNew();
    }
    
    public File getTargetDir ()
    {
        File file;
        
        ImportStep2.setConfig ("Import.UseExistingDataSource", UseExistingDataSourceRadioButton.isSelected());
        if (UseExistingDataSourceRadioButton.isSelected ())
        {
            file = (File) DataSourceComboBox.getSelectedItem();
            GlobalObjects.configuration.setProperty ("Import.DataSource", file.getAbsolutePath());
        }
        else file = new_source_dir;
        
        return file;
    }
    
    public boolean isOverwrite () 
    {
        ImportStep2.setConfig ("Import.OverwriteFiles", OverwriteFilesCheckBox.isSelected());
        return OverwriteFilesCheckBox.isSelected();
    } 
    
    public boolean isZipFiles ()
    {
        ImportStep2.setConfig ("Import.ZipFiles", ZipFlagCheckBox.isSelected());
        return ZipFlagCheckBox.isSelected();
    }
    
    public boolean addDataSource () 
    {
        ImportStep2.setConfig ("Import.AddDataSource", AddDataSourceCheckBox.isSelected());
        if (UseExistingDataSourceRadioButton.isSelected ()) return false;
        return AddDataSourceCheckBox.isSelected();
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        NewOrExistingButtonGroup = new javax.swing.ButtonGroup();
        TitleLabel = new javax.swing.JLabel();
        ContentsPanel = new javax.swing.JPanel();
        UseExistingDataSourceRadioButton = new javax.swing.JRadioButton();
        ExistingDataSourcePanel = new javax.swing.JPanel();
        DataSourceLabel = new javax.swing.JLabel();
        DataSourceComboBox = new javax.swing.JComboBox();
        MakeNewDataSourceRadioButton = new javax.swing.JRadioButton();
        NewDataSourcePanel = new javax.swing.JPanel();
        DataSourceDirLabel = new javax.swing.JLabel();
        DataSourceDirTextField = new javax.swing.JTextField();
        FileSelectorButton = new javax.swing.JButton();
        AddDataSourceLabel = new javax.swing.JLabel();
        AddDataSourceCheckBox = new javax.swing.JCheckBox();
        OptionsPanel = new javax.swing.JPanel();
        ZipFlagLabel = new javax.swing.JLabel();
        ZipFlagCheckBox = new javax.swing.JCheckBox();
        OverwriteFilesLabel = new javax.swing.JLabel();
        OverwriteFilesCheckBox = new javax.swing.JCheckBox();
        InstructionsLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        TitleLabel.setText("<html><h2>Step 5 of 6: Copy files to main program</h2><html>"); // NOI18N
        add(TitleLabel, java.awt.BorderLayout.NORTH);

        ContentsPanel.setLayout(new java.awt.GridBagLayout());

        NewOrExistingButtonGroup.add(UseExistingDataSourceRadioButton);
        UseExistingDataSourceRadioButton.setText("Move data to existing data source");
        UseExistingDataSourceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UseExistingDataSourceRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(UseExistingDataSourceRadioButton, gridBagConstraints);

        ExistingDataSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Existing data source details"));
        ExistingDataSourcePanel.setLayout(new java.awt.GridBagLayout());

        DataSourceLabel.setText("Data source:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        ExistingDataSourcePanel.add(DataSourceLabel, gridBagConstraints);

        DataSourceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ExistingDataSourcePanel.add(DataSourceComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(ExistingDataSourcePanel, gridBagConstraints);

        NewOrExistingButtonGroup.add(MakeNewDataSourceRadioButton);
        MakeNewDataSourceRadioButton.setText("Create new data source");
        MakeNewDataSourceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MakeNewDataSourceRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(MakeNewDataSourceRadioButton, gridBagConstraints);

        NewDataSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("New data source details"));
        NewDataSourcePanel.setLayout(new java.awt.GridBagLayout());

        DataSourceDirLabel.setText("Data directory:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        NewDataSourcePanel.add(DataSourceDirLabel, gridBagConstraints);

        DataSourceDirTextField.setColumns(30);
        DataSourceDirTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        NewDataSourcePanel.add(DataSourceDirTextField, gridBagConstraints);

        FileSelectorButton.setText("Select directory...");
        FileSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileSelectorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        NewDataSourcePanel.add(FileSelectorButton, gridBagConstraints);

        AddDataSourceLabel.setText("Add data source to main program:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        NewDataSourcePanel.add(AddDataSourceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        NewDataSourcePanel.add(AddDataSourceCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(NewDataSourcePanel, gridBagConstraints);

        OptionsPanel.setLayout(new java.awt.GridBagLayout());

        ZipFlagLabel.setText("Compress (ZIP) files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        OptionsPanel.add(ZipFlagLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(ZipFlagCheckBox, gridBagConstraints);

        OverwriteFilesLabel.setText("Overwrite existing files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        OptionsPanel.add(OverwriteFilesLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(OverwriteFilesCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(OptionsPanel, gridBagConstraints);

        add(ContentsPanel, java.awt.BorderLayout.CENTER);

        InstructionsLabel.setText("<html>\n<h3>Information</h3>\n\nThe converted data files will be copied from their temporary directory to the main program. You can choose whether to copy<br>\nthem to an existing data source or create a new data source for them. If you create a new data source, you can choose<br>\nwhether to mount the data source in the main program or not. An option allows you to create compressed (ZIPed) data files.<br>\n\n</html>"); // NOI18N
        add(InstructionsLabel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void UseExistingDataSourceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UseExistingDataSourceRadioButtonActionPerformed
        setExistingOrNew();
    }//GEN-LAST:event_UseExistingDataSourceRadioButtonActionPerformed

    private void MakeNewDataSourceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MakeNewDataSourceRadioButtonActionPerformed
        setExistingOrNew();
    }//GEN-LAST:event_MakeNewDataSourceRadioButtonActionPerformed

    private void FileSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileSelectorButtonActionPerformed
        int ret_val;
        JFileChooser file_chooser;
        
        file_chooser = new JFileChooser (new_source_dir);
        file_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        file_chooser.setApproveButtonText("Select");
        ret_val = file_chooser.showOpenDialog(this);
        if (ret_val == JFileChooser.APPROVE_OPTION)
        {
            new_source_dir = file_chooser.getSelectedFile();
            DataSourceDirTextField.setText (new_source_dir.getAbsolutePath());
            GlobalObjects.configuration.setProperty ("Import.NewSourceDirectory", new_source_dir.getAbsolutePath());
        }
    }//GEN-LAST:event_FileSelectorButtonActionPerformed

    
    private void setExistingOrNew ()
    {
        if (UseExistingDataSourceRadioButton.isSelected())
        {
            DataSourceComboBox.setEnabled(true);
            FileSelectorButton.setEnabled(false);
            AddDataSourceCheckBox.setEnabled(false);
        }
        else
        {
            DataSourceComboBox.setEnabled(false);
            FileSelectorButton.setEnabled(true);
            AddDataSourceCheckBox.setEnabled(true);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox AddDataSourceCheckBox;
    private javax.swing.JLabel AddDataSourceLabel;
    private javax.swing.JPanel ContentsPanel;
    private javax.swing.JComboBox DataSourceComboBox;
    private javax.swing.JLabel DataSourceDirLabel;
    private javax.swing.JTextField DataSourceDirTextField;
    private javax.swing.JLabel DataSourceLabel;
    private javax.swing.JPanel ExistingDataSourcePanel;
    private javax.swing.JButton FileSelectorButton;
    private javax.swing.JLabel InstructionsLabel;
    private javax.swing.JRadioButton MakeNewDataSourceRadioButton;
    private javax.swing.JPanel NewDataSourcePanel;
    private javax.swing.ButtonGroup NewOrExistingButtonGroup;
    private javax.swing.JPanel OptionsPanel;
    private javax.swing.JCheckBox OverwriteFilesCheckBox;
    private javax.swing.JLabel OverwriteFilesLabel;
    private javax.swing.JLabel TitleLabel;
    private javax.swing.JRadioButton UseExistingDataSourceRadioButton;
    private javax.swing.JCheckBox ZipFlagCheckBox;
    private javax.swing.JLabel ZipFlagLabel;
    // End of variables declaration//GEN-END:variables
    
}
