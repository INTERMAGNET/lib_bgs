/*
 * AbsoluteValuesPanel.java
 *
 * Created on 08 October 2008, 15:28
 */

package bgs.geophys.library.Magnetogram.BLV;







import bgs.geophys.library.Data.ImagCD.BLVData;
import bgs.geophys.library.Swing.ExcelAdapter;
import java.awt.Color;
import java.awt.Component;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author  jex
 */
public class AdoptedValuesPanel extends javax.swing.JPanel {
private ExcelAdapter excel_adapter;

    private class BLVTableModel extends DefaultTableModel
    {
        public BLVTableModel (Object[][] data, String[] columnNames)
        {
            super (data, columnNames);
        }
        public boolean isCellEditable(int row, int column) 
        {
            return false;
        }
    }



    /** Creates new form AbsoluteValuesPanel */
    public AdoptedValuesPanel(final BLVData bLV, String title) {
        initComponents();
        
        fileNameLabel.setText("Adopted Values From: "+ bLV.getSourceFile());
        fileNameLabel.setText(title);
        Object[][] tableEntries;
        String[] columnTitles;
        if(bLV.getFormat().equalsIgnoreCase("IBFV1.11")){
            tableEntries = new Object[bLV.getAdoptedValues().size()][6];
            columnTitles = new String[6];
        }
        else{
            tableEntries = new Object[bLV.getAdoptedValues().size()][7];
            columnTitles = new String[7];
        }
        
        for (int j=0;j<bLV.getAdoptedValues().size();j++){
            Calendar c = new GregorianCalendar(bLV.getYear(),1,1);
            c.set(Calendar.DAY_OF_YEAR,bLV.getAdoptedValue(j).getDay());
            tableEntries[j][0] = String.format("%2d/%2d/%4d",
                                       c.get(Calendar.DAY_OF_MONTH),
                                       (c.get(Calendar.MONTH)+1),
                                       c.get(Calendar.YEAR));
                                 
            tableEntries[j][1] = bLV.getAdoptedValue(j).getDay();
            tableEntries[j][2] = bLV.getAdoptedValue(j).getComponentValueScaled(bLV.getComponentAt(0),BLVData.getScalingFromFile());
            tableEntries[j][3] = bLV.getAdoptedValue(j).getComponentValueScaled(bLV.getComponentAt(1),BLVData.getScalingFromFile());
            tableEntries[j][4] = bLV.getAdoptedValue(j).getComponentValueScaled(bLV.getComponentAt(2),BLVData.getScalingFromFile());
            if(bLV.getFormat().equalsIgnoreCase("IBFV2.00")){
              tableEntries[j][5] = bLV.getAdoptedValue(j).getSComponent();
              tableEntries[j][6] = bLV.getAdoptedValue(j).getDeltaFScaled(BLVData.getScalingFromFile());
            }
            else tableEntries[j][5] = bLV.getAdoptedValue(j).getDeltaFScaled(BLVData.getScalingFromFile());

            }
        columnTitles[0] = new String ("Date");
        columnTitles[1] = new String ("Day of Year");
        columnTitles[2] = new String (bLV.getComponentAt(0).toString() +" ("+bLV.getUnitLabelAtComponent(0)+")");
        columnTitles[3] = new String (bLV.getComponentAt(1).toString() +" ("+bLV.getUnitLabelAtComponent(1)+")");
        columnTitles[4] = new String (bLV.getComponentAt(2).toString() +" ("+bLV.getUnitLabelAtComponent(2)+")");

        if(bLV.getFormat().equalsIgnoreCase("IBFV2.00")){
           columnTitles[5] = new String ("Scalar F (nT)");
           columnTitles[6] = new String (BLVData.getDeltaFNameLabel()+" ("+BLVData.getDeltaFUnitLabel()+")");
        }
        else columnTitles[5] = new String (BLVData.getDeltaFNameLabel()+" ("+BLVData.getDeltaFUnitLabel()+")");

        this.adoptedValuesTable.setModel(new BLVTableModel(
            tableEntries,
            columnTitles
//            new String [] {
//                "Date",
//                "Day of Year",
//                bLV.getComponentAt(0).toString()+" ("+bLV.getUnitLabelAtComponent(0)+")",
//                bLV.getComponentAt(1).toString()+" ("+bLV.getUnitLabelAtComponent(1)+")",
//                bLV.getComponentAt(2).toString()+" ("+bLV.getUnitLabelAtComponent(2)+")",
//                BLVData.getDeltaFNameLabel()+" ("+BLVData.getDeltaFUnitLabel()+")",
//                          }
            )); 
                //*************************************************
        
excel_adapter = new ExcelAdapter (this, adoptedValuesTable, 
                                  true, false, false, true, true);
    }
    
     public AdoptedValuesPanel(final BLVData bLV) {
      new AdoptedValuesPanel(bLV,"Adopted Values From: "+ bLV.getSourceFile());  
    }
   
public ExcelAdapter getExcelAdapter () { return excel_adapter; }   

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        adoptedValuesTable = new javax.swing.JTable();
        fileNameLabel = new javax.swing.JLabel();

        adoptedValuesTable.setFont(new java.awt.Font("Courier New", 0, 11));
        adoptedValuesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        adoptedValuesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        adoptedValuesTable.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(adoptedValuesTable);

        fileNameLabel.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(205, 205, 205)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileNameLabel))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(fileNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 546, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable adoptedValuesTable;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}