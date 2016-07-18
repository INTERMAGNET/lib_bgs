package bgs.geophys.library.cdf.edit;

// $Id: CDFE_AttrListListener.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Listen for selections made in the attribute list and update the 
 * attributePanel.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_AttrListListener implements CDFConstants, ListSelectionListener {

    private CDFE_AttributePanel myPanel;
    private CDFE_MYEditor myEditor;
    private long scope;

    public CDFE_AttrListListener(CDFE_AttributePanel myPanel) {
	super();
	this.myPanel = myPanel;
	myEditor = myPanel.getMyEditor();
	scope = myPanel.getScope();
    }

    public void valueChanged(ListSelectionEvent e) {
	JList jl = (JList)e.getSource();
	if (!e.getValueIsAdjusting() && (jl.getSelectedIndex() >= 0)) {
	    jl.removeListSelectionListener(this);
	    myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    
	    Attribute a = null;
	    Attribute oldAttr = myPanel.getSelectedAttr();
	    String attrName = jl.getSelectedValue().toString();
	    try {
		a = myEditor.theCDF.getAttribute(attrName);
		myPanel.updateAttrPanel(a, false);
	    } catch (CDFException exc) {
		exc.printStackTrace();
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
					      CDFException.getStatusMsg(exc.getCurrentStatus()),
					      "MYEditor: Error",
					      JOptionPane.ERROR_MESSAGE);
		if (oldAttr != null) jl.setSelectedValue(oldAttr.toString(), true);
	    } 
	    jl.addListSelectionListener(this);
	}
	myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
