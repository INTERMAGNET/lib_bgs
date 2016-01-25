package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EditVariableAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;

import java.awt.Cursor;
import java.awt.event.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;

import gsfc.nssdc.cdf.*;

/**
 * For future use.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */

public class CDFE_EditVariableAction extends AbstractAction {
    
    private static final String DEFAULT_NAME = "Edit Variable";

    private CDFE_MYEditor myEditor;

    static final long serialVersionUID = 1L;

    public CDFE_EditVariableAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_EditVariableAction(CDFE_MYEditor myEditor, String name) {
	super(name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();

	CDFE_EditVariableDialog dialog = new CDFE_EditVariableDialog(myEditor.getFrame(),
							   myEditor.variablePanel.getSelectedVar());
	myEditor.variablePanel.getVarSpecPanel().set(myEditor.variablePanel.getSelectedVar());
	myEditor.variablePanel.getRightPanel().revalidate();
	myEditor.variablePanel.getRightPanel().repaint();
	if (CDFE_CDFToolUtils.getMaxRecNum(myEditor.theCDF) != myEditor.gAttrPanel.getMaxWritten()) {
	  int max = CDFE_CDFToolUtils.getMaxRecNum(myEditor.theCDF);
	  myEditor.gAttrPanel.setMaxWritten(max);
          myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
	  myEditor.gAttrPanel.getRightPanel().revalidate();
	  myEditor.gAttrPanel.getRightPanel().repaint();
	  myEditor.vAttrPanel.setMaxWritten(max);
          myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
	  myEditor.vAttrPanel.getRightPanel().revalidate();
	  myEditor.vAttrPanel.getRightPanel().repaint();
	}
	myEditor.variablePanel.getValuePanel().set(myEditor.variablePanel.getSelectedVar());

	myEditor.setDefaultCursor();
    }
    
} // CDFE_EditVariableAction
