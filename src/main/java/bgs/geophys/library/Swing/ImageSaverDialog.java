/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Class to allowthe user to save images in a chosen format to a chosen
 * location.
 * 
 * @author smf
 */
public class ImageSaverDialog 
implements FocusListener, ItemListener
{

    /** a class that holds the predefined image / paper sizes */
    private class ImageSize 
    {
        private String description;
        private String x_size;
        private String y_size;
        
        public ImageSize (String description, String x_size, String y_size)
        {
            this.description = description;
            this.x_size = x_size;
            this.y_size = y_size;
        }
        
        public String toString () { return description; };
        public String getXSize () { return x_size; }
        public String getYSize () { return y_size; }
    }
    
    /** a class that holds details on a file format */
    private class FileFormat
    {
        private static final String JPEG_NAME = "jpg";
        private String name;
        private FileNameExtensionFilter filter;
        private boolean is_vector;
        
        public FileFormat (String name, boolean is_vector)
        {
            // special cases
            if (name.equalsIgnoreCase("jpg") || name.equalsIgnoreCase("jpeg"))
            {
                this.name = JPEG_NAME;
                filter = new FileNameExtensionFilter ("JPEG file", "jpg", "jpeg");
            }
            else
            {
                // general case
                this.name = name;
                filter = new FileNameExtensionFilter (name.toUpperCase() + " file", name.toLowerCase());
            }
            this.is_vector = is_vector;
        }
        
        public String getName () { return name; }
        public FileNameExtensionFilter getFilter () { return filter; }
        public boolean isVector () { return is_vector; }
        public boolean equals (String test_name)
        {
            if (name.equalsIgnoreCase(JPEG_NAME))
            {
                if (test_name == null) return false;
                if (test_name.equalsIgnoreCase("jpg")) return true;
                if (test_name.equalsIgnoreCase("jpeg")) return true;
                return false;
            }
            return name.equalsIgnoreCase(test_name);
        }
    }
    
    // private members - items in the file chooser
    private JFileChooser file_chooser;
    private JComboBox file_type_list;
    private ImageSize raster_size_list [];
    private ImageSize paper_size_list [];
    private JLabel image_size_label;
    private JTextField image_size_x;
    private JTextField image_size_y;
    private JComboBox image_size_list;
    private String last_image_size_x;
    private String last_image_size_y;

    // private members - configuration
    private String stored_dir;
    private String stored_raster_size_x;
    private String stored_raster_size_y;
    private String stored_paper_size_x;
    private String stored_paper_size_y;
    private String stored_file_type;
    private Vector<FileFormat> file_format_list;
    
    // private members - the things that were selected
    private File chosen_file;
    private FileFormat chosen_format;
        
    /** create an image save dialog
     * @param dir the initial directory where images will be saved
     * @param raster_size_x the initial width of raster images
     * @param raster_size_y the initial height of raster images
     * @param paper_size_x the initial width of vector images
     * @param paper_size_y the initial width of vector images
     * @param file_type the initial type of file to save */
    public ImageSaverDialog (String dir, String raster_size_x, String raster_size_y,
                             String paper_size_x, String paper_size_y, String file_type,
                             boolean include_pdf)
    {
        int count;
        Vector<String> names;
        FileFilter filter;
        JPanel image_size_panel, accessory_panel;
        BorderLayout layout;

        // get a list of image formats
        names = getImageWriterNames();
        file_format_list = new Vector<FileFormat> ();
        for (count=0; count<names.size(); count++)
        {
            file_format_list.add (new FileFormat (names.get(count), false));
        }
        if (include_pdf)
            file_format_list.add (new FileFormat ("pdf", true));
        
        // get stored configuration
        chosen_file = null;
        chosen_format = null;
        this.stored_dir = dir;
        this.stored_raster_size_x = raster_size_x;
        this.stored_raster_size_y = raster_size_y;
        this.stored_paper_size_x = paper_size_x;
        this.stored_paper_size_y = paper_size_y;
        this.stored_file_type = file_type;
        
        // find the current file format type
        if (file_format_list.size() <= 0)
            throw new UnsupportedOperationException ("Image Save Dialog: no image formats found");
        for (count=0; count<file_format_list.size(); count++)
        {
            chosen_format = file_format_list.get(count);
            if (chosen_format.getName().equalsIgnoreCase(stored_file_type))
                break;;
        }
        
        // set up lists of image and paper sizes
        raster_size_list = new ImageSize [] 
        {
            new ImageSize ( "640 x 480",  "640", "480"),
            new ImageSize ( "800 x 600",  "800", "600"),
            new ImageSize ("1024 x 768", "1024", "768")
        };
        paper_size_list = new ImageSize [] 
        {
            new ImageSize ("A3",     "42.0", "29.7"),
            new ImageSize ("A4",     "29.7", "21.0"),
            new ImageSize ("A5",     "21.0", "14.8"),
            new ImageSize ("Letter", "27.9", "21.6"),
            new ImageSize ("Legal",  "31.5", "21.6")
        };
        
        // create a panel with the image size components
        layout = new BorderLayout (3, 2);
        image_size_panel = new JPanel (layout);
        image_size_label = new JLabel ("");
        image_size_x = new JTextField (5);
        image_size_y = new JTextField (5);
        image_size_list = new JComboBox (raster_size_list);
        image_size_panel.add (image_size_label,    "North");
        image_size_panel.add (image_size_x,        "West");
        image_size_panel.add (new JLabel ("X"),    "Center");
        image_size_panel.add (image_size_y,        "East");
        image_size_panel.add (image_size_list,     "South");
        
        // configure the file chooser
        file_chooser = new JFileChooser (dir);
        filter = file_chooser.getAcceptAllFileFilter();
        if (filter != null) file_chooser.removeChoosableFileFilter(filter);
        for (count=0; count<file_format_list.size(); count++)
            file_chooser.addChoosableFileFilter(file_format_list.get(count).getFilter());
        configImageSize (chosen_format);
        file_chooser.setFileFilter (chosen_format.getFilter());
        
        // add the extra components - put them inside a panel (which uses
        // default flow layout) so that they are not expanded to fill the
        // available space
        accessory_panel = new JPanel ();
        accessory_panel.add (image_size_panel);
        file_chooser.setAccessory(accessory_panel);
        
        // set up event handling
        file_type_list = findFileTypeComboBox (file_chooser, file_format_list.get(0).getFilter());
        file_type_list.addItemListener (this);
        image_size_list.addItemListener (this);
        image_size_x.addFocusListener (this);
        image_size_y.addFocusListener (this);
    }
    
    /** run the file chooser
     * @param parent the parent component
     * @return JFileChooser.CANCEL_OPTION, JFileChooser.APPROVE_OPTION or
     *         JFileChooser.ERROR_OPTION */
    public int runModal (Component parent)
    {
        int chooser_option, confirm_option, count;
        String exts [], ext;
        FileNameExtensionFilter filter;
        File dir;
        boolean add_ext, repeat_dialog;
        
        do
        {
            repeat_dialog = false;
            chooser_option = file_chooser.showSaveDialog(parent);
            if (chooser_option == JFileChooser.APPROVE_OPTION)
            {
                // make sure the chosen file includes an appropriate extension
                filter = (FileNameExtensionFilter) file_chooser.getFileFilter();
                exts = filter.getExtensions();
                chosen_file = file_chooser.getSelectedFile();
                ext = getExtension(chosen_file);
                add_ext = true;
                for (count=0; count<exts.length; count++)
                {
                    if (exts[count].equals(ext)) add_ext = false;
                }
                if (add_ext)
                    chosen_file = new File (chosen_file.getAbsolutePath() + "." + exts [0]);
                    
                // find the file format
                chosen_format = findFileFormat(filter);
                
                // set the image size
                if (chosen_format.isVector())
                {
                    stored_paper_size_x = image_size_x.getText();
                    stored_paper_size_y = image_size_y.getText();
                }
                else
                {
                    stored_raster_size_x = image_size_x.getText();
                    stored_raster_size_y = image_size_y.getText();
                }
            
                // store the configuration
                dir = chosen_file.getParentFile();
                stored_dir = dir.getAbsolutePath();
                stored_file_type = chosen_format.getName();
                        
                // check if the file exists and if so whether the user wants to overwrite it
                if (chosen_file.exists())
                {
                     confirm_option = JOptionPane.showConfirmDialog (parent, 
                                                                     chosen_file.getName() + " exists, do you want to overwrite it?", 
                                                                     "File exists",
                                                                     JOptionPane.YES_NO_OPTION,
                                                                     JOptionPane.QUESTION_MESSAGE);
                     if (confirm_option == JOptionPane.NO_OPTION) repeat_dialog = true;
                }
            }
        } while (repeat_dialog);
        
        return chooser_option;
    }
    
    /** get the chosen file
     * @return the file that was chosen, or null if no file chosen */
    public File getChosenFile () { return chosen_file; }

    /** get the file type of the chosen file */
    public String getChosenFileType () { return chosen_format.getName(); }
    
    /** get the x size of the image / paper */
    public double getImageSizeX () 
    {
        try
        {
            if (chosen_format.isVector())
                return Double.parseDouble(getStoredPaperSizeX());
            return Double.parseDouble(getStoredRasterSizeX());
        }
        catch (NumberFormatException e) { }
        return 0.0;
    }
    
    /** get the y size of the image / paper */
    public double getImageSizeY () 
    {
        try
        {
            if (chosen_format.isVector())
                return Double.parseDouble(getStoredPaperSizeY());
            return Double.parseDouble(getStoredRasterSizeY());
        }
        catch (NumberFormatException e) { }
        return 0.0;
    }
    
    /** Get the extension of a file. */
    public static String getExtension(File f) 
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    /** use this for persistance */
    public String getStoredDir() { return stored_dir; }
    /** use this for persistance */
    public String getStoredRasterSizeX() { return stored_raster_size_x; }
    /** use this for persistance */
    public String getStoredRasterSizeY() { return stored_raster_size_y; }
    /** use this for persistance */
    public String getStoredPaperSizeX() { return stored_paper_size_x; }
    /** use this for persistance */
    public String getStoredPaperSizeY() { return stored_paper_size_y; }
    /** use this for persistance */
    public String getStoredFileType() { return stored_file_type; }
    
    public void focusGained(FocusEvent e) 
    {
        if (e.getSource().equals(image_size_x))
            last_image_size_x = image_size_x.getText();
        else if (e.getSource().equals(image_size_y))
            last_image_size_y = image_size_y.getText();
    }

    public void focusLost(FocusEvent e) 
    {
        if (e.getSource().equals(image_size_x))
        {
            try { Double.parseDouble (image_size_x.getText()); }
            catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog (file_chooser, "X size must be a real number", "Error", JOptionPane.ERROR_MESSAGE);
                image_size_x.setText(last_image_size_x);
            }
        }
        else if (e.getSource().equals(image_size_y))
        {
            try { Double.parseDouble (image_size_y.getText()); }
            catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog (file_chooser, "Y size must be a real number", "Error", JOptionPane.ERROR_MESSAGE);
                image_size_y.setText(last_image_size_y);
            }
        }
    }
    
    public void itemStateChanged(ItemEvent e) 
    {
        ImageSize image_size;
        FileNameExtensionFilter filter;

        if (e.getSource().equals(file_type_list))
        {
            filter = (FileNameExtensionFilter) file_chooser.getFileFilter();
            chosen_format = findFileFormat(filter);
            configImageSize (chosen_format);
        }
        else if (e.getSource().equals(image_size_list))
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                image_size = (ImageSize) e.getItem();
                image_size_x.setText(image_size.getXSize());
                image_size_y.setText(image_size.getYSize());
            }
        }
    }

    /** find the combo box that holds the file filters in the file chooser
     * @param container the file chooser to search
     * @param filter any of the filters already added to the file chooser
     * @return the combo box (or null, if box cannot be found) */
    private JComboBox findFileTypeComboBox (Container container, FileFilter filter)
    {
        int count, count2;
        JComboBox combo_box;
        Component comps [];
        Object object;
        String string;
        Component comp;
        
        comps = container.getComponents();
        for (count=0; count<comps.length; count++)
        {
            comp = comps [count];
            if (comp instanceof JComboBox)
            {
                combo_box = (JComboBox) comp;
                for (count2=0; count2<combo_box.getItemCount(); count2++)
                {
                    object = combo_box.getItemAt(count2);
                    if (object instanceof FileFilter)
                        string = ((FileFilter) object).getDescription();
                    else if (object instanceof String)
                        string = (String) object;
                    else
                        string = null;
                    if (filter.getDescription().equals(string)) return combo_box;
                }
            }
            else if (comp instanceof Container)
            {
                combo_box = findFileTypeComboBox((Container) comp, filter);
                if (combo_box != null) return combo_box;
            }
        }
        
        // nothing was found
        return null;
    }

    private void configImageSize (FileFormat file_format)
    {
        int count;

        if (file_format.isVector())
        {
            image_size_label.setText("Paper size (cm): ");
            image_size_list.setModel (new DefaultComboBoxModel (paper_size_list));
            image_size_x.setText(getStoredPaperSizeX());
            image_size_y.setText(getStoredPaperSizeY());
            last_image_size_x = getStoredPaperSizeX();
            last_image_size_y = getStoredPaperSizeY();
            for (count=0; count<paper_size_list.length; count++)
            {
                if (paper_size_list[count].getXSize().equals(getStoredPaperSizeX()) &&
                    paper_size_list[count].getYSize().equals(getStoredPaperSizeY()))
                    image_size_list.setSelectedItem(paper_size_list [count]);
            }
        }
        else
        {
            image_size_label.setText("Image size (pixels): ");
            image_size_list.setModel (new DefaultComboBoxModel (raster_size_list));
            image_size_x.setText(getStoredRasterSizeX());
            image_size_y.setText(getStoredRasterSizeY());
            last_image_size_x = getStoredRasterSizeX();
            last_image_size_y = getStoredRasterSizeY();
            for (count=0; count<raster_size_list.length; count++)
            {
                if (raster_size_list[count].getXSize().equals(getStoredRasterSizeX()) &&
                    raster_size_list[count].getYSize().equals(getStoredRasterSizeY()))
                    image_size_list.setSelectedItem(raster_size_list [count]);
            }
        }
    }
    
    private FileFormat findFileFormat (FileNameExtensionFilter filter)
    {
        int count;
        FileFormat format; 
        
        for (count=0; count<file_format_list.size(); count++)
        {
            format = file_format_list.get (count);
            if (format.filter.equals(filter)) return format;
        }
        throw new UnsupportedOperationException ("Image Save Dialog: image file format not found");
    }

    /** a routine that returns a list of names supported by ImageIO.write() */
    public static Vector<String> getImageWriterNames ()
    {
        int count, count2;
        boolean found, use_name;
        String names [];
        Vector<String> filtered_list;
        
        // get a list of image formats
        names = ImageIO.getReaderFormatNames();
        filtered_list = new Vector<String> ();
        for (count=0; count<names.length; count++)
        {
            found = false;
            use_name = true;

            // jpeg and jpg are the same format
            if (names[count].equalsIgnoreCase("jpeg")) names[count] = "jpg";
            
            // a number of format names are duplicated, so filter them out
            for (count2=0; (count2<filtered_list.size()) && (! found); count2++)
            {
                if (filtered_list.get(count2).equalsIgnoreCase(names[count]))
                    found = true;
            }
            if (found) use_name = false;
            
            // BMP and WBMP are listed on SUNs JRE 1.6, but don't work
            if (names[count].equalsIgnoreCase("bmp"))
                use_name = false;
            else if (names[count].equalsIgnoreCase("wbmp"))
                use_name = false;
                    
            if (use_name)
                filtered_list.add (names [count]);
        }
        
        return filtered_list;
    }
    
}
