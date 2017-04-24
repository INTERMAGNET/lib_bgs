/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Magnetogram.BLV;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author jex
 */
public class BLVXYToolTipGenerator implements XYToolTipGenerator{
 
String label;
Integer year;
BLVXYToolTipGenerator(String lab, Integer yr){
       label = new String(lab);
       year = yr;
     }
     
       public String generateToolTip(XYDataset xyDataset, int series, int item)
          {
              //get day of Year
             Calendar c = new GregorianCalendar();
//             c.setTimeInMillis(xyDataset.getX(series, item).longValue());
             c.set(Calendar.YEAR, year);
             c.set(Calendar.DAY_OF_YEAR,xyDataset.getX(series,item).intValue());
             DateFormat df = new SimpleDateFormat("DDD MMM-dd");
             
             Double val =  xyDataset.getY(series, item).doubleValue();

             return String.format("%s %s,  %.1f", label, df.format(c.getTime()),val);

}
 
   }