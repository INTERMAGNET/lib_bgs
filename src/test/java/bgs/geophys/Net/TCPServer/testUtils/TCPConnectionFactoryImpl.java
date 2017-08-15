/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPServer.testUtils;

import bgs.geophys.Net.TCPServer.TCPAbstractConnectionHandler;
import bgs.geophys.Net.TCPServer.TCPConnectionFactory;
import bgs.geophys.Net.TCPServer.TCPConnectionProtocol;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author smf
 */
public class TCPConnectionFactoryImpl 
implements TCPConnectionFactory
{

    private IOException stored_exception;
    private TCPAbstractConnectionHandler last_connection_handler;
    
    public TCPConnectionFactoryImpl ()
    {
        last_connection_handler = null;
    }
    
    @Override
    public TCPConnectionProtocol createConnectionFromSocket(Socket socket) 
    {
        return new TCPConnectionProtocol (socket);
    }

    @Override
    public TCPAbstractConnectionHandler createConnectionHandler(TCPConnectionProtocol connection, String name) 
    {
        last_connection_handler = new TCPConnectionHandler (connection, name);
        return last_connection_handler;
    }

    @Override
    public String getConnectionHandlerName() 
    {
        return "TestName";
    }

    @Override
    public void handleServerError(IOException e) 
    {
        stored_exception = e;
    }
    
    public IOException getStoredException () { return stored_exception; }
    
    public TCPAbstractConnectionHandler getLastConnectionHandler () { return last_connection_handler; }
    
}
