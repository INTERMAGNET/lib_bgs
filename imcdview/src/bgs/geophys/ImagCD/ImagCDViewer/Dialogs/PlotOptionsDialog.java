/*
 * PlotOptionsDialog.java
 *
 * Created on 18 February 2005, 09:36
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Swing.JComboSpinner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SpinnerListModel;

/**
 *
 * @author  smf
 */
public class PlotOptionsDialog extends javax.swing.JDialog 
{

    /** array of scale values for scalar components in nT (1st column) and
     * angular components in minutes of arc (2nd column), -ve means auto scale */
    private static final int scales [] [] =
    {
        {-1,     -1},
        { 10,     1},
        { 20,     2},
        { 50,     5},
        {100,    10},
        {200,    20},
        {500,    30},
        {1000,   60},
        {2000,  120},
        {5000,  300},
        {10000, 600}
    };
    
    // array of values displayed to the user to choose combined (scalar and angular) scale
    private static String combined_ranges [];
    
    // static initialiser
    static
    {
        int count;
        
        combined_ranges = new String [scales.length];
        for (count=0; count<scales.length; count++)
        {
            if (scales[count][1] <= 0) combined_ranges [count] = "Auto scale";
            else combined_ranges [count] = Integer.toString (scales[count][0]) + " nT / " + Integer.toString (scales[count][1]) + " minutes";
        }
    }

    // private members
    // array of values displayed to the user to choose scalar scale
    private String scalar_ranges [];
    // array of values displayed to the user to choose angular scale
    private String angular_ranges [];
    // flag for units of d/i (nT or min arc)
    private boolean angles_in_min_arc;
    // maximum length of spinner strings
    private int max_spinner_string_length;
    // special combo box component
    private JComboSpinner AllScaleComboSpinner;
    // copy of configuration - used to restore state on cancel
    private Properties saved_config;
    
    /** Creates new form PlotOptionsDialog */
    public PlotOptionsDialog(java.awt.Frame parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        initComponents2();
    }
    
    /** Creates new form PlotOptionsDialog */
    public PlotOptionsDialog(java.awt.Dialog parent, boolean modal) 
    {
        super(parent, modal);
        initComponents();
        initComponents2();
    }
    
    private void initComponents2 ()
    {
        
        int count, length;
        String str;
        
        AllScaleComboSpinner = (JComboSpinner) getZoomComboBox(false);
        AllScaleComboSpinner.getComboBox().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AllScaleComboSpinnerStateChanged(e);
                }
            } );
        
        AllScalePanel.add (AllScaleComboSpinner);
        
        // set range strings for scalar and angular values
        scalar_ranges = new String [scales.length];
        angular_ranges = new String [scales.length];
        max_spinner_string_length = -1;
        for (count=0; count<scales.length; count++)
        {
            if (scales[count][0] <= 0) scalar_ranges [count] = "Auto scale";
            else scalar_ranges [count] = Integer.toString (scales[count][0]) + " nT";
            if (scales[count][1] <= 0) angular_ranges [count] = "Auto scale";
            else angular_ranges [count] = Integer.toString (scales[count][1]) + " minutes";

            length = scalar_ranges[count].length();
            if (length > max_spinner_string_length) max_spinner_string_length = length;
            length = angular_ranges[count].length();
            if (length > max_spinner_string_length) max_spinner_string_length = length;
        }

        // find out whether D is in minutes of arc or nT
        if (isAngleScaleInMinArc())
        {
            angles_in_min_arc = true;
            AngleUnitsNtButton.setSelected(false);
            AngleUnitsMinArcButton.setSelected(true);
        }
        else
        {
            angles_in_min_arc = false;
            AngleUnitsMinArcButton.setSelected(false);
            AngleUnitsNtButton.setSelected(true);
        }
        
        // set the spinner data model
        XScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        YScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        ZScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        FScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        HScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        if (angles_in_min_arc) 
        {
            DScaleSpinner.setModel (new javax.swing.SpinnerListModel (angular_ranges));
            IScaleSpinner.setModel (new javax.swing.SpinnerListModel (angular_ranges));
        }
        else
        {
            DScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
            IScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        }
        FScalarScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
        FDiffScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));

        // set the spinners to their longest strings
        ((javax.swing.JSpinner.DefaultEditor) XScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) YScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) ZScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) FScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) HScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) DScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) IScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) FScalarScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) FDiffScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);

        // set components to their stored values
        XScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("X", scales)]);
        YScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("Y", scales)]);
        ZScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("Z", scales)]);
        FScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("F", scales)]);
        HScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("H", scales)]);
        if (angles_in_min_arc)
        {
            DScaleSpinner.getModel().setValue (angular_ranges [findScaleIndex ("D", scales)]);
            IScaleSpinner.getModel().setValue (angular_ranges [findScaleIndex ("I", scales)]);
        }
        else
        {
            DScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("D", scales)]);
            IScaleSpinner.getModel().setValue (scalar_ranges [findScaleIndex ("I", scales)]);
        }
        FScalarScaleSpinner.getModel().setValue(scalar_ranges [findScaleIndex ("FScalar", scales)]);
        FDiffScaleSpinner.getModel().setValue(scalar_ranges [findScaleIndex ("FDiff", scales)]);
        
        XColourExampleLabel.setBackground(getColourFromConfiguration("X"));
        YColourExampleLabel.setBackground(getColourFromConfiguration("Y"));
        ZColourExampleLabel.setBackground(getColourFromConfiguration("Z"));
        FColourExampleLabel.setBackground(getColourFromConfiguration("F"));
        HColourExampleLabel.setBackground(getColourFromConfiguration("H"));
        DColourExampleLabel.setBackground(getColourFromConfiguration("D"));
        IColourExampleLabel.setBackground(getColourFromConfiguration("I"));
        FScalarColourExampleLabel.setBackground(getColourFromConfiguration("FScalar"));
        FDiffColourExampleLabel.setBackground(getColourFromConfiguration("FDiff"));
        AllColourExampleLabel.setBackground(getColourFromConfiguration("All"));

        str = GlobalObjects.configuration.getProperty("PlotNativeOrCalculated");
        if (str == null) str = "";
        if (str.equalsIgnoreCase("Calculated")) 
        {
            CalculatedRadioButton.setSelected (true);
            enableShowCheckBoxes (true);
        }
        else
        {
            NativeRadioButton.setSelected (true);
            enableShowCheckBoxes (false);
        }
            
        XShowCheckBox.setSelected(isShownFromConfiguration("X"));
        YShowCheckBox.setSelected(isShownFromConfiguration("Y"));
        ZShowCheckBox.setSelected(isShownFromConfiguration("Z"));
        FShowCheckBox.setSelected(isShownFromConfiguration("F"));
        HShowCheckBox.setSelected(isShownFromConfiguration("H"));
        DShowCheckBox.setSelected(isShownFromConfiguration("D"));
        IShowCheckBox.setSelected(isShownFromConfiguration("I"));
        FScalarShowCheckBox.setSelected(isShownFromConfiguration("FScalar"));
        FDiffShowCheckBox.setSelected(isShownFromConfiguration("FDiff"));
        
        try { count = Integer.parseInt(GlobalObjects.configuration.getProperty("PlotExtraWidth", "0")); }
        catch (NumberFormatException e) { count = 0; }
        DisplayWidthSpinner.getModel().setValue (new Integer (count));
        
        // re-pack the dialog
        pack ();
        
        // get state for cancel button
        saved_config = GlobalObjects.checkpointConfig();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        RemoveAverageButtonGroup = new javax.swing.ButtonGroup();
        DUnitsButtonGroup = new javax.swing.ButtonGroup();
        NativeOrCalcButtonGroup = new javax.swing.ButtonGroup();
        NativeOrCalcPanel = new javax.swing.JPanel();
        NativeOrCalcLabel = new javax.swing.JLabel();
        NativeRadioButton = new javax.swing.JRadioButton();
        CalculatedRadioButton = new javax.swing.JRadioButton();
        RecordedNativeHelpLabel = new javax.swing.JLabel();
        Separator1 = new javax.swing.JSeparator();
        ElementPanel = new javax.swing.JPanel();
        ComponentTitleLabel = new javax.swing.JLabel();
        EnabledTitleLabel = new javax.swing.JLabel();
        ScaleTitleLabel = new javax.swing.JLabel();
        ColourTitleLabel = new javax.swing.JLabel();
        XLabel = new javax.swing.JLabel();
        XShowCheckBox = new javax.swing.JCheckBox();
        XScaleSpinner = new javax.swing.JSpinner();
        XColourExampleLabel = new javax.swing.JLabel();
        XColourChangeButton = new javax.swing.JButton();
        YLabel = new javax.swing.JLabel();
        YShowCheckBox = new javax.swing.JCheckBox();
        YScaleSpinner = new javax.swing.JSpinner();
        YColourExampleLabel = new javax.swing.JLabel();
        YColourChangeButton = new javax.swing.JButton();
        ZLabel = new javax.swing.JLabel();
        ZShowCheckBox = new javax.swing.JCheckBox();
        ZScaleSpinner = new javax.swing.JSpinner();
        ZColourExampleLabel = new javax.swing.JLabel();
        ZColourChangeButton = new javax.swing.JButton();
        FLabel = new javax.swing.JLabel();
        FShowCheckBox = new javax.swing.JCheckBox();
        FScaleSpinner = new javax.swing.JSpinner();
        FColourExampleLabel = new javax.swing.JLabel();
        FColourChangeButton = new javax.swing.JButton();
        HLabel = new javax.swing.JLabel();
        HShowCheckBox = new javax.swing.JCheckBox();
        HScaleSpinner = new javax.swing.JSpinner();
        HColourExampleLabel = new javax.swing.JLabel();
        HColourChangeButton = new javax.swing.JButton();
        DLabel = new javax.swing.JLabel();
        DShowCheckBox = new javax.swing.JCheckBox();
        DScaleSpinner = new javax.swing.JSpinner();
        DColourExampleLabel = new javax.swing.JLabel();
        DColourChangeButton = new javax.swing.JButton();
        ILabel = new javax.swing.JLabel();
        IShowCheckBox = new javax.swing.JCheckBox();
        IScaleSpinner = new javax.swing.JSpinner();
        IColourExampleLabel = new javax.swing.JLabel();
        IColourChangeButton = new javax.swing.JButton();
        FScalarLabel = new javax.swing.JLabel();
        FScalarShowCheckBox = new javax.swing.JCheckBox();
        FScalarScaleSpinner = new javax.swing.JSpinner();
        FScalarColourExampleLabel = new javax.swing.JLabel();
        FScalarColourChangeButton = new javax.swing.JButton();
        FDiffLabel = new javax.swing.JLabel();
        FDiffShowCheckBox = new javax.swing.JCheckBox();
        FDiffScaleSpinner = new javax.swing.JSpinner();
        FDiffColourExampleLabel = new javax.swing.JLabel();
        FDiffColourChangeButton = new javax.swing.JButton();
        AngleUnitsLabel = new javax.swing.JLabel();
        AngleUnitsPanel = new javax.swing.JPanel();
        AngleUnitsNtButton = new javax.swing.JRadioButton();
        AngleUnitsMinArcButton = new javax.swing.JRadioButton();
        Separator2 = new javax.swing.JSeparator();
        AllLabel = new javax.swing.JLabel();
        AllScalePanel = new javax.swing.JPanel();
        AllColourExampleLabel = new javax.swing.JLabel();
        AllColourChangeButton = new javax.swing.JButton();
        Separator3 = new javax.swing.JSeparator();
        DisplayWidthPanel = new javax.swing.JPanel();
        DisplayWidthLabel = new javax.swing.JLabel();
        DisplayWidthSpinner = new javax.swing.JSpinner();
        Separator4 = new javax.swing.JSeparator();
        ActionAreaPanel = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        ApplyButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Plot Options");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        NativeOrCalcPanel.setLayout(new java.awt.GridBagLayout());

        NativeOrCalcLabel.setText("Show recorded or calculated elements:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        NativeOrCalcPanel.add(NativeOrCalcLabel, gridBagConstraints);

        NativeOrCalcButtonGroup.add(NativeRadioButton);
        NativeRadioButton.setText("Recorded");
        NativeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NativeRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        NativeOrCalcPanel.add(NativeRadioButton, gridBagConstraints);

        NativeOrCalcButtonGroup.add(CalculatedRadioButton);
        CalculatedRadioButton.setText("Calculated");
        CalculatedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CalculatedRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        NativeOrCalcPanel.add(CalculatedRadioButton, gridBagConstraints);

        RecordedNativeHelpLabel.setText("Select ‘recorded’ elements to display only the elements recorded in the archive file.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        NativeOrCalcPanel.add(RecordedNativeHelpLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        getContentPane().add(NativeOrCalcPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(Separator1, gridBagConstraints);

        ElementPanel.setLayout(new java.awt.GridBagLayout());

        ComponentTitleLabel.setText("Element");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        ElementPanel.add(ComponentTitleLabel, gridBagConstraints);

        EnabledTitleLabel.setText("Display");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        ElementPanel.add(EnabledTitleLabel, gridBagConstraints);

        ScaleTitleLabel.setText("Scale");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        ElementPanel.add(ScaleTitleLabel, gridBagConstraints);

        ColourTitleLabel.setText("Colour");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        ElementPanel.add(ColourTitleLabel, gridBagConstraints);

        XLabel.setText("X");
        ElementPanel.add(XLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(XShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(XScaleSpinner, new java.awt.GridBagConstraints());

        XColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        XColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        XColourExampleLabel.setText("        ");
        XColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(XColourExampleLabel, gridBagConstraints);

        XColourChangeButton.setText("Change Colour...");
        XColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(XColourChangeButton, gridBagConstraints);

        YLabel.setText("Y");
        ElementPanel.add(YLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(YShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(YScaleSpinner, new java.awt.GridBagConstraints());

        YColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        YColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        YColourExampleLabel.setText("        ");
        YColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(YColourExampleLabel, gridBagConstraints);

        YColourChangeButton.setText("Change Colour...");
        YColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(YColourChangeButton, gridBagConstraints);

        ZLabel.setText("Z");
        ElementPanel.add(ZLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(ZShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(ZScaleSpinner, new java.awt.GridBagConstraints());

        ZColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        ZColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        ZColourExampleLabel.setText("        ");
        ZColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(ZColourExampleLabel, gridBagConstraints);

        ZColourChangeButton.setText("Change Colour...");
        ZColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(ZColourChangeButton, gridBagConstraints);

        FLabel.setText("F");
        ElementPanel.add(FLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(FShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(FScaleSpinner, new java.awt.GridBagConstraints());

        FColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        FColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        FColourExampleLabel.setText("        ");
        FColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FColourExampleLabel, gridBagConstraints);

        FColourChangeButton.setText("Change Colour...");
        FColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FColourChangeButton, gridBagConstraints);

        HLabel.setText("H");
        ElementPanel.add(HLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(HShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(HScaleSpinner, new java.awt.GridBagConstraints());

        HColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        HColourExampleLabel.setForeground(new java.awt.Color(255, 255, 255));
        HColourExampleLabel.setText("        ");
        HColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(HColourExampleLabel, gridBagConstraints);

        HColourChangeButton.setText("Change Colour...");
        HColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(HColourChangeButton, gridBagConstraints);

        DLabel.setText("D");
        ElementPanel.add(DLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(DShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(DScaleSpinner, new java.awt.GridBagConstraints());

        DColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        DColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        DColourExampleLabel.setText("        ");
        DColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(DColourExampleLabel, gridBagConstraints);

        DColourChangeButton.setText("Change Colour...");
        DColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(DColourChangeButton, gridBagConstraints);

        ILabel.setText("I");
        ElementPanel.add(ILabel, new java.awt.GridBagConstraints());
        ElementPanel.add(IShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(IScaleSpinner, new java.awt.GridBagConstraints());

        IColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        IColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        IColourExampleLabel.setText("        ");
        IColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(IColourExampleLabel, gridBagConstraints);

        IColourChangeButton.setText("Change Colour...");
        IColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(IColourChangeButton, gridBagConstraints);

        FScalarLabel.setText("F (scalar)");
        ElementPanel.add(FScalarLabel, new java.awt.GridBagConstraints());
        ElementPanel.add(FScalarShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(FScalarScaleSpinner, new java.awt.GridBagConstraints());

        FScalarColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        FScalarColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        FScalarColourExampleLabel.setText("        ");
        FScalarColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FScalarColourExampleLabel, gridBagConstraints);

        FScalarColourChangeButton.setText("Change Colour...");
        FScalarColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FScalarColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FScalarColourChangeButton, gridBagConstraints);

        FDiffLabel.setText("F difference");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        ElementPanel.add(FDiffLabel, gridBagConstraints);
        ElementPanel.add(FDiffShowCheckBox, new java.awt.GridBagConstraints());
        ElementPanel.add(FDiffScaleSpinner, new java.awt.GridBagConstraints());

        FDiffColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        FDiffColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        FDiffColourExampleLabel.setText("        ");
        FDiffColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FDiffColourExampleLabel, gridBagConstraints);

        FDiffColourChangeButton.setText("Change Colour...");
        FDiffColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FDiffColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(FDiffColourChangeButton, gridBagConstraints);

        AngleUnitsLabel.setText("D/I scale in");
        ElementPanel.add(AngleUnitsLabel, new java.awt.GridBagConstraints());

        DUnitsButtonGroup.add(AngleUnitsNtButton);
        AngleUnitsNtButton.setText("nT");
        AngleUnitsNtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AngleUnitsNtButtonActionPerformed(evt);
            }
        });
        AngleUnitsPanel.add(AngleUnitsNtButton);

        DUnitsButtonGroup.add(AngleUnitsMinArcButton);
        AngleUnitsMinArcButton.setText("Minutes");
        AngleUnitsMinArcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AngleUnitsMinArcButtonActionPerformed(evt);
            }
        });
        AngleUnitsPanel.add(AngleUnitsMinArcButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ElementPanel.add(AngleUnitsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 3);
        ElementPanel.add(Separator2, gridBagConstraints);

        AllLabel.setText("All");
        ElementPanel.add(AllLabel, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        ElementPanel.add(AllScalePanel, gridBagConstraints);

        AllColourExampleLabel.setBackground(new java.awt.Color(0, 102, 255));
        AllColourExampleLabel.setForeground(new java.awt.Color(0, 102, 255));
        AllColourExampleLabel.setText("        ");
        AllColourExampleLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(AllColourExampleLabel, gridBagConstraints);

        AllColourChangeButton.setText("Change Colour...");
        AllColourChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AllColourChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        ElementPanel.add(AllColourChangeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ElementPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(Separator3, gridBagConstraints);

        DisplayWidthLabel.setText("Amount to extend time window by (%):");
        DisplayWidthPanel.add(DisplayWidthLabel);

        DisplayWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 50, 1));
        DisplayWidthPanel.add(DisplayWidthSpinner);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(DisplayWidthPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(Separator4, gridBagConstraints);

        OKButton.setText("OK");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        ActionAreaPanel.add(OKButton);

        ApplyButton.setText("Apply");
        ApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplyButtonActionPerformed(evt);
            }
        });
        ActionAreaPanel.add(ApplyButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });
        ActionAreaPanel.add(CancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        getContentPane().add(ActionAreaPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AngleUnitsMinArcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AngleUnitsMinArcButtonActionPerformed
        setAngleSpinner(true);
}//GEN-LAST:event_AngleUnitsMinArcButtonActionPerformed

    private void AngleUnitsNtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AngleUnitsNtButtonActionPerformed
        setAngleSpinner(false);
}//GEN-LAST:event_AngleUnitsNtButtonActionPerformed

    private void AllColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AllColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for Y", YColourExampleLabel.getBackground());
        if (new_colour != null)
        {
            AllColourExampleLabel.setBackground (new_colour);
            XColourExampleLabel.setBackground (new_colour);
            YColourExampleLabel.setBackground (new_colour);
            ZColourExampleLabel.setBackground (new_colour);
            FColourExampleLabel.setBackground (new_colour);
            HColourExampleLabel.setBackground (new_colour);
            DColourExampleLabel.setBackground (new_colour);
            IColourExampleLabel.setBackground (new_colour);
            FScalarColourExampleLabel.setBackground (new_colour);
            FDiffColourExampleLabel.setBackground (new_colour);
        }
    }//GEN-LAST:event_AllColourChangeButtonActionPerformed

    private void ApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyButtonActionPerformed
        int index;
        String errmsg;
        
        errmsg = checkDialogFields();
        if (errmsg != null)
        {
            JOptionPane.showMessageDialog(this, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            GlobalObjects.configuration.setProperty ("DInMinArc", angles_in_min_arc ? "1" : "0");

            GlobalObjects.configuration.setProperty ("XPlotColour",   Integer.toString (XColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("YPlotColour",   Integer.toString (YColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("ZPlotColour",   Integer.toString (ZColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("FPlotColour",   Integer.toString (FColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("HPlotColour",   Integer.toString (HColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("DPlotColour",   Integer.toString (DColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("IPlotColour",   Integer.toString (IColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("FScalarPlotColour",   Integer.toString (FScalarColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("FDiffPlotColour",   Integer.toString (FDiffColourExampleLabel.getBackground().getRGB()));
            GlobalObjects.configuration.setProperty ("AllPlotColour", Integer.toString (AllColourExampleLabel.getBackground().getRGB()));

            GlobalObjects.configuration.setProperty ("XPlotScale",   Integer.toString (scales [findSpinnerIndex (XScaleSpinner,   scalar_ranges)] [0]));
            GlobalObjects.configuration.setProperty ("YPlotScale",   Integer.toString (scales [findSpinnerIndex (YScaleSpinner,   scalar_ranges)] [0]));
            GlobalObjects.configuration.setProperty ("ZPlotScale",   Integer.toString (scales [findSpinnerIndex (ZScaleSpinner,   scalar_ranges)] [0]));
            GlobalObjects.configuration.setProperty ("FPlotScale",   Integer.toString (scales [findSpinnerIndex (FScaleSpinner,   scalar_ranges)] [0]));
            GlobalObjects.configuration.setProperty ("HPlotScale",   Integer.toString (scales [findSpinnerIndex (HScaleSpinner,   scalar_ranges)] [0]));
            if (angles_in_min_arc)
            {
                GlobalObjects.configuration.setProperty ("DPlotScale",   Integer.toString (scales [findSpinnerIndex (DScaleSpinner,   angular_ranges)][1]));
                GlobalObjects.configuration.setProperty ("IPlotScale",   Integer.toString (scales [findSpinnerIndex (IScaleSpinner,   angular_ranges)][1]));
            }
            else
            {
                GlobalObjects.configuration.setProperty ("DPlotScale",   Integer.toString (scales [findSpinnerIndex (DScaleSpinner,    scalar_ranges)][0]));
                GlobalObjects.configuration.setProperty ("IPlotScale",   Integer.toString (scales [findSpinnerIndex (DScaleSpinner,    scalar_ranges)][0]));
            }
            GlobalObjects.configuration.setProperty ("FScalarPlotScale", Integer.toString (scales [findSpinnerIndex (FScalarScaleSpinner, scalar_ranges)][0]));
            GlobalObjects.configuration.setProperty ("FDiffPlotScale",   Integer.toString (scales [findSpinnerIndex (FDiffScaleSpinner,   scalar_ranges)][0]));
            index = AllScaleComboSpinner.getComboBox().getSelectedIndex();
            if (index < 0) index = 0;
            GlobalObjects.configuration.setProperty ("AllPlotScale",     Integer.toString (scales [index][0]));

            GlobalObjects.configuration.setProperty ("XIsShown",        Boolean.toString (XShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("YIsShown",        Boolean.toString (YShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("ZIsShown",        Boolean.toString (ZShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("FIsShown",        Boolean.toString (FShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("HIsShown",        Boolean.toString (HShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("DIsShown",        Boolean.toString (DShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("IIsShown",        Boolean.toString (IShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("FScalarIsShown",  Boolean.toString (FScalarShowCheckBox.isSelected ()));
            GlobalObjects.configuration.setProperty ("FDiffIsShown",    Boolean.toString (FDiffShowCheckBox.isSelected ()));
        
            if (CalculatedRadioButton.isSelected())
                GlobalObjects.configuration.setProperty ("PlotNativeOrCalculated", "Calculated");
            else
                GlobalObjects.configuration.setProperty ("PlotNativeOrCalculated", "Recorded");

            GlobalObjects.configuration.setProperty ("PlotExtraWidth", ((Integer) DisplayWidthSpinner.getValue()).toString());
        
            GlobalObjects.command_interpreter.interpretCommand ("new_plot_options");
        }
    }//GEN-LAST:event_ApplyButtonActionPerformed

    /* modify the dialog and configuration with a zoom plot operation
     * @param amount for relative zooming, amount = -1 to zoom in, +1 to zoom out 
     *        for absolute zooming, amount = index of zoom item in list to set zoom to
     * @param relative true for relative zooming, false for absolute */
    public void ZoomWithDialog (int amount, boolean relative)
    {
        AllScaleComboSpinner.incrementSelection (amount, relative);
        ApplyButtonActionPerformed (null);
    }
        
    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        GlobalObjects.configuration.setProperty ("DInMinArc",               saved_config.getProperty("DInMinArc"));

        GlobalObjects.configuration.setProperty ("XPlotColour",             saved_config.getProperty("XPlotColour"));
        GlobalObjects.configuration.setProperty ("YPlotColour",             saved_config.getProperty("YPlotColour"));
        GlobalObjects.configuration.setProperty ("ZPlotColour",             saved_config.getProperty("ZPlotColour"));
        GlobalObjects.configuration.setProperty ("FPlotColour",             saved_config.getProperty("FPlotColour"));
        GlobalObjects.configuration.setProperty ("HPlotColour",             saved_config.getProperty("HPlotColour"));
        GlobalObjects.configuration.setProperty ("DPlotColour",             saved_config.getProperty("DPlotColour"));
        GlobalObjects.configuration.setProperty ("IPlotColour",             saved_config.getProperty("IPlotColour"));
        GlobalObjects.configuration.setProperty ("FScalarPlotColour",       saved_config.getProperty("FScalarPlotColour"));
        GlobalObjects.configuration.setProperty ("FDiffPlotColour",         saved_config.getProperty("FDiffPlotColour"));
        GlobalObjects.configuration.setProperty ("AllPlotColour",           saved_config.getProperty("AllPlotColour"));

        GlobalObjects.configuration.setProperty ("XPlotScale",              saved_config.getProperty("XPlotScale"));
        GlobalObjects.configuration.setProperty ("YPlotScale",              saved_config.getProperty("YPlotScale"));
        GlobalObjects.configuration.setProperty ("ZPlotScale",              saved_config.getProperty("ZPlotScale"));
        GlobalObjects.configuration.setProperty ("FPlotScale",              saved_config.getProperty("FPlotScale"));
        GlobalObjects.configuration.setProperty ("HPlotScale",              saved_config.getProperty("HPlotScale"));
        GlobalObjects.configuration.setProperty ("DPlotScale",              saved_config.getProperty("DPlotScale"));
        GlobalObjects.configuration.setProperty ("IPlotScale",              saved_config.getProperty("IPlotScale"));
        GlobalObjects.configuration.setProperty ("FScalarPlotScale",        saved_config.getProperty("FScalarPlotScale"));
        GlobalObjects.configuration.setProperty ("FDiffPlotScale",          saved_config.getProperty("FDiffPlotScale"));
        GlobalObjects.configuration.setProperty ("AllPlotScale",            saved_config.getProperty("AllPlotScale"));

        GlobalObjects.configuration.setProperty ("XIsShown",                saved_config.getProperty("XIsShown"));
        GlobalObjects.configuration.setProperty ("YIsShown",                saved_config.getProperty("YIsShown"));
        GlobalObjects.configuration.setProperty ("ZIsShown",                saved_config.getProperty("ZIsShown"));
        GlobalObjects.configuration.setProperty ("FIsShown",                saved_config.getProperty("FIsShown"));
        GlobalObjects.configuration.setProperty ("HIsShown",                saved_config.getProperty("HIsShown"));
        GlobalObjects.configuration.setProperty ("DIsShown",                saved_config.getProperty("DIsShown"));
        GlobalObjects.configuration.setProperty ("IIsShown",                saved_config.getProperty("IIsShown"));
        GlobalObjects.configuration.setProperty ("FScalarIsShown",          saved_config.getProperty("FScalarIsShown"));
        GlobalObjects.configuration.setProperty ("FDiffIsShown",            saved_config.getProperty("FDiffIsShown"));
        
        GlobalObjects.configuration.setProperty ("PlotNativeOrCalculated",  saved_config.getProperty("PlotNativeOrCalculated"));

        GlobalObjects.configuration.setProperty ("PlotExtraWidth",          saved_config.getProperty("PlotExtraWidth"));

        GlobalObjects.command_interpreter.interpretCommand ("new_plot_options");
        closeDialog (null);
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        String errmsg;
        
        errmsg = checkDialogFields();
        if (errmsg != null)
        {
            JOptionPane.showMessageDialog(this, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            ApplyButtonActionPerformed(evt);
            closeDialog (null);
        }
    }//GEN-LAST:event_OKButtonActionPerformed

    private void FColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for F", FColourExampleLabel.getBackground());
        if (new_colour != null) FColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_FColourChangeButtonActionPerformed

    private void YColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for Y", YColourExampleLabel.getBackground());
        if (new_colour != null) YColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_YColourChangeButtonActionPerformed

    private void XColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for X", XColourExampleLabel.getBackground());
        if (new_colour != null) XColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_XColourChangeButtonActionPerformed

    private void ZColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for Z", ZColourExampleLabel.getBackground());
        if (new_colour != null) ZColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_ZColourChangeButtonActionPerformed

    private void DColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for D", DColourExampleLabel.getBackground());
        if (new_colour != null) DColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_DColourChangeButtonActionPerformed

    private void HColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for H", HColourExampleLabel.getBackground());
        if (new_colour != null) HColourExampleLabel.setBackground (new_colour);
    }//GEN-LAST:event_HColourChangeButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void IColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for I", IColourExampleLabel.getBackground());
        if (new_colour != null) IColourExampleLabel.setBackground (new_colour);
}//GEN-LAST:event_IColourChangeButtonActionPerformed

    private void FScalarColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FScalarColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for F (scalar)", FScalarColourExampleLabel.getBackground());
        if (new_colour != null) FScalarColourExampleLabel.setBackground (new_colour);
}//GEN-LAST:event_FScalarColourChangeButtonActionPerformed

    private void FDiffColourChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FDiffColourChangeButtonActionPerformed
        java.awt.Color new_colour;
        
        new_colour = javax.swing.JColorChooser.showDialog(this, "Choose colour for F difference", FDiffColourExampleLabel.getBackground());
        if (new_colour != null) FDiffColourExampleLabel.setBackground (new_colour);
}//GEN-LAST:event_FDiffColourChangeButtonActionPerformed

    private void NativeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NativeRadioButtonActionPerformed
        enableShowCheckBoxes (false);
    }//GEN-LAST:event_NativeRadioButtonActionPerformed

    private void CalculatedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CalculatedRadioButtonActionPerformed
        enableShowCheckBoxes (true);
    }//GEN-LAST:event_CalculatedRadioButtonActionPerformed
    
    private void AllScaleComboSpinnerStateChanged(ActionEvent evt) {
        int index;

        index = AllScaleComboSpinner.getComboBox().getSelectedIndex();
        if (index < 0) index = 0;
        XScaleSpinner.setValue (scalar_ranges[index]);
        YScaleSpinner.setValue (scalar_ranges[index]);
        ZScaleSpinner.setValue (scalar_ranges[index]);
        FScaleSpinner.setValue (scalar_ranges[index]);
        HScaleSpinner.setValue (scalar_ranges[index]);
        if (angles_in_min_arc) 
        {
            DScaleSpinner.setValue (angular_ranges[index]);
            IScaleSpinner.setValue (angular_ranges[index]);
        }
        else
        {
            DScaleSpinner.setValue (scalar_ranges[index]);
            IScaleSpinner.setValue (scalar_ranges[index]);
        }
        FScalarScaleSpinner.setValue (scalar_ranges[index]);
        FDiffScaleSpinner.setValue (scalar_ranges[index]);
    }                                            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ActionAreaPanel;
    private javax.swing.JButton AllColourChangeButton;
    private javax.swing.JLabel AllColourExampleLabel;
    private javax.swing.JLabel AllLabel;
    private javax.swing.JPanel AllScalePanel;
    private javax.swing.JLabel AngleUnitsLabel;
    private javax.swing.JRadioButton AngleUnitsMinArcButton;
    private javax.swing.JRadioButton AngleUnitsNtButton;
    private javax.swing.JPanel AngleUnitsPanel;
    private javax.swing.JButton ApplyButton;
    private javax.swing.JRadioButton CalculatedRadioButton;
    private javax.swing.JButton CancelButton;
    private javax.swing.JLabel ColourTitleLabel;
    private javax.swing.JLabel ComponentTitleLabel;
    private javax.swing.JButton DColourChangeButton;
    private javax.swing.JLabel DColourExampleLabel;
    private javax.swing.JLabel DLabel;
    private javax.swing.JSpinner DScaleSpinner;
    private javax.swing.JCheckBox DShowCheckBox;
    private javax.swing.ButtonGroup DUnitsButtonGroup;
    private javax.swing.JLabel DisplayWidthLabel;
    private javax.swing.JPanel DisplayWidthPanel;
    private javax.swing.JSpinner DisplayWidthSpinner;
    private javax.swing.JPanel ElementPanel;
    private javax.swing.JLabel EnabledTitleLabel;
    private javax.swing.JButton FColourChangeButton;
    private javax.swing.JLabel FColourExampleLabel;
    private javax.swing.JButton FDiffColourChangeButton;
    private javax.swing.JLabel FDiffColourExampleLabel;
    private javax.swing.JLabel FDiffLabel;
    private javax.swing.JSpinner FDiffScaleSpinner;
    private javax.swing.JCheckBox FDiffShowCheckBox;
    private javax.swing.JLabel FLabel;
    private javax.swing.JButton FScalarColourChangeButton;
    private javax.swing.JLabel FScalarColourExampleLabel;
    private javax.swing.JLabel FScalarLabel;
    private javax.swing.JSpinner FScalarScaleSpinner;
    private javax.swing.JCheckBox FScalarShowCheckBox;
    private javax.swing.JSpinner FScaleSpinner;
    private javax.swing.JCheckBox FShowCheckBox;
    private javax.swing.JButton HColourChangeButton;
    private javax.swing.JLabel HColourExampleLabel;
    private javax.swing.JLabel HLabel;
    private javax.swing.JSpinner HScaleSpinner;
    private javax.swing.JCheckBox HShowCheckBox;
    private javax.swing.JButton IColourChangeButton;
    private javax.swing.JLabel IColourExampleLabel;
    private javax.swing.JLabel ILabel;
    private javax.swing.JSpinner IScaleSpinner;
    private javax.swing.JCheckBox IShowCheckBox;
    private javax.swing.ButtonGroup NativeOrCalcButtonGroup;
    private javax.swing.JLabel NativeOrCalcLabel;
    private javax.swing.JPanel NativeOrCalcPanel;
    private javax.swing.JRadioButton NativeRadioButton;
    private javax.swing.JButton OKButton;
    private javax.swing.JLabel RecordedNativeHelpLabel;
    private javax.swing.ButtonGroup RemoveAverageButtonGroup;
    private javax.swing.JLabel ScaleTitleLabel;
    private javax.swing.JSeparator Separator1;
    private javax.swing.JSeparator Separator2;
    private javax.swing.JSeparator Separator3;
    private javax.swing.JSeparator Separator4;
    private javax.swing.JButton XColourChangeButton;
    private javax.swing.JLabel XColourExampleLabel;
    private javax.swing.JLabel XLabel;
    private javax.swing.JSpinner XScaleSpinner;
    private javax.swing.JCheckBox XShowCheckBox;
    private javax.swing.JButton YColourChangeButton;
    private javax.swing.JLabel YColourExampleLabel;
    private javax.swing.JLabel YLabel;
    private javax.swing.JSpinner YScaleSpinner;
    private javax.swing.JCheckBox YShowCheckBox;
    private javax.swing.JButton ZColourChangeButton;
    private javax.swing.JLabel ZColourExampleLabel;
    private javax.swing.JLabel ZLabel;
    private javax.swing.JSpinner ZScaleSpinner;
    private javax.swing.JCheckBox ZShowCheckBox;
    // End of variables declaration//GEN-END:variables

    /** find the index of a spinner choice
     * @param spinner the spinner
     * @param list the list of choices in the spinner */
    private int findSpinnerIndex (javax.swing.JSpinner spinner, String list [])
    {
        int count;
        String value;
        
        try
        {
            value = (String) spinner.getValue();
            for (count=0; count<list.length; count++)
            {
                if (value.equals(list[count])) return count;
            }
        }
        catch (ClassCastException e) { }
        
        return 0;
    }
        
    /** set up angle elements spinner for min arc / nT
     * @param new_d_in_min_arc new setting for angles_in_min_arc */
    public void setAngleSpinner (boolean new_angle_in_min_arc)
    {
        int D_index, I_index;
        
        if (angles_in_min_arc)
        {
            D_index = findSpinnerIndex(DScaleSpinner, angular_ranges);
            I_index = findSpinnerIndex(IScaleSpinner, angular_ranges);
        }
        else 
        {
            D_index = findSpinnerIndex(DScaleSpinner, scalar_ranges);
            I_index = findSpinnerIndex(IScaleSpinner, scalar_ranges);
        }
        angles_in_min_arc = new_angle_in_min_arc;
        if (angles_in_min_arc)
        {
            DScaleSpinner.setModel (new javax.swing.SpinnerListModel (angular_ranges));
            DScaleSpinner.setValue (angular_ranges [D_index]);
            IScaleSpinner.setModel (new javax.swing.SpinnerListModel (angular_ranges));
            IScaleSpinner.setValue (angular_ranges [I_index]);
        }
        else
        {
            DScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
            DScaleSpinner.setValue (scalar_ranges [D_index]);
            IScaleSpinner.setModel (new javax.swing.SpinnerListModel (scalar_ranges));
            IScaleSpinner.setValue (scalar_ranges [I_index]);
        }
        ((javax.swing.JSpinner.DefaultEditor) DScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
        ((javax.swing.JSpinner.DefaultEditor) IScaleSpinner.getEditor()).getTextField().setColumns(max_spinner_string_length);
    }

    private void enableShowCheckBoxes (boolean enable)
    {
        XShowCheckBox.setEnabled(enable);
        YShowCheckBox.setEnabled(enable);
        ZShowCheckBox.setEnabled(enable);
        FShowCheckBox.setEnabled(enable);
        HShowCheckBox.setEnabled(enable);
        DShowCheckBox.setEnabled(enable);
        IShowCheckBox.setEnabled(enable);
        FScalarShowCheckBox.setEnabled(enable);
        FDiffShowCheckBox.setEnabled(enable);
    }
    
    private String checkDialogFields ()
    {
        boolean found;
        
        found = false;
        if (XShowCheckBox.isSelected ()) found = true;
        if (YShowCheckBox.isSelected ()) found = true;
        if (ZShowCheckBox.isSelected ()) found = true;
        if (FShowCheckBox.isSelected ()) found = true;
        if (HShowCheckBox.isSelected ()) found = true;
        if (DShowCheckBox.isSelected ()) found = true;
        if (IShowCheckBox.isSelected ()) found = true;
        if (FScalarShowCheckBox.isSelected ()) found = true;
        if (FDiffShowCheckBox.isSelected ()) found = true;
        if (! found) return "No elements are displayed - please select one or more elements to display";
        
        return null;
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // static code below here
    ////////////////////////////////////////////////////////////////////////////
 
    /** get a combo box that contains the combined zoom items (those for
     * field strength and angular units - the combo box includes an
     * action listener that automtically calls the appropriate zoom
     * functions in the CommandInterpreter - all any client needs to
     * do is display it so that the user can select from it
     * @param listen_for_changes if true, install the action listener
     *        that calls the CommandInterpreter when a new zoom item
     *        has been selected - if false don't install a listener
     * @return the combo box - the indices in the combo box can be
     * used as the amount field in the ZoomWithDialog and ZoomWithoutDialog
     * methods */
    public static JComponent getZoomComboBox (boolean listen_for_changes)
    {
        JComboSpinner cb;
        
        cb = new JComboSpinner (combined_ranges);
        cb.getComboBox().setEditable(false);
        cb.getComboBox().setSelectedItem(combined_ranges [findScaleIndex ("All", scales)]);
        if (listen_for_changes)
        {
            cb.getComboBox().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    zoomComboBoxActionPerformed (e);
                }
            } );
        }
        return cb;
    }
    
    private static void zoomComboBoxActionPerformed(ActionEvent e) 
    {
        GlobalObjects.command_interpreter.interpretCommand("zoom_plot&" + Integer.toString (((JComboBox) e.getSource()).getSelectedIndex()));
    }
    
    /** set the currently selected item in a zoom combo box
     * @param cb a JComboSPinner created with getZoomComboBox()
     * @param fire_change_listeners true to alert listeners, false otherwise */
    public static void setZoomComboBoxSelectedItem (JComboSpinner cb, boolean fire_change_listeners)
    {
        int count;
        ActionListener listeners [];
        
        listeners = cb.getComboBox().getActionListeners();
        if (! fire_change_listeners)
        {
            for (count=0; count<listeners.length; count++)
                cb.getComboBox().removeActionListener(listeners[count]);
        }
        cb.getComboBox().setSelectedItem(combined_ranges [findScaleIndex ("All", scales)]);
        if (! fire_change_listeners)
        {
            for (count=0; count<listeners.length; count++)
                cb.getComboBox().addActionListener(listeners[count]);
        }
    }

    /** modify the configuration to zoom a plot, even though the dialog is not up 
     * @param amount for relative zooming, amount = -1 to zoom in, +1 to zoom out 
     *        for absolute zooming, amount = index of zoom item in list to set zoom to
     * @param relative true for relative zooming, false for absolute */
    public static void ZoomWithoutDialog (int amount, boolean relative)
    {
        ZoomComponent ("X", amount, relative);
        ZoomComponent ("Y", amount, relative);
        ZoomComponent ("Z", amount, relative);
        ZoomComponent ("F", amount, relative);
        ZoomComponent ("H", amount, relative);
        ZoomComponent ("D", amount, relative);
        ZoomComponent ("I", amount, relative);
        ZoomComponent ("FScalar", amount, relative);
        ZoomComponent ("FDiff", amount, relative);
        ZoomComponent ("All", amount, relative);
        GlobalObjects.command_interpreter.interpretCommand ("new_plot_options");
    }
    
    /** find whether the D/I scale is in nT or Min Arc */
    public static boolean isAngleScaleInMinArc ()
    {
        if (GlobalObjects.configuration.getProperty("AnglesInMinArc", "1").equals("1")) return true;
        return false;
    }
    
    /** find a scale index value in the program's configuration
     * @param component_name the name of the geomagnetic component */
    public static int getScaleFromConfiguration (String component_name)
    {
        String str;
        
        str = GlobalObjects.configuration.getProperty(component_name + "PlotScale");
        try
        {
            return Integer.parseInt(str);
        }
        catch (NullPointerException e) { }
        catch (NumberFormatException e) { }
        
        return -1;
    }

    /** find a colour in the program's configuration
     * @param component_name the name of the geomagnetic component */
    public static java.awt.Color getColourFromConfiguration (String component_name)
    {
        String str;
        java.awt.Color colour;
        
        str = GlobalObjects.configuration.getProperty(component_name + "PlotColour");
        try
        {
            return new java.awt.Color (Integer.parseInt(str));
        }
        catch (NullPointerException e) { }
        catch (NumberFormatException e) { }
        
        return java.awt.Color.BLUE;
    }

    /** find a components 'shown' state in the program's configuration
     * @param component_name the name of the geomagnetic component */
    public static boolean isShownFromConfiguration (String component_name)
    {
        String str;
        java.awt.Color colour;
        
        str = GlobalObjects.configuration.getProperty(component_name + "IsShown", "True");
        if (str.equalsIgnoreCase("True")) return true;
        if (str.equalsIgnoreCase("T")) return true;
        if (str.equalsIgnoreCase("Yes")) return true;
        if (str.equalsIgnoreCase("Y")) return true;
        if (str.equalsIgnoreCase("1")) return true;
        return false;
    }

    /** find a scale index value in the program's configuration
     * @param component_name the name of the geomagnetic component
     * @param list the list to check in */
    private static int findScaleIndex (String component_name, int list [][])
    {
        int count, value, type_index;
        
        if (component_name.equalsIgnoreCase("D") ||
            component_name.equalsIgnoreCase("I")) type_index = 1;
        else type_index = 0;
        value = getScaleFromConfiguration(component_name);
        for (count=0; count<list.length; count++)
        {
            if (value == list[count] [type_index]) return count;
        }
        
        return 0;
    }

    private static void ZoomComponent (String component_name, int amount, boolean relative)
    {
        int index, type_index;
        
        if (component_name.equalsIgnoreCase("D") ||
            component_name.equalsIgnoreCase("I")) type_index = 1;
        else type_index = 0;
        if (relative)
            index = findScaleIndex (component_name, scales) + amount;
        else
            index = amount;
        if (index < 0) return;
        if (index >= scales.length) return;
        
        GlobalObjects.configuration.setProperty(component_name + "PlotScale", Integer.toString (scales [index] [type_index]));
    }
    
}
