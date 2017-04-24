package bgs.geophys.library.cdf.edit;

// $Id: CDFE_ExampleFileView.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

/*
 * @(#)CDFE_ExampleFileView.java	1.6 98/06/29
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.File;
import java.util.Hashtable;

/**
 * A convenience implementation of the FileView interface that
 * manages name, icon, traversable, and file type information.
 *
 * This this implemention will work well with file systems that use
 "dot" extensions to indicate file type. For example: "picture.gif"
 as a gif image.

 If the java.io.File ever contains some of this information, such as
 file type, icon, and hidden file inforation, this implementation may
 become obsolete. At minimum, it should be rewritten at that time to
 use any new type information provided by java.io.File

 Example:
    JFileChooser chooser = new JFileChooser();
    fileView = new CDFE_ExampleFileView();
    fileView.putIcon("jpg", new ImageIcon("images/jpgIcon.jpg"));
    fileView.putIcon("gif", new ImageIcon("images/gifIcon.gif"));
    chooser.setFileView(fileView);
 *
 * @version 1.6 06/29/98
 * @author Jeff Dinkins
 */
public class CDFE_ExampleFileView extends FileView {
    private Hashtable icons = new Hashtable(5);
    private Hashtable fileDescriptions = new Hashtable(5);
    private Hashtable typeDescriptions = new Hashtable(5);

    /**
     * The name of the file.  Do nothing special here. Let
     * the system file view handle this.
     * @see #setName
     * @see FileView#getName
     */
    public String getName(File f) {
	return null;
    }

    /**
     * Adds a human readable description of the file.
     */
    public void putDescription(File f, String fileDescription) {
	fileDescriptions.put(fileDescription, f);
    }

    /**
     * A human readable description of the file.
     *
     * @see FileView#getDescription
     */
    public String getDescription(File f) {
	return (String) fileDescriptions.get(f);
    }

    /**
     * Adds a human readable type description for files. Based on "dot"
     * extension strings, e.g: ".gif". Case is ignored.
     */
    public void putTypeDescription(String extension, String typeDescription) {
	typeDescriptions.put(typeDescription, extension);
    }

    /**
     * Adds a human readable type description for files of the type of
     * the passed in file. Based on "dot" extension strings, e.g: ".gif".
     * Case is ignored.
     */
    public void putTypeDescription(File f, String typeDescription) {
	putTypeDescription(getExtension(f), typeDescription);
    }

    /**
     * A human readable description of the type of the file.
     *
     * @see FileView#getTypeDescription
     */
    public String getTypeDescription(File f) {

	return (String) typeDescriptions.get(getExtension(f));
    }

    /**
     * Conveinience method that returnsa the "dot" extension for the
     * given file.
     */
    public String getExtension(File f) {
	String name = f.getName();
	if(name != null) {
	    int extensionIndex = name.lastIndexOf('.');
	    if(extensionIndex < 0) {
		return null;
	    }
	    return name.substring(extensionIndex+1).toLowerCase();
	}
	return null;
    }

    /**
     * Adds an icon based on the file type "dot" extension
     * string, e.g: ".gif". Case is ignored.
     */
    public void putIcon(String extension, Icon icon) {
	icons.put(extension, icon);
    }

    /**
     * Icon that reperesents this file. Default implementation returns
     * null. You might want to override this to return something more
     * interesting.
     *
     * @see FileView#getIcon
     */
    public Icon getIcon(File f) {
	Icon icon = null;
	String extension = getExtension(f);
	if(extension != null) {
	    icon = (Icon) icons.get(extension);
	}
	return icon;
    }

    /**
     * Whether the file is hidden or not. This implementation returns
     * true if the filename starts with a "."
     *
     * @see FileView#isHidden
     */
    public Boolean isHidden(File f) {
	String name = f.getName();
	if(name != null && !name.equals("") && name.charAt(0) == '.') {
	    return Boolean.TRUE;
	} else {
	    return Boolean.FALSE;
	}
    }

    /**
     * Whether the directory is traversable or not. Generic implementation
 returns true for all directories.

 You might want to subtype CDFE_ExampleFileView to do somethimg more interesting,
 such as recognize compound documents directories; in such a case you might
 return a special icon for the diretory that makes it look like a regular
 document, and return false for isTraversable to not allow users to
 descend into the directory.
     *
     * @see FileView#isTraversable
     */
    public Boolean isTraversable(File f) {

	if (f.getName().toLowerCase().endsWith(".cdf")) {
		return Boolean.FALSE;
	} else {
	  if (f.isDirectory()) {
	    return Boolean.TRUE;
	  } else {
	    return Boolean.FALSE;
	  }
	}
    }

}
