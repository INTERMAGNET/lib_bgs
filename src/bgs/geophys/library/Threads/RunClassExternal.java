/*
 * RunClassExternal.java
 *
 * Created on 19 November 2003, 19:38
 */

package bgs.geophys.library.Threads;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 * Run a class in a new virtual machine. The class must have a static method
 * called main() that accepts an array of strings containing its
 * command line. 
 *
 * @author  Simon
 */
public class RunClassExternal
{

    // details on the process this object represents
    private String class_name;
    private String args[];
    private Process process;
    private Object user_data;
    
    // a list of all the RunClassExternal processes that are running
    private static Vector<RunClassExternal> process_list;
    
    // contents of the System properties java.home and java.class.path (may be null)
    private static String java_dir;
    private static String java_classpath;
    
    /** static member initialisation */
    static
    {
        java_dir = System.getProperty ("java.home");
        java_classpath = System.getProperty ("java.class.path");
        process_list = new Vector<RunClassExternal> ();
    }
    
    /** Creates a new instance of RunClassExternal and optionally starts a new process
     * @param class_name the name of the class to run
     * @param cl_args the command line to pass to the classes main() method
     * @param autostart If true start the process at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the process.
     * @param user_data data that the user associates with this class (may be null)
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClassExternal(String class_name, String[] cl_args, boolean autostart, Object user_data)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        this.class_name = class_name;
        this.args = cl_args;
        this.user_data = user_data;

        commonConstructor ();
        if (autostart) this.start();
    }
    
    /** Creates a new instance of RunClassExternal and optionally starts a new process
     * @param class_name the name of the class to run
     * @param args the command line to pass to the classes main() method
     *             this string is split into arguments based on whitespace
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param user_data data that the user associates with this class (may be null)
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method
     * @throws IOException if the programme could not be run */
    public RunClassExternal(String class_name, String args, boolean autostart, Object user_data) 
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        int count;
        StringTokenizer tokens;
        String cl_args [];
        
        this.class_name = class_name;
        tokens = new StringTokenizer (args);
        cl_args = new String [tokens.countTokens()];
        for (count=0; tokens.hasMoreTokens(); count ++) cl_args [count] = tokens.nextToken();
        this.args = cl_args;
        this.user_data = user_data;
        
        commonConstructor ();
        if (autostart) this.start();
    }

    /** common code to check that the class is runnable
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method */
    @SuppressWarnings("unchecked")
    private void commonConstructor ()
    throws ClassNotFoundException, NoSuchMethodException
    {
        ClassLoader class_loader;
        Class class_array [], new_class;
        Method main_method;
        
        if (java_dir == null) throw new ClassNotFoundException ("java.home not defined");
        if (java_classpath == null) throw new ClassNotFoundException ("java.class.path not defined");
        class_loader = ClassLoader.getSystemClassLoader();
        new_class = class_loader.loadClass (class_name);
        class_array = new Class [1];
        class_array[0] = String[].class;
        // next line will produce 'unchecked cast' warning - this is OK
        // because getMethod is supposed to take an array containing different
        // types in its 2nd parameter - NOTE unchecked warning supressed
        main_method = new_class.getMethod("main", class_array);
    }
    
    /** the code that starts the new VM
     * @throws IOException if the programme could not be run */
    public void start() throws IOException
    {        
        int count;
        String command, java_args [];
        
        java_args = new String [4 + args.length];
        java_args [0] = java_dir + File.separator + "bin" + File.separator + "java";
        java_args [1] = "-cp";
        java_args [2] = java_classpath;
        java_args [3] = class_name;
        for (count=0; count<args.length; count++) java_args [count + 4] = args [count];

        // create a new VM
        process = Runtime.getRuntime().exec(java_args);
                
        // add this thread to the list of threads
        process_list.add (this);
    }

    /** get the name of the class that this object is running 
     * @return the class name */
    public String getClassName () { return class_name; }
    
    /** get the command line that this class runs with
     * @return the command line */
    public String [] getArgs () { return args; }

    /** get the process that this class is running in
     * @return the process */
    public Process getProcess () { return process; }
    
    /** Getter for property user_data.
     * @return Value of property user_data. */
    public java.lang.Object getUserData() { return user_data; }
    
    /** remove completed processes from the list */
    public static void reapRunClassExternalProcesses ()
    {
        int count;
        RunClassExternal proc;
        
        // remove any dead processes
        for (count=0; count<process_list.size(); count++)
        {
            proc = (RunClassExternal) process_list.get(count);
            try
            {
                proc.getProcess().exitValue ();
                process_list.remove (count);
                count --;
            }
            catch (IllegalThreadStateException e) { }
        }
        
    }
    
    /** get an array of RunClassExternal objects representing the running threads
     * @return the list of threads */
    public static RunClassExternal [] listRunClassExternalProcesses ()
    {
        int count;
        RunClassExternal array [];
        
        // create an array of processes
        array = new RunClassExternal [process_list.size()];
        for (count=0; count<process_list.size(); count++)
        {
            array [count] = process_list.get(count);
        }
        return array;
    }
    
}
