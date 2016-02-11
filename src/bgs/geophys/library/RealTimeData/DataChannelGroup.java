/*
 * ChannelGroup.java
 *
 * Created on 17 August 2003, 17:15
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;

/***************************************************************
 * ChannelGroup is an interface for classes used to hold a set 
 * of channel details from a data system.
 *
 * @author  Iestyn Evans
 **************************************************************/
public interface DataChannelGroup {
    
    // publicly accessible methods - replaces fields
    
    /*************************************************************
     * Gets the channel group index
     *
     * @return The channel group index
     *************************************************************/
    public int GetIndex();
    
    /*************************************************************
     * Sets the channel group index
     *
     * @param index The channel group index
     *************************************************************/
    public void SetIndex(int index);
    
    /*************************************************************
     * Gets the name of the channel group.
     *
     * @return The channel group name
     *************************************************************/
    public String GetName();
    
    /*************************************************************
     * Sets the name of the channel group.
     *
     * @param name The group name
     *************************************************************/
    public void SetName(String name);
    
    /*************************************************************
     * Gets the number of channels in this group
     *
     * @return The number of channels
     *************************************************************/
    public int GetNChannels();

    /*************************************************************
     * Sets the number of channels in this group
     *
     * @param n_channels The number of channels
     *************************************************************/
    public void SetNChannels(int n_channels);

    /*************************************************************
     * Gets An array containing the channel numbers of channels in
     * this group
     *
     * @return The channel numbers array
     *************************************************************/
    public int [] GetChannels();

    /*************************************************************
     * Sets An array containing the channel numbers of channels in
     * this group
     *
     * @param channels The channel numbers array
     *************************************************************/
    public void SetChannels(int[] channels);
    
}
