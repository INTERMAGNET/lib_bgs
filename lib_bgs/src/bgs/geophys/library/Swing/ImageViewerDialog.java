/*
 * ImageViewerDialog.java
 *
 * Created on 18 February 2003, 12:51
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Class to display an image (including PCX) in a dialog
 *
 * @author  fmc
 */
public class ImageViewerDialog extends JDialog
implements ActionListener, WindowListener
{
  private JButton print_button, close_button;
  private File imagefile;
  private ImageViewerPanel image_panel;

  // objects used for printing
  private DocFlavor print_doc_flavor;
  private PrintRequestAttributeSet print_request_attr_set;
  private PrintService default_print_service;
  private PrintService [] print_service_array;

 /********************************************************************
  * creates a new ImageFileViewer dialog.
  *
  * @param owner - the owner of this dialog
  * @param filename - the name of the file to display
  * @param title - the title for this dialog
  * @param icon the icon to display in the dialog frame (or null)
  * @throws ImageViewerException if there was an error displaying the image
  ********************************************************************/
  public ImageViewerDialog(Frame owner, File filename, String title, ImageIcon icon) throws ImageViewerException
  {
    super (owner, "Image Viewer: " + title);
    
    this.imagefile = filename;
    if (icon != null)
        this.setIconImage(icon.getImage());

    // display components
    createComponents ();
  }

 /********************************************************************
  * creates a new ImageFileViewer dialog.
  *
  * @param owner - the owner of this dialog
  * @param filename - the name of the file to display
  * @param title - the title for this dialog
  * @param icon the icon to display in the dialog frame (or null)
  * @throws ImageViewerException if there was an error displaying the image
  ********************************************************************/
  public ImageViewerDialog(Dialog owner, File filename, String title, ImageIcon icon) throws ImageViewerException
  {
    super (owner, "Image Viewer: " + title);
    
    this.imagefile = filename;
    if (icon != null)
        this.setIconImage(icon.getImage());

    // display components
    createComponents ();
  }

  /** set up the components */
  private void createComponents ()
  throws ImageViewerException
  {
    Container content_pane;
    JScrollPane scroll_pane;
    JPanel close_panel;

    // set up printing
    print_doc_flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    print_request_attr_set = new HashPrintRequestAttributeSet ();
    default_print_service = PrintServiceLookup.lookupDefaultPrintService ();
    print_service_array = PrintServiceLookup.lookupPrintServices (print_doc_flavor, print_request_attr_set);

    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout(new BorderLayout());

    // create empty image panel
    image_panel = new ImageViewerPanel (imagefile);

    // create scroll pane to contain image
    scroll_pane = new JScrollPane (image_panel);
    scroll_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll_pane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    content_pane.add ("Center", scroll_pane);

    // add panel for print and close buttons
    close_panel = new JPanel();
    content_pane.add ("South", close_panel);

    // add print button
    print_button = new JButton ("Print");
    print_button.addActionListener (this);
    close_panel.add (print_button);

    // add close button
    close_button = new JButton ("Close");
    close_button.addActionListener (this);
    close_panel.add (close_button);
    getRootPane().setDefaultButton (close_button);
    
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);
    this.pack ();
    this.setVisible (true);
  }

  // listener for action events
  public void actionPerformed(ActionEvent actionEvent)
  {
    Object source;
    DocPrintJob print_job;
    DocAttributeSet doc_attr_set;
    Doc document;
    PrintService print_service;

    source = actionEvent.getSource();
    if (source == close_button)
    {
      closeDialog (null);
    }
    else if (source == print_button)
    {
      print_service = ServiceUI.printDialog (null, 200, 200, print_service_array, default_print_service, print_doc_flavor, print_request_attr_set);
      if (print_service != null)
      {
        print_job = print_service.createPrintJob();
        doc_attr_set = new HashDocAttributeSet();
        document = new SimpleDoc (image_panel, print_doc_flavor, doc_attr_set);
        try 
        {
          print_job.print (document, print_request_attr_set); 
          default_print_service = print_service;
        }
        catch (PrintException pe)
        {
          JOptionPane.showMessageDialog (this, pe.toString ());
        }
      }
    }
  }
  
  public void windowActivated(WindowEvent e) {  }
  public void windowClosed(WindowEvent e) {  }
  public void windowClosing(WindowEvent e)  { closeDialog (e); }
  public void windowDeactivated(WindowEvent e) {  }
  public void windowDeiconified(WindowEvent e) {  }
  public void windowIconified(WindowEvent e) {  }
  public void windowOpened(WindowEvent e) {  }
  
  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) 
  {
    setVisible(false);
    dispose();
  }
  
}
