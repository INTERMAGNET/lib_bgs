/*
 * DateUtils.java
 *
 * Created on 29 October 2003, 09:28
 */

package bgs.geophys.library.Misc;

import java.text.*;
import java.util.*;

/**
 * Utility routines for working with dates
 *
 * @author  smf
 */
public class DateUtils 
{
    
  // some time constants
  /** the number of milliseconds in a minute */
  public static final long MILLISECONDS_PER_MINUTE = 60000l;
  /** the number of milliseconds in an hour */
  public static final long MILLISECONDS_PER_HOUR = 3600000l;
  /** the number of milliseconds in a day */
  public static final long MILLISECONDS_PER_DAY = 86400000l;
  /** the number of seconds in a minute */
  public static final int SECONDS_PER_MINUTE = 60;
  /** the number of seconds in an hour */
  public static final int SECONDS_PER_HOUR = 3600;
  /** the number of seconds in a day */
  public static final int SECONDS_PER_DAY = 86400;

  /** code for month name case: upper case */
  public static final int MONTH_UPPERCASE = 1;
  /** code for month name case: lower case */
  public static final int MONTH_LOWERCASE = 2;
  /** code for month name case: lower case with capitalized first letter */
  public static final int MONTH_UPPERFIRSTONLY = 3;
  
  /** code for units of a duration: milliseconds */
  public static final int DURATION_TYPE_MILLISECONDS = 1;
  /** code for units of a duration: seconds */
  public static final int DURATION_TYPE_SECONDS = 2;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_MINUTES = 3;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_HOURS = 4;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_DAYS = 5;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_WEEKS = 6;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_MONTHS = 7;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_YEARS = 8;
  /** code for units of a duration:  */
  public static final int DURATION_TYPE_INFINITE = 9;
  
  /** a timezone representing GMT */
  public static TimeZone gmtTimeZone;
  
  /** a set of date format symbols for the US/UK locale */
  public static DateFormatSymbols english_date_format_symbols;

  // static variable initialisation
  static
  {
      gmtTimeZone = TimeZone.getTimeZone("gmt");
      
      try { english_date_format_symbols = new DateFormatSymbols (Locale.UK); }
      catch (MissingResourceException e) { english_date_format_symbols = null; }
      if (english_date_format_symbols == null)
      {
        try { english_date_format_symbols = new DateFormatSymbols (Locale.US); }
        catch (MissingResourceException e) { english_date_format_symbols = null; }
      }
      if (english_date_format_symbols == null) 
          english_date_format_symbols = new DateFormatSymbols ();
  }

  /** create a date from time and date fields (assumes date is GMT based)
   * @param year the four digit year - this method copes correctly with years
   *        before 1970
   * @param month the month (0..11)
   * @param day the day (0..30)
   * @param hour the hour (0..23) 
   * @param min the minute (0..59)
   * @param sec the second (0..59)
   * @param milli the millisecond (0..999)
   * @return the date */
  public static Date createDate (int year, int month, int day, 
                                 int hour, int min, int sec, int milli)
  {
      long duration;
      
      duration = (long) milli +
                 ((long) sec * 1000l) +
                 ((long) min * 60000l) +
                 ((long) hour * 3600000l) +
                 ((long) day * 86400000l) +
                 getDurationMs ((long) month, DURATION_TYPE_MONTHS, 0, year) +
                 getDurationMs ((long) (year - 1970), DURATION_TYPE_YEARS, 0, 1970);
      return new Date (duration);
  }
  
  /** get a duration in milliseconds from a duration in a number of other
   * time units
   * @param duration the duration
   * @param durationUnits the units for the duration - one of the
   *        DURATION_TYPE_... codes above
   * @param month the month that the duration starts from (0..11) - only needed for
   *        duration type MONTH
   * @param year the year that the duration starts from - only needed for
   *        duration types MONTH and YEAR
   * @return the duration in milliseconds */
  public static long getDurationMs (long duration, int durationUnits, 
                                    int month, int year)
  {
      int count, n_months, n_years;
      
      switch (durationUnits)
      {
          case DURATION_TYPE_SECONDS:
              return duration * 1000l;
          case DURATION_TYPE_MINUTES:
              return duration * 60000l;
          case DURATION_TYPE_HOURS:
              return duration * 3600000l;
          case DURATION_TYPE_DAYS:
              return duration * 86400000l;
          case DURATION_TYPE_WEEKS:
              return duration * 604800000l;
          case DURATION_TYPE_MONTHS:
              n_months = (int) duration;
              duration = 0;
              // NBNBNB - this loop accumulates duration in DAYS, not milliseconds
              for (count = 0; count < n_months; count++)
              {
                  duration += daysInMonth (month, year);
                  month ++;
                  if (month >= 12)
                  {
                      month = 0;
                      year ++;
                  }
              }
              return duration * 86400000l;
          case DURATION_TYPE_YEARS:
              n_years = (int) duration;
              duration = 0;
              if (n_years >= 0)
              {
                  for (count = 0; count < n_years; count++)
                      duration += DateUtils.isLeapYear(year ++) ? 31622400000l : 31536000000l;
              }
              else
              {
                  for (count = 0; count > n_years; count--)
                      duration -= DateUtils.isLeapYear(year --) ? 31622400000l : 31536000000l;
              }
              return duration;
      }
      
      // if the duration type is not known (or is milliseconds) return the duration unchanged
      return duration;
  }
  
  /** get the name of the given month
   * @param month the month to translate (0..11)
   * @param caseType one of the MONTH_ case codes
   * @param width the maximum number of characters to return OR
   *        -1 for the full length of each name */
  public static String getMonthName (int month, int caseType, int width)
  {
      String string;
      
      switch (month)
      {
      case 0: string = "January"; break;
      case 1: string = "February"; break;
      case 2: string = "March"; break;
      case 3: string = "April"; break;
      case 4: string = "May"; break;
      case 5: string = "June"; break;
      case 6: string = "July"; break;
      case 7: string = "August"; break;
      case 8: string = "September"; break;
      case 9: string = "October"; break;
      case 10: string = "November"; break;
      case 11: string = "December"; break;
      default: return null;
      }
      
      switch (caseType)
      {
      case MONTH_UPPERCASE: string = string.toUpperCase(); break;
      case MONTH_LOWERCASE: string = string.toLowerCase(); break;
      }
      
      if (width < 0) return string;
      if (width >= string.length()) return string;
      return string.substring (0, width);
  }
  
  /** is this a leap year
   * @param year the year to test
   * @return true for a leap year */
  public static boolean isLeapYear (int year)
  {
    if ((year % 4) != 0) return false;
    if ((year % 100 == 0) && (year % 400 != 0)) return false;
    return true;
  }
  
  /**********************************************
   * daysYear - find the number of days in the
   * given year
   *
   * @param year the year to use
   * @return - the number of days in the year
   **********************************************/
  public static int daysInYear (int year)
  {
    if (isLeapYear(year)) return 366;
    return 365;
  }

  /**********************************************
   * daysInMonth - find the number of days in the
   * given month
   *
   * @param month the month to use (0..11)
   * @param year the year to use
   * @return - the number of days in the month
   **********************************************/
  public static int daysInMonth (int month, int year)
  {
    int days[] = {31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    
    if (isLeapYear(year)) days [1] = 29;
    else days[1] = 28;
    return days [month];
  }

  /** parse a date string into a date object
   * @params string a string containing one of:
   *         1.) between 3 and 6 fields in the order day, month, year, hour, minute, second.
   *         2.) between 3 and 6 fields in the order year, month, day, hour, minute, second.
   *         Missing fields (hour, mintute or second) will be set to 0.
             Allow any delimiter at all except '&'.
   * @return the date OR null if there was an error */
  public static Date parseDate (String string)
  {
      int date_numbers [];
      DecimalFormat integer2_format, integer4_format;
      SimpleDateFormat date_format;
      Date test_date;

      // check that the values are all numeric,
      // except possibly the month, then reformat the date/time to correspond
      // to the SimpleDateFormat specification below
      try
      {
          date_numbers = parseDate2 (string);
          
          // parse the newly formatted string
          integer2_format = new DecimalFormat ("00");
          integer4_format = new DecimalFormat ("0000");
          date_format = new SimpleDateFormat ("yyyy MM dd HH mm ss");
          date_format.setTimeZone (gmtTimeZone);
          test_date = date_format.parse (integer4_format.format ((long) date_numbers [0]) + " " +
                                         integer2_format.format ((long) date_numbers [1]) + " " +
                                         integer2_format.format ((long) date_numbers [2]) + " " +
                                         integer2_format.format ((long) date_numbers [3]) + " " +
                                         integer2_format.format ((long) date_numbers [4]) + " " +
                                         integer2_format.format ((long) date_numbers [5]));
      }
      catch (NoSuchElementException e) { return null; }
      catch (NumberFormatException e) { return null; }
      catch (ParseException e) { return null; }
      
      return test_date;
  }
    
  /** parse a date string into a date object (as before, but handles 2 digit years)
   * @params string a string containing one of:
   *         1.) between 3 and 6 fields in the order day, month, year, hour, minute, second.
   *         2.) between 3 and 6 fields in the order year, month, day, hour, minute, second.
   *         Year may be 2 digit (but if it is less than 31, the software cannot
   *         tell the difference between the two field orderings, so yy-mm-dd is assumed).
   *         Missing fields (hour, mintute or second) will be set to 0.
             Allow any delimiter at all except '&'.
   * @params year2000split - year which separates 1900 (as a 2 digit year) from 2000
   * @return the date OR null if there was an error */
  public static Date parseDate (String string, int year2000split)
  {
      int date_numbers [];
      DecimalFormat integer2_format, integer4_format;
      SimpleDateFormat date_format;
      Date test_date;

      // check that the values are all numeric,
      // except possibly the month, then reformat the date/time to correspond
      // to the SimpleDateFormat specification below
      try
      {
          date_numbers = parseDate2 (string);

          // adjust 2 digit years
          if (date_numbers [0] < year2000split) date_numbers [0] += 2000;
          else if (date_numbers [0] < 100) date_numbers [0] += 1900;
          
          // parse the newly formatted string
          integer2_format = new DecimalFormat ("00");
          integer4_format = new DecimalFormat ("0000");
          date_format = new SimpleDateFormat ("yyyy MM dd HH mm ss");
          date_format.setTimeZone (gmtTimeZone);
          test_date = date_format.parse (integer4_format.format ((long) date_numbers [0]) + " " +
                                         integer2_format.format ((long) date_numbers [1]) + " " +
                                         integer2_format.format ((long) date_numbers [2]) + " " +
                                         integer2_format.format ((long) date_numbers [3]) + " " +
                                         integer2_format.format ((long) date_numbers [4]) + " " +
                                         integer2_format.format ((long) date_numbers [5]));
      }
      catch (NoSuchElementException e) { return null; }
      catch (NumberFormatException e) { return null; }
      catch (ParseException e) { return null; }
      
      return test_date;
  }
  
  /** parse a date string into a date object
   * @params string a string containing between 3 and 6 fields in one of the 
   *         orders:
   *           year, month, day, hour, minute, second
   *           day, month, year, hour, minute, second
   *         Missing fields (hour, mintute or second) will be set to 0.
   *         Allow any delimiter at all except '&'.
   * @return the date as six integer values, in order year, month, day, hour, min, sec
   * @throws NoSuchElementException if the sering is badly formatted
   * @throws NumberFormatException if the sering is badly formatted */
  public static int [] parseDate2 (String string)
  throws NoSuchElementException, NumberFormatException
  {
      int count, swap;
      StringTokenizer date_tokens;
      String date_fields [];
      int date_numbers [];

      // split the string into tokens
      date_tokens = new StringTokenizer (string, " !#%()*+,-./:;<=>?|\\[]^_{}~", false);
      if (date_tokens.countTokens() < 3) throw (new NoSuchElementException ());
      date_fields = new String [6];
        
      // get the year, month and day strings - convert the month to uppercase incase it is a month name
      date_fields [0] = date_tokens.nextToken();
      date_fields [1] = date_tokens.nextToken().toUpperCase();
      date_fields [2] = date_tokens.nextToken();
        
      // get the time strings, using defaults of 0 for missing times
      if (date_tokens.hasMoreTokens()) date_fields [3] = date_tokens.nextToken();
      else date_fields [3] = "0";
      if (date_tokens.hasMoreTokens()) date_fields [4] = date_tokens.nextToken();
      else date_fields [4] = "0";
      if (date_tokens.hasMoreTokens()) date_fields [5] = date_tokens.nextToken();
      else date_fields [5] = "0";
      
      // process string month names
      if (date_fields [1].startsWith ("JAN")) date_fields [1] = "1";
      else if (date_fields [1].startsWith ("FEB")) date_fields [1] = "2";
      else if (date_fields [1].startsWith ("MAR")) date_fields [1] = "3";
      else if (date_fields [1].startsWith ("APR")) date_fields [1] = "4";
      else if (date_fields [1].startsWith ("MAY")) date_fields [1] = "5";
      else if (date_fields [1].startsWith ("JUN")) date_fields [1] = "6";
      else if (date_fields [1].startsWith ("JUL")) date_fields [1] = "7";
      else if (date_fields [1].startsWith ("AUG")) date_fields [1] = "8";
      else if (date_fields [1].startsWith ("SEP")) date_fields [1] = "9";
      else if (date_fields [1].startsWith ("OCT")) date_fields [1] = "10";
      else if (date_fields [1].startsWith ("NOV")) date_fields [1] = "11";
      else if (date_fields [1].startsWith ("DEC")) date_fields [1] = "12";
      
      // convert all fields to integers
      date_numbers = new int [6];
      for (count=0; count<6; count++) date_numbers[count] = Integer.parseInt(date_fields [count]);

      // check if the year is in the third field - if so swap it
      if (date_numbers[2] > 31) 
      {
          swap = date_numbers [0];
          date_numbers [0] = date_numbers [2];
          date_numbers [2] = swap;
      }
        
      return date_numbers;
  }
  
  /** quick and dirty date formatting
   * @param year the four digit year
   * @param month the month (0..11)
   * @param day the day (0..30) */
  public static String formatDate (int day, int month, int year)
  {
      return formatDate (day, month, year, 3);
  }
  
  /** quick and dirty date formatting
   * @param year the four digit year
   * @param month the month (0..11)
   * @param day the day (0..30) 
   * @param n_fields number of fields to display - 1 = year only, 2 = year and month
   *        3 = all three fields */
  public static String formatDate (int day, int month, int year, int n_fields)
  {
      if (n_fields <= 1) return Integer.toString (year);
      if (n_fields == 2) return getMonthName (month, MONTH_UPPERFIRSTONLY, 3) + " " + 
                              Integer.toString (year);
      return GeoString.pad (Integer.toString (day +1), 2, true, '0') + "-" +
             getMonthName (month, MONTH_UPPERFIRSTONLY, 3) + "-" +
             Integer.toString (year);
  }
    
}
