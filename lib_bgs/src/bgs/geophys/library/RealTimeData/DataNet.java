/*
 * Net.java
 *
 * Created on 17 August 2003, 16:38
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;
import java.io.*;

import bgs.geophys.library.ssh.*;

/***************************************************************
 * Net is an interface for classes connecting to various data
 * servers. Methods are provided to open the connection, send
 * commands to the server and retrieve data from the server.
 *
 * @author  Iestyn Evans
 **************************************************************/
public interface DataNet {
    
    /** The value used by the class to indicate that a data sample is missing */
    public final static int MISSING_DATA_VALUE = 0x79797979;

    /**************************************************************
     * Set a timeout for socket operations - if the operation
     * (read or write) exceeds the timeout, it will fail
     * (rather than hang)
     * 
     * @param timeout the length of time, in milliseconds
     **************************************************************/
    public void setSocketTimeout (int timeout);
    
    /**************************************************************
     * Get the timeout for socket operations. The timeout may not
     * actually get applied (if there is an error attempting to set
     * it on the socket) - this routine allows you to check whether
     * it has been applied correctly (but only after the socket
     * has been opened - before that, the value stored by setSocketTimeout
     * will be returned)
     * 
     * @return the timeout, in milliseconds
     **************************************************************/
    public int getSocketTimeout ();
    
    /**************************************************************
     * Copy the configuration from one DataNet object to another -
     * this can save network time if you already have the configuration.
     *
     * @param existing_link the object to copy from
     *
     * @return true if the object is connected, false otherwise.
     **************************************************************/
    public void CopyConfig (DataNet existing_link);

    /**************************************************************
     * Test whether the Net object is currently connected to a
     * server.
     *
     * @return true if the object is connected, false otherwise.
     **************************************************************/
    public boolean IsOpen ();

    /**************************************************************
     * Open a connection to a server. If ssh_string is non-null
     * an SSH tunnel connection will be opened.
     *
     * @param host_string  name or IP address of host to connect to with
     *                     optional colon followed by port number
     * @param ssh_string   the name of an SSH server in the form
     *                     user@ssh_host:ssh_port
     *
     **************************************************************/
    public void Open (String host_string, String ssh_string)
    throws DataNetException, IOException;

    /**************************************************************
     * Open a connection to a server using the specified data
     * transfer mode. If ssh_string is non-null an SSH tunnel
     * connection will be opened.
     *
     * @param host_string   name or IP address of host to connect to with
     *                      optional colon followed by port number
     * @param ssh_string    the name of an SSH server in the form
     *                       user@ssh_host:ssh_port
     * @param compress_data true if data compression is to be attempted
     **************************************************************/
    public void Open (String host_string, String ssh_string, boolean compress_data)
    throws DataNetException, IOException;

    /**************************************************************
     * Open a connection to a server using the specified
     * data transfer type and the given SSH tunnel. If SSH
     * connection is not required then pass 'null' in place of the
     * tunnel.
     *
     * @param host_string   name or IP address of host to connect to with
     *                      optional colon followed by port number
     * @param compress_data true if data compression is to be attempted
     * @param tunnel        an sshTunnel connection to an SSH host
     **************************************************************/
    public void Open (String host_string, boolean compress_data, sshTunnel tunnel)
    throws DataNetException, IOException;

    /**************************************************************
     * Get the SSH tunnel for this connection.
     *
     * @return the sshTunnel for this connection. This will be
     *         null if SSH is not being used.
     **************************************************************/
    public sshTunnel getTunnel ();

    /**************************************************************
     *
     * Close the connection to the server.
     *
     **************************************************************/
    public void Close ()
    throws IOException, DataNetException;
    
    /**************************************************************
     *
     * Get the number of data channels on the system.
     *
     **************************************************************/
    public int GetNChannels ()
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get the channel number of a channel on the system from
     * its index.
     *
     * @param chanIndex             the index (0 to NChannels-1)
     * @return                      the channel number
     **************************************************************/
    public int GetChannel (int chanIndex)
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get details on a channel (such as its name and type).
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the details are required
     * @return          the channel details
     **************************************************************/
    public DataChannel GetChannelDetails (int chanIndex, Date date)
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get the number of channel groups on a system.
     *
     * @return                      the number of groups
     **************************************************************/
    public int GetNGroups ()
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get channel group details from index of group
     *
     * @param groupIndex            the index (0 to NGroups-1)
     * @return                      DataChannelGroup object
     **************************************************************/
    public DataChannelGroup GetGroupDetails (int groupIndex)
    throws IOException, DataNetException;

    
    /**************************************************************
     * Get an event for the given date
     *
     * @param day, month, year      the event day, month and year as
     *                              integers. If these are all zero, the
     *                              next event for the previous date is
     *                              returned.
     * @return                      the event
     **************************************************************/
    public DataEvent GetNextEvent (int day, int month, int year)
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get data statistics for the given channel/date
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date / hour at which the details are required
     * @param hourly    true to retrieve hourly stats, false for daily 
     * @return          the statistics
     **************************************************************/
    public DataStats ReadStats (int chanIndex, Date date, boolean hourly)
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get the sample rate for a channel.
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the sample rate is required
     * @param duration  the duration (in mSec) of data to search
     * @return          the sample rate (in samples/hour)
     **************************************************************/
    public double GetSampleRate (int chanIndex, Date date, int duration)
    throws IOException, DataNetException;
    
    
    /**************************************************************
     * Get data from a channel. The data for the requested
     * period must share the same sample rate and channel
     * information, or an exception results.
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the sample rate is required
     * @param duration  the duration (in mSec) of data to search
     * @return          a new TimeSeriesData object
     **************************************************************/
    public TimeSeriesData GetData (int chanIndex, Date date, long duration)
    throws IOException, DataNetException;
    
    /**************************************************************
     * Get the date and time of the most recently received data
     * block for one or all channels on the system.
     *
     * @param chanIndex set to -1 to get the date and time of the
     *                  most recent block across all channels or
     *                  set to the index of the channel to get data for
     *                  a single channel
     * @param reRead    set to true to re-read the most recent
     *                  data block from disk into the servers memory
     *                  cache, or set to false to use a previously
     *                  cached value
     * @return          the date and time
     **************************************************************/
    public Date GetMostRecentBlockTime (int chanIndex, boolean reRead)
    throws DataNetException, IOException;

    /**************************************************************
     * Get a file from the remote host.
     *
     * @param filename a string containing the filename to get
     * @return         a string containing the file contents
     * @throws DataNetException     If the object is not connected to
     *                              a server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see DataNetException
     **************************************************************/
    public String GetFile (String filename)
    throws DataNetException, IOException;
    
    /**************************************************************
     * Detects if the connected server is using the classes
     *                  protocol.
     *
     * @return True if the classes protocol is being used, false
     *                  otherwise.
     **************************************************************/
    public boolean isProtocol ();
    
    /**************************************************************
     * turn IO debugging on / off
     * @param on true for on, false for off
     ***************************************************************/
    public void DebugIO (boolean on);
}
