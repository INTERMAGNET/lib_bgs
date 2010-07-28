/*
 * MainMenu.java
 *
 * Created on 02 June 2002, 16:07
 */

package bgs.geophys.ImagCD.ImagCDViewer.Menus;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.*;
import bgs.geophys.ImagCD.ImagCDViewer.Data.*;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.*;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.*;

/**
 * The main menu for the application window along with its event handler
 * @author  Simon
 * @version 
 */
public class MainMenu extends JMenuBar
{

    // the main menu bar contains a single text entry field that allows
    // the user to input observatory searches
    private JTextField searchTextField;
    
    /** Creates new MainMenu */
    public MainMenu () 
    {
        super ();
    
        JMenu menu, submenu;
        Dimension size;
        
        menu = new JMenu("Database");
        menu.add(SwingUtils.CreateMenuItem("Select Data Sources...", "raise_data_source_dialog&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Reload data", "reload_data_from_source", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Import data", "import_data", GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(SwingUtils.CreateMenuItem("Exit", "program_exit", GlobalObjects.command_interpreter));
        add(menu);
        
        menu = new JMenu("View");
        menu.add(SwingUtils.CreateMenuItem("Explorer Window", "raise_explorer_dialog", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Data Catalogue", "raise_catalogue_dialog&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Export Window", "raise_export_dialog&from_main_window&&true&true", GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(SwingUtils.CreateMenuItem("Errata List", "view_longest_errata_list&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("CD Information", "view_latest_cd_readme&from_main_window", GlobalObjects.command_interpreter));
        add(menu);
        
        menu = new JMenu("Options");
        menu.add(SwingUtils.CreateMenuItem("Plot options", "raise_plot_options_dialog&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Data viewing options", "raise_data_options_dialog&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Export options", "raise_export_options_dialog&from_main_window", GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(SwingUtils.CreateMenuItem("Program options", "raise_program_options_dialog&from_main_window", GlobalObjects.command_interpreter));
        add(menu);
        
        submenu = new JMenu("Diagnostics");
        submenu.add(SwingUtils.CreateMenuItem("Configuration files...", "raise_config_view_dialog&from_main_window", GlobalObjects.command_interpreter));
        submenu.add(SwingUtils.CreateMenuItem("Memory usage...", "raise_memory_view_dialog&from_main_window", GlobalObjects.command_interpreter));
        
        menu = new JMenu("Help");
        menu.add(SwingUtils.CreateMenuItem("Help For New Users", "show_help&from_main_window&NewUser.html", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Contents", "show_help&from_main_window&Contents.html", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Index", "show_help&from_main_window&Index.html", GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(SwingUtils.CreateMenuItem("Report a fault...", "send_fault_report", GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(SwingUtils.CreateMenuItem("Intermagnet Technical Manual", "show_external_web_page&http://www.intermagnet.org/TechnicalSoft_e.html", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Data Formats", "show_help&from_main_window&DataFormats.html", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("Data Export Agreement" ,"raise export agreement dialog&from_main_window",GlobalObjects.command_interpreter));
        menu.addSeparator();
        menu.add(submenu);
        if (GlobalObjects.release_notes_url != null)
            menu.add(SwingUtils.CreateMenuItem("Release notes", "show_release_notes&from_main_window", GlobalObjects.command_interpreter));
        menu.add(SwingUtils.CreateMenuItem("About...", "help_about&from_main_window", GlobalObjects.command_interpreter));
        add(menu);
        
        add(Box.createHorizontalGlue());
        add(new JLabel("Find observatory:"));
        searchTextField = new JTextField (10);
        size = searchTextField.getMaximumSize();
        size.width = 100;
        searchTextField.setMaximumSize (size);
        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchActionPerformed(evt);
            }
        });
        add(searchTextField);
    }

    private void SearchActionPerformed (java.awt.event.ActionEvent evt)
    {
        GlobalObjects.command_interpreter.interpretCommand ("search_for_obsy&" + searchTextField.getText());
    }
    
}
