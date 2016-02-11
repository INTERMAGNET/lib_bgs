/*
 * Option.java
 *
 * Created on 06 February 2008, 23:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Collections;

import java.io.IOException;

/**
 *
 * @author smf
 */
public class Option 
{
    
    // NOTE - option values must be single digit, otheriwse the codedForm
    //        that the options are stored in will not work properly
    /** code for an option of type int */
    public static final int OPTION_TYPE_INT = 1;
    /** code for an option of type long */
    public static final int OPTION_TYPE_LONG = 2;
    /** code for an option of type float */
    public static final int OPTION_TYPE_FLOAT = 3;
    /** code for an option of type double */
    public static final int OPTION_TYPE_DOUBLE = 4;
    /** code for an option of type boolean */
    public static final int OPTION_TYPE_BOOLEAN = 5;
    /** code for an option of type string */
    public static final int OPTION_TYPE_STRING = 6;
    
    private int type;
    private String name;
    
    private int value_int;
    private long value_long;
    private float value_float;
    private double value_double;
    private boolean value_boolean;
    private String value_string;
        
    public Option (String name, int value)     
    {
        this.name = checkName (name);
        type = OPTION_TYPE_INT;     
        value_int = value; 
    }

    public Option (String name, long value)    
    {
        this.name = checkName (name);
        type = OPTION_TYPE_LONG;    
        value_long = value; 
    }
    
    public Option (String name, float value)   
    {
        this.name = checkName (name);
        type = OPTION_TYPE_FLOAT;
        value_float = value; 
    }
    
    public Option (String name, double value)  
    { 
        this.name = checkName (name);
        type = OPTION_TYPE_DOUBLE;
        value_double = value;
    }
    
    public Option (String name, boolean value) 
    {
        this.name = checkName (name);
        type = OPTION_TYPE_BOOLEAN; 
        value_boolean = value; 
    }
    
    public Option (String name, String value)  
    { 
        this.name = checkName (name);
        type = OPTION_TYPE_STRING;
        value_string = value; 
    }
    
    public Option (String codedForm)
    throws IOException
    {
        int separator;
        String value;

        if (codedForm.length() < 4)
            throw new IOException ("Coded option below minimum length");
        separator = codedForm.indexOf ('=');
        if (separator <= 1 || separator >= (codedForm.length() -1))
            throw new IOException ("Badly formatted option");
        try { type = Integer.parseInt(codedForm.substring(0, 1)); }
        catch (NumberFormatException e) { throw new IOException ("Bad type number in coded option"); }
        name = checkName (codedForm.substring (1, separator));
        value = codedForm.substring (separator +1);
        switch (type)
        {
        case OPTION_TYPE_INT:     value_int = Integer.parseInt (value);         break;
        case OPTION_TYPE_LONG:    value_long = Long.parseLong (value);          break;
        case OPTION_TYPE_FLOAT:   value_float = Float.parseFloat (value);       break;
        case OPTION_TYPE_DOUBLE:  value_double = Double.parseDouble (value);    break;
        case OPTION_TYPE_BOOLEAN: value_boolean = Boolean.parseBoolean (value); break;
        case OPTION_TYPE_STRING:  value_string = value;                         break;
        default: throw new IOException ("Type number invalid in coded option");
        }
    }

    /** check if the given name matches this name - this method is needed
     * because the name may be mangled on instantiation to protect the
     * codedForm from corruption (we don't want names with '=' signs in) */
    public boolean isNamed (String testName)
    {
        return name.equalsIgnoreCase(checkName(testName));
    }
    
    public int getType () { return type; }
    
    public int getIntValue ()         
    {
        if (type != OPTION_TYPE_INT) return Integer.MAX_VALUE; 
        return value_int; 
    }
    
    public long getLongValue ()       
    {
        if (type != OPTION_TYPE_LONG) return Long.MAX_VALUE;    
        return value_long; 
    }
    
    public float getFloatValue () 
    {
        if (type != OPTION_TYPE_FLOAT) return Float.MAX_VALUE;   
        return value_float; 
    }
    
    public double getDoubleValue ()   
    {
        if (type != OPTION_TYPE_DOUBLE) return Double.MAX_VALUE;  
        return value_double; 
    }
    
    public boolean getBooleanValue ()
    {
        if (type != OPTION_TYPE_BOOLEAN) return false;
        return value_boolean; 
    }
    
    public String getStringValue ()   
    {
        if (type != OPTION_TYPE_STRING) return null;  
        return value_string; 
    }
    
    public boolean setValue (int value)     
    {
        if (type != OPTION_TYPE_INT) return false; 
        value_int = value;
        return true; 
    }
    
    public boolean setValue (long value)    
    { 
        if (type != OPTION_TYPE_LONG) return false; 
        value_long = value;    
        return true; 
    }
    
    public boolean setValue (float value)   
    {
        if (type != OPTION_TYPE_FLOAT) return false; 
        value_float = value;   
        return true; 
    }
    
    public boolean setValue (double value)  
    {
        if (type != OPTION_TYPE_DOUBLE) return false; 
        value_double = value;  
        return true; 
    }
    
    public boolean setValue (boolean value) 
    {
        if (type != OPTION_TYPE_BOOLEAN) return false; 
        value_boolean = value; 
        return true; 
    }
    
    public boolean setValue (String value)  
    {
        if (type != OPTION_TYPE_STRING) return false; 
        value_string = value;  
        return true; 
    }

    public String toCodedForm ()
    {
        String codedForm;
        
        codedForm = Integer.toString (type) + name + "=";
        switch (type)
        {
        case OPTION_TYPE_INT:     codedForm += Integer.toString (value_int);     break;
        case OPTION_TYPE_LONG:    codedForm += Long.toString (value_long);       break;
        case OPTION_TYPE_FLOAT:   codedForm += Float.toString (value_float);     break;
        case OPTION_TYPE_DOUBLE:  codedForm += Double.toString (value_double);   break;
        case OPTION_TYPE_BOOLEAN: codedForm += Boolean.toString (value_boolean); break;
        case OPTION_TYPE_STRING:  codedForm += value_string;                     break;
        }
        return codedForm;
    }

    /** names must not include an '=' sign, otherwise codedForm will break */
    private String checkName (String name) { return name.replace('=', '_'); }
    
}
