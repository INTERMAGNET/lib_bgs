package bgs.geophys.library.RealTimeData.Sdas;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import bgs.geophys.library.Misc.*;
import bgs.geophys.library.RealTimeData.*;
import bgs.geophys.library.ssh.*;

import com.jcraft.jsch.*;

/***************************************************************
 * SdasNet is a class for connecting to an SDAS data server.
 * Methods are provided to open the connection, send commands
 * to the server and retrieve data from the server. The code is
 * in 3 sections: 1 - public methods that are not part of the
 * DataNet interface; 2 - implementation of the DataNet
 * interface; 3 - private utility methods
 *
 * This code is for SDAS V0.3 onwards . The main changes are:
 *  1.) Using the AINF server command to gather all information
 *      at connection time
 *  2.) Faster startup (due to reduced network traffic)
 *  3.) Requests for MRBT (most recent block time) are
 *      processed more efficiently
 * The changes are all aimed at improving network response.
 * They require new commands on the server (e.g AINF, AMRB).
 *
 * @author  S. Flower
 * @version 0.0
 **************************************************************/
public class SdasNetV0_3 extends Object implements DataNet
{

////////////////////////////////////////////////////////////////////////////////
//////// Section 1 - things that do NOT fulfill the DataNet interface //////////
////////////////////////////////////////////////////////////////////////////////

    /** This class holds details on a single configuration on the
     * remote SDAS server. Configurations include a date/time from
     * which they are valid. To find the correct configuration to use
     * given a date/time, search through the list until the given
     * date/time is after the config date/time */
    private class SdasConfigHistory
    {
        public Date valid_from;          // starting date/time of validity
        public Vector<SdasChannel> channels;          // array of SdasChannel objects
    }
    
    // public instance variables

    /** The default port for an SDAS server */
    public final static int SDAS_DEFAULT_PORT = 6801;

    /** the mode for data transport */
    public final static int DATA_BINARY = 1;
    public final static int DATA_ASCII  = 2;
    public final static int DATA_ZLIB   = 3;

    // fake a version number for zlib which is compatible with data server
    private final static String ZLIB_VERSION = "1.1.4";
    
    // set this to true to copy all IO with the remote SDAS to stderr
    private static boolean debug_io = false;

    // variables that hold the description of the remote SDAS system
    private boolean config_loaded;
    private Vector<SdasConfigHistory> sdas_configs;                // array of SdasConfigHistory objects
    private Vector<SdasChannelGroup> sdas_groups;                 // array of SdasChannelGroup objects
    private Vector<Date> mrb_times;                   // array of Date holding most recent block time for each channel
    
    // private instance variables
    private boolean IsConnected = false;        // TRUE when connection to server is open
    private int transferType = -1;              // mode for data transfer, decided at connection time
    private sshTunnel ssh_tunnel = null;        // the tunnel for SSH connection:
                                                //   if SdasNet is using a direct connection this will be null
                                                //   if SdasNet is using an ssh connection, this holds the ssh tunnel
    private sshStreamForward ssh_stream_fwd;    // the streamForward object for the SSH connection
                                                //   if SdasNet is using a direct connection this will be null
                                                //   if SdasNet is using an ssh connection, this holds the stream forwarding object
    private Socket SdasSocket = null;           // IP Socket to SDAS server:
                                                //   if SdasNet is using a direct connection this holds the socket
                                                //   if SdasNet is using an ssh connection, this will be null
    private int socket_timeout;                  // timeout (in mS) on socket
    private DataInputStream SdasInStream;       // Input stream from SDAS - valid for both direct and ssh connections
    private OutputStream SdasOutStream;         // Output stream to SDAS - valid for both direct and ssh connections
    private SimpleTimeZone gmt;                 // GMT time zone

    /**************************************************************
     * Constructs an empty SdasNet. To open the connection see the
     * Open method.
     *
     * @see #Open
     **************************************************************/
    public SdasNetV0_3()
    {
      sdas_configs = new Vector<SdasConfigHistory> ();
      sdas_groups = new Vector<SdasChannelGroup> ();
      mrb_times = new Vector<Date> ();
      gmt = new SimpleTimeZone (0, "GMT");
      config_loaded = false;
    }

    /**************************************************************
     * Get the date and time from the remote operating system
     *
     * @return          the date and time
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public Date GetOSTime ()
    throws SdasNetException, IOException

    {
        int channelNo;
        String response;
        Date date;

        if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");
        ProcessCommand ("GOST", false);
        response = Read ();
        try
        {
            SimpleDateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd H:m:s");
            date_format.setTimeZone (gmt);
            date = date_format.parse (response);
        }
        catch (ParseException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad date/time: " + response);
        }

        return date;
    }
    
////////////////////////////////////////////////////////////////////////////////
////////// Section 2 - things that DO fulfill the DataNet interface ////////////
////////////////////////////////////////////////////////////////////////////////

    /**************************************************************
     * Set a timeout for socket operations - if the operation
     * (read or write) exceeds the timeout, it will fail
     * (rather than hang)
     * 
     * @param timeout the length of time, in milliseconds
     **************************************************************/
    public void setSocketTimeout (int timeout)
    {
        if (IsConnected)
        {
            try
            {
                if (ssh_tunnel != null)
                    ssh_tunnel.setTimeout(timeout);
                else
                    SdasSocket.setSoTimeout(timeout);
                socket_timeout = timeout;
            }
            catch (SocketException e) { }
            catch (JSchException e) { }
        }
        else
            socket_timeout = timeout;
    }
    
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
    public int getSocketTimeout ()
    {
        return socket_timeout;
    }
    
    /**************************************************************
     * Copy the configuration from one SDASNet object to another.
     *
     * @param xl an SdasNet link to the same server -
     *        the existing_link can be used even after it has been closed
     **************************************************************/
    public void CopyConfig (DataNet xl)
    {
      int cfg_count, ch_count, grp_count;
      SdasConfigHistory config_history, existing_config_history;
      SdasChannel existing_channel_details, channel_details;
      SdasChannelGroup channel_group, existing_channel_group;
      SdasNetV0_3 existing_link;

      existing_link = (SdasNetV0_3) xl;
      if (existing_link.config_loaded)
      {
        for (cfg_count=0; cfg_count<existing_link.sdas_configs.size(); cfg_count++)
        {
          existing_config_history = existing_link.sdas_configs.get (cfg_count);
          config_history = new SdasConfigHistory ();
          config_history.valid_from = new Date (existing_config_history.valid_from.getTime());
          config_history.channels = new Vector<SdasChannel> ();
          for (ch_count=0; ch_count<existing_config_history.channels.size(); ch_count++)
          {
            existing_channel_details = (SdasChannel) existing_config_history.channels.get (ch_count);
            channel_details = new SdasChannel (existing_channel_details.GetChannel(),
                                               existing_channel_details.GetName(),
                                               existing_channel_details.GetType(),
                                               existing_channel_details.GetUnits());
            config_history.channels.add (channel_details);
          }
          sdas_configs.add (config_history);
        }

        for (grp_count=0; grp_count<existing_link.sdas_groups.size(); grp_count++)
        {
          existing_channel_group = existing_link.sdas_groups.get (grp_count);
          channel_group = new SdasChannelGroup (existing_channel_group);
          sdas_groups.add (channel_group);
        }
      
        config_loaded = true;
      }
    }

    /**************************************************************
     * Test whether an SdasNet object is currently connected to an
     * SDAS server.
     *
     * @return true if the object is connected, false otherwise.
     **************************************************************/
    public boolean IsOpen ()
    {
        return IsConnected;
    }

    /**************************************************************
     * Open a connection to an SDAS server. If ssh_string is non-
     * null then an SSH connection will be opened.
     *
     * @param host_string  name or IP address of host to connect to with
     *                     optional colon followed by port number
     * @param ssh_string   the name of an SSH server in the form
     *                     user@ssh_host:ssh_port
     *
     * @throws SdasNetException     If the object is already connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     * @see SdasNetException
     * @see Socket
     **************************************************************/
    public void Open (String host_string, String ssh_string)
    throws SdasNetException, UnknownHostException, IOException
    {
      Open (host_string, ssh_string, true);
    }

    /**************************************************************
     * Open a connection to an SDAS server using the specified data
     * transfer mode. If ssh_string is non-null an SSH tunnel
     * connection will be opened.
     *
     * @param host_string   name or IP address of host to connect to with
     *                      optional colon followed by port number
     * @param ssh_string    the name of an SSH server in the form
     *                      user@ssh_host:ssh_port
     * @param compress_data true if data compression is to be attempted
     *
     * @throws SdasNetException     If the object is already connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     * @see SdasNetException
     * @see Socket
     **************************************************************/
    public void Open (String host_string, String ssh_string, boolean compress_data)
    throws SdasNetException, UnknownHostException, IOException
    {
      int count, count2, ssh_port = -1;
      String ssh_user = null, ssh_host = null;
      sshSwingUserInfo ssh_ui;

      // parse the ssh string
      if (ssh_string != null)
      {
        ssh_user = sshTunnel.findUsernameInHostname(ssh_string);
        ssh_port = sshTunnel.findPortInHostname(ssh_string);
        ssh_host = sshTunnel.findHostInHostname(ssh_string);
        if (ssh_host == null)
          throw new SdasNetException (SdasNetException.BAD_HOST_NAME, "Missing ssh host name");
        if (ssh_host.length () <= 0)
          throw new SdasNetException (SdasNetException.BAD_HOST_NAME, "Missing ssh host name");
      }

      // if SSH connection is required then create tunnel
      if (ssh_host != null)
      {
        // create SSH connection
        if (ssh_user == null)
        {
          ssh_ui = new sshSwingUserInfo ("Connection to ssh server " + ssh_host,
                                         "Username:", "Password:");
          if (ssh_ui.getUsername() == null)
            throw new SdasNetException (SdasNetException.BAD_HOST_NAME, "SSH connection aborted");
        }
        else ssh_ui = new sshSwingUserInfo (ssh_user);
              
        try
        {
          ssh_tunnel = new sshTunnel (ssh_ui, ssh_host, ssh_port, socket_timeout);
        }
        catch (Exception e)
        {
          ssh_tunnel = null;
          throw new SdasNetException (SdasNetException.NOT_OPEN, "Error opening SSH connection");
        }
      }

      // Make the connection
      Open (host_string, compress_data, ssh_tunnel);
    }

    /**************************************************************
     * Open a connection to an SDAS server using the specified
     * data transfer type and the given SSH tunnel. If SSH
     * connection is not required then pass 'null' in place of the
     * tunnel.
     *
     * @param host_string   name or IP address of host to connect to with
     *                      optional colon followed by port number
     * @param compress_data true if data compression is to be attempted
     * @param tunnel        an sshTunnel connection to an SSH host
     *
     * @throws SdasNetException     If the object is already connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     * @see SdasNetException
     * @see Socket
     **************************************************************/
    public void Open (String host_string, boolean compress_data, sshTunnel tunnel)
    throws SdasNetException, UnknownHostException, IOException
    {
      int count, count2, host_port, transfer_mode, ncfg, nch, cfg_count, ch_count;
      int ngrp, grp_count;
      String host_name, port_string, response, name, string;
      SimpleDateFormat date_format;
      SdasConfigHistory config_history;
      SdasChannel channel_details;
      StringTokenizer parser;      
      ByteArrayOutputStream memoryBuffer;

      // parse the host string
      if (host_string.length () <= 0)
        throw (new SdasNetException (SdasNetException.BAD_HOST_NAME, "Missing host name"));
      count = host_string.indexOf (':');
      if (count < 0)
      {
        host_name = host_string;
        host_port = SDAS_DEFAULT_PORT;
      }
      else
      {
        host_name = host_string.substring (0, count);
        // there may be an optional ",sdas" on the end of the string, ignore this
        count2 = host_string.indexOf (',');
        if (count2 > 0)
        {
          if (count2 <= count)
            throw new SdasNetException (SdasNetException.BAD_HOST_NAME, "Error in host string " + host_string);
          else
            port_string = host_string.substring (count +1, count2);
        }
        else
          port_string = host_string.substring (count +1);
        if (port_string.length () <= 0)
          throw (new SdasNetException (SdasNetException.BAD_HOST_NAME, "Missing port number"));
        try { host_port = Integer.parseInt (port_string); }
        catch (Exception e) { throw (new SdasNetException (SdasNetException.BAD_HOST_NAME, "Bad port number")); }
      }

      if (tunnel != null)
      {
        try
        {
          ssh_stream_fwd = tunnel.newStreamForward (host_name, host_port);
        }
        catch (JSchException e)
        {
          throw new SdasNetException (SdasNetException.NOT_OPEN, "Error opening SSH connection");
        }

        // get the input and output streams from the ssh connection
        SdasInStream = new DataInputStream (ssh_stream_fwd.getInputStream ());
        SdasOutStream = ssh_stream_fwd.getOutputStream ();
      }
      else
      {    
        SdasSocket = new Socket (host_name, host_port);
        SdasSocket.setSoTimeout(socket_timeout);
        SdasInStream = new DataInputStream (SdasSocket.getInputStream());
        SdasOutStream = SdasSocket.getOutputStream();
      }

      // check the version of the remote sdas
      ProcessCommand ("VERS", false);
      response = Read ();
      try
      {
          if (Double.parseDouble(response) < 0.3)
              throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Incorrect SDAS server version [" + response + "]");
      }
      catch (NumberFormatException e)
      {
              throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Incorrect SDAS server version [" + response + "]");
      }
      
      // try to set compression methods - if compression is allowed
      // try in order of preference - DATA_ZLIB, DATA_BINARY, DATA_ASCII,
      // otherwise just try DATA_BINARY and DATA_ASCII.
      if (compress_data) transfer_mode = DATA_ZLIB;
      else transfer_mode = DATA_BINARY;
      switch (transfer_mode)
      {
      // there are intentionally no "break" statements in the "catch"
      // blocks so that transfer_mode options will cascade through
      case DATA_ZLIB:
        try
        {
          ProcessCommand ("CMPR ZLIB "+ ZLIB_VERSION, false);
          transferType = DATA_ZLIB;
          break;
        }
        catch (Exception e)
        {
          // unsuccessful, try binary
          transfer_mode = DATA_BINARY;
        }
      case DATA_BINARY:
        try
        {
          ProcessCommand ("CMPR BINARY", false);
          transferType = DATA_BINARY;
          break;
        }
        catch (Exception e)
        {
          // unsuccessful, try ascii
          transfer_mode = DATA_ASCII;
        }
      case DATA_ASCII:
        // if this is unsuccessful then the exception will be passed on
        ProcessCommand ("CMPR ASCII", false);
        transferType = DATA_ASCII;
        break;
      }

      // tell the remote SDAS what type of data to send - the number 0x41424344
      // is sent in binary form to the SDAS server which works out which way
      // round subsequent integer data bytes should be arranged for this client -
      // the number 0x41424344 is chosen because it consists entirely of
      // printable ASCII characters
      // do not swap bytes if ascii data is being transferred
      memoryBuffer = new ByteArrayOutputStream ();
      DataOutputStream convert = new DataOutputStream (memoryBuffer);
      convert.writeInt (0x41424344);
      convert.flush ();
      if (transferType == DATA_ZLIB || transferType == DATA_BINARY)
        ProcessCommand ("BINT " + memoryBuffer.toString (), false);

      // set the valus of the number used to indicate missing data
      ProcessCommand ("MISS " + Integer.toString (DataNet.MISSING_DATA_VALUE), false);

      // get information about the server
      if (! config_loaded)
      {
        date_format = new SimpleDateFormat ("yyyy-MM-dd H:m:s");
        date_format.setTimeZone (gmt);                    
        ProcessCommand ("AINF", false);
        response = Read ();
        try
        {
          // read the configuration history
          ncfg = Integer.parseInt(response);
          for (cfg_count=0; cfg_count<ncfg; cfg_count++)
          {
            config_history = new SdasConfigHistory ();
            config_history.channels = new Vector<SdasChannel> ();
            response = Read ();
            nch = Integer.parseInt(response);
            response = Read ();
            config_history.valid_from = date_format.parse (response);
            for (ch_count=0; ch_count<nch; ch_count++)
            {
              response = Read ();
              parser = new StringTokenizer (response, "'", false);
              channel_details = new SdasChannel (Integer.parseInt (parser.nextToken ()), parser.nextToken (), parser.nextToken (), parser.nextToken ());
              config_history.channels.add (channel_details);
            }
            sdas_configs.add (config_history);
          }
        
          // read the channel group details
          response = Read ();
          ngrp = Integer.parseInt(response);
          for (grp_count=0; grp_count<ngrp; grp_count++)
          {
            parser = new StringTokenizer (response, "'");
            name = parser.nextToken ();
            nch = Integer.parseInt(parser.nextToken ());
            string = parser.nextToken ();
            while (parser.hasMoreTokens()) string += "'" + parser.nextToken ();
            sdas_groups.add (new SdasChannelGroup (grp_count, name, nch, string));
          }
        }
        catch (NumberFormatException e)
        {
          throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad number or channel details: " + response);
        }
        catch (ParseException e)
        {
          throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad date/time: " + response);
        }
        catch (NoSuchElementException e)
        {
          throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad channel or group details: " + response);
        }
      }
      
      // debug dump of configuration
      if (debug_io) displayConfig();
      
      config_loaded = true;
      IsConnected = true;
    }

    /**************************************************************
     * Get the SSH tunnel for this connection.
     *
     * @return the sshTunnel for this connection. This will be
     *         null if SSH is not being used.
     **************************************************************/
    public sshTunnel getTunnel ()
    {
        return ssh_tunnel;
    }

    /**************************************************************
     * Close the connection to SDAS
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public void Close ()
    throws SdasNetException, IOException
    {
      if (IsConnected)
      {
        ProcessCommand ("CLOS", true);
        SdasInStream.close ();
        SdasOutStream.close ();
        if (SdasSocket != null) SdasSocket.close ();
        IsConnected = false;
      }
    }

    /**************************************************************
     * Get the number of data channels on an SDAS system.
     *
     * @return                      the number of channels
     **************************************************************/
    public int GetNChannels () 
    {
        SdasConfigHistory config_history;
        
        if (sdas_configs.size() <= 0) return 0;
        config_history = (SdasConfigHistory) sdas_configs.get (sdas_configs.size () -1);
        return config_history.channels.size();
    }

    /**************************************************************
     * Get the channel number of a channel on an SDAS system from
     * its index.
     *
     * @param chanIndex             the index (0 to NChannels-1)
     * @return                      the channel number
     * @throws SdasNetException     If the index is out of range
     * @see SdasNetException
     **************************************************************/
    public int GetChannel (int chanIndex)
    throws SdasNetException

    {
        SdasConfigHistory config_history;
        SdasChannel channel;
        
        if (sdas_configs.size() <= 0)
            throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        config_history = sdas_configs.get (sdas_configs.size () -1);
        if (chanIndex < 0  || chanIndex >= config_history.channels.size())
            throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        channel = (SdasChannel) config_history.channels.get (chanIndex);
        return channel.GetChannel();
    }

    /**************************************************************
     * Get details on a channel (such as its name and type). The
     * details are only valid for the given date/time.
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the details are required
     * @return          the channel details
     * @throws SdasNetException     If the index or date is out of range
     *
     * @see SdasNetException
     **************************************************************/
    public DataChannel GetChannelDetails (int chanIndex, Date date)
    throws SdasNetException, IOException

    {
        int count;
        SdasConfigHistory config_history;
        SdasChannel channel;
        
        for (count=sdas_configs.size () -1; count>=0; count--)
        {
            config_history = sdas_configs.get (count);
            if (date.getTime() >= config_history.valid_from.getTime ())
            {
                // found the correct configuration - now find the channel
                if (chanIndex < 0  || chanIndex >= config_history.channels.size())
                    throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
                return (DataChannel) config_history.channels.get (chanIndex);
            }
        }

        throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Unable to find channel details [" + Integer.toString (chanIndex) + ", " + date.toString() + "]");
    }

    /**************************************************************
     * Get the number of channel groups on an SDAS system.
     *
     * @return                      the number of groups
     **************************************************************/
    public int GetNGroups ()    { return sdas_groups.size (); }

    /**************************************************************
     * Get channel group details from index of group
     *
     * @param groupIndex            the index (0 to NGroups-1)
     * @return                      SdasChannelDetails object
     * @throws SdasNetException     If the index is out of range
     *
     * @see SdasNetException
     **************************************************************/
    public DataChannelGroup GetGroupDetails (int groupIndex)
    throws SdasNetException, IOException

    {
        if (groupIndex < 0  || groupIndex >= sdas_groups.size ())
            throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (groupIndex) + "]");
        return (DataChannelGroup) sdas_groups.get (groupIndex);
    }

    /**************************************************************
     * Get an SDAS event for the given date
     *
     * @param day, month, year      the event day, month and year as
     *                              integers. If these are all zero, the
     *                              next event for the previous date is
     *                              returned.
     * @return                      the event
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public DataEvent GetNextEvent (int day, int month, int year)
    throws SdasNetException, IOException
    {
        String  dateString, response = "";
        StringTokenizer parser;
        SimpleDateFormat date_format;
        Date evdate;
        int dur, datalen;
        String name, al_data = "";
        SdasEvent eventDetails;

        if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");

        date_format = new SimpleDateFormat ("dd-MM-yyyy H:m:s.S");
        date_format.setTimeZone (new SimpleTimeZone (0, "GMT"));
        
        // call SDAS to get the information
        try
        {
          dateString = day + "-" + month + "-" + year;
          ProcessCommand ("EVNT " + dateString + " 1", false);
          response = Read ();

          // read response and make new SdasEvent object
          parser = new StringTokenizer (response, "'");

          evdate = date_format.parse (parser.nextToken() + " " + parser.nextToken());
          dur = (int)(Float.parseFloat(parser.nextToken ()));
          name = parser.nextToken ();
          datalen = Integer.parseInt(parser.nextToken ());

          response = Read();
          if (datalen > 0)
          {
            // algorithm data is also being sent
            al_data = response;
            eventDetails = new SdasEvent (evdate, dur, name, al_data);
          }
          else
            eventDetails = new SdasEvent (evdate, dur, name);
        }
        catch (ParseException e)
        {
          throw new SdasNetException (SdasNetException.EVENT_DATA_ERROR, "Server sent bad event details: " + response);
        }
        return eventDetails;
    }


    /**************************************************************
     * Get data statistics for the given channel/date
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date / hour at which the details are required
     * @param hourly    true to retrieve hourly stats, false for daily 
     * @return          the statistics
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public DataStats ReadStats (int chanIndex, Date date, boolean hourly)
    throws SdasNetException, IOException

    {
        int channelNo;
        SdasStats stats;
        String response;
        SimpleDateFormat date_format;
        
        if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");
        if (chanIndex < 0  || chanIndex >= GetNChannels()) throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        channelNo = GetChannel (chanIndex);
        if (hourly) date_format = new SimpleDateFormat ("dd-MM-yyyy H");
        else date_format = new SimpleDateFormat ("dd-MM-yyyy -1");
        date_format.setTimeZone (gmt);
        ProcessCommand ("STAT " + Integer.toString (channelNo) + " " + date_format.format (date), false);
        response = Read ();
        try
        {
            StringTokenizer parser = new StringTokenizer (response, "'", false);
            stats = new SdasStats (channelNo, date, hourly, parser.nextToken (), parser.nextToken (), parser.nextToken (), parser.nextToken (), parser.nextToken (), parser.nextToken ());
        }
        catch (NoSuchElementException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad date statistics: " + response);
        }
        catch (NumberFormatException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad data statistics: " + response);
        }

        return stats;
    }

    /**************************************************************
     * Get the sample rate for an SDAS channel. The sample
     * rate is only valid for the given date/time/duration.
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the sample rate is required
     * @param duration  the duration (in mSec) of data to search
     * @return          the sample rate (in samples/hour)
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public double GetSampleRate (int chanIndex, Date date, int duration)
    throws SdasNetException, IOException

    {
        int sampleRate, dataLen, channelNo;
        String response;
        SimpleDateFormat date_format;

        date_format = new SimpleDateFormat ("dd-MM-yyyy H:m:s");
        date_format.setTimeZone (gmt);
        if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");
        if (chanIndex < 0  || chanIndex >= GetNChannels ()) throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        channelNo = GetChannel (chanIndex);
        ProcessCommand ("SRAT " + Integer.toString (channelNo) + " " + date_format.format (date) + " " + Integer.toString (duration), false);
        response = Read ();
        try
        {
            StringTokenizer parser = new StringTokenizer (response, " \t\n\r", false);
            sampleRate = Integer.parseInt (parser.nextToken ());
            dataLen = Integer.parseInt (parser.nextToken ());
        }
        catch (NumberFormatException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad sample rate or data length: " + response);
        }
        catch (NoSuchElementException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad sample rate or data length: " + response);
        }

        return sampleRate;
    }

    /**************************************************************
     * Get data from an SDAS channel. The data for the requested
     * period must share the same sample rate and channel
     * information, or an exception results.
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the sample rate is required
     * @param duration  the duration (in mSec) of data to search
     * @return          a new TimeSeriesData object
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood
     *                              or the sample rate / channel details
     *                              change during the given time window.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     * @see TimeSeriesData
     **************************************************************/
    public TimeSeriesData GetData (int chanIndex, Date date, long duration)
    throws SdasNetException, IOException

    {
        int sampleRate, dataLen, channelNo;
        String response;
        SimpleDateFormat date_format;
        TimeSeriesData tsd;
        SdasChannel ChannelDetails;

        date_format = new SimpleDateFormat ("dd-MM-yyyy H:m:s.S");
        date_format.setTimeZone (gmt);
        if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");
        if (chanIndex < 0  || chanIndex >= GetNChannels ()) throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        channelNo = GetChannel (chanIndex);
        ProcessCommand ("Data " + Integer.toString (channelNo) + " " + date_format.format (date) + " " + Long.toString (duration), false);
        response = Read ();
        try
        {
            StringTokenizer parser = new StringTokenizer (response, "'", false);
            sampleRate = Integer.parseInt (parser.nextToken ());
            dataLen = Integer.parseInt (parser.nextToken ());
            ChannelDetails = new SdasChannel (channelNo, parser.nextToken (), parser.nextToken (), parser.nextToken ());
        }
        catch (NumberFormatException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad sample rate or data length: " + response);
        }
        catch (NoSuchElementException e)
        {
            throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad sample rate or data length: " + response);
        }
        
        // In zlib data transfer, it can be difficult to tell when the end of the
        // data stream has been reached. If zlib data is being transferred,
        // write an additional "MISS" command to the output stream. This command
        // has been chosen because it returns only one response "OKOK". When
        // reading the input stream, this pattern is checked for to confirm that
        // the end of the data has been reached.
        if (transferType == DATA_ZLIB)
          Write ("MISS " + Integer.toString (DataNet.MISSING_DATA_VALUE), true);
        tsd = new SdasTimeSeriesData (date, (int)duration, sampleRate, ChannelDetails);
        ((SdasTimeSeriesData)tsd).FillFromStream (SdasInStream, transferType);

        return tsd;
    }

    /**************************************************************
     * Get the date and time of the most recently received data
     * block for one or all channels on an SDAS system.
     *
     * @param chanIndex set to -1 to get the date and time of the
     *                  most recent block across all channels or
     *                  set to the index of the channel to get data for
     *                  a single channel
     * @param reRead    set to true to re-read the most recent
     *                  data block times, set to false to use a previously
     *                  cached value
     * @return          the date and time
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public Date GetMostRecentBlockTime (int chanIndex, boolean reRead)
    throws SdasNetException, IOException

    {
        int count, nch;
        Date latest_date, date;
        String response;
        
        // force a read if one hasn't been done yet
        if (mrb_times.size() <= 0) reRead = true;
        
        // do we need to fill the cache ??
        if (reRead)
        {
            if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");
            ProcessCommand ("AMRB", false);
            response = Read ();
            try
            {
                nch = Integer.parseInt (response);
                mrb_times = new Vector<Date> ();
                for (count=0; count<nch; count++)
                {
                    response = Read ();
                    if (response.length() <= 0)
                    {
                        // no data for this channel
                        date = new Date (0);
                    }
                    else
                    {
                        SimpleDateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd H:m:s");
                        date_format.setTimeZone (gmt);
                        date = date_format.parse (response);
                    }
                    mrb_times.add (date);
                }
            }
            catch (ParseException e)
            {
                throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad date/time: " + response);
            }
            catch (NumberFormatException e)
            {
                throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad number of channels: " + response);
            }
        }
        
        // return data to the caller
        if (chanIndex < -1  || chanIndex >= mrb_times.size () || mrb_times.size () <= 0)
            throw new SdasNetException (SdasNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        if (chanIndex != -1) return (Date) mrb_times.get (chanIndex);
        latest_date = (Date) mrb_times.get (0);
        for (count=1; count<mrb_times.size(); count++)
        {
            date = (Date) mrb_times.get (count);
            if (date.getTime() > latest_date.getTime ()) latest_date = date;
        }
        return latest_date;
        
    }
    
    /**************************************************************
     * Get a file from the remote host.
     *
     * @param filename a string containing the filename to get
     * @return         a string containing the file contents
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    public String GetFile (String filename)
    throws SdasNetException, IOException
    {
      int length, read_bytes = 0;
      String response = "";
      String file_string = "";

      if (! IsConnected) throw new SdasNetException (SdasNetException.NOT_OPEN, "Attempt to access server before calling Open()");

      try
      {
        ProcessCommand ("FILE " + filename, false);
        response = Read ();
        length = Integer.parseInt (response);
        while (read_bytes < length)
        {
          response = Read ();
          read_bytes += response.length () + 1;
          file_string += response + '\n';
        }
      }
      catch (NumberFormatException e)
      {
        throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad sample rate or data length: " + response);
      }
      return file_string;
    }
    
    /**************************************************************
     * Detects if the connected server is using the classes
     *                  protocol.
     *
     * @return True if the classes protocol is being used, false
     *                  otherwise.
     **************************************************************/
    public boolean isProtocol()
    {
        // Add code here to determine wether true or false
        // At the moment return false
        return true;
    }
    
    /***************************************************************
     * turn IO debugging on / off
     * @param on true for on, false for off
     ***************************************************************/
    public void DebugIO (boolean on) { debug_io = on; }

   
////////////////////////////////////////////////////////////////////////////////
//////////////////// Section 3 - private utility methods ///////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**************************************************************
     * Send a command to the SDAS server and process the
     * returned error code. This method does not process
     * any ancilliary data that the server sends after the
     * error code. If an error code is returned, a request
     * is sent to the server for the corresponding error
     * message and this is included in an exception.
     *
     * @param command        the command to write (without any line termination)
     * @param ExpectClose    if true expect the remote server to signal
     *                       that the connection has been closed
     * @throws SdasNetException     If the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see SdasNetException
     **************************************************************/
    private void ProcessCommand (String command, boolean ExpectClose)
    throws SdasNetException, IOException
    {
        int code;
        String response;


        // send the command and get the response
        Write (command, true);
        response = Read ();

        // analyse the response
        if (response.equals ("OKOK")) return;
        else if (response.equals ("CLOS"))
        {
            if (ExpectClose) return;
            throw new SdasNetException (SdasNetException.UNEXPECTED_CLOSE, "Connection closed unexpectedly by server");
        }
        else if (response.indexOf ("SERR") == 0)
        {
            // extract the error code and send it back to the server to
            // get a descriptive message
            try
            {
                code = Integer.parseInt (response.substring (5));
                Write ("DERR " + Integer.toString (code), true);
                response = Read ();
                if (response.equals ("OKOK"))
                {
                    response = Read ();
                    response = response.substring(0, 1).toUpperCase() + response.substring(1);
                    throw new SdasNetException (code, response);
                }
                else throw new SdasNetException (SdasNetException.CANT_GET_SERVER_ERROR, "Unable to retrieve error message from server for error code: " + Integer.toString (code));
            }
            catch (StringIndexOutOfBoundsException e)
            {
                throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad response code:" + response);
            }
            catch (NumberFormatException e)
            {
                throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent bad response code:" + response);
            }
        }
        else throw new SdasNetException (SdasNetException.CORRUPT_SERVER_DATA, "Server sent unknown response code: " + response);
    }

    /**************************************************************
     * Write a line of data to the SDAS server. IOExceptions are
     * not thrown by this routime.
     *
     * @param data    the data to write (without any line termination)
     * @param AddTerm if true add a line terminator after sending the
     *                data
     *
     * @throws IOException          If there was a system IO error while
     *                              attempting to read from the server.
     *
     * @see SdasNetException
     **************************************************************/
    private void Write (String data, boolean AddTerm)
    throws IOException
    {
      String outputString;
      
      if (debug_io) System.out.println ("Tx: " + data);
      
      if (AddTerm) outputString = new String (data + "\n");
      else outputString = data;
      SdasOutStream.write(outputString.getBytes());
      SdasOutStream.flush ();
    }

    /**************************************************************
     * Read a line of data from the SDAS server.
     *
     * @return the line that was read
     * @throws SdasNetException     If the object is not connected to
     *                              an SDAS server.
     * @throws IOException          If there was a system IO error while
     *                              attempting to read from the server.
     *
     * @see SdasNetException
     **************************************************************/
    private String Read ()
    throws IOException
    {
        int byteCount;
        byte inputByte;
        byte inBuffer [] = new byte [200];
        String string;

        byteCount = 0;
        for (inputByte = SdasInStream.readByte ();
             inputByte != '\n';
             inputByte = SdasInStream.readByte ())
        {
            inBuffer [byteCount] = inputByte;
            byteCount ++;
            if (byteCount >= inBuffer.length) //****//
              break;
        }

        string = new String (inBuffer, 0, byteCount);
        if (debug_io) System.out.println ("Rx: " + string);
        return string;
    }   
    
    /***************************************************************
     * display configuration
     ***************************************************************/
    private void displayConfig ()
    {
        int cfg_count, ch_count, grp_count, channel_array [], count;
        SdasConfigHistory config_history;
        SdasChannel channel_details;
        SdasChannelGroup channel_group;
        
        System.out.println ("Config histories: " + Integer.toString (sdas_configs.size()));
        for (cfg_count=0; cfg_count<sdas_configs.size(); cfg_count++)
        {
          config_history = sdas_configs.get (cfg_count);
          System.out.println (" Number of channels: " + Integer.toString (config_history.channels.size()));
          System.out.println (" Valid from: " + config_history.valid_from.toString () + "(" + Long.toString (config_history.valid_from.getTime()) + ")");
          for (ch_count=0; ch_count<config_history.channels.size(); ch_count ++)
          {
            channel_details = (SdasChannel) config_history.channels.get (ch_count);
            System.out.println ("  " + Integer.toString (ch_count) + ") " + 
                                       Integer.toString (channel_details.GetChannel()) + " " +
                                       channel_details.GetName() + " " +
                                       channel_details.GetType() + " " +
                                       channel_details.GetUnits());
          }
        }
        System.out.println ("Channel groups: " + Integer.toString (sdas_groups.size()));
        for (grp_count=0; grp_count<sdas_groups.size(); grp_count++)
        {
          channel_group = sdas_groups.get (grp_count);
          System.out.print (" " + channel_group.GetName());
          channel_array = channel_group.GetChannels();
          for (count=0; count<channel_array.length; count ++)
            System.out.print (" " + Integer.toString (channel_array[count]));
          System.out.println ("");
        }
    }
}
