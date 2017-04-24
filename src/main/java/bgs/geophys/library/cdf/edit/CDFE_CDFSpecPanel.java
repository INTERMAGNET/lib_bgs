package bgs.geophys.library.cdf.edit;

//$Id: CDFE_CDFSpecPanel.java,v 1.6 2015/11/16 19:26:10 liu Exp $
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
 * A panel to display a CDF's specifications
 */
public class CDFE_CDFSpecPanel extends CDFE_JLabeledPanel implements CDFConstants
{
    //private SpecChangeListener scl;

    private CDFE_JLabeledTF
	version, format, encoding, majority, numzVars, numAttrs, maxRecN, 
        compression, checksum, lastUpdated;
    private Font numAttrCurrFont;

    static final long serialVersionUID = 1L;

    public CDFE_CDFSpecPanel() {
	super("CDF Specifications", BoxLayout.X_AXIS);

	version     = new CDFE_JLabeledTF("Version", 7);
	version.textField.setEditable(false);
	version.textField.setOpaque(false);
	version.textField.setBorder(new EmptyBorder(0,0,0,0));

	format = new CDFE_JLabeledTF("Format", 7); 
	format.textField.setEditable(false);
	format.textField.setOpaque(false);
	format.textField.setBorder(new EmptyBorder(0,0,0,0));

	encoding  = new CDFE_JLabeledTF("Encoding", 7);
	encoding.textField.setEditable(false);
	encoding.textField.setOpaque(false);
	encoding.textField.setBorder(new EmptyBorder(0,0,0,0));

	majority     = new CDFE_JLabeledTF("Majority", 7);
	majority.textField.setEditable(false);
	majority.textField.setOpaque(false);
	majority.textField.setBorder(new EmptyBorder(0,0,0,0));

        numzVars     = new CDFE_JLabeledTF("NumzVars", 5);
        numzVars.textField.setEditable(false);
        numzVars.textField.setOpaque(false);
        numzVars.textField.setBorder(new EmptyBorder(0,0,0,0));

        numAttrs = new CDFE_JLabeledTF("NumAttrs", 5);
        numAttrs.textField.setEditable(false);
        numAttrs.textField.setOpaque(false);
        numAttrs.textField.setBorder(new EmptyBorder(0,0,0,0));
	numAttrCurrFont = numAttrs.getFont();

        maxRecN = new CDFE_JLabeledTF("Max Rec #", 7);
        maxRecN.textField.setEditable(false);
        maxRecN.textField.setOpaque(false);
        maxRecN.textField.setBorder(new EmptyBorder(0,0,0,0));

        compression  = new CDFE_JLabeledTF("Compression", 10);
        compression.textField.setEditable(false);
        compression.textField.setOpaque(false);
        compression.textField.setBorder(new EmptyBorder(0,0,0,0));

        checksum  = new CDFE_JLabeledTF("Checksum", 5);
        checksum.textField.setEditable(false);
        checksum.textField.setOpaque(false);
        checksum.textField.setBorder(new EmptyBorder(0,0,0,0));
        
        lastUpdated  = new CDFE_JLabeledTF("LeapTableUpdated", 8);
        lastUpdated.textField.setEditable(false);
        lastUpdated.textField.setOpaque(false);
        lastUpdated.textField.setBorder(new EmptyBorder(0,0,0,0));
        
	add(version); add(Box.createRigidArea(new Dimension(3,1)));
	add(format); add(Box.createRigidArea(new Dimension(3,1)));
	add(encoding); add(Box.createRigidArea(new Dimension(3,1)));
	add(majority); add(Box.createRigidArea(new Dimension(3,1)));
        add(numzVars); add(Box.createRigidArea(new Dimension(3,1)));
        add(numAttrs); add(Box.createRigidArea(new Dimension(3,1)));
        add(maxRecN); add(Box.createRigidArea(new Dimension(3,1)));
        add(compression); add(Box.createRigidArea(new Dimension(3,1)));
	add(checksum); add(Box.createRigidArea(new Dimension(3,1))); 
	add(lastUpdated); 

    }
	
    ////////////////////////////////
    //                            //
    //            reset           //
    //                            //
    ////////////////////////////////

    public  void reset() {
	version.set("");
	format.set("");
	encoding.set("");
	majority.set("");
        numzVars.set("");
        numAttrs.set("");
        maxRecN.set("");
        compression.set("");
	checksum.set("");
	lastUpdated.set("");

    }

    ////////////////////////////////
    //                            //
    //             set            //
    //                            //
    ////////////////////////////////

    public void set(CDF cdf) {

	try {
	  String cdfVersion = cdf.getVersion();
	  version.set(cdfVersion);
	  format.set(CDFUtils.getStringFormat(cdf));
	  encoding.set(CDFUtils.getStringEncoding(cdf));
          majority.set(CDFUtils.getStringMajority(cdf));
          numzVars.set("" + cdf.getNumZvars());
          numAttrs.set("" + cdf.getNumAttrs()+"("+cdf.getNumGattrs()+"g/"+
			cdf.getNumVattrs()+"v)");
	  if (((String)numAttrs.get()).length() >= 11) 
	    numAttrs.textField.setFont(new Font(numAttrCurrFont.getName(), 
						numAttrCurrFont.getStyle(),
						numAttrCurrFont.getSize()-1));
	  else
            numAttrs.textField.setFont(numAttrCurrFont);

	  int maxRec = CDFE_CDFToolUtils.getMaxRecNum(cdf);
          maxRecN.set("" + (maxRec+1));
          compression.set(cdf.getCompression());
	  checksum.set(CDFUtils.getStringChecksum(cdf));
// Modifications: SMF
//          long lastTT2000 = cdf.getLeapSecondLastUpdated();
//	  if (cdfVersion.compareTo("3.6.0") < 0 || lastTT2000 < 0)
	    lastUpdated.set("Not Set");
//	  else
//	    lastUpdated.set(""+lastTT2000);
	} catch (CDFException e) {
	  System.err.println("Error: " + e.toString()); 
	}

    }

    ////////////////////////////////
    //                            //
    //          setRec            //
    //                            //
    ////////////////////////////////

    public void setRec(int maxRec) {

      maxRecN.set("" + maxRec);

    }

}
