/*
 * Utils.java
 *
 * Created on 21 January 2003, 13:55
 */

package bgs.geophys.library.Swing;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import bgs.geophys.library.File.*;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.table.TableColumnModel;

/**
 * Odds and ends, mostly static functions, to do with Swing
 *
 * @author  smf
 * @version 
 */
public class SwingUtils 
{
    
  /** find a menu item from its action command
   * @param menu_bar the menu bar to search
   * @param action_command the action command associated with the item
   * @return the item that was found or null */
  public static JMenuItem findMenuItem (JMenuBar menu_bar, String action_command)
  {
      int menu_count, item_count;
      JMenu menu;
      JMenuItem menu_item;

      for (menu_count=0; menu_count<menu_bar.getMenuCount(); menu_count++)
      {
          menu = menu_bar.getMenu (menu_count);
          if (menu == null) continue;
          for (item_count=0; item_count<menu.getItemCount(); item_count++)
          {
              menu_item = menu.getItem (item_count);
              if (menu_item == null) continue;
              if (menu_item.getActionCommand().equalsIgnoreCase(action_command))
                  return menu_item;
          }
      }
      return null;
  }

  /** get an array containing all menu items in a menu bar
   * @param menu_bar the menu bar to list
   * @return the item that was found or null */
  public static Vector<JMenuItem> listMenuItems (JMenuBar menu_bar)
  {
      int menu_count, item_count;
      JMenu menu;
      JMenuItem menu_item;
      Vector<JMenuItem> list;

      list = new Vector<JMenuItem> ();
      for (menu_count=0; menu_count<menu_bar.getMenuCount(); menu_count++)
      {
          menu = menu_bar.getMenu (menu_count);
          if (menu == null) continue;
          for (item_count=0; item_count<menu.getItemCount(); item_count++)
          {
              menu_item = menu.getItem (item_count);
              if (menu_item == null) continue;
              list.add (menu_item);
          }
      }
      return list;
  }

  /** create a copy of an image, that is lighter (or 'ghosted') */
  public static Image createGhostImage (Image img)
  { 
    JLabel imgObserver;
    Graphics2D g2;
        
    imgObserver = new JLabel ();
    BufferedImage ghost = new BufferedImage (img.getWidth(imgObserver),
                                             img.getHeight(imgObserver), 
                                             BufferedImage.TYPE_INT_ARGB_PRE); 
    g2 = ghost.createGraphics(); 
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f)); 
    g2.drawImage(img, 0, 0, ghost.getWidth(), ghost.getHeight(), imgObserver); 
    g2.dispose(); 
    return ghost; 
  } 
  
  /** Utility to load the named icon from the class path.
   * @param object an object whose class file is below the named data file
   *        in the directory tree - the file will be found by searching directories
   *        at and above the path for the object's class file
   * @param filename name of the icon file
   * @returns the icon or null if the load failed */
  public static ImageIcon LoadIcon (Object object, String filename)
  {
    int count, count2, length;
    ImageIcon icon;
    InputStream input_stream;
    FindAppFile find_app_file;
    byte bytes [];

    try
    {
      // first find the required file
      find_app_file = new FindAppFile (object, filename);
      if (! find_app_file.isFound()) return null;

      // next open an input stream and find the length of the file
      input_stream = find_app_file.getInputStream();
      length = input_stream.available();
      if (length <= 0) return null;
      bytes = new byte [length];
      
      // the data may be coming from a JarFile, in which case it may come in
      // chunks, rather than all at once, so read until nothing more is available
      count = 0;
      while (count < length)
      {
          count2 = input_stream.read (bytes, count, length - count);
          if (count2 <= 0) return null;
          count += count2;
      }
      if (count != length) return null;
      icon = new ImageIcon (bytes);
      if (icon.getIconHeight() <= 0 || icon.getIconWidth() <= 0) icon = null;
    }
    catch (Exception e) { return null; }
    return icon;
  }

  /** set the look and feel to the platform default
   * @param tool_tip_delay the length of tiem to display tooltips, in milliseconds */
  public static void setPlatformLookAndFeel (int tool_tip_delay)
  {
      ToolTipManager.sharedInstance().setDismissDelay(tool_tip_delay);
      try
      {
          UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
      }
      catch (UnsupportedLookAndFeelException e) { }
      catch (ClassNotFoundException e) { }
      catch (InstantiationException e) { }
      catch (IllegalAccessException e) { }
  }

  /** set the look and feel to the platform default - sets tooltip delay to 20 seconds*/
  public static void setPlatformLookAndFeel ()
  {
      setPlatformLookAndFeel(20000);
  }  
  
  /** Exception handler that displays the exception to the user
   * @param exception the exception */
  public static void ExceptionHandler (Exception e)
  {
    JOptionPane.showMessageDialog (null, e.toString(), "Unhandled Exception", JOptionPane.ERROR_MESSAGE);
  }

  /** A function to find out whether a dialog can be displayed - if not it
   * needs to be re-created
   * @param dialog the dialog to check
   * @returns true if the dialog is displayed */
  public static boolean isDialogDisplayable (JDialog dialog)
  {
    if (dialog == null) return false;
    if (dialog.isDisplayable ()) return true;
    return false;
  }

  /** A function to find out whether a dialog can be displayed - if not it
   * needs to be re-created
   * @param dialog the dialog to check
   * @returns true if the dialog is displayed */
  public static boolean isDialogDisplayable (Dialog dialog)
  {
    if (dialog == null) return false;
    if (dialog.isDisplayable ()) return true;
    return false;
  }

  /** A function to find out whether a frame can be displayed - if not it
   * needs to be re-created
   * @param dialog the dialog to check
   * @returns true if the dialog is displayed */
  public static boolean isFrameDisplayable (JFrame frame)
  {
    if (frame == null) return false;
    if (frame.isDisplayable ()) return true;
    return false;
  }

  /** A function to find out whether a frame can be displayed - if not it
   * needs to be re-created
   * @param dialog the dialog to check
   * @returns true if the dialog is displayed */
  public static boolean isFrameDisplayable (Frame frame)
  {
    if (frame == null) return false;
    if (frame.isDisplayable ()) return true;
    return false;
  }
  
  /** utility method to add an object to a panel using a gridbag layout
   * @param component the object to add
   * @param gridbag the gridbag layout manager
   * @param width the gridbag width
   * @param height the gridbag height
   */
  public static void addToGridBag (JPanel panel, Component component, GridBagLayout gridbag, int width, int height)
  {
    GridBagConstraints gbc;

    gbc = new GridBagConstraints ();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gridbag.setConstraints (component, gbc);
    panel.add (component);
  }
  
  /** Utility function to add a component to a grid bag layout
   * @param component the component to add
   * @param content_pane the content pane to use
   * @param x the x position in the grid bag
   * @param y the y position in the grid bag
   * @param width the width of this component
   * @param height the height of this component
   * @param weightx the weight in x dimension when filling extra space
   * @param weighty the weight in y dimension when filling extra space
   * @param fill the contraints fill code */
  public static void addToGridBag (Component component, Container content_pane, int x, int y, int width, int height, double weightx, double weighty, int fill)
  {
    GridBagConstraints constraints;
    GridBagLayout layout;

    constraints = new GridBagConstraints ();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = width;
    constraints.gridheight = height;
    constraints.weightx = weightx;
    constraints.weighty = weighty;
    constraints.fill = fill;
    ((GridBagLayout)(content_pane.getLayout())).setConstraints (component, constraints);
    content_pane.add (component);
  }

  /** Utility function to create a button
   * @param name the name displayed to the user
   * @param command the action command string
   * @param listener the action listener that will receive events from the button
   * @return the button */
  public static JButton createButton (String name, String command, ActionListener listener)
  {
    JButton button;

    button = new JButton (name);
    button.addActionListener (listener);
    button.setActionCommand (command);

    return button;
  }

  /** create a JMenuItem using the given text string, mnemonic,
   *  accelerator and action listener parameter
   * @param name - the string text for the menu item
   * @param mnmonic - the integer mnemonic to use
   * @param accelerator - the accelerator key
   * @param listener - the action listener for this item
   * @return - the JMenuItem */
  public static JMenuItem CreateMenuItem (String name, int mnemonic, KeyStroke accelerator, ActionListener listener)
  {
    JMenuItem menu_item;
    menu_item = new JMenuItem (name);
    menu_item.setMnemonic (mnemonic);
    menu_item.setActionCommand (name);
    menu_item.setAccelerator (accelerator);
    menu_item.addActionListener (listener);

    return menu_item;
  }
  
  /** create a JMenuItem using the given text string, mnemonic,
   *  accelerator and action listener parameter
   * @param name - the string text for the menu item
   * @param cmd_string the action command string to identify this
   *        menu item in the action listener
   * @param mnmonic - the integer mnemonic to use
   * @param accelerator - the accelerator key
   * @param listener - the action listener for this item
   * @return - the JMenuItem */
  public static JMenuItem CreateMenuItem (String name, String cmd_string, int mnemonic, KeyStroke accelerator, ActionListener listener)
  {
    JMenuItem menu_item;
    menu_item = new JMenuItem (name);
    menu_item.setMnemonic (mnemonic);
    menu_item.setActionCommand (cmd_string);
    menu_item.setAccelerator (accelerator);
    menu_item.addActionListener (listener);

    return menu_item;
  }
  
  /** Utility method to create a menu item, add an action listener
   * to it and set an action command string
   * @param name the name that will be displayed to the user
   * @param cmd_string the action command string to identify this
   *        menu item in the action listener
   * @param listener the action listener that will receive events from the button
   * @return the menu item */
  public static JMenuItem CreateMenuItem (String name, String cmd_string, ActionListener listener)
  {
    JMenuItem menu_item;

    menu_item = new JMenuItem (name);
    menu_item.addActionListener (listener);
    menu_item.setActionCommand (cmd_string);
    return menu_item;
  }

  /** Utility method to create a checkbox menu item, add an action listener
   * to it and set an action command string
   * @param name the name that will be displayed to the user
   * @param state the initial state
   * @param cmd_string the action command string to identify this
   *        menu item in the action listener
   * @param listener the action listener that will receive events from the button
   * @return the menu item */
  public static JCheckBoxMenuItem CreateCheckBoxMenuItem (String name, boolean state, String cmd_string, ActionListener listener)
  {
    JCheckBoxMenuItem menu_item;

    menu_item = new JCheckBoxMenuItem (name, state);
    menu_item.addActionListener (listener);
    menu_item.setActionCommand (cmd_string);
    return menu_item;
  }

  /** update and immediately repaint a label 
   * @param label the label to update
   * @param msg the new text for the label */
  public static void updateLabel (JLabel label, String msg)
  {
    Dimension dim;
    
    label.setText (msg);
    dim = label.getSize ();
    label.paintImmediately (0, 0, dim.width, dim.height);
  }

  /** update and immediately repaint a text field 
   * @param text_field the text field to update
   * @param msg the new text */
  public static void updateTextField (JTextField text_field, String msg)
  {
    Dimension dim;
    
    text_field.setText (msg);
    dim = text_field.getSize ();
    text_field.paintImmediately (0, 0, dim.width, dim.height);
  }

  /** rotate an image by 90, 180 or 270 degrees
   * @param image the image to rotate
   * @param x the right hand side of the area to rotate
   * @param x the top side of the area to rotate
   * @param width the width of the area to rotate
   * @param height the height of the area to rotate
   * @param amount the amount to rotate by, in degrees
   * @return the new image, which may be NULL if the operation failed */
  public static Image rotateImage (Image image, int x, int y, int width, int height, int amount)
  {
    PixelGrabber pg;
    Image dest_image;
    int src_pixels [], dest_pixels [], src_row, src_col, dest_row, dest_col, src_index, dest_index, dest_width;
    Label label;
      
    // check the amount
    if      (amount < -225) amount = 90;
    else if (amount < -135) amount = 180;
    else if (amount <  -45) amount = 270;
    else if (amount <   45) return image;
    else if (amount <  135) amount = 90;
    else if (amount <  225) amount = 180;
    else                    amount = 270;
      
    // extract the pixels
    src_pixels = new int [width * height];
    pg = new PixelGrabber (image, x, y, width, height, src_pixels, 0, width);
    try { pg.grabPixels (); } catch (InterruptedException e) { return null; }
      
    // rotate the picture
    if (amount == 180) dest_width = width;
    else dest_width = height;
    dest_pixels = new int [width * height];
    switch (amount)
    {
    case 90:
      for (src_row = 0; src_row < height; src_row ++)
      {
        for (src_col = 0; src_col < width; src_col ++)
        {
          dest_row = src_col;
          dest_col = height - (src_row +1);
          src_index = (src_row * width) + src_col;
          dest_index = (dest_row * dest_width) + dest_col;
          dest_pixels [dest_index] = src_pixels [src_index];
        }
      }
      break;
    case 180:
      for (src_row = 0; src_row < height; src_row ++)
      {
        for (src_col = 0; src_col < width; src_col ++)
        {
          dest_row = height - (src_row +1);
          dest_col = width - (src_col +1);
          src_index = (src_row * width) + src_col;
          dest_index = (dest_row * dest_width) + dest_col;
          dest_pixels [dest_index] = src_pixels [src_index];
        }
      }
      break;
    case 270:
      for (src_row = 0; src_row < height; src_row ++)
      {
        for (src_col = 0; src_col < width; src_col ++)
        {
          dest_row = width - (src_col +1);
          dest_col = src_row;
          src_index = (src_row * width) + src_col;
          dest_index = (dest_row * dest_width) + dest_col;
          dest_pixels [dest_index] = src_pixels [src_index];
        }
      }
      break;
    default:
        return null;
    }
    
    // create the new image
    label = new Label ();
    switch (amount)
    {
    case 90:
    case 270:
       dest_image = label.createImage(new MemoryImageSource (height, width, dest_pixels, 0, height)); 
       break;
    case 180:
       dest_image = label.createImage(new MemoryImageSource (width, height, dest_pixels, 0, width)); 
       break;
    default:
        dest_image = null;        
        break;
    }
    return dest_image;
  }
  
    /** this method can be used to change the size of any component
     * @param component the component to change
     * @param w_percent the amount (percentage) to change the width by
     * @param h_percent the amount (percentage) to change the height by */
    public static void changeSize (Component component, int w_percent, int h_percent)
    {
        Dimension size;
        
        size = component.getPreferredSize();
        if (size != null)
        {
            size.width = (size.width * w_percent) / 100;
            size.height = (size.height * h_percent) / 100;
            component.setPreferredSize(size);
        }
        
        size = component.getSize();
        if (size != null)
        {
            size.width = (size.width * w_percent) / 100;
            size.height = (size.height * h_percent) / 100;
            component.setSize(size);
        }
    }

    /** get a filename from the user using a JOptionPane dialog
     * @return the filename or null*/
    public static String getFilename (Component parent, String title, String prompt, String initialValue)
    {
        String string;
        JOptionPane opt_pane;
        JDialog dialog;
        Integer code;
        
        opt_pane = new JOptionPane (prompt, JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.OK_CANCEL_OPTION, null);
        opt_pane.setWantsInput (true);
        opt_pane.setInitialSelectionValue (initialValue);
        dialog = opt_pane.createDialog (parent, title);
        dialog.setVisible (true);
        code = (Integer) opt_pane.getValue();
        if (code.intValue() != JOptionPane.OK_OPTION) string = null;
        else
        {
            string = (String) opt_pane.getInputValue();
            if (string != null)
            {
                string = string.trim();
                if (string.length() <= 0) string = null;
            }
        }
        return string;
    }

    /** sort the contents of a list model into alphanumeric order 
     * @param list_model the model to sort */
    public static void sortListModel (DefaultListModel list_model)
    {
        int count;
        Vector <Comparable> array;
        
        array = new Vector <Comparable> ();
        for (count=0; count<list_model.size(); count++)
            array.add ((Comparable) list_model.get (count));
        Collections.sort (array);
        list_model.removeAllElements();
        for (count=0; count<array.size(); count++)
            list_model.addElement(array.get (count));
    }
    
    /** resize the columns in a table
     * @param table the table to resize
     * @param min_width the minimum width for a column
     * @param max_width the maximum width for a column
     * @param pcs the widths as percentages - there must be the same number as the
     *        number of columns in the table */
    public static void ResizeTableColumnWidths (JTable table, int min_width, int max_width, int... pcs)
    {
        int count, n_cols, widths [], total_width, old_resize_mode;
        TableColumnModel column_model;
        
        column_model = table.getColumnModel();
        n_cols = column_model.getColumnCount();
        widths = new int [n_cols];
        old_resize_mode = table.getAutoResizeMode();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // retrieve the current widths, set a minimum width and calculate
        // the total width
        total_width = 0;
        for (count=0; count<n_cols; count++)
        {
            widths [count] = column_model.getColumn(count).getWidth();
            column_model.getColumn(count).setMinWidth (min_width);
            column_model.getColumn(count).setMaxWidth (max_width);
            total_width += widths [count];
        }

        // recalculate widths
        for (count=0; count<n_cols; count++)
        {
            widths [count] = (total_width * pcs [count]) / 100;
            column_model.getColumn(count).setPreferredWidth(widths [count]);
        }
        table.doLayout();
        table.setAutoResizeMode(old_resize_mode);
    }

    /** configure the "File name:" and "Files of type" portion of
     * a File Chooser - this is intended for use with directory file
     * choosers, where these components are confusing
     * @param file_chooser the chooser to configure
     * @param file_name_label_text new text for the file name label
     * @paramfile_type_label_text new text for the files of type label
     * @param file_type_cb_enabled true to enable the files of type combo box, false to disable */
    public static void configureFileChooser (JFileChooser file_chooser,
                                             String file_name_label_text, 
                                             String file_type_label_text, 
                                             boolean file_type_cb_enabled)
    {
        int count;
        Vector<Component> comp_list;
        JLabel label;
        JComboBox combo_box;
        String string;

        // first the labels
        comp_list = findComponents(file_chooser, JLabel.class);
        for (count=0; count<comp_list.size(); count++)
        {
            label = (JLabel) comp_list.get(count);
            string = label.getText();
            if (string == null) string = "";
            if (string.equals("File name:")) label.setText(file_name_label_text);
            if (string.equals("Files of type:")) label.setText(file_type_label_text);
        }
        
        // then the combo box
        comp_list = findComponents(file_chooser, JComboBox.class);
        for (count=0; count<comp_list.size(); count++)
        {
            combo_box = (JComboBox) comp_list.get(count);
            combo_box.setEnabled(file_type_cb_enabled);
        }
        
    }
    
    /** a method that works through a container hierarchy, substituting
     * one label text for another - designed to allow the inaccessble labels
     * in JFileCHooser to be modified, but could be used with any container
     * @param container the container to work through
     * @param orig_text the text to search for
     * @param new_text the text to replace with
     * @param case_ind true for a case independant search */
    public static void substLabelText (Container container, String orig_text,
                                       String new_text, boolean case_ind)
    {
        int count;
        boolean replace;
        Vector<Component> comp_list;
        JLabel label;
        
        comp_list = findComponents(container, JLabel.class);
        for (count=0; count<comp_list.size(); count++)
        {
            label = (JLabel) comp_list.get(count);
            replace = false;
            if (case_ind)
            {
                if (orig_text.equalsIgnoreCase(label.getText()))
                    replace = true;
            }
            else
            {
                if (orig_text.equals(label.getText()))
                    replace = true;
            }
            if (replace)
                label.setText (new_text);
        }
    }
    
    /** find any components in the given container of the given class
     * @param container the container to work through
     * @param class_obj the class to match against
     * @return an array of matching components */
    public static Vector<Component> findComponents (Container container, Class class_obj)
    {
        int count;
        Component components [];
        Vector<Component> comp_list;
        
        comp_list = new Vector<Component> ();
        components = container.getComponents();
        for (count=0; count<components.length; count++)
        {
            if (components[count] instanceof Container)
            {
                comp_list.addAll (findComponents ((Container) components [count], class_obj));
            }
            if (class_obj.equals(components[count].getClass()))
                comp_list.add (components[count]);
        }
        return comp_list;
    }
    
    /** drop in replacement for ImageIO.write that handles JPEG colour problem */
    public static boolean writeImage (RenderedImage im, String formatName, File out)
    throws IOException
    {
        return writeImage (im, formatName, new FileOutputStream (out));
    }
    
    /** drop in replacement for ImageIO.write that handles JPEG colour problem */
    public static boolean writeImage (RenderedImage im, String formatName, ImageOutputStream out)
    throws IOException
    {
        ImageWriter writer;
        ImageWriteParam param;
        ColorModel cm;
        
        if (formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("jpeg"))
        {
            try
            {
                writer = ImageIO.getImageWritersByFormatName ("jpeg").next ();
                param = writer.getDefaultWriteParam ();
                param.setSourceBands (new int[] {0, 1, 2});
                cm = new DirectColorModel(24,
                                          0x00ff0000,	// Red
                                          0x0000ff00,	// Green
                                          0x000000ff,	// Blue
                                          0x0);          // Alpha
                param.setDestinationType (new ImageTypeSpecifier (cm, cm.createCompatibleSampleModel (1, 1)));
                
                writer.setOutput (out);
                writer.write (null, new IIOImage (im, null, null), param);
                writer.dispose ();
                return true;
            }
            catch (NoSuchElementException e) { return false; }
        }
        
        return ImageIO.write (im, formatName, out);
    }
    
    /** drop in replacement for ImageIO.write that handles JPEG colour problem */
    public static boolean writeImage (RenderedImage im, String formatName, OutputStream out)
    throws IOException
    {
        boolean ret_val;
        ImageOutputStream outStream;
        
        if (formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("jpeg"))
        {
            outStream = ImageIO.createImageOutputStream (out);
            ret_val = writeImage (im, formatName, outStream);
            outStream.close ();
            return ret_val;
        }
        
        return ImageIO.write (im, formatName, out);
    }
    
    
    
}


