package bgs.geophys.library.cdf.edit;

// $Id: CDFE_GenericComponentInterface.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

/**
 * Used to put a generic interface on any extended Swing/AWT component.
 *
 *  <BR><BR><FONT SIZE=-1>
 *  1999, NASA/Goddard Space Flight Center
 *  This software may be used, copied, or redistributed as long as it is not
 *  sold or incorporated in any product meant for profit.  This copyright 
 *  notice must be reproduced on each copy made.  This routine is provided 
 *  as is without any express or implied warranties whatsoever.
 *  </FONT>
 *
 * @author Phil Williams
 * @version $Revision: 1.1.1.1 $
 *
 */
public interface CDFE_GenericComponentInterface {

    /**
     * A wrapper to get information back from a component
     */
    public abstract Object get();
    
    /**
     * A wrapper to set a component's information 
     */
    public abstract void set(Object obj);

    /**
     * Reset the component to a default state
     */
    public abstract void reset();
}
