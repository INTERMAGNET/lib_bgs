/*
 * ProcessTable.java
 *
 * Created on 29 June 2007, 09:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Threads;

import java.util.Vector;

/**
 * A list of running processes
 *
 * @author smf
 */
public class ProcessTable 
{

    private Vector<ProcessMonitor> process_table;
    
    /** Creates a new instance of ProcessTable */
    public ProcessTable() 
    {
        process_table = new Vector<ProcessMonitor> ();
    }
    
    /** start a new process using a default check delay
     * @param command the command to start
     * @return the process ID, or a -ve number if the process failed to start - this id 
     *         is internally generated, it is not the system process ID*/
    public int startProcess (String command) { return startProcess (command, 100l); }

    /** start a new process
     * @param command the command to start
     * @param check_delay the amount of time (in mS) to wait before checking that the process started OK
     * @return the process ID, or a -ve number if the process failed to start - this id 
     *         is internally generated, it is not the system process ID*/
    public int startProcess (String command, long check_delay)
    {
        ProcessMonitor proc;
        
        // start the process
        proc = new ProcessMonitor (command);
        proc.startProcess ();
        
        // wait for it to get started
        try { Thread.sleep (check_delay); } catch (InterruptedException e) { }
        
        // check it is is still running
        if (! proc.isProcessAlive ()) return -1;
        process_table.add (proc);
        return proc.getProcessID ();
    }
    
    /** weed out dead processes from the table 
     * @return an array of processes that have been removed from the table, which may be of zero length */
    public Vector<ProcessMonitor> reapDeadProcesses ()
    {
        int count;
        Vector<ProcessMonitor> dead_procs;
        ProcessMonitor proc;
        
        // make a table of dead processes
        dead_procs = new  Vector<ProcessMonitor> ();
        for (count=0; count<process_table.size(); count++)
        {
            proc = process_table.get(count);
            if (! proc.isProcessAlive())
                dead_procs.add (proc);
        }
        
        // remove the dead processes from the main table
        for (count=0; count<dead_procs.size(); count++)
            process_table.remove (dead_procs.get (count));
        
        return dead_procs;
    }
    
    /** get the number of processes in the table (may include dead ones not yet 
     * collected using reapDeadProcesses) */
    public int getNProcesses () { return process_table.size(); }
    
    /** get a process by its index in the process table */
    public ProcessMonitor  getProcess (int index) { return process_table.get (index); }
    
    /** get a process by its process ID 
     * @return the process or null if it couldn't be found */
    public ProcessMonitor findProcess (int pid)
    {
        int count;
        ProcessMonitor proc;
        
        for (count=0; count<process_table.size(); count++)
        {
            proc = process_table.get(count);
            if (proc.getProcessID() == pid) return proc;
        }
        return null;
    }
    
}
