/*
 * DirectoryListener.java
 *
 * Created on 05 June 2002, 19:49
 */

package bgs.geophys.ImagCD.ImagCDViewer.Interfaces;

import java.io.File;

/**
 *
 * @author  Simon
 * @version 
 */
public interface DirectoryListener 
{
  /** Called when a new directory is chosen
   * @param dir the new directory
   * @return true if the directory has been accepted, false otherwise */
  public boolean DirectoryChosen (File dir);
  
}
