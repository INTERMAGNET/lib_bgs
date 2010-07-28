/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Data.DataCache;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.DisplayData;
import bgs.geophys.library.Data.GeomagAbsoluteStats;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Swing.ExcelAdapter;
import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author smf
 */
public class DataTablePanel extends DataViewerPanel
{

    private JTable table;
    private ExcelAdapter excel_adapter;
    private DataTableModel table_model;
    private JScrollPane table_pane;
    private JLabel title_label;

    /** Table model for data display table.
     * The table is initially created with default (empty) table
     * model, then dimensions and data are added using update method. */
    public class DataTableModel extends DefaultTableModel
    {
        private SimpleDateFormat date_format;
    
        public DataTableModel() 
        {
            super();
            date_format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
            date_format.setTimeZone(DateUtils.gmtTimeZone);
        }
    
        /** update - update the table with new data
         * @param data_list - a list of the traces to display
         * @param time_period - one of the codes from DataCache */
        public void update (DisplayData data_list, int time_period) 
        {
            int i, n, n_rows, code;
            long sample_period;
            Date date = new Date(); //instantiate date jex 8.3.2010
            String index_col [], values [];
            DecimalFormat formatter;
            GeomagAbsoluteValue data [];
            GeomagAbsoluteStats stats;
        
            // remove old data columns
            setColumnCount(0);
            setRowCount(0);
        
            // build index column
            n_rows = data_list.getNRows();
            index_col = new String [n_rows +4];
            index_col [0] = "<html><b>Average</b></html>";
            index_col [1] = "<html><b>Minimum</b></html>";
            index_col [2] = "<html><b>Maximum</b></html>";
            index_col [3] = "<html><b>Mid-point</b></html>";
            switch (time_period) 
            {
            case DataCache.MINUTE_MEANS: sample_period = 60000l; break;
            case DataCache.HOURLY_MEANS: sample_period = 3600000l; break;
            case DataCache.DAILY_MEANS:  sample_period = 86400000l; break;
            default: sample_period = 0l; break;
            }
            //set date to the same time as start date from data_list,
            //not to the pointer as previously which caused the start date to
            //increment with each visit to DataTablePanel jex 8.3.2010
            for (n = 0, date.setTime(data_list.getStartDate().getTime());
                 n < n_rows; 
                 n ++, date.setTime (date.getTime() + sample_period))
                index_col [n +4] = date_format.format(date);
        
            // add index column
            addColumn("Date / Time", index_col);
        
            // add data columns
            data = data_list.getData();
            stats = data_list.getStats();
            for (n = 0; n < data_list.getNTraces(); n++) 
            {
                code = data_list.getTraceCompCode(n);
                formatter = data_list.getFormatter(n);
                values = new String [n_rows +4];
                values [0] = "<html><b>" + formatter.format (stats.getAverage().getComponent(code, GeomagAbsoluteValue.ANGLE_MINUTES)) + "</b></html>";
                values [1] = "<html><b>" + formatter.format (stats.getMinimum().getComponent(code, GeomagAbsoluteValue.ANGLE_MINUTES)) + "</b></html>";
                values [2] = "<html><b>" + formatter.format (stats.getMaximum().getComponent(code, GeomagAbsoluteValue.ANGLE_MINUTES)) + "</b></html>";
                values [3] = "<html><b>" + formatter.format (stats.getMidPoint().getComponent(code, GeomagAbsoluteValue.ANGLE_MINUTES)) + "</b></html>";
                for (i = 0; i < n_rows; i++)
                    values [i +4] = formatter.format (data [i].getComponent (code, GeomagAbsoluteValue.ANGLE_MINUTES));
                addColumn(data_list.getTraceCompName(n, false) + " (" + data_list.getUnits(n, false) + ")", values);
            }
        
            // update table display
            fireTableDataChanged();
        }
    
        /** isCellEditable - override this method to make all cells non-
         *                  editable
         * @param row, column - the row and column of the cell
         * @return - false for all cells */
        @Override
        public boolean isCellEditable(int row, int col) 
        {
            return false;
        }
    }

    
    
    public DataTablePanel ()
    {
        this.setLayout(new BorderLayout ());
        
        // set up table for data display - allow the table to process Excel clipboard data
        table_model = new DataTableModel ();
        table = new JTable (table_model);
        excel_adapter = new ExcelAdapter (this, table, true, false, false, true, true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        
        table_pane = new JScrollPane(table);
        table_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        table_pane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        title_label = new JLabel ();
        
        this.add(title_label, "North");
        this.add(table_pane, "Center");
    }
    
    public ExcelAdapter getExcelAdapter () { return excel_adapter; }

    /********************************************************************
     * update - update the display with new data and dimensions
     *
     * @param obsy_name observatory name
     * @param compare_obsy_name comparison observatory name (may be null)
     * @param year displayed date
     * @param month displayed date
     * @param day displayed date
     * @param data_list - the data to display
     * @param time_period - one of the codes from DataCache
     ********************************************************************/
    public void update (String obsy_name, String compare_obsy_name,
                        int year, int month, int day,
                        DisplayData data_list, int time_period)
    {
        makeTitle ("Data Viewer: ", obsy_name, compare_obsy_name, false, year, month, day, time_period);
        title_label.setText("<html><h2>" + getTitle() + "</html>");
        table_model.update (data_list, time_period);
    }
}
