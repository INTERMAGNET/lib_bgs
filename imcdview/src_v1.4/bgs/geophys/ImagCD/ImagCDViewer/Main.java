/*
 * Main.java
 *
 * Created on 02 June 2002, 16:03
 */

package bgs.geophys.ImagCD.ImagCDViewer;

import bgs.geophys.library.Swing.*;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.*;
import bgs.geophys.library.Misc.CheckVersion;
import java.util.Locale;

/**
 * The program entry point.
 * @author  Simon
 * @version 
 */
public class Main extends java.lang.ThreadGroup
{
  
  /** Program entry point
   * @param args the command line arguments */
  public static void main (String [] args)
  {
    int count;
    ThreadGroup main_thread_group;

    // check for minimum java run time
    CheckVersion.CheckDefaultVersion().msgSwing(true);
    
    // set the default locale (prevents other locales from breaking date
    // formatting, file chooser language, etc...
    Locale.setDefault(Locale.UK);
        
    // debugging can be turned on using properties (java -Ddebug Main)
    // or on the command line (java Main debug)
    for (count=0; count<args.length; count++)
    {
        if (args[count].equalsIgnoreCase ("debug"))
            System.setProperty("debug", "true");
    }

    // create a thread group to run the main thread in
    main_thread_group = new Main ();

    // start the application inside this thread group - that way
    // unhandled exceptions from all threads (including SWING/AWT)
    // will be properly handled
    new Thread (main_thread_group, "MainThread") 
    {
        @Override
        public void run ()
        {
            MainWin dummy;
            
            // set the default look and feel
            SwingUtils.setPlatformLookAndFeel ();
            
            // force global objects to load
            dummy = GlobalObjects.top_frame;
        }
    }.start ();
  }

  /** constructor for thread group */
  public Main ()
  {
    super ("IMCDViewerThreadGroup");
  }
    
  /** catch unhandled exceptions in all threads */
  public void uncaughtException(Thread t, Throwable e)  
  {
      String report_fields [];
      FaultReport fault_report;

      report_fields = new String [3];
      report_fields [0] = "What part of the software were you using: ";
      report_fields [1] = "What data were you accessing: ";
      report_fields [2] = "Any other information: ";
      fault_report = new FaultReport (null, true, 
                                      AboutDialog.PROGRAM_NAME + " Version " + AboutDialog.formatVersionNumber(),
                                      "smf@bgs.ac.uk", report_fields, e);
      fault_report.setVisible (true);
      
      System.exit (1);
  }

}
