/*
 * CDErrorList.java
 *
 * Created on 03 May 2002, 21:30
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.util.Vector;
import java.io.File;

/**
 * A class that holds errors encountered during processing a
 * CD database - the idea is to allow the user to correct
 * the errors from reading this list.
 *
 * @author  smf
 * @version 
 */
public class CDErrorList extends Object 
{

  // codes for different types of error - keep in increasing order of severity
  /** code for an information message */
  public static final int INFORMATION_MESSAGE = 1;
  /** code for a warning message */
  public static final int WARNING_MESSAGE = 2;
  /** code for an error message */
  public static final int ERROR_MESSAGE = 3;
  
  // a class to hold a single error message
  private class CDError extends java.lang.Object 
  {
    public int severity;    //  one of the error severity codes
    public String message;  // the error message
    public File file;       // the file associated with this error (or none)
    public int year;        // the year that was being loaded when the error occurred
                            // set -ve for no year
    
    public CDError (int severity, String message, File file, int year)
    {
      this.severity = severity;
      this.message = message;
      this.file = file;
      this.year = year;
    }
  }

  // an array of error messages
  private Vector<CDError> error_list;
  
  // counts of the different types of error messages
  private int n_information_messages;
  private int n_warning_messages;
  private int n_error_messages;
  
  /** Creates new CDErrorList */
  public CDErrorList() 
  {
    error_list = new Vector<CDError> ();
    n_information_messages = n_warning_messages = n_error_messages = 0;
  }

  /** Add an error message
   * @param severity the severity code
   * @param message the message to add */
  public void Add (int severity, String message)
  {
    Add (severity, message, null, -1);
  }
  
  /** Add an error message with an associated file
   * @param severity the severity code
   * @param message the message to add
   * @param file the associated file */
  public void Add (int severity, String message, File file)
  {
    Add (severity, message, file, -1);
  }

  /** Add an error message with an associated file
   * @param severity the severity code
   * @param message the message to add
   * @param year the year that was being loaded when the error occurred */
  public void Add (int severity, String message, int year)
  {
    Add (severity, message, null, year);
  }
  
  /** Add an error message with an associated file
   * @param severity the severity code
   * @param message the message to add
   * @param file the associated file
   * @param year the year that was being loaded when the error occurred */
  public void Add (int severity, String message, File file, int year)
  {
    switch (severity)
    {
    case INFORMATION_MESSAGE: n_information_messages ++; break;
    case WARNING_MESSAGE:     n_warning_messages ++; break;
    case ERROR_MESSAGE:       n_error_messages ++; break;
    default:                  return;
    }
    error_list.add (new CDErrorList.CDError (severity, message, file, year));
  }

  /** get the total number of messages
   * @return the number of messages */
  public int GetNErrorMessages ()
  {
    return error_list.size ();
  }

  /** get the number of messages with a severity equal to or greater than that given
   * @param severity the severity to test against
   * @return the number of messages */
  public int GetNErrorMessages (int severity)
  {
    int total;
    
    total = 0;
    switch (severity)
    {
    // there should be no breaks in the following case statements as the
    // error message number should accumulate
    case INFORMATION_MESSAGE: total += n_information_messages;
    case WARNING_MESSAGE:     total += n_warning_messages;
    case ERROR_MESSAGE:       total += n_error_messages;
    }
    return total;
  }
  
  /** get an error message
   * @param severity the severity - messages of this severity or greater
   * will be returned
   * @param index the number of the message to retrieve
   * @return the error message OR null if the index is wrong or
   *         the message's severity is too low */
  public String GetErrorMessage (int severity, int index)
  {
    String string;
    CDErrorList.CDError error;

    try
    {
      error = (CDErrorList.CDError) error_list.elementAt(index);
      if (error.severity >= severity)
      {
        switch (error.severity)
        {
        case INFORMATION_MESSAGE: string = "Information: "; break;
        case WARNING_MESSAGE:     string = "Warning: ";     break;
        case ERROR_MESSAGE:       string = "Error: ";       break;
        default:                  string = "Unknown: ";     break;
        }
        string += error.message;
        if (error.year >= 0) string += ", year = " + Integer.toString (error.year);
        if (error.file != null) string += " (" + error.file.toString () + ")";
        return string;
      }
    }
    catch (ArrayIndexOutOfBoundsException e) { }
    return null;
  }

}
