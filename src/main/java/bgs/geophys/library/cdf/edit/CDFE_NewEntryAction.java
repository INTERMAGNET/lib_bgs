package bgs.geophys.library.cdf.edit;

//$Id: CDFE_NewEntryAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Create a new attribute entry.
 *
 * @author Phil Williams 
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_NewEntryAction extends AbstractAction implements CDFConstants {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "New Entry";

    private CDFE_MYEditor myEditor;
    private CDFE_EditAttrEntryPanel myPanel;
    private long scope;

    public CDFE_NewEntryAction(CDFE_EditAttrEntryPanel myPanel) {
	this(myPanel, DEFAULT_NAME);
    }

    public CDFE_NewEntryAction(CDFE_EditAttrEntryPanel myPanel, String name) {
	super(name);
	this.myPanel = myPanel;
	myEditor = myPanel.getMyEditor();
	scope = myPanel.getScope();
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();
	myPanel.getEntryMenu().setPopupMenuVisible(false);
	if (myEditor.theCDF == null) {
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
					  "Must open a CDF before "+
					  "creating attribute entries.",
					  "MYEditor: Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    char gORv;
	    if (scope == GLOBAL_SCOPE) gORv = 'g';
	    else gORv = 'v';
	    Entry entry = 
		CDFE_NewEntryDialog.createEntry(myPanel, "Create "+gORv+"AttrEntry for "+
					   myPanel.getSelectedAttr());
	    if (entry != null) {
	      myPanel.getAttrScrollBar().getModel().removeChangeListener(myPanel.getScrollBarSync());

	      if (scope == GLOBAL_SCOPE) {
//		myPanel.getAttrScrollBar().getModel().removeChangeListener(myPanel.getScrollBarSync());
                myEditor.gAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), true);
                myEditor.gAttrPanel.reselectCurrentAttribute();
	      } else {
                myEditor.vAttrPanel.updateAttrPanel(myPanel.getSelectedAttr(), false);
                myEditor.vAttrPanel.reselectCurrentAttribute();
	      }
	      myPanel.addToListOfEntries(entry);
	      myPanel.updateAttrEntryPanel(entry, true);
//	      if (scope == GLOBAL_SCOPE)
		myPanel.getAttrScrollBar().getModel().addChangeListener(myPanel.getScrollBarSync());
	      myPanel.reselectCurrentEntry();
	    }
	}

	myEditor.setDefaultCursor();
    }

} // CDFE_NewEntryAction
