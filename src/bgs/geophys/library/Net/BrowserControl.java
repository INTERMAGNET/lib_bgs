/*
 * BrowserControl.java
 *
 * Created on 11 October 2004, 16:59
 */

package bgs.geophys.library.Net;

import java.io.*;
import java.util.*;

/**
* A simple, static class to display a URL in the system browser.
*
* Under Unix, the system browser is hard-coded to be 'netscape'.
* Netscape must be in your PATH for this to work.  This has been
* tested with the following platforms: AIX, HP-UX and Solaris.
*
* Under Windows, this will bring up the default browser under windows,
* usually either Netscape or Microsoft IE.  The default browser is
* determined by the OS.  This has been tested under Windows 95/98/NT.
*
* Examples:
*   BrowserControl.displayURL("http://www.javaworld.com")
*   BrowserControl.displayURL("file://c:\\docs\\index.html")
*   BrowserContorl.displayURL("file:///user/joe/index.html");
* 
* Note - you must include the url type -- either "http://" or
* "file://".
*/

public class BrowserControl
{

    // true is we are running on Windows
    private boolean is_windows;
    
    // the name of the browser executable (UNIX only)
    private String unix_browser_path;
    
    // if true, the (UNIX) browser supports the netscape '-remote' option
    private boolean unix_supports_remote;

    
    /* create a new BrowserControl object */
    public BrowserControl ()
    {
        String name, path_env;
        StringTokenizer path_tokens;
        File file;
        Process process;
        Properties properties;
        
        is_windows = isWindowsPlatform();
        unix_browser_path = null;
        unix_supports_remote = false;
        try
        {
            if (! is_windows)
            {
                // get the path - in 1.5 this can be replaced by System.getenv ("PATH")
                properties = new Properties ();
                process = Runtime.getRuntime().exec ("env");
                properties.load (process.getInputStream());
                process.destroy();
                process.waitFor();
                path_env = properties.getProperty("PATH");
                if (path_env == null) throw new IOException ("PATH variable not found");
                
                // work through the path elements
                path_tokens = new StringTokenizer (path_env, File.pathSeparator);
                while (path_tokens.hasMoreTokens())
                {
                    name = path_tokens.nextToken();
                    file = new File (name + File.separator + "netscape");
                    if (file.exists())
                    {
                        unix_browser_path = file.getAbsolutePath();
                        unix_supports_remote = true;
                        break;
                    }
                    file = new File (name + File.separator + "mozilla");
                    if (file.exists())
                    {
                        unix_browser_path = file.getAbsolutePath();
                        unix_supports_remote = true;
                        break;
                    }
                    file = new File (name + File.separator + "firefox");
                    if (file.exists())
                    {
                        unix_browser_path = file.getAbsolutePath();
                        unix_supports_remote = true;
                        break;
                    }
                    file = new File (name + File.separator + "opera");
                    if (file.exists())
                    {
                        unix_browser_path = file.getAbsolutePath();
                        break;
                    }
                }
            }
        }
        catch (IOException e) { }
        catch (InterruptedException e) { }
    }

    /** is a browser avaiable
     * @return true for yes */
    public boolean isBrowserAvailable ()
    {
        if (is_windows) return true;
        if (unix_browser_path != null) return true;
        return false;
    }
    
    /**
     * Display a file in the system browser.  If you want to display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://" or
     * "file://").
     *
     * @throws InterruptedException if there was an error running the browser
     * @throws IOException if there was an error running the browser
     */
    public void displayURL(String url)
    throws InterruptedException, IOException
    {
        boolean need_new_browser;
        String cmd = null, string;
        BufferedReader reader;
        Process process;

        if (is_windows)
        {
            // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
            // BUT NOTE - the dll cannot handle .html or .htm - it needs one
            //            of the letters encoded in %xx notation
            cmd = "rundll32 url.dll,FileProtocolHandler " + url.replaceAll ("\\.htm", ".ht%6D");
            process = Runtime.getRuntime().exec(cmd);
        }
        else
        {
            // check that a browser was found
            if (unix_browser_path == null)
                throw new IOException ("Unable to find web browser");
            
            // Under Unix, Netscape (etc.) must be running for the "-remote"
            // command to work.  So, we try sending the command and
            // check for an exit value.  If the exit command is 0,
            // it worked, otherwise we need to start the browser.
            // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
            if (! unix_supports_remote) need_new_browser = true;
            else
            {
                need_new_browser = false;
                cmd = unix_browser_path + " -remote openURL(" + url + ")";
                process = Runtime.getRuntime().exec(cmd);
                
                // read the standard error - netscape does not set it exit code
                // correctly when the -remote command fails
                reader = new BufferedReader (new InputStreamReader (process.getErrorStream()));
                string = reader.readLine();
                if (string != null)
                {
                    if (string.length() > 0) need_new_browser = true;
                }

                // wait for exit code -- if it's 0, command worked,
                // otherwise we need to start the browser up.
                if (process.waitFor() != 0) need_new_browser = true;
            }
            
            // do we need to start a browser
            if (need_new_browser)
            {
                // cmd = 'netscape http://www.javaworld.com'
                cmd = unix_browser_path + " "  + url;
                process = Runtime.getRuntime().exec(cmd);
            }
        }
    }

    /**
     * Try to determine whether this application is running under Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform()
    {
        String os = System.getProperty("os.name");
        if ( os != null && os.startsWith("Windows"))
            return true;
        else
            return false;

    }
    
}
