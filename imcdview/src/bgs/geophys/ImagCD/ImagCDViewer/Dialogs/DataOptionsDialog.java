/*
 * DataOptionsDialog.java
 *
 * Created on 11 February 2009, 12:22
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author  smf
 */
public class DataOptionsDialog extends javax.swing.JDialog 
{
    public enum NonDatabaseRules { ALL_VECTOR, ALL_SCALAR, ALL_DELTA, USE_YEAR_RULES }
    
    // copy of configuration - used to restore state on cancel
    private Properties saved_config;
    
    /** Creates new form DataOptionsDialog */
    public DataOptionsDialog(java.awt.Frame parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        loadOptions ();
        
        // get state for cancel button
        saved_config = GlobalObjects.checkpointConfig();
    }
    
    /** Creates new form DataOptionsDialog */
    public DataOptionsDialog(java.awt.Dialog parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        loadOptions ();
        
        // get state for cancel button
        saved_config = GlobalObjects.checkpointConfig();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        FDataTypeButtonGroup = new javax.swing.ButtonGroup();
        OptionPanel = new javax.swing.JPanel();
        UseDatabaseCheckBox = new javax.swing.JCheckBox();
        HelpLabel1 = new javax.swing.JLabel();
        AllVectorRadioButton = new javax.swing.JRadioButton();
        AllScalarRadioButton = new javax.swing.JRadioButton();
        AllDeltaRadioButton = new javax.swing.JRadioButton();
        UseRulesRadioButton = new javax.swing.JRadioButton();
        VectorBeforePanel = new javax.swing.JPanel();
        VectorBeforeLabel = new javax.swing.JLabel();
        VectorBeforeTextField = new javax.swing.JTextField();
        VectorBeforeLabel2 = new javax.swing.JLabel();
        DeltaAfterPanel = new javax.swing.JPanel();
        DeltaAfterLabel = new javax.swing.JLabel();
        DeltaAfterTextField = new javax.swing.JTextField();
        DeltaAfterLabel2 = new javax.swing.JLabel();
        HelpPanel2 = new javax.swing.JPanel();
        HelpLabel2 = new javax.swing.JLabel();
        HelpLabel3 = new javax.swing.JLabel();
        ButtonPanel = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        ApplyButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        HelpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Data Viewing Options");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        OptionPanel.setLayout(new java.awt.GridBagLayout());

        UseDatabaseCheckBox.setText("Use database to determine whether recorded F is scalar, vector or delta-F");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(UseDatabaseCheckBox, gridBagConstraints);

        HelpLabel1.setText("Where observatory-year combination is not in database (or database is not in use) assume that:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(HelpLabel1, gridBagConstraints);

        FDataTypeButtonGroup.add(AllVectorRadioButton);
        AllVectorRadioButton.setText("All F data is vector data (i.e. hide all data)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(AllVectorRadioButton, gridBagConstraints);

        FDataTypeButtonGroup.add(AllScalarRadioButton);
        AllScalarRadioButton.setText("All F data is scalar data (i.e. show all data)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(AllScalarRadioButton, gridBagConstraints);

        FDataTypeButtonGroup.add(AllDeltaRadioButton);
        AllDeltaRadioButton.setText("All F data is delta-F");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(AllDeltaRadioButton, gridBagConstraints);

        FDataTypeButtonGroup.add(UseRulesRadioButton);
        UseRulesRadioButton.setText("Use the following rules:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(UseRulesRadioButton, gridBagConstraints);

        VectorBeforeLabel.setText("    Data in the year");
        VectorBeforePanel.add(VectorBeforeLabel);

        VectorBeforeTextField.setText("XXXX");
        VectorBeforePanel.add(VectorBeforeTextField);

        VectorBeforeLabel2.setText("and before is considered as vector data (i.e. not shown)");
        VectorBeforePanel.add(VectorBeforeLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(VectorBeforePanel, gridBagConstraints);

        DeltaAfterLabel.setText("    Data in the year");
        DeltaAfterPanel.add(DeltaAfterLabel);

        DeltaAfterTextField.setText("XXXX");
        DeltaAfterPanel.add(DeltaAfterTextField);

        DeltaAfterLabel2.setText("and after is considered as delta-F data");
        DeltaAfterPanel.add(DeltaAfterLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(DeltaAfterPanel, gridBagConstraints);

        HelpLabel2.setText("    Any data between these years is considered as scalar data (i.e. shown)");
        HelpPanel2.add(HelpLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(HelpPanel2, gridBagConstraints);

        HelpLabel3.setText("NOTE: Changes made to these parameters will not affect data that is already being displayed.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionPanel.add(HelpLabel3, gridBagConstraints);

        getContentPane().add(OptionPanel, java.awt.BorderLayout.CENTER);

        OKButton.setText("OK");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(OKButton);

        ApplyButton.setText("Apply");
        ApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplyButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(ApplyButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(CancelButton);

        HelpButton.setText("Help");
        HelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(HelpButton);

        getContentPane().add(ButtonPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        if (saveOptions ()) closeDialog();
    }//GEN-LAST:event_OKButtonActionPerformed

    private void ApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyButtonActionPerformed
        saveOptions ();
    }//GEN-LAST:event_ApplyButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        restoreOptions();
        closeDialog ();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog ();
    }//GEN-LAST:event_formWindowClosing

    private void HelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand("show_help&from_data_options_window&DataViewerOptions.html");
    }//GEN-LAST:event_HelpButtonActionPerformed
    
    private void closeDialog ()
    {
        this.setVisible(false);
        this.dispose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton AllDeltaRadioButton;
    private javax.swing.JRadioButton AllScalarRadioButton;
    private javax.swing.JRadioButton AllVectorRadioButton;
    private javax.swing.JButton ApplyButton;
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton CancelButton;
    private javax.swing.JLabel DeltaAfterLabel;
    private javax.swing.JLabel DeltaAfterLabel2;
    private javax.swing.JPanel DeltaAfterPanel;
    private javax.swing.JTextField DeltaAfterTextField;
    private javax.swing.ButtonGroup FDataTypeButtonGroup;
    private javax.swing.JButton HelpButton;
    private javax.swing.JLabel HelpLabel1;
    private javax.swing.JLabel HelpLabel2;
    private javax.swing.JLabel HelpLabel3;
    private javax.swing.JPanel HelpPanel2;
    private javax.swing.JButton OKButton;
    private javax.swing.JPanel OptionPanel;
    private javax.swing.JCheckBox UseDatabaseCheckBox;
    private javax.swing.JRadioButton UseRulesRadioButton;
    private javax.swing.JLabel VectorBeforeLabel;
    private javax.swing.JLabel VectorBeforeLabel2;
    private javax.swing.JPanel VectorBeforePanel;
    private javax.swing.JTextField VectorBeforeTextField;
    // End of variables declaration//GEN-END:variables

    private void loadOptions ()
    {
        boolean use_database_flag;
        NonDatabaseRules non_database_rules;
        int vector_before_year, delta_after_year;
        
        use_database_flag = isUseFDatabase();
        non_database_rules = getNonDatabaseRuleCode();
        vector_before_year = getVectorBeforeYear();
        delta_after_year =getDeltaAfterYear();

        UseDatabaseCheckBox.setSelected(use_database_flag);
        switch (non_database_rules)
        {
            case ALL_DELTA: AllDeltaRadioButton.setSelected(true); break;
            case ALL_VECTOR: AllVectorRadioButton.setSelected(true); break;
            case USE_YEAR_RULES: UseRulesRadioButton.setSelected(true); break;
            default: AllScalarRadioButton.setSelected(true); break;
        }
        VectorBeforeTextField.setText (Integer.toString (vector_before_year));
        DeltaAfterTextField.setText (Integer.toString (delta_after_year));
    }
    
    private boolean saveOptions ()
    {
        boolean use_database_flag;
        String non_database_rules, errmsg;
        int vector_before_year, delta_after_year;
        
        use_database_flag = UseDatabaseCheckBox.isSelected();
        if (AllVectorRadioButton.isSelected()) non_database_rules = "Vector";
        else if (AllDeltaRadioButton.isSelected()) non_database_rules = "Delta";
        else if (UseRulesRadioButton.isSelected()) non_database_rules = "UseRules";
        else non_database_rules = "Scalar";
        errmsg = null;
        try 
        { 
            errmsg = "Value in 'years before' field must be a number";
            vector_before_year = Integer.parseInt(VectorBeforeTextField.getText());
            errmsg = "Value in 'years after' field must be a number";
            delta_after_year = Integer.parseInt(DeltaAfterTextField.getText());
            if (vector_before_year >= delta_after_year)
                errmsg = "Value is 'years before' field must be less than value in 'years after' field.";
            else
                errmsg = null;
        }
        catch (NumberFormatException e) 
        { 
            vector_before_year = delta_after_year = 0;
        }
        
        if (errmsg != null)
        {
            JOptionPane.showMessageDialog (this, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        GlobalObjects.configuration.setProperty(getUseFDatabaseOptionName(), Boolean.toString(use_database_flag));
        GlobalObjects.configuration.setProperty(getNonDatabaseRuleCodeOptionName(), non_database_rules);
        GlobalObjects.configuration.setProperty(getVectorBeforeOptionName(), Integer.toString (vector_before_year));
        GlobalObjects.configuration.setProperty(getDeltaAfterOptionName(), Integer.toString (delta_after_year));
        return true;
    }
    
    private void restoreOptions ()
    {
        GlobalObjects.configuration.setProperty(getUseFDatabaseOptionName(), saved_config.getProperty(getUseFDatabaseOptionName()));
        GlobalObjects.configuration.setProperty(getNonDatabaseRuleCodeOptionName(), saved_config.getProperty(getNonDatabaseRuleCodeOptionName()));
        GlobalObjects.configuration.setProperty(getVectorBeforeOptionName(), saved_config.getProperty(getVectorBeforeOptionName()));
        GlobalObjects.configuration.setProperty(getDeltaAfterOptionName(), saved_config.getProperty(getDeltaAfterOptionName()));
    }
    
    public static String getUseFDatabaseOptionName () { return "DataOptions.UseFDatabase"; }
    public static String getNonDatabaseRuleCodeOptionName () { return "DataOptions.NonDatabaseRuleCode"; }
    public static String getVectorBeforeOptionName () { return "DataOptions.VectorBeforeYear"; }
    public static String getDeltaAfterOptionName () { return "DataOptions.DeltaAfterYear"; }
    
    public static boolean isUseFDatabase ()
    {
        return Boolean.parseBoolean (GlobalObjects.configuration.getProperty(getUseFDatabaseOptionName(), "true"));
    }
    public static NonDatabaseRules getNonDatabaseRuleCode ()
    {
        String non_database_rules;
        
        non_database_rules = GlobalObjects.configuration.getProperty(getNonDatabaseRuleCodeOptionName(), "U");
        if (non_database_rules.equalsIgnoreCase("V")) return DataOptionsDialog.NonDatabaseRules.ALL_VECTOR;
        else if (non_database_rules.equalsIgnoreCase("Vector")) return DataOptionsDialog.NonDatabaseRules.ALL_VECTOR;
        else if (non_database_rules.equalsIgnoreCase("D")) return DataOptionsDialog.NonDatabaseRules.ALL_DELTA;
        else if (non_database_rules.equalsIgnoreCase("Delta")) return DataOptionsDialog.NonDatabaseRules.ALL_DELTA;
        else if (non_database_rules.equalsIgnoreCase("R")) return DataOptionsDialog.NonDatabaseRules.USE_YEAR_RULES;
        else if (non_database_rules.equalsIgnoreCase("Rules")) return DataOptionsDialog.NonDatabaseRules.USE_YEAR_RULES;
        else if (non_database_rules.equalsIgnoreCase("U")) return DataOptionsDialog.NonDatabaseRules.USE_YEAR_RULES;
        else if (non_database_rules.equalsIgnoreCase("UseRules")) return DataOptionsDialog.NonDatabaseRules.USE_YEAR_RULES;
        return DataOptionsDialog.NonDatabaseRules.ALL_SCALAR;
    }
    public static int getVectorBeforeYear ()
    {
        try { return Integer.parseInt (GlobalObjects.configuration.getProperty(getVectorBeforeOptionName(), "2006")); }
        catch (NumberFormatException e) { }
        return 2006;
    }
    public static int getDeltaAfterYear ()
    {
        try { return Integer.parseInt (GlobalObjects.configuration.getProperty(getDeltaAfterOptionName(), "2009")); }
        catch (NumberFormatException e) { }
        return 2009;
    }
}