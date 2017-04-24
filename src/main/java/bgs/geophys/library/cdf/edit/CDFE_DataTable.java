package bgs.geophys.library.cdf.edit;

//$Id: CDFE_DataTable.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.DefaultCellEditor;

/**
 * A Table which accepts arrays of primative or string values.
 *
 * <BR><BR>The DefaultDataTable provided in the JFC does not support arrays
 * of primatives.  The user must ensure that all primative values are
 * wrapped in the corresponding <code>java.lang.Number</code> class. This 
 * class takes care of this problem.
 *
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 *
 */
public class CDFE_DataTable extends JTable {
    
    static final long serialVersionUID = 1L;

    // Constructors

    /**
     * Construct a default DataTable.
     */
    public CDFE_DataTable() {
	this(new CDFE_DefaultDataTableModel());
    }

    /**
     * Construct a default DataTable.
     * @param cellEditable boolean indicating whether the cells may be editable
     */
    public CDFE_DataTable(boolean cellEditable) {
        this(new CDFE_DefaultDataTableModel(cellEditable));
    }

    /**
     * Construct a DataTable using the given model.
     *
     * @param model A CDFE_DefaultDataTableModel
     */
    public CDFE_DataTable(CDFE_DefaultDataTableModel model) {
	super(model);
	setUpFloatEditor();
	setUpDoubleEditor();
	setUpByteEditor();
	setUpShortEditor();
	setUpIntegerEditor();
	setUpLongEditor();
    }

    /**
     * Construct a DataTable using the given model.
     *
     * @param model A CDFE_DefaultDataTableModel
     * @param cellEditable boolean indicating whether the cells may be editable
     */
    public CDFE_DataTable(CDFE_DefaultDataTableModel model, boolean cellEditable) {
        super(model);
	model.setCellEditable(cellEditable);
        setUpFloatEditor();
        setUpDoubleEditor();
        setUpByteEditor();
        setUpShortEditor();
        setUpIntegerEditor();
        setUpLongEditor();
    }

    /**
     * Construct a DataTable containing the given data.
     *
     * @param data the table data (must be an array).
     */
    public CDFE_DataTable(Object data) {
	this(new CDFE_DefaultDataTableModel(data));
    }

    /**
     * Construct a DataTable containing the given data.
     *
     * @param data the table data (must be an array).
     * @param cellEditable boolean indicating whether the cells may be editable
     */
    public CDFE_DataTable(Object data, boolean cellEditable) {
        this(new CDFE_DefaultDataTableModel(data, cellEditable));
    }

    /**
     * Construct a DataTable containing the given data using the specified
     * majority.
     *
     * @param data the table data (must be an array).
     * @param majority either CDFE_DefaultDataTableModel.ROW_MAJOR or
      CDFE_DefaultDataTableModel.COLUMN_MAJOR
     */
    public CDFE_DataTable(Object data, int majority) {
	this(new CDFE_DefaultDataTableModel(data, majority));
    }

    /**
     * Construct a DataTable containing the given data using the specified
     * majority.
     *
     * @param data the table data (must be an array).
     * @param majority either CDFE_DefaultDataTableModel.ROW_MAJOR or
      CDFE_DefaultDataTableModel.COLUMN_MAJOR
     * @param cellEditable boolean indicating whether the cells may be editable
     */
    public CDFE_DataTable(Object data, int majority, boolean cellEditable) {
        this(new CDFE_DefaultDataTableModel(data, majority, cellEditable));
    }

    // Private methods to set up the editors for the Number objects

    private void setUpFloatEditor() {
        //Set up the editor for the float cells.
        final CDFE_FloatNumberField floatField = new CDFE_FloatNumberField(0, 5);
        floatField.setHorizontalAlignment(CDFE_FloatNumberField.RIGHT);

        DefaultCellEditor floatEditor = 
            new DefaultCellEditor(floatField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Float, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return floatField.getFloat();
                }
            };
        this.setDefaultEditor(Float.class, floatEditor);
    }

    private void setUpDoubleEditor() {
        //Set up the editor for the double cells.
        final CDFE_FloatNumberField floatField = new CDFE_FloatNumberField(0, 5);
        floatField.setHorizontalAlignment(CDFE_FloatNumberField.RIGHT);

        DefaultCellEditor doubleEditor = 
            new DefaultCellEditor(floatField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Float, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return floatField.getDouble();
                }
            };
        this.setDefaultEditor(Double.class, doubleEditor);
    }

    private void setUpByteEditor() {
        //Set up the editor for the byte cells.
        final CDFE_WholeNumberField wholeField = new CDFE_WholeNumberField(0, 5);
        wholeField.setHorizontalAlignment(CDFE_WholeNumberField.RIGHT);

        DefaultCellEditor byteEditor = 
            new DefaultCellEditor(wholeField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Whole, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return wholeField.getByte();
                }
            };
        this.setDefaultEditor(Byte.class, byteEditor);
    }

    private void setUpShortEditor() {
        //Set up the editor for the short cells.
        final CDFE_WholeNumberField wholeField = new CDFE_WholeNumberField(0, 5);
        wholeField.setHorizontalAlignment(CDFE_WholeNumberField.RIGHT);

        DefaultCellEditor shortEditor = 
            new DefaultCellEditor(wholeField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Whole, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return wholeField.getShort();
                }
            };
        this.setDefaultEditor(Short.class, shortEditor);
    }

    private void setUpIntegerEditor() {
        //Set up the editor for the integer cells.
        final CDFE_WholeNumberField wholeField = new CDFE_WholeNumberField(0, 5);
        wholeField.setHorizontalAlignment(CDFE_WholeNumberField.RIGHT);

        DefaultCellEditor integerEditor = 
            new DefaultCellEditor(wholeField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Whole, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return wholeField.getInteger();
                }
            };
        this.setDefaultEditor(Integer.class, integerEditor);
    }

    private void setUpLongEditor() {
        //Set up the editor for the long cells.
        final CDFE_WholeNumberField wholeField = new CDFE_WholeNumberField(0, 5);
        wholeField.setHorizontalAlignment(CDFE_WholeNumberField.RIGHT);

        DefaultCellEditor longEditor = 
            new DefaultCellEditor(wholeField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Whole, not a String:

                static final long serialVersionUID = 1L;

                public Object getCellEditorValue() {
                    return wholeField.getLong();
                }
            };
        this.setDefaultEditor(Long.class, longEditor);
    }

} // CDFE_DataTable
