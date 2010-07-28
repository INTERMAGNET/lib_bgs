/*
 * NewUserDialog.java
 *
 * Created on 01 March 2007, 22:43
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabaseList;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;

/**
 *
 * @author  smf
 */
public class NewUserDialog extends javax.swing.JDialog 
{
    private String keyName;
    
    /** Creates new form NewUserDialog
     * @param parent the parent container
     * @param modal true for a modal dialog
     * @param keyName name of the key that holds yes/no for whether to show this dialog */
    public NewUserDialog(java.awt.Frame parent, boolean modal, String keyName) 
    {
        super(parent, modal);
        
        this.keyName = keyName;
        
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        HelpText = new javax.swing.JTextArea();
        ButtonPanel = new javax.swing.JPanel();
        CloseButton = new javax.swing.JButton();
        HelpButton = new javax.swing.JButton();
        ScanButton = new javax.swing.JButton();
        ShowNewUserInfoCheckbox = new javax.swing.JCheckBox();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("New User Help");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        HelpText.setColumns(20);
        HelpText.setEditable(false);
        HelpText.setRows(5);
        HelpText.setText("This information is for people who have not used this software before. \nMost of the software is intuitive and easy to use but you will need to \ntell the software where to find your data before you can use any of \nthe functions. A quick way to do this is to put a data CD in your \nCD-ROM drive and press the 'Scan' button below.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        getContentPane().add(HelpText, gridBagConstraints);

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

        ScanButton.setText("Scan for data");
        ScanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScanButtonActionPerformed(evt);
            }
        });

        ButtonPanel.add(ScanButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ButtonPanel, gridBagConstraints);

        ShowNewUserInfoCheckbox.setSelected(true);
        ShowNewUserInfoCheckbox.setText("Show this window at startup");
        ShowNewUserInfoCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ShowNewUserInfoCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(ShowNewUserInfoCheckbox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        CloseButtonActionPerformed (null);
    }//GEN-LAST:event_formWindowClosing

    private void ScanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScanButtonActionPerformed
        // create a scan progress dialog which will run the database scan
        new ScanProgress (this, GlobalObjects.database_list, null, 5, true, CDErrorList.ERROR_MESSAGE);
        GlobalObjects.redrawMap ();
    }//GEN-LAST:event_ScanButtonActionPerformed

    private void HelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpButtonActionPerformed
        GlobalObjects.command_interpreter.interpretCommand("show_help&from_main_window&NewUser.html");
    }//GEN-LAST:event_HelpButtonActionPerformed

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        GlobalObjects.configuration.put (keyName, 
                                         this.ShowNewUserInfoCheckbox.isSelected() ? "Y" : "N");
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_CloseButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton CloseButton;
    private javax.swing.JButton HelpButton;
    private javax.swing.JTextArea HelpText;
    private javax.swing.JButton ScanButton;
    private javax.swing.JCheckBox ShowNewUserInfoCheckbox;
    // End of variables declaration//GEN-END:variables
    
}