/*
 * WDCHour.java
 *
 * Created on 28 January 2007, 11:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Data;

import bgs.geophys.library.Maths.BGSMath;
import bgs.geophys.library.Maths.RoundEven;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Misc.GeoString;
import bgs.geophys.library.Misc.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class to read and write WDCHour hourly data. 
 * 
 * To use this object to write a file:
 *      1.) Instantiate an object in the normal way
 *      2.) Call the addData() methods to insert the data
 *      3.) Optionally call the get/setDataMean to adjust the associated mean values
 *      4.) Call any of the write() methods
 *
 * To use this object to read a file call the static read() method
 *
 * The formats are from
 * http://web.dmi.dk/projects/wdcc1/format.html
 * 
 *    1-3   A3 OBSERVATORY 3-LETTER CODE, left adjused
 *    4-5   I2 YEAR (last 2 digits, 82 = 1982)
 *    6-7   I2 MONTH (01-12)
 *      8   A1 ELEMENT(D,H,X,Y,Z,or F)
 *   9-10   I2 DAY OF MONTH (01-31)
 *  11-12   A2 Blanks
 *  13-14   A2 Arbitrary
 *     15   A1 INTERNATIONAL QUIET or DISTURBED DAYS,
 *             Q=1,D=2
 *     16   I1 Blank for data since 1900,8 for data before
 *  17-20   I4 Tabular base, in degrees for D and I, hundreds of nanoTeslas
 *             (gammas) for the intensity elements. The bases are right adjusted
 *             and signed if negative. Negative values are identified with a
 *             minus sign either adjacent to the first significant digit or in
 *             the high-order position of the field (position 17).  NOTE: A blank
 *             digit will not appear between a (-) sign and the first significant
 *             digit. For example, a base may appear as -050 or b-50 but not as
 *             -b50(b=blank).
 *  21-116 24I4 Twenty-four 4-digit Hourly Values for the day. The values are in
 *             tenth-minutes for D and in nanoTeslas (gammas) for the intensity
 *             elements. The first hourly value represents the mean value between
 *             00:00 UT and 01:00 UT, ..., the 24th value represents the mean
 *             between 23:00 UT and 24:00 UT. Rules for negative values are
 *             the same as those described for tabular bases. A missing value is
 *             identified by 9999.
 *  117-120   I4 Daily Mean. Rules for negative values are the same as those     
 *             described for tabular bases. If any of the hourly mean values for
 *             the day are missing 9999 will appear as the daily mean.
 * 
 * NBNBNB - there is a conflict over the use of columns 15 and 16 - data from
 * the WDC at DMI uses these two characters as a century holder (19 for 1900s, ...)
 * Using column 16 alone you can decode the century:
 *  ' ' - 1900  Original format
 *  '8' - 1800  Original format
 *  '9' - 1900  Modified format
 *  '0' - 2000  Modified format
 * However the contents of column 15 are ambiguous
 *
 * @author smf
 */
public class WDCHour extends GeomagDataFormat
{

    private boolean writeQuietDay;

    /*
     * Caution! SimpleDateFOrmat and DecimalFormat are not thread safe!
     */
    // formatting objects
    private SimpleDateFormat headerDateFormat;
    private static SimpleDateFormat dayDateFormat;
    private static SimpleDateFormat headerInputDateFormat;
    private static SimpleDateFormat filenameDateFormat;
    private DecimalFormat number2digits;

    // static initialisers - mainly creation of formatting objects
    static
    {        
        dayDateFormat = new SimpleDateFormat ("dd", DateUtils.english_date_format_symbols);
        dayDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        headerInputDateFormat = new SimpleDateFormat ("yyyyMMdd", DateUtils.english_date_format_symbols);
        headerInputDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        filenameDateFormat = new SimpleDateFormat ("yyyy", DateUtils.english_date_format_symbols);
        filenameDateFormat.setTimeZone(DateUtils.gmtTimeZone);        
    }
    
    /**
     * Creates a new instance of WDCHour by supplying all data
     * 
     * 
     * @param station_code the IAGA station code
     * @param comp_code the components in this data
     * @param writeQuietDay - true to use original format description
     *        for columns 15/16, false to use modified description
     * @throws GeomagDataException if there was an error in the data - the
     *         messages associated with the exception will describe the problem
     */
    public WDCHour (String station_code, String comp_code, boolean writeQuietDay)
    throws GeomagDataException
    {
        super (station_code, null, MISSING_HEADER_VALUE, MISSING_HEADER_VALUE,
               MISSING_HEADER_VALUE,
               comp_code, null, null, null, null, null, null, false, 24);
        
        this.writeQuietDay = writeQuietDay;
        initFormatObjects();
    }

    /**
     * Constructor with the same signature as the GeomagDataFormat parent class
     * Designed to give all subclasses of GeomagDataFormat a uniform constructor.
     * Delegates creation of a new instance of WDC to the appropriate WDC constructor
     *
     * @param stationCode
     * @param stationName
     * @param latitude
     * @param longitude
     * @param elevation
     * @param compCode
     * @param dataType
     * @param ginCode
     * @param instituteName
     * @param sensorOrientation
     * @param samplePeriodString
     * @param intervalType
     * @param allowFourDigitSC
     * @param blocksize
     * @throws bgs.geophys.library.Data.GeomagDataException
     */
    public WDCHour(String stationCode, String stationName, double latitude, double longitude, double elevation, String compCode, String dataType, String ginCode, String instituteName, String sensorOrientation, String samplePeriodString, String intervalType, boolean allowFourDigitSC, int blocksize) throws GeomagDataException {
        this(stationCode, compCode, false);
        initFormatObjects();
    }


    
    /**
     * Write data in WDCHour format to an output stream
     * 
     * @param os the output stream to use
     * @param termType one of the GeomagDataFormat TERM_TYPE_... codes
     * @throws IOException if there was an writing to the file
     */
    public void write (OutputStream os, byte termType [])
    throws IOException
    {
        int nComponents, day, hour, month, year, n_days, day_count, count;
        int componentCount, start_record, end_record, record_count, index;
        long longValue;
        double value, multiplier, tabularBase, tabBase [];
        Date endDate, date;
        String originString, componentCode, dateString, string,tabularBaseString, tabBaseString [];
        StringBuffer stringBuffer;
        GregorianCalendar calendar;
        
        // check that there is some data to write
        if (getDataLength() <= 0)
            throw new GeomagDataException ("No data to write");

        // check that data is one hour period
        if (getSamplePeriod() != 3600000l)
            throw new GeomagDataException ("Data must have sample period of one hour");
        
        // check that there are complete months to write
        calendar = new GregorianCalendar (DateUtils.gmtTimeZone);
        calendar.setTime(getStartDate());
        day = calendar.get (calendar.DAY_OF_MONTH);
        hour = calendar.get (calendar.HOUR_OF_DAY);
        if (day != 1 || hour != 0)
            throw new GeomagDataException ("Data must start with first hour of first day in month");
        calendar.add ((int) (getDataLength() / 3600000l), calendar.HOUR);
        day = calendar.get (calendar.DAY_OF_MONTH);
        hour = calendar.get (calendar.HOUR_OF_DAY);
        if (day != 1 || hour != 0)
            throw new GeomagDataException ("Data must end with last hour of last day in month");

        // get header strings
        nComponents = getComponentCodes().length();
        if (nComponents > 4) nComponents = 4;
        tabBaseString = new String [nComponents];
        
        originString = getInstituteName();
        if (originString == null) originString = " ";
        else if (originString.length() == 0) originString = " ";
        else if (originString.length() > 1) originString = originString.substring(0, 1);
        
        // calculate tab base (so that it is the same for all records of a component)
//        tabBase = calcMedian (99);
//        tabBaseString = new String [nComponents];
//        for (count=0; count<nComponents; count++)
//        {
//            // check for missing value
//            if (tabBase [count] == MISSING_DATA_SAMPLE)
//            {
//                tabBase [count] = 0;
//                tabBaseString [count] = "0000";
//            }
//            else
//            {
//                // process differently for field strengths and angles
//                componentCode = getComponentCodes().substring(count, count +1);
//                if (componentCode.equalsIgnoreCase("D") || componentCode.equalsIgnoreCase("I"))
//                {
//                    // the variation values need to lie between -99.9 and 999.8 min - 
//                    // the median for this range is about 450, so take 450min from the base
//                    tabBase [count] -= 450.0;
//                    
//                    // round the tabBase to the nearest degree and format it as a string -
//                    // this will also truncate the tabBase - finally convert it back to
//                    // minutes of arc - the result will not be the same as the original
//                    // because of the truncation
//                    longValue = Math.round (tabBase [count] / 60.0);
//                    tabBaseString [count] = GeoString.fix (Long.toString (longValue), 4, true, false);
//                    tabBase [count] = (double) longValue * 60.0;
//                }
//                else
//                {
//                    // the variation values need to lie between -999 and 9998 nT - 
//                    // the median for this range is about 4500, so take 4500nT from the base
//                    tabBase [count] -= 4500.0;
//                    
//                    // round the tabBase to the nearest 100nT and format it as a string -
//                    // this will also truncate the tabBase - finally convert it back to
//                    // nT - the result will not be the same as the original
//                    // because of the truncation
//                    longValue = Math.round (tabBase [count] / 100.0);
//                    tabBaseString [count] = GeoString.fix (Long.toString (longValue), 4, true, false);
//                    tabBase [count] = (double) longValue * 100.0;
//                }
//            }
//        }
        
        // while there is more data to write ...
        date = getStartDate();
        endDate = new Date (date.getTime() + ((long) (getDataLength()-1) * 3600000l));        
        end_record = 0;
        while (date.getTime() < endDate.getTime())
        {            

    //    System.out.println("date stamp: "+date.toString());
            // get the number of days in this month and the start / end 
            // record numbers (records are 1 day / 24 samples long)
            calendar.setTime (date);
            month = calendar.get (calendar.MONTH);
            year =  calendar.get (calendar.YEAR);
            n_days = DateUtils.daysInMonth(month, year);            
//            n_days =1;
            start_record = end_record;
            end_record = start_record + n_days;
            
            // format date string with month and year
            dateString = headerDateFormat.format (date);

            // for each component
            for (componentCount=0; componentCount<nComponents; componentCount ++)
            {
                componentCode = getComponentCodes().substring(componentCount, componentCount +1);
                if (componentCode.equalsIgnoreCase("D") || componentCode.equalsIgnoreCase("I"))
                    multiplier = 10.0;
                else 
                    multiplier = 1.0;

                // for each record in the month
                for (record_count=start_record, day=0; 
                     record_count<end_record; 
                     record_count++, day++)
                {
                    // get the index of the first data sample
                    index = record_count * 24;

                    // write the header fields
                    writeString (os, getStationCode(), TERM_TYPE_NONE);
                    writeString (os, dateString, TERM_TYPE_NONE);
                    writeString (os, componentCode, TERM_TYPE_NONE);
                    writeString (os, number2digits.format ((long) (day +1)), TERM_TYPE_NONE);
                    writeString (os, "    ", TERM_TYPE_NONE);
                    if (writeQuietDay)
                    {
                        stringBuffer = new StringBuffer ();
                        stringBuffer.append (getQuietDayFlag(record_count));
                        if (year < 1900) stringBuffer.append('8');
                        else if (year < 2000) stringBuffer.append(' ');
                        else stringBuffer.append('2');
                        string = new String (stringBuffer);
                    }
                    else
                        string = number2digits.format (year / 100);

        // calculate tabular base on a per record basis  
        tabBase = calcMedian (index,index+24,99); //last index should be the first point of the next set JE 20.01.11
        tabularBase = tabBase[componentCount];
            // check for missing value
            if (tabularBase  == MISSING_DATA_SAMPLE)
            {
                tabularBase  = 0;
                tabularBaseString  = "0000";
            }
            else
            {
                // process differently for field strengths and angles
//                componentCode = getComponentCodes().substring(count, count +1);
                if (componentCode.equalsIgnoreCase("D") || componentCode.equalsIgnoreCase("I"))
                {
                    // the variation values need to lie between -99.9 and 999.8 min - 
                    // the median for this range is about 450, so take 450min from the base
                    tabularBase  -= 450.0;
                    
                    // round the tabBase to the nearest degree and format it as a string -
                    // this will also truncate the tabBase - finally convert it back to
                    // minutes of arc - the result will not be the same as the original
                    // because of the truncation
                    longValue = Math.round (tabularBase / 60.0);
                    tabularBaseString  = GeoString.fix (Long.toString (longValue), 4, true, false);
                    tabularBase  = (double) longValue * 60.0;
                }
                else
                {
                    // the variation values need to lie between -999 and 9998 nT - 
                    // the median for this range is about 4500, so take 4500nT from the base
                    tabularBase -= 4500.0;
                    
                    // round the tabBase to the nearest 100nT and format it as a string -
                    // this will also truncate the tabBase - finally convert it back to
                    // nT - the result will not be the same as the original
                    // because of the truncation
                    longValue = Math.round (tabularBase  / 100.0);
                    tabularBaseString  = GeoString.fix (Long.toString (longValue), 4, true, false);
                    tabularBase  = (double) longValue * 100.0;
                }
            }
        

                    writeString (os, string, TERM_TYPE_NONE);
//                    writeString (os, tabBaseString [componentCount], TERM_TYPE_NONE);
                    writeString (os, tabularBaseString, TERM_TYPE_NONE);
                            
                    // write the data
                    for (count=0; count<24; count++)
                    {
                        value = getData(componentCount, index + count);
                        if (value == MISSING_DATA_SAMPLE) writeString (os, "9999", TERM_TYPE_NONE);
                        else 
                        {
//                            longValue = BGSMath.round ((value - tabBase [componentCount]) * multiplier, BGSMath.ROUND_HALF_EVEN);
                            longValue = BGSMath.round ((value - tabularBase) * multiplier, BGSMath.ROUND_HALF_EVEN);
// This next line is commented out because I don't think it is necessary to use it here. Documentation of
// the problem which it was attempting to solve and the testing can be found in imcdview/Worklists/ResolvedIssues JE 9.3.11
//                            longValue = (long) RoundEven.toDouble((value - tabularBase) * multiplier, 0);
                            writeString (os, GeoString.fix (Long.toString (longValue), 4, true, false), TERM_TYPE_NONE);
                        }
                    }
                    
                    // write the mean
                    value = getDataMean(componentCount, record_count);
                    if (value == MISSING_DATA_SAMPLE) writeString (os, "9999", termType);
                    else 
                    {
      //                  longValue = BGSMath.round ((value - tabBase [componentCount]) * multiplier, BGSMath.ROUND_HALF_EVEN);
//                        longValue = BGSMath.round ((value - tabularBase) * multiplier, BGSMath.ROUND_HALF_EVEN);
                        longValue = (long) RoundEven.toDouble((value - tabularBase) * multiplier, 0);
                        writeString (os, GeoString.fix (Long.toString (longValue), 4, true, false), termType);
                    }
 
            
            
                }  // end of each day for a component
            } //end of each component

            // update the date for the next pass

            calendar.setTime(date);
            calendar.add(calendar.MONTH, 1);
            date = calendar.getTime();
            } // end of data
        } // end of method
    
    
     /** generate a WDC minute filename
     * @param prefix the prefix for the name (including any directory)
     * @param force_lower_case set true to force the filename to lower case
     * @return the file OR null if there is an error */
    public String makeFilename(String prefix, boolean force_lower_case) 
    {
        if (getStartDate() == null) return null;
        return makeFilename (prefix, getStationCode (), getStartDate (), force_lower_case);
    }
    
    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////// static methods below here /////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    
    /** Creates a new instance of WDC hour data by reading it from a file
      * @param is the stream to read from
      * @throws IOException if there was an error reading the file
      * @throws GeomagDataException if there was an error in the data format - the
      *         messages associated with the exception will describe the problem */
    public static WDCHour read (InputStream is) 
    throws IOException, GeomagDataException
    {
        int lineNumber, count, componentIndex, index;
        double value, multiplier, tabBase;
        byte component, componentCodes [];
        char quietDayCode;
        boolean testUseQuietDay, useQuietDay;
        Date startDate [], date;
        String record, centuryString;
        BufferedReader reader;
        WDCHour WdcHour;
        String dateString, stationCode, testStationCode;
        ArrayList<Double> c1, c2, c3, c4, dataPtr;
        ArrayList<Double> c1Mean, c2Mean, c3Mean, c4Mean, meanPtr;
        ArrayList<Character> quietDay;
        
        // open the file
        reader = new BufferedReader (new InputStreamReader (is));
        
        // create arrays
        componentCodes = new byte [4];
        startDate = new Date [4];
        for (count=0; count<4; count++)
        {
            componentCodes [count] = ' ';
            startDate [count] = null;
        }
        c1 = new ArrayList<Double> ();
        c2 = new ArrayList<Double> ();
        c3 = new ArrayList<Double> ();
        c4 = new ArrayList<Double> ();
        c1Mean = new ArrayList<Double> ();
        c2Mean = new ArrayList<Double> ();
        c3Mean = new ArrayList<Double> ();
        c4Mean = new ArrayList<Double> ();
        quietDay = new ArrayList<Character> ();
        
        // read records from the file
        lineNumber = 0;
        stationCode = null;
        useQuietDay = false;
        while ((record = reader.readLine()) != null)
        {
            lineNumber ++;
            
            // records must be 120 bytes long
            if (record.length() != 120)
                throw new GeomagDataException ("Record length incorrect at line number " + Integer.toString (lineNumber));
            
            // split out the header fields
            try
            {
                testStationCode = record.substring (0, 3);
                switch (record.charAt (15))
                {
                    case ' ':
                        centuryString = "19";
                        testUseQuietDay = true;
                        break;
                    case '8':
                        if (record.charAt(14) == '2')
                        {
                            centuryString = "18";
                            testUseQuietDay = true;
                        }
                        else
                        {
                            // "18" is ambiguous as to wether the quiet day
                            // field is being used (not for date), but it is also extremely
                            // rare (we don't have much data this old) so assume it
                            // is century 18, not using quiet day
                            centuryString = "18";
                            testUseQuietDay = false;
                        }
                        break;
                    case '9':
                        centuryString = "19";
                        testUseQuietDay = false;
                        break;
                    case '0':
                        centuryString = "20";
                        testUseQuietDay = false;
                        break;
                    default:
                        throw new GeomagDataException ("Bad century / quiet day code: " + record.substring(14, 16));
                }
                dateString = centuryString + record.substring (3, 7) + record.substring (8, 10);
                date = headerInputDateFormat.parse(dateString);
                component = (byte) record.charAt (7);
                quietDayCode = record.charAt(14);
                tabBase = Double.parseDouble(record.substring (16, 20).trim());
            }
            catch (ParseException e)
            {
                throw new GeomagDataException ("Bad data in header (columns 1 - 20) at line number " + Integer.toString (lineNumber));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in header (columns 1 - 20) at line number " + Integer.toString (lineNumber));
            }
            
            // is this the first record
            if (stationCode == null)
            {
                // yes - fill out the header variables
                stationCode = testStationCode;
                useQuietDay = testUseQuietDay;
            }
            else
            {
                // no - check the header variables
                if (! stationCode.equalsIgnoreCase(testStationCode))
                    throw new GeomagDataException ("Station code changes at line number " + Integer.toString (lineNumber) + ": " + stationCode + " / " + testStationCode);
                if (useQuietDay != testUseQuietDay)
                    throw new GeomagDataException ("Use of quiet day flag changes at line number " + Integer.toString (lineNumber));
            }
            
            // work out which component to put the data in and convert the tab base to nT or minutes
            switch (component)
            {
                case 'X': componentIndex = 0; multiplier = 1.0; dataPtr = c1; meanPtr = c1Mean; tabBase = tabBase * 100.0; break;
                case 'Y': componentIndex = 1; multiplier = 1.0; dataPtr = c2; meanPtr = c2Mean; tabBase = tabBase * 100.0;  break;
                case 'Z': componentIndex = 2; multiplier = 1.0; dataPtr = c3; meanPtr = c3Mean; tabBase = tabBase * 100.0;  break;
                case 'H': componentIndex = 0; multiplier = 1.0; dataPtr = c1; meanPtr = c1Mean; tabBase = tabBase * 100.0;  break;
                case 'D': componentIndex = 1; multiplier = 0.1; dataPtr = c2; meanPtr = c2Mean; tabBase = tabBase * 60.0;   break;
                case 'I': componentIndex = 1; multiplier = 0.1; dataPtr = c2; meanPtr = c2Mean; tabBase = tabBase * 60.0;   break;
                case 'F': componentIndex = 3; multiplier = 1.0; dataPtr = c4; meanPtr = c4Mean; tabBase = tabBase * 100.0;  break;
                default:
                    throw new GeomagDataException ("Bad component code at line number " + Integer.toString (lineNumber));
            }
            if (componentCodes[componentIndex] == ' ')
                componentCodes[componentIndex] = component;
            else if (componentCodes[componentIndex] != component)
                throw new GeomagDataException ("Component code changes at line number " + Integer.toString (lineNumber));
            
            // check the times are contiguous
            if (startDate [componentIndex] == null)
                startDate [componentIndex] = date;
            else if (startDate[componentIndex].getTime () !=
                     (date.getTime() - ((long) dataPtr.size() * 3600000l)))
                throw new GeomagDataException ("Non-contiguous date/time at line number " + Integer.toString (lineNumber));
            
            // extract the data and mean
            try
            {
                for (count=0; count<24; count++)
                {
                    index = 20 + (count * 4);
                    value = Double.parseDouble (record.substring (index, index+4).trim());
                    if (value < 9999.0) value = tabBase + (value * multiplier);
                    else value = MISSING_DATA_SAMPLE;
                    dataPtr.add (new Double (value));
                }
                value = Double.parseDouble(record.substring(116, 120).trim());
                if (value < 9999.0) value = tabBase + (value * multiplier);
                else value = MISSING_DATA_SAMPLE;
                meanPtr.add (new Double (value));
            }
            catch (NumberFormatException e)
            {
                throw new GeomagDataException ("Bad number in data at line number " + Integer.toString (lineNumber));
            }
            
            // record the quiet day flag - only do this if we are using
            // the flags and the componentIndex is 0 (otherwise you will
            // get too many flags
            if (useQuietDay && (componentIndex == 0))
                quietDay.add (new Character (quietDayCode));
        }

        // check there was some data and fill missing F with blank data
        if (componentCodes [0] == ' ')
            throw new GeomagDataException ("Missing X or H component in data file");
        if (componentCodes [1] == ' ')
            throw new GeomagDataException ("Missing Y or D component in data file");
        if (componentCodes [2] == ' ')
            throw new GeomagDataException ("Missing Z component in data file");
        if (componentCodes [3] == ' ')
        {
            startDate[3] = startDate[0];
            for (count=0; count<c1.size(); count++)
                c4.add (new Double (MISSING_COMPONENT));
            for (count=0; count<c1Mean.size(); count++)
                c4Mean.add (new Double (MISSING_COMPONENT));
        }
        
        // check start dates are the same - also
        // check all components data are the same length - this could be changed
        // to tack missing values to the end of the short arrays - if you do
        // this remember to also add values to the means array
        for (count=1; count<4; count++)
        {
            if (startDate [0].getTime() != startDate [count].getTime())
                throw new GeomagDataException ("Components 1 and " + Integer.toString(count) + " start at different times");
        }
        if (c1.size() != c2.size())
            throw new GeomagDataException ("Components 1 and 2 have different amounts of data");
        if (c1.size() != c3.size())
            throw new GeomagDataException ("Components 1 and 3 have different amounts of data");
        if (c1.size() != c4.size())
            throw new GeomagDataException ("Components 1 and 4 have different amounts of data");
        
        // create the WDCMinute object and add data to it
        WdcHour = new WDCHour (stationCode, new String (componentCodes), useQuietDay);
        WdcHour.addData (startDate[0], 3600000l, MISSING_DATA_SAMPLE, MISSING_COMPONENT,
                         c1, c2, c3, c4);
        
        // sort out the mean values and quiet day flags
        for (count=0; count<c1Mean.size(); count++)
        {
            WdcHour.setDataMean(0, count, c1Mean.get(count).doubleValue());
            WdcHour.setDataMean(1, count, c2Mean.get(count).doubleValue());
            WdcHour.setDataMean(2, count, c3Mean.get(count).doubleValue());
            WdcHour.setDataMean(3, count, c4Mean.get(count).doubleValue());
            if (useQuietDay)
                WdcHour.setQuietDayFlag(count, quietDay.get(count));
        }
        
        return WdcHour;
    }
    
    /** generate a WDC hour filename
     * @param prefix the prefix for the name (including any directory)
     * @param station_code the IAGA station code
     * @param start_date the start date for the data 
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the filename */
    public static String makeFilename (String prefix,
                                       String station_code, Date start_date,
                                       boolean force_lower_case)
    {
        String filename;
        
        filename = station_code + filenameDateFormat.format (start_date) + ".WDC";
        if (force_lower_case) filename = filename.toLowerCase();
        if (prefix == null) return filename;
        return prefix + filename;
    }

    private void initFormatObjects(){
        headerDateFormat = new SimpleDateFormat ("yyMM", DateUtils.english_date_format_symbols);
        headerDateFormat.setTimeZone(DateUtils.gmtTimeZone);
        number2digits = new DecimalFormat ("00", Utils.getScientificDecimalFormatSymbols());
    }
    
}
