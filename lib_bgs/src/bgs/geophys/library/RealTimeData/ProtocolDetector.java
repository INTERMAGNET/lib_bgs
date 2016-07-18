/*
 * ProtocolDetector.java
 *
 * Created on 25 August 2003, 19:38
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;

// Import new data suites here
import bgs.geophys.library.RealTimeData.Earthworm.*;
import bgs.geophys.library.RealTimeData.Sdas.*;

/*****************************************************************
 * ProtocolDetector attempts to discover if a server is running
 *              a known data protocol and 
 *              returns the correct Net object, opened and ready
 *              to receive commands. 
 *
 * @author  Iestyn Evans
 ****************************************************************/
public class ProtocolDetector extends Object {

    private String lastHost;
    private int lastProtocol;
    
    // Values used to represent protocol types - add new values here
    private static final int PROTOCOL_NONE = 0,
                             PROTOCOL_EARTHWORM = 1,
                             PROTOCOL_SDAS_V0_2 = 2,
                             PROTOCOL_SDAS_V0_3 = 3;
    private boolean compress = true;
  
    // Earthworm initial connection bug...
    /** Wether the thread should sleep between opening network connections This is
     * part of the workround for the Earthworm connection bug */
    private static final boolean WORKROUND_EW = true;
    /** Time for thread to sleep between opening network connections. This is
     * part of the workround for the Earthworm connection bug */
    private static final int WORKROUND_EW_TIME = 50;
    
    
    /*****************************************************************
     *
     * Creates a new instance of ProtocolDetector
     *
     ****************************************************************/
    public ProtocolDetector()
    {
        lastProtocol = PROTOCOL_NONE;
        lastHost = "";
    }
    
    /*****************************************************************
     * Attempts to detect the type of protocol being used by a server.
     *      If no port is specified the default port for each protocol
     *      is used. A valid Net object is returned if the protocol
     *      is detected. The returned object is not connected.
     *
     * @param host_address The address of the server to connect to. A string
     * in the form <host_address>[:port][,system] where system (if specified)
     * is either SDAS or EARTHWORM
     * @param ssh_server The address of an SSH server to use for connection.
     * This is a string in the form username@ssh_server:ssh_port
     * @param compress true to compress data on the link, false to
     * not use compression
     *
     * @return A valid Net object for the detected protocol.The net object
     * is open and ready to receive commands.
     *
     * @throws NetException If no valid protocol can be detected.
     ****************************************************************/
    public DataNet detectProtocol(String host_address, String ssh_server, boolean compress)
    throws DataNetException
    {
        this.compress = compress;
        return detectProtocol(host_address, ssh_server);
    }
    
    /*****************************************************************
     * Attempts to detect the type of protocol being used by a server.
     *      If no port is specified the default port for each protocol
     *      is used. A valid Net object is returned if the protocol
     *      is detected. The returned object is not connected.
     *
     * @param host_address The address of the server to connect to. A string
     * in the form <host_address>[:port][,system] where system (if specified)
     * is either SDAS or EARTHWORM
     * @param ssh_server The address of an SSH server to use for connection.
     * This is a string in the form username@ssh_server:ssh_port
     *
     * @return A valid Net object for the detected protocol.The net object
     * is open and ready to receive commands.
     *
     * @throws NetException If no valid protocol can be detected.
     ****************************************************************/
    public DataNet detectProtocol(String host_address, String ssh_server)
    throws DataNetException
    {
        DataNet testNet;
        int protocol;
        String hostname, string;
        StringTokenizer tokens;
        
        // if the system was given in the address, then strip it out
        hostname = host_address;
        protocol = PROTOCOL_NONE;
        tokens = new StringTokenizer (host_address, ",");
        switch (tokens.countTokens())
        {
        case 1:
            break;
        case 2:
            hostname = tokens.nextToken();
            string = tokens.nextToken();
            if (string.equalsIgnoreCase("sdas")) protocol = PROTOCOL_SDAS_V0_2;
            else if (string.equalsIgnoreCase("sdas0.2")) protocol = PROTOCOL_SDAS_V0_2;
            else if (string.equalsIgnoreCase("sdas0.3")) protocol = PROTOCOL_SDAS_V0_3;
            else if (string.equalsIgnoreCase("earthworm")) protocol = PROTOCOL_EARTHWORM;
            else if (string.equalsIgnoreCase("ew")) protocol = PROTOCOL_EARTHWORM;
            else if (string.equalsIgnoreCase("none")) protocol = PROTOCOL_NONE;
            else if (string.equalsIgnoreCase("unknown")) protocol = PROTOCOL_NONE;
            else if (string.equalsIgnoreCase("automatic")) protocol = PROTOCOL_NONE;
            else if (string.equalsIgnoreCase("auto")) protocol = PROTOCOL_NONE;
            else
                throw new DataNetException (DataNetException.BAD_NETWORK_ADDRESS, "System type (given as [" + string +"]) must be 'SDAS' or 'EARTHWORM'");
            break;
        default:
            throw new DataNetException (DataNetException.BAD_NETWORK_ADDRESS, "Bad network address [" + host_address + "] - format is <address>[:port][,system]");
        }
        
        // Earthworm
        if ((protocol == PROTOCOL_EARTHWORM) || (protocol == PROTOCOL_NONE))
        {
            try {
                testNet = new EWNet();
                testNet.Open (hostname, ssh_server, compress);
                if (testNet.isProtocol())
                {
                    lastProtocol = PROTOCOL_EARTHWORM;
                    lastHost = hostname;
                    
                    // Initial connection workround
                    if (WORKROUND_EW)
                    {
                        Thread.currentThread().sleep(WORKROUND_EW_TIME);
                    }
                    
                    return testNet;
                }
                testNet.Close();
            }
            catch (Exception e) { }
        }
        
        // Sdas V0.3
        if ((protocol == PROTOCOL_SDAS_V0_3) || (protocol == PROTOCOL_NONE))
        {
            try {
                testNet = new SdasNetV0_3 ();
                testNet.Open(hostname, ssh_server, compress);
                if (testNet.isProtocol())
                {
                    lastProtocol = PROTOCOL_SDAS_V0_3;
                    lastHost = hostname;
                    return testNet;
                }
                testNet.Close();
            }
            catch (Exception e) { }
        }

        // Sdas V0.2
        if ((protocol == PROTOCOL_SDAS_V0_2) || (protocol == PROTOCOL_NONE))
        {
            try {
                testNet = new SdasNetV0_2();
                testNet.Open(hostname, ssh_server, compress);
                if (testNet.isProtocol())
                {
                    lastProtocol = PROTOCOL_SDAS_V0_2;
                    lastHost = hostname;
                    return testNet;
                }
                testNet.Close();
            }
            catch (Exception e) { }
        }

        // Add new protocols here
        
        
        // If here then no valid protocols have been found
        lastProtocol = PROTOCOL_NONE;
        lastHost = "";
        throw new DataNetException(DataNetException.NO_VALID_PROTOCOL, "No valid protocol can be detected.");
        
    }
    
    /*****************************************************************
     * Returns a valid Net object for the previously detected protocol.
     *
     * @return A valid Net object for the detected protocol. The net
     * object is NOT open - call net.open(this.getLastHost()) to open it.
     *
     * @throws NetException If no valid protocol has been detected.
     ****************************************************************/
    public DataNet getLastProtocol()
    throws DataNetException
    {
        switch (lastProtocol)
        {   
        case PROTOCOL_EARTHWORM:
            return new EWNet();
        case PROTOCOL_SDAS_V0_3:
            return new SdasNetV0_3();
        case PROTOCOL_SDAS_V0_2:
            return new SdasNetV0_2();
                 
        // Add new protocols here
                
        default:
            throw new DataNetException(DataNetException.NO_VALID_PROTOCOL, "No valid protocol can be detected.");
        }
    }

    /*****************************************************************
     * Returns the hostname used with the previously detected protocol.
     *
     * @return A valid hostname string for the detected protocol.
     *
     * @throws NetException If no valid protocol has been detected.
     ****************************************************************/
    public String getLastHost()
    throws DataNetException
    {
        if (lastProtocol == PROTOCOL_NONE)
                throw new DataNetException(DataNetException.NO_VALID_PROTOCOL, "No valid protocol can be detected.");
        return lastHost;
    }
}
