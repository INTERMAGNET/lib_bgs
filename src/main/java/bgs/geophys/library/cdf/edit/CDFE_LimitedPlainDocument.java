package bgs.geophys.library.cdf.edit;

// $Id: CDFE_LimitedPlainDocument.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * The ever present extension to PlainDocument to limit the number of 
 * charaters in a text field.  Checkout the Java tutorial for more info
 */
public class CDFE_LimitedPlainDocument extends PlainDocument {
    
    static final long serialVersionUID = 1L;

    CDFE_LimitedPlainDocument(int columns) {
	super();
	putProperty("columns", new Integer(columns));
    }

    public void insertString(int offs, String str, AttributeSet a)
    throws BadLocationException {
       // check if the new length (length of document +
       // length of string which will be inserted into the document)
       // exceeds the maximum number of characters
       if (str != null && 
	   getLength() + str.length() > ((Integer)getProperty("columns")).intValue()) {
           throw new BadLocationException("", offs);
       }
       else
           super.insertString(offs, str, a);
    }
}
