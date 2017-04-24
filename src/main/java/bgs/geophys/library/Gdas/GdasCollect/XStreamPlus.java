/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect;

import com.thoughtworks.xstream.XStream;
// import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.BaseException;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author smf
 */
public class XStreamPlus extends XStream
{

    /** code to make a class field */
    public static final int ACTION_MAKE_CLASS_FIELD = 1;
    /** code to make a field */
    public static final int ACTION_MAKE_FIELD = 2;
    /** code to make an atribute */
    public static final int ACTION_MAKE_ATTRIBUTE = 3;
    /** code to omit a member variable */
    public static final int ACTION_OMIT_MEMBER = 4;
    
    /** an alternative way to contstruct a XStreamPlus object that attempts
     * to find an appropriate DOM driver */
    public static XStreamPlus makeXstreamPlus ()
    {
        XStreamPlus xsp;
        
        try
        {
            // check for presence of XPP
            Class.forName("org.xmlpull.mxp1.MXParser");
            // if the default XPP3 driver is available this call will use it
            xsp = new XStreamPlus (new XppDriver ());
        }
        catch (ClassNotFoundException e)
        {
            // fall back to the driver that comes with XStream
            xsp = new XStreamPlus (new DomDriver ());
        }
        
        return xsp;
    }
    
    public XStreamPlus () { super (); }
    public XStreamPlus (HierarchicalStreamDriver h) { super (h); }
    public XStreamPlus (ReflectionProvider r) { super (r); }
    public XStreamPlus (ReflectionProvider r, HierarchicalStreamDriver h) { super (r, h); }
//    public XStreamPlus (ReflectionProvider r, ClassMapper c, HierarchicalStreamDriver h) { super (r, c, h); }
    public XStreamPlus (ReflectionProvider r, Mapper m, HierarchicalStreamDriver h) { super (r, m, h); }
//    public XStreamPlus (ReflectionProvider r, ClassMapper c, HierarchicalStreamDriver h, String s) { super (r, c, h, s); }
    
    /** control how a member variable is written to XML
     * @param class_obj the class containing the member
     * @param action one of the ACTION_.. codes
     * @param member_name the name of the member
     * @param alias_name the alias for the member in the XML
     */
    public void configField (Class class_obj, int action,
                             String member_name, String alias_name)
    {
        switch (action)
        {
        case ACTION_MAKE_CLASS_FIELD:
            alias (alias_name, class_obj);
            break;
        case ACTION_MAKE_FIELD:
            aliasField (alias_name, class_obj, member_name);
            break;
        case ACTION_MAKE_ATTRIBUTE:
            useAttributeFor (class_obj, member_name);
            aliasAttribute (alias_name, member_name);
            break;
        case ACTION_OMIT_MEMBER:
            omitField (class_obj, member_name);
            break;
        }
    }
    
    /** read the XML data from file
     * @param obj the object to read in to
     * @param filename the name of the file to read
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed
     * @throws IOException if there was an IO error
     * @throws XMLException if there was an error reading the XML */
    public void XMLFromFile (Object obj, String filename)
    throws FileNotFoundException, IOException, XMLException
    {
        FileReader reader;
        
        reader = new FileReader (filename);
        try { fromXML (reader, obj); }
        catch (BaseException e) { throw new XMLException (e); }
        reader.close();
    }
    
    /** read the XML data from file
     * @param obj the object to read in to
     * @param file the file to read
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed
     * @throws IOException if there was an IO error
     * @throws XMLException if there was an error reading the XML */
    public void XMLFromFile (Object obj, File file)
    throws FileNotFoundException, IOException, XMLException
    {
        FileReader reader;
        
        reader = new FileReader (file);
        try { fromXML (reader, obj); }
        catch (BaseException e) { throw new XMLException (e); }
        reader.close();
    }
    
    /** write the XML data to file
     * @param obj the object to write
     * @param filename the name of the file to write
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed
     * @throws IOException if there was an IO error
     * @throws XMLException if there was an error creating the XML */
    public void XMLToFile (Object obj, String filename)
    throws FileNotFoundException, IOException, XMLException
    {
        FileWriter writer;
        
        writer = new FileWriter (filename);
        try { toXML (obj, writer); }
        catch (BaseException e) { throw new XMLException (e); }
        writer.close();
    }
    
    /** write the XML data to file
     * @param obj the object to write
     * @param file the file to write
     * @throws FileNotFoundException if the configuration file could not be found, or the base data directory could not be accessed
     * @throws IOException if there was an IO error
     * @throws XMLException if there was an error creating the XML */
    public void XMLToFile (Object obj, File file)
    throws FileNotFoundException, IOException, XMLException
    {
        FileWriter writer;
        
        writer = new FileWriter (file);
        try { toXML (obj, writer); }
        catch (BaseException e) { throw new XMLException (e); }
        writer.close();
    }
    
}
