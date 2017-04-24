package bgs.geophys.library.cdf.edit;

//$Id: CDFE_CopyVariableAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;

import gsfc.nssdc.cdf.*;

/**
 * Copy the currently selected variable.  The user will be prompted for a 
 * new variable name.
 *
 * Note:  This will copy the metadata as well as any data for the selected
 * variable.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */

public class CDFE_CopyVariableAction extends AbstractAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Copy Variable";

    private CDFE_MYEditor myEditor;

    public CDFE_CopyVariableAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_CopyVariableAction(CDFE_MYEditor myEditor, String name) {
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
	    String oldName = vObj.toString();
	    String dest = 
		JOptionPane.showInputDialog(myEditor,
					    "Enter new variable name",
					    "Copy Variable: "+oldName,
					    JOptionPane.QUESTION_MESSAGE);
	    if (dest != null) {
		try {
		    Variable dv = ((Variable)vObj).duplicate(dest);
		    myEditor.variablePanel.
			addToListOfVariables(dv.getName());
		    myEditor.variablePanel.reselectCurrentVariable();
                    myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
                    myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
                    if (myEditor.vAttrPanel.getSelectedAttr() != null)
                      myEditor.vAttrPanel.updateAttrPanel(myEditor.vAttrPanel.getSelectedAttr(), 
							  true);


		} catch (CDFException ce) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myEditor, 
					  "CDF Error:\n"+
					  CDFException.getStatusMsg(ce.getCurrentStatus()),
					  "CDFException",
					  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
	myEditor.setDefaultCursor();
    }
    
} // CDFE_CopyVariableAction
