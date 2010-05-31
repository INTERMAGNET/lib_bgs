/*
 * ThreadMultiStreamReader.java
 *
 * Created on 24 November 2003, 14:05
 */

package bgs.geophys.library.Threads;

import java.util.*;
import java.io.*;

/**
 * This class allows you to read multiple streams and either keep or
 * discard the data. This is useful, for example, to read the 
 * output/error streams from an external process.
 *
 * @author  smf
 */
public class ThreadMultiStreamReader extends Thread 
{
    
    /** an inner class to hold details on each stream being monitored */
    private class StreamDetails
    {
        private DataInputStream stream;  // the stream to read from
        private boolean discard;         // true to discard data, false to store
        private byte data [];            // the data read from the stream
        private Object user_data;        // user info associated with this stream
        private boolean closed;
        
        /** construct a new StreamDetails 
         * @param stream the stream to read from
         * @param discard true to discard data read, false to keep it
         * @param user_data user info associated with this stream */
        public StreamDetails (InputStream stream, boolean discard, Object user_data)
        {
            this.stream = new DataInputStream (stream);
            this.discard = discard;
            data = new byte [0];
            this.user_data = user_data;
            closed = false;
        }
        
        /** read from this stream
         * @return the number of bytes read OR -1 for EOF*/
        public int readData ()
        {
            int length, offset, count;
            byte buffer [], old_data[];

            try
            {
                if (closed) length = 0;
                else
                {
                    length = stream.available();
                    if (length > 0)
                    {
                        if (length > 2048) length = 2048;
                        buffer = new byte [length];

                        length = stream.read (buffer);
                        if ((length > 0) && (! discard))
                        {
                            offset = data.length;
                            old_data = data;
                            data = new byte [offset + length];
                            for (count=0; count<offset; count++) data [count] = old_data [count];
                            for (count=0; count<length; count++) data [count + offset] = buffer [count];
                        }
                    }
                }
            }
            catch (IOException e) { length = 0; }
            
            return length;
        }
        
        /** close this stream */
        public void closeStream ()
        {
            try { stream.close(); } catch (IOException e) { }
            closed = true;
        }
        
        /* find out wether a stream is closed 
         * @return true if the stream is closed */
        public boolean isClosed ()
        {
            return closed;
        }
        
        /** create a byte reading stream on any data read so far */
        public ByteArrayInputStream createByteReader ()
        {
            ByteArrayInputStream ba_stream;
            
            if (data.length <= 0) return null;
            ba_stream = new ByteArrayInputStream (data);
            data = new byte [0];
            return ba_stream;
        }
    }
    
    // an array of input streams - contains StreamDetails objects
    private Vector<StreamDetails> input_streams;
    
    // a flag to tell the worker thread to stop
    private boolean stop_flag;
    
    // the amount of time in milliseconds to wait between reading the streams
    private int delay_time;
    
    /** Creates a new instance of ThreadMultiStreamReader
     * @param delay_time the amount of time in milliseconds to wait between reading the streams */
    public ThreadMultiStreamReader(int delay_time) 
    {
        input_streams = new Vector<StreamDetails> ();
        stop_flag = false;
        this.delay_time = delay_time;
    }
    
    /** tell the worker thread to stop running */
    public void stopReading ()
    {
        stop_flag = true;
    }
    
    /** add a stream to the monitoring process 
     * @param stream the stream to read from
     * @param discard true to discard data read, false to keep it
     * @param user_data user info associated with this stream */
    public void addStream (InputStream stream, boolean discard, Object user_data)
    {
        StreamDetails details;
        
        details = new StreamDetails (stream, discard, user_data);
        synchronized (input_streams)
        {
            input_streams.add (details);
        }
    }
    
    /** retrieve data from a stream monitor
     * @param user_data the object used to identify the stream
     * @return a byte stream reader with the data OR null if there is no data */
    public ByteArrayInputStream retrieveData (Object user_data)
    {
        int index;
        StreamDetails details;
        ByteArrayInputStream ba_stream;
        
        synchronized (input_streams)
        {
            ba_stream = null;
            index = input_streams.indexOf (user_data);
            if (index >= 0)
            {
                details = input_streams.get(index);
                ba_stream = details.createByteReader ();
            }
        }
        return ba_stream;
    }
    
    /** find out whether is stream monitor has completed reading
     * @param user_data the object used to identify the stream
     * @return true is reading is finished */
    public boolean isClosed (Object user_data)
    {
        int index;
        StreamDetails details;
        boolean flag;
        
        synchronized (input_streams)
        {
            flag = true;
            index = input_streams.indexOf (user_data);
            if (index >= 0)
            {
                details = input_streams.get(index);
                flag = details.isClosed ();
            }
        }
        return flag;
    }
    
    /** remove a stream from monitoring
     * @param user_data the object used to identify the stream */
    public void remove (Object user_data)
    {
        synchronized (input_streams)
        {
            input_streams.remove(user_data);
        }
    }
    
    /** run the stream reader in a new thread */
    public void run ()
    {
        int count, length;
        StreamDetails details;
        
        // until we are told to stop
        while (! stop_flag)
        {
            // prevent multiple thread errors
            synchronized (input_streams)
            {
                for (count=0; (count<input_streams.size()) && (! stop_flag); count++)
                {
                    details = input_streams.get(count);
                    do
                    {
                        length = details.readData();
                    } while (length > 0);
                    if (length < 0) details.closeStream ();
                }
            }
            
            // wait for a bit, so we don't hog the CPU
            if (! stop_flag)
            {
                try { sleep (delay_time); } catch (InterruptedException e) { }
            }
        }
    }

}
