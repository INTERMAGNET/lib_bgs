/*
 * ExportThread.java
 *
 * Created on 10 February 2007, 16:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataDay;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataMonth;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatory;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatoryIterator;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.ExportDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.ExportOptionsDialog;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.GeomagDataException;
import bgs.geophys.library.Data.GeomagDataFormat;
import bgs.geophys.library.Data.IMFV122;
import bgs.geophys.library.Data.WDCHour;
import bgs.geophys.library.Data.WDCMinute;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Data.Iaga2002;
import bgs.geophys.library.Data.PlainData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A thread for exporting CD data to other formats. Controlled by the export
 * dialog.
 *
 * @author smf
 */
public class ExportThread 
implements Runnable
{
 
    /** interface used to monitor progress and allow user to cancel */
    public interface progressAndCancel
    {
        /** code for the type of error: WARNING */
        public static final int ERROR_WARNING = 1;
        /** code for the type of error: ERROR */
        public static final int ERROR_ERROR = 2;
        /** code for the type of error: FATAL */
        public static final int ERROR_FATAL = 3;
        
        /** display progress information */
        public void showProgress (String message, String obsy,
                                  int total_percent, int operation_percent);
        /** check for cancel */
        public boolean isCancelled ();
        /** display an error message
         * @param message the message to display
         * @param type one of the error type codes */
        public void registerError (String message, int type);
        /** notify that export is complete */
        public void allDone ();
    }
    
    /** codes for what to export: daily data */
    public static final int PERIOD_DAY = 1;
    /** codes for what to export: hourly data */
    public static final int PERIOD_HOUR = 2;
    /** codes for what to export: minute data */
    public static final int PERIOD_MINUTE = 3;
    
    /** codes for export format: IAGA 2002 */
    public static final int FORMAT_IAGA2002 = 1;
    /** codes for export format: WDC */
    public static final int FORMAT_WDC = 2;
    /** codes for export format: INTERMAGNET */
    public static final int FORMAT_INTERMAGNET = 3;
    /** codes for export format: Plain text */
    public static final int FORMAT_PLAIN_TEXT = 4;
    
    /** codes for zip type: Don't use a zip file */
    public static final int ZIP_NONE = 1;
    /** codes for zip type:  */
    public static final int ZIP_DAILY = 2;
    /** codes for zip type:  */
    public static final int ZIP_MONTHLY = 3;
    /** codes for zip type:  */
    public static final int ZIP_YEARLY = 4;
    /** codes for zip type:  */
    public static final int ZIP_OBSERVATORY = 5;
    /** codes for zip type:  */
    public static final int ZIP_ALL = 6;
    
    // data variables
    private String output_dir;
    
    // the number of components in the output file - this will always be 4 as
    // there are always 4 components in CD data
    private final int N_COMPONENTS = 4;
    
    // list of observatories to export
    private CDObservatoryIterator.ObservatoryInfo obsyList [] = null;
    // the type of data to export: MINUTE, HOUR or DAY
    private int dataType;
    // data format: IAGA2002, INTERMAGNET, WDC, Plain
    private int dataFormat;
    // a code for the GIN (used by INTERMAGNET IMF data format)
    private String gin_code;
    // date of first day of data - day (0..30), month (0..11)
    private int startDay, startMonth, startYear;
    // the duration (in milliseconds) for the entire data request
    private long duration;
    // the duration of individual data files - this varies
    // between data formats and duration types - this variable
    // contains DateUtils.DURATION_TYPE_... codes
    private int fileLength;
    // how to group the output data files into zip archives
    private int zipType;
    // flags for use of zipped and un-zipped data files
    private boolean useZip;
    private boolean usePlain;
    // directory of data source to use (or null to use all data sources)
    private String base_dir;
    
    // options
    // what to put in the fourth element: GeomagAbsoluteValue.COMPONENT_F, 
    // GeomagAbsoluteValue.COMPONENT_F_SCALAR, GeomagAbsoluteValue.COMPONENT_F_DIFF
    private int fourthElement;
    // value for iaga 2002 'digital sampling' header field at different sample rates
    private String iaga_2002_minute_digital_sampling;
    private String iaga_2002_hour_digital_sampling;
    private String iaga_2002_day_digital_sampling;
    // value for iaga 2002 'interval type' header field at different sample rates
    private String iaga_2002_minute_interval_type;
    private String iaga_2002_hour_interval_type;
    private String iaga_2002_day_interval_type;
    
    // object to notify of progress
    private progressAndCancel notify;
    
    /** Creates a new instance of ExportThread
     * @param obsyList the list of observatories to export
     * @param startDay the start day of data to export (1..31)
     * @param startMonth the start month of data to export (1..12)
     * @param startYear the start year of data to export
     * @param dataType MINUTE, HOUR or DAY data
     * @param dataFormat IAGA2002, Plain, WDC or INTERMAGNET
     * @param duration the amount of data to export
     * @param durationUnits one of the DateUtils DURATION_TYPE codes - 
     *                      only DAYS, MONTHS or YEARS are recognised
     * @param fileLength one of the DateUtils DURATION_TYPE codes - 
     *                   only DAYS, MONTHS, YEARS or INFINITE are recognised
     * @param zipLength none, daily, monthly, yearly, observatory, all
     * @param useZip if true use zipped data files
     * @param usePlain if true use un-zipped data files
     * @param base_dir - if the export should be restricted to one data source
     *        then set this to the path of the data source, otherwise set it to null
     * @param notify object used to send notification messages to
     * @throws an IO exception if there was a problem with the parameters -
     *         the exception will always contain a message */
    public ExportThread(CDObservatoryIterator.ObservatoryInfo obsyList [],
                        int startDay, int startMonth, int startYear,
                        int dataType, int dataFormat, String gin_code,
                        int duration, int durationUnits,
                        int fileLength, int zipType,
                        boolean useZip, boolean usePlain,
                        String base_dir,
                        progressAndCancel notify) 
    throws IOException
    {
        long time1, time2;
        GregorianCalendar calendar;
        
        // check and store parameters
        this.output_dir = GeomagDataFormat.checkPrefix (GlobalObjects.export_output_dir.getAbsolutePath(),
                                                        true, true);
        this.obsyList = obsyList;
        this.startDay = startDay -1;
        this.startMonth = startMonth -1;
        this.startYear = startYear;
        this.dataType = dataType;
        switch (dataType)
        {
            case PERIOD_MINUTE: break;
            case PERIOD_HOUR:   break;
            case PERIOD_DAY:    break;
            default:            throw new IOException ("Unknown data period (code " + Integer.toString (dataType) + ")");
        }
        this.dataFormat = dataFormat;
        this.gin_code = gin_code;
        this.fileLength = fileLength;
        switch (fileLength)
        {
            case DateUtils.DURATION_TYPE_DAYS:
            case DateUtils.DURATION_TYPE_MONTHS:
            case DateUtils.DURATION_TYPE_YEARS:
            case DateUtils.DURATION_TYPE_INFINITE:
                break;
            default:
                throw new IOException ("Bad file length code (code " + Integer.toString (fileLength) +")");
        }
        this.zipType = zipType;
        this.useZip = useZip;
        this.usePlain = usePlain;
        this.base_dir = base_dir;
        this.notify = notify;
        
        // get export options
        this.fourthElement = ExportOptionsDialog.getFourthElementProperty();
        this.iaga_2002_minute_digital_sampling = ExportOptionsDialog.getIaga2002MinuteDigitalSampling();
        this.iaga_2002_hour_digital_sampling = ExportOptionsDialog.getIaga2002HourDigitalSampling();
        this.iaga_2002_day_digital_sampling = ExportOptionsDialog.getIaga2002DayDigitalSampling();
        this.iaga_2002_minute_interval_type = ExportOptionsDialog.getIaga2002MinuteIntervalType();
        this.iaga_2002_hour_interval_type = ExportOptionsDialog.getIaga2002HourIntervalType();
        this.iaga_2002_day_interval_type = ExportOptionsDialog.getIaga2002DayIntervalType();
        
        // check that the data type and file length
        // are supported for the given data format (also check the
        // data format code is valid)
        switch (dataFormat)
        {
            case FORMAT_IAGA2002:    
                break;
            case FORMAT_INTERMAGNET: 
                if (dataType != PERIOD_MINUTE)
                    throw new IOException ("INTERMAGNET format only supports minute means");
                if (fileLength != DateUtils.DURATION_TYPE_DAYS)
                    throw new IOException ("INTERMAGNET data must be written in day files");
                break;
            case FORMAT_PLAIN_TEXT:  
                break;
            case FORMAT_WDC:
                if (dataType == PERIOD_DAY)
                    throw new IOException ("Daily means cannot be written in WDC format");
                break;
            default:
                throw new IOException ("Unknown data format (code " + Integer.toString (dataFormat) + ")");
        }
        
        // check that the zip grouping
        switch (zipType)
        {
            case ZIP_NONE:
                break;
            case ZIP_DAILY:
                if (fileLength != DateUtils.DURATION_TYPE_DAYS)
                    throw new IOException ("Daily zip files require day duration data files");
                break;
            case ZIP_MONTHLY:
                if (fileLength != DateUtils.DURATION_TYPE_DAYS &&
                    fileLength != DateUtils.DURATION_TYPE_MONTHS)
                    throw new IOException ("Monthly zip files require day or month duration data files");
                break;
            case ZIP_YEARLY:
                if (fileLength == DateUtils.DURATION_TYPE_INFINITE)
                    throw new IOException ("Yearly zip files require day, month or year duration data files");
            case ZIP_OBSERVATORY:
                break;
            case ZIP_ALL:
                break;
            default:
                throw new IOException ("Unknown zip grouping (code " + Integer.toString (zipType) + ")");
        }
        
        // calculate duration (the length of time to the start/end of data
        // as specified by the user, then adjust start date and duration according to 
        // the length of data supported by the file into which data will be written
        this.duration = DateUtils.getDurationMs (duration, durationUnits, this.startMonth, this.startYear);
        switch (fileLength)
        {
            case DateUtils.DURATION_TYPE_DAYS:
                // no adjustments needed
                break;
                
            case DateUtils.DURATION_TYPE_MONTHS:
                // adjust date and duration to the start of the month
                this.duration += DateUtils.getDurationMs (this.startDay, DateUtils.DURATION_TYPE_DAYS, this.startMonth, this.startYear);
                this.startDay = 0;
                
                // adjust duration to be in complete months
                calendar = new GregorianCalendar (DateUtils.gmtTimeZone);
                calendar.setLenient (true);
                calendar.clear ();
                calendar.set (this.startYear, this.startMonth, this.startDay +1, 0, 0, 0);
                calendar.set (GregorianCalendar.MILLISECOND, 0);
                calendar.add (GregorianCalendar.DAY_OF_MONTH, (int) (this.duration / 86400000l));
                time1 = calendar.getTimeInMillis();
                if (calendar.get (GregorianCalendar.DAY_OF_MONTH) != 1)
                {
                    calendar.set (GregorianCalendar.DAY_OF_MONTH, 1);
                    calendar.add (GregorianCalendar.MONTH, 1);
                }
                time2 = calendar.getTimeInMillis();
                this.duration += time2 - time1;
                break;

            case DateUtils.DURATION_TYPE_YEARS:
                // adjust date and duration to the start of the year
                this.duration += DateUtils.getDurationMs (this.startDay, DateUtils.DURATION_TYPE_DAYS, this.startMonth, this.startYear);
                this.startDay = 0;
                this.duration += DateUtils.getDurationMs (this.startMonth, DateUtils.DURATION_TYPE_MONTHS, this.startMonth, this.startYear);
                this.startMonth = 0;
                
                // adjust duration to be in complete years
                calendar = new GregorianCalendar (DateUtils.gmtTimeZone);
                calendar.setLenient (true);
                calendar.clear ();
                calendar.set (this.startYear, this.startMonth, this.startDay +1, 0, 0, 0);
                calendar.set (GregorianCalendar.MILLISECOND, 0);
                calendar.add (GregorianCalendar.DAY_OF_MONTH, (int) (this.duration / 86400000l));
                time1 = calendar.getTimeInMillis();
                if (calendar.get (GregorianCalendar.DAY_OF_MONTH) != 1)
                {
                    calendar.set (GregorianCalendar.DAY_OF_MONTH, 1);
                    calendar.add (GregorianCalendar.MONTH, 1);
                }
                if (calendar.get (GregorianCalendar.MONTH) != 0)
                {
                    calendar.set (GregorianCalendar.MONTH, 0);
                    calendar.add (GregorianCalendar.YEAR, 1);
                }
                time2 = calendar.getTimeInMillis();
                this.duration += time2 - time1;
                break;
                
            case DateUtils.DURATION_TYPE_INFINITE:
                // no adjustments needed
                break;
                
            default:
                throw new IOException ("Internal software fault: file duration set to unrecognised code (" + Integer.toString (fileLength) + ")");
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    // code below here runs in the export thread
    //////////////////////////////////////////////////////////////////////////////////
    
    /** implement run to create a new thread */
    public void run ()
    {
        export_data ();
    }

    /** export minute data according to the preset parameters */
    private void export_data ()
    {
        int obs_count, day, month, year, n_missing_samps_to_add, n_samps_per_day;
        int n_msg_date_fields, fileStartDay, fileStartMonth, fileStartYear;
        int obsy_total_percent, obsy_percent, next_obsy_total_percent, total_percent;
        Date date;
        long duration_count, sample_period;
        boolean write_file, output_to_zip;
        String sample_period_string, interval_type, obsy_code, last_zip_filename;
        String zip_filename, data_type_code, sample_period_string2;
        CDDataMonth month_data [];
        CDDataDay day_data;
        CDObservatory observatory;
        Iaga2002 iaga2002;
        PlainData plainData;
        IMFV122 imfv122;
        WDCMinute wdcMinute;
        WDCHour wdcHour;
        GeomagDataFormat formatObject;
        ZipOutputStream zip_stream;
        ZipEntry zip_entry;
        SimpleDateFormat date3field, date2field, date1field;
        
        // set up date formats for zip files
        date3field = new SimpleDateFormat ("yyyyMMdd");
        date2field = new SimpleDateFormat ("yyyyMM");
        date1field = new SimpleDateFormat ("yyyy");
        
        // set up some header fields
        switch (dataType)
        {
        case PERIOD_MINUTE:
            sample_period_string = this.iaga_2002_minute_digital_sampling;
            interval_type = this.iaga_2002_minute_interval_type;
            if (interval_type.length () <= 0) interval_type = ExportOptionsDialog.getDefaultIaga2002MinuteIntervalType();
            sample_period = 60000l;
            n_samps_per_day = 1400;
            data_type_code = "m";
            break;
        case PERIOD_HOUR:
            sample_period_string = this.iaga_2002_hour_digital_sampling;
            interval_type = this.iaga_2002_hour_interval_type;
            if (interval_type.length() <= 0) interval_type = ExportOptionsDialog.getDefaultIaga2002HourIntervalType();
            sample_period = 3600000l;
            n_samps_per_day = 24;
            data_type_code = "h";
            break;
        case PERIOD_DAY:
        default: // to keep compiler happy
            sample_period_string = this.iaga_2002_day_digital_sampling;
            interval_type = this.iaga_2002_day_interval_type;
            if (interval_type.length() <= 0) interval_type = ExportOptionsDialog.getDefaultIaga2002DayIntervalType();
            sample_period = 86400000l;
            n_samps_per_day = 1;
            data_type_code = "d";
            break;
        }
        switch (fileLength)
        {
        case DateUtils.DURATION_TYPE_DAYS:
            n_msg_date_fields = 3;
            break;
        case DateUtils.DURATION_TYPE_MONTHS:
            n_msg_date_fields = 2;
            break;
        case DateUtils.DURATION_TYPE_YEARS:
            n_msg_date_fields = 1;
            break;
        case DateUtils.DURATION_TYPE_INFINITE:
            n_msg_date_fields = 3;
            break;
        default: // to keep compiler happy
            n_msg_date_fields = 1;
            break;
        }

        zip_stream = null;
        try
        {
            // for each observatory and while we haven't been cancelled ...
            zip_filename = "";
            for (obs_count = 0; 
                 (obs_count < obsyList.length) && (! notify.isCancelled()); 
                 obs_count ++)
            {
                obsy_total_percent = (obs_count * 100) / obsyList.length;
                next_obsy_total_percent = ((obs_count +1) * 100) / obsyList.length;
                obsy_code = obsyList[obs_count].GetObservatoryCode();
                
                // flag the start of a new file
                fileStartDay = fileStartMonth = fileStartYear = -1;
                
                // initialise the date fields and get the first month's data
                day = startDay;
                month = startMonth;
                year = startYear;
                month_data = CDMisc.findData (obsy_code, year, month, useZip, usePlain, base_dir);
                n_missing_samps_to_add = 0;
            
                // reset the data formatting object
                formatObject = null;
            
                // for each day of data and while we haven't been cancelled ...
                for (duration_count = 0; 
                     (duration_count < duration) && (! notify.isCancelled());
                     duration_count += DateUtils.MILLISECONDS_PER_DAY)
                {
                    obsy_percent = (int) ((duration_count * 100l) / duration);
                    total_percent = obsy_total_percent +
                                    (((next_obsy_total_percent - obsy_total_percent) * obsy_percent) / 100);
                    notify.showProgress ("Reading " + obsy_code + " " + DateUtils.formatDate (day, month, year),
                                         obsy_code, total_percent, obsy_percent);
                    
                    // if needed fill in the file start date
                    if (fileStartDay == -1)
                    {
                        fileStartDay = day;
                        fileStartMonth = month;
                        fileStartYear = year;
                    }
                
                    // get this day's data
                    if (month_data == null) day_data = null;
                    else if (month_data[0] == null) day_data = null;
                    else day_data = month_data[0].getDayData(day);
                
                    // create data formatting objects (if they haven't already been
                    // created) - we can only do this if we have some data
                    try
                    {
                        if ((formatObject == null) && (day_data != null))
                        {
                            observatory = month_data [0].getObservatoryData();
                            switch (dataFormat)
                            {
                            case FORMAT_IAGA2002:
                                if (sample_period_string.length() > 0)
                                    sample_period_string2 = sample_period_string;
                                else if (day_data.getSampleRate() <= 0)
                                    sample_period_string2 = "Unknown";
                                else
                                    sample_period_string2 = Double.toString (((double) day_data.getSampleRate()) / 1000.0) + " seconds";
                                iaga2002 = new Iaga2002 (observatory.GetObservatoryCode(),
                                                         observatory.GetObservatoryName(),
                                                         observatory.GetLatitude (),
                                                         observatory.GetLongitude (),
                                                         (double) observatory.GetAltitude (),
                                                         day_data.getCompOrientation(),
                                                         "definitive",
                                                         day_data.getSource().trim(),
                                                         day_data.getSensorOrientation(),
                                                         sample_period_string2,
                                                         interval_type);
                                formatObject = iaga2002;
                                break;
                            case FORMAT_PLAIN_TEXT:
                                plainData = new PlainData (observatory.GetObservatoryCode(),
                                                           day_data.getCompOrientation(),
                                                           "definitive");
                                formatObject = plainData;
                                break;
                            case FORMAT_INTERMAGNET:
                                switch (dataType)
                                {
                                case PERIOD_MINUTE:
                                    imfv122 = new IMFV122 (observatory.GetObservatoryCode(),
                                                           gin_code,
                                                           observatory.GetLatitude (),
                                                           observatory.GetLongitude (),
                                                           day_data.getCompOrientation(),
                                                           "definitive");
                                    formatObject = imfv122;
                                    break;
                                }
                                break;
                            case FORMAT_WDC:
                                switch (dataType)
                                {
                                case PERIOD_MINUTE:
                                    wdcMinute = new WDCMinute (observatory.GetObservatoryCode(),
                                                               observatory.GetLatitude (),
                                                               observatory.GetLongitude (),
                                                               day_data.getCompOrientation(),
                                                               "D");
                                    formatObject = wdcMinute;
                                    break;
                                case PERIOD_HOUR:
                                    wdcHour = new WDCHour (observatory.GetObservatoryCode(),
                                                           day_data.getCompOrientation(),
                                                           false);
                                    formatObject = wdcHour;
                                    break;
                                }
                                break;
                            }
                        }
                    }
                    catch (GeomagDataException e)
                    {
                        formatObject = null;
                        notify.registerError ("Unable to create formatting object for " +
                                              obsyList [obs_count].GetDisplayName() +
                                              " on " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear, n_msg_date_fields),
                                              notify.ERROR_FATAL);
                    }

                    // if there was a fatal error, cut to the end of the loop
                    if (notify.isCancelled()) continue;
                    
                    // add the new data to the formatting object -
                    // there are four possible states here:
                    // 1.) there is no data for this day and no previous data for this file
                    //     day_data == null && formatObject == null
                    // 2.) there is no data for this day but there is previous data for this file
                    //     day_data == null && formatObject != null
                    // 3.) there is data for this day and no previous data for this file
                    //     day_data != null && formatObject == null
                    //     NBNBNB - if this is the case check for previous missing data 
                    //              that needs to be added
                    // 4.) there is data for this day and there is previous data for this file
                    //     day_data != null && formatObject != null
                    try
                    {
                        date = DateUtils.createDate (year, month, day, 0, 0, 0, 0);
                        if (day_data == null)
                        {
                            if (formatObject == null)
                                n_missing_samps_to_add += n_samps_per_day;
                            else
                                formatObject.addMissingData(date, sample_period, n_samps_per_day);
                        }
                        else
                        {
                            if (n_missing_samps_to_add > 0)
                            {
                                formatObject.addMissingData(date, sample_period, n_missing_samps_to_add);
                                n_missing_samps_to_add = 0;
                            }
                            switch (dataType)
                            {
                            case PERIOD_MINUTE:
                                formatObject.addData (date, sample_period, fourthElement, day_data.getMinuteMeans());
                                break;
                            case PERIOD_HOUR:
                                formatObject.addData (date, sample_period, fourthElement, day_data.getHourlyMeans());
                                break;
                            case PERIOD_DAY:
                                formatObject.addData (date, sample_period, fourthElement, day_data.getDailyMeans());
                                break;
                            }
                        }
                    }
                    catch (GeomagDataException e)
                    {
                        notify.registerError ("Unable to add data to formatting object for " +
                                              obsyList [obs_count].GetDisplayName() +
                                              " on " + DateUtils.formatDate (day, month, year, n_msg_date_fields),
                                              notify.ERROR_FATAL);
                    }
                
                    // if there was a fatal error, cut to the end of the loop
                    if (notify.isCancelled()) continue;
                    
                    // should we write the data ??
                    write_file = false;
                    switch (fileLength)
                    {
                    case DateUtils.DURATION_TYPE_DAYS:
                        write_file = true; 
                        break;
                    case DateUtils.DURATION_TYPE_MONTHS:
                        if (day == (DateUtils.daysInMonth (month, year) -1)) write_file = true; 
                        break;
                    case DateUtils.DURATION_TYPE_YEARS:
                        if ((day == (DateUtils.daysInMonth (month, year) -1)) &&
                            (month == 11)) write_file = true; 
                        break;
                    case DateUtils.DURATION_TYPE_INFINITE:
                        if (duration_count >= (duration - DateUtils.MILLISECONDS_PER_DAY))
                            write_file = true;
                        break;
                    }
                    if (write_file)
                    {
                        notify.showProgress ("Writing " + obsy_code + " " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear),
                                             obsy_code, total_percent, obsy_percent);
                        
                        // use formatObject to write the data then reset it to null
                        try
                        {
                            if (formatObject == null)
                            {
                                notify.registerError ("No data found for " +
                                                      obsyList [obs_count].GetDisplayName() +
                                                      " on " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear, n_msg_date_fields),
                                                      notify.ERROR_WARNING);
                            }
                            else if (formatObject.isAllMissing())
                            {
                                notify.registerError ("No data found for " +
                                                      obsyList [obs_count].GetDisplayName() +
                                                      " on " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear, n_msg_date_fields),
                                                      notify.ERROR_WARNING);
                            }
                            else 
                            {
                                // what type of zip operations are needed
                                last_zip_filename = zip_filename;
                                output_to_zip = true;
                                switch (zipType)
                                {
                                    case ZIP_NONE:
                                        output_to_zip = false;
                                        break;
                                    case ZIP_DAILY:
                                        zip_filename = output_dir + File.separator + 
                                                       formatObject.getStationCode().toLowerCase() + 
                                                       date3field.format (formatObject.getStartDate()) +
                                                       data_type_code +
                                                       ".zip";
                                        break;
                                    case ZIP_MONTHLY:
                                        zip_filename = output_dir + File.separator + 
                                                       formatObject.getStationCode().toLowerCase() + 
                                                       date2field.format (formatObject.getStartDate()) +
                                                       data_type_code +
                                                       ".zip";
                                        break;
                                    case ZIP_YEARLY:
                                        zip_filename = output_dir + File.separator + 
                                                       formatObject.getStationCode().toLowerCase() + 
                                                       date1field.format (formatObject.getStartDate()) +
                                                       data_type_code +
                                                       ".zip";
                                        break;
                                    case ZIP_OBSERVATORY:
                                        zip_filename = output_dir + File.separator + 
                                                       formatObject.getStationCode().toLowerCase() + 
                                                       data_type_code +
                                                       ".zip";
                                        break;
                                    case ZIP_ALL:
                                        zip_filename = output_dir + File.separator +
                                                       "imag" +
                                                       data_type_code +
                                                       ".zip";
                                        break;
                                }

                                // are we using zip files
                                if (! output_to_zip)
                                {
                                    // no - write the data to file
                                    formatObject.write (output_dir, true);
                                }
                                else
                                {
                                    // yes - do we need to start a new zip file ???
                                    if (! last_zip_filename.equals(zip_filename))
                                    {
                                        if (zip_stream != null) zip_stream.close();
                                        zip_stream = new ZipOutputStream (new FileOutputStream (zip_filename));
                                    }
        
                                    // create a new zip entry and write the data
                                    zip_entry = new ZipEntry (formatObject.makeFilename ("", true));
                                    zip_stream.putNextEntry (zip_entry);
                                    formatObject.write (zip_stream);
                                    zip_stream.closeEntry();
                                }
                            }
                        }
                        catch (FileNotFoundException e)
                        {
                            notify.registerError ("Unable to create file for " +
                                                  obsyList [obs_count].GetDisplayName() +
                                                  " on " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear, n_msg_date_fields),
                                                  notify.ERROR_ERROR);
                        }
                        catch (IOException e)
                        {
                            notify.registerError ("Error writing data to file for " +
                                                  obsyList [obs_count].GetDisplayName() +
                                                  " on " + DateUtils.formatDate (fileStartDay, fileStartMonth, fileStartYear, n_msg_date_fields),
                                                  notify.ERROR_ERROR);
                        }
                        formatObject = null;
                        n_missing_samps_to_add = 0;
                    
                        // flag the start of a new file
                        fileStartDay = fileStartMonth = fileStartYear = -1;
                    }

                    // update the date fields for the next pass
                    // - get the next month's data if it is needed
                    // - reset the metadata
                    day ++;
                    if (day >= DateUtils.daysInMonth (month, year))
                    {
                        day = 0;
                        month ++;
                        if (month >= 12)
                        {
                            month = 0;
                            year ++;
                        }
                        
                        // this is a new month - get it's data
                        month_data = CDMisc.findData (obsy_code, year, month, useZip, usePlain, base_dir);
                    }
                }
            }
        }
        catch (OutOfMemoryError e)
        {
            long maxMemory, freeMemory, usedMemory;
            
            // get memory figures
            freeMemory = Runtime.getRuntime().freeMemory();
            maxMemory = Runtime.getRuntime().maxMemory();
            usedMemory = maxMemory - freeMemory;
            
            // delete large objects and call garbage collector
            iaga2002 = null;
            plainData = null;
            imfv122 = null;
            wdcMinute = null;
            wdcHour = null;
            formatObject = null;
            System.gc();
            
            notify.registerError ("Not enough memory to complete export - " +
                                  Long.toString ((usedMemory + 500000) / 1000000) + "Mb" +
                                  " used out of " +
                                  Long.toString ((maxMemory + 500000) / 1000000) + "Mb",
                                  notify.ERROR_FATAL);
        }
    
        // close the final zip file
        try
        {
            if (zip_stream != null) zip_stream.close();
        }
        catch (IOException e)
        {
            notify.registerError ("Error closing zip file", notify.ERROR_ERROR);
        }
        
        // bring down the progress dialog
        notify.allDone();
    }
    
}
