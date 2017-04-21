/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import bgs.geophys.library.Misc.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author smf
 */
public class ImagCDFilename extends GeomagDataFilename
{

    // Date formatting objects
    private static SimpleDateFormat date_format;
    
    private boolean compressed;
    
    static 
    {
        GregorianCalendar cal;
        
        cal = new GregorianCalendar (1950, 0, 1);
        cal.setTimeZone(DateUtils.gmtTimeZone);
        date_format = new SimpleDateFormat ("yyMMM", DateUtils.english_date_format_symbols);
        date_format.set2DigitYearStart(cal.getTime());
        date_format.setTimeZone(DateUtils.gmtTimeZone);
    }
    
    /** Creates a new instance of IMFV122Filename */
    public ImagCDFilename (String observatoryCode, Date date, Case characterCase) 
    {
        constructImagCDFilename (observatoryCode, date, false, characterCase);
    }
    
    /** Creates a new instance of IMFV122Filename */
    public ImagCDFilename (String observatoryCode, Date date, boolean compressed, Case characterCase) 
    {
        constructImagCDFilename (observatoryCode, date, compressed, characterCase);
    }
    
    /** Creates a new instance of IMFV122Filename */
    public ImagCDFilename(String filename) 
    throws ParseException 
    {
        super(filename);
    }
    
    @Override
    void parseFilename(String filename) 
    throws ParseException 
    {
        String observatoryCode;
        Date date;
        int len;
        
        len = filename.length();
        if (len != 12)
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        observatoryCode = filename.substring (0, 3);
        date = date_format.parse(filename.substring(3, 8));
        if (filename.substring(9).equalsIgnoreCase("bin")) compressed = false;
        else compressed = true;
        constructImagCDFilename (observatoryCode, date, compressed, GeomagDataFilename.Case.LOWER);
        super.setFilename(filename);  // Replace the filename generated by the constructIMFV122Filename() with the one passed to this constructor
    }

    @Override
    String generateFilename(Case characterCase) 
    {
        String filename, type;
        
        if (compressed) type = ".ZIP";
        else type = ".BIN";
        filename = getObservatoryCode () + date_format.format (getDate()) + type;
        switch(characterCase) {
            case UPPER: return filename.toUpperCase();
            case LOWER: return filename.toLowerCase();
        }
        return filename;
    }

    private void constructImagCDFilename(String observatoryCode, Date date, boolean compressed, Case characterCase) 
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        super.setObservatoryCode(validateObservatoryCode(observatoryCode).toUpperCase());
        super.setDate(date);
        super.setQualityType(QualityType.NOT_APPLICABLE);
        super.setInterval(GeomagDataFilename.Interval.DAILY.NOT_APPLICABLE);
        super.setFragment(false);
        this.compressed = compressed;
        super.setFilename(generateFilename(characterCase));
    }
    
    private String validateObservatoryCode(String observatoryCode) {
        if (observatoryCode.length() != 3) {
            throw new IllegalArgumentException("Invalid observatoryCode argument. Must be exactly three characters.");
        }
        return observatoryCode;
    }
    
}
