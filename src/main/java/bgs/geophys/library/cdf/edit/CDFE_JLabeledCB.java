package bgs.geophys.library.cdf.edit;

//$Id: CDFE_JLabeledCB.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

/**
 * A JComboBox with a label.
 *
 * Most methods are wrappers to the same method found on java.swing.JComboBox
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 *
 * @see java.swing.JComboBox
 *
 */
public class CDFE_JLabeledCB extends CDFE_AbstractLabeledComponent {

    static final long serialVersionUID = 1L;

    /**
     * The JComboBox
     */
    public	JComboBox	comboBox;

    /**
     * If an instance is created using the constructor that accepts 
     * a <tt>Vector</tt> this is set to the vector.  It is used to 
     * reset the comboBox back to it's original contents.
     */
    public      Vector          defaultItems;

    // Implementation of interface routines

    public Object get() {
	return comboBox.getSelectedItem();
    }

    public void set(Object obj) {
	comboBox.setSelectedItem(obj);
	if (comboBox.isEditable()) {
	    JTextField tf = (JTextField)comboBox.getEditor().getEditorComponent();
	    tf.setCaretPosition(0);
	}
    }

    public void reset() {
	comboBox.removeAllItems();
	if (defaultItems != null) {
	    for (int i = 0 ; i < defaultItems.size() ; i++)
		comboBox.addItem( (String)defaultItems.elementAt(i) );
	}

	comboBox.setSelectedIndex(-1);
    }

    public void addItem(Object obj) {
	comboBox.addItem(obj);
    }

    public void setEnabled(boolean enabled) {
	if (enabled) {
	    comboBox.setEnabled(true);
	    label.setForeground(enabledColor);
	} else {
	    comboBox.setEnabled(false);
	    label.setForeground(disabledColor);
	}
    }

    /**
     * Creates a Box containing a JComboBox and a JLabel.
     * The JCombobox is initialized with the specified items. 
     * The position of the label defaults to NORTH. 
     * 
     * @param	str	  	Label of the ComboBox	
     * @param	items	        Items contained in the ComboBox.	
     * @param      editable 	If true, make the combobox editable.
     */
    public CDFE_JLabeledCB (String str, Vector items, boolean editable, 
		       boolean enabled )  {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	label = new JLabel(str);

	if (items != null) {
	    // Have to clone the Vector since the CB controls it.
	    defaultItems = (Vector)items.clone();
	    comboBox = new JComboBox( items);
	} else
	    comboBox = new JComboBox();

        comboBox.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setEditable(editable);
	if (editable)
	    CDFE_CutAndPaste.register((JTextField)comboBox.getEditor().getEditorComponent());

	
	enabledColor  = Color.black;
	disabledColor = new Color(142, 142, 142);
	setEnabled(enabled);

        add(label);
        add(Box.createVerticalStrut(3));
        add(comboBox);
    }

    public CDFE_JLabeledCB (String str, Vector items, boolean editable) {
	this (str, items, editable, true);
    }

    /**
     * Creates a JComboBox with the given label.
     * The position of the label defaults to NORTH. 
     * 
     * @param	str	  	Label of the ComboBox	
     * @param      editable 	If true, make the combobox editable.
     */
    public CDFE_JLabeledCB (String str, boolean editable)  {
	this(str, null, editable);
    }

    public void setIndex(int index) {
	comboBox.setSelectedIndex(index);
    }

    public void addItemListener(ItemListener aListener) {
	comboBox.addItemListener(aListener);
    }

    public void removeItemListener(ItemListener aListener) {
	comboBox.removeItemListener(aListener);
    }

    /**
     * Returns string representations of all the items contained
     * in this combo box.
     *
     * @return A string array containing the string reps of the items.
     */
    public String [] getStringItems() {
	String [] items = null;
	int count = comboBox.getItemCount();
	items = new String [count];
	for (int i=0;i<count;i++)
	    items[i] = comboBox.getItemAt(i).toString();

	return items;
    }

    /**
     * Checks to see if the string is contained in the list.
     * This is only useful for comboBoxes that contain only strings.
     *
     * @return True if the item is in the list of items.
     */
    public boolean contains(String item) {
	int count = comboBox.getItemCount();

	for (int i=0;i<count;i++)
	    if (comboBox.getItemAt(i).toString().equals(item))
		return true;
	
	return false;
    }

    public JComboBox getComboBox() {
	return comboBox;
    }

    public int getIndex() {
        return comboBox.getSelectedIndex();
    }

}  // End CDFE_JLabeledCB
