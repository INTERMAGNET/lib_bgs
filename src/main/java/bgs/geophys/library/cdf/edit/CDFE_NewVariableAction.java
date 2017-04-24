package bgs.geophys.library.cdf.edit;

//$Id: CDFE_NewVariableAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Create a new variable.
 *
 * @author Phil Williams 
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_NewVariableAction extends AbstractAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "New Variable";

    private CDFE_MYEditor myEditor;

    public CDFE_NewVariableAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_NewVariableAction(CDFE_MYEditor myEditor, String name) {
	super(name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	if (myEditor.theCDF == null) {
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
					  "Must open a CDF before "+
					  "creating variables.",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    Variable var = 
		CDFE_NewVariableDialog.createVariable((JFrame)myEditor.getFrame(),
						 myEditor.theCDF);
	    if (var != null) {
		myEditor.variablePanel.addToListOfVariables(var);
		myEditor.variablePanel.updateVarPanel(var);
		myEditor.variablePanel.reselectCurrentVariable();
                if (myEditor.vAttrPanel.getSelectedAttr() != null) {
                  Attribute selectedAttr = myEditor.vAttrPanel.getSelectedAttr();
                  myEditor.vAttrPanel.getvDisplay().buildTable(selectedAttr);
		  JScrollPane sp = new JScrollPane(myEditor.vAttrPanel.getvDisplay().getTable());
		  myEditor.vAttrPanel.getRightPanel().add(sp, BorderLayout.CENTER);
	        }
		myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		myEditor.vAttrPanel.getRightPanel().revalidate();
		myEditor.vAttrPanel.getRightPanel().repaint();
                myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		myEditor.gAttrPanel.getRightPanel().revalidate();
		myEditor.gAttrPanel.getRightPanel().repaint();

	    }
	}
	myEditor.setDefaultCursor();
    }

} // CDFE_NewVariableAction
