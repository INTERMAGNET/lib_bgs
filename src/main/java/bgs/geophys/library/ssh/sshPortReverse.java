/*
 * sshPortForward.java
 *
 * Created on 29 April 2004, 16:23
 */

package bgs.geophys.library.ssh;

import com.jcraft.jsch.*;

import java.io.*;

/**
 * Class that holds a single instance of reverse port forwarding
 * over an ssh connection
 *
 * @author  smf
 */
public class sshPortReverse 
{
    
    private int fwd_lport;
    private Session session;
    
    /** Create a forwarded port/stream through an existing ssh connection
     * similar to -R option to ssh, but this method chooses the local port
     * @param session the exisitng ssh connection, which must be open
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortReverse(Session session, String fwd_rhost, int fwd_rport) throws JSchException
    {
        boolean connected;
        
        fwd_lport = 1024;
        connected = false;
        while (! connected)
        {
            try
            {
                session.setPortForwardingR (fwd_lport,  fwd_rhost, fwd_rport);
                connected = true;
            }
            catch (JSchException e)
            {
                fwd_lport ++;
                if (fwd_lport > 32767) throw (e);
            }
        }
        this.session = session;
    }

    /** Create a forwarded port/stream through an existing ssh connection
     * similar to -R option to ssh
     * @param session the exisitng ssh connection, which must be open
     * @param fwd_lport the local port that the user connects to
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortReverse(Session session, int fwd_lport, String fwd_rhost, int fwd_rport) throws JSchException
    {
        session.setPortForwardingR (fwd_lport,  fwd_rhost, fwd_rport);
        this.fwd_lport = fwd_lport;
        this.session = session;
    }
    
    /** disconnect this channel */
    public void close ()
    {
        try
        {
            session.delPortForwardingR (fwd_lport);
        }
        catch (Exception e) { }
    }
    
    
    /** get the port used to communicate with the remote host */
    public int getLocalPort () { return fwd_lport; }

}
