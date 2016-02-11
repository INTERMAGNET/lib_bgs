/*
 * AnnotationString.java
 *
 * Created on 25 August 2003, 18:31
 */

package bgs.geophys.library.RealTimeData;

import java.util.*;
import java.text.*;

import bgs.geophys.library.Misc.*;

/***************************************************************************
 * The AnnotationString class holds an annotation string. Tokens in the
 * string are converted to SDAS data values at construction time
 *
 * @author  S. Flower
 * @version 0.0
 ***************************************************************************/
public class AnnotationString
{
  // private members
  String raw_string;
  String formatted_string [];
  
  /**************************************************************************
   * Contruct an AnnotationString to contain data annotation.
   * 
   * @param string  the raw annotation string
   * @param range the range of the associated data
   * @param channel the SDAS channel number
   * @param channel_name the SDAS channel name
   * @param channel_type the SDAS channel type
   * @param channel_units the physical units for the data
   *************************************************************************/
  public AnnotationString(String string, Range range, int channel, String channel_name, String channel_type, String channel_units)
  {
    int n_lines, count;
    String line;
    StringTokenizer lines;


    // take a copy of the raw string
    raw_string = string;
    
    // divide the string into lines - be sure to add \r to the list of
    // delimiters, as javascript adds a \r to the end of each line of test
    // in a text area
    lines = new StringTokenizer (string, "\n\r", false);
    n_lines = lines.countTokens ();

    // allocate space for the formatted string
    formatted_string = new String [n_lines];

    // create the lines of the formatted string
    for (count=0; count<n_lines; count++)
    {
      line = lines.nextToken ();
      formatted_string [count] = substituteAnnotation (line, range, channel, channel_name,
                                                       channel_type, channel_units);
    }
    
  }

  /**************************************************************************
   * Contruct an AnnotationString to contain header annotation.
   * 
   * @param string  the raw annotation string
   * @param date the start date/time for the data
   * @param duration the data duration in milliseconds
   * @param current_data flag - true means data is most recent, false for historical data
   *************************************************************************/
  public AnnotationString(String string, Date date, int duration, boolean current_data)
  {
    int n_lines, count;
    String line;
    StringTokenizer lines;


    // divide the string into lines - be sure to add \r to the list of
    // delimiters, as javascript adds a \r to the end of each line of test
    // in a text area
    lines = new StringTokenizer (string, "\n\r", false);
    n_lines = lines.countTokens ();

    // allocate space for the formatted string
    formatted_string = new String [n_lines];

    // create the lines of the formatted string
    for (count=0; count<n_lines; count++)
    {
      line = lines.nextToken ();
      formatted_string [count] = substituteHeader (line, date, duration, current_data);
    }
    
  }

  /**************************************************************************
   * Contruct a copy of an annotation string
   * 
   * @param source  the existing annotation string
   *************************************************************************/
  public AnnotationString(AnnotationString source)
  {
    int n_lines, count;
    
    n_lines = source.getNLines ();
    formatted_string = new String [n_lines];
    for (count=0; count<n_lines; count++) formatted_string[count] = new String (source.getLine (count));
    raw_string = source.GetRawString ();
    
  }
  
  /**************************************************************************
   * Contruct an AnnotationString to contain a plain message.
   * 
   * @param string  the raw annotation string
   *************************************************************************/
  public AnnotationString(String string)

  {
    
    int n_lines, count;
    StringTokenizer lines;

    
    // divide the string into lines - be sure to add \r to the list of
    // delimiters, as javascript adds a \r to the end of each line of test
    // in a text area
    lines = new StringTokenizer (string, "\n\r", false);
    n_lines = lines.countTokens ();

    // allocate space for the formatted string
    formatted_string = new String [n_lines];

    // create the lines of the formatted string
    for (count=0; count<n_lines; count++) formatted_string [count] = lines.nextToken ();
    
  }
  
  /**************************************************************************
   * Contruct an empty AnnotationString
   *************************************************************************/
  public AnnotationString()

  {
    formatted_string = new String [1];
    formatted_string [0] = new String ("");
  }

  /*************************************************************************
   * Get the raw annotation string
   *
   * @return the raw annotation string
   *************************************************************************/
  public String GetRawString ()
  {
    return raw_string;
  }
  
  /*************************************************************************
   * Get the number of lines in the annotation string
   * 
   * @return the number of lines
   *************************************************************************/
  public int getNLines ()
  {
    return formatted_string.length;
  }
  
  /*************************************************************************
   * Get a formatted line.
   * 
   * @param line_no the line number (0..n_lines-1)
   * @return the line
   *************************************************************************/
  public String getLine (int line_no)
  {
    if (line_no < 0 || line_no >= formatted_string.length) return "";
    return formatted_string [line_no];
  }

  /**************************************************************************
   * Substitute tokens in an annotation string.
   * 
   * @param raw_string the string to substitute from
   * @param data_range the range of the associated data
   * @param channel the SDAS channel number
   * @param channel_name the SDAS channel name
   * @param channel_type the SDAS channel type
   * @param channel_units the physical units for the data
   * @return the substituted string
   *************************************************************************/
  private String substituteAnnotation (String raw_string, Range data_range,
                                       int channel, String channel_name,
                                       String channel_type, String channel_units)
  {
    
    int current_pos, start_index, end_index;
    String token;
    StringBuffer accumulator;
    
    
    accumulator = new StringBuffer ();
    for (current_pos = 0; current_pos < raw_string.length (); )
    {
      start_index = raw_string.indexOf ('<', current_pos);
      if (start_index < 0)
      {
        accumulator.append (raw_string.substring (current_pos));
        current_pos = raw_string.length ();
      }
      else
      {
        if (start_index > current_pos)
          accumulator.append (raw_string.substring (current_pos, start_index));
        end_index = raw_string.indexOf ('>', start_index);
        if (end_index <= start_index)
        {
          accumulator.append ('<');
          current_pos ++;
        }
        else
        {
          token = raw_string.substring (start_index +1, end_index);
          if (token.equals ("min"))
            accumulator.append (data_range.get_min ());
          else if (token.equals ("max"))
            accumulator.append (data_range.get_max ());
          else if (token.equals ("range"))
            accumulator.append (data_range.get_range ());
          else if (token.equals ("average"))
            accumulator.append (data_range.get_average ());
          else if (token.equals ("channel-number"))
            accumulator.append (Integer.toString (channel));
          else if (token.equals ("station-name"))
            accumulator.append (channel_name);
          else if (token.equals ("channel-type"))
            accumulator.append (channel_type);
          else if (token.equals ("units"))
            accumulator.append (channel_units);
          else
          {
            accumulator.append ('<');
            accumulator.append (token);
            accumulator.append ('>');
          }
          current_pos = end_index +1;
        }
      }
    }

    return new String (accumulator);
    
  }

  /**************************************************************************
   * Substitute tokens in a header string.
   * 
   * @param raw_string the string to substitute from
   * @param date the start date/time for the data
   * @param duration the data duration in milliseconds
   * @param current_data flag - true means data is most recent, false for historical data
   * @return the substituted string
   *************************************************************************/
  private String substituteHeader (String raw_string, Date date,
                                   int duration, boolean current_data)
  
  {
    
    int current_pos, start_index, end_index;
    String token, string;
    StringBuffer accumulator;
    SimpleDateFormat dateFormat, timeFormat;
    SimpleTimeZone gmt;
    Date start_date, end_date;
    GregorianCalendar start_calendar, end_calendar;

    

    dateFormat = new SimpleDateFormat ("dd-MMM-yyyy");
    timeFormat = new SimpleDateFormat ("H:m:s");
    gmt = new SimpleTimeZone (0, "GMT");
    dateFormat.setTimeZone (gmt);
    timeFormat.setTimeZone (gmt);
    
    start_date = date;
    end_date = new Date (start_date.getTime () + (long) duration);
    start_calendar = new GregorianCalendar (TimeZone.getTimeZone ("GMT"));
    start_calendar.setTime (start_date);
    end_calendar = new GregorianCalendar (TimeZone.getTimeZone ("GMT"));
    end_calendar.setTime (end_date);
    
    accumulator = new StringBuffer ();
    for (current_pos = 0; current_pos < raw_string.length (); )
    {
      start_index = raw_string.indexOf ('<', current_pos);
      if (start_index < 0)
      {
        accumulator.append (raw_string.substring (current_pos));
        current_pos = raw_string.length ();
      }
      else
      {
        if (start_index > current_pos)
          accumulator.append (raw_string.substring (current_pos, start_index));
        end_index = raw_string.indexOf ('>', start_index);
        if (end_index <= start_index)
        {
          accumulator.append ('<');
          current_pos ++;
        }
        else
        {
          token = raw_string.substring (start_index +1, end_index);
          if (token.equals ("start-date"))
            accumulator.append (dateFormat.format (start_date));
          else if (token.equals ("start-time"))
            accumulator.append (timeFormat.format (end_date));
          else if (token.equals ("end-date"))
            accumulator.append (dateFormat.format (end_date));
          else if (token.equals ("end-time"))
            accumulator.append (timeFormat.format (end_date));
          else if (token.equals ("to-end-date"))
          {
            if (start_calendar.get (Calendar.DAY_OF_MONTH) != end_calendar.get (Calendar.DAY_OF_MONTH) ||
                start_calendar.get (Calendar.MONTH) !=        end_calendar.get (Calendar.MONTH) ||
                start_calendar.get (Calendar.YEAR) !=         end_calendar.get (Calendar.YEAR))
              accumulator.append ("to " + dateFormat.format (end_date));
          }
          else if (token.equals ("current-data"))
            accumulator.append (current_data ? "(current data)" : "(historical data)");
          else
          {
            accumulator.append ('<');
            accumulator.append (token);
            accumulator.append ('>');
          }
          current_pos = end_index +1;
        }
      }
    }
  
    return new String (accumulator);
    
  }

}
