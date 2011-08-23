/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import java.util.Iterator;
import java.util.Vector;

/**
 * a class to allow iteration over the means in a year mean file
 *
 * @author smf
 */
public class YearMeanIterator 
implements Iterator<YearMean>
{

    public enum IncludeJumps { NO_JUMPS, INCLUDE_JUMPS, APPLY_JUMPS }
    
    private Vector<YearMean> array;
    private int array_index;
    private YearMean last_year_mean;
    
    /** create an object to iterate over an array of year mean file
     * objects
     * @param year_mean_file the file to iterate over
     * @param type the type of year mean objects to include
     * @param include_jumps how to include jumps:
     *        NO_JUMPS - don't include jumps
     *        INCLUDE_JUMPS - insert jump objects in the iteration
     *        APPLY_JUMPS - add jumps to the other year means
     * @param include_incomplete include INCOMPLETE types in the iteration
     *        (normally used with ALL_DAYS type)
     * @param start_from_first_jump if true, start the iteration at the
     *        first jump, rather than the start of the year means */
    public YearMeanIterator (YearMeanFile year_mean_file, 
                             YearMean.YearMeanType type,
                             IncludeJumps include_jumps,
                             boolean include_incomplete,
                             boolean start_from_first_jump)
    {
        int count;
        boolean include_mean, first_jump_found;
        YearMean mean, last_mean, jump_mean, jump_accumulator, mean2;

        array = new Vector<YearMean> ();
        jump_accumulator = mean = last_mean = jump_mean = null;
        first_jump_found = false;
        for (count=0; count<year_mean_file.getNMeans(); count++)
        {
            if (mean != null)
            {
                if (mean.getType() != YearMean.YearMeanType.JUMP)
                    last_mean = mean;
            }
            mean = year_mean_file.getMean(count);
            // work out whether to include the mean or not
            switch (mean.getType())
            {
                case INCOMPLETE:
                    if (include_incomplete) include_mean = true;
                    else if (type == YearMean.YearMeanType.INCOMPLETE) include_mean = true;
                    else include_mean = false;
                    break;
                case JUMP:
                    first_jump_found = true;
                    switch (include_jumps)
                    {
                        case NO_JUMPS:
                            include_mean = false;
                            break;
                        case INCLUDE_JUMPS:
                            include_mean = true;
                            break;
                        case APPLY_JUMPS:
                            jump_mean = mean;
                            include_mean = false;
                            break;
                        default:
                            include_mean = false;
                            break;
                    }
                    break;
                default:
                    if (type == mean.getType()) include_mean = true;
                    else include_mean = false;
                    
            }
            if (start_from_first_jump && (! first_jump_found))
                include_mean = false;
            
            // add the mean to the list
            if (include_mean)
            {
                // if we have just found a jump, plot it
                if ((jump_mean != null) && (last_mean != null))
                {
                    if (jump_accumulator == null)
                        mean2 = mean;
                    else
                    {
                        mean2 = new YearMean (mean);
                        mean2.subtract (jump_mean);
                    }
                    mean2 = new YearMean (jump_mean.getYear(), last_mean, mean2);
                    array.add (mean2);
                    // the addition of 0.005 to the year is needed to make JFreeChart plot
                    // two points (it wont plot two time series values that have the same time)
                    mean2 = new YearMean (jump_mean.getYear() + 0.005, mean2);
                    mean2.add (jump_mean);
                    array.add (mean2);
                }
                
                // apply any jump and insert the mean
                if (jump_mean != null)
                {
                    if (jump_accumulator == null)
                        jump_accumulator = new YearMean (jump_mean);
                    else
                        jump_accumulator.add (jump_mean);
                    jump_mean = null;
                }
                if (jump_accumulator != null)
                {
                    mean = new YearMean (mean);
                    mean.add (jump_accumulator);
                }
                array.add (mean);
            }
        }            

        // reset the iterator
        rewind ();
    }
    
    /** create an object to iterate over an array of year mean file
     * objects
     * @param year_mean_array the array to iterate over */
    public YearMeanIterator (YearMean year_mean_array [])
    {
        int count;

        // copy the array
        array = new Vector<YearMean> ();
        for (count=0; count<year_mean_array.length; count++)
            array.add (year_mean_array [count]);
    
        // reset the iterator
        rewind ();
    }
    
    /** create an object to iterate over an array of year mean file
     * objects
     * @param year_mean_array the array to iterate over */
    public YearMeanIterator (Vector<YearMean> year_mean_array)
    {
        int count;
        
        array = new Vector<YearMean> ();
        for (count=0; count<year_mean_array.size(); count++)
            array.add (year_mean_array.get(count));
    
        // reset the iterator
        rewind ();
    }
    
    public boolean hasNext() 
    {
        if (array_index >= array.size()) return false;
        return true;
    }

    public YearMean next() 
    {
        last_year_mean = array.get (array_index ++);
        return last_year_mean;
    }

    public void remove() 
    {
        // not implemented
    }

    public void rewind () { array_index = 0; last_year_mean = null; }
    public int getNYearMeans () { return array.size (); }
    public YearMean getYearMean (int index) { return array.get(index); }
    
}
