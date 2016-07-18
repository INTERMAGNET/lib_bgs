/*
 * DatabaseTable.java
 *
 * Created on 09 May 2002, 10:08
 */

package bgs.geophys.library.Database;

import java.util.*;
import java.io.*;

import javax.swing.table.*;
import javax.swing.event.*;

import bgs.geophys.library.File.*;

/**
 * A class designed to hold data from CSV files - the CSV file is read into
 * the object when it is constructed.
 *
 * The class implements the TableModel interface to make it easy to use with a JTable
 *
 * @author  smf
 * @version
 */
public class CSVDatabaseTable extends Object implements TableModel
{

  /** code for CSV file with no top title row */
  public static final int CSV_TOP_ROW_DATA           = 1;
  /** code for CSV file with top title row */
  public static final int CSV_TOP_ROW_TITLE          = 2;
  /** code for CSV file with top title row that is ignored */
  public static final int CSV_TOP_ROW_IGNORE         = 3;
  
  // private members
  private Vector<CSVDatabaseColumn> columns;         // array of CSVDatabaseColumn objects

  private int search_col_index;   // index of the column being searched, or -1 for all columns
  private int search_row_index;   // index of the next row to search
  private String search_string;   // the string to search for
  private boolean search_case_ind;// true for case indpendant search
  
  private Vector<TableModelListener> listeners;     // array of TableModelListeners
  
  /** create an empty table with no rows or columns */
  public CSVDatabaseTable()
  {
    columns = new Vector<CSVDatabaseColumn> ();
    listeners = new Vector<TableModelListener> ();
  }

  /** create a table, with columns as specified
   * @param col_list a list of columns with alternate column
   *        names and data types separated by commas. Valid data
   *        types are INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
   * @throws CSVDatabaseException if col_list is invalid */
  public CSVDatabaseTable(String col_list)
  throws CSVDatabaseException
  {
    columns = new Vector<CSVDatabaseColumn> ();
    listeners = new Vector<TableModelListener> ();
    AddColumns (col_list);
  }
  
  /** create a table, with columns as specified and fill it from the given file
   * @param col_list a list of columns with alternate column
   *        names and data types separated by commas. Valid data
   *        types are INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
   * @param file the CSV format file to fill it from
   * @param title_code one of the CSV title codes
   * @throws CSVDatabaseException if col_list is invalid 
   * @throws FileNotFoundException if the file could not be found
   * @throws IOException there was an error reading the file
   * @throws CSVDatabaseException is the data types in the file were incorrect
   *         if the name would cause a duplicate column name */
  public CSVDatabaseTable(String col_list, File file, int title_code)
  throws FileNotFoundException, IOException, CSVDatabaseException
  {
    columns = new Vector<CSVDatabaseColumn> ();
    listeners = new Vector<TableModelListener> ();
    AddColumns (col_list);
    FillFromCSVFile(file, title_code);
  }

  /** create a table, with columns as specified and fill it from the given stream
   * @param col_list a list of columns with alternate column
   *        names and data types separated by commas. Valid data
   *        types are INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
   * @param buffered_reader the CSV format file stream to fill it from
   * @param title_code one of the CSV title codes
   * @throws CSVDatabaseException if col_list is invalid 
   * @throws FileNotFoundException if the file could not be found
   * @throws IOException there was an error reading the file
   * @throws CSVDatabaseException is the data types in the file were incorrect
   *         if the name would cause a duplicate column name */
  public CSVDatabaseTable(String col_list, BufferedReader buffered_reader, int title_code)
  throws FileNotFoundException, IOException, CSVDatabaseException
  {
    columns = new Vector<CSVDatabaseColumn> ();
    listeners = new Vector<TableModelListener> ();
    AddColumns (col_list);
    FillFromCSVFile(buffered_reader, title_code);
  }

  /** create a table, with columns as specified and fill it from a file in a jar file
   * @param col_list list of column definitions for the table
   * @param object an object whose class file is below the named data file
   *        in the jar file's directory tree - the file will be found by searching directories
   *        at and above the path for the object's class file
   * @param filename name of the file
   * @param title_code one of the CSVDatabaseTable title codes
   * @returns the table or null if the load failed
   * @throws CSVDatabaseException if col_list is invalid 
   * @throws FileNotFoundException if the file could not be found
   * @throws IOException there was an error reading the file
   * @throws CSVDatabaseException is the data types in the file were incorrect
   *         if the name would cause a duplicate column name */
  public CSVDatabaseTable (String col_list, Object object, String filename, int title_code)
  throws FileNotFoundException, IOException, CSVDatabaseException
  {
    FindAppFile find_app_file;
    BufferedReader buffered_reader;
    
    find_app_file = new FindAppFile (object, filename);
    if (! find_app_file.isFound()) throw (new FileNotFoundException ());
    buffered_reader = new BufferedReader (new InputStreamReader(find_app_file.getInputStream()));
    columns = new Vector<CSVDatabaseColumn> ();
    listeners = new Vector<TableModelListener> ();
    AddColumns (col_list);
    FillFromCSVFile(buffered_reader, title_code);
  }
  
  /** Add a list of columns to the table
   * @param col_list a list of columns with alternate column
   *        names and data types separated by commas. Valid data
   *        types are INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING
   * @throws CSVDatabaseException if col_list is invalid */
  public void AddColumns (String col_list)
  throws CSVDatabaseException
  {
    StringTokenizer cols;
    String name, type;
    
    cols = new StringTokenizer (col_list, ",");
    if (! cols.hasMoreElements())
      throw (new CSVDatabaseException ("Empty list of columns"));
    while (cols.hasMoreElements())
    {
      name = cols.nextToken ();
      if (! cols.hasMoreElements())
        throw (new CSVDatabaseException ("Missing data type specifier at end of column list"));
      type = cols.nextToken ();
      if (type.equalsIgnoreCase("INT")) AddColumn(CSVDatabaseColumn.TYPE_INT, name);
      else if (type.equalsIgnoreCase("LONG")) AddColumn(CSVDatabaseColumn.TYPE_DOUBLE, name);
      else if (type.equalsIgnoreCase("FLOAT")) AddColumn(CSVDatabaseColumn.TYPE_FLOAT, name);
      else if (type.equalsIgnoreCase("DOUBLE")) AddColumn(CSVDatabaseColumn.TYPE_DOUBLE, name);
      else if (type.equalsIgnoreCase("BOOLEAN")) AddColumn(CSVDatabaseColumn.TYPE_BOOLEAN, name);
      else if (type.equalsIgnoreCase("STRING")) AddColumn(CSVDatabaseColumn.TYPE_STRING, name);
      else throw (new CSVDatabaseException ("Bad data type specifier: " + type));
    }
  }
  
  /** Add an unnamed column to the table
   * @param data_type the type of data
   * @throws CSVDatabaseException if the name would cause a duplicate column name */
  public void AddColumn (int data_type)
  throws CSVDatabaseException
  {
    String name;
    
    name = new String ("Column" + columns.size() +1);
    AddColumn (data_type, name);
  }

  /** Add a column to the table
   * @param data_type the type of data
   * @param name the name for the column
   * @throws CSVDatabaseException if the name would cause a duplicate column name */
  public void AddColumn (int data_type, String name)
  throws CSVDatabaseException
  {
    int count, length;
    CSVDatabaseColumn column;
    
    try
    {
      FindColumn (name);
      throw (new CSVDatabaseException ("Duplicate column name: " + name));
    }
    catch (Exception e) { }
    column = new CSVDatabaseColumn (name, data_type);
    columns.add (column);
    
    // fill the new column to the same number of rows as the
    // exisiting rows
    length = GetNRows();
    for (count=0; count<length; count++) column.Add ();
  }

  /** Give a column a new name
   * @param name the current name
   * @param new_name the new name
   * @throws CSVDatabaseException if the column name could not be found or
   *         if the name would cause a duplicate column name */
  public void RenameColumn (String name, String new_name)
  throws CSVDatabaseException
  {
    int index;
  
    index = FindColumn (name);
    RenameColumn (index, new_name);
  }

  /** Give a column a new name
   * @param col the number of the column to rename (1..NColumns)
   * @param new_name the new name
   * @throws CSVDatabaseException if the column index was out of range
   *         if the name would cause a duplicate column name */
  public void RenameColumn (int col, String new_name)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
  
    try
    {
      FindColumn (new_name);
      throw (new CSVDatabaseException ("Duplicate column name: " + new_name));
    }
    catch (Exception e) { }
    column = columns.get(col);
    column.Rename (new_name);
  }

  /** Add a row in CSV format to the table. The CSV string may contain
   * any number of fields. Extra fields in the string are ignored, missing
   * fields are flagged as emty. CSV is an ASCII format. Fields are separated
   *  by commas. Strings
   * may be optionally enclosed by double quotes. Strings containing commas
   * must be enclosed by double quotes. To insert a double quote in a string
   * prefix it with nother double quote. Not whitespace is allowed between
   * fields.
   * @param csv_string the string containing CSV data
   * @param title_flag if true interpret the string as a list of column names
   * @throws CSVDatabaseException if the data types in the string were incorrent
   * @throws NumberFormatException if a field contains a badly formed number */
  public void AddCSVRow (String string, boolean title_flag)
  throws CSVDatabaseException
  {
    boolean quit, quoted, quit2;
    char character;
    int input_ptr, column_ptr, quote_count, input_length;
    StringBuffer buffer;
    String field;
    CSVDatabaseColumn column;
  
    // check that there are some columns
    if (columns.size() <= 0) return;
    
    // until we run out of string to process...
    input_ptr = column_ptr = 0;
    input_length = string.length();
    buffer = new StringBuffer ();
    for (quit=false; (! quit) && (input_ptr < input_length); )
    {
      // extract the next field
      buffer.setLength (0);
      quote_count = 0;
      if (string.charAt (input_ptr) == '"')
      {
        quoted = true;
        input_ptr ++;
      }
      else quoted = false;
      for (quit2=false; (! quit2) && (input_ptr < input_length); )
      {
        character = string.charAt (input_ptr ++);
        switch (character)
        {
        case '"':
          quote_count ++;
          if (quote_count == 2)
          {
            buffer.append ('"');
            quote_count = 0;
          }
          break;
        case ',':
          if (quoted)
          {
            if (quote_count > 0) quit2 = true;
            else buffer.append (',');
          }
          else quit2 = true;
          break;
        default:
          while (quote_count > 0)
          {
            buffer.append ('"');
            quote_count --;
          }
          buffer.append (character);
          break;
        }
      }
      
      // check that there is something to process
      if (buffer.length() <= 0) 
      {
        // add an empty field
        column = columns.get (column_ptr ++);
        column.Add ();
      }
      else if (title_flag)
        RenameColumn (column_ptr ++, new String (buffer));
      else
      {
        // add this field to the next column
        column = columns.get (column_ptr ++);
        field = new String (buffer);
        switch (column.GetType ())
        {
        case CSVDatabaseColumn.TYPE_FLOAT:
          column.Add (Float.parseFloat (field));
          break;
        case CSVDatabaseColumn.TYPE_DOUBLE:
          column.Add (Double.parseDouble (field));
          break;
        case CSVDatabaseColumn.TYPE_INT:
          column.Add (Integer.parseInt(field));
          break;
        case CSVDatabaseColumn.TYPE_LONG:
          column.Add (Long.parseLong(field));
          break;
        case CSVDatabaseColumn.TYPE_BOOLEAN:
          if (field.equalsIgnoreCase("true") ||
              field.equalsIgnoreCase("t") ||
              field.equalsIgnoreCase("yes") ||
              field.equalsIgnoreCase("y") ||
              field.equalsIgnoreCase("1"))
            column.Add (true);
          else if (field.equalsIgnoreCase("false") ||
              field.equalsIgnoreCase("f") ||
              field.equalsIgnoreCase("no") ||
              field.equalsIgnoreCase("n") ||
              field.equalsIgnoreCase("0"))
            column.Add (false);
          else
            throw (new CSVDatabaseException ("Badly formed boolean string: " + field));
          break;
        case CSVDatabaseColumn.TYPE_STRING:
          column.Add (field);
          break;
        }
      }

      // check if we are done
      if (column_ptr >= columns.size()) quit = true;
    }
    
    // add blank fields to any remaining columns
    while (column_ptr < columns.size ())
    {
      column = columns.get (column_ptr ++);
      column.Add ();
    }
  }

  /** Fill a databse from a CSV format file. Individual lines are read from
   * the file and passed to AddRow for processing.
   * @param file the file to read
   * @param title_code one of the CSV title codes
   * @throws FileNotFoundException if the file could not be found
   * @throws IOException there was an error reading the file
   * @throws CSVDatabaseException is the data types in the file were incorrect
   *         if the name would cause a duplicate column name */
  public void FillFromCSVFile (File file, int title_code)
  throws FileNotFoundException, IOException, CSVDatabaseException
  {
    FileReader file_reader;
    BufferedReader buffered_reader;

    // open the file
    file_reader = new FileReader (file);
    buffered_reader = new BufferedReader (file_reader);
    
    try
    {
      FillFromCSVFile (buffered_reader, title_code);
    }
    catch (IOException e)
    {
      file_reader.close ();
      throw (e);
    }
    catch (CSVDatabaseException e)
    {
      file_reader.close ();
      throw (e);
    }
    
    // close the file
    file_reader.close ();
  }
  
  /** Fill a databse from a CSV format file. Individual lines are read from
   * the file and passed to AddRow for processing.
   * @param buffred_reader the stream to read from
   * @param title_code one of the CSV title codes
   * @throws IOException there was an error reading the file
   * @throws CSVDatabaseException is the data types in the file were incorrect
   *         if the name would cause a duplicate column name */
  public void FillFromCSVFile (BufferedReader buffered_reader, int title_code)
  throws IOException, CSVDatabaseException
  {
    String string;
    
    // read and convert the file
    while ((string = buffered_reader.readLine ()) != null)
    {
      switch (title_code)
      {
      case CSV_TOP_ROW_TITLE: AddCSVRow (string, true); break;
      case CSV_TOP_ROW_DATA: AddCSVRow (string, false); break;
      }
      title_code = CSV_TOP_ROW_DATA;
    }
  }

  /** Get the number of columns in the table
   * @return the number of columns */
  public int GetNColumns ()
  {
    return columns.size ();
  }

  /** Get the number of rows in the table
   * @return the number of rows */
  public int GetNRows ()
  {
    CSVDatabaseColumn column;
    
    if (GetNColumns () <= 0) return 0;
    column = columns.get(0);
    return column.GetNRows ();
  }

  /** Find the column with the given name
   * @param name the name to find
   * @return the column index
   * @throws CSVDatabaseException if the column could not be found */
  public int FindColumn (String name)
  throws CSVDatabaseException
  {
    int count;
    CSVDatabaseColumn column;
    
    for (count=0; count<columns.size(); count++)
    {
      column = columns.get (count);
      if (column.GetName().equals (name)) return count;
    }
    
    throw (new CSVDatabaseException ("Can't find column: " + name));
  }

  /** Get a column from its index
   * @param the index of the column (0..n_columns-1)
   * @return the column */
  public CSVDatabaseColumn GetColumn (int index)
  {
      return columns.get (index);
  }

  /** Find the first record in the table with data that matches that given
   * @param name the name of the column to search
   * @param search the data to serch for. Strings are converted to the
   * appropriate data type for the column, using the Parse() method of
   * the primitive data types wrapper classes.
   * @param case_ind true for a case indepedant search
   * @return the index of the row that was found or -1 for no match
   * @throws CSVDatabaseException if the column could not be found */
  public int FindFirst (String name, String search, boolean case_ind)
  throws CSVDatabaseException
  {
    search_col_index = FindColumn (name);
    search_row_index = 0;
    search_string = search;
    search_case_ind = case_ind;
    return FindNext ();
  }

  /** Find the first record in the table with data that matches that given
   * @param col the index of the column to search
   * @param search the data to serch for. Strings are converted to the
   * appropriate data type for the column, using the Parse() method of
   * the primitive data types wrapper classes.
   * @param case_ind true for a case indepedant search
   * @return the index of the row that was found or -1 for no match
   * @throws CSVDatabaseException if the column is out of bounds */
  public int FindFirst (int col, String search, boolean case_ind)
  throws CSVDatabaseException
  {
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    search_col_index = col;
    search_row_index = 0;
    search_string = search;
    search_case_ind = case_ind;
    return FindNext ();
  }
  
  /** Find the first record in the table with data that matches that given
   * in any of the databases's columns
   * @param search the data to serch for. Strings are converted to the
   * appropriate data type for the column, using the Parse() method of
   * the primitive data types wrapper classes.
   * @param case_ind true for a case indepedant search
   * @return the index of the row that was found or -1 for no match */
  public int FindFirst (String search, boolean case_ind)
  {
    search_col_index = -1;
    search_row_index = 0;
    search_string = search;
    search_case_ind = case_ind;
    return FindNext ();
  }
  
  /** Find the next matching row. The FindFirst method must be called
   * before calling FindNext
   * @return the index of the row that was found or -1 for no match */
  public int FindNext ()
  {
    int start_col, end_col, row, col;
    String string;
    CSVDatabaseColumn column;
    
    // set up the columns that will be searched
    if (search_col_index < 0)
    {
      start_col = 0;
      end_col = columns.size () -1;
    }
    else start_col = end_col = search_col_index;
    
    // until we get a match...
    try
    {
      while (search_row_index < GetNRows())
      {
        // store and update the row counter
        row = search_row_index ++;
        
        // for each column...
        for (col=start_col; col<=end_col; col++)
        {
          // get the data for this column and compare it
          column = columns.get (col);
          string = column.GetData (row);
          if (search_case_ind)
          {
            if (string.equalsIgnoreCase(search_string))
              return row;
          }
          else
          {
            if (string.equals(search_string))
              return row;
          }
        }
      }
    }
    catch (Exception e) { }
    
    // if you get here, then no match was found
    return -1;
  }
  
  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of integer type */
  public int GetIntData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetIntData (row);
  }

  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of long integer type */
  public long GetLongData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetLongData (row);
  }

  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of float type */
  public float GetFloatData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetFloatData (row);
  }

  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of double type */
  public double GetDoubleData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetDoubleData (row);
  }

  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of boolean type */
  public boolean GetBooleanData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetBooleanData (row);
  }

  /** Retrieve data from the database
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds or
   *         if the data is not of String type */
  public String GetStringData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetStringData (row);
  }

  /** Retrieve data from the database in string form, independant of
   * its data type
   * @param row the row index
   * @param col the column index
   * @return the data
   * @throws CSVDatabaseException if the row or column are out of bounds */
  public String GetData (int row, int col)
  throws CSVDatabaseException
  {
    CSVDatabaseColumn column;
    
    if (col < 0 || col >= columns.size())
      throw (new CSVDatabaseException ("Column index out of range [" + Integer.toString (col) + "/" + Integer.toString (columns.size()) + "]"));
    column = columns.get (col);
    return column.GetData (row);
  }
  
  /** Get a row from the table in CSV format
   * @param row the row to retrieve
   * @return the CSV format string
   * @throws CSVDatabaseException if the row is out of range */
  public String GetCSVRow (int row)
  throws CSVDatabaseException
  {
    int count;
    CSVDatabaseColumn column;
    String field;
    StringBuffer buffer;

    buffer = new StringBuffer ();
    for (count=0; count<columns.size(); count ++)
    {
      column = columns.get (count);
      field = column.GetData (row);
      if (count > 0) buffer.append (",");
      if (field != null) buffer.append (ToCSVString (field));
    }
    
    return new String (buffer);
  }

  /** Convert a string to a form that can be saved in a CSV file
   * @param string the string to convert
   * @return the CSV format string */
  public static String ToCSVString (String string)
  {
    int count;
    char character;
    StringBuffer buffer;
    
    // if there are no commas or double quotes then we don't need to do anything
    if ((string.indexOf(',') < 0) && (string.indexOf ("\"") < 0))
      return string;
    
    // create a string buffer with a starting double quote
    buffer = new StringBuffer ("\"");
    
    // work through the string, doubling double quote characters
    for (count=0; count<string.length(); count++)
    {
      character = string.charAt (count);
      if (character == '"') buffer.append ('"');
      buffer.append (character);
    }

    // terminate the string with a double quote and return it
    buffer.append ("\"");
    return new String (buffer);
  }

  /** read the header from a CSV file
   * @param file the file to read
   * @return an array of strings, one per header field */
  public static Vector<String> readHeader (File file)
  throws FileNotFoundException, IOException
  {
    BufferedReader reader;
    String string;
    CSVInterpreter interpreter;
    Vector<String> array;
    
    reader = new BufferedReader (new FileReader (file));
    string = reader.readLine();
    reader.close();
    
    interpreter = new CSVInterpreter (string);
    array = new Vector<String> ();
    while (interpreter.hasMoreElements())
        array.add (interpreter.nextElement());
    
    return array;
  }
  
  //----------------------------------------------------------------------------
  // -- below this point is the implementation of the TableModel interface
  // -- the current implentation does not allow table edits - it is read-only
  //----------------------------------------------------------------------------

    /** not part of the interface, but needed to call the TableModelListeners
     * when updates to the data have been made */
    public void notifyListenersOfUpdate ()
    {
         int count;
         TableModelListener client;
         
         for (count=0; count<listeners.size(); count++)
         {
             client = listeners.elementAt(count);
             client.tableChanged(new TableModelEvent (this, TableModelEvent.HEADER_ROW));
         }
    }
  
   /** Adds a listener to the list that is notified each time a change to the data model occurs. */
    public void addTableModelListener(TableModelListener l) 
    {
        listeners.add (l);
    }
    
    /** Returns the most specific superclass for all the cell values in the column.  */
    public Class getColumnClass(int columnIndex) 
    {
        CSVDatabaseColumn column;
        Long type_long;
        Integer type_int;
        Float type_float;
        Double type_double;
        Boolean type_boolean;
        String type_string;

        column = columns.get (columnIndex);
        switch (column.GetType ())
        {
        case CSVDatabaseColumn.TYPE_FLOAT:
            type_float = new Float (0.0);
            return type_float.getClass();
        case CSVDatabaseColumn.TYPE_DOUBLE:
            type_double = new Double (0.0);
            return type_double.getClass();
        case CSVDatabaseColumn.TYPE_INT:
            type_int = new Integer (0);
            return type_int.getClass();
        case CSVDatabaseColumn.TYPE_LONG:
            type_long = new Long (0);
            return type_long.getClass();
        case CSVDatabaseColumn.TYPE_BOOLEAN:
            type_boolean = new Boolean (false);
            return type_boolean.getClass();
        case CSVDatabaseColumn.TYPE_STRING:
            type_string = new String ();
            return type_string.getClass();
        }
        return null;
    }
          
    /** Returns the number of columns in the model. */
    public int getColumnCount()
    {
        return GetNColumns();
    }
   
    /** Returns the name of the column at columnIndex. */
    public String getColumnName(int columnIndex) 
    {
        CSVDatabaseColumn column;

        column = columns.get (columnIndex);
        return column.GetName ();
    }
          
    /** Returns the number of rows in the model. */
    public int getRowCount() 
    {
        CSVDatabaseColumn column;

        column = columns.get (0);
        return column.GetNRows ();
    }

    /** Returns the value for the cell at columnIndex and rowIndex. */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        CSVDatabaseColumn column;
        Long type_long;
        Integer type_int;
        Float type_float;
        Double type_double;
        Boolean type_boolean;

        column = columns.get (columnIndex);
        try
        {
            switch (column.GetType ())
            {
            case CSVDatabaseColumn.TYPE_FLOAT:
                type_float = new Float (column.GetFloatData (rowIndex));
                return type_float;
            case CSVDatabaseColumn.TYPE_DOUBLE:
                type_double = new Double (column.GetDoubleData (rowIndex));
                return type_double;
            case CSVDatabaseColumn.TYPE_INT:
                type_int = new Integer (column.GetIntData (rowIndex));
                return type_int;
            case CSVDatabaseColumn.TYPE_LONG:
                type_long = new Long (column.GetLongData (rowIndex));
                return type_long;
            case CSVDatabaseColumn.TYPE_BOOLEAN:
                type_boolean = new Boolean (column.GetBooleanData (rowIndex));
                return type_boolean;
            case CSVDatabaseColumn.TYPE_STRING:
                return column.GetStringData (rowIndex);
            }
        }
        catch (Exception e) { }
        return null;
    }
    
    /** Returns true if the cell at rowIndex and columnIndex is editable. */
     public boolean isCellEditable(int rowIndex, int columnIndex) 
     {
         return false;
     }

     /** Removes a listener from the list that is notified each time a change to the data model occurs. */
     public void removeTableModelListener(TableModelListener l) 
     {
         int count;
         
         for (count=0; count<listeners.size(); count++)
         {
             if (listeners.elementAt(count) == l)
                 listeners.removeElementAt(count);
         }
     }

     /** Sets the value in the cell at columnIndex and rowIndex to aValue. */
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) 
     {
        // do nothing - the table is not editable
     }
 
}
