/*
 * TableButtonMouseListener.java
 *
 * Created on 18 November 2003, 18:14
 */

package bgs.geophys.library.Swing;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/** 
 * Class to forward events to a button inside a table cell. To use this class:
 *              table.addMouseListener (new TableButtonMouseListener(systems_table));
 * Buttons should then generate ActionEvents in the normal way.
 * For displaying buttons in tables see here:
 *      http://www.fawcette.com/Archives/premier/mgznarch/javapro/1999/jp_jan_99/ap0199/ap0199.asp
 *
 * @author  smf
 */
public class TableButtonMouseListener implements MouseListener 
{
    private JTable table;

    /** Construct a new table mouse listener. Apply this to a table
     * as follows: table.addMouseListener (new TableButtonMouseListener(table));
     * @param table the table on which events will be redirected */
    public TableButtonMouseListener(JTable table) 
    {
        this.table = table;
    }

    /** called when the mouse is clicked
     * @param e a description of the event */
    public void mouseClicked(MouseEvent e) 
    {
        forwardEventToButton(e); 
    }
    
    /** called when the mouse enters a region button
     * @param e a description of the event */
    public void mouseEntered(MouseEvent e) 
    { 
        forwardEventToButton(e); 
    }
    
    /** called when the mouse exits a region button
     * @param e a description of the event */
    public void mouseExited(MouseEvent e) 
    {
        forwardEventToButton(e); 
    
    }

    /** called when the mouse button is pressed
     * @param e a description of the event */
    public void mousePressed(MouseEvent e) 
    {
        forwardEventToButton(e); 
    
    }
    
    /** called when the mouse button is released
     * @param e a description of the event */
    public void mouseReleased(MouseEvent e) 
    {
        forwardEventToButton(e);  
    }

    /** internal methd to forward events */
    private void forwardEventToButton(MouseEvent e) 
    {
        int count;
        Object value;
        JButton button;
        MouseEvent button_event;
        ActionEvent action_event;
        ActionListener action_listeners [];

        TableColumnModel columnModel = table.getColumnModel();
        int column = columnModel.getColumnIndexAtX(e.getX());
        int row    = e.getY() / table.getRowHeight();

        if (row >= table.getRowCount() || row < 0 ||
            column >= table.getColumnCount() || column < 0) return;

        value = table.getValueAt(row, column);
        if(!(value instanceof JButton)) return;

        button = (JButton)value;
        button_event = (MouseEvent)SwingUtilities.convertMouseEvent (table, e, button);
        button.dispatchEvent(button_event);
        
        // the button doesn't generate an action event by itself, so do it here
        if (button_event.getID() == MouseEvent.MOUSE_RELEASED &&
            button_event.getClickCount() == 1 &&
            button_event.getButton() == MouseEvent.BUTTON1)
        {
            action_event = new ActionEvent (button_event.getSource(), button_event.getID(), button.getActionCommand());
            action_listeners = button.getActionListeners();
            for (count=0; count<action_listeners.length; count++)
                action_listeners[count].actionPerformed (action_event);
        }

        // This is necessary so that when a button is pressed 
        // and released it gets rendered properly.  Otherwise, 
        // the button may still appear pressed down when it has 
        // been released.
        table.repaint();
    }

}
    
