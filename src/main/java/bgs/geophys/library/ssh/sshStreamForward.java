/*
 * sshStreamForward.java
 *
 * Created on 29 April 2004, 16:23
 */

package bgs.geophys.library.ssh;

import com.jcraft.jsch.*;

import java.io.*;
import bgs.geophys.library.io.*;

/**
 * Class that holds a single instance of port forwarding (using streams)
 * over and ssh connection
 *
 * @author  smf
 */
public class sshStreamForward 
{
    
    private PipedInputStream2 input_stream_read_pipe, output_stream_read_pipe;
    private PipedOutputStream2 input_stream_write_pipe, output_stream_write_pipe;
    private Channel channel;
    
    /** Create a forwarded port/stream through an existing ssh connection
     * @param session the exisitng ssh connection, which must be open
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @throws JSchException if there was an ssh connection/authentication error
     * @throws IOException if there was an IO error setting up pipes
     */
    public sshStreamForward (Session session, String fwd_rhost, int fwd_rport)
    throws JSchException, IOException
    {
        input_stream_write_pipe = new PipedOutputStream2 ();
        input_stream_read_pipe = new PipedInputStream2 (input_stream_write_pipe);
        input_stream_read_pipe.setReadWaitTime (-1);
        output_stream_write_pipe = new PipedOutputStream2 ();
        output_stream_read_pipe = new PipedInputStream2 (output_stream_write_pipe);
        output_stream_read_pipe.setReadWaitTime (-1);
            
        channel=session.openChannel ("direct-tcpip");
        ((ChannelDirectTCPIP)channel).setInputStream (input_stream_read_pipe);
        ((ChannelDirectTCPIP)channel).setOutputStream (output_stream_write_pipe);
        ((ChannelDirectTCPIP)channel).setHost (fwd_rhost);
        ((ChannelDirectTCPIP)channel).setPort (fwd_rport);
        channel.connect();
    }

    /** disconnect this channel */
    public void close ()
    {
System.out.println ("sshStreamForward: close called");
        channel.disconnect ();
    }
    
    /** get the stream used to write to the remote host
     * @return the output stream */
    public OutputStream getOutputStream () { return input_stream_write_pipe; }
    
    /** get the stream used to read from the remote host
     * @return the input stream */
    public InputStream getInputStream () { return output_stream_read_pipe; }


}
