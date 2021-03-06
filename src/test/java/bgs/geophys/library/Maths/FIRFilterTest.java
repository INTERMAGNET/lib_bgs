/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Maths;

import bgs.geophys.library.Data.ImagCDF.ImagCDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smf
 */
public class FIRFilterTest {
    
    public FIRFilterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testFIRFilter ()
    {
        // The filter used for second to minute conversion in the 'gm_convert' application
        FIRFilter filter = new FIRFilter(FIRFilter.FILTER_GAUSS, 91, 1.0, 15.90062181, ImagCDF.MISSING_DATA_VALUE, 10);
        
        // a data set that spans a data gap
        double data1 [] = {99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00};
        double data2 [] = {99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 15023.30, 15023.30,
                           15023.40, 15023.40, 15023.50, 15023.60, 15023.50, 15023.60, 15023.50, 15023.60,
                           15023.50, 15023.40, 15023.40, 15023.40, 15023.40, 15023.40, 15023.40, 15023.30,
                           15023.40, 15023.40, 15023.40};
        double data3 [] = {99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00, 99999.00,
                           99999.00, 99999.00, 15023.30, 15023.30, 15023.40, 15023.40, 15023.50, 15023.60,
                           15023.50, 15023.60, 15023.50, 15023.60, 15023.50, 15023.40, 15023.40, 15023.40,
                           15023.40, 15023.40, 15023.40, 15023.30, 15023.40, 15023.40, 15023.40, 15023.50,
                           15023.60, 15023.60, 15023.50, 15023.60, 15023.50, 15023.40, 15023.40, 15023.30,
                           15023.20, 15023.20, 15023.10, 15023.00, 15023.00, 15023.00, 15023.00, 15023.20,
                           15023.20, 15023.20, 15023.20, 15023.40, 15023.40, 15023.30, 15023.40, 15023.40,
                           15023.30, 15023.30, 15023.30, 15023.30, 15023.30, 15023.30, 15023.20, 15023.20,
                           15023.20, 15023.20, 15023.30, 15023.30, 15023.40, 15023.40, 15023.40, 15023.40,
                           15023.50, 15023.50, 15023.50, 15023.40, 15023.40, 15023.30, 15023.30, 15023.30,
                           15023.30, 15023.20, 15023.30, 15023.20, 15023.30, 15023.30, 15023.30, 15023.40,
                           15023.40, 15023.50, 15023.60};
        
        assertEquals(99999.0,            filter.applyFilter(data1), 0.0001);
        assertEquals(99999.0,            filter.applyFilter(data2), 0.0001);
        assertEquals(15023.315273278487, filter.applyFilter(data3), 0.0001);
        
    }
}