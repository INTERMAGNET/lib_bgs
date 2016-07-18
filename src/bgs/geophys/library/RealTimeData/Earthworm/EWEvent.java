/***************************************************************
 * EWEvent.java
 * EWEvent is used to hold details of a single EW event
 * on an EW system - date, time, duration, detection
 * algorithm and algorithm data. This is for Sdas
 * compatibiltiy and should never actualy be used!
 *
 * Created on 06 January 2003, 12:14
 **************************************************************/

package bgs.geophys.library.RealTimeData.Earthworm;

import java.util.*;
import bgs.geophys.library.RealTimeData.*;

public class EWEvent extends Object implements DataEvent
{
    // private fields
    /*************************************************************
     * The start date and time
     *************************************************************/
    private Date startDate;
    /*************************************************************
     * The duration
     *************************************************************/
    private int duration;
    /*************************************************************
     * The name of the detection algorithm
     *************************************************************/
    private String algorithm_name;
    /*************************************************************
     * The data used in detection algorithm
     *************************************************************/
    private String algorithm_data;

    /*************************************************************
     * Create a new SdasEvent object.
     *
     * @param Date stDate - the start date
     * @param int dur - the event duration
     * @param String al_name - the algorithm name
     *************************************************************/
    public EWEvent(Date stDate, int dur, String al_name)
    {
        startDate = stDate;
        duration = dur;
        algorithm_name = al_name;
    }

    /*************************************************************
     * Create a new SdasEvent object.
     *
     * @param Date stDate - the start date
     * @param int dur - the event duration
     * @param String al_name - the algorithm name
     * @param String al_data - the algorithm data
     *************************************************************/
    public EWEvent(Date stDate, int dur, String al_name, String al_data)
    {
        startDate = stDate;
        duration = dur;
        algorithm_name = al_name;
        algorithm_data = al_data;
    }

    /*************************************************************
     * Create a new EWEvent object from an existing one.
     *
     * @param source the existing object
     *************************************************************/
    public EWEvent(EWEvent source)
    {
        startDate = source.startDate;
        duration = source.duration;
        algorithm_name = source.algorithm_name;
        algorithm_data = source.algorithm_data;
    }
    
    
    /*************************************************************
     * Gets the start date and time
     *
     * @return The start date
     *************************************************************/
    public Date GetStartDate()
    {
        return startDate;
    }
    
    /*************************************************************
     * Sets the start date and time
     *
     * @param startDate The startDate
     *************************************************************/
    public void SetStartDate(Date startDate)
    {
        this.startDate = startDate;
    }
    
    /*************************************************************
     * Gets the duration
     *
     * @return The duration
     *************************************************************/
    public int GetDuration()
    {
        return duration;
    }
    
    /*************************************************************
     * Sets the duration
     *
     * @param duration The duration
     *************************************************************/
    public void SetDuration(int duration)
    {
        this.duration = duration;
    }
    
    /*************************************************************
     * Gets the name of the detection algorithm
     *
     * @return The detected algorithm
     *************************************************************/
    public String GetAlgorithmName()
    {
        return algorithm_name;
    }
    
    /*************************************************************
     * Sets the name of the detection algorithm
     *
     * @param algorithm_name The name of the algorithm
     *************************************************************/
    public void SetAlgorithmName(String algorithm_name)
    {
        this.algorithm_name = algorithm_name;
    }
    
    /*************************************************************
     * Gets the data used in detection algorithm
     *
     * @return The data
     *************************************************************/
    public String GetAlgorithmData()
    {
        return algorithm_data;
    }
    
    /*************************************************************
     * Sets the of data used in detection algorithm
     *
     * @param algorithm_data The data used
     *************************************************************/
    public void SetAlgorithmData(String algorithm_data)
    {
        this.algorithm_data = algorithm_data;
    }
}