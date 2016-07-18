package bgs.geophys.library.cdf.edit;

// $Id: CDFE_AttributePanel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

// Swing Imports
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.table.*;

// Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;

// CDF Imports
import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.*;

/**
 *
 * This class defines all the methods to build and control the global/variable attribute
 panel for the CDFE_MYEditor.  The setXXX methods will set the various
 * subpanels for the selectedAttr. If the selectedAttr is simply a string
 * (which implies a new attribute) then the widgets are updated from the
 * appropriate info. The getXXX methods will query the widgets in a given
 * subpanel and place the values in the currentAttributes Hashtable.  The 
 * keys for this hashtable correpsond to the keys for the controls Hashtable.
 *
 */
public class CDFE_AttributePanel extends JSplitPane
//			   implements CDFConstants, AttributeEventListener {
			   implements CDFConstants {    
    // The Frame
    private  CDFE_MYEditor myEditor;
    
    //private JTabbedPane infoTabPane;
    private Attribute         selectedAttr = null;
    private long		scope;

    // Listen for changes in the list
    private CDFE_AttrListListener    all;
    
    /**
     * Holds the widgets and other info that reference global/variable Attributes
     */
    private Hashtable controls          = new Hashtable();
    private Hashtable currentAttributes = new Hashtable(); // Attributes

    // Panel
    private CDFE_JLabeledPanel rightPanel;
    private CDFE_EditgAttributeTableScrollPane gDisplay;
    private CDFE_EditvAttributeTableScrollPane vDisplay;
    private CDFE_CDFSpecPanel cdfspec;

    private int maxWritten;

    static final long serialVersionUID = 1L;

    //
    // Constructor
    //
    public CDFE_AttributePanel(CDFE_MYEditor myEditor, long scope) {
	super(HORIZONTAL_SPLIT);
	this.scope = scope;
	setOneTouchExpandable(true);

	this.myEditor = myEditor;

	all = new CDFE_AttrListListener(this);

//	setBorder(new CompoundBorder(CDFE_MYEditor.loweredBorder, 
//				     CDFE_MYEditor.emptyBorder5));

	if (scope == GLOBAL_SCOPE) {
	  gDisplay  = new CDFE_EditgAttributeTableScrollPane(this);
	} else {
	  vDisplay = new CDFE_EditvAttributeTableScrollPane(this);
	}
	// Build the split pane components

	// JList to hold attribute names
	Vector attributeVector = new Vector();
	JList  listOfAttributes = new JList(attributeVector);
        listOfAttributes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listOfAttributes.addListSelectionListener( all );
	JScrollPane attributeScrollPane = new JScrollPane(listOfAttributes);
	
	controls.put("listOfAttributes", listOfAttributes);
	controls.put("attributeScrollPane", attributeScrollPane);
	controls.put("attributeVector", attributeVector);

        JViewport jvp = new JViewport();
        if (scope == GLOBAL_SCOPE)
          jvp.setView(new JLabel("gAttribute"));
        else
          jvp.setView(new JLabel("vAttribute"));
        attributeScrollPane.setColumnHeader(jvp);

	rightPanel = new CDFE_JLabeledPanel("CDF: "+myEditor.theCDF.toString(),
				       new BorderLayout());

	cdfspec = new CDFE_CDFSpecPanel();
	CDF cdf = myEditor.theCDF;
	maxWritten = CDFE_CDFToolUtils.getMaxRecNum(cdf);

	cdfspec.set(cdf);

	rightPanel.add(cdfspec, BorderLayout.NORTH);

	updateListOfAttributes();

	if (scope == GLOBAL_SCOPE)
	  rightPanel.add(gDisplay, BorderLayout.CENTER);
	else
	  rightPanel.add(vDisplay, BorderLayout.CENTER);
	setLeftComponent (attributeScrollPane);
	setRightComponent(rightPanel);

	// Provide minimum sizes for the two components in the split pane
	attributeScrollPane.setPreferredSize(new Dimension(100, 620));
	rightPanel.setPreferredSize(new Dimension(670, 620));

	// Set the initial location and size of the divider
        setDividerLocation(100);
	setDividerSize(5);

	// Provide a preferred size for the split pane
	setPreferredSize(new Dimension(myEditor.getEditWidth(), 
	                               myEditor.getEditHeight()));

    }

    //---------------------------------------------------------
    //
    //                    Event Handler Methods                   
    //
    //---------------------------------------------------------
/*
    public void performAttributeAction(AttributeEvent e) {
	int type = e.getID();

	switch (type) {
	case AttributeEvent.CREATED:
	    addToListOfAttributes(e.getAttribute());
	    reselectCurrentAttribute();
	    break;
	case AttributeEvent.DELETED:
	    removeFromListOfAttributes(e.getAttribute());
	    resetPanel(true);
	    break;
        case AttributeEvent.NAME_CHANGE:
            removeFromListOfAttributes(e.getAttribute());
            break;
	default:
	    break;
	}
    }
*/
    //---------------------------------------------------------
    //
    //                    Controller Methods                   
    //
    //---------------------------------------------------------

    /**
     * Update panel for the existing attribute
     */
    public  void updateAttrPanel(Attribute attr, boolean updategAttrTable) {
	selectedAttr = attr;

	if (attr != null) {
          if (scope == VARIABLE_SCOPE) {
	    myEditor.enablevAttrMenu();
	    vDisplay.buildTable(selectedAttr);
          } else {
	    myEditor.enablegAttrMenu();
	    if (updategAttrTable) 
	      gDisplay.buildTable();
	    else 
	      gDisplay.reset();
	  }
        } else {
	  if (scope == VARIABLE_SCOPE) vDisplay.reset();
	  else gDisplay.reset();
	}	    
	rightPanel.revalidate();
	rightPanel.repaint();
	System.gc();
    }

    /**
     * provides a software wrapper to select an attribute.  These changes are not
     * listened for.
     */
    public void reselectCurrentAttribute() {
	JList jl = (JList)controls.get("listOfAttributes");
	JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");
	
	jl.removeListSelectionListener( all );
	jl.setSelectedValue(selectedAttr.toString(), true);
	jl.addListSelectionListener( all );
/*
	if (scope == VARIABLE_SCOPE) {
	  vDisplay.buildTable(selectedAttr);
	}
*/
	rightPanel.revalidate();
	rightPanel.repaint();
	sp.revalidate();
	sp.repaint();
	System.gc();
        if (scope == VARIABLE_SCOPE) myEditor.enablevAttrMenu();
        else myEditor.enablegAttrMenu();
    }
	
    public  void resetPanel(boolean resetList) {
	if (scope == GLOBAL_SCOPE) gDisplay.reset();
	else vDisplay.reset();
	//description.reset();
	//values.reset();
	if (resetList) {
	    updateListOfAttributes();
	    selectedAttr = null;
	}
    }

    // Update the Attribute List to include all the attributes in the file
    public void updateListOfAttributes() {
	Vector v = new Vector();
	JList jl = (JList)controls.get("listOfAttributes");
	JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");
	Attribute attr = null;
	if (myEditor.theCDF != null)
	    for (Enumeration e = myEditor.theCDF.getAttributes().elements() ; 
		 e.hasMoreElements() ; ) {
		attr = (Attribute) e.nextElement();
		if (attr.getScope() == scope) 
		  v.addElement(attr.toString());
	    }

	controls.put("attributeVector", v);
	jl.setListData(v);
	sp.revalidate();
	sp.repaint();
	System.gc();
    }

    // Add the attribute to the list
    public void addToListOfAttributes(Object attr) {
	if (((Attribute) attr).getScope() == scope) {
	  Vector v = (Vector)controls.get("attributeVector");
	  JList jl = (JList)controls.get("listOfAttributes");
	  JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");

	  if (!v.contains(attr.toString())) {
	    v.addElement(attr.toString());
	    controls.put("attributeVector", v);
	    jl.setListData(v);
	    sp.revalidate();
	    sp.repaint();
	    System.gc();
	  }
	}
    }

    // Remove the attribute from the list
    public void removeFromListOfAttributes(Object attr) {
        if (((Attribute) attr).getScope() == scope) {
          Vector v = (Vector)controls.get("attributeVector");
          JList jl = (JList)controls.get("listOfAttributes");
          JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");

          v.removeElement(attr.toString());
          controls.put("attributeVector", v);
          jl.setListData(v);
          sp.revalidate();
          sp.repaint();
	  System.gc();
        }
    }

    /**
     * Back up the current attribute which will allow a future undo operation
     */
    /*
    private void backupCurrentAttribute() {
	// If selectedAttr is an attribute then copy it to <name>_BAK
	String curName = selectedAttr.toString();
	if (selectedAttr instanceof Attribute) {
	    try {
		((Attribute)selectedAttr).copy(curName+"_BAK");
		
		// Delete it
		((Attribute)selectedAttr).delete();
		
		// set selectedAttr to String
		selectedAttr = curName;
		
		// change currentAttribute.get("editing") to true
		currentAttributes.put("editing", new Boolean(true));
	    } catch (CDFException ce) {
		System.err.println("This shouldn't happen.");
		ce.printStackTrace();
	    }
	}
    }
    */
    //-----------------------------------------------------------
    //
    //                        Utility Methods
    //
    //-----------------------------------------------------------

    /**
     * Saves the current attribute to the CDF in memory 
 Called from CDFE_AttrListListener and AttrMenuListener.
     */
    public void saveAttributeChanges() {
	// This is brute force until I can determine if there is 
	// a way to get only what has changed

    }

    /**
     * Check to see if support data or metadata is used in other
     * attributes and change the name there as well.
     */
    public void renameSelectedAttr(String newName) throws CDFException {
	
	String oldName = selectedAttr.toString();
	selectedAttr.rename(newName);
	
	// Change the name in the list of Attributes
	Vector v = (Vector)controls.get("attributeVector");
	JList jl = (JList)controls.get("listOfAttributes");
	JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");
	    
	jl.removeListSelectionListener( all );
	int idx = v.indexOf(oldName);
	v.removeElementAt(idx);
	v.insertElementAt(selectedAttr.toString(), idx);
	controls.put("attributeVector", v);
	jl.setListData(v);
	jl.setSelectedValue(newName, true);
	sp.revalidate();
	sp.repaint();
	System.gc();
	jl.addListSelectionListener( all );
    }

    /**
     * Check to see if support data or metadata is used in other
     * attributes and do not allow deletion of attribute.
     */
    public void deleteSelectedAttr() throws CDFException {
	// if attribute is support data or metadata check for usage
	// and throw exception
	try {
	    Vector v = (Vector)controls.get("attributeVector");
	    JList jl = (JList)controls.get("listOfAttributes");
	    jl.removeListSelectionListener(all);
	    JScrollPane sp = (JScrollPane)controls.get("attributeScrollPane");
	    v.removeElementAt(v.indexOf(selectedAttr.toString()));
	    controls.put("attributeVector", v);
	    jl.setListData(v);
	    jl.setSelectedIndex(-1);
	    jl.addListSelectionListener(all);
	    sp.revalidate();
	    sp.repaint();
	    resetPanel(false);
	    System.gc();
	    
	    selectedAttr.delete();
	    if (selectedAttr.getScope() == GLOBAL_SCOPE)
	      myEditor.disablegAttrMenu();
	    else
	      myEditor.disablevAttrMenu();
	    selectedAttr = null;
	} catch (CDFException e) {
	    System.err.println("AttributePanel.deleteSelectedAttr: "+e);
	}
    }

    public Attribute getSelectedAttr() {
	return selectedAttr;
    }

    public Hashtable getControls() {
	return controls;
    }

    public Object getControl(String name) {
	return controls.get(name);
    }

    public Object getAttribute(String name) {
	return currentAttributes.get(name);
    }

    private void putAttribute(String name, Object value) {
	currentAttributes.put(name, value);
    }

    public int getMaxWritten() {
	return maxWritten;
    }

    public void setMaxWritten(int max) {
	maxWritten = max;
    }

    public long getScope() {
        return scope;
    }

    public CDFE_MYEditor getMyEditor() {
        return myEditor;
    }

    public CDFE_EditgAttributeTableScrollPane getgDisplay() {
        return gDisplay;
    }

    public CDFE_EditvAttributeTableScrollPane getvDisplay() {
        return vDisplay;
    }

    public CDFE_CDFSpecPanel getCDFSpecPanel() {
        return cdfspec;
    }


    public CDFE_JLabeledPanel getRightPanel() {
        return rightPanel;
    }

}
