/*
 *
 * SelectHostDialog.java
 *
 * Created on 27 August 2002, 16:11
 */

package bgs.geophys.library.Swing.DataLogger;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import bgs.geophys.library.DataLogger.*;
import bgs.geophys.library.DataLogger.SystemList.*;

/**
 *
 * @author  fmc
 * @version 
 */
public class SelectHostDialog extends JDialog implements ActionListener, TreeSelectionListener
{

  /** class to hold ip addresses and names of remote systems */
  private class SystemInfo extends Object
  {
    /** the name of the system */
    public String name;
    /** the address of the system */
    public String address;
    /** the address of the SSH server */
    public String ssh_server;

    /** create a new SystemInfo object
     * @param name the name of the remote system
     * @param address the address of the remote system */
    public SystemInfo (String name, String address, String ssh_server)
    {
      this.name = name;
      this.address = address;
      this.ssh_server = ssh_server;
    }
    /** overrid toString to display the system name
     * @return the system name */
    public String toString () { return name; }
  }

  // components
  private JLabel sshLabel;
  private JTextField hostText, sshText;
  private JCheckBox useSshBox;
  private JButton okButton, cancelButton, helpButton;
  private JTree tree;
  
  private boolean dialog_cancelled;
  
  /************************************************************
   * SelectHost - dialogue for entering hostname
   * to use for connection to remote host. This dialogue is
   * modal and so when control is returned to the calling
   * routine a hostname will be available from the dialogue.
   * Call like this:
   *     host_dialog = new SelectHost (systems_filename);
   *     host_dialog.setVisible(true);
   *     hostname = hostDialog.getHostname ();
   * If "Cancel" was pressed the hostname will be null.
   * 
   * @param systems_list - a list of systems that the user
   * can connect to - null if no such list exists
   ************************************************************/
  public SelectHostDialog(SystemList system_list)
  {
    int count, count2;
    JLabel label;
    JPanel contentPanel, topPanel, labelPanel, textPanel, boxPanel, controlPanel;
    DefaultMutableTreeNode system_tree, section_tree, system_node;
    JScrollPane tree_view;
    SystemDetails system_details;
    String data_server_connect_string;

    setTitle ("Select Host");
    setResizable (false);
    setModal (true);

    // set up dialogue panels
    contentPanel = (JPanel) getContentPane();
    contentPanel.setLayout (new BorderLayout ());

    topPanel = new JPanel ();
    topPanel.setLayout (new BorderLayout ());
    contentPanel.add ("North", topPanel);

    labelPanel = new JPanel();
    labelPanel.setLayout (new GridLayout (2, 1));
    topPanel.add ("West", labelPanel);
    
    textPanel = new JPanel ();
    textPanel.setLayout (new GridLayout (2, 1));
    topPanel.add ("Center", textPanel);

    boxPanel = new JPanel ();
    boxPanel.setLayout (new GridLayout (2, 1));
    topPanel.add ("East", boxPanel);

    controlPanel = new JPanel();
//    controlPanel.setLayout (new GridLayout (1, 3, 5, 5));
    controlPanel.setLayout (new GridLayout (1, 2, 5, 5));
    controlPanel.setBorder(BorderFactory.createEtchedBorder());
    contentPanel.add ("South",controlPanel);

    // create labels
    labelPanel.add (new JLabel ("Host name:"));
    sshLabel = new JLabel ("SSH server:");
    labelPanel.add (sshLabel);
    sshLabel.setEnabled (false);

    // create text boxes
    hostText = new JTextField (20);
    textPanel.add (hostText);
    sshText = new JTextField (20);
    textPanel.add (sshText);
    sshText.setEnabled (false);

    // create checkbox
    boxPanel.add (new JLabel (""));
    useSshBox = new JCheckBox ("Use SSH");
    boxPanel.add (useSshBox);
    useSshBox.addActionListener (this);

    // get a list of remote systems
    if (system_list != null)
    {
        // create a tree from the system list
        system_tree = new DefaultMutableTreeNode ("Remote Systems");
        for (count=0; count<system_list.getNSections (); count++)
        {
            section_tree = new DefaultMutableTreeNode (system_list.getSectionName(count));
            system_tree.add (section_tree);
            for (count2=0; count2<system_list.getNSystems (count); count2++)
            {
                system_details = system_list.getSystemDetails(count, count2);
                data_server_connect_string = system_details.getIpAddress() + ":" +
                                             Integer.toString (system_details.getDataIpPort()) + "," +
                                             system_details.getProtocolName();
                system_node = new DefaultMutableTreeNode (new SystemInfo (system_details.getDisplayName(), data_server_connect_string, system_details.getSshServer ()));
                section_tree.add (system_node);
            }
        }
        tree = new JTree (system_tree);
        tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener (this);

        // create the scroll pane and add the tree to it. 
        tree_view = new JScrollPane (tree);
        
        // add the scroll pane to the dialog
        contentPanel.add ("Center", tree_view);
    }
    
    // add buttons
    okButton = new JButton ("OK");
    controlPanel.add (okButton);
    okButton.addActionListener (this);
    getRootPane().setDefaultButton (okButton);

    cancelButton = new JButton ("Cancel");
    controlPanel.add (cancelButton);
    cancelButton.addActionListener (this);

    // pack components into dialogue
    this.pack();
    
    dialog_cancelled = false;
  }

  /** set the value of the text in the host name box on the dialog
   * @param host_text new host name value */
  public void setHostText (String host_text) 
  { 
      hostText.setText(host_text); 
  }
  
  /** set the value of the text in the SSH host name box on the dialog
   * @param ssh_text new host name value */
  public void setSshText (String ssh_text) 
  { 
      sshText.setText (ssh_text); 
  }

  /** enable usage of ssh
   * @param enable "true", "yes", "1" to enable, anything else to disable */
  public void enableSsh (String enable)
  {
      if (enable.equalsIgnoreCase("true") || enable.equalsIgnoreCase("t") ||
          enable.equalsIgnoreCase("yes")  || enable.equalsIgnoreCase ("y") ||
          enable.equalsIgnoreCase("1")) enableSsh (true);
      else enableSsh (false);
  }

  /** enable usage of ssh
   * @param enable true to enable, false to disable */
  public void enableSsh (boolean enable)
  {
      sshText.setEnabled (enable);
      sshLabel.setEnabled (enable);
      useSshBox.setSelected (enable);
  }
  
  public void actionPerformed (ActionEvent actionEvent)
  {
    Object source;

    source = actionEvent.getSource ();
    if (source.equals (okButton))
    {
      dialog_cancelled = false;
      setVisible (false);
    }
    else if (source.equals (cancelButton))
    {
      dialog_cancelled = true;
      setVisible (false);
    }
    else if (source.equals (useSshBox))
    {
      sshLabel.setEnabled (useSshBox.isSelected ());
      sshText.setEnabled (useSshBox.isSelected ());
    }
  }

  public void valueChanged(TreeSelectionEvent e)
  {
    DefaultMutableTreeNode node;
    SystemInfo nodeInfo;
    
    node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    if (node == null) return;
    
    if (node.isLeaf())
    {
        nodeInfo = (SystemInfo) node.getUserObject();
        hostText.setText (nodeInfo.address);
        sshText.setText (nodeInfo.ssh_server);
    }
  }

  /** get the hostname entered - null if the user pressed cancel 
   * @return the hostname */
  public String getHostname ()
  {
    if (dialog_cancelled) return null;
    return hostText.getText();
  }

  /** get the SSH string in the format user@host:port
   * @return the server name - null if ssh is not to be used */
  public String getSSHHostname ()
  {
    if (dialog_cancelled) return null;
    if (! useSshBox.isSelected()) return null;
    return sshText.getText ();
  }
}
