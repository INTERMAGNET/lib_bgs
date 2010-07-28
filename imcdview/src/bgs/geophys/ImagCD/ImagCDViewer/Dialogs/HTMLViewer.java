/*
 * HTMLViewer.java
 *
 * Created on 11 October 2004, 17:19
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * A simple HTML viewer. The html hyperlinkes may include a link of the form
 *    internal_exec:<command>
 * Where command is a string that will be passed to the programs command interpreter
 * This allows you to do things like raise dialogs from within help files.
 *
 * @author  smf
 */
public class HTMLViewer extends javax.swing.JDialog {
    
    private Vector<URL> page_history;
    private int page_history_index;
    

    
    /** Creates new form HTMLViewer */
    public HTMLViewer(java.awt.Frame parent, boolean modal, URL url) 
    {
        super(parent, modal);
        initComponents();
        
        getRootPane().setDefaultButton (CloseButton);
        
        page_history = new Vector<URL> ();
        page_history_index = -1;
        
        gotoPage (url, true);
    }

    public HTMLViewer(java.awt.Dialog parent, boolean modal, URL url) 
    {
        super(parent, modal);
        initComponents();
        
        getRootPane().setDefaultButton (CloseButton);
        
        page_history = new Vector<URL> ();
        page_history_index = -1;
        
        gotoPage (url, true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        EditorPane = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        BackwardsButton = new javax.swing.JButton();
        CloseButton = new javax.swing.JButton();
        ForwardsButton = new javax.swing.JButton();

        setTitle("HTML Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        EditorPane.setEditable(false);
        EditorPane.setPreferredSize(new java.awt.Dimension(600, 300));
        EditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                EditorPaneHyperlinkUpdate(evt);
            }
        });
        EditorPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                EditorPanePropertyChange(evt);
            }
        });

        jScrollPane1.setViewportView(EditorPane);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        BackwardsButton.setText("Backwards");
        BackwardsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackwardsButtonActionPerformed(evt);
            }
        });

        jPanel1.add(BackwardsButton, java.awt.BorderLayout.WEST);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        jPanel1.add(CloseButton, java.awt.BorderLayout.CENTER);

        ForwardsButton.setText("Forwards");
        ForwardsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ForwardsButtonActionPerformed(evt);
            }
        });

        jPanel1.add(ForwardsButton, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void EditorPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_EditorPanePropertyChange
        Object title;
        javax.swing.text.html.HTMLDocument doc;
        
        if (evt.getPropertyName().equals ("page"))
        {
            try
            {
                doc = (javax.swing.text.html.HTMLDocument) EditorPane.getDocument();
                title = doc.getProperty (javax.swing.text.html.HTMLDocument.TitleProperty);
                if (title != null) this.setTitle (title.toString ());
            }
            catch (ClassCastException e) { }
        }
    }//GEN-LAST:event_EditorPanePropertyChange

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        closeDialog (null);
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void EditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_EditorPaneHyperlinkUpdate
        String prefix, name, command;
        
        if (evt.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)
        {
            name = evt.getDescription ();
            prefix = "internal_exec:";
            if (name.indexOf(prefix) == 0)
            {
                command = name.substring (prefix.length());
                GlobalObjects.command_interpreter.interpretCommand(command);
            }
            else
                gotoPage (evt.getURL(), true);
        }
    }//GEN-LAST:event_EditorPaneHyperlinkUpdate

    private void BackwardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardsButtonActionPerformed
        if (page_history_index > 0)
        {
            page_history_index --;
            gotoPage ((URL) page_history.get (page_history_index), false);
            setupNavigationButtons ();
        }
    }//GEN-LAST:event_BackwardsButtonActionPerformed

    private void ForwardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForwardsButtonActionPerformed
        if (page_history_index < page_history.size () -1)
        {
            page_history_index ++;
            gotoPage ((URL) page_history.get (page_history_index), false);
            setupNavigationButtons ();
        }
    }//GEN-LAST:event_ForwardsButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /** goto to given HTML page
     * @param url the page to go to 
     * @param update_history true to modify the history on successful load */
    public void gotoPage (URL url, boolean update_history)
    {
        GlobalObjects.debug_manager.logDebugMessage ("HTML Viewer", url.toString());
        
        try
        {
            this.setTitle("HTML Viewer - " + url.toString ());
            EditorPane.setPage(url);
            if (update_history)
            {
                page_history.add (url);
                page_history_index = page_history.size() -1;
                setupNavigationButtons ();
            }
        }
        catch (IOException e) 
        {
              javax.swing.JOptionPane.showMessageDialog (this, "Unable to display page " + url.toString(), "Error Loading Page", javax.swing.JOptionPane.ERROR_MESSAGE);
        };
    }
    
    /** configure the navigation buttons according to the current
     *  position in the history */
    private void setupNavigationButtons ()
    {
        if (page_history_index >= 1) BackwardsButton.setEnabled(true);
        else BackwardsButton.setEnabled(false);
        if (page_history_index < (page_history.size() -1) && page_history_index >= 0) ForwardsButton.setEnabled(true);
        else ForwardsButton.setEnabled(false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackwardsButton;
    private javax.swing.JButton CloseButton;
    private javax.swing.JEditorPane EditorPane;
    private javax.swing.JButton ForwardsButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
}
