/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCD;

import bgs.geophys.library.Data.ImagCDFilename;
import bgs.geophys.library.Misc.DateUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * a class to collect information on the completeness of data
 * in a CD file
 * 
 * @author smf
 */
public class ImagCDFileStats 
{

    private String station_id;
    private int colatitude;
    private int longitude;
    private int elevation;
    private String orientation;
    private String institute;
    private int d_conversion;
    private String quality_code;
    private String instrument;
    private int k9;
    private int samp_rate;
    private String sensor_orient;
    private Date publication_date;
    private String station_id_errmsg;
    private String date_errmsg;
    private String colatitude_errmsg;
    private String longitude_errmsg;
    private String elevation_errmsg;
    private String orientation_errmsg;
    private String institute_errmsg;
    private String d_conversion_errmsg;
    private String quality_code_errmsg;
    private String instrument_errmsg;
    private String k9_errmsg;
    private String samp_rate_errmsg;
    private String sensor_orient_errmsg;
    private String publication_date_errmsg;
    private int n_days_in_month;
    private int n_vector_minute_samples;
    private int n_scalar_minute_samples;
    private int n_vector_hour_samples;
    private int n_scalar_hour_samples;
    private int n_vector_day_samples;
    private int n_scalar_day_samples;
    private int n_k_indices;
    private int expected_vector_minute_samples;
    private int expected_scalar_minute_samples;
    private int expected_vector_hour_samples;
    private int expected_scalar_hour_samples;
    private int expected_vector_day_samples;
    private int expected_scalar_day_samples;
    private int expected_k_indices;
    
    public ImagCDFileStats (ImagCDFile cd_file)
    {
        calcCDFileStats (cd_file);
    }

    /** calculate stats for a CD file
     * @param cd_file the file to calculate for */
    public void calcCDFileStats (ImagCDFile cd_file)
    {
        int day_count, hour_count, minute_count, comp_count, ki_count;
        int ms[], hs[], ds[], ks;
        ImagCDDataDay day_seg, day_seg0;
        boolean chk_station, chk_elev, chk_colat, chk_long, chk_orient;
        boolean chk_src, chk_d_conv, chk_qual, chk_instrum, chk_k9;
        boolean chk_samp_rate, chk_sens_or, chk_pub_date, chk_date;
        String rec_elem;
        GregorianCalendar expected_date, actual_date;
        ImagCDDataDay data_day;

        clearHeaderErrMsgs();
        clearDataStats();
        
        // get header data from the first day
        data_day = cd_file.getDataDay(1);
        station_id = data_day.getStationID();
        colatitude = data_day.getColatitude();
        longitude = data_day.getLongitude();
        elevation = data_day.getElevation();
        orientation = data_day.getRecordedElements();
        institute = data_day.getInstituteCode();
        d_conversion = data_day.getDConversion();
        quality_code = data_day.getQualityCode();
        instrument = data_day.getInstrumentCode();
        k9 = data_day.getK9Limit();
        samp_rate = data_day.getSamplePeriod();
        sensor_orient = data_day.getSensorOrientation();
        publication_date = data_day.getPublicationDate();
        n_days_in_month = cd_file.getNDays();


        // calculated amounts of data expected
        expected_vector_minute_samples = n_days_in_month * ImagCDDataDay.N_MINUTE_MEAN_VALUES * 3;
        expected_scalar_minute_samples = n_days_in_month * ImagCDDataDay.N_MINUTE_MEAN_VALUES;
        expected_vector_hour_samples = n_days_in_month * ImagCDDataDay.N_HOURLY_MEAN_VALUES * 3;
        expected_scalar_hour_samples = n_days_in_month * ImagCDDataDay.N_HOURLY_MEAN_VALUES;
        expected_vector_day_samples = n_days_in_month * ImagCDDataDay.N_DAILY_MEAN_VALUES * 3;
        expected_scalar_day_samples = n_days_in_month * ImagCDDataDay.N_DAILY_MEAN_VALUES;
        expected_k_indices = n_days_in_month * ImagCDDataDay.N_K_INDICES;

        // initialise counters
        chk_station = chk_elev = chk_colat = chk_long = chk_orient = false;
        chk_src = chk_d_conv = chk_qual = chk_instrum = chk_k9 = false;
        chk_samp_rate = chk_sens_or = false;
        chk_pub_date = chk_date = true;
        ms = new int[ImagCDDataDay.N_COMPONENTS];
        hs = new int[ImagCDDataDay.N_COMPONENTS];
        ds = new int[ImagCDDataDay.N_COMPONENTS];
        for (comp_count = 0; comp_count < ImagCDDataDay.N_COMPONENTS; comp_count++) 
            ms[comp_count] = hs[comp_count] = ds[comp_count] = 0;
        ks = 0;
        expected_date = new GregorianCalendar(DateUtils.gmtTimeZone);
        actual_date = new GregorianCalendar(DateUtils.gmtTimeZone);

        // check the headers
        day_seg0 = cd_file.getDataDay(1);
        if (day_seg0.getStationID().trim().length() == 3)
            chk_station = true;
        else if (day_seg0.getStationID().trim().length() == 0)
            station_id_errmsg = "Missing station code";
        else
            station_id_errmsg = "Bad station code: " + day_seg0.getStationID();
        if (day_seg0.getColatitude() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_colat = true;
        else
            colatitude_errmsg = "Missing colatitude: " + day_seg0.getColatitude();
        if (day_seg0.getLongitude() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_long = true;
        else
            longitude_errmsg = "Missing longitude: " + day_seg0.getLongitude();
        if (day_seg0.getElevation() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_elev = true;
        else
            elevation_errmsg = "Missing elevation: " + day_seg0.getElevation();

        
        rec_elem = day_seg0.getRecordedElements().trim();
//        Calendar c = new GregorianCalendar();
//        c.set(2009, 0, 01);
//        Date year2009 = new Date(c.getTimeInMillis());

        if (data_day.getYear()<2009)
/*        if (publication_date.before(year2009))*/{
            if(rec_elem.equalsIgnoreCase("hdzf") ||
            rec_elem.equalsIgnoreCase("xyzf") ||
            rec_elem.equalsIgnoreCase("diff") )
               chk_orient = true;
            }
        else{ // 2009 onwards
            if(rec_elem.equalsIgnoreCase("hdzg") ||
               rec_elem.equalsIgnoreCase("xyzg"))
               chk_orient = true;
            }
        if (!chk_orient)    orientation_errmsg = "Bad component codes: " + day_seg0.getRecordedElements();

        if (day_seg0.getInstituteCode().trim().length() > 0)
            chk_src = true;
        else
            institute_errmsg = "Missing institute code";
        if (day_seg0.getDConversion() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_d_conv = true;
        else
            d_conversion_errmsg = "Missing D conversion factor";
        if (day_seg0.getQualityCode().trim().length() != 0)
            chk_qual = true;
        else
            quality_code_errmsg = "Missing data quality code";
        if (day_seg0.getInstrumentCode().trim().length() > 0)
            chk_instrum = true;
        else
            instrument_errmsg = "Missing instrument code";
        if (day_seg0.getK9Limit() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_k9 = true;
        else
            k9_errmsg = "Missing K9 value";
        if (day_seg0.getSamplePeriod() != ImagCDDataDay.MISSING_HEADER_FIELD)
            chk_samp_rate = true;
        else
            samp_rate_errmsg = "Missing sampling rate";
        if (day_seg0.getSensorOrientation().trim().length() > 0)
            chk_sens_or = true;
        else
            sensor_orient_errmsg = "Missing sensor orientation";
        if (day_seg0.getPublicationDate() == null)
            chk_pub_date = false;
        else
            chk_pub_date = true;

        // check for XYZF and D conversion != 10000
        if (chk_orient && chk_d_conv && rec_elem.equalsIgnoreCase("xyzf") &&
            day_seg0.getDConversion() != 10000)
        {
            d_conversion_errmsg = "D conversion should be 10000 for XYZF orientation";
            chk_d_conv = false;
        }
        
        // for each day ...
        for (day_count = 0, expected_date.setTime(cd_file.getStartDate());
             day_count < cd_file.getNDays();
             day_count++, expected_date.add(GregorianCalendar.DAY_OF_MONTH, 1))
        {
            day_seg = cd_file.getDataDay(day_count + 1);

            // check the header fields
            if (day_count > 0) 
            {
                if (chk_station) 
                {
                    if (!day_seg0.getStationID().trim().equalsIgnoreCase(day_seg.getStationID().trim())) 
                    {
                        chk_station = false;
                        station_id_errmsg = "Station ID differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_colat) 
                {
                    if (day_seg0.getColatitude() != day_seg.getColatitude()) 
                    {
                        chk_colat = false;
                        colatitude_errmsg = "Colatitude differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_long) 
                {
                    if (day_seg0.getLongitude() != day_seg.getLongitude()) 
                    {
                        chk_long = false;
                        longitude_errmsg = "Longitude differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_elev) 
                {
                    if (day_seg0.getElevation() != day_seg.getElevation()) 
                    {
                        chk_elev = false;
                        elevation_errmsg = "Elevation differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_orient) 
                {
                    if (!day_seg0.getRecordedElements().equalsIgnoreCase(day_seg.getRecordedElements())) 
                    {
                        chk_orient = false;
                        orientation_errmsg = "Orientation differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_src) 
                {
                    if (!day_seg0.getInstituteCode().equalsIgnoreCase(day_seg.getInstituteCode())) 
                    {
                        chk_src = false;
                        institute_errmsg = "Source (institute) code differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_d_conv) 
                {
                    if (day_seg0.getDConversion() != day_seg.getDConversion()) 
                    {
                        chk_d_conv = false;
                        d_conversion_errmsg = "D conversion differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_qual) 
                {
                    if (!day_seg0.getQualityCode().equalsIgnoreCase(day_seg.getQualityCode()))
                    {
                        chk_qual = false;
                        quality_code_errmsg = "Data quality code differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_instrum)
                {
                    if (!day_seg0.getInstrumentCode().equalsIgnoreCase(day_seg.getInstrumentCode())) 
                    {
                        chk_instrum = false;
                        instrument_errmsg = "Instrument code differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_k9) 
                {
                    if (day_seg0.getK9Limit() != day_seg.getK9Limit())
                    {
                        chk_k9 = false;
                        k9_errmsg = "K9 value differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_samp_rate)
                {
                    if (day_seg0.getSamplePeriod() != day_seg.getSamplePeriod()) 
                    {
                        chk_samp_rate = false;
                        samp_rate_errmsg = "Sample rate differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_sens_or) 
                {
                    if (!day_seg0.getSensorOrientation().equalsIgnoreCase(day_seg.getSensorOrientation())) {
                        chk_sens_or = false;
                        sensor_orient_errmsg = "Sensor orientation differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
                if (chk_pub_date)
                {
                    if (!day_seg0.getPublicationDate().equals(day_seg.getPublicationDate())) 
                    {
                        chk_pub_date = false;
                        publication_date_errmsg = "Publication date differs between days, starting at day " + Integer.toString(day_count + 1);
                    }
                }
            }

            // check for year / daynum in header
            if (chk_date) 
            {
                actual_date.set(day_seg.getYear(), 0, 1, 0, 0, 0);
                actual_date.set(GregorianCalendar.DAY_OF_YEAR, day_seg.getDayNumber());
                if (actual_date.get (GregorianCalendar.YEAR) != expected_date.get (GregorianCalendar.YEAR) ||
                    actual_date.get (GregorianCalendar.MONTH) != expected_date.get (GregorianCalendar.MONTH) ||
                    actual_date.get (GregorianCalendar.DAY_OF_MONTH) != expected_date.get (GregorianCalendar.DAY_OF_MONTH))
                {
                    chk_date = false;
                    date_errmsg = "Incorrect date (year / day number) for filename at day " + Integer.toString(day_count + 1);
                }
            }

            // for each component
            for (comp_count = 0; comp_count < ImagCDDataDay.N_COMPONENTS; comp_count++) 
            {
                // calculate minute stats
                for (minute_count = 0; minute_count < ImagCDDataDay.N_MINUTE_MEAN_VALUES; minute_count++) 
                {
                    if (day_seg.getMinuteData(comp_count, minute_count) != ImagCDDataDay.MISSING_DATA) 
                        ms[comp_count] += 1;
                }

                // calculate hour stats
                for (hour_count = 0; hour_count < ImagCDDataDay.N_HOURLY_MEAN_VALUES; hour_count++) 
                {
                    if (day_seg.getHourData(comp_count, hour_count) != ImagCDDataDay.MISSING_DATA) 
                        hs[comp_count] += 1;
                }

                // calculate day stats
                if (day_seg.getDayData(comp_count) != ImagCDDataDay.MISSING_DATA) 
                    ds[comp_count] += 1;
            }

            // calculate k-index stats
            for (ki_count = 0; ki_count < ImagCDDataDay.N_K_INDICES; ki_count++) 
            {
                if (day_seg.getKIndexData(ki_count) != ImagCDDataDay.MISSING_K_INDEX)
                    ks += 1;
            }
        }

        // set stats
        n_vector_minute_samples = ms[0] + ms[1] + ms[2];
        n_scalar_minute_samples = ms[3];
        n_vector_hour_samples = hs[0] + hs[1] + hs[2];
        n_scalar_hour_samples = hs[3];
        n_vector_day_samples = ds[0] + ds[1] + ds[2];
        n_scalar_day_samples = ds[3];
        n_k_indices = ks;
    }

    public int getNDaysInMonth() { return n_days_in_month; }
    public String getStationID() { return station_id; }
    public int getColatitude() { return colatitude; }
    public int getLongitude() { return longitude; }
    public int getElevation() { return elevation; }
    public String getOrientation() { return orientation; }
    public String getInstitute() { return institute; }
    public int getDConversion() { return d_conversion; }
    public String getQualityCode() { return quality_code; }
    public String getInstrument() { return instrument; }
    public int getK9() { return k9; }
    public int getSampRate() { return samp_rate; }
    public String getSensorOrient() { return sensor_orient; }
    public Date getPublicationDate () { return publication_date; }
    
    public void clearHeaderErrMsgs() 
    {
        station_id_errmsg = null;
        date_errmsg = null;
        colatitude_errmsg = null;
        longitude_errmsg = null;
        elevation_errmsg = null;
        orientation_errmsg = null;
        institute_errmsg = null;
        d_conversion_errmsg = null;
        quality_code_errmsg = null;
        instrument_errmsg = null;
        k9_errmsg = null;
        samp_rate_errmsg = null;
        sensor_orient_errmsg = null;
        publication_date_errmsg = null;
    }

    public boolean isHeaderError() 
    {
        if (station_id_errmsg != null) return true;
        if (date_errmsg != null) return true;
        if (colatitude_errmsg != null) return true;
        if (longitude_errmsg != null) return true;
        if (elevation_errmsg != null) return true;
        if (orientation_errmsg != null) return true;
        if (institute_errmsg != null) return true;
        if (d_conversion_errmsg != null) return true;
        if (quality_code_errmsg != null) return true;
        if (instrument_errmsg != null) return true;
        if (k9_errmsg != null) return true;
        if (samp_rate_errmsg != null) return true;
        if (sensor_orient_errmsg != null) return true;
        if (publication_date_errmsg != null) return true;
        return false;
    }

    public String getStationIDErrmsg() {
        return station_id_errmsg;
    }

    public String getDateErrmsg() {
        return date_errmsg;
    }

    public String getColatitudeErrmsg() {
        return colatitude_errmsg;
    }

    public String getLongitudeErrmsg() {
        return longitude_errmsg;
    }

    public String getElevationErrmsg() {
        return elevation_errmsg;
    }

    public String getOrientationErrmsg() {
        return orientation_errmsg;
    }

    public String getInstituteErrmsg() {
        return institute_errmsg;
    }

    public String getDConversionErrmsg() {
        return d_conversion_errmsg;
    }

    public String getQualityCodeErrmsg() {
        return quality_code_errmsg;
    }

    public String getInstrumentErrmsg() {
        return instrument_errmsg;
    }

    public String getK9Errmsg() {
        return k9_errmsg;
    }

    public String getSampRateErrmsg() {
        return samp_rate_errmsg;
    }

    public String getSensorOrientErrmsg() {
        return sensor_orient_errmsg;
    }

    public String getPublicationDateErrmsg() {
        return publication_date_errmsg;
    }
    
    public void clearDataStats() 
    {
        n_vector_minute_samples = n_scalar_minute_samples = 0;
        n_vector_hour_samples = n_scalar_hour_samples = 0;
        n_vector_day_samples = n_scalar_day_samples = 0;
        n_k_indices = 0;
    }

    public boolean isDataError() 
    {
        if (n_vector_minute_samples != expected_vector_minute_samples) return true;
        if (n_scalar_minute_samples != expected_scalar_minute_samples) return true;
        if (n_vector_hour_samples != expected_vector_hour_samples) return true;
        if (n_scalar_hour_samples != expected_scalar_hour_samples) return true;
        if (n_vector_day_samples != expected_vector_day_samples) return true;
        if (n_scalar_day_samples != expected_scalar_day_samples) return true;
        if (n_k_indices != expected_k_indices) return true;
        return false;
    }

    public int getNVectorMinuteSamples() { return n_vector_minute_samples; }
    public int getNScalarMinuteSamples() { return n_scalar_minute_samples; }
    public int getNVectorHourSamples() { return n_vector_hour_samples; }
    public int getNScalarHourSamples() { return n_scalar_hour_samples; }
    public int getNVectorDaySamples() { return n_vector_day_samples; }
    public int getNScalarDaySamples() { return n_scalar_day_samples; }
    public int getNKIndices() { return n_k_indices; }

    public int getExpectedVectorMinuteSamples() { return expected_vector_minute_samples; }
    public int getExpectedScalarMinuteSamples() { return expected_scalar_minute_samples; }
    public int getExpectedVectorHourSamples() { return expected_vector_hour_samples; }
    public int getExpectedScalarHourSamples() { return expected_scalar_hour_samples; }
    public int getExpectedVectorDaySamples() { return expected_vector_day_samples; }
    public int getExpectedScalarDaySamples() { return expected_scalar_day_samples;}
    public int getExpectedKIndices() { return expected_k_indices; }

}
