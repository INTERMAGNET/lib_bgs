package bgs.geophys.library.cdf.edit;

//
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

// usage:
//
// JTextField tf = new JTextField(20);
// CDFE_CutAndPaste.register(tf);
//

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class CDFE_CutAndPaste {
    
    public static void register(final JTextComponent tc) {
       tc.addMouseListener
		(new MouseAdapter()
			{ public void mousePressed(MouseEvent e) {
			       if (SwingUtilities.isRightMouseButton(e)) {
			            JPopupMenu menu = new JPopupMenu();
				    JMenuItem item;
				    item = new JMenuItem("Cut");
				    item.addActionListener (
				       new ActionListener() {
					    public void actionPerformed(ActionEvent ex) { tc.cut(); }
						}
					);
				    if (tc.getSelectedText() == null)
					item.setEnabled(false);
				    menu.add(item);
				    item = new JMenuItem("Copy");
				    item.addActionListener (new ActionListener() {
					    public void actionPerformed(ActionEvent ex) { tc.copy(); }
						}
						);
				    if (tc.getSelectedText() == null)
					item.setEnabled(false);
				    menu.add(item);
				    item = new JMenuItem("Paste");
				    item.addActionListener (new ActionListener() {
					   public void actionPerformed(ActionEvent ex) { tc.paste(); }
						}
						);
				    menu.add(item);
				    menu.show(tc, e.getX(), e.getY());
				    }
				}
			    
			}
			);
    }
    
}
