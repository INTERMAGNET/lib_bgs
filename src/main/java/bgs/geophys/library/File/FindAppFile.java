/*
 * FindAppFile.java
 *
 * Created on 20 October 2003, 08:50
 */

package bgs.geophys.library.File;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.net.*;

/**
 * You often want to distribute data files with an application. This class helps
 * you to do that. If the application consists of a set of class files, the
 * data files can live in directories under an of the class files. If the application
 * consists of a jar file, the data can live anywhere in the jar file. Either way
 * this class will be able to find the file, given its name.
 * @author  Administrator
 */
public class FindAppFile 
{
    
    /** inner class that implements a simple pattern matching file filter */
    private static class AppFileFilter implements FileFilter
    {
        private File stored_file;
        public AppFileFilter (String filename)
        {
            stored_file = new File (filename);
        }
        public boolean accept (File file)
        {
            int count;
            // the next line should be: count=stored_file.compareTo(file)
            // however this doesn't seem to match correctly
            // the version here matches the strings, which works OK, but means that
            // file names are always case dependant (which is wrong for some OS)
            count = stored_file.getName().compareTo (file.getName ());
            if (count == 0) return true;
            return false;
        }
    }
 
    /** constant showing that no file has been found */
    private static final int NO_FILE_FOUND = 0;
    
    /** constant showing that a normal file was found */
    private static final int FOUND_NORMAL_FILE = 1;
    
    /** constant showing that a jar entry was found */
    private static final int FOUND_JAR_ENTRY = 2;

    /** member describing what has been found */
    private int file_found_code;
    
    /** member holding the name of any normal file found */
    private File normal_file;
    
    /** member holding the name of the application's jar file - it is assumed that
     * there is only one of these, so it can be made static and found once */
    private static boolean jar_file_checked;
    private static JarFile jar_file;
    
    /** member holding the name of any jar entry found */
    private JarEntry jar_entry;

    /** initialise static members */
    static
    {
        jar_file_checked = false;
    }
    
    /** Creates a new instance of FindAppFile
     * @param object an object that is used as follows:
     *        1.) find the jar file that contains the class (if any) and attempt to
     *            find the 'data' file in the jar file
     *        2.) attempt to find the name of the object's package path - then
     *            attempt to find the name of the 'data' file from the
     *            path of the class file, or its subdirectories
     * @param filename the name of the 'data' file to find */
    public FindAppFile (Object object, String filename) 
    {
        file_found_code = NO_FILE_FOUND;
        
        // try to find the application's jar file - only do this once because
        // it is time consuming - jar_file_checked and jar_file are static members
        if (! jar_file_checked)
        {
            jar_file = findMyJarFile (object);
            jar_file_checked = true;
        }

        // try to find the file in the application's jar file
        if (jar_file != null)
        {
            jar_entry = findFileInJar (jar_file, filename);
            if (jar_entry != null) file_found_code = FOUND_JAR_ENTRY;
        }
        
        // if the applications jar file was found, but the data file was
        // not found, this indicates that the file is missing - trying to
        // call the next piece of code will cause problems, so don't try
        // to find the file in the class hierarchy of the jar file was found
        if ((file_found_code == NO_FILE_FOUND) && (jar_file == null))
        {
            // try to find the file in the class hierarchy
            normal_file = findFileFromClass (object, filename);
            if (normal_file != null) file_found_code = FOUND_NORMAL_FILE;
        }
    }

    /** has any file been found ?? 
     * @return true if it has */
    public boolean isFound ()
    {
        if (file_found_code == NO_FILE_FOUND) return false;
        return true;
    }
    
    /** has a normal file been found ?? 
     * @return true if it has */
    public boolean isNormalFile ()
    {
        if (file_found_code == FOUND_NORMAL_FILE) return true;
        return false;
    }

    /** has a jar entry been found ?? 
     * @return true if it has */
    public boolean isJarEntry ()
    {
        if (file_found_code == FOUND_JAR_ENTRY) return true;
        return false;
    }

    /** if a normal file has been found, return it
     * @return the file, or null */
    public File getNormalFile ()
    {
        if (file_found_code != FOUND_NORMAL_FILE) return null;
        return normal_file;
    }

    /** if a jar entry has been found, return the name of the jar that contains the entry
     * @return the file, or null */
    public JarFile getJarFile ()
    {
        if (file_found_code != FOUND_JAR_ENTRY) return null;
        return jar_file;
    }
    
    /** if a jar entry has been found, return the jar entry
     * @return the jar entry, or null */
    public JarEntry getJarEntry ()
    {
        if (file_found_code != FOUND_JAR_ENTRY) return null;
        return jar_entry;
    }
    
    /** return a file stream to the file, whether it is a normal file or
     * a jar entry
     * @return an input stream
     * @throws FileNotFoundException if the file could not be found 
     * @throws SecurityException if the file could not be opened
     * @throws IOException if an IO error occurred
     * @throws ZIPException if the jar file was corrupt */
    public InputStream getInputStream ()
    throws FileNotFoundException, SecurityException, IOException, ZipException
    {
        FileInputStream file_input_stream;
        InputStream input_stream;
        
        switch (file_found_code)
        {
        case FOUND_NORMAL_FILE:
            file_input_stream = new FileInputStream (normal_file);
            return file_input_stream;
        case FOUND_JAR_ENTRY:
            input_stream = jar_file.getInputStream (jar_entry);
            return input_stream;
        }
        return null;
    }
    
    /** convert the file that was found to a URL
     * @return the URL (or null if no file was found)
     * @throws MalformedURLException if there is an error creating the URL */
    public URL getURL ()
    throws MalformedURLException
    {
        switch (file_found_code)
        {
        case FOUND_NORMAL_FILE:
            return new URL ("file:" + normal_file);
        case FOUND_JAR_ENTRY:
            return new URL ("jar:file:" + jar_file.getName() + "!/" + jar_entry.getName());
        }
        return null;
    }

    /** get the name of the file that was found - the name does NOT include any
      * path information */
    public String getName ()
    {
        int count;
        String path;
        
        switch (file_found_code)
        {
        case FOUND_NORMAL_FILE:
            return normal_file.getName();
        case FOUND_JAR_ENTRY:
            // zip (and jar) file paths always use forward slash character
            path = jar_entry.getName ();
            count = path.lastIndexOf('/');
            
            // check for no path information
            if (count < 1) return path;
            
            // check for path terminated by /
            if (count >= path.length() -1)
            {
                count = path.lastIndexOf ('/', count -1);
                if (count < 1) return path;
            }
            
            return path.substring(count +1);
        }
        return null;
    }
    
    /** copyFile Copy the file that was found to the destination directory
     * @param dest_dir the destination directory as a File object
     * @returns the copied file
     * @throws FileNotFoundException if the file could not be found 
     * @throws SecurityException if the file could not be opened
     * @throws IOException if an IO error occurred
     * @throws ZIPException if the jar file was corrupt */
    public File copyFile (File dest_dir)
    throws FileNotFoundException, SecurityException, IOException, ZipException
    {
        InputStream input_stream;
        FileOutputStream output_stream;
        File dest_file;

        // open source and destination
        dest_file = new File (dest_dir, getName());
        input_stream = getInputStream();
        output_stream = new FileOutputStream (dest_file);
        FileUtils.copy (getInputStream(), output_stream);

        // close streams
        input_stream.close ();
        output_stream.flush ();
        output_stream.close ();
        
        return dest_file;
    }

    /** A function to find a 'data' file stored in the class hierarchy.
     * The filename will be found in any directory/subdirectory of the
     * class file that is specified by object, so if object lives in
     * c:\simon\java, this is the starting point for searching for the
     * data file.
     * @param object an object used to reference the file from
     * @param filename the name of the file to find
     * @return a reference to the file in the form of a URL or null if it could not be found */
    public static File findFileFromClass (Object object, String filename)
    {
        URL url;
        Class class_obj;
        FindFile find_file;
        File file, found_file;
        AppFileFilter app_file_filter;
        String reference_path, string;
        
        try
        {
            // find the path to the reference object
            class_obj = object.getClass();
            url = class_obj.getResource(".");
            reference_path = url.getFile();

            // break the filename into name and directory (if any)
            // tack the directory onto the path to the reference object
            file = new File (filename);
            string = file.getParent ();
            if (string != null)
            {
                reference_path = reference_path + File.separator + string;
                filename = file.getName ();
            }
            
            // attempt to find the file
            find_file = new FindFile ();
            app_file_filter = new AppFileFilter (filename);
            found_file = find_file.first (reference_path, true, false, false, app_file_filter);
            return found_file;
        }
        catch (Exception e) { }
        return null;
    }

    /********************************************************************
     * find the jar file that this application is running from (if any)
     * @param object the object whose jar file we will attemopt to find
     * @return the jar file or null if the application is not running from
     * a jar file
     ********************************************************************/
    public static JarFile findMyJarFile (Object object)
    {
        JarFile jar_file;
        JarEntry jar_entry;
        StringTokenizer tokens;
        String jar_file_list, class_name;
        
        // java sets the classpath to point to the jar file, so retrieve the class path
        jar_file_list = System.getProperty("java.class.path");
        if (jar_file_list == null) return null;

        // find the name of the object's class and convert it to a jar entry
        class_name = object.getClass().getName();
        tokens = new StringTokenizer (class_name, ".");
        class_name = "";
        while (tokens.hasMoreTokens())
        {
            if (class_name.length() > 0) class_name += "/";
            class_name += tokens.nextToken();
        }
        class_name += ".class";
        
        // try to find a jar file that contains this class
        tokens = new StringTokenizer (jar_file_list, File.pathSeparator);
        while (tokens.hasMoreTokens())
        {
            try
            {
                jar_file = new JarFile (tokens.nextToken());
                jar_entry = findFileInJar (jar_file, class_name);
                if (jar_entry != null) return jar_file;
            }
            catch (Exception e) { }
        }
        return null;
    }
    
    /********************************************************************
     * attempt to find a file inside a JAR
     * @param jar_file the jar file
     * @param filename name of the file to find
     * @return the jar entry or null
     ********************************************************************/
    public static JarEntry findFileInJar (JarFile jar_file, String filename)
    {
      int n_tokens, count, n_directories;
      Enumeration entries;
      JarEntry jar_entry;
      String jar_entry_name, normal_filename, string;
      StringTokenizer tokens;

      // substitute '/' for the path separator in the filename
      tokens = new StringTokenizer (filename, "\\/");
      normal_filename = "";
      n_directories = 0;
      while (tokens.hasMoreTokens ())
      {
          if (normal_filename.length() > 0) normal_filename += "/";
          normal_filename += tokens.nextToken();
          n_directories ++;
      }

      // try to find the file in the jar entries
      try
      {
          // list the entries
          for (entries = jar_file.entries(); entries.hasMoreElements(); )
          {
              // make a path that consists of the same number of directories as the filename
              jar_entry = (JarEntry) entries.nextElement();
              tokens = new StringTokenizer (jar_entry.getName (), "\\/");
              jar_entry_name = "";
              n_tokens = tokens.countTokens();
              for (count = n_tokens -1; count >= 0; count --)
              {
                  string = tokens.nextToken();
                  if (count < n_directories)
                  {
                      if (jar_entry_name.length() > 0) jar_entry_name += "/";
                      jar_entry_name += string;
                  }
              }

              // test if the filename and the jar entry are the same
              if (jar_entry_name.compareTo (normal_filename) == 0) return jar_entry;
          }
      }
      catch (Exception e) { e.printStackTrace(); }
      return null;
    }

}

