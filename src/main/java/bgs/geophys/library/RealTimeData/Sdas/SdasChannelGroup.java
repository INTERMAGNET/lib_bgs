
package bgs.geophys.library.RealTimeData.Sdas;

import java.util.*;
import bgs.geophys.library.RealTimeData.*;

/***************************************************************
 * SdasChannel is used to hold details of a single channel
 * on an SDAS system including its name and type.
 *
 * @author  S. Flower
 * @version 0.0
 **************************************************************/
public class SdasChannelGroup extends Object implements DataChannelGroup
{

    // private fields
    /*************************************************************
     * The SDAS channel group index
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
     * Create a new SdasChannelGroup object.
     *
     * @param groupIndex: the index of this group
     * @param groupName: the name this group
     * @param nch: the number of channels in this group
     * @param chanString: a string containing a list of the channel
     *                    numbers in this group, separated by '
     * @throws SdasNetException if there is an error
     *************************************************************/
    public SdasChannelGroup (int groupIndex, String groupName,
                             int nch, String chanString)
    throws SdasNetException
    {
        String string;
        StringTokenizer st;
        int i;

        index = groupIndex;
        name = groupName;
        n_channels = nch;

        channels = new int [n_channels];
        st = new StringTokenizer (chanString, "'");

        for (i=0; i < n_channels; i++)
        {
            string = st.nextToken ();
            try
            {
                channels [i] = Integer.parseInt (string);
            }
            catch (NumberFormatException e)
            {
                throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Bad channel [" + string + "]");
            }
        }
    }

    /*************************************************************
     * Create a new SDAS Channel object from an existing one.
     *
     * @param source the existing object
     *************************************************************/
    public SdasChannelGroup (SdasChannelGroup source)
    {
        index = source.index;
        name = source.name;
        n_channels = source.n_channels;
        channels = source.channels;
    }
    
    /** Gets An array containing the channel numbers of channels in
     * this group
     *
     * @return The channel numbers array
     */
    public int[] GetChannels() {
        return channels;
    }
    
    /** Gets the channel group index
     *
     * @return The channel group index
     */
    public int GetIndex() {
        return index;
    }
    
    /** Gets the number of channels in this group
     *
     * @return The number of channels
     */
    public int GetNChannels() {
        return n_channels;
    }
    
    /** Gets the name of the channel group.
     *
     * @return The channel group name
     */
    public String GetName() {
        return name;
    }
    
    /** Sets An array containing the channel numbers of channels in
     * this group
     *
     * @param channels The channel numbers array
     */
    public void SetChannels(int[] channels) {
        this.channels = channels;
    }
    
    /** Sets the channel group index
     *
     * @param index The channel group index
     */
    public void SetIndex(int index) {
        this.index = index;
    }
    
    /** Sets the number of channels in this group
     *
     * @param n_channels The number of channels
     */
    public void SetNChannels(int n_channels) {
        this.n_channels = n_channels;
    }
    
    /** Sets the name of the channel group.
     *
     * @param name The group name
     */
    public void SetName(String name) {
        this.name = name;
    }
    
}