/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Enum type that represents a Geomag observatory
 *
 * @author sani
 */
public enum Station {

    LERWICK("Lerwick", "LER", 60.133, 358.816, 85, "HDZ", "1-SECOND"),
    ESKDALEMUIR("Eskdalemuir", "ESK", 55.317, 356.800, 245, "HDZ", "1-SECOND"),
    HARTLAND("Hartland", "HAD", 50.995, 355.518, 95, "HDZ", "1-SECOND");

    // the name of the observatory
    private final String name;
    //the code of the observaotry
    private final String code;
    //the latitude of the observatory
    private final double latitude;
    //the longitude of the observatory
    private final double longitude;
    //the elevation of the observatory
    private final double elevation;
    //a string representing the orientation of the sensor at the observatory
    private final String sensorOrientation;
    //a string representing the sample period at the observatory
    private final String samplePeriod;
    
    Station(String name, String code,double latitude, double longitude, 
            double elevation, String sensorOrientation, String samplePeriod){
        this.name = name;
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.sensorOrientation = sensorOrientation;
        this.samplePeriod = samplePeriod;
    }

    /**
     * Returns the full name of the observatory.
     * @return Station name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the code of the observatory.
     * @return Station code.
     */
    public String code() {
        return code;
    }

    /**
     * Returns the elevation of the observatory.
     * @return Station elevation.
     */
    public double elevation() {
        return elevation;
    }

    /**
     * Returns the latitude of the observatory.
     * @return Station .
     */
    public double latitude() {
        return latitude;
    }

    /**
     * returns the longitue of the observatory.
     * @return Station longitude.
     */
    public double longitude() {
        return longitude;
    }

    /**
     * Returns a string representing the sensor orientation at the observatory
     * @return Station sensor orientation
     */
    public String sensorOrientation() {
        return sensorOrientation;
    }

    /**
     * Returns a string representing the sample period at the observatory
     * @return Station sample period
     */
    public String samplePeriod() {
        return samplePeriod;
    }
}
