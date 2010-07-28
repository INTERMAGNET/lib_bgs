/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.StatusBar.DayChangeListener;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.ImagCD.ImagCDDataDay;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author smf
 */
public class ImagCDFileEditorPanel extends JPanel
implements StatusBar.DayChangeListener
{

    /** an exception to allow information on file load errors to be passed */
    public class FileLoadException extends Exception
    {
        public FileLoadException (String msg) { super (msg); }
    }
    
    /** a listener used to let a frame know when the title has changed */
    public interface TitleChangedListener
    {
        public void NewTitle (String title);
    }
    
    // private members - the data used in this window
    /** the currently loaded data file */
    private ImagCDFile data_file;
    /** flag to show whether file can be edited or not */
    private boolean editable;
    /** the title for the window */
    private String window_title;
    /** list of title changed listeners */
    public Vector<TitleChangedListener> title_changed_listeners;
     
    // private members - user interface components
    private HeaderPanel header_panel;
    private MinuteMeanPanel minute_mean_panel;
    private HourlyMeanPanel hourly_mean_panel;
    private DailyMeanPanel daily_mean_panel;
    private KIndexPanel k_index_panel;
    private TrailerPanel trailer_panel;
    private JTabbedPane tabbed_pane;
    private StatusBar status_bar;

    private final int MIN_WIDTH = 500;
    private final int MIN_HEIGHT = 250;

    /** create a file editor on an empty file */
    public ImagCDFileEditorPanel (boolean editable)
    {
        data_file = new ImagCDFile ();
        this.editable = editable;
        commonInitialisation ();
        if (editable) setWindowTitle ("Unnamed");
        else setWindowTitle ("Unnamed (read only)");
    }

    /** create a file editor for an existing file
     * @throws FileLoadException if there is an error load the file - this
     *         exception will always have a message */
    public ImagCDFileEditorPanel (File file, boolean editable)
    throws FileLoadException
    {
        data_file = new ImagCDFile ();
        this.editable = editable;
        commonInitialisation ();
        loadDataFromFile (file);
    }
    
    /** common initialisation called by all constructors */
    private void commonInitialisation ()
    {
        title_changed_listeners = new Vector<TitleChangedListener> ();
        
        // set the layout
        this.setLayout(new BorderLayout());
    
        // create the panels that go on the main window
        header_panel = new HeaderPanel (data_file, editable);
        minute_mean_panel = new MinuteMeanPanel (data_file, editable);
        hourly_mean_panel = new HourlyMeanPanel (data_file, editable);
        daily_mean_panel = new DailyMeanPanel (data_file, editable);
        k_index_panel= new KIndexPanel (data_file, editable);
        trailer_panel = new TrailerPanel (data_file, editable);

        // put the panels in a tabbed pane
        tabbed_pane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbed_pane.add("Header", header_panel);
        tabbed_pane.add("Minute means", minute_mean_panel);
        tabbed_pane.add("Hourly means", hourly_mean_panel);
        tabbed_pane.add("Daily means", daily_mean_panel);
        tabbed_pane.add("K indices", k_index_panel);
        tabbed_pane.add("Trailer", trailer_panel);
        this.add(tabbed_pane,BorderLayout.CENTER);
    
        // add the status bar
        status_bar = new StatusBar (data_file);
        status_bar.addDayChangeListener(this);
        this.add (status_bar, BorderLayout.SOUTH);
    }
  
    public void addTitleChangedListener (TitleChangedListener listener)
    {
        title_changed_listeners.add (listener);
    }

    public void removeDayChangeListener (TitleChangedListener listener)
    {
        title_changed_listeners.remove (listener);
    }
    
    /** add data to a file window from a file
     * @param file the file to load data from
     * @throws FileLoadException if there is an error load the file - this
     *         exception will always have a message */
    public void loadDataFromFile (File file)
    throws FileLoadException
    {
        String string;
    
        string = data_file.loadFromFile(file);
        if (string != null) throw new FileLoadException (string);
        updateDataTables ();
        updateStatus ();
    }

    /** set the day of the month and the number of days displayed to the user */
    public void updateStatus ()
    {
        String read_only;
        
        if (editable) read_only = "";
        else read_only = " (read only)";
        status_bar.updateStatus(data_file);
        if (data_file.getOpenFile() == null) setWindowTitle ("Unnamed" + read_only);
        else setWindowTitle (data_file.getOpenFile().getName() + ", day " + data_file.getDayDisplayed() + " of " + data_file.getNDays() + read_only);
    }
  
    /** force the data tables to reload their data */
    public void updateDataTables ()
    {
        header_panel.updateTable ();
        minute_mean_panel.updateTable ();
        hourly_mean_panel.updateTable ();
        daily_mean_panel.updateTable ();
        k_index_panel.updateTable ();
        trailer_panel.updateTable ();
    }
  
    /** retrieve the data file associated with this window
     * @param the data file */
    public ImagCDFile getDataFile ()
    {
       return data_file;
    }
    
    /** get the suggested window title */
    public String getWindowTitle () { return window_title; }
    
    /** set the suggested window title */
    public void setWindowTitle (String title)
    {
        int count;
        TitleChangedListener listener;
        
        window_title = title;
        for (count=0; count<title_changed_listeners.size(); count++)
        {
            listener = title_changed_listeners.get (count);
            listener.NewTitle(window_title);
        }
    }

    public void changeDayCommand(StatusBar.DayChangeEvent evt) 
    {
        switch (evt.getCommand())
        {
            case FIRST_DAY: data_file.setDayDisplayedToFirst(); break;
            case PREVIOUS_DAY: data_file.decDayDisplayed(); break;
            case NEXT_DAY: data_file.incDayDisplayed(); break;
            case LAST_DAY: data_file.setDayDisplayedToLast(); break;
        }
        updateDataTables();
        updateStatus();
    }
}
