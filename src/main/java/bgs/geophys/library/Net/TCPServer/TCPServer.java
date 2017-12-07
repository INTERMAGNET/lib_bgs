/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPServer;

import bgs.geophys.library.LogConfig.LogConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.Logger;

/**
 * A TCP server that allows string exchange with a client. It's built using
 * objects for the communication that can be extended to allow for more
 * complex protocols. To use it:
 * 
 * Create a TCPConnectionFactory object. The factory will create the individual connections
 * to a client when the server socket receives an incoming connection. It allows
 * you to specify the object that does the actual communication, so setting the
 * protocol used between client and server.
 * 
 * The TCPConnectionFactory will need to provide a concrete implementation of
 * TCPAbstractConnectionHandler, which starts a new thread for each connection,
 * and override the run method to manage the communication with the client.
 * 
 * The TCPConnectionFactory will also need to provide an object based on TCPConnectionProtocol
 * to manage the protocol exchange with the client. Two objects are currently
 * available for this: TCPConnectionProtocol and TCPPacketConnectionProtocol. 
 * Or you can extend TCPConnectionProtocol with your own protocol.
 * 
 * Create a TCPServer and pass it the TCPConnectionFactory and the port to listen on.
 * 
 * @author smf
 */
public class TCPServer
implements Runnable
{
    public static final int DEFAULT_PORT = 6901;
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPServer.class);

    private ServerSocket listen_socket;
    private Thread thread;
    private boolean abort_thread;
    private TCPConnectionFactory connection_factory;
    
    public TCPServer (TCPConnectionFactory connection_factory)
    throws IOException
    {
        this (connection_factory, DEFAULT_PORT);
    }
    
    public TCPServer (TCPConnectionFactory connection_factory, int port)
    throws IOException
    {
        // store the listener
        this.connection_factory = connection_factory;
        
        // listen on the given port
        LOGGER.info ("Opening listening socket on port " + port);
        listen_socket = new ServerSocket(port);
        LOGGER.info ("Listening socket open on port " + port);
        
        // start a thread to listen for incoming connections
        abort_thread = false; 
        thread = new Thread (this); 
        thread.setName ("TCPServerListenerThread"); 
        LOGGER.info ("Starting thread '" + thread.getName() + "' to listen for connections");
        thread.start (); 
    }

    public void close ()
    {
        abort_thread = true;
        try 
        {
            LOGGER.info ("Closing listening socket");
            listen_socket.close(); 
            LOGGER.info ("Closed listening socket");
        }
        catch (IOException e) 
        { 
            LOGGER.info ("Problem closing listening socket", e);
        }
    }
    
    @Override
    public void run() 
    {
        LOGGER.info ("Started thread '" + Thread.currentThread().getName() + "' to listen for connections");
        while (! abort_thread) 
        {
            try
            {
                // receive a new connection
                LOGGER.info ("Waiting to accept incoming connection");
                Socket socket = listen_socket.accept();
                LOGGER.info ("Connection accepted from " + socket.toString());
    
                // hand off the connection to the listener
                if (! abort_thread)
                {
                    TCPConnectionProtocol connection = connection_factory.createConnectionFromSocket(socket);
                    String name = connection_factory.getConnectionHandlerName();
                    TCPAbstractConnectionHandler connection_handler = connection_factory.createConnectionHandler(connection, name);
                    LOGGER.info ("Starting connection handler thread " + name);
                    connection_handler.start();
                }
            }
            catch (IOException e)
            {
                LOGGER.info ("IO error accepting or handing off connection", e);
                if (! abort_thread)
                    connection_factory.handleServerError(e);
            }
        }
        
 }
    
}
