/*
 * @(#)PipedOutputStream2.java	1.25 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package bgs.geophys.library.io;

import java.io.*;

/**
 * This is a copy of the SUN PipedOutputStream object. The
 * SUN PipedInputStream object had a feature that stopped
 * it working (see PipedInputStream2 for details). Since
 * these two objects are closely linked, copies of both
 * objects have been taken.
 * <p>
 * A piped output stream can be connected to a piped input stream 
 * to create a communications pipe. The piped output stream is the 
 * sending end of the pipe. Typically, data is written to a 
 * <code>PipedOutputStream</code> object by one thread and data is 
 * read from the connected <code>PipedInputStream2</code> by some 
 * other thread. Attempting to use both objects from a single thread 
 * is not recommended as it may deadlock the thread.
 *
 * @author  James Gosling
 * @version 1.25, 01/23/03
 * @see     java.io.PipedInputStream
 * @since   JDK1.0
 */
public class PipedOutputStream2 extends OutputStream {

	/* REMIND: identification of the read and write sides needs to be
	   more sophisticated.  Either using thread groups (but what about
	   pipes within a thread?) or using finalization (but it may be a
	   long time until the next GC). */
    private PipedInputStream2 sink;

    /**
     * Creates a piped output stream connected to the specified piped 
     * input stream. Data bytes written to this stream will then be 
     * available as input from <code>snk</code>.
     *
     * @param      snk   The piped input stream to connect to.
     * @exception  IOException  if an I/O error occurs.
     */
    public PipedOutputStream2(PipedInputStream2 snk) throws IOException {
	connect(snk);
    }
    
    /**
     * Creates a piped output stream that is not yet connected to a 
     * piped input stream. It must be connected to a piped input stream, 
     * either by the receiver or the sender, before being used. 
     *
     * @see     bgs.geophys.library.io.PipedInputStream2#connect(bgs.geophys.library.io.PipedOutputStream2)
     * @see     bgs.geophys.library.io.PipedOutputStream2#connect(bgs.geophys.library.io.PipedInputStream2)
     */
    public PipedOutputStream2() {
    }
    
    /**
     * Connects this piped output stream to a receiver. If this object
     * is already connected to some other piped input stream, an 
     * <code>IOException</code> is thrown.
     * <p>
     * If <code>snk</code> is an unconnected piped input stream and 
     * <code>src</code> is an unconnected piped output stream, they may 
     * be connected by either the call:
     * <blockquote><pre>
     * src.connect(snk)</pre></blockquote>
     * or the call:
     * <blockquote><pre>
     * snk.connect(src)</pre></blockquote>
     * The two calls have the same effect.
     *
     * @param      snk   the piped input stream to connect to.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void connect(PipedInputStream2 snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (sink != null || snk.connected) {
	    throw new IOException("Already connected");
	}
	sink = snk;
	snk.in = -1;
	snk.out = 0;
        snk.connected = true;
    }

    /**
     * Writes the specified <code>byte</code> to the piped output stream. 
     * If a thread was reading data bytes from the connected piped input 
     * stream, but the thread is no longer alive, then an 
     * <code>IOException</code> is thrown.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param      b   the <code>byte</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b)  throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
	sink.receive(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this piped output stream. 
     * If a thread was reading data bytes from the connected piped input 
     * stream, but the thread is no longer alive, then an 
     * <code>IOException</code> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        } else if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	} 
	sink.receive(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes 
     * to be written out. 
     * This will notify any readers that bytes are waiting in the pipe.
     *
     * @exception IOException if an I/O error occurs.
     */
    public synchronized void flush() throws IOException {
	if (sink != null) {
            synchronized (sink) {
                sink.notifyAll();
            }
	}
    }

    /**
     * Closes this piped output stream and releases any system resources 
     * associated with this stream. This stream may no longer be used for 
     * writing bytes.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close()  throws IOException {
	if (sink != null) {
	    sink.receivedLast();
	}
    }
}
