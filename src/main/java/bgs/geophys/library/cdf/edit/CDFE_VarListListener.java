package bgs.geophys.library.cdf.edit;

// $Id: CDFE_VarListListener.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Listen for selections made in the variable list and update the 
 CDFE_VariablePanel.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_VarListListener implements ListSelectionListener {

    private CDFE_VariablePanel myPanel;
    private CDFE_MYEditor myEditor;

    public CDFE_VarListListener(CDFE_VariablePanel myPanel) {
	super();
	this.myPanel = myPanel;
	myEditor = myPanel.getMyEditor();
    }

    public void valueChanged(ListSelectionEvent e) {
	JList jl = (JList)e.getSource();
	if (!e.getValueIsAdjusting() && (jl.getSelectedIndex() >= 0)) {
	    jl.removeListSelectionListener(this);
	    myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    
	    Variable v = null;
	    Variable oldVar = myPanel.getSelectedVar();
	    String varName = jl.getSelectedValue().toString();
	    try {
		v = myEditor.theCDF.getVariable(varName);
		myPanel.saveVariableChanges();
		myPanel.updateVarPanel(v);
	    } catch (CDFException exc) {
		exc.printStackTrace();
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(myEditor.sharedInstance(), 
					      CDFException.getStatusMsg(exc.getCurrentStatus()),
					      "MYEditor: Error",
					      JOptionPane.ERROR_MESSAGE);
		if (oldVar != null) jl.setSelectedValue(oldVar.toString(), true);
	    } 
	    jl.addListSelectionListener(this);
	}
	myEditor.sharedInstance().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
