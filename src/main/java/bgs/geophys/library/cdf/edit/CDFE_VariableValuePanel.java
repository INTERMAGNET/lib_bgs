package bgs.geophys.library.cdf.edit;

//$Id: CDFE_VariableValuePanel.java,v 1.3 2015/07/13 16:32:50 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

// Swing Imports
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.*;

// Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;

// CDF Imports
import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.*;

/**
 * A panel to display/edit a variable's data values 
 */
public class CDFE_VariableValuePanel extends CDFE_JLabeledPanel implements CDFConstants, 
                                                  ActionListener,
						  KeyListener, FocusListener, 
						  DocumentListener,
						  CDFE_VariableEventListener {
    private CDF cdf;
    private CDFE_MYEditor myEditor;
    private CDFE_VariablePanel myVP;
    private Variable var = null, oldVar = null;
    private CDFE_VariableValuePanel values;
    private boolean variance;
    private long nDims;
    private long[] sizes;
    private long dataType;
    private long[] dimVars;
    private long[] dimSizes;
    private Object padValue;
    private long sparseRec;
    private long maxRec;
    private long numElements;
    private long status;
    private String Virtual = "<Virtual>";
    private boolean virtual;
    private Object datavalue;
    private String output;
    private Object focusedObj = null;

    private CDFE_JLabeledTF jtfRec, jtfVal;
    private JTextField oldRec;
    private CDFE_JLabeledPanel   jpIndex;
    private JTextField[] jtfIndex;
    private JTextField[] oldIndex;
    private JButton update;
    private JButton cancel;
    private static final String UPDATE = "Update";
    private static final String CANCEL = "Cancel";
    private long[] dimIndex;
    private long recNum;
    private CDFE_JLabeledPanel jp;
    private String format;

    static final long serialVersionUID = 1L;

    public CDFE_VariableValuePanel(CDFE_VariablePanel myVP) {
        super("Edit Data", BorderFactory.createEtchedBorder(),
              TitledBorder.CENTER, TitledBorder.TOP,
              new FlowLayout());
        setSize(720, 20);

	values = this;
	this.myVP = myVP;
	myEditor = myVP.getMyEditor();
	cdf = myEditor.theCDF;

	jtfRec = new CDFE_JLabeledTF("Record", 10);
	jtfRec.textField.setHorizontalAlignment(JTextField.RIGHT);
	oldRec = new JTextField();
	add(jtfRec);
	jpIndex = new CDFE_JLabeledPanel("Index", BorderFactory.createEmptyBorder(),
				    TitledBorder.CENTER, TitledBorder.TOP,
				    new FlowLayout());
	JTextField jtfIndex0 = new JTextField("1");
	JTextField jtfIndex1 = new JTextField("1");
	JTextField jtfIndex2 = new JTextField("1");
	jpIndex.add(jtfIndex0);
	jpIndex.add(jtfIndex1);
	jpIndex.add(jtfIndex2);
	jtfIndex0.setHorizontalAlignment(JTextField.RIGHT);
	jtfIndex1.setHorizontalAlignment(JTextField.RIGHT);
	jtfIndex2.setHorizontalAlignment(JTextField.RIGHT);
	add(jpIndex);

	jtfVal = new CDFE_JLabeledTF("Data Value", 32);
	jtfVal.textField.getDocument().addDocumentListener(this);
	jtfVal.textField.addFocusListener(this);
	add(jtfVal);

	add(Box.createRigidArea(new Dimension(18, 8)));

	JPanel jjj = new JPanel();
        update = new JButton(UPDATE) {
          static final long serialVersionUID = 1L;
          // deprecated - public boolean isFocusTraversable() { return false; }
          public boolean isFocusable() { return false; }
        };
	update.setToolTipText("Click to apply change to the Variable's value");
        update.addActionListener( this );
	update.setEnabled(false);
	add(update);

        cancel = new JButton(CANCEL) {
          public boolean isFocusTraversable() { return false; }
        };
        cancel.setToolTipText("Click to cancel change to the Variable's value");
        cancel.addActionListener( this );
        cancel.setEnabled(false);
	add(cancel);

	values.setVisible(false);	
    }

    public void set(Variable var) {

	values.setVisible(true);

	this.var = var;

	if (oldVar != null && oldVar.getRecVariance()) {
	  jtfRec.removeActionListener(this);
          jtfRec.textField.removeKeyListener(this);
	  jtfRec.textField.removeFocusListener(this);
	}
	oldVar = var;

        variance = var.getRecVariance();
        nDims = var.getNumDims();
        dataType = var.getDataType();
	numElements = var.getNumElements();
        dimVars = var.getDimVariances();
        dimSizes = var.getDimSizes();
        padValue = null;
	try {
	  if (var.checkPadValueExistence()) 
	    padValue = var.getPadValue();
	} catch (CDFException ex) {}

        format = CDFE_CDFToolUtils.getCFormat(var);

        sparseRec = var.getSparseRecords();

        try {
          maxRec = var.getMaxWrittenRecord();
        } catch (CDFException ex) {
          maxRec = -1;
        }

        if (variance) {
	  jtfRec.setEnabled(true);
          jtfRec.setEditable(true);
          jtfRec.addActionListener(this);
          jtfRec.textField.addKeyListener(this);
          jtfRec.textField.addFocusListener(this);
	  jtfRec.textField.setRequestFocusEnabled(true);
        } else {
          jtfRec.setEditable(false);
	  jtfRec.setEnabled(true); 
	  jtfRec.textField.setRequestFocusEnabled(false);
	}

	jpIndex.removeAll();
        if (nDims > 0) {
            jtfIndex = new JTextField[(int) nDims];
	    oldIndex = new JTextField[(int) nDims];
            dimIndex = new long[(int)nDims];
            for (int i = 0; i < (int) nDims; i++) {
		final long dimV = dimVars[i];
		jtfIndex[i] = new JTextField(4) {
		  public boolean isFocusTraversable() { 
		    if (dimV == VARY) return true;
		    else return false; 
		  }
		};
		jtfIndex[i].setHorizontalAlignment(JTextField.RIGHT);
		oldIndex[i] = new JTextField();
		if (dimVars[i] == VARY) {
                  jtfIndex[i].addActionListener(this);
                  jtfIndex[i].addKeyListener(this);
                  jtfIndex[i].addFocusListener(this);
		} else {
		  jtfIndex[i].setRequestFocusEnabled(false);
		}
                jpIndex.add(jtfIndex[i]);
            }
        } else {
            jtfIndex = new JTextField[1];
	    jtfIndex[0] = new JTextField();
	    jtfIndex[0].setHorizontalAlignment(JTextField.RIGHT);
            dimIndex = new long[] {0};
	    jtfIndex[0] = new JTextField(4);
	    jtfIndex[0].setEnabled(false);
	    jtfIndex[0].setRequestFocusEnabled(false);
	    jpIndex.add(jtfIndex[0]);
        }

	validate();

	setInitialIndex();

	showIndexData();

    }

    private void setInitialIndex() {

        recNum = 0;
        if (variance) jtfRec.set("1");
        else jtfRec.set("*");

        String initial;
        if (nDims > 0) {
            for (int i = 0; i < (int) nDims; i++) {
                dimIndex[i] = 0;
                if (dimVars[i] == NOVARY) {
                  initial = "*";
                  jtfIndex[i].setEditable(false);
                } else {
                  initial = "1";
		  jtfIndex[i].setEditable(true);
		}
                jtfIndex[i].setText(initial);
            }
        } else {
            initial = "*";
	    jtfIndex[0].setEnabled(false);
	    jtfIndex[0].setEditable(false);
            jtfIndex[0].setText(initial);
        }
    }

    private void showIndexData() {

        virtual = false;
        try {
          datavalue = var.getSingleData((long) recNum, dimIndex);

          status = cdf.getStatus();
          if (status == VIRTUAL_RECORD_DATA) virtual = true;

          if (dataType == CDF_EPOCH)
            output = Epoch.encode(((Double)datavalue).doubleValue());
          else if (dataType == CDF_EPOCH16)
            output = Epoch16.encode((double[])datavalue);
          else if (dataType == CDF_TIME_TT2000)
            output = CDFTT2000.toUTCstring(((Long)datavalue).longValue());
	  else {
            if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
              if (((String)datavalue).length() >= numElements)
                output = ((String)datavalue).substring(0, (int)numElements);
              else if (((String)datavalue).length() < numElements) {
                  StringBuffer tmp = new StringBuffer();
                  for (int i = 0; i < (int) numElements - ((String)datavalue).length(); i++)
                    tmp.append(" ");
                  output = (String)datavalue + tmp.toString();
              }
              output = "\"" + output + "\"";
            } else {
	      if (format == null) output = datavalue.toString();
	      else {
		if (dataType == CDF_REAL4 || dataType == CDF_FLOAT ||
		    dataType == CDF_DOUBLE) {
		  double tmp;
		  if (dataType == CDF_REAL4 || dataType == CDF_FLOAT) 
		    tmp = ((Float)datavalue).floatValue();
		  else 
		    tmp = ((Double)datavalue).doubleValue();
		  output = CDFE_Cformat.getInstance(format).form((double)tmp).toString();
		} else
		  output = datavalue.toString();
	      }
	    }
	  }
        } catch (CDFException ex) {
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(myEditor.getFrame(),
                                        "Exception:"+cdf.getStatus());
        }
	jtfVal.set((virtual) ? output+ "     " + Virtual : output);
	update.setEnabled(true);
	cancel.setEnabled(true);
    }

    public void reset() {
	values.setVisible(false);
    }

    public void performVariableAction(CDFE_VariableEvent e) {
	Variable selectedVar = e.getVariable();
	int type = e.getID();
	
	switch (type) {
	case CDFE_VariableEvent.CREATED:
	    new CDFE_NewVariableAction(myEditor, selectedVar.toString());
	    break;
	case CDFE_VariableEvent.DELETED:
	    reset();
	    break;
	case CDFE_VariableEvent.NAME_CHANGE:
	case CDFE_VariableEvent.DATATYPE_CHANGE:
	case CDFE_VariableEvent.NDIM_CHANGE:
	case CDFE_VariableEvent.NELEMENTS_CHANGE:
	case CDFE_VariableEvent.DIMSIZE_CHANGE:
	case CDFE_VariableEvent.RECVARY_CHANGE:
	case CDFE_VariableEvent.DIMVARY_CHANGE:
	default:
	    break;
	}
    }

    /**
     * Process button to change the variable data value at current indices 
     */
    public void actionPerformed( ActionEvent event ) {
        Object source = event.getSource();

	String recStr = (String) jtfRec.get(); 
	long tmp;
	if (recStr.trim().equals("*")) tmp = 1;
	else tmp = Long.parseLong(recStr.trim());
	if (variance) {
	  tmp--;
	  if (tmp < 0) {
	    Toolkit.getDefaultToolkit().beep();
	    jtfRec.set(oldRec.getText());
	    return;
	  }
	  oldRec.setText(recStr);
	  recNum = tmp;
	}
	if (nDims < 1) dimIndex = new long[] {0};
	else {
	  for (int i = 0; i < nDims; i++) {
	    if (source == jtfIndex[i]) {
	      tmp = Long.parseLong(jtfIndex[i].getText().trim());
	      if (tmp < 1 || tmp > dimSizes[i]) {
	        Toolkit.getDefaultToolkit().beep();
	        jtfIndex[i].setText(oldIndex[i].getText());
	        return;
	      }
	      oldIndex[i].setText(jtfIndex[i].getText());
	      dimIndex[i] = tmp - 1;
	    }
	  }
	}
	

	if (source instanceof JTextField) 
	  showIndexData();
	else if (source instanceof JButton) {
	    update.setEnabled(false);
	    cancel.setEnabled(false);
            String action = event.getActionCommand();
	    if (action.equals(CANCEL)) { // Cancel the value update
	      showIndexData();
	    } else if (action.equals(UPDATE)) { // Update the value for the variable
	      try {
		Object valueX = null;
		String newValue = ((String)jtfVal.get()).trim();
		String newActualValue;
		boolean changed = false;
		int ick = newValue.toLowerCase().indexOf("<virtual>");
		if (ick != -1) {
		   valueX = CDFE_CDFToolUtils.parseContent(newValue.substring(0, ick-1).trim(), dataType, numElements); 
		   jtfVal.set(newValue);
		} else
		   valueX = CDFE_CDFToolUtils.parseContent(newValue, dataType, numElements);
		changed = true;
/*
                switch ((int)dataType) {
                case (int) CDF_BYTE:
                case (int) CDF_INT1:   
			valueX = new Byte(newActualValue);
			changed = true;
			break;
                case (int) CDF_UINT1:  
                case (int) CDF_INT2:   
			valueX = new Short(newActualValue);
			changed = true;
			break;
                case (int) CDF_UINT2:  
                case (int) CDF_INT4:
			valueX = new Integer(newActualValue);   
			changed = true;
			break;
                case (int) CDF_UINT4:  
                case (int) CDF_INT8:  
			valueX = new Long(newActualValue);
			changed = true;
			break;
                case (int) CDF_REAL4:  
                case (int) CDF_FLOAT:  
			double ad = CDFE_Cformat.atof(newActualValue);
			valueX = new Float(ad);
			changed = true;
			break;
                case (int) CDF_REAL8:  
                case (int) CDF_DOUBLE: 
			valueX = new Double(CDFE_Cformat.atof(newActualValue));
			changed = true;
			break;
		case (int) CDF_EPOCH:
			try {
			  valueX = new Double(Epoch.parse(newActualValue));
			  changed = true;
			} catch (CDFException ex) {
			  Toolkit.getDefaultToolkit().beep();
			  JOptionPane.showMessageDialog(myEditor.getFrame(),
						"ILLEGAL_EPOCH_VALUE:"+newActualValue);
			  showIndexData();
                          update.setEnabled(false);
                          cancel.setEnabled(false);
			}
			break;
                case (int) CDF_EPOCH16:
                        try {
                          valueX = Epoch16.parse(newActualValue);
			  changed = true;
                        } catch (CDFException ex) {
			  Toolkit.getDefaultToolkit().beep();
			  JOptionPane.showMessageDialog(myEditor.getFrame(),
						"ILLEGAL_EPOCH_VALUE:"+newActualValue);
			  showIndexData();
                          update.setEnabled(false);
                          cancel.setEnabled(false);
                        }
                        break;
		case (int) CDF_TIME_TT2000:
			try {
			  valueX = new Long(CDFTT2000.fromUTCstring(newActualValue));
			  changed = true;
			} catch (CDFException ex) {
			  Toolkit.getDefaultToolkit().beep();
			  JOptionPane.showMessageDialog(myEditor.getFrame(),
						"ILLEGAL_TT2000:"+newActualValue);
			  showIndexData();
                          update.setEnabled(false);
                          cancel.setEnabled(false);
			}
			break;
                case (int) CDF_CHAR:   
                case (int) CDF_UCHAR: 
			newActualValue = newActualValue.substring(1, 
						newActualValue.length()-1);
			if (newActualValue.length() > numElements)
			  newActualValue = newActualValue.substring(0, (int) numElements);
			valueX = newActualValue; 
			changed = true;
			break;
                    // keep compiler happy
                default: 
			dataType = CDFConstants.CDF_BYTE;   
			valueX = null;
			break;
              }
*/
	      if (changed) {
	        var.putSingleData(recNum, dimIndex, valueX);
	        if ((sparseRec == NO_SPARSERECORDS && recNum > maxRec) || 
            	    (sparseRec != NO_SPARSERECORDS && virtual)) {
	          myEditor.variablePanel.getVarSpecPanel().set(var);
	          myEditor.variablePanel.getRightPanel().revalidate();
	          myEditor.variablePanel.getRightPanel().repaint();
	          maxRec = recNum;
	          if (CDFE_CDFToolUtils.getMaxRecNum(myEditor.theCDF) != 
		    myEditor.gAttrPanel.getMaxWritten()) {
		    int max = CDFE_CDFToolUtils.getMaxRecNum(myEditor.theCDF);
		    myEditor.gAttrPanel.setMaxWritten(max);
		    myEditor.gAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		    myEditor.gAttrPanel.getRightPanel().revalidate();
		    myEditor.gAttrPanel.getRightPanel().repaint();
		    myEditor.vAttrPanel.setMaxWritten(max);
		    myEditor.vAttrPanel.getCDFSpecPanel().set(myEditor.theCDF);
		    myEditor.vAttrPanel.getRightPanel().revalidate();
		    myEditor.vAttrPanel.getRightPanel().repaint();
		  }
                  showIndexData();
		}
	      }
	    } catch (CDFException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myEditor.getFrame(),
                                            CDFException.getStatusMsg(ex.getCurrentStatus()),
                                            "MYEditor: CDFException",
                                            JOptionPane.ERROR_MESSAGE);
                showIndexData();
                update.setEnabled(true);
                cancel.setEnabled(true);
	    } catch (NumberFormatException ex) {
		Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(myEditor.getFrame(),
                                            ex.toString(),
                                            "MYEditor: CDFException",
                                            JOptionPane.ERROR_MESSAGE);
                showIndexData();
                update.setEnabled(true);
                cancel.setEnabled(true);
	    }
	  }
	}

    }

    /**
     * Listen for documnet events in the record textfield
     */
    public void removeUpdate(DocumentEvent e) {
	update.setEnabled(true);
	cancel.setEnabled(true);
    }

    public void insertUpdate(DocumentEvent e) {
	update.setEnabled(true);
	cancel.setEnabled(true);
    }

    public void changedUpdate(DocumentEvent e) {
	update.setEnabled(true);
	cancel.setEnabled(true);
    }

    public void keyPressed(KeyEvent e) {
	int keyCode = e.getKeyCode();
	if (keyCode == KeyEvent.VK_UP) { // Up key
	  if (focusedObj == jtfRec.textField) {
	    recNum++;
	    jtfRec.set(""+(recNum+1));
	    showIndexData();
	  } else {
	    if (nDims > 0) {
	      for (int i = 0; i < nDims; i++) {
		if (focusedObj == jtfIndex[i]) {
		  if (dimVars[i] == NOVARY) break;
		  dimIndex[i]++;
		  if (dimIndex[i] == dimSizes[i]) dimIndex[i] = 0;
		  jtfIndex[i].setText(""+(dimIndex[i]+1));
		  showIndexData();
		  break;
		}
	      }
	    }
	  }
	} else if (keyCode == KeyEvent.VK_DOWN) { // Down key
          if (focusedObj == jtfRec.textField) {
            recNum--;
	    if (recNum < 0) recNum = maxRec;
	    if (recNum < 0) recNum = 0;
            jtfRec.set(""+(recNum+1));
            showIndexData();
          } else {
            if (nDims > 0) {
              for (int i = 0; i < nDims; i++) {
                if (focusedObj == jtfIndex[i]) {
                  if (dimVars[i] == NOVARY) break;
                  dimIndex[i]--;
                  if (dimIndex[i] < 0) dimIndex[i] = dimSizes[i] - 1;
                  jtfIndex[i].setText(""+(dimIndex[i]+1));
                  showIndexData();
                  break;
                }
              }
	    }
          }
	}

    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }

    public void focusGained(FocusEvent e) {
	focusedObj = e.getSource();
	  boolean nomore = false;
          if (focusedObj == jtfRec.textField) {
            if (nDims > 0) {
              for (int i = 0; i < nDims; i++) {
                if (dimVars[i] == VARY) {
		  jtfRec.textField.setNextFocusableComponent(jtfIndex[i]);
                  nomore = true;
                  break;
                }
              }
            }
            if (!nomore) jtfRec.textField.setNextFocusableComponent(jtfVal.textField);
          } else if (focusedObj == jtfVal.textField) {
            if (variance) {
              jtfVal.textField.setNextFocusableComponent(jtfRec.textField);
            } else {
              if (nDims > 0) {
                for (int i = 0; i < nDims; i++) {
                  if (dimVars[i] == VARY) {
                    jtfVal.textField.setNextFocusableComponent(jtfIndex[i]);
                    nomore = true;
                    break;
                  }
                }
              }
              if (!nomore) jtfVal.textField.setNextFocusableComponent(jtfVal.textField);
            }
          }
    }

    public void focusLost(FocusEvent e) {

    }

    public boolean isFocusCycleRoot() {
	return true;

    }

    public boolean isManagingFocus() {
	return true;
    }

    public boolean isVirtual() {
	return virtual;

    }

    public long getRecNum() {
	return recNum;
    }

}

