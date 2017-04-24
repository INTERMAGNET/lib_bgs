package bgs.geophys.library.cdf.edit;

//$Id: CDFE_AbstractDataTableModel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.table.AbstractTableModel;

/**
 * Provides the framework and helper methods for displaying arrays of
 * primatives in a table.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public abstract class CDFE_AbstractDataTableModel extends AbstractTableModel {

    /**
     * The type of the array.
     *
     * Either Z, C, B, S, I, J, F, D or L
     */
    protected char _type;

    // Abstract methods

    /**
     * Set the data in this model
     */
    public abstract void setData(Object data);

    /**
     * Get the data object out of this model.
     */
    public abstract Object getData();


    ////////////////////////////////////////////////////
    //                                                //
    //             Private Methods                    //
    //                                                //
    ////////////////////////////////////////////////////

    /**
     * Create a new 1D array of the correct type
     */
    protected Object newArray(int length) {
	switch(_type) {
	case 'B':
	    return new byte[length];
	case 'S':
	    return new short[length];
	case 'I':
	    return new int[length];
	case 'J':
	    return new long[length];
	case 'F':
	    return new float[length];
	case 'D':
	    return new double[length];
	case 'Z':
	    return new boolean[length];
	case 'L':
	    return new String[length];
	default:
	    return new String[length];
	}
    }

    /**
     * Create a new data object of the correct type
     */
    protected Object newDataObject(int x, int y) {
	switch(_type) {
	case 'B':
	    return new byte[x][y];
	case 'S':
	    return new short[x][y];
	case 'I':
	    return new int[x][y];
	case 'J':
	    return new long[x][y];
	case 'F':
	    return new float[x][y];
	case 'D':
	    return new double[x][y];
	case 'Z':
	    return new boolean[x][y];
	case 'L':
	    return new String[x][y];
	default:
	    return new String[x][y];
	}
    }
    
} // AbtractDataTableModel
