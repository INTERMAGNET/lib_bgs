package bgs.geophys.library.cdf.edit;

//$Id: CDFE_AbstractFileAction.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

/**
 * FileAction.java
 *
 *
 * Created: Thu Apr 15 12:40:41 1999
 *
 * @author 
 * @version $Revision: 1.1.1.1 $
 */

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.util.*;

import gsfc.nssdc.cdf.*;
/**
 * Superclass for all actions under the File menu.
 *
 * Provides some methods that are common to all actions under the file menu.
 *
 * @see CloseAction
 * @see ExitAction
 * @see NewFileAction
 * @see OpenFileAction
 * @see SaveAction
 * @see SaveAsAction
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public abstract class CDFE_AbstractFileAction extends AbstractAction {
    
    protected CDFE_MYEditor myEditor;
    protected JFileChooser chooser;
    
    protected CDFE_AbstractFileAction(CDFE_MYEditor myEditor, String name) {
	super(name);
	this.myEditor = myEditor;
    }

    /**
     * Close a file that is opened.
     *
     * Currently, the CDFE_MYEditor can only have a single file opened at a time.
 This will need to be changed if and when CDFE_MYEditor can handle multiple
 files.
     */
    public void performCloseAction() {
        StringBuffer msg = new StringBuffer();

        try {
            myEditor.closeFile();
        } catch (CDFException exc) {
            exc.printStackTrace();
	    Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(myEditor,
                                          CDFException.getStatusMsg(exc.getCurrentStatus()),
                                          "MYEditor: CDFException",
                                          JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
	    Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(myEditor,
                                          e.getMessage(),
                                          "MYEditor: Exception",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Will perform a save or save as.
     *
     * @param saveAs if true that prompt user for a file name
     * @param closeFile if true then close the file after saving
     */
    public void performSaveAction(boolean saveAs, boolean closeFile) {
	StringBuffer msg = new StringBuffer();

	File selectedFile = null;
	String name = null;
	boolean saveIt = true;

	if (saveAs) {
	    chooser = myEditor.getFileChooser();
	    chooser.setCurrentDirectory(myEditor.getWorkingDir());
	    chooser.rescanCurrentDirectory();
	    String newName = myEditor.getLogicalFilename();
	    if (newName != null)
		chooser.setSelectedFile(new File(newName+".cdf"));
	    else
		chooser.setSelectedFile(myEditor.getFile());
	    int retVal = 
		chooser.showSaveDialog(myEditor.sharedInstance());
	    if (retVal == JFileChooser.APPROVE_OPTION) {
		myEditor.setWaitCursor();
		selectedFile = chooser.getSelectedFile();
		if (selectedFile.exists()) {
		    int result =
			JOptionPane.showConfirmDialog(myEditor,
					  selectedFile.getName() + 
					  " exists.\nDo you want to "+
					  "overwrite it?",
					  "Overwrite?",
					  JOptionPane.YES_NO_OPTION);
		    if (result != JOptionPane.YES_OPTION) {
			saveIt = false;
			closeFile = false;
		    }
		}
	    } else {
		saveIt = false;
		closeFile = false;
	    }
	} else 
	    selectedFile = myEditor.getFile();
	    
	try {
	    if (saveIt) {
		myEditor.saveFile(selectedFile.getPath());
	    }

	    if (closeFile)
		myEditor.closeFile();

	} catch (CDFException exc) {
	    exc.printStackTrace();
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(),
					  "File not saved due to CDF Error: "+
					  CDFException.getStatusMsg(exc.getCurrentStatus()),
					  "MYEditor: CDFException",
					  JOptionPane.ERROR_MESSAGE);
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(),
					  "I/O Error. Check file permissions.",
					  "MYEditor: IOException",
					  JOptionPane.ERROR_MESSAGE);
	} catch (InterruptedException ie) {
	    ie.printStackTrace();
	    Toolkit.getDefaultToolkit().beep();
	    JOptionPane.showMessageDialog(myEditor.sharedInstance(),
					  "Save was interupted, please "+
					  "try again",
					  "MYEditor: InterruptedException",
					  JOptionPane.ERROR_MESSAGE);
	}
    }

} // FileAction
