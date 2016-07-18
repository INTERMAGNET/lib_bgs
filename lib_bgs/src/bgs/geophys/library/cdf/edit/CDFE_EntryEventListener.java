package bgs.geophys.library.cdf.edit;

//$Id: CDFE_EntryEventListener.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.util.EventListener;

/**
 * Interface to handle EntryEvents
 */
public interface CDFE_EntryEventListener extends EventListener {

    void performEntryAction(CDFE_EntryEvent e);
}
