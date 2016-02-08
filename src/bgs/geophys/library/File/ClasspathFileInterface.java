/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.File;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface to allow programs to read from normal files or files
 * in the classpath without having to know which data source the
 * file comes from
 * 
 * @author smf
 */
public interface ClasspathFileInterface 
{
    
    public boolean exists ();
    public String getName ();
    public String getAbsolutePath ();
    public InputStream openInputStream () throws IOException;
    
}
