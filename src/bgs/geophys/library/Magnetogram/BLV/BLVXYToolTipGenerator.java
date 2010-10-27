/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Magnetogram.BLV;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author jex
 */
public class BLVXYToolTipGenerator implements XYToolTipGenerator{
 
String label;
BLVXYToolTipGenerator(String lab){
       label = new String(lab);  
     }
     
       public String generateToolTip(XYDataset xyDataset, int series, int item)
          {
              //get day of Year
             Calendar c = new GregorianCalendar();
             c.setTimeInMillis(xyDataset.getX(series, item).longValue());
             DateFormat df = new SimpleDateFormat("DDD MMM-dd");
             
             Double val =  xyDataset.getY(series, item).doubleValue();
//             Integer doy = c.get(Calendar.DAY_OF_YEAR);
              return String.format("%s %s,  %.1f", label, df.format(c.getTime()),val);

}
 
   }