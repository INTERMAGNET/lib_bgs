package bgs.geophys.library.cdf.edit;

//$Id: CDFE_RenameVariableAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Rename the currently selected variable.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_RenameVariableAction extends AbstractAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Rename Variable";

    private CDFE_MYEditor myEditor;

    public CDFE_RenameVariableAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_RenameVariableAction(CDFE_MYEditor myEditor, String name) {
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
	Object newVar = null;
	if (vObj == null) {
	    Toolkit.getDefaultToolkit().beep(); 
	    JOptionPane.showMessageDialog(myEditor, 
					  "No variable selected",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    String oldName = vObj.toString();
	    String newName = 
		JOptionPane.showInputDialog((JFrame)myEditor.getFrame(),
					    "Enter a new variable name",
					    "Rename Variable: "+oldName,
					    JOptionPane.QUESTION_MESSAGE);
	    if (newName != null) {
		myEditor.setWaitCursor();
		try {
		    myEditor.variablePanel.renameSelectedVar(newName);
                    if (myEditor.vAttrPanel.getSelectedAttr() != null) {
                      Attribute selectedAttr = myEditor.vAttrPanel.getSelectedAttr();
                      myEditor.vAttrPanel.getvDisplay().buildTable(selectedAttr);
                      JScrollPane sp = new JScrollPane(myEditor.vAttrPanel.getvDisplay().getTable());
                      myEditor.vAttrPanel.getRightPanel().add(sp, BorderLayout.CENTER);
                      myEditor.vAttrPanel.getRightPanel().revalidate();
		      myEditor.vAttrPanel.getRightPanel().repaint();
                    }
                    myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		} catch (CDFException ce) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
						  CDFException.getStatusMsg(ce.getCurrentStatus()),
						  "CDFException",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
	myEditor.setDefaultCursor();
    }
    

} // CDFE_RenameVariableAction
