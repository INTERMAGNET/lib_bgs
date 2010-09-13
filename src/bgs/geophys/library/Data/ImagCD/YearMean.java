/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Misc.DateUtils;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class to hold a single annual mean.
 * Inside this class field strengths are in nT, angles in minutes.
 * 
 * @author smf
 */
public class YearMean
implements Comparable<YearMean>
{
    
    /** codes for the type of annual mean */
    public enum YearMeanType { ALL_DAYS, ALL_DAYS_PLUS_INCOMPLETE,
                               QUIET_DAYS, DISTURBED_DAYS, 
                               JUMP, INCOMPLETE, UNKNOWN }
    
    /** the values used to flag a missing element */
    public static final double MISSING_ELEMENT = 99999.0;
    public static final int MISSING_DEGREES = 999;
    public static final double MISSING_MINUTES = 99.9;
    
    private double year;
    private GregorianCalendar cal;
    private Date date;
    
    // field strengths in nT, angles in degrees
    private double x, y, z, h, d, i, f;
    private YearMeanType type;
    private String recorded_elements;
    private String note_number;
    
    /** create an empty year mean */
    public YearMean ()
    {
        this.year = 0;
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        date = null;
        x = y = z = h = d = i = f = MISSING_ELEMENT;
        type = YearMeanType.UNKNOWN;
        recorded_elements = "";
        note_number = "";
    }
    
    /** create an empty year mean */
    public YearMean (double year)
    {
        this ();
        this.year = year;
    }

    /** copy constructor */
    public YearMean (YearMean copy)
    {
        this (copy.year, copy.x, copy.y, copy.z, copy.h, copy.d, copy.i,  copy.f,
              copy.type, copy.recorded_elements, copy.note_number);
    }
    
    /** copy constructor with adjusted year */
    public YearMean (double year, YearMean copy)
    {
        this (copy);
        this.year = year;
    }
    
    /** difference constructor */
    public YearMean (YearMean first, YearMean second)
    {
        this ();
        year = (first.getYear() + second.getYear()) / 2.0;
        if (first.x == MISSING_ELEMENT || second.x == MISSING_ELEMENT)
            this.x = MISSING_ELEMENT;
        else
            this.x = first.x - second.x;
        if (first.y == MISSING_ELEMENT || second.y == MISSING_ELEMENT)
            this.y = MISSING_ELEMENT;
        else
            this.y = first.y - second.y;
        if (first.z == MISSING_ELEMENT || second.z == MISSING_ELEMENT)
            this.z = MISSING_ELEMENT;
        else
            this.z = first.z - second.z;
        if (first.h == MISSING_ELEMENT || second.h == MISSING_ELEMENT)
            this.h = MISSING_ELEMENT;
        else
            this.h = first.h - second.h;
        if (first.d == MISSING_ELEMENT || second.d == MISSING_ELEMENT)
            this.d = MISSING_ELEMENT;
        else
            this.d = first.d - second.d;
        if (first.i == MISSING_ELEMENT || second.i == MISSING_ELEMENT)
            this.i = MISSING_ELEMENT;
        else
            this.i = first.i - second.i;
        if (first.f == MISSING_ELEMENT || second.f == MISSING_ELEMENT)
            this.f = MISSING_ELEMENT;
        else
            this.f = first.f - second.f;
        this.type = first.type;
        this.recorded_elements = first.recorded_elements;
        this.note_number = "";
    }
    
    /** create a year mean which is the interpolation of two other year means
     * @param year the point in time to interpolate at
     * @param ym1 the first year data
     * @param ym2 the second year data
     * @return the interpolated value */
    public YearMean (double year, YearMean ym1, YearMean ym2)
    {
        this ();
        
        double year_diff, proportion;
        
        this.year = year;
        year_diff = ym2.getYear() - ym1.getYear();
        if (year_diff == 0.0)
        {
            x = ym1.x;
            y = ym1.y;
            z = ym1.z;
            h = ym1.h;
            d = ym1.d;
            i = ym1.i;
            f = ym1.f;
        }
        else
        {
            proportion = (year - ym1.getYear()) / year_diff;
            x = ym1.x + ((ym2.x - ym1.x) * proportion);
            y = ym1.y + ((ym2.y - ym1.y) * proportion);
            z = ym1.z + ((ym2.z - ym1.z) * proportion);
            h = ym1.h + ((ym2.h - ym1.h) * proportion);
            d = ym1.d + ((ym2.d - ym1.d) * proportion);
            i = ym1.i + ((ym2.i - ym1.i) * proportion);
            f = ym1.f + ((ym2.f - ym1.f) * proportion);
        }
        if (ym1.getType() == ym2.getType())
            this.type = ym1.getType();
        else
            this.type = YearMeanType.UNKNOWN;
        if (ym1.getRecordedElements().equalsIgnoreCase(ym2.getRecordedElements()))
            this.recorded_elements = ym1.getRecordedElements();
        else
            this.recorded_elements = "";
        this.note_number = "";
    }    
    
    /** create a year mean */
    public YearMean (double year, double x, double y, double z, double h, double d,
                     double i, double f, YearMeanType type, String recorded_elements,
                     String note_number)
    {
        this (year);
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
        this.d = d;
        this.i = i;
        this.f = f;
        this.type = type;
        this.recorded_elements = checkElements (recorded_elements);
        if (this.recorded_elements == null) this.recorded_elements = "";
        this.note_number = note_number;
    }
    
    /** add two means - this mean retains the metadata (date, type, etc.)
     * @param o the mean to add - must be a JUMP type */
    /* don't add if missing data flag JE 13.09.10 */
    public void add (YearMean o)
    {
        if (o.getType() == YearMeanType.JUMP)
        {
            if(this.x != MISSING_ELEMENT) this.x += o.x;
            if(this.y != MISSING_ELEMENT)this.y += o.y;
            if(this.z != MISSING_ELEMENT)this.z += o.z;
            if(this.h != MISSING_ELEMENT)this.h += o.h;
            if(this.d != MISSING_ELEMENT)this.d += o.d;
            if(this.i != MISSING_ELEMENT)this.i += o.i;
            if(this.f != MISSING_ELEMENT)this.f += o.f;
            if (this.note_number.length() <= 0) this.note_number = o.note_number;
            else this.note_number = this.note_number + ", " + o.note_number;
        }
    }
    
    /** subtract two means - this mean retains the metadata (date, type, etc.)
     * @param o the mean to add - must be a JUMP type */
    /* don't subtract if missing data flag JE 13.09.10 */
    public void subtract (YearMean o)
    {
        if (o.getType() == YearMeanType.JUMP)
        {
            if(this.x != MISSING_ELEMENT) this.x -= o.x;
            if(this.y != MISSING_ELEMENT) this.y -= o.y;
            if(this.z != MISSING_ELEMENT) this.z -= o.z;
            if(this.h != MISSING_ELEMENT)this.h -= o.h;
            if(this.d != MISSING_ELEMENT)this.d -= o.d;
            if(this.i != MISSING_ELEMENT)this.i -= o.i;
            if(this.f != MISSING_ELEMENT)this.f -= o.f;
            if (this.note_number.length() <= 0) this.note_number = o.note_number;
            else this.note_number = this.note_number + ", " + o.note_number;
        }
    }
    
    public double getYear () { return year; }
    
    public Date getDate () 
    {
        int day_of_year, n_days, int_year;
        
        if (date == null)
        {
            int_year = (int) year;
            day_of_year = (int) ((year - (double) int_year) * (double) DateUtils.daysInYear(int_year));
            cal.set (int_year, 0, 1, 0, 0, 0);
            cal.set (GregorianCalendar.DAY_OF_YEAR, day_of_year);
            date = cal.getTime();
        }
        return date;
    }
    
    public double getX () { return x; }
    public double getY () { return y; }
    public double getZ () { return z; }
    public double getH () { return h; }
    public double getD () { return d; }
    public double getI () { return i; }
    public double getF () { return f; }
    public YearMeanType getType () { return type; }
    
    public String getTypeCode ()
    {
        switch (type)
        {
            case ALL_DAYS: return "A";
            case ALL_DAYS_PLUS_INCOMPLETE: return "B";
            case QUIET_DAYS: return "Q";
            case DISTURBED_DAYS: return "D";
            case JUMP: return "J";
            case INCOMPLETE: return "I";
        }
        return "U";
    }
    
    public String getTypeName ()
    {
        switch (type)
        {
            case ALL_DAYS: return "All days";
            case ALL_DAYS_PLUS_INCOMPLETE: return "All days plus incomplete days";
            case QUIET_DAYS: return "Quiet days";
            case DISTURBED_DAYS: return "Disturbed days";
            case JUMP: return "Jump";
            case INCOMPLETE: return "Incomplete";
        }
        return "Unknown";
    }
    
    public static YearMeanType parseMeanType (String display_type)
    {
        display_type = display_type.toUpperCase().trim().replace(" ", "").replace("\t", "").replace("_", "");
        if (display_type.equals("ALLDAYS"))
            return YearMeanType.ALL_DAYS;
        else if (display_type.equalsIgnoreCase("ALLDAYSPLUSINCOMPLETE"))
            return YearMeanType.ALL_DAYS_PLUS_INCOMPLETE;
        else if (display_type.equals("QUIETDAYS"))
            return YearMeanType.QUIET_DAYS;
        else if (display_type.equals("DISTURBEDDAYS"))
            return YearMeanType.DISTURBED_DAYS;
        else if (display_type.equals("INCOMPLETE"))
            return YearMeanType.INCOMPLETE;
        else if (display_type.equals("JUMP"))
            return YearMeanType.JUMP;
        
        return YearMeanType.UNKNOWN;
    }
    
    public String getRecordedElements () { return recorded_elements; }
    public String getNoteNumber () { return note_number; }
    
    /** get any of the components
     * @param component_code the component code - one of the COMPONENT_CODE_
     *        constants in GeomagAbsoluteValue
     * @return the component value */
    public double getComponent (int component_code)
    {
        switch (component_code)
        {
            case GeomagAbsoluteValue.COMPONENT_X: return x;
            case GeomagAbsoluteValue.COMPONENT_Y: return y;
            case GeomagAbsoluteValue.COMPONENT_Z: return z;
            case GeomagAbsoluteValue.COMPONENT_H: return h;
            case GeomagAbsoluteValue.COMPONENT_D: return d;
            case GeomagAbsoluteValue.COMPONENT_I: return i;
            case GeomagAbsoluteValue.COMPONENT_F: return f;
        }
        return MISSING_ELEMENT;
    }
    
    public int compareTo (YearMean o) 
    {
        if (this.year < o.year) return -1;
        if (this.year > o.year) return 1;
        if (this.type.ordinal() < o.type.ordinal()) return -1;
        if (this.type.ordinal() > o.type.ordinal()) return 1;
        return 0;
    }
    
    public boolean equals (YearMean o)
    {
        if (this.year != o.year) return false;
        if (this.type.ordinal() != o.type.ordinal()) return false;
        return true;
    }
    
    public String toString ()
    {
        return "Year=" + Double.toString (year) +
               ", D=" + Double.toString (d) +
               ", I=" + Double.toString (i) +
               ", H=" + Double.toString (h) +
               ", X=" + Double.toString (x) +
               ", Y=" + Double.toString (y) +
               ", Z=" + Double.toString (z) +
               ", F=" + Double.toString (f) +
               ", type=" + type.toString();
    }

    public static String checkElements (String ele)
    {
        if (ele.equalsIgnoreCase ("dhzf")) return "HDZF";
        if (ele.equalsIgnoreCase ("dhz")) return "HDZ";
        if (ele.equalsIgnoreCase ("hdz")) return ele;
        if (ele.equalsIgnoreCase ("hdzf")) return ele;
        if (ele.equalsIgnoreCase ("xyz")) return ele;
        if (ele.equalsIgnoreCase ("xyzf")) return ele;
        if (ele.equalsIgnoreCase ("dif")) return ele;
        if (ele.equalsIgnoreCase ("diff")) return ele;
        return null;
    }

}
