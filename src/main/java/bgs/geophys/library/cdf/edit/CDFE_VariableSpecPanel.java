package bgs.geophys.library.cdf.edit;

//$Id: CDFE_VariableSpecPanel.java,v 1.2 2015/07/13 14:24:52 liu Exp $
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
 * A panel to display a variable's specifications
 */
public class CDFE_VariableSpecPanel extends CDFE_JLabeledPanel implements CDFConstants {
    //private SpecChangeListener scl;

    private CDFE_JLabeledTF datatype, recvary, ndim, maxRecN, maxRecA, compression;
    private CDFE_JLabeledTF sparse, blocking, pad;
    private CDFE_JLabeledPanel sizePanel;  // Panel holding sizes
    private CDFE_WholeNumberField [] sizes;   // dimsizes
    private Font padCurrFont;
    private Font numRecWtCurrFont, numRecAlCurrFont;

    static final long serialVersionUID = 1L;

    public CDFE_VariableSpecPanel() {
//	super("Variable Specifications", BoxLayout.X_AXIS);
	super("Variable Specifications", new GridLayout(2, 5));
	setSize(670, 70);
	
	datatype = new CDFE_JLabeledTF("Data Type", 12); 
	datatype.textField.setEditable(false);
	datatype.textField.setOpaque(false);
	datatype.textField.setHorizontalAlignment(JTextField.LEFT);
	datatype.textField.setBorder(new EmptyBorder(0,0,0,0));

	recvary  = new CDFE_JLabeledTF("Variances", 4);
	recvary.textField.setEditable(false);
	recvary.textField.setOpaque(false);
	recvary.textField.setHorizontalAlignment(JTextField.LEFT);
	recvary.textField.setBorder(new EmptyBorder(0,0,0,0));

	ndim     = new CDFE_JLabeledTF("Dimensions", 3);
	ndim.textField.setEditable(false);
	ndim.textField.setOpaque(false);
	ndim.textField.setHorizontalAlignment(JTextField.LEFT);
	ndim.textField.setBorder(new EmptyBorder(0,0,0,0));


        maxRecN = new CDFE_JLabeledTF("Rec Written", 10);
        maxRecN.textField.setEditable(false);
        maxRecN.textField.setOpaque(false);
	maxRecN.textField.setHorizontalAlignment(JTextField.LEFT);
        maxRecN.textField.setBorder(new EmptyBorder(0,0,0,0));
	numRecWtCurrFont = maxRecN.getFont();

        maxRecA = new CDFE_JLabeledTF("Rec Allocated", 10);
        maxRecA.textField.setEditable(false);
        maxRecA.textField.setOpaque(false);
        maxRecA.textField.setHorizontalAlignment(JTextField.LEFT);
        maxRecA.textField.setBorder(new EmptyBorder(0,0,0,0));
	numRecAlCurrFont = maxRecA.getFont();

        blocking = new CDFE_JLabeledTF("Blocking", 7);
        blocking.textField.setEditable(false);
        blocking.textField.setOpaque(false);
	blocking.textField.setHorizontalAlignment(JTextField.LEFT);
        blocking.textField.setBorder(new EmptyBorder(0,0,0,0));

        pad = new CDFE_JLabeledTF("Pad Value", 7);
        pad.textField.setEditable(false);
        pad.textField.setOpaque(false);
	pad.textField.setHorizontalAlignment(JTextField.LEFT);
        pad.textField.setBorder(new EmptyBorder(0,0,0,0));
	padCurrFont = pad.getFont();

        sparse = new CDFE_JLabeledTF("Rec Sparseness", 7);
        sparse.textField.setEditable(false);        
        sparse.textField.setOpaque(false);
	sparse.textField.setHorizontalAlignment(JTextField.LEFT);
        sparse.textField.setBorder(new EmptyBorder(0,0,0,0));        
        
        compression  = new CDFE_JLabeledTF("Compression", 10);
        compression.textField.setEditable(false);
        compression.textField.setOpaque(false);
	compression.textField.setHorizontalAlignment(JTextField.LEFT);
        compression.textField.setBorder(new EmptyBorder(0,0,0,0));

        add(datatype); 
        add(recvary); 
        add(ndim); 
        add(pad); 
        add(blocking); 
        add(maxRecN); 
	add(maxRecA);
        add(sparse); 
        add(compression);

    }
	
    ////////////////////////////////
    //                            //
    //            reset           //
    //                            //
    ////////////////////////////////

    public  void reset() {
	datatype.set("");
	recvary.set("");
	ndim.set("");
	maxRecN.set("");
	maxRecA.set("");
	compression.set("");
        blocking.set("");
        sparse.set("");
        pad.set("");

    }

    ////////////////////////////////
    //                            //
    //             set            //
    //                            //
    ////////////////////////////////

    public  void set(Variable var) {

	setBorder(new TitledBorder(new EtchedBorder(), "Variable: "+var.getName()));

	long writtenRec, writtenRecX;
	try {
	  writtenRecX = var.getMaxWrittenRecord() + 1;
	} catch (CDFException ex) {
	  writtenRecX = 0;
	}
        try {
          writtenRec = var.getNumWrittenRecords();
        } catch (CDFException ex) {
          writtenRec = 0;
        }
	maxRecN.set("#: " + writtenRec + " /Max #: " + writtenRecX);
        if (((String)maxRecN.get()).length() >= 11)
          maxRecN.textField.setFont(new Font(numRecWtCurrFont.getName(),
                                             numRecWtCurrFont.getStyle(),
                                             numRecWtCurrFont.getSize()-1));
        else
          maxRecN.textField.setFont(numRecWtCurrFont);

	String tmpStr = CDFUtils.getStringDataType(var);
	tmpStr = tmpStr + "/" + var.getNumElements();
	datatype.set(tmpStr);
	StringBuffer tmp = new StringBuffer();
	tmp.append(var.getRecVariance()? "T/" : "F/");
	long numDims = var.getNumDims();

	if (numDims != 0) {
	  long[] variances = var.getDimVariances();
	  for (int i = 0; i < (int) numDims; i++) 
	    tmp.append(variances[i] == 0? "F" : "T");
	}  
	recvary.set(tmp.toString());

	StringBuffer sNumDims = new StringBuffer();
	sNumDims.append(numDims + ":");

	sNumDims.append("[");
	if (numDims != 0) {
	    long [] dimSizes = var.getDimSizes();
	    for (int i = 0; i < (int)numDims; i++) {
		//		sizes[i].setText(""+dimSizes[i]);
		sNumDims.append(dimSizes[i]+"");
		if (i!=(int)numDims-1) sNumDims.append(",");
	    }
	}
	sNumDims.append("]");
	ndim.set(sNumDims.toString());
	try {
	  String comp = var.getCompression();
	  compression.set(comp);
	} catch (CDFException ex) {
	  compression.set("None");
	}  

	Object padValue = null;
	try {
	  if (var.checkPadValueExistence()) 
	    padValue = var.getPadValue();
	} catch (CDFException ex) {}
	long dataType = var.getDataType();
	String padString;
        if (padValue == null)
          padString = "";
        else {
          if (dataType != CDF_EPOCH && dataType != CDF_EPOCH16 &&
              dataType != CDF_TIME_TT2000) {
            padString = padValue.toString();
	    if (dataType == CDF_CHAR || dataType == CDF_UCHAR)
		pad.set("\""+padString+"\"");
	    else
		pad.set(padString);
            if (((String)pad.get()).length() >= 15) 
              pad.textField.setFont(new Font(padCurrFont.getName(),
                                             padCurrFont.getStyle(),
                                             padCurrFont.getSize()-3));
            else
              pad.textField.setFont(padCurrFont);
          } else {
	    if (dataType == CDF_EPOCH) {
              padString = Epoch.encode(((Double)padValue).doubleValue());
	      pad.set(padString);
              pad.textField.setFont(new Font(padCurrFont.getName(), padCurrFont.getStyle(), 
					     padCurrFont.getSize()-3));
	    } else if (dataType == CDF_EPOCH16) {
              padString = Epoch16.encode(((double[])padValue));
              pad.set(padString);
              pad.textField.setFont(new Font(padCurrFont.getName(), padCurrFont.getStyle(),
                                             padCurrFont.getSize()-5));
	    } else {
              padString = CDFTT2000.toUTCstring(((Long)padValue).longValue());
              pad.set(padString);
              pad.textField.setFont(new Font(padCurrFont.getName(), padCurrFont.getStyle(),
                                             padCurrFont.getSize()-3));
            }
	  }
        }

	long bf;
	try {
	  bf = var.getBlockingFactor();
	} catch (CDFException e) {
	  bf = 0;
	}
	blocking.set(""+bf);

	long spr = var.getSparseRecords();
	sparse.set(CDFUtils.getStringSparseRecord(spr));

        long allocRec, allocRecX;
        try {
          allocRec = var.getNumAllocatedRecords();
        } catch (CDFException ex) {
          allocRec = 0;
        }
        try {
          allocRecX = var.getMaxAllocatedRecord() + 1;
        } catch (CDFException ex) {
          allocRecX = 0;
        }
	maxRecA.set("#: " + allocRec + " /Max #: " + allocRecX);
        if (((String)maxRecA.get()).length() >= 11)
          maxRecA.textField.setFont(new Font(numRecAlCurrFont.getName(),
                                             numRecAlCurrFont.getStyle(),
                                             numRecAlCurrFont.getSize()-1));
        else
          maxRecA.textField.setFont(numRecAlCurrFont);

    }
}
