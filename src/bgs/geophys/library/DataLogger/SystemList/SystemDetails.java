/*
 * SystemDetails.java
 *
 * Created on 31 May 2004, 14:54
 */

package bgs.geophys.library.DataLogger.SystemList;

import org.w3c.dom.*;
import java.util.*;

import bgs.geophys.library.ssh.*;

/**
 * Holds details of an individual data logger
 * @author  Administrator
 */
public class SystemDetails 
{
    
    /** the type of system (used when connecting to the data server),
     * e.g. 'sdas' or 'earthworm' - the string must be recognied by
     * the protocol detector */
    private String protocol_name;
    
    /** the name that the user sees */
    private String display_name;
    
    /** the public IP address to connect to */
    private String ip_address;

    /** the private IP address to connect to */
    private String ip_address_priv;
    
    /** the data server IP port */
    private int data_ip_port;
    
    /** the telnet server IP port */
    private int telnet_ip_port;
    
    /** the ssh server string */
    private String ssh_server;
    
    /** the type of ping - first possible codes then the value for this object */
    public final static int PING_NONE=1;
    public final static int PING_TELNET=2;
    public final static int PING_SDAS=3;
    private int ping_type;
    
    /** an array of channel groups */
    private Vector<ChannelGroupDetails> groups;
    
    /** construct a new system
     * @param protocol_name the type of system
     * @param display_name the name the user sees to identify the system
     * @param ip_address the system ip address
     * @param data_ip_port the IP port for the data server
     * @param telnet_ip_port the telnet listening port on the server
     * @param ssh_server the ssh server details
     * @param ping_type the type of ping to use */
    public SystemDetails(String protocol_name, String display_name, String ip_address,
                         int data_ip_port, int telnet_ip_port, String ssh_server, int ping_type) 
    {
        this.protocol_name = protocol_name;
        this.display_name = display_name;
        this.ip_address = ip_address;
        this.data_ip_port = data_ip_port;
        this.telnet_ip_port = telnet_ip_port;
        this.ssh_server = ssh_server;
        this.ping_type = ping_type;
        groups = new Vector<ChannelGroupDetails> ();
    }

    /** construct a new system with private IP
     * @param protocol_name the type of system
     * @param display_name the name the user sees to identify the system
     * @param ip_address the system ip address
     * @param ip_address_priv this system private ip address
     * @param data_ip_port the IP port for the data server
     * @param telnet_ip_port the telnet listening port on the server
     * @param ssh_server the ssh server details
     * @param ping_type the type of ping to use */
    public SystemDetails(String protocol_name, String display_name, String ip_address, String ip_address_priv,
                         int data_ip_port, int telnet_ip_port, String ssh_server, int ping_type)
    {
        // call default constructor
        this(protocol_name, display_name, ip_address, data_ip_port, telnet_ip_port, ssh_server, ping_type);
        this.ip_address_priv = ip_address_priv;
    }

    
    /** construct a new system from a document
     * @params system_node the node containing the system
     * @throws SystemListException if there is a parsing error */
    public SystemDetails (Node system_node)
    throws SystemListException
    {
        NamedNodeMap node_map;
        Node node;
        String string;
        
        // flag all data as missing
        protocol_name = null;
        display_name = null;
        ip_address = null;
        ip_address_priv = null;
        data_ip_port = -1;
        telnet_ip_port = 23;
        ssh_server = null;
        ping_type = PING_NONE;

        // check that this node is a system
        if (! system_node.getNodeName().equalsIgnoreCase("system"))
            throw new SystemListException ("Not a system");
        
        // get the system name (if any)
        node_map = system_node.getAttributes();
        if (node_map != null)
        {
            node = node_map.getNamedItem ("name");
            if (node != null)
            {
                try { display_name = node.getNodeValue(); }
                catch (DOMException e) { }
            }
        }
        
        // walk through the children, extracting parameters
        groups = new Vector<ChannelGroupDetails> ();
        for (node=system_node.getFirstChild(); node != null; node=node.getNextSibling())
        {
            // parse the element
            try
            {
                if (node.getNodeName().equalsIgnoreCase("ip-address"))
                    ip_address = node.getFirstChild().getNodeValue();
                else if (node.getNodeName().equalsIgnoreCase("ip-address-priv"))
                    ip_address_priv = node.getFirstChild().getNodeValue();
                else if (node.getNodeName().equalsIgnoreCase("data-port"))
                    data_ip_port = Integer.parseInt (node.getFirstChild().getNodeValue());
                else if (node.getNodeName().equalsIgnoreCase("telnet-port"))
                    telnet_ip_port = Integer.parseInt (node.getFirstChild().getNodeValue());
                else if (node.getNodeName().equalsIgnoreCase("ssh-server"))
                    ssh_server = node.getFirstChild().getNodeValue();
                else if (node.getNodeName().equalsIgnoreCase("ping-type"))
                {
                    string = node.getFirstChild().getNodeValue();
                    if (string.equalsIgnoreCase("telnet"))
                        ping_type = PING_TELNET;
                    else if (string.equalsIgnoreCase("sdas"))
                        ping_type = PING_SDAS;
                    else if (string.equalsIgnoreCase("none"))
                        ping_type = PING_NONE;
                    else
                        throw new SystemListException ("Error with system description");
                }
                else if (node.getNodeName().equalsIgnoreCase("protocol-name"))
                    protocol_name = node.getFirstChild().getNodeValue();
                else if (node.getNodeName().equalsIgnoreCase("channel-group"))
                    groups.add (new ChannelGroupDetails (node));
            }
            catch (Exception e)
            {
                throw new SystemListException ("Error with system description");
            }
        }
        
        // check that compulsory parameters were filled in
        if (ip_address == null)
            throw new SystemListException ("Missing IP address");
        if (data_ip_port < 0)
            throw new SystemListException ("Missing data port number");
        if (display_name == null) display_name = ip_address;
    }
    
    /** Getter for property protocol_name.
     * @return Value of property protocol_name. */
    public String getProtocolName() { return protocol_name; }
    
    /** Getter for property display_name.
     * @return Value of property display_name. */
    public String getDisplayName() { return display_name;  }
    
    /** Getter for property ip_address.
     * @return Value of property ip_address. */
    public String getIpAddress() { return ip_address; }

    /** Getter for property ip_address.
     * @return Value of property ip_address. */
    public String getPrivIpAddress() { return ip_address_priv; }
    
    /** Getter for property data_ip_port.
     * @return Value of property data_ip_port. */
    public int getDataIpPort() { return data_ip_port; }
    
    /** Getter for property data_ip_port.
     * @return Value of property telnet_ip_port. */
    public int getTelnetIpPort() { return telnet_ip_port; }
    
    /** Getter for property ssh_server.
     * @return Value of property ssh_server. */
    public String getSshServer() { return ssh_server; }
    
    /** find out whether this object holds a valid ssh server string
     * @return true if there is a valid ssh server */
    public boolean isSshServerValid ()
    {
        return sshTunnel.isHostnameValid (ssh_server);
    }
    
    /** The syntax for the ssh server string is: user@host:port
     * @return the username portion of the ssh server string - may be null */
    public String getSshServerUsername()
    {
        return sshTunnel.findUsernameInHostname (ssh_server);
    }
    
    /** The syntax for the ssh server string is: user@host:port
     * @return the port portion of the ssh server string - may be null */
    public int getSshServerPort()
    {
        return sshTunnel.findPortInHostname (ssh_server);
    }
    
    /** The syntax for the ssh server string is: user@host:port
     * @return the host portion of the ssh server string - may be null */
    public String getSshServerHostname()
    {       
        return sshTunnel.findHostInHostname (ssh_server);
    }
    
    /** Getter for property ping_type.
     * @return Value of property ping_type. */
    public int getPingType() { return ping_type; }
    
    /** find the number of channel groups
     * @return the number of channel groups */
    public int getNChannelGroups () { return groups.size(); }
    
    /** return a channel group
     * @params index the index of the group (0..N-1)
     * @return the group (or null if index is out of bounds */
    public ChannelGroupDetails getChannelGroupDetails (int index)
    {
        if (index < 0 || index >= groups.size()) return null;
        return (ChannelGroupDetails) groups.get (index);
    }
}

