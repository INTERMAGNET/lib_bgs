/*
 * Event.java
 *
 * Created on 17 August 2003, 17:15
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;

/***************************************************************
 * Event is an interface for classes which hold details of a
 * single event on a data system - date, time, duration,
 * detection algorithm and algorithm data
 *
 * @author Iestyn Evans
 **************************************************************/
public interface DataEvent {
    
    // publicly accessible methods - replaces properties
    
    /*************************************************************
     * Gets the start date and time
     *
     * @return The start date
     *************************************************************/
    public Date GetStartDate();
    
    /*************************************************************
     * Sets the start date and time
     *
     * @param startDate The startDate
     *************************************************************/
    public void SetStartDate(Date startDate);
    
    /*************************************************************
     * Gets the duration
     *
     * @return The duration
     *************************************************************/
    public int GetDuration();
    
    /*************************************************************
     * Sets the duration
     *
     * @param duration The duration
     *************************************************************/
    public void SetDuration(int duration);
    
    /*************************************************************
     * Gets the name of the detection algorithm
     *
     * @return The detected algorithm
     *************************************************************/
    public String GetAlgorithmName();
    
    /*************************************************************
     * Sets the name of the detection algorithm
     *
     * @param algorithm_name The name of the algorithm
     *************************************************************/
    public void SetAlgorithmName(String algorithm_name);
    
    /*************************************************************
     * Gets the data used in detection algorithm
     *
     * @return The data
     *************************************************************/
    public String GetAlgorithmData();
    
    /*************************************************************
     * Sets the data used in detection algorithm
     *
     * @param algorithm_data The data
     *************************************************************/
    public void SetAlgorithmData(String algorithm_data);
    
}
