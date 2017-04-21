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
public class NameIntegerValuePair extends NameValuePair
{
    
    private int value;
    
    /** Creates a new instance of NameStringValuePair */
    public NameIntegerValuePair (String name, int value) 
    {
        this.name = name;
        this.value = value;
    }
    
    public int getValue () { return value; }
    
}
