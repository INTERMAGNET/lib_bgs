/*
 * GeomagDataFilename.java
 *
 * Created on 03 May 2007, 13:15
 *
 */

package bgs.geophys.library.Data;
import bgs.geophys.library.Data.GeomagDataFormat.DurationType;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author ewan
 */
public abstract class GeomagDataFilename {
    
    // protected variables
    private String filename = "";
    private String observatoryCode = "";
    private Date date = new Date();
    private QualityType qualityType = QualityType.NOT_APPLICABLE;
    private Interval interval = Interval.NOT_APPLICABLE;
    private boolean fragment = false;
    private DurationType duration = DurationType.FRAGMENT_OR_UNKNOWN;

    // Property enumerators
    public enum QualityType {
        NOT_APPLICABLE, PROVISIONAL, DEFINITIVE, VARIATION, ADJUSTED, REPORTED, TEST
    }
    
    public enum Interval {
        NOT_APPLICABLE, MONTHLY, DAILY, HOURLY, MINUTE, SECOND
    }
    
    public enum Case {
        UPPER, LOWER
    }
    
    /** Default constructor
    */
    GeomagDataFilename() {
        //do nothing
    }
    
    /** Creates a new GeomagDataFilename instance by asking the sub class to parse a filename */
    public GeomagDataFilename(String filename) throws ParseException {
        parseFilename(filename);
    }
    
    // Functions to be implemented by subclass
    abstract void parseFilename(String filename) throws ParseException;
    abstract String generateFilename(Case characterCase);
    
    // Property accessors
    public String getFilename()         { return filename; }
    public String getObservatoryCode()  { return observatoryCode; }
    public Date getDate()               { return date; }
    public QualityType getQualityType() { return qualityType; }
    public Interval getInterval()       { return interval; }
    public boolean isFragment()         { return fragment; }
    public DurationType getDuration()   { return duration; }

    // Protected property setter - to be used by subclasses
    protected void setFilename(String filename)                 { this.filename = filename; }
    protected void setObservatoryCode(String observatoryCode)   { this.observatoryCode = observatoryCode; }
    protected void setDate(Date date)                           { this.date = date; }
    protected void setQualityType(QualityType qualityType)      { this.qualityType = qualityType; }
    protected void setInterval(Interval interval)               { this.interval = interval; }
    protected void setDuration(DurationType duration)           { this.duration = duration; }
    protected void setFragment(boolean fragment)                { this.fragment = fragment; }

    @Override
    public String toString() 
    {
//        return getFilename();
        
        /* Alternative code for debugging purposes */
        String newline = System.getProperty("line.separator");
        return "Filename: " + getFilename() + newline
                + "Obs code: " + getObservatoryCode() + newline
                + "Date    : " + getDate().toString() + newline
                + "Type    : " + getQualityType().toString() + newline
                + "Interval: " + getInterval().toString() + newline
                + "Fragment: " + isFragment() + newline
                + "Duration: " + getDuration().toString();
    }
}
