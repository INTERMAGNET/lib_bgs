/*
 * ErrorListDisplay.java
 *
 * Created on 14 October 2004, 21:25
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

/**
 *
 * @author  Administrator
 */
public class ErrorListDisplay extends javax.swing.JDialog 
{
    
    private bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList error_list;
    private javax.swing.DefaultListModel list_model;
    
    /** Creates new form ErrorListDisplay
     * @param parent the parent container
     * @param modal true for a modal dialog
     * @param error_list the list to display
     * @param severity a CDErrorList severity code indicating the
     *        initial severity filter for the display */
    public ErrorListDisplay(java.awt.Frame parent, boolean modal, 
                            bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList error_list,
                            int severity) 
    {
        super(parent, modal);
        initialise (error_list, severity);
    }

    /** Creates new form ErrorListDisplay
     * @param parent the parent container
     * @param modal true for a modal dialog
     * @param error_list the list to display
     * @param severity a CDErrorList severity code indicating the
     *        initial severity filter for the display */
    public ErrorListDisplay(java.awt.Dialog parent, boolean modal, 
                            bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList error_list,
                            int severity) 
    {
        super(parent, modal);
        initialise (error_list, severity);
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
    
    /** initialise the form
     * @param error_list the list to display
     * @param severity a CDErrorList severity code indicating the
     *        initial severity filter for the display */
    private void initialise (bgs.geophys.ImagCD.ImagCDViewer.Data.CDErrorList error_list,
                             int severity)
    {
        initComponents();
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
        this.error_list = error_list;
        list_model = new javax.swing.DefaultListModel ();
        fillErrorList ();
        ErrorList.setModel(list_model);
        setVisible (true);
        ErrorList.setSelectionMode (javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        SeverityButtonGroup = new javax.swing.ButtonGroup();
        Panel1 = new javax.swing.JPanel();
        SeverityLabel = new javax.swing.JLabel();
        InformationRadioButton = new javax.swing.JRadioButton();
        WarningRadioButton = new javax.swing.JRadioButton();
        ErrorRadioButton = new javax.swing.JRadioButton();
        ListScrollPane = new javax.swing.JScrollPane();
        ErrorList = new javax.swing.JList();
        Panel2 = new javax.swing.JPanel();
        NMessagesLabel = new javax.swing.JLabel();
        Panel3 = new javax.swing.JPanel();
        CloseButton = new javax.swing.JButton();
        CopyButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Database Error List");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        SeverityLabel.setText("Severity:");
        Panel1.add(SeverityLabel);

        InformationRadioButton.setText("Information");
        SeverityButtonGroup.add(InformationRadioButton);
        InformationRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InformationRadioButtonActionPerformed(evt);
            }
        });

        Panel1.add(InformationRadioButton);

        WarningRadioButton.setText("Warning");
        SeverityButtonGroup.add(WarningRadioButton);
        WarningRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WarningRadioButtonActionPerformed(evt);
            }
        });

        Panel1.add(WarningRadioButton);

        ErrorRadioButton.setText("Error");
        SeverityButtonGroup.add(ErrorRadioButton);
        ErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ErrorRadioButtonActionPerformed(evt);
            }
        });

        Panel1.add(ErrorRadioButton);

        getContentPane().add(Panel1, java.awt.BorderLayout.NORTH);

        ListScrollPane.setPreferredSize(new java.awt.Dimension(400, 250));
        ListScrollPane.setViewportView(ErrorList);

        getContentPane().add(ListScrollPane, java.awt.BorderLayout.CENTER);

        Panel2.setLayout(new java.awt.BorderLayout());

        NMessagesLabel.setText("Number of messages: XXX");
        Panel2.add(NMessagesLabel, java.awt.BorderLayout.EAST);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        Panel3.add(CloseButton);

        CopyButton.setText("Copy to clipboard");
        CopyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyButtonActionPerformed(evt);
            }
        });

        Panel3.add(CopyButton);

        Panel2.add(Panel3, java.awt.BorderLayout.WEST);

        getContentPane().add(Panel2, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void CopyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyButtonActionPerformed
        String string;
        int count;
        java.awt.datatransfer.Clipboard clipboard;
        java.awt.datatransfer.StringSelection string_sel;
        
        string = "";
        for (count=0; count<list_model.getSize(); count++)
            string += list_model.elementAt(count) + "\n";
        
        clipboard = java.awt.Toolkit.getDefaultToolkit ().getSystemClipboard ();
        string_sel  = new java.awt.datatransfer.StringSelection (string);
        clipboard.setContents (string_sel, string_sel);
    }//GEN-LAST:event_CopyButtonActionPerformed

    private void ErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ErrorRadioButtonActionPerformed
        fillErrorList ();
    }//GEN-LAST:event_ErrorRadioButtonActionPerformed

    private void WarningRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WarningRadioButtonActionPerformed
        fillErrorList ();
    }//GEN-LAST:event_WarningRadioButtonActionPerformed

    private void InformationRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InformationRadioButtonActionPerformed
        fillErrorList ();
    }//GEN-LAST:event_InformationRadioButtonActionPerformed

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        closeDialog (null);
    }//GEN-LAST:event_CloseButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseButton;
    private javax.swing.JButton CopyButton;
    private javax.swing.JList ErrorList;
    private javax.swing.JRadioButton ErrorRadioButton;
    private javax.swing.JRadioButton InformationRadioButton;
    private javax.swing.JScrollPane ListScrollPane;
    private javax.swing.JLabel NMessagesLabel;
    private javax.swing.JPanel Panel1;
    private javax.swing.JPanel Panel2;
    private javax.swing.JPanel Panel3;
    private javax.swing.ButtonGroup SeverityButtonGroup;
    private javax.swing.JLabel SeverityLabel;
    private javax.swing.JRadioButton WarningRadioButton;
    // End of variables declaration//GEN-END:variables

    /** fill the error list containiner */
    private void fillErrorList ()
    {
        int count, severity;
        String msg;
        
        if (InformationRadioButton.isSelected()) severity = error_list.INFORMATION_MESSAGE;
        else if (WarningRadioButton.isSelected()) severity = error_list.WARNING_MESSAGE;
        else severity = error_list.ERROR_MESSAGE;
        list_model.clear ();
        for (count=0; count<error_list.GetNErrorMessages(); count++)
        {
            msg = error_list.GetErrorMessage(severity, count);
            if (msg != null) list_model.addElement (msg);
        }
        NMessagesLabel.setText ("Number of messages: " + Integer.toString (error_list.GetNErrorMessages(severity)));
    }

}
