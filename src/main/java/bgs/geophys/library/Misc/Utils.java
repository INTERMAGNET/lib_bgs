/*
 * Utils.java
 *
 * Created on 17 March 2003, 10:51
 */

package bgs.geophys.library.Misc;

import java.awt.event.*;
import java.net.URL;
import java.text.*;
import java.util.*;
import javax.swing.*;

/**
 * General, static utilities (functions)
 * @author  djsco
 */
public class Utils
{

  private static java.text.DecimalFormatSymbols decimal_format_symbols = null;
    
  /** A function to find a file stored in the class hierarchy. This function
   * was designed to allow graphics files to be stored alongside java class
   * files, but could be used for any type of file. The filename will be converted
   * as follows:
   *     filename     <classpath>/<package-path>/filename
   *     /filename    <classpath>/filename
   * where package-path points to the package holding the Main object.
   * @param filename the name of the file to find
   * @param reference an object used to reference the file from - the
   *        path to the object will be used as <package-path>
   * @return a reference to the file in the form of a URL or null if it could not be found */
  public static URL findFileFromClass (String filename, Object reference)
  {
    URL url;
    Class class_obj;
    
    try
    {
      class_obj = reference.getClass();
      url = class_obj.getResource (filename);
      return url;
    }
    catch (Exception e) { }
    return null;
  }

  /********************************************************************
   * bytesToInt - convert an array of 4 bytes into an integer
   *
   * @params bytes[] the array of bytes in Intel order (least
   *                 significant byte first)
   * @return the integer represented by the byte array
   ********************************************************************/
  public static int bytesToInt (byte bytes[])
  {
    int i0, i1, i2, i3;

    i0 = (bytes[0] >= 0)? bytes[0] : (bytes[0] + 256);
    i1 = (bytes[1] >= 0)? bytes[1] : (bytes[1] + 256);
    i2 = (bytes[2] >= 0)? bytes[2] : (bytes[2] + 256);
    i3 = (bytes[3] >= 0)? bytes[3] : (bytes[3] + 256);

    return (i3 << 24) + (i2 << 16) + (i1 << 8) + i0;
  }

  public static int bytesToInt (byte bytes[], int offset)
  {
    int i0, i1, i2, i3;

    i0 = (bytes[offset + 0] >= 0)? bytes[offset + 0] : (bytes[offset + 0] + 256);
    i1 = (bytes[offset + 1] >= 0)? bytes[offset + 1] : (bytes[offset + 1] + 256);
    i2 = (bytes[offset + 2] >= 0)? bytes[offset + 2] : (bytes[offset + 2] + 256);
    i3 = (bytes[offset + 3] >= 0)? bytes[offset + 3] : (bytes[offset + 3] + 256);

    return (i3 << 24) + (i2 << 16) + (i1 << 8) + i0;
  }

  public static int bytesToInt (byte b0, byte b1, byte b2, byte b3)
  {
    int i0, i1, i2, i3;

    i0 = (b0 >= 0)? b0 : (b0 + 256);
    i1 = (b1 >= 0)? b1 : (b1 + 256);
    i2 = (b2 >= 0)? b2 : (b2 + 256);
    i3 = (b3 >= 0)? b3 : (b3 + 256);

    return (i3 << 24) + (i2 << 16) + (i1 << 8) + i0;
  }
  
  /********************************************************************
   * bytesToIntBE - convert an array of 4 big-endian bytes into an integer
   *
   * @params bytes[] the array of bytes in BIG ENDIAN order (most
   *                 significant byte first)
   * @return the integer represented by the byte array
   ********************************************************************/
  public static int bytesToIntBE (byte bytes [])
  {
    int i0, i1, i2, i3;

    i0 = (bytes[0] >= 0)? bytes[0] : (bytes[0] + 256);
    i1 = (bytes[1] >= 0)? bytes[1] : (bytes[1] + 256);
    i2 = (bytes[2] >= 0)? bytes[2] : (bytes[2] + 256);
    i3 = (bytes[3] >= 0)? bytes[3] : (bytes[3] + 256);

    return (i0 << 24) + (i1 << 16) + (i2 << 8) + i3;
  }

  public static int bytesToIntBE (byte bytes [], int offset)
  {
    int i0, i1, i2, i3;

    i0 = (bytes[offset + 0] >= 0)? bytes[offset + 0] : (bytes[offset + 0] + 256);
    i1 = (bytes[offset + 1] >= 0)? bytes[offset + 1] : (bytes[offset + 1] + 256);
    i2 = (bytes[offset + 2] >= 0)? bytes[offset + 2] : (bytes[offset + 2] + 256);
    i3 = (bytes[offset + 3] >= 0)? bytes[offset + 3] : (bytes[offset + 3] + 256);

    return (i0 << 24) + (i1 << 16) + (i2 << 8) + i3;
  }
    
  /** convert an integer to a byte array in Intel byte order
   * @param number the int to convert
   * @return a four element byte array */
  public static byte [] intToBytes (int number)
  {
    boolean negative;
    byte bytes [];
    
    bytes = new byte [4];
    if (number < 0)
    {
      negative = true;
      number = number & 0x7fffffff;
    }
    else negative = false;
    bytes [0] = (byte) (number & 0xff);
    bytes [1] = (byte) ((number >> 8) & 0xff);
    bytes [2] = (byte) ((number >> 16) & 0xff);
    bytes [3] = (byte) ((number >> 24) & 0x7f);
    if (negative) bytes [3] |= 0x80;
    
    return bytes;
  }

  /** reverse the byte order of the array of bytes given
   * @param buffer a buffer containing the bytes to reverse
   * @return a new buffer containing the reversed bytes */
  public static byte [] reverseBytes (byte [] buffer)
  {
    byte [] buffer2 = new byte [4];
    int n;

    for (n=0; n < 4; n++)
      buffer2 [n] = buffer [3-n];
    return buffer2;
  }

  /** reverse the byte order of the given integer
   * @param i an integer
   * @return an integer with the bytes reversed */
  public static int reverseBytes (int i)
  {
    return bytesToInt (reverseBytes (intToBytes (i)));
  }

  /** hex dump a byte array
   * @params bytes[] the array of bytes
   * @params offset the offset into the array to start at
   * @params length the length to dump
   * @return the hex dump string */
  public static String hexDumpBytes (byte bytes [], int offset, int length)
  {
    int count, number;
    String string, substring;
    
    string = "";
    for (count=0; count<length; count ++)
    {
      number = bytes [offset + count];
      if (number < 0) number += 256;
      substring = Integer.toHexString(number);
      if (string.length() > 0)
      {
        if (substring.length() < 2) string += " 0" + substring;
        else string += " " + substring;
      }
      else
      {
        if (substring.length() < 2) string += "0" + substring;
        else string += substring;
      }
    }
    return string;
  }
  
  /** parse a hex dump into a byte array
   * @params string the hex dump
   * @return the hex dump string or null if there was a parse error */
  public static byte [] parseHexDump (String string)
  {
    int count, length;
    byte bytes [];
    StringTokenizer tokens;

    try
    {
        tokens = new StringTokenizer (string);
        length = tokens.countTokens();
        if (length <= 0) throw (new NumberFormatException ());
        bytes = new byte [length];
    
        for (count=0; count<length; count ++)
        {
          bytes [count] = (byte) Integer.parseInt (tokens.nextToken(), 16);
        }
    }
    catch (NumberFormatException e) { return null; }
    return bytes;
  }
  
  /*****************************************************************************
   * make_ordinal_number
   *
   * Description: convert an ordinal number to its string equivalent
   *
   * @param number - the value to convert
   * @return - a static string representing the ordinal value
   ****************************************************************************/
  public static String make_ordinal_number (int number)
  {
    int unit, tens;
    String string;

    /* find the units and tens values from the number */
    unit = number % 10;
    tens = (number % 100) / 10;

    /* fake the unit for the teens, which are always 'th' */
    if (tens == 1) unit = 9;

    /* calculate the string */
    switch (unit)
    {
    case 1:  string = number + "st"; break;
    case 2:  string = number + "nd"; break;
    case 3:  string = number + "rd"; break;
    default: string = number + "th"; break;
    }

    return string;
  }
  
  /** Get a set of DecimalFormatSymbols (for use with the DecimalFormat object)
   * that correctly represents number formats in common scientific usage
   * @return the format symbols */
  public static java.text.DecimalFormatSymbols getScientificDecimalFormatSymbols ()
  {
      if (decimal_format_symbols == null)
      {
          decimal_format_symbols = new java.text.DecimalFormatSymbols ();
          decimal_format_symbols.setDecimalSeparator('.');
          decimal_format_symbols.setMinusSign('-');
          decimal_format_symbols.setPercent('%');
          decimal_format_symbols.setZeroDigit('0');
      }
      return decimal_format_symbols;
  }

  /** find the message attached to the lowest exception in
   * a chain of exceptions
   * @param t the top exception (which may contain nested exceptions)
   * @return the prefix + the message OR just the prefix if there are no messages*/
  public static String formatExceptionMessage (Throwable t)
  {
      return formatExceptionMessage(t.getClass().getName(), t);
  }
  
  /** find the message attached to the lowest exception in
   * a chain of exceptions
   * @param prefix a prefix for the message
   * @param t the top exception (which may contain nested exceptions)
   * @return the prefix + the message OR just the prefix if there are no messages*/
  public static String formatExceptionMessage (String prefix, Throwable t)
  {
    String msg;
        
    msg = findFinalMessage (t);
    if (msg == null) return prefix;
    return prefix + ": " + msg;
  }
    
  /** find the message attached to the lowest exception in
   * a chain of exceptions
   * @param t the top exception (which may contain nested exceptions)
   * @return the message OR null if there are no messages*/
  public static String findFinalMessage (Throwable t)
  {
    String msg;
        
    if (t.getMessage() != null) msg = t.getMessage();
    else msg = null;
    while (t.getCause() != null)
    {
      t = t.getCause();
      if (t.getMessage() != null) msg = t.getMessage();
    }
    return msg;
  }
}
