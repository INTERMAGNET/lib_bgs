/*
 * WorldMap.java
 *
 * Created on 15 May 2003, 14:50
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Menus.*;



/**
 *
 * @author  fmc
 */


public class WorldMap extends JPanel 
implements MouseListener
{
  private Graphics2D g2d;
  private Image map_image;
  
  private static final double MIN_LAT = -90;
  private static final double MAX_LAT = 90;
  private static final double MIN_LONG = 0;
  private static final double MAX_LONG = 360;
  private static final int CIRCLE_DIAMETER = 10;
  private static final int CIRCLE_RADIUS = 5;
    
 /********************************************************************
  * Creates a new instance of WorldMap
  *
  * @param imageicon - the icon image of the map
  ********************************************************************/
  public WorldMap (ImageIcon imageicon)
  {
    this.map_image = imageicon.getImage();
    this.setPreferredSize(new Dimension (imageicon.getIconWidth(), imageicon.getIconHeight()));
    this.addMouseListener (this);
    
    // you need to call this function to turn tool-tips on, even though this is
    // not the text that will be used
    setToolTipText ("World Map");
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    ToolTipManager.sharedInstance().setReshowDelay(0);
  }
  
  /** method that searches for an observatory string and pops
   * up the observatory's popup menu on the map
   * @param obsy any part of the name of the observatory
   * @return true if the observatory was found, false otherwise */
  public boolean showObsyPopup (String obsy)
  {
      String obsyUpper;
      CDObservatoryIterator obsy_iterator;
      CDObservatoryIterator.ObservatoryInfo obsy_info, found_info;

      // get objects that we need
      obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
      obsyUpper = obsy.toUpperCase();
      found_info = null;

      // first try to match the observatory code
      for (obsy_info = obsy_iterator.GetFirstObservatory(); 
           (obsy_info != null) && (found_info == null); 
           obsy_info = obsy_iterator.GetNextObservatory())
      {
          if (obsy_info.GetObservatoryCode().toUpperCase().indexOf(obsyUpper) >= 0) 
              found_info = obsy_info;
      }
    
      // next try to match the observatory name
      for (obsy_info = obsy_iterator.GetFirstObservatory(); 
           (obsy_info != null) && (found_info == null); 
           obsy_info = obsy_iterator.GetNextObservatory())
      {
          if (obsy_info.GetDisplayName().toUpperCase().indexOf(obsyUpper) >= 0) 
              found_info = obsy_info;
      }

      // did we find an observatory ??
      if (found_info != null)
      {
          // yes - get a popup
          showObsyPopup (GlobalObjects.top_frame, null, found_info, found_info.GetDisplayX(), found_info.GetDisplayY(), true, "from_main_window");
          return true;
      }
      
      // the observatory wasn't found
      return false;
  }
  
  // Override the paintComponent method in JPanel in order to draw on the panel
  public void paintComponent(Graphics g)
  {
    int minx, miny, width, height;
    CDObservatoryIterator obsy_iterator;
    CDObservatoryIterator.ObservatoryInfo obsy_info;
    
    // get objects that we need
    obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();
    
    // Call the paintComponent method in JPanel
    super.paintComponent (g);
    g2d = (Graphics2D) g;

    // draw the base map
    width = this.getWidth();
    height = this.getHeight();
    g2d.drawImage (map_image, 0, 0, width, height, null);

    // draw the observatories
    if (obsy_iterator != null)
    {
        for (obsy_info = obsy_iterator.GetFirstObservatory(); obsy_info != null; obsy_info = obsy_iterator.GetNextObservatory())
            plotObservatory (obsy_info, width, height);
    }
  }

  /** plot an observatory's position
   * @param obsy_info the observatory information
   * @param xpix the width of the area to plot on
   * @param ypix the height of the area to plot on */
  private void plotObservatory (CDObservatoryIterator.ObservatoryInfo obsy_info, int xpix, int ypix)
  {
    float xscale, yscale, latitude, longitude;
    int x, y, x_offset, y_offset;

    // check that the observatory can be plotted
    latitude = (float) obsy_info.GetLatitude();
    longitude = (float) obsy_info.GetLongitude();
    if (latitude != CDObservatory.MISSING_DATA && longitude != CDObservatory.MISSING_DATA)
    {
        // scale lats and longs to image size
        xscale = (float) (xpix / (MAX_LONG - MIN_LONG));
        // 0 is at top of image
        yscale = (float) (ypix / (MIN_LAT - MAX_LAT));
    
        // offsets because 0, 0 is at centre of image
        if (longitude < 180.0) x_offset = xpix / 2;
        else x_offset = -xpix / 2;

        y_offset = ypix / 2;

        // calculate x and y of coordinates
        x = (int) (longitude * xscale) + x_offset;
        y = (int) (latitude * yscale) + y_offset;
        obsy_info.SetDisplayPosition (x, y);
    
        g2d.setColor(Color.red);
        g2d.fillOval (x - CIRCLE_RADIUS, y - CIRCLE_RADIUS, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
    }
  }

  /** override of getToolTipText that returns a string describing the observatory
   * under the mouse */
    @Override
  public String getToolTipText (MouseEvent e)
  {
      CDObservatoryIterator.ObservatoryInfo obsy_info;
      
      obsy_info = findObservatory (e.getX(), e.getY());
      if (obsy_info == null) return new String ("");
      return obsy_info.GetDisplayName ();
  }
  
  public void mouseClicked(java.awt.event.MouseEvent mouseEvent) 
  {
      CDObservatoryIterator.ObservatoryInfo obsy_info;
      boolean showPopup;
      
      obsy_info = findObservatory (mouseEvent.getX(), mouseEvent.getY());
      if (obsy_info != null && mouseEvent.getButton() != MouseEvent.NOBUTTON)
      {
          if (mouseEvent.getButton() == MouseEvent.BUTTON1) showPopup = true;
          else showPopup = false;
          showObsyPopup (GlobalObjects.top_frame, null, obsy_info, mouseEvent.getX (), mouseEvent.getY (), showPopup, "from_main_window");
      }
  }
  
  public void mouseEntered(java.awt.event.MouseEvent mouseEvent) 
  {
  }
  
  public void mouseExited(java.awt.event.MouseEvent mouseEvent) 
  {
  }
  
  public void mousePressed(java.awt.event.MouseEvent mouseEvent) 
  {
  }
  
  public void mouseReleased(java.awt.event.MouseEvent mouseEvent) 
  {
  }

  /** find an observatory on the map
   * @param x  the x position on the map
   * @param y  the y position on the map
   * @return the observatory information or NULL if there was no match */
  private CDObservatoryIterator.ObservatoryInfo findObservatory (int x, int y)
  {
      CDObservatoryIterator obsy_iterator;
      CDObservatoryIterator.ObservatoryInfo obsy_info;
      
      // get objects that we need
      obsy_iterator = GlobalObjects.database_list.GetObservatoryIterator();

      // get each observatory
      for (obsy_info = obsy_iterator.GetFirstObservatory(); obsy_info != null; obsy_info = obsy_iterator.GetNextObservatory())
      {
          // test for intersection between the observatory and our position
          if ((x > obsy_info.GetDisplayX() - CIRCLE_RADIUS) &&
              (x < obsy_info.GetDisplayX() + CIRCLE_RADIUS) &&
              (y > obsy_info.GetDisplayY() - CIRCLE_RADIUS) &&
              (y < obsy_info.GetDisplayY() + CIRCLE_RADIUS))
            return obsy_info;
      }
    
      return null;
  }
  
  /** show an observatory popup
   * @param parent_frame the parent frame - may be null
   * @param parent_dialog the parent dialog (alternative to frame - you can't use both!) 
   * @param obsy_info the observatory
   * @param xpos the X position for the popup
   * @param ypos the Y position for the popup
   * @param popupFlag true for a popup, false for a menu
   * @param from_window code for the window that the popup is launched from -
   *        used to add the window to each menu command that launches a dialog
   */
  public static void showObsyPopup (Frame parent_frame, Dialog parent_dialog,
                                    CDObservatoryIterator.ObservatoryInfo obsy_info,
                                    int xpos, int ypos, boolean popupFlag, String from_window)
  {
      double xmax, ymax;
      Dimension d;
      Point p;
      WorldMapObservatoryPopup observatory_popup;
      WorldMapObservatoryStatic observatory_static;
    
      observatory_popup = null;
      observatory_static = null;
      if (popupFlag) observatory_popup = new WorldMapObservatoryPopup (obsy_info.GetDisplayName (), obsy_info, from_window);
      else if (parent_frame != null) observatory_static = new WorldMapObservatoryStatic (parent_frame, obsy_info.GetDisplayName (), obsy_info);
      else observatory_static = new WorldMapObservatoryStatic (parent_dialog, obsy_info.GetDisplayName (), obsy_info);
      
      // get window size and menu position - make height a bit smaller so that menus are not hidden behind toolbar
      xmax = Toolkit.getDefaultToolkit ().getScreenSize ().getWidth ();
      ymax = Toolkit.getDefaultToolkit ().getScreenSize ().getHeight () - 50;

      // ensure location for popup menu is not off side or bottom of screen
      if (observatory_popup != null)
          d = observatory_popup.getPreferredSize ();
      else
      {
          
          d = observatory_static.getPreferredSize();
          if (parent_frame != null)
              p = parent_frame.getLocation();
          else if (parent_dialog != null)
              p = parent_dialog.getLocation();
          else
              p = new Point (0, 0);
          xpos += p.x;
          ypos += p.y;
      }
      if (xpos + d.getWidth () > xmax)
        xpos = xpos - (int) d.getWidth ();
      if (ypos + d.getHeight () > ymax)
        ypos = ypos - (int) d.getHeight ();
          
      // display the menu
      if (observatory_popup != null)
      {
          if (parent_frame != null)
              observatory_popup.show (parent_frame, xpos, ypos);
          else
              observatory_popup.show (parent_dialog, xpos, ypos);
      }
      else
      {
          observatory_static.setLocation(xpos, ypos);
          observatory_static.setVisible(true);
      }
  }
  
}
