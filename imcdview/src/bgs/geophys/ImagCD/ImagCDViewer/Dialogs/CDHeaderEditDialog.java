/*
 * CDHeaderEditDialog.java
 *
 * Created on 25 January 2009, 11:37
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs;

import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.EditorListener;
import bgs.geophys.library.Data.ImagCD.ImagCDDataDay;
import bgs.geophys.library.Data.ImagCD.ImagCDDataException;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import bgs.geophys.library.Data.ImagCD.ImagCDHeader;
import bgs.geophys.library.Swing.SwingUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  smf
 */
public class CDHeaderEditDialog extends javax.swing.JDialog 
implements ActionListener, DocumentListener
{
    
    /** class to hold one line of widgets in the FilesPanel */
    private class FileWidgets
    {
        private ImagCDHeader cd_header;
        private JLabel title_label;
        private JTextField year_text_field;
        private JButton save_button;
        private JTextField day_number_text_field;
        private FileWidgets (ImagCDHeader cd_header)
        {
            this.cd_header = cd_header;
            title_label = new JLabel (cd_header.getFile().getName());
            year_text_field = new JTextField (Integer.toString (cd_header.getYear()), 5);
            day_number_text_field = new JTextField (Integer.toString (cd_header.getDayNumber()), 5);
            save_button = new JButton ("Save");
        }
        public ImagCDHeader getCDHeader () { return cd_header; }
        public void setCDHeader (ImagCDHeader cd_header) { this.cd_header = cd_header; }
        public JLabel getTitleLabel () { return title_label; }
        public JTextField getYearTextField () { return year_text_field; }
        public JButton getSaveButton () { return save_button; }
        public JTextField getDayNumberTextField () { return day_number_text_field; }
    }
    
    private Vector<EditorListener> editor_listeners;
    private Vector<FileWidgets> file_widgets_list;
    private GridBagLayout files_grid_bag;
    private boolean is_edited;
    
    /** Creates new form CDHeaderEditDialog */
    public CDHeaderEditDialog (java.awt.Frame parent, boolean modal,
                               ImagCDHeader cd_header) 
    {
        super(parent, modal);
        initComponents();
        setupDialog (cd_header);
    }
    
    /** Creates new form CDHeaderEditDialog */
    public CDHeaderEditDialog (java.awt.Dialog parent, boolean modal,
                               ImagCDHeader cd_header) 
    {
        super(parent, modal);
        initComponents();
        setupDialog (cd_header);
    }
    
    /** Creates new form CDHeaderEditDialog */
    public CDHeaderEditDialog (java.awt.Window parent, ModalityType modality_type,
                               ImagCDHeader cd_header) 
    {
        super(parent, modality_type);
        initComponents();
        setupDialog (cd_header);
    }
    
    private void setupDialog (ImagCDHeader cd_header)
    {
        editor_listeners = new Vector<EditorListener> ();
        
        StationIDTextField.setText (cd_header.getStationID());
        ColatitudeTextField.setText (Double.toString ((double) cd_header.getColatitude() / 1000.0));
        LongitudeTextField.setText (Double.toString ((double) cd_header.getLongitude() / 1000.0));
        ElevationTextField.setText (Integer.toString (cd_header.getElevation()));
        RecordedElementsTextField.setText (cd_header.getRecordedElements());
        InstituteTextField.setText (cd_header.getInstituteCode());
        DConversionTextField.setText (Integer.toString (cd_header.getDConversion()));
        QualityCodeTextField.setText (cd_header.getQualityCode());
        InstrumentCodeTextField.setText (cd_header.getInstrumentCode());
        K9LimitTextField.setText (Integer.toString (cd_header.getK9Limit()));
        SamplePeriodTextField.setText (Integer.toString (cd_header.getSamplePeriod()));
        SensorOrientationTextField.setText (cd_header.getSensorOrientation());
        PublicationDateTextField.setText (cd_header.getPublicationDate());
        FormatVersionTextField.setText (cd_header.getFormatVersion());
        HdrReserverWordTextField.setText (Integer.toString (cd_header.getReserved()));
        TrlReserverWordTextField1.setText (Integer.toString (cd_header.getTrailer1()));
        TrlReserverWordTextField2.setText (Integer.toString (cd_header.getTrailer2()));
        TrlReserverWordTextField3.setText (Integer.toString (cd_header.getTrailer3()));
        TrlReserverWordTextField4.setText (Integer.toString (cd_header.getTrailer4()));

        is_edited = false;
        StationIDTextField.getDocument().addDocumentListener(this);
        ColatitudeTextField.getDocument().addDocumentListener(this);
        LongitudeTextField.getDocument().addDocumentListener(this);
        ElevationTextField.getDocument().addDocumentListener(this);
        RecordedElementsTextField.getDocument().addDocumentListener(this);
        InstituteTextField.getDocument().addDocumentListener(this);
        DConversionTextField.getDocument().addDocumentListener(this);
        QualityCodeTextField.getDocument().addDocumentListener(this);
        InstrumentCodeTextField.getDocument().addDocumentListener(this);
        K9LimitTextField.getDocument().addDocumentListener(this);
        SamplePeriodTextField.getDocument().addDocumentListener(this);
        SensorOrientationTextField.getDocument().addDocumentListener(this);
        PublicationDateTextField.getDocument().addDocumentListener(this);
        FormatVersionTextField.getDocument().addDocumentListener(this);
        HdrReserverWordTextField.getDocument().addDocumentListener(this);
        TrlReserverWordTextField1.getDocument().addDocumentListener(this);
        TrlReserverWordTextField2.getDocument().addDocumentListener(this);
        TrlReserverWordTextField3.getDocument().addDocumentListener(this);
        TrlReserverWordTextField4.getDocument().addDocumentListener(this);
        
        files_grid_bag = (GridBagLayout) FilesPanel.getLayout();
        file_widgets_list = new Vector<FileWidgets> ();
        addFile (cd_header);
    }

    /** add an editor listener */
    public void addEditorListener (EditorListener listener)
    {
        editor_listeners.add (listener);
    }
    
    /** remove an editor listener */
    public void removeEditorListener (EditorListener listener)
    {
        editor_listeners.remove (listener);
    }
    
    /** add a file to the list of files being edited */
    public void addFile (ImagCDHeader cd_header)
    {
        int row;
        FileWidgets file_widgets;
        
        file_widgets = new FileWidgets (cd_header);
        row = file_widgets_list.size() +1;  // add one to allow for title row
        file_widgets_list.add (file_widgets);
        SwingUtils.addToGridBag(file_widgets.getTitleLabel(),         FilesPanel, GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1,                            1, 1.0, 1.0, GridBagConstraints.NONE);
        SwingUtils.addToGridBag(file_widgets.getYearTextField(),      FilesPanel, GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1,                            1, 1.0, 1.0, GridBagConstraints.NONE);
        SwingUtils.addToGridBag(file_widgets.getDayNumberTextField(), FilesPanel, GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1,                            1, 1.0, 1.0, GridBagConstraints.NONE);
        SwingUtils.addToGridBag(file_widgets.getSaveButton(),         FilesPanel, GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.NONE);
        file_widgets.getSaveButton().addActionListener(this);
        pack ();
    }
    
    public boolean isEdited () { return is_edited; }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        HeaderPanel = new javax.swing.JPanel();
        HeaderTitleLabel = new javax.swing.JLabel();
        StationIDLabel = new javax.swing.JLabel();
        StationIDTextField = new javax.swing.JTextField();
        ColatitudeLabel = new javax.swing.JLabel();
        ColatitudeTextField = new javax.swing.JTextField();
        ColatitudeUnitsLabel = new javax.swing.JLabel();
        LongitudeLabel = new javax.swing.JLabel();
        LongitudeTextField = new javax.swing.JTextField();
        LongitudeUnitsLabel = new javax.swing.JLabel();
        ElevationLabel = new javax.swing.JLabel();
        ElevationTextField = new javax.swing.JTextField();
        ElevationUnitsLabel = new javax.swing.JLabel();
        RecordedElementsLabel = new javax.swing.JLabel();
        RecordedElementsTextField = new javax.swing.JTextField();
        RecordedElementsHintsLabel = new javax.swing.JLabel();
        InstituteLabel = new javax.swing.JLabel();
        InstituteTextField = new javax.swing.JTextField();
        DConversionLabel = new javax.swing.JLabel();
        DConversionTextField = new javax.swing.JTextField();
        DConversionUnitsLabel = new javax.swing.JLabel();
        QualityCodeLabel = new javax.swing.JLabel();
        QualityCodeTextField = new javax.swing.JTextField();
        InstrumentCodeLabel = new javax.swing.JLabel();
        InstrumentCodeTextField = new javax.swing.JTextField();
        K9LimitLabel = new javax.swing.JLabel();
        K9LimitTextField = new javax.swing.JTextField();
        K9LimitUnitsLabel = new javax.swing.JLabel();
        SamplePeriodLabel = new javax.swing.JLabel();
        SamplePeriodTextField = new javax.swing.JTextField();
        SamplePeriodUnitsLabel = new javax.swing.JLabel();
        SensorOrientationLabel = new javax.swing.JLabel();
        SensorOrientationTextField = new javax.swing.JTextField();
        PublicationDateLabel = new javax.swing.JLabel();
        PublicationDateTextField = new javax.swing.JTextField();
        PublicationDateHint = new javax.swing.JLabel();
        FormatVersionLabel = new javax.swing.JLabel();
        FormatVersionTextField = new javax.swing.JTextField();
        FormatVersionHint = new javax.swing.JLabel();
        HdrReservedWordLabel = new javax.swing.JLabel();
        HdrReserverWordTextField = new javax.swing.JTextField();
        Separator1 = new javax.swing.JSeparator();
        TrailerPanel = new javax.swing.JPanel();
        TrailerTitleLabel = new javax.swing.JLabel();
        TrlReservedWordLabel1 = new javax.swing.JLabel();
        TrlReserverWordTextField1 = new javax.swing.JTextField();
        TrlReservedWordLabel2 = new javax.swing.JLabel();
        TrlReserverWordTextField2 = new javax.swing.JTextField();
        TrlReservedWordLabel3 = new javax.swing.JLabel();
        TrlReserverWordTextField3 = new javax.swing.JTextField();
        TrlReservedWordLabel4 = new javax.swing.JLabel();
        TrlReserverWordTextField4 = new javax.swing.JTextField();
        Separator2 = new javax.swing.JSeparator();
        FilesPanel = new javax.swing.JPanel();
        FilesTitleLabel = new javax.swing.JLabel();
        FileTitleLabel = new javax.swing.JLabel();
        YearTitleLabel = new javax.swing.JLabel();
        DayNumberTitleLabel = new javax.swing.JLabel();
        SaveButtonTitleLabel = new javax.swing.JLabel();
        ButtonPanel = new javax.swing.JPanel();
        SaveAllButton = new javax.swing.JButton();
        CloseButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("INTERMAGNET CD Binary Header Editor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        HeaderPanel.setLayout(new java.awt.GridBagLayout());

        HeaderTitleLabel.setText("<html><h4>Header</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        HeaderPanel.add(HeaderTitleLabel, gridBagConstraints);

        StationIDLabel.setText("Station ID:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(StationIDLabel, gridBagConstraints);

        StationIDTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(StationIDTextField, gridBagConstraints);

        ColatitudeLabel.setText("Colatitude:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(ColatitudeLabel, gridBagConstraints);

        ColatitudeTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(ColatitudeTextField, gridBagConstraints);

        ColatitudeUnitsLabel.setText("(decimal degrees)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(ColatitudeUnitsLabel, gridBagConstraints);

        LongitudeLabel.setText("Longitude:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(LongitudeLabel, gridBagConstraints);

        LongitudeTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(LongitudeTextField, gridBagConstraints);

        LongitudeUnitsLabel.setText("(decimal degrees)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(LongitudeUnitsLabel, gridBagConstraints);

        ElevationLabel.setText("Elevation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(ElevationLabel, gridBagConstraints);

        ElevationTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(ElevationTextField, gridBagConstraints);

        ElevationUnitsLabel.setText("(metres)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(ElevationUnitsLabel, gridBagConstraints);

        RecordedElementsLabel.setText("Recorded elements:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(RecordedElementsLabel, gridBagConstraints);

        RecordedElementsTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(RecordedElementsTextField, gridBagConstraints);

        RecordedElementsHintsLabel.setText("(XYZF, XYZG, HDZF or HDZG)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(RecordedElementsHintsLabel, gridBagConstraints);

        InstituteLabel.setText("Institute code:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(InstituteLabel, gridBagConstraints);

        InstituteTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(InstituteTextField, gridBagConstraints);

        DConversionLabel.setText("D conversion:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(DConversionLabel, gridBagConstraints);

        DConversionTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(DConversionTextField, gridBagConstraints);

        DConversionUnitsLabel.setText("(H/3438*10000, nT)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(DConversionUnitsLabel, gridBagConstraints);

        QualityCodeLabel.setText("Quality code:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(QualityCodeLabel, gridBagConstraints);

        QualityCodeTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(QualityCodeTextField, gridBagConstraints);

        InstrumentCodeLabel.setText("Instrument code:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(InstrumentCodeLabel, gridBagConstraints);

        InstrumentCodeTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(InstrumentCodeTextField, gridBagConstraints);

        K9LimitLabel.setText("K9 limit:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(K9LimitLabel, gridBagConstraints);

        K9LimitTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(K9LimitTextField, gridBagConstraints);

        K9LimitUnitsLabel.setText("(nT)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(K9LimitUnitsLabel, gridBagConstraints);

        SamplePeriodLabel.setText("Sample period:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(SamplePeriodLabel, gridBagConstraints);

        SamplePeriodTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(SamplePeriodTextField, gridBagConstraints);

        SamplePeriodUnitsLabel.setText("(mS)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(SamplePeriodUnitsLabel, gridBagConstraints);

        SensorOrientationLabel.setText("Sensor orientation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(SensorOrientationLabel, gridBagConstraints);

        SensorOrientationTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(SensorOrientationTextField, gridBagConstraints);

        PublicationDateLabel.setText("Publication date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(PublicationDateLabel, gridBagConstraints);

        PublicationDateTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(PublicationDateTextField, gridBagConstraints);

        PublicationDateHint.setText("(YYMM)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(PublicationDateHint, gridBagConstraints);

        FormatVersionLabel.setText("Format version:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(FormatVersionLabel, gridBagConstraints);

        FormatVersionTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(FormatVersionTextField, gridBagConstraints);

        FormatVersionHint.setText("\"1.0\", \"1.1\" or \"2.0\"");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        HeaderPanel.add(FormatVersionHint, gridBagConstraints);

        HdrReservedWordLabel.setText("Reserved word:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        HeaderPanel.add(HdrReservedWordLabel, gridBagConstraints);

        HdrReserverWordTextField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        HeaderPanel.add(HdrReserverWordTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(HeaderPanel, gridBagConstraints);

        Separator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        getContentPane().add(Separator1, gridBagConstraints);

        TrailerPanel.setLayout(new java.awt.GridBagLayout());

        TrailerTitleLabel.setText("<html><h4>Trailer</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        TrailerPanel.add(TrailerTitleLabel, gridBagConstraints);

        TrlReservedWordLabel1.setText("Reserved word 1:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        TrailerPanel.add(TrlReservedWordLabel1, gridBagConstraints);

        TrlReserverWordTextField1.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        TrailerPanel.add(TrlReserverWordTextField1, gridBagConstraints);

        TrlReservedWordLabel2.setText("Reserved word 2:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        TrailerPanel.add(TrlReservedWordLabel2, gridBagConstraints);

        TrlReserverWordTextField2.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        TrailerPanel.add(TrlReserverWordTextField2, gridBagConstraints);

        TrlReservedWordLabel3.setText("Reserved word 3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        TrailerPanel.add(TrlReservedWordLabel3, gridBagConstraints);

        TrlReserverWordTextField3.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        TrailerPanel.add(TrlReserverWordTextField3, gridBagConstraints);

        TrlReservedWordLabel4.setText("Reserved word 4:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        TrailerPanel.add(TrlReservedWordLabel4, gridBagConstraints);

        TrlReserverWordTextField4.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        TrailerPanel.add(TrlReserverWordTextField4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(TrailerPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(Separator2, gridBagConstraints);

        FilesPanel.setLayout(new java.awt.GridBagLayout());

        FilesTitleLabel.setText("<html><h4>Files being edited</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        FilesPanel.add(FilesTitleLabel, gridBagConstraints);

        FileTitleLabel.setText("<html><b>File</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        FilesPanel.add(FileTitleLabel, gridBagConstraints);

        YearTitleLabel.setText("<html><b>Year</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        FilesPanel.add(YearTitleLabel, gridBagConstraints);

        DayNumberTitleLabel.setText("<html><b>Day<br>number</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        FilesPanel.add(DayNumberTitleLabel, gridBagConstraints);

        SaveButtonTitleLabel.setText("<html>&nbsp;</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        FilesPanel.add(SaveButtonTitleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        getContentPane().add(FilesPanel, gridBagConstraints);

        SaveAllButton.setText("Save All");
        SaveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveAllButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(SaveAllButton);

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(CloseButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(ButtonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog ();
    }//GEN-LAST:event_formWindowClosing

    private void SaveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveAllButtonActionPerformed
        int count;
        String errmsg;
        FileWidgets file_widgets;
        
        for (count=0; count<file_widgets_list.size(); count++)
        {
            file_widgets = file_widgets_list.get (count);
            errmsg = saveHeader (file_widgets);
            if (errmsg != null)
                JOptionPane.showMessageDialog (this,
                                               errmsg,
                                               "Error - Not all files have been modified",
                                               JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_SaveAllButtonActionPerformed

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        closeDialog ();
    }//GEN-LAST:event_CloseButtonActionPerformed

    public void actionPerformed(ActionEvent e) 
    {
        int count;
        String errmsg;
        FileWidgets file_widgets;
        
        file_widgets = null;
        for (count=0; count<file_widgets_list.size(); count++)
        {
            file_widgets = file_widgets_list.get (count);
            if (file_widgets.getSaveButton().equals(e.getSource ())) break;
            file_widgets = null;
        }
        if (file_widgets == null)
            JOptionPane.showMessageDialog (this,
                                           "Internal error finding correct file",
                                           "Error",
                                           JOptionPane.ERROR_MESSAGE);
        else
        {
            errmsg = saveHeader (file_widgets);
            if (errmsg != null)
                JOptionPane.showMessageDialog (this,
                                               errmsg,
                                               "Error",
                                               JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void insertUpdate(DocumentEvent e) { is_edited = true; }
    public void removeUpdate(DocumentEvent e) { is_edited = true; }
    public void changedUpdate(DocumentEvent e) { is_edited = true; }
    
    public void closeDialog ()
    {
        int count;
        
        this.setVisible(false);
        this.dispose();
        for (count=0; count<editor_listeners.size(); count++)
            editor_listeners.get(count).editorExited(this);
    }
    
    private String saveHeader (FileWidgets file_widgets)
    {
        int count;
        ImagCDHeader cd_header_clone;
        
        // clone the header and use the clone to check the form data
        cd_header_clone = new ImagCDHeader (file_widgets.getCDHeader());
        try
        {
            cd_header_clone.setStationID(StationIDTextField.getText());
            cd_header_clone.setYear(file_widgets.getYearTextField().getText());
            cd_header_clone.setDaynumber(file_widgets.getDayNumberTextField().getText());
            cd_header_clone.setColatitude(ColatitudeTextField.getText());
            cd_header_clone.setLongitude(LongitudeTextField.getText());
            cd_header_clone.setElevation(ElevationTextField.getText());
            cd_header_clone.setRecordedElements(RecordedElementsTextField.getText());
            cd_header_clone.setInstituteCode(InstituteTextField.getText());
            cd_header_clone.setDConversion(DConversionTextField.getText());
            cd_header_clone.setQualityCode(QualityCodeTextField.getText());
            cd_header_clone.setInstrumentCode(InstrumentCodeTextField.getText());
            cd_header_clone.setK9Limit(K9LimitTextField.getText());
            cd_header_clone.setSamplePeriod(SamplePeriodTextField.getText());
            cd_header_clone.setSensorOrientation(SensorOrientationTextField.getText ());
            cd_header_clone.setPublicationDate(PublicationDateTextField.getText ());
            cd_header_clone.setFormatVersion(FormatVersionTextField.getText ());
            cd_header_clone.setReserved(HdrReserverWordTextField.getText ());
            cd_header_clone.setTrailer1(TrlReserverWordTextField1.getText ());
            cd_header_clone.setTrailer2(TrlReserverWordTextField2.getText ());
            cd_header_clone.setTrailer3(TrlReserverWordTextField3.getText ());
            cd_header_clone.setTrailer4(TrlReserverWordTextField4.getText ());
        }
        catch (ImagCDDataException e)
        {
            return e.getMessage();
        }
        
        try
        {
            cd_header_clone.write();
            file_widgets.setCDHeader(cd_header_clone);
            for (count=0; count<editor_listeners.size(); count++)
                editor_listeners.get(count).savedFile(this, cd_header_clone.getFile());
        }
        catch (FileNotFoundException e)
        {
            return "Unable to open file: " + cd_header_clone.getFile().getAbsolutePath();
        }
        catch (IOException e)
        {
            return "Error writing to file: " + cd_header_clone.getFile().getAbsolutePath();
        }
        
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton CloseButton;
    private javax.swing.JLabel ColatitudeLabel;
    private javax.swing.JTextField ColatitudeTextField;
    private javax.swing.JLabel ColatitudeUnitsLabel;
    private javax.swing.JLabel DConversionLabel;
    private javax.swing.JTextField DConversionTextField;
    private javax.swing.JLabel DConversionUnitsLabel;
    private javax.swing.JLabel DayNumberTitleLabel;
    private javax.swing.JLabel ElevationLabel;
    private javax.swing.JTextField ElevationTextField;
    private javax.swing.JLabel ElevationUnitsLabel;
    private javax.swing.JLabel FileTitleLabel;
    private javax.swing.JPanel FilesPanel;
    private javax.swing.JLabel FilesTitleLabel;
    private javax.swing.JLabel FormatVersionHint;
    private javax.swing.JLabel FormatVersionLabel;
    private javax.swing.JTextField FormatVersionTextField;
    private javax.swing.JLabel HdrReservedWordLabel;
    private javax.swing.JTextField HdrReserverWordTextField;
    private javax.swing.JPanel HeaderPanel;
    private javax.swing.JLabel HeaderTitleLabel;
    private javax.swing.JLabel InstituteLabel;
    private javax.swing.JTextField InstituteTextField;
    private javax.swing.JLabel InstrumentCodeLabel;
    private javax.swing.JTextField InstrumentCodeTextField;
    private javax.swing.JLabel K9LimitLabel;
    private javax.swing.JTextField K9LimitTextField;
    private javax.swing.JLabel K9LimitUnitsLabel;
    private javax.swing.JLabel LongitudeLabel;
    private javax.swing.JTextField LongitudeTextField;
    private javax.swing.JLabel LongitudeUnitsLabel;
    private javax.swing.JLabel PublicationDateHint;
    private javax.swing.JLabel PublicationDateLabel;
    private javax.swing.JTextField PublicationDateTextField;
    private javax.swing.JLabel QualityCodeLabel;
    private javax.swing.JTextField QualityCodeTextField;
    private javax.swing.JLabel RecordedElementsHintsLabel;
    private javax.swing.JLabel RecordedElementsLabel;
    private javax.swing.JTextField RecordedElementsTextField;
    private javax.swing.JLabel SamplePeriodLabel;
    private javax.swing.JTextField SamplePeriodTextField;
    private javax.swing.JLabel SamplePeriodUnitsLabel;
    private javax.swing.JButton SaveAllButton;
    private javax.swing.JLabel SaveButtonTitleLabel;
    private javax.swing.JLabel SensorOrientationLabel;
    private javax.swing.JTextField SensorOrientationTextField;
    private javax.swing.JSeparator Separator1;
    private javax.swing.JSeparator Separator2;
    private javax.swing.JLabel StationIDLabel;
    private javax.swing.JTextField StationIDTextField;
    private javax.swing.JPanel TrailerPanel;
    private javax.swing.JLabel TrailerTitleLabel;
    private javax.swing.JLabel TrlReservedWordLabel1;
    private javax.swing.JLabel TrlReservedWordLabel2;
    private javax.swing.JLabel TrlReservedWordLabel3;
    private javax.swing.JLabel TrlReservedWordLabel4;
    private javax.swing.JTextField TrlReserverWordTextField1;
    private javax.swing.JTextField TrlReserverWordTextField2;
    private javax.swing.JTextField TrlReserverWordTextField3;
    private javax.swing.JTextField TrlReserverWordTextField4;
    private javax.swing.JLabel YearTitleLabel;
    // End of variables declaration//GEN-END:variables
    
}
