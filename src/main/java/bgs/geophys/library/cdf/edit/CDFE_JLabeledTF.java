package bgs.geophys.library.cdf.edit;

// $Id: CDFE_JLabeledTF.java,v 1.1.1.1 2015/04/02 17:52:08 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * A JTextArea with a label.
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
 */
public class CDFE_JLabeledTF extends CDFE_AbstractLabeledComponent 
			implements ActionListener {

    static final long serialVersionUID = 1L;

    /**
     * The text field
     */
    public      JTextField      textField;

    /**
     * The text field's Document model
     */
    public      Document        document;

    // Implementation of Interface Routines
    public Object get() {
	return textField.getText();
    }

    public void set(Object str) {
	textField.setText("");
	textField.setText((String)str);
	textField.setCaretPosition(0);
    }

    public void reset() {
	textField.setText(null);
    }
    
    public void addItem(Object obj) {
	textField.setText(obj.toString());
    }

    public void setEnabled(boolean enabled) {
	if (enabled) {
	    textField.setEnabled(true);
	    label.setForeground(enabledColor);
	} else {
	    textField.setEnabled(false);
	    label.setForeground(disabledColor);
	}
    }

    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }

    public void selectAll() {
	textField.selectAll();
    }
    
    public void addActionListener(ActionListener l) {
	textField.addActionListener(l); 
    }

    public void removeActionListener(ActionListener l) {
	textField.removeActionListener(l); 
    }

    // Constructors

    /**
     * Create a panel with a label and a text field.
     *
     * @param str the label text
     * @param columns the number of columns in the text field
     * @param document the text field's document model
     */
    public CDFE_JLabeledTF (String str, int columns)  {
	this(str, columns, new PlainDocument(), true);
    }

    /**
     * Create a panel with a label and a text field.
     *
     * @param str the label text
     * @param columns the number of columns in the text field
     * @param document the text field's document model
     */
    public CDFE_JLabeledTF(String str, int columns, Document document) {
	this(str, columns, document, true);
    }
    
    /**
     * Create a panel with a label and a text field.
     *
     * @param str the label text
     * @param columns the number of columns in the text field
     * @param document the text field's document model
     * @param enabled the initial state of this component
     */
    public CDFE_JLabeledTF (String str, int columns, 
		       Document document, boolean enabled)  {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	this.document = document;

        label = new JLabel(str);
        textField = new JTextField(columns);
	textField.setDocument(document);
        textField.setAlignmentX(LEFT_ALIGNMENT);
//        CutAndPaste.register(textField);

	enabledColor  = Color.black;
	disabledColor = new Color(142, 142, 142);
	setEnabled(enabled);

        add(label);
        add(Box.createVerticalStrut(3));
        add(textField);
    }

    /**
     * Create a panel with a label and a text field.
     *
     * @param str the label text
     * @param columns the number of columns in the text field
     * @param document the text field's document model
     * @param enabled the initial state of this component
     * @param altEdit enabled an alternate editing method
     */
    public CDFE_JLabeledTF (String str, int columns, 
		       boolean enabled, boolean altEdit)  {
	this.document = new PlainDocument();
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	JPanel jp = new JPanel();
	jp.setLayout(new BoxLayout( jp, BoxLayout.X_AXIS ));
	jp.setAlignmentY(TOP_ALIGNMENT);

        label = new JLabel(str);

	JButton edit = new JButton("...");
	edit.addActionListener( this );

        textField = new JTextField(columns);
	textField.setDocument(document);
        textField.setAlignmentX(LEFT_ALIGNMENT);
//        CutAndPaste.register(textField);

	jp.add(textField);
	jp.add(edit);

        add(label);
        add(Box.createVerticalStrut(3));
        add(jp);

	enabledColor  = Color.black;
	disabledColor = new Color(142, 142, 142);
	setEnabled(enabled);
    }

    public CDFE_JLabeledTF (String str, JTextField textField, boolean enabled)  {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        label = new JLabel(str);
        this.textField = textField;
//        CutAndPaste.register(textField);

	enabledColor  = Color.black;
	disabledColor = new Color(142, 142, 142);
	setEnabled(enabled);

        add(label);
        add(Box.createVerticalStrut(3));
        add(textField);
    }

    /**
     * Sets the maximum length of text in the textfield.
     * This method is used if you the column number is different than
     * the maximum number of characters.
     */
    public void setMaxLength(int maximum) {

	textField.setDocument(new CDFE_LimitedPlainDocument(maximum));
    }

    public void setLabel(String sLabel) {
	label.setText(sLabel);
	label.revalidate();
	if (label.isVisible()) 
	    label.repaint();
    }

    public void actionPerformed(ActionEvent event) {
	String message = "Enter "+label.getText()+" value:";
	String oldValue = textField.getText();
	JOptionPane editor = new JOptionPane(message,
					     JOptionPane.QUESTION_MESSAGE,
					     JOptionPane.OK_CANCEL_OPTION);
	editor.setWantsInput( true );
	editor.setInputValue(oldValue);
	editor.setMessage(message);
	JDialog ed = editor.createDialog(null, "Edit Value");
	// Deprecated - ed.show();
	ed.setVisible(true);
	String newValue = (String)editor.getInputValue();
	if (newValue != null) {
	    textField.setText(newValue);
	    textField.setCaretPosition(0);
	}
    }
	
}  // End CDFE_JLabeledTF

