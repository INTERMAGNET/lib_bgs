/*
 * RunClass.java
 *
 * Created on 24 November 2003, 11:58
 */

package bgs.geophys.library.Threads;

import java.io.*;
import java.net.URL;

/**
 * A wrapper for RunClassInternal and RunClassExternal that allows
 * you to specify whether the class is run internally or externally
 * using a single argument to the constructor.
 * 
 * You can run an external jar internally using:
 * 
 *   RunClass.addToClasspath (path-to-jar-file)
 *   new RunClass (...)
 *
 * @author  smf
 */
public class RunClass 
{

    /** code for internal execution */
    public static final int INTERNAL = 1;
    /** code for external execution */
    public static final int EXTERNAL = 2;
    
    // private members
    private int type;                           // INTERNAL or EXTERNAL
    private RunClassInternal internal_runner;
    private RunClassExternal external_runner;
    
    // a stream reader to discard standard output and error - we only
    // want one of these for every possible thread/process
    private static ThreadMultiStreamReader stream_reader;
    
    // initialisation of static objects
    static
    {
        stream_reader = new ThreadMultiStreamReader (3);
        stream_reader.setDaemon(true);
        stream_reader.start();
    }
    
    /** Creates a new instance of RunClass and optionally starts running the class
     * @param class_name the name of the class to run
     * @param cl_args the command line to pass to the classes main() method
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param type INTERNAL or EXTERNAL
     * @param disacrd_output for EXTERNAL streams only, read and discard standard output and error streams
     * @param user_data data that the user associates with this class
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClass(String class_name, String[] cl_args, boolean autostart, int type, boolean discard_output, Object user_data)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        Process process;
        
        this.type = type;
        switch (type)
        {
        case INTERNAL:
            internal_runner = new RunClassInternal (class_name, cl_args, autostart, user_data);
            break;
        case EXTERNAL:
            external_runner = new RunClassExternal (class_name, cl_args, autostart, user_data);
            if (discard_output)
            {
                process = external_runner.getProcess();
                stream_reader.addStream(process.getErrorStream(), true, external_runner);
                stream_reader.addStream(process.getInputStream(), true, process);
            }
            break;
        }
    }
    
    /** Creates a new instance of RunClass and optionally starts running the class
     * @param class_name the name of the class to run
     * @param cl_args the command line to pass to the classes main() method
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param type INTERNAL or EXTERNAL
     * @param disacrd_output for EXTERNAL streams only, read and discard standard output and error streams
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClass(String class_name, String[] cl_args, boolean autostart, int type, boolean discard_output)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        this (class_name, cl_args, autostart, type, discard_output, null);
    }

    /** Creates a new instance of RunClass and optionally starts running the class
     * @param class_name the name of the class to run
     * @param args the command line to pass to the classes main() method
     *             this string is split into arguments based on whitespace
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param type INTERNAL or EXTERNAL
     * @param disacrd_output for EXTERNAL streams only, read and discard standard output and error streams
     * @param user_data data that the user associates with this class
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClass(String class_name, String args, boolean autostart, int type, boolean discard_output, Object user_data)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        Process process;
        
        this.type = type;
        switch (type)
        {
        case INTERNAL:
            internal_runner = new RunClassInternal (class_name, args, autostart, user_data);
            break;
        case EXTERNAL:
            external_runner = new RunClassExternal (class_name, args, autostart, user_data);
            if (discard_output)
            {
                process = external_runner.getProcess();
                stream_reader.addStream(process.getErrorStream(), true, external_runner);
                stream_reader.addStream(process.getInputStream(), true, process);
            }
            break;
        }
    }
    
    /** Creates a new instance of RunClass and optionally starts running the class
     * @param class_name the name of the class to run
     * @param args the command line to pass to the classes main() method
     *             this string is split into arguments based on whitespace
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param type INTERNAL or EXTERNAL
     * @param disacrd_output for EXTERNAL streams only, read and discard standard output and error streams
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClass(String class_name, String args, boolean autostart, int type, boolean discard_output)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        this (class_name, args, autostart, type, discard_output, null);
    }
    
    /** call this method to manually start the class running
     * @throws IOException if the programme could not be run */
    public void start ()
    throws IOException
    {
        switch (type)
        {
        case INTERNAL:
            internal_runner.start();
        case EXTERNAL:
            external_runner.start();
        }
    }
    
    /** get the name of the class that this object is running 
     * @return the class name */
    public String getClassName () 
    {
        switch (type)
        {
        case INTERNAL: return internal_runner.getClassName();
        case EXTERNAL: return external_runner.getClassName();
        }
        return null;
    }
    
    public boolean isAlive ()
    {
        switch (type)
        {
        case INTERNAL: return internal_runner.isAlive();
        case EXTERNAL: return external_runner.getProcess().isAlive();
        }
        return false;
    }
    
    /** get the command line that this class runs with
     * @return the command line */
    public String [] getArgs () 
    {
        switch (type)
        {
        case INTERNAL: return internal_runner.getArgs();
        case EXTERNAL: return external_runner.getArgs();
        }
        return null;
    }
    
    /** get the thread (internal) or process (external) that this class is running in
     * @return the process or thread - cast to the correct object type */
    public Object getThreadOrProcess () 
    {
        switch (type)
        {
        case INTERNAL: return (Object) internal_runner;     // internal_runner is a Thread
        case EXTERNAL: return (Object) external_runner.getProcess();
        }
        return null;
    }
    
    /** remove completed threads and processes from the list */
    public static void reapRunClasses ()
    {
        RunClassInternal.reapRunClassInternalThreads();
        RunClassExternal.reapRunClassExternalProcesses();
    }
    
    /** get an array of RunClassInternal and RunClassExternal objects representing the running threads
     * @return the list of threads and processes - for each array element use instanceof to check the 
     *         type and then cast to the correct object */
    public static Object [] listRunClasses ()
    {
        int count;
        RunClassInternal internal[];
        RunClassExternal external[];
        Object array [];
        
        internal = RunClassInternal.listRunClassInternalThreads();
        external = RunClassExternal.listRunClassExternalProcesses();
        array = new Object [internal.length + external.length];
        for (count=0; count<internal.length; count++)
            array [count] = (Object) internal [count];
        for (count=0; count<external.length; count++)
            array [count + internal.length] = (Object) external [count];
        return array;
    }
    
    /** add a property to the properties that will be set when a class is run either internally or externally */
    public static void addToProperties (String name, String value)
    {
        RunClassInternal.addToProperties(name, value);
        RunClassExternal.addToProperties(name, value);
    }
    
    public static void removeFromProperties (String name)
    {
        RunClassInternal.removeFromProperties(name);
        RunClassExternal.removeFromProperties(name);
    }

    /** if you want to run a jar file that isn't on the class path as an internal
     * process, you need to call this method to add it to the classpath first */
    public static void addToClasspath (File file)   throws IOException { ClasspathAdder.addFile(file); }
    public static void addToClasspath (String path) throws IOException { ClasspathAdder.addFile(path); }
    public static void addToClasspath (URL url)     throws IOException { ClasspathAdder.addURL(url); }
}
