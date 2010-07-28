/*
 * WorldMapObservatoryPopup.java
 *
 * Created on 21 May 2003, 12:25
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
 * Popup menu for observatories on the world map
 * @author  smf
 */
public class WorldMapObservatoryPopup extends JPopupMenu 
{
    /** Creates a new instance of WOrldMapObservatoryPopup */
    public WorldMapObservatoryPopup (String title, CDObservatoryIterator.ObservatoryInfo observatory_info, String from_window)
    {
        int count;
        Vector<Component> item_list;

        // add the title - use a label rather than a menu title (which does
        // not display for all look and feels
        add (new JLabel (title));
        add (new JSeparator ());

        // add the menu elements
        item_list = listMenuItems (observatory_info, true, from_window);
        for (count=0; count<item_list.size(); count++) add ((Component) item_list.get (count));
    }
    
    /** create the list of items for an observatory popup menu
     * @param observatory_info details on the observatory
     * @param use_menu if TRUE create menu items, otherwise create buttons
     * @return a vector array of items that can be put ina container */
    public static Vector<Component> listMenuItems (CDObservatoryIterator.ObservatoryInfo observatory_info, boolean use_menu, String from_window)
    {
        int year, n;
        CDDataMonth month_data [];
        String obsy_code;
        Vector year_list, file_year_list;
        boolean found_data = false, need_sep;
        Vector<Component> item_list;
        
        // set default from window
        if (from_window == null) from_window = "from_main_window";
        else if (from_window.length() <= 0) from_window = "from_main_window";
        
        item_list = new Vector<Component> ();
        obsy_code = observatory_info.GetObservatoryCode ();
        need_sep = false;

        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_COUNTRY_README);
        if (file_year_list.size() > 0)
        {
            need_sep = true;
            item_list.add (createItem ("Country Information",
                                       "text_file_selector&" + from_window + "&" +
                                       CDObservatory.FILE_COUNTRY_README +
                                       "&" + obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }

        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_COUNTRY_MAP);
        if (file_year_list.size() > 0)
        {
            need_sep = true;
            item_list.add (createItem ("Country Map",
                                       "image_file_selector&" + from_window + "&" +
                                       CDObservatory.FILE_COUNTRY_MAP +
                                       "&" + obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        
        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_COUNTRY_INFO);
        if (file_year_list.size() > 0)
        {
            need_sep = true;
            item_list.add (createItem ("Institute Information",
                                       "image_file_selector&" + from_window + "&" +
                                       CDObservatory.FILE_COUNTRY_INFO +
                                       "&" + obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        
        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_README);
        if (file_year_list.size() > 0)
        {
            need_sep = true;
            item_list.add (createItem ("Observatory Information",
                                       "text_file_selector&" + from_window + "&" +
                                       CDObservatory.FILE_README +
                                       "&" + obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        
        // look for any data files - go through each year starting with the most recent
        // to discover if any data is available
        year_list = CDMisc.makeYearList (obsy_code);
        year = 0;
        for (n = year_list.size () - 1; n >= 0; n--)
        {
            year = ((Integer)year_list.elementAt (n)).intValue ();
            month_data = CDMisc.findData (obsy_code, year, -2, true, true, null);
            if (month_data [0] != null)
            {
                found_data = true;
                break;
            }
        }

        // add items that allow the user to view data
        if (found_data)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = true;
            item_list.add (createItem ("Export Data", "raise_export_dialog&from_main_window&" + obsy_code + "&true&true",
                                       GlobalObjects.command_interpreter, use_menu));
        }
        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_YEAR_MEAN);
        if (file_year_list.size() > 0)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = false;
            item_list.add (createItem ("View Annual Means",
                                       "annual_mean_viewer&" +
                                       obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_BLV_TEXT);
        if (file_year_list.size() > 0)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = false;
            item_list.add (createItem ("View Baselines",
                                       "baseline_viewer&" +
                                       obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        if (found_data)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = false;
            item_list.add (createItem ("View Data",
                                       "view_data&" + obsy_code + "&" + year + "&0&true&true",
                                       GlobalObjects.command_interpreter, use_menu));
        }
        
        // special code for K-indices, where the plots and text come from different sources
        file_year_list = CDMisc.makeYearList (obsy_code, CDObservatory.FILE_DKA);
        if (file_year_list.size() > 0)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = false;
            item_list.add (createItem ("View K Index (Text File)",
                                       "text_file_selector&" + from_window + "&" +
                                       CDObservatory.FILE_DKA +
                                       "&" + obsy_code + "&" + file_year_list.get (file_year_list.size() -1).toString(),
                                       GlobalObjects.command_interpreter, use_menu));
        }
        if (found_data)
        {
            if (need_sep) item_list.add (new JSeparator ());
            need_sep = false;
            item_list.add (createItem ("View K Index (Plot)",
                                       "k_index_viewer&" + from_window + "&" +
                                       obsy_code + "&" + year + "&true&true",
                                       GlobalObjects.command_interpreter, use_menu));
        }
        
        return item_list;
    }

    private static Component createItem (String name, String command, ActionListener listener, boolean use_menu)
    {
        JButton button;
        
        if (use_menu)
            return SwingUtils.CreateMenuItem (name, command, listener);
        button = SwingUtils.createButton (name, command, listener);
        button.setBorder(new EmptyBorder (2, 17, 2, 17));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setDefaultCapable(false);
        return button;
    }
    
}
