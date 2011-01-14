/*
 * Iaga2002ToImagConverter.java
 *
 * Created on 04 May 2007, 15:08
 *
 */

package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.GeomagDataFormat.IntervalType;
import bgs.geophys.library.Data.Iaga2002;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * This class can be used to convert Iaga2002 objects to corresponding
 * ImagBinaryCDFile objects, singly or in batches.
 * Before converting, existing ImagBinaryCDFile objects may be added to the
 * Converter, and will be used to store data from any Iaga2002 where the time of
 * the data samples falls within the range of that ImagBinaryCDFile object.
 *
 * @author ewan
 * @version 1.1
 */
public class Iaga2002ToImagCDConverter {
    
    private Vector<ImagCDFile> imagFiles;
    private Vector<Iaga2002> iaga2002Files;
    private int DConversion, K9Limit;
    private String instituteCode, qualityCode, instrumentCode;
    private Date version;
    
    private static Logger messageLogger = Logger.getLogger(Iaga2002ToImagCDConverter.class.getName());
    static {
        messageLogger.setLevel(Logger.getLogger("").getHandlers()[0].getLevel());
    }
    
    /** Creates a new instance of Iaga2002ToImagConverter */
    public Iaga2002ToImagCDConverter() {
        this.imagFiles = new Vector<ImagCDFile>(12,12);
    }
    
    /**
     * Getter for the collection of ImagBinaryCDFile objects held by the class.
     *  The collection eill be empty upon instantiation of the class, and
     *  will contain the results of the format convertions after a call
     *  to one of the conversion methods has been made.
     *  Use the add() method of the collection returned to add ImagBinaryCDFile objects
     *  to the collection before calling one of the the conversion methods.  The
     *  converted data will be added to the ImagBinaryCDFile objects if they are suitable.
     *  i.e. cover an appropriate period and do not contain data from a different observatory.
     *
     * @return The collection of ImagBinaryCDFile objects held by the class.
     */
    public Vector<ImagCDFile> getImagFiles() {
        return this.imagFiles;
    }
    
    /**
     * Takes an Iaga2002 object and converts it
     *  into a collection of ImagBinaryCDFile objects.
     *  The Imag format data header contains fields not found in the IAGA2002 format
     *  secification, so these must be supplied by the user.
     *  To have the converted data added to an existing ImagBinaryCDFile object, add
     *  the object to collection returned by the getImagFiles() method of this class.
     *
     * @param iaga2002 the iaga2002 object to be converted
     * @param DConversion is the DConversion code to be used in the Imag files
     * @param instituteCode is a 4-character string representing the institution responsible for the data, e.g. BGS, NOAA...
     * @param qualityCode in a 4-character string representing the status of the data in terms of QC processing.
     * @param instrumentCode is a 4 character string describing the instrumentation used to perform the measurements.
     * @param K9Limit the K9 limit to use (cannot be determined from the Iaga2002 object)
     * @param version is the date (month and year) representing the version of the dataset
     * @return a Vector of ImagBinaryCDFile objects containing the converted Iaga2002 data.
     */
    public Vector<ImagCDFile> convert(Iaga2002 iaga2002, int DConversion, String instituteCode,
                                            String qualityCode, String instrumentCode, int K9Limit, Date version) {
        // Treat the single file as a batch of 1 file to perform conversion.
        List<Iaga2002> iaga2002List = new ArrayList<Iaga2002>(1);
        iaga2002List.add(iaga2002);
        return this.convertBatch(iaga2002List, DConversion, instituteCode, qualityCode, instrumentCode, K9Limit, version);
    }
    
    /**
     * Takes collection of Iaga2002 objects and converts them
     *  into a collection of ImagBinaryCDFile objects.
     *  The Imag format data header contains fields not found in the IAGA2002 format
     *  secification, so these must be supplied by the user.
     *  To have the converted data added to an existing ImagBinaryCDFile object, add
     *  the object to collection returned by the getImagFiles() method of this class.
     * 
     * @param iaga2002Batch is a List of Iaga2002 files that are to be converted into a set of ImagBinaryCD files.
     * @param DConversion is the DConversion code to be used in the Imag files
     * @param instituteCode is a 4-character string representing the institution responsible for the data, e.g. BGS, NOAA...
     * @param qualityCode in a 4-character string representing the status of the data in terms of QC processing.
     * @param instrumentCode is a 4 character string describing the instrumentation used to perform the measurements.
     * @param K9Limit the K9 limit to use (cannot be determined from the Iaga2002 object)
     * @param version is the date (month and year) representing the version of the dataset
     * @return a Vector of ImagBinaryCDFile objects containing the converted Iaga2002 data.
     */
    public Vector<ImagCDFile> convertBatch(List<Iaga2002> iaga2002Batch, int DConversion, String instituteCode,
                                            String qualityCode, String instrumentCode, int K9Limit, Date version) {
        // Initialize the class variables
        this.iaga2002Files = new Vector<Iaga2002>(iaga2002Batch);
        this.DConversion = DConversion;
        this.instituteCode = instituteCode;
        this.qualityCode = qualityCode;
        this.instrumentCode = instrumentCode;
        this.K9Limit = K9Limit;
        this.version = version;
        
        this.performConversion();
        
        return this.imagFiles;
    }
    
    /** This method is the engine of the class, doing all the converstion work.
     *  It takes no parameters as it uses the class level variables that were initialized
     *  When convertBatch() was called.
     */
    private void performConversion() {
        
        messageLogger.info("Beginning conversion of " + iaga2002Files.size() + " files.");
        
        // Create an iterator to search through the source Iaga2002 files
        Iterator<Iaga2002> iaga2002Iter = iaga2002Files.iterator();
        while( iaga2002Iter.hasNext() ) {
            Iaga2002 currIaga2002 = iaga2002Iter.next();
            if ( hasSuitableDataInterval(currIaga2002) ) { // Skip annual. monthly or second data, for example
                
                // Retrieve the data interval type of this Iaga2002 file - we'll need it later on.
                IntervalType intervalType = currIaga2002.getIntervalTypeEnum();
                
                // Initialize the objects we will need to process the Iaga files
                ImagCDFile currImag = new ImagCDFile();  // initialize with null ImagBinaryCDFile
                ImagCDDataDay currDataDay = new ImagCDDataDay();  // initialize with null DataDay
                GregorianCalendar sampleDate = new GregorianCalendar (TimeZone.getTimeZone("GMT"));
                
                // Loop through each sample in the Iaga2002 file in turn
                for ( int currSample = 0; currSample < currIaga2002.getDataLength(); currSample++ ) {
                    sampleDate.setTime(currIaga2002.getDataSampleDate(currSample)); // Retreive the date of the sample we are looking at
                    

                    /*** UPDATE currImag to point to valid ImagBinaryCDFile ***/
                    // Does this sample go in the ImagBinaryCDFile we are currently working on?
                    int sampleYear = sampleDate.get(Calendar.YEAR);
                    int sampleMonth = sampleDate.get(Calendar.MONTH);
                    if ( ! ( ( sampleMonth == currImag.getMonth() ) && ( sampleYear == currImag.getYear() ) ) ) {
                        // We are looking at a different month - we need to find another file to put this data in,
                        // or initialise a new ImagBinaryCDFile.
                        currImag = findSuitableImagFile(currIaga2002.getStationCode(), sampleDate.getTime());
                        if ( currImag == null ) {
                            // No suitable file found, so we must initialize one.
                            messageLogger.finer("Started new ImagBinaryCDFile.  Date: " + String.format("%1$tY/%1$tb", currIaga2002.getDataSampleDate(currSample)));
                            currImag = new ImagCDFile(sampleYear, sampleMonth);
                            currImag.setFileChanged();
                            this.imagFiles.add(currImag);
                        }
                    }
                    
                    
                    /*** UPDATE currDataDay o point to correct DataDay - populate header fields if necessary ***/
                    // Does this sample go in the DataDay we are currently working on?
                    int sampleDayOfYear = sampleDate.get(Calendar.DAY_OF_YEAR);
                    if ( sampleDayOfYear != currDataDay.getDayNumber() ) {
                        
                        messageLogger.finer("Started new DataDay. StationID: " + currIaga2002.getStationCode() + " Day of Year: "+ sampleDayOfYear + ". (" + String.format("%1$td %1$tb", currIaga2002.getDataSampleDate(currSample)) + ").");
                        
                        // We have moved onto another day.  Point currDataDay to the DataDay object corresponding to the new day.
                        currDataDay = currImag.getDataDay(sampleDate.get(Calendar.DAY_OF_MONTH));
                        // Set the header fields for this day using both the Iaga2002 header and the fields supplied in the parameters
                        currDataDay.setStationID(currIaga2002.getStationCode());
                        currDataDay.setYear(sampleYear);
                        currDataDay.setDayNumber(sampleDayOfYear);          // AKA Julian Day
                        currDataDay.setColatitude(doubleToScaledInteger(latitudeToColatitude(currIaga2002.getLatitude()), 1000));
                        currDataDay.setLongitude(doubleToScaledInteger(currIaga2002.getLongitude(), 1000));
                        currDataDay.setElevation(doubleToScaledInteger(currIaga2002.getElevation(), 1));
                        // IMCDVIEW requires recordedElements to be four characters, so append 'F' if it is missing from component code
                        String componentCodes = currIaga2002.getComponentCodes();
                        if ( !componentCodes.endsWith("F") && componentCodes.length() < 4 ) { componentCodes = componentCodes + "F"; }
                        currDataDay.setRecordedElements(componentCodes);           // AKA Orientation
                        currDataDay.setInstituteCode(this.instituteCode);                             // AKA Origin
                        currDataDay.setDConversion(this.DConversion);
                        currDataDay.setQualityCode(this.qualityCode);                                 // AKA Data Quality
                        currDataDay.setInstrumentCode(this.instrumentCode);
                        currDataDay.setK9Limit(this.K9Limit);
                        {
                            int instrumentSamplingPeriod = currIaga2002.getInstrumentSamplingPeriod();
                            if ( instrumentSamplingPeriod < 0 ) currDataDay.setSamplePeriod(0);
                            else currDataDay.setSamplePeriod(instrumentSamplingPeriod);         // AKA Digital Sample Rate
                        }
                        // IMCDVIEW requires sensorOrientation to be four characters, so append 'F' if it is missing
                        String sensorOrientation = currIaga2002.getSensorOrientation();
                        if ( !sensorOrientation.endsWith("F") && sensorOrientation.length() < 4 ) { sensorOrientation = sensorOrientation + "F"; }
                        currDataDay.setSensorOrientation(sensorOrientation);
                        currDataDay.setPublicationDate(this.version);
                    }
                    
                    
                    /*** READ each component from the current Iaga2002 sample, and  ***
                     *** WRITE the value into the ImagBinaryCDFile                  ***/
                    // get each component in turn
                    for ( int currComponent = 0; currComponent < 4; currComponent++ ) {

                        // If the value of the current component of the current observation is a
                        // missing data or missing component value, convert to the equivalent
                        // iaga missing data value.  If not, convert the iaga double format to the
                        // integer format used to store the values in the imag file.
                        double iagaValue = currIaga2002.getData(currComponent, currSample);
                        int imagValue;
                        if (Iaga2002.isMissingComponent(iagaValue) || Iaga2002.isMissingSample(iagaValue))
                            imagValue = ImagCDDataDay.MISSING_DATA;
                        else 
                            imagValue = doubleToScaledInteger(iagaValue, 10);

                        // Put the converted value into the correct place in the imag file,
                        // according to the date and interval of the data, and what component it is.
                        int offset;
                        switch ( intervalType ) {
                            case MINUTE :
                                offset = ( sampleDate.get(Calendar.HOUR_OF_DAY) * 60 ) + sampleDate.get(Calendar.MINUTE);
                                currDataDay.setMinuteData(imagValue, currComponent, offset);
                                break;
                            case HOUR :
                                offset = sampleDate.get(Calendar.HOUR_OF_DAY);
                                currDataDay.setHourData(imagValue, currComponent, offset);
                                break;
                            case DAY :
                                currDataDay.setDayData(imagValue, currComponent);
                                break;
                        }
                    }
                }
            }
        }


        // Now loop though created ImagCDFile objects, filling in missing headers
        // on empty days (assume the earliest valid header can be copied
        // to missing headers).
        Iterator<ImagCDFile> imagCdFileIter = this.imagFiles.iterator();
        while( imagCdFileIter.hasNext() ) {
            
            ArrayList<Integer> emptyDaysIdx = new ArrayList<Integer>();
            byte[] nonEmptyDayHeaderBytes = null;
            ImagCDFile imagCdFile = imagCdFileIter.next();
            
            messageLogger.fine("Looking for empty days in ImagCDFile " + imagCdFile.getStartDate());
            
            // Find all of the empty days, and also find one non-empty day
            for ( int i = 1; i <= imagCdFile.getNDays(); i++ ) {
                ImagCDDataDay day = imagCdFile.getDataDay(i);
                
                if ( day.getStationID().equals("") ) {
                    // This day is empty, store index in emptyDaysIdx
                    emptyDaysIdx.add(i);
                
                } else if ( nonEmptyDayHeaderBytes == null) {
                    // This day is non-empty, so store header bytes
                    // in nonEmptyDayHeaderBytes
                    messageLogger.fine("Found non-empty day at day " + i);
                    nonEmptyDayHeaderBytes = new byte[64];
                    for ( int byteIdx = 0; byteIdx < 64; byteIdx++ ) {
                        nonEmptyDayHeaderBytes[byteIdx] = day.getHeaderByte(byteIdx);
                    }
                }
            }
            
            messageLogger.fine("Found " + emptyDaysIdx.size() + " empty days.");
            
            // Copy the header of the non-empty day into the header of
            // the empty days.
            if ( emptyDaysIdx.size() > 0 && nonEmptyDayHeaderBytes != null ) {
                messageLogger.fine("Copying header from non-empty day into empty days.");
                Iterator<Integer> emptyDaysIter = emptyDaysIdx.iterator();
                while( emptyDaysIter.hasNext() ) {
                    // Copy the header of the non-empty day into the header
                    // of this empty day.
                    ImagCDDataDay emptyDay = imagCdFile.getDataDay(emptyDaysIter.next());
                    for ( int byteIdx = 0; byteIdx < 64; byteIdx++ ) {
                        emptyDay.setHeaderByte(nonEmptyDayHeaderBytes[byteIdx], byteIdx);
                    }
                }
            }
            messageLogger.fine("Finished with file " + imagCdFile.getStartDate());
            
        }
        
        messageLogger.info("Finished file conversion.");
    }
    
    /** Iterates through the class-level Vector of IagaBinaryCDFile objects, and return one of them
     *  if it can be used to hold the data for the specified date and station code.
     *  Two conditions must be met:
     *  1) The date of the sample must fall within the period covered by the ImagCDBinaryFile object.
     *  2) The header of the first DataDay of the ImagCDBinaryFile object must contain no Station Code,
     *     or the same station code as the sample to be inserted.
     */
    private ImagCDFile findSuitableImagFile(String stationCode, Date sampleDate) {
        // Create an iterator to go through all the imag files
        Iterator<ImagCDFile> imagIter = this.imagFiles.iterator();
        while ( imagIter.hasNext() ) {
            ImagCDFile currImag = imagIter.next();
            if ( (currImag.getStartDate().getTime() <= sampleDate.getTime()) // Does the start of the iaga2002 data lie within the boundary
                 && (currImag.getEndDate().getTime() > sampleDate.getTime())   // dates of this imagcdbinary file?
                 // Do the Iaga2002 file and the ImagCDBinary file refer to the same observatory?
                 && ((currImag.getDataDay(1).getStationID().trim().equalsIgnoreCase(stationCode)) ||
                    currImag.getDataDay(1).getStationID().trim().equals("")))
                // Yes, the data from this iaga2002 file fits in this imag file.
                return currImag;
        }
        return null;
    }
    
    /** Returns true if the data in the Iaga2002 file may be stored in an ImagBinaryCDFile.
     *  This is true when the Iaga2002 file holds minute, hourlym or daily data.
     */
    private boolean hasSuitableDataInterval(Iaga2002 iaga2002) {
        //ImagBinaryCDFiles can only hold minute, hourly, and daily data
        switch ( iaga2002.getIntervalTypeEnum() ) {
            case MINUTE :
            case HOUR :
            case DAY :
                return true;
        }
        return false;
    }
    
    /** Converts a double value the scaled integer (implied decimal point) format
     *  used in the ImagBinaryCDFile.  Where a loss of precision occurs, the value
     *  rounded using ROUND_EVEN
     */
    private int doubleToScaledInteger(double value, int scaleFactor) {
        return BigDecimal.valueOf(value * scaleFactor).setScale(0, RoundingMode.HALF_EVEN).intValue();
    }
    
    /** Converts latitude to colatitude
     */
    private double latitudeToColatitude(double latitude) {
        if ( latitude < 0 ) return -90 - latitude;
        else return 90 -latitude;
    }
    
    /* Converts colatitude to latitude
     */
    private double colatitudeToLatitude(double colatitude) {
        return latitudeToColatitude(colatitude);
    }
}
