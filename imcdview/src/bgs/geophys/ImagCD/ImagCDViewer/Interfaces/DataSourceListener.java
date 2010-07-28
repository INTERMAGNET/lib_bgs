/*
 * DataSourceListener.java
 *
 * Created on 05 June 2002, 20:39
 */

package bgs.geophys.ImagCD.ImagCDViewer.Interfaces;

import bgs.geophys.ImagCD.ImagCDViewer.Data.*;

/**
 *
 * @author  Simon
 * @version 
 */
public interface DataSourceListener 
{
  /** Called when a list of data sources is chosen
   * @param list the new list of data sources */
  public void dataSourcesChosen (CDDatabaseList list);

}
