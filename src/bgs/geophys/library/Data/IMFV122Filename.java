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
public class IMFV122Filename extends GeomagDataFilename
{

    // Date formatting objects
    private static SimpleDateFormat date_format;
    
    static 
    {
        GregorianCalendar cal;
        
        cal = new GregorianCalendar (1950, 0, 1);
        cal.setTimeZone(DateUtils.gmtTimeZone);
        date_format = new SimpleDateFormat ("MMMddyy", DateUtils.english_date_format_symbols);
        date_format.set2DigitYearStart(cal.getTime());
        date_format.setTimeZone(DateUtils.gmtTimeZone);
    }
    
    /** Creates a new instance of IMFV122Filename */
    public IMFV122Filename (String observatoryCode, Date date, Case characterCase) 
    {
        constructIMFV122Filename (observatoryCode, date, characterCase);
    }
    
    /** Creates a new instance of IMFV122Filename */
    public IMFV122Filename(String filename) 
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
        QualityType qualityType;
        int len;
        
        len = filename.length();
        try
        {
            switch (len)
            {
                case 11:
                case 12:
                    date = date_format.parse(filename.substring(0, 7));
                    observatoryCode = filename.substring (len -3);
                    if (len == 11)
                        constructIMFV122Filename (observatoryCode, date, GeomagDataFilename.Case.LOWER);
                    else
                    {
                        qualityType = enumerateQualityType(filename.substring(7, 8));
                        constructIMFV122Filename (observatoryCode, date, qualityType, GeomagDataFilename.Case.LOWER);
                    }
                    break;
                default:
                    throw new IllegalArgumentException ();
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        }
        
        super.setFilename(filename);  // Replace the filename generated by the constructIMFV122Filename() with the one passed to this constructor
    }

    @Override
    String generateFilename(Case characterCase) 
    {
        String filename;
        
        filename = date_format.format (getDate()) + "." + getObservatoryCode ();
        switch(characterCase) {
            case UPPER: return filename.toUpperCase();
            case LOWER: return filename.toLowerCase();
        }
        return filename;
    }

    private void constructIMFV122Filename(String observatoryCode, Date date, Case characterCase) 
    {
        super.setObservatoryCode(validateObservatoryCode(observatoryCode).toUpperCase());
        super.setDate(date);
        super.setQualityType(QualityType.NOT_APPLICABLE);
        super.setInterval(GeomagDataFilename.Interval.DAILY.NOT_APPLICABLE);
        super.setFragment(false);
        super.setFilename(generateFilename(characterCase));
    }
    
    private void constructIMFV122Filename(String observatoryCode, Date date, QualityType qualityType, Case characterCase) 
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        super.setObservatoryCode(validateObservatoryCode(observatoryCode).toUpperCase());
        super.setDate(date);
        super.setQualityType(validateQualityType(qualityType));
        super.setInterval(GeomagDataFilename.Interval.DAILY.NOT_APPLICABLE);
        super.setFragment(false);
        super.setFilename(generateFilename(characterCase));
    }
    
    private String validateObservatoryCode(String observatoryCode) {
        if (observatoryCode.length() != 3) {
            throw new IllegalArgumentException("Invalid observatoryCode argument. Must be exactly three characters.");
        }
        return observatoryCode;
    }
    
    private QualityType validateQualityType(QualityType qualityType) {
        switch(qualityType) {
            case ADJUSTED: break;
            case DEFINITIVE: break;
            case REPORTED: break;
            case TEST: break;
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
        return qualityType;
    }
    
    /**
     * Converts a GeomagDataFilename.QualityType enumeration into a single character string to be used in a filename
     */
    private String stringifyQualityType(QualityType qualityType) {
        switch(qualityType) {
            case ADJUSTED: return "a";
            case DEFINITIVE: return "d";
            case REPORTED: return "r";
            case TEST: return "t";
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
    }
    
    /**
     * Attempts to convert a single character string into a GeomagDataFilename.QualityType enumeration
     */
    private QualityType enumerateQualityType(String typeString) {
        if (typeString.equalsIgnoreCase("a")) {
            return QualityType.ADJUSTED; 
        }
        else if (typeString.equalsIgnoreCase("d")) {
            return QualityType.DEFINITIVE; 
        }
        else if (typeString.equalsIgnoreCase("r")) {
            return QualityType.REPORTED;
        }
        else if (typeString.equalsIgnoreCase("t")) {
            return QualityType.TEST;
        }
        else {
            throw new IllegalArgumentException("Invalid type string: " + typeString);
        }
    }
}
