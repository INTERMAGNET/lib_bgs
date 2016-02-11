package bgs.geophys.library.cdf.edit;

// $Id: CDFE_MYEditor.java,v 1.2 2015/11/05 19:54:43 liu Exp $
/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.*;

public class CDFE_MYEditor extends JDialog implements CDFConstants, WindowListener {

    public CDF theCDF;

//    protected CDFEdit cdfedit;
    protected JFrame parent;
    protected File theFile;
    private String filename;
    private String rootfilename;
    private String cdfspec;
    private String logicalFilename;
    protected String ext;
    private boolean saveAsOnClose = false;

    //private String newName = null;
    private File   workingDirectory;

    // The Frame
    public JDialog frame;

    // The width and height of the frame
//    public static int Width = 800;
//    public static int Height = 640;
    private int Width;
    private int Height;

    public static int INITIAL_WIDTH = 300;
    public static int INITIAL_HEIGHT = 100;

    public final static Insets insets0 = new Insets(0,0,0,0);
    public final static Insets insets2 = new Insets(2,2,2,2);
    public final static Insets insets5 = new Insets(5,5,5,5);
    public final static Insets insets10 = new Insets(10,10,10,10);
    public final static Insets insets15 = new Insets(15,15,15,15);
    public final static Insets insets20 = new Insets(20,20,20,20);

    public final static Border emptyBorder0 = new EmptyBorder(0,0,0,0);
    public final static Border emptyBorder2 = new EmptyBorder(2,2,2,2);
    public final static Border emptyBorder5 = new EmptyBorder(5,5,5,5);
    public final static Border emptyBorder10 = new EmptyBorder(10,10,10,10);
    public final static Border emptyBorder15 = new EmptyBorder(15,15,15,15);
    public final static Border emptyBorder20 = new EmptyBorder(20,20,20,20);

    public final static Border etchedBorder2 = new CompoundBorder(
							new EtchedBorder(),
							emptyBorder2);
    public final static Border etchedBorder5 = new CompoundBorder(
							new EtchedBorder(),
							emptyBorder5);
    public final static Border etchedBorder10 = new CompoundBorder(
							new EtchedBorder(),
							emptyBorder10);

    public final static Border raisedBorder = new BevelBorder(BevelBorder.RAISED);
    public final static Border lightLoweredBorder = new BevelBorder(BevelBorder.LOWERED, 
							  Color.white, Color.gray);
    public final static Border loweredBorder = new SoftBevelBorder(BevelBorder.LOWERED);

    public Font defaultFont = new Font("Dialog", Font.PLAIN, 12);
    public Font boldFont = new Font("Dialog", Font.BOLD, 12);
    public Font bigFont = new Font("Dialog", Font.PLAIN, 18);
    public Font bigBoldFont = new Font("Dialog", Font.BOLD, 18);
    public Font reallyBigFont = new Font("Dialog", Font.PLAIN, 18);
    public Font reallyBigBoldFont = new Font("Dialog", Font.BOLD, 24);

    // The panels used in the demo
    public CDFE_AttributePanel gAttrPanel, vAttrPanel;
    public CDFE_VariablePanel variablePanel;

    // Some components
    private JPanel jp;
    public JTabbedPane tabbedPane;
    private JFileChooser chooser;
    protected CDFE_EditCDFSpecAction editCDFAction;
    protected CDFE_EditVariableAction editVarAction;
    protected CDFE_EditAttrEntriesAction editgAttrEntriesAction;
    protected CDFE_EditAttrEntriesAction editvAttrEntriesAction;

    private JMenuItem save, saveAs, close;
    private JMenu fileMenu;
    private JMenu variableMenu;
    private JMenu gAttributeMenu;
    private JMenu vAttributeMenu;

    private static Dimension screenSize;

    protected CDFE_MYEditor instance;
    
    private CDFE_ExitAction exit_action;
    
    static boolean defaultModal = true;

    static final long serialVersionUID = 1L;

    /*******************************************/
    /****** Construct the MYEditor     ********/
    /*******************************************/
    public CDFE_MYEditor(JFrame parent, String cdfspec)
    throws CDFException
    {
	this(parent, cdfspec, defaultModal);
    }

    public CDFE_MYEditor(JFrame parent, String cdfspec, boolean modal) 
    throws CDFException
    {

	super(parent, modal);
	setTitle("CDF: "+cdfspec);
//	System.setProperty("com.apple.macos.useScreenMenuBar", "true");
	System.setProperty("apple.laf.useScreenMenuBar", "true");
	instance = this;
//	this.cdfedit = cdfedit;
	this.parent = parent;
	this.cdfspec = cdfspec;
	frame = this;
//	theCDF = cdfedit.sourceCDF;
        this.theCDF = CDF.open(cdfspec, READONLYoff);
	theFile = new File(cdfspec);

        exit_action = new CDFE_ExitAction( this );

        // Only way to close is to use the buttons
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

	jp = new JPanel(true);
	jp.setName("Main MYEditor Panel");
	jp.setFont(bigFont);
	jp.setLayout(new BorderLayout());
	workingDirectory = new File(System.getProperty("user.dir").toString());

	// Set up the default properties

	chooser = new JFileChooser();
	CDFE_SimpleFileFilter cdfFilter = 
	    new CDFE_SimpleFileFilter("cdf", "Common Data Format Files");
	
	chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	chooser.setFileHidingEnabled(true);
	chooser.addChoosableFileFilter(cdfFilter);
	chooser.setFileFilter(cdfFilter);
	
	chooser.setCurrentDirectory(getWorkingDir());

	// Build a tab pane
	tabbedPane = new JTabbedPane();

	// Add the tab to the center
	jp.add(tabbedPane, BorderLayout.CENTER);

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Width = screenSize.width * 3 / 4;
        Height = screenSize.height * 2 / 3;

  	// Global Attribute Pane
	gAttrPanel = new CDFE_AttributePanel(this, GLOBAL_SCOPE);
	tabbedPane.addTab("Global Attributes", null, gAttrPanel, 
			  "Global Attributes display/editing");

        // Variable Attribute Pane
        vAttrPanel = new CDFE_AttributePanel(this, VARIABLE_SCOPE);
        tabbedPane.addTab("Variable Attributes", null, vAttrPanel, 
			  "Variable Attributes display/editing");

	// Variable Pane
	variablePanel = new CDFE_VariablePanel(this);
	tabbedPane.addTab("Variables", null, variablePanel, 
			  "Variable data display/editing");

	createTabListener();

        // Add a MenuBar
        setJMenuBar(buildMenuBar());

	buildEditFrame();

    }

    JMenuBar buildMenuBar() {
	// MenuBar
	JMenuBar menuBar = new JMenuBar();

	JMenuItem mi;

	// File Menu
	fileMenu = (JMenu) menuBar.add(new JMenu("File"));
	fileMenu.setMnemonic('F');

	save = (JMenuItem) fileMenu.add(new CDFE_SaveAction( this ));
	save.setToolTipText("Save the file and stay current file editing");
	save.setMnemonic(KeyEvent.VK_S);
	save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						 Event.CTRL_MASK));
	save.setEnabled(true);

	saveAs = (JMenuItem) fileMenu.add(new CDFE_SaveAsAction( this ));
	saveAs.setToolTipText("Save the file with a new name");
	saveAs.setMnemonic('A');
	saveAs.setEnabled(true);

        fileMenu.add(new JSeparator());

        editCDFAction = new CDFE_EditCDFSpecAction( this );
        mi = (JMenuItem) fileMenu.add(editCDFAction);
	mi.setToolTipText("Edit the CDF specification");
        mi.setMnemonic(KeyEvent.VK_E);
        mi.setEnabled(true);

        
        mi = (JMenuItem) fileMenu.add(exit_action);
        mi.setToolTipText("Save changes and exit the current file editing");
        mi.setMnemonic(KeyEvent.VK_X);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                 Event.CTRL_MASK));

	fileMenu.setEnabled(true);

        // gAttribute Menu
        gAttributeMenu = (JMenu) menuBar.add(new JMenu("gAttributes"));
        gAttributeMenu.setMnemonic('G');

        mi = (JMenuItem) gAttributeMenu.add(new CDFE_NewAttributeAction(gAttrPanel, 'g'));
        mi.setMnemonic('N');
        mi.setEnabled(true);

        mi = (JMenuItem) gAttributeMenu.add(new CDFE_DeleteAttributeAction(gAttrPanel, 'g'));
        mi.setMnemonic('D');
        mi.setEnabled(false);

        mi = (JMenuItem) gAttributeMenu.add(new CDFE_RenameAttributeAction(gAttrPanel, 'g'));
        mi.setMnemonic('R');
        mi.setEnabled(false);

        gAttributeMenu.add(new JSeparator());

        editgAttrEntriesAction = new CDFE_EditAttrEntriesAction(gAttrPanel, 'g');
        mi = (JMenuItem) gAttributeMenu.add(editgAttrEntriesAction);
        mi.setMnemonic('E');
        mi.setEnabled(false);

	gAttributeMenu.setEnabled(true);

        // vAttribute Menu
        vAttributeMenu = (JMenu) menuBar.add(new JMenu("vAttributes"));
        vAttributeMenu.setMnemonic('V');

        mi = (JMenuItem) vAttributeMenu.add(new CDFE_NewAttributeAction(vAttrPanel, 'v'));
        mi.setMnemonic('N');
        mi.setEnabled(true);

        mi = (JMenuItem) vAttributeMenu.add(new CDFE_DeleteAttributeAction(vAttrPanel, 'v'));
        mi.setMnemonic('D');
        mi.setEnabled(false);

        mi = (JMenuItem) vAttributeMenu.add(new CDFE_RenameAttributeAction(vAttrPanel, 'v'));
        mi.setMnemonic('R');
        mi.setEnabled(false);

        vAttributeMenu.add(new JSeparator());

        editvAttrEntriesAction = new CDFE_EditAttrEntriesAction(vAttrPanel, 'v');
        mi = (JMenuItem) vAttributeMenu.add(editvAttrEntriesAction);
        mi.setMnemonic('E');
        mi.setEnabled(false);

	vAttributeMenu.setEnabled(false);

	// Variable Menu
	variableMenu = (JMenu) menuBar.add(new JMenu("zVariables"));
	variableMenu.setMnemonic('Z');

	mi = (JMenuItem) variableMenu.add(new CDFE_NewVariableAction( this ));
	mi.setMnemonic('N');
	mi.setEnabled(true);
	
	mi = (JMenuItem) variableMenu.add(new CDFE_CopyVariableAction( this ));
	mi.setMnemonic('C');
	mi.setEnabled(false);
	
	mi = (JMenuItem) variableMenu.add(new CDFE_DeleteVariableAction( this ));
	mi.setMnemonic('D');
	mi.setEnabled(false);

	mi = (JMenuItem) variableMenu.add(new CDFE_RenameVariableAction( this ));
 	mi.setMnemonic('R');
	mi.setEnabled(false);

	variableMenu.add(new JSeparator());

	editVarAction = new CDFE_EditVariableAction( this );
	mi = (JMenuItem) variableMenu.add(editVarAction);
 	mi.setMnemonic('E');
	mi.setEnabled(false);

	variableMenu.setEnabled(false);

	return menuBar;
    }

    public void buildEditFrame() {

	// Get the application properties

	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		//System.exit(0);
	    }
	});

	// show the frame
        frame.setSize(Width, Height);
	frame.setLocation(screenSize.width/2 - Width/2,
			  screenSize.height/2 - Height/2);
	frame.getContentPane().setLayout(new BorderLayout());
	frame.getContentPane().add(jp, BorderLayout.CENTER);

	frame.pack();
//	((CDFEdit)getFrame()).getDialog().setVisible(false);
	frame.setVisible(true);

	// Deprecated API - jp.requestDefaultFocus();   
	jp.requestFocus();
	frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public CDFE_MYEditor sharedInstance() {
	return instance;
    }
    
    public Container getRootComponent() {
	return parent;
    }
    
    public JFrame getFrame() {
	return parent;
    }

    public int getEditWidth() {
        return Width;
    }

    public int getEditHeight() {
        return Height;
    }

    ////////////////////////////////////////////////////////////
    //                                                        //
    //                File Handling Methods                   //
    //                                                        //
    ////////////////////////////////////////////////////////////

    public void saveFile(String name) throws CDFException, IOException,
					     InterruptedException {
	// Update the hidden global attributes

	// Save the current variable if any
	variablePanel.saveVariableChanges();

	// Make sure that the selectedVar is now a variable.

	if (name != null) {
	    filename = name;
	    theFile = new File(filename);
	    if (name.toLowerCase().indexOf(".cdf") != -1) {
	      rootfilename  = name.substring(0, name.length() - 4);
	      ext = name.toLowerCase().substring(name.length() - 4);
	    } else {
	      rootfilename  = name;
	      ext = ".cdf";
	    }

	    // Set the title to display the filename
	    getFrame().setTitle("MYEditor: "+theFile.getName());
	}

	// commit changes to working file
	theCDF.save();
	if (!rootfilename.equals(cdfspec)) 
	  copyFile(cdfspec+".cdf", rootfilename+".cdf"); 

	save.setEnabled(true);

	saveAsOnClose = false;

    }

    public void closeFile() throws CDFException, IOException, InterruptedException {

        theCDF.close();

        theCDF = null;
    }

    private void copyFile(String source, String destination) throws IOException {
	// Create a workling file
        File inputFile = new File(source);
        File outputFile = new File(destination);
	
        BufferedInputStream in = new BufferedInputStream(
				   new FileInputStream(inputFile));
        BufferedOutputStream out = new BufferedOutputStream(
				   new FileOutputStream(outputFile));
        int c;
	byte[] tmp = new byte[4096];
	
        while ((c = in.read(tmp, 0, 4096)) != -1)
	    out.write(tmp, 0, c);
	
        in.close();
        out.close();
    }

    public void setWorkingDir(File dir) {
	workingDirectory = dir;
    }

    public File getWorkingDir() {
	return workingDirectory;
    }

    ///////////////////////////////////////
    //                                   //
    //         Utility Methods           //
    //                                   //
    ///////////////////////////////////////
    
    public boolean shouldSaveAs() {
	return saveAsOnClose;
    }

    public String getLogicalFilename() {
	return logicalFilename;
    }

    /**
     * Tab Listener
     */
    private void createTabListener() {
	// add listener to know when we've been shown
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
		if (gAttributeMenu.isPopupMenuVisible()) 
		  gAttributeMenu.setPopupMenuVisible(false);
		else if (vAttributeMenu.isPopupMenuVisible())
		  vAttributeMenu.setPopupMenuVisible(false);
		else if (variableMenu.isPopupMenuVisible())
		  variableMenu.setPopupMenuVisible(false);
                JTabbedPane tab = (JTabbedPane) e.getSource();
                int index = tab.getSelectedIndex();
		if (index == 0) { // global attribute tab
		  gAttributeMenu.setEnabled(true);
		  vAttributeMenu.setEnabled(false);
		  variableMenu.setEnabled(false);
		} else if (index == 1) {
		  vAttributeMenu.setEnabled(true);
		  gAttributeMenu.setEnabled(false);
		  variableMenu.setEnabled(false);
		} else {
		  variableMenu.setEnabled(true);
		  gAttributeMenu.setEnabled(false);
		  vAttributeMenu.setEnabled(false);
		}
                Component currentPage = tab.getComponentAt(index);
            }
        };
        tabbedPane.addChangeListener(changeListener);
    }

    public void setWaitCursor() {
	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void setDefaultCursor() {
	frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public JFileChooser getFileChooser() {
	return chooser;
    }

    public File getFile() {
	return theFile;
    }

    public void enableSaving(boolean newfile) {
	save.setEnabled(!newfile);
	saveAs.setEnabled(true);
	close.setEnabled(true);
    }

    public void enableVarMenu() {
	Component [] mis = variableMenu.getMenuComponents();
	for (int i = 1; i< mis.length; i++)
	    mis[i].setEnabled(true);
    }

    public void enablegAttrMenu() {
        Component [] mis = gAttributeMenu.getMenuComponents();
        for (int i = 1; i< mis.length; i++)
            mis[i].setEnabled(true);
    }

    public void enablevAttrMenu() {
        Component [] mis = vAttributeMenu.getMenuComponents();
        for (int i = 1; i< mis.length; i++)
            mis[i].setEnabled(true);
    }

    public void disableSaving() {
	save.setEnabled(false);
	saveAs.setEnabled(false);
	close.setEnabled(false);
    }

    public void disableVarMenu() {
	Component [] mis = variableMenu.getMenuComponents();
	for (int i = 1; i< mis.length; i++)
	    mis[i].setEnabled(false);
    }

    public void disablegAttrMenu() {
        Component [] mis = gAttributeMenu.getMenuComponents();
        for (int i = 1; i< mis.length; i++)
            mis[i].setEnabled(false);
    }

    public void disablevAttrMenu() {
        Component [] mis = vAttributeMenu.getMenuComponents();    
        for (int i = 1; i< mis.length; i++)    
            mis[i].setEnabled(false);
    }    
    
     public static Dimension getScreenSize() {
	return Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Override
    public void windowOpened(WindowEvent e) { }
    @Override
    public void windowClosing(WindowEvent e) { exit_action.actionPerformed(null);}
    @Override
    public void windowClosed(WindowEvent e) { }
    @Override
    public void windowIconified(WindowEvent e) { }
    @Override
    public void windowDeiconified(WindowEvent e) { }
    @Override
    public void windowActivated(WindowEvent e) { }
    @Override
    public void windowDeactivated(WindowEvent e) { }

}
