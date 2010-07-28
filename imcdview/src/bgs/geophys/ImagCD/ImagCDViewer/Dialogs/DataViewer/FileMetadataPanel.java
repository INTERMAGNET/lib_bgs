/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.DataViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataDay;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataMonth;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.DisplayData;
import bgs.geophys.library.Data.ImagCD.ImagCDDataException;
import bgs.geophys.library.Data.ImagCD.ImagCDHeader;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Swing.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author smf
 */
public class FileMetadataPanel extends DataViewerPanel
{
    private JScrollPane scroll_pane;
    private JTable table;
    private DefaultTableModel table_model;
    private JLabel title_label;

    public FileMetadataPanel ()
    {
        String columns [];
        Dimension dim;
        
        columns = new String [10];
        columns [0] = "File";
        columns [1] = "<html>Recorded<br>elements</html>";
        columns [2] = "<html>Colatitude<br>(degrees)</html>";
        columns [3] = "<html>Longitude<br>(degrees)</html>";
        columns [4] = "<html>Elevation<br>(metres)</html>";
        columns [5] = "Institute";
        columns [6] = "Instrument";
        columns [7] = "<html>Sample<br>Rate (S)</html>";
        columns [8] = "<html>Publication<br>date</html>";
        columns [9] = "<html>Format<br>version</html>";
        table_model = new DefaultTableModel (columns, 1);
        table = new JTable (table_model);
        scroll_pane = new JScrollPane (table);
        scroll_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_pane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        SwingUtils.ResizeTableColumnWidths(table, 1, 1000, 37, 7, 7, 7, 7, 7, 7, 7, 7, 7);
        
        dim = table.getTableHeader().getPreferredSize();
        if (dim != null)
        {
            dim.height *= 2;
            table.getTableHeader().setPreferredSize(dim);
        }

        title_label = new JLabel ();
        
        this.setLayout (new BorderLayout ());
        this.add (title_label, "North");
        this.add (scroll_pane, "Center");
    }
    
    /********************************************************************
     * update - update the display with new data
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
        int count, count2;
        File file;
        String string, string2, row [], errmsg;
        ImagCDHeader cd_header;

        makeTitle ("Metadata for: ", obsy_name, compare_obsy_name, true, year, month, day, time_period);
        title_label.setText ("<html><h2>" + getTitle() + "</html>");
        
        table_model.setRowCount (0);
      
        row = new String [10];
        if (data_list.getNDataFiles() <= 0)
        {
            row [0] = "No data files were found.";
            for (count=1; count<row.length; count++) row [count] = "";
            table_model.addRow (row);
        }
        else
        {
            for (count=0; count<data_list.getNDataFiles(); count++)
            {
                file = data_list.getDataFile(count);
                row [0] = file.getAbsolutePath();
                try 
                {
                    cd_header = new ImagCDHeader (file, true); 
                    errmsg = null;
                }
                catch (FileNotFoundException e) 
                {
                    errmsg = "Not found"; 
                    cd_header = null; 
                }
                catch (IOException e) 
                {
                    errmsg = "IO error";
                    cd_header = null; 
                }
                catch (ImagCDDataException e) 
                {
                    errmsg = "Bad header"; 
                    cd_header = null; 
                }
                if (cd_header == null)
                {
                    for (count=1; count<row.length; count++) row [count] = errmsg;
                }
                else
                {
                    row [1] = cd_header.getRecordedElements();
                    row [2] = String.format ("%.3f", (double) cd_header.getColatitude() / 1000.0);
                    row [3] = String.format ("%.3f", (double) cd_header.getLongitude() / 1000.0);
                    row [4] = Integer.toString (cd_header.getElevation());
                    row [5] = cd_header.getInstituteCode();
                    string = cd_header.getInstrumentCode();
                    string2 = cd_header.getSensorOrientation();
                    if (string.length() <= 0)
                    {
                        if (string2.length() > 0) string = "(" + string2 + ")";
                    }
                    else
                    {
                        if (string2.length() > 0) string = string + " (" + string2 + ")";
                    }
                    row [6] = string;
                    row [7] = String.format ("%.3f", (double) cd_header.getSamplePeriod() / 1000.0);
                    row [8] = cd_header.getPublicationDate();
                    row [9] = cd_header.getFormatVersion();
                }
                table_model.addRow (row);
            }
        }
    }
    
}
