/*
 * WorlMapObservatoryStatic.java
 *
 * Created on 15 April 2005, 17:18
 */

package bgs.geophys.ImagCD.ImagCDViewer.Menus;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;

/**
 * Popup menu for observatories on the world map - this menu stays put
 * until the user dismisses it.
 *
 * @author  smf
 */
public class WorldMapObservatoryStatic extends JDialog
{
     
    /** Creates a new instance of WorlMapObservatoryStatic */
    public WorldMapObservatoryStatic(Frame parent, String title, CDObservatoryIterator.ObservatoryInfo observatory_info)
    {
        super (parent, title);
        
        createComponents (observatory_info);
    }

    /** Creates a new instance of WorlMapObservatoryStatic */
    public WorldMapObservatoryStatic(Dialog parent, String title, CDObservatoryIterator.ObservatoryInfo observatory_info)
    {
        super (parent, title);
        
        createComponents (observatory_info);
    }
    
    private void createComponents (CDObservatoryIterator.ObservatoryInfo observatory_info)
    {
        int count, y_pos;
        Vector item_list;
        GridBagLayout grid_layout;
        Container content_pane;
        Component component;
        
        
        content_pane = this.getContentPane();
        grid_layout = new GridBagLayout ();
        content_pane.setLayout(grid_layout);

        // add the menu elements
        item_list = WorldMapObservatoryPopup.listMenuItems (observatory_info, false, "from_main_window");
        y_pos = 0;
        for (count=0; count<item_list.size(); count++)
        {
            component = (Component) item_list.get (count);
            SwingUtils.addToGridBag (component, content_pane, 0, y_pos, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH);
            y_pos += 1;
        }
        
        pack ();
        
        setDefaultCloseOperation (DISPOSE_ON_CLOSE);
    }
     
}
