/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPServer;

import bgs.geophys.library.Net.TCPServer.TCPServer;
import bgs.geophys.library.Net.TCPServer.TCPConnectionFactory;
import bgs.geophys.library.LogConfig.LogConfig;
import bgs.geophys.library.NET.TCPServer.testUtils.TCPConnectionHandler;
import bgs.geophys.library.NET.TCPServer.testUtils.TCPConnectionFactoryImpl;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author smf
 */
public class TCPServerIT 
{
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPServerIT.class);
    
    public TCPServerIT() 
    {
    }

    /**
     * Test of close method, of class TCPServer.
     */
    @Test
    public void testClose()
    throws IOException
    {
        System.out.println("testClose");
        
        int test_port = 23486;
        
        // create the server
        LOGGER.info ("Creating server socket");
        TCPConnectionFactory tcp_connection_factory = new TCPConnectionFactoryImpl();
        TCPServer server = new TCPServer (tcp_connection_factory, test_port);

        // check we can connect to it
        LOGGER.info ("Creating client socket");
        Socket socket = new Socket ();
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress ("localhost", test_port), 2000);
        LOGGER.info ("Created client socket: " + socket.toString());
        socket.close();
        LOGGER.info ("Closed client socket");
        
        // stop the server
        server.close();
        LOGGER.info ("Closed server socket");

        // wait till it's stopped
        try {Thread.sleep(200l); }
        catch (InterruptedException e) { }

        // attempt to connect to it
        boolean socket_exception_thrown = false;
        try
        {
            LOGGER.info ("Attempting to create to server");
            socket = new Socket ();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress ("localhost", test_port), 2000);
            LOGGER.info ("Connected to server (THIS IS WRONG): " + socket.toString());
        }
        catch (ConnectException e)
        {
            LOGGER.info ("Connect exception", e);
            socket_exception_thrown = true;
        }
        
        if (! socket_exception_thrown)
            fail("Server still receiving connections");
    }

    /**
     * Test of run method, of class TCPServer.
     */
    @Test
    public void testRun() 
    throws IOException
    {
        System.out.println("run");
        
        int test_port = 23487;
        String test_payload = "Some stuff to send across the network";
        
        // create the server
        LOGGER.info ("Creating server socket");
        TCPConnectionFactoryImpl tcp_connection_factory = new TCPConnectionFactoryImpl();
        TCPServer server = new TCPServer (tcp_connection_factory, test_port);
        
        // make a client connect to the server
        LOGGER.info ("Creating client socket");
        Socket socket = new Socket ("localhost", test_port);
        socket.setSoTimeout(2000);
        socket.setReuseAddress(true);
        LOGGER.info ("Created client socket: " + socket.toString());
        BufferedOutputStream out_to_server = new BufferedOutputStream (socket.getOutputStream());
        BufferedInputStream in_from_server = new BufferedInputStream (socket.getInputStream());
        
        // get the welcome message
        String hello = read(in_from_server, (byte) 0, false);
        LOGGER.info ("Received hello: " + hello);
        assertEquals(TCPConnectionHandler.HELLO_STRING, hello);
        
        // the TCP connection handler exepects to receive a string of the form
        // "length:<payload>", after which it will send a response and exit
        String test_length_string = Integer.toString (test_payload.length());
        String data = test_length_string + ":" + test_payload;
        LOGGER.info ("Sending payload: " + data);
        out_to_server.write (data.getBytes());
        out_to_server.flush();
        
        // wait for a bit and then check if the data arrived
        try {Thread.sleep(2000l); }
        catch (InterruptedException e) { }

        // bit of a hack to get to the connection handler which should have received the data we sent
        TCPConnectionHandler tcp_connection_handler = (TCPConnectionHandler) tcp_connection_factory.getLastConnectionHandler();
        assertEquals (test_length_string, tcp_connection_handler.getLengthString());
        assertEquals (test_payload, tcp_connection_handler.getData());
        
        // get the response
        LOGGER.info ("Receiving response");
        String response = read(in_from_server, (byte) 0, false);
        LOGGER.info ("Received response: " + response);
        assertEquals (TCPConnectionHandler.RESPONSE_STRING, response);
    }
    
    /** read up to this delimiter character */
    private String read (BufferedInputStream in_from_server, byte delim_char, boolean include_delim)
    throws IOException
    {
        int alloc_increment = 20;
        byte buffer [] = new byte [alloc_increment];
        int pos = 0;
        int single_byte = 0;
        do
        {
            single_byte = in_from_server.read();
            if (single_byte < 0) throw new IOException ("End of stream detected");
            if ((single_byte != delim_char) || include_delim)
            {
                if (pos >= buffer.length)
                {
                    byte new_buffer [] = new byte [buffer.length + alloc_increment];
                    System.arraycopy(buffer, 0, new_buffer, 0, buffer.length);
                    buffer = new_buffer;
                }
                buffer [pos ++] = (byte) single_byte;
            }
        } while ((byte) single_byte != delim_char);
        return new String (buffer, 0, pos);
    }
    
}
