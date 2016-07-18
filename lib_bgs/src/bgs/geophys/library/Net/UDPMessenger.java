/*
 * UDPMessenger.java
 *
 * Created on 17 November 2005, 10:18
 */

package bgs.geophys.library.Net;

import java.io.*; 
import java.net.*; 
import java.util.*; 

/**
 * Superclass for UDPMessangerServer and UDPMessangerClient
 *
 * @author  smf
 */
public abstract class UDPMessenger 
{
    
    // members that are needed by both client and server
    protected DatagramSocket socket; 
    
    /** this class is used to store packets */ 
    public class UDPMessengerPacket
    {
        public int packet_id; 
        public byte packet_contents []; 
        public Date packet_time;
        public int resend_count;
        public InetAddress source_ip_address; 
        public int source_ip_port; 
        
        public String toString ()
        {
            return "[" + Integer.toString (packet_id) + "] " +
                   packet_time.toString () + " " +
                   new String (packet_contents);
        }
    } 
    
    /** construct a message */
    protected UDPMessengerPacket constructPacket (int packet_id, String message, InetAddress ip_address, int ip_port)
    {
        int count;
        UDPMessengerPacket msg_packet;
        byte data [];
        
        msg_packet = new UDPMessengerPacket ();
        msg_packet.packet_id = packet_id;
        data = message.getBytes();
        msg_packet.packet_contents = new byte [data.length]; 
        for (count=0; count<data.length; count++) msg_packet.packet_contents[count] = data [count];
        msg_packet.packet_time = new Date ();
        msg_packet.resend_count = 0;
        msg_packet.source_ip_address = ip_address; 
        msg_packet.source_ip_port = ip_port; 
        
        return msg_packet;
    } 

    /** construct a message */
    protected UDPMessengerPacket constructPacket (int packet_id, byte data [], InetAddress ip_address, int ip_port)
    {
        int count;
        UDPMessengerPacket msg_packet;
        
        msg_packet = new UDPMessengerPacket ();
        msg_packet.packet_id = packet_id;
        msg_packet.packet_contents = new byte [data.length]; 
        for (count=0; count<data.length; count++) msg_packet.packet_contents[count] = data [count];
        msg_packet.packet_time = new Date ();
        msg_packet.resend_count = 0;
        msg_packet.source_ip_address = ip_address; 
        msg_packet.source_ip_port = ip_port; 
        
        return msg_packet;
    } 
    
    /** send packets */
    protected void send_packet (int packet_id, byte packet_contents [], InetAddress send_address, int send_port)
    throws IOException
    {
        int count;
        DatagramPacket dg_packet;
        byte packet_bytes [], id_bytes [];
        
        packet_bytes = new byte [packet_contents.length +4];
        id_bytes = bgs.geophys.library.Misc.Utils.intToBytes(packet_id);
        for (count=0; count<4; count++) packet_bytes [count] = id_bytes [count];
        for (count=0; count<packet_contents.length; count++) packet_bytes [count +4] = packet_contents [count];
        dg_packet = new DatagramPacket (packet_bytes, packet_bytes.length, send_address, send_port); 
        socket.send (dg_packet); 
    }
    
}
