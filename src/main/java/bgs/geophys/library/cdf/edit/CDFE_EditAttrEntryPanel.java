package bgs.geophys.library.cdf.edit;

// $Id: CDFE_EditAttrEntryPanel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * This class defines all the methods to build and control the entry
 panel for the CDFE_MYEditor.  The setXXX methods will set the various
 * subpanels for the selectedEntry. If the selectedEntry is simply a string
 * (which implies a new entry) then the widgets are updated from the
 * appropriate info. The getXXX methods will query the widgets in a given
 * subpanel and place the values in the currentEntries Hashtable.  The 
 * keys for this hashtable correpsond to the keys for the controls Hashtable.
 *
 */
public class CDFE_EditAttrEntryPanel extends JDialog
			   implements CDFConstants, CDFE_EntryEventListener {
    
    static final long serialVersionUID = 1L;

    // The Frame
    private CDFE_MYEditor myEditor;
    private Attribute selectedAttr;
    private CDFE_AttributePanel attrPanel;
    
    //private JTabbedPane infoTabPane;
    private Entry     selectedEntry = null;
    private String    selectedVariable = null;

    // Listen for changes in the list
    private CDFE_EntryListListener    vll;
    
    /**
     * Holds the widgets and other info that reference Entries.
     */
    protected Hashtable controls          = new Hashtable();
    private Hashtable currentEntries = new Hashtable(); // Entries
    private JMenu entryMenu;
    private JScrollBar sb1, sb2;
    private CDFE_ScrollBarSynchronizer scrollBarSync;

    // Subpanels
    private JSplitPane spane;
    private CDFE_EditgEntryTablePanel gDisplay;
    private CDFE_EditvEntryTablePanel vDisplay;
    private JPanel		rightPanel;
    private CDFE_EditAttrEntryAction editAttrEntry;
    private JScrollPane entryScrollPane;
    private long scope;

    //
    // Constructor
    //
    public CDFE_EditAttrEntryPanel(CDFE_AttributePanel attrPanel) {
	super(attrPanel.getMyEditor().frame, true);
	this.attrPanel = attrPanel;

	scope = attrPanel.getScope();

	myEditor = attrPanel.getMyEditor();
	selectedAttr = attrPanel.getSelectedAttr();

	setSize(650, 250);
	char gORv;
	if (scope == GLOBAL_SCOPE) gORv = 'g';
	else gORv = 'v';
	setTitle("Edit "+gORv+"Attribute: "+selectedAttr);
        // Add a MenuBar

	setJMenuBar(buildMenuBar());

        spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        spane.setOneTouchExpandable(true);
	vll = new CDFE_EntryListListener(this, scope);

	spane.setBorder(new CompoundBorder(CDFE_MYEditor.loweredBorder, 
				     	   CDFE_MYEditor.emptyBorder5));

	// Instatiate the subpanels
	if (scope == GLOBAL_SCOPE) {
	  gDisplay  = new CDFE_EditgEntryTablePanel(this);
	  gDisplay.buildTable(selectedAttr);
	} else {
	  vDisplay  = new CDFE_EditvEntryTablePanel(this);
	  vDisplay.buildTable(selectedAttr);
	}

	// JList to hold entry names
	Vector entryVector = new Vector();
	JList  listOfEntries = new JList(entryVector);
        listOfEntries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listOfEntries.addListSelectionListener( vll );
	entryScrollPane = new JScrollPane(listOfEntries);
	
	controls.put("listOfEntries", listOfEntries);
	controls.put("entryScrollPane", entryScrollPane);
	controls.put("entryVector", entryVector);

	JViewport jvp = new JViewport();
	if (scope == GLOBAL_SCOPE) 
	  jvp.setView(new JLabel("Entry"));
	else
	  jvp.setView(new JLabel("zVariable"));
	entryScrollPane.setColumnHeader(jvp);

	rightPanel = new JPanel();
	rightPanel.setLayout(new BorderLayout());
	if (scope == GLOBAL_SCOPE) 
	  rightPanel.add(gDisplay);
	else
	  rightPanel.add(vDisplay);
	spane.setLeftComponent(entryScrollPane);
	spane.setRightComponent(rightPanel);

	updateListOfEntries();
	int lenID, lenData;
	if (scope == GLOBAL_SCOPE) {
	  lenID = 60;
	  lenData = 555;
	} else {
	  lenID = 110;
	  lenData = 505;
	}

	// Provide minimum sizes for the two components in the split pane
	entryScrollPane.setPreferredSize(new Dimension(lenID, 250));
	rightPanel.setPreferredSize(new Dimension(lenData, 250));

	// Set the initial location and size of the divider
        spane.setDividerLocation(lenID);
	spane.setDividerSize(5);

	// Provide a preferred size for the split pane
	spane.setPreferredSize(new Dimension(620, 250));

	// Synchronize the attribute list and entry table 
	sb1 = entryScrollPane.getVerticalScrollBar();
	if (scope == GLOBAL_SCOPE) 
	  sb2 = gDisplay.getScrollPane().getVerticalScrollBar();
	else
	  sb2 = vDisplay.getScrollPane().getVerticalScrollBar();
	scrollBarSync = new CDFE_ScrollBarSynchronizer(sb2);
	sb1.getModel().addChangeListener(scrollBarSync);

	resetPanel(false);
	getContentPane().add(spane, BorderLayout.CENTER);
	validate();
	pack();
	setVisible(true);
    }

    JMenuBar buildMenuBar() {
        // MenuBar
        JMenuBar menuBar = new JMenuBar();

        JMenuItem mi;

        // Entry Menu
        entryMenu = (JMenu) menuBar.add(new JMenu("Edit"));
        entryMenu.setMnemonic('E');

        mi = (JMenuItem) entryMenu.add(new CDFE_NewEntryAction( this ));
	mi.setToolTipText("Create a new attribute entry");
        mi.setMnemonic('N');
	if (scope == GLOBAL_SCOPE) mi.setEnabled(true);
	else mi.setEnabled(false);

        mi = (JMenuItem) entryMenu.add(new CDFE_DeleteEntryAction( this ));
	mi.setToolTipText("Delete the selected attribute entry");
        mi.setMnemonic('D');
        mi.setEnabled(false);

        entryMenu.add(new JSeparator());

        editAttrEntry = new CDFE_EditAttrEntryAction( this );
        
        mi = (JMenuItem) entryMenu.add(editAttrEntry);
	mi.setToolTipText("Modify the selected attribute entry");
	mi.setMnemonic('M');
        mi.setEnabled(false);

        entryMenu.add(new JSeparator());

        mi = (JMenuItem) entryMenu.add(new ExitEntryEdit(this));
        mi.setMnemonic('X');
        mi.setEnabled(true);

        return menuBar;
    }

    //---------------------------------------------------------
    //
    //                    Event Handler Methods                   
    //
    //---------------------------------------------------------
    public void performEntryAction(CDFE_EntryEvent e) {
	int type = e.getID();

	switch (type) {
	case CDFE_EntryEvent.CREATED:
	    addToListOfEntries(e.getEntry());
	    reselectCurrentEntry();
	    break;
	case CDFE_EntryEvent.DELETED:
	    removeFromListOfEntries(e.getEntry());
	    resetPanel(true);
	    break;
        case CDFE_EntryEvent.NAME_CHANGE:
            removeFromListOfEntries(e.getEntry());
            resetPanel(true);
            break;
	default:
	    break;
	}
    }

    //---------------------------------------------------------
    //
    //                    Controller Methods                   
    //
    //---------------------------------------------------------

    /**
     * Update panel for existing entries
     */
    public  void updateAttrEntryPanel(Entry entry, boolean update) {

        if (entry != null) {
	  if (scope == GLOBAL_SCOPE) {
	    if (selectedEntry == null) enableEntryMenu();
	  } else {
	    enableSelectedEntryMenu(entry);
	  }
	} else {
	  JList jl = (JList)controls.get("listOfEntries");
	  selectedVariable = (String)jl.getSelectedValue();
	  enableSelectedEntryMenu(selectedVariable);
	}
        selectedEntry = entry;
        resetPanel(false);
	if (scope == GLOBAL_SCOPE) {
	  // Build a new table only an entry is created/deleted  
	  if (update) gDisplay.buildTable(selectedAttr);
	} else {
	  if (update) {
	    vDisplay.buildTable(selectedAttr);
            sb1.getModel().setValue(sb1.getModel().getValue());
	  }
	}
	rightPanel.revalidate();
	rightPanel.repaint();

    }

    /**
     * provides a software wrapper to select an entry.  These changes are not
     * listened for.
     */
    public void reselectCurrentEntry() {
	JList jl = (JList)controls.get("listOfEntries");
	JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");
	
	jl.removeListSelectionListener( vll );
        String nEntry;
        if (scope == GLOBAL_SCOPE)
          nEntry = "" + (selectedEntry.getID()+1);
        else
          nEntry = (String)jl.getSelectedValue();
	jl.setSelectedValue(nEntry, true);
	jl.addListSelectionListener( vll );
/*
        if (scope == GLOBAL_SCOPE) {
          gDisplay.buildTable(selectedAttr);
	  enableEntryMenu();
        } else {
          vDisplay.buildTable(selectedAttr);
	  enableSelectedEntryMenu(selectedEntry);
	}
*/
	sp.revalidate();
	sp.repaint();
    }
	
    public  void resetPanel(boolean resetList) {
        if (scope == GLOBAL_SCOPE)
          gDisplay.reset();
        else
          vDisplay.reset();
	//description.reset();

	if (resetList) {
	    updateListOfEntries();
	    selectedEntry = null;
	}
    }

    // Update the Entry List to include all the entries in the file
    private  void updateListOfEntries() {
	Vector v = new Vector();
	JList jl = (JList)controls.get("listOfEntries");
	JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");
	
	if (selectedAttr != null)
	  if (scope == GLOBAL_SCOPE) {
            try {
              Enumeration e = selectedAttr.getEntries().elements();
  	      for (; e.hasMoreElements() ; ) {
		Entry en = (Entry) e.nextElement();
		if (en != null) {
          	  String nEntry = "" + (en.getID()+1);
		  v.addElement(nEntry);
		}
	      }
	    } catch (CDFException ex) { }
	  } else {
            for (Enumeration e = myEditor.theCDF.getVariables().elements() ;
                 e.hasMoreElements() ; ) {
                Variable en = (Variable) e.nextElement();
                if (en != null) {
                  String nEntry = en.getName();
                  v.addElement(nEntry);
                }
            }
	  }
	controls.put("entryVector", v);
	jl.setListData(v);
	sp.revalidate();
	sp.repaint();
    }

    // Add the entry to the list
    public void addToListOfEntries(Object entry) {
	Vector v = (Vector)controls.get("entryVector");
	Vector newV = new Vector();
	JList jl = (JList)controls.get("listOfEntries");
	JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");

        String nEntry;
        if (scope == GLOBAL_SCOPE) {
	  int id = (int) ((Entry)entry).getID();
	  int idxCnt = v.size();
	  if (idxCnt == 0) 
	    newV.addElement(""+(id+1));
	  else {
	    boolean chk = true;
	    for (int i = 0; i < idxCnt; i++) {
	       int ix = Integer.parseInt((String) v.elementAt(i)) - 1;
	       if (chk) {
	         if (id < ix) {
		   newV.addElement(""+(id+1));
		   chk = false;
		 }
	       }
	       newV.addElement(""+(ix+1));
	    }
	    if (chk) newV.addElement(""+(id+1));
	  }
        } else 
	  newV = v;
	controls.put("entryVector", newV);
	jl.setListData(newV);
	sp.revalidate();
	sp.repaint();
    }

    // Remove the entry from the list
    public void removeFromListOfEntries(Object entry) {
        Vector v = (Vector)controls.get("entryVector");
        JList jl = (JList)controls.get("listOfEntries");
        JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");

        String nEntry;
        if (scope == GLOBAL_SCOPE)
          nEntry = "" + (((Entry)entry).getID()+1);
        else
         nEntry = ((Variable) entry).getName();

        v.removeElement(nEntry);
        controls.put("entryVector", v);
        jl.setListData(v);
        sp.revalidate();
        sp.repaint();
    }

    /**
     * Back up the current entry which will allow a future undo operation
     */
    /*
    private void backupCurrentEntry() {
	// If selectedEntry is a Entry then copy it to <name>_BAK
        String curName;
        if (scope == GLOBAL_SCOPE)
          curName = "" + (selectedEntry.getID()+1);
        else      
         curName = selectedVariable;

	if (selectedEntry instanceof Entry) {
	    try {
		((Entry)selectedEntry).copy(curName+"_BAK");
		
		// Delete it
		((Entry)selectedEntry).delete();
		
		// set selectedEntry to String
		selectedEntry = curName;
		
		// change currentEntry.get("editing") to true
		currentEntries.put("editing", new Boolean(true));
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
     * Saves the current entry to the CDF in memory 
 Called from CDFE_EntryListListener and EntryMenuListener.
     */
    public void saveEntryChanges() {
	// This is brute force until I can determine if there is 
	// a way to get only what has changed

    }

    /**
     * Check to see if support data or metadata is used in other
     * entry attributes and change the name there as well.
     */
    public void renameSelectedEntry(String newName)
			throws CDFException {
	
        Vector v = (Vector)controls.get("entryVector");
        JList jl = (JList)controls.get("listOfEntries");

        String oldName;
        if (scope == GLOBAL_SCOPE)
          oldName = "" + (selectedEntry.getID()+1);
        else
          oldName = (String) jl.getSelectedValue();
	
	selectedEntry.rename(newName);
	
	// if entry is support data or metadata check for usage
	// and rename attributes 
	// Change the name in the list of Entries
	JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");
	    
	jl.removeListSelectionListener( vll );
	int idx = v.indexOf(oldName);
	v.removeElementAt(idx);
        String nName;
        if (scope == GLOBAL_SCOPE) {
          nName = "" + (selectedEntry.getID()+1);
	  v.insertElementAt("" + (selectedEntry.getID()+1), idx);
        } else {
          nName = (String)jl.getSelectedValue();
	  v.insertElementAt(nName, idx);
	}
	controls.put("entryVector", v);
	jl.setListData(v);
	jl.setSelectedValue(newName, true);
	sp.revalidate();
	sp.repaint();
	jl.addListSelectionListener( vll );
    }

    /**
     * Check to see if support data or metadata is used in other
     * entry attributes and do not allow deletion of entry.
     */
    public void deleteSelectedEntry() throws CDFException {
	// if entry is support data or metadata check for usage
	// and throw exception
	Vector v = (Vector)controls.get("entryVector");
	JList jl = (JList)controls.get("listOfEntries");
	jl.removeListSelectionListener(vll);
	JScrollPane sp = (JScrollPane)controls.get("entryScrollPane");

        String nName;
        if (scope == GLOBAL_SCOPE)
          nName = "" + (selectedEntry.getID()+1);
        else      
          nName = (String)jl.getSelectedValue();
	v.removeElementAt(v.indexOf(nName));
	controls.put("entryVector", v);
	jl.setListData(v);
	jl.setSelectedIndex(-1);
	jl.addListSelectionListener(vll);
	sp.revalidate();
	sp.repaint();
	selectedEntry.delete();
	selectedEntry = null;
    }

    public void enableEntryMenu() {
        Component [] mis = entryMenu.getMenuComponents();
        for (int i = 0; i< mis.length; i++)
            mis[i].setEnabled(true);
    }

    public void enableSelectedEntryMenu(Object en) {
        Component [] mis = entryMenu.getMenuComponents();
	mis[5].setEnabled(true);
        for (int i = 0; i< mis.length-2; i++) {
	    mis[i].setEnabled(false);
	    if ((scope ==  GLOBAL_SCOPE || en == null) && i == 0) 
		mis[i].setEnabled(true);
            if (en instanceof String && i == 0) mis[i].setEnabled(true);
	    if (en instanceof Entry  && (i == 1 || i == 3)) mis[i].setEnabled(true);
	}
    }

    public void disableEntryMenu() {
        Component [] mis = entryMenu.getMenuComponents();
        for (int i = 1; i< mis.length; i++)    
            mis[i].setEnabled(false);    
    }    
    
    public Attribute getSelectedAttr() {
	return selectedAttr;
    }

    public Entry getSelectedEntry() {
        return selectedEntry;
    }

    public long getScope() {
        return scope;
    }

    public Hashtable getControls() {
	return controls;
    }

    public Object getControl(String name) {
	return controls.get(name);
    }

    public Object getEntry(String name) {
	return currentEntries.get(name);
    }

    private void putEntry(String name, Object value) {
	currentEntries.put(name, value);
    }

    public CDFE_EditgEntryTablePanel getgDisplay() {
	return gDisplay;
    }

    public CDFE_EditvEntryTablePanel getvDisplay() {
        return vDisplay;
    }

    public CDFE_MYEditor getMyEditor() {
        return myEditor;
    }

    public JScrollBar getAttrScrollBar() {
        return sb1;
    }

    public JScrollBar getEntryScrollBar() {
        return sb2;
    }    
    
    public CDFE_ScrollBarSynchronizer getScrollBarSync() {
        return scrollBarSync;
    }

    public JScrollPane getScrollPane() {
        return entryScrollPane;
    }

    public JMenu getEntryMenu() {
        return entryMenu;
    }

    class ExitEntryEdit extends AbstractAction {

       static final long serialVersionUID = 1L;

//      private static final String DEFAULT_NAME = "Exit Edit";

      private CDFE_MYEditor myEditor;
      private CDFE_EditAttrEntryPanel myPanel;

      ExitEntryEdit(CDFE_EditAttrEntryPanel myPanel) {
        this(myPanel, new String("Exit Edit"));
      }

      ExitEntryEdit(CDFE_EditAttrEntryPanel myPanel, String name) {
        super(name);
        this.myPanel = myPanel;
        this.myEditor = myPanel.myEditor;
      }

      public void putValue(String key, Object value) {
        if (value != null)
            super.putValue(key, value);
      }

      public void actionPerformed(ActionEvent event) {
        myEditor.setWaitCursor();
	myPanel.setVisible(false);
	myPanel.dispose();
      }
    }

}
