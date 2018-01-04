/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Autoplot;

import bgs.geophys.library.Data.GeomagDataFilename;
import bgs.geophys.library.Data.GeomagDataFormat;
import bgs.geophys.library.Data.GeomagDataFormat.DurationType;
import bgs.geophys.library.Data.Iaga2002;
import bgs.geophys.library.Data.Iaga2002Filename;
import bgs.geophys.library.Data.ImagCDF.IMCDFPublicationLevel;
import bgs.geophys.library.Data.ImagCDF.ImagCDF;
import bgs.geophys.library.Data.ImagCDF.ImagCDFFilename;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Threads.RunClass;
import bgs.geophys.library.Threads.ThreadMultiStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * a class to control a running autoplot instance
 * the constructor starts the autoplot instance
 * comms with the autoplot instance are managed by a separate thread
 *     request to view data cause a message to be created and passed
 *     to the thread that manages the comms with autoplot
 *
 * @author smf
 */
public class AutoplotInstance
implements Runnable
{
    
    private class APMessage
    {
        private List<String> commands;
        public APMessage ()
        {
            commands = new ArrayList<> ();
        }
        public void addCommand (String command) { commands.add (command); }
        public String removeCommand () { if (commands.isEmpty()) return null; return commands.remove(0); }
    }
    
    private class APMessageQueue
    {
        private List<APMessage> messages;
        public APMessageQueue ()
        {
            messages = new ArrayList<> ();
        }
        public void addMessage (APMessage message) { messages.add (message); }
        public boolean hasNext () { return (! messages.isEmpty()); }
        public APMessage removeMessage () { if (messages.isEmpty()) return null; return messages.remove(0); }
    }
    
    private class APExceptionQueue
    {
        private List<Exception> exceptions;
        public APExceptionQueue ()
        {
            exceptions = new ArrayList<> ();
        }
        public void addException (Exception exception) { exceptions.add (exception); }
        public Exception removeException () { if (exceptions.isEmpty()) return null; return exceptions.remove(0); }
    }
    
    // the string that autoplot emits describing the port its server is running on
    private static final String AP_PORT_SEARCH_STRING = "autoplot is listening on port";

    // time to wait for data from autoplot in mS - needs to be long enough to cope with long opertions in jython
    private static final int AUTOPLOT_RX_TIMEOUT = 100000;      

    // the prompt that autoplot sends on it's server interface - used to terminate data received from autoplot
    private static final String AUTOPLOT_PROMPT = "autoplot> ";
    
    // variables used to control autoplot
    private RunClass autoplot_process;
    private String autoplot_stdout_id;
    private String aautoplot_stderr_id;
    private int autoplot_port;
    private Socket autoplot_socket;
    private BufferedInputStream stream_from_autoplot;
    private OutputStream stream_to_autoplot;
    private boolean debug_comms;
    
    // variables used to communicate with autoplot via a thread
    private Thread ap_comms_thread;
    private boolean ap_thread_cancel;
    private final APMessageQueue ap_message_queue;
    private APExceptionQueue ap_exception_queue;
    

    /** start an instance of autoplot
     * 
     * @param autoplot_args the argument list to pass to autoplot
     * @param stream_reader a stream reader that will monitor autoplot's stdout and stderr
     * @param stream_id_counter a counter to generate unique IDs for the stream reader
     * @param startup_script_name the name of the startup script that autoplot should load
     * @param debug_comms  true to print transmitted and received data on the comms link to autoplot
     * @throws IOException if the autoplot jar can't be found or if a free port can't be found
     * @throws ClassNotFoundException  if the autoplot class can't be found in the jar
     * @throws NoSuchMethodException if the main() method can't be found in the autoplot class
     */
    public AutoplotInstance (String autoplot_args [], ThreadMultiStreamReader stream_reader, 
                             int stream_id_counter, String startup_scrpt_name, boolean debug_comms)
    throws ClassNotFoundException, NoSuchMethodException, IOException
    {
        this.debug_comms = debug_comms;
        
        // run autoplot
        autoplot_process = new RunClass ("org.virbo.autoplot.AutoplotUI", autoplot_args, true, RunClass.EXTERNAL, false);

        // log autoplot's streams with the stream reader
        Process process = (Process) autoplot_process.getThreadOrProcess();
        autoplot_stdout_id = "AP STDOUT ID " + Integer.toString (stream_id_counter);
        aautoplot_stderr_id = "AP STDERR ID " + Integer.toString (stream_id_counter);
        stream_reader.addStream (process.getInputStream(), false, autoplot_stdout_id);
        stream_reader.addStream (process.getErrorStream(), false, aautoplot_stderr_id);

        // monitor autoplot output, looking for the line describing the port autoplot is using
        // monitor in 100ms chunks for a maximum of 5 secs
        autoplot_port = -1;
        for (int count=0; (count<50) && (autoplot_port == -1); count++)
        {
            // read the next line from stdout or stderr
            String line = null;
            try { line = stream_reader.retrieveNextDataLine(autoplot_stdout_id);  }
            catch (IOException e)
            {
                try { line = stream_reader.retrieveNextDataLine(aautoplot_stderr_id); }
                catch (IOException e2) { line = null; }
            }
            if (line != null)
            {
                // parse the line looking for the autplot 'port' message
                if (line.startsWith (AP_PORT_SEARCH_STRING))
                {
                    String port_str = line.substring(AP_PORT_SEARCH_STRING.length());
                    port_str = port_str.replace(".", " ");
                    port_str = port_str.trim ();
                    try { autoplot_port = Integer.parseInt(port_str); }
                    catch (NumberFormatException e) { autoplot_port = -1; }
                }
            }
            try { Thread.sleep (100); } catch (InterruptedException e) { }
        }
        if (autoplot_port < 0)
            throw new IOException ("Unable to find port autoplot is lstening on");

        // set up streams to communicate with autoplot
        autoplot_socket = new Socket ("localhost", autoplot_port);
        autoplot_socket.setSoTimeout (1000);
        stream_from_autoplot = new BufferedInputStream (autoplot_socket.getInputStream());
        stream_to_autoplot = autoplot_socket.getOutputStream();
        
        // receive (consume) the first prompt from autoplot
        rx ();
        
        // tell autoplot to load the startup script
        sendCommand ("import " + startup_scrpt_name + " as geomag");
        
        // pass control to the thread that will communuicate with autoplot
        // from this point onwards all comms should be through the message queue
        // using the thread, not direct using sendCommand()
        this.ap_message_queue = new APMessageQueue ();
        this.ap_exception_queue = new APExceptionQueue ();
        this.ap_thread_cancel = false;
        this.ap_comms_thread = new Thread (this);
        this.ap_comms_thread.setDaemon(true);
        this.ap_comms_thread.start();
    }
    
    public boolean isAlive ()
    {
        return autoplot_process.isAlive();
    }
    
    public void exit ()
    {
        if (debug_comms) System.out.println ("Autoplot: process exit requested");
        ap_thread_cancel = true;
        // this should do something neat like sending a jython command to allow autoplot to exit neatly
        Process process = (Process) autoplot_process.getThreadOrProcess();
        process.destroy();
        
        if (debug_comms) System.out.println ("Autoplot: Socket to Autoplot closed");
        try
        {
            stream_from_autoplot.close ();
            stream_to_autoplot.close ();
            autoplot_socket.close();
        }
        catch (IOException e) { }
    }
    
    /** any exceptions the autoplot generates are delivered in the thread that
     * manages autoplot comms. They are then put in a queue and can be retrieved
     * using this method
     * 
     * @return the earliest exception or null for no exceptions */
    public Exception getExceptionFromAutoplot ()
    {
        Exception exception;
        synchronized (ap_exception_queue)
        {
            exception = ap_exception_queue.removeException();
        }
        return exception;
    }
 
    /** load a single IAGA-2002 file (without aggregation). This requires reading
     * the IAGA2002 file so that Autoplot can be instructed how to load it - it
     * would be nicer if there was a way Autoplot could understand how to read it
     * without the overhead of having to read it to get metadata first.
     * 
     * @param iaga_2002_file the file to load
     * @param reset true to reset autoplot before plotting the data
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadIaga2002 (File iaga_2002_file, boolean reset) 
    throws FileNotFoundException, IOException
    {
        // start a new message
        APMessage message = new APMessage ();
        
        // collect data about the IAGA-2002 file
        FileInputStream is = new FileInputStream (iaga_2002_file);
        Iaga2002 iaga_2002 = Iaga2002.read (is, false, true);
        is.close();

        String title = makeTitle(iaga_2002.getStationName(), iaga_2002.getStationCode(), (int) iaga_2002.getSamplePeriod());
        String component_codes = iaga_2002.getOriginalComponentCodes();
        int n_header_lines = iaga_2002.getNHeaderLines();
        if (reset) message.addCommand("reset ()");
        for (int count=0; count<component_codes.length(); count++)
        {
            // create and send the command to display this element
            String component = component_codes.substring(count, count+1);
            String field_name = "field" + Integer.toString (count +3);
            AutoplotURIBuilder uri_builder = new AutoplotURIBuilder(AutoplotURIBuilder.URIType.VAP_DAT_FILE, iaga_2002_file);
            if (count == 0) uri_builder.addDatTitle(title);
            uri_builder.addDatSkipLines(n_header_lines);
            uri_builder.addDatTimeFieldName("field0");
            uri_builder.addDatColumnFieldName(field_name);
            uri_builder.addDatTimeFormat("$Y-$m-$d+$H:$M:$S.$(milli)");
            uri_builder.addDatFillValue(Iaga2002.MISSING_DATA_SAMPLE);
            uri_builder.addDatValidMin(GeomagDataFormat.getValidMaxOrMin(component, false));
            uri_builder.addDatValidMax(GeomagDataFormat.getValidMaxOrMin(component, true));
            uri_builder.addDatLabel(component + "(" + Iaga2002.getUnits(component) + ")");
            uri_builder.addDatUnits(Iaga2002.getUnits(component));
            message.addCommand("plot (" + count + ",'" + uri_builder.getURI() + "')");

            // switch off all x-axis disolays except the bottom one
            String dom_plot_name = "dom.plots[" + count + "]";
            if (count < component_codes.length() -1)
            {
                message.addCommand(dom_plot_name + ".controller.plot.xaxis.setVisible (0)");
            }
        }
        
        addSizeCommands (message, component_codes.length());
        sendMessage (message);
    }
    
    /** load data from IAGA-2002 files (with aggregation). This requires reading
     * the first IAGA2002 file so that Autoplot can be instructed how to load it - it
     * would be nicer if there was a way Autoplot could understand how to read the data
     * without the overhead of having to read it here to get metadata first. Also, there
     * is an assumption that the IAGA-2002 files are similar - same geomagnetic elements,
     * same number of comments lines, ... which they may not be.
     * 
     * @param folder the folder to load data from
     * @param iaga_code the station code for the data to load
     * @param start_date the first piece of data to show - this must correspond to 
     * @param interval the sample period of the data to look for
     * @param reset true to reset autoplot before plotting the data
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadIaga2002 (File folder, String iaga_code, Date start_date, GeomagDataFilename.QualityType quality_type,
                              GeomagDataFilename.Interval interval, boolean reset) 
    throws FileNotFoundException, IOException
    {
        // start a new message
        APMessage message = new APMessage ();
        
        // build a filename for the first file and read it
        GeomagDataFilename.Case filename_case;
        GregorianCalendar cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        cal.setTime (start_date);
        Iaga2002Filename iaga_2002_filename = new Iaga2002Filename (iaga_code, start_date, quality_type, interval, 
                                                                    false, DurationType.DAY, GeomagDataFilename.Case.LOWER);
        File iaga_2002_file = new File (folder, iaga_2002_filename.getFilename());
        filename_case = GeomagDataFilename.Case.LOWER;
        if (! iaga_2002_file.exists())
        {
            iaga_2002_filename = new Iaga2002Filename (iaga_code, start_date, quality_type, interval, 
                                                       false, DurationType.DAY, GeomagDataFilename.Case.UPPER);
            iaga_2002_file = new File (folder, iaga_2002_filename.getFilename());
            filename_case = GeomagDataFilename.Case.UPPER;
            if (! iaga_2002_file.exists())
                throw new FileNotFoundException ("Unable to find first data file: " + iaga_2002_filename);
        }
        FileInputStream is = new FileInputStream (iaga_2002_file);
        Iaga2002 iaga_2002 = Iaga2002.read (is, false, true);
        is.close();
        
        // build the filename pattern and timerange to pass to autoplot
        String quality_type_string;
        switch(quality_type) 
        {
            case ADJUSTED: quality_type_string = "a"; break;
            case DEFINITIVE: quality_type_string = "d"; break;
            case PROVISIONAL: quality_type_string = "p"; break;
            case QUASI_DEFINITIVE: quality_type_string = "q"; break;
            case REPORTED: quality_type_string = "r"; break;
            case VARIATION: quality_type_string = "v"; break;
            case TEST: quality_type_string = "t"; break;
            default: throw new IllegalArgumentException("Invalid quality type");
        }        
        String filename_time_pattern, suffix, time_range;
        SimpleDateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd");
        date_format.setTimeZone(DateUtils.gmtTimeZone);
        SimpleDateFormat month_format = new SimpleDateFormat ("yyyy-MM");
        month_format.setTimeZone(DateUtils.gmtTimeZone);
        SimpleDateFormat year_format = new SimpleDateFormat ("yyyy");
        year_format.setTimeZone(DateUtils.gmtTimeZone);
        switch (interval)
        {
            case SECOND:
                filename_time_pattern = "$Y$m$d";
                suffix = "sec.sec";
                time_range = date_format.format (start_date);
                break;
            case MINUTE:
                filename_time_pattern = "$Y$m$d";
                suffix = "min.min";
                time_range = date_format.format (start_date);
                break;
            case HOURLY:
                filename_time_pattern = "$Y$m";
                suffix = "hor.hor";
                time_range = month_format.format (start_date);
                break;
            case DAILY:
                filename_time_pattern = "$Y";
                suffix = "day.day";
                time_range = year_format.format (start_date);
                break;
            default:
                throw new FileNotFoundException ("Iaga2002 interval type not supported: " + interval.toString());
        }
        String filename_pattern;
        if (filename_case == GeomagDataFilename.Case.LOWER)
            filename_pattern = iaga_code.toLowerCase() + filename_time_pattern + quality_type_string.toLowerCase() + suffix.toLowerCase();
        else
            filename_pattern = iaga_code.toUpperCase() + filename_time_pattern + quality_type_string.toUpperCase() + suffix.toUpperCase();
        
        // buold the URI and send commands to autoplot
        String title = makeTitle(iaga_2002.getStationName(), iaga_2002.getStationCode(), (int) iaga_2002.getSamplePeriod());
        String component_codes = iaga_2002.getOriginalComponentCodes();
        int n_header_lines = iaga_2002.getNHeaderLines();
        if (reset) message.addCommand("reset ()");
        for (int count=0; count<component_codes.length(); count++)
        {
            // create and send the command to display this element
            String component = component_codes.substring(count, count+1);
            String field_name = "field" + Integer.toString (count +3);
            File file_pattern = new File (folder, filename_pattern);
            AutoplotURIBuilder uri_builder = new AutoplotURIBuilder(AutoplotURIBuilder.URIType.VAP_DAT_FILE, file_pattern, time_range);
            if (count == 0) uri_builder.addDatTitle(title);
            uri_builder.addDatSkipLines(n_header_lines);
            uri_builder.addDatTimeFieldName("field0");
            uri_builder.addDatColumnFieldName(field_name);
            uri_builder.addDatTimeFormat("$Y-$m-$d+$H:$M:$S.$(milli)");
            uri_builder.addDatFillValue(Iaga2002.MISSING_DATA_SAMPLE);
            uri_builder.addDatValidMin(GeomagDataFormat.getValidMaxOrMin(component, false));
            uri_builder.addDatValidMax(GeomagDataFormat.getValidMaxOrMin(component, true));
            uri_builder.addDatLabel(component + "(" + Iaga2002.getUnits(component) + ")");
            uri_builder.addDatUnits(Iaga2002.getUnits(component));
            message.addCommand("plot (" + count + ",'" + uri_builder.getURI() + "')");

            // switch off all x-axis disolays except the bottom one
            String dom_plot_name = "dom.plots[" + count + "]";
            if (count < component_codes.length() -1)
            {
                message.addCommand(dom_plot_name + ".controller.plot.xaxis.setVisible (0)");
            }
        }
        
        addSizeCommands (message, component_codes.length());
        sendMessage (message);
    }
    
    /** load a single ImagCDF file (without aggregation).
     * 
     * @param imagcdf_file the file to load
     * @param reset true to reset autoplot before plotting the data
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadImagCDF (File imagcdf_file, boolean reset) 
    throws FileNotFoundException, IOException
    {
        // start a new message
        APMessage message = new APMessage ();
        
        String title;
        try
        {
            ImagCDFFilename cdf_filename = new ImagCDFFilename (imagcdf_file.getName());
            title = "Geomagnetic observatory data for " + cdf_filename.getObservatoryCode();
        }
        catch (ParseException e)
        {
            title = "Geomagnetic observatory data";
        }
        if (reset) message.addCommand("reset ()");
        String uri_filename = imagcdf_file.getAbsolutePath().replace("\\", "/");
        message.addCommand("base_cdf_uri = 'vap+cdf:file:/" + uri_filename + "?'");
        message.addCommand("geomag_cdf_ds = geomag.getGeomagDataSets (base_cdf_uri)");
        message.addCommand("geomag.showDSPlot (geomag_cdf_ds, '" + title + "')");
        
        sendMessage (message);
    }
    
    /** load an ImagCDF file (with aggregation).
     * 
     * @param folder the folder to load data from
     * @param iaga_code the station code for the data to load
     * @param start_date the first piece of data to show
     * @param component_codes the list of geomagnetic elements in the file
     * @param publication_level the ImagCDF publication level
     * @param interval the sample interval for the data
     * @param reset true to reset autoplot before plotting the data
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadImagCDF (File folder, String iaga_code, Date start_date, String component_codes,
                             IMCDFPublicationLevel publication_level,
                             ImagCDFFilename.Interval interval, boolean reset) 
    throws FileNotFoundException, IOException
    {
        // start a new message
        APMessage message = new APMessage ();
        
        // build the filename pattern and timerange to pass to autoplot
        SimpleDateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd");
        date_format.setTimeZone(DateUtils.gmtTimeZone);
        SimpleDateFormat month_format = new SimpleDateFormat ("yyyy-MM");
        month_format.setTimeZone(DateUtils.gmtTimeZone);
        SimpleDateFormat year_format = new SimpleDateFormat ("yyyy");
        year_format.setTimeZone(DateUtils.gmtTimeZone);
        String filename_time_pattern, time_range;
        switch (interval)
        {
            case DAILY: 
                filename_time_pattern = "$Y$m$d";
                time_range = year_format.format (start_date);
                break;
            case HOURLY:
                filename_time_pattern = "$Y$m$d_$H";
                time_range = month_format.format (start_date);
                break;
            case MINUTE:
                filename_time_pattern = "$Y$m$d_$H$M";
                time_range = date_format.format (start_date);
                break;
            case SECOND:
                filename_time_pattern = "$Y$m$d_$H$M$S";
                time_range = date_format.format (start_date);
                break;
            default:
                throw new FileNotFoundException ("ImagCDFFilename interval type not supported: " + interval.toString());
        }
        String filename_pattern = iaga_code.toLowerCase() + "_" + filename_time_pattern + "_" + publication_level.toString() + ".cdf";
        
        // build the URI and send commands to autoplot
        if (reset) message.addCommand("reset ()");
        for (int count=0; count<component_codes.length(); count++)
        {
            // create and send the command to display this element
            String component = component_codes.substring(count, count+1);
            String field_name = "field" + Integer.toString (count +3);
            String cdf_var_name = "GeomagneticField" + component;
            File file_pattern = new File (folder, filename_pattern);
            AutoplotURIBuilder uri_builder = new AutoplotURIBuilder(AutoplotURIBuilder.URIType.VAP_CDF_FILE, file_pattern, time_range);
            uri_builder.addCDFVarName(cdf_var_name);
            message.addCommand("plot (" + count + ",'" + uri_builder.getURI() + "')");

            // switch off all x-axis disolays except the bottom one
            String dom_plot_name = "dom.plots[" + count + "]";
            if (count < component_codes.length() -1)
            {
                message.addCommand(dom_plot_name + ".controller.plot.xaxis.setVisible (0)");
            }
        }

        String title = makeTitle (null, iaga_code, 0);
        message.addCommand("dom.plots[0].controller.setTitleAutomatically('" + title + "')");
        
        addSizeCommands (message, component_codes.length());
        sendMessage (message);
        
    }

    /** resize the plot elements - the way to do this was worked out emprically -
     * there may be a beter way! Using the layout buttons in autoplot gets the
     * same layout, but I haven't found the Jython interface to simulate the buttons
     * 
     * @param n_elements the number of plots in the autoplot window
     */
    private void addSizeCommands (APMessage message, int n_elements)
    throws IOException
    {
        String top_settings [], bottom_settings [];
        switch (n_elements)
        {
            case 3:
                top_settings = new String []    {"+2.0em",       "37.40%",       "72.50%"};
                bottom_settings = new String [] {"37.40%-1.0em", "72.50%-1.0em", "112.30%-4.2em"};
                break;
            case 4:
                top_settings = new String []    {"+2.0em",       "28.90%",       "55.80%",       "81.00%"};
                bottom_settings = new String [] {"28.90%-1.0em", "55.80%-1.0em", "81.00%-1.0em", "112.30%-4.2em"};
                break;
            default:
                top_settings = null;
                bottom_settings = null;
                break;
        }
        if (top_settings != null)
        {
            for (int count=0; count<top_settings.length; count++)
            {
                String dom_plot_name = "dom.plots[" + count + "]";
                message.addCommand(dom_plot_name + ".controller.row.top = '" + top_settings[count] + "'");
                message.addCommand (dom_plot_name + ".controller.row.bottom = '" + bottom_settings[count] + "'");
            }
        }
    }
    
    private String makeTitle (String obsy_name, String obsy_code, int sample_period)
    {
        String title = "Geomagnetic observatory data";
        if (obsy_name == null)
        {
            if (obsy_code != null) title += " for " + obsy_code;
        }
        else
        {
            if (obsy_code != null) title += " for " + obsy_name + " (" + obsy_code + ")";
            else title += " for" + obsy_name;
        }
        switch (sample_period)
        {
            case 1000: title += ", 1-second period"; break; 
            case 60000: title += ", 1-minute period"; break;
            case 3600000: title += " 1-hour period"; break;
            default:
                if (sample_period > 0)
                    title += Integer.toString (sample_period / 1000) + "s period";
                break;
        }
        return title;
    }
    
    private void sendMessage (APMessage message)
    {
        synchronized (ap_message_queue)
        {
            ap_message_queue.addMessage(message);
        }
    }
    
    
    /* --------------------------------------------------------------------------------------------------------
     * --------------------------------------------------------------------------------------------------------
     * ------------- Code below here is part of the thread that communicates with autoplot --------------------
     * --------------------------------------------------------------------------------------------------------
     * -------------------------------------------------------------------------------------------------------- */

    /** thread to manage comms with Autoplot */
    @Override
    public void run ()
    {
        while (! ap_thread_cancel)
        {
            // extract all messages from the message queue - there's no point in
            // displaying a file if we're going to immediately display another
            // file which will overwrite the display
            APMessage message = null;
            synchronized (ap_message_queue)
            {
                while (ap_message_queue.hasNext())
                    message = ap_message_queue.removeMessage();
            }
            
            // process this message
            if (message != null)
            {
                // extract and process commands from the message, storing any exceptions
                String command;
                while ((! ap_thread_cancel) &&
                        (command = message.removeCommand()) != null)
                {
                    try
                    {
                        sendCommand (command);
                    }
                    catch (IOException exception)
                    {
                        synchronized (ap_exception_queue)
                        {
                            ap_exception_queue.addException (exception);
                        }
                    }
                }
            }
            
            // sleep for a bit before checking the queue again
            if (message == null)
            {
                try { Thread.sleep (100l); }
                catch (InterruptedException e) { }
            }
        }
    }
    
    /** send a command to autoplot
     * 
     * @param command the command, in Jython
     * @throws java.io.IOException if there's an IO error*/
    private String sendCommand (String command) 
    throws IOException
    {
        if (debug_comms) System.out.println ("Autoplot: sending data [" + command + "]");
        tx (command);
        tx ("\n");
        String rx_data = rx();
        if (debug_comms) System.out.println ("Autoplot: received data [" + command.replace("\n", "").replace("\r", "") + "]");
        return rx_data;
    }

    // send a command to autoplot
    private void tx (String data) 
    throws IOException
    {
        stream_to_autoplot.write (data.getBytes());
    }

    // receive a response from autoplot
    private String rx ()
    throws IOException
    {
        int length, buffer, time_waiting_ms;
        String ret_val;
        StringBuffer response;
        
        response = new StringBuffer ();
        time_waiting_ms = 0;
        
        do
        {
            // get the next next byte from the autoplot connection
            length = stream_from_autoplot.available();
            if (length > 0) buffer = stream_from_autoplot.read();
            else buffer = -1;
          
            // if no byte was available, then sleep (to prevent hammering the CPU)
            if (buffer < 0)
            {
                try 
                {
                    Thread.sleep (50); 
                    time_waiting_ms += 50;
                } 
                catch (InterruptedException e) { }
            }
            else
            {
                time_waiting_ms = 0;
                response.append((char) buffer);
                ret_val = response.toString();
                if (ret_val.endsWith(AUTOPLOT_PROMPT))
                    return ret_val.substring(0, response.length() - AUTOPLOT_PROMPT.length());
            }
        } while (time_waiting_ms < AUTOPLOT_RX_TIMEOUT);

        throw new IOException ("Timeout waiting for response from autoplot");
    }

    
}
