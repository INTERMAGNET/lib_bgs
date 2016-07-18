/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.ssh;

import bgs.geophys.library.Threads.RunClass;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import javax.security.auth.login.LoginException;

/**
 *
 * @author smf
 */
public class Telnet 
{

    /** telnet to a remote host, possibly using an ssh tunnel 
     * @param hostname the machine to telnet to
     * @param ssh the ssh to tunnel through or blank/null for no tunnel -
     *        the string is in the form user@host:port,password
     *        only the host part is compulsory 
     * @return the running class containing the telnet session */
    public static RunClass telnet (String hostname, String ssh)
    throws LoginException, JSchException
    {
        sshSwingUserInfo ssh_ui;
        sshTunnel ssh_tunnel;
        int ssh_port;
        String ssh_user, ssh_host, ssh_password;
        sshPortForward port_forward;
        RunClass run_class;
        
        // separate out the parts of the ssh string
        ssh_port = -1;
        ssh_user = ssh_host = ssh_password = null;
        ssh_ui = null;
        if (ssh.length() <= 0) ssh = null;
        if (ssh != null)
        {
            ssh_user = sshTunnel.findUsernameInHostname(ssh);
            ssh_port = sshTunnel.findPortInHostname(ssh);
            ssh_host = sshTunnel.findHostInHostname(ssh);
            ssh_password = sshTunnel.findPasswordInHostname(ssh);
            if (ssh_host.length() <= 0) ssh_host = null;
        }
            
        // retrieve any missing ssh components
        if (ssh_host != null)
        {
            if (ssh_user == null)
            {
                ssh_ui = new sshSwingUserInfo ("Connection to ssh server " + ssh_host,
                                               "Username:", "Password:");
                if (ssh_ui.getUsername() == null)
                    throw new LoginException ("SSH connection aborted");
            }
            else if (ssh_password == null)
            {
                ssh_ui = new sshSwingUserInfo (ssh_user);
            }
            else
            {
                ssh_ui = new sshSwingUserInfo (ssh_user, ssh_password);
                ssh_ui.setShowMessages(false);
            }
        }
        
        // create the ssh connection
        if (ssh_host != null)
            ssh_tunnel = new sshTunnel (ssh_ui, ssh_host, ssh_port);
        else
            ssh_tunnel = null;

        // open the telnet session
        try
        {
            if (ssh_tunnel != null)
            {
                port_forward = ssh_tunnel.newPortForward(hostname, 23);
                run_class = new RunClass ("de.mud.jta.Main", "-term vt100 localhost " + Integer.toString (port_forward.getLocalPort()),
                                           true, RunClass.EXTERNAL, true, ssh_tunnel);
            }
            else
            {
                run_class = new RunClass ("de.mud.jta.Main", "-term vt100 " + hostname,
                                          true, RunClass.EXTERNAL, true);
            }
        }
        catch (ClassNotFoundException e) { throw new LoginException ("Internal error: can't find JTA classes"); }
        catch (NoSuchMethodException e) { throw new LoginException ("Internal error: can't find JTA classes"); }
        catch (IOException e) { throw new LoginException ("Internal error: can't load JTA classes"); }

        return run_class;
    }
    
}
