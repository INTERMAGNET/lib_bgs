/*
 * MainWin.java
 *
 * Created on 03 June 2002, 15:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.ImagCD.ImagCDViewer.Menus.*;

/**
 *
 * @author  Simon
 * @version 
 */
public class MainWin extends JFrame
implements WindowListener, ComponentListener
{

  // private members
  private MainMenu main_menu;     // the main menu
  private WorldMap map_panel;
  private String config_size_key_prefix;
  
  public static final int MIN_WIDTH = 400;
  public static final int MIN_HEIGHT = 250;
  

  /** Creates new MainWin */
  public MainWin() 
  {
    
    super ("Intermagnet Data Viewer");
    
    // create the main window and set its menus
    main_menu = new MainMenu ();
      
    // create the menu for the main frame
    setJMenuBar (main_menu);

    // set up the main window
    setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    addComponentListener(this);
    setIconImage (GlobalObjects.imag_icon.getImage ());
    
    // add panel containing world map
    ImageIcon map_image = GlobalObjects.world_map;
    map_panel = new WorldMap (map_image);
    this.getContentPane().add (map_panel);
    
    this.pack ();
    
    // don't make the frame visible with 'setVisible (true);' - as other
    // dialogs may need to display before this one - see GlobalObjects
    // static initialiser for details.
  }

  /** Invoked when the window is set to be the user's active window, which means the window (or one of its subcomponents) will receive keyboard events. 
   * @param e the event description */
  public void windowActivated(WindowEvent e) 
  {
  }
  
  /** Invoked when a window has been closed as the result of calling dispose on the window. 
   * @param e the event description */
  public void windowClosed(WindowEvent e) 
  {
    GlobalObjects.command_interpreter.interpretCommand ("program_exit");
  }

  /** Invoked when the user attempts to close the window from the window's system menu. 
    * @param e the event description */
  public void windowClosing(WindowEvent e) 
  {
    GlobalObjects.command_interpreter.interpretCommand ("program_exit");
  }

  /** Invoked when a window is no longer the user's active window, which means that keyboard events will no longer be delivered to the window or its subcomponents. 
    * @param e the event description */
  public void windowDeactivated(WindowEvent e) 
  {
  }

  /** Invoked when a window is changed from a minimized to a normal state.
   * @param e the event description */
  public void windowDeiconified(WindowEvent e) 
  {
  }

  /** Invoked when a window is changed from a normal to a minimized state. 
   * @param e the event description */
  public void windowIconified(WindowEvent e) 
  {
  }

  /** Invoked the first time a window is made visible. 
   * @param e the event description */
  public void windowOpened(WindowEvent e) 
  {
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
  
  public void redrawMap ()
  {
      map_panel.repaint ();
  }
  
  /** forward observatory searches to the map panel
   * @param obsy any part of the name of the observatory
   * @return true if the observatory was found, false otherwise */
  public boolean showObsyPopup (String obsy)
  {
      return map_panel.showObsyPopup(obsy);
  }
  
}