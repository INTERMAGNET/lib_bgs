/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDataDay;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDException;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.GeomagDataException;
import bgs.geophys.library.Data.GeomagDataFilename;
import bgs.geophys.library.Data.GeomagDataFilename.Case;
import bgs.geophys.library.Data.IMFV122;
import bgs.geophys.library.Data.IMFV122Filename;
import bgs.geophys.library.Data.Iaga2002;
import bgs.geophys.library.Data.Iaga2002Filename;
import bgs.geophys.library.Data.ImagCD.DKAFile;
import bgs.geophys.library.Data.ImagCD.DKAFilename;
import bgs.geophys.library.Data.ImagCD.ImagCDDataDay;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import bgs.geophys.library.Data.ImagCD.ImagCDFileStats;
import bgs.geophys.library.Data.ImagCDFilename;
import bgs.geophys.library.File.FileUtils;
import bgs.geophys.library.File.ZipFileMaker;
import bgs.geophys.library.Misc.DateUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Class to handle import of data in a background thread. The import occurs
 * in three phases - each phase is run in a new thread:
 * 
 * phase 1 - find all relevant files
 * phase 2 - read data into new files in a temporary directory
 * phase 3 - copy newly formatted data to a data source directory
 * 
 * @author smf
 */
public class ImportThread
implements Runnable
{

    /** an enumeration defining the types of files we are interested in */
    public enum FileFormat { UNKNOWN, CD, IMF, IAGA_2002, K_INDEX }
    
    /* an extension to the File object that includes the file type
     * and a flag to show whether the file is in use or not */
    public class GeomagFile extends File
    {
        private FileFormat type;
        private boolean in_use;
        public GeomagFile (String name, FileFormat type, boolean in_use)
        {
            super (name);
            this.type = type;
            this.in_use = in_use;
        }
        public GeomagFile (String parent, String child, FileFormat type, boolean in_use)
        {
            super (parent, child);
            this.type = type;
            this.in_use = in_use;
        }
        public GeomagFile (File parent, String child, FileFormat type, boolean in_use)
        {
            super (parent, child);
            this.type = type;
            this.in_use = in_use;
        }
        public FileFormat getType() { return type; }
        public String getTypeString() { return ImportThread.toString(type); }
        public void setType(FileFormat type) { this.type = type; }
        public void setType(String type) { this.type = ImportThread.parseFileFormat(type); }
        public boolean isInUse() { return in_use; }
        public void setInUse(boolean in_use) { this.in_use = in_use; }
        public int compareTo (GeomagFile o)
        {
            // make sure K-index files come last, so that they
            // are converted after IAGA and IMF data
            switch (type)
            {
                case CD:
                    if (o.type != FileFormat.CD) return -1;
                    break;
                case IAGA_2002:
                    if (o.type == FileFormat.CD) return +1;
                    if (o.type != FileFormat.IAGA_2002) return -1;
                    break;
                case IMF:
                    if (o.type == FileFormat.CD || o.type == FileFormat.IAGA_2002) return +1;
                    if (o.type != FileFormat.IMF) return -1;
                    break;
                case K_INDEX:
                    if (o.type == FileFormat.CD || o.type == FileFormat.IAGA_2002 || o.type == FileFormat.IMF) return +1;
                    if (o.type != FileFormat.K_INDEX) return -1;
                    break;
                case UNKNOWN:
                    if (o.type != FileFormat.UNKNOWN) return -1;
                    break;
            }
            return super.compareTo (o);
        }
    }
    
    /** a class to bring together information on a CD file */
    public class CDInfo
    {
        private ImagCDFilename cd_filename;
        private File dir_for_cd_file;
        private ImagCDFileStats cd_file_stats;
        public CDInfo (ImagCDFilename cd_filename, File dir_for_cd_file)
        {
            this.cd_filename = cd_filename;
            this.dir_for_cd_file = dir_for_cd_file;
            this.cd_file_stats = null;
        }
        public void calcCDFileStats (ImagCDFile cd_file)
        {
            if (cd_file_stats == null) cd_file_stats = new ImagCDFileStats (cd_file);
            else cd_file_stats.calcCDFileStats(cd_file);
        }
        public boolean isError ()
        {
            if (cd_file_stats == null) return false;
            if (cd_file_stats.isDataError()) return true;
            if (cd_file_stats.isHeaderError()) return true;
            return false;
        }
        public ImagCDFilename getCDFilename() {  return cd_filename; }
        public ImagCDFileStats getCDFileStats() { return cd_file_stats; }
        public File getDirForCDFile() { return dir_for_cd_file; }
        public File getCDFile () { return new File (dir_for_cd_file, cd_filename.getFilename()); }
        @Override
        public String toString ()
        {
            String string;
            string = cd_filename.getFilename();
            if (cd_file_stats != null)
            {
                if (cd_file_stats.isDataError() || cd_file_stats.isHeaderError())
                    string += " (has errors)";
            }
            return string;
        }
    }
    
    /** class to describe what happens to an individual file
     * when attempting to install it */
    public class InstallResult
    {
        private String filename;
        private String details;
        private boolean error;
        public InstallResult (String filename, String details, boolean error)
        {
            this.filename = filename;
            this.details = details;
            this.error = error;
        }
        public String getFilename () { return filename; }
        public String getDetails () { return details; }
        public boolean isError () { return error; }
    }
    
    // members that provide general control of the background thread
    private int phase;
    private Vector<String> message_queue;
    private Thread thread;
    private boolean abort_flag;
    private GeomagDataFilename.Case cd_filename_case;

    // members that control phase 1 - file search
    private File start_dir;
    private boolean include_sub_dirs;
    private boolean use_cd_format;
    private boolean use_imf_format;
    private boolean use_iaga_2002_format;
    private boolean use_k_index_format;
    private int files_found;
    private boolean phase_1_successful;
    private Vector<GeomagFile> geomag_file_list;
    
    // members that control phase 2 - file conversion
    private File tmp_dir;
    private boolean phase_2_successful;
    private int phase_2_percent_complete;
    private Vector<String> conversion_summary;
    private Vector<CDInfo> imag_cd_file_list;
    private boolean calc_hourly_means;
    private boolean calc_daily_means;
    private int max_missing_pc;
    private ImagCDFile current_cd_file;
    private GregorianCalendar phase2and3_cal;
        
    // members that control phase 3 - file installation
    private int phase_3_percent_complete;
    private File install_dest_dir;
    private boolean install_overwrite_files;
    private boolean install_compress_files;
    private boolean mount_data_source;
    private Vector<InstallResult> installation_results;
    
    
    public ImportThread ()
    {
        int count;
        
        phase = 0;
        thread = null;
        abort_flag = false;
        phase_1_successful = false;
        phase_2_successful = false;
        phase2and3_cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        message_queue = new Vector<String> ();
        geomag_file_list = new Vector<GeomagFile> ();
        for (count=0; count<1000000; count++)
        {
            tmp_dir = new File (System.getProperty ("java.io.tmpdir", ".") + File.separator + "imcdview_tmp_" + Integer.toString (count));
            if (! tmp_dir.exists()) break;
        }
        conversion_summary = new Vector<String> ();
        imag_cd_file_list = new Vector<CDInfo> ();
        installation_results = new Vector<InstallResult> ();
        cd_filename_case = GeomagDataFilename.Case.LOWER;
    }
    
    /** call after the thread is finished to cleanup resources */
    public void cleanpUp ()
    {
        // remove the temporary directory
        recursive_delete (tmp_dir, true);
    }

    /** call to abort any background task */
    public void abort () { abort_flag = true; }

    /** call to start background task that scans the user's directory */
    public void startPhase1 (File start_dir, boolean include_sub_dirs,
                             boolean use_cd_format, boolean use_imf_format,
                             boolean use_iaga_2002_format, boolean use_k_index_format)
    {
        this.abort_flag = false;
        this.phase = 1;
        this.phase_1_successful = false;
        this.files_found = 0;
        this.start_dir = start_dir;
        this.include_sub_dirs = include_sub_dirs;
        this.use_cd_format = use_cd_format;
        this.use_imf_format = use_imf_format;
        this.use_iaga_2002_format = use_iaga_2002_format;
        this.use_k_index_format = use_k_index_format;
        geomag_file_list.removeAllElements();
        thread = new Thread (this, "ImportThreadPhase" + Integer.toString (phase));
        thread.start();
    }

    /** call to start background task that converts data */
    public void startPhase2 (boolean calc_hourly_means, boolean calc_daily_means,
                             int max_missing_pc)
    {
        this.abort_flag = false;
        this.phase = 2;
        this.phase_2_successful = false;
        this.phase_2_percent_complete = 0;
        this.calc_daily_means = calc_daily_means;
        this.calc_hourly_means = calc_hourly_means;
        this.max_missing_pc = max_missing_pc;
        conversion_summary.removeAllElements();
        imag_cd_file_list.removeAllElements();
        thread = new Thread (this, "ImportThreadPhase" + Integer.toString (phase));
        thread.start();
    }

    /** call to start background task that installs data */
    public void startPhase3 (File install_dest_dir,
                             boolean install_overwrite_files, 
                             boolean install_compress_files,
                             boolean mount_data_source)
    {
        this.abort_flag = false;
        this.phase = 3;
        this.phase_2_percent_complete = 0;
        this.install_dest_dir = install_dest_dir;
        this.install_overwrite_files = install_overwrite_files;
        this.install_compress_files = install_compress_files;
        this.mount_data_source = mount_data_source;
        installation_results.removeAllElements();;
        thread = new Thread (this, "ImportThreadPhase" + Integer.toString (phase));
        thread.start();
    }
    
    public boolean isPhase1Successful () { return phase_1_successful; }
    public boolean isPhase2Successful () { return phase_2_successful; }
    public File getTmpDir () { return tmp_dir; }
    public int getPhase2PercentComplete () { return phase_2_percent_complete; }
    public int getPhase3PercentComplete () { return phase_3_percent_complete; }
    public int getPhase () { return phase; }
    
    
    /** find out if the background thread is still running */
    public boolean isThreadAlive ()
    {
        if (thread == null) return false;
        else if (! thread.isAlive()) return false;
        return true;
    }
    
    /** find out if the background thread was aborted */
    public boolean isAborted () { return abort_flag; }
    
    /** get the next message from the background thread - this
     * method is desgined to allow the background thread to pass
     * 1-line progress messages back to the foreground thread
     * @return the message or null if the message queue is empty */
    public String getMessageFromThread()
    {
        String ret_val;
        
        synchronized (message_queue)
        {
            if (message_queue.size() > 0) ret_val = message_queue.remove(0);
            else ret_val = null;
        }
        return ret_val;
    }
    
    /** get the number of files found in the search directory during phase 1 -
     * don't call this method until phase 1 thread has died, it is not thread safe */
    public int getNGeomagFiles () { return geomag_file_list.size(); }
    
    /** get a file found in the search directory during phase 1 -
     * don't call this method until phase 1 thread has died, it is not thread safe */
    public GeomagFile getGeomagFile (int index) { return geomag_file_list.get (index); }

    /** get summary information on the conversion performed in phase 2 -
     * don't call this method until phase 2 thread has died, it is not thread safe */
    public int getNConversionSummaryLines () { return conversion_summary.size(); }
    
    /** get summary information on the conversion performed in phase 2 -
     * don't call this method until phase 2 thread has died, it is not thread safe */
    public String getConversionSummaryLine (int index) { return conversion_summary.get (index); }
    
    /** get the number of files created in the temporary directory during phase 2 -
     * don't call this method until phase 2 thread has died, it is not thread safe */
    public int getNImagCDFiles () { return imag_cd_file_list.size(); }
    
    /** get a file created in the temporary directory during phase 2 -
     * don't call this method until phase 2 thread has died, it is not thread safe */
    public CDInfo getImagCDFile (int index) 
    {
        return imag_cd_file_list.get (index); 
    }

    /** get the number of installation results from phase 3 */
    public int getNInstallResults () { return installation_results.size(); }
    
    /** get an installation result from phase 3 */
    public InstallResult getInstallResult (int index) { return installation_results.get (index); }
    
    /** get the FileFormat enumeration as a string */
    public static String toString (FileFormat file_type)
    {
        switch (file_type)
        {
            case CD: return "INTERMAGNET CD format";
            case IAGA_2002: return "IAGA 2002 format";
            case IMF: return "INTERMAGNET minute mean format";
            case K_INDEX: return "K index file";
        }
        return "Unknown format";
    }
    
    /** get the FileFormat enumeration as a string */
    public static FileFormat parseFileFormat (String type)
    {
        if (type.equalsIgnoreCase(toString(FileFormat.CD)))
            return FileFormat.CD;
        if (type.equalsIgnoreCase(toString(FileFormat.IAGA_2002)))
            return FileFormat.IAGA_2002;
        if (type.equalsIgnoreCase(toString(FileFormat.K_INDEX)))
            return FileFormat.K_INDEX;
        if (type.equalsIgnoreCase(toString(FileFormat.IMF)))
            return FileFormat.IMF;
        return FileFormat.UNKNOWN;
    }

    
    /////////////////////////////////////////////////////////////////////
    // code below here is run in the background
    /////////////////////////////////////////////////////////////////////
    
    public void run() 
    {
        switch (phase)
        {
            case 1: runPhase1 (); break;
            case 2: runPhase2 (); break;
            case 3: runPhase3 (); break;
        }
    }

    private boolean recursive_delete (File dir, boolean delete_parent)
    {
        int count;
        File files[];

        if (dir == null) return true;
        files = dir.listFiles();
        if (files == null) return true;
        for (count=0; count<files.length; count++)
        {
            if (files[count].isDirectory())
            {
                if (! recursive_delete (files[count], true)) return false;
            }
            else
            {
                if (! files[count].delete()) return false;
            }
        }      
        if (delete_parent)
        {
            if (! dir.delete()) return false;
        }
        return true;
    }
    
    private void sendMessageFromThread(String string)
    {
        synchronized (message_queue)
        {
            message_queue.add (string);
        }
    }

    
    /////////////////////////////////////////////////////////////////////
    // code below here is run in the background during phase 1
    /////////////////////////////////////////////////////////////////////

    private void runPhase1 ()
    {
        File files [];
        
        files = start_dir.listFiles();
        if (files == null) 
        {
            sendMessageFromThread("Search file is not a directory: " + start_dir.getAbsolutePath());
            return;
        }
        if (files.length == 0) 
        {
            sendMessageFromThread("Search directory is empty: " + start_dir.getAbsolutePath());
            return;
        }
        sendMessageFromThread ("Listing contents of directory " + start_dir.getAbsolutePath() + " ...");
        parseDirectory (files);
        if (geomag_file_list.size() <= 0)
        {
            sendMessageFromThread ("Directory listing complete, no files found");
            return;
        }
        sendMessageFromThread ("Sorting listing ...");
        Collections.sort (geomag_file_list);
        sendMessageFromThread ("Directory listing complete, " + Integer.toString (files_found) + " files scanned");
        phase_1_successful = true;
    }
    
    private void parseDirectory (File directory_contents [])
    {
        int count;
        File files [];
        String name;
        GeomagFile geomag_file;

        for (count=0; (count<directory_contents.length) && (! abort_flag); count++)
        {
            files_found ++;
            if (((files_found % 100) == 0) && files_found > 0)
                sendMessageFromThread ("Listing directory contents, " + Integer.toString (files_found) + " files found ...");
            if (directory_contents[count].isFile())
            {
                name = directory_contents[count].getName();
                try
                {
                    new Iaga2002Filename (name);
                    geomag_file = new GeomagFile (directory_contents[count].getAbsolutePath(), ImportThread.FileFormat.IAGA_2002, use_iaga_2002_format);
                }
                catch (ParseException e)
                {
                    try
                    {
                        new IMFV122Filename (name);
                        geomag_file = new GeomagFile (directory_contents[count].getAbsolutePath(), ImportThread.FileFormat.IMF, use_imf_format);
                    }
                    catch (ParseException e2)
                    {
                        try
                        {
                            new ImagCDFilename (name);
                            geomag_file = new GeomagFile (directory_contents[count].getAbsolutePath(), ImportThread.FileFormat.CD, use_cd_format);
                        }
                        catch (ParseException e3)
                        {
                            try
                            {
                                new DKAFilename (name);
                                geomag_file = new GeomagFile (directory_contents[count].getAbsolutePath(), ImportThread.FileFormat.K_INDEX, use_k_index_format);
                            }
                            catch (ParseException e4)
                            {
                                geomag_file = new GeomagFile (directory_contents[count].getAbsolutePath(), ImportThread.FileFormat.UNKNOWN, false);
                            }
                        }
                    }
                }
                geomag_file_list.add (geomag_file);
            }
            else if (directory_contents[count].isDirectory())
            {
                if (include_sub_dirs && (! abort_flag))
                {
                    files = directory_contents[count].listFiles();
                    parseDirectory(files);
                }
            }
        }
    }

    
    /////////////////////////////////////////////////////////////////////
    // code below here is run in the background during phase 2
    /////////////////////////////////////////////////////////////////////
    
    private void runPhase2 ()
    {
        int file_count, sample_count, n_in_use, n_converted;
        String errmsg;
        GeomagFile geomag_file;
        Iaga2002 iaga_2002;
        DKAFile k_index;
        DKAFile.KDay k_day;
        IMFV122 imfv122;
        FileInputStream input_stream;
        Date date;

        // initialise
        current_cd_file = null;
        
        // if needed create a temporary directory and check it is empty
        if (! tmp_dir.exists()) tmp_dir.mkdirs();
        if (! tmp_dir.isDirectory())
        {
            sendMessageFromThread("Unable to create temporary directory: " + tmp_dir.getAbsolutePath());
            phase_2_percent_complete = 100;
            return;
        }
        if (! recursive_delete (tmp_dir, false))
        {
            sendMessageFromThread("Unable to delete contents of temporary directory: " + tmp_dir.getAbsolutePath());
            phase_2_percent_complete = 100;
            return;
        }      
        
        // count the number of files in use
        n_in_use = n_converted = 0;
        for (file_count=0; file_count<geomag_file_list.size(); file_count++)
        {
            geomag_file = geomag_file_list.get(file_count);
            if (geomag_file.isInUse()) n_in_use ++;
        }

        // read the CD format files and copy them to the temporary directory
        sendMessageFromThread ("Copying CD format files ...");
        for (file_count=0; file_count<geomag_file_list.size(); file_count++)
        {
            geomag_file = geomag_file_list.get(file_count);
            if (geomag_file.isInUse() && geomag_file.getType() == FileFormat.CD)
            {
                n_converted ++;
                
                // check the format of the CD file
                current_cd_file = new ImagCDFile ();
                errmsg = current_cd_file.loadFromFile(geomag_file);
                if (errmsg == null)
                {
                    calcCurrentCDFileMeans();
                    errmsg = writeCurrentCDFile ();
                    if (errmsg != null)
                        addToConversionSummary("CD format file " + geomag_file.getAbsolutePath() + " could not be written: " + errmsg);
                }
                else
                    addToConversionSummary ("CD format file " + geomag_file.getAbsolutePath() + " could not be read: " + errmsg);
            }
            
            // set the percent complete
            if (n_in_use <= 0) phase_2_percent_complete = 100;
            else phase_2_percent_complete = (n_converted * 100) / n_in_use;
        }
        
        // convert the IAGA, IMF and K index files into CD files
        for (file_count=0; file_count<geomag_file_list.size(); file_count++)
        {
            geomag_file = geomag_file_list.get(file_count);
            iaga_2002 = null;
            imfv122 = null;
            k_index = null;
            input_stream = null;
            if (geomag_file.isInUse())
            {
                n_converted ++;
                
                // read the file
                try
                {
                    switch (geomag_file.getType())
                    {
                        case IAGA_2002:
                            input_stream = new FileInputStream (geomag_file);
                            iaga_2002 = Iaga2002.read (input_stream);
                            break;
                        case IMF:
                            input_stream = new FileInputStream (geomag_file);
                            imfv122 = IMFV122.read (new FileInputStream (geomag_file));
                            break;
                        case K_INDEX:
                            k_index = new DKAFile (geomag_file);
                            break;
                    }
                    if (iaga_2002 != null)
                    {
                        for (sample_count=0; sample_count<iaga_2002.getDataLength(); sample_count++)
                        {
                            date = iaga_2002.getDataSampleDate(sample_count);
                            errmsg = queryCloseCurrentCDFile (iaga_2002.getStationCode(), date);
                            if (errmsg != null)
                                addToConversionSummary ("CD format file could not be written: " + errmsg);
                            else
                            {
                                errmsg = setupCurrentCDFile (iaga_2002.getStationCode(),
                                                             90.0 - iaga_2002.getLatitude(),
                                                             iaga_2002.getLongitude(),
                                                             iaga_2002.getElevation(),
                                                             iaga_2002.getComponentCodes(),
                                                             iaga_2002.getInstituteName(),
                                                             iaga_2002.getSensorOrientation(),
                                                             iaga_2002.getInstrumentSamplingPeriod(),
                                                             date);
                                if (errmsg != null)
                                    addToConversionSummary ("CD format file could not be read: " + errmsg);
                                else
                                {
                                    errmsg = addSampleToCurrentCDFile (date, iaga_2002.getSamplePeriod(), 
                                                                       iaga_2002.getComponentCodes(),
                                                                       iaga_2002.getData(0, sample_count),
                                                                       iaga_2002.getData(1, sample_count),
                                                                       iaga_2002.getData(2, sample_count),
                                                                       iaga_2002.getData(3, sample_count),
                                                                       Iaga2002.MISSING_DATA_SAMPLE,
                                                                       Iaga2002.MISSING_COMPONENT);
                                    if (errmsg != null)
                                        addToConversionSummary ("CD format file could not be read: " + errmsg);
                                }
                            }
                        }
                    }
                    if (imfv122 != null)
                    {
                        for (sample_count=0; sample_count<imfv122.getDataLength(); sample_count++)
                        {
                            date = imfv122.getDataSampleDate(sample_count);
                            errmsg = queryCloseCurrentCDFile (imfv122.getStationCode(), date);
                            if (errmsg != null)
                                addToConversionSummary ("CD format file could not be written: " + errmsg);
                            else
                            {
                                errmsg = setupCurrentCDFile (imfv122.getStationCode(),
                                                             0.0, 0.0, 0.0,
                                                             imfv122.getComponentCodes(),
                                                             "", "", 0l, date);
                                if (errmsg != null)
                                    addToConversionSummary ("CD format file could not be read: " + errmsg);
                                else
                                {
                                    errmsg = addSampleToCurrentCDFile(date, 60000l, 
                                                                      imfv122.getComponentCodes(),
                                                                      imfv122.getData(0, sample_count),
                                                                      imfv122.getData(1, sample_count),
                                                                      imfv122.getData(2, sample_count),
                                                                      imfv122.getData(3, sample_count),
                                                                      IMFV122.MISSING_DATA_SAMPLE,
                                                                      IMFV122.MISSING_COMPONENT);
                                    if (errmsg != null)
                                        addToConversionSummary ("CD format file could not be read: " + errmsg);
                                }
                            }
                        }
                    }
                    if (k_index != null)
                    {
                        for (sample_count=0; sample_count<k_index.getNDays(); sample_count++)
                        {
                            k_day = k_index.getDay(sample_count);
                            date = k_day.getDate();
                            errmsg = queryCloseCurrentCDFile (k_index.getStationCode(), date);
                            if (errmsg != null)
                                addToConversionSummary ("CD format file could not be written: " + errmsg);
                            else
                            {
                                errmsg = setupCurrentCDFile (k_index.getStationCode(),
                                                             0.0, 0.0, 0.0, "",
                                                             "", "", 0l, date);
                                if (errmsg != null)
                                    addToConversionSummary ("CD format file could not be read: " + errmsg);
                                else
                                    addKDayToCurrentCDFile(date, k_day);
                            }
                        }
                    }
                }
                catch (FileNotFoundException e)
                {
                    addToConversionSummary ("File not found: " + geomag_file.getAbsolutePath());
                }
                catch (GeomagDataException e)
                {
                    addToConversionSummary ("Error reading " + geomag_file.getName() + ": " + e.getMessage());
                }
                catch (IOException e)
                {
                    addToConversionSummary ("Error reading " + geomag_file.getName() + ": " + e.getMessage());
                }
                catch (ParseException e)
                {
                    addToConversionSummary ("Error reading " + geomag_file.getName() + ": " + e.getMessage());
                }
                if (input_stream != null) 
                {
                    try { input_stream.close(); }
                    catch (IOException e) { }
                }
            }
            
            // set the percent complete
            if (n_in_use <= 0) phase_2_percent_complete = 100;
            else phase_2_percent_complete = (n_converted * 100) / n_in_use;
        }
        
        // write out the last file
        errmsg = queryCloseCurrentCDFile ("Unknown", new Date ());
        if (errmsg != null)
            addToConversionSummary ("CD format file could not be written: " + errmsg);
        
        // check that some files were converted
        if (imag_cd_file_list.size() <= 0)
        {
            sendMessageFromThread ("File format conversion complete, no files converted");
            phase_2_percent_complete = 100;
            return;
        }
        
        // flag completion
        sendMessageFromThread ("File format conversion complete, " + imag_cd_file_list.size () + " files converted");
        phase_2_successful = true;
        phase_2_percent_complete = 100;
    }

    private String queryCloseCurrentCDFile (String station_code, Date date)
    {
        String errmsg;
        
        // are we using the same file as last time ??
        if (current_cd_file == null) 
            return null;
        else if ((date.getTime() >= current_cd_file.getStartDate().getTime()) &&
                 (date.getTime() < current_cd_file.getEndDate().getTime()) &&
                  station_code.equalsIgnoreCase(current_cd_file.getDataDay(1).getStationID().trim()))
            return null;

        // we need a different file, so write this one and close it
        calcCurrentCDFileMeans();
        errmsg = writeCurrentCDFile ();
        current_cd_file = null;
        return errmsg;
    }
    
    private String setupCurrentCDFile (String station_code, double colatitude,
                                       double longitude, double elevation,
                                       String component_codes, String intitute,
                                       String sensor_orient, long sample_period,
                                       Date date)
    {
        int count;
        ImagCDFilename cd_filename;
        CDInfo cd_info;
        
        // only do this if there is no current file - assumes that
        // queryCloseCurrentCDFile() has been called first
        if (current_cd_file != null) return null;

        // look through the list of files to see if this file has already been used
        cd_filename = null;
        for (count=0; count<imag_cd_file_list.size(); count++)
        {
            cd_info = imag_cd_file_list.get(count);
            cd_filename = cd_info.getCDFilename();
            phase2and3_cal.setTime(cd_filename.getDate());
            phase2and3_cal.add (GregorianCalendar.MONTH, 1);
            if ((date.getTime() >= cd_filename.getDate().getTime()) &&
                (date.getTime() < phase2and3_cal.getTime().getTime()) &&
                station_code.equalsIgnoreCase(cd_filename.getObservatoryCode()))
                break;
            cd_filename = null;
        }
            
        // have we already created this file ??
        if (cd_filename != null)
        {
            // yes - attempt to open it
            current_cd_file = new ImagCDFile ();
            return current_cd_file.loadFromFile(new File (tmp_dir, cd_filename.getFilename()));
        }
        
        // no file could be found - create a new one
        phase2and3_cal.setTime (date);
        current_cd_file = new ImagCDFile (phase2and3_cal.get (GregorianCalendar.YEAR), phase2and3_cal.get (GregorianCalendar.MONTH));
        current_cd_file.setStationId(station_code);
        current_cd_file.setColatitude((int) ((colatitude * 1000.0) + 0.5));
        current_cd_file.setLongitude((int) ((longitude * 1000.0) + 0.5));
        current_cd_file.setElevation((int) (elevation + 0.5));
        current_cd_file.setRecordedElements(component_codes);
        current_cd_file.setInstituteCode(intitute);
        current_cd_file.setDConversion(0);
        current_cd_file.setQualityCode("IMAG");
        current_cd_file.setInstrumentCode("");
        current_cd_file.setK9Limit(0);
        current_cd_file.setSamplePeriod((int) (sample_period / 1000l));
        current_cd_file.setSensorOrientation (sensor_orient);
        current_cd_file.setPublicationDate (new Date ());
        current_cd_file.setFormatVersion ("2.0");
        return null;
    }
    
    private String addSampleToCurrentCDFile (Date date, long sample_period, String component_codes,
                                             double c1, double c2, double c3, double c4,
                                             double miss_samp, double miss_comp)
    {
        int minute, hour, cd1, cd2, cd3, cd4;
        ImagCDDataDay cd_day;
        
        // check a file is available
        if (current_cd_file == null) return null;

        // get the day that the data relates to
        phase2and3_cal.setTime(date);
        cd_day = current_cd_file.getDataDay(phase2and3_cal.get(GregorianCalendar.DAY_OF_MONTH));
        
        // check for correct component code
        if (! component_codes.equalsIgnoreCase(cd_day.getRecordedElements().trim()))
            return "Attempt to insert data with incorrect elements (" + component_codes + " should be " + cd_day.getRecordedElements().trim() + ")";

        // convert the data
        if (c1 == miss_comp || c1 == miss_samp) cd1 = ImagCDDataDay.MISSING_DATA;
        else cd1 = (int) Math.round (c1 * 10.0);
        if (c2 == miss_comp || c2 == miss_samp) cd2 = ImagCDDataDay.MISSING_DATA;
        else cd2 = (int) Math.round (c2 * 10.0);
        if (c3 == miss_comp || c3 == miss_samp) cd3 = ImagCDDataDay.MISSING_DATA;
        else cd3 = (int) Math.round (c3 * 10.0);
        if (c4 == miss_comp || c4 == miss_samp) cd4 = ImagCDDataDay.MISSING_DATA;
        else cd4 = (int) Math.round (c4 * 10.0);
        
        // insert the data
        switch ((int) sample_period)
        {
            case 60000:
                minute = (phase2and3_cal.get (GregorianCalendar.HOUR_OF_DAY) * 60) + phase2and3_cal.get (GregorianCalendar.MINUTE);
                cd_day.setMinuteData(cd1, 0, minute);
                cd_day.setMinuteData(cd2, 1, minute);
                cd_day.setMinuteData(cd3, 2, minute);
                cd_day.setMinuteData(cd4, 3, minute);
                break;
            case 3600000:
                hour = phase2and3_cal.get (GregorianCalendar.HOUR_OF_DAY);
                cd_day.setHourData(cd1, 0, hour);
                cd_day.setHourData(cd2, 1, hour);
                cd_day.setHourData(cd3, 2, hour);
                cd_day.setHourData(cd4, 3, hour);
                break;
            case 86400000:
                cd_day.setDayData(cd1, 0);
                cd_day.setDayData(cd2, 1);
                cd_day.setDayData(cd3, 2);
                cd_day.setDayData(cd4, 3);
                break;
        }
        
        return null;
    }

    private void addKDayToCurrentCDFile (Date date, DKAFile.KDay k_day)
    {
        int count, value;
        ImagCDDataDay cd_day;
        GregorianCalendar cal;
        
        // check a file is available
        if (current_cd_file == null) return;

        // insert the data
        cal = new GregorianCalendar (DateUtils.gmtTimeZone);
        cal.setTime(date);
        cd_day = current_cd_file.getDataDay(cal.get(GregorianCalendar.DAY_OF_MONTH));
        for (count=0; count<k_day.getNIndices(); count++)
        {
            value = k_day.getIndex(count);
            if (value < 0 || value > 9) value = ImagCDDataDay.MISSING_K_INDEX;
            // note that K index values in CD-ROM format are multiplied by 10
            cd_day.setKIndexData (value * 10, count);
        }
    }
    
    private String writeCurrentCDFile ()
    {
        int count;
        CDInfo cd_info, cd_info2;
        ImagCDFilename cd_filename, list_name;
        File dest_file;
        String errmsg;
        boolean add_to_list, remove_from_list;
        
        errmsg = null;
        cd_filename = null;
        add_to_list = remove_from_list = false;
        if (current_cd_file != null)
        {
            try
            {
                cd_filename = new ImagCDFilename (current_cd_file.getDataDay(1).getStationID().trim(),
                                                  current_cd_file.getStartDate(), cd_filename_case);
                cd_info = new CDInfo (cd_filename, tmp_dir);
                dest_file = cd_info.getCDFile();
                current_cd_file.setFileChanged();
                errmsg = current_cd_file.writeToFile (dest_file);
            }
            catch (IllegalArgumentException e)
            {
                errmsg = "Unable to create file name";
                cd_info = null;
                dest_file = null;
            }
            if (errmsg == null)
            {
                cd_info.calcCDFileStats (current_cd_file);
                remove_from_list = true;
                add_to_list = true;
            }
            else
            {
                if (dest_file != null) dest_file.delete();
                remove_from_list = true;
            }
            
            // add / remove this file from the list of files ??
            for (count=0; count<imag_cd_file_list.size(); count++)
            {
                cd_info2 = imag_cd_file_list.get (count);
                list_name = cd_info2.getCDFilename();
                if (list_name.getFilename().equalsIgnoreCase(cd_filename.getFilename()))
                {
                    if (remove_from_list)
                    {
                        imag_cd_file_list.remove(count);
                        count --;
                    }
                }
            }
            if (add_to_list && (cd_info != null)) 
            {
                imag_cd_file_list.add (cd_info);
            }
            current_cd_file = null;
        }
        return errmsg;
    }
    
    private void calcCurrentCDFileMeans ()
    {
        current_cd_file.calculateHourlyMeans(false, max_missing_pc);
        current_cd_file.calculateDailyMeans(false, max_missing_pc);
    }
    
    private void addToConversionSummary (String line)
    {
        // limit the number of lines inthe summary
        switch (conversion_summary.size())
        {
            case 100:
                conversion_summary.add ("Too many errors, no further errors will be reported");
                break;
            case 101:
                break;
            default:
                conversion_summary.add(line);
                break;
        }
    }

    
    /////////////////////////////////////////////////////////////////////
    // code below here is run in the background during phase 3
    /////////////////////////////////////////////////////////////////////
    
    private void runPhase3 ()
    {
        int file_count;
        boolean do_copy;
        String errmsg, year_string;
        File year_dir, dir, dest_file, src_file;
        CDInfo cd_info;
        ImagCDFilename cd_filename, cmpr_filename;
        ZipFileMaker zip_file_maker;
        
        // create the destination directory
        if (! createDir (install_dest_dir)) return;
        
        // for each CD-ROM format file ...
        for (file_count=0; file_count<imag_cd_file_list.size(); file_count ++)
        {
            cd_info = imag_cd_file_list.get (file_count);
            cd_filename = cd_info.getCDFilename();
            phase2and3_cal.setTime(cd_filename.getDate());
            src_file = cd_info.getCDFile();
            year_string = Integer.toString (phase2and3_cal.get (GregorianCalendar.YEAR));
            errmsg = null;
            
            // create the year directory
            year_dir = new File (install_dest_dir, "MAG" + year_string);
            if (createDir (year_dir))
            {
                // create the ctry_inf and the appropriate yyyyMAPS directory
                dir = new File (year_dir, year_string + "MAPS");
                createDir (dir);
                dir = new File (year_dir, "CTRY_INF");
                createDir (dir);

                // create the data directory
                dir = new File (year_dir, cd_filename.getObservatoryCode().toLowerCase());
                if (createDir (dir))
                {
                    // are we compressing the file ??
                    if (install_compress_files)
                    {
                        // create the name of the destination file
                        cmpr_filename = new ImagCDFilename (cd_filename.getObservatoryCode(),
                                                            cd_filename.getDate(), true,
                                                            cd_filename_case);
                        dest_file = new File (dir, cmpr_filename.getFilename());
                
                        // does the destination already exist ??
                        if (dest_file.exists() && (! install_overwrite_files)) do_copy = false;
                        else do_copy = true;
        
                        // copy the file
                        if (do_copy)
                        {
                            try
                            {
                                zip_file_maker = new ZipFileMaker (dest_file);
                                zip_file_maker.add (src_file, src_file.getName());
                                zip_file_maker.close();
                            }
                            catch (FileNotFoundException e) { errmsg = "Unable to compress and copy file"; }
                            catch (IOException e) { errmsg = "Unable to compress and copy file"; }
                            if (errmsg != null) dest_file.delete();
                        }
                        else errmsg = "File already exists (and overwrite not allowed)";

                        // report what happened
                        if (errmsg == null) installation_results.add (new InstallResult (dest_file.getName(), "Compressed and copied OK", false));
                        else  installation_results.add (new InstallResult (src_file.getName(), errmsg, true));
                    }
                    else
                    {
                        // create the name of the destination file
                        dest_file = new File (dir, cd_filename.getFilename());
                
                        // does the destination already exist ??
                        if (dest_file.exists() && (! install_overwrite_files)) do_copy = false;
                        else do_copy = true;
        
                        // copy the file
                        if (do_copy)
                        {
                            try 
                            {
                                FileUtils.copy (src_file, dest_file);
                            }
                            catch (FileNotFoundException e) { errmsg = "Unable to copy file"; }
                            catch (SecurityException e) { errmsg = "Unable to copy file"; }
                            catch (IOException e) { errmsg = "Unable to copy file"; }
                            if (errmsg != null) dest_file.delete();
                        }
                        else errmsg = "File already exists (and overwrite not allowed)";

                        // report what happened
                        if (errmsg == null) installation_results.add (new InstallResult (src_file.getName(), "Copied OK", false));
                        else installation_results.add (new InstallResult (src_file.getName(), errmsg, true));
                    }
                }
            }
            
            // set the percent complete
            phase_3_percent_complete = (file_count * 100) / imag_cd_file_list.size();
        }
        
        // do we want to mount the data source?
        if (mount_data_source)
        {
            try
            {
                GlobalObjects.database_list.AddDatabase (install_dest_dir);
                installation_results.add (new InstallResult (install_dest_dir.getAbsolutePath(), "Added as data source to main program.", false));
            }
            catch (CDException e)
            {
                installation_results.add (new InstallResult (install_dest_dir.getAbsolutePath(), "Unable to add to data sources in main program.", true));
            }
        }
        GlobalObjects.command_interpreter.interpretCommand("reload_data_from_source");
        
        phase_3_percent_complete = 100;
    }

    private boolean createDir (File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory()) return true;
            installation_results.add (new InstallResult (dir.getAbsolutePath(), "File already exists and is not a directory - delete and try again.", true));
            return false;
        }
        if (dir.mkdirs()) 
        {
            installation_results.add (new InstallResult (dir.getAbsolutePath(), "Directory created", false));
            return true;
        }
        installation_results.add (new InstallResult (dir.getAbsolutePath(), "Unable to create directory.", true));
        return false;
    }
    
}
