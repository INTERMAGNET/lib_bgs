/*
 * AnnualMeanTablePanel.java
 *
 * Created on 20 February 2009, 08:22
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer;

import bgs.geophys.library.Data.ImagCD.YearMeanFile;
import bgs.geophys.library.Data.ImagCD.YearMean;
import bgs.geophys.library.Swing.ExcelAdapter;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  smf
 */
public class AnnualMeanTablePanel extends AnnualMeanViewerPanel
{
    
    private ExcelAdapter excel_adapter;
    private DataTableModel table_model;
    
    /** Table model for data display DataTable.
     * The DataTable is initially created with default (empty) DataTable
     * model, then dimensions and data are added using update method. */
    public class DataTableModel extends DefaultTableModel
    {
        private DecimalFormat year_format;
        private DecimalFormat field_format;
        private DecimalFormat angle_format;
    
        public DataTableModel() 
        {
            super();
            
            String values [];
            
            year_format = new DecimalFormat ("0000.000");
            
            values = new String [9];
            values [0] = "Year";
            values [1] = "D (min)";
            values [2] = "I (min)";
            values [3] = "H (nT)";
            values [4] = "X (nT)";
            values [5] = "Y (nT)";
            values [6] = "Z (nT)";
            values [7] = "F (nT)";
            values [8] = "Type";
            setColumnIdentifiers(values);
        }
    
        /** update - update the DataTable with new data
         * @param options the display options */
        public void update (YearMeanFile year_mean_file, AnnualMeanOptions options) 
        {
            int n_cols, n_rows, row_count, count, col_count, trace_count;
            boolean use_mean;
            String values [];
            YearMean mean;
            AnnualMeanOptions.AnnualMeanTraceOptions trace_options;
        
            // get number of rows and columns
            n_cols = 3 + options.getNComponentsShown();
            switch (options.getDisplayType())
            {
                case ALL_DAYS: 
                    n_rows = year_mean_file.getNMeans(YearMean.YearMeanType.ALL_DAYS);
                    break;
                case DISTURBED_DAYS: 
                    n_rows = year_mean_file.getNMeans(YearMean.YearMeanType.DISTURBED_DAYS);
                    break;
                case QUIET_DAYS: 
                    n_rows = year_mean_file.getNMeans(YearMean.YearMeanType.QUIET_DAYS);
                    break;
                default: 
                    n_rows = year_mean_file.getNMeans(YearMean.YearMeanType.ALL_DAYS) +
                             year_mean_file.getNMeans(YearMean.YearMeanType.INCOMPLETE);
                    break;
            }
            if (options.isShowJumps())
                n_rows += year_mean_file.getNMeans(YearMean.YearMeanType.JUMP);
        

            // set column titles and create / destroy cells
            values = new String [n_cols];
            col_count = 0;
            values[col_count ++] = "Year";
            for (trace_count=0; trace_count<options.getNComponentsShown(); trace_count++)
            {
                trace_options = options.getComponentOptions(trace_count);
                values [col_count ++] = trace_options.getTraceName(true, false) + " (" + trace_options.getTraceUnits(false) + ")";
            }
            values [col_count ++] = "Type";
            values [col_count ++] = "Note";
            setColumnIdentifiers(values);
            setRowCount(n_rows);
            
            // insert data
            row_count = 0;
            for (count=0; count<year_mean_file.getNMeans(); count++)
            {
                mean = year_mean_file.getMean(count);
                use_mean = false;
                if (mean.getType() == YearMean.YearMeanType.JUMP) 
                {
                    if (options.isShowJumps()) use_mean = true;
                }
                switch (options.getDisplayType())
                {
                    case ALL_DAYS:
                        if (mean.getType() == YearMean.YearMeanType.ALL_DAYS) use_mean = true;
                        break;
                    case DISTURBED_DAYS:
                        if (mean.getType() == YearMean.YearMeanType.DISTURBED_DAYS) use_mean = true;
                        break;
                    case QUIET_DAYS:
                        if (mean.getType() == YearMean.YearMeanType.QUIET_DAYS) use_mean = true;
                        break;
                    default:
                        if (mean.getType() == YearMean.YearMeanType.ALL_DAYS) use_mean = true;
                        if (mean.getType() == YearMean.YearMeanType.INCOMPLETE) use_mean = true;
                        break;
                }
                
                if (use_mean)
                {
                    col_count = 0;
                    setValueAt (year_format.format (mean.getYear()), row_count, col_count ++);
                    for (trace_count=0; trace_count<options.getNComponentsShown(); trace_count++)
                    {
                        trace_options = options.getComponentOptions(trace_count);
                        setValueAt (trace_options.getFormatter().format (mean.getComponent(trace_options.getComponentCode())), row_count, col_count ++);
                    }
                    setValueAt (mean.getTypeName(), row_count, col_count ++);
                    setValueAt (mean.getNoteNumber(), row_count, col_count ++);
                    row_count ++;
                }
            }

            // update DataTable display
            fireTableStructureChanged();
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
    
    
    
    
    /** Creates new form AnnualMeanTablePanel */
    public AnnualMeanTablePanel() 
    {
        initComponents();
        
        // set up DataTable for data display - allow the DataTable to process Excel clipboard data
        table_model = new DataTableModel ();
        DataTable.setModel(table_model);
        excel_adapter = new ExcelAdapter (this, DataTable, true, false, false, true, true);
    }
    
    public ExcelAdapter getExcelAdapter () { return excel_adapter; }

    /********************************************************************
     * update - update the display with new data and dimensions
     *
     * @param obsy_name observatory name
     * @param year the year the annual mean data file was published
     * @param year_mean_file the data to display
     ********************************************************************/
    public void update (String obsy_name, int year, YearMeanFile year_mean_file,
                        AnnualMeanOptions options)
    {
        int count;
        
        makeTitle ("Annual Mean Data Viewer", options, obsy_name, year);
        TitleLabel.setText("<html><h2>" + getTitle() + "</h2></html>");
        SubTitleLabel.setText("<html><h3>" + getSubTitle() + "</h3></html>");
        table_model.update (year_mean_file, options);
        
        NotesTextArea.setText("");
        for (count=0; count<year_mean_file.getNNotes(); count++)
        {
            if (count > 0) NotesTextArea.append ("\n");
            NotesTextArea.append (year_mean_file.getNote(count));            
        }
        NotesTextArea.setCaretPosition(0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TitlePanel = new javax.swing.JPanel();
        TitleLabel = new javax.swing.JLabel();
        SubTitleLabel = new javax.swing.JLabel();
        SplitPane = new javax.swing.JSplitPane();
        DataScrollPane = new javax.swing.JScrollPane();
        DataTable = new javax.swing.JTable();
        NotesPanel = new javax.swing.JPanel();
        NotesLabel = new javax.swing.JLabel();
        NotesScrollPane = new javax.swing.JScrollPane();
        NotesTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        TitlePanel.setLayout(new java.awt.BorderLayout());

        TitleLabel.setText("<html><h2>Title</h2></html>");
        TitlePanel.add(TitleLabel, java.awt.BorderLayout.NORTH);

        SubTitleLabel.setText("<html><h3>SubTitle</h3></html>");
        TitlePanel.add(SubTitleLabel, java.awt.BorderLayout.SOUTH);

        add(TitlePanel, java.awt.BorderLayout.NORTH);

        SplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        SplitPane.setResizeWeight(0.85);

        DataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Year", "D", "I", "H", "X", "Y", "Z", "F", "Type"
            }
        ));
        DataTable.setColumnSelectionAllowed(true);
        DataScrollPane.setViewportView(DataTable);

        SplitPane.setLeftComponent(DataScrollPane);

        NotesPanel.setLayout(new java.awt.BorderLayout());

        NotesLabel.setText("Notes:");
        NotesPanel.add(NotesLabel, java.awt.BorderLayout.NORTH);

        NotesTextArea.setColumns(20);
        NotesTextArea.setEditable(false);
        NotesTextArea.setRows(3);
        NotesScrollPane.setViewportView(NotesTextArea);

        NotesPanel.add(NotesScrollPane, java.awt.BorderLayout.CENTER);

        SplitPane.setRightComponent(NotesPanel);

        add(SplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane DataScrollPane;
    private javax.swing.JTable DataTable;
    private javax.swing.JLabel NotesLabel;
    private javax.swing.JPanel NotesPanel;
    private javax.swing.JScrollPane NotesScrollPane;
    private javax.swing.JTextArea NotesTextArea;
    private javax.swing.JSplitPane SplitPane;
    private javax.swing.JLabel SubTitleLabel;
    private javax.swing.JLabel TitleLabel;
    private javax.swing.JPanel TitlePanel;
    // End of variables declaration//GEN-END:variables
    
}
