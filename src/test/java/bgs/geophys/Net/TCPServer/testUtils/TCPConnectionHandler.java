/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPServer.testUtils;

import bgs.geohpys.library.LogConfig.LogConfig;
import bgs.geophys.Net.TCPServer.TCPAbstractConnectionHandler;
import bgs.geophys.Net.TCPServer.TCPConnectionProtocol;
import java.io.IOException;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author smf
 */
public class TCPConnectionHandler 
extends TCPAbstractConnectionHandler
{

    public static final String HELLO_STRING = "Hello";
    public static final String RESPONSE_STRING = "Message received";
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPConnectionHandler.class);
    
    private String length_string;
    private String data;
    private IOException stored_exception;
    
    public TCPConnectionHandler (TCPConnectionProtocol connection, String name)
    {
        super (connection, name);
        length_string = "";
        data = "";
        LOGGER.info ("TCPConnectionHandler created");
    }

    @Override
    public void run() 
    {
        LOGGER.info ("TCPConnectionHandler thread '" + Thread.currentThread().getName() + "' started");
        try
        {
            sendNullTermString(HELLO_STRING);
            LOGGER.info ("Sent hello: " + HELLO_STRING);
            length_string = connection.read((byte) ':', false);
            LOGGER.info ("Received length_string: " + length_string);
            int length = Integer.parseInt(length_string);
            data = connection.read (length);
            LOGGER.info ("Received payload: " + data);
            sendNullTermString(RESPONSE_STRING);
            LOGGER.info ("Sent response: " + RESPONSE_STRING);
        }
        catch (IOException e)
        {
            LOGGER.info ("IOException in TCPConnetionHandler thread", e);
            stored_exception = e;
        }
        
        LOGGER.info ("Closing connection");
        connection.close();
    }
    
    public String getLengthString () { return length_string.toString(); }
    public String getData () { return data.toString(); }
    
    public IOException getStoredException () { return stored_exception; }
 
    private void sendNullTermString (String string)
    throws IOException
    {
        connection.write (string + "\u0000");
    }
    
}
