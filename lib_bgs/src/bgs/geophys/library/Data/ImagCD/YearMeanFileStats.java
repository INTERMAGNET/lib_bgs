/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import java.util.Vector;

/**
 * An object that creates statistical data on a YearMeanFile.
 * 
 * @author smf
 */
public class YearMeanFileStats 
{

    // copy of the set
    private YearMeanIterator year_mean_iterator;
    // a YearMean that holds the minimum of the set 
    private YearMean minimum;
    // a YearMean that holds the maximum of the set 
    private YearMean maximum;
    // a YearMean that holds the average of the set 
    private YearMean average;
    // a YearMean that holds the range of the set 
    private YearMean range;
    // a YearMean that holds the mid-point of the set 
    private YearMean mid_point;
    // the number of valid data points for each component */
    private int n_points [];
    // earliest and latest means
    private YearMean earliest;
    private YearMean latest;
    // an array of YearMean objects that hold differences between
    // the data points in the set - the length of the array is one less than
    // the length of the set
    private Vector<YearMean> first_diff;
    // stats on the first difference
    private YearMeanFileStats first_diff_stats;
    
    /** construct a GeomagAbsoluteStats object from a set of absolute data */
    public YearMeanFileStats (YearMeanIterator year_mean_iterator)
    {
        this.year_mean_iterator = year_mean_iterator;
        
        // all stats are set to null and created when requested
        minimum = maximum = average = mid_point = null;
        earliest = latest = null;
        range = null;
        first_diff = null;
        first_diff_stats = null;
        n_points = null;
    }

    /** get the original data set */
    public YearMeanIterator getDataSet ()
    {
        return year_mean_iterator;
    }
    
    /** get the minimum of the data set */
    public YearMean getMinimum ()
    {
        if (minimum == null) calcStats ();
        return minimum;
    }
    
    /** get the maximum of the data set */
    public YearMean getMaximum ()
    {
        if (maximum == null) calcStats ();
        return maximum;
    }
    
    /** get the average of the data set */
    public YearMean getAverage ()
    {
        if (average == null) calcStats ();
        return average;
    }
    
    /** get the average of the data set */
    public YearMean getRange ()
    {
        if (range == null) calcStats ();
        return range;
    }
    
    /** get the average of the data set */
    public YearMean getMidPoint ()
    {
        if (mid_point == null) calcStats ();
        return mid_point;
    }
    
    /** get the earliest value in the data set 
     * @return the mean with the earliest date - may return null if the mean set is empty */
    public YearMean getEarliest ()
    {
        if (year_mean_iterator.getNYearMeans() <= 0) return null;
        return year_mean_iterator.getYearMean(0);
    }
    
    /** get the earliest value in the data set
     * @return the mean with the latest date - may return null if the mean set is empty */
    public YearMean getLatest ()
    {
        if (year_mean_iterator.getNYearMeans() <= 0) return null;
        return year_mean_iterator.getYearMean(year_mean_iterator.getNYearMeans() -1);
    }
    
    /** get the first difference of the data set */
    public YearMean [] getFirstDifference ()
    {
        int count;
        YearMean buffer [];
        
        if (first_diff == null) calcFirstDiff ();
        buffer = new YearMean [first_diff.size()];
        for (count=0; count<first_diff.size(); count++)
            buffer [count] = first_diff.get(count);
        return buffer;
    }
    
    /** get the statistics on the first difference of the data set */
    public YearMeanFileStats getFirstDifferenceStats ()
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
            case GeomagAbsoluteValue.COMPONENT_H: return n_points [3];
            case GeomagAbsoluteValue.COMPONENT_D: return n_points [4];
            case GeomagAbsoluteValue.COMPONENT_I: return n_points [5];
            case GeomagAbsoluteValue.COMPONENT_F: return n_points [6];
        }
        return -1;
    }
    
    
    
    // calculate the statistics
    private void calcStats ()
    {
        int comp_count, n_elements;
        double s_min [], s_max [], s_sum [], s_rng [], s_mid [], val, year_av;
        YearMean mean;
        YearMeanIterator i;
        
        // initialise counters - need a counter for 7 elements
        n_elements = 7;
        n_points = new int [n_elements];
        s_min = new double [n_elements];
        s_max = new double [n_elements];
        s_sum = new double [n_elements];
        s_rng = new double [n_elements];
        s_mid = new double [n_elements];
        year_av = 0.0;
        for (comp_count=0; comp_count<n_elements; comp_count++)
        {
            n_points [comp_count] = 0;
            s_min [comp_count] = Double.MAX_VALUE;
            s_max [comp_count] = - Double.MAX_VALUE;
            s_sum [comp_count] = 0.0;
        }
        
        // calculate statistics
        year_mean_iterator.rewind();
        while (year_mean_iterator.hasNext())
        {
            mean = year_mean_iterator.next();
            year_av += mean.getYear();
            for (comp_count=0; comp_count<n_elements; comp_count++)
            {
                switch (comp_count)
                {
                    case 0: val = mean.getX (); break;
                    case 1: val = mean.getY (); break;
                    case 2: val = mean.getZ (); break;
                    case 3: val = mean.getH (); break;
                    case 4: val = mean.getD (); break;
                    case 5: val = mean.getI (); break;
                    case 6: val = mean.getF (); break;
                    default: val = YearMean.MISSING_ELEMENT; break;
                }
                if (val != YearMean.MISSING_ELEMENT)
                {
                    n_points [comp_count] += 1;
                    if (val < s_min [comp_count]) s_min [comp_count] = val;
                    if (val > s_max [comp_count]) s_max [comp_count] = val;
                    s_sum [comp_count] += val;
                }
            }
        }
        
        // check for missing data and finalise averages, range and mid-point
        if (year_mean_iterator.getNYearMeans() <= 0)
            year_av = 0.0;
        else
            year_av /= (double) year_mean_iterator.getNYearMeans();
        for (comp_count=0; comp_count<n_elements; comp_count++)
        {
            if (n_points [comp_count] <= 0)
            {
                s_min [comp_count] = YearMean.MISSING_ELEMENT;
                s_max [comp_count] = YearMean.MISSING_ELEMENT;
                s_sum [comp_count] = YearMean.MISSING_ELEMENT;
                s_rng [comp_count] = YearMean.MISSING_ELEMENT;
                s_mid [comp_count] = YearMean.MISSING_ELEMENT;
            }
            else
            {
                s_sum [comp_count] /= (double) n_points [comp_count];
                s_rng [comp_count] = s_max [comp_count] - s_min [comp_count];
                s_mid [comp_count] = s_min [comp_count] + (s_rng [comp_count] / 2.0);
            }
        }
        
        // create absolute value stats
        minimum   = new YearMean (year_av, s_min [0], s_min [1], s_min [2], s_min [3], s_min [4], s_min [5], s_min [6], YearMean.YearMeanType.UNKNOWN, "???", "");
        maximum   = new YearMean (year_av, s_max [0], s_max [1], s_max [2], s_max [3], s_max [4], s_max [5], s_max [6], YearMean.YearMeanType.UNKNOWN, "???", "");
        average   = new YearMean (year_av, s_sum [0], s_sum [1], s_sum [2], s_sum [3], s_sum [4], s_sum [5], s_sum [6], YearMean.YearMeanType.UNKNOWN, "???", "");
        mid_point = new YearMean (year_av, s_mid [0], s_mid [1], s_mid [2], s_mid [3], s_mid [4], s_mid [5], s_mid [6], YearMean.YearMeanType.UNKNOWN, "???", "");
        range = new YearMean (year_av, s_max [0] - s_min [0], s_max [1] - s_min [1], s_max [2] - s_min [2], s_max [3] - s_min [3], 
                                       s_max [4] - s_min [4], s_max [5] - s_min [5], s_max [6] - s_min [6], YearMean.YearMeanType.UNKNOWN, "???", "");
    }
    
    // calculate the first difference
    private void calcFirstDiff ()
    {
        int count, length;
        YearMean mean, last_mean;
        double year_diff;
        
        length = year_mean_iterator.getNYearMeans() -1;
        if (length < 0) length = 0;
        first_diff = new Vector<YearMean> ();
        
        year_mean_iterator.rewind();
        mean = null;
        for (count=0; year_mean_iterator.hasNext(); count++)
        {
            last_mean = mean;
            mean = year_mean_iterator.next();
            if (last_mean != null)
            {
                year_diff = mean.getYear() - last_mean.getYear();
                if (year_diff > 0.99 && year_diff < 1.01)
                    first_diff.add (new YearMean (mean, last_mean));
            }
        }
        
        first_diff_stats = new YearMeanFileStats (new YearMeanIterator (first_diff));
    }
}
