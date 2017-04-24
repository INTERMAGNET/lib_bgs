/*
 * SerialPortEx.java
 *
 * Created on 04 June 2004, 21:21
 */

package bgs.geophys.library.SerialPort;

import javax.comm.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This class acts as a wrapper for the SUN comm API. It provides a set
 * of methods for opening, reading, writing, closing and enumerating
 * serial ports. It also avoids the need for explicitly installing 
 * the comm API files - normally 3 files need to be installed in the
 * JRE - these are dealt with as follows:
 *
 *   javax.comm.properties - replaced by hardwired code - see static initialiser
 *   comm.jar - must be included in program's class path
 *   win32com.dll - ???
 *
 * @author  Administrator
 */
public class SerialPortEx implements SerialPortEventListener
{

    /** a wrapper for the javax.comm no such port exception */
    public class SPXNoSuchPortException extends Exception 
    { 
        public SPXNoSuchPortException () { super (); }
        public SPXNoSuchPortException (String message) { super (message); }
    }
    
    /** a wrapper for the javax.comm port in use exception */
    public class SPXPortInUseException extends Exception
    { 
        public SPXPortInUseException () { super (); }
        public SPXPortInUseException (String message) { super (message); }
    }
    
    /** a wrapper for the javax.comm no such port exception */
    public class SPXUnsupportedCommOperationException extends Exception
    { 
        public SPXUnsupportedCommOperationException () { super (); }
        public SPXUnsupportedCommOperationException (String message) { super (message); }
    }
    
    /** an exception that is thrown if the comm API classes or driver
     * are not loaded correctly - the message will ALWAYS be present and
     * will describe something about the config problem */
    public class SPXCommApiConfigException extends Exception
    {
        public SPXCommApiConfigException (String message) { super (message); }
    }
    

    // private members
    private SerialPort serial_port;
    private boolean output_buffer_empty;
    private Vector<SerialDataListener> listener_list;
    private String app_name;
    private String port_name;
    private int in_mask;
    private int out_mask;
    
    /** get a list of serial ports as an array of strings 
     * @param loadLubraryVerbose if true send debugging messages to System.out about the COMM API library loading process
     * @return the list of serial ports OR null if there are no serial ports
     * @throws SPXCommApiConfigException if the comm API is not correctly configured */
    public static String [] listSerialPorts (boolean loadLibraryVerbose)
    throws SPXCommApiConfigException
    {
        int count;
        CommPortIdentifier portId;
        Enumeration en;
        Vector<String> array;
        String string_list [];

        // check that the library is loaded
        new SerialPortEx (loadLibraryVerbose);
        
        // iterate through the ports.
        array = new Vector<String> ();
        for (en = CommPortIdentifier.getPortIdentifiers(); en.hasMoreElements(); )
        {
            portId = (CommPortIdentifier) en.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
                array.add (portId.getName());
        }
        if (array.size() <= 0) return null;
        
        // make and return the array of strings
        string_list = new String [array.size()];
        for (count=0; count<array.size(); count++) string_list [count] = (String) array.get (count);
        return string_list;
    }

    /** construct a new serial port object
     * @param loadLibraryVerbose if true send debugging messages to System.out about the COMM API library loading process
     * @throws SPXCommApiConfigException if the comm API is not correctly configured */
    public SerialPortEx (boolean loadLibraryVerbose)
    throws SPXCommApiConfigException
    {
        loadLibrary (loadLibraryVerbose);
        
        serial_port = null;
        app_name = port_name = "";
        listener_list = new Vector<SerialDataListener> ();
    }
    
    /** construct a new serial port object with a list of serial port listeners
     * @param listener_list an array of serial port listeners
     * @param loadLibraryVerbose if true send debugging messages to System.out about the COMM API library loading process
     * @throws SPXCommApiConfigException if the comm API is not correctly configured */
    public SerialPortEx (Vector<SerialDataListener> listener_list, boolean loadLibraryVerbose)
    throws SPXCommApiConfigException
    {
        int count;
        
        loadLibrary (loadLibraryVerbose);
        
        serial_port = null;
        app_name = port_name = "";
        this.listener_list = new Vector<SerialDataListener> ();
        for (count=0; count<listener_list.size(); count++)
            this.listener_list.add (listener_list.get(count));
    }

    /** clear up before deletion */
    public void finalize () { close (); }

    /** opens the serial port that the user specifies
     * @param app_name the name of the application
     * @param open_timeout the amount of time to wait to open the port (in mS)
     * @param settings the serial port settings
     * @throws SPXNoSuchPortException if the port cannot be found
     * @throws SPXPortInUseException if the port cannot be opened 
     * @throws SPXUnsupportedCommOperationException if the serial port canot be correctly configured */
    public void open (String app_name, int open_timeout, SerialSettings settings)
    throws SPXNoSuchPortException, SPXPortInUseException, SPXUnsupportedCommOperationException
    {
        open (app_name, open_timeout, settings.port_name,
              settings.baud, settings.data_bits, settings.stop_bits,
              settings.parity_code, settings.in_buffer_size, settings.out_buffer_size,
              settings.in_mask, settings.out_mask);
    }
    
    /** opens the serial port that the user specifies
     * @param app_name the name of the application
     * @param open_timeout the amount of time to wait to open the port (in mS)
     * @param port_name the name of the serial port
     * @param baud the baud rate
     * @param data_bits the number of data bits (5-8)
     * @param stop_bits the number of stop bits (1-2)
     * @param parity_code the parity setting code: 'none', 'even', 'odd',
     *        'mark', 'space'
     * @param in_buffer_size the size of the input buffer in bytes
     * @param out_buffer_size the size of the output buffer in bytes
     * @throws SPXNoSuchPortException if the port cannot be found
     * @throws SPXPortInUseException if the port cannot be opened 
     * @throws SPXUnsupportedCommOperationException if the serial port canot be correctly configured */
    public void open (String app_name, int open_timeout, String port_name,
                      int baud, int data_bits, int stop_bits,
                      String parity_code, int in_buffer_size, int out_buffer_size)
    throws SPXNoSuchPortException, SPXPortInUseException, SPXUnsupportedCommOperationException
    {
        open (app_name, open_timeout, port_name, baud, data_bits, stop_bits,
              parity_code, in_buffer_size, out_buffer_size, 0xff, 0xff);
    }
    
    /** opens the serial port that the user specifies
     * @param app_name the name of the application
     * @param open_timeout the amount of time to wait to open the port (in mS)
     * @param port_name the name of the serial port
     * @param baud the baud rate
     * @param data_bits the number of data bits (5-8)
     * @param stop_bits the number of stop bits (1-2)
     * @param parity_code the parity setting code: 'none', 'even', 'odd',
     *        'mark', 'space'
     * @param in_buffer_size the size of the input buffer in bytes
     * @param out_buffer_size the size of the output buffer in bytes
     * @param in_mask a value that is ANDed with incoming data to allow
     *        bits to be masked off
     * @param out_mask a value that is ANDed with outgoing data to allow
     *        bits to be masked off
     * @throws SPXNoSuchPortException if the port cannot be found
     * @throws SPXPortInUseException if the port cannot be opened 
     * @throws SPXUnsupportedCommOperationException if the serial port canot be correctly configured */
    public void open (String app_name, int open_timeout, String port_name,
                      int baud, int data_bits, int stop_bits,
                      String parity_code, int in_buffer_size, int out_buffer_size,
                      int in_mask, int out_mask)
    throws SPXNoSuchPortException, SPXPortInUseException, SPXUnsupportedCommOperationException
    {
        int parity;
        CommPortIdentifier port_identifier;
        
        try
        {
            // open the serial port
            port_identifier = CommPortIdentifier.getPortIdentifier (port_name);
            if (port_identifier.getPortType() != CommPortIdentifier.PORT_SERIAL)
                throw new SPXUnsupportedCommOperationException (port_name + " is not a serial port");
            serial_port = (SerialPort) port_identifier.open (app_name, open_timeout);
        
            // configure the serial port
            serial_port.setInputBufferSize (in_buffer_size);
            serial_port.setOutputBufferSize (out_buffer_size);
            serial_port.setFlowControlMode (serial_port.FLOWCONTROL_NONE);
            switch (data_bits)
            {
            case 5: data_bits = serial_port.DATABITS_5; break;
            case 6: data_bits = serial_port.DATABITS_6; break;
            case 7: data_bits = serial_port.DATABITS_7; break;
            case 8: data_bits = serial_port.DATABITS_8; break;
            default:
                throw new SPXUnsupportedCommOperationException ("Unspported number of data bits: " + Integer.toString (data_bits));
            }
            switch (stop_bits)
            {
            case 1: stop_bits = serial_port.STOPBITS_1; break;
            case 2: stop_bits = serial_port.STOPBITS_2; break;
            default:
                throw new SPXUnsupportedCommOperationException ("Unspported number of stop bits: " + Integer.toString (stop_bits));
            }
            if (parity_code.equalsIgnoreCase("none") || parity_code.equalsIgnoreCase("n"))
                parity = serial_port.PARITY_NONE;
            else if (parity_code.equalsIgnoreCase("even") || parity_code.equalsIgnoreCase("e"))
                parity = serial_port.PARITY_EVEN;
            else if (parity_code.equalsIgnoreCase("odd") || parity_code.equalsIgnoreCase("o"))
                parity = serial_port.PARITY_ODD;
            else if (parity_code.equalsIgnoreCase("mark") || parity_code.equalsIgnoreCase("m"))
                parity = serial_port.PARITY_MARK;
            else if (parity_code.equalsIgnoreCase("space") || parity_code.equalsIgnoreCase("s"))
                parity = serial_port.PARITY_SPACE;
            else
                throw new SPXUnsupportedCommOperationException ("Unspported parity code: " + parity_code);
            serial_port.setSerialPortParams (baud, data_bits, stop_bits, parity);

            try
            {
                serial_port.addEventListener (this);
            }
            catch (TooManyListenersException e)
            {
                throw new SPXUnsupportedCommOperationException ("unable to register event listener");
            }
            serial_port.notifyOnDataAvailable (true);
            serial_port.notifyOnOutputEmpty (true);
            output_buffer_empty = true;

            // store the users parameters
            this.app_name = app_name;
            this.port_name = port_name;
            this.in_mask = in_mask;
            this.out_mask = out_mask;
        
            // call any listeners
            sendToListeners (SerialDataListener.OPEN_PORT, 
                             Integer.toString (baud) + "," +
                             Integer.toString (data_bits) + "," +
                             Integer.toString (stop_bits) + "," +
                             parity_code);
        }
        // translate exceptions from javax.comm to bgs library
        catch (NoSuchPortException e) { throw new SPXNoSuchPortException (e.getMessage()); }
        catch (PortInUseException e) { throw new SPXPortInUseException (e.getMessage ()); }
        catch (UnsupportedCommOperationException e) { throw new SPXUnsupportedCommOperationException (e.getMessage()); }
    }
    
    /** close the serial port */
    public void close ()
    {
        if (serial_port != null)
        {
            serial_port.close ();
            sendToListeners (SerialDataListener.CLOSE_PORT, "");
        }
        serial_port = null;
    }
    
    /** add a serial data listener
     * @param listener the new listener */
    public void addSerialDataListener (SerialDataListener listener)
    {
        listener_list.add (listener);
    }
    
    /** remove a serial data listener
     * @param listener the listener to remove */
    public void removeSerialDataListener (SerialDataListener listener)
    {
        listener_list.remove (listener);
    }
    
    /** discard input buffer
     * @throws IOException if there is an IO error */
    public void flushInputBuffer ()
    throws IOException
    {
        String string;
        Vector<SerialDataListener> saved_listener_list;
        
        saved_listener_list = listener_list;
        listener_list = new Vector<SerialDataListener> ();
        string = this.read ();
        listener_list = saved_listener_list;
        sendToListeners (SerialDataListener.FLUSH_DATA, string);
    }
    
    /** read all available bytes from the serial port
     * @throws IOException if there is an IO error */
    public String read ()
    throws IOException
    {
        int n_bytes, count;
        byte [] buffer;
        String string;
        InputStream input_stream;
        
        input_stream = serial_port.getInputStream ();
        buffer = new byte [100];
        string = "";
        while (input_stream.available() > 0) 
        {
            n_bytes = input_stream.read (buffer);
            for (count=0; count<n_bytes; count++) buffer [count] &= in_mask;
            string += new String (buffer, 0, n_bytes);
        }
        
        sendToListeners (SerialDataListener.RX_DATA, string);
        return string;
    }
    
    /** read all available bytes from the serial port
     * @param terminator_string a set of termination characters - any of these in the
     *        input stream will terminate the read
     * @param include_term if true include the termination character
     * @throws IOException if there is an IO error */
    public String read (String terminator_string, boolean include_term)
    throws IOException
    {
        return this.read (terminator_string, include_term, -1);
    }
    
    /** read all available bytes from the serial port
     * @param terminators a set of termination characters - any of these in the
     *        input stream will terminate the read
     * @param include_term if true include the termination character
     * @throws IOException if there is an IO error */
    public String read (byte terminators [], boolean include_term)
    throws IOException
    {
        return this.read (terminators, include_term, -1);
    }
    
    /** read all available bytes from the serial port
     * @param terminator_string a set of termination characters - any of these in the
     *        input stream will terminate the read
     * @param include_term if true include the termination character
     * @param timeout the time to wait (in mS)
     * @throws IOException if there is an IO error */
    public String read (String terminator_string, boolean include_term, int timeout)
    throws IOException
    {
        byte terminators [];

        terminators = terminator_string.getBytes();
        return this.read (terminators, include_term, timeout);
    }
    
    /** read all available bytes from the serial port
     * @param terminators a set of termination characters - any of these in the
     *        input stream will terminate the read
     * @param include_term if true include the termination character
     * @param timeout the time to wait (in mS)
     * @throws IOException if there is an IO error */
    public String read (byte terminators [], boolean include_term, int timeout)
    throws IOException
    {
        int count, rx_byte;
        boolean found;
        byte buffer [];
        String string;
        Date start_date, now_date;
        InputStream input_stream;
        
        input_stream = serial_port.getInputStream ();
        string = "";
        buffer = new byte [1];
        start_date = new Date ();
        found = false;
        do
        {
            // check for timeout
            if (input_stream.available() <= 0)
            {
                if (timeout > 0)
                {
                    now_date = new Date ();
                    if (now_date.getTime() - start_date.getTime() >= timeout) found = true;
                }
            }
            else
            {
                // read the next byte
                rx_byte = input_stream.read ();
                if (rx_byte < 0) found = true;
                else
                {
                    // check for termination character
                    buffer [0] = (byte) (rx_byte & in_mask);
                    for (count=0; count<terminators.length; count++)
                    {
                        if (terminators[count] == rx_byte) found = true;
                    }
                    if (! found) string += new String (buffer);
                    else if (include_term) string += new String (buffer);
                }
            }
        } while (! found);
        
        sendToListeners (SerialDataListener.RX_DATA, string);
        return string;
    }

    /** read a number of bytes from the serial port
     * @param n_bytes the number of bytes to read
     * @param timeout the time to wait (in mS)
     * @throws IOException if there is an IO error */
    public String read (int n_bytes, int timeout)
    throws IOException
    {
        int count, rx_byte;
        boolean found;
        byte buffer [];
        String string;
        Date start_date, now_date;
        InputStream input_stream;
        
        input_stream = serial_port.getInputStream ();
        string = "";
        buffer = new byte [1];
        start_date = new Date ();
        found = false;
        do
        {
            // check for end of data
            if (string.length () >= n_bytes) found = true;
            
            // check for timeout
            else if (input_stream.available() <= 0)
            {
                if (timeout > 0)
                {
                    now_date = new Date ();
                    if (now_date.getTime() - start_date.getTime() >= timeout) found = true;
                }
            }

            // read the next byte
            else
            {
                rx_byte = input_stream.read ();
                if (rx_byte < 0) found = true;
                buffer [0] = (byte) (rx_byte & in_mask);
                string += new String (buffer);
            }
        } while (! found);
        
        sendToListeners (SerialDataListener.RX_DATA, string);
        return string;
    }
    
    /** write a string to the serial port
     * @param string the string to write
     * @throws IOException if there is an IO error */
    public void write (String string)
    throws IOException
    {
        int count;
        byte buffer [];
        
        if (string.length() > 0)
        {
            buffer = string.getBytes();
            for (count=0; count<buffer.length; count++) buffer [count] &= out_mask;
            serial_port.getOutputStream().write (buffer);
            sendToListeners (SerialDataListener.TX_DATA, new String (buffer));
            output_buffer_empty = false;
        }
    }
    
    /** wait until the output buffer empties */
    public void waitForOutputEmpty ()
    {
        while (! output_buffer_empty) { }
    }

    /** wait until the output buffer empties
     * @param timeout the maximum length of time to wait in mS
     * @return true if the output buffer is empty, false if a timeout occurred */
    public boolean waitForOutputEmpty (int timeout)
    {
        Date start_date, now_date;
        
        start_date = new Date ();
        while (! output_buffer_empty)
        { 
            now_date = new Date ();
            if (now_date.getTime() - start_date.getTime() >= timeout) return false;
        }
        
        return true;
    }
    
    /** receive serial port events  */
    public void serialEvent (SerialPortEvent event)
    {
	switch (event.getEventType()) 
        {
	case SerialPortEvent.BI:
	case SerialPortEvent.OE:
	case SerialPortEvent.FE:
	case SerialPortEvent.PE:
	case SerialPortEvent.CD:
	case SerialPortEvent.CTS:
	case SerialPortEvent.DSR:
	case SerialPortEvent.RI:
	case SerialPortEvent.DATA_AVAILABLE:
            break;
            
	case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            output_buffer_empty = true;
	    break;
	}
    }

    /** send serial events to listeners
     * @param data_type one of the codes for the data type (Open, Tx, Rx, Flush or Close)
     * @param data the serial data */
    private void sendToListeners (int data_type, String data)
    {
        int count;
        SerialDataListener listener;
        
        for (count=0; count<listener_list.size(); count++)
        {
            listener = (SerialDataListener) listener_list.get (count);
            listener.serialDataEvent (app_name, port_name, data_type, data);
        }
    }


    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // code below here deals with loading the serial API library files
    // in the original SUN comm API, three files needed to be stored in the JSR:
    //   java.comm.properties - tells Java the library to load - different for different OSs
    //   comm.jar - the java side of the API
    //   win32comm.dll - the native side of the API
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    // copying the DLL into the library path is a one-tine operation - so the
    // flag that shows wether it has been done should be static
    private static boolean dll_copied = false;
    
    // loading the library is a one-tine operation - so the flag that
    // shows whether it has been done should be static
    private static boolean library_loaded = false;
    
    /** this code is a fix to avoid needing to distribute the javax.comm API files:
     *   win32comm.dll is handled by copying the file into an appropriate directory
     *   javax.comm.properties is dealt with by hardwiring a link to the driver
     *   comm.jar should be merged with the final jar file (outside this code)
     * NBNBNB CALL THIS FUNCTION BEFORE ANY OTHER COMM API FUNCTIONS */
    private void loadLibrary (boolean verbose)
    throws SPXCommApiConfigException
    {
        String driverName, dllName, javaHome;
        File list [], javaBin, dllFile;

        driverName = "com.sun.comm.Win32Driver";
        dllName = "win32com.dll";
        
        // do we need to copy the dll ??
        if (! dll_copied)
        {
            // get the java home and bin directories
            javaHome = System.getProperty ("java.home", "");
            if (verbose) System.out.println ("SerialPortEx: Java home directory: " + javaHome);
            javaBin = new File (javaHome, "bin");
            if (! javaBin.exists())
            {
                if (verbose) System.out.println ("SerialPortEx: Java bin directory does not exist: " + javaBin.getAbsolutePath());
                throw new SPXCommApiConfigException ("Java bin directory does not exist: " + javaBin.getAbsolutePath());
            }
            if (! javaBin.isDirectory())
            {
                if (verbose) System.out.println ("SerialPortEx: Java bin is not a directory: " + javaBin.getAbsolutePath());
                throw new SPXCommApiConfigException ("Java bin is not a directory: " + javaBin.getAbsolutePath());
            }
            
            // check the directory to see if it contains the DLL
            dllFile = new File (javaBin, dllName);
            if (dllFile.exists())
            {
                if (verbose) System.out.println ("SerialPortEx: Found existing DLL at " + dllFile.getAbsolutePath());
            }
            else
            {
                try
                {
                    if (verbose) System.out.println ("SerialPortEx: Copying DLL to " + dllFile.getAbsolutePath());
                    putFile (dllName, dllFile);
                    if (verbose) System.out.println ("SerialPortEx: DLL copied OK");
                }
                catch (IOException e)
                {
                    if (verbose) 
                    {
                        System.out.print ("SerialPortEx: Error copying file");
                        if (e.getMessage() != null) System.out.print (": " + e.getMessage());
                        System.out.println ("");
                    }
                    throw new SPXCommApiConfigException ("Error copying Windows DLL file to " + dllFile.getAbsolutePath());
                }
            }
            
            // flag that the DLL is copied OK
            dll_copied = true;
        }

        // do we need to load the library
        if (! library_loaded)
        {
            // attempt to load the driver (replacement for javax.comm.properties on Windows) - 
            // this code works by hardwiring the driver loading (the properties file only contains a
            // link to the driver) - it must only run once or you will get multiple entries for each port -
            // the properties file must NOT be installed (or again you will get multiple entries)
            try
            {
                if (verbose) System.out.println ("SerialPortEx: Attempting to load library classes");
                CommDriver commDriver = (CommDriver)Class.forName(driverName).newInstance();
                commDriver.initialize();
                if (verbose) System.out.println ("SerialPortEx: Library classes loaded OK");
            }
            catch (IllegalAccessException e) 
            { 
                System.out.print ("SerialPortEx: Illegal access error loading library");
                if (e.getMessage() != null) System.out.print (": " + e.getMessage());
                System.out.println ("");
                throw new SPXCommApiConfigException ("Unable to load comm driver: illegal access"); 
            }
            catch (ClassNotFoundException e) 
            { 
                System.out.print ("SerialPortEx: Class not found error loading library");
                if (e.getMessage() != null) System.out.print (": " + e.getMessage());
                System.out.println ("");
                throw new SPXCommApiConfigException ("Unable to load comm driver: class not found"); 
            }
            catch (InstantiationException e) 
            { 
                System.out.print ("SerialPortEx: Instantiation error loading library");
                if (e.getMessage() != null) System.out.print (": " + e.getMessage());
                System.out.println ("");
                throw new SPXCommApiConfigException ("Unable to load comm driver: instantiation exception"); 
            }
        }
        
        library_loaded = true;
    }
    
    /** copy a file from a place in the class structure to a place on disk */
    private static void putFile (String resourceName, File file)
    throws IOException
    {
        int length;
        URL url;
        InputStream is;
        FileOutputStream os;
        byte buffer [];

        buffer = new byte [2048];
        
        url = new SerialSettings().getClass().getResource(resourceName);
        if (url == null) throw new IOException ("Unable to find resource: " + resourceName);
        is = url.openStream();
        
        os = new FileOutputStream (file);
        
        for (length = is.read(buffer); length > 0; length = is.read(buffer))
            os.write(buffer, 0, length);
        
        os.close ();
        is.close();
    }
}

