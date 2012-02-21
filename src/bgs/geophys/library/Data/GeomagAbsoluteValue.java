/*
 * GeomagAbsoluteValue.java
 *
 * Created on 16 January 2006, 09:46
 */

package bgs.geophys.library.Data;

import java.text.DecimalFormat;

/**
 * A class that holds a single, absolute geomagnetic data sample. All
 * angles are in radians, unless specified otherwise.
 *
 * @author  smf
 */
public class GeomagAbsoluteValue 
{
    
    /** code for data in XYZ orientation */
    public static final int ORIENTATION_XYZ = 1;
    /** code for data in HDZ orientation */
    public static final int ORIENTATION_HDZ = 2;
    /** code for data in DIF orientation */
    public static final int ORIENTATION_DIF = 3;
    /** code for data in an unknown orientation */
    public static final int ORIENTATION_UNKNOWN = 4;   
    
    /** code for angles in radians */
    public static final int ANGLE_RADIANS = 1;
    /** code for angles in degrees */
    public static final int ANGLE_DEGREES = 2;
    /** code for angles in degrees */
    public static final int ANGLE_MINUTES = 3;
    
    /** code for unknown component */
    public static final int COMPONENT_UNKNOWN = -1;
    /** code for X component */
    public static final int COMPONENT_X = 1;
    /** code for Y component */
    public static final int COMPONENT_Y = 2;
    /** code for Z component */
    public static final int COMPONENT_Z = 3;
    /** code for H component */
    public static final int COMPONENT_H = 4;
    /** code for D component */
    public static final int COMPONENT_D = 5;
    /** code for I component */
    public static final int COMPONENT_I = 6;
    /** code for F (vector) component */
    public static final int COMPONENT_F = 7;
    /** code for F (scalar) component */
    public static final int COMPONENT_F_SCALAR = 8;
    /** code for F (difference) component */
    public static final int COMPONENT_F_DIFF = 9;
    /** code for no F component */
    public static final int COMPONENT_F_UNRECORDED = 13;
    /** code for original 1st component (native) */
    public static final int COMPONENT_NATIVE_1 = 10;
    /** code for original 2nd component (native) */
    public static final int COMPONENT_NATIVE_2 = 11;
    /** code for original 3rd component (native) */
    public static final int COMPONENT_NATIVE_3 = 12;
    
    // the vector components
    protected double X, Y, Z, H, D, I, F;
    // the scalar and derived components
    protected double FScalar, FDiff;
    
    // flags to show which components have been calculated
    protected boolean X_ok, Y_ok, Z_ok, H_ok, D_ok, I_ok, F_ok, FScalar_ok, FDiff_ok;

    // which type of F data
    protected int FType;
    
    // the original orientation code
    protected int native_orientation;
    
    /** value returned when data is missing (can be changed) */
    protected double missingDataValue;

    //value returned when the component is not recorded
    protected double missingComponentValue;
    
    // constants to convert various angular measures
    protected static double degrees_to_radians;
    protected static double minutes_to_radians;
    protected static double radians_to_degrees;
    protected static double radians_to_minutes;
    
    // formatters for field strengths and angles
    private static DecimalFormat fieldStrengthFormat = new DecimalFormat ("#####0.0");
    private static DecimalFormat angleMinutesFormat = new DecimalFormat ("#####0.00");
    private static DecimalFormat angleDegreesFormat = new DecimalFormat ("###0.0000");
    private static DecimalFormat angleRadiansFormat = new DecimalFormat ("#0.000000");
    
    // initialise static members
    static
    {
        degrees_to_radians = StrictMath.PI / 180.0;
        minutes_to_radians = degrees_to_radians / 60.0;
        radians_to_degrees = 1.0 / degrees_to_radians;
        radians_to_minutes = 1.0 / minutes_to_radians;
    }
    
    /** Creates a new empty instance of GeomagAbsoluteValue */
    public GeomagAbsoluteValue ()
    {
        this.native_orientation = ORIENTATION_UNKNOWN;
        missingDataValue = 99999.9;
        missingComponentValue = 88888.8;
        X_ok = Y_ok = Z_ok = false;
        H_ok = D_ok = I_ok = F_ok = false;
        FScalar_ok = FDiff_ok = false;
    }
    
    /** Creates a new empty instance of GeomagAbsoluteValue */
    public GeomagAbsoluteValue (int orient)
    {
        this.native_orientation = orient;
        missingDataValue = 99999.9;
        missingComponentValue = 88888.8;
        X_ok = Y_ok = Z_ok = false;
        H_ok = D_ok = I_ok = F_ok = false;
        FScalar_ok = FDiff_ok = false;
    }
    
    /** Creates an instance of GeomagAbsoluteValue where all elements are already known */
    public GeomagAbsoluteValue (double x, double y, double z, double f, double h, 
                                double d, double i, double f_scalar, double f_diff, 
                                double missingDataValue, int orientation, int angle_units)
    {
        this.missingDataValue = missingDataValue;
        missingComponentValue = 88888.8;
        this.native_orientation = orientation;
        X_ok = Y_ok = Z_ok = H_ok = D_ok = I_ok = F_ok = FScalar_ok = FDiff_ok = true;
        if (x == missingDataValue || x==missingComponentValue ) X_ok = false;
        else this.X = x;
        if (y == missingDataValue || y==missingComponentValue ) Y_ok = false;
        else this.Y = y;
        if (z == missingDataValue || z==missingComponentValue ) Z_ok = false;
        else this.Z = z;
        if (f == missingDataValue ||f==missingComponentValue ) F_ok = false;
        else this.F = f;
        if (h == missingDataValue||h==missingComponentValue ) H_ok = false;
        else this.H = h;
        if (d == missingDataValue||d==missingComponentValue ) D_ok = false;
        else
        {
            switch (angle_units)
            {
                case ANGLE_DEGREES: this.D = d * degrees_to_radians; break;
                case ANGLE_MINUTES: this.D = d * minutes_to_radians; break;
                case ANGLE_RADIANS: this.D = d; break;
                default:
                    throw new IllegalArgumentException ("Bad geomagnetic angular measure code");
            }
        }
        if (i == missingDataValue || i==missingComponentValue ) I_ok = false;
        else
        {
            switch (angle_units)
            {
                case ANGLE_DEGREES: this.I = i * degrees_to_radians; break;
                case ANGLE_MINUTES: this.I = i * minutes_to_radians; break;
                case ANGLE_RADIANS: this.I = i; break;
                default:
                    throw new IllegalArgumentException ("Bad geomagnetic angular measure code");
            }
        }
        if (f_scalar == missingDataValue ||f_scalar==missingComponentValue ) FScalar_ok = false;
        else this.FScalar = f_scalar;
        if (f_diff == missingDataValue|| f_diff==missingComponentValue ) FDiff_ok = false;
        else this.FDiff = f_diff;
    }
    
    /** Creates a new instance of GeomagAbsoluteValue from absolute vector
     * and scalar data
     * @param comp1 - X, H or D
     * @param comp2 - Y, D or I
     * @param comp3 - Z, Z or F
     * @param FScalar F from a proton (or other independant instrument)
     * @param FScalarType the type of F scalar data - choose from:
     *        COMPONENT_F - FScalar is actually vector, so ignore it
     *        COMPONENT_F_SCALAR - FScalar contains scalar data
     *        COMPONENT_F_DIFF - FScalar contains an F difference value
     * @param missingValue the value used to represent missing data
     * @param orientation code for the orientation - ORIENTATION_XYZ, ORIENTTATION_HDZ or ORIENTTATION_DIF
     * @param angle_units - D and I (if used) are in ANGLE_RADIANS, ANGLE_DEGREES or ANGLE_MINUTES */
    public GeomagAbsoluteValue (double comp1, double comp2, double comp3,
                                double FScalar, int FScalarType, double missingDataValue,
                                int orientation, int angle_units) 
    {
        this (comp1, comp2, comp3, missingDataValue, orientation, angle_units);
        setF(FScalar, FScalarType);
        
    }
    public GeomagAbsoluteValue (double comp1, double comp2, double comp3,
                                double FScalar, int FScalarType, double missingDataValue,
                                double missingComponentValue,
                                int orientation, int angle_units)
    {
        this (comp1, comp2, comp3, missingDataValue, orientation, angle_units);
        this.missingComponentValue = missingComponentValue;
        setF(FScalar, FScalarType);

    }

    public void setF(double FScalar, int FScalarType){
    this.FType = FScalarType;
    switch (FScalarType)
        {
            case COMPONENT_F_DIFF:   // used after 2009
                if (FScalar == missingDataValue ||FScalar==missingComponentValue){
                    FScalar_ok = false;
                    FDiff_ok = false;
                }
                else if (FScalar < -10000.0) 
                {
                    this.FScalar = - FScalar;
                    FScalar_ok = true;
                    FDiff_ok = false;
                }
                else  //it's a genuine FDiff value
                {
                    FDiff_ok = true;
                    this.FDiff = FScalar;
                    getF();
                    if (F_ok)
                    {
                        // F(s)      = F(v) - dF
                        this.FScalar = F - FScalar;
                        FScalar_ok = true;
                    }
                    else {
                        FScalar_ok = false;
                        FDiff_ok = false;
                    }
                }
                break;
            case COMPONENT_F_SCALAR:
                if (FScalar == missingDataValue || FScalar==missingComponentValue) FScalar_ok = false;
                else
                {
                    this.FScalar = FScalar;
                    FScalar_ok = true;
                }
                FDiff_ok = false;
                break;
            default:  // unrecorded
                this.FScalar = this.missingComponentValue;
                this.FDiff = this.missingComponentValue;
                FScalar_ok = false;
                FDiff_ok = false; 
                break;
        }
    }

    /** Creates a new instance of GeomagAbsoluteValue from absolute vector data only
     * (no scalar data)
     * @param comp1 - X, H or D
     * @param comp2 - Y, D or I
     * @param comp3 - Z, Z or F
     * @param missingValue the value used to represent missing data
     * @param missingDataValue number indicating missing data
     * @param orientation code for the orientation - ORIENTATION_XYZ, ORIENTTATION_HDZ or ORIENTTATION_DIF
     * @param angle_units - D and I (if used) are in ANGLE_RADIANS, ANGLE_DEGREES or ANGLE_MINUTES */
    public GeomagAbsoluteValue (double comp1, double comp2, double comp3, 
                                double missingDataValue,
                                int orientation, int angle_units) 
    {
        this.missingDataValue = missingDataValue;
        missingComponentValue = 88888.8;
        FScalar_ok = FDiff_ok = false;
        switch (orientation)
        {
        case ORIENTATION_XYZ:
            X_ok = Y_ok = Z_ok = true;
            H_ok = D_ok = I_ok = F_ok = false;
            if (comp1 == missingDataValue ||comp1==missingComponentValue ) X_ok = false;
            else X = comp1;
            if (comp2 == missingDataValue|| comp2==missingComponentValue ) Y_ok = false;
            else Y = comp2;
            if (comp3 == missingDataValue|| comp3==missingComponentValue ) Z_ok = false;
            else Z = comp3;
            break;
        case ORIENTATION_HDZ:
            H_ok = D_ok = Z_ok = true;
            X_ok = Y_ok = I_ok = F_ok = false;
            if (comp1 == missingDataValue||comp1==missingComponentValue ) H_ok = false;
            else H = comp1;
            switch (angle_units)
            {
            case ANGLE_RADIANS:
                if (comp2 == missingDataValue||comp2==missingComponentValue ) D_ok = false;
                else D = comp2;
                break;
            case ANGLE_DEGREES:
                if (comp2 == missingDataValue||comp2==missingComponentValue) D_ok = false;
                else D = comp2 * degrees_to_radians;
                break;
            case ANGLE_MINUTES:
                if (comp2 == missingDataValue||comp2==missingComponentValue) D_ok = false;
                else D = comp2 * minutes_to_radians;
                break;
            default:
                throw new IllegalArgumentException ("Bad geomagnetic angular measure code");
            }
            if (comp3 == missingDataValue||comp3==missingComponentValue) Z_ok = false;
            else Z = comp3;
            break;
        case ORIENTATION_DIF:
            D_ok = I_ok = F_ok = true;
            X_ok = Y_ok = Z_ok = H_ok = false;
            switch (angle_units)
            {
            case ANGLE_RADIANS:
                if (comp1 == missingDataValue||comp1==missingComponentValue) D_ok = false;
                else D = comp1;
                if (comp2 == missingDataValue||comp2==missingComponentValue) I_ok = false;
                else I = comp2;
                break;
            case ANGLE_DEGREES:
                if (comp1 == missingDataValue||comp1==missingComponentValue) D_ok = false;
                else D = comp1 * degrees_to_radians;
                if (comp2 == missingDataValue||comp2==missingComponentValue) I_ok = false;
                else I = comp2 * degrees_to_radians;
                break;
            case ANGLE_MINUTES:
                if (comp1 == missingDataValue||comp1==missingComponentValue) D_ok = false;
                else D = comp1 * minutes_to_radians;
                if (comp2 == missingDataValue||comp2==missingComponentValue) I_ok = false;
                else I = comp2 * minutes_to_radians;
                break;
            default:
                throw new IllegalArgumentException ("Bad geomagnetic angular measure code");
            }
            if (comp3 == missingDataValue||comp3==missingComponentValue) F_ok = false;
            else F = comp3;
            break;
        default:
            throw new IllegalArgumentException ("Bad geomagnetic orientation code");
        }
        native_orientation = orientation;
    }

    /** Creates a new instance of GeomagAbsoluteValue from variometer data and
     * a baseline (no scalar data, no missing values)
     * @param comp1 - x or h
     * @param comp2 - y or d
     * @param comp3 - z
     * @param comp1_baseline - baseline for x or h
     * @param comp2_baseline - baseline for y or d
     * @param comp3_baseline - baseline for z
     * @param orientation code for the orientation - ORIENTATION_XYZ, ORIENTTATION_HDZ
     * @param angle_units - D baseline (if used) is in ANGLE_RADIANS, ANGLE_DEGREES or ANGLE_MINUTES */
    public GeomagAbsoluteValue (double comp1, double comp2, double comp3, 
                                double comp1_baseline, double comp2_baseline, double comp3_baseline,
                                int orientation, int angle_units) 
    {
        double h;

        //missingDataValue = Long.MAX_VALUE;
        missingDataValue = 99999.9;
        missingComponentValue = 88888.8;
        FScalar_ok = FDiff_ok = false;
        switch (orientation)
        {
        case ORIENTATION_XYZ:
            X_ok = Y_ok = Z_ok = true;
            H_ok = D_ok = I_ok = F_ok = false;
            if (comp1 == missingDataValue||comp1==missingComponentValue) X_ok = false;
            else X = comp1 + comp1_baseline;
            if (comp2 == missingDataValue||comp2==missingComponentValue) Y_ok = false;
            else Y = comp2 + comp2_baseline;
            if (comp3 == missingDataValue||comp3==missingComponentValue) Z_ok = false;
            else Z = comp3 + comp3_baseline;
            break;
        case ORIENTATION_HDZ:
            H_ok = D_ok = Z_ok = true;
            X_ok = Y_ok = I_ok = F_ok = false;
            // from CWT's notes : H (t) = sqrt (sqr(PV(t) + PB) + sqr(QV(t)))
            //                    D(t) = D0 + arctan (QV(t) / (PV(t) + PB))
            // where PV(t) = variation in h
            //       PB = h baseline
            //       QV(t) = variation in d
            if (comp1 == missingDataValue||comp1==missingComponentValue) H_ok = false;
            else{ h = comp1 + comp1_baseline;
                H = StrictMath.sqrt ((h * h) + (comp2 * comp2));
            }
            switch (angle_units)
            {
            case ANGLE_RADIANS:
                if (comp2 == missingDataValue||comp2==missingComponentValue) D_ok = false;
                else D = StrictMath.asin (comp2 / H) + comp2_baseline;
                break;
            case ANGLE_DEGREES:
                if (comp2 == missingDataValue||comp2==missingComponentValue) D_ok = false;
                else D = StrictMath.asin (comp2 / H) + (comp2_baseline * degrees_to_radians);
                break;
            case ANGLE_MINUTES:
                if (comp2 == missingDataValue||comp2==missingComponentValue) D_ok = false;
                else D = StrictMath.asin (comp2 / H) + (comp2_baseline * minutes_to_radians);
                break;
            default:
                throw new IllegalArgumentException ("Bad geomagnetic angular measure code");
            }
            if (comp3 == missingDataValue||comp3==missingComponentValue) Z_ok = false;
            else Z = comp3 + comp3_baseline;
            break;
        default:
            throw new IllegalArgumentException ("Bad geomagnetic orientation code");
        }
        native_orientation = orientation;
    }


    
    /** get the vector data that was originally supplied at
     * the time this object was created - this method is more
     * efficient than calling getComponent three times
     * @param include_f true to include the scalar f value
     * @param angle_units - D and I (if used) are in ANGLE_RADIANS, ANGLE_DEGREES or ANGLE_MINUTES
     * @return include_f == false: a three element array containing the original
     *         vector data, include_f == true, a four element array containing
     *         the original vector data and the original f scalar value.
     *         Angular values are always returned in radians */
    public double [] getNativeComponents (boolean include_f, int angle_units)
    {
        double comps [];
        
        if (include_f) comps = new double [4];
        else comps = new double [3];
        switch (native_orientation)
        {
            case ORIENTATION_XYZ:
                if (X_ok) comps [0] = X;
                else comps [0] = missingDataValue;
                if (Y_ok) comps [1] = Y;
                else comps [1] = missingDataValue;
                if (Z_ok) comps [2] = Z;
                else comps [2] = missingDataValue;
                break;
            case ORIENTATION_HDZ:
                if (H_ok) comps [0] = H;
                else comps [0] = missingDataValue;
                if (D_ok) 
                {
                    switch (angle_units)
                    {
                        case ANGLE_DEGREES: comps [1] = D * radians_to_degrees; break;
                        case ANGLE_MINUTES: comps [1] = D * radians_to_minutes; break;
                        default: comps [1] = D; break;
                    }
                }
                else comps [1] = missingDataValue;
                if (Z_ok) comps [2] = Z;
                else comps [2] = missingDataValue;
                break;
            case ORIENTATION_DIF:
                if (D_ok)
                {
                    switch (angle_units)
                    {
                        case ANGLE_DEGREES: comps [0] = D * radians_to_degrees; break;
                        case ANGLE_MINUTES: comps [0] = D * radians_to_minutes; break;
                        default: comps [0] = D; break;
                    }
                }
                else comps [0] = missingDataValue;
                if (I_ok)
                {
                    switch (angle_units)
                    {
                        case ANGLE_DEGREES: comps [1] = I * radians_to_degrees; break;
                        case ANGLE_MINUTES: comps [1] = I * radians_to_minutes; break;
                        default: comps [1] = I; break;
                    }
                }
                else comps [1] = missingDataValue;
                if (F_ok) comps [2] = F;
                else comps [2] = missingDataValue;
                break;
            default:
                comps [0] = comps [1] = comps [2] = missingDataValue;
                break;
        }
        if (include_f)
        {
            if (FScalar_ok) comps [3] = FScalar;
            else comps [3] = missingDataValue;
        }
        return comps;
    }
        
    public double getX ()
    {
        // have we already got X ??
        if (! X_ok)
        {
            // no, so we must have started with HDZ or DIF - do we have H ??
            if (! H_ok)
            {
                // no - then we must have D, I and F, so calculate H
                if (I_ok && F_ok)
                {
                    H = F * StrictMath.cos (I);
                    H_ok = true;
                }
            }
            
            // now we must have H and D, so we can calculate X
            if (H_ok && D_ok)
            {
                X = H * StrictMath.cos (D);
                X_ok = true;
            }
        }
        if (X_ok) return X;
        if (X==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }

    public double getY ()
    {
        // have we already got Y ??
        if (! Y_ok)
        {
            // no, so we must have started with HDZ or DIF - do we have H ??
            if (! H_ok)
            {
                // no - then we must have D, I and F, so calculate H
                if (I_ok && F_ok)
                {
                    H = F * StrictMath.cos (I);
                    H_ok = true;
                }
            }
            // now we must have H and D, so we can calculate Y
            if (H_ok && D_ok)
            {
                Y = H * StrictMath.sin (D);
                Y_ok = true;
            }
        }
        if (Y_ok) return Y;
        if (Y==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    public double getZ ()
    {
        // have we already got Z ??
        if (! Z_ok)
        {
            // no so we must have started with DIF
            if (F_ok && I_ok)
            {
                Z = F * StrictMath.sin (I);
                Z_ok = true;
            }
        }
        if (Z_ok) return Z;
        if (Z==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    public double getH ()
    {
        // have we already got H ??
        if (! H_ok)
        {
            // no, so we must have started with XYZ or DIF - do we have D ??
            if (! D_ok)
            {
                if (X_ok && Y_ok)
                {
                    H = StrictMath.sqrt((X * X) + (Y * Y));    // no, so we must have started with XYZ
                    H_ok = true;
                }
            }
            else
            {
                if (F_ok && I_ok)
                {
                    H = F * StrictMath.cos (I);                // yes, so we must have started with DIF
                    H_ok = true;
                }
            }
        }
        if (H_ok) return H;
        if (H==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    public double getD ()
    {
        // have we already got D ??
        if (! D_ok)
        {
            // then we must have started with XYZ
            if (Y_ok && X_ok)
            {
                D = StrictMath.atan2 (Y, X);
                D_ok = true;
            }
        }
        if (D_ok) return D;
        if (D==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    public double getI ()
    {
        // have we already got I ??
        if (! I_ok)
        {
            // then we must have started with XYZ or HDZ - do we have H
            if (! H_ok)
            {
                if (X_ok && Y_ok)
                {
                    // no - so we must have started with XYZ - calculate H
                    H = StrictMath.sqrt((X * X) + (Y * Y));
                    H_ok = true;
                }
            }
            if (Z_ok && H_ok)
            {
                I = StrictMath.atan2 (Z, H);
                I_ok = true;
            }
        }
        if (I_ok) return I;
        if (I==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    public double getF ()
    {
        // have we already got F ??
        if (! F_ok)
        {
            // then we must have started with XYZ or HDZ - do we have H ??
            if (! H_ok)
            {
                if (X_ok && Y_ok && Z_ok)
                {
                    F = StrictMath.sqrt ((X * X) + (Y * Y) + (Z * Z));  // no - then we must have X, Y and Z
                    F_ok = true;
                }
            }
            else
            {
                if (H_ok && Z_ok)
                {
                    F = StrictMath.sqrt ((H * H) + (Z * Z));            // yes - and we must have started with Z
                    F_ok = true;
                }
            }
        }
        if (F_ok) return F;
        if (F==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    // get F scalar value (if available)
    public double getFScalar ()
    {
        if (FScalar_ok) return FScalar;
        if (FScalar==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }
    
    // get F difference (If available)
    public double getFDiff ()
    {
        if (! FDiff_ok)
        {
            if (! F_ok) getF();
            if (F_ok && FScalar_ok)
            {
                FDiff = F - FScalar;
                FDiff_ok = true;
            }
        }
        if (FDiff_ok) return FDiff;
        if (FDiff==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }

    // get F difference (If available)
    //for IMF2009 - returns -FScalar if FDiff not defined
    public double getFDiffIAF2009 ()
    {
        if (! FDiff_ok)
        {
            if (! F_ok) getF();
            if (F_ok && FScalar_ok)
            {
                FDiff = F - FScalar;
                FDiff_ok = true;
            }
        }
        if (FDiff_ok) return FDiff;
        if(FScalar_ok) return -FScalar;
        if (FDiff==missingComponentValue||FScalar==missingComponentValue) return missingComponentValue;
        return missingDataValue;
    }

    /** get a component 
     * @param component_code code for the component to get
     * @param angle_units - D and I (if requested) are in ANGLE_RADIANS, ANGLE_DEGREES or ANGLE_MINUTES */
    public double getComponent (int component_code, int angle_units)
    {
        switch (component_code)
        {
            case COMPONENT_X:
                return getX();
            case COMPONENT_Y:
                return getY();
            case COMPONENT_Z: 
                return getZ();
            case COMPONENT_H: 
                return getH();
            case COMPONENT_D: 
                switch (angle_units)
                {
                    case ANGLE_DEGREES: return getDDegrees ();
                    case ANGLE_MINUTES: return getDMinutes();
                    case ANGLE_RADIANS: return getD();
                }
                break;
            case COMPONENT_I:
                switch (angle_units)
                {
                    case ANGLE_DEGREES: return getIDegrees ();
                    case ANGLE_MINUTES: return getIMinutes();
                    case ANGLE_RADIANS: return getI();
                }
                break;
            case COMPONENT_F: 
                return getF();
            case COMPONENT_F_SCALAR: 
                return getFScalar();
            case COMPONENT_F_DIFF: 
                return getFDiff();
            case COMPONENT_NATIVE_1:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: return getComponent (COMPONENT_D, angle_units);
                    case ORIENTATION_HDZ: return getH ();
                    case ORIENTATION_XYZ: return getX();
                }
                break;
            case COMPONENT_NATIVE_2:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: return getComponent (COMPONENT_I, angle_units);
                    case ORIENTATION_HDZ: return getComponent (COMPONENT_D, angle_units);
                    case ORIENTATION_XYZ: return getY();
                }
                break;
            case COMPONENT_NATIVE_3:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: return getF ();
                    case ORIENTATION_HDZ: return getZ ();
                    case ORIENTATION_XYZ: return getZ();
                }
                break;
        }
        return missingDataValue;
    }
    
    // utility routines to get angles in degrees or minutes
    public double getDDegrees () 
    {
        if (! D_ok) getD ();
        if (D_ok) return getD() * radians_to_degrees; 
        return missingDataValue;
    }
    public double getDMinutes () 
    {
        if (! D_ok) getD ();
        if (D_ok) return getD() * radians_to_minutes; 
        return missingDataValue;
    }
    public double getIDegrees () 
    {
        if (! I_ok) getI ();
        if (I_ok) return getI() * radians_to_degrees; 
        return missingDataValue;
    }
    public double getIMinutes () 
    {
        if (! I_ok) getI ();
        if (I_ok) return getI() * radians_to_minutes; 
        return missingDataValue;
    }

    // utility routines to maimipulate the number used to represent missing data
    public double getMissingDataValue() 
    { 
        return missingDataValue;  
    }
    public double getMissingComponentValue()
    {
        return missingComponentValue;
    }
    public void setMissingDataValue(double missingDataValue) 
    {
        this.missingDataValue = missingDataValue;
    }
    
    // utility routines to get information about the native orientation
    // of the data
    public int getNativeOrientation () 
    {
        return native_orientation; 
    }
    public String getNativeOrientationCode ()
    {
        return getNativeOrientationCode (false);
    }
    public String getNativeOrientationCode (boolean show_missing_f)
    {
        String string;
        
        switch (native_orientation)
        {
            case ORIENTATION_DIF: string = "DIF"; break;
            case ORIENTATION_HDZ: string = "HDZ"; break;
            case ORIENTATION_XYZ: string = "XYZ"; break;
            default: return "";
        }
        if (FScalar_ok) string += "F";
        else if (show_missing_f) string += "N";
        return string;
    }

    public boolean isComponentMissing (int comp)
    {
      if (comp==COMPONENT_X) return X_ok;
      if (comp==COMPONENT_Y) return Y_ok;
      if (comp==COMPONENT_Z) return Z_ok;
      if (comp==COMPONENT_H) return H_ok;
      if (comp==COMPONENT_D) return D_ok;
      if (comp==COMPONENT_I) return I_ok;
      if (comp==COMPONENT_F) return F_ok;
      if (comp == COMPONENT_F_SCALAR) return FScalar_ok;
      if (comp == COMPONENT_F_DIFF) return FDiff_ok;
      return true;
    }

    /** is the data completely missing */
    public boolean isMissing ()
    {
      if (X_ok) return false;
      if (Y_ok) return false;
      if (Z_ok) return false;
      if (H_ok) return false;
      if (D_ok) return false;
      if (I_ok) return false;
      if (F_ok) return false;
      if (FScalar_ok) return false;
      if (FDiff_ok) return false;
      return true;
    }
    
    /** get a string version of a component code
     * @param code one of the COMPONENT_xxx codes */
    public String getComponentName (int code)
    {
        String string;
        
        switch (resolveNative(code))
        {
            case COMPONENT_X: string = "X"; break;
            case COMPONENT_Y: string = "Y"; break;
            case COMPONENT_Z: string = "Z"; break;
            case COMPONENT_F: string = "F"; break;
            case COMPONENT_H: string = "H"; break;
            case COMPONENT_D: string = "D"; break;
            case COMPONENT_I: string = "I"; break;
            case COMPONENT_F_SCALAR: string = "F (scalar)"; break;
            case COMPONENT_F_DIFF: string = "F Difference"; break;
            default: string = "Unknown"; break;
        }
        return string;
    }
    
    /** get a string version of the appropriate units for any component
     * @param code one of the COMPONENT_xxx codes
     * @param angle_units one of the ANGLE_xxx codes
     * @param show_from true for abbreviated unit name */
    public String getUnitName (int code, int angle_units, boolean short_form)
    {
        String string;
        
        switch (resolveNative(code))
        {
            case COMPONENT_X:
            case COMPONENT_Y:
            case COMPONENT_Z:
            case COMPONENT_F:
            case COMPONENT_H:
            case COMPONENT_F_SCALAR:
            case COMPONENT_F_DIFF:
                string = "nT";
                break;
            case COMPONENT_D:
            case COMPONENT_I:
                switch (angle_units)
                {
                    case ANGLE_DEGREES: string = "deg."; break;
                    case ANGLE_MINUTES: string = "min."; break;
                    case ANGLE_RADIANS: string = "rad."; break;
                    default: string = "unk.";
                }
                break;
            default:
                string = "unk";
                break;
        }
        
        if (! short_form)
        {
            if (string.equals("nT")) string = "nanotesla";
            else if (string.equals("deg.")) string = "degrees";
            else if (string.equals("min.")) string = "minutes of arc";
            else if (string.equals("rad.")) string = "radians";
            else string = "unknown";
        }
        
        return string;
    }
    
    /** get a formatter that will provide suitable precision for a
     * component
     * @param code one of the COMPONENT_xxx codes
     * @param angle_units one of the ANGLE_xxx codes */
    public DecimalFormat getFormatter (int code, int angle_units)
    {
        DecimalFormat formatter;
        
        switch (resolveNative(code))
        {
            case COMPONENT_X:
            case COMPONENT_Y:
            case COMPONENT_Z:
            case COMPONENT_F:
            case COMPONENT_H:
            case COMPONENT_F_SCALAR:
            case COMPONENT_F_DIFF:
                formatter = fieldStrengthFormat;
                break;
            case COMPONENT_D:
            case COMPONENT_I:
                switch (angle_units)
                {
                    case ANGLE_DEGREES: formatter = angleDegreesFormat; break;
                    case ANGLE_MINUTES: formatter = angleMinutesFormat; break;
                    case ANGLE_RADIANS: formatter = angleRadiansFormat; break;
                    default: formatter = null;
                }
                break;
            default:
                formatter = null;;
                break;
        }
        
        return formatter;
    }
    
    /** resolve native component codes
     * @param code a component code, one the COMPONENT_xxx codes - may
     *        include COMPONENT_NATIVE_1,2,3
     * @return a component code which will never include any of 
     *         COMPONENT_1,2,3 */
    public int resolveNative (int code)
    {
        switch (code)
        {
            case COMPONENT_NATIVE_1:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: code = COMPONENT_D; break;
                    case ORIENTATION_HDZ: code = COMPONENT_H; break;
                    case ORIENTATION_XYZ: code = COMPONENT_X; break;
                }
                break;
            case COMPONENT_NATIVE_2:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: code = COMPONENT_I; break;
                    case ORIENTATION_HDZ: code = COMPONENT_D; break;
                    case ORIENTATION_XYZ: code = COMPONENT_Y; break;
                }
                break;
            case COMPONENT_NATIVE_3:
                switch (native_orientation)
                {
                    case ORIENTATION_DIF: code = COMPONENT_F; break;
                    case ORIENTATION_HDZ: code = COMPONENT_Z; break;
                    case ORIENTATION_XYZ: code = COMPONENT_Z; break;
                }
                break;
        }
        return code;
    }
    
    public String toString ()
    {
        String string;

        string = "GeomagAbsoluteValue";
        if (X_ok) string += ", X: " + Double.toString(X);
        if (Y_ok) string += ", Y: " + Double.toString(Y);
        if (Z_ok) string += ", Z: " + Double.toString(Z);
        if (H_ok) string += ", H: " + Double.toString(H);
        if (D_ok) string += ", D: " + Double.toString(D);
        if (I_ok) string += ", I: " + Double.toString(I);
        if (F_ok) string += ", F: " + Double.toString(F);
        if (FScalar_ok) string += ", F(S): " + Double.toString(FScalar);
        if (F_ok) string += ", dF: " + Double.toString(FDiff);
        return string;
    }
    
    public static String getComponentName (int component, boolean short_form)
    {
        if (short_form)
        {
            switch (component)
            {
            case GeomagAbsoluteValue.COMPONENT_D: return "D";
            case GeomagAbsoluteValue.COMPONENT_I: return "I";
            case GeomagAbsoluteValue.COMPONENT_X: return "X";
            case GeomagAbsoluteValue.COMPONENT_Y: return "Y";
            case GeomagAbsoluteValue.COMPONENT_Z: return "Z";
            case GeomagAbsoluteValue.COMPONENT_H: return "H";
            case GeomagAbsoluteValue.COMPONENT_F: return "F";
            case GeomagAbsoluteValue.COMPONENT_F_SCALAR: return "F(S)";
            case GeomagAbsoluteValue.COMPONENT_F_DIFF: return "dF";
            }
            return "U";
        }
        switch (component)
        {
        case GeomagAbsoluteValue.COMPONENT_D: return "Declination";
        case GeomagAbsoluteValue.COMPONENT_I: return "Inclination";
        case GeomagAbsoluteValue.COMPONENT_X: return "North intensity";
        case GeomagAbsoluteValue.COMPONENT_Y: return "East intensity";
        case GeomagAbsoluteValue.COMPONENT_Z: return "Vertical intensity";
        case GeomagAbsoluteValue.COMPONENT_H: return "Horizontal intensity";
        case GeomagAbsoluteValue.COMPONENT_F: return "Total Field";
        case GeomagAbsoluteValue.COMPONENT_F_SCALAR: return "Total Field (from scalar instrument)";
        case GeomagAbsoluteValue.COMPONENT_F_DIFF: return "Difference in Total Field measurements";
        }
        return "Unknown";
    }
}

