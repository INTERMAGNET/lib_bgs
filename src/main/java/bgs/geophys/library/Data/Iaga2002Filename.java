/*
 * Iaga2002Filename.java
 *
 * Created on 03 May 2007, 13:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import bgs.geophys.library.Data.GeomagDataFilename.Interval;
import bgs.geophys.library.Data.GeomagDataFilename.QualityType;
import bgs.geophys.library.Data.GeomagDataFormat.DurationType;
import bgs.geophys.library.Misc.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Iaga2002Filename can be used for both creating and parsing valid IAGA2002 filenames.
 *  - Currently, the class does not support extensions to the base name as described
 *    in the IAGA2002 format specification (i.e. additional characters between the
 *    data quality type and data interval portions of the filename.
 *  - Although the code has been implemented, this class has not been fully tested with
 *    data files containing part-days of minute or second data.
 * @author ewan
 */
public class Iaga2002Filename extends GeomagDataFilename {
    
    // Date formatting objects
    private static SimpleDateFormat yyyy;
    private static SimpleDateFormat yyyyMM;
    private static SimpleDateFormat yyyyMMdd;
    private static SimpleDateFormat yyyyMMddHH;
    private static SimpleDateFormat yyyyMMddHHmm;
    private static SimpleDateFormat yyyyMMddHHmmss;
    
    static {
        yyyy = new SimpleDateFormat("yyyy");
        yyyy.setTimeZone(DateUtils.gmtTimeZone);
        yyyyMM = new SimpleDateFormat("yyyyMM");
        yyyyMM.setTimeZone(DateUtils.gmtTimeZone);
        yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        yyyyMMdd.setTimeZone(DateUtils.gmtTimeZone);
        yyyyMMddHH = new SimpleDateFormat("yyyyMMddHH");
        yyyyMMddHH.setTimeZone(DateUtils.gmtTimeZone);
        yyyyMMddHHmm = new SimpleDateFormat("yyyyMMddHHmm");
        yyyyMMddHHmm.setTimeZone(DateUtils.gmtTimeZone);
        yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
        yyyyMMddHHmmss.setTimeZone(DateUtils.gmtTimeZone);
    }
    
    /** Creates a new instance of Iaga2002Filename */
    public Iaga2002Filename(
            String observatoryCode, Date date, QualityType qualityType,
            Interval interval, boolean fragment, DurationType duration,
            Case characterCase)
    {
        constructIaga2002Filename(
                observatoryCode, date, qualityType, interval, fragment,
                duration, characterCase);
    }
    
    /** Creates a new instance of Iaga2002Filename */
    @Deprecated
    public Iaga2002Filename(String observatoryCode, Date date, QualityType qualityType, Interval interval, boolean fragment, Case characterCase) {
        this(observatoryCode, date, qualityType, interval, fragment, DurationType.FRAGMENT_OR_UNKNOWN, characterCase);
    }
    
    /** Creates a new instance of Iaga2002Filename */
    public Iaga2002Filename(String filename) throws ParseException {
        super(filename);
    }
    
    /** Creates a new instance of Iaga2002Filename */
    public Iaga2002Filename(Iaga2002 iaga2002, Case characterCase) throws ParseException {
        String observatoryCode = iaga2002.getStationCode();
        Date date = iaga2002.getStartDate();
        QualityType qualityType = enumerateQualityType(iaga2002.getDataType().substring(0,1));
        String intervalString = iaga2002.getIntervalType();
        Interval interval;
        if (intervalString.toLowerCase().lastIndexOf("month") >= 0) {
            interval = Interval.MONTHLY;
        } else if (intervalString.toLowerCase().lastIndexOf("day") >= 0) {
            interval = Interval.DAILY;
        } else if (intervalString.toLowerCase().lastIndexOf("hour") >= 0) {
            interval = Interval.HOURLY;
        } else if (intervalString.toLowerCase().lastIndexOf("minute") >= 0) {
            interval = Interval.MINUTE;
        } else if (intervalString.toLowerCase().lastIndexOf("second") >= 0) {
            interval = Interval.SECOND;
        } else {
            throw new ParseException("Could not parse 'Data Interval Type' header field: " + intervalString, 0);
        }
        DurationType duration = iaga2002.getDurationType();
        boolean fragment = (duration == Iaga2002.DurationType.FRAGMENT_OR_UNKNOWN ? true : false);
        
        constructIaga2002Filename(observatoryCode, date, qualityType, interval, fragment, duration, characterCase);
    }
    
    private void constructIaga2002Filename(
            String observatoryCode, Date date, QualityType qualityType,
            Interval interval, boolean fragment, DurationType duration,
            Case characterCase)
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        super.setObservatoryCode(validateObservatoryCode(observatoryCode).toUpperCase());
        super.setDate(date);
        super.setQualityType(validateQualityType(qualityType));
        super.setInterval(validateInterval(interval));
        super.setFragment(fragment);
        super.setDuration(duration);
        super.setFilename(generateFilename(characterCase));
    }
    
    /** Parses a filename, updating the properties of the GeomagDataFilename superclass accordingly. */
    protected void parseFilename(String filename) throws ParseException {
        String observatoryCode;
        Date date;
        QualityType qualityType;
        Interval interval;
        DurationType duration;
        boolean fragment;
        int len = filename.length();
        if (len < 15) { throw new ParseException("Filename: " + filename + " malformatted.", 0); }
        try {
            observatoryCode = filename.substring(0,3);
            date = parseFilenameDate(filename);
            qualityType = enumerateQualityType(filename.substring(len - 8, len - 7));
            interval = enumerateInterval(filename.substring(len - 7, len - 4));
            duration = parseFilenameDuration(filename);
            fragment = false;
            if (len > 19) { fragment = true; }
        } catch (ParseException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        }
        
        constructIaga2002Filename(observatoryCode, date, qualityType, interval, fragment, duration, GeomagDataFilename.Case.LOWER);
        super.setFilename(filename);  // Replace the filename generated by the constructIaga2002Filename() with the one passed to this constructor

    }
    
    /** Parses the date from the filename */
    private Date parseFilenameDate(String filename) throws ParseException {
        Date date;
        int len = filename.length();
        try {
            date = yyyyMMddHHmmss.parse(filename.substring(3, len - 8));
//            System.out.println("Matched date format: yyyyMMddHHmmss");
        }
        catch (ParseException e1) {
            try {
                date = yyyyMMddHHmm.parse(filename.substring(3, len - 8));
//                System.out.println("Matched date format: yyyyMMddHHmm");
            }
            catch (ParseException e2) {
                try {
                    date = yyyyMMdd.parse(filename.substring(3, len - 8));
//                    System.out.println("Matched date format: yyyyMMdd");
                }
                catch (ParseException e3) {
                    try {
                        date = yyyyMM.parse(filename.substring(3, len - 8));
//                        System.out.println("Matched date format: yyyyMM");
                    }
                    catch (ParseException e4) {
                        date = yyyy.parse(filename.substring(3, len - 8));
//                        System.out.println("Matched date format: yyyy");
                    }
                }
            }
        }
        return date;
    }
    
    /** Parses the duration of the dataset from the filename */
    private DurationType parseFilenameDuration(String filename) {
        int len = filename.length();
        try {
            yyyyMMddHHmmss.parse(filename.substring(3, len - 8));
            return DurationType.SECOND;
        }
        catch (ParseException e1) { /*Do nothing*/ }
        try {
            yyyyMMddHHmm.parse(filename.substring(3, len - 8));
            return DurationType.MINUTE;
        }
        catch (ParseException e2) { /*Do nothing*/ }
        try {
            yyyyMMdd.parse(filename.substring(3, len - 8));
            return DurationType.DAY;
        }
        catch (ParseException e3) { /* Do nothing */ }
        try {
            yyyyMM.parse(filename.substring(3, len - 8));
            return DurationType.MONTH;
        }
        catch (ParseException e4) { /* Do nothing */ }
        try {
            yyyy.parse(filename.substring(3, len - 8));
            return DurationType.YEAR;
        }
        catch (ParseException e5) { return DurationType.FRAGMENT_OR_UNKNOWN; }
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
        else if (typeString.equalsIgnoreCase("p")) {
            return QualityType.PROVISIONAL;
        }
        else if (typeString.equalsIgnoreCase("q")) {
            return QualityType.QUASI_DEFINITIVE;
        }
        else if (typeString.equalsIgnoreCase("r")) {
            return QualityType.REPORTED;
        }
        else if (typeString.equalsIgnoreCase("v")) {
            return QualityType.VARIATION;
        }
        else if (typeString.equalsIgnoreCase("t")) {
            return QualityType.TEST;
        }
        else {
            throw new IllegalArgumentException("Invalid type string: " + typeString);
        }
    }
    
    /**
     * Attempts to convert a three character string into a GeomagDataFilename.QualityType enumeration
     */
    private Interval enumerateInterval(String intervalString) {
        if (intervalString.equalsIgnoreCase("mon")) {
            return Interval.MONTHLY;
        }
        else if (intervalString.equalsIgnoreCase("day")) {
            return Interval.DAILY;
        }
        else if (intervalString.equalsIgnoreCase("hor")) {
            return Interval.HOURLY;
        }
        else if (intervalString.equalsIgnoreCase("min")) {
            return Interval.MINUTE;
        }
        else if (intervalString.equalsIgnoreCase("sec")) {
            return Interval.SECOND;
        }
        else {
            throw new IllegalArgumentException("Invalid interval string.");
        }
    }
    
    /** Creates a valid IAGA2002 filename based on the properties of the class */
    protected String generateFilename(Case characterCase) {
        SimpleDateFormat dateFormat;
        String filename;
        
        if ( DurationType.FRAGMENT_OR_UNKNOWN == getDuration() ) {
            switch(this.getInterval()){
                case MONTHLY: dateFormat = yyyyMM; break;
                case DAILY:   dateFormat = yyyyMMdd; break;
                case HOURLY:  dateFormat = yyyyMMddHH; break;
                case MINUTE:  dateFormat = yyyyMMddHHmm; break;
                case SECOND:  dateFormat = yyyyMMddHHmmss; break;
                default:      dateFormat = yyyyMMddHHmmss;
            }
        }
        else {
            switch( getDuration() ){
                case SECOND: dateFormat = yyyyMMddHHmmss; break;
                case MINUTE: dateFormat = yyyyMMddHHmm; break;
                case HOUR:   dateFormat = yyyyMMddHH; break;
                case DAY:    dateFormat = yyyyMMdd; break;
                case MONTH:  dateFormat = yyyyMM; break;
                case YEAR:   dateFormat = yyyy; break;
                default: dateFormat = yyyyMMddHHmmss;
            }
        }
        
        filename = this.getObservatoryCode()
                    + dateFormat.format(this.getDate())
                    + stringifyQualityType(this.getQualityType())
                    + stringifyInterval(this.getInterval())
                    + "."
                    + stringifyInterval(this.getInterval());
        
        switch(characterCase) {
            case UPPER: return filename.toUpperCase();
            case LOWER: return filename.toLowerCase();
        }
     //   System.out.println("iaga2002 filename: "+filename);
        return filename;
    }

    /**
     * Converts a GeomagDataFilename.QualityType enumeration into a single character string to be used in a filename
     */
    private String stringifyQualityType(QualityType qualityType) {
        switch(qualityType) {
            case ADJUSTED: return "a";
            case DEFINITIVE: return "d";
            case PROVISIONAL: return "p";
            case QUASI_DEFINITIVE: return "q";
            case REPORTED: return "r";
            case VARIATION: return "v";
            case TEST: return "t";
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
    }
    
    /** Converts a GeomagDataFilename.Interval enumeration into a three character string to be used in a filename */
    private String stringifyInterval(Interval interval) {
        switch(interval) {
            case DAILY: return "day";
            case HOURLY: return "hor";
            case MINUTE: return "min";
            case MONTHLY: return "mon";
            case SECOND: return "sec";
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
    }
    
    // Constructor argument validation
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
            case PROVISIONAL: break;
            case REPORTED: break;
            case VARIATION: break;
            case TEST: break;
            case QUASI_DEFINITIVE: break;
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
        return qualityType;
    }
    
    private Interval validateInterval(Interval interval) {
        switch(interval) {
            case DAILY: break;
            case HOURLY: break;
            case MINUTE: break;
            case MONTHLY: break;
            case SECOND: break;
            default: throw new IllegalArgumentException("Invalid interval argument.");
        }
        return interval;
    }
    
}
