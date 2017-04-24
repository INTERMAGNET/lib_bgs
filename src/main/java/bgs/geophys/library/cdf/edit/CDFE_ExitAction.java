package bgs.geophys.library.cdf.edit;

//$Id: CDFE_ExitAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * Quit the application.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_ExitAction extends CDFE_AbstractFileAction {
    
    static final long serialVersionUID = 1L;

    private static final String DEFAULT_NAME = "Exit";

    public CDFE_ExitAction(CDFE_MYEditor myEditor) {
	this(myEditor, DEFAULT_NAME);
    }

    public CDFE_ExitAction(CDFE_MYEditor myEditor, String name) {
	super(myEditor, name);
	this.myEditor = myEditor;
    }

    public void putValue(String key, Object value) {
	if (value != null)
	    super.putValue(key, value);
    }

    public void actionPerformed(ActionEvent event) {
	myEditor.setWaitCursor();

	if (myEditor.theCDF != null) {
	  performCloseAction();
	} 
/*      else {
          myEditor.theCDF = null;

          new File(myEditor.workingRootname+".cdf").delete();
          if (myEditor.ext.toLowerCase().equals(".skt"))
            new File(myEditor.workingRootname+myEditor.ext).delete();
	  myEditor.setVisible(false);
	}
*/
	myEditor.setDefaultCursor();
	myEditor.setVisible(false);
//	((CDFEdit)myEditor.getFrame()).getDialog().setVisible(true);
	myEditor.dispose();
    }

} // CDFE_ExitAction
