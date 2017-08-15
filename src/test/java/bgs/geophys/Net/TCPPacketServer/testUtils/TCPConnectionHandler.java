/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPPacketServer.testUtils;

import bgs.geohpys.library.LogConfig.LogConfig;
import bgs.geophys.Net.TCPPacketServer.MarshallToPacket;
import bgs.geophys.Net.TCPPacketServer.TCPPacketConnectionProtocol;
import bgs.geophys.Net.TCPPacketServer.UnMarshallFromPacket;
import bgs.geophys.Net.TCPServer.testUtils.*;
import bgs.geophys.Net.TCPServer.TCPAbstractConnectionHandler;
import bgs.geophys.Net.TCPServer.TCPConnectionProtocol;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author smf
 */
public class TCPConnectionHandler 
extends TCPAbstractConnectionHandler
{

    private static final Logger LOGGER = LogConfig.getLogger(TCPConnectionHandler.class);
    
    private Exception stored_exception;
    
    public TCPConnectionHandler (TCPConnectionProtocol connection, String name)
    {
        super (connection, name);
        LOGGER.info ("TCPConnectionHandler created");
    }

    @Override
    public void run() 
    {
        TCPPacketConnectionProtocol connection = (TCPPacketConnectionProtocol) this.connection;
        LOGGER.info ("TCPConnectionHandler thread '" + Thread.currentThread().getName() + "' started");
        try
        {
            // loop until quit
            UnMarshallFromPacket rx_packet;
            do
            {
                // receive a packet
                LOGGER.info ("Waiting to read packet");
                rx_packet = connection.read();
                LOGGER.info ("Read packet: " + rx_packet.getMethodName());
                
                // make a summary of the packet
                String response = "Return_" + rx_packet.getMethodName();
                for (int count=0; count<rx_packet.getNArguments(); count++)
                {
                    switch (rx_packet.getArgumentType(count))
                    {
                        case String:
                            response += " (String) " + rx_packet.getArgumentString(count);
                            break;
                        case ArrayString:
                            response += " (ArrayString)";
                            for (String string : rx_packet.getArgumentArrayString(count))
                                response += " [" + string + "]";
                            break;
                        case Int:
                            response += " (Int) " + Integer.toString (rx_packet.getArgumentInt(count));
                            break;
                        case ArrayInt:
                            response += " (ArrayInt)";
                            for (int int_val : rx_packet.getArgumentArrayInt(count))
                                response += " [" + Integer.toString(int_val) + "]";
                            break;
                        case Date:
                            response += " (Date) " + ((rx_packet.getArgumentDate(count)).toString ());
                            break;
                        case ArrayDate:
                            response += " (ArrayDate)";
                            for (Date date : rx_packet.getArgumentArrayDate(count))
                                response += " [" + date.toString() + "]";
                            break;
                    }
                }
                
                // return this summary to the caller
                MarshallToPacket tx_packet = new MarshallToPacket ("Return_" + rx_packet.getMethodName());
                LOGGER.info ("Resonding with packet: " + tx_packet.getPacketData());
                tx_packet.addString(response);
                LOGGER.info ("Resonding with packet: " + tx_packet.getPacketData());
                connection.write(tx_packet);
            } while (! rx_packet.getMethodName().equalsIgnoreCase("quit"));
        }
        catch (IOException | ParseException e)
        {
            LOGGER.info ("IOException in TCPConnetionHandler thread", e);
            stored_exception = e;
        }
        
        // allow some time before cclosing the connection
        try { Thread.sleep (2000l); }
        catch (InterruptedException e) { }
        
        LOGGER.info ("Closing connection");
        connection.close();
    }
    
    
    public Exception getStoredException () { return stored_exception; }
 
}
