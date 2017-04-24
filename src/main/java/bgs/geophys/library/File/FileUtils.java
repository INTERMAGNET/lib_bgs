/*
 * FileUtils.java
 *
 * Created on 20 November 2003, 17:15
 */

package bgs.geophys.library.File;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.ErrorHandler;

/**
 * Static methods that are handy when working with files.
 * @author  smf
 */
public class FileUtils 
{
    
  /** an interface used to respond to exceptions during file operations like
   * copy and delete */
  public interface ExceptionHandler
  {
    /** called when there is an unrecoverable exception
     * @param exception details of the error */
    public void onUnrecoverableFileException (Exception exception);
    /** called when there is a recoverable exception
     * @param exception details of the error
     * @return true to continue the operation, false to halt */
    public boolean onRecoverableFileException (Exception exception);
    /** called when a file is about to be overwritten
     * @param file the name of the file that is about to be overwritten
     * @return true to overwrite, false otherwise */
    public boolean onFileOverwriteQuery (File file);
  }
  
  /** method to ensure that a read request is completed
   * @param input_stream the stream to read from
   * @param bytes the array to completely fill
   * @returns the number of bytes read from the stream
   * @throws IOException if there is a read error */
  public static int readFully (InputStream input_stream, byte bytes [])
  throws IOException
  {
      return readFully (input_stream, bytes, 0, bytes.length);
  }

  /** method to ensure that a read request is completed
   * @param input_stream the stream to read from
   * @param bytes the array to completely fill
   * @param offset the offest in bytes to start filling
   * @param length the number of bytes to read
   * @returns the number of bytes read from the stream
   * @throws IOException if there is a read error */
  public static int readFully (InputStream input_stream, byte bytes [], int offset, int length)
  throws IOException
  {
    int count, count2;
    count = 0;
    while (count < length)
    {
        count2 = input_stream.read (bytes, offset + count, length - count);
        if (count2 <= 0) return count;
        count += count2;
    }
    return count;
  }
  
  /** method to copy one file to another
   * @param input_file the file to copy from
   * @param output_file the file to copy to (may be a directory instead of a file)
   * @throws FileNotFoundException if the file could not be found 
   * @throws SecurityException if the file could not be opened
   * @throws IOException if an IO error occurred
   */
  public static void copy (File input_file, File output_file)
  throws FileNotFoundException, SecurityException, IOException
  {
    File target;
    FileInputStream input_stream;
    FileOutputStream output_stream; 

    // check if output_file is a directory
    if (output_file.isDirectory()) target = new File (output_file, input_file.getName());
    else target = output_file;
        
    // open source and destination
    input_stream = new FileInputStream (input_file);
    output_stream = new FileOutputStream (target);
    copy (input_stream, output_stream);
    output_stream.close();
    input_stream.close();
 }
  
  /** method to copy one file to another
   * @param input_url the file to copy from
   * @param output_file the file to copy to (may be a directory instead of a file)
   * @throws FileNotFoundException if the file could not be found 
   * @throws SecurityException if the file could not be opened
   * @throws IOException if an IO error occurred
   */
  public static void copy (URL input_file, File output_file)
  throws FileNotFoundException, SecurityException, IOException
  {
    File target;
    FileOutputStream output_stream; 
    URLConnection connection;

    // check if output_file is a directory
    if (output_file.isDirectory()) target = new File (output_file, input_file.getFile());
    else target = output_file;
        
    // open source and destination
    connection = input_file.openConnection();
    output_stream = new FileOutputStream (target);
    copy (connection.getInputStream(), output_stream);
    output_stream.close();
 }
  
  /** method to copy one file or directory to another directory recursively
   * @param input_file the file or directory to copy from
   * @param output_dir the directory to copy to
   * @throws FileNotFoundException if the directory could not be found or the output is not a directory
   * @throws SecurityException if the file could not be opened
   * @throws IOException if an IO error occurred */
  public static void copyRecursively (File input_file, File output_dir)
  throws FileNotFoundException, SecurityException, IOException
  {
      int count;
      File file_list [], new_output_dir;
      
      // check the input file and output directory
      if (! input_file.exists()) throw new FileNotFoundException ("Source file does not exist: " + input_file.getAbsolutePath());
      if (! output_dir.exists()) throw new FileNotFoundException ("Target directory does not exist: " + output_dir.getAbsolutePath());
      if (! output_dir.isDirectory()) throw new FileNotFoundException ("Target must be a directory: " + output_dir.getAbsolutePath());
      
      // is the input a directory ??
      if (input_file.isDirectory())
      {
        // yes - list its contents and recurse
        new_output_dir = new File (output_dir, input_file.getName());
        new_output_dir.mkdir();
        if (! new_output_dir.isDirectory())
          throw new FileNotFoundException ("Unable to create directory: " + new_output_dir.getAbsolutePath());
        file_list = input_file.listFiles();
        if (file_list == null) file_list = new File [0];
        for (count=0; count<file_list.length; count++)
          copyRecursively (file_list [count], new_output_dir);
      }
      else
      {
        copy (input_file, output_dir);
      }
  }
  
  /** method to copy one file or directory to another directory recursively
   * with external exception handling
   * @param input_file the file or directory to copy from
   * @param output_dir the directory to copy to
   * @param exception_handler an exception handling object
   * @return false if user cancelled or there was an unrecoverable exception,
   *         true otherwise (even though there may have been exceptions) */
  public static boolean copyRecursively (File input_file, File output_dir, 
                                         ExceptionHandler exception_handler)
  {
      int count;
      File file_list [], new_output_dir, test_file;
      
      // check the input file and output directory
      if (! input_file.exists())
      {
        exception_handler.onUnrecoverableFileException(new FileNotFoundException ("Source file does not exist: " + input_file.getAbsolutePath()));
        return false;
      }
      if (! output_dir.exists()) 
      {
        exception_handler.onUnrecoverableFileException(new FileNotFoundException ("Target directory does not exist: " + output_dir.getAbsolutePath()));
        return false;
      }
      if (! output_dir.isDirectory()) 
      {
        exception_handler.onUnrecoverableFileException(new FileNotFoundException ("Target must be a directory: " + output_dir.getAbsolutePath()));
        return false;
      }
      
      // are we copying a directory ??
      if (input_file.isDirectory())
      {
        // yes - list its contents and recurse
        new_output_dir = new File (output_dir, input_file.getName());
        new_output_dir.mkdir();
        if (! new_output_dir.isDirectory())
        {
          if (! exception_handler.onRecoverableFileException(new FileNotFoundException ("Unable to create directory: " + new_output_dir.getAbsolutePath())))
            return false;
        }
        else
        {
          file_list = input_file.listFiles();
          if (file_list == null) file_list = new File [0];
          for (count=0; count<file_list.length; count++)
          {
            if (! copyRecursively (file_list [count], new_output_dir, exception_handler))
              return false;
          }
        }
      }
      else
      {
        // no - copy the file
        try
        {
          // no - check if the destination exists and copy the file
          test_file = new File (output_dir, input_file.getName());
          if (test_file.exists())
          {
            if (exception_handler.onFileOverwriteQuery(test_file))
              copy (input_file, output_dir);
          }
          else copy (input_file, output_dir);
        }
        catch (Exception e)
        {
          if (! exception_handler.onRecoverableFileException (e)) 
            return false;
        }
      }
          
      // normal successful exit
      return true;
  }
  
  /** method to recursively delete a file or directory
   * @param delete_file the file or directory to delete
   * @throws FileNotFoundException if the directory could not be found or the output is not a directory
   * @throws SecurityException if the file could not be opened
   * @throws IOException if the file could not be deleted */
  public static void deleteRecursively (File delete_file)
  throws FileNotFoundException, SecurityException, IOException
  {
      int count;
      File file_list [];
      
      if (! delete_file.exists()) throw new FileNotFoundException ("File does not exist: " + delete_file.getAbsolutePath());
      
      // is the input a directory ??
      if (delete_file.isDirectory())
      {
        // yes - list its contents, recurse
        file_list = delete_file.listFiles();
        if (file_list == null) file_list = new File [0];
        for (count=0; count<file_list.length; count++)
          deleteRecursively (file_list [count]);
      }

      // delete the file
      if (! delete_file.delete())
          throw new IOException ("Unable to delete: " + delete_file.getAbsolutePath());
  }
  
  /** method to recursively delete a file or directory
   * with external exception handling
   * @param delete_file the file or directory to delete
   * @param exception_handler an exception handling object
   * @return false if user cancelled or there was an unrecoverable exception,
   *         true otherwise (even though there may have been exceptions) */
  public static boolean deleteRecursively (File delete_file,
                                           ExceptionHandler exception_handler)
  {
    int count;
    File file_list [];
      
    if (! delete_file.exists()) 
    {
      exception_handler.onUnrecoverableFileException(new FileNotFoundException ("File does not exist: " + delete_file.getAbsolutePath()));
      return false;
    }
      
    // is the input a directory ??
    if (delete_file.isDirectory())
    {
      // yes - list its contents, recurse, then delete the directory
      file_list = delete_file.listFiles();
      if (file_list == null) file_list = new File [0];
      for (count=0; count<file_list.length; count++)
      {
        if (! deleteRecursively (file_list [count], exception_handler))
          return false;
      }
      
    }

    // delete the file / directory
    if (! delete_file.delete())
    {
      if (! exception_handler.onRecoverableFileException(new IOException ("Unable to delete: " + delete_file.getAbsolutePath())))
        return false;
    }
    
    // normal successful exit
    return true;
  }

           
  /** method to copy one stream to another
   * @param input_stream the input stream
   * @param output_stream the output stream
   * @throws IOException if an IO error occurred
   */
  public static void copy (InputStream input_stream, OutputStream output_stream)
  throws IOException
  {
    int count;
    byte [] buffer;

    buffer = new byte [8192];
    for (count = input_stream.read (buffer); count >= 0; count = input_stream.read (buffer))
      output_stream.write (buffer, 0, count);
  }
  
    /**
     * Recursively walk a directory tree and return a List of all
     * Files found; the List is sorted using File.compareTo.
     *
     *
     * @param startingDir is a valid directory, which can be read.
     */
    static public List<File> getFileListing( File startingDir ) throws FileNotFoundException{
        validateDirectory(startingDir);
        List<File> result = new ArrayList<File>();
        
        File[] filesAndDirs = startingDir.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        Iterator<File> filesIter = filesDirs.iterator();
        File file = null;
        while ( filesIter.hasNext() ) {
            file = filesIter.next();
            result.add(file); //always add, even if directory
            if (!file.isFile()) {
                //must be a directory
                //recursive call!
                List<File> deeperList = getFileListing(file);
                result.addAll(deeperList);
            }
            
        }
        Collections.sort(result);
        return result;
    }
  
    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     *
     * @param directory is the File object to validate as a directory.
     */
    static public void validateDirectory(File directory) throws FileNotFoundException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!directory.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + directory);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + directory);
        }
        if (!directory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: " + directory);
        }
    }

    
  /********************************************************************
   * 
   * @param dir File directory to search in
   * @param filename String to match (name only, not path)
   * @return first file found in dir that matches the string filename
   * regardless of case.
   * 
   * 
   * @throws java.io.FileNotFoundException if dir not valid
   * or doesn't contain the filename
   * @author jex 32.03.2009
   */  
    static public File FindFileIgnoreCase(File dir, String filename) throws FileNotFoundException         
  {
      String nextFile;
      int i;
      
       validateDirectory(dir);
       for(i=0;i<dir.listFiles().length;i++){
       nextFile = dir.listFiles()[i].getName();
       if(nextFile.equalsIgnoreCase(filename)) return dir.listFiles()[i];  
       }
      
       throw new FileNotFoundException(filename+" not found in " + dir);
    }
        
}

