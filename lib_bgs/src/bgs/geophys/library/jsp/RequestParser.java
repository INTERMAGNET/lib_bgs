/*
 * RequestParser.java
 *
 * Created on 21 November 2006, 13:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.jsp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Class for parsing a plain or multi-part URL query string, providing methods for returning
 * parameter values, which are independent of the case of the parameter name.
 * Also deals with (some) URL escape codes
 * @author smf
 */
public class RequestParser
{
    /** inner class that holds an individual name/value pair from
     * the request - the value can hold a plain form field or a file item */
    private class NameValue
    {
        private String name;
        private String form_value;
        private FileItem file_item;
        public NameValue (String name, String value)
        {
            this.name = name;
            this.form_value = value;
            this.file_item = null;
        }
        public NameValue (String name, FileItem file_item)
        {
            this.name = name;
            if (file_item.isFormField())
            {
                this.form_value = file_item.getString();
                this.file_item = null;
            }
            else
            {
                this.form_value = null;
                this.file_item = file_item;
            }
        }
        public String getName () { return name; }
        public String getValue () { return form_value; }
        public FileItem getFileItem () { return file_item; }
        public boolean isFormField () { if (form_value != null) return true; return false; }
    }
    
    // list of the name/value pairs found in the request
    List<NameValue> item_list;
    
    /** Creates a new instance of RequestParser that can handle file uploads
     * @param servletRequest the request from POST or GET
     * @param maxFileUploadSize maximum size for files that are uploaded, in bytes
     * @param temporaryDir temporary directory for file upload - should not
     *        be used for anything else
     * @throws FileUploadException if the request could not be parsed */
    public RequestParser(HttpServletRequest servletRequest, int maxFileUploadSize,
                         File temporaryDir)
    throws FileUploadException
    {
        int count;
        Enumeration en;
        String name, value, values[];
        FileItemFactory file_item_factory;
        ServletFileUpload file_upload;
        List file_items;
        FileItem file_item;
        Iterator itr;
        
        // create an empty item list
        item_list = new ArrayList<NameValue> ();
        
        // is this a multipart request ??
        if (ServletFileUpload.isMultipartContent(servletRequest))
        {
            // yes - process using Apache library
            // Create a file_item_factory for disk-based file items
             file_item_factory = new DiskFileItemFactory (DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD,
                                                         temporaryDir);

            // Create a new file upload handler - fix maximum size of files
            file_upload = new ServletFileUpload(file_item_factory);
            file_upload.setFileSizeMax (maxFileUploadSize);
            file_items = file_upload.parseRequest (servletRequest);
            itr = file_items.iterator();
            while(itr.hasNext()) 
            {
                // add items to the list
                file_item = (FileItem)itr.next();
                item_list.add (new NameValue (file_item.getFieldName(), file_item));
            }
        }
        else
        {
            // not a multipart form - this code works for both POST and GET transfers
            en = servletRequest.getParameterNames();
            while (en.hasMoreElements())
            {
                name = (String) en.nextElement();
                values = servletRequest.getParameterValues (name);
                if (values != null)
                {
                    for (count=0; count<values.length; count++)
                    {
                        try { value = URLDecoder.decode (values[count], "UTF-8"); }
                        catch (UnsupportedEncodingException e) { value = values[count] ; }
                        item_list.add (new NameValue (name, value));
                    }                
                }
            }
        }
    }
    
    /** Gets the value of the given form field
     * @param name The (case-insensitive) form field name
     * @return the field value
     * @throws ParseException if the value could not be found */
    public String getValue(String name)
    throws ParseException
    {
        String value;
        
        value = getValue (name, null);
        if (value == null) throw new ParseException ("Can't find parameter: " + name, 0);
        return value;
    }
    
    /** Gets the value of the given form field
     * @param name The (case-insensitive) form field name
     * @param def Default value to use if the form field has not been provided
     * @return the field value */
    public String getValue(String name, String def)
    {
        return getValue (name, def, 0);
    }
    
    /** Gets the value of the given form field
     * @param name The (case-insensitive) form field name
     * @param def Default value to use if the form field has not been provided
     * @param repeatCount the index of the form field for fields with duplicate
     *        names (0 = first occurence, 1 = second, ...)
     * @return the field value */
    public String getValue(String name, String def, int repeatCount)
    {
        NameValue name_value;
        
        name_value = findNameValue (name, repeatCount);
        if (name_value == null) return def;
        if (name_value.getValue() == null) return def;
        return name_value.getValue();
    }
    
    /** Gets an upload file item
     * @param name The (case-insensitive) file field name
     * @return the file item OR null */
    public FileItem getUploadFile (String name)
    {
        NameValue name_value;
        
        name_value = findNameValue (name, 0);
        if (name_value == null) return null;
        return name_value.getFileItem();
    }
    
    /** Gets the number of occurences of the given field name (which may
     * be a form field or a file upload)
     * @param name The (case-insensitive) name
     * @return the count (0 = parameter does not exist)
     */
    public int getRepeatCount (String name)
    {
        int repeatCount;
        NameValue name_value;
        Iterator itr;
        
        repeatCount = 0;
        itr = item_list.iterator();
        while (itr.hasNext())
        {
            name_value = (NameValue) itr.next();
            if (name_value.getName().equalsIgnoreCase(name))
                repeatCount ++;
        }
        return repeatCount;
    }
    
    /** Gets the given field
     * @param name The (case-insensitive) name
     * @param repeatCount the index of the field for fields with duplicate
     *        names (0 = first occurence, 1 = second, ...)
     * @return the field value OR null */
    private NameValue findNameValue (String name, int repeatCount)
    {
        NameValue name_value;
        Iterator itr;
        
        itr = item_list.iterator();
        while (itr.hasNext())
        {
            name_value = (NameValue) itr.next();
            if (name_value.getName().equalsIgnoreCase(name))
            {
                repeatCount --;
                if (repeatCount <= -1) return name_value;
            }
        }
        return null;
    }
    
    /** find the base URL from a given absolute URL, e.g. for
     *     http://localhost:8084/GIN/GINServices?formay=html
     *  return
     *     http://localhost:8084/GIN
     * @param request the http servlet request object
     * @return the base URL */
    public static String getBaseURL (HttpServletRequest request)
    {
        int index;
        String urlString, filePart;
        URL url, baseUrl;
        
        // get the URL
        urlString = new String (request.getRequestURL());
        try
        {
            url = new URL (urlString);
        }
        catch (MalformedURLException e)
        {
            return urlString;
        }
        
        // find the URL path and remove the top element
        filePart = url.getPath();
        index = filePart.lastIndexOf("/");
        if (index >= 0)
            filePart = filePart.substring(0, index);
        try
        {
            baseUrl = new URL (url.getProtocol(), url.getHost(), url.getPort(), filePart);
        }
        catch (MalformedURLException e)
        {
            return urlString;
        }
        
        return baseUrl.toExternalForm();
    }
    
}
