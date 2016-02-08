/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.File;

import java.io.File;
import java.net.URL;

/**
 * Create objects conforming to the ClasspathFileInterface - these files
 * can be read whether they are on the standard file system, in the classpth
 * or in a jar
 * 
 * @author smf
 */
public class ClasspathFileFactory 
{

    private File base_folder;
    private Class base_reference;
    private String base_ref_rel_path;

    /** create files from this base folder
     * 
     * @param base_folder the folder that the file names will be tagged to to create an absolute path
     */
    public ClasspathFileFactory (File base_folder)
    {
        this.base_folder = base_folder;
        this.base_reference = null;
        this.base_ref_rel_path = null;
    }
    
    /** create files from this base URL
     * 
     * @param base_reference the reference point in the class hierarchy that file names will be tagged to to create a resource
     */
    public ClasspathFileFactory (Class base_reference)
    {
        this.base_folder = null;
        this.base_reference = base_reference;
        this.base_ref_rel_path = null;
    }

    /** create files from this base URL
     * 
     * @param base_reference the reference point in the class hierarchy that file names will be tagged to to create a resource
     */
    public ClasspathFileFactory (Class base_reference, String base_ref_rel_path)
    {
        this.base_folder = null;
        this.base_reference = base_reference;
        this.base_ref_rel_path = base_ref_rel_path;
    }

    /** get an object that can be opened for reading
     * 
     * @param name name of the file (referenced to the base_folder or base_reference)
     * @return the ClasspathFileInterface object
     */
    public ClasspathFileInterface getClasspathFile (String name)
    {
        if (base_folder != null) return new NormalFile (base_folder, name);
        if (base_ref_rel_path == null) return new ClasspathFile (base_reference, name);;
        return new ClasspathFile (new ClasspathFile (base_reference, base_ref_rel_path), name);
    }
    
    
}
