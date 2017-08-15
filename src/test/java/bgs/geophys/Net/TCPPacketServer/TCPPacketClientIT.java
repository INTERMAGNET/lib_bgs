/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPPacketServer;

import bgs.geohpys.library.LogConfig.LogConfig;
import bgs.geophys.Net.TCPPacketServer.testUtils.TCPConnectionFactoryImpl;
import bgs.geophys.Net.TCPServer.TCPServer;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.*;

/**
 * Test both client and server for exchanging TCP packets
 * 
 * @author smf
 */
public class TCPPacketClientIT {
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPPacketClientIT.class);
    
    public TCPPacketClientIT() 
    {
    }

    @Test
    public void testServerConnection ()
    throws IOException, ParseException
    {
        
        int test_port = 23486;
        
        // set up a server
        LOGGER.info ("Creating server socket");
        TCPConnectionFactoryImpl tcp_connection_factory = new TCPConnectionFactoryImpl();
        TCPServer server = new TCPServer (tcp_connection_factory, test_port);
        
        // connect to it
        LOGGER.info ("Creating client");
        TCPPacketClient client = new TCPPacketClient ("localhost", test_port, 2000, true);
        
        // send a packet
        MarshallToPacket test_tx_packet = new MarshallToPacket ("test");
        test_tx_packet.addString("String1");
        test_tx_packet.addString("String2");
        test_tx_packet.addArrayString(new String [] {"String", "3", "as an", "array"});
        test_tx_packet.addInt(Integer.MAX_VALUE);
        test_tx_packet.addInt(Integer.MIN_VALUE);
        test_tx_packet.addArrayInt(new int [] {0, 1, 2, 3, -10000, 10000});
        GregorianCalendar cal = new GregorianCalendar (2000, 0, 1, 0, 0, 0);
        cal.set (GregorianCalendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        test_tx_packet.addDate (cal.getTime());
        Date date_array [] = new Date [3];
        cal.add (GregorianCalendar.YEAR, 1);
        date_array [0] = cal.getTime();
        cal.add (GregorianCalendar.MONTH, 1);
        date_array [1] = cal.getTime();
        cal.add (GregorianCalendar.DATE, 1);
        date_array [2] = cal.getTime();
        test_tx_packet.addArrayDate(date_array);
        LOGGER.info ("Writing packet: " + test_tx_packet.getPacketData());
        client.write (test_tx_packet);
        
        // receive the response
        UnMarshallFromPacket test_rx_packet = client.read();
        LOGGER.info ("Received packet: " + test_rx_packet.getMethodName() + " " + test_rx_packet.getArgumentString(0));
        assertEquals("Return_test", test_rx_packet.getMethodName());
        String expected = "Return_test (String) String1 (String) String2 (ArrayString)" +
                          " [String] [3] [as an] [array] (Int) 2147483647 (Int) -214748" +
                          "3648 (ArrayInt) [0] [1] [2] [3] [-10000] [10000] (Date) Sat" +
                          " Jan 01 00:00:00 GMT 2000 (ArrayDate) [Mon Jan 01 00:00:00 G" +
                          "MT 2001] [Thu Feb 01 00:00:00 GMT 2001] [Fri Feb 02 00:00:0" +
                          "0 GMT 2001]";
        assertEquals(expected, test_rx_packet.getArgumentString(0));
        // send a quit packet
        MarshallToPacket quit_tx_packet = new MarshallToPacket ("quit");
        LOGGER.info ("Writing packet: " + quit_tx_packet.getPacketData());
        client.write (quit_tx_packet);
        UnMarshallFromPacket quit_rx_packet = client.read();
        LOGGER.info ("Received packet: " + quit_rx_packet.getMethodName() + " " + quit_rx_packet.getArgumentString(0));
        assertEquals("Return_quit", quit_rx_packet.getMethodName());
        
        // server should be down
        try { Thread.sleep (3000l); }
        catch (InterruptedException e) { }
        LOGGER.info ("Checking connection is down");
        if (tcp_connection_factory.getLastConnectionHandler().isAlive())
            fail ("Server handling connection is still up");
    }
    
}
