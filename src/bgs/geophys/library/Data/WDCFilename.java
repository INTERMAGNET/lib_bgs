/*
 * WDCFilename.java
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
import java.util.GregorianCalendar;

/** WDCFilename can be used for both creating and parsing valid WDC filenames.
 * @author smf
 */
public class WDCFilename extends GeomagDataFilename {
    
    // Date formatting objects
    private static SimpleDateFormat yyyy;
    private static SimpleDateFormat yyMM;
    
    static {
        GregorianCalendar cal;
        
        yyyy = new SimpleDateFormat("yyyy");
        yyyy.setTimeZone(DateUtils.gmtTimeZone);
        yyMM = new SimpleDateFormat("yyyyMM");
        yyMM.setTimeZone(DateUtils.gmtTimeZone);
        cal = new GregorianCalendar (1960, 0, 1);
        cal.setTimeZone(DateUtils.gmtTimeZone);
        yyMM.set2DigitYearStart(cal.getTime());
    }
    
    /** Creates a new instance of WDCFilename */
    public WDCFilename(String observatoryCode, Date date, QualityType qualityType,
                       Interval interval, DurationType duration,
                       Case characterCase)
    {
        constructWDCFilename(observatoryCode, date, qualityType, interval,
                             duration, characterCase);
    }
    
    /** Creates a new instance of WDCFilename */
    public WDCFilename(String filename) throws ParseException {
        super(filename);
    }
    
    /** Creates a new instance of WDCFilename */
    public WDCFilename(WDCHour wdc_hour, Case characterCase) throws ParseException {
        String observatoryCode = wdc_hour.getStationCode();
        Date date = wdc_hour.getStartDate();
        QualityType qualityType = QualityType.NOT_APPLICABLE;
        String intervalString = wdc_hour.getIntervalType();
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
        DurationType duration = wdc_hour.getDurationType();
        
        constructWDCFilename(observatoryCode, date, qualityType, interval, duration, characterCase);
    }
    
    /** Creates a new instance of WDCFilename */
    public WDCFilename(WDCMinute wdc_min, Case characterCase) throws ParseException {
        String observatoryCode = wdc_min.getStationCode();
        Date date = wdc_min.getStartDate();
        QualityType qualityType = QualityType.NOT_APPLICABLE;
        String intervalString = wdc_min.getIntervalType();
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
        DurationType duration = wdc_min.getDurationType();
        
        constructWDCFilename(observatoryCode, date, qualityType, interval, duration, characterCase);
    }
    
    private void constructWDCFilename(String observatoryCode, Date date, QualityType qualityType,
                                      Interval interval, DurationType duration,
                                      Case characterCase)
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        super.setObservatoryCode(validateObservatoryCode(observatoryCode).toUpperCase());
        super.setDate(date);
        super.setQualityType(validateQualityType(qualityType));
        super.setInterval(validateInterval(interval));
        super.setFragment(false);
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
        int len = filename.length();
        try {
            switch (len)
            {
                case 12:
                    date = yyMM.parse (filename.substring(3, 7));
                    interval = Interval.HOURLY;
                    if (! filename.substring (7).equalsIgnoreCase("m.wdc"))
                        throw new ParseException("", 0);
                    break;
                case 11:
                    date = yyyy.parse (filename.substring(3, 7));
                    interval = Interval.MINUTE;
                    if (! filename.substring (7).equalsIgnoreCase(".wdc"))
                        throw new ParseException("", 0);
                    break;
                default:
                    throw new ParseException("", 0);
            }
            observatoryCode = filename.substring(0,3);
            qualityType = QualityType.NOT_APPLICABLE;
            duration = DurationType.FRAGMENT_OR_UNKNOWN;
        } catch (ParseException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        }
        
        constructWDCFilename(observatoryCode, date, qualityType, interval, duration, GeomagDataFilename.Case.LOWER);
        super.setFilename(filename);  // Replace the filename generated by the constructIaga2002Filename() with the one passed to this constructor

    }
    
    /** Creates a valid WDC filename based on the properties of the class */
    protected String generateFilename(Case characterCase) {
        SimpleDateFormat dateFormat;
        String filename, interval_string;
        
        switch(this.getInterval())
        {
            case MINUTE: dateFormat = yyMM; interval_string = "m"; break;
            default:     dateFormat = yyyy; interval_string = "";  break;
        }
        
        filename = this.getObservatoryCode()
                    + dateFormat.format(this.getDate())
                    + interval_string
                    + ".wdc";
        
        switch(characterCase) {
            case UPPER: return filename.toUpperCase();
            case LOWER: return filename.toLowerCase();
        }
     //   System.out.println("iaga2002 filename: "+filename);
        return filename;
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
            case NOT_APPLICABLE: break;
            case DEFINITIVE: break;
            default: throw new IllegalArgumentException("Invalid type argument.");
        }
        return qualityType;
    }
    
    private Interval validateInterval(Interval interval) {
        switch(interval) {
            case HOURLY: break;
            case MINUTE: break;
            default: throw new IllegalArgumentException("Invalid interval argument.");
        }
        return interval;
    }
    
}
