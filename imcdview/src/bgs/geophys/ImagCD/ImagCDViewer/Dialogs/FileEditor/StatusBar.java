/*
 * StatusBar.java
 *
 * Created on 20 February 2003, 13:45
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import bgs.geophys.library.Swing.*;
import java.util.Vector;


/**
 *
 * @author  smf
 */
public class StatusBar extends JPanel
implements ActionListener
{
    /** codes for change of displayed day events */
    enum DisplayedDayCommand { FIRST_DAY, PREVIOUS_DAY, NEXT_DAY, LAST_DAY }
    
    /** an event that commands a change of day */
    public class DayChangeEvent
    {
        private DisplayedDayCommand cmd;
        public DayChangeEvent (DisplayedDayCommand cmd) { this.cmd = cmd; }
        public DisplayedDayCommand getCommand () { return cmd; }
    }
    
    /** an interface to listen for day changed events */
    public interface DayChangeListener
    {
        public void changeDayCommand (DayChangeEvent evt);
    }
            
    // private members
    private JTextField status_1, status_2, status_3;
    private JButton first_button, previous_button, next_button, last_button;
    private Vector<DayChangeListener> day_change_listeners;
    
    /** Creates a new instance of StatusBar
     * @param data_file the data file for the initial status values */
    public StatusBar(ImagCDFile data_file) 
    {
        JLabel label_1, label_2, label_3;
        JPanel panel, panel2;
        
        day_change_listeners = new Vector<DayChangeListener> ();
       
        setBorder (BorderFactory.createBevelBorder(BevelBorder.RAISED));
        first_button = new JButton (GlobalObjects.first_icon);
        first_button.setToolTipText("First day of month");
        first_button.addActionListener(this);
        previous_button = new JButton (GlobalObjects.previous_icon);
        previous_button.setToolTipText("Previous day");
        previous_button.addActionListener(this);
        next_button = new JButton (GlobalObjects.next_icon);
        next_button.setToolTipText("Next day");
        next_button.addActionListener(this);
        last_button = new JButton (GlobalObjects.last_icon);
        last_button.setToolTipText("Last day of month");
        last_button.addActionListener(this);
        label_1 = new JLabel ("Day");
        status_1 = new JTextField ("");
        status_1.setColumns (9);
        status_1.setEnabled (false);
        label_2 = new JLabel ("Month / Year");
        status_2 = new JTextField ("");
        status_2.setColumns (9);
        status_2.setEnabled (false);
        label_3 = new JLabel ("File");
        status_3 = new JTextField ("");
        status_3.setColumns (50);
        status_3.setEnabled (false);

        setLayout (new BorderLayout ());
        panel = new JPanel (new FlowLayout (FlowLayout.LEFT));
        panel2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
        panel.add (first_button);
        panel.add (previous_button);
        panel.add (next_button);
        panel.add (last_button);
        panel.add (label_1);
        panel.add (status_1);
        panel.add (label_2);
        panel.add (status_2);
        panel2.add (label_3);
        panel2.add (status_3);
        this.add (panel, "North");
        this.add (panel2, "South");
        updateStatus (data_file);
    }
    
    public void addDayChangeListener (DayChangeListener listener)
    {
        day_change_listeners.add (listener);
    }

    public void removeDayChangeListener (DayChangeListener listener)
    {
        day_change_listeners.remove (listener);
    }

    /** display status information
      * @param data_file the data file for the status */
    public void updateStatus (ImagCDFile data_file)
    {
        File file;
        
        SwingUtils.updateTextField (status_1, Integer.toString (data_file.getDayDisplayed()) + " of " + Integer.toString (data_file.getNDays ()));
        SwingUtils.updateTextField (status_2, Integer.toString (data_file.getMonth () +1) + " / " + Integer.toString (data_file.getYear ()));
        file = data_file.getOpenFile();
        if (file == null) SwingUtils.updateTextField (status_3, "None");
        else SwingUtils.updateTextField (status_3, file.toString ());
    }

    public void actionPerformed(ActionEvent e) 
    {
        int count;
        DayChangeListener listener;
        DayChangeEvent dce;

        if (e.getSource() == first_button)
            dce = new DayChangeEvent (StatusBar.DisplayedDayCommand.FIRST_DAY);
        else if (e.getSource() == last_button)
            dce = new DayChangeEvent (StatusBar.DisplayedDayCommand.LAST_DAY);
        else if (e.getSource() == next_button)
            dce = new DayChangeEvent (StatusBar.DisplayedDayCommand.NEXT_DAY);
        else if (e.getSource() == previous_button)
            dce = new DayChangeEvent (StatusBar.DisplayedDayCommand.PREVIOUS_DAY);
        else
            dce = null;
        
        if (dce != null)
        {
            for (count=0; count<day_change_listeners.size(); count++)
            {
                listener = day_change_listeners.get(count);
                listener.changeDayCommand(dce);
            }
        }
    }
    
}
