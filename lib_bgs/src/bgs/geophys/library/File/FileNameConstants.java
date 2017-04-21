/*
 * FileNameConstants.java
 *
 * Created on 14 November 2003, 12:38
 */

package bgs.geophys.library.File;

import java.io.*;

/**
 * This class holds some constants and static methods that are useful
 * for finding configuration files and the like.
 *
 * @author  smf
 */
public class FileNameConstants 
{

    /** a constant used for all options to do with data logging programs */
    public static final String DATALOGGER_PACKAGE_NAME = "dl";
    /** a constant used for all options to do with Intermagnet */
    public static final String INTERMAGNET_PACKAGE_NAME = "imag";
    
    /* a constant used for the list of remote data logging systems */
    public static final String DATALOGGER_SYSTEM_LIST_FILENAME = "systems_list.cfg";
    /* a constant used for the name of the options file */
    public static final String DATALOGGER_OPTIONS_FILENAME = "options.cfg";
    /* a constant used for the name of the GDASView options file */
    public static final String GDASVIEW_OPTIONS_FILENAME = "GDASDefaults.txt";
    /* a constant used for the name of the IMAG CD Viewer options file */
    public static final String IMCD_VIEWER_OPTIONS_FILENAME = "ImCdDataViewer.cfg";
    /* a constant used for the name of the IMAG CD Viewer options file */
    public static final String IMAG_CDF_BROWSER_OPTIONS_FILENAME = "ImCDFBrowser.cfg";

    
    /** given a package name, create a directory name for option / configuration storage.
     * Create the directory if it does not already exist
     * @param name the name of the package
     * @return the directory to be used for options and configuration files */
    public static String getConfigDir (String package_name)
    {
        String options_dirname;
        File options_dir;
        
        // build directory name and check that it exists
        options_dirname = System.getProperty("user.home") + System.getProperty("file.separator");
        options_dirname += "." + package_name + System.getProperty("file.separator");
        options_dir = new File (options_dirname);
        if (!options_dir.exists()) options_dir.mkdir();
        return options_dirname;
    }

    /** given a package name and a filename find a place to put the file.
     * @param name the name of the package
     * @return the directory to be used for options and configuration files */
    public static String getConfigFileName (String package_name, String filename)
    {
        return getConfigDir (package_name) + filename;
    }

}
