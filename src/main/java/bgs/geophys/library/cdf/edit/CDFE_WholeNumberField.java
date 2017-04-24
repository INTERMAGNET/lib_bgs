package bgs.geophys.library.cdf.edit;

// $Id: CDFE_WholeNumberField.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*; 
import javax.swing.text.*; 

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * This is an extension from the Java tutorial that can be found at
 * <a href="http://java.sun.com">JavaSoft</a>
 */
public class CDFE_WholeNumberField extends CDFE_NumberField {

    
    private boolean parseNegative = true;
    
    /**
       * Get the value of parseNegative.
       * @return Value of parseNegative.
       */
    public boolean isParseNegative() {return parseNegative;}
    
    /**
       * Set the value of parseNegative.
       * @param v  Value to assign to parseNegative.
       */
    public void setParseNegative(boolean  v) {this.parseNegative = v;}
    
    static final long serialVersionUID = 1L;

    public CDFE_WholeNumberField(int value, int columns) {
	super(columns);
        formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setParseIntegerOnly(true);
	setText(new Long(value).toString());
    }

    public Document createDefaultModel() {
	return new PlainDocument() {

            static final long serialVersionUID = 1L;

	    public void insertString(int offs, String str, AttributeSet a) 
		throws BadLocationException {
		

		char[] text   = this.getText(0, getEndPosition().getOffset()).
		    toCharArray();
		char[] source = str.toCharArray();
		char[] result = new char[source.length];
		int j = 0;
		
		for (int i = 0; i < result.length; i++) {
		    if (Character.isDigit(source[i]) || 
			(isParseNegative() && 
			 (offs == 0) && (source[i] == '-')))
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
}
