/*
 * Vector2d.java
 *
 * Created on 05 December 2005, 20:55
 */

package bgs.geophys.library.Misc;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A class the implements a 2 dimensional version of a Vector
 *
 * @author  Administrator
 */
public class Vector2d <Q> extends Object
{

    /** a class to hold a 2D index value */
    public static class Index2d
    {
        public int row_index;
        public int column_index;
        public Index2d (int r, int c) { row_index=r; column_index=c; }
    }
    
    /** a class to hold a single sort criterion */
    private static class SortCriterion
    {
        public int column_index;    // the index of the column that will be sorted
        public boolean ascending;   // true to sort ascending, false for descending
        public SortCriterion (int column_index, boolean ascending)
        {
            this.column_index = column_index;
            this.ascending = ascending;
        }
    }
    
    /** a class to hold a sort description - an array of sort criteria */
    public static class SortDescription
    {
        private Vector<SortCriterion> criteria;
        public SortDescription () { criteria = new Vector<SortCriterion> (); }
        public void addCriterion (int column_index, boolean ascending)
        {
            SortCriterion criterion;
            criterion = new SortCriterion (column_index, ascending);
            criteria.add (criterion);
        }
        public int getNCriteria () { return criteria.size (); }
        public SortCriterion getCriterion (int index) { return criteria.get(index); }
    }
    
    // the array is implemented as a vector of vectors - this is the top vector
    private Vector<Vector<Q>> columns;
    
    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment. 
     *
     * @param   initialRowCapacity     the initial number of rows in the vector.
     * @param   rowCapacityIncrement   the amount by which the row capacity is
     *                                 increased when the vector overflows.
     * @param   initialColumnCapacity     the initial number of columns in the vector.
     * @param   columnCapacityIncrement   the amount by which the column capacity is
     *                                    increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public Vector2d (int initialRowCapacity, int rowCapacityIncrement,
                     int initialColumnCapacity, int columnCapacityIncrement)
    {
        int count;
        
        if (initialRowCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialRowCapacity);
        if (initialColumnCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialColumnCapacity);
        columns = new Vector<Vector<Q>> (initialColumnCapacity, columnCapacityIncrement);
        for (count=0; count<initialColumnCapacity; count++)
            columns.add (new Vector<Q> (initialRowCapacity, rowCapacityIncrement));
    }

    /**
     * Constructs an empty vector with the specified initial capacity and 
     * with its capacity increment equal to zero.
     *
     * @param   initialRowCapacity     the initial number of rows in the vector.
     * @param   initialColumnCapacity     the initial number of columns in the vector.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public Vector2d (int initialRowCapacity, int initialColumnCapacity)
    {
	this(initialRowCapacity, 0, initialColumnCapacity, 0);
    }

    /**
     * Constructs an empty vector so that its internal data array 
     * has size <tt>10</tt> and its standard capacity increment is 
     * zero. 
     */
    public Vector2d () 
    {
	this(0, 0);
    }

    /**
     * Trims the capacity of this vector to be the vector's current 
     * size. If the capacity of this vector is larger than its current 
     * size, then the capacity is changed to equal the size by replacing 
     * its internal data array, kept in the field <tt>elementData</tt>, 
     * with a smaller one. An application can use this operation to 
     * minimize the storage of a vector. 
     */
    public synchronized void trimToSize() 
    {
        int count;
        
        for (count=0; count<columns.size(); count++)
            (columns.get(count)).trimToSize();
        columns.trimToSize();
    }

    /**
     * Increases the capacity of this vector, if necessary, to ensure 
     * that it can hold at least the number of components specified by 
     * the minimum capacity argument.
     *
     * <p>If the current capacity of this vector is less than
     * <tt>minCapacity</tt>, then its capacity is increased by replacing its
     * internal data array, kept in the field <tt>elementData</tt>, with a
     * larger one.  The size of the new data array will be the old size plus
     * <tt>capacityIncrement</tt>, unless the value of
     * <tt>capacityIncrement</tt> is less than or equal to zero, in which case
     * the new capacity will be twice the old capacity; but if this new size
     * is still smaller than <tt>minCapacity</tt>, then the new capacity will
     * be <tt>minCapacity</tt>.
     *
     * @param minRowCapacity the desired minimum row capacity.
     * @param minColumnCapacity the desired minimum column capacity.
     */
    public synchronized void ensureCapacity(int minRowCapacity, int minColumnCapacity) 
    {
        int count;
        
        for (count=0; count<columns.size(); count++)
            columns.get(count).ensureCapacity(minRowCapacity);
        columns.ensureCapacity(minColumnCapacity);
    }
    
    /**
     * Sets the size of this vector. If the new size is greater than the 
     * current size, new <code>null</code> items are added to the end of 
     * the vector. If the new size is less than the current size, all 
     * components at index <code>newSize</code> and greater are discarded.
     *
     * @param   newRowSize      the new column size of this vector.
     * @param   newColumnSize   the new row size of this vector.
     * @throws  ArrayIndexOutOfBoundsException if new size is negative.
     */
    public synchronized void setSize(int newRowSize, int newColumnSize) 
    {
        int count;
        
        for (count=0; count<columns.size(); count++)
            columns.get(count).setSize(newRowSize);
        columns.setSize(newColumnSize);
    }

    /**
     * Returns the current capacity of this vector.
     *
     * @return  the current row capacity (the length of its internal 
     *          data array, kept in the field <tt>elementData</tt> 
     *          of this vector).
     */
    public synchronized Index2d capacity() 
    {
        if (columns.size() <= 0) return new Index2d (0, 0);
        return new Index2d ((columns.get(0)).capacity(), columns.capacity());
    }

    /**
     * Returns the number of components in this vector.
     *
     * @return  the number of components in this vector.
     */
    public synchronized Index2d size() 
    {
        if (columns.size() <= 0) return new Index2d (0, 0);
        return new Index2d ((columns.get(0)).size(), columns.size());
    }

    /**
     * Tests if this vector has no components.
     *
     * @return  <code>true</code> if and only if this vector has 
     *          no components, that is, its size is zero;
     *          <code>false</code> otherwise.
     */
    public synchronized boolean isEmpty() 
    {
	if (columns.isEmpty()) return true;
        if ((columns.get(0)).isEmpty()) return true;
        return false;
    }

    /**
     * Returns an enumeration of the components of this vector. The 
     * returned <tt>Enumeration</tt> object will generate all items in 
     * this vector. The first item generated is the item at index <tt>0</tt>, 
     * then the item at index <tt>1</tt>, and so on. 
     *
     * @param   columnIndex the index of the column to enumerate
     * @return  an enumeration of the components of this vector.
     * @see     Enumeration
     * @see     Iterator
     */
    public Enumeration rowElements(final int rowIndex) 
    {
	return new Enumeration() 
        {
	    int count = 0;
            int row_index = rowIndex;

	    public boolean hasMoreElements() 
            {
                if (row_index >= columns.size()) return false;
                if (count < columns.get(row_index).size()) return true;
                return false;
	    }

	    public Q nextElement() 
            {
                Vector<Q> column;
                
                synchronized (columns)
                {
                    if (row_index < columns.size())
                    {
                        column = columns.get(row_index);
                        synchronized (column)
                        {
                            if (count < column.size()) return column.get(count ++);
                        }
                    }
                }
		throw new NoSuchElementException("Vector Enumeration");
	    }
	};
    }

    /**
     * Returns an enumeration of the components of this vector. The 
     * returned <tt>Enumeration</tt> object will generate all items in 
     * this vector. The first item generated is the item at index <tt>0</tt>, 
     * then the item at index <tt>1</tt>, and so on. 
     *
     * @param   rowIndex the index of the row to enumerate
     * @return  an enumeration of the components of this vector.
     * @see     Enumeration
     * @see     Iterator
     */
    public Enumeration columnElements(int columnIndex)
    {
        if (columnIndex >= columns.size())
        {
            return new Enumeration() 
            {
                public boolean hasMoreElements() { return false; }
                public Q nextElement() { throw new NoSuchElementException("Vector Enumeration"); }
            };
        }
        return (columns.get (columnIndex)).elements();
    }
    
    /**
     * Tests if the specified object is a component in this vector.
     *
     * @param   elem   an object.
     * @return  <code>true</code> if and only if the specified object 
     * is the same as a component in this vector, as determined by the 
     * <tt>equals</tt> method; <code>false</code> otherwise.
     */
    public boolean contains(Q elem) 
    {
	return indexOf(elem, 0, 0) != null;
    }

    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <code>equals</code> method. 
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>null</code> if the object is not found.
     * @see     Object#equals(Object)
     */
    public Index2d indexOf(Q elem) {
	return indexOf(elem, 0, 0);
    }

    /**
     * Searches for the first occurence of the given argument, beginning 
     * the search at <code>index</code>, and testing for equality using 
     * the <code>equals</code> method. 
     *
     * @param   elem    an object.
     * @param   rowIndex      the non-negative row index to start searching from.
     * @param   columnIndex   the non-negative column index to start searching from.
     * @return  the index of the first occurrence of the object argument in
     *          this vector at position <code>index</code> or later in the
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &gt;= index)</tt> is 
     *          <tt>true</tt>; returns <code>null</code> if the object is not 
     *          found. (Returns <code>null</code> if <tt>index</tt> &gt;= the
     *          current size of this <tt>Vector</tt>.)
     * @exception  IndexOutOfBoundsException  if <tt>index</tt> is negative.
     * @see     Object#equals(Object)
     */
    public synchronized Index2d indexOf(Q elem, int rowIndex, int columnIndex)
    {
        int column_count, row_count;
        Q object;
        
        for (column_count=columnIndex; column_count<columns.size(); column_count++)
        {
            for (row_count=rowIndex; row_count<(columns.get(0)).size(); row_count++)
            {
                object = (columns.get(column_count)).get(row_count);
        	if (elem == null)
                {
                    if (object == null) return new Index2d (row_count, column_count);
                }
                else if (object.equals(elem)) return new Index2d (row_count, column_count);
            }
        }
	return null;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this vector.
     *
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified object in
     *          this vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>null</code> if the object is not found.
     */
    public synchronized Index2d lastIndexOf(Q elem) 
    {
        Index2d size;
        
        size = size ();
	return lastIndexOf(elem, size.row_index-1, size.column_index-1);
    }

    /**
     * Searches backwards for the specified object, starting from the 
     * specified index, and returns an index to it. 
     *
     * @param  elem    the desired component.
     * @param   rowIndex      the non-negative row index to start searching from.
     * @param   columnIndex   the non-negative column index to start searching from.
     * @return the index of the last occurrence of the specified object in this
     *          vector at position less than or equal to <code>index</code> in
     *          the vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &lt;= index)</tt> is 
     *          <tt>true</tt>; <code>null</code> if the object is not found.
     *          (Returns <code>null</code> if <tt>index</tt> is negative.)
     * @exception  IndexOutOfBoundsException  if <tt>index</tt> is greater
     *             than or equal to the current size of this vector.
     */
    public synchronized Index2d lastIndexOf(Q elem, int rowIndex, int columnIndex) 
    {
        int column_count, row_count;
        Q object;
        Index2d size;
        
        size = size ();
        if (rowIndex >= size.row_index)
            throw new IndexOutOfBoundsException(rowIndex + " >= "+ size.row_index);
        if (columnIndex >= size.column_index)
            throw new IndexOutOfBoundsException(columnIndex + " >= "+ size.column_index);

        for (column_count=columnIndex; column_count>=0; column_count--)
        {
            for (row_count=rowIndex; row_count<0; row_count--)
            {
                object = (columns.get(column_count)).get(row_count);
        	if (elem == null)
                {
                    if (object == null) return new Index2d (row_count, column_count);
                }
                else if (object.equals(elem)) return new Index2d (row_count, column_count);
            }
        }
	return null;
    }

    /**
     * Returns the component at the specified index.<p>
     *
     * This method is identical in functionality to the get method
     * (which is part of the List interface).
     *
     * @param   rowIndex      the non-negative row index.
     * @param   columnIndex   the non-negative column index.
     * @return     the component at the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the <tt>index</tt> 
     *             is negative or not less than the current size of this 
     *             <tt>Vector</tt> object.
     *             given.
     * @see	   #get(int)
     * @see	   List
     */
    public synchronized Q elementAt(int rowIndex, int columnIndex) 
    {
        Index2d size;
        
        size = size ();
        if (rowIndex >= size.row_index)
            throw new IndexOutOfBoundsException(rowIndex + " >= "+ size.row_index);
        if (columnIndex >= size.column_index)
            throw new IndexOutOfBoundsException(columnIndex + " >= "+ size.column_index);

        return (columns.get(columnIndex)).get(rowIndex);
    }

    /**
     * Returns the first component (the item at index <tt>0</tt>) of 
     * this vector.
     *
     * @return     the first component of this vector.
     * @exception  NoSuchElementException  if this vector has no components.
     */
    public synchronized Q firstElement() 
    {
        Index2d size;
        
        size = size ();
	if (size.column_index <= 0 || size.row_index <= 0) 
	    throw new NoSuchElementException();
        
        return (columns.get(0)).get(0);
    }

    /**
     * Returns the last component of the vector.
     *
     * @return  the last component of the vector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @exception  NoSuchElementException  if this vector is empty.
     */
    public synchronized Q lastElement() {
        Index2d size;
        
        size = size ();
	if (size.column_index <= 0 || size.row_index <= 0) 
	    throw new NoSuchElementException();
        
        return (columns.get(size.column_index-1)).get(size.row_index-1);
    }

    /**
     * Sets the component at the specified <code>index</code> of this 
     * vector to be the specified object. The previous component at that 
     * position is discarded.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the set method
     * (which is part of the List interface). Note that the set method reverses
     * the order of the parameters, to more closely match array usage.  Note
     * also that the set method returns the old value that was stored at the
     * specified position.
     *
     * @param      obj     what the component is to be set to.
     * @param   rowIndex      the non-negative row index.
     * @param   columnIndex   the non-negative column index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see        List
     * @see	   #set(int, java.lang.Object)
     */
    public synchronized void setElementAt(Q obj, int rowIndex, int columnIndex) 
    {
        Index2d size;
        
        size = size ();
        if (rowIndex >= size.row_index)
            throw new ArrayIndexOutOfBoundsException(rowIndex + " >= "+ size.row_index);
        if (columnIndex >= size.column_index)
            throw new ArrayIndexOutOfBoundsException(columnIndex + " >= "+ size.column_index);
        
        (columns.get(columnIndex)).setElementAt(obj, rowIndex);
    }

    /**
     * Deletes the component at the specified index. Each component in 
     * this vector with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this vector 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the remove method
     * (which is part of the List interface).  Note that the remove method
     * returns the old value that was stored at the specified position.
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #remove(int)
     * @see	   List
     */
    public synchronized void removeRow(int index) 
    {
        int count;
        
        for (count=0; count<columns.size(); count++)
            (columns.get(count)).remove (index);
    }

    /**
     * Deletes the component at the specified index. Each component in 
     * this vector with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this vector 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the remove method
     * (which is part of the List interface).  Note that the remove method
     * returns the old value that was stored at the specified position.
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #remove(int)
     * @see	   List
     */
    public synchronized void removeColumn(int index) 
    {
        columns.remove(index);
    }
    
    /**
     * Inserts the specified object as a component in this vector at the 
     * specified <code>index</code>. Each component in this vector with 
     * an index greater or equal to the specified <code>index</code> is 
     * shifted upward to have an index one greater than the value it had 
     * previously. <p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than or equal to the current size of the vector. (If the
     * index is equal to the current size of the vector, the new element
     * is appended to the Vector.)<p>
     *
     * This method is identical in functionality to the add(Object, int) method
     * (which is part of the List interface). Note that the add method reverses
     * the order of the parameters, to more closely match array usage.
     *
     * @param      index   where to insert the new row.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #add(int, Object)
     * @see	   List
     */
    public synchronized void insertRow(int index) 
    {
        int count;

        for (count=0; count<columns.size(); count++)
            (columns.get(count)).insertElementAt(null, index);
    }

    /**
     * Inserts the specified object as a component in this vector at the 
     * specified <code>index</code>. Each component in this vector with 
     * an index greater or equal to the specified <code>index</code> is 
     * shifted upward to have an index one greater than the value it had 
     * previously. <p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than or equal to the current size of the vector. (If the
     * index is equal to the current size of the vector, the new element
     * is appended to the Vector.)<p>
     *
     * This method is identical in functionality to the add(Object, int) method
     * (which is part of the List interface). Note that the add method reverses
     * the order of the parameters, to more closely match array usage.
     *
     * @param      index   where to insert the new column.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #add(int, Object)
     * @see	   List
     */
    public synchronized void insertColumn(int index) 
    {
        int count;
        Vector<Q> column;
        Index2d size;
        
        size = size();
        
        column = new Vector<Q> ();
        for (count=0; count<size.row_index; count++) column.add (null);
        columns.insertElementAt(column, index);
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * This method is identical in functionality to the add(Object) method
     * (which is part of the List interface).
     *
     * @see	   #add(Object)
     * @see	   List
     */
    public synchronized void addRow() 
    {
        int count;

        for (count=0; count<columns.size(); count++)
            (columns.get(count)).add(null);
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * This method is identical in functionality to the add(Object) method
     * (which is part of the List interface).
     *
     * @see	   #add(Object)
     * @see	   List
     */
    public synchronized void addColumn() 
    {
        int count;
        Vector<Q> column;
        Index2d size;
        
        size = size();
        
        column = new Vector<Q> ();
        for (count=0; count<size.row_index; count++) column.add (null);
        columns.add(column);
    }

    /**
     * Removes all components from this vector and sets its size to zero.<p>
     *
     * This method is identical in functionality to the clear method
     * (which is part of the List interface).
     *
     * @see	#clear
     * @see	List
     */
    public synchronized void removeAllElements() 
    {
        columns.removeAllElements();
    }

    /**
     * Returns a clone of this vector. The copy will contain a
     * reference to a clone of the internal data array, not a reference 
     * to the original internal data array of this <tt>Vector</tt> object. 
     *
     * @return  a clone of this vector.
     */
    public synchronized Vector2d<Q> clone() 
    {
        int row_count, column_count;
        Vector2d<Q> v;
        Index2d size;
        
        size = size();
        v = new Vector2d<Q> (size.row_index, size.column_index);
        for (column_count=size.column_index; column_count>=0; column_count--)
        {
            for (row_count=size.row_index; row_count<0; row_count--)
            {
                v.setElementAt(this.elementAt(row_count, column_count), row_count, column_count);
            }
	}
        
        return v;
    }

    /**
     * Returns an array containing all of the elements in this Vector
     * in the correct order.
     *
     * @since 1.2
     */
//    public synchronized <T> T[] toArray(T[] a) {
//        if (a.length < elementCount)
//            a = (T[])java.lang.reflect.Array.newInstance(
//                                a.getClass().getComponentType(), elementCount);
//
//	System.arraycopy(elementData, 0, a, 0, elementCount);
//
//        if (a.length > elementCount)
//            a[elementCount] = null;
//
//        return a;
//    }
    public synchronized Q[][] toArray(Q[][] result) 
    {
        int row_count, column_count;
        Index2d this_size;
        
        this_size = size ();
        for (column_count=this_size.column_index; column_count>=0; column_count--)
        {
            for (row_count=this_size.row_index; row_count<0; row_count--)
            {
                result[row_count][column_count] = this.elementAt(row_count, column_count);
            }
        }
        
        return result;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this Vector.
     *
     * @param   rowIndex      the non-negative row index.
     * @param   columnIndex   the non-negative column index.
     * @return object at the specified index
     * @exception ArrayIndexOutOfBoundsException index is out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     * @since 1.2
     */
    public synchronized Q get(int rowIndex, int columnIndex) 
    {
        return elementAt(rowIndex, columnIndex);
    }

    /**
     * Replaces the element at the specified position in this Vector with the
     * specified element.
     *
     * @param   rowIndex      the non-negative row index.
     * @param   columnIndex   the non-negative column index.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @exception ArrayIndexOutOfBoundsException index out of range
     *		  (index &lt; 0 || index &gt;= size()).
     * @since 1.2
     */
    public synchronized Q set(int rowIndex, int columnIndex, Q element) 
    {
        Q obj;
        
        obj = get (rowIndex, columnIndex);
        setElementAt(element, rowIndex, columnIndex);
        return obj;
    }

    /**
     * Removes all of the elements from this Vector.  The Vector will
     * be empty after this call returns (unless it throws an exception).
     *
     * @since 1.2
     */
    public void clear() 
    {
        removeAllElements();
    }

    // Bulk Operations

    /**
     * Returns true if this Vector contains all of the elements in the
     * specified Collection.
     *
     * @param   c a collection whose elements will be tested for containment
     *          in this Vector
     * @return true if this Vector contains all of the elements in the
     *	       specified collection.
     * @throws NullPointerException if the specified collection is null.
     */
    public synchronized boolean containsAll(Collection<Q> c) 
    {
        boolean found;
        int row, col;
        Index2d this_size;
        Iterator<Q> iterator;
        Q obj;
        
        this_size = size();
        iterator = c.iterator();
        while (iterator.hasNext())
        {
            obj = iterator.next();
            found = false;
            for (col=0; col<this_size.column_index; col++)
            {
                for (row=0; row<this_size.row_index; row++)
                {
                    if (contains(obj)) found = true;
                }
            }
            if (! found) return false;
        }
        return true;
    }

    /**
     * Compares the specified Object with this Vector for equality.  Returns
     * true if and only if the specified Object is also a List, both Lists
     * have the same size, and all corresponding pairs of elements in the two
     * Lists are <em>equal</em>.  (Two elements <code>e1</code> and
     * <code>e2</code> are <em>equal</em> if <code>(e1==null ? e2==null :
     * e1.equals(e2))</code>.)  In other words, two Lists are defined to be
     * equal if they contain the same elements in the same order.
     *
     * @param o the Object to be compared for equality with this Vector.
     * @return true if the specified Object is equal to this Vector
     */
    public synchronized boolean equals(Object o) 
    {
        int column_count, row_count;
        Index2d this_size, other_size;
        Vector2d other;
        
        try { other = (Vector2d) o; }
        catch (ClassCastException e) { return false; }
        
        this_size = size();
        other_size = other.size();
        if (this_size.column_index != other_size.column_index) return false;
        if (this_size.row_index != other_size.row_index) return false;
        for (column_count=this_size.column_index; column_count>=0; column_count--)
        {
            for (row_count=this_size.row_index; row_count<0; row_count--)
            {
                if (! this.get (row_count, column_count).equals(other.get (row_count, column_count))) return false;
            }
        }
        return true;
    }

    /**
     * Returns a string representation of this Vector, containing
     * the String representation of each element.
     */
    public synchronized String toString() 
    {
        int column_count, row_count;
        String string;
        Index2d size;
        
        size = size();        
        string = "";
        for (column_count=size.column_index; column_count>=0; column_count--)
        {
            for (row_count=size.row_index; row_count<0; row_count--)
            {
                string += get (row_count, column_count).toString();
            }
        }
        return string;
    }

    /** sort the entire vector using the supplied criteria 
     * assumes all elements implement comparable
     * @param sort_description the criteria by which the data will be sorted */
    public void sort(SortDescription sort_description) 
    {
        Index2d size;
        
        size = size ();
        sort (sort_description, 0, size.row_index -1, 0, size.column_index -1);
    }
    
    /** sort a portion of the vector using the supplied criteria 
     * assumes all elements implement comparable
     * @param sort_description the criteria by which the data will be sorted
     * @param from_row the starting row (0..n_rows-1)
     * @param to_row the ending row  (0..n_rows-1) */
    public void sort (SortDescription sort_description, int from_row, int to_row) 
    {
        Index2d size;
        
        size = size ();
        sort (sort_description, from_row, to_row, 0, size.column_index -1);
    }
    
    /** sort a portion of the vector using the supplied criteria 
     * assumes all elements implement comparable. This is an implementation
     * of the heap sort algorithm, copied from C source code at
     * http://linux.wku.edu/~lamonml/algor/sort/heap.html
     * @param sort_description the criteria by which the data will be sorted
     * @param from_row the starting row (0..n_rows-1)
     * @param to_row the ending row  (0..n_rows-1)
     * @param from_column the starting column (0..n_columns-1)
     * @param to_column the ending column  (0..n_columns-1) */
    public void sort (SortDescription sort_description, int from_row, int to_row,
                      int from_column, int to_column) 
    {
        int i, array_size;

        array_size = (to_row - from_row) +1;
        // the 3rd parameter in siftDown was changed from 'array_size' to
        // 'array_size -1' because using array_size causes siftDown to
        // iterate beyond the end of the data array
        for (i = from_row + (array_size / 2) -1; i >= from_row; i--)
            siftDown(sort_description, i, array_size -1, from_column, to_column);

        for (i = from_row + array_size -1; i >= from_row + 1; i--)
        {
            swapRows (0, i, from_column, to_column);
            siftDown(sort_description, 0, i-1, from_column, to_column);
        }
    }
    // siftDown is part of the heap sort algorithm
    private void siftDown (SortDescription sort_description, int root, int bottom, int from_column, int to_column)
    {
        boolean done;
        int maxChild;

        done = false;
        while ((root*2 <= bottom) && (! done))
        {
            if (root*2 == bottom) maxChild = root * 2;
            else if (compare(sort_description, root *2, root * 2 + 1) > 0) maxChild = root * 2;
            else maxChild = root * 2 + 1;

            if (compare(sort_description, root, maxChild) < 0)
            {
                swapRows(root, maxChild, from_column, to_column);
                root = maxChild;
            }
            else done = true;
        }
    }

    /** swap all elements between 2 rows
     * @param row1 the row to swap
     * @param row2 the other row to swap
     * @param from_column the starting column (0..n_columns-1)
     * @param to_column the ending column  (0..n_columns-1) */
    public void swapRows (int row1, int row2, int from_column, int to_column) 
    {
        int column;
        Q tmp;
        
        for (column=from_column; column<=to_column; column++)
        {
            tmp = get (row1, column);
            set (row1, column, get (row2, column));
            set (row2, column, tmp);
        }
    }
    
    /** do a comparison - assumes all elements implement comparable
     * @param sort_description the criteria by which the data will be sorted
     * @param row1 the row to compare
     * @param row2 the other row to compare
     * @return -1 if row1 < row2, 0 if row1 == row2, +1 if row1 > row2 */
    @SuppressWarnings ("unchecked")
    public int compare (SortDescription sort_description, int row1, int row2)
    {
        int criterion_count, result;
        SortCriterion criterion;
        Q obj1, obj2;
        Comparable<Q> c1;
        
        for (criterion_count=0; criterion_count<sort_description.getNCriteria(); criterion_count++)
        {
            criterion = sort_description.getCriterion(criterion_count);
            obj1 = get (row1, criterion.column_index);
            obj2 = get (row2, criterion.column_index);
            if (obj1 instanceof Comparable && obj2 instanceof Comparable)
            {
                // next line will produce 'unchecked cast' warning - this is OK
                // because the if statement above has checked that the cast will
                // be safe - NOTE unchecked warning supressed
                c1 = (Comparable<Q>) obj1;
                result = c1.compareTo (obj2);
                if (result != 0)
                {
                    if (! criterion.ascending) result *= -1;
                    return result;
                }
            }
            else throw new ClassCastException ();
        }
        return 0;
    }
    
}
