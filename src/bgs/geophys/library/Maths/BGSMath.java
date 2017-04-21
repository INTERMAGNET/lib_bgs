/*
 * BGSMath.java
 *
 * Created on 07 March 2007, 11:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Maths;

/**
 * Some additional math functions not provided in java.lang.Math
 *
 * @author smf
 */
public class BGSMath
{

    // test code for the rounding routines
//    public static void main (String args[])
//    {
//        int count;
//        double values [] = {-3.5, -2.5, -1.5, -1.1, -0.9, -0.5, -0.1, 0.0, 0.1, 0.5, 0.9, 1.1, 1.5, 2.5, 3.5};
//        
//        for (count=0; count<values.length; count++)
//        {
//            System.out.println ("  Ceiling " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_CEILING));
//            System.out.println ("     Down " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_DOWN));
//            System.out.println ("    Floor " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_FLOOR));
//            System.out.println ("Half down " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_HALF_DOWN));
//            System.out.println ("Half even " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_HALF_EVEN));
//            System.out.println ("  Half up " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_HALF_UP));
//            System.out.println ("       Up " + Double.toString(values[count]) + ": " + BGSMath.round(values[count], BGSMath.ROUND_UP));
//        }
//    }
    
    /** Rounding mode to round towards positive infinity. */
    public static final int ROUND_CEILING = 1;
    /** Rounding mode to round towards zero. */
    public static final int ROUND_DOWN = 2;
    /** Rounding mode to round towards negative infinity. */
    public static final int ROUND_FLOOR = 3;
    /** Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant, in which case round down. */
    public static final int ROUND_HALF_DOWN = 4;
    /** Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant, in which case, round towards the even neighbor.  */
    public static final int ROUND_HALF_EVEN = 5;
    /**  Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant, in which case round up.  */
    public static final int ROUND_HALF_UP = 6;
    /** Rounding mode to assert that the requested operation has an exact result, hence no rounding is necessary.  */
    public static final int ROUND_UNNECESSARY = 7;
    /** Rounding mode to round away from zero. */
    public static final int ROUND_UP = 8;
 
    /** rounding with control of how the rounding occurs, as in BigDecimal */
    public static long round (double value, int method)
    {
        double fraction;
        
        switch (method)
        {
            case ROUND_CEILING:
                if (value > 0.0) return round (value, ROUND_UP);
                return round (value, ROUND_DOWN);
                
            case ROUND_DOWN:
                return (long) value;
                
            case ROUND_FLOOR:
                if (value > 0.0) return round (value, ROUND_DOWN);
                return round (value, ROUND_UP);
                
            case ROUND_HALF_DOWN:
                fraction = value - Math.floor(value);
                if (fraction == 0.5) return (long) value;
                return Math.round (value);
                
            case ROUND_HALF_EVEN:
                return (long) Math.rint (value);
                
            case ROUND_HALF_UP:
                fraction = value - Math.floor(value);
                if (fraction != 0.5) return Math.round (value);
                if (value < 0.0) return (long) (value - 1.0);
                return (long) (value + 1.0);
                
            case ROUND_UNNECESSARY:
                fraction = value - Math.floor(value);
                if (fraction != 0.0) throw new ArithmeticException ();
                return (long) value;
                
            case ROUND_UP:
                fraction = value - Math.floor(value);
                if (fraction == 0.0) return (long) value;
                if (value > 0.0) return (long) (value + 1.0);
                return (long) (value - 1.0);
        }
        
        throw new ArithmeticException ();
    }
    
    /** rounding with control of how the rounding occurs, as in BigDecimal */
    public static int round (float value, int method)
    {
        float fraction;
        
        switch (method)
        {
            case ROUND_CEILING:
                if (value > 0.0) return round (value, ROUND_UP);
                return round (value, ROUND_DOWN);
            case ROUND_DOWN:
                return (int) value;
            case ROUND_FLOOR:
                if (value > 0.0) return round (value, ROUND_DOWN);
                return round (value, ROUND_UP);
            case ROUND_HALF_DOWN:
                fraction = value - (float) Math.floor((double) value);
                if (fraction == 0.5) return (int) value;
                return Math.round (value);
            case ROUND_HALF_EVEN:
                return (int) Math.rint ((double) value);
            case ROUND_HALF_UP:
                return Math.round (value);
            case ROUND_UNNECESSARY:
                fraction = value - (float) Math.floor ((double) value);
                if (fraction != 0.0) throw new ArithmeticException ();
                return (int) value;
            case ROUND_UP:
                fraction = value - (float) Math.floor ((double) value);
                if (fraction > 0.0) return (int) (value + 1.0);
                return (int) value;
        }
        
        throw new ArithmeticException ();
    }

    /** radius of the earth in kilometres */
    public static final double EARTH_RADIUS_KM = 6378.7;
    /** radius of the earth in nautical miles */
    public static final double EARTH_RADIUS_NM = 3437.74677;
    /** radius of the earth in statute miles */
    public static final double EARTH_RADIUS_SM = 3963.0;
    /* constant to convert angle in degrees to angle in radians */
    public static final double DEGREES_TO_RADIANS = 57.2958;

    /** calculate the distance along a great circle between two positions on the earth
     * @param radius the radius to use
     * @param lat1 the latitude of the first position in degrees
     * @param lon1 the longitude of the first position in degrees
     * @param lat2 the latitude of the second position in degrees
     * @param lon2 the longitude of the second position in degrees
     * @return the distance in the same units as the radius */
    public static double greatCircleDistance (double radius, double lat1, double lon1, double lat2, double lon2)
    {
        lat1 /= DEGREES_TO_RADIANS;
        lon1 /= DEGREES_TO_RADIANS;
        lat2 /= DEGREES_TO_RADIANS;
        lon2 /= DEGREES_TO_RADIANS;
        return radius * Math.acos ((Math.sin (lat1) * Math.sin (lat2)) + 
                                   (Math.cos (lat1) * Math.cos (lat2) * Math.cos (lon2 - lon1)));

        
    }
}
