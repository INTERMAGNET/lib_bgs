/*
 * CDCountry.java
 *
 * Created on 01 May 2002, 22:56
 */

package bgs.geophys.ImagCD.ImagCDViewer.Data;

import java.io.File;

/**
 * Class to hold details about a country 
 *
 * @author  smf
 * @version 
 */
public class CDCountry
{
  /** code for the country (as used on INTERMAGNET CD-ROMs - may be different from 
   * ISO 3-digit code) */
  public String country_code;
  /** file containing map (plot) of the observatories in a country */
  public File map_plot;
  /** file containing plot of logos / contact details for a country */
  public File agency_plot;
  /** file containing text details about the observatories in a country */
  public File readme_text;
  /** name of the country, or null if the name could not be found */
  public String country_name;
  /** ISO 3 digit country code */
  public String iso3digit_code;

  /** create an empty country objects
   *
   * @param code - the country code */
  public CDCountry (String code)
  {
    country_code = code;
    map_plot = null;
    agency_plot = null;
    readme_text = null;
    country_name = null;
    iso3digit_code = null;
  }
}
