/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.RealTimeData.Sdas;

/**
 * A class to hold an IO event, mainly for debugging
 * 
 * @author smf
 */
public class SdasIOEvent 
{

    public enum EventType {OPEN, READ, WRITE, CLOSE, EXCEPTION}
    
    private EventType event_type;
    private Exception exception;
    private byte line [];
    private int line_length;
    
    /** create an OPEN or CLOSE event */
    public SdasIOEvent (boolean open)
    {
        if (open) event_type = EventType.OPEN;
        else event_type = EventType.CLOSE;
        exception = null;
        line = null;
        line_length = -1;
    }
    
    /** create a read event */
    public SdasIOEvent ()
    {
        event_type = EventType.READ;
        exception = null;
        line = new byte [10];
        line_length = 0;
    }
    
    /** create a write event */
    public SdasIOEvent (String line)
    {
        event_type = EventType.WRITE;
        exception = null;
        this.line = line.getBytes();
        line_length = line.length();
    }
    
    /** create an exception event */
    public SdasIOEvent (Exception e)
    {
        event_type = EventType.EXCEPTION;
        exception = e;
        line = null;
        line_length = -1;
    }
    
    /** add data to a read/write line */
    public void addToLine (String s)
    {
        int count;
        byte bytes [];
        
        bytes = s.getBytes();
        for (count=0; count<bytes.length; count++)
            addToLine (bytes[count]);
    }
    
    /** add data to a read/write line */
    public void addToLine (byte b)
    {
        byte new_line [];

        if (line != null)
        {
            if ((line.length -1) == line_length)
            {
                new_line = new byte [line.length + 10];
                System.arraycopy(line, 0, new_line, 0, line_length);
                line = new_line;
            }
            line [line_length ++] = b;
        }
    }
    
    /** display this event */
    @Override
    public String toString ()
    {
        switch (event_type)
        {
            case OPEN: return "Open";
            case CLOSE: return "Close";
            case READ: return "Read: " + decodeLine ();
            case WRITE: return "Write: " + decodeLine ();
            case EXCEPTION: return "Exception: " + exception.toString();
        }
        return "Unknown contents";
    }
    
    public String decodeLine ()
    {
        int count;
        StringBuffer buffer;
        
        buffer = new StringBuffer ();
        for (count=0; count<line_length; count++)
        {
            switch (line [count])
            {
                case '\r': buffer.append ("\\r"); break;
                case '\n': buffer.append ("\\n"); break;
                case '\t': buffer.append ("\\t"); break;
                default:
                    if (line[count] < ' ' || line[count] > '~')
                        buffer.append ("\\" + Integer.toHexString(line[count]));
                    else
                        buffer.append (new String (line, count, 1));
            }
        }
        return buffer.toString();
    }
}
