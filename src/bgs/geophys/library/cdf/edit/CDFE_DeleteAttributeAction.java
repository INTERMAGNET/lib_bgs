package bgs.geophys.library.cdf.edit;

//$Id: CDFE_DeleteAttributeAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Delete the currently selected Attribute.
 *
 * Note:  If the attribute is metadata or support_data, then a check is
 * performed to determine if the attribute is in use, if so, then the attribute
 * will not be removed and the user will be notified where the attribute is in
 * use.  This link will have to be removed before the attribute can be deleted.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */

public class CDFE_DeleteAttributeAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Delete Attribute";

    private CDFE_MYEditor myEditor;
    private CDFE_AttributePanel attrPanel;
    private long scope;

    public CDFE_DeleteAttributeAction(CDFE_AttributePanel attrPanel, char gORv) {
	this(attrPanel, "Delete "+gORv+"Attribute");
    }

    public CDFE_DeleteAttributeAction(CDFE_AttributePanel attrPanel, String name) {
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
	if (vObj == null) {
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor, 
					  "No attribute selected",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    char gORv = (scope == GLOBAL_SCOPE) ? 'g' : 'v';
	    int result = JOptionPane.
		showConfirmDialog(myEditor, 
				  "Do you want to delete "+gORv+"Attribute: "+
				  vObj.toString(),
				  "Delete "+gORv+"Attribute",
				  JOptionPane.YES_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		myEditor.setWaitCursor();
		try {
		    attrPanel.deleteSelectedAttr();
                    attrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		    if (scope == VARIABLE_SCOPE) {
		      myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF) ;
		      myEditor.gAttrPanel.getRightPanel().revalidate();
		      myEditor.gAttrPanel.getRightPanel().repaint();
		    } else {
		      myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		      myEditor.vAttrPanel.getRightPanel().revalidate();
		      myEditor.vAttrPanel.getRightPanel().repaint();
		    }
		    attrPanel.updateAttrPanel(null, true);
		    attrPanel.removeFromListOfAttributes((Attribute)vObj);

		    if (scope == VARIABLE_SCOPE) 
		      if (myEditor.variablePanel.getSelectedVar() != null) 
			myEditor.variablePanel.updateVarPanel(myEditor.variablePanel.getSelectedVar());

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

} // CDFE_DeleteAttributeAction
