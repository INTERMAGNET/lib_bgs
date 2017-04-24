package bgs.geophys.library.cdf.edit;

// $Id: CDFE_NewEntryDialog.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.beans.*; // Property change stuff
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Array;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.CDFUtils;

/**
 * Present the user with a dialog box to allow CDF entry creation
 *
 * @author Mike Liu
 *
 */
public class CDFE_NewEntryDialog extends JDialog implements CDFConstants, ActionListener {

    static final long serialVersionUID = 1L;

    private CDFE_EditAttrEntryPanel  myPanel;
    private JButton             enter, cancel;
    private CDFE_JLabeledTF          tfen, tfva;
    private CDFE_JLabeledCB          cbdt;
    private JTextField		jtfen, jtfva;
    private JFrame              myFrame;
    private static final String ENTER = "Create";
    private static final String CANCEL = "Cancel";

    private static CDFE_NewEntryDialog cache;
    private long scope;
    private boolean fromEnter = false;

    private Entry entry = null;
    private Attribute attr = null;

    private Dimension ss;
    private Dimension ps;

    /**
     * default constructor
     */
    public CDFE_NewEntryDialog() {
	// needed to allow extending this class
    }

    private CDFE_NewEntryDialog(JFrame frame, long scope) {

	super(frame, true);
	this.myFrame = frame;
	this.scope = scope;
	setTitle("Create Entry");

	JPanel mp = new JPanel();
	mp.setLayout(new BorderLayout());
	mp.setBorder(new EmptyBorder(10,10,10,10));
	getContentPane().add(mp);

	// Only way to close is to use the buttons
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent we) {
	    }
	});

	setResizable(false);
	setSize(430,300);
	setLocation(10, 20);
	setVisible(false);

	ss = Toolkit.getDefaultToolkit().getScreenSize();
	ps = getPreferredSize();    

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	JPanel sp = new JPanel();
	sp.setLayout(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

	if (scope == GLOBAL_SCOPE) {
          jtfen = new JTextField(3);
          tfen = new CDFE_JLabeledTF("Entry Number", jtfen, true);
          tfen.textField.setToolTipText("Enter the entry number for the given gAttribute.");
	} else {
	  jtfen = new JTextField(10);
	  tfen = new CDFE_JLabeledTF("Variable Name", jtfen, true);
	  tfen.textField.setToolTipText("Create the entry for the given vAttribute.");
	}
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(tfen, gbc);
        sp.add(tfen);

	Vector types = new Vector(17);
	types.addElement("CDF_BYTE");
	types.addElement("CDF_INT1");
	types.addElement("CDF_UINT1");
	types.addElement("CDF_INT2");
	types.addElement("CDF_UINT2");
	types.addElement("CDF_INT4");
	types.addElement("CDF_UINT4");
	types.addElement("CDF_INT8");
	types.addElement("CDF_REAL4");
	types.addElement("CDF_FLOAT");
	types.addElement("CDF_REAL8");
	types.addElement("CDF_DOUBLE");
	types.addElement("CDF_EPOCH");
	types.addElement("CDF_EPOCH16");
	types.addElement("CDF_TIME_TT2000");
	types.addElement("CDF_CHAR");
	types.addElement("CDF_UCHAR");

	cbdt = new CDFE_JLabeledCB("Data Type", types, false);
	cbdt.set("CDF_CHAR");
	cbdt.comboBox.addActionListener( this );
	cbdt.comboBox.setToolTipText("Select the data type");
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(cbdt, gbc);
	sp.add(cbdt);
	
        jtfva = new JTextField("\" \"", 30);
        tfva = new CDFE_JLabeledTF("Value", jtfva, true);
	tfva.textField.setToolTipText("Enter the entry data. Use , to separate multiple values");
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(tfva, gbc);
        sp.add(tfva);

	mp.add(sp, BorderLayout.CENTER);
	createButtonPanel( mp );

	pack();
	setLocation(10, 20);

    }

    private void createButtonPanel(JPanel jp) {

	JPanel bp = new JPanel();
	
	enter = new JButton(ENTER);
	enter.addActionListener( this );
	enter.setToolTipText("Create the entry");
	bp.add(enter);
	cancel = new JButton(CANCEL);
	cancel.addActionListener( this );
	cancel.setToolTipText("Cancel the creation of an entry");
	bp.add(cancel);
	
	jp.add(bp, BorderLayout.SOUTH);
    }

    /**
     * Present the user with a modal dialog box to create an Attribute's entry
     * for a given attribute.
     *
     * @param frame the parent frame
     * @param attr the attribute where the entry will be created to
     * @param title The title for the dialog
     */
    public static Entry createEntry(CDFE_EditAttrEntryPanel myPanel, String title) {
    
	if (cache == null)
	    cache = new CDFE_NewEntryDialog((JFrame)myPanel.getMyEditor().getFrame(), myPanel.getScope());
	else {
	  if (myPanel.getScope() != cache.scope) {
	    cache = null;
	    cache = new CDFE_NewEntryDialog((JFrame)myPanel.getMyEditor().getFrame(), myPanel.getScope());
	  }
	  cache.reset();
	}

	cache.myPanel = myPanel;
	cache.attr = myPanel.getSelectedAttr();
	cache.scope = myPanel.getScope();
	if (cache.scope == GLOBAL_SCOPE) {
	  cache.tfen.setEnabled(true);
	  // Deprecated - cache.tfen.requestDefaultFocus();
	  cache.tfen.requestFocus();
	  cache.tfen.setEditable(true);
	} else {
	  JList jl = (JList)myPanel.controls.get("listOfEntries");
	  cache.tfen.set((String)jl.getSelectedValue());
	  // Deprecated - cache.tfva.requestDefaultFocus();
	  cache.tfva.requestFocus();
	  cache.tfen.setEnabled(true);
	  cache.tfen.setEditable(false);
	}

	// Set the defaults

	cache.setTitle(title);
	
	cache.setVisible(true);

	return cache.entry;
    }	

    /**
     * Process button events and change of dimension events.
     */
    public void actionPerformed( ActionEvent event ) {

	Object source = event.getSource();

	if (source instanceof JComboBox) {
	  int idx = cbdt.comboBox.getSelectedIndex();
	  if (idx == 13 || idx == 14) 
	    jtfva.setText("\" \"");
	  else if (idx == 11) 
	    jtfva.setText("01-Jan-0000 00:00:00.000");
	  else if (idx == 12)
            jtfva.setText("01-Jan-0000 00:00:00.000.000.000.000");
	  else if (idx == 0 || idx == 1 || idx == 2 || idx == 3 ||
		     idx == 4 || idx == 5 || idx == 6) 
	     jtfva.setText("0");
	  else if (idx == 7 || idx == 8 || idx == 9 || idx == 10)
	     jtfva.setText("0.0");
	} else if (source instanceof JButton) {
	    String action = event.getActionCommand();
	    if (action.equals(CANCEL)) {              // Cancel the operation
		if (!fromEnter) entry = null;
		setVisible(false);
		dispose();
		System.gc();
	    } else if (action.equals(ENTER)) {        // Create the entry
		((JButton)source).setEnabled(false);
		// Notify the tables that editing has stopped

		long dataType;
		Object value = null;

		switch (cbdt.comboBox.getSelectedIndex()) {
		case 0:  dataType = CDFConstants.CDF_BYTE;   break;
		case 1:  dataType = CDFConstants.CDF_INT1;   break;
		case 2:  dataType = CDFConstants.CDF_UINT1;  break;
		case 3:  dataType = CDFConstants.CDF_INT2;   break;
		case 4:  dataType = CDFConstants.CDF_UINT2;  break;
		case 5:  dataType = CDFConstants.CDF_INT4;   break;
		case 6:  dataType = CDFConstants.CDF_UINT4;  break;
		case 7:  dataType = CDFConstants.CDF_INT8;  break;
		case 8:  dataType = CDFConstants.CDF_REAL4;  break;
		case 9:  dataType = CDFConstants.CDF_FLOAT;  break;
		case 10:  dataType = CDFConstants.CDF_REAL8;  break;
		case 11: dataType = CDFConstants.CDF_DOUBLE; break;
		case 12: dataType = CDFConstants.CDF_EPOCH;  break;
		case 13: dataType = CDFConstants.CDF_EPOCH16;  break;
		case 14: dataType = CDFConstants.CDF_TIME_TT2000;  break;
		case 15: dataType = CDFConstants.CDF_CHAR;   break;
		case 16: dataType = CDFConstants.CDF_UCHAR;  break;
		    // keep compiler happy
		default: dataType = CDFConstants.CDF_BYTE;   break;
		}

		String data = jtfva.getText();
		long numElements = 1;
		try {
		  long id;
		  boolean goON = true;
		  if (scope == GLOBAL_SCOPE) {
		    id = Long.parseLong(jtfen.getText());
		    if (id < 1) {
			Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(myFrame,
                                                "gEntry: "+id+" not valid",
                                                "MYEditor: CDFException",
                                                JOptionPane.ERROR_MESSAGE);
                        goON = false;
                    } else {
		      JList jl = (JList) myPanel.controls.get("listOfEntries");
		      for (int i = 0; i < jl.getModel().getSize(); i++) {
		        long idx = Long.parseLong(
			  	       ((String)jl.getModel().getElementAt(i)).trim());
		        if (idx == id) {
			  Toolkit.getDefaultToolkit().beep();
                          JOptionPane.showMessageDialog(myFrame,
                                                "gEntry: "+id+" already exists",
                                                "MYEditor: CDFException",
                                                JOptionPane.ERROR_MESSAGE);
			  goON = false; 
			  break;
			}
		        if (idx > id) break;
		      }
		      if (goON) id--;
		    }
		  } else {
		    JList jl = (JList)myPanel.controls.get("listOfEntries");
		    id = myPanel.getMyEditor().theCDF.getVariableID(
					(String)jl.getSelectedValue());
		  }

		  if (goON) {
                    if ((dataType == CDFConstants.CDF_CHAR) ||
                      (dataType == CDFConstants.CDF_UCHAR)) {
		      if (data.charAt(0) == '"')
			   value = data.substring(1, data.length()-1);
                      else value = data;
                    } else
                      value = CDFE_CDFToolUtils.parseContents(data, dataType);

		    if (value != null) {
		      entry = Entry.create(attr, id, dataType, value);
		      fromEnter = true;
		      cancel.doClick();
//		      setVisible(false);
//		      dispose();
//		      System.gc();
		    }
		  }
		} catch (CDFException e) {
		  e.printStackTrace();
		  Toolkit.getDefaultToolkit().beep();
		  JOptionPane.showMessageDialog(myFrame,
						CDFException.getStatusMsg(e.getCurrentStatus()),
						"MYEditor: CDFException",
						JOptionPane.ERROR_MESSAGE);
		  entry = null;
                } catch (NumberFormatException ex) {
		  Toolkit.getDefaultToolkit().beep();
                  JOptionPane.showMessageDialog(myFrame,
                                                ex.toString(),
                                                "MYEditor: CDFException",
                                                JOptionPane.ERROR_MESSAGE);
		}
		((JButton)source).setEnabled(true);
	    }
	}
    }

    private void reset() {
	// This will occur once the dialog is closed

	tfva.set("");
	tfen.set("");
	cbdt.set("CDF_CHAR");
	tfen.setEnabled(true);
	tfva.setEnabled(true);
	cbdt.setEnabled(true);
    }

}
