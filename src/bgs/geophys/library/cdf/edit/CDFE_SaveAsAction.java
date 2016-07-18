package bgs.geophys.library.cdf.edit;

//$Id: CDFE_SaveAsAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.awt.event.*;

/**
 * Save the current file with a new name.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_SaveAsAction extends CDFE_AbstractFileAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Save As...";

    public CDFE_SaveAsAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_SaveAsAction(CDFE_MYEditor myEditor, String name) {
	super(myEditor, name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();

	performSaveAction(true, false);
	
	myEditor.setDefaultCursor();
    }

    
} // SaveAction
