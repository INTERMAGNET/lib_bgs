package bgs.geophys.library.Data;

import java.util.Date;

/**
 * Represents a single point in a geomagnetic data timeseries, and
 * contains a time and value which characterizes the field at the
 * location that the timeseries data were sampled.
 *
 * @author Ewan Dawson
 */
public class GeomagDataPoint implements Comparable {

    private Date time;
    private GeomagAbsoluteValue value;

    /**
     * Main constructor, used to initialize members of this class,
     * which is immutable.
     *
     * @param time the UTC time at which the sample was recorded
     * @param value the value of the field
     */
    public GeomagDataPoint(Date time, GeomagAbsoluteValue value) {
        this.time = time;
        this.value = value;
    }

    /**
     * This constructor allows the initialization of a "missing"
     * sample - the value is instantiated, but has it's components
     * set to 'missing value' values.
     * Useful for creating a timeseries with no gaps.
     *
     * @param time the UTC time at which no sample was recorded.
     */
    public GeomagDataPoint(Date time) {
        this.time = time;
        this.value = new GeomagAbsoluteValue();
    }

    public Date getTime() { return this.time; }

    public GeomagAbsoluteValue getValue() { return this.value; }

    public String toString() {
        return this.time.toString() + " >> " + this.value.toString();
    }

    public int compareTo(Object o) {
        if (! (o instanceof GeomagDataPoint)) {
            throw new IllegalArgumentException("Comparator must be of type GeomagDataPoint");
        }
        return this.time.compareTo(((GeomagDataPoint) o).time);
    }
    
}
