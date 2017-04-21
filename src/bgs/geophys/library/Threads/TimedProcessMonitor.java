/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Threads;

import java.util.Date;

/**
 * An extension to process monitor that allows processes to be timed
 * 
 * @author smf
 */
public class TimedProcessMonitor extends ProcessMonitor
{
    
    private Date start_date;
    
    /** Creates a new instance of ProcessMonitor 
     * @param command the command and arguments to run */
    public TimedProcessMonitor (String command) 
    {
        super (command);
    }

    /** Creates a new instance of ProcessMonitor 
     * @param command the command and arguments to run
     * @param stdin_data data to feed to the process on its standard input */
    public TimedProcessMonitor (String command, String stdin_data) 
    {
        super (command, stdin_data);
    }

    /** start the external process */
    @Override
    public void startProcess ()
    {
        start_date = new Date ();
        super.startProcess();
    }
    
    /** get the date that the process started
     * @return null if the process has not started yet
     */
    public Date getProcessStartDate ()
    {
        return start_date;
    }
}
