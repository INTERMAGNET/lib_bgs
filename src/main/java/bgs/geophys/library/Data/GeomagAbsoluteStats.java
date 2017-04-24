/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

/**
 * An object that creates statistical data on a set of GeomagAbsoluteValue
 * objects. Assumes that all objects have the same orientation and angular units.
 * 
 * @author smf
 */
public class GeomagAbsoluteStats 
{

    // copy of the set
    private GeomagAbsoluteValue data_set [];
    // a GeomagAbsoluteValue that holds the minimum of the set 
    private GeomagAbsoluteValue minimum;
    // a GeomagAbsoluteValue that holds the maximum of the set 
    private GeomagAbsoluteValue maximum;
    // a GeomagAbsoluteValue that holds the average of the set 
    private GeomagAbsoluteValue average;
    // a GeomagAbsoluteDifference that holds the range of the set 
    private GeomagAbsoluteDifference range;
    // a GeomagAbsoluteValue that holds the mid-point of the set 
    private GeomagAbsoluteValue mid_point;
    // the number of valid data points for each component */
    private int n_points [];
    // an array of GeomagAbsoluteValue objects that hold differences between
    // the data points in the set - the length of the array is one less than
    // the length of the set
    private GeomagAbsoluteDifference first_diff [];
    // stats on the first difference
    private GeomagAbsoluteStats first_diff_stats;
    
    /** construct a GeomagAbsoluteStats object from a set of absolute data */
    public GeomagAbsoluteStats (GeomagAbsoluteValue data_set [])
    {
        if (data_set == null)
            this.data_set = new GeomagAbsoluteValue [0];
        else
            this.data_set = data_set;
        
        // all stats are set to null and created when requested
        minimum = maximum = average = mid_point = null;
        range = null;
        first_diff = null;
        first_diff_stats = null;
        n_points = null;
    }

    /** get the original data set */
    public GeomagAbsoluteValue [] getDataSet ()
    {
        return data_set;
    }
    
    /** get the minimum of the data set */
    public GeomagAbsoluteValue getMinimum ()
    {
        if (minimum == null) calcStats ();
        return minimum;
    }
    
    /** get the maximum of the data set */
    public GeomagAbsoluteValue getMaximum ()
    {
        if (maximum == null) calcStats ();
        return maximum;
    }
    
    /** get the average of the data set */
    public GeomagAbsoluteValue getAverage ()
    {
        if (average == null) calcStats ();
        return average;
    }
    
    /** get the average of the data set */
    public GeomagAbsoluteDifference getRange ()
    {
        if (range == null) calcStats ();
        return range;
    }
    
    /** get the average of the data set */
    public GeomagAbsoluteValue getMidPoint ()
    {
        if (mid_point == null) calcStats ();
        return mid_point;
    }
    
    /** get the first difference of the data set */
    public GeomagAbsoluteDifference [] getFirstDifference ()
    {
        if (first_diff == null) calcFirstDiff ();
        return first_diff;
    }
    
    /** get the statistics on the first difference of the data set */
    public GeomagAbsoluteStats getFirstDifferenceStats ()
    {
        if (first_diff_stats == null) calcFirstDiff ();
        return first_diff_stats;
    }
    
    /** get the number of non-missing data points for each element
     * @param component_code one of the GeomagAbsoluteValue.COMPONENT_xxx codes
     * @return the number of non-missing data points */
    public int getNDataPoints (int component_code)
    {
        if (n_points == null) calcStats();
        // use of the array elements below must correspond with use of
        // the array elements in calcStats - i.e. the component used for
        // a particular element must be the same
        switch (component_code)
        {
            case GeomagAbsoluteValue.COMPONENT_X: return n_points [0];
            case GeomagAbsoluteValue.COMPONENT_Y: return n_points [1];
            case GeomagAbsoluteValue.COMPONENT_Z: return n_points [2];
            case GeomagAbsoluteValue.COMPONENT_F: return n_points [3];
            case GeomagAbsoluteValue.COMPONENT_H: return n_points [4];
            case GeomagAbsoluteValue.COMPONENT_D: return n_points [5];
            case GeomagAbsoluteValue.COMPONENT_I: return n_points [6];
            case GeomagAbsoluteValue.COMPONENT_F_SCALAR: return n_points [7];
            case GeomagAbsoluteValue.COMPONENT_F_DIFF: return n_points [8];
        }
        return -1;
    }
    
    
    
    // calculate the statistics
    private void calcStats ()
    {
        int comp_count, set_count, n_elements, orient_code;
        double s_min [], s_max [], s_sum [], s_rng [], s_mid [], miss_val, val;
        GeomagAbsoluteValue value;
        
        // initialise counters - need a counter for 7 elements + F scalar and F diff
        n_elements = 9;
        n_points = new int [n_elements];
        s_min = new double [n_elements];
        s_max = new double [n_elements];
        s_sum = new double [n_elements];
        s_rng = new double [n_elements];
        s_mid = new double [n_elements];
        for (comp_count=0; comp_count<n_elements; comp_count++)
        {
            n_points [comp_count] = 0;
            s_min [comp_count] = Double.MAX_VALUE;
            s_max [comp_count] = - Double.MAX_VALUE;
            s_sum [comp_count] = 0.0;
        }
        
        // calculate statistics
        for (set_count=0; set_count<data_set.length; set_count++)
        {
            value = data_set [set_count];
            miss_val = value.getMissingDataValue();
            for (comp_count=0; comp_count<n_elements; comp_count++)
            {
                switch (comp_count)
                {
                    case 0: val = value.getX (); break;
                    case 1: val = value.getY (); break;
                    case 2: val = value.getZ (); break;
                    case 3: val = value.getF (); break;
                    case 4: val = value.getH (); break;
                    case 5: val = value.getD (); break;
                    case 6: val = value.getI (); break;
                    case 7: val = value.getFScalar (); break;
                    case 8: val = value.getFDiff (); break;
                    default: val = miss_val; break;
                }
                if (val != miss_val)
                {
                    n_points [comp_count] += 1;
                    if (val < s_min [comp_count]) s_min [comp_count] = val;
                    if (val > s_max [comp_count]) s_max [comp_count] = val;
                    s_sum [comp_count] += val;
                }
            }
        }
        
        // check for missing data and finalise averages, range and mid-point
        miss_val = 99999.9;
        for (comp_count=0; comp_count<n_elements; comp_count++)
        {
            if (n_points [comp_count] <= 0)
            {
                s_min [comp_count] = miss_val;
                s_max [comp_count] = miss_val;
                s_sum [comp_count] = miss_val;
                s_rng [comp_count] = miss_val;
                s_mid [comp_count] = miss_val;
            }
            else
            {
                s_sum [comp_count] /= (double) n_points [comp_count];
                s_rng [comp_count] = s_max [comp_count] - s_min [comp_count];
                s_mid [comp_count] = s_min [comp_count] + (s_rng [comp_count] / 2.0);
            }
        }
        
        // create absolute value stats
        if (data_set.length <= 0)
            orient_code = GeomagAbsoluteValue.ORIENTATION_UNKNOWN;
        else
            orient_code = data_set[0].getNativeOrientation();
        minimum   = new GeomagAbsoluteValue (s_min [0], s_min [1], s_min [2], s_min [3], s_min [4], s_min [5], s_min [6], s_min [7], s_min [8], miss_val, orient_code, GeomagAbsoluteValue.ANGLE_RADIANS);
        maximum   = new GeomagAbsoluteValue (s_max [0], s_max [1], s_max [2], s_max [3], s_max [4], s_max [5], s_max [6], s_max [7], s_max [8], miss_val, orient_code, GeomagAbsoluteValue.ANGLE_RADIANS);
        average   = new GeomagAbsoluteValue (s_sum [0], s_sum [1], s_sum [2], s_sum [3], s_sum [4], s_sum [5], s_sum [6], s_sum [7], s_sum [8], miss_val, orient_code, GeomagAbsoluteValue.ANGLE_RADIANS);
        mid_point = new GeomagAbsoluteValue (s_mid [0], s_mid [1], s_mid [2], s_mid [3], s_mid [4], s_mid [5], s_mid [6], s_mid [7], s_mid [8], miss_val, orient_code, GeomagAbsoluteValue.ANGLE_RADIANS);
        range = new GeomagAbsoluteDifference (maximum, minimum);
    }
    
    // calculate the first difference
    private void calcFirstDiff ()
    {
        int length, set_count;
        
        length = data_set.length -1;
        if (length < 0) length = 0;
        first_diff = new GeomagAbsoluteDifference [length];
        
        for (set_count=0; set_count<length; set_count++)
            first_diff [set_count] = new GeomagAbsoluteDifference (data_set [set_count], data_set [set_count +1]);
        
        first_diff_stats = new GeomagAbsoluteStats(first_diff);
    }
}
