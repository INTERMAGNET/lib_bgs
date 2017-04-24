package bgs.geophys.library.cdf.edit;

/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.JTable;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.*;

/**
 *  This class creates the table showing all entries for a given variable 
 *  from a CDF to allow user to edit
 */

public class CDFE_EditvEntryTablePanel extends JPanel implements CDFConstants {

    static final long serialVersionUID = 1L;

    private Attribute attr;

    private CDFE_EditvEntryTablePanel aTablePanel;
    private CDFE_MYEditor myEditor;

    private CDF cdf;
    private JTable table;
    private MyTableModel myModel;
    private JScrollPane scrollPane;

    private Object[][] dataObject = null;

    private static String[] headcol = {	"Var Name", "Data Type",
					"Entry Data" };

    CDFE_EditvEntryTablePanel(CDFE_EditAttrEntryPanel attrPanel) {

	aTablePanel = this;
	myEditor = attrPanel.getMyEditor();
	this.cdf = myEditor.theCDF;
        scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
    }

    public void buildTable(Attribute attr) {

	this.attr = attr;

	if (attr != null) buildDataTable();
	if (dataObject != null) {
          myModel = new MyTableModel();
          table = new JTable(myModel);
//          myModel.addTableModelListener(this);
          table.setPreferredScrollableViewportSize(new Dimension(530, 210));
          table.setRowSelectionAllowed(false);
          table.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);

          TableColumn column = null;
          for (int i = 0; i < headcol.length; i++) {
             column = table.getColumnModel().getColumn(i);
             column.setResizable(true);
             if (i == 0) column.setPreferredWidth(110);
             if (i == 1) column.setPreferredWidth(110);
             if (i == 2) column.setPreferredWidth(300);
          }
//          setUpDataTypeColumn(table.getColumnModel().getColumn(1));
	  table.setCellSelectionEnabled(false);

	  scrollPane.setViewportView(table);
	  aTablePanel.removeAll();
	  aTablePanel.revalidate();
	  aTablePanel.repaint();
	  aTablePanel.add(scrollPane, BorderLayout.CENTER);
	}
    }

    public void setTable(Object[][] data) {
        dataObject = data;
        myModel.fireTableChanged(new TableModelEvent(myModel));

    }

    public Object[][] getTable() {

        return (Object[][]) dataObject;

    }

    public void setColumnNames(String[] columnNames) {

        headcol = columnNames;

    }

    public String[] getColumnNames() {

        return (String[]) headcol;

    }
/*
    public void tableChanged(TableModelEvent tme) {
    }
*/

    public void setUpDataTypeColumn(TableColumn dataTypeColumn) {
        //Set up the editor for the dataType cells.
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("CDF_BYTE");
        comboBox.addItem("CDF_INT1");
        comboBox.addItem("CDF_UINT1");
        comboBox.addItem("CDF_INT2");
        comboBox.addItem("CDF_UINT2");
        comboBox.addItem("CDF_INT4");
        comboBox.addItem("CDF_UINT4");
        comboBox.addItem("CDF_INT8");
        comboBox.addItem("CDF_REAL4");
        comboBox.addItem("CDF_FLOAT");
        comboBox.addItem("CDF_REAL8");
        comboBox.addItem("CDF_DOUBLE");
        comboBox.addItem("CDF_EPOCH");
	comboBox.addItem("CDF_EPOCH16");
	comboBox.addItem("CDF_TIME_TT2000");
        comboBox.addItem("CDF_CHAR");
        comboBox.addItem("CDF_UCHAR");

        dataTypeColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the dataType cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for valid data types");
        dataTypeColumn.setCellRenderer(renderer);

        //Set up tool tip for the dataType column header.
        TableCellRenderer headerRenderer = dataTypeColumn.getHeaderRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer)headerRenderer).setToolTipText(
                     "Click the dataType to see a list of choices");
        }
    }

    class MyTableModel extends AbstractTableModel {

        static final long serialVersionUID = 1L;

        public int getColumnCount() {
            return headcol.length;
        }

        public int getRowCount() {
	  if (dataObject != null)
            return dataObject.length;
	  else
	    return 0;
        }

        public String getColumnName(int col) {
            return headcol[col];
        }

        public Object getValueAt(int row, int col) {
	  if (dataObject != null)
            return dataObject[row][col];
	  else
	    return null;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col == 0)
                return false;
            else
		return false;
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then a Boolean column would contain text ("true"/"false"),
         * rather than a check box.
         */
        // public Class getColumnClass(int c) {
        //    return getValueAt(0, c).getClass();
        // }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {

            Object oldone = getValueAt(row, col);

            if (dataObject[0][col] instanceof Integer) {
                //If we don't do something like this, the column
                //switches to contain Strings.
                //XXX: See TableEditDemo.java for a better solution!!!
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Integer((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only integer values.");
                }
            } else if (dataObject[0][col] instanceof Float) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Float((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only float values.");
                }
            } else if (dataObject[0][col] instanceof Byte) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Byte((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only yyte values.");
                }
            } else if (dataObject[0][col] instanceof Long) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Long((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only long values.");
                }
            } else if (dataObject[0][col] instanceof Short) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Short((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only short values.");
                }
            } else if (dataObject[0][col] instanceof Double) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = new Double((String)value);
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only double values.");
                }
            } else if (dataObject[0][col] instanceof Boolean) {
                try {
                    if (col == 1) applyValue(oldone, value, row, col);
                    if (col == 2) value = checkValue(value, row);
                    dataObject[row][col] = (Boolean)value;
                    fireTableCellUpdated(row, col);
                } catch (NumberFormatException e) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myEditor.frame,
                        "The \"" + getColumnName(col)
                        + "\" column accepts only boolean values.");
                }

            } else {
                if (col == 1) applyValue(oldone, value, row, col);
                if (col == 2) value = checkValue(value, row);
                dataObject[row][col] = value;
                fireTableCellUpdated(row, col);
            }

        }

        void applyValue(Object origin, Object newone, int row, int col) {
            if (!((String)origin).equals((String)newone)) {
              if (((String)newone).equals("CDF_CHAR") ||
                  ((String)newone).equals("CDF_UCHAR"))
                JOptionPane.showMessageDialog(myEditor.frame,
                    "The data type changed from " + origin
                    + " to " + newone + ". Please change entry value accordingly.");
            }
        }

        void checkNumElem(Object newone, int row) {
            String dataType = (String) getValueAt(row, 1);
            if (dataType.equals("CDF_CHAR") && dataType.equals("CDF_UCHAR"))
              dataObject[row][2] = "" + ((String)newone).length();
            else
              dataObject[row][2] = "1";
            fireTableCellUpdated(row, 3);
        }

        Object checkValue(Object newone, int row) {

            String dataType = (String) getValueAt(row, 1);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            if (dataType.equals("CDF_BYTE") || dataType.equals("CDF_INT1")) {
                formatter.setParseIntegerOnly(true);
                try {
                  if (newone instanceof Byte)
                    return new Byte(formatter.parse(((Byte)newone).toString()).byteValue());
                  else
                    return (String) "" + formatter.parse((String)newone).byteValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_UINT1") || dataType.equals("CDF_INT2")) {
                formatter.setParseIntegerOnly(true);
                try {
                  if (newone instanceof Short)
                    return new Short(formatter.parse(((Short)newone).toString()).shortValue());
                  else
                    return (String) "" + formatter.parse((String)newone).shortValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_UINT2") || dataType.equals("CDF_INT4")) {
                formatter.setParseIntegerOnly(true);
                try {
                  if (newone instanceof Integer)
                    return new Integer(formatter.parse(((Integer)newone).toString()).intValue());
                  else
                    return (String) "" + formatter.parse((String)newone).intValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_UINT4") || dataType.equals("CDF_INT8")) {
                formatter.setParseIntegerOnly(true);
                try {
                  if (newone instanceof Long)
                    return new Long(formatter.parse(((Long)newone).toString()).longValue());
                  else
                    return (String) "" + formatter.parse((String)newone).longValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_REAL4") || dataType.equals("CDF_FLOAT")) {
                formatter.setParseIntegerOnly(false);
                try {
                  if (newone instanceof Float)
                    return new Float(formatter.parse(((Float)newone).toString()).floatValue());
                  else
                    return (String) "" + formatter.parse((String)newone).floatValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_REAL8") || dataType.equals("CDF_EPOCH")) {
                formatter.setParseIntegerOnly(false);
                try {
                  if (newone instanceof Double)
                    return new Double(formatter.parse(((Double)newone).toString()).doubleValue());
                  else
                    return (String) "" + formatter.parse((String)newone).doubleValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_EPOCH16")) {
                formatter.setParseIntegerOnly(false);
                try {
                    return (String) "xx" + formatter.parse((String)newone).doubleValue();
                } catch (ParseException e) {
                  return null;
                }
            } else if (dataType.equals("CDF_CHAR") || dataType.equals("CDF_UCHAR")) {
                return newone;
            }
            return null;
        }

    }

/**
  * Build the data and columns that are used for the tables.
 */

    private void buildDataTable() {

        Vector vars = cdf.getVariables();

	dataObject = null;

	if (vars != null) {
	  Variable var;

	  int varNumX = vars.size();
	  if (varNumX > 0) {
	    int varNum = 0;
            for (int ii = 0; ii < varNumX; ii++) {
              var = (Variable) vars.elementAt(ii);
              try {
                Entry en = attr.getEntry(var);
		varNum++;
              } catch (CDFException ex) {
              }
	    }
	    if (varNum > 0) {
	      dataObject = new Object[varNum][headcol.length];

	      Entry en = null;
	      long dataType;
	      long length;
	      Object value = null;
	      int jj = -1;

              for (int ii = 0; ii < varNumX; ii++) {
	        try {
		  var = (Variable) vars.elementAt(ii);
	          en = attr.getEntry(var);
	        } catch (CDFException ex) {
	          en = null;
	        }
	        if (en != null) {
		  jj++;
		  var = (Variable) vars.elementAt(ii);
	          dataType = en.getDataType();
// Modifications: SMF
//                  try {
	            value = en.getData();
// Modifications: SMF
//                  } catch (CDFException ex) {}
		  length = en.getNumElements();	
                  dataObject[jj][0] = var.getName();
	          dataObject[jj][1] = CDFUtils.getStringDataType(dataType) + "/" + 
					length;
	          if (dataType == CDF_EPOCH) {
                    if (value.getClass().getName().indexOf('[') == 0) { // an array
                      StringBuffer tmpStr = new StringBuffer();
                      for (int i = 0; i < ((double[])value).length; i++) {
                        if (i > 0) tmpStr.append(", ");
                        String tmp = Epoch.encode(((double[])value)[i]);
                        tmpStr.append(tmp);
                      }
//                      tmpStr.setLength(tmpStr.length()-2);
                      dataObject[jj][2] = tmpStr.toString();
                    } else {
                      dataObject[jj][2] = Epoch.encode(((Double)value).doubleValue());
		    }
                  } else if (dataType == CDF_EPOCH16) {
                    if (value.getClass().getName().indexOf('[') == 0) { // an array
                      StringBuffer tmpStr = new StringBuffer();
		      double[] aaa = new double[2];
                      for (int i = 0; i < ((double[])value).length; i=i+2) {
                        if (i > 0) tmpStr.append(", ");
			aaa[0] = ((double[])value)[i];
			aaa[1] = ((double[])value)[i+1];
                        String tmp = Epoch16.encode(aaa);
                        tmpStr.append(tmp);
                      }
//                      tmpStr.setLength(tmpStr.length()-2);
                      dataObject[jj][2] = tmpStr.toString();
                    }
                  } else if (dataType == CDF_TIME_TT2000) {
                    if (value.getClass().getName().indexOf('[') == 0) { // an array
                      StringBuffer tmpStr = new StringBuffer();
                      for (int i = 0; i < ((long[])value).length; i++) {
                        if (i > 0) tmpStr.append(", ");
                        String tmp = CDFTT2000.toUTCstring(((long[])value)[i]);
                        tmpStr.append(tmp);
                      }
//                      tmpStr.setLength(tmpStr.length()-2);
                      dataObject[jj][2] = tmpStr.toString();
                    } else {
                      dataObject[jj][2] = CDFTT2000.toUTCstring(((Long)value).longValue());
                    }
                  } else
		    dataObject[jj][2] = CDFUtils.getStringData(value, ", ");
	        }
	      }
	    }
	  }

        }

    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void reset() {
	if (dataObject != null) {
          aTablePanel.setVisible(true);
	  setTable(dataObject);
	}
    }

}
