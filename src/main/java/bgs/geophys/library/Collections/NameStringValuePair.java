/*
 * NameStringValuePair.java
 *
 * Created on 03 February 2008, 17:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Collections;

/**
 *
 * @author smf
 */
public class NameStringValuePair extends NameValuePair
{
    
    private String value;
    
    /** Creates a new instance of NameStringValuePair */
    public NameStringValuePair (String name, String value) 
    {
        this.name = name;
        this.value = value;
    }
    
    public String getValue () { return value; }
    
}
