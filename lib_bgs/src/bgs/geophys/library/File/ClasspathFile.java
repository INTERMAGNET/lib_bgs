/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.File;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A class that makes available a number of methods from the File class
 * but works on files that are referenced as resources on the classpath
 * and so work both in folder structures of class as well as in jar files
 * 
 * @author smf
 */
public class ClasspathFile 
implements ClasspathFileInterface
{
    private Class reference_class;
    private String resource_name;
    private URL reference;
    
    /** construct a reference to a resource using an absolute class path */
    public ClasspathFile (String resource_name)
    {
        this.reference_class = this.getClass();
        this.resource_name = resource_name;
        this.reference = reference_class.getResource(resource_name);
    }
    
    /** construct a reference to a resource using a reference relative to a class in the class path */
    public ClasspathFile (Class reference_class, String resource_name)
    {
        this.reference_class = reference_class;
        this.resource_name = resource_name;
        this.reference = reference_class.getResource(resource_name);
    }

    /** construct a reference to a resource relative to an existing reference */
    public ClasspathFile (ClasspathFile parent, String child) 
    {
        this.reference_class = parent.reference_class;
        this.resource_name = parent.resource_name + "/" + child;
        this.reference = reference_class.getResource(resource_name);
    }
    
    @Override
    public boolean exists ()
    {
        if (reference == null) return false;
        return true;
    }
    
    public URL getURL ()
    {
        return reference;
    }
    
    @Override
    public String getName ()
    {
        return reference.getFile();
    }
    
    @Override
    public String getAbsolutePath ()
    {
        return reference.toString();
    }

    @Override
    public InputStream openInputStream ()
    throws IOException
    {
        return reference.openStream();
    }
    
    @Override
    public String toString ()
    {
        return reference.toString();
    }
    
}
