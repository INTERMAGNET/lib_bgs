package bgs.geophys.library.cdf.edit;

//$Id: CDFE_DefaultDataTableModel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.lang.IllegalArgumentException;
import java.lang.reflect.*;
import javax.swing.event.TableModelEvent;

/**
 *
 * This table model can be used to handle arrays of
 * primative data types.  
 *
 * It handles the wrapping and 
 * unwrapping of the primatives to the correct Number
 * object wrapper.<BR><BR>
 *
 * Note that 1D arrays are always treated as a single row. Future
 * versions may support choosing wheter data is treated as row major or
 * column major (i.e. [rows][colums] or [columns][rows], respectively).<BR><BR>
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_DefaultDataTableModel extends CDFE_AbstractDataTableModel {

    static final long serialVersionUID = 1L;

    public final static int ROW_MAJOR = 1;
    public final static int COLUMN_MAJOR = 2;

    /**
     * Number of rows in this model
     */
    private int _rows;

    /**
     * Number of columns in this model
     */
    private int _cols;

    /**      */
    protected String [] columnNames = {};

    /**
     * The data
     */
    private Object _data;
    
    /**
     * Cells are subjected to editing
     */
    private boolean cellEditable;
   
    /**
     * Either ROW_MAJOR or COLUMN_MAJOR
     */
    private int majority;
    
    /**
     * Get the value of majority.
     * @return Value of majority.
     */
    public int getMajority() {return majority;}
    
    /**
     * Set the value of majority.
     * @param v  Value to assign to majority.
     */
    public void setMajority(int  v) {this.majority = v;}
    
    /////////////////////////////////////////////
    //                                         //
    //             Constructors                //
    //                                         //
    /////////////////////////////////////////////

    /**
     * Construct a new TableModel with no data.  The majority is set
     * to ROW_MAJOR by default
     */
    public CDFE_DefaultDataTableModel() {
	super();
	_data = null;
	_rows = 0;
	_cols = 0;
	this.majority = ROW_MAJOR;
	this.cellEditable = true;
    }

    /**
     * Construct a new TableModel with no data.  The majority is set
     * to ROW_MAJOR by default
     * @param cellEditable boolean indicating whether the cells are editable
     */
    public CDFE_DefaultDataTableModel(boolean cellEditable) {
        super();
        _data = null;
        _rows = 0;
        _cols = 0;
        this.majority = ROW_MAJOR;
	this.cellEditable = cellEditable;
    }

    /**
     * Construct a new TableModel using data and the given majority.
     *
     * @param data An array object of primative values or Strings
     * @param majority Either DefaultDataTableModel.ROW_MAJOR or 
     *   DefaultDataTableModel.COLUMN_MAJOR
     */
    public CDFE_DefaultDataTableModel(Object data, int majority) 
	throws java.lang.IllegalArgumentException
    {
	this.majority = majority;
	this.cellEditable = true;
	setData(data);
    }

    /**
     * Construct a new TableModel using data and the given majority.
     *
     * @param data An array object of primative values or Strings
     * @param majority Either DefaultDataTableModel.ROW_MAJOR or
     *   DefaultDataTableModel.COLUMN_MAJOR
     * @param cellEditable boolean indicating whether the cells are editable
     */
    public CDFE_DefaultDataTableModel(Object data, int majority, boolean cellEditable)
        throws java.lang.IllegalArgumentException
    {
        this.majority = majority;
        setData(data);
	this.cellEditable = cellEditable;
    }

    /**
     * Construct a new TableModel using data as ROW_MAJOR
     *
     * @param data An array object of primative values or Strings
     */
    public CDFE_DefaultDataTableModel(Object data) 
	throws java.lang.IllegalArgumentException
    {
	this(data, ROW_MAJOR);
    }

    /**
     * Construct a new TableModel using data as ROW_MAJOR
     *
     * @param data An array object of primative values or Strings
     * @param cellEditable boolean indicating whether the cells are editable
     */
    public CDFE_DefaultDataTableModel(Object data, boolean cellEditable)
        throws java.lang.IllegalArgumentException
    {
        this(data, ROW_MAJOR, cellEditable);
    }

    /**
     * Set whether all cells are subjected to editing 
     *
     * @param cellEditable boolean indicating whether the cells are editable
     */
    public void setCellEditable(boolean cellEditable)
    {
        this.cellEditable = cellEditable;
    }

    //////////////////////////////////////////////////////////////////////////
    //                                                                      //
    //             Implementation of the TableModel Interface               //
    //                                                                      //
    //////////////////////////////////////////////////////////////////////////

    public String getColumnName(int column) {
        if (columnNames[column] != null)
	    return columnNames[column];
        else 
            return " ";
    }

    /** 
     * Get the class for the col column.  Note that all columns in 
     * Data are of the same class which is based upon the data type
     */
    public Class getColumnClass(int col) {
	switch(_type) {
	case 'B':
	    return Byte.class;
	case 'S':
	    return Short.class;
	case 'I':
	    return Integer.class;
	case 'J':
	    return Long.class;
	case 'F':
	    return Float.class;
	case 'D':
	    return Double.class;
	case 'L':
	    return String.class;
	case 'Z':
	    return Boolean.class;
	default:
	    return String.class; // keep compiler happy
	}
    }

    /**
     * Is the cell at row, col editable.
     *
     * Data tables can have rows, or columns of varying length.
     *
     * @param row the cell's row
     * @param col the cell's column
     *
     * @return true if the cell is editable.
     **/
    public boolean isCellEditable(int row, int col) {
	if (cellEditable == false) return false;

	int x = 0,y = 0;
	if (majority == ROW_MAJOR) {
	    x = row;
	    y = col;
	} else {
	    x = col;
	    y = row;
	}
	
	// Make sure that there is data in the cell
	if (y < Array.getLength(Array.get(_data, x)))
	    return true;
	else
	    return false;
    }

    /**   
     * @return the number of columns in this table
     */
    public int getColumnCount() {
        return _cols;
    }

    // Data methods
    
    /**      */
    public int getRowCount() {
	return _rows;
    }

    /**  
     * Get the value at [row,col].  This takes array values and wraps them
     * in the proper java.lang.Number for display
     */
    public Object getValueAt(int row, int col) {
	int x = 0,y = 0;
	if (majority == ROW_MAJOR) {
	    x = row;
	    y = col;
	} else {
	    x = col;
	    y = row;
	}

	switch(_type) {
	case 'B':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Byte(((byte [][])_data)[x][y]);
	    else
		return null;
	case 'S':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Short(((short [][])_data)[x][y]);
	    else
		return null;
	case 'I':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Integer(((int [][])_data)[x][y]);
	    else
		return null;
	case 'J':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Long(((long [][])_data)[x][y]);
	    else
		return null;
	case 'F':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Float(((float [][])_data)[x][y]);
	    else
		return null;
	case 'D':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Double(((double [][])_data)[x][y]);
	    else
		return null;
	case 'Z':
	    if (y < Array.getLength(Array.get(_data, x)))
		return new Boolean(((boolean [][])_data)[x][y]);
	    else
		return null;
	case 'L':
	    if (y < Array.getLength(Array.get(_data, x)))
		return Array.get(Array.get(_data, x), y);
	    else
		return null;
	default:
	    return null; // Keep compiler happy
	}
    }

    /** 
     * This unwraps the java.lang.Number object returned by the 
     * Cell editor and places the value in the data array.
     */
    public void setValueAt(Object value, int row, int col) {
	if (value != null) {
	    int x = 0,
		y = 0;
	    if (majority == ROW_MAJOR) {
		x = row;
		y = col;
	    } else {
		x = col;
		y = row;
	    }

	    switch(_type) {
	    case 'B':
		if (value instanceof java.lang.String)
		    try {
			((byte [][])_data)[x][y] = new Byte((String)value).byteValue();
		    } catch (NumberFormatException e) {
			((byte [][])_data)[x][y] = 0;
		    }
		else if (value instanceof java.lang.Byte)
		    ((byte [][])_data)[x][y] = ((Byte)value).byteValue();
		else
		    ((byte [][])_data)[x][y] = 0;
		break;
	    case 'S':
		if (value instanceof java.lang.String)
		    try {
			((short [][])_data)[x][y] = new Short((String)value).shortValue();
		    } catch (NumberFormatException e) {
			((short [][])_data)[x][y] = 0;
		    }
		else if (value instanceof java.lang.Short)
		    ((short [][])_data)[x][y] = ((Short)value).shortValue();
		else 
		    ((short [][])_data)[x][y] = 0;
		break;
	    case 'I':
		if (value instanceof java.lang.String)
		    try {
			((int [][])_data)[x][y] = new Integer((String)value).intValue();
		    } catch (NumberFormatException e) {
			((int [][])_data)[x][y] = 0;
		    }
		else if (value instanceof java.lang.Integer)
		    ((int [][])_data)[x][y] = ((Integer)value).intValue();
		else
		    ((int [][])_data)[x][y] = 0;
		break;
	    case 'J':
		if (value instanceof java.lang.String)
		    try {
			((long [][])_data)[x][y] = new Long((String)value).longValue();
		    } catch (NumberFormatException e) {
			((long [][])_data)[x][y] = 0;
		    }
		else if (value instanceof java.lang.Long)
		    ((long [][])_data)[x][y] = ((Long)value).longValue();
		else
		    ((long [][])_data)[x][y] = 0;
		break;
	    case 'F':
		if (value instanceof java.lang.String)
		    try {
			((float [][])_data)[x][y] = new Float((String)value).floatValue();
		    } catch (NumberFormatException e) {
			((float [][])_data)[x][y] = (float)0.0;
		    }
		else if (value instanceof java.lang.Float)
		    ((float [][])_data)[x][y] = ((Float)value).floatValue();
		else
		    ((float [][])_data)[x][y] = (float)0.0;
		break;
	    case 'D':
		if (value instanceof java.lang.String)
		    try {
			((double [][])_data)[x][y] = new Double((String)value).doubleValue();
		    } catch (NumberFormatException e) {
			((double [][])_data)[x][y] = 0.0;
		    }
		else if (value instanceof java.lang.Double)
		    ((double [][])_data)[x][y] = ((Double)value).doubleValue();
		else
		    ((double [][])_data)[x][y] = 0.0;
		break;
	    case 'Z':
		if (value instanceof java.lang.String)
		    ((boolean [][])_data)[x][y] = new Boolean((String)value).booleanValue();
		else if (value instanceof java.lang.Boolean)
		    ((boolean [][])_data)[x][y] = ((Boolean)value).booleanValue();
		else
		    ((boolean [][])_data)[x][y] = false;
		break;
	    case 'L':
		Array.set(Array.get(_data, x), y, value);
		break;
	    }
	}
    }

    //////////////////////////////////////////////////////////////////////////
    //                                                                      //
    //             Data Modification and Querying                           //
    //                                                                      //
    //////////////////////////////////////////////////////////////////////////

    /**
     * Set the number of rows and columns and get the signature of
     * the data object.
     *
     * @param data  The data object for the table
     * @exception java.lang.IllegalArgumentException if the data is
     *         not an array.
     */
    public void setData(Object data)
	throws java.lang.IllegalArgumentException
    {
	if (data != null) {
	    //	    if (!data.getClass().isArray())
	    //throw new 
	    //    IllegalArgumentException("Data must be an array");
	    int dim = 0;
	    if ((dim = (data.getClass().getName().lastIndexOf("[") + 1)) > 2)
		throw new 
		    IllegalArgumentException("Data must be < 2D");
	    
	    // Capture the data type
	    String sig = data.getClass().getName();
	    
	    // The data is an Object Array
	    if (sig.indexOf("L") > 0) {
		if (!(data instanceof Number) &&
		    (sig.indexOf("String") < 0))
		    throw new IllegalArgumentException("Bad data value");
		_type = 'L';
	    } else 
		_type = sig.toCharArray()[sig.length()-1];
	    
	    switch (dim) {
		/*
		  0D is a scalar which therefore must be an object. Get the
		  primative value and wrap it up
		*/
	    case 0:
		_rows = 1;
		_cols = 1;
		if (data instanceof java.lang.Byte) {
		    _data = new byte[1][1];
		    ((byte[][])_data)[0][0] = ((Byte)data).byteValue();
		    _type = 'B';
		} else if (data instanceof java.lang.Boolean) {
		    _data = new boolean[1][1];
		    ((boolean[][])_data)[0][0] = 
			((Boolean)data).booleanValue();
		    _type = 'Z';
		} else if (data instanceof java.lang.Character) {
		    _data = new char[1][1];
		    ((char[][])_data)[0][0] = ((Character)data).charValue();
		    _type = 'C';
		} else if (data instanceof java.lang.Short) {
		    _data = new short[1][1];
		    ((short[][])_data)[0][0] = ((Short)data).shortValue();
		    _type = 'S';
		} else if (data instanceof java.lang.Integer)  {
		    _data = new int[1][1];
		    ((int[][])_data)[0][0] = ((Integer)data).intValue();
		    _type = 'I';
		} else if (data instanceof java.lang.Long) {
		    _data = new long[1][1];
		    ((long[][])_data)[0][0] = ((Long)data).longValue();
		    _type = 'J';
		} else if (data instanceof java.lang.Float) {
		    _data = new float[1][1];
		    ((float[][])_data)[0][0] = ((Float)data).floatValue();
		    _type = 'F';
		} else if (data instanceof java.lang.Double) {
		    _data = new double[1][1];
		    ((double[][])_data)[0][0] = ((Double)data).doubleValue();
		    _type = 'D';
		} else  {
		    _data = new String[1][1];
		    ((String[][])_data)[0][0] = data.toString();
		    _type = 'L';
		} 
		break;

		/*
		  1D arrays will always come from the user.  All arrays
		  internal to the model are 2D arrays. Therefore, when we
		  get a 1D array, it must be wrapped in a 1x1 array.
		*/
	    case 1:
		if (majority == ROW_MAJOR) {
		    _rows = 1;
		    _cols = Array.getLength(data);
		} else {
		    _rows = Array.getLength(data);
		    _cols = 1;
		}
		_data = newDataObject(_rows, _cols); // Wrap the data
		Array.set(this._data, 0, data);
		break;
	    case 2: // 2D arrays
		if (majority == ROW_MAJOR) {
		    _rows = Array.getLength(data);
		    _cols = 0;
		    for (int i=0;i<_rows;i++)
			if (Array.getLength(Array.get(data,i)) > _cols)
			    _cols = Array.getLength(Array.get(data,i));
		} else {
		    _cols = Array.getLength(data);
		    _rows = 0;
		    for (int i=0;i<_cols;i++)
			if (Array.getLength(Array.get(data,i)) > _rows)
			    _rows = Array.getLength(Array.get(data,i));
		}
		_data = data;
		break;
	    }
	} else {
	    _rows = 0;
	    _cols = 0;
	    this._data = null;
	}

	// Setup the column names
	columnNames = new String[_cols];
	    
	// Get the column names and cache them.
	for(int column = 0; column < _cols; column++) {
	    columnNames[column] = "Col "+(column+1);
	}

	fireTableChanged(null);
    }
    
    /**
     * Get the value of _data.
     * @return Value of _data.
     */
    public Object getData() {
	if (_data == null) return null;
	Object retVal;
	if (Array.getLength(_data) == 1) {      // Unwrap the data if needed
	    retVal = Array.get(_data, 0);
	    if (Array.getLength(retVal) == 1) { // The table is a single cell
		return Array.get(retVal, 0);
	    } else                              // Table is a single row/column
		return retVal;
	} else
	    return _data;
    }
    
    /**
     * Set the number of rows for the model.  If the size is
     * greater than the current size, then rows are added.  If the
     * size is less than the current number of rows, then the rows at
     * newSize and above are discarded.
     */
    public void setNumRows(int newSize) {
	if ((newSize < 1) || (newSize == getRowCount()))
	    return;

	Object newData = null;
	if (majority == ROW_MAJOR) {
	    newData = newDataObject(newSize, getColumnCount());
	    for (int i=0;
		 i< ((newSize > getRowCount()) ? getRowCount() : newSize);
		 i++)
		Array.set(newData, i, Array.get(_data, i));
	    // Add rows if needed
	    for (int i = getRowCount();
		 i < ((newSize > getRowCount()) ? newSize : 0);
		 i++) {
		Array.set(newData, i, 
			  newArray(getColumnCount()));
	    }
	} else { // COLUMN_MAJOR
	    newData = newDataObject(getColumnCount(), newSize);
	    for (int i = 0; i < getColumnCount(); i++) {
		//Get the column
		Object aCol = Array.get(_data, i);
		int aLength = Array.getLength(aCol);
		// build a new col
		Object aNewCol = newArray(newSize);
		// Copy from old to new
		System.arraycopy(aCol, 0, 
				 aNewCol, 0, 
				 ((newSize > aLength) ? aLength : newSize));
		Array.set(newData, i, aNewCol);
	    }
	}
	setData(newData);
    }

    /**
     * Set the number of rows for the model.  If the size is
     * greater than the current size, then rows are added.  If the
     * size is less than the current number of rows, then the rows at
     * newSize and above are discarded.
     */
    public void setNumColumns(int newSize) {
	if ((newSize < 1) || (newSize == getColumnCount()))
	    return;

	Object newData = null;
	if (majority == COLUMN_MAJOR) {
	    newData = newDataObject(newSize, getRowCount());
	    for (int i=0;
		 i< ((newSize > getColumnCount()) ? getColumnCount() : newSize);
		 i++)
		Array.set(newData, i, Array.get(_data, i));
	    // Add rows if needed
	    for (int i = getColumnCount();
		 i < ((newSize > getColumnCount()) ? newSize : 0);
		 i++) {
		Array.set(newData, i, 
			  newArray(getRowCount()));
	    }
	} else { // ROW_MAJOR
	    newData = newDataObject(getRowCount(), newSize);
	    for (int i = 0; i < getRowCount(); i++) {
		//Get the column
		Object aRow = Array.get(_data, i);
		int aLength = Array.getLength(aRow);
		// build a new col
		Object aNewRow = newArray(newSize);
		// Copy from old to new
		System.arraycopy(aRow, 0, 
				 aNewRow, 0, 
				 ((newSize > aLength) ? aLength : newSize));
		Array.set(newData, i, aNewRow);
	    }
	}
	setData(newData);
    }

    public void createEmptyTable(int rows, int cols, Class dataClass) {
	Class stringClass = null;
	try {
	    stringClass = Class.forName("java.lang.String");
	} catch (ClassNotFoundException e) {
	}

	if (dataClass == Byte.TYPE)           _type = 'B';
	else if (dataClass == Boolean.TYPE)   _type = 'Z';
	else if (dataClass == Character.TYPE) _type = 'C';
	else if (dataClass == Short.TYPE)     _type = 'S';
	else if (dataClass == Integer.TYPE)   _type = 'I';
	else if (dataClass == Long.TYPE)      _type = 'J';
	else if (dataClass == Float.TYPE)     _type = 'F';
	else if (dataClass == Double.TYPE)    _type = 'D';
	else if (dataClass.isAssignableFrom(stringClass)) _type = 'L';
	else throw new IllegalArgumentException("Data tables may only be "+
						"Number or Strings."); 

	int x = 0,
	    y = 0;
	if (majority == ROW_MAJOR) {
	    x = rows;
	    y = cols;
	} else {
	    x = cols;
	    y = rows;
	}

	setData(newDataObject(x, y));
    }

} // CDFE_DefaultDataTableModel
