/*
 * HeaderPanel.java
 *
 * Created on 24 October 2003, 11:08
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor;

import bgs.geophys.library.Data.ImagCD.ImagCDDataDay;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import javax.swing.*;
import javax.swing.table.*;

import bgs.geophys.library.Misc.*;

/**
 *
 * @author  Administrator
 */
public class TrailerPanel extends JPanel 
{

    /** the trailer panel is implemented as a table - this inner class
     * is the data model for the table */
    private class DataModel extends AbstractTableModel
    {
        // private members
        private ImagCDFile data_file;
        private boolean editable;
        
        /** constructor for the data model
         * @param data_file the data file that this panel displays */
        public DataModel (ImagCDFile data_file, boolean editable)
        {
            this.data_file = data_file;
            this.editable = editable;
        }
        
        /** get the number of rows in the data model
         * @return the number of rows */
        public int getRowCount()
        {
            return 4;
        }

        /** get the number of columns in the data model
         * @return the number of columns */
        public int getColumnCount()
        {
            return 2;
        }
        
        /** determine whether a cell is editable
         * @param row - the row to return data for
         * @param column - the column to return data for
         * @return true if the cell is editable */
        public boolean isCellEditable (int row, int column)
        {
            if (! editable) return false;
            if (column <= 0) return false;
            return true;
        }

        /** get the name of a column
         * @param column the column index
         * @return the column name */
        public String getColumnName (int column)
        {
            switch (column)
            {
            case 0: return "Trailer Item";
            case 1: return "Hex Dump";
            }
            return "";
        }
        
        /** get the data in the data model
         * @param row - the row to return data for
         * @param column - the column to return data for
         * @return the data item */
        public Object getValueAt(int row, int column)
        {
            ImagCDDataDay data_day;
            
            switch (column)
            {
            case 0:
                return "Reserved word " + Integer.toString (row +1);
            case 1:
                data_day = data_file.getDataDay (data_file.getDayDisplayed());
                return data_day.getTrailerHexDump (row * 4, 4);
            }
            return "";
        }

        /** set the data in the data model
         * @param object the new value to set
         * @param row - the row to set data for
         * @param column - the column to set data for */
        public void setValueAt(Object object, int row, int column)
        {
            int count;
            byte bytes [];
            String string;
            ImagCDDataDay data_day;
            
            string = (String) object;
            bytes = Utils.parseHexDump(string);
            if (bytes == null)
                JOptionPane.showMessageDialog(table, "Value must be four hexadecimal numbers separated by spaces", "Warning", JOptionPane.WARNING_MESSAGE);
            else if (bytes.length != 4)
                JOptionPane.showMessageDialog(table, "Value must be four hexadecimal numbers separated by spaces", "Warning", JOptionPane.WARNING_MESSAGE);
            else
            {
                data_day = data_file.getDataDay (data_file.getDayDisplayed());
                for (count=0; count<4; count ++)
                    data_day.setTrailerByte (bytes [count], (row * 4) + count);
                data_file.setFileChanged();
            }
        }
    }
    
    // private member variables
    private DataModel data_model;   // the data for the table
    private JTable table;           // the table the the data lives in
    
    /** Creates a new instance of TrailerPanel
      * @param data_file the data file that this panel displays */
    public TrailerPanel(ImagCDFile data_file, boolean editable) 
    {
        JScrollPane scrollpane;
        
        data_model = new DataModel (data_file, editable);
        table = new JTable(data_model);
        table.setToolTipText("These bytes are not normally used by INTERMAGNET. To set a value, enter four hexadecimal numbers.");
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        scrollpane = new JScrollPane(table);
        add (scrollpane);
    }

    /** Force an update of the table display */
    public void updateTable ()
    {
        this.repaint ();
    }
    
}
