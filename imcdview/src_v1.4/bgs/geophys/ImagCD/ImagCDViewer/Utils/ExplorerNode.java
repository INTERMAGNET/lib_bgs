/*
 * ExplorerNode.java
 *
 * Created on 10 June 2002, 23:05
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.library.Misc.DateUtils;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * A class that holds data about each node in the explorer.
 * @author  Simon Flower
 * @version 
 */
public class ExplorerNode extends DefaultMutableTreeNode 
{

  /** code for the top level of the tree */
  public static final int TOP_LEVEL_NODE            = 1;
  /** code for the database directory nodes of the tree */
  public static final int DATABASE_DIR_NODE         = 2;
  /** code for the files at the database level (errata, readme, etc.), also used for
   * the files at the YEAR level (README.ALL, all map, all countries, intro) */
  public static final int DATABASE_FILE             = 3;
  /** code for the year nodes of the tree */
  public static final int YEAR_NODE                 = 4;
  /** code for the country or observatory level selection nodes of the tree */
  public static final int CN_OB_SELECT_NODE         = 5;
  /** code for the country nodes of the tree */
  public static final int COUNTRY_NODE              = 6;
  /** code for the contents of the country directories of the tree */
  public static final int COUNTRY_FILE_NODE         = 7;
  /** code for the observatory nodes of the tree */
  public static final int OBSERVATORY_NODE          = 8;
  /** code for the contents of the observatory directories of the tree */
  public static final int OBSERVATORY_FILE_NODE     = 9;
  /** code for the data file nodes of the tree */
  public static final int DATA_FILE_NODE            = 10;
  /** code for the products that can be produced from a data file node */
  public static final int DATA_FILE_PRODUCTS_NODE   = 11;

  // private members
  private int level;    // one of the node level codes above
  private Object user_data;
  private String command;
  private static SimpleDateFormat file_date_format;
          
  // static initialisation
  static
  {
      file_date_format = new SimpleDateFormat ("yyMMM");
      file_date_format.setTimeZone(DateUtils.gmtTimeZone);
  }
  
  /** Creates new ExplorerNode
   * @param display_text the text that the node displays to the user
   * @param level one of the node level codes */
  public ExplorerNode(String display_text, int level)
  {
    super (display_text);
    this.level = level;
    this.user_data = null;
    this.command = null;
  }

  /** Creates new ExplorerNode
   * @param display_text the text that the node displays to the user
   * @param user_data data associated with this node - this is used
   *         to get information from the node about its 'contents'
   * @param level one of the node level codes */
  public ExplorerNode(String display_text, Object user_data, int level)
  {
    super (display_text);
    this.level = level;
    this.user_data = user_data;
    this.command = null;
  }
  
  /** Creates new ExplorerNode
   * @param display_text the text that the node displays to the user
   * @param user_data data associated with this node - this is used
   *         to get information from the node about its 'contents'
   * @param level one of the node level codes
   * @param command the command that this node runs when 'double clicked' */
  public ExplorerNode(String display_text, Object user_data, int level, String command)
  {
    super (display_text);
    this.level = level;
    this.user_data = user_data;
    this.command = command;
  }

  /** get the level that this node represents
   * @returns the level */
  public int getLevel ()
  {
    return level;
  }

  /** get the user data that this node represents
   * @returns the data (may be null) */
  public Object getUserData ()
  {
    return user_data;
  }

  /** get the command that this node runs when double clicked
   * @returns the command (may be null) */
  public String getCommand ()
  {
    return command;
  }

  /** sort the children of this node and optionally recursively their children
   * @param recurse true to descend recursively through children */
  public void sort (boolean recurse) 
  {    
    int cc, i, j, compare;
    ExplorerNode here, there;
    Date hereDate, thereDate;
    String hereString, thereString, hereStation, thereStation;
              
    cc = getChildCount();    
    
    // first do the recursion
    for (i = 0; (i < cc) && recurse; i++) 
    {      
      here = (ExplorerNode) getChildAt (i);
      here.sort (recurse);
    }
    
    // then do the sorting
    for (i = 0; i < cc - 1; i++) 
    {      
      for (j = i+1; j < cc; j++) 
      {
        here = (ExplorerNode) getChildAt (i);
        there = (ExplorerNode) getChildAt(j);
        hereString = here.getUserObject().toString ();
        thereString = there.getUserObject().toString ();
        
        // sorting is done differently, depending on the node's level in the tree
        // also we only compare levels that are the same
        compare = 0;
        if (here.level == there.level)
        {        
          switch (here.level)
          {
            case DATABASE_DIR_NODE:
            case YEAR_NODE: 
            case COUNTRY_NODE:
            case OBSERVATORY_NODE:
              // these nodes have a simple string comparison
              compare = hereString.compareTo(thereString);
              break;
                
            case DATA_FILE_NODE:
              // special sort for CD binary data file names
              try
              {
                hereStation = hereString.substring(0, 3);
                thereStation = thereString.substring(0, 3);
                hereDate = file_date_format.parse (hereString.substring(3, 8));
                thereDate = file_date_format.parse (thereString.substring(3, 8));
                compare = hereStation.compareTo(thereStation);
                if (compare == 0)
                {
                  if (hereDate.getTime() < thereDate.getTime()) compare = -1;
                  else if (hereDate.getTime() > thereDate.getTime()) compare = +1;
                }
                if (compare == 0)
                  compare = hereString.compareTo(thereString);
              }
              catch (IndexOutOfBoundsException e) {compare = 0;}
              catch (ParseException e) {compare = 0;}
              break;
          }
        }
        
        if (compare > 0) 
        {
          super.remove(here);
          super.remove(there);
          super.insert(there, i);
          super.insert(here, j);
        }
      }
    }
  }

}
