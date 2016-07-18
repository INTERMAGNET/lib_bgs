/*
 * PrintCanvas.java
 *
 * Created on 29 October 2003, 12:20
 */

package bgs.geophys.library.Swing;

import bgs.geophys.library.Misc.Utils;
import java.awt.Component;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.swing.JOptionPane;

/**
 * Object that allows printing of objects that implement the Printable interface
 * 
 * @author  smf
 */
public class PrintCanvas 
{
    
    // objects used for printing
    private DocFlavor print_doc_flavor;
    private PrintRequestAttributeSet print_request_attr_set;
    private PrintService default_print_service;
    private PrintService [] print_service_array;
    
    /** Creates a new instance of PrintCanvas */
    public PrintCanvas() 
    {
        print_doc_flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        print_request_attr_set = new HashPrintRequestAttributeSet ();
        default_print_service = PrintServiceLookup.lookupDefaultPrintService ();
        print_service_array = PrintServiceLookup.lookupPrintServices (print_doc_flavor, print_request_attr_set);
    }
    
    /** print the canvas - show the printer selection dialog first
     * @param parent the parent for any error message dialogs
     * @param what_to_print the object to print - this object must implement the
     * Printable interface
     * @return an erro message OR null if the print worked OK */
    public void safeDoPrint (Component parent, Printable what_to_print)
    {
        safeDoPrint (parent, what_to_print, 200, 200);
    }
    
    /** print the canvas - show the printer selection dialog first
     * @param parent the parent for any error message dialogs
     * @param what_to_print the object to print - this object must implement the
     * @param x_dialog_pos the x position for the print dialog
     * @param y_dialog_pos the y position for the print dialog
     * Printable interface
     * @return an erro message OR null if the print worked OK */
    public void safeDoPrint (Component parent, Printable what_to_print, int x_dialog_pos, int y_dialog_pos)
    {
        String msg;
        
        msg = doPrint (what_to_print, x_dialog_pos, y_dialog_pos);
        if (msg != null)
            JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /** print the canvas - show the printer selection dialog first
     * @param what_to_print the object to print - this object must implement the
     * Printable interface
     * @return an erro message OR null if the print worked OK */
    public String doPrint (Printable what_to_print)
    {
        return doPrint (what_to_print, 200, 200);
    }
    
    /** print the canvas - show the printer selection dialog first
     * @param what_to_print the object to print - this object must implement the
     *        Printable interface
     * @param x_dialog_pos the x position for the print dialog
     * @param y_dialog_pos the y position for the print dialog
     * @return an erro message OR null if the print worked OK */
    public String doPrint (Printable what_to_print, int x_dialog_pos, int y_dialog_pos)
    {
        DocPrintJob print_job;
        DocAttributeSet doc_attr_set;
        Doc document;
        PrintService print_service;


        // check that some printers are defined
        if (print_service_array == null || print_service_array.length <= 0 || default_print_service == null)
            return "No printers are set up on this computer";
            
        // this code is copied from http://www-106.ibm.com/developerworks/java/library/j-mer0322/
        // and                      http://www-106.ibm.com/developerworks/java/library/j-mer0424.html#2
        print_service = ServiceUI.printDialog (null, x_dialog_pos, y_dialog_pos,
                                               print_service_array, default_print_service, 
                                               print_doc_flavor, print_request_attr_set);
        if (print_service != null)
        {
          print_job = print_service.createPrintJob();
          doc_attr_set = new HashDocAttributeSet();
          document = new SimpleDoc(what_to_print, print_doc_flavor, doc_attr_set);
          try 
          {
            print_job.print (document, print_request_attr_set); 
            default_print_service = print_service;
          }
          catch (PrintException pe) { return Utils.formatExceptionMessage(pe); }
        }
        
        return null;
    }
}
