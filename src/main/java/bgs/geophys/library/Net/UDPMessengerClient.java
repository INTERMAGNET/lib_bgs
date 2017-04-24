/* 
 * UDPMessengerClient.java 
 * 
 * Created on 14 November 2005, 15:34 
 */

package bgs.geophys.library.Net; 

import java.io.*; 
import java.net.*; 
import java.util.*; 

/** 
 * Use UDP to implement a simple, reliable message passing system. See also
 * UDPMessengerServer
 * 
 * To use the system as a client: 
 *      Construct a UDPMessager() with a remote ip_address/ip_port (set to the servers IP address)
 *      Call constructPacket() to create a packet
 *      Call send() to send the packet 
 *      Call receive() to receive the reply 
 * 
 * Every send() should receive a corresponding reply()
 * 
 * @author  smf 
 */ 

public class UDPMessengerClient extends UDPMessenger implements Runnable
{ 
    
    // thread control
    private Thread receive_thread, resend_thread; 
    private boolean abort_threads; 
    
    // members that control client messages
    private InetAddress server_ip_address; 
    private int server_ip_port; 
    private long reply_timeout; 
    private int retry_limit; 
    private int next_packet_id; 

    // maximum size of received messages
    int max_msg_size;
    
    // a list of transmissions awaiting reply - transmitted packets are put
    // in here at the time they are transmitted - they are removed (by
    // the worker thread) either when a reply is received or when there is
    // a timeout waiting for a reply
    private Vector<UDPMessengerPacket> transmitted_packet_list; 
    
    // a list of incoming (reply) messages waiting to be processed - only
    // valid replies (those messages with a corresponding packet_id in the
    // transmit list) will be placed in here
    private Vector<UDPMessengerPacket> received_packet_list; 
    
    /** Creates an instance of UDPMessengerClient
     * @param server_ip_host the remote server name or address to send messages to 
     * @param server_ip_port the remote server port to send messages to 
     * @param max_msg_size the maximum size of received messages
     * @throws SocketException if there was a problem opening the port
     * @throws UnknownHostException if there was a problem looking up the host name */ 
    public UDPMessengerClient(String server_ip_host, int server_ip_port, int max_msg_size) 
    throws SocketException, UnknownHostException
    { 
        this.server_ip_address = InetAddress.getByName(server_ip_host); 
        this.server_ip_port = server_ip_port; 
        this.reply_timeout = 10000l; 
        this.retry_limit = 10; 
        next_packet_id = 0; 
        transmitted_packet_list = new Vector<UDPMessengerPacket> (); 
        received_packet_list = new Vector<UDPMessengerPacket> (); 
        this.max_msg_size = max_msg_size;
        
        socket = new DatagramSocket (); 
        
        abort_threads = false; 
        receive_thread = new Thread (this); 
        resend_thread = new Thread (this); 
        receive_thread.setName ("UDPClientMessengerReceiveThread"); 
        resend_thread.setName ("UDPClientMessengerResendThread"); 
        receive_thread.start (); 
        resend_thread.start ();
    } 
    
    /** Creates a client instance of UDPMessenger with user specified reply criteria 
     * @param server_ip_host the remote server name or address to send messages to 
     * @param server_ip_port the remote server port to send messages to 
     * @param reply_timeout number of milliseconds to wait for a reply before re-sending 
     * @param retry_limit number of times to retry a send before signalling a failure 
     * @param max_msg_size the maximum size of received messages
     * @throws SocketException if there was a problem opening the port
     * @throws UnknownHostException if there was a problem looking up the host name */ 
    public UDPMessengerClient(String server_ip_host, int server_ip_port, long reply_timeout, int retry_limit, int max_msg_size)
    throws SocketException, UnknownHostException
    { 
        this.server_ip_address = InetAddress.getByName(server_ip_host); 
        this.server_ip_port = server_ip_port; 
        this.reply_timeout = reply_timeout; 
        this.retry_limit = retry_limit; 
        next_packet_id = 0; 
        transmitted_packet_list = new Vector<UDPMessengerPacket> (); 
        received_packet_list = new Vector<UDPMessengerPacket> (); 
        this.max_msg_size = max_msg_size;
        
        socket = new DatagramSocket (); 
        
        abort_threads = false; 
        receive_thread = new Thread (this); 
        resend_thread = new Thread (this); 
        receive_thread.setName ("UDPClientMessengerReceiveThread"); 
        resend_thread.setName ("UDPClientMessengerResendThread"); 
        receive_thread.start (); 
        resend_thread.start ();
    } 

    /** get the server IP address */
    public InetAddress getServerIPAddress () { return server_ip_address; }
    
    /** abort any running thread */ 
    public void finalize () 
    { 
        abort_threads = true; 
        if (receive_thread != null) 
        { 
            // calling socket.close causes a socket.receive call to unblock
            socket.close();
            while (receive_thread.isAlive()) 
            { 
                try {receive_thread.join (150); } 
                catch (InterruptedException e) { } 
            } 
        } 
        if (resend_thread != null) 
        { 
            while (resend_thread.isAlive()) 
            { 
                // the resend thread is probably asleep, so interrupt it
                resend_thread.interrupt (); 
                try {resend_thread.join (150); } 
                catch (InterruptedException e) { } 
            } 
        } 
    } 
    
    /** construct a packet
     * @param message the data to send */
    public UDPMessengerPacket constructPacket (String message)
    {
        return constructPacket (next_packet_id ++, message, socket.getLocalAddress(), socket.getLocalPort());
    }
    
    /** construct a packet
     * @param data the data to send */
    public UDPMessengerPacket constructPacket (byte data [])
    {
        return constructPacket (next_packet_id ++, data, socket.getLocalAddress(), socket.getLocalPort());
    }
    
    /** send a packet 
     * @param msg_packet the packet to send
     * @throws IOException if there was an error sending the data */ 
    public void send (UDPMessengerPacket msg_packet) 
    throws IOException 
    { 
        msg_packet.packet_time = new Date ();
        send_packet (msg_packet.packet_id, msg_packet.packet_contents, server_ip_address, server_ip_port);
        transmitted_packet_list.add (msg_packet);
    } 
    
    /** remove received packets from the queue 
     * @param msg_packet the packet that you want a reply to
     * @return the reply to the given packet or null if no packet matches
     * @throws IOException if no reply has (or ever will be) received for the given packet */ 
    public UDPMessengerPacket receiveReply (UDPMessengerPacket msg_packet)
    throws IOException
    { 
        int count;
        UDPMessengerPacket msg_packet2, reply_packet;

        // first check if the sent packet is still in the transmitted list
        // if it is, then no reply has yet been received
        synchronized (transmitted_packet_list)
        {
            for (count=0; count<transmitted_packet_list.size(); count++)
            {
                msg_packet2 = transmitted_packet_list.get (count);
                if (msg_packet2.packet_id == msg_packet.packet_id) return null;
            }
        }
        
        // the sent packet is not in the transmitted list, so either
        // a reply has been received, or no reply has been received
        // and the packet has been discarded (host unreachable)
        synchronized (received_packet_list)
        {
            for (count=0; count<received_packet_list.size(); count++)
            {
                reply_packet = received_packet_list.get (count);
                if (reply_packet.packet_id == msg_packet.packet_id) return reply_packet;
            }
        }
   
        // a reply has not been recevied, so an error has occurred
        throw new IOException ("No reply received");
    } 
    
    /** remove received packets from the queue, blocking until a message is received 
     * @param msg_packet the packet that you want a reply to
     * @param timeout number of milliseconds to wait 
     * @return the reply to the given packet or null if no packet matches
     * @throws IOException if no reply has (or ever will be) received for the given packet */ 
    public UDPMessengerPacket receiveReply (UDPMessengerPacket msg_packet, long timeout) 
    throws IOException
    { 
        UDPMessengerPacket reply_packet; 
        long start_time;
        
        start_time = new Date().getTime();
        reply_packet = null;
        while (new Date().getTime() - start_time < timeout)
        {
            reply_packet = receiveReply (msg_packet);
            if (reply_packet != null) break;
            try { Thread.sleep (100); }
            catch (InterruptedException e) { }
        }
        return reply_packet; 
    }

    /** transmit a message and wait (block) for a reply
     * @param message the message to transmit
     * @return the reply
     * @throws IOException if no reply was received */
    public UDPMessengerPacket transmit (String message)
    throws IOException
    {
        UDPMessengerPacket tx_packet, rx_packet;
        
        tx_packet = constructPacket (message);
        send (tx_packet);
        do
        {
            rx_packet = receiveReply (tx_packet);
        } while (rx_packet == null);
        return rx_packet;
    }

    /** this may be a public method, but don't call it! */
    public void run() 
    { 
        if (Thread.currentThread().equals (receive_thread)) receiveWorker();
        if (Thread.currentThread().equals (resend_thread)) resendWorker();
    }    

    /** worker method for receive thread */
    private void receiveWorker ()
    {
        int count, length, packet_id;
        byte data[]; 
        boolean found;
        DatagramPacket packet;
        UDPMessengerPacket msg_packet, reply_packet;
    
        // until aborted ... 
        while (! abort_threads) 
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
                    // extract the packet ID
                    packet_id = bgs.geophys.library.Misc.Utils.bytesToInt(data);
                    
                    // are we expecting a packet with this ID ??
                    found = false;
                    synchronized (transmitted_packet_list)
                    {
                        for (count=0; count<transmitted_packet_list.size(); count++)
                        {
                            msg_packet = transmitted_packet_list.get(count);
                            if (msg_packet.packet_id == packet_id)
                            {
                                found = true;
                                transmitted_packet_list.remove(count --);
                            }
                        }
                    }
                    
                    // if the packet is OK, decode it and insert it in the receive queue
                    if (found)
                    {
                        reply_packet = new UDPMessengerPacket ();
                        reply_packet.packet_id = packet_id;
                        reply_packet.packet_contents = new byte [length];
                        for (count=0; count<length; count++) reply_packet.packet_contents[count] = data [count +4];
                        reply_packet.packet_time = new Date ();
                        reply_packet.resend_count = 0;
                        reply_packet.source_ip_address = packet.getAddress();
                        reply_packet.source_ip_port = packet.getPort();
                        synchronized (received_packet_list)
                        {
                            received_packet_list.add (reply_packet); 
                        }
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
    
    /** worker method for resend thread */
    private void resendWorker ()
    {
        int count;
        UDPMessengerPacket msg_packet;
        Date time_now;
        
        // until aborted ... 
        while (! abort_threads) 
        {
            // get the current time
            time_now = new Date ();
            
            // for each packet in the transmit queue
            synchronized (transmitted_packet_list)
            {
                for (count=0; count<transmitted_packet_list.size(); count++)
                {
                    msg_packet = transmitted_packet_list.get(count);
                    
                    // has the packet timed out??
                    if (time_now.getTime() - msg_packet.packet_time.getTime() > reply_timeout)
                    {
                        // yes - has it exceeded its retry count - if so discard it, otherwise resend it */
                        msg_packet.resend_count ++;
                        if (msg_packet.resend_count > retry_limit)
                            transmitted_packet_list.remove(count --);
                        else
                        {
                            try
                            {
                                send_packet (msg_packet.packet_id, msg_packet.packet_contents,
                                             server_ip_address, server_ip_port);
                            }
                            catch (IOException e) { e.printStackTrace(); }
                            msg_packet.packet_time = new Date ();
                        }
                    }
                }
            }
            // wait before going round again
            try { Thread.sleep (500); }
            catch (InterruptedException e) { }
        }
    }

} 
 
