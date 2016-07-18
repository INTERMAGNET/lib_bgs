/*
 * sshSwingUserInfo.java
 *
 * Created on 29 April 2004, 08:28
 */

package bgs.geophys.library.ssh;

import com.jcraft.jsch.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A class that implents the jsch UserInfo interface using Swing components. There
 * are three ways to use this object, depending on what information you already have
 * and what you need to get from the user:
 * 
 * 1.) If you have both a username and password:
 *          Use the constructor with two parameters
 *
 * 2.) If you have a username but no password:
 *          Use the constructor with one parameter
 *
 * 3.) If you need both a username and password:
 *          Use the contructor with three parameters
 **         Check that the username / password are not null before continuing
 *
 * @author  Simon
 */
public class sshSwingUserInfo implements UserInfo
{
    
    private String passwd;
    private String username;
    private boolean show_messages;
    
    /** Creates a new instance of sshSwingUserInfo with a username or password */
    public sshSwingUserInfo (String username, String passwd) 
    {
        this.username = username;
        this.passwd = passwd;
        this.show_messages = true;
    }

    /** Creates a new instance of sshSwingUserInfo with a username only */
    public sshSwingUserInfo (String username)
    {
        this.username = username;
        passwd = null;
        this.show_messages = true;
    }

    /** Creates a new instance of sshSwingUserInfo without a username or password */
    public sshSwingUserInfo (String dialog_title, String username_prompt, String password_prompt)
    {
        if (! privatePromptUsernameAndPassword(dialog_title, username_prompt, password_prompt))
        {
            username = null;
            passwd = null;
        }
        else
        {
            if (username.length() <= 0) username = null;
            if (passwd.length() <= 0) passwd = null;
        }
        this.show_messages = true;
    }
    
    public void setPassword (String passwd) { this.passwd = passwd; }
    
    public void setShowMessages (boolean show) { this.show_messages = show; }
    
    public String getUsername(){ return username; }
    public String getPassword(){ return passwd; }
    public String getPassphrase () { return null; }

    public boolean promptYesNo(String str)
    {
        Object[] options = { "Yes", "No" };
        int foo = JOptionPane.showOptionDialog (null, 
                                                str,
                                                "Warning", 
                                                JOptionPane.DEFAULT_OPTION, 
                                                JOptionPane.WARNING_MESSAGE,
                                                null, options, options[0]);
         return foo==0;
    }

    public boolean promptPassphrase (String message) { return true; }

    public boolean promptPassword(String message)
    {
        if (passwd != null) return true;
        return privatePromptPassword (message);
    }

    public void showMessage(String message) 
    {
        if (show_messages)
            JOptionPane.showMessageDialog(null, message); 
    }

    /////////////////////////////////////////////////////////////////////////////////
    ////////////////////// private methods below this point /////////////////////////
    /////////////////////////////////////////////////////////////////////////////////    
    private boolean privatePromptPassword(String message)
    {
        int result;
        JTextField passwordField;
        
        passwordField = (JTextField) new JPasswordField(20);
        result = JOptionPane.showConfirmDialog (null, (Object) passwordField, message,
					            JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION)
        {
            passwd=passwordField.getText();
            return true;
        }
        return false;
    }

    /** prompt for a username with a modal dialog 
     * @param message the message to display to the user */
    private boolean privatePromptUsernameAndPassword(String dialog_title, String username_prompt, String password_prompt)
    {
        int result;
        JPanel panel, column1_panel, column2_panel;
        JPasswordField usernameField, passwordField;
        
        usernameField = new JPasswordField (20);
        usernameField.setEchoChar ((char) 0);
        passwordField = new JPasswordField (20);
        column1_panel = new JPanel ();
        column1_panel.setLayout(new BorderLayout ());
        column1_panel.add ("West", new JLabel (username_prompt));
        column1_panel.add ("East", usernameField);
        column2_panel = new JPanel ();
        column2_panel.setLayout(new BorderLayout ());
        column2_panel.add ("West", new JLabel (password_prompt));
        column2_panel.add ("East", passwordField);
        panel = new JPanel ();
        panel.setLayout (new BorderLayout ());
        panel.add ("North", column1_panel);
        panel.add ("South", column2_panel);
        result = JOptionPane.showConfirmDialog (null, (Object) panel, dialog_title,
					            JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION)
        {
            username = new String (usernameField.getPassword()).trim();
            passwd = new String (passwordField.getPassword()).trim();
            return true;
        }
        return false;
    }
    
}
