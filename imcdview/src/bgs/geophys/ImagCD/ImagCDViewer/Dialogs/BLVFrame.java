/*
 * BLVFrame.java
 *
 * Created on 26 November 2008, 14:08
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatory;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.CDMisc;
import bgs.geophys.library.Data.ImagCD.BLVData;
import bgs.geophys.library.Data.ImagCD.BLVException;
import bgs.geophys.library.JFreeChart.JFreeChartUtils;
import bgs.geophys.library.Magnetogram.BLV.BLVPanel;
import bgs.geophys.library.Swing.ExcelAdapter;
import bgs.geophys.library.Swing.ImageSaverDialog;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.io.File;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jfree.chart.JFreeChart;


/**
 *
 * @author  jex
 */
public class BLVFrame extends javax.swing.JFrame 
{
    
   private Vector year_list;
   private int current_year_index, disp_year_index;
   private String obsy_code;
   private boolean dont_update;
   private Component selectedPanel;   // for print and save options
   private boolean isPlotSelected;
   private JFreeChart plotChart;
   private BLVPanel BLVPlotPanel = null;
   
    // the base directory of the data source to use (or null to use all data sources)
    private String base_dir;
    // the data source to use (or null to use all data sources)
    private CDDatabase database;
  
    /** Creates new form BLVFrame
     * @param obsy_code code for the observatory
     * @param current_year the year to display
    * @param base_dir - if the viewer should be restricted to one data source
    *        then set this to the path of the data source, otherwise set it to null
     */
    public BLVFrame (String obsy_code, int current_year, String base_dir) 
    throws BLVException
    {
        Vector obsy_list;
        CDObservatory observatory = null;
        File textfile;
        BLVData bLV;
        int year;
        int n, checkYear, numberOfYears;
        String errorMessage = null;


        this.obsy_code = obsy_code;
        if (base_dir == null)
            database = null;
        else
            database = CDMisc.findDataSource(base_dir);
        if (database == null)
            this.base_dir = null;
        else
            this.base_dir = base_dir;
        
        // make list of available years for this observatory
        if (database == null)
            year_list = CDMisc.makeYearList (obsy_code);
        else
            year_list = CDMisc.makeYearList (obsy_code, database);
        
        // check data OK for each year
        checkYear=0;
        numberOfYears = year_list.size();
        errorMessage = new String("");
        
        for(n=0;n<numberOfYears;n++){
          year = ((Integer) year_list.elementAt(checkYear)).intValue();
        
          if (database == null)
              obsy_list = CDMisc.getObservatoryList(obsy_code,year);
          else
              obsy_list = CDMisc.getObservatoryList(obsy_code,year,database);
            if (obsy_list == null) {
                observatory = null;
                textfile = null;
            } else {
                observatory = (CDObservatory) obsy_list.elementAt(0);
                textfile = observatory.GetFile(CDObservatory.FILE_BLV_TEXT);
            }
         
 
        if(textfile!= null){  // check the data
            bLV = new BLVData();
            try {
 //               System.out.println("reading1 data..");
                bLV.readInData(textfile.getAbsolutePath());
                
//                                bLV.printBLVData();
                checkYear++;  // move on an element in list
            }catch (BLVException ex){
//                System.out.println("Error reading..");
                // remove from year list in error case
                // don't increment checkYear since all elements shuffle along
                year_list.removeElementAt(checkYear);
                errorMessage = errorMessage.concat("\n"+ex.getMessage());


            }}
        } //end of checking each year for the observatory
       
        initComponents();    
        this.setTitle ("Baseline viewer: ");
        setIconImage (GlobalObjects.imag_icon.getImage ());
        
        
         if(!errorMessage.contentEquals("")){
                             JOptionPane.showMessageDialog(this,
                        errorMessage,
                        "Error Message",
                        javax.swing.JOptionPane.ERROR_MESSAGE);

         }
        
               if(year_list.size()<1){   // no data available
  
                // no data available
//                JOptionPane.showMessageDialog(this,
//                        "No data available for " + observatory.GetObservatoryName(),
//                        "Error Message", 
//                        javax.swing.JOptionPane.ERROR_MESSAGE);
                throw (new BLVException("No data available for " + observatory.GetObservatoryName()));
 //                 return;
        }
        
       
        
        // set the current year index to be the most recent year or
        // the specified year
        current_year_index = -1;
        disp_year_index = -2;
        dont_update = true;
        selectYearComboBox.removeAllItems();
        for (n = 0; n < year_list.size(); n++)
        {
            selectYearComboBox.addItem (year_list.elementAt (n).toString ());
            if (((Integer)year_list.elementAt (n)).intValue () == current_year)
            current_year_index = n;
        }
        if (current_year_index < 0) current_year_index = year_list.size() - 1;
        dont_update = false;
//                     this.plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        update ();
//                     this.plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        pack ();
        
        assignUniqueID();
    }
    
    /** variables to define a unique ID for this viewer */
    private static int id_counter = 0;
    private int unique_id;
    private void assignUniqueID () { unique_id = id_counter ++; }
    public int getUniqueID () { return unique_id; }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plotPanel = new javax.swing.JPanel();
        ButtonPanel = new javax.swing.JPanel();
        prev_button = new javax.swing.JButton();
        year_label = new javax.swing.JLabel();
        selectYearComboBox = new javax.swing.JComboBox();
        next_button = new javax.swing.JButton();
        copyToClipboardButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        plotPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                plotPanelMouseClicked(evt);
            }
        });
        plotPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                plotPanelPropertyChange(evt);
            }
        });
        plotPanel.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                plotPanelVetoableChange(evt);
            }
        });
        plotPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(plotPanel, java.awt.BorderLayout.CENTER);

        ButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ButtonPanelMouseEntered(evt);
            }
        });

        prev_button.setText("Previous year");
        prev_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prev_buttonActionPerformed(evt);
            }
        });
        ButtonPanel.add(prev_button);

        year_label.setText("Year:");
        ButtonPanel.add(year_label);

        selectYearComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectYearComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectYearComboBoxActionPerformed(evt);
            }
        });
        ButtonPanel.add(selectYearComboBox);

        next_button.setText("Next year");
        next_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next_buttonActionPerformed(evt);
            }
        });
        ButtonPanel.add(next_button);

        copyToClipboardButton.setText("Copy to clipboard");
        copyToClipboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyToClipboardButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(copyToClipboardButton);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(saveButton);

        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(printButton);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(closeButton);

        getContentPane().add(ButtonPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void prev_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prev_buttonActionPerformed
      current_year_index --;
      if (current_year_index < 0) current_year_index = 0;
      update ();
}//GEN-LAST:event_prev_buttonActionPerformed

    private void next_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next_buttonActionPerformed
      current_year_index ++;
      if (current_year_index >= year_list.size()) current_year_index = year_list.size() -1;
      update ();
}//GEN-LAST:event_next_buttonActionPerformed

    private void selectYearComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectYearComboBoxActionPerformed
        current_year_index = selectYearComboBox.getSelectedIndex();
        update ();
}//GEN-LAST:event_selectYearComboBoxActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        GlobalObjects.print_canvas.doPrint((Printable) selectedPanel);
    }//GEN-LAST:event_printButtonActionPerformed

    
private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    
    ImageSaverDialog save_dialog;
    save_dialog = GlobalObjects.createImageSaverDialog();
    if (save_dialog.runModal(this) == JFileChooser.APPROVE_OPTION){
       Cursor old_cursor;

       GlobalObjects.persistImageSaverOptions(save_dialog);
        old_cursor = this.getCursor();
        this.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        plotChart = BLVPlotPanel.finalChart;  //reset this in case it has changed
        JFreeChartUtils.save (this, plotChart, save_dialog.getChosenFile(), 
                              "INTERMAGNET", 
                              "Base line values for " + obsy_code,
                              save_dialog.getChosenFileType(),
                              save_dialog.getImageSizeX(),
                              save_dialog.getImageSizeY());
        this.setCursor(old_cursor);
    }   
}//GEN-LAST:event_saveButtonActionPerformed

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
   
    this.dispose();
}//GEN-LAST:event_closeButtonActionPerformed

private void plotPanelVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_plotPanelVetoableChange

}//GEN-LAST:event_plotPanelVetoableChange

private void plotPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_plotPanelPropertyChange
  
  // TODO add your handling code here:
}//GEN-LAST:event_plotPanelPropertyChange

private void ButtonPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ButtonPanelMouseEntered
//   printButton.setEnabled(isPlotSelected);
//   saveButton.setEnabled(isPlotSelected);    // TODO add your handling code here:
}//GEN-LAST:event_ButtonPanelMouseEntered

private void plotPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotPanelMouseClicked
       
     // TODO add your handling code here:
}//GEN-LAST:event_plotPanelMouseClicked

private void copyToClipboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyToClipboardButtonActionPerformed
        // TODO add your handling code here:
  BLVPlotPanel.getExcelInfo();
}//GEN-LAST:event_copyToClipboardButtonActionPerformed
    
    /** update the BLV display */
    private void update() {
        int current_year;
        BLVData bLV;
        Vector obsy_list;
        CDObservatory observatory;
        File textfile;
        Cursor old_cursor;
        JButton help_button;
     
        // do we really want to update
        if (dont_update) {
            return;
        }
        
        // check that year has changed
        if (current_year_index != disp_year_index) {
 
            current_year = ((Integer) year_list.elementAt(current_year_index)).intValue();
            
            // get list of observatories for this year
            if (database == null)
                obsy_list = CDMisc.getObservatoryList(obsy_code, current_year);
            else
                obsy_list = CDMisc.getObservatoryList(obsy_code, current_year, database);
            if (obsy_list == null) {
                observatory = null;
                textfile = null;
            } else {

                observatory = (CDObservatory) obsy_list.elementAt(0);
                textfile = observatory.GetFile(CDObservatory.FILE_BLV_TEXT);
            }

            // set year displayed in drop-down list - remove action listener first
            old_cursor = getCursor();
            // show busy cursor
            setCursor(GlobalObjects.wait_cursor);

          if(textfile!= null){
            bLV = new BLVData();
            try {
//                System.out.println("reading data...");
                bLV.readInData(textfile.getAbsolutePath());
                // display the data without filename on the plot
 
//                bLV.printBLVData();
                this.plotPanel.removeAll();
                plotPanel.repaint();
                
                help_button = new JButton ("Help on zoom and scroll...");
                help_button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            GlobalObjects.command_interpreter.interpretCommand("show_help&from_baseline_viewer_" + Integer.toString (getUniqueID()) + "&ZoomBLV.html");
                        }
                    });
                
                BLVPlotPanel = new BLVPanel(bLV, false,
 "Baseline Viewer: Base Line Data for "+observatory.GetObservatoryName()+" "+current_year,
 "Baseline Viewer: Adopted Values for "+observatory.GetObservatoryName()+" "+current_year,
 "Baseline Viewer: Observed Values for "+observatory.GetObservatoryName()+" "+current_year,  
 "Baseline Viewer: Comments for "+observatory.GetObservatoryName()+" "+current_year,
                        ProgramOptionsDialog.getProgramFontSize(true));
                BLVPlotPanel.addToButtonPanel(help_button);
                
        // add a listener to get tabbed pane events from the BLV panel
        BLVPlotPanel.addTabbedPaneChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        
                
//  replace panel instead of just adding
//             this.plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
// make sure the plot panel goes in at index panelIndex to make referencing easier
             this.plotPanel.add(BLVPlotPanel, "Center");
//             this.plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    
// set frame title, as in TextFileSelector
             if (base_dir == null)
                 this.setTitle("Baseline viewer: " + observatory.GetObservatoryName() + ", " + current_year);
             else
                 this.setTitle("Baseline viewer: " + observatory.GetObservatoryName() + ", " + current_year + " [restricted to " + base_dir + "]");
            
                // set flag for displayed year
                disp_year_index = current_year_index;
                selectYearComboBox.setSelectedIndex(current_year_index);
                this.copyToClipboardButton.setEnabled(false);
  } catch (BLVException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage()+"\n From update",
                        "Error Message",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
           // remove from year list in error case
           if(disp_year_index != -2){ 
            current_year_index = disp_year_index; // go back to what it was
           }else{
               current_year_index --;
               if (current_year_index < 0) current_year_index = 0;
           }
                
            selectYearComboBox.setSelectedIndex(current_year_index);
            return;       
            
    }finally{
     // TODO: cope with condition where textfile is null (obsy not found)
            // enable and disable components according to the year selected
            if (current_year_index == 0) {
                prev_button.setEnabled(false);
            } else {
                prev_button.setEnabled(true);
            }
            if (current_year_index == year_list.size() - 1) {
                next_button.setEnabled(false);
            } else {
                next_button.setEnabled(true);
            }
            // set year displayed in drop-down list - remove action listener first


                     setCursor(old_cursor);
                     this.selectedPanel = BLVPlotPanel.currentSelectedTab;
                    
                     this.printButton.setEnabled(BLVPlotPanel.isPlotSelected);
                     this.saveButton.setEnabled(BLVPlotPanel.isPlotSelected);
                     this.isPlotSelected = BLVPlotPanel.isPlotSelected;
                     plotChart = BLVPlotPanel.finalChart;
//      this.printButton.setEnabled(false);
//                     this.saveButton.setEnabled(false);               
        }
    }else{
      JOptionPane.showMessageDialog(this,
                        "No Base Line Data available for "+ current_year,
                        "Error Message",
                        javax.swing.JOptionPane.ERROR_MESSAGE);//TODO: textfile null
           if(disp_year_index != -2){ 
            current_year_index = disp_year_index; // go back to what it was
           }else{
               current_year_index --;
               if (current_year_index < 0) current_year_index = 0;
           }
                
            selectYearComboBox.setSelectedIndex(current_year_index);
            setCursor(old_cursor);
            return;
           }
    }
    pack();
    }
 
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyToClipboardButton;
    private javax.swing.JButton next_button;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JButton prev_button;
    private javax.swing.JButton printButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox selectYearComboBox;
    private javax.swing.JLabel year_label;
    // End of variables declaration//GEN-END:variables

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt)
    { 
        boolean plotEnable;
        boolean tableEnable;
        
        if (BLVPlotPanel == null){
            plotEnable = false;
            tableEnable = false;
        }
        else {
            plotEnable = BLVPlotPanel.isPlotPanelSelected ();
            tableEnable = BLVPlotPanel.isTablePanelSelected();
        }
        printButton.setEnabled(plotEnable);
        saveButton.setEnabled(plotEnable);
        copyToClipboardButton.setEnabled(tableEnable);
    }
    
    
}
