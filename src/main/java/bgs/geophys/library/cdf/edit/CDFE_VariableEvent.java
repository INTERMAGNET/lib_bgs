package bgs.geophys.library.cdf.edit;

//$Id: CDFE_VariableEvent.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.AWTEvent;
import java.awt.Event;

import gsfc.nssdc.cdf.*;

/**
 * Event to handle a changes to variables.
 *
 * Currently only used by AttributeComboBox to handle variable creation 
 * events. However, the hooks are present to handle various variable events.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_VariableEvent extends AWTEvent {

    static final long serialVersionUID = 1L;

    public static final int CREATED = 
	RESERVED_ID_MAX + 10;

    public static final int DELETED = 
	RESERVED_ID_MAX + 20;

    public static final int NAME_CHANGE = 
	RESERVED_ID_MAX + 30;

    public static final int DATATYPE_CHANGE = 
	RESERVED_ID_MAX + 40;

    public static final int NDIM_CHANGE = 
	RESERVED_ID_MAX + 50;

    public static final int NELEMENTS_CHANGE = 
	RESERVED_ID_MAX + 60;

    public static final int DIMSIZE_CHANGE = 
	RESERVED_ID_MAX + 70;

    public static final int RECVARY_CHANGE = 
	RESERVED_ID_MAX + 80;

    public static final int DIMVARY_CHANGE = 
	RESERVED_ID_MAX + 90;


    /**
     * The variable on which the change occured
     */
    private Variable _var;
    
    /**
     * Get the value of _var.
     * @return Value of _var.
     */
    public Variable getVariable() {return _var;}
    
    public CDFE_VariableEvent(Object source, Variable var, int id) {
	super(source, id);
	this._var = var;
    }
}
