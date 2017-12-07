/*
 * GeoString.java
 *
 * Created on 21 April 2004, 15:02
 */

package bgs.geophys.library.Misc;

import bgs.geophys.library.Maths.BGSMath;

/**
 * Some string utilities that are not in the standard Java runtime.
 *
 * @author  bba
 */
public class GeoString 
{
    
    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @return the formatted number string */
    public static String formatAsInteger (int number, int width)
    {
        if (width < 0)
            return fix (Integer.toString (number), - width, false, false);
        return fix (Integer.toString (number), width, true, false);
    }
    
    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @return the formatted number string */
    public static String formatAsInteger (long number, int width)
    {
        if (width < 0)
            return fix (Long.toString (number), width, false, false);
        return fix (Long.toString (number), width, true, false);
    }
    
    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @return the formatted number string */
    public static String formatAsInteger (float number, int width)
    {
        return formatAsInteger (Math.round(number), width);
    }
    
    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @param round_method the rounding method to use (from BGSMath)
     * @return the formatted number string */
    public static String formatAsInteger (float number, int width, int round_method)
    {
        return formatAsInteger (BGSMath.round(number, round_method), width);
    }
    
    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @return the formatted number string */
    public static String formatAsInteger (double number, int width)
    {
        return formatAsInteger (Math.round(number), width);
    }

    /** format a number as an integer to a specified width
     * @param number the number to format
     * @param width - length of the output field, +ve => pad with spaces to the left
     *                                                   or trim to the left
     *                                            -ve => pad with spaces to the right
     *                                                   or trim to the right
     * @param round_method the rounding method to use (from BGSMath)
     * @return the formatted number string */
    public static String formatAsInteger (double number, int width, int round_method)
    {
        return formatAsInteger (BGSMath.round(number, round_method), width);
    }

    /** fix a string at a given width by padding or truncating 
     * @param string the string to fix
     * @param width the new width
     * @param padLeft if true add padding to the left, otherwise add to the right
     * @param truncateLeft if true truncate to the left, otherwise truncate to the right
     * @return the new string */
    public static String fix (String string, int width, 
                              boolean padLeft, boolean truncateLeft)
    {
        return pad (truncate (string, width, truncateLeft),
                    width,
                    padLeft,
                    ' ');
    }
    
    /** fix a string at a given width by padding or truncating 
     * @param string the string to fix
     * @param width the new width
     * @param padLeft if true add padding to the left, otherwise add to the right
     * @param truncateLeft if true truncate to the left, otherwise truncate to the right
     * @param padChar the character to pad with
     * @return the new string */
    public static String fix (String string, int width, 
                              boolean padLeft, boolean truncateLeft,
                              char padChar)
    {
        return pad (truncate (string, width, truncateLeft),
                    width,
                    padLeft,
                    padChar);
    }
    
    /** truncate a string to a given width
     *  if the string is shorter than the given width, return it unchanged
     * @param string the string to truncate
     * @param width the new width
     * @param truncateLeft if true truncate to the left, otherwise truncate to the right
     * @return the new string */
    public static String truncate (String string, int width, boolean truncateLeft)
    {
        // check for string to short to need truncating
        if (width >= string.length()) return string;
        
        // truncate from the left ??
        if (truncateLeft) return string.substring (string.length() - width);
        else return string.substring (0, width);
    }
    
    /** pad a string to a given width
     *  if the string is longer than the given width, return it unchanged
     * @param string the string to pad
     * @param width the new width
     * @param padLeft if true add padding to the left, otherwise add to the right
     * @return the new string */
    public static String pad (String string, int width, boolean padLeft)
    {
        // check for string too long to need padding
        return pad (string, width, padLeft, ' ');
    }
    
    /** pad a string to a given width
     *  if the string is longer than the given width, return it unchanged
     * @param string the string to pad
     * @param width the new width
     * @param padLeft if true add padding to the left, otherwise add to the right
     * @param padChar the character to pad with
     * @return the new string */
    public static String pad (String string, int width, boolean padLeft, char padChar)
    {
        int padLength, count;
        StringBuffer paddedSpace;
        
        // check for string too long to need padding
        padLength = width - string.length();
        if (padLength <= 0) return string;
        
        // create the padding
        paddedSpace = new StringBuffer (padLength);
        for (count=0; count<padLength; count++) paddedSpace.append(padChar);
        
        // left or right padding ??
        if (padLeft) return new String (paddedSpace) + string;
        return string + paddedSpace;
    }
    
    /** remove tags (like html) from a string */
    public static String removeTags (String string)
    {
        int start_index, end_index;
        String remainder;
        
        while ((start_index = string.indexOf('<')) >= 0)
        {
            if (start_index > 0) remainder = string.substring(0, start_index);
            else remainder = "";
            
            end_index = string.indexOf('>', start_index);
            if (end_index >= 0)
            {
                end_index ++;
                if (end_index < string.length())
                    remainder += string.substring (end_index);
            }
            
            string = remainder;
        }
        
        return string;
    }
}

    
