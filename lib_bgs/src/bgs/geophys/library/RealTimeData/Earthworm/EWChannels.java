/*
 * EWChannels.java
 *
 * Created on 17 July 2003, 11:01
 */

package bgs.geophys.library.RealTimeData.Earthworm;

import java.util.*;
import bgs.geophys.library.RealTimeData.*;
import java.io.*;

/**************************************************************************
 * Class to hold response values from a MENU: request as a list of
 *                      EWChannel objects.
 *
 * @author  Iestyn Evans
 *************************************************************************/
public class EWChannels extends Object
{
    // Private Variables
    private int SIZE_OF_RESPONSE;
    private List<EWChannel> channels;
    
    // Public Variables
    /**************************************************************************
     * Holds the request ID used to gather this data.
     *************************************************************************/
    public String requestID;
    
    /**************************************************************************
     * Holds values of flags returned in the response that gathered this data.
     *************************************************************************/
    public String flags;
    
    /**************************************************************************
     *
     * Create a new EWChannels object.
     *
     *************************************************************************/
    public EWChannels()
    {
        channels = new LinkedList<EWChannel>();
        SIZE_OF_RESPONSE = new EWChannel().getSizeOfResponse();
    }
    
    /**************************************************************************
     * Sets the Request ID generated for the request.
     *
     * @param requestID The request ID used.
     *************************************************************************/
    public void setRequestID( String requestID )
    {
        this.requestID = requestID;
    }

    /**************************************************************************
     * Adds a new channel to the list from a string array.
     *
     * @param trace The array that contains a channels information.
     *************************************************************************/
    public boolean add( String[] trace )
    {
        // Check that the array size is as expected
        if ( trace.length != SIZE_OF_RESPONSE ) {
            return false;
        }
        
        // Create a new EWChannel to hold the information
        EWChannel channel = new EWChannel();
        
        // Add information to the object
        channel.pin = trace[0];
        channel.siteCode = trace[1];
        channel.channelCode = trace[2];
        channel.networkID = trace[3];
        channel.dataType = trace[6];
        
        // Parse date values
        try {
            channel.startTime.setTime((long)(Double.parseDouble(trace[4])*1000));
            channel.endTime.setTime((long) (Double.parseDouble(trace[5]) * 1000));
        }
        catch ( NumberFormatException nfx ) {
            // Perhaps throw an EWNetException??
            return false;
        }

        // Set interface compatibility details i.e. number, name, type etc...
        channel.SetChannel(channels.size());
        channel.SetName(channel.siteCode + " " + channel.channelCode + " "
                        + channel.networkID);
        channel.SetType("");
        channel.SetUnits("");
        
        // Add object to collection
        return channels.add(channel);
    }
    
    /*************************************************************************
     * Returns the size of the channel information (i.e. the number of
     *                      items expected).
     *
     * @return Size of channel information.
     ************************************************************************/
    public int getSizeOfResponse()
    {
        return SIZE_OF_RESPONSE;
    }
    
    /*************************************************************************
     *
     * Clear all held channels from memory.
     *
     ************************************************************************/
    public void clear() 
    {
        requestID = null;
        flags = null;
        channels.clear();
    }
    
    /*************************************************************************
     * Returns the number of channels.
     *
     * @return Number of channels.
     ************************************************************************/
    public int size() 
    {
        return channels.size();
    }

    /*************************************************************************
     * Returns an EWChannel object using the given index. An empty object is
     *                      returned if the index is invalid.
     *
     * @param index The index of the channel to be returned.
     *
     * @return Size of channel information.
     ************************************************************************/
    public EWChannel get(int index) 
    {
        if ( index >= size() ) {
            // Index is invalid - perhaps throw an EWNetException??
            return new EWChannel();
        }
 
        return (EWChannel)channels.get(index);
    }
    
    
    /*************************************************************
     * Prints details of all channels in group
     *
     * @param out The stream to print to
     *************************************************************/
    public void print(PrintStream out)
    throws IOException
    {
        out.println("Number of channels: " + channels.size());
        out.println();
        for (int i = 0; i < channels.size(); i++)
            ((EWChannel)channels.get(i)).print(out);
        out.println();
    }
}
