package bgs.geophys.library.cdf.edit;

// $Id: CDFE_NewAttributeDialog.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.IllegalArgumentException;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.CDFUtils;

/**
 * Present the user with a dialog box to allow CDF attribute creation
 *
 * @author Mike Liu
 *
 */
public class CDFE_NewAttributeDialog extends JDialog implements CDFConstants,
							   ActionListener {

    static final long serialVersionUID = 1L;

    private JButton             enter, cancel;
    private CDFE_JLabeledCB		cbas;
    private CDFE_JLabeledTF          tfn;
    private JFrame              myFrame;
    private static final String ENTER = "Create";
    private static final String CANCEL = "Cancel";

    private static CDFE_NewAttributeDialog cache;

    private Attribute attr = null;
    private CDF cdf = null;
    private long scope = -1;

    private Dimension ss;
    private Dimension ps;

    /**
     * default constructor
     */
    public CDFE_NewAttributeDialog() {
	// needed to allow extending this class
    }

    private CDFE_NewAttributeDialog(JFrame frame)
    {
	super(frame, true);
	this.myFrame = frame;
	String which = (scope == GLOBAL_SCOPE)? "g" : "v";
	setTitle("Create "+which+"Attribute");

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

	tfn = new CDFE_JLabeledTF("Attribute Name", 15);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(tfn, gbc);
	sp.add(tfn);
	
        Vector gvtypes = new Vector(2);
        gvtypes.addElement("GLOBAL_SCOPE");
        gvtypes.addElement("VARIABLE_SCOPE");

        cbas = new CDFE_JLabeledCB("Attribute Scope", gvtypes, false, false);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(cbas, gbc);
        sp.add(cbas);

	mp.add(sp, BorderLayout.CENTER);
	createButtonPanel( mp );

        setResizable(false);
        setSize(350,200);
        setLocation(10, 20);

    }

    private void createButtonPanel(JPanel jp) {
	JPanel bp = new JPanel();
	
	enter = new JButton(ENTER);
	enter.addActionListener( this );
	bp.add(enter);
	cancel = new JButton(CANCEL);
	cancel.addActionListener( this );
	bp.add(cancel);
	
	jp.add(bp, BorderLayout.SOUTH);
    }

    /**
     * Present the user with a modal dialog box to create an attribute.
     *
     * @param frame the parent frame.
     * @param cdf the CDF file where the variable will be created
     */
    public static Attribute createAttribute(JFrame frame, CDF cdf, long scope) {

	if (cache == null) cache = new CDFE_NewAttributeDialog(frame);

	cache.scope = scope;
	cache.cdf = cdf;
	// Deprecated - cache.tfn.requestDefaultFocus();
	cache.tfn.requestFocus();
	String which = (scope == GLOBAL_SCOPE)? "g" : "v";
	cache.setTitle("Create "+which+"Attribute");

	cache.reset();

 	cache.setVisible(true);

	return cache.attr;

    }

    /**
     * Present the user with a modal dialog box to create an attribute.
     *
     * @param frame the parent frame
     * @param cdf the CDF file where the attribute will be created
     * @param title The title for the dialog
     */
    public static Attribute createAttribute(JFrame frame, CDF cdf, String title, long scope) {

	if (cache == null) cache = new CDFE_NewAttributeDialog(frame);

	cache.scope = scope;
	cache.cdf = cdf;
	// Deprecated - cache.tfn.requestDefaultFocus();
	cache.tfn.requestFocus();
        String which = (scope == GLOBAL_SCOPE)? "g" : "v";
        cache.setTitle("Create "+which+"Attribute");
	
	cache.reset();
	
	cache.setVisible(true);

	return cache.attr;
    }	

    /**
     * Process button events.
     */
    public void actionPerformed( ActionEvent event )
    {
	Object source = event.getSource();

	if (source instanceof JButton) {
	    String action = event.getActionCommand();
	    if (action.equals(CANCEL)) {              // Cancel the operation
		attr = null;
		setVisible(false);
		dispose();
		System.gc();
	    } else if (action.equals(ENTER)) {        // Create the attribute
		((JButton)source).setEnabled(false);
		// Notify the tables that editing has stopped

		String name = (String)tfn.get();
		if (name != null && name.trim().length() > 0) {
		  name = name.trim();
		  if (!name.equals("")) {
		    try {
		      attr = Attribute.create(cdf, name, scope);
		      setVisible(false);
		    } catch (CDFException e) {
		      e.printStackTrace();
		      Toolkit.getDefaultToolkit().beep();
		      JOptionPane.showMessageDialog(myFrame,
						  "CDF Error:\n"+
						  CDFException.getStatusMsg(e.getCurrentStatus()),
						  "MYEditor: CDFException",
						  JOptionPane.ERROR_MESSAGE);
		      attr = null;
		    }
		  }
		} else {
                  Toolkit.getDefaultToolkit().beep();
                  JOptionPane.showMessageDialog(myFrame,
                                                "Variable name is missing",
                                                "MYEditor: CDFException",
                                                JOptionPane.ERROR_MESSAGE);
                  attr = null;
		}
		((JButton)source).setEnabled(true);
	    }
	}
    }

    private void reset() {
	// This will occur once the dialog is closed

	tfn.set("");
	cbas.comboBox.setSelectedIndex((int)scope-1);
	tfn.setEnabled(true);
	cbas.setEnabled(false);
	
    }

}
