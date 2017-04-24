/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Represents a data interval or cadence.
 * @author sani
 */
public enum DataInterval {
    /**
     * DataInterval enum type literals.
     */
    HOUR("1-HOUR", ".hor", 3600000), MINUTE("1-MINUTE", "", 60000), SECOND("1-SECOND", "", 1000);

    private final String intervalString;
    private final String fileExtension;
    private final long period;

    private DataInterval(String intervalString, String fileExtension, long period){
        this.intervalString = intervalString;
        this.fileExtension = fileExtension;
        this.period = period;
        
    }

    /**
     * Returns the a string representation fot the data interval.
     * @return Data interval string.
     */
    public String intervalString() {
        return intervalString;
    }

    /**
     * Returns the file extension for data files of the data interval type.
     * @return The file extension.
     */
    public String fileExtension() {
        return fileExtension;
    }

    /**
     * Returns the period of the for the data type in milli seconds.
     * @return The data interval period in milli seconds.
     */
    public long period() {
        return period;
    }

}
