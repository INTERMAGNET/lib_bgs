package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EditAttrEntryAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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

public class CDFE_EditAttrEntryAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Modify Entry";

    private CDFE_EditAttrEntryPanel myPanel;
    private CDFE_MYEditor myEditor;

    public CDFE_EditAttrEntryAction(CDFE_EditAttrEntryPanel myPanel) {
	this(myPanel, DEFAULT_NAME);
    }

    public CDFE_EditAttrEntryAction(CDFE_EditAttrEntryPanel myPanel, String name) {
	super(name);
	this.myPanel = myPanel;
	this.myEditor = myPanel.getMyEditor();
	myPanel.getEntryMenu().setPopupMenuVisible(false);	
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	myPanel.getEntryMenu().setPopupMenuVisible(false);
	char gORv = (myPanel.getScope() == GLOBAL_SCOPE) ? 'g' : 'v';
	CDFE_EditAttrEntryDialog.editEntry(myPanel, "Modify Entry for "+gORv+"Attribute: "
						+myPanel.getSelectedAttr());
	myPanel.updateAttrEntryPanel(myPanel.getSelectedEntry(), true);
	if (myPanel.getScope() == GLOBAL_SCOPE)
	  myEditor.gAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), true);
	else
	  myEditor.vAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), true);

	myEditor.setDefaultCursor();
    }
    
} // EditgAttrEntryAction
