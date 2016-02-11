package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EntryEvent.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.AWTEvent;
import java.awt.Event;

import gsfc.nssdc.cdf.*;

/**
 * Event to handle a changes to entries.
 *
 * Currently only used by AttributeComboBox to handle entry creation 
 * events. However, the hooks are present to handle various entry events.
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 */
public class CDFE_EntryEvent extends AWTEvent {

    static final long serialVersionUID = 1L;

    public static final int CREATED = 
	RESERVED_ID_MAX + 10;

    public static final int DELETED = 
	RESERVED_ID_MAX + 20;

    public static final int NAME_CHANGE = 
	RESERVED_ID_MAX + 30;

    public static final int DATATYPE_CHANGE = 
	RESERVED_ID_MAX + 40;

    /**
     * The entry on which the change occured
     */
    private Entry _entry;
    
    /**
     * Get the value of _entry.
     * @return Value of _entry.
     */
    public Entry getEntry() {return _entry;}
    
    public CDFE_EntryEvent(Object source, Entry entry, int id) {
	super(source, id);
	this._entry = entry;
    }
}
