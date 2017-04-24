/*
 * sshTunnel.java
 *
 * Created on 29 April 2004, 07:58
 */

package bgs.geophys.library.ssh;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.*;

/**
 * Class that uses jsch to make ssh tunnels (port forwarding)
 *
 * @author  Simon
 */
public class sshTunnel 
{
    
    private JSch jsch;
    private Session session;
    private String ssh_host;
    private Channel x_channel;
    
    public final static int SSH_DEFAULT_PORT = 22;

    /** Creates a new instance of sshTunnel by creating a connection
     * to the ssh server
     * @params ssh_user the name of the user for authentication 
     * @params ssh_host the ssh server to connect to
     * @params ssh_port the ssh port to connect to 
     * @throws JSchException if there was an ssh connection/authentication error */
    public sshTunnel (String ssh_user, String ssh_host, int ssh_port)
    throws JSchException
    {
        commonConstruction(new sshSwingUserInfo(ssh_user), ssh_host, ssh_port, 0);
    }
    
    /** Creates a new instance of sshTunnel by creating a connection
     * to the ssh server
     * @params ssh_user the name of the user for authentication 
     * @params ssh_host the ssh server to connect to
     * @params ssh_port the ssh port to connect to 
     * @params timeout a socket level timeout (in mS)
     * @throws JSchException if there was an ssh connection/authentication error */
    public sshTunnel (String ssh_user, String ssh_host, int ssh_port, int timeout)
    throws JSchException
    {
        commonConstruction(new sshSwingUserInfo(ssh_user), ssh_host, ssh_port, timeout);
    }

    /** Creates a new instance of sshTunnel by creating a connection
     * to the ssh server
     * @params ui user information for the ssh conection - this must contain a username
     * @params ssh_port the ssh port to connect to 
     * @throws JSchException if there was an ssh connection/authentication error */
    public sshTunnel (sshSwingUserInfo ui, String ssh_host, int ssh_port)
    throws JSchException
    {
        commonConstruction(ui, ssh_host, ssh_port, 0);
    }
    
    /** Creates a new instance of sshTunnel by creating a connection
     * to the ssh server
     * @params ui user information for the ssh conection - this must contain a username
     * @params ssh_port the ssh port to connect to 
     * @params timeout a socket level timeout (in mS)
     * @throws JSchException if there was an ssh connection/authentication error */
    public sshTunnel (sshSwingUserInfo ui, String ssh_host, int ssh_port, int timeout)
    throws JSchException
    {
        commonConstruction(ui, ssh_host, ssh_port, timeout);
    }
    
    /** common code to construct and ssh tunnel */
    private void commonConstruction (sshSwingUserInfo ui, String ssh_host, int ssh_port, int timeout)
    throws JSchException
    {
        jsch = new JSch();
        jsch.setKnownHosts (System.getProperty("user.home") + System.getProperty("file.separator") + ".JschKnownHosts");
        
        session = jsch.getSession (ui.getUsername(), ssh_host, ssh_port);
        if (timeout > 0) session.setTimeout(timeout);
        session.setUserInfo (ui);
        session.connect ();
        
        this.ssh_host = ssh_host;
        x_channel = null;
    }

    /** disconnect this tunnel */
    public void disconnect ()
    {
        if (x_channel != null) x_channel.disconnect();
        x_channel = null;
        
        session.disconnect ();
    }
    
    /** set a timeout for this tunnel */
    public void setTimeout (int timeout) 
    throws JSchException
    {
        session.setTimeout(timeout);
    }
    
    /** add a new port forwarded to a remote host through the ssh server
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @return details of the port
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortForward newPortForward (String fwd_rhost, int fwd_rport)
    throws JSchException
    {
        return new sshPortForward (session, fwd_rhost, fwd_rport);
    }

    /** add a new port forwarded to a remote host through the ssh server
     * @param fwd_lport the local port that the client will use for the connection
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @return details of the port
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortForward newPortForward (int fwd_lport, String fwd_rhost, int fwd_rport)
    throws JSchException
    {
        return new sshPortForward (session, fwd_lport, fwd_rhost, fwd_rport);
    }

    /** add a new port forwarded to a remote host through the ssh server
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @return details of the port
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortReverse newPortReverse (String fwd_rhost, int fwd_rport)
    throws JSchException
    {
        return new sshPortReverse (session, fwd_rhost, fwd_rport);
    }

    /** add a new port forwarded to a remote host through the ssh server
     * @param fwd_lport the local port that the client will use for the connection
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @return details of the port
     * @throws JSchException if there was an ssh connection/authentication error
     */
    public sshPortReverse newPortReverse (int fwd_lport, String fwd_rhost, int fwd_rport)
    throws JSchException
    {
        return new sshPortReverse (session, fwd_lport, fwd_rhost, fwd_rport);
    }

    /** add a new stream forwarded to a remote host through the ssh server
     * @param fwd_rhost the name of the host to forward the connection to
     * @param fwd_rport the port on the host to forward the connection to
     * @return details of the stream
     * @throws JSchException if there was an ssh connection/authentication error
     * @throws IOException if there was an IO error setting up pipes
     */
    public sshStreamForward newStreamForward (String fwd_rhost, int fwd_rport)
    throws JSchException, IOException
    {
        return new sshStreamForward (session, fwd_rhost, fwd_rport);
    }

    /** set up X forwarding 
     * @param xhost the address of the X client
     * @param xport the port on the X client */
    public void setXForwarding (String xhost, int xport)
    throws JSchException
    {
        session.setX11Host(xhost);
        session.setX11Port(xport);
    }
    
    /** start up X forwarding
     * @param application the application that will start an X client on the host */
    public void startXForwarding (String application)
    throws JSchException
    {
      ByteArrayInputStream input;
      ByteArrayOutputStream output;
      
      stopXForwarding();
      
      x_channel = session.openChannel("shell");
      x_channel.setXForwarding(true);
      input = new ByteArrayInputStream ((application+"\r").getBytes());
      x_channel.setInputStream(input);
      output = new ByteArrayOutputStream ();
      x_channel.setOutputStream(output);
      x_channel.connect();
    }
    
    /** stop x forwarding */
    public void stopXForwarding ()
    {
        if (x_channel != null) x_channel.disconnect();
        x_channel = null;
    }
    
    /** Given a string with the syntax [user@]host[:port][,password]
     * check if the string is valid 
     * @return true if the string is valid */
    public static boolean isHostnameValid (String host_string)
    {
        if (host_string == null) return false;
        if (host_string.length () <= 0) return false;
        return true;
    }    

    /** Given a string with the syntax [user@]host[:port][,password]
     * find the user portion of the string
     * @param host_string the string to parse
     * @return the username portion of the string - may be null */
    public static String findUsernameInHostname (String host_string)
    {
        StringTokenizer tokens;
        
        if (! isHostnameValid (host_string)) return null;
        tokens = new StringTokenizer (host_string, "@");
        if (tokens.countTokens() <= 1) return null;
        return tokens.nextToken();
    }
    
    /** Given a string with the syntax [user@]host[:port]
     * find the host name portion of the string
     * @param host_string the string to parse
     * @return the username portion of the string - may be null */
    public static String findHostInHostname (String host_string)
    {
        int count;
        String string;
        StringTokenizer tokens;
        
        if (! isHostnameValid (host_string)) return null;
        tokens = new StringTokenizer (host_string, "@");
        if (tokens.countTokens() <= 1) string = host_string;
        else
        {
            tokens.nextToken();
            string = tokens.nextToken();
        }
        tokens = new StringTokenizer (string, ":");
        if (tokens.countTokens() < 1) return null;
        string = tokens.nextToken();
        tokens = new StringTokenizer (string, ",");
        if (tokens.countTokens() < 1) return null;
        return tokens.nextToken();
    }
    
    /** Given a string with the syntax [user@]host[:port][,password]
     * find the port number in string
     * @param host_string the string to parse
     * @return the port number */
    public static int findPortInHostname (String host_string)
    {
        StringTokenizer tokens;
        
        try
        {
            tokens = new StringTokenizer (host_string, ":");
            tokens.nextToken();
            tokens = new StringTokenizer (tokens.nextToken(), ",");
            return Integer.parseInt(tokens.nextToken());
        }
        catch (Exception e) { }
        return SSH_DEFAULT_PORT;
    }
    
    /** Given a string with the syntax [user@]host[:port][,password]
     * find the password in the string
     * @param host_string the string to parse
     * @return the password - may be null */
    public static String findPasswordInHostname (String host_string)
    {
        StringTokenizer tokens;
        
        try
        {
            tokens = new StringTokenizer (host_string, ",");
            tokens.nextToken();
            return tokens.nextToken();
        }
        catch (Exception e) { }
        return null;
    }
    
    /** find the name of the remote ssh server */
    public String getSshHostName () { return ssh_host; }
}
