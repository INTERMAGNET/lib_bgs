package bgs.geophys.library.cdf.edit;

//$Id: CDFE_FloatNumberField.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*; 
import javax.swing.text.*; 

import java.text.ParseException;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * A JTextField that only accpets valid floating point numbers
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 *
 */
public class CDFE_FloatNumberField extends CDFE_NumberField {
    
    static final long serialVersionUID = 1L;

    /**
     * Construct a FloatNumberField containing the given value that is
     * the specified width.
     *
     * @param value the default value
     * @param columns the width of the text field in characters.
     */
    public CDFE_FloatNumberField(float value, int columns) {
	super(columns);
        formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setParseIntegerOnly(false);
	formatter.setMaximumIntegerDigits(5);
	formatter.setMaximumFractionDigits(2);
	setText(new Float(value).toString());
    }

    /**
     * Creates the Document used by this CDFE_FloatNumberField.
     */
    public Document createDefaultModel() {
	return new PlainDocument() {

            static final long serialVersionUID = 1L;

	    public void insertString(int offs, String str, AttributeSet a) 
		throws BadLocationException {
		
		String text = this.getText(0, getLength());
		String bOff = text.substring(0, offs);
		String aOff = text.substring(offs, text.length());
		StringBuffer testText = new StringBuffer();
		testText.append(bOff);
		testText.append(str);
		testText.append(aOff);

		try {
		    formatter.parse(testText.toString());
		    super.insertString(offs, str, a);
		} catch (ParseException e) {
		    java.awt.Toolkit.getDefaultToolkit().beep();
		}
	    }
	};
    }
		    
    /**
     * DO NOT USE (for development only)
     */
    public Document createDefaultModelOLD() {
	return new PlainDocument() {

            static final long serialVersionUID = 1L;

	    public void insertString(int offs, String str, AttributeSet a) 
		throws BadLocationException {

		char[] text   = this.getText(0, getEndPosition().getOffset()).toCharArray();
		char[] source = str.toCharArray();
		char[] result = new char[source.length];
		int j = 0;
		
		// Check to see if a decimal point is present
		boolean decimal = false;
		for (int i = 0; i < text.length; i++)
		    if (text[i] == '.') {
			decimal = true;
			break;
		    }

		// Check to see if a decimal point is present
		boolean exponent = false;
		for (int i = 0; i < text.length; i++)
		    if (text[i] == 'E') {
			exponent = true;
			break;
		    }

		for (int i = 0; i < result.length; i++) {
		    if (Character.isDigit(source[i]) || 
			((source[i] == '.') && (!decimal)) ||
			((offs != 0) && (!exponent) && (source[i] == 'E')) ||
			((offs == 0) && (source[i] == '-')))
			result[j++] = source[i];
		    else {
			java.awt.Toolkit.getDefaultToolkit().beep();
		    }
		}

		super.insertString(offs, new String(result, 0, j), a);
	    }
	};
    }

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    public  Integer getInteger() {
	try {
	    return new Integer(formatter.parse(getText()).intValue());
	} catch (ParseException e) {
	    return null;
	}
    }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    public  Long getLong() {
	try {
	    return new Long(formatter.parse(getText()).longValue());
	} catch (ParseException e) {
	    return null;
	}
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public  Float getFloat() {
	try {
	    return new Float(formatter.parse(getText()).floatValue());
	} catch (ParseException e) {
	    return null;
	}
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    public  Double getDouble() {
	try {
	    return new Double(formatter.parse(getText()).doubleValue());
	} catch (ParseException e) {
	    return null;
	}
    }

} // CDFE_FloatNumberField
