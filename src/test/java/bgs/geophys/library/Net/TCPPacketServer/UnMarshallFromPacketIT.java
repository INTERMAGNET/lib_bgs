/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPPacketServer;

import bgs.geophys.library.Net.TCPPacketServer.UnMarshallFromPacket;
import bgs.geophys.library.Net.TCPPacketServer.testUtils.AbstractPacketTest;
import java.text.ParseException;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smf
 */
public class UnMarshallFromPacketIT extends AbstractPacketTest
{
    
    private UnMarshallFromPacket ufp;
    private UnMarshallFromPacket ufp2;
    
    public UnMarshallFromPacketIT() 
    throws ParseException
    {
        super ();
        
        ufp = new UnMarshallFromPacket (mtp.getPacketData());
        ufp2 = new UnMarshallFromPacket (mtp2.getPacketData());
    }

    /**
     * Test of getMethodName method, of class UnMarshallFromPacket.
     */
    @Test
    public void testGetMethodName() 
    {
        System.out.println("getMethodName");
        assertEquals (METHOD_NAME, ufp.getMethodName());
        assertEquals (METHOD_NAME_ERR, ufp2.getMethodName());
    }

    @Test
    public void testIsError ()
    {
        System.out.println ("isError");
        assertEquals (false, ufp.isError());
        assertEquals (true, ufp2.isError());
    }
    
    @Test
    public void testGetErrorMessage ()
    {
        System.out.println ("getErrorMessage");
        assertEquals (null, ufp.getErrorMessage());
        assertEquals (ERRMSG, ufp2.getErrorMessage());
    }
    
    /**
     * Test of getNArguments method, of class UnMarshallFromPacket.
     */
    @Test
    public void testGetNArguments() 
    {
        System.out.println("getNArguments");
        assertEquals (10, ufp.getNArguments());
    }

    /**
     * Test of getArgumentString method, of class UnMarshallFromPacket.
     */
    @Test
    public void testGetArgumentString() 
    {
        System.out.println("getArgumentString");
        assertEquals(ARG1, ufp.getArgumentString(0));
        assertEquals(ARG2, ufp.getArgumentString(1));
        assertEquals(ARG4, ufp.getArgumentString(3));
        assertEquals(ARG10, ufp.getArgumentString(9));
    }

    @Test
    public void testGetArgumentStringArray() 
    {
        String strings [] = ufp.getArgumentArrayString (2);
        for (int count=0; count<strings.length; count ++)
            assertEquals (ARG3[count], strings[count]);
    }
    
    /**
     * Test of getArgumentInt method, of class UnMarshallFromPacket.
     */
    @Test
    public void testGetArgumentInt() 
    {
        System.out.println("getArgumentInt");
        assertEquals(ARG5, ufp.getArgumentInt(4));
        assertEquals(ARG6, ufp.getArgumentInt(5));
    }

    @Test
    public void testGetArgumentIntArray() 
    {
        int int_vals [] = ufp.getArgumentArrayInt (6);
        for (int count=0; count<int_vals.length; count ++)
            assertEquals (ARG7[count], int_vals[count]);
    }
    
    /**
     * Test of getArgumentDate method, of class UnMarshallFromPacket.
     */
    @Test
    public void testGetArgumentDate() 
    {
        System.out.println("getArgumentDate");
        assertEquals(ARG8.getTime(), ufp.getArgumentDate(7).getTime());
    }
    
    @Test
    public void testGetArgumentDateArray() 
    {
        Date dates [] = ufp.getArgumentArrayDate (8);
        for (int count=0; count<dates.length; count ++)
            assertEquals (ARG9[count].getTime(), dates[count].getTime());
    }
}
