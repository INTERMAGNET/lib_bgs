/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagDataFilename;
import bgs.geophys.library.Misc.GeoString;
import java.io.File;
import java.text.ParseException;

/**
 * A class to hold a filename for a k index file
 * 
 * @author smf
 */
public class DKAFilename 
{
    private String station_code;
    private int year;
    private String filename;
    private File file;
       
    public DKAFilename (String filename)
    throws ParseException
    {
        this (new File (filename));
    }
    
    public DKAFilename (File file)
    throws ParseException
    {
        this.file = file;
        filename = file.getName();
        if (filename.length() != 10) throw new ParseException ("File name incorrect length: " + filename, 0);
        station_code = filename.substring(0, 3);
        try
        {
            year = Integer.parseInt(filename.substring(3, 5));
        }
        catch (NumberFormatException e)
        {
            throw new ParseException ("Non-numeric year in filename: " + filename, 0);
        }
        if (! filename.substring(5).equalsIgnoreCase("k.dka"))
            throw new ParseException ("Filename should end in K.DKA: " + filename, 0);
    }
     
    public DKAFilename (String station_code, int year)
    {
        this (new File ("."), station_code, year, GeomagDataFilename.Case.LOWER);
    }
       
    public DKAFilename (File directory, String station_code, int year, GeomagDataFilename.Case cs)
    {
        this.station_code = station_code;
        this.year = year;
        filename = station_code + 
                   GeoString.pad(Integer.toString (year % 100), 2, true, '0') +
                   "k.dka";
        switch (cs)
        {
            case LOWER: filename = filename.toUpperCase(); break;
            case UPPER: filename = filename.toLowerCase(); break;
        }
        file = new File (directory, filename);
    }
    
    public String getStationCode () { return station_code; }
    public int getYear () { return year; }
    public String getFileame () { return filename; }
    public File getFile () { return file; }

}
