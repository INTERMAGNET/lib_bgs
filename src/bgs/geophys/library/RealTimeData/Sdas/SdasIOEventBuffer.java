/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.RealTimeData.Sdas;

import java.util.Vector;

/**
 * A class to hold a buffer of IO events, mainly for debugging
 *
 * @author smf
 */
public class SdasIOEventBuffer 
{

    // the IO event buffer is held with element 0 containing the most recent event */
    private Vector<SdasIOEvent> io_event_buffer;
    private int buffer_len;
    
    public SdasIOEventBuffer (int buffer_len)
    {
        io_event_buffer = new Vector<SdasIOEvent> ();
        this.buffer_len = buffer_len;
    }
    
    public void setEventBufferLength (int buffer_len)
    {
        this.buffer_len = buffer_len;
        trimBuffer ();
    }
    
    public void addEvent (SdasIOEvent event)
    {
        io_event_buffer.add(0, event);
        trimBuffer ();
    }
    
    public int getNEvents () { return io_event_buffer.size(); }
    public SdasIOEvent getEvent (int index) { return io_event_buffer.elementAt(index); }
    
    private void trimBuffer ()
    {
        while (io_event_buffer.size() > buffer_len)
            io_event_buffer.remove(buffer_len);
    }
}
