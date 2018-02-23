/*
 * ProcessMonitor.java
 *
 * Created on 08 June 2006, 09:16
 */

package bgs.geophys.library.Threads;

import java.text.*;
import java.util.*;
import java.io.*;

/**
 * A class to start, monitor and kill an external process
 *
 * @author  smf
 */
public class ProcessMonitor implements Runnable
{

    private String command;
    private String stdout_data;
    private String stderr_data;
    private String stdin_data;
    private Process process;
    private Thread monitor_thread;
    private int exit_value;
    private Vector<IOException> stored_io_exceptions;
    private int process_id;
    
    private static int process_id_counter;
    
    static { process_id_counter = 0; }
    
    /** Creates a new instance of ProcessMonitor 
     * @param command the command and arguments to run */
    public ProcessMonitor (String command) 
    {
        this.command = command;
        this.stdin_data = null;
        process = null;
        monitor_thread = null;
        exit_value = -1;
        stored_io_exceptions = new Vector<IOException> ();
        stdout_data = stderr_data = null;
        process_id = process_id_counter ++;
    }

    /** Creates a new instance of ProcessMonitor 
     * @param command the command and arguments to run
     * @param stdin_data data to feed to the process on its standard input */
    public ProcessMonitor (String command, String stdin_data) 
    {
        this.command = command;
        this.stdin_data = stdin_data;
        process = null;
        monitor_thread = null;
        exit_value = -1;
        stored_io_exceptions = new Vector<IOException> ();
        stdout_data = stderr_data = null;
        process_id = process_id_counter ++;
    }
    
    
    /** start the external process */
    public void startProcess ()
    {
        String param_array [];
        
        // start the process
        param_array = createArgs ();
        if (param_array.length <= 0)
            stored_io_exceptions.add (new IOException ("Command has no arguments"));
        else
        {
            try
            {
                process = Runtime.getRuntime().exec(param_array);
                
                // start feeding the streams
                monitor_thread = new Thread (this);
                monitor_thread.start();
            }
            catch (IOException e)
            {
                stored_io_exceptions.add (e);
            }
        }
    }
    
    /** stop the external process - nicely */
    public void stopProcess ()
    {
        synchronized (process)
        {
            if (process != null) process.destroy();
        }
    }

    /** stop the external process - not nicely */
    public void killProcess ()
    {
        synchronized (process)
        {
            if (process != null) process.destroy();
            if (monitor_thread != null) monitor_thread.interrupt();
        }
    }

    /** is the external process still running */
    public boolean isProcessAlive ()
    {
        if (monitor_thread == null) return false;
        return monitor_thread.isAlive();
    }

    /** get the command used to start this process */
    public String getCommand ()
    {
        return command;
    }
    
    /** get the process id - this is an internally generated id, not the system PID */
    public int getProcessID ()
    {
        return process_id;
    }
    
    /** get the exit value of the external process - DONT call
     * this until isProcessAlive returns false - you won't get a
     * sensbile value! */
    public int getExitValue ()
    {
        return exit_value;
    }
    
    /** get data from stdout of the external process - DONT call
     ** this until isProcessAlive returns false - you won't get a
     * sensbile value! */
    public String getStdoutData ()
    {
        return stdout_data;
    }

    /** get data from stderr of the external process - DONT call
     ** this until isProcessAlive returns false - you won't get a
     * sensbile value! */
    public String getStderrData ()
    {
        return stderr_data;
    }
    
    /** while this object is processing the IO streams to the external process
     * it may record IOExceptions - thes can be retrieved through this method */
    public int getNIOExceptions ()
    {
        int size;
        
        synchronized (stored_io_exceptions)
        {
            size = stored_io_exceptions.size();
        }
        return size;
    }

    /** while this object is processing the IO streams to the external process
     * it may record IOExceptions - thes can be retrieved through this method */
    public IOException getIOException (int index)
    {
        IOException e;
        
        synchronized (stored_io_exceptions)
        {
            e = stored_io_exceptions.get(index);
        }
        return e;
    }
    
    /** even though it is public DONT call it! */
    public void run() 
    {
        int length, count;
        boolean process_alive, quit;
        char ch;
        StringBuffer stdout_buffer, stderr_buffer, stdin_buffer, buffer;
        InputStream stream;
        byte chunk [];
        InputStream process_stdout, process_stderr;
        OutputStream process_stdin;
              
        // connect stdout, stderr and optionally stdin to the new process
        synchronized (process)
        {
            process_stdout = process.getInputStream();
            process_stderr = process.getErrorStream();
            process_stdin = process.getOutputStream();
        }

        // loop, feeding streams, until external process exits
        chunk = new byte [2048];
        stdout_buffer = new StringBuffer ();
        stderr_buffer = new StringBuffer ();
        if (stdin_data != null) stdin_buffer = new StringBuffer (stdin_data);
        else stdin_buffer = new StringBuffer ();
        do
        {
            // get data out of standard output and standard error
            for (count=0; count<2; count++)
            {
                if (count == 0)
                {
                    stream = process_stdout;
                    buffer = stdout_buffer;
                }
                else
                {
                    stream = process_stderr;
                    buffer = stderr_buffer;
                }
                try
                {
                    while (stream.available() > 0)
                    {
                        length = stream.read (chunk);
                        if (length > 0) buffer.append (new String (chunk, 0, length));
                    }
                }
                catch (IOException e)
                {
                    synchronized (stored_io_exceptions)
                    {
                        stored_io_exceptions.add (e);
                    }
                }
            }
            
            // feed data into standard input
            if (stdin_buffer.length () <= 0) quit = true;
            else quit = false;
            while (! quit)
            {
                ch = stdin_buffer.charAt (0);
                try
                {
                    process_stdin.write ((int) ch);
                    stdin_buffer.delete (0, 1);
                    if (stdin_buffer.length () <= 0) quit = true;
                }
                catch (IOException e)
                {
                    stored_io_exceptions.add (e);
                    quit = true;
                }
            }
            
            // find out if the process is alive and get its exit code
            try 
            {
                synchronized (process)
                {
                    exit_value = process.exitValue();
                }
                process_alive = false;
            }
            catch (IllegalThreadStateException e)
            {
                process_alive = true;
            }
            
            // wait before looping
            if (process_alive) 
            {
                try 
                {
                    Thread.sleep(200); 
                }
                catch (InterruptedException e) 
                {
                    process.destroy ();
                    process_alive = false;
                }
            }
            
        } while (process_alive);
        
        // convert stdout and stderr to strings
        stdout_data = new String (stdout_buffer);
        stderr_data = new String (stderr_buffer);
    }
    
    /** create an argument list for runtime.exec from the command string - respect quotes
      * @return the argument list */
    private String [] createArgs ()
    {
        int count, ch, state;
        String param_array [];
        StringCharacterIterator iterator;
        StringBuffer buffer;
        Vector<StringBuffer> arg_list;
        
        // we collect data into a Vector of StringBuffers
        arg_list = new Vector<StringBuffer> ();
        
        // for each character in the string
        iterator = new StringCharacterIterator (command);
        state = 0;
        buffer = null;
        for (ch = iterator.first(); ch != iterator.DONE; ch = iterator.next())
        {
            // what state are we in??
            switch (state)
            {
            case 0: // removing whitespace at the front of a string
                // check for non-whitespace characters
                switch (ch)
                {
                case '\r':
                case '\n':
                case ' ':
                    // continue consuming whitespace
                    break;
                case '\'':
                    // change state - collect data up to next single quote
                    state = 1;
                    buffer = new StringBuffer ();
                    arg_list.add (buffer);
                    break;
                case '"':
                    // change state - collect data up to next double quote
                    state = 2;
                    buffer = new StringBuffer ();
                    arg_list.add (buffer);
                    break;
                default:
                    // change state - collect data up to next whitespace
                    state = 3;
                    buffer = new StringBuffer ();
                    buffer.append ((char) ch);
                    arg_list.add (buffer);
                    break;
                }
                break;
                
            case 1: // collect data up to next single quote
                if (ch == '\'') state = 0;
                else buffer.append ((char) ch);
                break;
                
            case 2: // collect data up to next double quote
                if (ch == '"') state = 0;
                else buffer.append ((char) ch);
                break;
                
            case 3: // collect data up to next whitespace, but check for quoted strings as well
                switch (ch)
                {
                case '\r':
                case '\n':
                case ' ':
                    state = 0;
                    break;
                case '\'':
                    state = 1;
                    buffer = new StringBuffer ();
                    arg_list.add (buffer);
                    break;
                case '"':
                    state = 2;
                    buffer = new StringBuffer ();
                    arg_list.add (buffer);
                default:
                    buffer.append ((char) ch);
                    break;
                }
                break;
            }
        }
        
        // convert the Vector of StringBuffers into an array of strings
        param_array = new String [arg_list.size()];
        for (count=0; count<param_array.length; count++)
            param_array [count] = new String (arg_list.get(count));
        
        return param_array;
    }
    
}
