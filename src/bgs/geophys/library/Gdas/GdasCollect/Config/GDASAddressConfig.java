/*
 * GDASAddressConfig.java
 *
 * Created on 04 July 2006, 23:07
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

import bgs.geophys.library.Gdas.GdasCollect.Status.GDASAddressStatus;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds the details of the address of a GDAS system - a
 * GDAS system may have more than one address. This class is immutable
 * so that classes that hold it can be sure that addresses are unique.
 *
 * @author  Administrator
 */
public class GDASAddressConfig 
implements Comparable <GDASAddressConfig>
{

    /** configuration variable: the IP address for the remote SDAS host */
    private String host_name;    
    /** configuration variable: the IP port for the data server on the remote SDAS host */
    private int ip_port;
    /** configuration variable: the time (in seconds) between conection checks for this address */
    private int connection_check_delay;
    /** configuration variable: the number of collection operations between restarts of the SDAS network connection */
    private int sdas_conn_shutdown_period;
    /** configuration variable: timeout (in seconds) for the socket - value of the low level SO_TIMEOUT parameter
     *                          divided by 1000 - a value of 0 means infinite (no) timeout */
    private int socket_timeout;
    /** configuration variable: in use flag - true to use this address, false otherwise
                                this is overridden by the hostname conditional */
    private boolean user_in_use;
    /** configuration variable: hostname check - contains a comma separated list of hostnames
     *                          to match - the address is only used if the machine's hostname
     *                          matches a name from the list - if the list starts with a '!'
     *                          then the address is only used in the machine's hostname does
     *                          not match any name in the list */
    private String hostname_check;
    
    // this variable is set from the user in use flag and the hostname check -
    // it controls whether this address is used or not
    private boolean address_in_use;
    
    /** the associated status object */
    private GDASAddressStatus gdas_address_status;
    
    /** Creates a new instance of GDASDetails */
    public GDASAddressConfig (String host_name) 
    {
        this.host_name = host_name;
        this.ip_port = bgs.geophys.library.RealTimeData.Sdas.SdasNetV0_2.SDAS_DEFAULT_PORT;
        this.connection_check_delay = 3600;
        this.sdas_conn_shutdown_period = 20;
        this.socket_timeout = 0;
        this.user_in_use = true;
        this.hostname_check = "";
        this.gdas_address_status = null;
        setAddressInUse();
    }
        
    /** Creates a new instance of GDASDetails */
    public GDASAddressConfig (String host_name, int ip_port) 
    {
        this.host_name = host_name;
        this.ip_port = ip_port;
        this.connection_check_delay = 3600;
        this.sdas_conn_shutdown_period = 20;
        this.socket_timeout = 0;
        this.user_in_use = true;
        this.hostname_check = "";
        this.gdas_address_status = null;
        setAddressInUse();
    }
    
    /** Creates a new instance of GDASDetails */
    public GDASAddressConfig (String host_name, int ip_port,
                              int connection_check_delay,
                              int sdas_conn_shutdown_period,
                              int socket_timeout,
                              boolean in_use,
                              String hostname_check) 
    {
        this.host_name = host_name;
        this.ip_port = ip_port;
        this.connection_check_delay = connection_check_delay;
        this.sdas_conn_shutdown_period = sdas_conn_shutdown_period;
        this.socket_timeout = socket_timeout;
        this.user_in_use = in_use;
        this.hostname_check = hostname_check;
        this.gdas_address_status = null;
        setAddressInUse();
    }
        
    /** construct a new Config as a copy of another */
    public GDASAddressConfig(GDASAddressConfig copy)
    {
        this.host_name = new String (copy.host_name);
        this.ip_port = copy.ip_port;
        this.connection_check_delay = copy.connection_check_delay;
        this.sdas_conn_shutdown_period = copy.sdas_conn_shutdown_period;
        this.socket_timeout = copy.socket_timeout;
        this.hostname_check = copy.hostname_check;
        this.user_in_use = copy.user_in_use;
        this.gdas_address_status = copy.gdas_address_status;
        setAddressInUse();
    }
   
    // read methods for configuration variables
    public String getHostName() { return host_name; }
    public int getIpPort() { return ip_port; }
    public int getConnectionCheckDelay () { return connection_check_delay; }
    public int getSdasConnectionShutdownPeriod() { return sdas_conn_shutdown_period; }
    public int getSocketTimeout() { return socket_timeout; }
    public boolean isUserInUseFlag() { return user_in_use; }
    public boolean isAddressInUse () { return address_in_use; }
    public String getHostnameCheck () { return hostname_check; }
    public GDASAddressStatus getGdasAddressStatus () { return gdas_address_status; }

    // write methods for configuration vaiables
    public void setGdasAddressStatus (GDASAddressStatus gdas_address_status) { this.gdas_address_status = gdas_address_status; }
    
    /** show this class as a string */
    @Override
    public String toString () { return host_name + ":" + Integer.toString (ip_port); }

    /** see if this address is equal to another */
    public boolean equals (GDASAddressConfig o)
    {
        if (compareTo (o) == 0) return true;
        return false;
    }

    public int compareTo(GDASAddressConfig o) 
    {
        int ret_val;
        
        ret_val = host_name.compareTo(o.host_name);
        if (ret_val == 0)
        {
            if (ip_port < o.ip_port) ret_val = -1;
            else if (ip_port > o.ip_port) ret_val = 1;
        }
        return ret_val;
    }
    
    /** call after fields have been set - this routine
     * then sets the address in use flag */
    public void setAddressInUse ()
    {
        int n_check_names;
        String machine_name, test_name;
        StringTokenizer tokens;
        boolean negate, found;

        // set the address in use flag from the user's flag
        address_in_use = user_in_use;
        
        // get the local machine name
        try 
        {
            machine_name = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) 
        {
            machine_name = null; 
        }

        // check the hostname_check attribute was loaded and that the machine name is available
        // also check we haven't already turned off this address
        if ((machine_name != null) && (hostname_check != null) && address_in_use)
        {
            // check for a "!" at the start of the hostname check string
            test_name = hostname_check.trim();
            negate = false;
            if (test_name.startsWith("!"))
            {
                negate = true;
                test_name = test_name.replace("!", "");
            }
        
            // extract individual test names from the hostname check string
            tokens = new StringTokenizer (test_name, ",");
            n_check_names = tokens.countTokens();
            found = false;
            while (tokens.hasMoreTokens())
            {
                test_name = tokens.nextToken().trim();
                if (machine_name.equalsIgnoreCase(test_name)) found = true;
            }

            // do we need to override the in_use false
            if (negate)
            {
                if (found) address_in_use = false;
            }
            else
            {
                if ((n_check_names > 0) && (! found)) address_in_use = false;
            }
        }
    }
    
}
