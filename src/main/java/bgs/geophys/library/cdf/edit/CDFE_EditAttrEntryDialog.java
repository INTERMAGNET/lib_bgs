package bgs.geophys.library.cdf.edit;

// $Id: CDFE_EditAttrEntryDialog.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
import java.util.*;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Array;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.CDFUtils;

/**
 * Present the user with a dialog box to allow CDF global attribute entry modification
 *
 * @author Mike Liu
 *
 */
public class CDFE_EditAttrEntryDialog extends JDialog implements CDFConstants, ActionListener {

    static final long serialVersionUID = 1L;

    private CDFE_EditAttrEntryPanel  myPanel;
    private CDFE_JLabeledTF          tfen, tfne, tfva;
    private CDFE_JLabeledCB          cbdt;
    private JTextField		jtfen, jtfne, jtfva;
    private JFrame              myFrame;
    private JButton		enter, cancel, reset;
    private static final String ENTER = "Modify";
    private static final String RESET = "Reset";
    private static final String CANCEL = "End";

    private static CDFE_EditAttrEntryDialog cache;
    private long scope;
    private long dataType;
    private long numElements;
    private Entry entry = null;
    private String outData;

    private Dimension ss;
    private Dimension ps;

    /**
     * default constructor
     */
    private CDFE_EditAttrEntryDialog() {
	// needed to allow extending this class
    }

    private CDFE_EditAttrEntryDialog(CDFE_EditAttrEntryPanel myPanel) {
	super((JFrame)myPanel.getMyEditor().getFrame(), true);
	myFrame = (JFrame)myPanel.getMyEditor().getFrame();
//	this.scope = scope;

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

/*	if (scope == GLOBAL_SCOPE) {
          jtfen = new JTextField(3);
          tfen = new CDFE_JLabeledTF("Entry Number", jtfen, false);
	} else {
          jtfen = new JTextField(10);
          tfen = new CDFE_JLabeledTF("Variable Name", jtfen, false);
	}
*/
        jtfen = new JTextField(20);
        tfen = new CDFE_JLabeledTF("Variable Name", jtfen, false);
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

	cbdt = new CDFE_JLabeledCB("Data Type", types, false, true);
	cbdt.comboBox.setToolTipText("Select the data type");
	cbdt.comboBox.addActionListener(this);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(cbdt, gbc);
	sp.add(cbdt);
	
        jtfne = new JTextField(3);
        tfne = new CDFE_JLabeledTF("NumElements", jtfne, true);
        tfne.textField.setToolTipText("Enter the number of elements.");
	tfne.setEnabled(true);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(tfne, gbc);
        sp.add(tfne);

	jtfva = new JTextField(40);
        tfva = new CDFE_JLabeledTF("Data", jtfva, true);
	tfva.textField.setToolTipText("Modify the entry data. Use \",\" to separate multiple values");
	jtfva.addActionListener(new ActionValue());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(tfva, gbc);
        sp.add(tfva);

	mp.add(sp, BorderLayout.CENTER);
	createButtonPanel( mp );

        setResizable(false);
        setSize(430,300);

	pack();
	setLocation(10, 20);

    }

    private void createButtonPanel(JPanel jp) {

	JPanel bp = new JPanel();
	
	enter = new JButton(ENTER);
	enter.addActionListener( this );
	enter.setToolTipText("Apply the change");
	bp.add(enter);
	
        reset = new JButton(RESET);
        reset.addActionListener( this );
	reset.setToolTipText("Rest the fields to their original values");
        bp.add(reset);

	cancel = new JButton(CANCEL);
	cancel.setToolTipText("End the change");
	cancel.addActionListener( this );
	bp.add(cancel);
	
	jp.add(bp, BorderLayout.SOUTH);
    }

    /**
     * Present the user with a modal dialog box to modify an Attribute's entry
     * for a given attribute.
     *
     * @param frame the parent frame
     * @param attr the attribute where the entry will be created to
     * @param title The title for the dialog
     */

    public static void editEntry(CDFE_EditAttrEntryPanel myPanel, String title) {
    
	if (cache == null) cache = new CDFE_EditAttrEntryDialog(myPanel);
					    
	cache.myPanel = myPanel;
	cache.scope = myPanel.getScope();
        cache.entry = myPanel.getSelectedEntry();
        cache.dataType = cache.entry.getDataType();
        cache.numElements = cache.entry.getNumElements();

        if (cache.scope == GLOBAL_SCOPE) {
          cache.tfen.setMaxLength(3);
	  cache.tfen.setLabel("Entry Number");
        } else {
          cache.tfen.setMaxLength(30); 
          cache.tfen.setLabel("Variable Name");
        }

	cache.reset();
	cache.setTitle(title);
	cache.setVisible(true);

    }	

    /**
     * Process button and combo box events 
     */
    public void actionPerformed( ActionEvent event ) {
	Object source = event.getSource();
        if (source instanceof JComboBox) {

          String item = (String)((JComboBox)source).getSelectedItem();
          long dt;
          switch (cbdt.comboBox.getSelectedIndex()) {
            case 0:  dt = CDFConstants.CDF_BYTE;   break;
            case 1:  dt = CDFConstants.CDF_INT1;   break;
            case 2:  dt = CDFConstants.CDF_UINT1;  break;
            case 3:  dt = CDFConstants.CDF_INT2;   break;
            case 4:  dt = CDFConstants.CDF_UINT2;  break;
            case 5:  dt = CDFConstants.CDF_INT4;   break;
            case 6:  dt = CDFConstants.CDF_UINT4;  break;
            case 7:  dt = CDFConstants.CDF_INT8;  break;
            case 8:  dt = CDFConstants.CDF_REAL4;  break;
            case 9:  dt = CDFConstants.CDF_FLOAT;  break;
            case 10:  dt = CDFConstants.CDF_REAL8;  break;
            case 11: dt = CDFConstants.CDF_DOUBLE; break;
            case 12: dt = CDFConstants.CDF_EPOCH;  break;
	    case 13: dt = CDFConstants.CDF_EPOCH16;  break;
	    case 14: dt = CDFConstants.CDF_TIME_TT2000;  break;
            case 15: dt = CDFConstants.CDF_CHAR;   break;
            case 16: dt = CDFConstants.CDF_UCHAR;  break;
            // keep compiler happy
            default: dt = CDFConstants.CDF_BYTE;   break;
          }
          if (dt != dataType) {
            if ((dataType == CDF_CHAR || dataType == CDF_CHAR) &&
                (dt == CDF_BYTE || dt == CDF_INT1 || dt == CDF_UINT1)) {
              String tmp = (String)tfva.get();
              tmp = tmp.substring(1, tmp.length()-1);
              StringBuffer tmpBuffer = new StringBuffer();
              byte[] tmpData = tmp.getBytes();
              for (int i = 0; i < Array.getLength(tmpData); i++) {
                if (i > 0) tmpBuffer.append(",");
                int xx = tmpData[i];
                if (dt == CDF_UINT1 && xx < 0) xx -= 2*Byte.MIN_VALUE;
                tmpBuffer.append(xx);
              }
              tmpBuffer.setLength(tmpBuffer.length()-1);
              tfva.set(tmpBuffer.toString());
		outData = (String) tfva.get();
            } else {
              if ((dataType == CDF_BYTE || dataType == CDF_INT1 || dataType == CDF_UINT1) &&
                  (dt == CDF_CHAR || dt == CDF_CHAR)) {
                String tmp = ((String)tfva.get()).trim();
                StringBuffer newStr = new StringBuffer();
                StringTokenizer st = new StringTokenizer(tmp, ",");
                if (st.countTokens() == -1) {
                  int dataValue = Integer.parseInt(tmp);
                  if (dataType == CDF_UINT1) dataValue = dataValue < 0 ?
                                                         dataValue-2*Byte.MIN_VALUE : 
							 dataValue;
                  newStr.append(dataValue);
                } else {
                   while (st.hasMoreTokens()) {
                     String str = ((String)st.nextToken()).trim();
                     int dataValue = Integer.parseInt(str);
                     if (dataType == CDF_UINT1) dataValue = dataValue < 0 ?
                                                            dataValue-2*Byte.MIN_VALUE : 
							    dataValue;
                     newStr.append(dataValue);
                   }
                }
                tfva.set("\""+newStr.toString()+"\"");
	        outData = (String) tfva.get();
              }
            }
	  }
        } else if (source instanceof JButton) {
	    String action = event.getActionCommand();
	    if (action.equals(CANCEL)) {              // Cancel the operation
		entry = null;
		setVisible(false);
		dispose();
		System.gc();
            } else if (action.equals(RESET)) {        // Reset the data
                cache.reset();
	    } else if (action.equals(ENTER)) {        // Modify the entry
		// Notify the tables that editing has stopped
		((JButton)source).setEnabled(false);
		long ndataType;
		Object value = null;

		switch (cbdt.comboBox.getSelectedIndex()) {
		case 0:  ndataType = CDFConstants.CDF_BYTE;   break;
		case 1:  ndataType = CDFConstants.CDF_INT1;   break;
		case 2:  ndataType = CDFConstants.CDF_UINT1;  break;
		case 3:  ndataType = CDFConstants.CDF_INT2;   break;
		case 4:  ndataType = CDFConstants.CDF_UINT2;  break;
		case 5:  ndataType = CDFConstants.CDF_INT4;   break;
		case 6:  ndataType = CDFConstants.CDF_UINT4;  break;
		case 7:  ndataType = CDFConstants.CDF_INT8;  break;
		case 8:  ndataType = CDFConstants.CDF_REAL4;  break;
		case 9:  ndataType = CDFConstants.CDF_FLOAT;  break;
		case 10:  ndataType = CDFConstants.CDF_REAL8;  break;
		case 11: ndataType = CDFConstants.CDF_DOUBLE; break;
		case 12: ndataType = CDFConstants.CDF_EPOCH;  break;
		case 13: ndataType = CDFConstants.CDF_EPOCH16;  break;
		case 14: ndataType = CDFConstants.CDF_TIME_TT2000;  break;
		case 15: ndataType = CDFConstants.CDF_CHAR;   break;
		case 16: ndataType = CDFConstants.CDF_UCHAR;  break;
		    // keep compiler happy
		default: ndataType = CDFConstants.CDF_BYTE;   break;
		}
		
		String dataString = ((String) tfva.get()).trim();
		String data = dataString;
		long numElems;
		if (ndataType == CDF_CHAR || ndataType == CDF_UCHAR) {
		  if (data.charAt(0) == '"')
		    data= data.substring(1, data.length()-1);
		  numElems = data.length();
		} else {
                  StringTokenizer st = new StringTokenizer(data, ",");
		  numElems = st.countTokens();
		}
		if (ndataType != dataType && dataString.equals(outData)) {
		  // only data type is changed
		  boolean equ = CDFE_CDFToolUtils.equivalentDataTypes(dataType, ndataType);
	          if (!equ) {
		    Toolkit.getDefaultToolkit().beep();
	            JOptionPane.showMessageDialog(myFrame,
                                            "Data Type Error:\n"+
                                            "Existing and New Data Type not Equivalent",
                                            "MYEditor: CDFException",
                                            JOptionPane.ERROR_MESSAGE);
		    reset();
		  } else {
		    try {
		      entry.updateDataSpec(ndataType, numElems);
		      dataType = ndataType;
		    } catch (CDFException ex) {
		      Toolkit.getDefaultToolkit().beep();
                      JOptionPane.showMessageDialog(myFrame,
                                                    CDFException.getStatusMsg(ex.getCurrentStatus()),
                                                    "MYEditor: Error",
                                                    JOptionPane.ERROR_MESSAGE);
		      cbdt.set(CDFUtils.getStringDataType(dataType));
		    }
                  }
		} else {
		  // only value or both value and data type are changed 
		  try {
		    entry.putData(ndataType,
		   	          CDFE_CDFToolUtils.parseContents(data, ndataType));
		
		    dataType = ndataType;
		    tfne.set(""+numElems);
		  } catch (CDFException ex) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myFrame,
                                                  CDFException.getStatusMsg(ex.getCurrentStatus()),
                                                  "MYEditor: Error",
                                                  JOptionPane.ERROR_MESSAGE);
                  } catch (NumberFormatException ex) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myFrame,
                                                  ex.toString(),
                                                  "MYEditor: CDFException",
                                                  JOptionPane.ERROR_MESSAGE);
		  }
		}

		((JButton)source).setEnabled(true);
	    }
	}
    }

    private void reset() {

        if (scope == GLOBAL_SCOPE) {
          tfen.set(""+(entry.getID()+1));
        } else {
          if (entry != null)
            try {
              tfen.set(myPanel.getMyEditor().theCDF.getVariable(entry.getID()).getName());
            } catch (CDFException e) { System.err.println("Error: entry"+entry);}
          else {
            JList jl = (JList)myPanel.controls.get("listOfEntries");
            tfen.set((String)jl.getSelectedValue());
          }
        }

        cbdt.set(CDFUtils.getStringDataType(entry));
        tfne.set(""+numElements);
// Modifications: SMF
//        try {
          if (dataType == CDF_CHAR || dataType == CDF_UCHAR)
            tfva.set("\"" + CDFUtils.getStringData(entry.getData(), ", ") +
                           "\"");
          else {
	    int ii;
	    if (dataType == CDF_EPOCH) ii = 1;
	    else if (dataType == CDF_EPOCH16) ii = 2;
	    else ii = 0;
            tfva.set(CDFUtils.getStringData(entry.getData(), ", ", ii));
	  }
// Modifications: SMF
//        } catch (CDFException ex) {}
	outData = (String) tfva.get();
        // Deprecated - tfva.requestDefaultFocus();
        tfva.requestFocus();
	tfen.setEnabled(true);
        tfen.setEditable(false);
	tfne.setEditable(false);
    }

/** Action for textfield for entry data value
 */

    class ActionValue implements ActionListener {
        public void actionPerformed(ActionEvent e) {
           jtfva = (JTextField) e.getSource();
	   if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
	     if (((String)tfva.get()).charAt(0) == '"') 
	       tfne.set(""+(((String)tfva.get()).length()-2));
	     else
	       tfne.set(""+((String)tfva.get()).length());
	   } else {
	     String tmp = (String)tfva.get();
	     StringTokenizer st = new StringTokenizer(tmp);
	     tfne.set(""+st.countTokens());
	   }
        }
    }

}
