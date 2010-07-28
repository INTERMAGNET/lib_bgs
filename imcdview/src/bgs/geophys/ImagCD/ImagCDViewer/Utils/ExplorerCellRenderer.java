/*
 * ExplorerCellRenderer.java
 *
 * Created on 10 June 2002, 22:32
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;

/**
 * A class to render tree nodes for the CD explorer
 * @author  Simon Flower
 * @version 
 */
public class ExplorerCellRenderer extends DefaultTreeCellRenderer 
{

  /** Creates new ExplorerCellRenderer */
  public ExplorerCellRenderer() 
  {
    super ();
  }

  /** over-ride getTreeCellRendererComponent to draw the object
   * @param tree the tree being drawn
   * @param value the object (node) to draw
   * @param sel true if htis node is selected
   * @param expanded true if this node is expanded
   * @param leaf true if this is a leaf node
   * @param row ???
   * @param hasFocus true if this node has inout focus */
  public Component getTreeCellRendererComponent (JTree tree, Object value, boolean sel,
                                                 boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
    
    ExplorerNode node;
    String string;
    
    node = (ExplorerNode) value;
    switch (node.getLevel ())
    {
    case ExplorerNode.TOP_LEVEL_NODE:
      setIcon (GlobalObjects.imag_icon); 
      break;
    case ExplorerNode.DATABASE_DIR_NODE:
      setIcon (GlobalObjects.filesystem_icon);
      break;
    case ExplorerNode.YEAR_NODE:
    case ExplorerNode.OBSERVATORY_NODE:
    case ExplorerNode.CN_OB_SELECT_NODE:
    case ExplorerNode.COUNTRY_NODE:
      if (expanded) setIcon (GlobalObjects.open_folder_icon);
      else setIcon (GlobalObjects.folder_icon); 
      break;
    case ExplorerNode.COUNTRY_FILE_NODE:
    case ExplorerNode.OBSERVATORY_FILE_NODE:
    case ExplorerNode.DATA_FILE_NODE:
    case ExplorerNode.DATABASE_FILE:
      string = this.getText().toLowerCase();
      if (string.startsWith("check")) setIcon (GlobalObjects.check_icon); 
      else setIcon (GlobalObjects.file_icon); 
      break;
    case ExplorerNode.DATA_FILE_PRODUCTS_NODE:
      string = this.getText().toLowerCase();
      if (string.contains("export")) setIcon (GlobalObjects.export_icon);
      else if (string.contains("binary file")) setIcon (GlobalObjects.table_icon);
      else setIcon (GlobalObjects.plot_icon); 
      break;
    default: 
      setIcon (null); 
      break;
    }
    return this;
  }
  
}
