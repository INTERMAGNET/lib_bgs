/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.cdf.dump;

import gsfc.nssdc.cdf.CDFException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.Timer;

/**
 * A dialog to display a dump of a CDF file
 * 
s * @author smf
 */
public class CDFDumpDialog 
extends javax.swing.JDialog
implements ActionListener, WindowListener
{

    private File cdf_file;
    private CDFDump cdf_dump;
    private File dumped_cdf_file;
    private Timer timer;
    
    /** Creates new form CDFDump. Dumps a CDF file to a temporary text file and displays the text.
     * 
     * @param parent the frame that this dialog should be attached to
     * @param modal whether to run this dialog as a modal dialog
     * @param cdf_file the file to display
     * @param rec_start the first record to show, or -1 for all records
     * @param rec_end the last record to display
     * @throws CDFException if there's a problem with the CDF software of the file format
     * @throws IOException  if there's an IO error with the files
     */
    public CDFDumpDialog(java.awt.Frame parent, boolean modal, File cdf_file, int rec_start, int rec_end) 
    throws CDFException, IOException
    {
        // setup dialog
        super(parent, modal);
        initComponents();
        this.addWindowListener(this);
        CDFDumpTextPane.setText("Converting and reading CDF...");
        String msg = "File: " + cdf_file.getAbsolutePath();
        if (rec_start > 0) msg += ", records " + rec_start + " to " + rec_end;
        InfoTextField.setText (msg);
        this.setTitle(cdf_file.getAbsolutePath());
        
        // get a temporary file to dump the CDF into
        this.cdf_file = cdf_file;
        dumped_cdf_file = File.createTempFile ("tmp_data_check_1s", ".txt");
        dumped_cdf_file.deleteOnExit();
        
        // start the dump
        cdf_dump = new CDFDump (cdf_file, rec_start, rec_end, dumped_cdf_file);
        
        // start the monitoring timer
        timer = new Timer (100, this);
        timer.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CDFDumpScrollPane = new javax.swing.JScrollPane();
        CDFDumpTextPane = new javax.swing.JTextPane();
        ActionPanel = new javax.swing.JPanel();
        ExitButton = new javax.swing.JButton();
        InfoTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        CDFDumpTextPane.setPreferredSize(new java.awt.Dimension(600, 400));
        CDFDumpScrollPane.setViewportView(CDFDumpTextPane);

        getContentPane().add(CDFDumpScrollPane, java.awt.BorderLayout.CENTER);

        ActionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ExitButton.setText("Exit");
        ExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitButtonActionPerformed(evt);
            }
        });
        ActionPanel.add(ExitButton);

        InfoTextField.setEditable(false);
        InfoTextField.setText("Information");
        ActionPanel.add(InfoTextField);

        getContentPane().add(ActionPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitButtonActionPerformed
        exit_dialog();
    }//GEN-LAST:event_ExitButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CDFDumpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CDFDumpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CDFDumpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CDFDumpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                File cdf_file = new File ("c:\\data\\DataCheck1sTestData\\ler_20140101_000000_4.cdf");
                try
                {
                    CDFDumpDialog dialog = new CDFDumpDialog(new javax.swing.JFrame(), true, cdf_file, 1, 1000);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.exit(0);
                        }
                    });
                    dialog.setVisible(true);
                }
                catch (CDFException | IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ActionPanel;
    private javax.swing.JScrollPane CDFDumpScrollPane;
    private javax.swing.JTextPane CDFDumpTextPane;
    private javax.swing.JButton ExitButton;
    private javax.swing.JTextField InfoTextField;
    // End of variables declaration//GEN-END:variables

    // exit neatly
    private void exit_dialog ()
    {
        timer.stop ();
        cdf_dump.killDump();
        this.dispose();
    }
    
    // receive timer events to monitor the dump process
    private int wait_counter = 0;
    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        boolean done = false;
        wait_counter ++;
        try
        {
            if (cdf_dump.isDumpFinished())
            {
                FileReader file_reader = new FileReader (dumped_cdf_file);
                CDFDumpTextPane.read(file_reader, cdf_file.getAbsoluteFile());
                file_reader.close();
                done = true;
            }
            else if (wait_counter % 10 == 0)
            {
                CDFDumpTextPane.setText (CDFDumpTextPane.getText () + ".");
            }
        }
        catch (IOException e)
        {
            done = true;
            String msg = "Error reading CDF file " + cdf_file.getAbsolutePath();
            if (e.getMessage() != null) msg += ": " + e.getMessage ();
            CDFDumpTextPane.setText(msg);
        }
        
        if (done)
            timer.stop();
    }

    @Override
    public void windowOpened(WindowEvent e) { }
    @Override
    public void windowClosing(WindowEvent e) { exit_dialog(); }
    @Override
    public void windowClosed(WindowEvent e) { }
    @Override
    public void windowIconified(WindowEvent e) { }
    @Override
    public void windowDeiconified(WindowEvent e) { }
    @Override
    public void windowActivated(WindowEvent e) { }
    @Override
    public void windowDeactivated(WindowEvent e) { }
}
