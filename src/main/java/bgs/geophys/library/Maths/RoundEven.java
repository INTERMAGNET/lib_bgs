package bgs.geophys.library.Maths;

import java.math.BigDecimal;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * Perform accurate HALF_EVEN rounding (Banker's rounding) on
 * double values with a relatively low number of significant
 * figues (e.g. kind of values used in geomag applications).
 *
 * Has been tested to perform accurate rounding to between 0 and
 * TWO_STEP_MAX_PRECISION decimal places on the results of mean calculations
 * performed on data sets of up to 10,000 values, with each value of up to
 * TWO_STEP_MAX_PRECISION decimal places and between
 * -10,000.000000 and 10,000.000000
 *
 * The result can be returned as a string, a double, or a BigDecimal.
 * 
 * @author Ewan Dawson
 */
public final class RoundEven {  // final allows for optimization in JVM
    
    public static final int TWO_STEP_MAX_PRECISION = 5;
    public static final double TWO_STEP_EPSILON = 1E-11;


    /**
     * Accurately round floating-point value value to n
     * decimal places, using half even rounding.
     * This method returns a double, so you may not get the result
     * you expected if the true rounded value cannot be accurately
     * represented as a 32-bit floating point value.
     *
     * @param value      The floating-point value to be rounded.
     * @param precision  The number of decimal places to round to.
     * @return  The value rounded to precision decimal places.
     */
    public static final double toDouble(double value, int precision) {
        return doRounding(value, precision).doubleValue();
    }
    
    /**
     * Accurately round floating-point value value to n
     * decimal places, using half even rounding.
     * This method returns a string representation of the rounded value.
     *
     * @param value     The floating-point value to be rounded.
     * @param precision  The number of decimal places to round to.
     * @return      The value rounded to precision decimal places.
     */
    public static final String toString(double value, int precision) {
        return doRounding(value, precision).toString();
    }
    
    /**
     * Accurately round floating-point value value to n
     * decimal places, using half even rounding.
     * This method returns the rounded value in a BigDecimal object.
     *
     * @param value     The floating-point value to be rounded.
     * @param precision  The number of decimal places to round to.
     * @return      The value rounded to precision decimal places.
     */
    public static final BigDecimal toBigDecimal(double value, int precision) {
        return doRounding(value, precision);
    }
    
    /**
     * Perform the rounding, using BigDecimal.  A two-step
     * rounding algorithm is used, which is necessary when using
     * performing half-even rounding due to the difficulty in
     * representing decimal numbers precisely as 64-bit binary
     * floating point values.
     *
     * @param value     The floating-point value to be rounded.
     * @param precision  The number of decimal places to round to.
     * @return      The value value rounded to precision decimal places.
     */
    private static final BigDecimal doRounding(double value, int precision){

        BigDecimal result;

        // This algorithm does not offer accuracy beyond a certain precision,
        // which I have conservatively chosen to be MAX_PRECISION decimal places.
        if ( precision < 0 || TWO_STEP_MAX_PRECISION < precision ) {
            throw new IllegalArgumentException(
                "Invalid precision: " + precision + ". Precision must be between 0 and " + TWO_STEP_MAX_PRECISION
            );
        }

        // Convert the value to BigDecimal, and compute the intermediate rounded value.
        BigDecimal original = new BigDecimal(value);
        BigDecimal intermed = original.setScale(precision+1, HALF_EVEN);

        // If the difference of the original and intermediate values is
        // greater than EPSILON, we must discard the intermediate value and
        // revert to one stage rounding.
        if ( original.subtract(intermed).abs().doubleValue() >= TWO_STEP_EPSILON )
            result = original.setScale(precision, HALF_EVEN);
        else
            result = intermed.setScale(precision, HALF_EVEN);

        return result;
    }
}
