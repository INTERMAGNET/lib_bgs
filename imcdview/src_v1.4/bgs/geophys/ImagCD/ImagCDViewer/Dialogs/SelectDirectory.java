/*
 * SelectDirectory.java
 *
 * Created on 04 June 2002, 11:01
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * A dialog that allows the user to select a directory.
 *
 * @author  Simon
 * @version 
 */
public class SelectDirectory extends JDialog
implements ActionListener, ListSelectionListener, MouseListener, WindowListener
{

  // private members
  private JComboBox file_roots_combo_box;           // the file roots combo box
  private File [] file_system_roots;                // list of roots to the file system
  private DefaultListModel directory_list_contents; // list of the directories
  private JList directory_list;
  private JTextField selected_dir_text;             // text holding the selected directory
  private File current_dir;                         // the directory being viewed
  private int directory_level;                      // 0 = root, 1 = subdir, 2 = sub-subdir, etc.
  private final String cdup_name = ".. (up one)";   // string used to go up one directory
  private Vector<DirectoryListener> directory_listeners;               // list of callbacks when new directory is chosen
  private File selected_dir;                        // the directory selected by the user (may be null for no selection)
  
  /** Creates new SelectDirectory
   * @param owner the owner of this dialog */
  public SelectDirectory (Frame owner) 
  {
    // create the dialog
    super (owner, "Select Directory", false);
    selected_dir = null;
    createComponents ();
  }

  /** Creates new SelectDirectory
   * @param owner the owner of this dialog */
  public SelectDirectory (Dialog owner) 
  {
    // create the dialog
    super (owner, "Select Directory", false);
    selected_dir = null;
    createComponents ();
  }

  /** Creates new SelectDirectory
   * @param owner the owner of this dialog
   * @param modal true for a modal dialog */
  public SelectDirectory (Frame owner, boolean modal) 
  {
    // create the dialog
    super (owner, "Select Directory", modal);
    selected_dir = null;
    createComponents ();
  }

  /** Creates new SelectDirectory
   * @param owner the owner of this dialog
   * @param modal true for a modal dialog */
  public SelectDirectory (Dialog owner, boolean modal) 
  {
    // create the dialog
    super (owner, "Select Directory", modal);
    selected_dir = null;
    createComponents ();
  }

  /** Creates new SelectDirectory
   * @param owner the owner of this dialog
   * @param title the title for the dialog
   * @param modal true for a modal dialog */
  public SelectDirectory (Frame owner, String title, boolean modal) 
  {
    // create the dialog
    super (owner, title, modal);
    selected_dir = null;
    createComponents ();
  }

  /** Creates new SelectDirectory
   * @param owner the owner of this dialog
   * @param title the title for the dialog
   * @param modal true for a modal dialog */
  public SelectDirectory (Dialog owner, String title, boolean modal) 
  {
    // create the dialog
    super (owner, title, modal);
    selected_dir = null;
    createComponents ();
  }

  /** set up the components */
  private void createComponents ()
  {
    // declare local variables
    int count;
    JScrollPane scroll_pane;
    JButton button;
    Container content_pane;
    
    
    // flag that no directory is currently selected
    current_dir = null;
    directory_level = -1;
    directory_listeners = new Vector<DirectoryListener> ();
    
    // sort out how to close the dialog
    this.setDefaultCloseOperation(SelectDirectory.DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
    
    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout (new GridBagLayout ());

    // insert the filesystem roots in a combo box
    file_system_roots = File.listRoots();
    file_roots_combo_box = new JComboBox ();
    for (count=0; count<file_system_roots.length; count++)
    {
      file_roots_combo_box.addItem(file_system_roots[count].getPath());
    }
    file_roots_combo_box.addActionListener (this);
    file_roots_combo_box.setActionCommand ("file_roots_combo_box");

    // create a list that will contain directories 
    directory_list_contents = new DefaultListModel ();
    directory_list = new JList (directory_list_contents);
    scroll_pane = new JScrollPane (directory_list);
    directory_list.setVisibleRowCount(3);
    directory_list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
    directory_list.addListSelectionListener (this);
    directory_list.addMouseListener (this);
    directory_list.setCellRenderer (new IconListCellRenderer (GlobalObjects.open_folder_icon, GlobalObjects.folder_icon));

    // create a text field that will show the user what has been selected
    selected_dir_text = new JTextField ();
    
    // layout the dialog
    button = SwingUtils.createButton ("OK", "ok", this);
    SwingUtils.addToGridBag (file_roots_combo_box,                               content_pane, 0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);
    SwingUtils.addToGridBag (scroll_pane,                                        content_pane, 0, 1, 3, 3, 1.0, 1.0, GridBagConstraints.BOTH);
    SwingUtils.addToGridBag (new JLabel ("Directory: "),                         content_pane, 0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NONE);
    SwingUtils.addToGridBag (selected_dir_text,                                  content_pane, 1, 5, 2, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);
    SwingUtils.addToGridBag (button,                                             content_pane, 0, 6, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);
    SwingUtils.addToGridBag (SwingUtils.createButton ("Cancel", "cancel", this), content_pane, 1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL);
    getRootPane().setDefaultButton (button);

    // fill the directory selection list -
    // insert this root in the selected directory text field, find the directories for this root and put them in the list
    current_dir = file_system_roots [0];
    selected_dir_text.setText (current_dir.getPath ());
    directory_level = 0;
    listDirectory (file_system_roots [0]);
    
    // set the dialogs size and show it
    setSize (300, 200);
    setVisible (true);
  }

  /** Add an object to be called when the directory is chosen
   * @param directory_listener the object that will be called */
  public void addDirectoryListener (DirectoryListener directory_listener)
  {
    directory_listeners.addElement (directory_listener);
  }
  
  /** Utility function to list a directory
   * @param base_dir the directory to list */
  private void listDirectory (File base_dir)
  {
    int count;
    String name;
    File files [];
    Cursor old_cursor;

    old_cursor = getCursor();
    setCursor(GlobalObjects.wait_cursor);

    files = base_dir.listFiles ();
    if (files.length > 1) Arrays.sort (files);
    directory_list_contents.removeAllElements ();
    if (directory_level > 0) directory_list_contents.addElement (cdup_name);
    if (files != null)
    {
      for (count=0; count<files.length; count++)
      {
        if (files [count].isDirectory())
        {
          name = files [count].getName ();
          if (! name.equals ("..") && ! name.equals ("."))
            directory_list_contents.addElement (files[count].getName());
        }
      }
    }
    
    setCursor(old_cursor);
  }
  
  /** Catch actions in this frame
   * @param event object describing the event */
  public void actionPerformed (ActionEvent event)
  {
    int selected_index, count;
    boolean dir_ok;
    String command;
    DirectoryListener directory_listener;
    
    command = event.getActionCommand ();
    if (command.equals ("file_roots_combo_box"))
    {
      // get the index of the selected root
      selected_index = file_roots_combo_box.getSelectedIndex ();
      
      // insert this root in the selected directory text field
      current_dir = file_system_roots [selected_index];
      selected_dir_text.setText (current_dir.getPath ());
      directory_level = 0;
      
      // find the directories for this root and put them in the list
      listDirectory (file_system_roots [selected_index]);
    }
    else if (command.equals ("ok"))
    {
      // call any one who is listening
      dir_ok = true;
      selected_dir = new File (selected_dir_text.getText ());
      for (count=0; count<directory_listeners.size(); count++)
      {
        directory_listener = (DirectoryListener) directory_listeners.elementAt (count);
        if (! directory_listener.DirectoryChosen (selected_dir)) dir_ok = false;
      }
      if (dir_ok) this.dispose ();
    }
    else if (command.equals ("cancel"))
    {
      this.dispose ();
    }
  }

  /** Catch list events in this frame
   * @param event object describing the event */
  public void valueChanged (ListSelectionEvent event)
  {
    int selected_index;
    String name;
    
    selected_index = directory_list.getSelectedIndex ();
    if (selected_index > -1)
    {
      name = (String) directory_list_contents.get (selected_index);
      if (directory_level == 0) selected_dir_text.setText (current_dir.getPath () + name);
      else if (name.equals (cdup_name)) selected_dir_text.setText (current_dir.getPath ());
      else selected_dir_text.setText (current_dir.getPath () + File.separator + name);
    }
  }

  /** find the directory that was selected after the user closes the dialog
   * @return the directory or null for no selection */
  public File getSelectedDir () { return selected_dir; }
  
  /** Invoked when the mouse has been clicked on a component.
   * @param e the mouse event */
  public void mouseClicked(MouseEvent e) 
  {
    int selected_index;
    String name;
    
    if (e.getClickCount() == 2) 
    {
      selected_index = directory_list.locationToIndex (e.getPoint());
      if (selected_index > -1)
      {
        name = (String) directory_list_contents.get (selected_index);

        // set the new current directory 
        if (name.equals (cdup_name))
        {
          current_dir = current_dir.getParentFile ();
          directory_level --;
        }
        else
        {
          current_dir = new File (current_dir, name);
          directory_level ++;
        }

        // find the sub-directories and put them in the list
        listDirectory (current_dir);
      }
    }
  }
  
  /** Invoked when the mouse enters a component. 
   * @param e the mouse event */
  public void mouseEntered(MouseEvent e) 
  {
  }
  
  /** Invoked when the mouse exits a component. 
   * @param e the mouse event */
  public void mouseExited(MouseEvent e) 
  {
  }
  
  /** Invoked when a mouse button has been pressed on a component. 
   * @param e the mouse event */
  public void mousePressed(MouseEvent e) 
  {
  }
  
  /** Invoked when a mouse button has been released on a component. 
   * @param e the mouse event */
  public void mouseReleased(MouseEvent e) 
  {
  }
  
  public void windowActivated(WindowEvent e) {  }
  public void windowClosed(WindowEvent e) {  }
  public void windowClosing(WindowEvent e)  { closeDialog (e); }
  public void windowDeactivated(WindowEvent e) {  }
  public void windowDeiconified(WindowEvent e) {  }
  public void windowIconified(WindowEvent e) {  }
  public void windowOpened(WindowEvent e) {  }
  
  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) 
  {
    setVisible(false);
    dispose();
  }
 
}