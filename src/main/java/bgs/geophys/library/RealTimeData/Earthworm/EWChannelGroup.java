
package bgs.geophys.library.RealTimeData.Earthworm;

import java.util.*;
import bgs.geophys.library.RealTimeData.*;

/***************************************************************
 * EWChannelGroup is used to hold details of multiple channels
 * on an EW system including its name and type.
 *
 * @author  S. Flower
 * @version 0.0
 **************************************************************/
public class EWChannelGroup extends Object implements DataChannelGroup
{

    // Private fields
    /*************************************************************
     * The EW channel group index
     *************************************************************/
    private int index;
    
    /*************************************************************
     * The name of the channel group.
     *************************************************************/
    private String name;
    
    /*************************************************************
     * The number of channels in this group
     *************************************************************/
    private int n_channels;

    /*************************************************************
     * An array containing the channel numbers of channels in
     * this group
     *************************************************************/
    private int [] channels;

    /*************************************************************
     * Create a new EWChannelGroup object.
     *
     * @param groupIndex: the index of this group
     * @param groupName: the name this group
     * @param nch: the number of channels in this group
     * @param chanString: a string containing a list of the channel
     *                    numbers in this group, separated by '
     *************************************************************/
    public EWChannelGroup(int groupIndex, String groupName, int nch, String chanString)
    {
        StringTokenizer st;
        int i;

        index = groupIndex;
        name = groupName;
        n_channels = nch;

        channels = new int [n_channels];
        st = new StringTokenizer (chanString, "'");

        for (i=0; i < n_channels; i++)
        {
            try
            {
                channels [i] = Integer.parseInt (st.nextToken());
            }
            catch (NumberFormatException e)
            {
                // this will have been dealt with when getting channel details
            }
        }
    }

    /*************************************************************
     * Create a new EWChannelGroup object from an existing one.
     *
     * @param source the existing object
     *************************************************************/
    public EWChannelGroup(EWChannelGroup source)
    {
        index = source.index;
        name = source.name;
        n_channels = source.n_channels;
        channels = source.channels;
    }
    
    
    /*************************************************************
     * Gets the channel group index
     *
     * @return The channel group index
     *************************************************************/
    public int GetIndex()
    {
        return index;
    }
    
    /*************************************************************
     * Sets the channel group index
     *
     * @param index The channel group index
     *************************************************************/
    public void SetIndex(int index)
    {
        this.index = index;
    }
    
    /*************************************************************
     * Gets the name of the channel group.
     *
     * @return The channel group name
     *************************************************************/
    public String GetName()
    {
        return name;
    }
    
    /*************************************************************
     * Sets the name of the channel group.
     *
     * @param name The group name
     *************************************************************/
    public void SetName(String name)
    {
        this.name = name;
    }   
    
    /*************************************************************
     * Gets the number of channels in this group
     *
     * @return The number of channels
     *************************************************************/
    public int GetNChannels()
    {
        return n_channels;
    }

    /*************************************************************
     * Sets the number of channels in this group
     *
     * @param n_channels The number of channels
     *************************************************************/
    public void SetNChannels(int n_channels)
    {
        this.n_channels = n_channels;
    }

    /*************************************************************
     * Gets An array containing the channel numbers of channels in
     * this group
     *
     * @return The channel numbers array
     *************************************************************/
    public int [] GetChannels()
    {
        return channels;
    }   

    /*************************************************************
     * Sets An array containing the channel numbers of channels in
     * this group
     *
     * @param channels The channel numbers array
     *************************************************************/
    public void SetChannels(int[] channels)
    {
        this.channels = channels;
    }
    
}