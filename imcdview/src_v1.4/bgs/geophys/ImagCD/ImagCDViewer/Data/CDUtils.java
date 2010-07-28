/*
 * CDUtils.java
 *
 * Created on 29 April 2002, 21:20
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

/**
 * CDUtils is a set of static utilites (subroutines as we call them)
 * and codes to help when working with the Intermagnet CD.
 *
 * @author  smf
 * @version 
 */
public class CDUtils extends Object 
{

  // types of gemoagnetic data that the CD holds
  /** code for minute mean data */
  public static final int MINUTE_MEAN =      1;
  /** code for hourly mean data */
  public static final int HOURLY_MEAN =      2;
  /** code for daily mean data */
  public static final int DAILY_MEAN =       3;
  /** code for k index data */
  public static final int K_INDEX =          4;
  /** code for Ak index data */
  public static final int AK_INDEX =         5;
  
}
