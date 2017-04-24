/*
 * TextViewerDialog.java
 *
 * Created on 14 February 2003, 11:41
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import java.awt.event.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;

/**
 * Dialog to display contents of a text file.
 *
 * @author  fmc
 */
public class TextViewerDialog extends JDialog 
implements ActionListener, WindowListener
{
  private TextViewerPanel text_panel;
  private JButton print_button, close_button;

  // objects used for printing
  private DocFlavor print_doc_flavor;
  private PrintRequestAttributeSet print_request_attr_set;
  private PrintService default_print_service;
  private PrintService [] print_service_array;

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param filename - the name of the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Frame owner, File filename, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
    
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (filename, null, null);
  }

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param url - the name of the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Frame owner, URL url, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
      
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (null, url, null);
  }

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param reader - stream to the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Frame owner, Reader reader, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
      
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (null, null, reader);
  }

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param filename - the name of the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Dialog owner, File filename, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
    
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (filename, null, null);
  }

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param url - the name of the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Dialog owner, URL url, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
      
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (null, url, null);
  }

 /********************************************************************
  * creates a new TextViewerDialog dialog.
  *
  * @param owner - the owner of this dialog
  * @param reader - stream to the file to display
  * @param icon the icon to display in the dialog frame (or null)
  * @param title - the title of this dialog
  * @throws TextViewerException if there was an error with the file
  ********************************************************************/
  public TextViewerDialog(Dialog owner, Reader reader, String title, ImageIcon icon)
  throws TextViewerException
  {
    super (owner, "Text Viewer: " + title);
      
    setUpPrinting ();
    if (icon != null) this.setIconImage(icon.getImage());
    createComponents (null, null, reader);
  }

  /** set up printing */
  private void setUpPrinting ()
  {
    print_doc_flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    print_request_attr_set = new HashPrintRequestAttributeSet ();
    default_print_service = PrintServiceLookup.lookupDefaultPrintService ();
    print_service_array = PrintServiceLookup.lookupPrintServices (print_doc_flavor, print_request_attr_set);
  }
  
  /** set up the components
   * @param filename the name of the file - null to use url
   * @param url a url to use - null to use reader (where filename is null)
   * @param reader a stream to the file (where url and filename are null) */
  private void createComponents (File filename, URL url, Reader reader)
  throws TextViewerException
  {
    Container content_pane;
    JScrollPane scroll_pane;
    JPanel close_panel;

    // set the layout
    content_pane = getContentPane();
    content_pane.setLayout(new BorderLayout ());

    // create empty text panel
    if (filename != null)
      text_panel = new TextViewerPanel (filename);
    else if (url != null)
    {
      try
      {
        text_panel = new TextViewerPanel (new InputStreamReader (url.openStream()));
      }
      catch (IOException e)
      {
        throw new TextViewerException ("Unable to open " + url.toString());
      }
    }
    else
      text_panel = new TextViewerPanel (reader);

    // create scrolling pane for text area
    scroll_pane = new JScrollPane (text_panel);
    scroll_pane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll_pane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    content_pane.add ("Center", scroll_pane);

    // add panel for close button
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

    // setup closure
    this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener (this);

    // make dialog slightly shorter than pack height to that
    // close button is not hidden behind toolbar
    this.pack ();
    this.setSize (this.getWidth (), this.getHeight () - 50);
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
    if (source == print_button)
    {
      print_service = ServiceUI.printDialog (null, 200, 200, print_service_array, default_print_service, print_doc_flavor, print_request_attr_set);
      if (print_service != null)
      {
        print_job = print_service.createPrintJob();
        doc_attr_set = new HashDocAttributeSet();
        document = new SimpleDoc (text_panel, print_doc_flavor, doc_attr_set);
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
    else if (source == close_button)
    {
      closeDialog (null);
    }
  }
  
  /* methods for handling window events */
  public void windowActivated (WindowEvent e) { }
  public void windowClosed (WindowEvent e) { }
  public void windowClosing (WindowEvent e) { closeDialog (e); }
  public void windowDeactivated (WindowEvent e) { }
  public void windowDeiconified (WindowEvent e) { }
  public void windowIconified (WindowEvent e) { }
  public void windowOpened (WindowEvent e) { }
  
  private void closeDialog (WindowEvent e)
  {
    setVisible (false);
    dispose ();
  }
  
}
