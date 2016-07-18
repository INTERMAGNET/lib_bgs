/*
 * jarInterfaceLoader.java
 *
 * Created on 18 July 2004, 21:24
 */

package bgs.geophys.library.misc;

import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

/**
 *
 * @author  Ies
 */
public class jarInterfaceLoader
{
    
    /** Creates a new instance of jarInterfaceLoader */
    public jarInterfaceLoader()
    {
        
    }
    
    public static void loadInterfaces(File jar, Class interfaceToLoad)
    throws IOException, MalformedURLException, ClassNotFoundException
    {
        String name = jar.getName();
        String suffix = "";
        if (name.length() > 4)
        {
            suffix = name.substring(name.length() - 3).toLowerCase();
        }
        if (suffix.equals("jar"))
        {
            JarFile jarFile = new JarFile(jar);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()});
                            
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if (!entry.isDirectory())
                {
                    if (entry.getName().length() > 6)
                    {
                        if (entry.getName().substring(entry.getName().length() - 5).toLowerCase().equals("class"))
                        {
                            String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                            Class newClass = classLoader.loadClass(className);
                            Class[] interfaces = newClass.getInterfaces();
                            for (int j = 0; j < interfaces.length; j++)
                            {
                                if (interfaces[j].equals(interfaceToLoad))
                                {
                                    try
                                    {
                                        newClass.newInstance();
                                    }
                                    catch (Exception e)
                                    {
                                                    
                                    }
                                }
                            }
                        }
                    }
                }   
            }
        }                        
    }
    
}
