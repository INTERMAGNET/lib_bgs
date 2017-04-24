package bgs.geophys.library.cdf.edit;

/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

/**
 * @(#) ScrollBarSynchronizer.java	1.0 99/08/28	
 *
 * This code is designed for JDK1.2
 * Use tab spacing 4. Follow JavaDoc convention while coding.
 * Mail any suggestions or bugs to unicman@iname.com
 */
import	javax.swing.JScrollBar;					//**JDK12**//
import	javax.swing.BoundedRangeModel;				//**JDK12**//
import	javax.swing.event.ChangeListener;			//**JDK12**//
import	javax.swing.event.ChangeEvent;				//**JDK12**//

/**
 * This class is used for synchronizing two JScrollBars.
 * <p>
 * If (maximum - minimum) of the scroll bars don't match, the other scrollbar
 * will be adjusted proportionally.
 *
 * @Author	UnicMan
 * @version	1.0 99/08/28
 */
public class CDFE_ScrollBarSynchronizer implements	ChangeListener {
	private		JScrollBar		msbScrollBar;

	/**
	 * Constructor.
	 *
	 * @param	destScroll	ScrollBar to synchronize
	 */
	public CDFE_ScrollBarSynchronizer( JScrollBar destScroll ) {
		msbScrollBar = destScroll;
	}

	public void stateChanged( ChangeEvent e ) {
		BoundedRangeModel	sourceScroll = (BoundedRangeModel)e.getSource();
		
		int	iSMin	= sourceScroll.getMinimum();
		int 	iSMax	= sourceScroll.getMaximum();
		int	iSDiff	= iSMax - iSMin;
		int	iSVal	= sourceScroll.getValue();

		int	iDMin	= msbScrollBar.getMinimum();
		int 	iDMax	= msbScrollBar.getMaximum()+120; // compensate for title
		int	iDDiff	= iDMax - iDMin;
		int	iDVal;

		if( 	iSDiff == iDDiff-120 || iSDiff == 0)
			iDVal	= iSVal;
		else
			iDVal	= (iDDiff * iSVal) / iSDiff;
		msbScrollBar.setValue( iDMin + iDVal );
	}
}
