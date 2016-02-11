/*
 * FIRFilter.java
 *
 * Created on 13 September 2006, 09:41
 */

package bgs.geophys.library.Maths;

/**
 * Implementation of some FIR filters. Most of this code originated in the
 * filter.c program from the SDAS software.
 *
 * @author  smf
 */
public class FIRFilter 
{
    
    /** constant defining a mean 'filter' */
    public static final int FILTER_MEAN = 1;
    /** constant defining a cosine filter */
    public static final int FILTER_COSINE = 2;
    /** constant defining a gaussian filter */
    public static final int FILTER_GAUSS = 3;
    
    // private members
    private double coeffs [];       // filter coefficients
    private double missing_data_flag;
    private int allowed_missing;

    /** Creates a new instance of FIRFilter
     * @param type - the type of filter - from the list of filter type constants
     *               FILTER_MEAN, FILTER_COSINE or FILTER_GAUSS
     * @param n_points number of coefficients
     * @param period time period between filter samples (only used with FILTER_GAUSS)
     * @param sd standard deviation  (only used with FILTER_GAUSS)
     * @param missing_data_flag any data points that match this value will be considered
     *        missing
     * @param allowed_missing if there are more missing data points than the
     *        given number when trying to calculate a filtered sample, then the
     *        sample will be set to the missing data flag value */
    public FIRFilter (int type, int n_points, double period, double sd,
                      double missing_data_flag, int allowed_missing) 
    {
        int count;
        double sum, temp, half_npoints, one_over_root_2pi_sd, time;
        double one_over_2sd_sd;

        coeffs = new double [n_points];
        
        // calculate the filter coefficients
        switch (type)
        {
        case FILTER_COSINE:
            for (count=0; count<n_points; count++)
                coeffs [count] = 1.0 - StrictMath.cos (2.0 * StrictMath.PI * (((double) (count +1)) /
                                                       ((double) (n_points +1))));
            break;
                
        case FILTER_GAUSS:
            half_npoints = (double) (n_points -1) / 2.0;
            one_over_root_2pi_sd = 1.0 / (StrictMath.sqrt(2.0 * StrictMath.PI) * sd);
            one_over_2sd_sd = 1.0 / (2.0 * sd * sd);
            for (count=0; count<n_points; count++) 
            {
                time = ((double) count - half_npoints) * period;
                coeffs [count] = one_over_root_2pi_sd *
                                 StrictMath.exp((-1.0 * (time * time)) * one_over_2sd_sd);
            }
            break;
                
        default:        // default is a MEAN without filtering
            for (count=0; count<n_points; count++) coeffs [count] = 1.0;
            break;
        }
        
        /* calculate the sum of the coefficients */
        sum = 0.0;
        for (count=0; count<n_points; count++) sum += coeffs [count];
        
        /* divide by the sum of the coefficients */
        for (count=0; count<n_points; count++) coeffs [count] /= sum;

        this.missing_data_flag = missing_data_flag;
        this.allowed_missing = allowed_missing;
    }
    
    /** Apply this filter to some data to produce a single data sample 
     * @param input_data the data to filter
     * @return the filtered sample
     * @throws ArrayIndexOutOfBoundsException is the length of data is not the
     *         same as the number of filter points */
    public double applyFilter (double input_data [])
    {
        return applyFilter (input_data, 0);
    }
    
    /** Apply this filter to some data to produce a single data sample 
     * @param input_data the data to filter
     * @param offset the start point in the array of input data
     * @return the filtered sample
     * @throws ArrayIndexOutOfBoundsException is the length of data is not the
     *         same as the number of filter points */
    public double applyFilter (double input_data [], int offset)
    throws ArrayIndexOutOfBoundsException
    {
        double filter_data, coeff_total;
        int n_missing, point;
        
        // check data is the correct length
        if ((input_data.length - offset) < coeffs.length) 
            throw new ArrayIndexOutOfBoundsException ("Not enough data samples to filter");
        
        // filter the data
        filter_data = 0.0;
        n_missing = 0;
        coeff_total = 0.0;
        for (point=0; point<coeffs.length; point++)
        {
            if (input_data[point + offset] == missing_data_flag) n_missing ++;
            else
            {
                filter_data += input_data[point + offset] * coeffs[point];
                coeff_total += coeffs[point];
            }
        }

        // check that there were not too many missing points, then adjust 
        // the result for missing data by re-normalising the result
        if (n_missing > allowed_missing) filter_data = missing_data_flag;
        else filter_data /= coeff_total;
        
        return filter_data;
    }

    /** get the number of coefficients in the filter (and hence the number of data points needed to
     * apply the filter to */
    public int getNCoeffs () { return coeffs.length; }
    
}
