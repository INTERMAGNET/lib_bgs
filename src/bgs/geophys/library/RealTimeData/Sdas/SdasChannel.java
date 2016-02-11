
package bgs.geophys.library.RealTimeData.Sdas;

import bgs.geophys.library.RealTimeData.*;

/***************************************************************
 * SdasChannel is used to hold details of a single channel
 * on an SDAS system including its name and type.
 *
 * @author  S. Flower
 * @version 0.0
 **************************************************************/
public class SdasChannel extends Object implements DataChannel {

    /*************************************************************
     * The Sdas channel number.
     *************************************************************/
    private int channel;
    
    /*************************************************************
     * The name of the channel.
     *************************************************************/
    private String name;
    /*************************************************************
     * The type of channel. This would normally include the
     * orientation and an indication of the frequency range.
     *************************************************************/
    private String type;
    /*************************************************************
     * A description of the units used for thois channel.
     *************************************************************/
    private String units;
    
    /*************************************************************
     * Create a new Sdas Channel object.
     *
     * @param ChannelNumber the EW channel number
     * @param ChannelName the name the channel is known by in EW
     * @param ChannelType a code for the type of channel
     * @param ChannelUnits the name of the physical units for
     *                      the channel's data
     *************************************************************/
    public SdasChannel(int ChannelNumber, String ChannelName,
                       String ChannelType, String ChannelUnits)
    {
        channel = ChannelNumber;
        name = ChannelName;
        type = ChannelType;
        units = ChannelUnits;
    }
    
    /*************************************************************************
     *
     * Constructor
     *
     ************************************************************************/
    public SdasChannel()
    {
        // Need to look at....
        channel = 0;
        name = "";
        type = "";
        units = "";
    }
    
    /*************************************************************
     * Gets the channel number.
     *
     * @return The channel number
     *************************************************************/
    public int GetChannel()
    {
        return channel;
    }

    /*************************************************************
     * Sets the channel number.
     *
     * @param channel The channel number
     *************************************************************/
    public void SetChannel(int channel)
    {
        this.channel = channel;
    }   
    
    /*************************************************************
     * Gets the name of the channel.
     *
     * @return The channel name
     *************************************************************/
    public String GetName()
    {
        return name;
    }
    
    /*************************************************************
     * Sets the name of the channel.
     *
     * @param name The channel name
     *************************************************************/
    public void SetName(String name)
    {
        this.name = name;
    }
    
    /*************************************************************
     * Gets the type of channel. This would normally include the
     * orientation and an indication of the frequency range.
     *
     * @return The channel type
     *************************************************************/
    public String GetType()
    {
        return type;
    }
    
    /*************************************************************
     * Sets the type of channel. This would normally include the
     * orientation and an indication of the frequency range.
     *
     * @param type The channel type
     *************************************************************/
    public void SetType(String type)
    {
        this.type = type;
    }
    
    /*************************************************************
     * Gets the description of the units used for this channel.
     *
     * @return The channel units
     *************************************************************/
    public String GetUnits()
    {
        return units;
    }
    
    /*************************************************************
     * Sets the description of the units used for this channel.
     *
     * @param units The channel units
     *************************************************************/
    public void SetUnits(String units)
    {
        this.units = units;
    }
    
    /*************************************************************
     * Create a new SDAS Channel object from an existing one.
     *
     * @param source the existing object
     *************************************************************/
    public SdasChannel(SdasChannel source)
    {
        channel = source.channel;
        name = source.name;
        type = source.type;
        units = source.units;
    }
}