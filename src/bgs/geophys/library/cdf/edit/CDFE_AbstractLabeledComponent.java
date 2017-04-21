package bgs.geophys.library.cdf.edit;

// $Id: CDFE_AbstractLabeledComponent.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Provides the default implementations for all labeled components.
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author Phil Williams, QSS Group Inc</a>
 * @version $Revision: 1.1.1.1 $
 *
 */
public abstract class CDFE_AbstractLabeledComponent extends JPanel 
    implements CDFE_GenericComponentInterface {

    /**
     * The primary label
     */
    public JLabel label;

    /**
     * The secondary label, if desired.
     */
    public JLabel label2;

    /**
     * Color to use for the label when the component is enabled.
     */
    protected Color enabledColor;

    /**
     * Color to use for the label when the component is disabled.
     */
    protected Color disabledColor;

    /**
     * Should wrap this components set method.
     */
    public abstract void   addItem(Object obj);
    
    /**
     * Wraps this components reset method.
     */
    public void removeAllItems() {
	reset();
    }

    /**
     * Changes the enabled status of the component.  Should also take care
     * of changing the label color if desired.
     *
     * @param enabled the enabled status.
     */
    public abstract void setEnabled(boolean enabled);

    /**
     * Specifies the color to use when this component is enabled.
     *
     * @param c the color.
     */
    public void setEnabledColor(Color c) {
	this.enabledColor = c;
    }

    /**
     * What color is being used when this component is enabled.
     *
     * @returns the enabledColor
     */
    public Color getEnabledColor() {
	return this.enabledColor;
    }

    /**
     * Specifies the color to use when this component is disabled.
     *
     * @param c the color.
     */
    public void setDisabledColor(Color c) {
	this.disabledColor = c;
    }

    /**
     * What color is being used when this component is disabled.
     *
     * @returns the disabledColor
     */
    public Color getDisabledColor() {
	return this.disabledColor;
    }

}
