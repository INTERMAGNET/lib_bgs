/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author jex
 */
public class BLVInformationBox {
    /** Creates new form BGGMDialogBox */
    public static void BLVInformationBox(JFrame parent, String message, int exit_code) 
    {
        JOptionPane.showMessageDialog(parent,
                                      message,
                                      "Error Message",
                                      javax.swing.JOptionPane.ERROR_MESSAGE);
        if (exit_code >= 0) System.exit (exit_code);
    }
}
