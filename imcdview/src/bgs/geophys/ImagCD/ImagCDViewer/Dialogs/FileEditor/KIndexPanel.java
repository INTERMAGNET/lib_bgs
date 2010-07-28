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

/**
 *
 * @author  Administrator
 */
public class KIndexPanel extends JPanel 
{
    
    /** the K index panel is implemented as a table - this inner class
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
            return 8;
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
        @Override
        public boolean isCellEditable (int row, int column)
        {
            if (! editable) return false;
            if (column <= 0) return false;
            return true;
        }

        /** get the name of a column
         * @param column the column index
         * @return the column name */
        @Override
        public String getColumnName (int column)
        {
            int index;
            String string;
            
            switch (column) 
            {
            case 0: return "Hour";
            case 1: return "K index";
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
            
            if (column == 0) return Integer.toString (row * 3);
            data_day = data_file.getDataDay (data_file.getDayDisplayed());
            return Integer.toString (data_day.getKIndexData(row));
        }

        /** set the data in the data model
         * @param object the new value to set
         * @param row - the row to set data for
         * @param column - the column to set data for */
        @Override
        public void setValueAt(Object object, int row, int column)
        {
            int value;
            String string;
            ImagCDDataDay data_day;
            
            string = (String) object;
            try
            {
                value = Integer.parseInt(string);
                data_day = data_file.getDataDay (data_file.getDayDisplayed());
                data_day.setKIndexData(value, row);
                data_file.setFileChanged();
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(table, "Value must be an integer number", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    // private member variables
    private DataModel data_model;   // the data for the table
    private JTable table;           // the table the the data lives in
    
    /** Creates a new instance of HeaderPanel
      * @param data_file the data file that this panel displays */
    public KIndexPanel(ImagCDFile data_file, boolean editable) 
    {
        JScrollPane scrollpane;
        
        data_model = new DataModel (data_file, editable);
        table = new JTable(data_model);
        table.setToolTipText("Enter K index values times ten. For a missing value enter 999");
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
