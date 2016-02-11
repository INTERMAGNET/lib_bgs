package bgs.geophys.library.cdf.edit;

// $Id: CDFE_EditVariableDialog.java,v 1.2 2015/07/13 14:24:52 liu Exp $
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
public class CDFE_EditVariableDialog extends JDialog implements ActionListener, CDFConstants {

    static final long serialVersionUID = 1L;

    private JButton             enter, end, reset;
    private CDFE_JLabeledTF          jtfne, jtfcl, jtfbf, jtfir, jtfpv, jtfstr, jtfend,
				jtfstrd, jtfendd;
    private CDFE_WholeNumberField    tfdim;
    private JTextField		tfne, tfcl, tfbf, tfir, tfpv, tfstr, tfend, tfstrd, tfendd;
    private CDFE_JLabeledCB          cbdt, cbcp, cbsp;
    private JCheckBox           cbrv;
    private CDFE_JLabeledDataTable   jdtsizes, jdtvarys, jdtallorecs;
    private JFrame              myFrame;
    private long		recVariance;
    private Object 		pad;
    private String		padString;
    private static final String ENTER = "Change";
    private static final String END = "End";
    private static final String RESET = "Reset";

    private long numDim, dataType, numElements, bf, sparse, compress = -1, compresslvl;
    private long[] dimSizes, dimVariances, compressParms;
    private boolean[] dimVarL;
    private long numRecA, numRecW;
    private boolean editable;

    private Variable var = null;
    private CDF cdf = null;

    private Dimension ss;
    private Dimension ps;

    /**
     * default constructor
     */
    public CDFE_EditVariableDialog() {
	// needed to allow extending this class
    }

    public CDFE_EditVariableDialog(JFrame frame, Variable var)
    {
	super(frame, true);
	this.myFrame = frame;
	this.var = var;

	setTitle("Edit zVariable: "+var.getName());

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

	setResizable(true);
	setSize(500,450);
	setLocation(10, 20);
	setVisible(false);

	ss = Toolkit.getDefaultToolkit().getScreenSize();
	ps = getPreferredSize();    

        try {
	  numRecA = var.getNumAllocatedRecords();
          numRecW = var.getNumWrittenRecords();
	  editable = (numRecA > 0 || numRecW > 0) ? false : true;
	} catch (CDFException ex) {
	  editable = false;
	}

	JSplitPane sp = new JSplitPane();
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
        // Build the required specifications panel
        CDFE_JLabeledPanel rsp = new CDFE_JLabeledPanel("", BorderFactory.createEmptyBorder());
	rsp.setLayout(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

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

	cbdt = new CDFE_JLabeledCB("Data Type", types, false, editable);
	cbdt.addItemListener(new DataTypeListener());
	gbc.gridwidth = GridBagConstraints.RELATIVE;
	gbl.setConstraints(cbdt, gbc);
	rsp.add(cbdt);
	
	tfne = new JTextField(2);
	jtfne = new CDFE_JLabeledTF("NumElements", tfne, editable);
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
        CDFE_JLabeledPanel osp = new CDFE_JLabeledPanel("", BorderFactory.createEmptyBorder());

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
	gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(cbcp, gbc);
        osp.add(cbcp);
        
        tfcl = new JTextField(1);
        jtfcl = new CDFE_JLabeledTF("Level", tfcl, false);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfcl, gbc);
        osp.add(jtfcl);

        tfpv = new JTextField(3);
        jtfpv = new CDFE_JLabeledTF("Pad Value", tfpv, true);
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

        JPanel jd = createDelRecsPanel();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jd, gbc);
	osp.add(jd);

        sp.setLeftComponent (rsp);
        sp.setRightComponent(osp);

        // Provide minimum sizes for the two components in the split pane
        rsp.setMinimumSize(new Dimension(260, 400));
        osp.setMinimumSize(new Dimension(240, 400));

        // Set the initial location and size of the divider
        sp.setDividerLocation(260);
        sp.setDividerSize(5);

        // Provide a preferred size for the split pane
        sp.setPreferredSize(new Dimension(500, 400));

	mp.add(sp, BorderLayout.CENTER);
	createButtonPanel( mp );

	reset();

//	pack();
	setVisible(true);

    }

    private void createButtonPanel(JPanel jp) {
	JPanel bp = new JPanel();
	
	enter = new JButton(ENTER);
	enter.addActionListener( this );
	enter.setToolTipText("Modify the variable based on the entered fields");
	bp.add(enter);
	bp.add(Box.createRigidArea(new Dimension(40, 1)));
	reset = new JButton(RESET);
	reset.setToolTipText("Reset fields back to their current values"); 
	reset.addActionListener( this );
	bp.add(reset);
	bp.add(Box.createRigidArea(new Dimension(40, 1)));
        end = new JButton(END);
	end.setToolTipText("End/Quit the editing");
        end.addActionListener( this );
        bp.add(end);
	
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

	cbrv = new JCheckBox("Record");
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(cbrv, gbc);
	jp.add(cbrv);
	
	jdtvarys = new CDFE_JLabeledDataTable("Dimension", editable);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(jdtvarys, gbc);
	jp.add(jdtvarys);

	jp.setEnabled(true);
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
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.NONE;
	gbl.setConstraints(jtf, gbc);
	jp.add(jtf);

	jdtsizes = new CDFE_JLabeledDataTable("Sizes", false);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(jdtsizes, gbc);
	jp.add(jdtsizes);

	jp.setEnabled(true);
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
        jtfstr = new CDFE_JLabeledTF("Start  Rec#", tfstr, true);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfstr, gbc);
        jp.add(jtfstr);

	tfend = new JTextField(4);
        jtfend = new CDFE_JLabeledTF("End  Rec#", tfend, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;    
        gbl.setConstraints(jtfend, gbc);   
        jp.add(jtfend);   
    
        return jp;
    }

    private JPanel createDelRecsPanel() {
        CDFE_JLabeledPanel jp = new CDFE_JLabeledPanel("Delete Records");

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        jp.setLayout(gbl);

        // Set the default contraints
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2,2,2,2);

        tfstrd = new JTextField(4);
        jtfstrd = new CDFE_JLabeledTF("Start  Rec#", tfstrd, true);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfstrd, gbc);
        jp.add(jtfstrd);

        tfendd = new JTextField(4);
        jtfendd = new CDFE_JLabeledTF("End  Rec#", tfendd, true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfendd, gbc);
        jp.add(jtfendd);
   
        return jp;
    }

    /**
     * Make sure the change flag is set if a new ComboBox item is selected
     */

    /** Action for comboBox for data type
      */

    class DataTypeListener implements ItemListener {
      public void itemStateChanged(ItemEvent event) {
	Object source = event.getSource();
	String type = (String)cbdt.get();
	if (type.equals("CDF_CHAR") || type.equals("CDF_UCHAR")) {
	  tfne.setText(""+numElements);
	  jtfne.setEnabled(true);
	  tfne.setEditable(editable);
	} else {
	  tfne.setText("1");
	  jtfne.setEnabled(true);
	  tfne.setEditable(false);
	}
      }
    }

    /** Action for comboBox for compression
      */

    class CompressionListener implements ItemListener {
      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
	if (cbcp.getIndex() == 4) { // GZIP
	  jtfcl.setVisible(true);
	  jtfcl.setEnabled(true);
	  jtfcl.setEditable(true);
	  jtfcl.set("5");
        } else {
          jtfcl.setVisible(true);               
          jtfcl.setEnabled(true);   
	  jtfcl.setEditable(false);
          if (cbcp.getIndex() == 0) jtfcl.set("");
	  else jtfcl.set("0");
	}
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
	    if (action.equals(END)) {              // End the editing
		setVisible(false);
		enter.removeActionListener(this);
		end.removeActionListener(this);
		reset.removeActionListener(this);
		dispose();
		System.gc();
            } else if (action.equals(RESET)) {    // Cancel the change
                reset();
	    } else if (action.equals(ENTER)) {        // Edit the variable
		((JButton)source).setEnabled(false);
		if (editable) {
		  long dt;
		  switch (cbdt.getIndex()) {
		    case 0:  dt = CDF_BYTE;   break;
		    case 1:  dt = CDF_INT1;   break;
		    case 2:  dt = CDF_UINT1;  break;
		    case 3:  dt = CDF_INT2;   break;
		    case 4:  dt = CDF_UINT2;  break;
		    case 5:  dt = CDF_INT4;   break;
		    case 6:  dt = CDF_UINT4;  break;
		    case 7:  dt = CDF_INT8;  break;
		    case 8:  dt = CDF_REAL4;  break;
		    case 9:  dt = CDF_FLOAT;  break;
		    case 10:  dt = CDF_REAL8;  break;
		    case 11: dt = CDF_DOUBLE; break;
		    case 12: dt = CDF_EPOCH;  break;
		    case 13: dt = CDF_EPOCH16;  break;
		    case 14: dt = CDF_TIME_TT2000;  break;
		    case 15: dt = CDF_CHAR;   break;
		    case 16: dt = CDF_UCHAR;  break;
		    // keep compiler happy
		    default: dt = CDF_BYTE;   break;
		  }

		  long ne = 1;
		  
		  if ((dataType == CDF_CHAR) || 
		      (dataType == CDF_UCHAR))
		    ne = Long.parseLong((String)tfne.getText());
		  if (dataType != dt || numElements != ne) {
		    try { 
		      var.updateDataSpec(dt, ne);
		      dataType = ne;
		      numElements = ne;
		    } catch (CDFException ex) {
		      Toolkit.getDefaultToolkit().beep();
                      JOptionPane.showMessageDialog(myFrame,
                                            CDFException.getStatusMsg(ex.getCurrentStatus()),
                                            "MYEditor: Data Type Change",
                                            JOptionPane.ERROR_MESSAGE);
		      cbdt.set(CDFUtils.getStringDataType(dataType));
		    }
		  }
		}
		doEditing();
		((JButton)source).setEnabled(true);

	    }
	}
    }

    private void reset() {

	numDim = var.getNumDims();
	numElements = var.getNumElements();
	dimSizes = var.getDimSizes();
	dataType = var.getDataType();
	dimVariances = var.getDimVariances();
	dimVarL = new boolean[(int)numDim];

	for (int i = 0; i < (int) numDim; i++) 
	  dimVarL[i] = dimVariances[i] == VARY ? true : false;
	recVariance = var.getRecVariance() ? VARY : NOVARY;
	
	pad = null;
	try {
	  if (var.checkPadValueExistence()) 
	    pad = var.getPadValue();
	} catch (CDFException e) {}

	try {
	    bf = var.getBlockingFactor();
        } catch (CDFException e) {
          bf = 0;
        }
	sparse = var.getSparseRecords();
	compress = var.getCompressionType();
	if (compress == GZIP_COMPRESSION) {
	  compressParms = var.getCompressionParms();
	  compresslvl = compressParms[0];
	}

	tfdim.setText(""+numDim);
	tfdim.setEnabled(true);
	tfdim.setEditable(false);

	tfne.setText(""+numElements);
	jtfne.setEnabled(true);
	if (editable) 
	  if (dataType == CDF_CHAR || dataType == CDF_UCHAR) 
	    tfne.setEditable(true);
	  else
	    tfne.setEditable(false);

        cbdt.set(CDFUtils.getStringDataType(dataType));
	cbdt.setEnabled(true);
        cbdt.comboBox.setEnabled(editable);

	tfdim.setEnabled(true);
	tfdim.setEditable(false);
	jdtsizes.setEnabled(true);

        if (numDim > 0) {
            jdtsizes.setVisible(true);
            jdtsizes.getModel().createEmptyTable(1, (int)numDim,
                                                 Long.TYPE);
	    jdtsizes.getModel().setData(dimSizes);
	    jdtsizes.setEnabled(true);

            jdtvarys.setVisible(true);
	    jdtvarys.setEnabled(true);
            jdtvarys.getModel().createEmptyTable(1, (int)numDim,
                                                 Boolean.TYPE);
            jdtvarys.getModel().setData(dimVarL);
        } else {
            jdtsizes.setVisible(true);
            jdtsizes.getModel().setData(null);
            jdtvarys.setVisible(true);
            jdtvarys.getModel().setData(null);
        }

	cbrv.setSelected(recVariance == VARY ? true : false);
	cbrv.setEnabled(editable);

	doPading();

	doBlockingFactor();

	doSparseness();

	doCompression();

	tfstr.setText("");
	tfend.setText("");
	tfstrd.setText("");
	tfendd.setText("");
	tfir.setText("");
	if (recVariance == NOVARY) { 
	  tfstr.setEditable(false);
	  tfend.setEditable(false);
	  tfstrd.setEditable(false);
	  tfendd.setEditable(false);
	  tfir.setEditable(false);
	}
    }

    private void doEditing() {

        // record variance
        long rv = cbrv.isSelected() ? VARY : NOVARY;
        if (rv != recVariance) {
          try {
	    var.setRecVariance(rv);
            recVariance = rv;
	  } catch (CDFException ex) {
	    Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Record Variances Error",
                              JOptionPane.ERROR_MESSAGE);
          }
        }

        // dimension variances
	if (numDim > 0) {
          long[] dv = new long[(int)numDim];
	  boolean changed = false;
	  boolean[] dvX = new boolean[(int)numDim];

	  if (numDim == 1)
	    dvX[0] = ((Boolean)jdtvarys.getModel().getData()).booleanValue(); 
	  else 
	    dvX = (boolean[])jdtvarys.getModel().getData();

	  for (int i = 0; i < numDim; i++) {
	    dv[i] = dvX[i] ? VARY : NOVARY;
	    if (dv[i] != dimVariances[i]) changed = true;
	  }
          if (changed) {
            try {
              var.setDimVariances(dv);
	      dimVariances = dv;
            } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Dimension Variances Error",
                              JOptionPane.ERROR_MESSAGE);
	    }
          }
        }

	// blocking factor
        String sbf = tfbf.getText();
	if (sbf != null && sbf.trim().length() != 0) {
	  long da = Long.parseLong(sbf);
	  if (da != bf) {
	    try {
		var.setBlockingFactor(da);
		bf = da;
	    } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Blocking Factor Error",
                              JOptionPane.ERROR_MESSAGE);
	      doBlockingFactor();
	    }
	  }
	}

	// record sparseness
	long sa = (long) cbsp.getIndex();
	if (sa > -1 && sa != sparse) {
            try {
                var.setSparseRecords(sa);
		sparse = sa;
            } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Record Sparseness Error",
                              JOptionPane.ERROR_MESSAGE);
	      doSparseness();
            }
	}

	// compression
	long cp = (long) cbcp.getIndex();
	long[] cl = new long[] {0};
	if (cp != compress) {
	  if (cp == 4) {
	    cp++;
	    cl[0] = Long.parseLong(tfcl.getText());
          }
	  try {
              var.setCompression(cp, cl);
	      compress = cp;
	      if (cp == 5) compressParms[0] = cl[0];
	      if (cp == 5) compresslvl = cl[0];
          } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Compression Error",
                              JOptionPane.ERROR_MESSAGE);
	      doCompression();
          }
	} else {
	  if (cp == 4) {
	    cp++;
	    cl[0] = Long.parseLong(tfcl.getText());
	    if (cl[0] != compresslvl) {
	      try {
                var.setCompression(cp, cl);
                compress = cp;
                compressParms[0] = cl[0];
		compresslvl = cl[0];
              } catch (CDFException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Compression Error",
                              JOptionPane.ERROR_MESSAGE);
	        doCompression();
	      }
	    }
	  }
	}

	// pad value
	String pv = tfpv.getText();
	if (pv != null) pv = pv.trim();
	if (pv.length() != 0) {
	  if (!pv.equals(padString)) {
	    try {
	      Object padValue = CDFE_CDFToolUtils.parseContent(pv, dataType, numElements);
	      if (padValue != null) {
                var.setPadValue(padValue);
	        pad = padValue;
	        padString = pv;
	      }
            } catch (CDFException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Pad Value Error",
                              JOptionPane.ERROR_MESSAGE);
	      doPading();
            } catch (NumberFormatException ex) {
	      Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(myFrame,
                                            ex.toString(),
                                            "MYEditor: CDFException",
                                            JOptionPane.ERROR_MESSAGE);
	      doPading();
	    }
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
	String strend = tfend.getText();
	if (str != null && strend != null) {
	  String str1 = str.trim();
	  String end1 = strend.trim();
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

        // delete records
        str = tfstrd.getText();
        strend = tfendd.getText();
        if (str != null && strend != null) {
          String str1 = str.trim();
          String end1 = strend.trim();
          if (str1 != null && !str1.equals("") &&
              end1 != null && !end1.equals("")) {
            recstr = Long.parseLong(str1);
            recend = Long.parseLong(end1);
            if (recstr <= recend && recstr >= 1) {
              try {
                var.deleteRecords(recstr - 1, recend - 1);
              } catch (CDFException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myFrame,
                              CDFException.getStatusMsg(ex.getCurrentStatus()),
                              "CDFEdit: Delete records Error",
                              JOptionPane.ERROR_MESSAGE);
              }
            }
          }
        }

    }

    private void doCompression() {
        cbcp.comboBox.setSelectedIndex((int)
                        (compress== GZIP_COMPRESSION ? compress-1 : compress));
        if (compress == GZIP_COMPRESSION) {
          jtfcl.setVisible(true);
          jtfcl.setEnabled(true);
          jtfcl.setEditable(true);
          tfcl.setText(""+compresslvl);
        } else {
          jtfcl.setVisible(true);
          jtfcl.setEnabled(true);
          jtfcl.setEditable(false);
          if (compress == NO_COMPRESSION) tfcl.setText("");
          else tfcl.setText("0");
        }
    }

    private void doSparseness() {
        if (sparse == 0)
          cbsp.comboBox.setSelectedIndex(-1);
        else
          cbsp.comboBox.setSelectedIndex((int)sparse);
    }

    private void doBlockingFactor() {
        if (bf == 0)
          tfbf.setText("");
        else
          tfbf.setText(""+bf);
    }

    private void doPading() {
        if (pad == null)
          padString = "";
        else {
          if (dataType != CDF_EPOCH && dataType != CDF_EPOCH16 &&
              dataType != CDF_TIME_TT2000)
            padString = pad.toString();
          else {
	    if (dataType == CDF_EPOCH16) {
	      double[] aaa = new double[2];
	      aaa[0] = ((Double)pad).doubleValue();
	      aaa[1] = ((Double)pad).doubleValue();
              padString = Epoch16.encode(aaa);
	    } else if (dataType == CDF_EPOCH) {
              double aaa = ((Double)pad).doubleValue();
              padString = Epoch.encode(aaa);
            } else {
              long aaa = ((Long)pad).longValue();
	      padString = CDFTT2000.toUTCstring(aaa);
            }
	  }
        }
        tfpv.setText(padString);
    }
}
