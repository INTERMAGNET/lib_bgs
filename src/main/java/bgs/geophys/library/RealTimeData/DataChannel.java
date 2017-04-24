/*
 * Channel.java
 *
 * Created on 17 August 2003, 17:15
 */

package bgs.geophys.library.RealTimeData;

/***************************************************************
 * Channel is an interface for classes used to hold details of
 * a single channel on a data system including its name and type.
 *
 * @author  Iestyn Evans
 **************************************************************/
public interface DataChannel {
    
    // publicly accessible methods - replaces properties
    
    /*************************************************************
     * Gets the channel number.
     *
     * @return The channel number
     *************************************************************/
    public int GetChannel();

    /*************************************************************
     * Sets the channel number.
     *
     * @param channel The channel number
     *************************************************************/
    public void SetChannel(int channel);
    
    /*************************************************************
     * Gets the name of the channel.
     *
     * @return The channel name
     *************************************************************/
    public String GetName();
    
    /*************************************************************
     * Sets the name of the channel.
     *
     * @param name The channel name
     *************************************************************/
    public void SetName(String name);
    
    /*************************************************************
     * Gets the type of channel. This would normally include the
     * orientation and an indication of the frequency range.
     *
     * @return The channel type
     *************************************************************/
    public String GetType();
    
    /*************************************************************
     * Sets the type of channel. This would normally include the
     * orientation and an indication of the frequency range.
     *
     * @param type The channel type
     *************************************************************/
    public void SetType(String type);
    
    /*************************************************************
     * Gets the description of the units used for this channel.
     *
     * @return The channel units
     *************************************************************/
    public String GetUnits();
    
    /*************************************************************
     * Sets the description of the units used for this channel.
     *
     * @param units The channel units
     *************************************************************/
    public void SetUnits(String units);
}
