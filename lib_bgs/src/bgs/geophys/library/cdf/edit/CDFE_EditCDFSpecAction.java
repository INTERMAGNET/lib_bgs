package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EditCDFSpecAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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

public class CDFE_EditCDFSpecAction extends AbstractAction {
    
    private static final String DEFAULT_NAME = "Edit CDF Specs";

    private CDFE_MYEditor myEditor;

    static final long serialVersionUID = 1L;

    public CDFE_EditCDFSpecAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_EditCDFSpecAction(CDFE_MYEditor myEditor, String name) {
	super(name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();

	CDFDialog dialog = new CDFDialog(myEditor.getFrame(), myEditor.theCDF);
        myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
        myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
	myEditor.vAttrPanel.getRightPanel().revalidate();
	myEditor.vAttrPanel.getRightPanel().repaint();
	myEditor.gAttrPanel.getRightPanel().revalidate();
	myEditor.gAttrPanel.getRightPanel().repaint();
	myEditor.setDefaultCursor();
    }
    
} // CDFE_EditCDFSpecAction
