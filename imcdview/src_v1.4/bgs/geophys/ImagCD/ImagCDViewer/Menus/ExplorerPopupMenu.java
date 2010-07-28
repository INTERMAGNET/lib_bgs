/*
 * ExplorerPopupMenu.java
 *
 * Created on 11 June 2002, 22:16
 */

package bgs.geophys.ImagCD.ImagCDViewer.Menus;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.event.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * Popup menu for explorer node actions.
 * @author  Simon
 * @version 
 */
public class ExplorerPopupMenu extends JPopupMenu
{

  /** Creates new ExplorerPopupMenu
   * @param node the node that the popup menu operates on */
  public ExplorerPopupMenu (ExplorerNode node)
  {
    super ();
    
    int count;
    String title, commands [], labels [];
    boolean first_item_flag;
    ExplorerNode child_node;
    
    // put the common items on the menu
    add (SwingUtils.CreateMenuItem ("Select data sources...", "raise_data_source_dialog&from_explorer_window", GlobalObjects.command_interpreter));
    add (SwingUtils.CreateMenuItem ("Reload data", "reload_data_from_source", GlobalObjects.command_interpreter));
    
    // put the node specific items on the menu
    switch (node.getLevel ())
    {
    case ExplorerNode.DATABASE_DIR_NODE:
    case ExplorerNode.YEAR_NODE:
      addSeparator ();
      add (SwingUtils.CreateMenuItem ("Copy...", "copy_data_source&" + node.getUserData (), GlobalObjects.command_interpreter));
      add (SwingUtils.CreateMenuItem ("Move...", "move_data_source&" + node.getUserData (), GlobalObjects.command_interpreter));
      add (SwingUtils.CreateMenuItem ("Delete", "delete_data_source&" + node.getUserData (), GlobalObjects.command_interpreter));
      break;
    }
    
    // if this node is a leaf, extract its viewing command, otherwise
    // extract the viewing commands of its children
    if (node.isLeaf())
    {
      commands = new String [1];
      labels = new String [1];
      commands [0] = node.getCommand();
      labels [0] = (String) node.getUserObject ();
    }
    else
    {
      commands = new String [node.getChildCount()];
      labels = new String [node.getChildCount()];
      for (count=0; count<commands.length; count++)
      {
        child_node = (ExplorerNode) node.getChildAt(count);
        if (child_node.isLeaf())
        {
          commands [count] = child_node.getCommand();
          labels [count] = (String) child_node.getUserObject ();
        }
        else commands [count] = labels [count] = null;
      }
    }
    
    // add the viewiing commands to the menu
    first_item_flag = true;
    for (count=0; count<commands.length; count++)
    {
      if (commands [count] != null)
      {
        if (first_item_flag) addSeparator();
        add (SwingUtils.CreateMenuItem (labels [count], commands [count], GlobalObjects.command_interpreter));
        first_item_flag = false;
      }
    }

    // set the menu's title
    title = node.toString ();
    if (title != null) setLabel (title);
  }
    
}
