package bgs.geophys.library.cdf.edit;

// $Id: CDFE_EntryListListener.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.*;

import gsfc.nssdc.cdf.*;

/**
 * Listen for selections made in the entry list and update the 
 * entryPanel.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_EntryListListener implements CDFConstants, ListSelectionListener {

    private CDFE_EditAttrEntryPanel myPanel;
    private CDFE_MYEditor myEditor;
    private long scope;

    public CDFE_EntryListListener(CDFE_EditAttrEntryPanel myPanel, long scope) {
	super();
	this.myPanel = myPanel;
	this.scope = scope;
	myEditor = myPanel.getMyEditor();
    }

    public void valueChanged(ListSelectionEvent e) {
	JList jl = (JList)e.getSource();
	if (!e.getValueIsAdjusting() && (jl.getSelectedIndex() >= 0)) {
	    jl.removeListSelectionListener(this);
	    myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    
	    Entry en = null;
	    boolean result;
	    Entry oldEntry = myPanel.getSelectedEntry();
	    long entryID;
	    String entryName;
	    try {
	      	if (scope == GLOBAL_SCOPE) { 
	          entryID = Long.parseLong((String)jl.getSelectedValue()) - 1;
		  en = myPanel.getSelectedAttr().getEntry(entryID);
	      	} else {
	          entryName = (String)jl.getSelectedValue();
		  en = myPanel.getSelectedAttr().getEntry(myEditor.theCDF.getVariable(entryName));
	      	}
		myPanel.updateAttrEntryPanel(en, false);
	    } catch (CDFException exc) {
		if (scope == GLOBAL_SCOPE) {
		  exc.printStackTrace();
		  Toolkit.getDefaultToolkit().beep();
		  JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
		   			        CDFException.getStatusMsg(exc.getCurrentStatus()),
					        "MYEditor: Error",
					        JOptionPane.ERROR_MESSAGE);
		  if (oldEntry != null) jl.setSelectedValue(oldEntry.getName(), true);
		} else {
		  myPanel.updateAttrEntryPanel(null, false);
		}
	    } 
	    jl.addListSelectionListener(this);
	}
	myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
