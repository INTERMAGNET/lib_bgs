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
public class DailyMeanPanel extends JPanel 
{
    
    /** the daily mean panel is implemented as a table - this inner class
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
            return 1;
        }

        /** get the number of columns in the data model
         * @return the number of columns */
        public int getColumnCount()
        {
            return 4;
        }
        
        /** determine whether a cell is editable
         * @param row - the row to return data for
         * @param column - the column to return data for
         * @return true if the cell is editable */
        @Override
        public boolean isCellEditable (int row, int column)
        {
            if (! editable) return false;
            return true;
        }

        /** get the name of a column
         * @param column the column index
         * @return the column name */
        @Override
        public String getColumnName (int column)
        {
            String string;
            
            string = data_file.getDataDay(data_file.getDayDisplayed()).getRecordedElements ();
            if (string.length() <= column) return "Component " + Integer.toString (column +1);
            return string.substring(column, column +1);
        }
        
        /** get the data in the data model
         * @param row - the row to return data for
         * @param column - the column to return data for
         * @return the data item */
        public Object getValueAt(int row, int column)
        {
            ImagCDDataDay data_day;
            
            data_day = data_file.getDataDay (data_file.getDayDisplayed());
            return Integer.toString (data_day.getDayData(column));
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
                data_day.setDayData(value, column);
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
    public DailyMeanPanel(ImagCDFile data_file, boolean editable) 
    {
        JScrollPane scrollpane;
        
        data_model = new DataModel (data_file, editable);
        table = new JTable(data_model);
        table.setToolTipText("Enter field values in tenths of a nT. For a missing value enter 999999");
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        scrollpane = new JScrollPane(table);
        add (scrollpane);
    }
    
    /** Force an update of the table display */
    public void updateTable ()
    {
        int count;
        TableColumn table_column;
        
        for (count=0; count<4; count++)
        {
            table_column = table.getColumnModel().getColumn(table.convertColumnIndexToView(count));
            table_column.setHeaderValue (data_model.getColumnName(count));
        }
        this.repaint ();
    }

}
