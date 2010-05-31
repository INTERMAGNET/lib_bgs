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
public class NameRealValuePair extends NameValuePair
{
    
    private double value;
    
    /** Creates a new instance of NameStringValuePair */
    public NameRealValuePair (String name, double value) 
    {
        this.name = name;
        this.value = value;
    }
    
    public double getValue () { return value; }
    
}
