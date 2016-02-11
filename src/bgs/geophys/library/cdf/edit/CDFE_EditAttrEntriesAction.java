package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EditAttrEntriesAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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

public class CDFE_EditAttrEntriesAction extends AbstractAction {
    
    private static final String DEFAULT_NAME = "Edit Attribute";

    private CDFE_MYEditor myEditor;
    private CDFE_AttributePanel myAttrPanel;

    static final long serialVersionUID = 1L;

    public CDFE_EditAttrEntriesAction(CDFE_AttributePanel myAttrPanel, char gORv) {
	this(myAttrPanel, "Edit "+gORv+"Attribute");
    }

    public CDFE_EditAttrEntriesAction(CDFE_AttributePanel myAttrPanel, String name) {
	super(name);
	this.myAttrPanel = myAttrPanel;
	myEditor = myAttrPanel.getMyEditor();
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	CDFE_EditAttrEntryPanel entryPanel = new CDFE_EditAttrEntryPanel(myAttrPanel);
	myEditor.setDefaultCursor();
    }
    
} // CDFE_EditAttrEntriesAction
