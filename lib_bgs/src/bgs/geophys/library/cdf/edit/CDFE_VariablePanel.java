package bgs.geophys.library.cdf.edit;

// $Id: CDFE_VariablePanel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * This class defines all the methods to build and control the variable
 panel for the CDFE_MYEditor.  The setXXX methods will set the various
 * subpanels for the selectedVar. If the selectedVar is simply a string
 * (which implies a new variable) then the widgets are updated from the
 * appropriate info. The getXXX methods will query the widgets in a given
 * subpanel and place the values in the currentAttributes Hashtable.  The 
 * keys for this hashtable correpsond to the keys for the controls Hashtable.
 *
 */
public class CDFE_VariablePanel extends JSplitPane
//			   implements CDFConstants, VariableEventListener {
			   implements CDFConstants {
    
    // The Frame
    private  CDFE_MYEditor myEditor;
    private CDF cdf;
    
    //private JTabbedPane infoTabPane;
    private Variable             selectedVar = null;

    // Listen for changes in the list
    private CDFE_VarListListener    vll;
    
    // These are the controls for the generic attribute editor
    private JPanel genericAttributePanel;

    /**
     * Holds the widgets and other info that reference VariableAttributes
     */
    private Hashtable controls          = new Hashtable();
    private Hashtable currentAttributes = new Hashtable(); // Attributes

    // Subpanels
    private CDFE_VariableSpecPanel variableSpecs;
    private CDFE_EditvVarEntryTableScrollPane display;
    private CDFE_VariableValuePanel   value;
    private JPanel		rightPanel;
    private JScrollPane variableScrollPane;

    static final long serialVersionUID = 1L;

    //
    // Constructor
    //
    public CDFE_VariablePanel(CDFE_MYEditor myEditor) {
	super(HORIZONTAL_SPLIT);
	setOneTouchExpandable(true);
	this.myEditor = myEditor;
	cdf = myEditor.theCDF;

	vll = new CDFE_VarListListener(this);

	setBorder(new CompoundBorder(CDFE_MYEditor.loweredBorder, 
				     CDFE_MYEditor.emptyBorder5));

	// Instatiate the subpanels
	variableSpecs = new CDFE_VariableSpecPanel();
	display  = new CDFE_EditvVarEntryTableScrollPane(this);
	value = new CDFE_VariableValuePanel(this);

	// JList to hold variable names
	Vector variableVector = new Vector();
	JList  listOfVariables = new JList(variableVector);
        listOfVariables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listOfVariables.addListSelectionListener( vll );
	variableScrollPane = new JScrollPane(listOfVariables);
	
	controls.put("listOfVariables", listOfVariables);
	controls.put("variableScrollPane", variableScrollPane);
	controls.put("variableVector", variableVector);

        JViewport jvp = new JViewport();
        jvp.setView(new JLabel("zVariable"));
        variableScrollPane.setColumnHeader(jvp);

	rightPanel = new JPanel();
	rightPanel.setLayout(new BorderLayout());
	rightPanel.add(variableSpecs, BorderLayout.NORTH);
	rightPanel.add(display, BorderLayout.CENTER);
	rightPanel.add(value, BorderLayout.SOUTH);

	setLeftComponent (variableScrollPane);
	setRightComponent(rightPanel);

	updateListOfVariables();

	// Provide minimum sizes for the two components in the split pane
	variableScrollPane.setPreferredSize(new Dimension(120, 620));
//	rightPanel.setPreferredSize(new Dimension(670, 620));

	// Set the initial location and size of the divider
        setDividerLocation(120);
	setDividerSize(5);

	// Provide a preferred size for the split pane
	setPreferredSize(new Dimension(myEditor.getEditWidth(), 
                                       myEditor.getEditHeight()));

	resetPanel(false);
    }

    //---------------------------------------------------------
    //
    //                    Event Handler Methods                   
    //
    //---------------------------------------------------------
/*
    public void performVariableAction(VariableEvent e) {
	int type = e.getID();

	switch (type) {
	case VariableEvent.CREATED:
	    addToListOfVariables(e.getVariable());
	    reselectCurrentVariable();
	    break;
	case VariableEvent.DELETED:
	    removeFromListOfVariables(e.getVariable());
	    resetPanel(true);
	    break;
        case VariableEvent.NAME_CHANGE:
            removeFromListOfVariables(e.getVariable());
            resetPanel(true);
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
     * Update panel for existing variables
     */
    public  void updateVarPanel(Variable var) {
        String sVar = (selectedVar == null ? "null" : selectedVar.toString());
        selectedVar = var;
        resetPanel(false);
	if (var != null) {
          variableSpecs.set(selectedVar);  // Set the widgets to the current Specs
	  display.buildTable(selectedVar);
	  value.set(selectedVar);
	  myEditor.enableVarMenu();
	} else {
	  myEditor.disableVarMenu();
	  variableSpecs.reset();
	  display.reset();
	  value.reset();
	}

	rightPanel.revalidate();
	rightPanel.repaint();
	System.gc();

    }

    /**
     * provides a software wrapper to select a variable.  These changes are not
     * listened for.
     */
    public void reselectCurrentVariable() {
	JList jl = (JList)controls.get("listOfVariables");
	JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");
	
	jl.removeListSelectionListener( vll );
	jl.setSelectedValue(selectedVar.toString(), true);
	jl.addListSelectionListener( vll );
//	display.buildTable(selectedVar);
	myEditor.enableVarMenu();

	sp.revalidate();
	sp.repaint();
	System.gc();
    }
	
    public  void resetPanel(boolean resetList) {
	variableSpecs.reset();
	display.reset();
	//description.reset();
	value.reset();

	if (resetList) {
	    updateListOfVariables();
	    selectedVar = null;
	}
    }

    // Update the Variable List to include all the variables in the file
    private  void updateListOfVariables() {
	Vector v = new Vector();
	JList jl = (JList)controls.get("listOfVariables");
	JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");
	
	if (cdf != null)
	    for (Enumeration e = cdf.getVariables().elements() ; 
		 e.hasMoreElements() ; )
		v.addElement(e.nextElement().toString());

	controls.put("variableVector", v);
	jl.setListData(v);
	sp.revalidate();
	sp.repaint();
	System.gc();
    }

    // Add the variable to the list
    public void addToListOfVariables(Object var) {
	Vector v = (Vector)controls.get("variableVector");
	JList jl = (JList)controls.get("listOfVariables");
	JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");

	v.addElement(var.toString());
	controls.put("variableVector", v);
	jl.setListData(v);
	sp.revalidate();
	sp.repaint();
	System.gc();
    }

    // Remove the variable from the list
    public void removeFromListOfVariables(Object var) {
        Vector v = (Vector)controls.get("variableVector");
        JList jl = (JList)controls.get("listOfVariables");
        JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");

        v.removeElement(var.toString());
        controls.put("variableVector", v);
        jl.setListData(v);
        sp.revalidate();
        sp.repaint();
	System.gc();
    }

    /**
     * Back up the current variable which will allow a future undo operation
     */
    /*
    private void backupCurrentVariable() {
	// If selectedVar is a Variable then copy it to <name>_BAK
	String curName = selectedVar.toString();
	if (selectedVar instanceof Variable) {
	    try {
		((Variable)selectedVar).copy(curName+"_BAK");
		
		// Delete it
		((Variable)selectedVar).delete();
		
		// set selectedVar to String
		selectedVar = curName;
		
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
     * Saves the current variable to the CDF in memory 
 Called from CDFE_VarListListener and VarMenuListener.
     */
    public void saveVariableChanges() {
	// This is brute force until I can determine if there is 
	// a way to get only what has changed

	if (selectedVar != null) {             // is something selected?
//	    display.save(selectedVar);
	}
    }

    /**
     * Check to see if support data or metadata is used in other
     * variable attributes and change the name there as well.
     */
    public void renameSelectedVar(String newName)
			throws CDFException {
	
	String oldName = selectedVar.toString();
	selectedVar.rename(newName);
	
	// if variable is support data or metadata check for usage
	// and rename attributes 
	// Change the name in the list of Variables
	Vector v = (Vector)controls.get("variableVector");
	JList jl = (JList)controls.get("listOfVariables");
	JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");
	    
	jl.removeListSelectionListener( vll );
	int idx = v.indexOf(oldName);
	v.removeElementAt(idx);
	v.insertElementAt(selectedVar.toString(), idx);
	controls.put("variableVector", v);
	jl.setListData(v);
	jl.setSelectedValue(newName, true);
	sp.revalidate();
	sp.repaint();
	System.gc();
	jl.addListSelectionListener( vll );
    }

    /**
     * Check to see if support data or metadata is used in other
     * variable attributes and do not allow deletion of variable.
     */
    public void deleteSelectedVar()
			throws CDFException {
	// if variable is support data or metadata check for usage
	// and throw exception
	Vector v = (Vector)controls.get("variableVector");
	JList jl = (JList)controls.get("listOfVariables");
	jl.removeListSelectionListener(vll);
	JScrollPane sp = (JScrollPane)controls.get("variableScrollPane");
	v.removeElementAt(v.indexOf(selectedVar.toString()));
	controls.put("variableVector", v);
	jl.setListData(v);
	jl.setSelectedIndex(-1);
	jl.addListSelectionListener(vll);
	sp.revalidate();
	sp.repaint();
	resetPanel(false);
	System.gc();
	    
	selectedVar.delete();
	selectedVar = null;
	myEditor.disableVarMenu();
    }

    public Variable getSelectedVar() {
	return selectedVar;
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

    public CDFE_EditvVarEntryTableScrollPane getDisplay() {
	return display;
    }

    public CDFE_MYEditor getMyEditor() {
        return myEditor;
    }

    public JPanel getRightPanel() {
        return rightPanel;
    }

    public CDFE_VariableValuePanel getValuePanel() {
        return value;
    }

    public CDFE_VariableSpecPanel getVarSpecPanel() {
        return variableSpecs;
    }

    public CDFE_EditvVarEntryTableScrollPane getvVarEntryTableScrollPane() {
        return display;
    }

}
