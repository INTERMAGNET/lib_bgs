package bgs.geophys.library.cdf.edit;

//$Id: CDFE_DeleteVariableAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;

import java.awt.*;
import java.awt.Cursor;
import java.awt.event.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;

import gsfc.nssdc.cdf.*;

/**
 * Delete the currently selected variable.
 *
 * Note:  If the variable is metadata or support_data, then a check is
 * performed to determine if the variable is in use, if so, then the variable
 * will not be removed and the user will be notified where the variable is in
 * use.  This link will have to be removed before the variable can be deleted.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */

public class CDFE_DeleteVariableAction extends AbstractAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Delete Variable";

    private CDFE_MYEditor myEditor;

    public CDFE_DeleteVariableAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_DeleteVariableAction(CDFE_MYEditor myEditor, String name) {
	super(name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	Object vObj = myEditor.variablePanel.getSelectedVar();
	if (vObj == null) {
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor, 
					  "No variable selected",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    int result = JOptionPane.
		showConfirmDialog(myEditor, 
				  "Do you want to delete Variable: "+
				  vObj.toString(),
				  "Delete zVariable",
				  JOptionPane.YES_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		myEditor.setWaitCursor();
		try {
		    myEditor.variablePanel.deleteSelectedVar();
		    myEditor.variablePanel.updateVarPanel(null);
                    if (myEditor.vAttrPanel.getSelectedAttr() != null) {
		      myEditor.vAttrPanel.updateAttrPanel(myEditor.vAttrPanel.getSelectedAttr(),
							  true);
                    }
		    int max = CDFE_CDFToolUtils.getMaxRecNum(myEditor.theCDF); 
		    myEditor.gAttrPanel.setMaxWritten(max);
                    myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		    myEditor.vAttrPanel.getRightPanel().revalidate();
                    myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		    myEditor.gAttrPanel.getRightPanel().revalidate();

		} catch (CDFException exc) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myEditor, 
						  "CDF Error:\n"+
						  CDFException.getStatusMsg(exc.getCurrentStatus()),
						  "MYEditor: CDFException",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
	myEditor.setDefaultCursor();
    }

} // CDFE_DeleteVariableAction
