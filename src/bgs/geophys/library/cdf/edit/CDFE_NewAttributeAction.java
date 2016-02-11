package bgs.geophys.library.cdf.edit;

//$Id: CDFE_NewAttributeAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Create a new attribute.
 *
 * @author Phil Williams 
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_NewAttributeAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "New Attribute";

    private CDFE_MYEditor myEditor;
    private CDFE_AttributePanel attrPanel;
    private char gORv;

    public CDFE_NewAttributeAction(CDFE_AttributePanel attrPanel, char gORv) {
	this(attrPanel, "New "+gORv+"Attribute");
	this.gORv = gORv;
    }

    public CDFE_NewAttributeAction(CDFE_AttributePanel attrPanel, String name) {
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
	if (myEditor.theCDF == null) {
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
					  "Must open a CDF before "+
					  "creating attributes.",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    Attribute attr = 
		CDFE_NewAttributeDialog.createAttribute((JFrame)myEditor.getFrame(),
					       	   myEditor.theCDF, "Create "+gORv+"Attribute",
						   attrPanel.getScope());
	    if (attr != null) {
		attrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		attrPanel.updateAttrPanel(attr, true);
		attrPanel.updateListOfAttributes();
                if (attrPanel.getScope() == VARIABLE_SCOPE) {
		  myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		  myEditor.gAttrPanel.getRightPanel().revalidate();
		  myEditor.gAttrPanel.getRightPanel().repaint();
		} else {
		  myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		  myEditor.vAttrPanel.getRightPanel().revalidate();
		  myEditor.vAttrPanel.getRightPanel().repaint();
		}

                if (myEditor.variablePanel.getSelectedVar() != null) {
                  Variable selectedVar = myEditor.variablePanel.getSelectedVar();
                  myEditor.variablePanel.getvVarEntryTableScrollPane().buildTable(selectedVar);
                  myEditor.variablePanel.getRightPanel().revalidate();
                }

		attrPanel.reselectCurrentAttribute();
	    }
	}
	myEditor.setDefaultCursor();
    }

} // CDFE_NewAttributeAction
