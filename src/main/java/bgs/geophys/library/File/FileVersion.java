/*
 * FileVersion.java
 *
 * Created on 01 March 2005, 19:25
 */

package bgs.geophys.library.File;

import java.io.*;
import java.util.*;

/**
 * A class that holds details on versions of a file. The files are
 * stored on disk in the format <name>_v<number>.<type>
 *
 * @author  smf
 */
public class FileVersion
{
    
    /** a class used to hold (and sort) an individual file with
     * a version number */
    private class VersionOfAFile implements Comparable<VersionOfAFile>
    {
        private int version_no;
        private File file;
        
        /** create a new file with a version number
         * @param file the file to store
         * @param name the base name for all files
         * @param type the type for all files
         * @param case_ind set to true to perform case independant file name matches
         * @throws IOException if this file is not a version of the
         *         given name.type file */
        public VersionOfAFile (File file, String name, String type, boolean case_ind)
        throws IOException
        {
            boolean error;
            String search_prefix, file_prefix, search_suffix, file_suffix, filename;
            
            // construct the filename prefix and suffix
            error = false;
            search_prefix = name + "_v";
            search_suffix = type;
            filename = file.getName ();
            try
            {
              file_prefix = filename.substring (0, search_prefix.length());
              file_suffix = filename.substring (filename.length() - search_suffix.length());
              if (filename.length() <= search_prefix.length() + search_suffix.length()) error = true;
              else if (case_ind)
              {
                  if ((! file_prefix.equalsIgnoreCase(search_prefix)) ||
                      (! file_prefix.equalsIgnoreCase(search_prefix))) error = true;
              }
              else
              {
                  if ((! file_prefix.equals(search_prefix)) ||
                      (! file_prefix.equals(search_prefix))) error = true;
              }
              version_no = Integer.parseInt (filename.substring (search_prefix.length (), filename.length () - search_suffix.length()));
            }
            catch (IndexOutOfBoundsException e) { error = true; }
            catch (NumberFormatException e) { error = true; }
            
            if (error) throw new IOException ("Incorrect file name");
            this.file = file;
        }
        
        public int compareTo(VersionOfAFile o) 
        {
            if (this.version_no < o.version_no) return -1;
            if (this.version_no > o.version_no) return 1;
            return 0;
        }
    }
    
    // private members
    private File dir;
    private String name;
    private String type;
    private Vector<VersionOfAFile> versions;
    private boolean case_ind;
    
     /** Creates a new instance of FileVersion
      * @param dir a directory that holds the versions of the file
      * @param name that base name
      * @param type the file type (including leading '.')
      * @param case_ind set to true to perform case independant file name matches
      * @throws IOException if the directory is not valid */
     public FileVersion (File dir, String name, String type, boolean case_ind)
     throws IOException
     {
         int count;
         File files [];
         FileVersion.VersionOfAFile voaf;
         
         versions = new Vector<VersionOfAFile> ();

         // store details on the file
         this.dir = dir;
         this.name = name;
         this.type = type;
         this.case_ind = case_ind;
         
         // list all files in the given directory
         files = dir.listFiles ();
         if (files == null) throw new IOException (dir + " is not a directory");
         
         // find the ones that are versions of the given name.type
         for (count=0; count<files.length; count++)
         {
             try 
             { 
                 voaf = new FileVersion.VersionOfAFile (files[count], name, type, case_ind); 
                 versions.add (voaf);
             }
             catch (IOException e) { }
         }
         Collections.sort (versions);
     }

     /** get the number of versions that have been found */
     public int getNVersions ()         { return versions.size (); }
     
     /** get a file by its (array) index - files are sorted so that
      * the earliest index has the lowest version */
     public File getFile (int index)    { return ((FileVersion.VersionOfAFile) versions.get (index)).file; }
     
     /** get a file by its version number */
     public File getFileByVersion (int version_no)
     {
         int count;
         FileVersion.VersionOfAFile voaf;
         
         for (count=0; count<versions.size (); count++)
         {
             voaf = (FileVersion.VersionOfAFile) versions.get (count);
             if (voaf.version_no == version_no) return voaf.file;
         }
         return null; 
     }
     
     /** check if a file is included in the list of versions
      * @param file the file to check
      * @param check_dir if true include the full path in the check, otherwise
      *        only check the file name
      * @return the index of the file in the list, or negative number*/
     public int findFile (File file, boolean check_dir)
     {
         int count;
         File check1, check2;
         FileVersion.VersionOfAFile voaf;
         
         if (check_dir) check1 = file;
         else check1 = new File (file.getName());
         
         for (count=0; count<versions.size (); count++)
         {
             voaf = (FileVersion.VersionOfAFile) versions.get (count);
             if (check_dir) check2 = voaf.file;
             else check2 = new File (voaf.file.getName());
             if (case_ind)
             {
               if (check1.getPath().equalsIgnoreCase(check2.getPath())) return count;
             }
             else
             {
               if (check1.getPath().equals(check2.getPath())) return count;
             }
         }
         
         return -1;
     }
     
     /** add a file to the list - does not check if the file is already in the list
      * @param file the file to add
      * @throws IOException if the file is not valid for the list */
     public void addFile (File file)
     throws IOException
     {
         FileVersion.VersionOfAFile voaf;
         
         voaf = new FileVersion.VersionOfAFile (file, name, type, case_ind);
         versions.add (voaf);
         Collections.sort (versions);
     }

     /** given a version number, generate the file name
      * @param version_no the version number
      * @return the name of the file (without path) */
     public String makeFileName (int version_no)
     {
         return name + "_v" + Integer.toString (version_no) + type;
     }
     
}
