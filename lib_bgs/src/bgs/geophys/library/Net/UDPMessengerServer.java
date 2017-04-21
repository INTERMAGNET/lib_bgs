/* 
 * UDPMessengerServer.java 
 * 
 * Created on 14 November 2005, 15:34 
 */

package bgs.geophys.library.Net; 

import java.io.*; 
import java.net.*; 
import java.util.*; 

/** 
 * Use UDP to implement a simple, reliable message passing system. See also
 * UDPMessangerClient
 * 
 * To use the system as a server 
 *      Construct a UDPMessager() with a local ip_port 
 *      Call receive() to receive messages 
 *      Call reply to send a reply 
 *
 * Every send() should have a corresponding reply()
 * 
 * @author  smf 
 */ 

public class UDPMessengerServer extends UDPMessenger implements Runnable 
{ 
    
    // thread control
    private Thread thread; 
    private boolean abort_thread; 

    // maximum size of received messages
    private int max_msg_size;
    
    // a list of messages waiting to be processed 
    private Vector<UDPMessengerPacket> received_packet_list; 
    
    /** Creates an instance of UDPMessengerServer 
     * @param ip_port the local port for clients to send messages to 
     * @param max_msg_size the maximum size of received messages
     * @throws SocketException if there was a problem opening the port */ 
    public UDPMessengerServer(int ip_port, int max_msg_size) throws SocketException 
    { 
        received_packet_list = new Vector<UDPMessengerPacket> (); 
        this.max_msg_size = max_msg_size;
        
        socket = new DatagramSocket (ip_port); 
        
        abort_thread = false; 
        thread = new Thread (this); 
        thread.setName ("UDPServerMessengerThread"); 
        thread.start (); 
    } 
    
    /** abort any running thread */ 
    public void finalize () 
    { 
        abort_thread = true; 
        if (thread != null) 
        { 
            // calling socket.close causes a socket.receive call to unblock
            socket.close();
            while (thread.isAlive()) 
            {
                try {thread.join (250); } 
                catch (InterruptedException e) { } 
            }
            thread = null;
        }
    } 
    
    /** remove recevied packets from the queue 
     * @return the next packet from the queue or null if the queue is empty */ 
    public UDPMessengerPacket receive () 
    { 
        UDPMessengerPacket msg_packet; 
        
        synchronized (received_packet_list) 
        { 
            try { msg_packet = received_packet_list.remove (0); } 
            catch (ArrayIndexOutOfBoundsException e) { msg_packet = null; } 
        } 
        return msg_packet; 
    }
    
    /** remove received packets from the queue, blocking until a message is received
     * @param timeout number of milliseconds to wait 
     * @return the next packet from the queue or null if the queue is empty */ 
    public UDPMessengerPacket receive (long timeout) 
    { 
        UDPMessengerPacket msg_packet; 
        long start_time;
        
        start_time = new Date().getTime();
        msg_packet = null;
        while (new Date().getTime() - start_time < timeout)
        {
            msg_packet = receive ();
            if (msg_packet != null) break;
            try { Thread.sleep (100); }
            catch (InterruptedException e) { }
        }
        return msg_packet; 
    }
    
    /** send a response to a received UDP packet 
     * @param server_packet the packet to reposnd to 
     * @param response the response to send 
     * @throws IOException if there was an error replying */ 
    public void reply (UDPMessengerPacket msg_packet, String response)
    throws IOException 
    { 
        send_packet (msg_packet.packet_id, response.getBytes(),
                     msg_packet.source_ip_address, msg_packet.source_ip_port);
    } 
    
    /** send a response to a received UDP packet 
     * @param server_packet the packet to reposnd to 
     * @param response the response to send 
     * @throws IOException if there was an error replying */ 
    public void reply (UDPMessengerPacket msg_packet, byte response [])
    throws IOException 
    { 
        send_packet (msg_packet.packet_id, response,
                     msg_packet.source_ip_address, msg_packet.source_ip_port);
    } 
    
    /** this may be a public method, but don't call it! */
    public void run() 
    { 
        int count, length;
        byte data[]; 
        DatagramPacket packet;
        UDPMessengerPacket msg_packet;
    
        // until aborted ... 
        while (! abort_thread) 
        { 
            // get the next message 
            data = new byte [max_msg_size];
            packet = new DatagramPacket (data, max_msg_size); 
            try 
            { 
                // receive a datagram - blocks until a datagram is available
                socket.receive (packet); 
                
                // packet must contain at least an ID
                length = packet.getLength() - 4;
                if (length >= 0)
                {
                    // decode the data
                    msg_packet = new UDPMessengerPacket ();
                    msg_packet.packet_id = bgs.geophys.library.Misc.Utils.bytesToInt(data);
                    msg_packet.packet_contents = new byte [length];
                    for (count=0; count<length; count++) msg_packet.packet_contents[count] = data [count +4];
                    msg_packet.packet_time = new Date ();
                    msg_packet.resend_count = 0;
                    msg_packet.source_ip_address = packet.getAddress();
                    msg_packet.source_ip_port = packet.getPort();
                    
                    // put it in the queue 
                    synchronized (received_packet_list) 
                    { 
                        received_packet_list.add (msg_packet); 
                    } 
                }
            } 
            catch (IOException e) 
            { 
                // prevent continuous IO exceptions from hogging the CPU 
                try { Thread.sleep(50); } 
                catch (InterruptedException e2) { } 
            } 
        } 
    }    

    public int getIpPort ()
    {
        return socket.getLocalPort();
    }
    
} 
 
