/*
 * EditableCookie.java
 *
 * Created on 28 December 2006, 10:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.jsp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class that takes a bean, allows for editing the bean via JSP and makes
 * the bean persistant as a cookie.
 *
 * @author smf
 */
public class EditableCookie
{
    
    /** internal class that hold details on each editable field in the bean */
    private class EditableField
    {
        public EditableField (Method getMethod, Method setMethod,
                              String prePrompt, String baseName, int dataType,
                              String postPrompt, boolean newTable, String tableName,
                              boolean newRow, boolean newColumn)
        {
            this.getMethod = getMethod;
            this.setMethod = setMethod;
            this.prePrompt = prePrompt;
            this.baseName = baseName;
            this.dataType = dataType;
            this.postPrompt = postPrompt;
            this.newTable = newTable;
            this.tableName = tableName;
            this.newRow = newRow;
            this.newColumn = newColumn;
            this.group = null;
        }
        public Method getMethod;    // bean's get method
        public Method setMethod;    // bean's set method
        public String prePrompt;    // boiler plate text before field
        public String baseName;     // name of the field
        public int dataType;        // one of the DATA_TYPE_... fields
        public String postPrompt;   // boiler plate text after field
        public String tableName;    // boiler plate text used if newTable is true
        public boolean newTable;    // true to start this field in a new table
        public boolean newRow;      // true to start this field in a new row
        public boolean newColumn;   // true to start this field in a new column
        public String group;        // only valid for Boolean values - grouping turns
                                    // booleans from check box items to radio buttons
    }

    // hidden code for unknown data type 
    private static final int DATA_TYPE_UNKNOWN = -1;
    /** code for data types supported: String */
    public static final int DATA_TYPE_STRING = 1;
    /** code for data types supported: Double */
    public static final int DATA_TYPE_DOUBLE = 2;
    /** code for data types supported: Float */
    public static final int DATA_TYPE_FLOAT = 3;
    /** code for data types supported: Integer */
    public static final int DATA_TYPE_INTEGER = 4;
    /** code for data types supported: Long */
    public static final int DATA_TYPE_LONG = 5;
    /** code for data types supported: Boolean */
    public static final int DATA_TYPE_BOOLEAN = 6;

    // the bean that we are going to edit
    private Object bean; 
    // an array of the editable fields in the bean
    private Collection<EditableField> editableFields;
    // name of the cookie used for storing the bean
    private String cookieName;
    // the title for the form editor
    private String formTitle;
    // arrays of hidden fields - names and values
    private Vector<String> hiddenNameList, hiddenValueList;
    
    /** Creates a new instance of EditableCookie 
     * @param bean the bean to edit */
    public EditableCookie(Object bean) 
    {
        super ();
        
        int count, count2, dataType;
        String getName, setName, baseName;
        Method method_list [], getMethod, setMethod;
        Class parameterTypes [], returnType;
        EditableField editableField;
        Iterator iterator;

        this.bean = bean;
        this.editableFields = new ArrayList<EditableField> ();
        this.cookieName = "EditableCookie" + bean.getClass().getSimpleName();
        this.formTitle = "Editor for " + bean.getClass().getSimpleName();
        this.hiddenNameList = new Vector<String> ();
        this.hiddenValueList = new Vector<String> ();
        
        // get the methods from the bean
        method_list = bean.getClass().getMethods();
        
        // iterate over the methods
        for (count=0; count<method_list.length; count++)
        {
            getMethod = method_list[count];
            setMethod = null;
            getName = getMethod.getName();
            baseName = setName = null;
            parameterTypes = null;
            returnType = null;
            dataType = DATA_TYPE_UNKNOWN;
            
            // find methods with names starting "get..."
            if (getName.startsWith("get") && getName.length() > 3)
            {
                // check for a corresponding "set..." method
                baseName = getName.substring(3);
                setName = "set" + baseName;
                for (count2=0; count2<method_list.length; count2++)
                {
                    if (method_list[count2].getName().equals(setName))
                    {
                        setMethod = method_list[count2];
                        break;
                    }
                }
            }
                
            // check the return type from get is the same as the set parameter
            if (setMethod != null)
            {
                returnType = getMethod.getReturnType();
                parameterTypes = setMethod.getParameterTypes();
                if (parameterTypes.length != 1) setMethod = null;
                else if (! returnType.equals (parameterTypes[0])) setMethod = null;
            }
            
            // is the data type supported ??
            if (setMethod != null)
            {
                if (returnType.getName().equals("java.lang.String"))
                    dataType = DATA_TYPE_STRING;
                else if (returnType.getName().equals("java.lang.Double"))
                    dataType = DATA_TYPE_DOUBLE;
                else if (returnType.getName().equals("java.lang.Float"))
                    dataType = DATA_TYPE_FLOAT;
                else if (returnType.getName().equals("java.lang.Integer"))
                    dataType = DATA_TYPE_INTEGER;
                else if (returnType.getName().equals("java.lang.Long"))
                    dataType = DATA_TYPE_LONG;
                else if (returnType.getName().equals("java.lang.Boolean"))
                    dataType = DATA_TYPE_BOOLEAN;
                else
                    setMethod = null;
            }
            
            // when you get here, if setMethod is non-null we have a bean parameter
            if (setMethod != null)
            {
                editableFields.add (new EditableField (getMethod,
                                                       setMethod,
                                                       baseName + ": ",
                                                       baseName, 
                                                       dataType,
                                                       "",
                                                       false,
                                                       null,
                                                       false,
                                                       false));
            }
        }
    }
    
    /** load the bean from its associated cookie 
     * @param request the HttpServletRequest that contains the cookies that
     *        may be used to load the bean */
    public void loadBeanFromCookie (HttpServletRequest request)
    {
        int count;
        Cookie cookies [];
        Iterator iterator;
        
        // try to find a cookie for this bean from the servlet request object
        cookies = request.getCookies();
        if (cookies != null)
        {
            for (count=0; count<cookies.length; count++)
            {
                if (cookies[count].getName().equals(cookieName))
                {
                    // try to load the bean from the cookie - the cookie holds
                    // the values in the same form as an http GET request
                    loadBeanFromQueryString (cookies[count].getValue());
                    break;
                }
            }
        }
    }
    
    /** save the bean to a cookie 
     * @param request the HttpServletRequest that contains the cookies that
     *        may be used to load the bean
     * @param response the servlet response object that will send the cookie
     *        back to the client */
    public void saveBeanToCookie (HttpServletRequest request, HttpServletResponse response)
    {
        String cookieValue, fieldValue, encodedFieldValue;
        EditableField editableField;
        Iterator iterator;
        Cookie cookie;

        // create the cookie from the bean - the cookie holds the bean's
        // values in the same form as an http GET request
        cookieValue = "";
        for (iterator = editableFields.iterator(); iterator.hasNext(); )
        {
            editableField = (EditableField) iterator.next();
            try
            {
                fieldValue = getFieldValue (editableField);
            }
            catch (IllegalAccessException e) { fieldValue = ""; }
            catch (InvocationTargetException e) { fieldValue = ""; }
            try
            {
                encodedFieldValue = URLEncoder.encode(fieldValue, "UTF-8");
            }
            catch (UnsupportedEncodingException e) { encodedFieldValue = fieldValue; }
            if (cookieValue.length() > 0) cookieValue += "&";
            cookieValue += editableField.baseName + "=" + encodedFieldValue;
        }
        
        cookie = new Cookie (cookieName, cookieValue);
        cookie.setMaxAge(365 * 86400);
        cookie.setPath(request.getServletPath());        
        response.addCookie(cookie);
    }
        
    /** create an html form that the user can edit the bean values with 
     * @param response the servlet response object
     * @param submitURL the URL to submit the form to
     * @throws IOException if there was an error writing to the client */
    public void showHtmlEditorForm (HttpServletResponse response,
                                    String submitURL)
    throws IOException
    {
        int count;
        boolean first;
        Iterator iterator;
        EditableField editableField;
        String value, checked;
        PrintWriter writer;

        response.setContentType("text/html");
        
        writer = new PrintWriter(response.getOutputStream(), true);
        writer.println ("<html><head><title>" +
                        formTitle +
                        "</title></head><body><h1>" +
                        formTitle +
                        "</h1>");
        
        writer.println ("<form method=\"get\" " +
                              "action=\"" + submitURL + "\" " +
                              "name=\"" + bean.getClass().getSimpleName() + "Editor\">");
        for (iterator = editableFields.iterator(), first = true; iterator.hasNext(); first = false)
        {
            editableField = (EditableField) iterator.next();
            try { value = getFieldValue (editableField); }
            catch (IllegalAccessException e)    { value = ""; }
            catch (InvocationTargetException e) { value = ""; }
            
            if ((! first) && editableField.newTable) writer.print ("</table>");
            if (editableField.tableName != null) writer.print ("<p><b><u>" + editableField.tableName + "</u></b>");
            if (first) writer.print ("<table><tr><td>");
            else if (editableField.newTable) writer.print ("<table><tr><td>");
            else if (editableField.newRow) writer.print ("<tr><td>");
            else if (editableField.newColumn) writer.print ("<td>");
            
            writer.print (editableField.prePrompt);
            if (editableField.dataType == DATA_TYPE_BOOLEAN)
            {
                if (value.equalsIgnoreCase("true")) checked = "checked";
                else checked = "";
                if (editableField.group == null)
                    writer.print ("<input type=\"checkbox\" " +
                                         "name=\"" + editableField.baseName + "\" " + 
                                         "value=\"" + editableField.baseName + "\" " + 
                                          checked + "/>");
                else
                    writer.print ("<input type=\"radio\" " +
                                         "name=\"" + editableField.group + "\" " +
                                         "value=\"" + editableField.baseName + "\" " + 
                                          checked + "/>");
            }
            else
            {
                writer.print ("<input type=\"text\" " +
                                     "name=\"" + editableField.baseName + "\" " +
                                     "value=\"" + value + "\"/>");
            }
            writer.print (editableField.postPrompt);
            writer.println ("");
        }
        writer.println ("</table><table><tr><td>");
        writer.println ("<input type=\"submit\" name=\"SubmitButton\" value=\"OK\"/>");
        writer.println ("<input type=\"submit\" name=\"SubmitButton\" value=\"Cancel\"/>");        
        writer.println ("</table>");
        for (count=0; count<hiddenNameList.size(); count++)
        {
            writer.println ("<input type=\"hidden\" " +
                                    "name=\"" + (String) hiddenNameList.get (count) + "\"" +
                                    "value=\"" + (String) hiddenValueList.get (count) + "\"/>");
        }
        writer.println ("</form>");

        writer.println ("</body></html>");
    }
    
    /** retrieveFormResults
     * @param request the servlet request object 
     * @return true if bean was updated, false if user cancelled */
    public boolean retrieveFormResults (HttpServletRequest request)
    {
        String buttonValue;
        
        buttonValue = request.getParameter ("SubmitButton");
        if (buttonValue == null) buttonValue = "Cancel";
        if (buttonValue.equals("OK"))
        {
            loadBeanFromQueryString (request.getQueryString());
            return true;
        }
        return false;
    }

    /** set the form title */
    public void setTitle (String formTitle)
    {
        this.formTitle = formTitle;
    }
    
    /** set the text displayed before a field
     * @param baseName the field name
     * @param prePrompt the text to display
     * @return true if the field name exists, false otherwise */
    public boolean setPrePrompt (String baseName, String prePrompt)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        editableField.prePrompt = prePrompt;
        return true;
    }
    
    /** set the text displayed after a field
     * @param baseName the field name
     * @param postPrompt the text to display
     * @return true if the field name exists, false otherwise */
    public boolean setPostPrompt (String baseName, String postPrompt)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        editableField.postPrompt = postPrompt;
        return true;
    }
    
    /** set the flag to force a new table starting on the given field
     * @param baseName the field name
     * @param newTable true for a new table
     * @param tableName optional boiler plate text for table name (null for no text)
     * @return true if the field name exists, false otherwise */
    public boolean setNewTable (String baseName, boolean newTable, String tableName)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        editableField.newTable = newTable;
        editableField.tableName = tableName;
        return true;
    }
    
    /** set the flag to force a new row starting on the given field
     * @param baseName the field name
     * @param newColumn true for a new row
     * @return true if the field name exists, false otherwise */
    public boolean setNewRow (String baseName, boolean newRow)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        editableField.newRow = newRow;
        return true;
    }
    
    /** set the flag to force a new column starting on the given field
     * @param baseName the field name
     * @param newColumn true for a new row
     * @return true if the field name exists, false otherwise */
    public boolean setNewColumn (String baseName, boolean newColumn)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        editableField.newColumn = newColumn;
        return true;
    }
    
    /** set the group for a Boolean field - turns it from a check box to a radio button
     * @param baseName the field name
     * @param groupthe group name
     * @return true if the field name exists and is Boolean, false otherwise */
    public boolean setGroup (String baseName, String group)
    {
        EditableField editableField;
        
        editableField = findField (baseName);
        if (editableField == null) return false;
        if (editableField.dataType != DATA_TYPE_BOOLEAN) return false;
        editableField.group = group;
        return true;
    }
    
    /** add a hidden field to the editor form 
     * @param name the name of the field
     * @param value the value of the field */
    public void addHiddenField (String name, String value)
    {
        hiddenNameList.add (name);
        hiddenValueList.add (value);
    }
    
    // find the an editable field from its name
    private EditableField findField (String baseName)
    {
        Iterator iterator;
        EditableField editableField;
        
        for (iterator = editableFields.iterator(); iterator.hasNext(); )
        {
            editableField = (EditableField) iterator.next();
            if (editableField.baseName.equals(baseName)) return editableField;
        }
        return null;
    }

    /** get the contents of a field in the bean - for details on invocation via reflection see
     ** http://java.sun.com/docs/books/tutorial/reflect/object/invoke.html
     * @param editableField the field to get
     * @return the value as a string
     * @throws IllegalAccessException if there was a fault invoking the set... method
     * @throws InvocationTargetException if there was a fault invoking the set... method */
    private String getFieldValue(EditableField editableField)
    throws IllegalAccessException, InvocationTargetException
    {
        Object[] arguments;
        Object method_ret_val;
        String ret_val;
        
        arguments = new Object [0];
        method_ret_val = editableField.getMethod.invoke (bean, arguments);
        switch (editableField.dataType)
        {
            case DATA_TYPE_STRING:
                ret_val = (String) method_ret_val;
                break;
            case DATA_TYPE_DOUBLE:
                ret_val = ((Double) method_ret_val).toString();
                break;
            case DATA_TYPE_FLOAT:
                ret_val = ((Float) method_ret_val).toString();
                break;
            case DATA_TYPE_INTEGER:
                ret_val = ((Integer) method_ret_val).toString();
                break;
            case DATA_TYPE_LONG:
                ret_val = ((Long) method_ret_val).toString();
                break;
            case DATA_TYPE_BOOLEAN:
                ret_val = ((Boolean) method_ret_val).toString();
                break;
            default:
                ret_val = "";
        }
        return ret_val;
    }
    
    /** set the contents of a field in the bean - for details on invocation via reflection see
     ** http://java.sun.com/docs/books/tutorial/reflect/object/invoke.html
     * @param editableField the field to set
     * @param string the new value (null to ignore)
     * @throws NumberFormatException if the string should represent a number, but the format is wrong
     * @throws IllegalAccessException if there was a fault invoking the set... method
     * @throws InvocationTargetException if there was a fault invoking the set... method */
    private void setFieldValue(EditableField editableField, String string) 
    throws NumberFormatException, IllegalAccessException, InvocationTargetException
    {
        Double double_arg;
        Float float_arg;
        Integer int_arg;
        Long long_arg;
        Boolean boolean_arg;
        Object[] arguments;
        
        switch (editableField.dataType)
        {
            case DATA_TYPE_STRING:
                arguments = new Object[] {string};
                break;
            case DATA_TYPE_DOUBLE:
                double_arg = new Double (string);
                arguments = new Object [] {double_arg};
                break;
            case DATA_TYPE_FLOAT:
                float_arg = new Float (string);
                arguments = new Object [] {float_arg};
                break;
            case DATA_TYPE_INTEGER:
                int_arg = new Integer (string);
                arguments = new Object [] {int_arg};
                break;
            case DATA_TYPE_LONG:
                long_arg = new Long (string);
                arguments = new Object [] {long_arg};
                break;
            case DATA_TYPE_BOOLEAN:
                boolean_arg = new Boolean (string);
                arguments = new Object [] {boolean_arg};
                break;
            default:
                arguments = null;
        }
        if (arguments != null) editableField.setMethod.invoke (bean, arguments);
    }

    private void loadBeanFromQueryString (String queryString)
    {
        Iterator iterator;
        EditableField editableField;
        int count;
        String individualPair [], names [], values [];
        StringTokenizer kvpTokens;

        // parse the query string into names and values
        if (queryString == null) queryString = "";
        kvpTokens = new StringTokenizer (queryString, "&");
        names = new String [kvpTokens.countTokens()];
        values = new String [kvpTokens.countTokens()];
        for (count=0; kvpTokens.hasMoreTokens(); count ++)
        {
            individualPair = kvpTokens.nextToken().split("=");
            switch (individualPair.length)
            {
                case 0:
                    names[count]="";
                    values[count]="";
                    break;
                case 1:
                    names[count]=individualPair[0];
                    values[count]="";
                    break;
                default:
                    names[count]=individualPair[0];
                    try { values[count]=URLDecoder.decode(individualPair[1], "UTF-8"); }
                    catch (UnsupportedEncodingException e) { values[count] = individualPair[1] ; }
                    break;
            }
        }
        
        // load the bean fields
        for (iterator = editableFields.iterator(); iterator.hasNext(); ) 
        {
            editableField = (EditableField) iterator.next();
            for (count=0; count<names.length; count++)
            {
                if (names[count].equalsIgnoreCase(editableField.baseName))
                {
                    try { setFieldValue (editableField, values [count]); }
                    catch (Exception e) { }
                    break;
                }
            }
        }
    }
}
