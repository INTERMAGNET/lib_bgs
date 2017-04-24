package bgs.geophys.library.cdf.edit;

// $Id: CDFE_JLabeledDataTable.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

// Swing Imports
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.table.*;

/**
 * A CDFE_DataTable wrapped in a JScrollPane contained in  a CDFE_JLabeledPanel.
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
public class CDFE_JLabeledDataTable extends CDFE_JLabeledPanel {
    private JScrollPane sp;
    private CDFE_DataTable table;
    
    static final long serialVersionUID = 1L;

    /**
     * Construct a JLabeledDataTable labeled with the label.
     *
     * @param label the label.
     */
    public CDFE_JLabeledDataTable(String label) {
	super(label);
	table = new CDFE_DataTable();
	table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	ListSelectionModel rowSM = table.getSelectionModel();
	rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	sp = new JScrollPane(table);
	add(sp);
    }

    public CDFE_JLabeledDataTable(String label, boolean cellEditable) {
        super(label);
        table = new CDFE_DataTable(cellEditable);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sp = new JScrollPane(table);
        add(sp);
    }

    /**
     * Wraps CDFE_DataTable.setVisible.
     *
     * @see javax.swing.JTable#setVisible
     */
    public void setVisible(boolean v) {
	table.setVisible(v);
    }

    /**
     * Wraps CDFE_DataTable.setModel.
     *
     * @see javax.swing.JTable#setModel
     */
    public void setModel(CDFE_DefaultDataTableModel tm) {
	table.setModel(tm);
	sp.revalidate();
	sp.repaint();
    }

    /**
     * Wraps CDFE_DataTable.getModel.
     *
     * @see javax.swing.JTable#getModel
     */
    public CDFE_DefaultDataTableModel getModel() {
	return (CDFE_DefaultDataTableModel)table.getModel();
    }

    /**
     * Get the scroll pane containing the table
     */
    public JScrollPane getScrollPane() {
	return sp;
    }

    /**
     * Get the table containing the table
     */
    public CDFE_DataTable getDataTable() {
	return table;
    }

    public void setEnabled(boolean enable) {
	super.setEnabled(enable);
	table.setEnabled(enable);
    }
    
} 
