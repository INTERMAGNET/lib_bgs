package bgs.geophys.library.cdf.edit;

//$Id: CDFE_DeleteEntryAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Delete the currently selected entry.
 *
 * Note:  If the entry is metadata or support_data, then a check is
 * performed to determine if the entry is in use, if so, then the entry
 * will not be removed and the user will be notified where the entry is in
 * use.  This link will have to be removed before the entry can be deleted.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */

public class CDFE_DeleteEntryAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Delete Entry";

    private CDFE_MYEditor myEditor;
    private CDFE_EditAttrEntryPanel myPanel;

    public CDFE_DeleteEntryAction(CDFE_EditAttrEntryPanel myPanel) {
	this(myPanel, DEFAULT_NAME);
    }

    public CDFE_DeleteEntryAction(CDFE_EditAttrEntryPanel myPanel, String name) {
	super(name);
	this.myPanel = myPanel;
	myEditor = myPanel.getMyEditor();
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	Entry eObj = myPanel.getSelectedEntry();
	if (eObj == null) {
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor, 
					  "No entry selected",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    String id = null;
	    if (myPanel.getScope() == GLOBAL_SCOPE) id = "" + (eObj.getID() + 1);
	    else {
	      try {
		id = myEditor.theCDF.getVariable(eObj.getID()).getName();
	      } catch (CDFException exc) {
	      }
	    }
	    StringBuffer msg = new StringBuffer("Delete ");
	    char gORv;
	    if (myPanel.getScope() == GLOBAL_SCOPE) {
	      msg.append("gEntry");
	      gORv = 'g';
	    } else {
	      msg.append("zEntry");
	      gORv = 'z';
	    }
	    int result = JOptionPane.
		showConfirmDialog(myEditor, 
				  "Do you want to delete Entry: "+id+" from "+gORv+
				  "Attribute:"+myPanel.getSelectedAttr(),
				  msg.toString(),
				  JOptionPane.YES_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		myEditor.setWaitCursor();
		try {
		    myPanel.deleteSelectedEntry();
		    myPanel.updateAttrEntryPanel(null, true);
		    if (myPanel.getScope() == GLOBAL_SCOPE) 
		      myEditor.gAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), true);
		    else
		      myEditor.vAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), false);
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

} // CDFE_DeleteEntryAction
