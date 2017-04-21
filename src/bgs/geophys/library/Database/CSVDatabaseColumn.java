/*
 * DatabaseColumn.java
 *
 * Created on 09 May 2002, 16:45
 */

package bgs.geophys.library.Database;

import java.util.Vector;

/**
 *
 * @author  smf
 * @version 
 */
public class CSVDatabaseColumn extends Object 
{

  /** code to indicate data is of type float */
  public static final int TYPE_FLOAT      = 1;
  /** code to indicate data is of type double */
  public static final int TYPE_DOUBLE     = 2;
  /** code to indicate data is of type int */
  public static final int TYPE_INT        = 3;
  /** code to indicate data is of type long */
  public static final int TYPE_LONG       = 4;
  /** code to indicate data is of type boolean */
  public static final int TYPE_BOOLEAN    = 5;
  /** code to indicate data is of type double */
  public static final int TYPE_STRING     = 6;

  // private member variables
  private String name;              // the column name
  private Vector<Float> float_data; // the data for this column
  private Vector<Double> double_data;
  private Vector<Integer> int_data;
  private Vector<Long> long_data;
  private Vector<Boolean> boolean_data;
  private Vector<String> string_data;
  private int data_type;            // code for the type of data held in the array
  
  /** Creates new DatabaseColumn
   * @param name the name of the column
   * @param data_type one of the data type codes */
  public CSVDatabaseColumn(String name, int data_type) 
  {
    float_data = new Vector <Float> ();
    double_data = new Vector <Double> ();
    int_data = new Vector <Integer> ();
    long_data = new Vector <Long> ();
    boolean_data = new Vector <Boolean> ();
    string_data = new Vector <String> ();
    this.name = name;
    this.data_type = data_type;
  }

  /** rename this column
   * @param name the new name */
  public void Rename (String name)
  {
    this.name = name;
  }

  /** Get the number of rows in this column
   * @return the number of rows */
  public int GetNRows ()
  {
    switch (data_type)
    {
    case TYPE_FLOAT: return float_data.size();
    case TYPE_DOUBLE: return double_data.size();
    case TYPE_INT: return int_data.size();
    case TYPE_LONG: return long_data.size();
    case TYPE_BOOLEAN: return boolean_data.size();
    case TYPE_STRING: return string_data.size();
    }
    return 0;
  }
  
  /** Get the name for this column
   * @return the column name */
  public String GetName ()
  {
    return name;
  }

  /** Get the data type for this column
   * @return the data type code */
  public int GetType ()
  {
    return data_type;
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of integer type */
  public int GetIntData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_INT)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return (int_data.get (row)).intValue ();
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of long type */
  public long GetLongData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_LONG)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return (long_data.get (row)).longValue ();
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of float type */
  public float GetFloatData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_FLOAT)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return (float_data.get (row)).floatValue ();
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of double type */
  public double GetDoubleData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_DOUBLE)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return (double_data.get (row)).doubleValue ();
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of boolean type */
  public boolean GetBooleanData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_BOOLEAN)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return (boolean_data.get (row)).booleanValue ();
  }

  /** Retrieve data from the data array
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds or
   *         if the data is not of String type */
  public String GetStringData (int row)
  throws CSVDatabaseException
  {
    if (row < 0 || row >= GetNRows ())
      throw (new CSVDatabaseException ("Row index out of range [" + Integer.toString (row) + "/" + Integer.toString (GetNRows()) + "]"));
    if (data_type != TYPE_STRING)
      throw (new CSVDatabaseException ("Incorrect data type requested"));
    return string_data.get (row);
  }

  /** Convert data to a string and return it
   * @param row the row index
   * @return the data
   * @throws CSVDatabaseException if the row is out of bounds */
  public String GetData (int row)
  throws CSVDatabaseException
  {
    switch (data_type)
    {
    case TYPE_FLOAT: return (Float.toString (GetFloatData (row)));
    case TYPE_DOUBLE: return (Double.toString (GetDoubleData (row)));
    case TYPE_INT: return (Integer.toString (GetIntData (row)));
    case TYPE_LONG: return (Long.toString (GetLongData (row)));
    case TYPE_BOOLEAN:
      if (GetBooleanData (row)) return new String ("true");
      return new String ("false");
    case TYPE_STRING: return (GetStringData (row));
    }
    return null;
  }
  
  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (int data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_INT)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    this.int_data.add (new Integer (data));
  }
  
  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (long data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_LONG)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    this.long_data.add (new Long (data));
  }

  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (float data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_FLOAT)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    this.float_data.add (new Float (data));
  }

  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (double data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_DOUBLE)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    this.double_data.add (new Double (data));
  }

  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (boolean data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_BOOLEAN)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    this.boolean_data.add (new Boolean (data));
  }

  /** Add a new data element to this column
   * @param data the data to add
   * @throws CSVDatabaseException if the data is not of the correct type */
  public void Add (String data)
  throws CSVDatabaseException
  {
    if (data_type != TYPE_STRING)
      throw (new CSVDatabaseException ("Attempt to add incorrect data type"));
    string_data.add (data);
  }

  /** Add an empty data element to this column */
  public void Add ()
  {
    switch (data_type)
    {
    case TYPE_FLOAT:
      float_data.add (new Float (0.0));
      break;
    case TYPE_DOUBLE:
      double_data.add (new Double (0.0));
      break;
    case TYPE_INT:
      int_data.add (new Integer (0));
      break;
    case TYPE_LONG:
      long_data.add (new Long (0));
      break;
    case TYPE_BOOLEAN:
      boolean_data.add (new Boolean (false));
      break;
    case TYPE_STRING:
      string_data.add (new String (""));
      break;
    }
  }

  /** empty this column of all data */
  public void Empty ()
  {
    float_data.removeAllElements();
    double_data.removeAllElements();
    int_data.removeAllElements();
    long_data.removeAllElements();
    boolean_data.removeAllElements();
    string_data.removeAllElements();
  }
  
}
