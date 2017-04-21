/*
 * SystemList.java
 *
 * Created on 03 May 2004, 22:35
 */

package bgs.geophys.library.DataLogger.SystemList;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import bgs.geophys.library.File.*;

/**
 *
 * @author  Administrator
 */
public class SystemList implements org.xml.sax.ErrorHandler
{
    
    private Vector<SectionList> sections;        // an array of list sections
    private String last_error;      // last error message - to help debug exceptions
    private Document xml_document;

    /** Creates a new instance of SystemList */
    public SystemList() 
    {
        // show that nothing is loaded
        sections = null;
        last_error = null;
    }        
        
    /** copy a SystemList into this one
     * @param copy_from the object to copy from */
    public void copy (SystemList copy_from)
    {
        this.sections = copy_from.sections;
        this.last_error = copy_from.last_error;
    }

    /** Attempt to download the latest list of systems. 
     * If a list was previously loaded it is lost.
     * @param update_address the URL to download the list from
     * @return true if the list was updated */
    public boolean update (URL update_address)
    {
        boolean ret_val;
        
        try { ret_val = read (update_address.openStream ()); }
        catch (Exception e) { ret_val = false; }
        
        return ret_val;
    }

    /** Attempt to save the list to a local file.
     * @return true if the list was loaded OK */
    public boolean writeLocal ()
    {
        boolean ret_val;
        String system_list_filename;
        FileOutputStream output_stream;
        
        output_stream = null;
        ret_val = true;
        try
        {
            system_list_filename = FileNameConstants.getConfigFileName(FileNameConstants.DATALOGGER_PACKAGE_NAME, 
                                                                       FileNameConstants.DATALOGGER_SYSTEM_LIST_FILENAME);        
            output_stream = new FileOutputStream (new File (system_list_filename));
            write (output_stream, null);
        }
        catch (Exception e)
        {
            last_error = e.toString();
            ret_val = false;
        }

        try { if (output_stream != null) output_stream.close(); } catch (Exception e2) { }
        
        return ret_val;
    }
    
    /** Attempt to load the list from a local file. If a list was previously
     * loaded it is lost.
     * @return true if the list was loaded OK */
    public boolean readLocal ()
    {
        boolean ret_val;
        String system_list_filename;
        
        system_list_filename = FileNameConstants.getConfigFileName(FileNameConstants.DATALOGGER_PACKAGE_NAME, 
                                                                   FileNameConstants.DATALOGGER_SYSTEM_LIST_FILENAME);        
        try 
        { 
            ret_val = read (new FileInputStream (new File (system_list_filename)));
        }
        catch (Exception e) 
        {
            ret_val = false; 
        }
        
        return ret_val;
    }

    /** find out whether the list is loaded
     * @return true if the list is loaded */
    public boolean isLoaded ()
    {
        if (sections != null) return true;
        return false;
    }

     /** find the number of sections
     * @return the number of sections */
    public int getNSections () 
    { 
        if (sections == null) return 0;
        return sections.size (); 
    }
    
    /** find the name of a section
     * @param section_index the section index (0 .. n_sections -1)
     * @return section name */
    public String getSectionName (int section_index) 
    {
        SectionList section;
        
        section = (SectionList) sections.get (section_index);
        return section.getName ();
    }
    
    /** find the number of systems in a section
     * @param section_index the section index (0 .. n_sections -1)
     * @return section name */
    public int getNSystems (int section_index) 
    {
        SectionList section;
        
        section = (SectionList) sections.get (section_index);
        return section.getNSystems();
    }

    /** find a specific system
     * @param section_index the section index (0 .. n_sections -1)
     * @param system_index the system index (0 .. n_systems -1)
     * @return the system */
    public SystemDetails getSystemDetails (int section_index, int system_index) 
    {
        SectionList section;
        
        section = (SectionList) sections.get (section_index);
        return (SystemDetails) section.getSystemDetails (system_index);
    }
    
    /** get the message associated with the last error */
    public String getLastErrorMessage () { return last_error; }
    
    /** write data from this object to an output stream 
     * @param output_stream the stream to write to
     * @param dtd_name the name of the DTD for this document (or null for no DTD) */
    private boolean write (OutputStream output_stream, String dtd_name)
    {
        try
        {
            DOMSource dom_source = new DOMSource(xml_document);
            StreamResult stream_result = new StreamResult (output_stream);
            TransformerFactory tf = TransformerFactory.newInstance ();
            Transformer serializer = tf.newTransformer ();
            serializer.setOutputProperty (OutputKeys.ENCODING, "ISO-8859-1");
            if (dtd_name != null) serializer.setOutputProperty (OutputKeys.DOCTYPE_SYSTEM, dtd_name);
            serializer.setOutputProperty (OutputKeys.INDENT, "yes");
            serializer.transform (dom_source, stream_result);         
        }
        catch (Exception e) { processException(e); return false; }
        
        return true;
    }
    
    /** read data into this object from an input stream
     * @param input_stream the stream to read from
     * @return true if the data was read OK */
    private boolean read (InputStream input_stream)
    {
        boolean ret_val;
        DocumentBuilderFactory db_factory;
        DocumentBuilder db;
        Node list_node, section_node;
        SectionList section_list;
        
        try
        {
            // load the XML file
            db_factory = DocumentBuilderFactory.newInstance();
            db_factory.setValidating (false);
            db_factory.setCoalescing (true);
            db_factory.setIgnoringComments (true);
            db_factory.setIgnoringElementContentWhitespace (true);
            db = db_factory.newDocumentBuilder();
            db.setErrorHandler (this);
            xml_document = db.parse(input_stream);

            // parse the XML file
            sections = new Vector<SectionList> ();
            for (list_node=xml_document.getFirstChild(); list_node != null; list_node=list_node.getNextSibling()) 
            {
                if (list_node.getNodeName().equalsIgnoreCase("system-list"))
                {
                    for (section_node=list_node.getFirstChild(); section_node != null; section_node=section_node.getNextSibling()) 
                    {
                        if (section_node.getNodeName().equalsIgnoreCase("section"))
                        {
                            section_list = new SectionList(section_node);
                            sections.add(section_list);
                        }
                    }
                }
            }
        }
        catch (SystemListException e) { processException (e); return false; }
        catch (ParserConfigurationException e) { processException (e); return false; }
        catch (SAXException e) { processException (e); return false; }
        catch (IOException e) { processException (e); return false; }
     
        return true;
    }
    
    // implementation of SAX exception handling interface
    public void error(SAXParseException exception)      throws SAXException { throw exception; }
    public void fatalError(SAXParseException exception) throws SAXException { throw exception; }
    public void warning(SAXParseException exception)    throws SAXException { throw exception; }

    // what to do with exceptions
    private void processException (Exception e)
    {
        last_error = e.getMessage();
        if (last_error == null) last_error = "Unknwon error";
    }
    
    //////////////////////////////////////// debugging code below this line ////////////////////////////////////////////////
    private void dump ()
    {
        int count, count2, count3, count4;
        SectionList section;
        SystemDetails system;
        ChannelGroupDetails channel_group;
        ChannelGroupDetails.ChannelDetails channel_details;
        
        try
        {
            for (count=0; count<sections.size(); count++)
            {
                section = (SectionList) sections.get (count);
                System.out.println ("Section: " + section.getName ());
                for (count2=0; count2<section.getNSystems(); count2++)
                {
                    system = section.getSystemDetails(count2);
                    System.out.println (" System: " + system.getDisplayName());
                    System.out.println ("  Address: " + system.getIpAddress());
                    System.out.println ("  IP port: " + system.getDataIpPort());
                    System.out.println ("  Type: " + system.getProtocolName());
                    System.out.println ("  SSH server: " + system.getSshServer());
                    for (count3=0; count3<system.getNChannelGroups(); count3++)
                    {
                        channel_group = system.getChannelGroupDetails(count3);
                        System.out.println ("  Channel group: " + channel_group.getName());
                        for (count4=0; count4<channel_group.getNChannels(); count4++)
                        {
                            channel_details = channel_group.getChannelDetails(count4);
                            System.out.println ("   Channel: " + Integer.toString (count4));
                            System.out.println ("    Index: " + Integer.toString (channel_details.channel_no));
                            System.out.println ("    Ident: " + channel_details.channel_ident);
                        }
                    }
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    /** extract the systems list from a document
     * @params start_node pass the document in through this parameter
     *         casting it to type Node
     * @params dump_level set this to 0 - it is used for recursion
     */
    private void dumpNode (Node start_node, int dump_level)
    {
        int count;
        Node node;
        NamedNodeMap node_map;
        String node_value;
        
        // work through the nodes
        for (node = start_node; node != null; node = node.getNextSibling())
        {
            // get the node value (if any)
            try { node_value = node.getNodeValue(); }
            catch (DOMException e) { node_value = null; }

            // dump the node
            System.out.print (dump_level);
            System.out.print (" " + node.getNodeName());
            if (node_value != null) System.out.print (": " + node_value);
            System.out.println ("");
            
            // dump its attributes
            node_map = node.getAttributes();
            if (node_map != null)
            for (count=0; count<node_map.getLength(); count++)
                dumpNode (node_map.item(count), -dump_level);
            
            // call this routine recursively for child nodes
            if (node.hasChildNodes()) dumpNode (node.getFirstChild(), dump_level +1);
        }
        
    }
    
    
}
