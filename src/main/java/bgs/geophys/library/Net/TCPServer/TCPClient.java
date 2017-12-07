/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPServer;

import bgs.geophys.library.LogConfig.LogConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.Logger;

/**
 * A client to the TCPServer object that uses all the methods for reading and
 * writing from the TCPConnectionProtocol class.
 * 
 * @author smf
 */
public class TCPClient extends TCPConnectionProtocol
{
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPClient.class);
    
    public TCPClient (String address) 
    throws IOException 
    {
        this (address, TCPServer.DEFAULT_PORT, 0, true); 
    }
    
    public TCPClient (String address, int port, int timeout, boolean reuse)
    throws IOException
    {
        connection_socket = new Socket (address, port);
        connection_socket.setSoTimeout(timeout);
        connection_socket.setReuseAddress(reuse);
        LOGGER.info ("Created client socket: " + connection_socket.toString());
        
        out_to_remote = new BufferedOutputStream (connection_socket.getOutputStream());
        in_from_remote = new BufferedInputStream (connection_socket.getInputStream());
    }
    
    
}
