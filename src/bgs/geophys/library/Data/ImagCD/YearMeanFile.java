/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.GeomagDataFilename;
import bgs.geophys.library.Data.ImagCD.YearMeanIterator.IncludeJumps;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Misc.GeoString;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.jfree.util.StringUtils;

/**
 * Class to read and write data in INTERMAGNET yearmean format.
 * Inside this class field strengths are in nT, angles in minutes
 * 
 * @author smf
 */
public class YearMeanFile 
{

    /** width of a line in the yearmean file */
    public static final int LINE_WIDTH = 73;
    
    // private members
    private String station_code;
    private Vector<YearMean> means;
    private GregorianCalendar cal;
    private Vector<String> notes;
    
    /** create an empty year mean object */
    public YearMeanFile (String station_code)
    {
        this.station_code = station_code;
        means = new Vector<YearMean> ();
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        notes = new Vector<String> ();
    }
    
    /** fill a year mean object from a file 
     * @param file the file to read from 
     * @throws FileNotFound exception if the file could not be found
     * @throws IOException if there was a read error
     * @throws ParseException if there was an error reading data from
     *         the file, or the filename could not be understaood - the 
     *         fileame should be line: SSSSYYk.dka */
    public YearMeanFile (File file)
    throws FileNotFoundException, IOException, ParseException
    {
        int count;
        double year, x, y, z, h, d, i, f, d_mins, i_mins;
        boolean parsed_ok, found_note;
        YearMean.YearMeanType type;
        String recorded_elements, line, type_string, note_number, uc_line;
        String d_string, i_string;
        BufferedReader reader;
        YearMeanFilename yearmean_filename;
        YearMean mean, last_mean;
        int intDegreesDeclination, intDegreesInclination;
        
        // parse the filename
        yearmean_filename = new YearMeanFilename (file);
        station_code = yearmean_filename.getStationCode();
        means = new Vector<YearMean> ();
        notes = new Vector<String> ();
        
        // read the file
        reader = new BufferedReader (new FileReader (file));
        while ((line = reader.readLine()) != null)
        {
            parsed_ok = false;
            try
            {
                // split the line into columns and extract the fields - this catches fields that
                // have been left blank correctly - see Honolulu 2004 jump records for example
                // the comments show an example line:
                //   0         1         2         3         4         5         6         7
                //   01234567890123456789012345678901234567890123456789012345678901234567890123
                //   _YYYY.yyy_DDD_dd.d_III_ii.i_HHHHHH_XXXXXX_YYYYYY_ZZZZZZ_FFFFFF_A_EEEE_NNNCrLf
                //   1980.500  -8 21.3  69 18.5  17294  17110  -2513  45788  48945 A  DHZ    
                year =              extractNumber (line,  0,  9, false);
                d_string =          extractString (line, 10, 14, true);
                d =                 extractNumber (line, 10, 13, true);
                intDegreesDeclination = extractInteger(line, 10, 13, true);
                d_mins =            extractNumber (line, 14, 18, true);
                i_string =          extractString (line, 19, 23, true);
                i =                 extractNumber (line, 19, 22, true);
                intDegreesInclination = extractInteger(line, 19, 22, true);
                i_mins =            extractNumber (line, 23, 27, true);
                h =                 extractNumber (line, 28, 34, true);
                x =                 extractNumber (line, 35, 41, true);
                y =                 extractNumber (line, 42, 48, true);
                z =                 extractNumber (line, 49, 55, true);
                f =                 extractNumber (line, 56, 62, true);
                type_string =       extractString (line, 63, 64, false);
                recorded_elements = extractString (line, 65, 69, true);
                note_number =       extractString (line, 70, 73, true);

                // check for 999999 missing values as in format description Intermagnet Discussion Document DD10
                if( (int) h== 999999) h = YearMean.MISSING_ELEMENT;
                if( (int) x== 999999) x = YearMean.MISSING_ELEMENT;
                if( (int) y== 999999) y = YearMean.MISSING_ELEMENT;
                if( (int) z== 999999) z = YearMean.MISSING_ELEMENT;
                if( (int) f== 999999) f = YearMean.MISSING_ELEMENT;
                // check that angles aren't missing 
                if (d==YearMean.MISSING_DEGREES)d=YearMean.MISSING_ELEMENT;
                if (i==YearMean.MISSING_DEGREES)i=YearMean.MISSING_ELEMENT;
                if ((int) d_mins == (int) YearMean.MISSING_MINUTES) d_mins = YearMean.MISSING_ELEMENT;
                if ((int) i_mins == (int) YearMean.MISSING_MINUTES) i_mins = YearMean.MISSING_ELEMENT;
                
                // calculate minute values for d and i if values are not missing
                if(d!=YearMean.MISSING_ELEMENT){
                   // add minutes to angles
                   if (d >= 0) d = (d * 60.0) + d_mins;
                   else d = (d * 60.0) - d_mins;
                   // check for -ve angles in the form "-0 24.8"
                   // also "-0-24.8" and "0-24.8"
                   if ((d_string.startsWith("-") && d > 0) ||
                       (d_string.endsWith("-") && intDegreesDeclination ==0)) d = - d;
                   // place D between + and - 180 (but remember it's in minutes
                   while (d < - 10800.0) d += 21600.0;
                   while (d > 10800.0) d -= 21600.0;
                } 
                if(i!=YearMean.MISSING_ELEMENT){
                   // add minutes to angles
                   if (i >= 0) i = (i * 60.0) + i_mins;
                   else i = (i * 60.0) - i_mins;
                   // check for -ve angles in the form "-0 24.8"
                   // also "-0-24.8" and "0-24.8"
                   if ((i_string.startsWith("-") && i > 0) ||
                      (i_string.endsWith("-") && intDegreesInclination ==0)) i=-i;
                }
                

                
                // encode the type string
                if (type_string.equalsIgnoreCase("A")) type = YearMean.YearMeanType.ALL_DAYS;
                else if (type_string.equalsIgnoreCase("Q")) type = YearMean.YearMeanType.QUIET_DAYS;
                else if (type_string.equalsIgnoreCase("D")) type = YearMean.YearMeanType.DISTURBED_DAYS;
                else if (type_string.equalsIgnoreCase("J")) type = YearMean.YearMeanType.JUMP;
                else if (type_string.equalsIgnoreCase("I")) type = YearMean.YearMeanType.INCOMPLETE;
                else type = YearMean.YearMeanType.UNKNOWN;
                
                // sometimes jump records have been provided with a year of 0
                // adjust this to use the year from the previous record
                if ((year == 0.0) && (type == (YearMean.YearMeanType.JUMP)) &&
                    (means.size() > 0))
                {
                    year = means.get (means.size() -1).getYear() + 0.5;
                }
                
                // sometimes the recorded elements field is missing for jump records
                if ((type == YearMean.YearMeanType.JUMP) && (YearMean.checkElements(recorded_elements) == null))
                {
                    if (means.size() > 0) recorded_elements = means.get (means.size() -1).getRecordedElements();
                }
                
                // create and add a new year mean
                mean = new YearMean (year, x, y, z, h, d, i, f, type, recorded_elements, note_number);
                addYearMean(mean);
                
                parsed_ok = true;
            }
            catch (NoSuchElementException e) { }
            catch (NumberFormatException e) { }
            catch (IndexOutOfBoundsException e) { }
            
            // if the line didn't parse, it may be a note - notes
            // come after the end of the data and don't include the 'key'
            if (parsed_ok) found_note = false;
            else if (means.size() <= 0) found_note = false;
            else
            {
                found_note = true;
                uc_line = line.toUpperCase().replace(" ", "");
                if (line.trim().length() <= 0) found_note = false;
                else if (uc_line.startsWith("*A=")) found_note = false;
                else if (uc_line.startsWith("*Q=")) found_note = false;
                else if (uc_line.startsWith("*D=")) found_note = false;
                else if (uc_line.startsWith("*I=")) found_note = false;
                else if (uc_line.startsWith("*J=")) found_note = false;
                else if (uc_line.startsWith("ELE")) found_note = false;
            }
            if (found_note) notes.add (line);
        }
        reader.close();
        
        // check that there is at least one mean
        if (means.size() <= 0)
            throw new ParseException ("No annual mean values found", 0);
        
        // sort the list
        Collections.sort (means);
        
        // reject duplicates - some observatories have put multiple jump records
        // in where they have separated out the all/quiet/distrubed data sets
        for (count=1; count<means.size(); count++)
        {
            last_mean = means.get (count -1);
            mean = means.get (count);
            if (last_mean.equals(mean))
            {
                means.remove(count);
                count --;
            }
        }
    }
    
    public String getStationCode () { return  station_code; }
    public int getNMeans () { return means.size(); }
    public YearMean getMean (int index) { return means.get(index); }
    public void remove (YearMean mean) { means.remove(mean); }
    public int getNNotes () { return notes.size(); }
    public String getNote (int index) { return notes.get(index); }
    public void addNote (String note) { notes.add (note); }
    
    /** add a year mean 
     * @param mean the year mean */
    public void addYearMean (YearMean mean) 
    {
        means.add (mean);
    }

    /** create an object to iterate over an array of year mean file
     * objects
     * @param type the type of year mean objects to include
     * @param include_jumps how to include jumps:
     *        NO_JUMPS - don't include jumps
     *        INCLUDE_JUMPS - insert jump objects in the iteration
     *        APPLY_JUMPS - add jumps to the other year means
     * @param include_incomplete include INCOMPLETE types in the iteration
     *        (normally used with ALL_DAYS type)
     * @param start_from_first_jump if true, start the iteration at the
     *        first jump, rather than the start of the year means */
    public YearMeanIterator Iterator (YearMean.YearMeanType type,
                                      IncludeJumps include_jumps,
                                      boolean include_incomplete,
                                      boolean start_from_first_jump)
    {
        return new YearMeanIterator (this, type, include_jumps, include_incomplete, start_from_first_jump);
    }
                
    public int getNMeans (YearMean.YearMeanType type)
    {
        int count, total;
        YearMean mean;
        
        total = 0;
        for (count=0; count<means.size(); count++)
        {
            mean = means.get (count);
            if (type.equals(mean.getType())) total ++;
        }
        return total;
    }
    
    
    /** write data to a file
     * @param directory the directory to put the file in
     * @param obsy_title the observatory name + code at the top of the file
     * @param colatitude observatory position
     * @param longitude observatory position
     * @param elevation observatory position */
    public void writeData (File directory, String obsy_title,
                           double colatitude, double longitude, double elevation)
    throws FileNotFoundException, IOException
    {
        int count, value, d, i;
        String string;
        BufferedWriter writer;
        YearMeanFilename yearmean_filename;
        YearMean mean;
        
        // check that there is something to write
        if (means.size() <= 0)
            throw new FileNotFoundException ("Nothing to write");

        // sort the list of means
        Collections.sort (means);
        
        // open the file
        yearmean_filename = new YearMeanFilename (directory, station_code, GeomagDataFilename.Case.LOWER);
        writer = new BufferedWriter (new FileWriter (yearmean_filename.getFile()));

        // write the header
        writeLine (writer, "", true);
        writeLine (writer, centre ("ANNUAL MEAN VALUES", LINE_WIDTH), true);
        writeLine (writer, "", true);
        writeLine (writer, centre (obsy_title, LINE_WIDTH), true);
        writeLine (writer, "", true);
        writeLine (writer, String.format ("COLATITUDE %6.3f   LONGITUDE %7.3f E   ELEVATION %.0f meters", colatitude, longitude, elevation), true);
        writeLine (writer, "", true);
        writeLine (writer, "YEAR      D        I         H      X      Y      Z      F  * ELE  Note", true);
        writeLine (writer, "       deg  min deg  min     nT     nT     nT     nT     nT            ", true);
        writeLine (writer, "", true);
   
        // write the data
        for (count=0; count<means.size(); count++)
        {
            // get the next mean and write it out
            mean = means.get(count);
            d = (int) mean.getD();
            i = (int) mean.getI();
            writeLine (writer, String.format ("%8.3f %3d %4.1f %3d %4.1f %6.0f %6.0f %6.0f %6.0f %6.0f",
                                                mean.getYear(),
                                                d, mean.getD() - (double) d,
                                                i, mean.getI() - (double) i,
                                                mean.getH(),
                                                mean.getX(),
                                                mean.getY(),
                                                mean.getZ(),
                                                mean.getF()
                                             ) +
                                             " " + mean.getTypeCode() +
                                             " " + GeoString.fix (mean.getRecordedElements(), 4, false, false) +
                                             " " + GeoString.fix (mean.getNoteNumber(), 3, false, false), true);
        }

        // write the trailer
        writeLine (writer, "", false);
        writeLine (writer, "* A = All days", false);
        writeLine (writer, "* Q = Quiet days", false);
        writeLine (writer, "* D = Disturbed days", false);
        writeLine (writer, "* I = Incomplete", false);
        writeLine (writer, "* J = Jumps:      jump value = old site value - new site value", false);
        writeLine (writer, "", false);
        writeLine (writer, "ELE = Recorded elements from which the annual mean values were derived", false);
        writeLine (writer, "", false);
        for (count=0; count<notes.size(); count++)
            writeLine (writer, notes.get (count), false);
        
        // close the file
        writer.close();
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // private code below here
    ////////////////////////////////////////////////////////////////////////////
    
    private String centre (String contents, int width)
    {
        int length, padlen;
        
        length = contents.length();
        if (length > width) return contents.substring(0, width);
        padlen = (width - length) / 2;
        contents = GeoString.pad (contents, length + padlen, true);
        contents = GeoString.pad (contents, width, false);
        return contents;
    }
    
    private double extractNumber (String string, int start_pos, int end_pos, boolean blank_as_zero)
    throws NumberFormatException
    {
        string = string.substring(start_pos, end_pos).trim();
        if (string.length() <= 0) 
        {
            if (blank_as_zero) return 0.0;
            throw new NumberFormatException ();
        }
        return Double.parseDouble(string);
    }
    
    private int extractInteger (String string, int start_pos, int end_pos, boolean blank_as_zero)
    throws NumberFormatException
    {
        string = string.substring(start_pos, end_pos).trim();
        if (string.length() <= 0) 
        {
            if (blank_as_zero) return 0;
            throw new NumberFormatException ();
        }
        return Integer.parseInt(string);
    }

    private String extractString (String string, int start_pos, int end_pos, boolean short_as_empty)
    {
        if (short_as_empty)
        {
            if (end_pos > string.length()) end_pos = string.length();
            if (end_pos < start_pos) return "";
        }
        return string.substring(start_pos, end_pos).trim();
    }
    
    private void writeLine (BufferedWriter writer, String line, boolean pad_left)
    throws IOException
    {
        writer.write (GeoString.pad(line, LINE_WIDTH, pad_left));
        writer.newLine();
    }
    
    private void calcStats ()
    {
        
    }
    
}
