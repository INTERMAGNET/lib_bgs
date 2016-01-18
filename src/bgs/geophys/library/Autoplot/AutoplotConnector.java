/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Autoplot;

import bgs.geophys.library.Threads.RunClass;
import bgs.geophys.library.Threads.ThreadMultiStreamReader;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * A class that can:
 *  1.) Extract the autoplot jar to a location under the user's home directory
 *  2.) Extract a python script to be used with autoplot to a sub-folder of the autoplot install directory
 *  3.) Configure and run autoplot to comminicate via it's server socket
 *  4.) Send commands and receive responses from autoplot - autoplot speaks jython on it's server connection
 * 
 * @author smf
 */
public class AutoplotConnector
{

    // where the autoplot jar and jython script should be installed in the class hierarchy
    private static final String AUTOPLOT_RESOURCE_LOCATION = "/bgs/geophys/library/Autoplot/Resources/";
    // the name of the autoplot jar
    private static final String AUTOPLOT_JAR_NAME = "autoplot.jar";
    // the nname of the jython autoplot startup script
    private static final String AUTOPLOT_STARTUP_SCRIPT_NAME = "startup";
    private static final String AUTOPLOT_SCRIPT_SUFFIX = ".py";
    // the default name of the folder that autoplot will be installed to under the user's home directory
    private static final String DEFAULT_AUTOPLOT_FOLDER_NAME = ".autoplot";
    
    // where things are in the file system
    private File autoplot_dest_jar;
    private File python_home_dir;
    private File python_lib_dir;
    // a stream reader to read data from running autoplot processes and a counter to generate unique stream IDs
    private ThreadMultiStreamReader stream_reader;
    private int stream_id_counter;
    // a thread that will handle termination
    private AutoplotExitHandler autoplot_exit_handler;
    
    /**
     * install the autoplot jar using a default folder name
     * @throws IOException if autoplot can't be found */
    public AutoplotConnector ()
    throws IOException
    {
        this (DEFAULT_AUTOPLOT_FOLDER_NAME);
    }
    
    /**
     * install the autoplot jar
     * @param autoplot_folder_name
     * @throws IOException if autoplot can't be found */
    public AutoplotConnector (String autoplot_folder_name)
    throws IOException
    {
        // find user's home folder and create an autoplot sub-folder
        File home_folder = new File (System.getProperty("user.home", "."));
        File autoplot_dest_folder = new File (home_folder, autoplot_folder_name);
        if (autoplot_dest_folder.exists())
        {
            if (! autoplot_dest_folder.isDirectory())
                throw new FileNotFoundException ("Autoplot folder must be a directory:  " + autoplot_dest_folder.getAbsolutePath());
        }
        else
        {
            if (! autoplot_dest_folder.mkdir())
                throw new FileNotFoundException ("Unable to create autoplot directory:  " + autoplot_dest_folder.getAbsolutePath());
        }
        autoplot_dest_jar = new File (autoplot_dest_folder, AUTOPLOT_JAR_NAME);
        
        // copy the autoplot jar to the destination folder
        copyFromJar (AUTOPLOT_RESOURCE_LOCATION + AUTOPLOT_JAR_NAME, autoplot_dest_jar);
        
        // add the location of the jar file to the classpath
        RunClass.addToClasspath (autoplot_dest_jar);

        // add a property python.home - set it to the folder where the autoplot jar file was found
        // Jython needs write access to this folder, so test for that
        python_home_dir = autoplot_dest_jar.getParentFile();
        if (! python_home_dir.canWrite())
            throw new FileNotFoundException ("Unable to write to python.home directory " + python_home_dir.getAbsolutePath() + ". This is the directory holding the autoplot.jar file.");
        RunClass.addToProperties ("python.home", python_home_dir.getAbsolutePath());
        
        // the Lib folder lives under $python.home and needs to be created if it doesn't exist
        python_lib_dir = new File (python_home_dir, "Lib");
        if (! python_lib_dir.exists()) python_lib_dir.mkdir();
        if (! python_lib_dir.isDirectory())
            throw new FileNotFoundException ("Python Lib is not a directory: " + python_lib_dir.getAbsolutePath());
        
        // find the startup script for autoplot and extract it to the Python lib directory
        File lib_file = new File (python_lib_dir, AUTOPLOT_STARTUP_SCRIPT_NAME + AUTOPLOT_SCRIPT_SUFFIX);
        copyFromJar (AUTOPLOT_RESOURCE_LOCATION + AUTOPLOT_STARTUP_SCRIPT_NAME + AUTOPLOT_SCRIPT_SUFFIX, lib_file);

        // start the stream reader that will monitor autoplots stdout and stderr
        stream_reader = new ThreadMultiStreamReader (100);
        stream_reader.setDaemon(true);
        stream_reader.start();
        stream_id_counter = 1;
        
        // create and link the exit handler that will shutdown autoplot instances on termination of the JVM
        autoplot_exit_handler = new AutoplotExitHandler();
        Runtime.getRuntime().addShutdownHook(autoplot_exit_handler);
    }

    /** start an autoplot instance with the given command line arguments. The arguments to set up a server are added to
     * the command line supplied (so don't need to be put in). Communicate with the new instance of autoplot using the
     * object that this method returns
     * 
     * @param args the arguments to pass to autoplot (Don't include the "-s <port>" argument)
     * @param debug_comms  true to print transmitted and received messages to/from autoplot on stdout
     * @param kill_on_exit if true, kill the autoplot instance when this JVM exits
     * @return an object that is used to communicate with the autoplot instance
     * @throws IOException if the autoplot jar can't be found or if a free port can't be found
     * @throws ClassNotFoundException  if the autoplot class can't be found in the jar
     * @throws NoSuchMethodException if the main() method can't be found in the autoplot class
     */
    public AutoplotInstance runAutoplot (String args [], boolean debug_comms, boolean kill_on_exit)
    throws IOException, ClassNotFoundException, NoSuchMethodException
    {
        // build the argument list that will be passed to autoplot
        // server=0 means that autoplot will chose the port address 
        // and outputs it on stdout in the form:
        //   autoplot is listening on port 55948.
        String [] ap_args = new String [args.length + 1];
        for (int count=0; count<args.length; count++)
            ap_args [count] = args [count];
        ap_args[args.length] = "--server=0";

        // start autoplot
        AutoplotInstance ap_instance = new AutoplotInstance (ap_args, stream_reader, stream_id_counter ++, AUTOPLOT_STARTUP_SCRIPT_NAME, debug_comms);
        
        // add this autoplot instance to the list that will be processed by the shutdown handler
        if (kill_on_exit)
            autoplot_exit_handler.addAutoplotInstance(ap_instance);
        
        return ap_instance;
    }
    
    // copy a file from the enclosing jar (or classes) to a given destination
    // compare file sizes and don't copy if they are the same (assume that
    // the file has already been copied)
    private void copyFromJar (String src_resource_name, File dest_file)
    throws FileNotFoundException
    {
        // create a URL that points to the resource and try to find its size
        URL src_url = this.getClass().getResource (src_resource_name);
        if (src_url == null) throw new FileNotFoundException ("Unable to find resource " + src_resource_name + " in .jar file");
        long src_size;
        try 
        {
            URLConnection url_connection = src_url.openConnection();
            src_size = url_connection.getContentLengthLong ();
        }
        catch (IOException ex) 
        {
            src_size = -1;
        }
        
        // if the destination file exists, check it's size against the size of the src resource
        boolean do_copy = true;
        if (dest_file.exists() && src_size > 0)
        {
            if (dest_file.length() == src_size)
                do_copy = false;
        }
        
        // copy the source to the destination
        if (do_copy)
        {
            try
            {
                Files.copy (src_url.openStream(), dest_file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e)
            {
                throw new FileNotFoundException ("Unable to copy file:  " + dest_file.getAbsolutePath());
            }
        }
    }
    
}
    


