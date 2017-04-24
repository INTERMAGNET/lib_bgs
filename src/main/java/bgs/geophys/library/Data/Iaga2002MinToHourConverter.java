/*
 * Iaga2002MinToHourConverter.java
 *
 * Created on 07 September 2007, 12:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Class to handle the conversion minute mean data in Iaga2002 format to
 * hourly mean data in the same format.
 * After instantiating the class, use method CreateHourlyMeanData() to
 * perform the conversion
 *
 * @author Ewan Dawson (ewan@bgs.ac.uk)
 */
public class Iaga2002MinToHourConverter {
    
    // This value is currently constant, as GeomagDataFormat does not currently
    // allow the maximum number of missing minute means allowed to be changed
    // outside the class, and defaults to 10% ( or 6 of 60 minute means).
    //private int maximumMissingAllowed = 6;
    
    /**
     * Creates a new instance of Iaga2002MinToHourConverter.
     * To perform a conversion, call the method CreateHourlyMeanData()
     */
    public Iaga2002MinToHourConverter() {
    }
    
    /**
     * Takes an Iaga2002 object containing minute mean data,
     * generates hourly means from the data, and returns the hourly means in
     * a new Iaga2002 object.
     * The number of minute values in the input object should be
     * exactly divisible by 60 (i.e. a whole number of hours of means)
     * The hourly means generated will be centered on minute 30.
     * Where more than 10% of the minute values are missing from the minute
     * means, the hourly means generated will have the value MISSING_COMPONENT.
     * @param minuteMeanData the Iaga2002 object containing the minute means for converstion.
     * @throws GeomagDataException if there is an error creating the hourly means.
     * @return a new Iaga2002 object containing the minute mean data.
     */
    public Iaga2002 CreateHourlyMeanData(Iaga2002 minuteMeanData) throws GeomagDataException {
        // Only accept Iaga2002 object containing minute mean data.
        if ( minuteMeanData.getIntervalTypeEnum() != Iaga2002.IntervalType.MINUTE) {
            // Not minute data
            throw new IllegalArgumentException("Incorrect interval type.  Data interval must be 1 minute.");
        }
        
        // 60-min blocks of minute mean data to be added to this object,
        // which will generate the houry means automatically
        // (MinuteData is an extension of GeomagDataFormat designed to accept
        // data in 60-element chunks.)
        MinuteData hourlyMeanGenerator = new MinuteData(
                minuteMeanData.getStationCode(),
                minuteMeanData.getStationName(),
                minuteMeanData.getLatitude(),
                minuteMeanData.getLongitude(),
                minuteMeanData.getElevation(),
                minuteMeanData.getComponentCodes(),
                minuteMeanData.getDataType(),
                minuteMeanData.getGINCode(),
                minuteMeanData.getInstituteName(),
                minuteMeanData.getSensorOrientation(),
                minuteMeanData.getSamplePeriodString(),
                minuteMeanData.getIntervalType(),
                minuteMeanData.getStationCode().length() == 4 ? true : false
                );
        
        // We need a calendar object for working out the time of each sample.
        GregorianCalendar hourStartTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        
        // Loop through the minute data in 60-min blocks (if there is an
        // incomplete 60-min block at the end, it will be ignored).
        for ( int hourIndex = 0; hourIndex < (int)(minuteMeanData.getDataLength()) / 60; hourIndex++ ) {
            double [][] hourData = new double[4][60];  // 4 arrays (1 for each element), each holding 60 1-minute means
            for ( int minuteIndex = 0; minuteIndex < 60; minuteIndex++ ) {
                for ( int elementIndex = 0; elementIndex < 4; elementIndex++ ) {
                    // store 1 minute mean, calculating the array index from the
                    // current 60-min block number and position within that block
                    hourData[elementIndex][minuteIndex] = minuteMeanData.getData(elementIndex, hourIndex*60 + minuteIndex);
                }
            }
            
            // Set hourStartTime to the time of the 1st sample in the current 60-min block
            hourStartTime.setTime(minuteMeanData.getStartDate());
            hourStartTime.add(Calendar.HOUR_OF_DAY, hourIndex);
            
            // Add the 60-min block of minute means to our hourlyMeanGenerator object.
            hourlyMeanGenerator.addData(hourStartTime.getTime(), 60000, Iaga2002.MISSING_DATA_SAMPLE, Iaga2002.MISSING_COMPONENT, hourData);
        }
        
        // Create an Iaga2002 object to fill with the generated hourly means.
        Iaga2002 hourMeanData = new Iaga2002(
                                             minuteMeanData.getStationCode(),
                                             minuteMeanData.getStationName(),
                                             minuteMeanData.getLatitude(),
                                             minuteMeanData.getLongitude(),
                                             minuteMeanData.getElevation(),
                                             minuteMeanData.getComponentCodes(),
                                             minuteMeanData.getDataType(),
                                             minuteMeanData.getInstituteName(),
                                             minuteMeanData.getSensorOrientation(),
                                             minuteMeanData.getSamplePeriodString(),
                                             "1-hour (00:00-00:59)"
                                            );
        
        hourStartTime.setTime(minuteMeanData.getDataSampleDate(0));
        //SimpleDateFormat timeFormatter = new SimpleDateFormat("MMM dd HH:mm");
        for ( int hourMeansIndex = 0; hourMeansIndex < hourlyMeanGenerator.getDataMeanLength(); hourMeansIndex++ ) {
            //System.out.println("Time of mean: " + timeFormatter.format(hourStartTime.getTime()));
            hourMeanData.addData(
                                 hourStartTime.getTime(),
                                 60 * 60 * 1000,                // 1 hour, counted in milliseconds.
                                 Iaga2002.MISSING_DATA_SAMPLE,
                                 Iaga2002.MISSING_COMPONENT,
                                 hourlyMeanGenerator.getDataMean(0, hourMeansIndex, Iaga2002.MISSING_DATA_SAMPLE),
                                 hourlyMeanGenerator.getDataMean(1, hourMeansIndex, Iaga2002.MISSING_DATA_SAMPLE),
                                 hourlyMeanGenerator.getDataMean(2, hourMeansIndex, Iaga2002.MISSING_DATA_SAMPLE),
                                 hourlyMeanGenerator.getDataMean(3, hourMeansIndex, Iaga2002.MISSING_DATA_SAMPLE)
                                );
            hourStartTime.add(Calendar.HOUR_OF_DAY, 1); // Increment the calender by one hour.
        }
        
        return hourMeanData;
    }

    /**
     * The maximum number of missing minute mean values permitted in one
     * interval over which the mean is calculated.  If the number of missing
     * means is greater than this number, the hourly mean will be recorded as
     * MISSING_DATA_SAMPLE
     */
    //public int getMaximumMissingAllowed() {
    //    return maximumMissingAllowed;
    //}

    // Setter is disabled, because GeomagDataFormat does not currently allow
    // the percentage missing data allowed to be changed from outside the class.
//    public void setMaximumMissingAllowed(int MaximumMissingAllowed) {
//        if ( MaximumMissingAllowed < 0 || MaximumMissingAllowed > 59 ) {
//            throw new IllegalArgumentException("MaximumMissingAllowed must be between 0 and 59.");
//        }
//        this.maximumMissingAllowed = MaximumMissingAllowed;
//    }
    
}
