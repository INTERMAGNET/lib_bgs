/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * A combo box that includes left and right arrows to cycle through
 * the values like a spinner
 * 
 * @author smf
 */
public class JComboSpinner extends JPanel
implements ActionListener
{
    private JComboBox combo_box;
    private JButton previous_button;
    private JButton next_button;
    
    public JComboSpinner ()
    {
        combo_box = new JComboBox ();
        initComponents ();
    }
    
    public JComboSpinner (ComboBoxModel model)
    {
        combo_box = new JComboBox (model);
        initComponents ();
    }
    
    public JComboSpinner (Object items[])
    {
        combo_box = new JComboBox (items);
        initComponents ();
    }
    
    public JComboSpinner (Vector items)
    {
        combo_box = new JComboBox (items);
        initComponents ();
    }
    
    public JComboSpinner (JComboBox combo_box)
    {
        this.combo_box = combo_box;
        initComponents ();
    }
    
    public JComboBox getComboBox () { return combo_box; }
    
    private void initComponents ()
    {
        Color colour;
        Dimension d;
        
        this.setLayout(new FlowLayout (FlowLayout.CENTER, 0, 0));
        this.add (combo_box);
        previous_button = new JButton (" < ");
        next_button = new JButton (" > ");
        
        colour = previous_button.getForeground();
        previous_button.setBorder (new LineBorder (colour, 1, false));
        previous_button.addActionListener(this);
        d = new Dimension (previous_button.getPreferredSize().width, combo_box.getPreferredSize().height);
        previous_button.setPreferredSize (d);
        this.add (previous_button);
        
        colour = next_button.getForeground();
        next_button.setBorder (new LineBorder (colour, 1, false));
        next_button.addActionListener(this);
        d = new Dimension (next_button.getPreferredSize().width, combo_box.getPreferredSize().height);
        next_button.setPreferredSize (d);
        this.add (next_button);
    }
    
    /** change the selected item in the combo box
     * @param for relative zooming, amount to increment (+ve) or decrement (-ve) the selection by
     *        for absolute zooming, amount = index of item in list select
     * @param relative true for relative zooming, false for absolute */
    public void incrementSelection (int amount, boolean relative)
    {
        int index;

        if (relative)
        {
            index = combo_box.getSelectedIndex();
            if (index >= 0)
                index += amount;
        }
        else index = amount;

        if (index >= 0 && index < combo_box.getItemCount())
            combo_box.setSelectedIndex(index);
    }

    public void actionPerformed(ActionEvent e) 
    {
        int current_index;
        
        current_index = combo_box.getSelectedIndex();
        if (current_index >= 0)
        {
            if (e.getSource().equals(previous_button))
                current_index --;
            else if (e.getSource().equals(next_button))
                current_index ++;
            if (current_index < 0) current_index = 0;
            if (current_index >= combo_box.getItemCount()) current_index = combo_box.getItemCount() -1;
            combo_box.setSelectedIndex(current_index);
        }
    }
    
}
