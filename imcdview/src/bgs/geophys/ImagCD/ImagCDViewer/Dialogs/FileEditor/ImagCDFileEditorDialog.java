/*
 * MainWin.java
 *
 * Created on 03 June 2002, 15:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.EditorListener;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;

/**
 *
 * @author  Simon
 * @version 
 */
public class ImagCDFileEditorDialog extends JDialog
implements WindowListener, ActionListener, ImagCDFileEditorPanel.TitleChangedListener
{
    
    /** options when closing the editor window */
    public enum CloseOptions {NeverSave, QuerySave, AlwaysSave}
            
    private Vector<EditorListener> editor_listeners;
    private ImagCDFileEditorPanel editor_panel;
    private JButton close_button;
    private JButton save_button;
    
    /** Creates new file window without any data */
    public ImagCDFileEditorDialog (Frame owner, boolean modal, boolean editable) 
    {
        super (owner, modal);
        editor_panel = new ImagCDFileEditorPanel (editable);
        commonInitialisation();
    }

    /** Creates new file window without any data */
    public ImagCDFileEditorDialog (Dialog owner, boolean modal, boolean editable) 
    {
        super (owner, modal);
        editor_panel = new ImagCDFileEditorPanel (editable);
        commonInitialisation();
    }
    
    /** Creates new file window without any data */
    public ImagCDFileEditorDialog (Window owner, ModalityType modality_type, boolean editable) 
    {
        super (owner, modality_type);
        editor_panel = new ImagCDFileEditorPanel (editable);
        commonInitialisation();
    }
    
    /** Creates new file window from the given file
     * @throws FileLoadException if there is an error load the file - this
     *         exception will always have a message */
    public ImagCDFileEditorDialog (Frame owner, boolean modal, File file, boolean editable)
    throws ImagCDFileEditorPanel.FileLoadException 
    {
        super (owner, modal);
        editor_panel = new ImagCDFileEditorPanel (file, editable);
        commonInitialisation();
    }

    /** Creates new file window from the given file
     * @throws FileLoadException if there is an error load the file - this
     *         exception will always have a message */
    public ImagCDFileEditorDialog (Dialog owner, boolean modal, File file, boolean editable)
    throws ImagCDFileEditorPanel.FileLoadException 
    {
        super (owner, modal);
        editor_panel = new ImagCDFileEditorPanel (file, editable);
        commonInitialisation();
    }
    
    /** Creates new file window from the given file
     * @throws FileLoadException if there is an error load the file - this
     *         exception will always have a message */
    public ImagCDFileEditorDialog (Window owner, ModalityType modality_type, File file, boolean editable)
    throws ImagCDFileEditorPanel.FileLoadException 
    {
        super (owner, modality_type);
        editor_panel = new ImagCDFileEditorPanel (file, editable);
        commonInitialisation();
    }
    
    /** add an editor listener */
    public void addEditorListener (EditorListener listener)
    {
        editor_listeners.add (listener);
    }
    
    /** remove an editor listener */
    public void removeEditorListener (EditorListener listener)
    {
        editor_listeners.remove (listener);
    }
    
    // things all constructors need to do
    private void commonInitialisation ()
    {
        JPanel panel;

        editor_panel.addTitleChangedListener(this);
        editor_listeners = new Vector<EditorListener> ();
        
        // create the editor pane
        getContentPane().setLayout(new BorderLayout ());
        getContentPane().add (editor_panel, "Center");
        
        // create the buttons
        panel = new JPanel ();
        panel.setLayout (new FlowLayout ());
        close_button = new JButton ("Close");
        close_button.addActionListener (this);
        panel.add (close_button);
        save_button = new JButton ("Save");
        save_button.addActionListener (this);
        panel.add (save_button);
        getContentPane().add (panel, "South");
        
        // set up the main window
        setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(GlobalObjects.imag_icon.getImage());
        setTitle(editor_panel.getWindowTitle());
        pack();
        setVisible (true);
        addWindowListener(this);
    }
    
    // implementation of WindowListner interface
    public void windowActivated(WindowEvent e) {  }
    public void windowClosed(WindowEvent e) {  }
    public void windowClosing(WindowEvent e)  { closeDialog (CloseOptions.QuerySave, false); }
    public void windowDeactivated(WindowEvent e) {  }
    public void windowDeiconified(WindowEvent e) {  }
    public void windowIconified(WindowEvent e) {  }
    public void windowOpened(WindowEvent e) {  }

    public boolean isEdited () { return editor_panel.getDataFile().getFileChanged(); }
    
    /** Closes the dialog */
    public void closeDialog (CloseOptions close_options,
                             boolean close_on_save_failure)
    {
        int option, count;
        boolean do_save, save_failure, do_close;
        String errmsg;

        // work out whether to save the file
        do_save = false;
        if (editor_panel.getDataFile().getFileChanged())
        {
            switch (close_options)
            {
                case AlwaysSave:
                    do_save = true;
                    break;
                case QuerySave:
                    option = JOptionPane.showConfirmDialog(this, 
                                                           "Changes to this file have not been saved. Save changes?", "Save changes?", 
                                                           JOptionPane.YES_NO_OPTION, 
                                                           JOptionPane.QUESTION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) do_save = true;
            }
        }
        
        // save the file
        save_failure = false;
        if (do_save)
        {
            errmsg = editor_panel.getDataFile().writeToFile();
            if (errmsg == null)
            {
                for (count=0; count<editor_listeners.size(); count++)
                    editor_listeners.get(count).savedFile(this, editor_panel.getDataFile().getOpenFile());
            }
            else
            {
                JOptionPane.showMessageDialog(this, errmsg, "Unable to save file", JOptionPane.ERROR_MESSAGE);
                save_failure = true;
            }
        }

        // work out whether to close the dialog
        do_close = true;
        if (save_failure)
        {
            if (! close_on_save_failure) do_close = false;
        }

        // close the dialog
        if (do_close)
        {
            setVisible(false);
            dispose();
            for (count=0; count<editor_listeners.size(); count++)
                editor_listeners.get(count).editorExited(this);
        }
    }
    
    /** retrieve the data file associated with this window
     * @param the data file */
    public ImagCDFile getDataFile ()
    {
       return editor_panel.getDataFile();
    }

    public void actionPerformed(ActionEvent e) 
    {
        int count;
        String errmsg;
        
        if (e.getSource() == close_button)
            closeDialog (CloseOptions.QuerySave, false);
        else if (e.getSource() == save_button)
        {
            errmsg = editor_panel.getDataFile().writeToFile();
            if (errmsg == null)
            {
                for (count=0; count<editor_listeners.size(); count++)
                    editor_listeners.get(count).savedFile(this, editor_panel.getDataFile().getOpenFile());
            }
            else
            {
                JOptionPane.showMessageDialog (this, 
                                               errmsg, 
                                               "Unable to save file", 
                                               JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void NewTitle(String title) 
    {
        setTitle (title);
    }

}
