/*
 * ImportStep2.java
 *
 * Created on 27 December 2008, 10:30
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.Import;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author  smf
 */
public class ImportStep2 extends javax.swing.JPanel 
{

    private File selected_dir;
    
    /** Creates new form ImportStep2 */
    public ImportStep2 () 
    {
        int max_pc;
        
        initComponents();
        
        selected_dir = new File (GlobalObjects.configuration.getProperty ("Import.Directory", System.getProperty("user.home", ".")));
        ChosenDirectoryTextField.setText (selected_dir.getAbsolutePath());
        CDFormatCheckBox.setSelected       (getFromConfig ("Import.UseCdFormat",       false));
        DailyMeansCheckBox.setSelected     (getFromConfig ("Import.MakeDailyMeans",    true));
        HourlyMeansCheckBox.setSelected    (getFromConfig ("Import.MakeHourlyMeans",   true));
        IAGA2002FormatCheckBox.setSelected (getFromConfig ("Import.UseIaga2002Format", true));
        IMFFormatCheckBox.setSelected      (getFromConfig ("Import.UseIMFFormat",      false));
        KIndexFormatCheckBox.setSelected   (getFromConfig ("Import.UseKIndexFormat",   true));
        IncludeSubDirsCheckBox.setSelected (getFromConfig ("Import.IncludeSubDirs",    false));
        try
        {
            max_pc = Integer.parseInt (GlobalObjects.configuration.getProperty("Import.MaxMissingPCForMean", "10"));
        }
        catch (NumberFormatException e) { max_pc = 10; }
        MaxMissingSpinner.setValue (new Integer (max_pc));
    }
    
    public File getSelectedFile () { return selected_dir; }
    
    public boolean isUseCDFormat () 
    {
        setConfig ("Import.UseCdFormat", CDFormatCheckBox.isSelected());
        return CDFormatCheckBox.isSelected(); 
    }
    public boolean isMakeDailyMeans () 
    {
        setConfig ("Import.MakeDailyMeans", DailyMeansCheckBox.isSelected());
        return DailyMeansCheckBox.isSelected(); 
    }
    public boolean isMakeHourlyMeans () 
    {
        setConfig ("Import.MakeHourlyMeans", HourlyMeansCheckBox.isSelected());
        return HourlyMeansCheckBox.isSelected(); 
    }
    public boolean isUseIAGA2002Format () 
    {
        setConfig ("Import.UseIaga2002Format", IAGA2002FormatCheckBox.isSelected());
        return IAGA2002FormatCheckBox.isSelected(); 
    }
    public boolean isUseKIndexFormat () 
    {
        setConfig ("Import.UseKIndexFormat", KIndexFormatCheckBox.isSelected());
        return KIndexFormatCheckBox.isSelected(); 
    }
    public boolean isUseIMFFormat () 
    {
        setConfig ("Import.UseIMFFormat", IMFFormatCheckBox.isSelected());
        return IMFFormatCheckBox.isSelected(); 
    }
    public boolean isIncludeSubDirs () 
    {
        setConfig ("Import.IncludeSubDirs", IncludeSubDirsCheckBox.isSelected());
        return IncludeSubDirsCheckBox.isSelected(); 
    }
    public int getMaxMissingPC ()
    {
        int max_pc;
        
        max_pc = ((Integer) MaxMissingSpinner.getValue()).intValue();
        GlobalObjects.configuration.setProperty("Import.MaxMissingPCForMean", Integer.toString (max_pc));
        return max_pc;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        TitleLabel = new javax.swing.JLabel();
        InstructionsLabel = new javax.swing.JLabel();
        ContentsPanel = new javax.swing.JPanel();
        ChosenDirectoryLabel = new javax.swing.JLabel();
        ChosenDirectoryTextField = new javax.swing.JTextField();
        FileSelectorButton = new javax.swing.JButton();
        IncludeSubDirsLabel = new javax.swing.JLabel();
        IncludeSubDirsCheckBox = new javax.swing.JCheckBox();
        CDFormatLabel = new javax.swing.JLabel();
        CDFormatCheckBox = new javax.swing.JCheckBox();
        IMFFormatLabel = new javax.swing.JLabel();
        IMFFormatCheckBox = new javax.swing.JCheckBox();
        IAGA2002FormatLabel = new javax.swing.JLabel();
        IAGA2002FormatCheckBox = new javax.swing.JCheckBox();
        KIndexFormatLabel = new javax.swing.JLabel();
        KIndexFormatCheckBox = new javax.swing.JCheckBox();
        HourlyMeansLabel = new javax.swing.JLabel();
        HourlyMeansCheckBox = new javax.swing.JCheckBox();
        DailyMeansLabel = new javax.swing.JLabel();
        DailyMeansCheckBox = new javax.swing.JCheckBox();
        MaxMissingLabel = new javax.swing.JLabel();
        MaxMissingSpinner = new javax.swing.JSpinner();

        setLayout(new java.awt.BorderLayout());

        TitleLabel.setText("<html><h2>Step 2 of 6: Select input data</h2><html>"); // NOI18N
        add(TitleLabel, java.awt.BorderLayout.PAGE_START);

        InstructionsLabel.setText("<html> \n<h3>Information</h3>\n\nChoose the directory from which data will be read and select which file types will be used and whether means will<br>\nbe calculated or not. If you check 'Include data in sub-directories' then the sub-directories of the chosen directory<br>\nwill be scanned for valid data files. \n\n</html>"); // NOI18N
        add(InstructionsLabel, java.awt.BorderLayout.SOUTH);

        ContentsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select options"));
        ContentsPanel.setLayout(new java.awt.GridBagLayout());

        ChosenDirectoryLabel.setText("Directory to read data from:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(ChosenDirectoryLabel, gridBagConstraints);

        ChosenDirectoryTextField.setColumns(40);
        ChosenDirectoryTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(ChosenDirectoryTextField, gridBagConstraints);

        FileSelectorButton.setText("Select directory...");
        FileSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileSelectorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        ContentsPanel.add(FileSelectorButton, gridBagConstraints);

        IncludeSubDirsLabel.setText("Include data in sub-directories:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(IncludeSubDirsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(IncludeSubDirsCheckBox, gridBagConstraints);

        CDFormatLabel.setText("Copy INTERMAGNET archive format files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(CDFormatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(CDFormatCheckBox, gridBagConstraints);

        IMFFormatLabel.setText("Convert INTERMAGNET minute mean format files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(IMFFormatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(IMFFormatCheckBox, gridBagConstraints);

        IAGA2002FormatLabel.setText("Convert IAGA-2002 format files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(IAGA2002FormatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(IAGA2002FormatCheckBox, gridBagConstraints);

        KIndexFormatLabel.setText("Convert K index (DKA) files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(KIndexFormatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(KIndexFormatCheckBox, gridBagConstraints);

        HourlyMeansLabel.setText("Calculate hourly means:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(HourlyMeansLabel, gridBagConstraints);

        HourlyMeansCheckBox.setText("(Mean data read from files will");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(HourlyMeansCheckBox, gridBagConstraints);

        DailyMeansLabel.setText("Calculate daily means:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(DailyMeansLabel, gridBagConstraints);

        DailyMeansCheckBox.setText("override calculated values)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(DailyMeansCheckBox, gridBagConstraints);

        MaxMissingLabel.setText("Maximum missing data allowed for mean calculation (%):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        ContentsPanel.add(MaxMissingLabel, gridBagConstraints);

        MaxMissingSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 20, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ContentsPanel.add(MaxMissingSpinner, gridBagConstraints);

        add(ContentsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void FileSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileSelectorButtonActionPerformed
        int ret_val;
        JFileChooser file_chooser;
        
        file_chooser = new JFileChooser (selected_dir);
        file_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        file_chooser.setApproveButtonText("Select");
        ret_val = file_chooser.showOpenDialog(this);
        if (ret_val == JFileChooser.APPROVE_OPTION)
        {
            selected_dir = file_chooser.getSelectedFile();
            ChosenDirectoryTextField.setText (selected_dir.getAbsolutePath());
            GlobalObjects.configuration.setProperty ("Import.Directory", selected_dir.getAbsolutePath());
        }
    }//GEN-LAST:event_FileSelectorButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CDFormatCheckBox;
    private javax.swing.JLabel CDFormatLabel;
    private javax.swing.JLabel ChosenDirectoryLabel;
    private javax.swing.JTextField ChosenDirectoryTextField;
    private javax.swing.JPanel ContentsPanel;
    private javax.swing.JCheckBox DailyMeansCheckBox;
    private javax.swing.JLabel DailyMeansLabel;
    private javax.swing.JButton FileSelectorButton;
    private javax.swing.JCheckBox HourlyMeansCheckBox;
    private javax.swing.JLabel HourlyMeansLabel;
    private javax.swing.JCheckBox IAGA2002FormatCheckBox;
    private javax.swing.JLabel IAGA2002FormatLabel;
    private javax.swing.JCheckBox IMFFormatCheckBox;
    private javax.swing.JLabel IMFFormatLabel;
    private javax.swing.JCheckBox IncludeSubDirsCheckBox;
    private javax.swing.JLabel IncludeSubDirsLabel;
    private javax.swing.JLabel InstructionsLabel;
    private javax.swing.JCheckBox KIndexFormatCheckBox;
    private javax.swing.JLabel KIndexFormatLabel;
    private javax.swing.JLabel MaxMissingLabel;
    private javax.swing.JSpinner MaxMissingSpinner;
    private javax.swing.JLabel TitleLabel;
    // End of variables declaration//GEN-END:variables
    
    public static boolean getFromConfig (String key, boolean default_value)
    {
        String value;
        
        value = GlobalObjects.configuration.getProperty(key);
        if (value == null) return default_value;
        if (value.equalsIgnoreCase("1")) return true;
        if (value.equalsIgnoreCase("Y")) return true;
        if (value.equalsIgnoreCase("Yes")) return true;
        if (value.equalsIgnoreCase("T")) return true;
        if (value.equalsIgnoreCase("True")) return true;
        return false;
    }
    
    public static void setConfig (String key, boolean value)
    {
        GlobalObjects.configuration.setProperty(key, Boolean.toString(value));
    }
}