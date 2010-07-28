/*
 * CatalogueDialog.java
 *
 * Created on 14 March 2007, 18:23
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataMonth;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabaseList;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDException;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatoryIterator;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Menus.WorldMapObservatoryPopup;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.library.Misc.DateUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author  smf
 */
public class CatalogueDialog extends javax.swing.JDialog 
{

    // a table model that prevents editing
    private class AvailabilityTableModel extends DefaultTableModel
    {
        public AvailabilityTableModel (Object data[][], Object titles[]) { super (data, titles); }
        public boolean isCellEditable(int rowIndex, int vColIndex) { return false; }
    }
    
    // a table renderer that sorts out tool tips
    private class AvailabilityCellRenderer extends JLabel 
    implements TableCellRenderer 
    {
        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent (JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, 
                                                        int rowIndex, int vColIndex) 
        {
            int count;
            String text, tooltip, string, avail_attrib, missing_attrib;
            
            if (isSelected) 
            {
                avail_attrib =   "<b bgcolor=\"#008000\">&nbsp</b>";
                missing_attrib = "<b bgcolor=\"#800000\">&nbsp</b>";
            }
            else
            {
                avail_attrib =   "<b bgcolor=\"#00FF00\">&nbsp</b>";
                missing_attrib = "<b bgcolor=\"#FF0000\">&nbsp</b>";
            }
    
            string = value.toString();
            text = "<html>";
            tooltip = "";
            for (count=0; count<string.length(); count++)
            {
                if (string.charAt(count) == '-')
                    text += missing_attrib;
                else
                {
                    text += avail_attrib;
                    if (tooltip.length() <= 0) tooltip = "Data available for:";
                    tooltip += " " + DateUtils.getMonthName (count, DateUtils.MONTH_UPPERFIRSTONLY, -1);
                }
            }
            if (tooltip.length() <= 0) tooltip = "No data available";
            text += "</html>";

            setText (text);
            setToolTipText(tooltip);
    
            // Since the renderer is a component, return itself
            return this;
        }
    }


    
    private AvailabilityTableModel table_model;
        
    /** Creates new form CatalogueDialog */
    public CatalogueDialog(java.awt.Frame parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        initComponents2();
    }
    
    /** Creates new form CatalogueDialog */
    public CatalogueDialog(java.awt.Dialog parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        initComponents2();
    }
    
    private void initComponents2 ()
    {

        int database_count, min_year, max_year, year_count, n_years, obsy_count, count;
        int margin, nColumns, nRows, col, modelCol, row_count, width, w, col_count;
        int total_width;
        boolean scan_in_progress;
        CDDatabaseList database_list;
        CDDatabase database;
        CDObservatoryIterator obsy_iterator;
        CDObservatoryIterator.ObservatoryInfo obsy_info;
        CDDataMonth data_month [];
        String title_list [], availability [] [], avail_code;
        TableColumnModel column_model;
        TableColumn column;
        TableCellRenderer renderer;
        Dimension size;
        AvailabilityCellRenderer cell_renderer;
        
        // get the database list
        database_list = GlobalObjects.database_list;
        
        
        // get the max/min year and a list of observatories
        min_year = 99999;
        max_year = 0;
        scan_in_progress = false;
        try
        {
            for (database_count=0; database_count<database_list.GetNDatabases (); database_count++)
            {
                database = database_list.GetDatabase(database_count);
                if (database.GetEarliestYear() < min_year)
                    min_year = database.GetEarliestYear();
                if (database.GetLatestYear() > max_year)
                    max_year = database.GetLatestYear();
            }
        }
        catch (CDException e)
        {
            scan_in_progress = true;
        }
        obsy_iterator = new CDObservatoryIterator (database_list);
        obsy_iterator.Sort();

        // create an array of years - these will be the column names
        if (scan_in_progress)
        {
            title_list = new String [1];
            title_list [0] = "Scan in progress";
            n_years = 0;
        }
        else if (max_year < min_year)
        {
            title_list = new String [1];
            title_list [0] = "No data";
            n_years = 0;
        }
        else
        {
            n_years = max_year - min_year +1;
            if (n_years == 1)
                setTitle ("Data Catalogue for " + Integer.toString (min_year));
            else
                setTitle ("Data Catalogue for " + Integer.toString (min_year) + " to " + Integer.toString (max_year));

            title_list = new String [n_years +1];
            title_list [0] = "Observatory";
            for (year_count=0; year_count<n_years; year_count++)
                title_list [year_count +1] = Integer.toString (min_year + year_count);
        }
            
        // create a 2d array of data availability codes
        availability = new String [obsy_iterator.GetNObservatories()] [n_years +1];
        for (obsy_count = 0, obsy_info=obsy_iterator.GetFirstObservatory(); 
             obsy_count < obsy_iterator.GetNObservatories(); 
             obsy_count++, obsy_info=obsy_iterator.GetNextObservatory())
        {
            availability [obsy_count] [0] = obsy_info.GetObservatoryCode();
            for (year_count=0; year_count<n_years; year_count++)
            {
                data_month = CDMisc.findData (obsy_info.GetObservatoryCode(), 
                                              min_year + year_count,
                                              -1, true, true, null);
                avail_code = "";
                for (count=0; count<data_month.length; count++)
                {
                    if (data_month[count] == null)
                        avail_code += "-";
                    else
                        avail_code += "+";
                }
                availability [obsy_count] [year_count +1] = avail_code;
            }
        }
        
        // set some table properties
        AvailabilityTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
        AvailabilityTable.setShowHorizontalLines(false);
        // I think the next line is needed because of a Netbeans bug which sets
        // the scrollbar to none
        AvailabilityScrollPane.setHorizontalScrollBar(new JScrollBar (JScrollBar.HORIZONTAL));
        
        // fill the table with data
        table_model = new AvailabilityTableModel (availability, title_list);
        AvailabilityTable.setModel(table_model);
        
        // set up special renderer on all columns except the first (which holds
        // the observatory codes)
        cell_renderer = new AvailabilityCellRenderer ();
        column_model = AvailabilityTable.getColumnModel();
        nColumns = column_model.getColumnCount();
        for (col_count = 1; col_count < nColumns; col_count++) 
        {
            column = column_model.getColumn(col_count);
            column.setCellRenderer(cell_renderer);
        }

        // resize column widths to fit contents
        margin = column_model.getColumnMargin() * 2;
        nColumns = column_model.getColumnCount();
        nRows = table_model.getRowCount();
        total_width = 0;
        for (col_count = 0; col_count < nColumns; col_count++) 
        {
            column = column_model.getColumn(col_count);
            modelCol = column.getModelIndex();
            renderer = column.getHeaderRenderer();
            if (renderer == null) renderer = AvailabilityTable.getTableHeader().getDefaultRenderer();
            width = renderer.getTableCellRendererComponent (AvailabilityTable,
                                                            column.getHeaderValue(),
                                                            false, false, 0, 0).getPreferredSize().width;
            for (row_count = 0; row_count < nRows; row_count++) 
            {
                renderer = AvailabilityTable.getCellRenderer(row_count, col_count);
                w = renderer.getTableCellRendererComponent (AvailabilityTable,
                                                            table_model.getValueAt(row_count, modelCol), 
                                                            false, false, row_count, col_count).getPreferredSize().width;
                if (w > width) width = w;
            }
            column.setPreferredWidth(width + margin);
            total_width += width + margin;
        }

        // use the following code to set the window width to the size of the
        // table - this may not be a good idea if the table is very wide
//        size = AvailabilityTable.getPreferredScrollableViewportSize();
//        size.width = total_width;
//        AvailabilityTable.setPreferredScrollableViewportSize(size);
//        pack ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        AvailabilityScrollPane = new javax.swing.JScrollPane();
        AvailabilityTable = new javax.swing.JTable();
        CloseButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Data Catalogue");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        AvailabilityTable.setFont(new java.awt.Font("Monospaced", 0, 12));
        AvailabilityTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        AvailabilityTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        AvailabilityTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AvailabilityTableMouseClicked(evt);
            }
        });

        AvailabilityScrollPane.setViewportView(AvailabilityTable);

        getContentPane().add(AvailabilityScrollPane, java.awt.BorderLayout.CENTER);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        getContentPane().add(CloseButton, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AvailabilityTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AvailabilityTableMouseClicked
        int row, column, count;
        boolean showPopup;
        String obsy_code;
        CDObservatoryIterator iterator;
        CDObservatoryIterator.ObservatoryInfo obsy_info;
        WorldMapObservatoryPopup popup;
        Point view_pos;
        
        row = AvailabilityTable.rowAtPoint (evt.getPoint());
        column = AvailabilityTable.columnAtPoint (evt.getPoint());
        
        if (column > 0)
        {
            try
            {
                obsy_code = (String) table_model.getValueAt (row, 0);
                iterator = new CDObservatoryIterator (GlobalObjects.database_list);
                for (count=0, obsy_info = iterator.GetFirstObservatory(); 
                     count<iterator.GetNObservatories();
                     count ++, obsy_info = iterator.GetNextObservatory())
                {
                    if (obsy_code.equalsIgnoreCase (obsy_info.GetObservatoryCode()))
                    {
                        if (evt.getButton() == evt.BUTTON1) showPopup = true;
                        else showPopup = false;
                        view_pos = AvailabilityScrollPane.getViewport().getViewPosition();
                        WorldMap.showObsyPopup (null, this, obsy_info, evt.getX () - view_pos.x, evt.getY () - view_pos.y, showPopup, "from_catalog_window");
                        break;
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException e) { }
        }
            
    }//GEN-LAST:event_AvailabilityTableMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        CloseButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_CloseButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane AvailabilityScrollPane;
    private javax.swing.JTable AvailabilityTable;
    private javax.swing.JButton CloseButton;
    // End of variables declaration//GEN-END:variables
    
}
