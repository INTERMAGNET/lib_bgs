/*
 * CursorStack.java
 *
 * Created on 28 June 2007, 16:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing;

import java.awt.Component;
import java.awt.Cursor;
import java.util.Stack;

/**
 *
 * @author smf
 */
public class CursorStack 
{
    
    private Stack<Cursor> cursor_stack = new Stack<Cursor> ();
    private Component component;
    
    /** Creates a new instance of CursorStack
     * @param component the component the cursors will be stacked on */
    public CursorStack (Component component) 
    {
        this.component = component;
    }
    
    /** display a new cursor, pushing the current one onto the stack
     * @param new_cursor one of the predfined Cursor codes */
    public void pushCursor (int new_cursor)
    {
        cursor_stack.push (component.getCursor());
        component.setCursor (Cursor.getPredefinedCursor(new_cursor));
    }
    
    /** redisplay the previous cursor */
    public void popCursor ()
    {
        component.setCursor (cursor_stack.pop());
    }
}
