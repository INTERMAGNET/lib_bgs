package bgs.geophys.library.cdf.edit;

// $Id: CDFE_JLabeledPanel.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A JPanel with a label. 
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author  Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_JLabeledPanel extends JPanel {

    private TitledBorder tb;
    private LayoutManager lm;
    private Border primaryBorder;
    private Border secondaryBorder;
    private String label;
    private String label2;
    private Color enabledColor = Color.black;
    private Color disabledColor = new Color(142, 142, 142);

    static final long serialVersionUID = 1L;

    /**
     * Construct a JLabeledPanel with the given label.
     *
     * A BoxLayout (Y_AXIS) is set as the default LayoutManager.  The 
     * primary border is an EtchedBorder.
     *
     * @param label The primary label
     */
    public CDFE_JLabeledPanel(String label) {
	this.label = label;
	setAlignmentX(SwingConstants.WEST);

	lm            = new BoxLayout(this, BoxLayout.Y_AXIS);
	primaryBorder = new EtchedBorder();
	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);
	setLayout(lm);
	setBorder(tb);
    }

    /**
     * Construct a JLabeledPanel with the given label.
     *
     * The primary border is an EtchedBorder.
     *
     * @param label The primary label
     * @param lm A LayoutManager to apply to the panel.
     */
    public CDFE_JLabeledPanel(String label, LayoutManager lm) {
	this.label = label;
	this.lm = lm;

	primaryBorder = new EtchedBorder();
	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);
	setLayout(lm);
	setBorder(tb);
    }

    /**
     * Construct a JLabeledPanel with the given label.
     *
     * A BoxLayout (Y_AXIS) is set as the default LayoutManager.
     *
     * @param label The primary label
     * @param primaryBorder an alternate border to use for the TitledBorder
     */
    public CDFE_JLabeledPanel(String label, Border primaryBorder) {
	this.label = label;
	this.primaryBorder = primaryBorder;

	lm            = new BoxLayout(this, BoxLayout.Y_AXIS);
	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);

	setLayout(lm);
	setBorder(tb);
    }
    
    /**
     * Construct a JLabeledPanel with the given label.
     *
     * @param label The primary label
     * @param lm A LayoutManager to apply to the panel.
     * @param primaryBorder an alternate border to use for the TitledBorder
     */
    public CDFE_JLabeledPanel(String label, 
			 Border primaryBorder, LayoutManager lm) {
	this.label = label;
	this.primaryBorder = primaryBorder;
	this.lm = lm;

	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);

	setLayout(lm);
	setBorder(tb);
    }

    /**
     * Construct a JLabeledPanel with the given label at the given justification
     * and position.
     *
     * @param label The primary label
     * @param lm A LayoutManager to apply to the panel.
     * @param justification Left/center/right justification for the title at the border.
     * @param position Position of the title at the border.
     * @param primaryBorder an alternate border to use for the TitledBorder
     */
    public CDFE_JLabeledPanel(String label, Border primaryBorder, int justification, 
			 int position, LayoutManager lm) {
        this.label = label;
        this.primaryBorder = primaryBorder;
        this.lm = lm;

        tb            = new TitledBorder(primaryBorder, label, justification, position);
        tb.setTitleColor(enabledColor);

        setLayout(lm);
        setBorder(tb);
    }

    /**
     * Construct a JLabeledPanel with the given label.
     *
     * A BoxLayout with the specified axis is set as the LayoutManager.  The 
     * primary border is an EtchedBorder.
     *
     * @param label The primary label
     * @param axis BoxLayout.Y_AXIS or BoxLayout.X_AXIS
     */
    public CDFE_JLabeledPanel(String label, int axis) {
	this.label = label;

	lm            = new BoxLayout(this, axis);
	primaryBorder = new EtchedBorder();
	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);

	setLayout(lm);
	setBorder(tb);
    }

    /**
     * Construct a JLabeledPanel with the given label.
     *
     * A BoxLayout with the specified axis is set as the LayoutManager.
     *
     * @param label The primary label
     * @param axis BoxLayout.Y_AXIS or BoxLayout.X_AXIS
     * @param primaryBorder an alternate border to use for the TitledBorder
     */
    public CDFE_JLabeledPanel(String label, int axis, Border primaryBorder) {
	this.label = label;
	this.primaryBorder = primaryBorder;

	lm            = new BoxLayout(this, axis);
	tb            = new TitledBorder(primaryBorder, label);
	tb.setTitleColor(enabledColor);

	setLayout(lm);
	setBorder(tb);
    }

    /**
     * Enables or disables all the components contained in this panel
     *
     * @param enabled the new state of this panel.
     */
    public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	int nComponents = getComponentCount();
	for (int i=0; i< nComponents; i++)
	    getComponent(i).setEnabled(enabled);

	if (enabled)
	    tb.setTitleColor(enabledColor);
	else
	    tb.setTitleColor(disabledColor);
    }
	    
    /**
     * Changes the color of the title.
     *
     * @param c the new title color
     */
    public void setTitleColor(Color c) {
	tb.setTitleColor(c);
    }

}

    
