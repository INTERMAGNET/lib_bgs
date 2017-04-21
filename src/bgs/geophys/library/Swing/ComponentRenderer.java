/*
 * ComponentRenderer.java
 *
 * Created on 18 November 2003, 17:55
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Class to render buttons (and other components) properly in a table. To use
 * this class on all rows in a column:
 *      button_renderer = new ComponentRenderer (table.getDefaultRenderer(Object.class));
 *      table.getColumnModel().getColumn(N).setCellRenderer(button_renderer);
 * For displaying buttons in tables see here:
 *      http://www.fawcette.com/Archives/premier/mgznarch/javapro/1999/jp_jan_99/ap0199/ap0199.asp
 * @author  smf
 */
public class ComponentRenderer implements TableCellRenderer 
{
    private TableCellRenderer default_renderer;

    /** Contruct a new renderer for a JTable.
     * @param renderer the default renderer for the table - find
     * this by doing: table.getDefaultRenderer(Object.class) */
    public ComponentRenderer (TableCellRenderer renderer) 
    {
         default_renderer = renderer;
    }

    /* override getTableCellRendererComponent to draw the contents of a cell
     * @param table the table in which to draw
     * @param value the contents of the cell
     * @param isSelected true if the cell is selected
     * @param hasFocus true if the cell has the focus
     * @param row the cell's table row
     * @param column the cell's table column */
    public Component getTableCellRendererComponent (JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column)
    {
        if (value instanceof Component) return (Component) value;
        return default_renderer.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    }
    
}
