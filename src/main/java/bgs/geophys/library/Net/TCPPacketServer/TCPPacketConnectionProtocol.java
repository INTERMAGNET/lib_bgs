/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPPacketServer;

import bgs.geohpys.library.LogConfig.LogConfig;
import bgs.geophys.library.Net.TCPServer.TCPConnectionProtocol;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import org.apache.logging.log4j.Logger;

/**
 * A TCP connection object that adds to the ability of a TCCPServer by 
 * allowing communication using packets marshalled and unmarshalled from data
 * 
 * See TCPServer to understand bow to use this class
 * 
 * @author smf
 */
public class TCPPacketConnectionProtocol extends TCPConnectionProtocol
{
    private static final Logger LOGGER = LogConfig.getLogger(TCPPacketConnectionProtocol.class);
    
    public TCPPacketConnectionProtocol ()
    {
        super ();
    }
    
    public TCPPacketConnectionProtocol (Socket connection_socket)
    {
        super (connection_socket);
    }
    
    public UnMarshallFromPacket read ()
    throws IOException, ParseException
    {
        // the start of the received data will be a number terminated by ':'
        // which describes the length of the packet to be received
        LOGGER.info ("Waiting to read packet length");
        String total_length_num_string = this.read((byte) ':', false);
        LOGGER.info ("Read: " + total_length_num_string);
        if (total_length_num_string.length() < 1)
            throw new IOException ("Empty total length");
        int total_length = 0;
        try
        {
            total_length = Integer.parseInt(total_length_num_string);
        }
        catch (NumberFormatException e)
        {
            throw new IOException ("Non-numeric total length: " + total_length_num_string);
        }
        
        // read the remainder of the packet
        LOGGER.info ("Waiting to read packet payload");
        String payload = this.read (total_length);
        LOGGER.info ("Read: " + payload);
        
        // construct the unmarshalled data
        return new UnMarshallFromPacket (total_length_num_string + ":" + payload);
    }
    
    public void write (MarshallToPacket packet)
    throws IOException
    {
        this.write(packet.getPacketData());
    }
    
}
