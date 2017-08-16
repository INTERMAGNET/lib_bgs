package bgs.geophys.Net.TCPPacketServer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Marshall a method call to a packet for transport across the network
 * 
 * 1.) Create a MarshallToPacket object with the name of the method you want to call
 * 2.) Add parameters (String, int or Date)
 * 3.) call getPacket() to retrieve the packet that needs to be sent over the network
 * 
 * Packet contents:
 * 
 * <total-length>:<method-name-len>:<method-name><n-args>:[<arg-type>:<arg-len>:<arg>[<arg-type>:<arg-len>:<arg>[...]]]
 * <total-length>:<method-name-len>:<method-name>-1:<err-msg-len>:<err-msg>
 * 
 * All data in a packet is ASCII
 * <total-length> is the size of the data in the packets, not including the "<total-length>:" string
 * <method-name> is the name of the method to be called (or, for a return value, the name of the packet that was called
 * <n-args> is the number of arguments in the packet 0 <= n_args <= number of arguments
 *   <arg-type> can be String, int or Date, in which case 
 *     <arg> is a string
 *   <arg-type> can be ArrayString, ArrayInt or ArrayDate, in which case
 *     <arg> is a list: <n-elements>:<element-length>:<element-data>...
 *   <arg-len> is always the total length of the argument (single data value or array)
 *     so that the argument data can be easily read because it's length is known in advance
 * <n-args> == -1 - there was an error, in which case the error description follows
 *    When there is an error, no parameter data is put into the packet
 * 
 * @author smf
 */
public class MarshallToPacket
{

    private String method_name;
    private List<String> args;
    private String errmsg;
    
    public MarshallToPacket (String method_name)
    {
        errmsg = null;
        args = new ArrayList<> ();
        this.method_name = method_name;
    }
    
    public void setErrmsg (String errmsg)
    {
        this.errmsg = errmsg;
    }
    
    public String getMethodName () { return method_name; }
    public boolean isError () { return errmsg != null; }
    public String getErrorMessage () { return errmsg; }
    
    public void addString (String arg)
    {
        args.add ("String:" + arg.length() + ":" + arg);
    }
    
    public void addArrayString (String string_array [])
    {
        StringBuffer array_args = new StringBuffer (Integer.toString (string_array.length));
        array_args.append (":");
        for (String string : string_array)
            array_args.append (string.length() + ":" + string);
        args.add ("ArrayString:" + Integer.toString (array_args.length()) + ":" + array_args.toString());
    }
    
    public void addInt (int arg)
    {
        String string = Integer.toString (arg);
        args.add ("int:" + string.length() + ":" + string);
    }
    
    public void addArrayInt (int int_array [])
    {
        StringBuffer array_args = new StringBuffer (Integer.toString (int_array.length));
        array_args.append (":");
        for (int int_val : int_array)
        {
            String string = Integer.toString (int_val);
            array_args.append (string.length() + ":" + string);
        }
        args.add ("ArrayInt:" + Integer.toString (array_args.length()) + ":" + array_args.toString());
    }
    
    public void addDate (Date arg)
    {
        String string = Long.toString (arg.getTime());
        args.add ("Date:" + string.length() + ":" + string);
    }
    
    public void addArrayDate (Date date_array [])
    {
        StringBuffer array_args = new StringBuffer (Integer.toString (date_array.length));
        array_args.append (":");
        for (Date date : date_array)
        {
            String string = Long.toString (date.getTime());
            array_args.append (string.length() + ":" + string);
        }
        args.add ("ArrayDate:" + Integer.toString (array_args.length()) + ":" + array_args.toString());
    }
    
    /** construct string to go in data packet. 
     * @return the coded string */
    public String getPacketData ()
    {
        // create a string that contains the entire message
        StringBuffer buffer = new StringBuffer ();
        buffer.append (method_name.length());
        buffer.append (":");
        buffer.append (method_name);
        if (errmsg != null)
        {
            buffer.append ("-1:");
            buffer.append(errmsg.length());
            buffer.append (":");
            buffer.append (errmsg);
        }
        else
        {
            buffer.append (args.size());
            buffer.append (":");
            for (String arg : args)
                buffer.append (arg);
        }
        
        String total_length_string = Integer.toString(buffer.length());
        return total_length_string + ":" + buffer.toString();
    }
    
}
