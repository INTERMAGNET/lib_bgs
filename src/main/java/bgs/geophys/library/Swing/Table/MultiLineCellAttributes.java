/*
 * MultiLineCellAttributes.java
 *
 * Created on 20 July 2006, 18:18
 */

package bgs.geophys.library.Swing.Table;

import java.awt.*;

/**
 * class that implements the attributes that the cell may have
 * @author  Administrator
 */
public class MultiLineCellAttributes 
{
    
    private Color unselected_foreground_colour;
    private Color unselected_background_colour;
    private Color selected_foreground_colour;
    private Color selected_background_colour;
    private Font font;

    /** create a set of cell attributes - any of the values may be
     * null, in which case default values are used */
    public MultiLineCellAttributes (Color unselected_foreground_colour, 
                                    Color unselected_background_colour,
                                    Color selected_foreground_colour, 
                                    Color selected_background_colour,
                                    Font font)
    {
        this.unselected_foreground_colour = unselected_foreground_colour;
        this.unselected_background_colour = unselected_background_colour;
        this.selected_foreground_colour = selected_foreground_colour;
        this.selected_background_colour = selected_background_colour;
        this.font = font;
    }
        
    public Color getUnselectedForegroundColor () { return unselected_foreground_colour; }
    public Color getUnselectedBackgroundColor () { return unselected_background_colour; }
    public Color getSelectedForegroundColor () { return selected_foreground_colour; }
    public Color getSelectedBackgroundColor () { return selected_background_colour; }
    public Font getFont () { return font; }
    
}
