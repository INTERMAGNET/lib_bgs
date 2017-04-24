/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.File;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * A class to load a file and make it available in various text formats
 * 
 * @author smf
 */
public class FileToText 
{
    
    private File file;
    private String native_sep;
    private Vector<String> lines;
    
    public FileToText (String filename)
    throws FileNotFoundException, IOException
    {
        this.file = new File (filename);
        loadFile ();
    }
    
    public FileToText (File file)
    throws FileNotFoundException, IOException
    {
        this.file = file;
        loadFile ();
    }
    
    public File getFile () { return file; }
    public String getFilename () { return file.getAbsolutePath(); }
    
    public String getText () { return getText (native_sep); }

    public String [] getLines () 
    { 
        String [] ret_val;
        
        ret_val = new String [lines.size()];
        lines.toArray(ret_val);
        return ret_val;
    }
    
    public String getText (String sep)
    {
        int count;
        String ret_val;

        ret_val = "";
        for (count=0; count<lines.size(); count++)
            ret_val += lines.get(count) + sep;
        return ret_val;
    }
    
    
    private void loadFile ()
    throws FileNotFoundException, IOException
    {
        BufferedReader input;
        String line;
        
        native_sep = System.getProperty("line.separator");
        lines = new Vector<String> ();
        
        input =  new BufferedReader (new FileReader (file));
        while ((line = input.readLine()) != null) lines.add(line);
        input.close();
    }
}
