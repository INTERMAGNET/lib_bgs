package bgs.geophys.library.cdf.edit;

// $Id: CDFDialog.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
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
 * Present the user with a dialog box to allow editing CDF specification
 *
 * @author Mike Liu 
 *
 */
public class CDFDialog extends JDialog implements ActionListener, 
						  CDFConstants {
    static final long serialVersionUID = 1L;

    private String              name;
    private JButton             enter, cancel, end;
    private CDFE_JLabeledTF          jtfcl;
    private JTextField		tfcl;
    private CDFE_JLabeledCB          cbcp, cben, cbma, cbfo, cbcs;
    private JFrame              myFrame;
    private boolean enChanged = false,
		    maChanged = false,
		    foChanged = false,
                    csChanged = false;			
    private long compression, complevel, encoding, majority, format, checksum;
    private static final String UPDATE = "Update";
    private static final String END = "End";
    private static final String CANCEL = "Reset";

    private CDF cdf = null;

    /**
     * default constructor
     */
    public CDFDialog() {
	// needed to allow extending this class
    }

    public CDFDialog(JFrame frame, CDF cdf) {
	super(frame, true);
	this.myFrame = frame;
	setTitle("Edit Spec for CDF: "+cdf.toString());

	this.cdf = cdf;

        JPanel mp = new JPanel(new BorderLayout());
        mp.setBorder(new EmptyBorder(10,10,10,10));
        getContentPane().add(mp);

        // Only way to close is to use the buttons
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            }
        });

        setSize(450,400);
        setLocation(10, 20);
        setVisible(false);
 
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
        // Build the specifications panel
	JPanel osp = new JPanel(gbl);

	// Set the default contraints
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = new Insets(2,2,2,2);

        Vector encodingV = new Vector();
        encodingV.addElement("NETWORK");
        encodingV.addElement("SUN");
	encodingV.addElement("VAX");
	encodingV.addElement("DECSTATION");
	encodingV.addElement("SGi");
        encodingV.addElement("IBMPC");
        encodingV.addElement("IBMRS");
        encodingV.addElement("HOST");
        encodingV.addElement("PPC");
        encodingV.addElement("HP");
        encodingV.addElement("NeXT");
	encodingV.addElement("ALPHAOSF1");
        encodingV.addElement("ALPHAVMSg");
        encodingV.addElement("ALPHAVMSd");
        encodingV.addElement("ALPHAVMSi");

        cben = new CDFE_JLabeledCB("Encoding", encodingV, false, true);
	cben.addItemListener(new EncodingListener());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(cben, gbc);
        osp.add(cben);

        Vector majorityV = new Vector();
        majorityV.addElement("ROW");
        majorityV.addElement("COLUMN");
        cbma = new CDFE_JLabeledCB("Majority", majorityV, false, true);
	cbma.addItemListener(new MajorityListener());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(cbma, gbc);
        osp.add(cbma);

        Vector formatV = new Vector();
        formatV.addElement("SINGLE");
        formatV.addElement("MULTI");
	cbfo = new CDFE_JLabeledCB("Format", formatV, false, true);
	cbfo.addItemListener(new FormatListener());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(cbfo, gbc);
        osp.add(cbfo);

        Vector compressionV = new Vector();
        compressionV.addElement("None");
        compressionV.addElement("RLE");
        compressionV.addElement("HUFF");
        compressionV.addElement("AHUFF");
        compressionV.addElement("GZIP");

        tfcl = new JTextField(1);

        cbcp = new CDFE_JLabeledCB("Compression", compressionV, false);
        cbcp.addItemListener(new CompressionListener());
        gbc.gridwidth = 1;
        gbl.setConstraints(cbcp, gbc);
        osp.add(cbcp); 
        
        jtfcl = new CDFE_JLabeledTF("Level", tfcl, true);
            
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jtfcl, gbc);
        osp.add(jtfcl);
        
        Vector checksumV = new Vector();
        checksumV.addElement("NO");
        checksumV.addElement("MD5");
        cbcs = new CDFE_JLabeledCB("Checksum", checksumV, false, true);
        cbcs.addItemListener(new ChecksumListener());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(cbcs, gbc);
        osp.add(cbcs);
        
	mp.add(osp, BorderLayout.CENTER);
	createButtonPanel( mp );

	reset();
	pack();
	// show();
        setVisible (true);

    }

    private void createButtonPanel(JPanel jp) {
	JPanel bp = new JPanel();
	
	enter = new JButton(UPDATE);
	enter.addActionListener( this );
	enter.setToolTipText("Update the CDF specification");
	bp.add(enter);

        cancel = new JButton(CANCEL);
        cancel.addActionListener( this );
        cancel.setToolTipText("Reset fields back to their last values");
        bp.add(cancel);

        end = new JButton(END);
        end.addActionListener( this );
	end.setToolTipText("End editing");
        bp.add(end);

	jp.add(bp, BorderLayout.SOUTH);
    }

    /**
     * Make sure the change flag is set if a new ComboBox item is selected
     */

    /** Action for comboBox for compression
      */

    class CompressionListener implements ItemListener {
      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
	int ind = cbcp.getIndex();
	if (ind == 4) ind++;
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

    /** Action for comboBox for encoding
      */
    
    class EncodingListener implements ItemListener {

      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();        
        int ind = cben.getIndex() + 1;
	if (ind > PPC_ENCODING) ind++; 
	if (ind != encoding) enChanged = true;
      }
    }
    /** Action for comboBox for majority
      */
    
    class MajorityListener implements ItemListener {

      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();        
        if (cbma.getIndex() != (majority-1)) maChanged = true;
      }
    }

    /** Action for comboBox for format
      */
    
    class FormatListener implements ItemListener {

      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();        
        if (cbfo.getIndex() != (format-1)) csChanged = true;
      }
    }

    /** Action for comboBox for checksum
      */
    
    class ChecksumListener implements ItemListener {
      
      public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();        
        if (cbcs.getIndex() != checksum-1) csChanged = true;
      }
    }
    
    /**
     * Make sure the change flag is set if a new ComboBox item is selected
     */
    /**
     * Process button events.
     */
    public void actionPerformed( ActionEvent event ) {

	Object source = event.getSource();

	if (source instanceof JButton) {
	    String action = event.getActionCommand();
	    if (action.equals(CANCEL)) {              // Reset fields
		reset();
            } else if (action.equals(END)) {              // End the operation
                setVisible(false);
		enter.removeActionListener(this);
		cancel.removeActionListener(this);
		end.removeActionListener(this);
		dispose();
		System.gc();
	    } else if (action.equals(UPDATE)) {        // Update the CDF spec
	      ((JButton)source).setEnabled(false);
	      long tmp = -1;

	      long[] level = {0};
	      tmp = (long) cbcp.getIndex();
	      if (tmp == 4) tmp++;
	      try {
	  	  if (tmp != compression) { // compression changed
		    if (tmp == GZIP_COMPRESSION) {
		      level[0] = Long.parseLong((String)jtfcl.get());
		      if (level[0] > 0 && level[0] < 10) {
                        cdf.setCompression(tmp, level);
                        compression = tmp;
                        complevel = level[0];
		      } else {
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(myFrame,
			  "GZIP compression level: has to be between 1 and 9",
                                        "CDFEdit: CDFException",
                                        JOptionPane.ERROR_MESSAGE);
		      }
		    } else {
                      cdf.setCompression(tmp, level);
                      compression = tmp;
                      complevel = level[0];
		    }
		  } else { // same compression... But, same compression level if GZIP?
                    if (tmp == GZIP_COMPRESSION) {
                      level[0] = Long.parseLong((String)jtfcl.get());
		      if (level[0] != complevel) { // level chaned
                        if (level[0] > 0 && level[0] < 10) {
                          cdf.setCompression(tmp, level);
                          compression = tmp;
                          complevel = level[0];
                        } else {
			  Toolkit.getDefaultToolkit().beep(); 
                          JOptionPane.showMessageDialog(myFrame,
                                        "GZIP compression level: has to be between 1 and 9",
                                        "CDFEdit: CDFException",
                                        JOptionPane.ERROR_MESSAGE);
			}
		      }
		    }
		  }
	      } catch (CDFException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myFrame, 
					      CDFException.getStatusMsg(ex.getCurrentStatus()),
                                              "CDFEdit: Compression Error!",
                                              JOptionPane.ERROR_MESSAGE);
	      }

	      if (enChanged) {
		enChanged = false;
		tmp = (long) cben.getIndex() + 1;
		if (tmp > 9) tmp++;
		if (tmp != encoding) {
		  try { 
		    cdf.setEncoding(tmp);
		    encoding = tmp;
		  } catch (CDFException ex) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(myFrame, 
						  CDFException.getStatusMsg(ex.getCurrentStatus()),
						  "CDFEdit: Encoding Error!",
						  JOptionPane.ERROR_MESSAGE); 
		  }
		}
	      }

	      if (maChanged) {
		maChanged = false;
		tmp = (long) cbma.getIndex() + 1;
		if (tmp != majority) {
		  try {
		    cdf.setMajority(tmp);
		    majority = tmp;
                  } catch (CDFException ex) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myFrame, 
						  CDFException.getStatusMsg(ex.getCurrentStatus()),
                                                  "CDFEdit: Majority Error!",
                                                  JOptionPane.ERROR_MESSAGE);
		  }
                }    
	      }

	      if (foChanged) {
		foChanged = false;
		tmp = (long) cbfo.getIndex() + 1;
		if (tmp == MULTI_FILE) {
		  Toolkit.getDefaultToolkit().beep();
		  JOptionPane.showMessageDialog(myFrame, 
						"Multi_file format is not supported yet!",
						"CDFEdit: Format Error!",
						JOptionPane.WARNING_MESSAGE);
		} else {
		  try {
		    cdf.setFormat(tmp);
		    format = tmp;
		  } catch (CDFException ex) {
		    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myFrame, 
						  CDFException.getStatusMsg(ex.getCurrentStatus()),
                                                  "CDFEdit: Format Error!",
                                                  JOptionPane.ERROR_MESSAGE);
                  }
                }
	      }

              if (csChanged) {
                csChanged = false;
                tmp = (long) cbcs.getIndex();
                if (tmp != checksum) {
                  try {
                    cdf.setChecksum(tmp);
                    checksum = tmp;
                  } catch (CDFException ex) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myFrame,
                                                  CDFException.getStatusMsg(ex.getCurrentStatus()),
                                                  "CDFEdit: Checksum Error!",
                                                  JOptionPane.ERROR_MESSAGE);
                  }
                }
              }

	      ((JButton)source).setEnabled(true);
	    }
	} else if (source instanceof JTextField) {
          long tmp = (long) cbcp.getIndex();
          if (tmp > 3) tmp++;
          if (tmp == GZIP_COMPRESSION) {
            long level = Long.parseLong((String)jtfcl.get());
            if (level < 1 || level > 9) {
	      Toolkit.getDefaultToolkit().beep(); 
              JOptionPane.showMessageDialog(myFrame,
                                  "GZIP compression level: has to be between 1 and 9",
                                  "CDFEdit: CDFException",
                                  JOptionPane.ERROR_MESSAGE);
	    }
	  }
	}
    }

    public void reset() {
	// This will occur once the dialog is closed

	enChanged = false;
	maChanged = false;
	foChanged = false;
	csChanged = false;

        compression = cdf.getCompressionType();
        if (compression == GZIP_COMPRESSION) {
          long[] compparms = cdf.getCompressionParms();
          complevel = compparms[0];
        }
        encoding = cdf.getEncoding();
        format = cdf.getFormat();
        majority = cdf.getMajority();
	checksum = cdf.getChecksum();

        int ind = (int) compression;
        if (ind > 3) ind--; // skip 4 as GZIP is 5 but is 4 on combobox
        cbcp.setIndex(ind);
        if (compression == GZIP_COMPRESSION) {
          jtfcl.set(""+complevel);
          tfcl.setVisible(true);
          jtfcl.setEnabled(true);
	  jtfcl.setEditable(true);
        } else {
          tfcl.setVisible(true);
          jtfcl.setEnabled(true);
	  jtfcl.setEditable(false);
	  if (compression == NO_COMPRESSION)
	    jtfcl.set("");
	  else
	    jtfcl.set("0");
        }
        ind = (int) encoding - 1;
        if (ind > PPC_ENCODING) ind--;
        cben.setIndex(ind);

        ind = (int) majority - 1;
        cbma.setIndex(ind);

        ind = (int) format - 1;
        cbfo.setIndex(ind);

	cbcs.setIndex((int)checksum);
    }


}
