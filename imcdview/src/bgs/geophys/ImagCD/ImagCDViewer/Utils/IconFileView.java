/*
 * IconFileView.java
 *
 * class to implement a file view that shows INTERMAGNET
 * icons in a JFileChooser - in many of the methods null is 
 * returned, in which case the default file view will be used
 *
 * Created on 16 February 2007, 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDException;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDYear;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

/**
 *
 * @author smf
 */
public class IconFileView extends FileView
{
    
    private ImageIcon imag_icon;
    private File lastDir;
    private boolean lastDirResult;
        
    /** create a new IconFileView */
    public IconFileView ()
    {
        super ();
        
        imag_icon = GlobalObjects.imag_icon;
            
        lastDir = null;
    }

    /** get the name of this file - delelgate to default method */
    @Override
    public String getName (File f) { return null; }

    /** get the description of this file - delelgate to default method */
    @Override
    public String getDescription (File f) { return null; }
    
    /** find out whether this file istrversable - delelgate to default method */
    public Boolean isTraversable (File f) { return null; }

    /** get a description for this file */
    public String getTypeDescription (File f) 
    {
        // if it isn't an INTERMAGNET directory, delegate */
        if (isImagDirectory(f)) return "INTERMAGNET data directory";
        return null;
    }

    /** get an icon for this file */
    public Icon getIcon(File f) 
    {
        // if it isn't an INTERMAGNET directory, delegate */
        if (isImagDirectory(f)) return imag_icon;
        return null;
    }

    /** work out whether the given file is an INTERMAGNET directory */
    private boolean isImagDirectory (File dir)
    {
        int count;
        File files [];

        if (dir == null) return false;
        if (! dir.isDirectory()) return false;
        if (dir.equals(lastDir)) return lastDirResult;

        lastDir = dir;
        lastDirResult = false;
        files = dir.listFiles();
        for (count=0; count<files.length; count++)
        {
            try
            {
                CDYear.TestYearDir (files [count]);
                lastDirResult = true;
                break;
            }
            catch (CDException e) { }
        }
        return lastDirResult;
    }
    
}
