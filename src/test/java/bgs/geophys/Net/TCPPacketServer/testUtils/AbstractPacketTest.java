package bgs.geophys.Net.TCPPacketServer.testUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import bgs.geophys.Net.TCPPacketServer.MarshallToPacket;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Shared test data for packet manipulation classes. Inherit from this class (calling super
 * constructor) to use this data
 * 
 * @author smf
 */
public abstract class AbstractPacketTest 
{

    protected final static String METHOD_NAME = "TestMethod";
    protected final static String ARG1 = "String Argument 1";
    protected final static String ARG2 = "String Argument 2";
    protected final static String [] ARG3 = {"String", "Argument", "3"};
    protected final static String ARG4 = "Long String Argument with lots of characters in it to push the data over a single packet boundary";
    protected final static int ARG5 = Integer.MAX_VALUE;
    protected final static int ARG6 = Integer.MIN_VALUE;
    protected final static int [] ARG7 = {0, 1, 2, 3, -10000, 10000};
    protected static Date ARG8;
    protected static Date [] ARG9;
    protected final static String ARG10 = "Final argument";
    
    protected MarshallToPacket mtp;
    
    static
    {
        GregorianCalendar cal = new GregorianCalendar (2000, 0, 1, 0, 0, 0);
        cal.set (GregorianCalendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        ARG8 = cal.getTime();
        
        ARG9 = new Date [3];
        cal.add (GregorianCalendar.YEAR, 1);
        ARG9 [0] = cal.getTime();
        cal.add (GregorianCalendar.MONTH, 1);
        ARG9 [1] = cal.getTime();
        cal.add (GregorianCalendar.DATE, 1);
        ARG9 [2] = cal.getTime();
    }
    
    public AbstractPacketTest() 
    {
        // create a new object to test
        mtp = new MarshallToPacket (METHOD_NAME);
        
        // add some strings
        mtp.addString (ARG1);
        mtp.addString (ARG2);
        mtp.addArrayString (ARG3);
        mtp.addString (ARG4);
        
        // add some integers
        mtp.addInt (ARG5);
        mtp.addInt (ARG6);
        mtp.addArrayInt (ARG7);
        
        // add a date and a date array
        mtp.addDate (ARG8);
        mtp.addArrayDate (ARG9);
        
        // add a final string so we can clearly see the last argument in the packets
        mtp.addString (ARG10);
    }
    
}
