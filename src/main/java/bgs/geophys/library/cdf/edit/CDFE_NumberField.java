package bgs.geophys.library.cdf.edit;

//$Id: CDFE_NumberField.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.text.Document;
import java.text.NumberFormat;
import javax.swing.JTextField;

/**
 * Provides similiar methods as java.lang.Number within the context of 
 * a JTextField.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public abstract class CDFE_NumberField extends JTextField {
    
    public NumberFormat formatter;

    public CDFE_NumberField(int columns) { super(columns); }

    /**
     * Returns the value of the specified number as an 
     * <code>java.lang.Integer</code>. This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>java.lang.Integer</code>.
     */
    public abstract Integer getInteger();

    /**
     * Returns the value of the specified number as a 
     * <code>java.lang.Long</code>. This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>java.lang.Long</code>.
     */
    public abstract Long getLong();

    /**
     * Returns the value of the specified number as a 
     * <code>java.lang.Float</code>. This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public abstract Float getFloat();

    /**
     * Returns the value of the specified number as a
     * <code>java.lang.Double</code>. This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    public abstract Double getDouble();

    /**
     * Returns the value of the specified number as a
     * <code>java.lang.Byte</code>. This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>java.lang.Byte</code>.
     */
    public Byte getByte() {
	return new Byte(getInteger().byteValue());
    }

    /**
     * Returns the value of the specified number as a
     * <code>java.lang.Short</code>. This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>java.lang.Short</code>.
     */
    public Short getShort() {
	return new Short(getInteger().shortValue());
    }

    /**
     * Returns the value of the specified number as a <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    public int    intValue()    { return getInteger().intValue(); }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    public long   longValue()   { return getLong().longValue(); }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public float  floatValue()  { return getFloat().floatValue(); }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    public double doubleValue() { return getDouble().doubleValue(); }

    /**
     * Returns the value of the specified number as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     */
    public byte   byteValue()   { return getInteger().byteValue(); }

    /**
     * Returns the value of the specified number as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
     */
    public short  shortValue()  { return getInteger().shortValue(); }


    public abstract Document createDefaultModel();
} // CDFE_NumberField
