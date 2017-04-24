/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.cdf.dump;

import bgs.geophys.library.Threads.ProcessMonitor;
import bgs.geophys.library.Threads.TimedProcessMonitor;
import gsfc.nssdc.cdf.CDFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * Here is the help info from the cdfdump command on Windows:
 * 
Usage:         > cdfdump [-[no]format] [-dump <option>] [-output <file-path>]
                         [-vars <var1,var2,...varN>] [-about] [-[no]header]
                         [-recordrange <rec1,rec2>] [-significant nn]
                         <cdf-path>

Purpose:       CDFdump dumps the data contents in a CDF.

Parameter(s):  <cdf-path>
                  The pathname of the CDF to dump (do not enter an
                  extension).

               -[no]format
                  Specifies whether or not the FORMAT attribute is used
                  when displaying variable values (if the FORMAT attribute
                  exists and an entry exists for the variable). The default
                  is to use the format. For non-format display, the C's "%g"
                  print format is used, which produces an output in either
                  floating point ("%e") or scientific notation ("%e") form.
                  If the format is not properly set, the data could be shown
                  in multiple asterisks. In this case, use noformat option.

               -significant nn
                  Specifies how the floating-point (FP) values are to be
                  displayed. This option is applicable only if FORMAT is to
                  be used, however its FORMAT entry does not exist. The
                  format for showing the FP values, by default, is "%g",
                  which might not be good enough, precision-wise, for large
                  values.  With this option, the format becomes "%.nng",
                  where nn is the maximum significant digit number (>0) to
                  display.

               -dump <option>
                  Specifies how the program should produce a dump.
                  Valid options are "all", "data", "metadata", "global" and
                  "variable". For "all" option, the default, the output will
                  include detailed information about the CDF, not just only
                  the metadata and variable data. For "data" option, variable
                  data and minimum information about the variable is dumped.
                  For "metadata" option, output only includes the global
                  attributes as well as the variable attributes. For "global"
                  option, only the global attributes are dumped. For
                  "variable" option, the variable attributes and entries are
                  displayed.

               -output <file-path>
                  Redirects the output to a file.  The file created will
                  be named <file-path> (if <file-path> does not have an
                  extension, `.txt' is appended automatically).  If this
                  qualifier is not specified, the output is displayed at
                  the terminal. If this qualifier is entered as "source",
                  then the source CDF pathname is used for its output
                  name with its extension of ".cdf" being replaced by ".txt".

               -vars <[var1,][var2,]...[varN]>
                  Specifies which variables in the CDF will be dumped.
                  If not specified, the default, all variables are dumped.
                  Variable names should be separated by a comma.

               -recordrange <[rec1,][rec2]>
                  While the program, by default, will dump all records
                  from a variable. This option allows specifying only
                  a range of records, from the starting to the ending
                  inclusively, to be dumped. The variable record starts
                  from record one (1).  If only one record number is
                  provided, that number is assumed to be the starting
                  record and all records after that will be dumped.
                  So, "-recordrange 1" (or "/RECORDRANGE=1" for VMS)
                  is the same as the default as all records are dumped.
                  The record numbers should be separated by a comma.

               -about
                  Shows the library version that was used to create this tool
                  program.

               -[no]header
                  Whether to show the 1-line header "Dumping cdf from ...."
                  from the dump output. No display if "-noheader" is specified.

Example(s):    > cdfdump gisswetl
               > cdfdump -noformat -dump data a:\gisssoil
               > cdfdump -output tplate3 ..\..\samples\tplate3
               > cdfdump -dump data -vars "var1,var2" my_sample
               > cdfdump -dump data -recordrange "5,10" my_sample * 
 * @author smf
 */
public class CDFDump 
{
    
    private ProcessMonitor dump_process_monitor;
    private File cdf_text_dump_file;
    
    /** dump a CDF file to a text file using the cdfdump tool in the CDF toolkit. The
     * CDF software must be installed
     * 
     * @param cdf_file the file to dump
     * @param rec_start the first record to dump or -1 for all records
     * @param rec_end the last record to dump
     * @param cdf_text_dump_file the output file to dump into
     * @throws CDFException if there's a CDF problem
     * @throws IOException if there's an IO problem with either of the files
     */
    public CDFDump (File cdf_file, int rec_start, int rec_end, File cdf_text_dump_file) 
    throws CDFException, IOException
    {
        this.cdf_text_dump_file = cdf_text_dump_file;
        this.dump_process_monitor = null;
        
        // find the cdfdump command
        File cdf_dump_exe = null;
        String exe_name;
        if (File.separator.equals("\\"))
            exe_name = "cdfdump.exe";
        else
            exe_name = "cdfdump";
        String path = System.getenv("PATH");
        if (path != null)
        {
            String path_folders [] = path.split (File.pathSeparator);
            for (String path_folder : path_folders)
            {
                cdf_dump_exe = new File (path_folder, exe_name);
                if (cdf_dump_exe.exists()) break;
                cdf_dump_exe = null;
            }
        }
        
        // check cdfdump can be found
        if (cdf_dump_exe == null)
            throw new CDFException ("Cant find cdfdump command - is CDF installed on this computer?");
        
        // check CDF file can be found
        if (! cdf_file.canRead())
            throw new IOException ("Unable to read CDF file: " + cdf_file.getAbsolutePath());
        
        // bulld the cdfdump command line
        String cmd_line = "'" + cdf_dump_exe.getAbsolutePath() + "' -noheader";
        if (rec_start > 0) cmd_line += " -recordrange " + rec_start + "," + rec_end;
        cmd_line += " -output '" + cdf_text_dump_file.getAbsolutePath() + "' '" + cdf_file.getAbsolutePath() + "'";
        
        // start the cdfdump process
        dump_process_monitor = new TimedProcessMonitor (cmd_line);
        dump_process_monitor.startProcess();
    }

    /** terminate the dump process early */
    public void killDump ()
    {
        if (dump_process_monitor != null)
            dump_process_monitor.stopProcess();
    }

    /** find out if the dump has finished
     * 
     * @return true if the dump process has ended
     * @throws IOException if the dump process didn't terminate correctly
     *         only throws this exception once (after which false is returned)
     */
    public boolean isDumpFinished ()
    throws IOException
    {
        if (dump_process_monitor == null) return true;

        if (! dump_process_monitor.isProcessAlive())
        {
            // cdfdump doesn't set its exit code, so check whether the output file exists
            IOException e = null;
            if (cdf_text_dump_file.length() <= 0l)
            {
                if (dump_process_monitor.getNIOExceptions() > 0)
                    e = dump_process_monitor.getIOException(0);
                else
                {
                    String msg = "Error running cdfdump";
                    String data = dump_process_monitor.getStderrData();
                    if (data != null) msg += ": " + data;
                    e = new IOException (msg);
                }
            }
            dump_process_monitor = null;
            if (e != null) throw e;
            return true;
        }
        
        return false;
    }
    
}
