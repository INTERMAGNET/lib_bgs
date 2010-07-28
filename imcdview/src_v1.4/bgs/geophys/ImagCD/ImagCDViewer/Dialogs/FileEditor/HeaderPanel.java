/*
 * HeaderPanel.java
 *
 * Created on 24 October 2003, 11:08
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.ImagCD.ImagCDDataDay;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import java.text.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import bgs.geophys.library.Misc.*;

/**
 *
 * @author  Administrator
 */
public class HeaderPanel extends JPanel 
{

    /** the header panel is implemented as a table - this inner class
     * is the data model for the table */
    private class DataModel extends AbstractTableModel
    {
        // private members
        private HeaderPanel panel;
        private ImagCDFile data_file;
        private boolean editable;
        private SimpleDateFormat pub_date_format;
        
        /** constructor for the data model
          * data_file the data that this model holds data for */
        public DataModel (ImagCDFile data_file, boolean editable)
        {
            this.data_file = data_file;
            this.editable = editable;
            pub_date_format = new SimpleDateFormat ("yyMM");
            pub_date_format.setTimeZone (DateUtils.gmtTimeZone);
        }
        
        /** get the number of rows in the data model
         * @return the number of rows */
        public int getRowCount()
        {
            return 16;
        }

        /** get the number of columns in the data model
         * @return the number of columns */
        public int getColumnCount()
        {
            return 3;
        }
        
        /** determine whether a cell is editable
         * @param row - the row to return data for
         * @param column - the column to return data for
         * @return true if the cell is editable */
        public boolean isCellEditable (int row, int column)
        {
            if (! editable) return false;
            switch (column)
            {
            case 1: return true;
            case 2: if (row < 14) return true;
            }
            return false;
        }
        
        /** get the name of a column
         * @param column the column index
         * @return the column name */
        public String getColumnName (int column)
        {
            switch (column)
            {
            case 0: return "Header Item";
            case 1: return "Hex Dump";
            case 2: return "Decoded";
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
                switch (row)
                {
                case 0:  return "Station code";
                case 1:  return "Year and day number";
                case 2:  return "Co-latitude (degrees x 1000)";
                case 3:  return "Longitude (degrees x 1000)";
                case 4:  return "Elevation (metres)";
                case 5:  return "Recorded elements";
                case 6:  return "Institute code";
                case 7:  return "D conversion factor";
                case 8:  return "Data quality code";
                case 9:  return "Instrument code";
                case 10: return "Limit for K9";
                case 11: return "Sample period (mS)";
                case 12: return "Sensor orientation";
                case 13: return "Publication date";
                case 14: return "Format version";
                case 15: return "Reserved word";
                }
                break;
            case 1:
                data_day = data_file.getDataDay (data_file.getDayDisplayed());
                return data_day.getHeaderHexDump (row * 4, 4);
            case 2:
                data_day = data_file.getDataDay (data_file.getDayDisplayed());
                switch (row)
                {
                case 0: return data_day.getStationID();
                case 1: return Integer.toString (data_day.getYear()) + ", " + Integer.toString (data_day.getDayNumber());
                case 2: return Integer.toString (data_day.getColatitude());
                case 3: return Integer.toString (data_day.getLongitude());
                case 4: return Integer.toString (data_day.getElevation());
                case 5: return data_day.getRecordedElements();
                case 6: return data_day.getInstituteCode();
                case 7: return Integer.toString (data_day.getDConversion());
                case 8: return data_day.getQualityCode();
                case 9: return data_day.getInstrumentCode();
                case 10: return Integer.toString (data_day.getK9Limit());
                case 11: return Integer.toString (data_day.getSamplePeriod());
                case 12: return data_day.getSensorOrientation();
                case 13: 
                    if (data_day.getPublicationDate() != null)
                        return pub_date_format.format (data_day.getPublicationDate());
                    return "";
                case 14: return data_day.getFormatVersion();
                }
                break;
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
            String string, trimmed_string;
            StringTokenizer tokens;
            ImagCDDataDay data_day;
            
            string = (String) object;
            trimmed_string = string.trim();
            data_day = data_file.getDataDay (data_file.getDayDisplayed());
            switch (column)
            {
            case 1:
                bytes = Utils.parseHexDump(string);
                if (bytes == null)
                    JOptionPane.showMessageDialog(table, "Value must be four hexadecimal numbers separated by spaces", "Warning", JOptionPane.WARNING_MESSAGE);
                else if (bytes.length != 4)
                    JOptionPane.showMessageDialog(table, "Value must be four hexadecimal numbers separated by spaces", "Warning", JOptionPane.WARNING_MESSAGE);
                else
                {
                    for (count=0; count<4; count ++)
                        data_day.setHeaderByte (bytes [count], (row * 4) + count);
                    data_file.setFileChanged();
                }
                break;
            case 2:
                try
                {
                    switch (row)
                    {
                    case 0:
                        data_day.setStationID(string); 
                        break;
                    case 1:
                        tokens = new StringTokenizer (string, ",");
                        data_day.setYear(Integer.parseInt (tokens.nextToken ()));
                        data_day.setDayNumber(Integer.parseInt(tokens.nextToken ()));
                        break;
                    case 2:
                        data_day.setColatitude(Integer.parseInt (string));
                        break;
                    case 3:
                        data_day.setLongitude(Integer.parseInt (string));
                        break;
                    case 4:
                        data_day.setElevation(Integer.parseInt (string));
                        break;
                    case 5:
                        data_day.setRecordedElements(string);
                        break;
                    case 6:
                        data_day.setInstituteCode(string);
                        break;
                    case 7:
                        data_day.setDConversion(Integer.parseInt (string));
                        break;
                    case 8:
                        data_day.setQualityCode(string);
                        break;
                    case 9:
                        data_day.setInstrumentCode(string);
                        break;
                    case 10:
                        data_day.setK9Limit(Integer.parseInt (string));
                        break;
                    case 11:
                        data_day.setSamplePeriod(Integer.parseInt (string));
                        break;
                    case 12:
                        data_day.setSensorOrientation(string);
                        break;
                    case 13:
                        if (trimmed_string.length() <= 0)
                            data_day.setPublicationDate(null);
                        else
                            data_day.setPublicationDate(pub_date_format.parse(trimmed_string));
                        break;
                            
                    }
                    data_file.setFileChanged();
                }
                catch (NumberFormatException e)
                {
                    JOptionPane.showMessageDialog(table, "Value must be a decimal number", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                catch (NoSuchElementException e)
                {
                    JOptionPane.showMessageDialog(table, "Value must be two decimal numbers separated by a comma", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                catch (ParseException e)
                {
                    JOptionPane.showMessageDialog(table, "Value must be a date in the form YYMM where YY is a two digit year and MM a two digit month", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                break;                    
            }
        }
    }

    /** this inner class extends the default cell renderer to allow
     * tool tip text to be associated with individual cells */
    private class TTCellRenderer extends DefaultTableCellRenderer
    {
        /** override the component cell rendering method */
        public Component getTableCellRendererComponent (JTable table, Object value, boolean is_selected,
                                                        boolean is_focused, int row, int column)
        {
            // call the parent method to get the JLabel that renders the cell
            Component component = super.getTableCellRendererComponent(table, value, is_selected,
                                                                      is_focused, row, column);
            JLabel label = (JLabel) component;
            
            // set tool tip text, dependant on row
            switch (row)
            {
            case 0: label.setToolTipText("Enter a space followed by the IAGA station code."); break;
            case 1: label.setToolTipText("Enter the year and day number (1 to 365/6)."); break;
            case 2: label.setToolTipText("Enter the colatitude in degrees times 1000."); break;
            case 3: label.setToolTipText("Enter the longtitude in degrees times 1000."); break;
            case 4: label.setToolTipText("Enter the height in metres."); break;
            case 5: label.setToolTipText("Enter the elements recorded in this data file (HDZF, HDZG, XYZF or XYZG)."); break;
            case 6: label.setToolTipText("Enter a code for the institute responsible for the observatory,"); break;
            case 7: label.setToolTipText("Enter a factor to change 0.1 minutes to 0.1 nT"); break;
            case 8: label.setToolTipText("Enter 'IMAG' to indicate INTERMAGNET data quality."); break;
            case 9: label.setToolTipText("Enter a code for the sensor used to measure the field."); break;
            case 10: label.setToolTipText("Enter the limit used for K9"); break;
            case 11: label.setToolTipText("Enter the period between recorded samples in milli-seconds."); break;
            case 12: label.setToolTipText("Enter the sensor orientation (HDZF, XYZF, DIF)."); break;
            case 13: label.setToolTipText("Enter the publication date in the form YYMM."); break;
            default: label.setToolTipText("These bytes are not normally used by INTERMAGNET. To set a value, enter four hexadecimal numbers."); break;
            }
            
            return component;
        }
    }
    
    // private member variables
    private DataModel data_model;   // the data for the table
    private JTable table;           // the table the the data lives in
    
    /** Creates a new instance of HeaderPanel
      @param data_file the data file that this pane displays */
    public HeaderPanel(ImagCDFile data_file, boolean editable) 
    {
        int width, count;
        JScrollPane scrollpane;
        TTCellRenderer tt_cell_renderer;

        data_model = new DataModel (data_file, editable);
        table = new JTable(data_model);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        tt_cell_renderer = new HeaderPanel.TTCellRenderer ();
        for (count=0; count<table.getColumnCount(); count++)
            table.getColumnModel().getColumn(count).setCellRenderer(tt_cell_renderer);
        
        scrollpane = new JScrollPane(table);
        add (scrollpane);
        
        width = table.getColumnModel().getTotalColumnWidth();
        table.getColumnModel().getColumn(0).setPreferredWidth (width / 2);
        table.getColumnModel().getColumn(1).setPreferredWidth (width / 4);
        table.getColumnModel().getColumn(2).setPreferredWidth (width / 4);

    }

    /** Force an update of the table display */
    public void updateTable ()
    {
        this.repaint ();
    }
    
}
