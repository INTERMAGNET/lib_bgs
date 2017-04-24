/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.File;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A File that conforms to the ClasspathFileInterface, allowing files in the
 * file system or in the class path (and potentially in a JAR) to be used
 * in an identical manner
 * 
 * @author smf
 */
public class NormalFile extends File
implements ClasspathFileInterface
{
    public NormalFile (String pathname)
    {
        super (pathname);
    }
    public NormalFile (URI uri)
    {
        super (uri);
    }
    public NormalFile (NormalFile parent, String child)
    {
        super (parent, child);
    }
    public NormalFile (File parent, String child)
    {
        super (parent, child);
    }
    public NormalFile (String parent, String child)
    {
        super (parent, child);
    }

    @Override
    public InputStream openInputStream() throws IOException 
    {
        return new FileInputStream (this);
    }
}
