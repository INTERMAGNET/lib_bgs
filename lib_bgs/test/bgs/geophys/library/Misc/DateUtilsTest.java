/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Misc;

import java.util.Date;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

/**
 *
 * @author smf
 */
public class DateUtilsTest extends TestCase {
    
    public DateUtilsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createDate method, of class DateUtils.
     */
    public void testCreateDate() {

        int year, month, day;
        Date lib_date;
        GregorianCalendar calendar;
        String errmsg;
        
        System.out.println("createDate");
        
        for (year=1850; year<2050; year++)
        {
            for (month=0; month<12; month++)
            {
                for (day=0; day<DateUtils.daysInMonth(month, year); day++)
                {
                    lib_date = DateUtils.createDate(year, month, day, 0, 0, 0, 0);
                    calendar = new GregorianCalendar (DateUtils.gmtTimeZone);
                    calendar.setTime(lib_date);
                    if (calendar.get (GregorianCalendar.MILLISECOND) != 0) errmsg = "Millisecond != 0";
                    else if (calendar.get (GregorianCalendar.SECOND) != 0) errmsg = "Second != 0";
                    else if (calendar.get (GregorianCalendar.MINUTE) != 0) errmsg = "Minute != 0";
                    else if (calendar.get (GregorianCalendar.HOUR_OF_DAY) != 0) errmsg = "Hour != 0";
                    else if (calendar.get (GregorianCalendar.DAY_OF_MONTH) != (day +1)) errmsg = "Day != " + Integer.toString (day);
                    else if (calendar.get (GregorianCalendar.MONTH) != month) errmsg = "Month != " + Integer.toString (month);
                    else if (calendar.get (GregorianCalendar.YEAR) != year) errmsg = "Year != " + Integer.toString (year);
                    else errmsg = null;
                    if (errmsg != null)
                    {
                        this.fail (errmsg + ": " + lib_date.toString());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Test of getDurationMs method, of class DateUtils.
     */
//    public void testGetDurationMs() {
//        System.out.println("getDurationMs");
//        long duration = 0L;
//        int durationUnits = 0;
//        int month = 0;
//        int year = 0;
//        long expResult = 0L;
//        long result = DateUtils.getDurationMs(duration, durationUnits, month, year);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getMonthName method, of class DateUtils.
     */
//    public void testGetMonthName() {
//        System.out.println("getMonthName");
//        int month = 0;
//        int caseType = 0;
//        int width = 0;
//        String expResult = "";
//        String result = DateUtils.getMonthName(month, caseType, width);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of isLeapYear method, of class DateUtils.
     */
//    public void testIsLeapYear() {
//        System.out.println("isLeapYear");
//        int year = 0;
//        boolean expResult = false;
//        boolean result = DateUtils.isLeapYear(year);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of daysInYear method, of class DateUtils.
     */
//    public void testDaysInYear() {
//        System.out.println("daysInYear");
//        int year = 0;
//        int expResult = 0;
//        int result = DateUtils.daysInYear(year);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of daysInMonth method, of class DateUtils.
     */
//    public void testDaysInMonth() {
//        System.out.println("daysInMonth");
//        int month = 0;
//        int year = 0;
//        int expResult = 0;
//        int result = DateUtils.daysInMonth(month, year);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of parseDate method, of class DateUtils.
     */
//    public void testParseDate() {
//        System.out.println("parseDate");
//        String string = "";
//        Date expResult = null;
//        Date result = DateUtils.parseDate(string);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of parseDate2 method, of class DateUtils.
     */
//    public void testParseDate2() {
//        System.out.println("parseDate2");
//        String string = "";
//        int[] expResult = null;
//        int[] result = DateUtils.parseDate2(string);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of formatDate method, of class DateUtils.
     */
//    public void testFormatDate() {
//        System.out.println("formatDate");
//        int day = 0;
//        int month = 0;
//        int year = 0;
//        String expResult = "";
//        String result = DateUtils.formatDate(day, month, year);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
