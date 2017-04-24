/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagDataFilename;
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

/**
 * Class to read and write K index data in INTERMAGNET DKAFile format. Note -
 * each DKAFile object can hold 1 year of data from 1 station only.
 * 
 * @author smf
 */
public class DKAFile 
{

    /** the values used to flag a missing index value */
    public static final int MISSING_K_INDEX = 999;
    
    /** width of a line in the DKAFile file */
    public static final int LINE_WIDTH = 79;
    
    /** class to hold 1 day of K indices */
    public class KDay 
    implements Comparable<KDay>
    {
        private Date date;
        private int [] indices;
        private int sk, ak;
        
        public KDay (Date date)
        {
            int count;
            
            this.date = date;
            this.indices = new int [8];
            for (count=0; count<indices.length; count++) indices[count] = MISSING_K_INDEX;
            sk = ak = MISSING_K_INDEX;
        }
        public KDay (Date date, int indices [])
        {
            this (date);
            
            int count;
            
            for (count=0; count<this.indices.length; count++) this.indices[count] = indices [count];
        }
        public KDay (Date date, int indices [], int sk, int ak)
        {
            this (date, indices);
            this.sk = sk;
            this.ak = ak;
        }
        public Date getDate () { return date; }
        public int getNIndices () { return indices.length; }
        public int getIndex (int index) { return indices [index]; }
        public int getSK () { return sk; }
        public int getAK () { return ak; }
        public int compareTo (DKAFile.KDay o) { return date.compareTo(o.date); }
    }

    
    // private members
    private String station_code;
    private int dka_year;
    private Vector<KDay> k_days;
    private GregorianCalendar cal;
    
    /** create an empty DKAFile object */
    public DKAFile (String station_code)
    {
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        this.station_code = station_code;
        k_days = new Vector<KDay> ();
    }
    
    /** fill a DKAFile object from a file 
     * @param file the file to read from 
     * @throws FileNotFound exception if the file could not be found
     * @throws IOException if there was a read error
     * @throws ParseException if there was an error reading data from
     *         the file, or the filename could not be understaood - the 
     *         fileame should be line: SSSSYYk.dka */
    public DKAFile (File file)
    throws FileNotFoundException, IOException, ParseException
    {
        BufferedReader reader;
        String line, date_string;
        Date date;
        int indices [], sk, ak;
        DKAFilename dka_filename;
        StringTokenizer tokens;
        KDay k_day;
        
        k_days = new Vector<KDay> ();
        indices = new int [8];

        // parse the filename
        dka_filename = new DKAFilename (file);
        station_code = dka_filename.getStationCode();
        
        // read the file
        reader = new BufferedReader (new FileReader (file));
        while ((line = reader.readLine()) != null)
        {
            try
            {
                // split the line into fields
                tokens = new StringTokenizer (line, " \t-");
            
                // get and parse the date - ignore lines with badly formatted dates
                date_string = tokens.nextToken() + "-" + tokens.nextToken() + "-" + tokens.nextToken();
                date = DateUtils.parseDate(date_string, true);
                if (date == null) throw new NoSuchElementException ();
                date =  DateUtils.TwoDigitCenturyCorrect(date);

                // ignore the day number
                tokens.nextToken();
                
                // get and parse the 8 indices - ignore badly formatted indices
                indices [0] = extractIndex (tokens.nextToken());
                indices [1] = extractIndex (tokens.nextToken());
                indices [2] = extractIndex (tokens.nextToken());
                indices [3] = extractIndex (tokens.nextToken());
                indices [4] = extractIndex (tokens.nextToken());
                indices [5] = extractIndex (tokens.nextToken());
                indices [6] = extractIndex (tokens.nextToken());
                indices [7] = extractIndex (tokens.nextToken());
                
                // get SK and AK
                try { sk = extractIndex (tokens.nextToken()); }
                catch (NoSuchElementException e) { sk = MISSING_K_INDEX; }
                catch (NumberFormatException e) { sk = MISSING_K_INDEX; }
                try { ak = extractIndex (tokens.nextToken()); }
                catch (NoSuchElementException e) { ak = MISSING_K_INDEX; }
                catch (NumberFormatException e) { ak = MISSING_K_INDEX; }
                
                // and the index to the list, ignoring indices with the
                // wrong year
                k_day = new KDay (date, indices, sk, ak);
                try { addKindex(k_day); }
                catch (ParseException e) { }
            }
            catch (NoSuchElementException e) { }
            catch (NumberFormatException e) { }
        }
        reader.close();
        
        // sort the list of K indices
        Collections.sort (k_days);
    }
    
    /** add a K index 
     * @param k_day the day's worth of K indices
     * @throws ParseException if the date for the day is outside the year
     *         for this DKAFile object */
    public void addKindex (KDay k_day) 
    throws ParseException
    {
        int count;
        KDay k_day2;
        
        // check the day is in the right year
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        cal.setTime(k_day.getDate());
        if (k_days.size() <= 0)
            dka_year = cal.get (GregorianCalendar.YEAR);
        else
        {
            if (cal.get (GregorianCalendar.YEAR) != dka_year)
                throw new ParseException ("Wrong year for K index data", 0);
        }
        
        // check the day isn't already stored
        for (count=0; count<k_days.size(); count++)
        {
            k_day2 = k_days.get (count);
            if (k_day2.getDate().equals(k_day.getDate()))
                throw new ParseException ("Data already recorded for day " + k_day.getDate().toString(), 0);
        }
        
        k_days.add (k_day);
    }
    
    public String getStationCode () { return  station_code; }
    public int getYear () { return dka_year; }
    public int getNDays () { return k_days.size(); }
    public KDay getDay (int index) { return k_days.get(index); }

    /** write data to a file
     * @param directory the directory to put the file in
     * @param inst_title the top title of the file
     * @param obsy_name the observatory name
     * @param k9 the value in nT of K-9 */
    public void writeData (File directory, String inst_title, String obsy_name, int k9)
            throws FileNotFoundException, IOException
    {
        boolean missing;
        int day_count, n_days, count, value;
        String string;
        BufferedWriter writer;
        DKAFilename dka_filename;
        KDay k_day;
        Iterator<KDay> i;
        GregorianCalendar cal;

        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        
        // check that there is something to write
        if (k_days.size() <= 0)
            throw new FileNotFoundException ("Nothing to write");

        // sort the list of K indices
        Collections.sort (k_days);
        
        // open the file
        dka_filename = new DKAFilename (directory, station_code, dka_year, GeomagDataFilename.Case.LOWER);
        writer = new BufferedWriter (new FileWriter (dka_filename.getFile()));

        // write the header
        writeLine (writer, centre (inst_title, LINE_WIDTH));
        writeLine (writer, centre ("", LINE_WIDTH));
        writeLine (writer, centre (station_code + " " + obsy_name, LINE_WIDTH));
        writeLine (writer, centre ("", LINE_WIDTH));
        writeLine (writer, centre ("K - Index Values for " + Integer.toString (dka_year) + " (K-9 = " + Integer.toString (k9) + "nT)", LINE_WIDTH));
        writeLine (writer, centre ("", LINE_WIDTH));
        writeLine (writer, "DA-MON-YR  DAY #    1    2    3    4      5    6    7    8       SK      AK");
        writeLine (writer, centre ("", LINE_WIDTH));
        
        // write the data
        n_days = DateUtils.daysInYear(dka_year);
        i = k_days.iterator();
        k_day = null;
        for (day_count=0; day_count<n_days; day_count++)
        {
            // get the next set of K indices
            if (k_day == null && i.hasNext()) k_day = i.next();
            
            // work out if it is the right one for this day
            missing = true;
            if (k_day == null)
            {
                cal.set (GregorianCalendar.YEAR, dka_year);
                cal.set (GregorianCalendar.DAY_OF_YEAR, day_count +1);
            }
            else
            {
                cal.setTime (k_day.getDate());
                if ((day_count +1) == cal.get (GregorianCalendar.DAY_OF_YEAR))
                    missing = false;
            }
            
            // write out either the record or a missing record
            string = GeoString.pad (Integer.toString (cal.get (GregorianCalendar.DAY_OF_MONTH)), 2, true, '0') +
                          "-" +
                          DateUtils.getMonthName(cal.get (GregorianCalendar.MONTH), DateUtils.MONTH_UPPERCASE, 3) +
                          "-" +GeoString.pad (Integer.toString (dka_year % 100), 2, true, '0') +
                          "   " +
                          GeoString.pad (Integer.toString (cal.get (GregorianCalendar.DAY_OF_YEAR)), 3, true, '0');
            for (count=0; count<ImagCDDataDay.N_K_INDICES; count++)
            {
                if (missing) value = MISSING_K_INDEX;
                else value = k_day.getIndex(count);
                string += GeoString.pad (Integer.toString (value), 5, true);
                if (count == 4) string += "  ";
            }
            if (missing) value = MISSING_K_INDEX;
            else value = k_day.getSK();
            string += GeoString.pad (Integer.toString (value), 10, true);
            if (missing) value = MISSING_K_INDEX;
            else value = k_day.getAK();
            string += GeoString.pad (Integer.toString (value), 8, true);
            writeLine (writer, string);
            
            // flag that this day have been written
            if (! missing) k_day = null;
        }
        
        // close the file
        writer.close();
    }

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
    
    private void writeLine (BufferedWriter writer, String line) 
    throws IOException
    {
        writer.write (GeoString.pad(line, LINE_WIDTH, true));
        writer.newLine();
    }
    
    private int extractIndex (String field)
    throws NumberFormatException
    {
        int ret_val;

        // some old K indices were recorded as real numbers
        try
        {
            ret_val = Integer.parseInt(field);
        }
        catch (NumberFormatException e)
        {
            ret_val = (int) (Double.parseDouble(field) + 0.5);
        }

        return ret_val;
    }
    
}
