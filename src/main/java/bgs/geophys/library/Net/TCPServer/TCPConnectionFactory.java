/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPServer;

import java.io.IOException;
import java.net.Socket;

/**
 * A TCP server listener should have a method that can be used to set it's
 * TCP connection object. When a new connection is received, the connection
 * object will be set (via this method) and then the thread started to
 * handle IO with the client
 * 
 * @author smf
 */
public interface TCPConnectionFactory
{
    public TCPConnectionProtocol createConnectionFromSocket (Socket socket);
    public TCPAbstractConnectionHandler createConnectionHandler (TCPConnectionProtocol connection, String name);
    public String getConnectionHandlerName ();
    public void handleServerError (IOException e);
}
