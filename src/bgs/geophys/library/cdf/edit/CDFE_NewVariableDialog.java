package bgs.geophys.library.cdf.edit;

// $Id: CDFE_NewVariableDialog.java,v 1.2 2015/07/13 14:24:52 liu Exp $
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
import gsfc.nssdc.cdf.util.*;

/**
 * Present the user with a dialog box to allow CDF variable creation
 *
 * @author Phil Williams
 *
 */
public class CDFE_NewVariableDialog extends JDialog implements CDFConstants,
						ActionListener, DocumentListener {

    static final long serialVersionUID = 1L;

    private String              name;
    private JButton             enter, cancel;
    private CDFE_JLabeledTF          jtfvn, jtfne, jtfcl, jtfbf, jtfir, jtfpv, jtfstr, jtfend;
    private CDFE_WholeNumberField    tfdim, tfne;
    private JTextField		tfcl, tfbf, tfir, tfpv, tfstr, tfend;
    private CDFE_JLabeledCB          cbdt, cbcp, cbsp;
    private JCheckBox           cbrv;
    private long		dataType;
    private CDFE_JLabeledDataTable   jdtsizes, jdtvarys, jdtallorecs;
    private JFrame              myFrame;
    private static final String ENTER = "Create";
    private static final String CANCEL = "Cancel";
    private boolean fromEnter = false;

    private static CDFE_NewVariableDialog cache;

    private Variable var = null;
    private CDF cdf = null;

    private long [] sizes;
    private boolean [] varys;
    private long numElements;

    private Dimension ss;
    private Dimension ps;

    /**
     * default constructor
     */
    public CDFE_NewVariableDialog() {
	// needed to allow extending this class
    }

    private CDFE_NewVariableDialog(JFrame frame)
    {
	super(frame, true);
	this.myFrame = frame;
	setTitle("Create zVariable");

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
	setSize(600,530);
	setLocation(10, 20);
	setVisible(false);

	ss = Toolkit.getDefaultToolkit().getScreenSize();
	ps = getPreferredSize();    

	JSplitPane sp = new JSplitPane();
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
        // Build the required specifications panel
        CDFE_JLabeledPanel rsp = new CDFE_JLabeledPanel("Mandatory");
	rsp.setLayout(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

	jtfvn = new CDFE_JLabeledTF("Variable Name", 10);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(jtfvn, gbc);
	rsp.add(jtfvn);
	
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
	cbdt.set("CDF_BYTE");
	cbdt.addItemListener(new DataTypeListener());
	gbc.gridwidth = GridBagConstraints.RELATIVE;
	gbl.setConstraints(cbdt, gbc);
	rsp.add(cbdt);
	
	tfne = new CDFE_WholeNumberField(1, 3);
	jtfne = new CDFE_JLabeledTF("NumElements", tfne, true);
	jtfne.setEditable(false);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.NONE;
	gbl.setConstraints(jtfne, gbc);
	rsp.add(jtfne);

	JPanel jp = createDimensionPanel();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(jp, gbc);
	rsp.add(jp);

	jp = createVariancePanel();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(jp, gbc);
	rsp.add(jp);

        // Build the optional specifications panel
        CDFE_JLabeledPanel osp = new CDFE_JLabeledPanel("Optional");

        osp.setLayout(gbl);

        // Set the default contraints
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2,2,2,2);

        Vector sparseness = new Vector(3);
        sparseness.addElement("None");
        sparseness.addElement("PAD_SPARSERECORDS");
        sparseness.addElement("PREV_SPARSERECORDS");

        cbsp = new CDFE_JLabeledCB("Record Sparseness", sparseness, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(cbsp, gbc);
        osp.add(cbsp);

        Vector compression = new Vector(5);
        compression.addElement("None");
        compression.addElement("RLE");
        compression.addElement("HUFF");
	compression.addElement("AHUFF");
	compression.addElement("GZIP");
        
        cbcp = new CDFE_JLabeledCB("Compression", compression, true);
        cbcp.addItemListener(new CompressionListener());
        gbc.gridwidth = GridBagConstraints.RELATIVE;    
        gbl.setConstraints(cbcp, gbc);
        osp.add(cbcp);
        
        tfcl = new JTextField(1);
        jtfcl = new CDFE_JLabeledTF("Level", tfcl, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfcl, gbc);
        osp.add(jtfcl);

        tfpv = new JTextField(0);
        jtfpv = new CDFE_JLabeledTF("Pad Value                                                 ", tfpv, true);
	jtfpv.set(""+DEFAULT_BYTE_PADVALUE);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfpv, gbc);
        osp.add(jtfpv);

        tfbf = new JTextField(3);
        jtfbf = new CDFE_JLabeledTF("Blocking Factor", tfbf, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfbf, gbc);
        osp.add(jtfbf);

        tfir = new JTextField(3);
        jtfir = new CDFE_JLabeledTF("Write Initial Records", tfir, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfir, gbc);
        osp.add(jtfir);
        
        JPanel jx = createAllocRecsPanel();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jx, gbc);
        osp.add(jx);

        sp.setLeftComponent (rsp);
        sp.setRightComponent(osp);

        // Provide minimum sizes for the two components in the split pane
        rsp.setMinimumSize(new Dimension(260, 530));
        osp.setMinimumSize(new Dimension(260, 530));

        // Set the initial location and size of the divider
        sp.setDividerLocation(260);
        sp.setDividerSize(3);

        // Provide a preferred size for the split pane
        sp.setPreferredSize(new Dimension(520, 530));

	mp.add(sp, BorderLayout.CENTER);
	createButtonPanel( mp );

    }

    private void createButtonPanel(JPanel jp) {
	JPanel bp = new JPanel();
	
	enter = new JButton(ENTER);
	enter.addActionListener( this );
	enter.setToolTipText("Create the variable");
	bp.add(enter);
	cancel = new JButton(CANCEL);
	cancel.addActionListener( this );
	cancel.setToolTipText("Cancel the creation of a variable");
	bp.add(cancel);
	
	jp.add(bp, BorderLayout.SOUTH);
    }

    private JPanel createVariancePanel() {
	CDFE_JLabeledPanel jp = new CDFE_JLabeledPanel("Variances");

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	jp.setLayout(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

	cbrv = new JCheckBox("Record (Time)", true);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(cbrv, gbc);
	jp.add(cbrv);
	
	jdtvarys = new CDFE_JLabeledDataTable("Dimension");
	jdtvarys.setVisible(false);
	jdtvarys.setEnabled(false); // Since only Z vars are currently created
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(jdtvarys, gbc);
	jp.add(jdtvarys);

	return jp;
    }

    private JPanel createDimensionPanel() {
	CDFE_JLabeledPanel jp = new CDFE_JLabeledPanel("Dimensions");

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	jp.setLayout(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

	CDFE_JLabeledTF jtf;
	tfdim = new CDFE_WholeNumberField(0, 3);
	jtf = new CDFE_JLabeledTF("Number", tfdim, true);
	tfdim.getDocument().addDocumentListener( this );
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.NONE;
	gbl.setConstraints(jtf, gbc);
	jp.add(jtf);

	jdtsizes = new CDFE_JLabeledDataTable("Sizes");
	jdtsizes.setVisible(false);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(jdtsizes, gbc);
	jp.add(jdtsizes);

	return jp;
    }

    private JPanel createAllocRecsPanel() {
        CDFE_JLabeledPanel jp = new CDFE_JLabeledPanel("Allocate Records");

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        jp.setLayout(gbl);

        // Set the default contraints
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2,2,2,2);

	tfstr = new JTextField(4);
        jtfstr = new CDFE_JLabeledTF("Start  Rec  #", tfstr, true);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfstr, gbc);
        jp.add(jtfstr);

	tfend = new JTextField(4);
        jtfend = new CDFE_JLabeledTF("End  Rec  #", tfend, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;    
        gbl.setConstraints(jtfend, gbc);   
        jp.add(jtfend);   
    
        return jp;
    }

    /**
     * Present the user with a modal dialog box to create variables.
     *
     * @param frame the parent frame.
     * @param cdf the CDF file where the variable will be created
     */
    public static Variable createVariable(JFrame frame, CDF cdf) {
	if (cache == null)
	    cache = new CDFE_NewVariableDialog(frame);

	cache.cdf = cdf;
	// Deprecated - cache.jtfvn.requestDefaultFocus();
	cache.jtfvn.requestFocus();
	cache.setTitle("Create zVariable");

	cache.reset();

 	cache.setVisible(true);

	return cache.var;

    }

    /**
     * Present the user with a modal dialog box to create a variable with
     * the given defaults.  Any of the items may be turned off by use of
     * the appropriate flags.
     *
     * @param frame the parent frame
     * @param cdf the CDF file where the variable will be created
     * @param title The title for the dialog
     * @param defaults A seven element Object array that are the default 
     *     values for (in this order) name (String), data type (Long), 
     *     numElements (Long), numDims (Long), dimSizes (long []), 
     *     recVary (Boolean), dimVarys (boolean [])
     * @param enabled A seven element boolean array that can be used to 
     *     disable any of the components.  Some of these may be overrode by
     *     another elements value, for instance allow the numDims to be changed
     *     implies that the dimSizes and dimVarys may also be changed
     */
    public static Variable createVariable(JFrame frame, 
					  CDF cdf, 
					  String title,
					  Object [] defaults, 
					  boolean [] enabled)
	throws IllegalArgumentException
    {
	// Check to make sure that all the defaults are of the proper type
	if (defaults.length != 7)
	    throw new 
		IllegalArgumentException("Wrong number of default values");

	if (enabled.length != 7)
	    throw new 
		IllegalArgumentException("Wrong number of enabled values");

	if (!(defaults[0] instanceof String))
	    throw new IllegalArgumentException("Bad variable name.");

	if (!(defaults[1] instanceof Long))
	    throw new IllegalArgumentException("Bad data type.");

	if (!(defaults[2] instanceof Long))
	    throw new IllegalArgumentException("Bad numElements.");

	if (!(defaults[3] instanceof Long))
	    throw new IllegalArgumentException("Bad numDims.");

	if (!defaults[4].getClass().isArray() && 
	    (Array.getLength(defaults[4]) != ((Long)defaults[3]).longValue()))
	    throw new IllegalArgumentException("Bad dimSizes.");

	if (!(defaults[5] instanceof Boolean))
	    throw new IllegalArgumentException("Bad recVary.");

	if (!defaults[6].getClass().isArray() &&
	    (Array.getLength(defaults[6]) != ((Long)defaults[3]).longValue()))
	    throw new IllegalArgumentException("Bad dimVarys.");

	if (cache == null)
	    cache = new CDFE_NewVariableDialog(frame);

	cache.cdf = cdf;
	// Deprecated - cache.jtfvn.requestDefaultFocus();
	cache.jtfvn.requestFocus();

	// Set the defaults

	cache.setTitle(title);
	cache.jtfvn.set(defaults[0].toString());
	cache.cbdt.set(CDFUtils.getStringDataType(((Long)defaults[1]).longValue()));
	cache.tfne.setText(defaults[2].toString());
	cache.tfdim.setText(defaults[3].toString());
	if (cache.tfdim.longValue() != 0) {
	    cache.jdtsizes.setVisible(true);
	    cache.jdtsizes.getModel().setData(defaults[4]);
	    cache.jdtvarys.setVisible(true);
	    cache.jdtvarys.getModel().setData(defaults[6]);
	}
	cache.cbrv.setSelected(((Boolean)defaults[5]).booleanValue());

	// Enabled/Disable
	if (!defaults[0].toString().equals(""))
	    cache.jtfvn.setEnabled(enabled[0]);
	cache.cbdt.setEnabled(enabled[1]);
	if (((Long)defaults[2]).longValue() == CDF_CHAR)
	    cache.tfne.setEnabled(enabled[2]);
	cache.tfdim.setEnabled(enabled[3]);
	if (!cache.tfdim.isEnabled()) {
	    cache.jdtsizes.setEnabled(enabled[4]);
	    cache.jdtvarys.setEnabled(false); //enabled[6]
	}
	cache.cbrv.setEnabled(enabled[5]);
	
	cache.setVisible(true);

	return cache.var;
    }	

    /**
     * Listen for text events in the nDims textfield
     */
    public void removeUpdate(DocumentEvent e) {
	long nDims;
	if (!tfdim.getText().equals(""))
	    nDims = tfdim.longValue();
	else 
	    nDims = -1;

	if (nDims > 0) {
	    jdtsizes.setVisible(true);
	    jdtsizes.getModel().createEmptyTable(1, (int)nDims, 
						 Long.TYPE);
	    jdtvarys.setVisible(true);
	    jdtvarys.getModel().createEmptyTable(1, (int)nDims, 
						 Boolean.TYPE);
	    boolean [] dimVarys = new boolean[(int)nDims];
	    for (int i = 0; i < (int)nDims; i++)
		dimVarys[i] = true;
	    
	    jdtvarys.getModel().setData(dimVarys);
	} else {
	    jdtsizes.setVisible(false);
	    jdtsizes.getModel().setData(null);
	    jdtvarys.setVisible(false);
	    jdtvarys.getModel().setData(null);
	}
    }

    public void insertUpdate(DocumentEvent e) {
	long nDims;
	if (!tfdim.getText().equals(""))
	    nDims = tfdim.longValue();
	else 
	    nDims = -1;
	if (nDims > 0) {
	    jdtsizes.setVisible(true);
	    jdtsizes.getModel().createEmptyTable(1, (int)nDims, 
						 Long.TYPE);
	    jdtvarys.setVisible(true);
	    jdtvarys.getModel().createEmptyTable(1, (int)nDims, 
						 Boolean.TYPE);
	    boolean [] dimVarys = new boolean[(int)nDims];
	    for (int i = 0; i < (int)nDims; i++)
		dimVarys[i] = true;
	    
	    jdtvarys.getModel().setData(dimVarys);
	} else {
	    jdtsizes.setVisible(false);
	    jdtsizes.getModel().setData(null);
	    jdtvarys.setVisible(false);
	    jdtvarys.getModel().setData(null);
	}
    }
    
    public void changedUpdate(DocumentEvent e) {
	System.err.print("changedUpdate: "+e.toString());
    }

    /** Action for comboBox for compression */

    class CompressionListener implements ItemListener {
      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
        int ind = cbcp.getIndex();
        if (ind > 3) ind++;
        if (ind == 5) { // GZIP
          jtfcl.setVisible(true);
          jtfcl.setEnabled(true);
          jtfcl.setEditable(true);
          jtfcl.set("5");
        } else {
          jtfcl.setVisible(true);
          jtfcl.setEnabled(true);
          jtfcl.setEditable(false);
          if (ind == 0)
            jtfcl.set("");
          else
            jtfcl.set("0");
        }
     }
   }

    /** Action for comboBox for data type
      */

    class DataTypeListener implements ItemListener {

      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
        String sl = (String) ((JComboBox) source).getSelectedItem();
        if (sl.equals("CDF_CHAR") || sl.equals("CDF_UCHAR")) {
	  jtfne.set("10");
	  tfne.setEnabled(true);
	  jtfne.setVisible(true);
	  jtfne.setEditable(true);
	} else {
	  jtfne.set("1");
	  tfne.setEnabled(true);
	  jtfne.setVisible(true);
	  jtfne.setEditable(false); 
	}
	padValueReset(sl);
      }
    }

    private void padValueReset(String sl) {
        if (sl.equals("CDF_BYTE")) 
	  jtfpv.set(""+DEFAULT_BYTE_PADVALUE);
        else if (sl.equals("CDF_INT1")) 
	  jtfpv.set(""+DEFAULT_INT1_PADVALUE);
        else if (sl.equals("CDF_UINT1"))
	  jtfpv.set(""+DEFAULT_UINT1_PADVALUE);
        else if (sl.equals("CDF_INT2"))
	  jtfpv.set(""+DEFAULT_INT2_PADVALUE);
        else if (sl.equals("CDF_UINT2"))
	  jtfpv.set(""+DEFAULT_UINT2_PADVALUE);
        else if (sl.equals("CDF_INT4"))
	  jtfpv.set(""+DEFAULT_INT4_PADVALUE);
        else if (sl.equals("CDF_UINT4"))
	  jtfpv.set(""+DEFAULT_UINT4_PADVALUE);
        else if (sl.equals("CDF_INT8"))
	  jtfpv.set(""+DEFAULT_INT8_PADVALUE);
        else if (sl.equals("CDF_REAL4"))
	  jtfpv.set(""+DEFAULT_REAL4_PADVALUE);
        else if (sl.equals("CDF_FLOAT"))
	  jtfpv.set(""+DEFAULT_FLOAT_PADVALUE);
        else if (sl.equals("CDF_REAl8"))
	  jtfpv.set(""+DEFAULT_REAL8_PADVALUE);
        else if (sl.equals("CDF_DOUBLE"))
	  jtfpv.set(""+DEFAULT_DOUBLE_PADVALUE);
        else if (sl.equals("CDF_EPOCH"))
	  jtfpv.set("01-Jan-0000 00:00:00.000");
        else if (sl.equals("CDF_EPOCH16"))
	  jtfpv.set("01-Jan-0000 00:00:00.000.000.000.000");
        else if (sl.equals("CDF_TIME_TT2000"))
	  jtfpv.set("0000-01-01T00:00:00.000000000");
        else if (sl.equals("CDF_CHAR") || sl.equals("CDF_UCHAR")) {
	  char[] array = new char[ (int)tfne.longValue() ];
	  for (int ix = 0; ix < tfne.longValue(); ++ix)
		 array[ix] = DEFAULT_CHAR_PADVALUE;
	  jtfpv.set("\""+new String(array)+"\"");
	}
    }

    /**
     * Process button events and change of dimension events.
     */
    public void actionPerformed( ActionEvent event )
    {
	Object source = event.getSource();

	if (source instanceof JButton) {
	    String action = event.getActionCommand();
	    if (action.equals(CANCEL)) {              // Cancel the operation
		if (!fromEnter) var = null;
//		reset();
		setVisible(false);
		dispose();
		System.gc();
	    } else if (action.equals(ENTER)) {        // Create the variable
		((JButton)source).setEnabled(false);
		// Notify the tables that editing has stopped
		jdtsizes.getDataTable().
		    editingStopped(new ChangeEvent( jdtsizes ));

		name = (String)jtfvn.get();

		if (name != null && name.trim().length() > 0) {
		  name = name.trim();
		  switch (cbdt.getIndex()) {
		    case 0:  dataType = CDF_BYTE; break;
//		             jtfpv.set(""+DEFAULT_BYTE_PADVALUE); break;
		    case 1:  dataType = CDF_INT1; break;
//		             jtfpv.set(""+DEFAULT_BYTE_PADVALUE); break;
		    case 2:  dataType = CDF_UINT1; break;
//		             jtfpv.set(""+DEFAULT_UINT1_PADVALUE); break;
		    case 3:  dataType = CDF_INT2; break;
//		             jtfpv.set(""+DEFAULT_INT2_PADVALUE); break;
		    case 4:  dataType = CDF_UINT2; break;
//		             jtfpv.set(""+DEFAULT_UINT2_PADVALUE); break;
		    case 5:  dataType = CDF_INT4; break;
//		             jtfpv.set(""+DEFAULT_INT4_PADVALUE); break;
		    case 6:  dataType = CDF_UINT4; break;
//		             jtfpv.set(""+DEFAULT_UINT4_PADVALUE); break;
		    case 7:  dataType = CDF_INT8; break;
//		             jtfpv.set(""+DEFAULT_INT8_PADVALUE); break;
		    case 8:  dataType = CDF_REAL4; break;
//		             jtfpv.set(""+DEFAULT_REAL4_PADVALUE); break;
		    case 9:  dataType = CDF_FLOAT; break;
//		             jtfpv.set(""+DEFAULT_FLOAT_PADVALUE); break;
		    case 10:  dataType = CDF_REAL8; break;
//		              jtfpv.set(""+DEFAULT_REAL8_PADVALUE); break;
		    case 11: dataType = CDF_DOUBLE; break;
//		             jtfpv.set(""+DEFAULT_DOUBLE_PADVALUE); break;
		    case 12: dataType = CDF_EPOCH; break;
//		             jtfpv.set("01-Jan-0000 00:00:00.000"); break;
		    case 13: dataType = CDF_EPOCH16; break;
//		             jtfpv.set("01-Jan-0000 00:00:00.000.000.000.000"); break;
		    case 14: dataType = CDF_TIME_TT2000; break;
//		             jtfpv.set("0000-01-01T00:00:00.000000000"); break;
		    case 15: {
			     dataType = CDF_CHAR; break;
//		             char[] array = new char[ (int)tfne.longValue() ];
//			     for (int ix =0; ix < tfne.longValue(); ++ix)
//				 array[ix] = DEFAULT_CHAR_PADVALUE;
//		             jtfpv.set("\""+new String(array)+"\"");
//			     break;
			     }
		    case 16: {
			     dataType = CDF_UCHAR; break;
//                             char[] array = new char[ (int)tfne.longValue() ];
//			     for (int ix =0; ix < tfne.longValue(); ++ix)
//				 array[ix] = DEFAULT_CHAR_PADVALUE;
//                             jtfpv.set("\""+new String(array)+"\"");
//			     break;
			     }
		    // keep compiler happy
		    default: dataType = CDF_BYTE; break;
//		             jtfpv.set(""+DEFAULT_BYTE_PADVALUE); break;
		  }

		  numElements = 1;
		  if ((dataType == CDF_CHAR) || 
		    (dataType == CDF_UCHAR))
		    numElements = tfne.longValue();

		  try {
	            String pv = tfpv.getText();
	            if (pv != null) pv = pv.trim();
	            if (pv.length() != 0) {
	              Object padValue = CDFE_CDFToolUtils.parseContent(pv, dataType, numElements);
		    }
		    long recVary;
		    recVary = (cbrv.isSelected() ?
		  	       VARY : NOVARY);

		    long [] dimVarys;
		    long numDims = tfdim.longValue();
		    switch ((int)numDims) {
		      case 0:
		        sizes = new long [] {1};
		        dimVarys = new long [] {VARY};
		        break;
		      case 1:
		        sizes = new long [1];
		        dimVarys = new long [1];
		        sizes[0] = ((Long)jdtsizes.getModel().getData()).
		  	            longValue();
		        boolean dv = 
			  ((Boolean)jdtvarys.getModel().getData()).booleanValue();
		        dimVarys[0] = (dv ? VARY : NOVARY);
		        break;
		      default:
		        sizes = (long [])jdtsizes.getModel().getData();
		        varys = (boolean [])jdtvarys.getModel().getData();
		        dimVarys = new long[(int)numDims];
		        for (int i=0;i<(int)numDims;i++)
		  	  dimVarys[i] = 
			          (varys[i] ? VARY : NOVARY);
		        break;
		    }
		    
		    var = Variable.create(cdf,
					  name,
					  dataType,
					  numElements, 
					  numDims,
					  sizes,
					  recVary,
					  dimVarys);
/* Ready to set the variable from optional spec */
		    doOptional();
		    fromEnter = true;
		    cancel.doClick();
//		    reset();
//		    setVisible(false);
		  } catch (CDFException e) {
		    e.printStackTrace();
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myFrame,
						  "CDF Error:\n"+
						  CDFException.getStatusMsg(e.getCurrentStatus()),
						  "MYEditor: CDFException",
						  JOptionPane.ERROR_MESSAGE);
		    var = null;
                  } catch (NumberFormatException ex) {
                       Toolkit.getDefaultToolkit().beep();
                       JOptionPane.showMessageDialog(myFrame,
                                                     ex.toString(),
                                                     "MYEditor: Pad value error",
                                                     JOptionPane.ERROR_MESSAGE);
		       padValueReset((String) cbdt.get());
		  }
		} else {
		  Toolkit.getDefaultToolkit().beep();
                  JOptionPane.showMessageDialog(myFrame,
                                  "Variable name is missing!",
                                  "CDFEdit: CDFException",
                                  JOptionPane.ERROR_MESSAGE);
		  var = null;
		}
		((JButton)source).setEnabled(true);
	    }
	}
    }

    private void reset() {
	// This will occur once the dialog is closed

	jtfvn.set("");
	tfdim.setText("0");
	tfne.setText("1");
	cbdt.set("CDF_BYTE");
	jtfpv.set(""+DEFAULT_BYTE_PADVALUE);
	jtfvn.setEnabled(true);
	cbdt.setEnabled(true);
	tfne.setEnabled(true);
	tfne.setEditable(false);
	tfdim.setEnabled(true);
	jdtsizes.setEnabled(true);
	jdtvarys.setEnabled(true); //enabled[6]
	cbrv.setEnabled(true);
	jdtsizes.setVisible(false);
	jdtsizes.getModel().setData(null);
	jdtvarys.setVisible(false);
	jdtvarys.getModel().setData(null);
	cbrv.setSelected(true);
	cbcp.set("None");
        tfcl.setVisible(true);
        tfcl.setEnabled(true);
	tfcl.setEditable(false);
	tfcl.setText("");

	
    }

    private void doOptional() {

	// blocking factor
        String bf = tfbf.getText();
	if (bf != null && bf.trim().length() != 0) {
	  long da = Long.parseLong(bf);
	  if (da > 0) {
	    try {
		var.setBlockingFactor(da);
	    } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Blocking Factor Error",
                              JOptionPane.ERROR_MESSAGE);
	    }
	  }
	}

	// record sparseness
	long sa = (long) cbsp.getIndex();
	if (sa >= 1) {
            try {
                var.setSparseRecords(sa);
            } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Record Sparseness Error",
                              JOptionPane.ERROR_MESSAGE);

            }
	}

	// compression
	long cp = (long) cbcp.getIndex();
	if (cp >= 0) {
	  long[] cl = new long[] {0};
	  if (cp == 4) 	{
	    cp++;
	    cl[0] = Long.parseLong(tfcl.getText());
          }
	  try {
              var.setCompression(cp, cl);
          } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Compression Error",
                              JOptionPane.ERROR_MESSAGE);
          }
	}

	// pad value
	String pv = tfpv.getText();
	if (pv != null) pv = pv.trim();
	if (pv.length() != 0) {
	  try {
	    Object padValue = CDFE_CDFToolUtils.parseContent(pv, dataType, numElements);
            var.setPadValue(padValue);
          } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Pad Value Error",
                              JOptionPane.ERROR_MESSAGE);
          } catch (NumberFormatException ex) {
	     Toolkit.getDefaultToolkit().beep();
             JOptionPane.showMessageDialog(myFrame,
                                           ex.toString(),
                                           "MYEditor: CDFException",
                                           JOptionPane.ERROR_MESSAGE);
          }
	}

	// write initial records
	String ir = tfir.getText();
        if (ir != null && ir.trim().length() != 0) {
          long da = Long.parseLong(ir);
          if (da > 0) {
            try {
                var.setInitialRecords(da);
            } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Write Initial Records Error",
                              JOptionPane.ERROR_MESSAGE);
            }
	  }
        }

	// allocate records
        long recstr = 0, recend = 0;
	String str = tfstr.getText();
	String end = tfend.getText();
	if (str != null && end != null) {
	  String str1 = str.trim();
	  String end1 = end.trim();
	  if (str1 != null && !str1.equals("") && 
	      end1 != null && !end1.equals("")) {
	    recstr = Long.parseLong(str1);
	    recend = Long.parseLong(end1);
            if (recstr <= recend && recstr >= 1) {
              try {
                var.allocateBlock(recstr - 1, recend - 1);
              } catch (CDFException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Allocate records Error",
                              JOptionPane.ERROR_MESSAGE);
	      }
	    }
          }
        }

    }

}
