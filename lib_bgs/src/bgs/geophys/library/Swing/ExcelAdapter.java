/*
 * ExcelAdapter.java
 *
 * Created on 11 October 2004, 13:52
 */

package bgs.geophys.library.Swing;

/**
 *
 * @author  smf
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.*;

/**
* ExcelAdapter enables Copy-Paste Clipboard functionality on JTables.
* The clipboard data format used by the adapter is compatible with
* the clipboard format used by Excel. This provides for clipboard
* interoperability between enabled JTables and Excel.
*/
public class ExcelAdapter implements ActionListener
{
   private String rowstring,value;
   private Clipboard system;
   private StringSelection stsel;
   private JTable my_table ;
   private boolean allow_copy;
   private boolean allow_paste;
   private boolean allow_delete;
   private boolean allow_no_selection;
   private boolean strip_html;
   private Component owner;     // owner dialog/frame - may be null
   
   /** The Excel Adapter is constructed with a
    * JTable on which it enables Copy-Paste and acts
    * as a Clipboard listener. */
   public ExcelAdapter (Component owner, JTable myJTable, 
                        boolean allow_copy, boolean allow_paste, boolean allow_delete)
   {
       this (owner, myJTable, allow_copy, allow_paste, allow_delete, false, false);
   }
   
   /** The Excel Adapter is constructed with a
    * JTable on which it enables Copy-Paste and acts
    * as a Clipboard listener.
    * @param allow_no_selection if copy or delete is called when the table is
    *        not selected, copy or delete the whole table
    * @param strip_html remove html from copied data */
   public ExcelAdapter (Component owner, JTable myJTable, 
                        boolean allow_copy, boolean allow_paste, boolean allow_delete,
                        boolean allow_no_selection, boolean strip_html)
   {
      my_table = myJTable;
      this.owner = owner;
      
      // Identifying the copy KeyStroke user can modify this
      // to copy on some other Key combination.
      KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
      
      // Identifying the Paste KeyStroke user can modify this
      //to copy on some other Key combination.
      KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);

      my_table.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
      my_table.registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
      
      system = Toolkit.getDefaultToolkit().getSystemClipboard();
      
      this.allow_copy = allow_copy;
      this.allow_paste = allow_paste;
      this.allow_delete = allow_delete;
      this.allow_no_selection = allow_no_selection;
      this.strip_html = strip_html;
   }

   /**
    * Public Accessor methods for the Table on which this adapter acts.
    */
   public JTable getJTable() {return my_table;}
   public void setJTable(JTable jTable1) {this.my_table=jTable1;}
   
   /** copy the current selection to the clipboard 
    * @return an error message OR null if there were no problems */
   public String copySelection ()
   {
       StringBuffer sbf;
       String string;
       int numcols, numrows, rowsselected [], colsselected [], i, j;
       
       if (! allow_copy) return "Copy not allowed";
         
       // Check to ensure we have selected only a contiguous block of cells
       numcols=my_table.getSelectedColumnCount();
       numrows=my_table.getSelectedRowCount();
       rowsselected=my_table.getSelectedRows();
       colsselected=my_table.getSelectedColumns();

       // handle no selection
       if (numcols <= 0 || numrows <= 0)
       {
           if (allow_no_selection) return copyAll ();
           return "No cells have been selected";
       }

       if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
              numrows==rowsselected.length) &&
              (numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
              numcols==colsselected.length)))
           return "Invalid Copy Selection";
       
       sbf = new StringBuffer();
       for (i=0;i<numrows;i++)
       {
           for (j=0;j<numcols;j++)
           {
               try{
                   string = my_table.getValueAt (rowsselected[i], colsselected[j]).toString();                  
                if (strip_html) string = removeHtml (string);
                sbf.append (string);
               } catch (Exception e){/*needn't do anything if null returned*/}
               
               if (j<numcols-1) sbf.append("\t");
           }
           sbf.append("\n");
       }
       
       stsel  = new StringSelection(sbf.toString());
       system = Toolkit.getDefaultToolkit().getSystemClipboard();
       system.setContents(stsel, stsel);
       
       return null;
   }
   
   /** copy all data to the clipboard 
    * @return an error message OR null if there were no problems */
   public String copyAll ()
   {
       StringBuffer sbf;
       String string;
       int numcols, numrows, i, j;
       
       if (! allow_copy) return "Copy not allowed";
         
       numcols = my_table.getColumnCount();
       numrows = my_table.getRowCount();
       
       sbf = new StringBuffer();
       for (i=0;i<numrows;i++)
       {
           for (j=0;j<numcols;j++)
           {
               try{
               string = my_table.getValueAt (i, j).toString();               
               if (strip_html) string = removeHtml (string);
               sbf.append (string);
               } catch (Exception e){}
               if (j<numcols-1) sbf.append("\t");
           }
           sbf.append("\n");
       }
       
       stsel  = new StringSelection(sbf.toString());
       system = Toolkit.getDefaultToolkit().getSystemClipboard();
       system.setContents(stsel, stsel);
       
       return null;
   }
   
   /** delete the current selection
    * @return an error message OR null if there were no problems */
   public String deleteSelection ()
   {
       if (! allow_delete) return "Delete not allowed";
       
       int numcols=my_table.getSelectedColumnCount();
       int numrows=my_table.getSelectedRowCount();
       int[] rowsselected=my_table.getSelectedRows();
       int[] colsselected=my_table.getSelectedColumns();

       if (numcols <= 0 || numrows <= 0)
       {
           if (! allow_no_selection) return "No cells have been selected";
           numcols = my_table.getColumnCount();
           numrows = my_table.getRowCount();
       }
       
       for (int i=0;i<numrows;i++)
       {
           for (int j=0;j<numcols;j++)
           {
               my_table.setValueAt("",rowsselected[i],colsselected[j]);
           }
       }
       
       return null;
   }
   
   /** cut the current selection to the clipboard 
    * @return an error message OR null if there were no problems */
   public String cutSelection ()
   {
       String msg;
       
       msg = copySelection ();
       if (msg == null) msg = deleteSelection ();
       return msg;
   }
   
   /** copy the clipboard contents to the table
    * @return an error message OR null if there were no problems */
   public String pasteClipboard ()
   {
       if (! allow_paste) return "Paste not allowed";
         
       if (my_table.getSelectedColumnCount() <= 0 ||
           my_table.getSelectedRowCount() <= 0) return "No cells have been selected";
       
       int startRow=(my_table.getSelectedRows())[0];
       int startCol=(my_table.getSelectedColumns())[0];
       try
       {
           String trstring= (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));
           StringTokenizer st1=new StringTokenizer(trstring,"\n");
           for(int i=0;st1.hasMoreTokens();i++)
           {
               rowstring=st1.nextToken();
               StringTokenizer st2=new StringTokenizer(rowstring,"\t");
               for(int j=0;st2.hasMoreTokens();j++)
               {
                   value=(String)st2.nextToken();
                   if (startRow+i< my_table.getRowCount()  &&
                       startCol+j< my_table.getColumnCount())
                      my_table.setValueAt(value,startRow+i,startCol+j);
               }
           }
       }
       catch (UnsupportedFlavorException e) { return "Wrong type of data in clipboard"; }
       catch (java.io.IOException e) { return "Wrong type of data in clipboard"; }
       
       return null;
   }
   
   /**
    * This method is activated on the Keystrokes we are listening to
    * in this implementation. Here it listens for Copy and Paste ActionCommands.
    * Selections comprising non-adjacent cells result in invalid selection and
    * then copy action cannot be performed.
    * Paste is done by aligning the upper left corner of the selection with the
    * 1st element in the current selection of the JTable.
    */
   public void actionPerformed(ActionEvent e)
   {
       String msg;
       
       if (e.getActionCommand().compareTo("Copy")==0)
           msg = copySelection ();
       else if (e.getActionCommand().compareTo("Cut")==0)
           msg = cutSelection ();
       else if (e.getActionCommand().compareTo("Delete")==0)
           msg = deleteSelection ();
       else if (e.getActionCommand().compareTo("Paste")==0)
           msg = pasteClipboard ();
       else
           msg = "Invalid command: " + e.getActionCommand();
       
       if (msg != null)
           JOptionPane.showMessageDialog(owner, msg, "Error", JOptionPane.ERROR_MESSAGE);
   }
   
   // very simplistic html tag remover - there is probably a better way!
   private String removeHtml (String html_text)
   {
       StringBuffer sbf;
       int bracket_count, amp_count, count;
       char token;
       
       sbf = new StringBuffer ();
       bracket_count = amp_count = 0;
       for (count=0; count<html_text.length(); count++)
       {
           token = html_text.charAt(count);
           switch (token)
           {
               case '<':
                   bracket_count ++;
                   break;
               case '>':
                   bracket_count --;
                   break;
               case '&':
                   amp_count ++;
                   break;
               case ';':
                   amp_count --;
                   break;
               default:
                   if (bracket_count <= 0 && amp_count <= 0)
                       sbf.append (token);
           }
       }
       return sbf.toString();
   }
}

