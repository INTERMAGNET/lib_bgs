/*
 * CheckVersion.java
 *
 * Created on 30 November 2006, 16:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Misc;

import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 * Check the version number of the running JRE
 *
 * @author smf
 */
public class CheckVersion 
{

    private String jre_version;
    private String test_version;
    private boolean parse_fault;
    private boolean jre_version_too_low;
    
    /** check for the default Java version - update the string in this
     * method when the Java Development area upgrades to a new JDK */
    public static CheckVersion CheckDefaultVersion ()
    {
        return new CheckVersion ("1.6");
    }
    
    /** Creates a new instance of CheckVersion
     * @param test_version the version string to test against, formatted as n.m_o (e.g. 1.5.0_09) */
    public CheckVersion(String test_version) 
    {
        int jre_number, test_number;
        String jre_element, test_element;
        StringTokenizer test_tokens, jre_tokens;

        // start assuming that everything is OK
        this.test_version = test_version;
        parse_fault = false;
        jre_version_too_low = false;

        try
        {
            // the entire process can be sidestepped by setting a system property
            if (System.getProperty("AVOID_JRE_VERSION_CHECK") == null)
            {
                // gather version information
                jre_version = System.getProperty ("java.version");
                jre_tokens = new StringTokenizer (jre_version, "._");
                test_tokens = new StringTokenizer (test_version, "._");
            
                while (test_tokens.hasMoreTokens())
                {
                    jre_element = jre_tokens.nextToken();
                    test_element = test_tokens.nextToken();
                
                    jre_number = Integer.parseInt(jre_element);
                    test_number = Integer.parseInt(test_element);
                
                    if (jre_number < test_number)
                    {
                        jre_version_too_low = true;
                        break;
                    }
                }
            }
        }
        catch (Exception e) { parse_fault = true; }
    }
    
    /** was there a fault parsing the version number strings */
    public boolean isParseFault () { return parse_fault; }
    
    /** is the JRE version number too low */
    public boolean isJREVersionTooLow () { return jre_version_too_low; }
    
    /** tell the user (using a Swing dialog) if the JRE version number is too low
     * (if the version number is OK, don't take any action at all)
     * @param exit_flag - if true exit after the message has been delivered */
    public void msgSwing (boolean exit_flag)
    {
        String msg;

        msg = getErrorMessage (exit_flag);
        if (msg != null)
        {
            JOptionPane.showMessageDialog(null, msg,  "Error", JOptionPane.ERROR_MESSAGE);
            if (exit_flag) System.exit(1);
        }
    }
    
    /** tell the user (using stdout or stderr) if the JRE version number is too low
     * (if the version number is OK, don't take any action at all)
     * @param use_stderr true for stderr, false for stdout
     * @param exit_flag - if true exit after the message has been delivered */
    public void msgStd (boolean use_stderr, boolean exit_flag)
    {
        String msg;

        msg = getErrorMessage (exit_flag);
        if (msg != null)
        {
            if (use_stderr) System.err.println (msg);
            else System.out.println (msg);
            if (exit_flag) System.exit(1);
        }
    }
    
    /** get an error message for the version check 
     * @param exit_flag - if true exit after the message has been delivered 
     * @return the error message or null if the version numbers checked out OK */
    public String getErrorMessage (boolean exit_flag)
    {
        String msg;
        
        if (parse_fault)
        {
            msg = "There was a fault trying to determine the Java run time version.";
            if (exit_flag) msg += "\nThis program will exit.";
        }
        else if (jre_version_too_low)
        {
            msg = "The version of the Java Run Time (JRE) on this computer is too low.";
            msg += "\nThe minimum requirement is JRE version " + test_version;
            msg += "\nThis machine is using JRE version " + jre_version;
            if (exit_flag) msg += "\nThis program will exit.";
        }
        else msg = null;
        return msg;
    }
}
