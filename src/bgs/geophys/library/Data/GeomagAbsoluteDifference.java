/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

/**
 * An object that holds the difference between two GeomagAbsoluteValue objects.
 * Because this object shares a lot of properties with GeomagAbsoluteValue, it
 * is sub-classed from it.
 * 
 * @author smf
 */
public class GeomagAbsoluteDifference extends GeomagAbsoluteValue
{

    private GeomagAbsoluteValue value1;
    private GeomagAbsoluteValue value2;
    
    // flags to show which components have had differences calculated
    private boolean X_set, Y_set, Z_set, H_set, D_set, I_set, F_set, FScalar_set, FDiff_set;

    /** takes the first difference between value1 and value2.
     * Sets the native orientation to that of the values provided they are the
     * same, otherwise sets it to UNKNOWN. Sets the missing flag to the
     * missing flag of the first value.
     * @param value1
     * @param value2 */
    public GeomagAbsoluteDifference (GeomagAbsoluteValue value1,
                                     GeomagAbsoluteValue value2)
    {
        // get an empty set of data
        super ();
        
        // record the values
        this.value1 = value1;
        this.value2 = value2;

        // show that no differences have been calculated
        X_set = Y_set = Z_set = H_set = D_set = I_set = F_set = FScalar_set = FDiff_set = false;
        
        // set orientation and missing flag
        if (value1.native_orientation == value2.native_orientation)
            this.native_orientation = value1.native_orientation;
        else
            this.native_orientation = ORIENTATION_UNKNOWN;
        this.missingDataValue = value1.missingDataValue;
        
        // set the native orientation values, so they can be obtained
        // through the standard getters
        switch (native_orientation)
        {
        case ORIENTATION_DIF:
            getD ();
            getI ();
            getF ();
            break;
        case ORIENTATION_HDZ:
            getH ();
            getD ();
            getZ ();
            break;
        case ORIENTATION_XYZ:
            getX ();
            getY ();
            getZ ();
            break;
        }
    }   
    
    @Override
    public double getD ()
    {
        double val1, val2;
        
        if (! D_set)
        {
            val1 = value1.getD();
            val2 = value2.getD();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                D_ok = true;
                D = val1 - val2;
            }
            D_set = true;
        }
        if (! D_ok) return missingDataValue;
        return D;
    }
    
    @Override
    public double getF ()
    {
        double val1, val2;

        if (! F_set)
        {
            val1 = value1.getF();
            val2 = value2.getF();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                F_ok = true;
                F = val1 - val2;
            }
            F_set = true;
        }
        if (! F_ok) return missingDataValue;
        return F;
    }
    
    @Override
    public double getFDiff ()
    {
        double val1, val2;

        if (! FDiff_set)
        {
            val1 = value1.getFDiff();
            val2 = value2.getFDiff();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                FDiff_ok = true;
                FDiff = val1 - val2;
            }
            FDiff_set = true;
        }
        if (! FDiff_ok) return missingDataValue;
        return FDiff;
    }
                
    @Override
    public double getFScalar ()
    {
        double val1, val2;

        if (! FScalar_set)
        {
            val1 = value1.getFScalar();
            val2 = value2.getFScalar();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                FScalar_ok = true;
                FScalar = val1 - val2;
            }
            FScalar_set = true;
        }
        if (! FScalar_ok) return missingDataValue;
        return FScalar;
    }
    
    @Override
    public double getH ()
    {
        double val1, val2;

        if (! H_set)
        {
            val1 = value1.getH();
            val2 = value2.getH();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                H_ok = true;
                H = val1 - val2;
            }
            H_set = true;
        }
        if (! H_ok) return missingDataValue;
        return H;
    }

    @Override
    public double getI ()
    {
        double val1, val2;

        if (! I_set)
        {
            val1 = value1.getI();
            val2 = value2.getI();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                I_ok = true;
                I = val1 - val2;
            }
            I_set = true;
        }
        if (! I_ok) return missingDataValue;
        return I;
    }

    @Override
    public double getX ()
    {
        double val1, val2;

        if (! X_set)
        {
            val1 = value1.getX();
            val2 = value2.getX();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                X_ok = true;
                X = val1 - val2;
            }
            X_set = true;
        }
        if (! X_ok) return missingDataValue;
        return X;
    }

    @Override
    public double getY ()
    {
        double val1, val2;

        if (! Y_set)
        {
            val1 = value1.getY();
            val2 = value2.getY();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                Y_ok = true;
                Y = val1 - val2;
            }
            Y_set = true;
        }
        if (! Y_ok) return missingDataValue;
        return Y;
    }
    
    @Override
    public double getZ ()
    {
        double val1, val2;

        if (! Z_set)
        {
            val1 = value1.getZ();
            val2 = value2.getZ();
            if (val1 != value1.getMissingDataValue() && val2 != value2.getMissingDataValue())
            {
                Z_ok = true;
                Z = val1 - val2;
            }
            Z_set = true;
        }
        if (! Z_ok) return missingDataValue;
        return Z;
    }
    
}
