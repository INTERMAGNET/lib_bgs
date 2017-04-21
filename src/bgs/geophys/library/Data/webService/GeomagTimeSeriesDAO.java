/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Interface that provides a single read method to be implemented by data access objects.
 * @author sani
 */
public interface GeomagTimeSeriesDAO {

    public static final String INSTITUTE_NAME ="British Geological Survey (BGS)";

    /**
     * Reads data from a data source and uses that to create a GeomagTimeSeries object.
     * @return A GeomagTimeSeries object.
     * @throws bgs.geophys.library.Data.webService.GeomagDataAccessException
     */
    public GeomagTimeSeries read() throws GeomagDataAccessException;

}
