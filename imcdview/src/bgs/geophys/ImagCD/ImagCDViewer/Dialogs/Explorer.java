/*
 * Explorer.java
 *
 * Created on 04 June 2002, 11:01
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Menus.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * A dialog that allows the user to explore a CD file structure
 *
 * @author  Simon
 * @version 
 */
public class Explorer extends JFrame
implements MouseListener, WindowListener, ComponentListener
{

  // private members
  private JTree explorer_tree;
  private ExplorerNode top_node;
  private DefaultTreeModel tree_model;
  private JScrollPane scroll_pane;
  
  public static final int MIN_WIDTH = 200;
  public static final int MIN_HEIGHT = 150;

  /** Creates new Explorer */
  public Explorer ()
  {
    // create the dialog
    setTitle("Intermagnet CD Explorer");
    createComponents ();
  }

  /** set up the components */
  private void createComponents ()
  {
    Container content_pane;
    ExplorerCellRenderer renderer;
    
    // set the layout (use the default)
    content_pane = getContentPane();

    // set the operation when the user presses the frame's close button
    setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    
    // create the tree
    top_node = new ExplorerNode ("CD Data Sources", "cd_data_sources", ExplorerNode.TOP_LEVEL_NODE);
    createNodes ();
    top_node.sort (true);
    tree_model = new DefaultTreeModel (top_node);
    explorer_tree = new JTree (tree_model);
    explorer_tree.putClientProperty ("JTree.lineStyle", "Angled");
    renderer = new ExplorerCellRenderer ();
    explorer_tree.setCellRenderer (renderer);
    explorer_tree.addMouseListener (this);
    explorer_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    // put the tree on the dialog
    scroll_pane = new JScrollPane (explorer_tree);
    content_pane.add (scroll_pane);

    // set the dialogs size and show it
    this.setIconImage(GlobalObjects.imag_icon.getImage());
    setSize (300, 600);
    setVisible (true);
  }

  /** Reload the explorer tree */
  public void reload ()
  {
    top_node.removeAllChildren ();
    createNodes ();
    top_node.sort (true);
    tree_model.reload ();
  }

  /** create the nodes for the tree */
  private void createNodes ()
  {
    int database_count, year_count, country_count, observatory_count, month_count;
    boolean error, found_plain_data_files, found_zip_data_files;
    String title;
    File file;
    ExplorerNode database_node, year_node, cn_ob_select_node, country_node, observatory_node, file_node;
    ExplorerNode country_file_node, observatory_file_node, file_products_node;
    CDDatabaseList database_list;
    CDDatabase database;
    CDYear cd_year;
    CDCountry country;
    CDObservatory observatory;
    CDDataMonth month_data;

    // get the database list
    database_list = GlobalObjects.database_list;
    
    // create the tree structure
    error = false;
    for (database_count=0; database_count<database_list.GetNDatabases (); database_count++)
    {
      try
      {
        database = database_list.GetDatabase (database_count);
        database_node = new ExplorerNode (database.GetBaseDir().getAbsolutePath(), database.GetBaseDir().getAbsolutePath(), ExplorerNode.DATABASE_DIR_NODE);
        if (database.GetReadmeFileLength() > 0)
            database_node.add (new ExplorerNode ("CD README", null, ExplorerNode.DATABASE_FILE,
                               "text_file_viewer&from_explorer_window&" + database.GetReadmeFile() + "&CD README"));
        if (database.GetErrataFileLength() > 0)
            database_node.add (new ExplorerNode ("Errata List", null, ExplorerNode.DATABASE_FILE,
                               "text_file_viewer&from_explorer_window&" + database.GetErrataFile() + "&Errata List"));
        for (year_count=0; year_count<database.GetNYears (); year_count++)
        {
          cd_year = database.GetYear (year_count);
          year_node = new ExplorerNode (Integer.toString (cd_year.GetYear ()), database.GetBaseDir().getAbsolutePath(), ExplorerNode.YEAR_NODE);
          if (cd_year.GetFile (CDYear.FILE_README) != null)
              year_node.add (new ExplorerNode (Integer.toString (cd_year.GetYear()) + " README", null, ExplorerNode.DATABASE_FILE,
                             "text_file_viewer&from_explorer_window&" + cd_year.GetFile (CDYear.FILE_README).getAbsolutePath() + "&" + Integer.toString(cd_year.GetYear()) + " README"));
          if (cd_year.GetFile (CDYear.FILE_ALL_COUNTRIES) != null)
              year_node.add (new ExplorerNode ("Contributor List", null, ExplorerNode.DATABASE_FILE,
                             "image_file_viewer&from_explorer_window&" + cd_year.GetFile (CDYear.FILE_ALL_COUNTRIES).getAbsolutePath() + "&Contributor List " + cd_year.GetYear()));
          if (cd_year.GetFile (CDYear.FILE_ALL_MAP) != null)
              year_node.add (new ExplorerNode ("Contributor Map", null, ExplorerNode.DATABASE_FILE,
                             "image_file_viewer&from_explorer_window&" + cd_year.GetFile (CDYear.FILE_ALL_MAP).getAbsolutePath() + "&Contributor Map " + cd_year.GetYear()));
          if (cd_year.GetFile (CDYear.FILE_INTRO) != null)
              year_node.add (new ExplorerNode ("Introduction Screen", null, ExplorerNode.DATABASE_FILE,
                             "image_file_viewer&from_explorer_window&" + cd_year.GetFile (CDYear.FILE_INTRO).getAbsolutePath() + "&Introduction Screen " + cd_year.GetYear()));
          if (cd_year.GetNCountries () > 0)
          {
            cn_ob_select_node = new ExplorerNode ("Country", "country", ExplorerNode.CN_OB_SELECT_NODE);
            for (country_count=0; country_count<cd_year.GetNCountries (); country_count ++)
            {
              country = cd_year.GetCountry (country_count);
              country_node = new ExplorerNode (country.country_code + " (" + country.country_name + ")", country, ExplorerNode.COUNTRY_NODE);
              if (country.readme_text != null)
              {
                country_file_node = new ExplorerNode ("Country Information", country, ExplorerNode.COUNTRY_FILE_NODE,
                                                      "text_file_viewer&from_explorer_window&" + country.readme_text.getAbsolutePath () +
                                                      "&" + "Country information for " +
                                                      country.country_name + " " + cd_year.GetYear());
                country_node.add (country_file_node);
              }
              if (country.map_plot != null)
              {
                country_file_node = new ExplorerNode ("Country Map", country, ExplorerNode.COUNTRY_FILE_NODE,
                                                      "image_file_viewer&from_explorer_window&" + country.map_plot.getAbsolutePath () +
                                                      "&" + "Country map for " +
                                                      country.country_name + " " + cd_year.GetYear());
                country_node.add (country_file_node);
              }
              if (country.agency_plot != null)
              {
                country_file_node = new ExplorerNode ("Institute Details", country, ExplorerNode.COUNTRY_FILE_NODE,
                                                      "image_file_viewer&from_explorer_window&" + country.agency_plot.getAbsolutePath () +
                                                      "&" + "Institute details for " +
                                                      country.country_name + " " + cd_year.GetYear());
                country_node.add (country_file_node);
              }
              cn_ob_select_node.add (country_node);
            }
            year_node.add (cn_ob_select_node);
          }
          if (cd_year.GetNObservatories () > 0)
          {
            cn_ob_select_node = new ExplorerNode ("Observatory", "observatory", ExplorerNode.CN_OB_SELECT_NODE);
            for (observatory_count=0; observatory_count<cd_year.GetNObservatories (); observatory_count ++)
            {
              observatory = cd_year.GetObservatory (observatory_count);
              observatory_node = new ExplorerNode (observatory.GetObservatoryCode () + " (" + observatory.GetObservatoryName() + ")", observatory, ExplorerNode.OBSERVATORY_NODE);
              found_plain_data_files = found_zip_data_files = false;

              if (observatory.GetFile (CDObservatory.FILE_YEAR_MEAN) != null)
              {
                observatory_file_node = new ExplorerNode ("Annual Means (text file)", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "text_file_viewer&from_explorer_window&" + 
                                                          observatory.GetFile (CDObservatory.FILE_YEAR_MEAN).getAbsolutePath () +
                                                          "&" + "Historic yearly means for " + observatory.GetObservatoryName () + " " + observatory.GetYear ());
                observatory_node.add (observatory_file_node);
                observatory_file_node = new ExplorerNode ("Annual Mean Viewer", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "annual_mean_viewer&" + observatory.GetObservatoryCode() +
                                                          "&" + observatory.GetYear () +
                                                          "&" + database.GetBaseDir().getAbsolutePath());
                observatory_node.add (observatory_file_node);
              }
              if (observatory.GetFile (CDObservatory.FILE_BLV_TEXT) != null)
              {
                observatory_file_node = new ExplorerNode ("Baseline Values (text file)", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "text_file_viewer&from_explorer_window&" + observatory.GetFile(CDObservatory.FILE_BLV_TEXT).getAbsolutePath() +
                                                          "&" + "Baseline values for " + observatory.GetObservatoryName () + " " + observatory.GetYear());
                observatory_node.add (observatory_file_node);
              }
              if (observatory.GetFile (CDObservatory.FILE_BLV_PLOT) != null)
              {
                observatory_file_node = new ExplorerNode ("Baseline Plot (graphics file)", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "image_file_viewer&from_explorer_window&" + observatory.GetFile(CDObservatory.FILE_BLV_PLOT).getAbsolutePath() +
                                                          "&" + "Baseline value plot for " + observatory.GetObservatoryName () + " " + observatory.GetYear());
                observatory_node.add (observatory_file_node);
              }
             if (observatory.GetFile (CDObservatory.FILE_BLV_PLOT) != null)
              {
                observatory_file_node = new ExplorerNode ("Baseline Viewer", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "baseline_viewer&" + observatory.GetObservatoryCode() +
                                                          "&" + observatory.GetYear() +
                                                          "&" + database.GetBaseDir().getAbsolutePath());
                observatory_node.add (observatory_file_node);
              }
             
             if (observatory.GetFile (CDObservatory.FILE_README) != null)
              {
                observatory_file_node = new ExplorerNode ("Observatory Information", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "text_file_viewer&from_explorer_window&" + 
                                                          observatory.GetFile (CDObservatory.FILE_README).getAbsolutePath () +
                                                          "&" + "Observatory information for " + observatory.GetObservatoryName () + " " + observatory.GetYear ());
                observatory_node.add (observatory_file_node);
              }
              if (observatory.GetFile (CDObservatory.FILE_DKA) != null)
              {
                observatory_file_node = new ExplorerNode ("K index Text File Values", observatory, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                          "text_file_viewer&from_explorer_window&" + observatory.GetFile(CDObservatory.FILE_DKA).getAbsolutePath() +
                                                          "&" + "K index text file for " + observatory.GetObservatoryName () + " " + observatory.GetYear());
                observatory_node.add (observatory_file_node);
              }
              
              for (month_count=0; month_count<12; month_count ++)
              {
                // look for files that are not zipped
                file = observatory.GetMonthFile (month_count+1, false);
                if (file != null)
                {
                  found_plain_data_files = true;
                  month_data = new CDDataMonth (month_count+1, observatory, false);
                  file_node = new ExplorerNode (file.getName (), month_data, ExplorerNode.DATA_FILE_NODE);
                  file_products_node = new ExplorerNode ("View binary file", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "view_binary&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex ()-1) +
                                                         "&false&true" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("Data Plot", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "view_data&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex ()-1) +
                                                         "&false&true" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("K Index Plot", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "k_index_viewer&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () +
                                                         "&false&true" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("Export Data", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "raise_export_dialog&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex () -1) +
                                                         "&false&true" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  observatory_node.add (file_node);
                }
                // look for files that are zipped
                file = observatory.GetMonthFile (month_count+1, true);
                if (file != null)
                {
                  found_zip_data_files = true;
                  month_data = new CDDataMonth (month_count+1, observatory, true);
                  file_node = new ExplorerNode (file.getName (), month_data, ExplorerNode.DATA_FILE_NODE);
                  file_products_node = new ExplorerNode ("View binary file", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "view_binary&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex ()-1) +
                                                         "&true&false" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("Data Plot", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "view_data&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex ()-1) +
                                                         "&true&false" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("K Index Plot", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "k_index_viewer&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () +
                                                         "&true&false" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  file_products_node = new ExplorerNode ("Export Data", month_data, ExplorerNode.DATA_FILE_PRODUCTS_NODE,
                                                         "raise_export_dialog&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                         "&" + observatory.GetYear () + "&" + (month_data.getMonthIndex () -1) +
                                                         "&true&false" +
                                                         "&" + database.GetBaseDir().getAbsolutePath());
                  file_node.add (file_products_node);
                  observatory_node.add (file_node);
                }
              }
              if (found_plain_data_files)
              {
                if (found_zip_data_files) title = "Check annual means (from plain data)";
                else title = "Check annual means";
                file_products_node = new ExplorerNode (title, null, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                       "mean_viewer&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                       "&" + observatory.GetYear () + "&false&true" +
                                                       "&" + database.GetBaseDir().getAbsolutePath());
                observatory_node.add (file_products_node);
              }
              if (found_zip_data_files)
              {
                if (found_plain_data_files) title = "Check annual means (from zip data)";
                else title = "Check annual means";
                file_products_node = new ExplorerNode (title, null, ExplorerNode.OBSERVATORY_FILE_NODE,
                                                       "mean_viewer&from_explorer_window&" + observatory.GetObservatoryCode () +
                                                       "&" + observatory.GetYear () + "&true&false" +
                                                       "&" + database.GetBaseDir().getAbsolutePath());
                observatory_node.add (file_products_node);
              }
              cn_ob_select_node.add (observatory_node);
            }
            year_node.add (cn_ob_select_node);
          }
          database_node.add (year_node);
        }
        top_node.add (database_node);
      }
      catch (CDException e) { error = true; }
    }

    // check for errors
    if (error)
      JOptionPane.showMessageDialog (this, "Program error creating explorer tree", "Please report this bug", JOptionPane.ERROR_MESSAGE);
  }
  
 /********************************************************************
  * getLocationInScrollPane - MouseEvent position is returned relative
  * to the top left co-ordinates of JTree. This means that if the
  * window has been scrolled down, the event position could be below
  * the window. This method calculates the location of the event in
  * the visible part of the JScrollPane.
  *
  * @param x - the x co-ordinate of the MouseEvent position
  * @param y - the y co-ordinate of the MouseEvent position
  * @param p - the Point object containing the co-ordinates of the
  *            MouseEvent in the visible part of the JScrollPane.
  ********************************************************************/
  private Point getLocationInScrollPane (int x, int y)
  {
    JViewport viewport;
    Point topleft;
    int xpos, ypos;

    // find viewport of JScrollPane - this is the part that is visible
    viewport = scroll_pane.getViewport();
    topleft = viewport.getViewPosition();

    // calculate position of mouse event relative to top left
    // co-ordinates of viewport
    xpos = x - (int)topleft.getX();
    ypos = y - (int)topleft.getY();
    return new Point (xpos, ypos);
  }
  
  /** Invoked when the mouse has been clicked on a component.
   * @param e the mouse event */
  public void mouseClicked(MouseEvent e) 
  {
    int row;
    Point p;
    TreePath path;
    ExplorerNode node;
    ExplorerPopupMenu popup_menu;

    // find the node that the mouse was over
    row = explorer_tree.getRowForLocation (e.getX(), e.getY());
    if (row == -1) return;
    path = explorer_tree.getPathForLocation(e.getX(), e.getY());
    node = (ExplorerNode) path.getLastPathComponent ();

    // check for a command to run
    if (((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) &&
        (e.getClickCount() == 2))
    {
      if (node.getCommand () != null) GlobalObjects.command_interpreter.interpretCommand(node.getCommand());
    }
    
    // show a popup menu
    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
    {
      popup_menu = new ExplorerPopupMenu (node);
      p = getLocationInScrollPane (e.getX(), e.getY());
      popup_menu.show (this, (int)p.getX(), (int)p.getY());
    }
  }
  
  /** Invoked when a mouse button has been released on a component. 
   * @param e the mouse event */
  public void mouseReleased(MouseEvent e) 
  {
  }
  
  /** Invoked when the mouse enters a component. 
   * @param e the mouse event */
  public void mouseEntered(MouseEvent e) 
  {
  }
  
  /** Invoked when a mouse button has been pressed on a component. 
   * @param e the mouse event */
  public void mousePressed(MouseEvent e) 
  {
  }
  
  /** Invoked when the mouse exits a component. 
   * @param e the mouse event */
  public void mouseExited(MouseEvent e) 
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
  
  public void componentHidden(ComponentEvent componentEvent) {  }
  public void componentMoved(ComponentEvent componentEvent) { }
  public void componentShown(ComponentEvent componentEvent) { }
  /** Invoked when frame is resized
   *
   * If the frame is changed to a size smaller than the minimum size
   * it is resized back to the minimum size.
   *
   * @param e the event description */
  public void componentResized(ComponentEvent componentEvent)
  {
    int width, height;
    boolean resize = false;

    width = this.getWidth();
    height = this.getHeight();
    
    if (width < MIN_WIDTH)
    {
      resize = true;
      width = MIN_WIDTH;
    }
    if (height < MIN_HEIGHT)
    {
      resize = true;
      height = MIN_HEIGHT;
    }
    
    if (resize) this.setSize(width, height);

  }
}
