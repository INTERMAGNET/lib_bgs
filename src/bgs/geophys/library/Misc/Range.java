package bgs.geophys.library.Misc;

/****************************************************************************
 * Range is a simple class to calculate and hold a max/min range.
 * 
 * @author  S. Flower
 * @version 0.0
 ****************************************************************************/
public class Range
{

  // private member variables
  private int data_min, data_max, average;
  private int missing_value;
  private int n_valid_points;
  
  /****************************************************************************
   * Create an empty range
   * @param missing_value the value that denotes a missing data point
   ****************************************************************************/
  public Range (int missing_value)
  {
    data_min = data_max = average = 0;
    this.missing_value = missing_value;
  }
  
  /****************************************************************************
   * Create a range from maximum / minimum values
   *
   * @param missing_value the value that denotes a missing data point
   * @param min the minimum value for the range
   * @param max the maximum value for the range
   ****************************************************************************/
  public Range (int missing_value, int min, int max)
  {
    data_min = min;
    data_max = max;
    average = (max + min) / 2;
    this.missing_value = missing_value;
  }
  
  /****************************************************************************
   * Create a range from an existing range
   *
   * @param min the minimum value for the range
   * @param max the maximum value for the range
   ****************************************************************************/
  public Range (Range range)
  {
    data_min = range.data_min;
    data_max = range.data_max;
    average = range.average;
    missing_value = range.missing_value;
  }
  
  /****************************************************************************
   * Calculate the range from a set of data points
   *
   * @param data the data set
   ****************************************************************************/
  public void calc_range (int data[])
  {
    int count;
    
    n_valid_points = 0;
    for (count=0; count<data.length; count++)
    {
      if (data [count] != missing_value)
      {
        if (n_valid_points <= 0)
          data_min = data_max = average = data [count];
        else
        {
          if (data[count] > data_max) data_max = data[count];
          else if (data[count] < data_min) data_min = data[count];
          average += data [count];
        }
        n_valid_points ++;
      }
    }
    if (n_valid_points > 0) average /= n_valid_points;
    else data_min = data_max = average = 0;
    
  }

  /****************************************************************************
   * Set the minimum value of the range
   *
   * @param min - the new minimum value
   ****************************************************************************/
  public void set_min (int min) {data_min = min; }

  /****************************************************************************
   * Set the maximum value of the range
   *
   * @param max - the new maximum value
   ****************************************************************************/
  public void set_max (int max) {data_max = max; }

  /****************************************************************************
   * Set the value of the range
   *
   * @param min - the new minimum value
   * @param max - the new maximum value
   ****************************************************************************/
  public void set_range (int min, int max) {data_min = min; data_max = max;}
  
  /****************************************************************************
   * Get the minimum value of the range
   *
   * @return the minimum value
   ****************************************************************************/
  public int get_min () {return data_min; }

  /****************************************************************************
   * Get the maximum value of the range
   *
   * @return the maximum value
   ****************************************************************************/
  public int get_max () {return data_max; }

  /****************************************************************************
   * Get the value of the range
   *
   * @return the range
   ****************************************************************************/
  public int get_range () {return data_max - data_min;  }
  
  /****************************************************************************
   * Get the average value of the data associated with the range
   *
   * @return the average
   ****************************************************************************/
  public int get_average () {return average; }

  /****************************************************************************
   * Get the number of valid data points in the range. This will indicate
   * if any data is missing.
   *
   * @return the number of valid data points
   ****************************************************************************/
  public int get_n_valid_points() {return n_valid_points; }

}
