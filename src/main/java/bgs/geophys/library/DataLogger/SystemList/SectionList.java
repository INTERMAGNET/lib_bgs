/*
 * SectionList.java
 *
 * Created on 31 May 2004, 14:57
 */

package bgs.geophys.library.DataLogger.SystemList;

import java.util.*;
import org.w3c.dom.*;

/**
 * Holds details of a group of systems
 * @author  Administrator
 */
public class SectionList 
{

    /** the name of the section */
    private String name;

    /** the list of SystemDetails objects */
    private Vector<SystemDetails> list;

    /** construct a new section list
     * @param name the name for the section */
    public SectionList (String name) 
    {
        this.name = name;
        list = new Vector<SystemDetails>();
    }
    
    /** construct a new section from a document
     * @params section_node the node containing the section list
     * @throws SystemListException if there is a parsing error */
    public SectionList (Node section_node)
    throws SystemListException
    {
        NamedNodeMap node_map;
        Node node;

        // check that this node is a system
        if (! section_node.getNodeName().equalsIgnoreCase("section"))
            throw new SystemListException ("Not a section list");
        
        // get the section name (if any)
        name = "No name";
        node_map = section_node.getAttributes();
        if (node_map != null)
        {
            node = node_map.getNamedItem ("name");
            if (node != null)
            {
                try { name = node.getNodeValue(); }
                catch (DOMException e) { }
            }
        }
        
        // walk through the children, extracting systems
        list = new Vector<SystemDetails>();
        for (node=section_node.getFirstChild(); node != null; node=node.getNextSibling())
        {
            if (node.getNodeName().equalsIgnoreCase("system"))
            {
                list.add(new SystemDetails (node));
            }
        }
    }

    /** Getter for property name.
     * @return Value of property name. */
    public String getName() { return name;  }

    /** add a system to the list
     * @param system the system to add */
    public void addSystem (SystemDetails system) { list.add(system); }
    
    /** get the number of systems in this section
     * @return the number of systems */
    public int getNSystems () { return list.size(); }
    
    /** get a system from the list
     * @param index the number of the system (0..N-1)
     * @return the system details (may be null) */
    public SystemDetails getSystemDetails (int index)
    {
        if (index < 0 || index >= list.size()) return null;
        return (SystemDetails) list.get (index);
    }

}

