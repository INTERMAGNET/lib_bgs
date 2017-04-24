/*
 * OptionList.java
 *
 * Created on 06 February 2008, 18:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Collections;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 *
 * @author smf
 */
public class OptionList 
{
    
    // the array of options
    private ArrayList<Option> optionsArray;
    // the name of the file where the options are stored
    private String filename;
    
    /** Creates a new empty instance of OptionList
     * @param filename name of the file to read (and later save) options to/from */
    public OptionList (String filename)
    {
        this.filename = filename;
        optionsArray = new ArrayList<Option> ();
    }
    
    /** read options from options file */
    public void loadOptions ()
    throws IOException
    {
        String line;
        BufferedReader reader;
        Option option;
        
        reader = new BufferedReader (new FileReader (filename));
        while ((line = reader.readLine()) != null)
        {
            option = new Option (line);
            optionsArray.add (option);
        }
        reader.close();
    }
    
    /** write options to options file */
    public void saveOptions ()
    throws IOException
    {
        PrintWriter writer;
        Iterator<Option> itr;
        
        writer = new PrintWriter (filename);
        for (itr=optionsArray.iterator(); itr.hasNext(); )
            writer.println (itr.next().toCodedForm());
        writer.close();
    }

    public int getIntValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return Integer.MAX_VALUE;
        return opt.getIntValue();
    }
    
    public long getLongValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return Long.MAX_VALUE;
        return opt.getLongValue();
    }

    public float getFloatValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return Float.MAX_VALUE;
        return opt.getFloatValue();
    }

    public double getDoubleValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return Double.MAX_VALUE;
        return opt.getDoubleValue();
    }

    public boolean getBooleanValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return false;
        return opt.getBooleanValue();
    }

    public String getStringValue (String name)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return null;
        return opt.getStringValue();
    }

    public boolean setValue (String name, int value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    public boolean setValue (String name, long value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    public boolean setValue (String name, float value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    public boolean setValue (String name, double value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    public boolean setValue (String name, boolean value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    public boolean setValue (String name, String value)
    {
        Option opt;
        opt = findOption (name);
        if (opt != null) return opt.setValue (value);
        optionsArray.add (new Option (name, value));
        return true;
    }
    
    private Option findOption (String name)
    {
        Iterator<Option> it;
        Option opt;
        
        for (it = optionsArray.iterator(); it.hasNext(); )
        {
            opt = it.next();
            if (opt.isNamed(name)) return opt;
        }
        return null;
    }
}
