/*
 * JarFileWrite.java
 *
 * Created on 09 December 2005, 20:07
 */

package bgs.geophys.library.File;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

/**
 * A class to:
 *  1.) Using an existing jar file 
 *    2.) Add new entries to the new file
 *    3.) Mark entries that are not to be copied
 *  3.) Close the files and copy the new file over the old
 * Final effect is to add/delete entries to/from an existing jar file
 * @author  Administrator
 */
public class JarFileWriter 
{
    
    // debugging code
    /*
    public static void main(String args[]) 
    {
        JarFileWriter writer;
        
        try
        {
            writer = new JarFileWriter ("C:\\Documents and Settings\\Administrator\\Desktop\\Tester.jar",
                                        "qqq");
            writer.finalize();
            writer = new JarFileWriter ("C:\\Documents and Settings\\Administrator\\Desktop\\Tester.jar",
                                        "c:\\Documents and Settings\\Administrator\\Desktop\\TesterCopy.jar");
            // writer.addDir ("\\newdir2\\");
            writer.removeEntry("\\newdir2\\testone");
            writer.removeEntry("\\newdir2\\testtwo");
            writer.removeEntry("\\newdir2\\odbcconf.log");
            writer.addEntry ("\\newdir2\\testone", "Test one".getBytes());
            writer.addEntry ("\\newdir2\\testtwo", "Test two".getBytes());
            writer.addEntry ("\\newdir2\\odbcconf.log", "c:\\odbcconf.log");
            writer.finaliseNewJarFile();
            writer.renameNewToOld();
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    */
    
    // codes for the type of data stored in new_entry_data
    private static final int DATA_DIR = 0;
    private static final int DATA_FILE = 1;
    private static final int DATA_BYTES = 2;
    
    private JarFile old_jar;                  // the old jar file
    private JarEntry old_jar_entries [];      // the entries enumerated from the old jar file
    private Vector<JarEntry> deleted_entries; // a list of entries to delete from the old jar
    
    private String new_jar_name;            // the new jar filename
    private Vector<JarEntry> new_entries;   // a list entries to add to the new jar
    private Vector<Object> new_entry_data;  // the data that will go with each entry - either
                                            // a byte array or a file OR null (to create a directory)
    private Vector<Integer> new_data_type;  // the data type of each entry - uses codes above
    
    private boolean new_jar_finalised;      // true when new file can no longer be alterred
    
    // the following variables are used by getEntry() and getEntryData() to
    // store parameters on the last entry returned to the user
    private int last_entry_index;
    private int old_jar_entry_index;
    private int new_jar_entry_index;

       
    /** Creates a new instance of JarFileWrite using a new jar file */
    public JarFileWriter(String new_jar_name) 
    {
        old_jar = null;
        old_jar_entries = new JarEntry [0];
        
        this.new_jar_name = new_jar_name;
        
        deleted_entries = new Vector<JarEntry> ();
        new_entries = new Vector<JarEntry> ();
        new_entry_data = new Vector<Object> ();
        new_data_type = new Vector<Integer> ();
        new_jar_finalised = false;
        
        last_entry_index = -1;
    }
    
    /** Creates a new instance of JarFileWrite using a new jar file */
    public JarFileWriter(File new_jar)
    {
        this (new_jar.getPath());
    }
    
    /** Creates a new instance of JarFileWrite using an existing jar file
     * @param old_jar_name name of the old file
     * @param new_jar_name name of the new file
     * @throws IOException if there was an IO error
     * @throws FileNotFoundException if there was an error opening a file
     * @throws ZipException if the old jar file does not exist */
    public JarFileWriter(String old_jar_name, String new_jar_name) 
    throws IOException, FileNotFoundException, ZipException
    {
        old_jar = new JarFile (old_jar_name);
        readJarFile ();
        
        this.new_jar_name = new_jar_name;
        
        deleted_entries = new Vector<JarEntry> ();
        new_entries = new Vector<JarEntry> ();
        new_entry_data = new Vector<Object> ();
        new_data_type = new Vector<Integer> ();
        new_jar_finalised = false;
        
        last_entry_index = -1;
    }
    
    /** Creates a new instance of JarFileWrite using an existing jar file
     * @param old_jar_name name of the old file
     * @param new_jar_name name of the new file
     * @throws IOException if there was an IO error
     * @throws FileNotFoundException if there was an error opening a file
     * @throws ZipException if the old jar file does not exist */
    public JarFileWriter(File old_jar_name, File new_jar_name) 
    throws IOException, FileNotFoundException, ZipException
    {
        this (old_jar_name.getPath(), new_jar_name.getPath());
    }
    
    /** Creates a new instance of JarFileWrite using an existing jar file
     * @param old_jar_name name of the old file
     * @param new_jar_name name of the new file
     * @throws IOException if there was an IO error
     * @throws FileNotFoundException if there was an error opening a file
     * @throws ZipException if the old jar file does not exist */
    public JarFileWriter(String old_jar_name, File new_jar_name) 
    throws IOException, FileNotFoundException, ZipException
    {
        this (old_jar_name, new_jar_name.getPath());
    }
    
    /** Creates a new instance of JarFileWrite using an existing jar file
     * @param old_jar_name name of the old file
     * @param new_jar_name name of the new file
     * @throws IOException if there was an IO error
     * @throws FileNotFoundException if there was an error opening a file
     * @throws ZipException if the old jar file does not exist */
    public JarFileWriter(File old_jar_name, String new_jar_name) 
    throws IOException, FileNotFoundException, ZipException
    {
        this (old_jar_name.getPath(), new_jar_name);
    }
    
    /** clear resources - if you don't call this, the jar file will be left open */
    public void finalize ()
    {
        try {super.finalize(); }
        catch (Throwable t) { }
        
        try { if (old_jar != null) old_jar.close (); }
        catch (IOException e) { }
        old_jar = null;
    }
    
    /** find an entry in the new file
     * @param name the name of the entry to find
     * @return its index or -1 if it does not exist */
    public int findEntry (String name)
    throws FileNotFoundException
    {
        int count, length;
        JarEntry entry;
        
        length = getNEntries();
        for(count=0; count<length; count++)
        {
            entry = getEntry(count);
            if (entry.getName().equals(name)) return count;
        }
        
        return -1;
    }
    
    /** delete an entry from the new file - only files may be deleted, not directories
     * @param name the name of the entry to delete
     * @throws FileNotFoundException if the file does not exist in the old jar file */
    public void removeEntry (String name)
    throws FileNotFoundException
    {
        int count;
        
        // check the new file is not already finalised
        if (new_jar_finalised) throw (new FileNotFoundException ("Jar file already finalised"));
        
        // check that the name hasn't been deleted already
        for (count=0; count<deleted_entries.size(); count++)
        {
            if ((deleted_entries.get(count)).getName().equals(name))
                return;
        }
        
        // find the entry
        for (count=0; count<old_jar_entries.length; count++)
        {
            if (old_jar_entries[count].getName().equals(name))
            {
                last_entry_index = -1;
                deleted_entries.add (old_jar_entries[count]);
                return;
            }
        }
        
        // the entry wasn't found
        throw (new FileNotFoundException (name + " not found in jar file"));        
    }
    
    /** add a directory to the new file
     * @param name the name of the directory
     * @throws FileNotFoundException if the new jar file is already finalised */
    public void addDir (String name)
    throws FileNotFoundException
    {
        String dir_name;
        
        if (new_jar_finalised) throw (new FileNotFoundException ("Jar file already finalised"));
        
        if (name.endsWith("\\")) dir_name = name;
        else dir_name = name + "\\";
        
        new_entries.add (new JarEntry (dir_name));
        new_entry_data.add (null);
        new_data_type.add (new Integer (DATA_DIR));
    }
    
    /** add an entry to the new file
     * @param name the name of the entry to add
     * @param file the data to add
     * @throws FileNotFoundException if the new jar file is already finalised */
    public void addEntry (String name, File file)
    throws FileNotFoundException
    {
        if (new_jar_finalised) throw (new FileNotFoundException ("Jar file already finalised"));
        new_entries.add (new JarEntry (name));
        new_entry_data.add ((Object) file);
        new_data_type.add (new Integer (DATA_FILE));
    }
    
    /** add an entry to the new file
     * @param name the name of the entry to add
     * @param filename the data to add
     * @throws FileNotFoundException if the new jar file is already finalised */
    public void addEntry (String name, String filename)
    throws FileNotFoundException
    {
        if (new_jar_finalised) throw (new FileNotFoundException ("Jar file already finalised"));
        new_entries.add (new JarEntry (name));
        new_entry_data.add ((Object) new File (filename));
        new_data_type.add (new Integer (DATA_FILE));
    }
    
    /** add an entry to the new file
     * @param name the name of the entry to add
     * @param data the data to add
     * @throws FileNotFoundException if the new jar file is already finalised */
    public void addEntry (String name, byte buffer [])
    throws FileNotFoundException
    {
        if (new_jar_finalised) throw (new FileNotFoundException ("Jar file already finalised"));
        new_entries.add (new JarEntry (name));
        new_entry_data.add ((Object) buffer);
        new_data_type.add (new Integer (DATA_BYTES));
    }
    
    /** get the number of entries in the jar file (this is the entries as they
     * will be written when finaliseJarFile() is called)
     * @return the number of entries */
    public int getNEntries ()
    {
        return old_jar_entries.length + new_entries.size () - deleted_entries.size();
    }

    /** get the entriy from the jar file (these are the entries as they
     * will be written when finaliseJarFile() is called)
     * @param index the index number of the entry to retrieve
     * @return the entry */
    public JarEntry getEntry (int index)
    throws ArrayIndexOutOfBoundsException
    {
        int count, count2;
        boolean deleted;

        // check for the same request as on the previous call
        if (index == last_entry_index)
        {
            if (last_entry_index < 0 || 
                (old_jar_entry_index < 0 && new_jar_entry_index < 0))
                throw (new ArrayIndexOutOfBoundsException ());
            
            if (old_jar_entry_index >= 0)
                return old_jar_entries [old_jar_entry_index];
            return new_entries.get(new_jar_entry_index);
        }
        last_entry_index = index;
        old_jar_entry_index = -1;
        new_jar_entry_index = -1;
        
        // check for a dumb index
        if (index < 0) throw (new ArrayIndexOutOfBoundsException ());
        
        // for each entry in the old file...
        for (count=0; count<old_jar_entries.length; count++)
        {
            // has it been deleted ??
            deleted = false;
            for (count2=0; count2<deleted_entries.size(); count2++)
            {
                if (old_jar_entries[count].getName().equals((deleted_entries.get(count2)).getName()))
                    deleted = true;
            }
            
            // if it wasn't deleted, decrement the index and check if it is the one we want
            if (! deleted)
            {
                index --;
                if (index < 0)
                {
                    old_jar_entry_index = count;
                    return old_jar_entries [count];
                }
            }
        }
        
        // check for index too large
        if (index >= new_entries.size()) throw (new ArrayIndexOutOfBoundsException ());
        
        // get the entry from the new entries list
        new_jar_entry_index = index;
        return new_entries.get (index);
    }
    
    /** get the data associated with an entry 
     * @param index the index number of the entry to retrieve
     * @return the entry data (null for a directory)
     * @throws IOException if there was an error reading the data
     * @throws FileNotFoundException if there was an error reading the data */
    public byte [] getEntryData (int index)
    throws ArrayIndexOutOfBoundsException, IOException, FileNotFoundException
    {
        byte data [];
        JarEntry entry;
        InputStream input_stream;
        FileInputStream file_input_stream;

        // call getEntry find the entry - also sets the index variables
        // old_jar_entry_index and new_jar_entry_index
        entry = getEntry (index);
        
        // retrieve the data
        data = null;
        if (old_jar_entry_index >= 0)
        {
            data = new byte [(int) entry.getSize()];
            input_stream = old_jar.getInputStream (old_jar_entries[old_jar_entry_index]);
            input_stream.read (data);
            input_stream.close();
        }
        else
        {
            switch ((new_data_type.get (new_jar_entry_index)).intValue())
            {
            case DATA_DIR:
                data = null;
                break;
            case DATA_FILE:
                data = new byte [(int) entry.getSize()];
                file_input_stream = new FileInputStream ((File) new_entry_data.get (new_jar_entry_index));
                file_input_stream.read (data);
                file_input_stream.close();
                break;
            case DATA_BYTES:
                data = (byte []) new_entry_data.get (new_jar_entry_index);
                break;
            }
        }
        
        return data;
    }
    
    /** check if an entry already exists
     * @param name the name to check for
     * @return true if the entry exists, false otherwise */
    public boolean exists (String name)
    {
        try
        {
            findEntry (name);
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
        return true;
    }
    
    /** finalise the new jar file - copy/delete/add all the entries, remove the
     * old file and write out the new file - after this no further additions or
     * deletions may be made
     * @throws IOException if there was an IO error
     * @throws FileNotFoundException if there was an error opening a file */
    public void finaliseNewJarFile ()
    throws IOException, FileNotFoundException
    {
        int count, count2, byte_count;
        byte buffer[], bytes[];
        boolean deleted;
        InputStream input_stream;
        JarOutputStream new_jar_output_stream;
        JarEntry entry;
        FileInputStream file_input_stream;
        
        buffer = new byte [1024];
        
        // open the new jar file
        new_jar_output_stream = new JarOutputStream (new FileOutputStream (new_jar_name));
        
        // for each entry in the old file...
        for (count=0; count<old_jar_entries.length; count++)
        {
            // has it been deleted ??
            deleted = false;
            for (count2=0; count2<deleted_entries.size(); count2++)
            {
                if (old_jar_entries[count].getName().equals((deleted_entries.get(count2)).getName()))
                    deleted = true;
            }
            
            // if it wasn't deleted, copy it to the new file
            if (! deleted)
            {
                input_stream = old_jar.getInputStream (old_jar_entries[count]);
                new_jar_output_stream.putNextEntry (old_jar_entries[count]);
                while ((byte_count = input_stream.read (buffer)) != -1) 
                {
                    new_jar_output_stream.write(buffer, 0, byte_count);
                }
                input_stream.close();
                input_stream = null;
            }
        }
        
        // for each of the new entries...
        for (count=0; count<new_entries.size(); count++)
        {
            // write the entry header
            new_jar_output_stream.putNextEntry(new_entries.get(count));
            
            // get the type of data
            switch ((new_data_type.get (count)).intValue())
            {
            case DATA_DIR:
                break;
            case DATA_FILE:
                file_input_stream = new FileInputStream ((File) new_entry_data.get (count));
                while ((byte_count = file_input_stream.read(buffer)) != -1) 
                {
                    new_jar_output_stream.write(buffer, 0, byte_count);
                }
                file_input_stream.close();
                break;
            case DATA_BYTES:
                new_jar_output_stream.write((byte []) new_entry_data.get (count));
                break;
            }
        }
        
        // close the new jar file
        new_jar_output_stream.close();
        
        // make the new jar file as finalised
        new_jar_finalised = true;
    }

    /** remove the old jar file, rename the new to the old
     * @throws IOException if there is an error
     * @throws FileNotFoundException if there is an error
     * @throws ZipException if there is an error */
    public void renameNewToOld ()
    throws IOException, FileNotFoundException, ZipException
    {
        File old_file, new_file;
         
        // check jar has been finalised
        if (! new_jar_finalised)
            throw (new FileNotFoundException ("Jar file not finalised"));
        
        // don't do the rename if we don't have an old file
        if (old_jar == null)
            old_jar = new JarFile (new_jar_name);
        else
        {
            // close the old file
            old_jar.close();
         
            // get file names
            old_file = new File (old_jar.getName());
            new_file = new File (new_jar_name);
         
            // delete old file and rename new to old
            if (! old_file.delete ())
                throw (new FileNotFoundException ("Unable to delete " + old_jar.getName()));
            if (! new_file.renameTo (old_file))
                throw (new FileNotFoundException ("Unable to rename " + new_jar_name));
        }
        
        // re-read the contents
        old_jar = new JarFile (old_jar.getName());
        readJarFile ();
         
        // reset the new entries
        deleted_entries = new Vector<JarEntry> ();
        new_entries = new Vector<JarEntry> ();
        new_entry_data = new Vector<Object> ();
        new_data_type = new Vector<Integer> ();
        new_jar_finalised = false;
        
        last_entry_index = -1;
    }
    
    /** test if an entry starts with a given pathname prefix 
     * @param entry the entry to test
     * @param pathname the pathname to search for
     * @returns true if entry starts with pathname */
    public static boolean isEntryStartsWithPathname (JarEntry entry, String pathname)
    {
        String entry_name, entry_copy, pathname_copy;
        
        // get the pathname of the jar entry
        entry_name = entry.getName();
        
        // get copies of both pathnames, forcing forward slash as path separator
        pathname_copy = pathname.replace('\\', '/');
        entry_copy = entry_name.replace('\\', '/');
        
        // compare the strings
        return entry_copy.startsWith(pathname_copy);
    }

    /** read the old jar file and extract its entries */
    private void readJarFile ()
    throws IOException, FileNotFoundException, ZipException
    {
        int count;
        Enumeration entries;
        
        entries = old_jar.entries();
        count = 0;
        while (entries.hasMoreElements())
        {
            entries.nextElement();
            count ++;
        }
        if (count == 0) old_jar_entries = null;
        else
        {
            old_jar_entries = new JarEntry [count];
            entries = old_jar.entries();
            for (count=0; count<old_jar_entries.length; count++)
                old_jar_entries[count] = (JarEntry) entries.nextElement();
        }
    }

}


