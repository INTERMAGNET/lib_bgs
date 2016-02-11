/*
 * NetworkUtils.java
 *
 * Created on 20 January 2004, 18:53
 */

package bgs.geophys.library.Misc;

import java.net.*;
import java.util.*;

/**
 * Some network utilities (all static)
 *
 * @author  smf
 */
public class NetworkUtils {
    
    /** List the IP addresses associated with the host that the software runs on
     * @return an array of InetAddress objects */
    public static Vector<InetAddress> getAllInetAddresses()
    {
        Vector<InetAddress> address_list;
        Enumeration network_interface_list, ip_address_list;
        NetworkInterface network_interface;
        InetAddress ip_address;
        
        address_list = new Vector<InetAddress> ();

        try
        {
            network_interface_list = NetworkInterface.getNetworkInterfaces();
            while (network_interface_list.hasMoreElements())
            {
                network_interface = (NetworkInterface) network_interface_list.nextElement();
                ip_address_list = network_interface.getInetAddresses();
                while (ip_address_list.hasMoreElements())
                {
                    ip_address = (InetAddress) ip_address_list.nextElement();
                    address_list.add (ip_address);
                }
            }
        }
        catch (SocketException e) { }
        
        return address_list;
    }
    
    /** Work out if this computer is part of a BGS network
     * @return true if the computer is inside BGS, false otherwise */
    public static boolean isInBgsNetwork ()
    {
        int count;
        Vector<InetAddress> address_list;
        
        address_list = getAllInetAddresses();
        for (count=0; count<address_list.size(); count++)
        {
            if (isInBgsNetwork(address_list.get (count))) return true;
        }
        return false;
    }

    /** Work out if the given Internet address is part of a BGS network
     * @return true if the computer is inside BGS, false otherwise */
    public static boolean isInBgsNetwork (InetAddress address)
    {        
        byte raw_address[];
        int unsigned_raw_address[], count;
        
        raw_address = address.getAddress();
        unsigned_raw_address = new int [raw_address.length];
        for (count=0; count<raw_address.length; count++)
        {
            unsigned_raw_address [count] = raw_address [count];
            if (raw_address [count] < 0) unsigned_raw_address [count] += 256;
        }
        if (unsigned_raw_address.length != 4) return false;
        if (unsigned_raw_address[0] == 192 && unsigned_raw_address[1] == 171 && unsigned_raw_address [2] == 142) return true;
        if (unsigned_raw_address[0] == 192 && unsigned_raw_address[1] == 171 && unsigned_raw_address [2] == 143) return true;
        if (unsigned_raw_address[0] == 192 && unsigned_raw_address[1] == 171 && unsigned_raw_address [2] == 144) return true;
        if (unsigned_raw_address[0] == 192 && unsigned_raw_address[1] == 171 && unsigned_raw_address [2] == 147) return true;
        if (unsigned_raw_address[0] == 192 && unsigned_raw_address[1] == 171 && unsigned_raw_address [2] == 152) return true;
        if (unsigned_raw_address[0] == 194 && unsigned_raw_address[1] == 80  && unsigned_raw_address [2] == 46)  return true;
        return false;
    }
}
