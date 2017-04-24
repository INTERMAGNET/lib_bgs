package bgs.geophys.library.cdf.edit;

//$Id: CDFE_RenameAttributeAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Rename the currently selected attribute.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_RenameAttributeAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Rename Attribute";

    private CDFE_MYEditor myEditor;
    private CDFE_AttributePanel attrPanel;

    public CDFE_RenameAttributeAction(CDFE_AttributePanel attrPanel, char gORv) {
	this(attrPanel, "Rename "+gORv+"Attribute");
    }

    public CDFE_RenameAttributeAction(CDFE_AttributePanel attrPanel, String name) {
	super(name);
	this.attrPanel = attrPanel;
	myEditor = attrPanel.getMyEditor();
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	Object vObj = attrPanel.getSelectedAttr();
	Object newAttr = null;
	if (vObj == null) {
	    Toolkit.getDefaultToolkit().beep(); 
	    JOptionPane.showMessageDialog(myEditor, 
					  "No attribute selected",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    String oldName = vObj.toString();
	    String newName = 
		JOptionPane.showInputDialog((JFrame)myEditor.getFrame(),
					    "Enter a new attribute name",
					    "Rename Attribute: "+oldName,
					    JOptionPane.QUESTION_MESSAGE);
	    if (newName != null) {
		myEditor.setWaitCursor();
		try {
		    attrPanel.renameSelectedAttr(newName);
		    if (attrPanel.getScope() == GLOBAL_SCOPE) 
		      attrPanel.updateAttrPanel(attrPanel.getSelectedAttr(), true);
		    else {
                      if (myEditor.variablePanel.getSelectedVar() != null) 
			myEditor.variablePanel.updateVarPanel(myEditor.variablePanel.getSelectedVar());
		    }
		} catch (CDFException ce) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
						  "CDF Error:\n"+
						  ce.getMessage(),
						  "CDFException",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
	myEditor.setDefaultCursor();
    }
    

} // CDFE_RenameAttributeAction
