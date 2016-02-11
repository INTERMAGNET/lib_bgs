/*
 * MultiLineCellContents.java
 *
 * Created on 20 July 2006, 17:46
 */

package bgs.geophys.library.Swing.Table;

import java.awt.*;

/**
 * A class that allows a cell to have attributes (e.g. foreground, background color).
 * Used in conjunction with MultiLineCellRenderer.
 *
 * @author  Administrator
 */
public class MultiLineCellContents 
{
    // private members
    private MultiLineCellAttributes attributes;
    private String value;
    
    /** Creates a new instance of MultiLineCellContents */
    public MultiLineCellContents (Object value, MultiLineCellAttributes attributes) 
    {
        if (value == null) this.value = "";
        else this.value = value.toString();
        this.attributes = attributes;
    }
    
    /** override toString to display the value */
    public String toString () { return value; }
    
    /** get the attributes */
    public MultiLineCellAttributes getCellAttibutes () { return attributes; }
    
}
