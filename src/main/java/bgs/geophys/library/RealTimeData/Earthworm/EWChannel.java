/*
 * ResponseMenuSingle.java
 *
 * Created on 17 July 2003, 13:08
 */

package bgs.geophys.library.RealTimeData.Earthworm;

import java.util.Date;
import bgs.geophys.library.RealTimeData.*;
import java.io.*;

/*************************************************************************
 * Class to hold header information on a single EarthWorm trace.
 *
 * @author  Iestyn Evans
 ************************************************************************/

public class EWChannel extends Object implements DataChannel
{
    // Private Variables
    /*************************************************************
     * The number of fields expected in the response.
     *************************************************************/
    private final int SIZE_OF_RESPONSE = 7;
    
    // Public Variables - Earthworm only...
    /*************************************************************
     * The pin used to represent this channel.
     *************************************************************/
    public String pin;
    
    /*************************************************************
     * The site code for this channel.
     *************************************************************/
    public String siteCode;
    
    /*************************************************************
     * The channel code for this channel.
     *************************************************************/
    public String channelCode;
    
    /*************************************************************
     * The network ID for this channel.
     *************************************************************/
    public String networkID;
    
    /*************************************************************
     * The start time for data available on this channel.
     *************************************************************/
    public Date startTime;
    
    /*************************************************************
     * The end time for data available on this channel.
     *************************************************************/
    public Date endTime;
    
    /*************************************************************
     * The data-type code for the data format used on this channel.
     *************************************************************/
    public String dataType;
    
    
    // private variables for Sdas compatibility
    /*************************************************************
     * The EW channel number.
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
     * Create a new EW Channel object.
     *
     * @param ChannelNumber the EW channel number
     * @param ChannelName the name the channel is known by in EW
     * @param ChannelType a code for the type of channel
     * @param ChannelUnits the name of the physical units for
     *                      the channel's data
     *************************************************************/
    public EWChannel(int ChannelNumber, String ChannelName,
                       String ChannelType, String ChannelUnits)
    {
        channel = ChannelNumber;
        name = ChannelName;
        type = ChannelType;
        units = ChannelUnits;
    }
    
    /*************************************************************************
     *
     * Create a new EW Channel object.
     *
     ************************************************************************/
    public EWChannel()
    {
        // Create date objects
        startTime = new Date();
        endTime = new Date();
        
        // Need to look at....
        channel = 0;
        name = "";
        type = "";
        units = "";
    }
    
    /*************************************************************************
     * Copies data from one EWChannel object into another.
     *
     * @param copy The object to copy from.
     ************************************************************************/
    public EWChannel(EWChannel copy)
    {
      pin = copy.pin;
      siteCode = copy.siteCode;
      channelCode = copy.channelCode;
      networkID = copy.networkID;
      startTime = copy.startTime;
      endTime = copy.endTime;
      dataType = copy.dataType;
      channel = copy.channel;
      name = copy.name;
      type = copy.type;
      units = copy.units;
    }
    
    /*************************************************************************
     * Returns the size of the channel information (i.e. the number of items
     *                      expected).
     * @return Size of channel information.
     ************************************************************************/
    public int getSizeOfResponse()
    {
        return SIZE_OF_RESPONSE;
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
     * Prints details of this channel
     *
     * @param out The stream to print to
     *************************************************************/
    public void print(PrintStream out)
    throws IOException
    {
        out.println();
        out.println("Name:         " + name);
        out.println("Pin:          " + pin);
        out.println("Site code:    " + siteCode);
        out.println("Channel code: " + channelCode);
        out.println("Network ID:   " + networkID);
        out.println("Start time:   " + startTime.getTime());
        out.println("End time:     " + endTime.getTime());
        out.println("Data type:    " + dataType);
        out.println();
    }
}
