/*
 * EWNet.java
 *
 * Created on 17 July 2003, 10:36
 */

package bgs.geophys.library.RealTimeData.Earthworm;

// Imported Packages

import com.jcraft.jsch.*;

import java.io.*;
import java.net.*;
import java.util.*;

import bgs.geophys.library.RealTimeData.*;
import bgs.geophys.library.ssh.*;

/**************************************************************************
 * Class to handle the communication protocol with a remote WaveServer.
 *
 * @author  Iestyn Evans
 *************************************************************************/
public class EWNet extends Object implements DataNet
{
    // Constants
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String REQUEST_ID = "ewnet";
    
    /** The value used by EWNet to indicate that a data sample is missing */
    public final static int MISSING_DATA_VALUE = 0x79797979;    

    /** The default port for an EW server */
    private final static int DEFAULT_PORT = 16022;
    
// Class Level Variables
    private boolean isReset;                // Connection reset variable.
    private EWChannels cachedChannels;      // Cache of available channel info.
    private int requestNum;                 // How many requests have been sent.
    private String host_string;
    private String ssh_string;
    private sshTunnel ssh_tunnel;
    private int socket_timeout;
    private sshStreamForward stream_forward;
    private Socket server;
    private InputStream input_stream;
    private OutputStream output_stream;
    private boolean isConnected;
    private long lastAccess;                // Used to check for socket timeouts
    
    // Group related variables
    private String[] groupDetails;
    private int[] groupNumChannels;
    private String[] groupName;
    
    /**************************************************************************
     *
     * Create a new EWNet object.
     *
     *************************************************************************/
    public EWNet() 
    {   
        // Initialises number of requests sent
        requestNum = 0;
        
        // Create an empty EWChannels object
        cachedChannels = new EWChannels();
        
        // initialise flag variables
        socket_timeout = CONNECTION_TIMEOUT;
        isConnected = false;
        isReset = false;
    }

    /**************************************************************
     * Set a timeout for socket operations - if the operation
     * (read or write) exceeds the timeout, it will fail
     * (rather than hang)
     * 
     * @param timeout the length of time, in milliseconds
     **************************************************************/
    public void setSocketTimeout (int timeout)
    {
        if (isConnected)
        {
            try
            {
                if (ssh_tunnel != null)
                    ssh_tunnel.setTimeout(timeout);
                else
                    server.setSoTimeout(timeout);
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
     * Copy the configuration from one EWNet object to another.
     *
     * @param xl an EWNet link to the same server -
     *        the existing_link can be used even after it has been closed
     **************************************************************/
    public void CopyConfig (DataNet xl)
    {
        // do nothing - WORK TO DO need to work out how to copy the configuration
    }
    
    /**************************************************************
     * Open a connection to an EW server.
     *
     * @param hostname  name or IP address of host to connect to with
     *                  optional colon followed by port number
     * @throws EWNetException     If the object is already connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     **************************************************************/
    public void Open (String host_string, String ssh_string)
    throws EWNetException, UnknownHostException, IOException
    {
      int count, count2, ssh_port = -1;
      String hostname, port_string, ssh_user = null, ssh_host = null;
      sshSwingUserInfo ssh_ui;
      
      // save the strings which are passed in for use if a re-connection
      // is needed
      this.host_string = host_string;
      this.ssh_string = ssh_string;

      // make sure that a connection isn't already open but reset
      if (isReset)
      {
        Close ();
        isReset = false;
      }

      // parse the ssh string
      if (ssh_string != null)
      {
        ssh_user = sshTunnel.findUsernameInHostname(ssh_string);
        ssh_port = sshTunnel.findPortInHostname(ssh_string);
        ssh_host = sshTunnel.findHostInHostname(ssh_string);
        if (ssh_host == null)
          throw new EWNetException (EWNetException.BAD_HOST_NAME, "Missing ssh host name");
        if (ssh_host.length () <= 0)
          throw new EWNetException (EWNetException.BAD_HOST_NAME, "Missing ssh host name");
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
            throw new EWNetException (EWNetException.BAD_HOST_NAME, "SSH connection aborted");
        }
        else ssh_ui = new sshSwingUserInfo (ssh_user);
        
        try
        {
          // create SSH connection
          ssh_tunnel = new sshTunnel (ssh_ui, ssh_host, ssh_port, socket_timeout);
        }
        catch (Exception e)
        {
          ssh_tunnel = null;
          throw new EWNetException (EWNetException.NOT_OPEN, "Error opening SSH connection");
        }
      }

      // Make the connection
      Open (host_string, true, ssh_tunnel);
    }

    /**************************************************************
     * Open a connection to an EW server - this method included
     * for compatibility with SDAS.
     *
     * @param hostname  name or IP address of host to connect to with
     *                  optional colon followed by port number
     * @param compress_data true if data compression is to be attempted
     * @throws EWNetException     If the object is already connected to
     *                              an EW server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     **************************************************************/
    public void Open (String host_string, String ssh_string, boolean compress_data)
    throws IOException, EWNetException
    {
      // Earthworm does not have a transfer_mode string so connect using default
      Open (host_string, ssh_string);
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
     * @throws EWNetException     If the object is already connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws UnknownHostException If the specified host cannot be
     *                              found
     * @throws IOException          If there was a system IO error while
     *                              attempting to create the connection
     *
     * @see EWNetException
     * @see Socket
     **************************************************************/
    public void Open (String host_string, boolean compress_data, sshTunnel tunnel)
    throws IOException, EWNetException
    {
      String hostname, port_string;
      int count, count2, port;

      // save the strings which are passed in for use if a re-connection
      // is needed
      this.host_string = host_string;

      // make sure that a connection isn't already open but reset
      if (isReset)
      {
        Close ();
        isReset = false;
      }

      // parse the host string
      if (host_string.length () <= 0)
        throw (new EWNetException (EWNetException.BAD_HOST_NAME, "Missing host name"));
      count = host_string.indexOf (':');
      if (count < 0)
      {
        hostname = host_string;
        port = DEFAULT_PORT;
      }
      else
      {
        hostname = host_string.substring (0, count);
        port_string = host_string.substring (count +1);
        if (port_string.length () <= 0)
          throw (new EWNetException (EWNetException.BAD_HOST_NAME, "Missing port number"));
        try { port = Integer.parseInt (port_string); }
        catch (Exception e) { throw (new EWNetException (EWNetException.BAD_HOST_NAME, "Bad port number")); }
      }

      if (tunnel != null)
      {
        try
        {
//          pf = tunnel.newPortForward (hostname, port);
          stream_forward = tunnel.newStreamForward (hostname, port);
          input_stream = stream_forward.getInputStream ();
          output_stream = stream_forward.getOutputStream ();
          isConnected = true;
        }
        catch (Exception e)
        {
          throw new EWNetException (EWNetException.NOT_OPEN, "Error opening SSH connection");
        }
      }
      else
      {
        // attempt to connect to waveserver
        if (server != null)
          if (isConnected) throw new EWNetException (EWNetException.NOT_OPEN, "Attempt to open when already connected");
        server = new Socket (hostname, port);
        server.setSoTimeout(socket_timeout); // Timeout in ms
        input_stream = server.getInputStream ();
        output_stream = server.getOutputStream ();
        isReset = false;
        isConnected = true;
      }
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

    /************************************************************************** 
     * Tests wether there is a connection.
     *
     * @return Wether the connection closed.
     *************************************************************************/
    public boolean isClosed() 
    {
        // Simply calls the sockets isClosed() method
        // Maybe change??
        // change this to status of whether socket should be open or closed
        if (isReset) return true;
        return !isConnected;
    }

    /************************************************************************** 
     * Tests wether there is a connection.
     *
     * @return Wether the connection is open.
     *************************************************************************/
    public boolean IsOpen() 
    {
        return isConnected;
    }
        
    
    /************************************************************************** 
     * Closes the connection on the socket 
     *
     * @throws IOException if the connection cannot closed
     *************************************************************************/
    public void Close()
    throws IOException
    {
        // Test that connection is connected then close
        if (isConnected)
        {
          input_stream.close ();
          output_stream.close ();
          if (ssh_tunnel != null)
            stream_forward.close ();
          else
            server.close();
          isConnected = false;
        }
    }
    
    /************************************************************************** 
     * Sends a request to the server.
     *
     * @param request Data to be sent to the server.
     *
     * @return wether the data was sent.
     *
     * @throws IOException problems with connection (e.g. connection reset).
     * @throws EWNetException A connection cannot be opened.
     **************************************************************************/
    private boolean send( String request )
    throws IOException, EWNetException
    {
        // Debug stuff  
        /*
       if (lastAccess == 0)
       {
           lastAccess = System.currentTimeMillis();
       }
       else
       {
           long timeNow = System.currentTimeMillis();
           if (timeNow > lastAccess + CONNECTION_TIMEOUT)
           {
                System.err.println("Connection timeout on socket may have taken place. Time difference: " + (timeNow - lastAccess));
           }
           else
           {
                System.err.println("Time difference: " + (timeNow - lastAccess));
           }
           lastAccess = timeNow;
       }
        */
        // End debug stuff
        
        
        // Local variables
//        OutputStream out;
//        InputStream in;
        // Check for connection and open one if it does not exist
        if (!isConnected || isReset)
        {
            // Close existing connection - even if it is already closed this
            // will not hurt!
            Close();
            try
            {
              if (ssh_tunnel != null)
                Open (host_string, true, ssh_tunnel);
              else
                Open (host_string, ssh_string);
            }
            catch (Exception e)
            {
              // Unable to open connection: throw exception
              throw new EWNetException(EWNetException.CANT_OPEN_CONNECTION, "A connection to the server cannot be opened");
            }
        }
        
        // Main body of the function
        // Get IO streams from socket
//        out = server.getOutputStream();
//        in = server.getInputStream();
        
        // Use wrappers for easier access to streams
        PrintWriter socketOut = new PrintWriter(output_stream, true);
        
        // Send the request to the server
        socketOut.println(request);
        socketOut.flush();
        
        
        // Satisfactory result: Exit function
        return true;         
    }
    
    /**************************************************************************
     * Performs a MENU: request to the server.
     *
     * @param requestID ID generated by client to allow tracking of requests.
     * @param channels Object used to store results in.
     *
     * @return wether a valid reply was recieved.
     *
     * @throws IOException problems with connection (e.g. connection reset).
     * @throws EWNetException A bad response was recieved.
     **************************************************************************/
    public boolean menuOld( String requestID, EWChannels channels )
    throws IOException, EWNetException
    {        
        BufferedReader bufReader;
        String response1;
        String response2;
        
        // Check in case of requestNum overflow...
           if (requestNum > 1000000) requestNum = 0;
        
        // Send a MENU: request to the server
        if (send("MENU: " + requestID)) {
            try {
                // Read response into memory
//                bufReader = getBufferedReader();
                bufReader = new BufferedReader (new InputStreamReader (input_stream));
                response1 = bufReader.readLine().trim();            
            }
/*            catch (EWNetException ewx)
            {
                if (ewx.GetErrorCode() == EWNetException.CONNECTION_RESET)
                {
                    isReset = true;
                    if (ssh_tunnel != null)
                        Open (host_string, true, ssh_tunnel);
                    else
                        Open (host_string, ssh_string);
                    send("MENU: " + requestID);
//                    bufReader = getBufferedReader();
                    bufReader = new BufferedReader (new InputStreamReader (input_stream));
                    response1 = bufReader.readLine().trim();  
                }
                else
                {
                    throw ewx;
                }
            }*/
            catch (IOException iox)
            {
                if (iox.getMessage() == "Connection reset")
                {
                    isReset = true;
                    if (ssh_tunnel != null)
                        Open (host_string, true, ssh_tunnel);
                    else
                        Open (host_string, ssh_string);
                    send("MENU: " + requestID);
//                    bufReader = getBufferedReader();
                    bufReader = new BufferedReader (new InputStreamReader (input_stream));
                    response1 = bufReader.readLine().trim();  
                }
                else
                {
                    throw iox;
                }    
            }    
            // Debug Stuff
            System.err.println(response1);
            // End Debug stuff
            response2 = bufReader.readLine().trim();
            System.err.println("OK");

            // Check that the correct requestID has been returned
            // If it hasn't an error of some sort has taken place
            if (response1.compareTo(requestID) != 0) {
                // It seems to be possible that some systems return the requestID
                // in the second line of the response - these systems are dealt
                // with here.
                if (response2.compareTo(requestID) !=0) {
                    // A bad response has been sent - throw an exception
                    throw new EWNetException(EWNetException.BAD_REQUEST_ID ,"The returned Request ID does not match the ID sent");
                }
                
                String temp = response1;               
                response1 = response2;                
                response2 = temp;                
            }
            
            // Set the collections requestID
            channels.requestID = response1;
            
            // In the response each trace's details are seperated by two
            // spaces. The following line splits the response using two
            // whitespace characters - the result should be each individual
            // trace in its own string.
            String[] traces = response2.split("\\s\\s");
            
            // Loop through each trace and parse the information
            for (int i = 0; i < traces.length; i++)            
            {
                String[] trace = traces[i].split("\\s");                
                if ( trace.length != channels.getSizeOfResponse() ) {
                    System.err.println(trace);
                    // An incorrect number of fields have been returned
                    throw (new EWNetException(EWNetException.WRONG_FIELDS_RECIEVED, "The server responded with the wrong number of fields: " + trace.length));
                }
                // Add the trace to the collection
                channels.add(trace);                
            }
            // Valid response recieved and parsed
            return true;
    }        
        // The request could not be sent to the server for some reason
        // Possible reasons: network timeout, connection reset, etc...
        return false;
        
    }

    /**************************************************************************
     * Returns the open sockets InputStream as a Buffered Reader.
     *
     * @return the BufferedReader.
     *
     * @throws IOException Most likely there is no open connection
     *                      or the InputStream is closed.
     *************************************************************************/
/*    private BufferedReader getBufferedReader() 
    throws IOException, EWNetException 
    {
        BufferedReader bufReader;
        
        try {
//            bufReader = new BufferedReader(new InputStreamReader(server.getInputStream()));
              bufReader = new BufferedReader(new InputStreamReader(input_stream));
        }
        catch (IOException iox)
        {
            if (iox.getMessage() == "Connection reset")
                throw new EWNetException(EWNetException.CONNECTION_RESET, "The connection has been reset.");
            else
                throw iox;
        }
        return bufReader;
    }*/
    
    /**************************************************************************
     * Returns the open sockets InputStream as a Buffered Reader.
     *
     * @return the BufferedReader.
     *
     * @throws IOException Most likely there is no open connection
     *                      or the InputStream is closed.
     *************************************************************************/
/*    private BufferedInputStream getBufferedInputStream() 
    throws IOException, EWNetException 
    {
        BufferedInputStream bufInputStream;
        
        try {
//            bufInputStream = new BufferedInputStream(server.getInputStream());
            bufInputStream = new BufferedInputStream(input_stream);
        }
        catch (IOException iox)
        {
            if (iox.getMessage() == "Connection reset")
                throw new EWNetException(EWNetException.CONNECTION_RESET, "The connection has been reset.");
            else
                throw iox;
        }
        return bufInputStream;
    }
*/
    /**************************************************************************
     * Translates the binary data received from a GETSCNRAW request.
     *
     * @channel The object containing the channel details for this request.
     * @duration The expected duration in ms of the response data.
     *
     * @throws FileNotFoundException.
     * @throws IOException If there is a problem with the IO stream.
     * @throws NumberFormatException.
     * @throws EWNetException If an unsupported datatype is sent by the server.
     *
     * @return An EWTimeSeriesData containing the data from a GETSCNRAW request.
     *************************************************************************/
    private EWTimeSeriesData binaryRead(EWChannel channel, long duration)
    throws FileNotFoundException, IOException, NumberFormatException,
                EWNetException
    {
        BufferedInputStream in;
        int i;
        String header = "";

        try {
//            in = new BufferedInputStream(server.getInputStream());             
            in = new BufferedInputStream(input_stream);
            // Read in one ASCII line (the ASCII header)
            i = in.read();
        }
        catch (IOException iox)
        {
            if (iox.getMessage() == "Connection reset")
                throw new EWNetException(EWNetException.CONNECTION_RESET, "The connection has been reset");
            else
                throw iox;
        }

        while (i != '\n')
        {
            char ch = (char)i;
            header = header + ch;
            i = in.read();
        }
        
        String headerSplit[] = header.split("\\s");
        
        // Parse ASCII header to check that a valid request has been made
        validateGetScnRawHeader(headerSplit); // An exception will be thrown for a bad response
        
        // Gather and parse the following data packets
        int noOfBytes = Integer.parseInt((headerSplit[headerSplit.length - 1].trim()));
        byte[] fullData = new byte[noOfBytes];
        
        // Gather the data off the network and into memory
        for (int j = 0; j < noOfBytes; j++)
        {
            fullData[j] =  (byte)in.read();            
        }
        
        String headerDataType = headerSplit[6].trim();
        
        DataInputStream dInStream = new DataInputStream(
                new ByteArrayInputStream(fullData));
        
        // The code that parses and collates the data
        EWTimeSeriesData seriesData = new EWTimeSeriesData(channel);
        seriesData.FillFromStream(dInStream, headerDataType, noOfBytes, duration);
        
        // Clear the binary cache
        fullData = null;
        
        return seriesData;
    }

    /**************************************************************************
     * Validates the ascii header from a GetScnRaw request.
     *                      At the moment only a boolean is returned more 
     *                      data can be made available if required. 
     *
     * @param header The String array containing the split ascii header.
     *
     * @return Wether binary data follows the header.
     *
     * @throws EWNetException Server flags errors.
     *************************************************************************/
    private boolean validateGetScnRawHeader(String[] header)
    throws EWNetException
    {
        String flags = header[5].trim();
        parseFlag(flags);
        return true;
    }
    
    /**************************************************************************
     * Parses a Server flag string.
     *
     * @param flag The String containing the flag.
     *
     * @throws EWNetException Server flags errors.
     *************************************************************************/
    private void parseFlag(String flag)
    throws EWNetException
    {
        if (flag.compareTo("F") == 0)
        {
            // Request was valid
            return;
        }
        else if (flag.compareTo("FL") == 0)
        {
            // Request was before available time in the tank
            throw new EWNetException(EWNetException.DATA_IS_LEFT_OF_TANK, "Data requested comes before available data.");
        }
        else if (flag.compareTo("FR") == 0)
        {
            // Request was after available time in the tank
            throw new EWNetException(EWNetException.DATA_IS_RIGHT_OF_TANK, "Data requested comes after available data.");            
        }
        else if (flag.compareTo("FG") == 0)
        {
            // Request fell within a gap in the tank
            throw new EWNetException(EWNetException.DATA_IS_IN_GAP, "Data requested falls in a gap in the available data.");
        }
        else if (flag.compareTo("FB") == 0)
        {
            // A bad request was made
            throw new EWNetException(EWNetException.BAD_REQUEST, "The request sent to the server was not understood.");
        }
        else if (flag.compareTo("FC") == 0)
        {
            // Tank is corrupt
            throw new EWNetException(EWNetException.TANK_IS_CORRUPT, "The trace requested is contains coruppt data.");
        }
        else if (flag.compareTo("FN") == 0)
        {
            // Requested channel was not found on the server
            throw new EWNetException(EWNetException.CHANNEL_NOT_FOUND, "The requested channel was not found on the server.");
        }
        else if (flag.compareTo("FU") == 0)
        {
            // The server responded that an unknown error occured
            throw new EWNetException(EWNetException.UNKNOWN_ERROR, "The server responded with an unknown error.");
        }
        else
        {
            // An unknown flag was returned - no data follows
            throw new EWNetException(EWNetException.UNKNOWN_FLAG, "Server responded with an unknown flag.");
        }
    }

    /**************************************************************************
     * Performs a raw data request and returns the results in a list of
     *                      EWTracePackets.
     *
     * @param requestID String used to keep track of requests.
     * @param channel An EWChannel object for the channel data is required from.
     * @param startTime The requested start time for data.
     * @param endTime The requested end time for data.
     * 
     * @throws IOException If there is an error with the io stream.
     * @throws FileNotFoundException
     * @throws EWNetException If an unsuported datatype is sent by the server.
     *
     * @return The results as a new EWTimeSeriesData object with filled in 
     *                  gap data.
     *************************************************************************/
    public EWTimeSeriesData getScnRaw(String requestID, EWChannel channel,
                                Date startTime, Date endTime)
    throws FileNotFoundException, IOException, EWNetException    
    {
        // Format request
        String request = "GETSCNRAW: " + requestID + " " + channel.siteCode
                        + " " + channel.channelCode + " " + channel.networkID
                        + " " + doubleToEWString((double)(startTime.getTime()/1000))
                        + " " + doubleToEWString((double)(endTime.getTime()/1000));
        
       // Sends the request
       send(request);
        
        // Process and return the reponse
        try {
            return binaryRead(channel, endTime.getTime() - startTime.getTime());
        }
        catch (EWNetException ewx)
        {
            if (ewx.GetErrorCode() == EWNetException.CONNECTION_RESET)
            {
                isReset = true;
                if (ssh_tunnel != null)
                    Open (host_string, true, ssh_tunnel);
                else
                    Open (host_string, ssh_string);
                send(request);
                return binaryRead(channel, endTime.getTime() - startTime.getTime());
            }
            else
                throw ewx;
        }
    }

    /**************************************************************************
     * Performs an ascii data request and prints results to screen. getSCNRaw()
     *                      is the suggested method. Gaps are represented by G.
     *
     * @param requestID String used to keep track of requests.
     * @param channel An EWChannel object for the channel to request data from.
     * @param startTime The requested start time for data.
     * @param endTime The requested end time for data.
     * 
     * @throws IOException If there is an error with the io stream
     * @throws EWNetException
     *************************************************************************/
    public void getScn(String requestID, EWChannel channel,
                        Date startTime, Date endTime)
    throws IOException, EWNetException
    {
        // Format request
        String request = "GETSCN: " + requestID + " " + channel.siteCode
                        + " " + channel.channelCode + " " + channel.networkID
                        + " " + doubleToEWString((double)(startTime.getTime()/1000))
                        + " " + doubleToEWString((double)(endTime.getTime()/1000))
                        + "G";
        
        // Send request
        send(request);
        
        // Process and print response
//        BufferedReader reader = getBufferedReader();
        BufferedReader reader = new BufferedReader (new InputStreamReader (input_stream));
        System.out.println(reader.readLine());     
    }

    /**************************************************************************
     * Converts a double into an ASCII representation readable by WaveServer.
     *
     * @param d The double to convert.
     *
     * @return The converted String.
     *************************************************************************/
    private String doubleToEWString(double d)
        {
            // Make a string with 16 digits and a decimal point
            String s = Double.toString(d);
            String rawDigits = "";
            int power;
            int point = 0;
            int i;

            
            // Create a string containing just the digits
            for (i = 0; i < s.length() - 2; i++)
            {
                String digit = String.valueOf(s.charAt(i));
                // Check for location of decimal point in original string
                if (digit.compareTo(".") == 0) point = i;
                // Add digit onto the end of the string
                else rawDigits = rawDigits + digit;
            }
            
            
            
            // Work out powers of ten
            if (s.substring(i, i + 1).compareTo("E") == 0)
                power = Integer.parseInt(String.valueOf(s.charAt(i + 1)));
            else return "";
            
            
            
            // Create string to hold formated result
            String answer = "";
            
            // Create 'no power of ten' string with decimal point
            for (i = 0; i < point + power; i++)
            {
                if (i < rawDigits.length())
                    answer = answer + rawDigits.charAt(i);
                else
                    answer = answer + "0";
            }
            answer = answer + ".";
            
            // Append zeros to pad length of string as required
            while (answer.length() < 17)
            {
                if (i < rawDigits.length())
                    if (rawDigits.length() <= i)
                    answer = answer + "0";
                    else
                        answer = answer + rawDigits.charAt(i);
                else answer = answer + "0";
                i++;
            }
            
            
            // Return formated string
            return answer;
            
        }
    
    /**************************************************************************
     * Get the sample rate for an Earthworm trace. The sample rate is only
     *                      valid for the given date/time/duration.
     *
     * @param chanIndex The index for the channel.
     * @param date The date at which the sample rate is required.
     * @param duration. The duration (in mSec) of data to search.
     *
     * @return The sample rate (in samples/sec).
     *
     * @throws IOException If there was a system IO error while attempting
     *                      to access the server.
     * @throws EWNetException If a connection to the server has not been
     *                      opened.
     *************************************************************************/
    public double GetSampleRate (int chanIndex, Date date, int duration)
    throws IOException, EWNetException
    {  
        // Note: At the moment duration is not implemented.
        
        if (isClosed()) {
            throw new EWNetException(EWNetException.NOT_OPEN, "Attempt to access server before calling Open()");
        }
        if (cachedChannels.size() == 0) {
            // Attempt to get trace details
            menu(REQUEST_ID + requestNum++,cachedChannels);
        }
        if (chanIndex < 0  || chanIndex >= cachedChannels.size()) {
            // Bad index number
            throw new EWNetException (EWNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (chanIndex) + "]");
        }

        // Get some data for the time requested
        cachedChannels.get(chanIndex);
        EWTimeSeriesData seriesData = 
                getScnRaw(REQUEST_ID + requestNum++, cachedChannels.get(chanIndex),
                date, new Date(date.getTime() + 1000));

        return seriesData.GetSampleRateHz();
    }    
    
    
    /**************************************************************************
     * Get data from an Earthworm trace. The data for the requested period
     *                  must share the same sample rate and channel
     *                  information, or an exception results.
     *
     * @param chanIndex The index for the channel.
     * @param date The date at which the sample rate is required.
     * @param duration The duration (in mSec) of data to search.
     *
     * @return A new EWTimeSeriesData object.
     *
     * @throws IOException If there was a system IO error while attempting to
     *                      access the server.
     * @throws EWNetException.
     *************************************************************************/
    public TimeSeriesData GetData (int chanIndex, Date date, long duration)
    throws IOException, EWNetException
    {
        try
        {
            // Get channel details for specific index
            // Perhaps check for details in cache first??
            if (cachedChannels.size() == 0) {
                menu(REQUEST_ID + requestNum++ ,cachedChannels);
            }
            EWChannel channel = cachedChannels.get(chanIndex);

            // Get using raw data
            EWTimeSeriesData seriesData = 
                getScnRaw(REQUEST_ID + requestNum++ ,channel , date, 
                            new Date(date.getTime() + duration));


            // Trim the series to the required length
            seriesData.TrimToDate(date, duration);

            // Check if gap data needs to be inserted at the front....
            
            if (seriesData.GetStartDate().getTime() - date.getTime() > 0)
                seriesData.addGapDataFront(date);
            
            
            // Check if gap data needs to be added to the end
            Date actualEndDate = new Date(date.getTime() + duration);
            if (seriesData.GetEndDate().getTime() < actualEndDate.getTime())
                seriesData.addGapData(actualEndDate.getTime() - seriesData.GetEndDate().getTime(), MISSING_DATA_VALUE);

            
            return seriesData;
        }
        catch (IOException iox)
        {
            System.out.println(iox.getMessage());
            throw iox;
        }
        }
    
    /**************************************************************************
     * Get the date and time of the most recently received data block for one
     *                      or all channels on an SDAS system.
     *
     * @param chanIndex Set to -1 to get the date and time of the most recent
     *                      block across all channels or set to the index of
     *                      the channel to get data for a single channel.
     * @param reRead Set to true to re-read the most recent data block from
     *                      disk into the servers memory cache, or set to false
     *                      to use a previously cached value.
     *
     * @return The date and time
     *
     * @throws IOException If there was a system IO error while attempting to
     *                      access the server.
     * @throws EWNetException.
     *************************************************************************/
    public Date GetMostRecentBlockTime (int chanIndex, boolean reRead)
    throws IOException, EWNetException    
    {
        // Note: This may not be emulating the behaviour of the SdasNet
        // class. Look at later...
        
        // Set most recent time to 1970 as default!
        long mostRecentTime = 0;
        
        if (reRead)
        {
            // Get new information into cache
            cachedChannels.clear();
            menu(REQUEST_ID + requestNum ,cachedChannels);
            requestNum++;
        }
        
        if (chanIndex > cachedChannels.size())
        {
            // If invalid index is recieved check all channels
            chanIndex = -1;
        }
        
        if (chanIndex == -1)
        {
            // Loop all channels looking for last end time
            for (int i = 0; i < cachedChannels.size(); i++)
            {
                long checkTime = cachedChannels.get(i).endTime.getTime();
                if ( checkTime > mostRecentTime) mostRecentTime = checkTime;
            }
        }
        else
        {
            // Get time from single channel's end time
            mostRecentTime = cachedChannels.get(chanIndex).endTime.getTime();   
        }
        
        // Return latest available time
        return new Date(mostRecentTime);
    }

    /**************************************************************
     * Get the number of data channels on an EW system.
     *
     * @return                      the number of channels
     *
     * @throws EWNetException       If there is a problem while retrieving
     *                              data from the server.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     **************************************************************/
    public int GetNChannels ()
    throws IOException, EWNetException

    {
        int count;
        String response;

        
        if (isClosed()) throw new EWNetException (EWNetException.NOT_OPEN, "Attempt to access server before calling open()");

        // check if the information is cached
        if (cachedChannels.size() > 0) return cachedChannels.size();
        
        // call waveserver to get the information
        this.menu(REQUEST_ID + requestNum++, cachedChannels);
        
        return cachedChannels.size();
    }    
    
    /**************************************************************
     * Get the channel number of a channel on an EW system from
     * its index. (This is for SDAS compatibility)
     *
     * @param chanIndex             the index (0 to NChannels-1)
     * @return                      the channel number
     * @throws EWNetException       If the object is not connected to
     *                              an EW server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     **************************************************************/
    public int GetChannel (int chanIndex)
    throws EWNetException, IOException

    {
        return chanIndex;
    }
    
    /**************************************************************
     * Get the number of channel groups on an EW system. Used for
     *          SDAS compatibility. Number of groups is always 1.
     *
     * @return                      the number of groups
     **************************************************************/
    public int GetNGroups ()
    {
        return 1;
    }
    
    /**************************************************************
     * Get the number of channel groups on an EW system. Used for
     *          SDAS compatibility. Number of groups is always 1.
     *
     * @return                      the number of groups
     **************************************************************/
    public int GetNGroupsNew ()
    {
       if (groupDetails == null)
       {
            createGroups();
       }
       
       return groupDetails.length;
    }
    
    /**************************************************************
     * Get channel group details from index of group. For Sdas 
     *                  compatibility. 
     *
     * @param groupIndex            the index (0 is the only accepted value)
     *
     * @return                      EWChannelDetails object
     *
     * @throws EWNetException       If the object is not connected to
     *                              an EW server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     **************************************************************/
    public DataChannelGroup GetGroupDetails (int groupIndex)
    throws EWNetException, IOException

    {
        int nch, n_tokens, i;
        String name, channels;
        EWChannelGroup groupDetails;
        
        
        if (isClosed()) throw new EWNetException (EWNetException.NOT_OPEN, "Attempt to access server before calling Open()");

        // check the channel index is in bounds
        if (groupIndex != 0) throw new EWNetException (EWNetException.BAD_CHANNEL_INDEX, "Channel index too large or too small [" + Integer.toString (groupIndex) + "]");

        // get the information from waveserver
        cachedChannels.clear();
        this.menu(REQUEST_ID + requestNum++, cachedChannels);
        

        // number of channels in this group
        nch = cachedChannels.size();
        name = "WaveServer";
        
        // Details of all channels in this group. This is passed as a string
        // into the ChannelGroup constructor where it is split into
        // separate channel details.
         channels = "0";
         for (i=1; i < nch; i++)
         {
           channels += "'" + i;
         }
         groupDetails = new EWChannelGroup (groupIndex, name, nch, channels);

         return groupDetails;
    }
    
    public DataChannelGroup GetGroupDetailsNew (int groupIndex)
    throws EWNetException, IOException

    {
        EWChannelGroup details;
        
        if (isClosed()) throw new EWNetException (EWNetException.NOT_OPEN, "Attempt to access server before calling Open()");

        // check the channel index is in bounds
        if (groupIndex > groupName.length) throw new EWNetException (EWNetException.BAD_CHANNEL_INDEX, "Channel index too large [" + Integer.toString (groupIndex) + "]");

        
        details = new EWChannelGroup (groupIndex, groupName[groupIndex], groupNumChannels[groupIndex], groupDetails[groupIndex]);
        return details;
    }
    
    /**************************************************************
     * Get an EW event for the given date. For SDAS compatibility
     *                  will always throw an END_OF_EVENT_LIST error.
     *
     * @param day, month, year      the event day, month and year as
     *                              integers. If these are all zero, the
     *                              next event for the previous date is
     *                              returned.
     * @return                      the event
     * @throws EWNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     **************************************************************/
    public DataEvent GetNextEvent (int day, int month, int year)
    throws EWNetException, IOException
    {
        // Earthworm does not support events. Throw an end of events error...
        throw new EWNetException (DataNetException.END_OF_EVENT_LIST, "There are no more events.");

        //return new EWEvent(new Date(0),1 , "None", 1 );
    }
    
    /**************************************************************
     * Get a file from the remote host.
     *
     * @param filename a string containing the filename to get
     * @return         a string containing the file contents
     * @throws DataNetException     If the object is not connected to
     *                              an SDAS server or the data from
     *                              the server could not be understood.
     * @throws IOException          If there was a system IO error while
     *                              attempting to access the server.
     *
     * @see DataNetException
     **************************************************************/
    public String GetFile (String filename) throws DataNetException, IOException
    {
      // Included for SDAS compatibility.
      return "";
    }
    
    
    /**************************************************************
     * Get details on a channel (such as its name and type).
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date at which the details are required
     * @return          the channel details
     *
     * @throws IOException If there is a socket problem
     * @throws EWNetException If the channel requested can not be found
     **************************************************************/
    public DataChannel GetChannelDetails (int chanIndex, Date date)
    throws IOException, EWNetException
    {
        // What is this doing????
        //return new EWChannel(1, "Blah", "Blah", "Blah");

        // Check that info is available else get from waveserver
        if (cachedChannels == null)
        {
                this.menu(REQUEST_ID + requestNum++, cachedChannels);

        }
        
        if (chanIndex >= cachedChannels.size())
        {
            throw new EWNetException(EWNetException.CHANNEL_NOT_FOUND, "The requested channel was not found on the server.");
        }

        return cachedChannels.get(chanIndex);
    }
    
        /**************************************************************
     * Get data statistics for the given channel/date
     *
     * @param chanIndex the index for the channel (0 to NChannels-1)
     * @param date      the date / hour at which the details are required
     * @param hourly    true to retrieve hourly stats, false for daily 
     * @return          the statistics
     **************************************************************/
    public DataStats ReadStats (int chanIndex, Date date, boolean hourly)
    {
        return null;
    }
    
    /**************************************************************
     * Detects if the connected server is using the classes
     *                  protocol.
     *
     * @return True if the classes protocol is being used, false
     *                  otherwise.
     **************************************************************/
    public boolean isProtocol ()
    {
        try
        {
            if (send("InvalidRequest")) // A wave server should respond with
                                         // "ERROR REQUEST"
            {
                String response = new BufferedReader (new InputStreamReader (input_stream)).readLine();
                if (response.trim().compareTo("ERROR REQUEST") == 0)
                {
                    return true;
                }
                else
                {
                    System.err.println(response.compareTo("ERROR REQUEST"));
                    return false;
                }
            }
            return false;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            return false;
        }
    }
    
    /**************************************************************************
     * Performs a MENU: request to the server.
     *
     * @param requestID ID generated by client to allow tracking of requests.
     * @param channels Object used to store results in.
     *
     * @return wether a valid reply was recieved.
     *
     * @throws IOException problems with connection (e.g. connection reset).
     * @throws EWNetException A bad response was recieved.
     **************************************************************************/
    public boolean menuNewer(String requestID, EWChannels channels)
    throws IOException, EWNetException
    {        
        BufferedInputStream bufStream;
        String response = "";
        
        // Check in case of requestNum overflow...
           if (requestNum > 1000000) requestNum = 0;
        
        // Send a MENU: request to the server
        if (send("MENU: " + requestID))
        {
            try {
                // Read response into memory
//                bufStream = new BufferedInputStream(server.getInputStream());
                bufStream = new BufferedInputStream(input_stream);
                int c;
                while ((c = bufStream.read()) != 10)
                {
                    if (c == -1) throw new IOException ("Connection reset");
                    response = response + (char)c;
                }
            }
            catch (IOException iox)
            {
                if (iox.getMessage() == "Connection reset")
                {
                    isReset = true;
                    if (ssh_tunnel != null)
                        Open (host_string, true, ssh_tunnel);
                    else
                        Open (host_string, ssh_string);
                    send("MENU: " + requestID);
//                    bufStream = new BufferedInputStream(server.getInputStream());
                    bufStream = new BufferedInputStream(input_stream);
                    int c;
                    while ((c = bufStream.read()) != 10)
                    {
                        if (c == -1) throw new IOException ("Connection reset");
                        response = response + (char)c;
                    } 
                }
                else
                    throw iox;  
            }
  
        
            // Debug Stuff
            //System.err.println(response);
            // End Debug stuff

            // Split the response into sections
            // In the response each trace's details are seperated by two
            // spaces. The following line splits the response using two
            // whitespace characters - the result should be each individual
            // trace in its own string.
            String[] traces = response.split("\\s\\s");
            
            
            // Check that the correct requestID has been returned
            // to be done...
            
            
            // Set the collections requestID
            channels.requestID = traces[0];

            // Loop through each trace and parse the information
            for (int i = 1; i < traces.length; i++)            
            {
                String[] trace = traces[i].trim().split("\\s");                
                if ( trace.length != channels.getSizeOfResponse() ) {
                    System.err.println(trace);
                    // An incorrect number of fields have been returned
                    throw (new EWNetException(EWNetException.WRONG_FIELDS_RECIEVED, "The server responded with the wrong number of fields: " + trace.length));
                }
                // Add the trace to the collection
                channels.add(trace);                
            }
            // Valid response recieved and parsed
            return true;   
        }
        
        // The request could not be sent to the server for some reason
        // Possible reasons: network timeout, connection reset, etc...
        return false;
        
    }
    
    /**************************************************************************
     * Performs a MENU: request to the server.
     *
     * @param requestID ID generated by client to allow tracking of requests.
     * @param channels Object used to store results in.
     *
     * @return wether a valid reply was recieved.
     *
     * @throws IOException problems with connection (e.g. connection reset).
     * @throws EWNetException A bad response was recieved.
     **************************************************************************/
    public boolean menu(String requestID, EWChannels channels)
    throws IOException, EWNetException
    {        
        BufferedInputStream bufStream;
        String response = "";

        // Check in case of requestNum overflow...
           if (requestNum > 1000000) requestNum = 0;
        
        // Send a MENU: request to the server
        if (send("MENU: " + requestID))
        {
            try {
                // Read response into memory
//                bufStream = getBufferedInputStream();
                bufStream = new BufferedInputStream (input_stream);
                int c;
                while ((c = bufStream.read()) != 10)
                {
                    if (c == -1) throw new IOException ("Connection reset");
                    response = response + (char)c;
                }
            }
/*            catch (EWNetException ewx)
            {
                if (ewx.GetErrorCode() == EWNetException.CONNECTION_RESET)
                {
                    isReset = true;
                    if (ssh_tunnel != null)
                        Open (host_string, true, ssh_tunnel);
                    else
                        Open (host_string, ssh_string);
                    send("MENU: " + requestID);
//                    bufStream = getBufferedInputStream();
                    bufStream = new BufferedInputStream (input_stream);
                    int c;
                    while ((c = bufStream.read()) != 10)
                    {
                        response = response + (char)c;
                    }  
                }
                else
                {
                    throw ewx;
                }
            }*/
            catch (IOException iox)
            {
                if (iox.getMessage() == "Connection reset")
                {
                    isReset = true;
                    if (ssh_tunnel != null)
                        Open (host_string, true, ssh_tunnel);
                    else
                        Open (host_string, ssh_string);
                    send("MENU: " + requestID);
//                    bufStream = getBufferedInputStream();
                    bufStream = new BufferedInputStream (input_stream);
                    int c;
                    while ((c = bufStream.read()) != 10)
                    {
                        if (c == -1) throw new IOException ("Connection reset");
                        response = response + (char)c;
                    } 
                }
                else
                    throw iox;  
            }
  
        
            // Debug Stuff
//            System.err.println(response);
            // End Debug stuff

            // Split the response into sections
            // In the response each trace's details are seperated by two
            // spaces. The following line splits the response using two
            // whitespace characters - the result should be each individual
            // trace in its own string.
            String[] traces = response.split("\\s\\s");
            
            
            // Check that the correct requestID has been returned
            // to be done...
            
            
            // Set the collections requestID
            channels.requestID = traces[0];

            // Loop through each trace and parse the information
            for (int i = 1; i < traces.length; i++)            
            {
                String[] trace = traces[i].trim().split("\\s");                
                if ( trace.length != channels.getSizeOfResponse() ) {
                    System.err.println(trace);
                    // An incorrect number of fields have been returned
                    throw (new EWNetException(EWNetException.WRONG_FIELDS_RECIEVED, "The server responded with the wrong number of fields: " + trace.length));
                }
                // Add the trace to the collection
                channels.add(trace);                
            }
            // Valid response recieved and parsed
            return true;   
        }
        
        // The request could not be sent to the server for some reason
        // Possible reasons: network timeout, connection reset, etc...
        return false;
        
    }
    
    public void DebugIO(boolean on) 
    {
        System.out.println ("IO debugging not implemented in Earthworm");
    }
    
    private void createGroups()
    {
        // group variables
        String[] tempGroupDetails;
        int[] tempGroupNumChannels;
        String[] tempGroupName;
        
        // create space to play with
        tempGroupDetails = new String[cachedChannels.size() + 1];
        tempGroupNumChannels = new int[cachedChannels.size() + 1];
        tempGroupName = new String[cachedChannels.size() + 1];
        
        // counter to keep track of index
        int index = 0;
        
        // First create an Earthworm group that contains all the traces
        tempGroupName[index] = "Earthworm";
        tempGroupNumChannels[index] = cachedChannels.size();
        tempGroupDetails[index] = Integer.toString(index);
        for (int i = 0; i < cachedChannels.size(); i++)
        {
           tempGroupDetails[index] += "'" + i; 
        }
        index++;
        
        // Array to keep track of matched channels
        boolean[] matched = new boolean[cachedChannels.size()];
        for (int i = 0; i < cachedChannels.size(); i++)
        {
            matched[i] = false;
        }
        
        // Make best guesses at what groups might be wanted
        for (int count = 0; count < cachedChannels.size(); count++)
        {
            EWChannel channel = cachedChannels.get(count);
            String siteCode = channel.siteCode;
            String networkID = channel.networkID;
            if (!matched[count])
            {
                boolean inGroup[] = new boolean[cachedChannels.size()];
                inGroup[count] = true;
                int numMatches = 1;
                
                for (int count2 = 0; count2 < cachedChannels.size(); count2++)
                {
                    if (!inGroup[count2])
                    {
                        // Check for same siteCode ane networkID
                        if (siteCode.compareTo(cachedChannels.get(count2).siteCode) == 0)
                        {
                            if (networkID.compareTo(cachedChannels.get(count2).networkID) == 0)
                            {
                                // We have a match
                                inGroup[count2] = true;
                                numMatches++;
                            }
                            
                        }
                        
                    }
                }
                
                
                // Create this groups details and mark all the members as matched
                tempGroupName[index] = siteCode + " " + networkID;
                tempGroupNumChannels[index] = numMatches;
                tempGroupDetails[index] = Integer.toString(index);
                
                for (int i = 0; i < cachedChannels.size(); i++)
                {
                    if (inGroup[i])
                    {
                        tempGroupDetails[index] += "'" + i;
                        matched[i] = true;
                    }
                }
                
                // Increment index
                index++;
            }
        }
        
        // Clean up the arrays
        groupDetails = new String[index];
        groupNumChannels = new int[index];
        groupName = new String[index];
        
        for (int i = 0; i < index; i ++)
        {
            groupDetails[i] = tempGroupDetails[i];
            groupNumChannels[i] = tempGroupNumChannels[i];
            groupName[i] = tempGroupName[i];
        }
        
    }

}