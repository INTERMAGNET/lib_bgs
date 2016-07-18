/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.File;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class to create a zip file and put entries into it
 * 
 * @author smf
 */
public class ZipFileMaker 
{

    private static final int BUFFER_SIZE = 2048;
    
    private FileOutputStream file_out;
    private ZipOutputStream zip_out;
    
    /** create a new zip file */
    public ZipFileMaker (File zip_file) 
    throws FileNotFoundException
    {
        file_out = new FileOutputStream (zip_file);
        zip_out = new ZipOutputStream (new BufferedOutputStream (file_out));    
    }

    /** create a new zip file */
    public ZipFileMaker (String zip_filename) 
    throws FileNotFoundException
    {
        file_out = new FileOutputStream (zip_filename);
        zip_out = new ZipOutputStream (new BufferedOutputStream (file_out));    
    }

    /** create a new zip file */
    public ZipFileMaker (OutputStream out)
    {
        file_out = null;
        zip_out = new ZipOutputStream (new BufferedOutputStream (out));    
    }
    
    /** close the zip file (after all entries have been added */
    public void close () 
    throws IOException
    {
        zip_out.close();
    }

    /** add an entry to the zip file */
    public void add (File file) 
    throws FileNotFoundException, IOException
    {
        add (file, file.getPath());
    }
    
    /** add an entry to the zip file */
    public void add (File file, String entry_name) 
    throws FileNotFoundException, IOException
    {
        FileInputStream in;
        
        in = new FileInputStream (file);
        add (in, entry_name);
    }
    
    /** add an entry to the zip file */
    public void add (String filename) 
    throws FileNotFoundException, IOException
    {
        add (new File (filename));
    }    
    
    /** add an entry to the zip file */
    public void add (String filename, String entry_name) 
    throws FileNotFoundException, IOException
    {
        add (new File (filename), entry_name);
    }    
    
    /** add an entry to the zip file */
    public void add (InputStream in, String name) 
    throws IOException
    {
        int count;
        byte data [];
        BufferedInputStream file_in;
        ZipEntry entry;
        
        data = new byte [BUFFER_SIZE];
        file_in = new BufferedInputStream (in, BUFFER_SIZE);
        entry = new ZipEntry (name);
        zip_out.putNextEntry (entry);
        while ((count = file_in.read (data, 0, BUFFER_SIZE)) != -1)
            zip_out.write (data, 0, count);
        file_in.close();
    }
    
}
