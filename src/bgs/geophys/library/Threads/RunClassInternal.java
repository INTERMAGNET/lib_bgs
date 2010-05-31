/*
 * RunClassInternal.java
 *
 * Created on 19 November 2003, 19:38
 */

package bgs.geophys.library.Threads;

import java.lang.reflect.*;
import java.util.*;

/**
 * Run a class in a new thread. The class must have a static method
 * called main() that accepts an array of strings containing its
 * command line. This is an alternative to starting the class
 * using an external java virtual machine. If the class exits by
 * calling System.exit() or Runtime.exit() the virtual machine will
 * stop. You can get around this (in an application, not an applet)
 * by installing the ConfigurableSecurityManager. To install the
 * manager:
 * <pre>
        ConfigurableSecurityManager my_security_manager = new ConfigurableSecurityManager ();
        my_security_manager.removePermission(new RuntimePermission ("exitVM"));
        System.setSecurityManager(my_security_manager);
 * </pre>
 * This will stop any thread (including the main one) from exiting. When the
 * main thread needs to exit, it should do the following:
 * <pre>
        my_security_manager.addAllPermissions();
        System.exit (0);
 * </pre>
 * The one possible problem with this is when the called application catches
 * the SecurityException and handles it by itself.
 *
 * @author  Simon
 */
public class RunClassInternal extends Thread 
{

    // details on the thread this object represents
    private String class_name;
    private Class new_class;
    private String args[];
    private Object main_args [];
    private Method main_method;
    private Object user_data;
    
    // a list of all the RunClassInternal threads that are running
    private static Vector<RunClassInternal> thread_list;
    
    // a number used to give each Thread's group a unique identifier
    private static int thread_group_id;

    /** static member initialisation */
    static
    {
        thread_list = new Vector<RunClassInternal> ();
        thread_group_id = 0;
    }
    
    /** Creates a new instance of RunClassInternal and optionally starts a new thread
     * @param class_name the name of the class to run
     * @param cl_args the command line to pass to the classes main() method
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param user_data data that the user associates with this class (may be null)
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method */
    public RunClassInternal(String class_name, String[] cl_args, boolean autostart, Object user_data)
    throws ClassNotFoundException, NoSuchMethodException
    {
        super (new ThreadGroup ("RunClass" + Integer.toString (thread_group_id ++)),
               class_name);

        this.class_name = class_name;
        this.args = cl_args;
        this.user_data = user_data;
        
        commonConstructor ();
        if (autostart) this.start();
    }
    
    /** Creates a new instance of RunClassInternal and optionally starts a new thread
     * @param class_name the name of the class to run
     * @param args the command line to pass to the classes main() method
     *             this string is split into arguments based on whitespace
     * @param autostart If true start the thread at the end of construction.
     *        If autostart is false, call this classes start() method to
     *        start the thread.
     * @param user_data data that the user associates with this class (may be null)
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method */
    public RunClassInternal(String class_name, String args, boolean autostart, Object user_data)
    throws ClassNotFoundException, NoSuchMethodException
    {
        super (new ThreadGroup ("RunClassInternal" + Integer.toString (thread_group_id ++)),
               class_name);

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

    /** common code to set up the class for running
     * @throws ClassNotFoundException if the class could not be found
     * @throws NoSuchMethodException if the class does not have a main() method */
    @SuppressWarnings ("unchecked")
    private void commonConstructor ()
    throws ClassNotFoundException, NoSuchMethodException
    {
        ClassLoader class_loader;
        Class class_array [];
        
        class_loader = ClassLoader.getSystemClassLoader();
        new_class = class_loader.loadClass (class_name);
        class_array = new Class [1];
        class_array[0] = String[].class;
        // next line will produce 'unchecked cast' warning - this is OK
        // because getMethod is supposed to take an array containing different
        // types in its 2nd parameter - NOTE unchecked warning supressed
        main_method = new_class.getMethod("main", class_array);
        main_args = new Object [1];
        main_args[0] = args;
    }
    
    /** start the thread */
    public void start ()
    {
        super.start();
        thread_list.add (this);
 
                
    }
    
    /** the code that runs when the new thread starts */
    public void run() 
    {        
        Thread thread_array [];
        ThreadGroup my_group;

        try
        {
            // call main() on the class
            main_method.invoke (null, main_args);
            
            // wait for any threads to die
            my_group = getThreadGroup();
            do
            {
                thread_array = new Thread [my_group.activeCount()];
                my_group.enumerate(thread_array);
                thread_array[0].join();
            } while (thread_array.length > 0);
        }
        catch (Exception e) 
        {
            boolean found;
            
            // check if this was an attempt to exit - if so silently ignore it
            found = false;
            if (e instanceof SecurityException)
            {
                if (e.getMessage().indexOf ("exitVM") >= 0) found = true;
            }
            if (! found) e.printStackTrace(); 
        }
        
        new_class = null;
    }

    /** get the name of the class that this object is running 
     * @return the class name */
    public String getClassName () { return class_name; }
    
    /** get the command line that this class runs with
     * @return the command line */
    public String [] getArgs () { return args; }
    
    /** Getter for property user_data.
     * @return Value of property user_data. */
    public java.lang.Object getUserData() { return user_data; }
    
    /** remove completed threads from the list */
    public static void reapRunClassInternalThreads ()
    {
        int count;
        RunClassInternal proc;
        
        for (count=0; count<thread_list.size(); count++)
        {
            proc = (RunClassInternal) thread_list.get(count);
            if (! proc.isAlive())
            {
                thread_list.remove(count);
                count --;
            }
        }
    }
    
    /** get an array of RunClassInternal objects representing the running threads
     * @return the list of threads */
    public static RunClassInternal [] listRunClassInternalThreads ()
    {
        int count;
        
        RunClassInternal [] array;
        array = new RunClassInternal [thread_list.size()];
        for (count=0; count<thread_list.size(); count++)
            array [count] = (RunClassInternal) thread_list.get(count);
        return array;
    }
    
}
