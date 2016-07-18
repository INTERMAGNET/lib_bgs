/*
 * ChannelGroupDetails.java
 *
 * Created on 31 May 2004, 21:09
 */

package bgs.geophys.library.DataLogger.SystemList;

import java.util.*;
import org.w3c.dom.*;

/**
 *
 * @author  Administrator
 */
public class ChannelGroupDetails 
{

    /** an inner class to hold details on a single channel */
    public class ChannelDetails
    {
        /** a channel may be specified by identifier (= station-name + ' ' + channel-type-code) */
        public int channel_no;          // the channel number
        
        /** if a channel is not specified by name, it must be specified by channel number */
        public String channel_ident;    // the channel name ('station type')
        
        public ChannelDetails (String channel_ident)
        {
            this.channel_no = -9999;
            this.channel_ident = channel_ident;
        }
        
        public ChannelDetails (int channel_no)
        {
            this.channel_no = channel_no;
            this.channel_ident = null;
        }
    }

    
    // the name of this group
    private String name;
    
    // an array of channels which form this group
    private Vector<ChannelDetails> channel_list;
    
    /** Creates a new instance of ChannelGroupDetails
     * @param name the name of this group */
    public ChannelGroupDetails (String name)
    {
        this.name = name;
        channel_list = new Vector<ChannelDetails> ();
    }

    /** construct a new group from a document
     * @params group_node the node containing the group
     * @throws SystemListException if there is a parsing error */
    public ChannelGroupDetails (Node system_node)
    throws SystemListException
    {
        NamedNodeMap node_map;
        Node node, channel_node;
        ChannelDetails new_channel;
        String value;
        
        // check that this node is a group
        if (! system_node.getNodeName().equalsIgnoreCase("channel-group"))
            throw new SystemListException ("Not a group");
        
        // get the group name (if any)
        name = "No name";
        node_map = system_node.getAttributes();
        if (node_map != null)
        {
            node = node_map.getNamedItem ("name");
            if (node != null)
            {
                try { name = node.getNodeValue(); }
                catch (DOMException e) { }
            }
        }
        
        // walk through the children, extracting channel details
        channel_list = new Vector<ChannelDetails> ();
        for (node=system_node.getFirstChild(); node != null; node=node.getNextSibling())
        {
            // parse the element
            try
            {
                if (node.getNodeName().equalsIgnoreCase("channel"))
                {
                    value = node.getFirstChild().getNodeValue();
                    try
                    {
                        new_channel = new ChannelDetails (Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        new_channel = new ChannelDetails (value);
                    }
                    channel_list.add (new_channel);
                }
            }
            catch (Exception e)
            {
                throw new SystemListException ("Error with group description");
            }
        }
        
    }
    
    /** Add a channel to this group 
     * @params index the channel index */
    public void addChannel (int index)
    {
        channel_list.add (new ChannelDetails (index));
    }
    
    /** Add a channel to this group 
     * @params name the channel name */
    public void addChannel (String name)
    {
        channel_list.add (new ChannelDetails (name));
    }
    
    /** find the name of this group
     * @return the name */
    public String getName () { return name; }
    
    /** find the number of channels in this group
     * @return the number of channels */
    public int getNChannels () { return channel_list.size(); }
    
    /** get the channel details for the given channel index
     * @param index the channel index (0..N-1)
     * @return the channel details (or null if index out of range) */
    public ChannelDetails getChannelDetails (int index)
    {
        if (index<0 || index>=channel_list.size()) return null;
        return (ChannelDetails) channel_list.get (index);
    }
    
}
