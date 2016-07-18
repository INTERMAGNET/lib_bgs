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
public class YearMeanFilename 
{
    private String station_code;
    private String filename;
    private File file;
       
    public YearMeanFilename (File file)
    throws ParseException
    {
        this.file = file;
        filename = file.getName();
        if (filename.length() != 12) throw new ParseException ("File name incorrect length: " + filename, 0);
        station_code = filename.substring(9, 12);
        if (! filename.substring(0,9).equalsIgnoreCase("yearmean."))
            throw new ParseException ("Filename should start with yearmean.: " + filename, 0);
    }
     
    public YearMeanFilename (String station_code)
    {
        this (new File ("."), station_code, GeomagDataFilename.Case.LOWER);
    }
       
    public YearMeanFilename (File directory, String station_code, GeomagDataFilename.Case cs)
    {
        this.station_code = station_code;
        filename = "yearmean." + station_code;
        switch (cs)
        {
            case LOWER: filename = filename.toUpperCase(); break;
            case UPPER: filename = filename.toLowerCase(); break;
        }
        file = new File (directory, filename);
    }
    
    public String getStationCode () { return station_code; }
    public String getFileame () { return filename; }
    public File getFile () { return file; }

}
