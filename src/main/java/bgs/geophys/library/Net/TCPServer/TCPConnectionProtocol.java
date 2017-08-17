/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Net.TCPServer;

import bgs.geohpys.library.LogConfig.LogConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.Logger;

/**
 * A connection that can be used for either a single instance of a server socket
 * connection or a client connection
 * @author smf
 */
public class TCPConnectionProtocol 
{
    
    private static final Logger LOGGER = LogConfig.getLogger(TCPConnectionProtocol.class);
    
    protected Socket connection_socket;
    protected BufferedInputStream in_from_remote;
    protected BufferedOutputStream out_to_remote;
    private IOException stored_io_exception;

    public TCPConnectionProtocol ()
    {
        stored_io_exception = null;
    }
    
    /** server socket constructor
     * @param connection_socket the socket returned from accept() */
    public TCPConnectionProtocol (Socket connection_socket)
    {
        try
        {
            this.connection_socket = connection_socket;
            in_from_remote = new BufferedInputStream(connection_socket.getInputStream());
            out_to_remote = new BufferedOutputStream(connection_socket.getOutputStream());
            stored_io_exception = null;
        }
        catch (IOException e) { stored_io_exception = e; }
    }

    /** error constructor
     * @param e the error thrown from accept() */
    public TCPConnectionProtocol (IOException e)
    {
        stored_io_exception = e;
    }

    public void setTimeout (int timeout)
    throws IOException
    {
        connection_socket.setSoTimeout(timeout);
    }
    
    /** close this connection */
    public void close ()
    {
        try { connection_socket.close(); }
        catch (IOException e) { }
    }

    /** read this number of bytes */
    public String read (int n_bytes)
    throws IOException
    {
        if (stored_io_exception != null) throw stored_io_exception;
        byte buffer [] = new byte [n_bytes];
        for (int rx_total = 0; rx_total < buffer.length; )
        {
            int bytes_read = in_from_remote.read (buffer, rx_total, buffer.length - rx_total);
            if (bytes_read < 0) throw new IOException ("End of stream detected");
            rx_total += bytes_read;
        }
        return new String (buffer);
    }

    /** read up to this delimiter character */
    public String read (byte delim_char, boolean include_delim)
    throws IOException
    {
        if (stored_io_exception != null) throw stored_io_exception;
        int alloc_increment = 20;
        byte buffer [] = new byte [alloc_increment];
        int pos = 0;
        int single_byte = 0;
        do
        {
            single_byte = in_from_remote.read();
            if (single_byte < 0) throw new IOException ("End of stream detected");
            if ((single_byte != delim_char) || include_delim)
            {
                if (pos >= buffer.length)
                {
                    byte new_buffer [] = new byte [buffer.length + alloc_increment];
                    System.arraycopy(buffer, 0, new_buffer, 0, buffer.length);
                    buffer = new_buffer;
                }
                buffer [pos ++] = (byte) single_byte;
            }
        } while ((byte) single_byte != delim_char);
        return new String (buffer, 0, pos);
    }

    /** write to the other side of the connection */
    public void write (String data)
    throws IOException
    {
        if (stored_io_exception != null) throw stored_io_exception;
        out_to_remote.write(data.getBytes());
        out_to_remote.flush();
    }
}
