/*
 * ThreadStreamReader.java
 *
 * Created on 23 September 2003, 11:24
 */

package bgs.geophys.library.Threads;

import java.util.*;
import java.io.*;

/**
 * This class allows you to read a stream in another thread. This
 * is useful if you are using runtime.exec() - you can use another
 * thread to read stdout or stderr, while the mian thread feeds
 * stdin and waits for completion. This should get around problems
 * associated with limited size pipes.
 *
 * To use this class:
 *  call runtime.exec()
 *  create an instance of this class, using process.getOutStream() as the construction parameter
 *  interact with the process, wait for it to finish
 *  call IsReadingStopped or WaitForReadingToStop
 *  call GetData to retrieve the data
 *
 * @author  smf
 */
public class ThreadStreamReader extends Thread
{
    
    private InputStream input_stream;
    private byte data [];
    private boolean reading_stopped;
    private IOException stored_io_exception;

    /** Creates a new instance of ThreadStreamReader
      * @param input_stream the stream to read from */
    public ThreadStreamReader (InputStream input_stream)
    {
        this.input_stream = input_stream;
        data = null;
        reading_stopped = false;
        stored_io_exception = null;
    }

    /** run the stream reader in a new thread */
    public void run ()
    {
        int length, offset, count;
        byte buffer [], old_data [];
        DataInputStream data_input_stream;

        // open the stream for unbuffered reading
        data_input_stream = new DataInputStream (input_stream);
        
        // read until requested to stop (or exception)
        buffer = new byte [2048];
        try
        {
            do
            {
                // attempt to read a chunk
                length = data_input_stream.read (buffer);
                if (length > 0)
                {
                    // add the chunk to the main data buffer
                    if (data == null)
                    {
                        offset = 0;
                        old_data = null;
                        data = new byte [length];
                    }
                    else
                    {
                        offset = data.length;
                        old_data = data;
                        data = new byte [offset + length];
                    }
                    for (count=0; count<offset; count++) data [count] = old_data [count];
                    for (count=0; count<length; count++) data [count + offset] = buffer [count];
                }
                
                // if we found nothing, then wait a while (to save CPU)
                try { if (length == 0) sleep (10); }
                catch (InterruptedException e) { }
            } while (length >= 0);
        }
        catch (IOException e) { stored_io_exception = e; }
        
        // flag to callers that data is ready for collection
        reading_stopped = true;

    }

    /** tell the reader that its work is done */
    public void WaitForReadingToStop () 
    {
        while (! IsReadingStopped ()) { }
    }
    
    /** check whether reading has stopped
      @returns true if reading has stopped */
    public boolean IsReadingStopped ()
    {
        return reading_stopped;
    }
    
    /** get the data read from the stream
     * @param wait if true then wait for reading to stop
     * @returns the data (may be null)
     * @throws IOException if there was an error reading the stream */
    public byte [] GetData (boolean wait)
    throws IOException
    { 
        if (wait) WaitForReadingToStop ();
        if (stored_io_exception != null) throw stored_io_exception;
        if (! reading_stopped) return null;
        return data; 
    }
    
}


