/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPPacketServer;

import bgs.geophys.Net.TCPPacketServer.testUtils.AbstractPacketTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smf
 */
public class MarshallToPacketIT extends AbstractPacketTest
{
    
    
    public MarshallToPacketIT() 
    {
        super ();
    }

    /**
     * Test of getPacketData method, of class MarshallToPacket.
     */
    @Test
    public void testGetPacketData() 
    {
        System.out.println("getPacketData");
        String packets = mtp.getPacketData();
        String expected = "395:10:TestMethod10:String:17:String Argument 1String" +
                          ":17:String Argument 2ArrayString:23:3:6:String8:Argum" +
                          "ent1:3String:97:Long String Argument with lots of cha" +
                          "racters in it to push the data over a single packet b" +
                          "oundaryint:10:2147483647int:11:-2147483648ArrayInt:29" +
                          ":6:1:01:11:21:36:-100005:10000Date:12:946684800000Arr" +
                          "ayDate:47:3:12:97830720000012:98098560000012:98107200" +
                          "0000String:14:Final argument";
        assertEquals(expected, packets);
    }
    
}
