/*
 * PreciseDecimal.java
 * 
 * Created 2009-08-31
 */

package bgs.geophys.library.Misc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * PreciseDecimal extends BigDecimal and makes it simpler to work with.
 * BigDecimal is a class for working in arbitrarily high numerical
 * precision.  This class provides arithmetic operations to a
 * fixed accuracy of 18 decimal places.
 *
 * Working with PreciseDecimal is obviously slower than working with
 * double or float primitives, but is necessary when numerical
 * accuracy is important.
 * 
 * For example, with primitive doubles, you will find that
 * (0.1 + 0.1 + 0.1 + 0.1 + 0.1 + 0.1 + 0.1 + 0.1 + 0.1 + 0.1) =
 * 0.9999999999999999, while the same computation using PreciseDecimal
 * will yield the expected result of 1.0
 * 
 * @author ewan
 */
public class PreciseDecimal extends BigDecimal {

    /** The formatting object used to create string representations
     *  of floating point numbers to 18 d.p. precision.
     *  The rounding mode used is the default for NumberFormat,
     *  which is HALF_EVEN */
    private static final NumberFormat DEFAULT_DECIMAL_FORMAT =
    new DecimalFormat ("#.0#################");
    
    /** Instantiate a new PreciseDecimal from a double */
    public PreciseDecimal(double val)
        { super(DEFAULT_DECIMAL_FORMAT.format(val)); }
    /** Instantiate a new PreciseDecimal from a long */
    public PreciseDecimal(long val)
        { super(DEFAULT_DECIMAL_FORMAT.format(val)); }
    /** Instantiate a new PreciseDecimal from an int */
    public PreciseDecimal(int val)
        { super(DEFAULT_DECIMAL_FORMAT.format(val)); }
    /** Instantiate a new PreciseDecimal from a BigDecimal,
     *  converting to the PreciseDecimal default precision
     *  of 18 d.p. in the process. */
    private PreciseDecimal(BigDecimal bd)
        { super(DEFAULT_DECIMAL_FORMAT.format(bd.doubleValue())); }
    
    /** A PreciseDecimal representation of the constant value '0' */
    public static final PreciseDecimal ZERO = new PreciseDecimal (0);
    
    /** Compute the sum of two double values, precise to 18 d.p.
     * @param a the addend
     * @param b the augend
     * @return a + b, precise to 18 d.p. */
    public static PreciseDecimal add(double a, double b) {
        return add(new PreciseDecimal(a), b);
    }
    
    public static PreciseDecimal add(PreciseDecimal a, double b) {
        return add(a, new PreciseDecimal(b));
    }
    
    public static PreciseDecimal add(PreciseDecimal a, PreciseDecimal b) {
        if (a == null) return (b == null) ? ZERO : b;
        // Use the add method of the superclass to perform the computation
        return new PreciseDecimal( a.add(b) );
    }
    
    /** Compute the difference of two double values, precise to 18 d.p.
     * @param a the minuend
     * @param b the subtrahend
     * @return a - b, precise to 18 d.p. */
    public static PreciseDecimal subtract(double a, double b) {
        return subtract(new PreciseDecimal(a), b);
    }
    
    public static PreciseDecimal subtract(PreciseDecimal a, double b) {
        return subtract(a, new PreciseDecimal(b));
    }
    
    public static PreciseDecimal subtract(PreciseDecimal a, PreciseDecimal b) {
        if (a == null) return (b == null) ? ZERO : PreciseDecimal.subtract(ZERO, b);
        // Use the subtract method of the superclass to perform the computation
        return new PreciseDecimal( a.subtract(b) );
    }
    
    /** Compute the product of two double values, precise to 18 d.p.
     * @param a the multiplier
     * @param b the multiplicand
     * @return a * b, precise to 18 d.p. */
    public static PreciseDecimal multiply(double a, double b) {
        return multiply(new PreciseDecimal(a), b);
    }
    
    public static PreciseDecimal multiply(PreciseDecimal a, double b) {
        return multiply(a, new PreciseDecimal(b));
    }
    
    public static PreciseDecimal multiply(PreciseDecimal a, PreciseDecimal b) {
        if (a == null || b == null) return ZERO;
        // Use the multiply method of the superclass to perform the computation
        return new PreciseDecimal( a.multiply(b) );
    }
    
    /** Compute the quotient of two double values, precise to 18 d.p.
     * @param a the dividend
     * @param b the divisor
     * @return a / b, precise to 18 d.p. */
    public static PreciseDecimal divide(double a, double b) {
        return divide(new PreciseDecimal(a), b);
    }
    
    public static PreciseDecimal divide(PreciseDecimal a, double b) {
        return divide(a, new PreciseDecimal(b));
    }
    
    public static PreciseDecimal divide(PreciseDecimal a, PreciseDecimal b) {
        if (b == null) throw new ArithmeticException("Division by zero");
        if (a == null) return ZERO;
        // Use the divide method of the superclass to perform the computation
        return new PreciseDecimal( a.divide(b, RoundingMode.HALF_EVEN) );
    }
    
    /** Create an array of PreciseDecimal objects from an array of double values
     * @param array an array of double values
     * @return an array of PreciseDecimal representations of the input
     * array, precise to 18 d.p. */
    public static PreciseDecimal [] arrayFromDouble(double [] array){
        PreciseDecimal [] bdArray = new PreciseDecimal [array.length];
        for (int i=0; i < array.length; i++) {
            bdArray[i] = new PreciseDecimal(array[i]);
        }
        return bdArray;
    }
    
    /** Create an array of double values from an array of PreciseDecimal objects
     * @param array an array of PreciseDecimal objects
     * @return an array of double values */
    public static double [] arrayToDouble(PreciseDecimal [] array){
        double [] bdArray = new double [array.length];
        for (int i=0; i < array.length; i++) {
            bdArray[i] = array[i].doubleValue();
        }
        return bdArray;
    }
}
