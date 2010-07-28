/*
 * ImportStep1.java
 *
 * Created on 27 December 2008, 10:33
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.Import;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.CDHeaderEditDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorPanel.FileLoadException;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.FileEditor.ImagCDFileEditorDialog.CloseOptions;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.ImagCD.ImagCDViewer.Interfaces.EditorListener;
import bgs.geophys.ImagCD.ImagCDViewer.Utils.ImportThread.CDInfo;
import bgs.geophys.library.Data.ImagCD.ImagCDDataException;
import bgs.geophys.library.Data.ImagCD.ImagCDFile;
import bgs.geophys.library.Data.ImagCD.ImagCDFileStats;
import bgs.geophys.library.Data.ImagCD.ImagCDHeader;
import bgs.geophys.library.Misc.DateUtils;
import bgs.geophys.library.Swing.SwingUtils;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author  smf
 */
public class ImportStep4 extends javax.swing.JPanel 
implements WindowListener, EditorListener
{

    // class to allow tree renderer to work out how to display a node
    private class TreeNodeUserObject
    {
        private CDInfo cd_info;
        private String title;
        private boolean is_error;
        public TreeNodeUserObject (boolean empty)
        {
            this.cd_info = null;
            if (empty)
                this.title = "Converting files...";
            else
                this.title = "No files converted";
            this.is_error = true;
        }
        public TreeNodeUserObject (CDInfo cd_info)
        {
            setCDInfo (cd_info);
        }
        public TreeNodeUserObject (String title, boolean is_error)
        {
            this.cd_info = null;
            this.title = title;
            this.is_error = is_error;
        }
        public CDInfo getCDInfo () { return cd_info; }
        public void setCDInfo (CDInfo cd_info)
        {
            this.cd_info = cd_info;
            if (cd_info.getCDFileStats() == null)
            {
                is_error = true;
                this.title = cd_info.getCDFilename().getFilename() + " (no information available)";
            }
            else
            {
                is_error = cd_info.isError();
                if (is_error)
                    this.title = cd_info.getCDFilename().getFilename() + " (has warnings)";
                else
                    this.title = cd_info.getCDFilename().getFilename();
            }        
        }
        public String getTitle () { return title; }
        public void setTitle (String title) { this.title = title; }
        public boolean isError () { return is_error; }
        public void setError (boolean is_error) { this.is_error = is_error; }
        public String toString () 
        {
            String string;
            
            string = "<html>";
            if (is_error) string += "<p color=red>";
            else string += "<p>";
            string += title + "</html>";
            return string; 
        }
    }
    
    /** class to render objects in the files tree */
    private class FilesTreeRenderer extends DefaultTreeCellRenderer
    {
        public FilesTreeRenderer ()
        {
            super ();
        }
        @Override
        public Component getTreeCellRendererComponent (JTree tree, Object value, boolean sel,
                                                       boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
            
            TreeNodeUserObject user_object, user_object2;
            DefaultMutableTreeNode tree_node, node;
            boolean is_error;
            Enumeration children;
            
            tree_node = (DefaultMutableTreeNode) value;
            user_object = (TreeNodeUserObject) tree_node.getUserObject();
            if (tree_node.isLeaf())
            {
                if (user_object.getTitle().startsWith("Converting")) setIcon (GlobalObjects.hour_glass_icon);
                else if (user_object.isError()) setIcon (GlobalObjects.warn_icon);
                else setIcon (GlobalObjects.tick_icon);
            }
            else if (! tree_node.isRoot())
            {
                if (user_object.isError()) setIcon (GlobalObjects.file_warn_icon);
                else setIcon (GlobalObjects.file_icon);
            }
            else
            {
                if (tree_node.getUserObject() == null) setIcon (GlobalObjects.warn_icon);
                else
                {
                    is_error = false;
                    children = tree_node.children();
                    while (children.hasMoreElements() && (! is_error))
                    {
                        node = (DefaultMutableTreeNode) children.nextElement();
                        user_object2 = (TreeNodeUserObject) node.getUserObject();
                        is_error = user_object2.isError();
                    }
                    if (is_error) setIcon (GlobalObjects.folder_warn_icon);
                    else setIcon (GlobalObjects.folder_icon);
                }
            }
            return this;
        }
    }
    
    private DefaultTreeModel tree_model;
    private DefaultMutableTreeNode empty_root_node;
    private DefaultMutableTreeNode no_files_root_node;
    private DefaultMutableTreeNode file_root_node;
    private Vector<ImagCDFileEditorDialog> file_editor_list;
    private Vector<CDHeaderEditDialog> header_editor_list;
    private SimpleDateFormat pub_date_format;
    
    /** Creates new form ImportStep1 */
    public ImportStep4() 
    {
        DefaultTreeSelectionModel sel_model;
        
        pub_date_format = new SimpleDateFormat ("yyMM");
        pub_date_format.setTimeZone(DateUtils.gmtTimeZone);
        
        initComponents();
        empty_root_node = new DefaultMutableTreeNode (new TreeNodeUserObject (true));
        no_files_root_node = new DefaultMutableTreeNode (new TreeNodeUserObject (false));
        file_root_node = null;
        tree_model = new DefaultTreeModel (empty_root_node);
        FilesTree.setModel (tree_model);
        sel_model = new DefaultTreeSelectionModel ();
        sel_model.setSelectionMode(DefaultTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        FilesTree.setSelectionModel(sel_model);
        FilesTree.setShowsRootHandles(true);
        FilesTree.setCellRenderer(new FilesTreeRenderer ());
        file_editor_list = new Vector<ImagCDFileEditorDialog> ();
        header_editor_list = new Vector<CDHeaderEditDialog> ();
    }
    
    public void setTmpDirectory (File dir)
    {
        TmpDirectoryTextField.setText (dir.getAbsolutePath());
    }
    
    public void setPercentComplete (int percent) { ProgressBar.setValue(percent); }
    
    public void setShowNoFiles ()
    {
        tree_model.setRoot(no_files_root_node);
        file_root_node = null;
    }

    public void clearErrors () { ErrorsTextArea.setText (""); }
    public void addToErrors (String msg) { ErrorsTextArea.append(msg + "\n"); }

    public void clearFiles ()
    {
        tree_model.setRoot(empty_root_node);
        file_root_node = null;
    }

    public void addToFiles (CDInfo cd_info)
    {
        DefaultMutableTreeNode file_node;
        
        if (file_root_node == null)
        {
            file_root_node = new DefaultMutableTreeNode (new TreeNodeUserObject ("Files", false));
            tree_model.setRoot(file_root_node);
        }
        
        file_node = new DefaultMutableTreeNode (new TreeNodeUserObject (cd_info));
        tree_model.insertNodeInto (file_node, file_root_node, file_root_node.getChildCount());
        FilesTree.expandRow (0);
        
        updateChildNodes(file_node, cd_info);
    }

    public void enableEditButtons (boolean enable)
    {
        EditHeadersButton.setEnabled(enable);
        EditFilesButton.setEnabled(enable);
    }
    
    /** find out if any of the open editors have edits pending */
    public boolean isEditsPending ()
    {
        int count;
        ImagCDFileEditorDialog file_editor;
        CDHeaderEditDialog header_editor;
        
        reapEditors();
        for (count=0; count<file_editor_list.size(); count++)
        {
            file_editor = file_editor_list.get (count);
            if (file_editor.isEdited()) return true;
        }        
        for (count=0; count<header_editor_list.size(); count++)
        {
            header_editor = header_editor_list.get (count);
            if (header_editor.isEdited()) return true;
        }        
        return false;
    }
    
    /** close any open editors without asking the user if they should be saved */
    public void closeEditors ()
    {
        int count;
        ImagCDFileEditorDialog file_editor;
        CDHeaderEditDialog header_editor;
        
        for (count=0; count<file_editor_list.size(); count++)
        {
            file_editor = file_editor_list.get (count);
            file_editor.closeDialog (CloseOptions.NeverSave, true);
        }        
        
        for (count=0; count<header_editor_list.size(); count++)
        {
            header_editor = header_editor_list.get (count);
            header_editor.closeDialog();
        }        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        TitleLabel = new javax.swing.JLabel();
        ConversionPanel = new javax.swing.JPanel();
        SplitPane = new javax.swing.JSplitPane();
        ErrorsScrollPane = new javax.swing.JScrollPane();
        ErrorsTextArea = new javax.swing.JTextArea();
        FilesScrollPane = new javax.swing.JScrollPane();
        FilesTree = new javax.swing.JTree();
        StatusPanel = new javax.swing.JPanel();
        TmpDirectoryLabel = new javax.swing.JLabel();
        TmpDirectoryTextField = new javax.swing.JTextField();
        EditHeadersButton = new javax.swing.JButton();
        ProgressLabel = new javax.swing.JLabel();
        ProgressBar = new javax.swing.JProgressBar();
        EditFilesButton = new javax.swing.JButton();
        InstructionsLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        TitleLabel.setText("<html><h2>Step 4 of 6: Convert data</h2><html>"); // NOI18N
        add(TitleLabel, java.awt.BorderLayout.NORTH);

        ConversionPanel.setLayout(new java.awt.BorderLayout());

        SplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        ErrorsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Conversion errors"));

        ErrorsTextArea.setColumns(20);
        ErrorsTextArea.setEditable(false);
        ErrorsTextArea.setRows(5);
        ErrorsScrollPane.setViewportView(ErrorsTextArea);

        SplitPane.setLeftComponent(ErrorsScrollPane);

        FilesScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Converted files"));

        FilesTree.setVisibleRowCount(13);
        FilesScrollPane.setViewportView(FilesTree);

        SplitPane.setRightComponent(FilesScrollPane);

        ConversionPanel.add(SplitPane, java.awt.BorderLayout.PAGE_START);

        StatusPanel.setLayout(new java.awt.GridBagLayout());

        TmpDirectoryLabel.setText("Data being created in:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        StatusPanel.add(TmpDirectoryLabel, gridBagConstraints);

        TmpDirectoryTextField.setColumns(40);
        TmpDirectoryTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        StatusPanel.add(TmpDirectoryTextField, gridBagConstraints);

        EditHeadersButton.setText("Edit selected file headers");
        EditHeadersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditHeadersButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        StatusPanel.add(EditHeadersButton, gridBagConstraints);

        ProgressLabel.setText("Progress:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        StatusPanel.add(ProgressLabel, gridBagConstraints);

        ProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        StatusPanel.add(ProgressBar, gridBagConstraints);

        EditFilesButton.setText("Edit selected files");
        EditFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditFilesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        StatusPanel.add(EditFilesButton, gridBagConstraints);

        ConversionPanel.add(StatusPanel, java.awt.BorderLayout.PAGE_END);

        add(ConversionPanel, java.awt.BorderLayout.CENTER);

        InstructionsLabel.setText("<html>  <h3>Information</h3>  \nThe conversion to INTERMAGNET archive  format is taking place. Progress is shown above. Once the conversion is complete<br> \na list will show any errors that occurred during the conversion. A second list will show the converted files and describe any<br>\nmissing elements. Select any of the items in the list of files, then press \"Edit selected file headers\" to change the header<br>\nand trailer data. The \"Edit selected files\" button allows you to view and edit any part of the selected file.\n</html>"); // NOI18N
        add(InstructionsLabel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void EditHeadersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditHeadersButtonActionPerformed
        int count;
        File file;
        String errmsg;
        Vector<CDInfo> file_list;
        ImagCDHeader header_list [];
        CDHeaderEditDialog header_edit;
                
        // find the selected files
        file_list = listSelectedFiles ();
        if (file_list == null)
        {
            JOptionPane.showMessageDialog(this, "Select some files to edit.", "No files selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // load headers for each of them
        header_list = new ImagCDHeader [file_list.size ()];
        for (count=0; count<file_list.size(); count++)
        {
            errmsg = null;
            file = file_list.get(count).getCDFile();
            try { header_list [count] = new ImagCDHeader (file, false); }
            catch (FileNotFoundException e) { errmsg = "Unable to find file: " + file.getAbsolutePath(); }
            catch (IOException e) { errmsg = "Error reading file: " + file.getAbsolutePath(); }
            catch (ImagCDDataException e)
            {
                if (e.getMessage() != null) errmsg = e.getMessage() + ": " + file.getAbsolutePath();
                else errmsg = "Error interpreting file: " + file.getAbsolutePath();
            }
            if (errmsg != null)
            {
                JOptionPane.showMessageDialog(this, errmsg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // launch an editor
        header_edit = new CDHeaderEditDialog (SwingUtilities.getWindowAncestor(this),
                                              ModalityType.MODELESS, 
                                              header_list[0]);
        header_edit.addEditorListener(this);
        for (count=1; count<header_list.length; count++)
            header_edit.addFile(header_list[count]);
        header_edit.setVisible(true);
        header_editor_list.add (header_edit);
    }//GEN-LAST:event_EditHeadersButtonActionPerformed

    private void EditFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditFilesButtonActionPerformed
        int count, count2;
        boolean found;
        Vector<CDInfo> file_list;
        ImagCDFileEditorDialog file_editor;
        CDInfo cd_info;
        
        // find the selected files
        file_list = listSelectedFiles ();
        if (file_list == null)
        {
            JOptionPane.showMessageDialog(this, "Select some files to edit.", "No files selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // for each file
        for (count=0; count<file_list.size(); count++)
        {
            cd_info = file_list.get(count);

            // see if there is already an editor for this file
            found = false;
            for (count2=0; count2<file_editor_list.size(); count2++)
            {
                file_editor = file_editor_list.get (count2);
                if (SwingUtils.isDialogDisplayable(file_editor) &&
                    file_editor.getDataFile().getOpenFile().equals (cd_info.getCDFile()))
                {
                    file_editor.setVisible(true);
                    found = true;
                }
            }
        
            // create a new editor for the file
            if (! found)
            {
                try
                {
                    file_editor = new ImagCDFileEditorDialog (SwingUtilities.getWindowAncestor(this),
                                                              ModalityType.MODELESS, 
                                                              cd_info.getCDFile(),
                                                              true);
                    file_editor.addEditorListener(this);
                    file_editor.addWindowListener(this);
                    file_editor_list.add (file_editor);
                }
                catch (FileLoadException e)
                {
                    JOptionPane.showMessageDialog (this, 
                                                   "Unable to read file: " + e.getMessage(),
                                                   "Error",
                                                   JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_EditFilesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ConversionPanel;
    private javax.swing.JButton EditFilesButton;
    private javax.swing.JButton EditHeadersButton;
    private javax.swing.JScrollPane ErrorsScrollPane;
    private javax.swing.JTextArea ErrorsTextArea;
    private javax.swing.JScrollPane FilesScrollPane;
    private javax.swing.JTree FilesTree;
    private javax.swing.JLabel InstructionsLabel;
    private javax.swing.JProgressBar ProgressBar;
    private javax.swing.JLabel ProgressLabel;
    private javax.swing.JSplitPane SplitPane;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JLabel TitleLabel;
    private javax.swing.JLabel TmpDirectoryLabel;
    private javax.swing.JTextField TmpDirectoryTextField;
    // End of variables declaration//GEN-END:variables

    public void windowOpened(WindowEvent e) { }
    public void windowClosing(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { reapEditors (); }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    
    public void savedFile(Object editor, File file) 
    {
        Enumeration children;
        DefaultMutableTreeNode tree_node;
        TreeNodeUserObject user_object;
        CDInfo cd_info;
        ImagCDFile cd_file;
        String errmsg;

        // find the file
        cd_info = null;
        tree_node = null;
        if (file_root_node != null) 
        {
            children = file_root_node.children();
            while (children.hasMoreElements())
            {
                tree_node = (DefaultMutableTreeNode) children.nextElement();
                user_object = (TreeNodeUserObject) tree_node.getUserObject();
                cd_info = user_object.getCDInfo();
                if (cd_info.getCDFile().equals(file)) break;
                cd_info = null;
            }
        }
        
        // update the file
        if (cd_info != null)
        {
            cd_file = new ImagCDFile ();
            errmsg = cd_file.loadFromFile(file);
            if (errmsg != null)
                JOptionPane.showMessageDialog (this, 
                                               file.getName() + ": " + errmsg,
                                               "Unable to recalculate statistics",
                                               JOptionPane.ERROR_MESSAGE);
            else
            {
                cd_info.calcCDFileStats(cd_file);
                if (tree_node != null)
                    updateChildNodes(tree_node, cd_info);
            }
        }
    }

    public void editorExited(Object editor) 
    {
        reapEditors();
    }
    
    private void reapEditors ()
    {
        int count;
        ImagCDFileEditorDialog file_editor;
        CDHeaderEditDialog header_editor;
        
        for (count=0; count<header_editor_list.size(); count++)
        {
            header_editor = header_editor_list.get (count);
            if (! SwingUtils.isDialogDisplayable(header_editor))
                header_editor_list.remove(header_editor);
        }        
        for (count=0; count<file_editor_list.size(); count++)
        {
            file_editor = file_editor_list.get (count);
            if (! SwingUtils.isDialogDisplayable(file_editor))
                file_editor_list.remove(file_editor);
        }        
    }
    
    // get a list of files that are selected in the file tree
    private Vector<CDInfo> listSelectedFiles ()
    {
        int count, count2;
        boolean root_node_selected;
        TreePath tree_paths [];
        DefaultMutableTreeNode tree_node;
        Vector<CDInfo> file_list;
        Enumeration children;
        CDInfo cd_info, cd_info2;
        TreeNodeUserObject user_object;
        
        // find the selection
        tree_paths = FilesTree.getSelectionPaths();
        if (tree_paths == null) return null;
        file_list = new Vector<CDInfo> ();
        
        // check for the root node selected - this effectively means all files are selected
        root_node_selected = false;
        for (count=0; count<tree_paths.length; count++)
        {
            tree_node = (DefaultMutableTreeNode) tree_paths[count].getLastPathComponent();
            if (tree_node.equals (file_root_node)) root_node_selected = true;
        }
        if (root_node_selected)
        {
            children = file_root_node.children();
            while (children.hasMoreElements())
            {
                tree_node = (DefaultMutableTreeNode) children.nextElement();
                user_object = (TreeNodeUserObject) tree_node.getUserObject();
                cd_info = user_object.getCDInfo();
                if (cd_info != null) file_list.add (cd_info);
            }
            return file_list;
        }
        
        // for each selected path ...
        for (count=0; count<tree_paths.length; count++)
        {
            tree_node = (DefaultMutableTreeNode) tree_paths[count].getLastPathComponent();
            
            // work up the path until a node is found that has a user object
            // that contains a CDInfo object
            cd_info = null;
            do
            {
                if (! (tree_node.getUserObject() instanceof TreeNodeUserObject))
                    tree_node = null;
                else
                {
                    user_object = (TreeNodeUserObject) tree_node.getUserObject();
                    cd_info = user_object.getCDInfo();
                    tree_node = (DefaultMutableTreeNode) tree_node.getParent();
                }
            } while ((cd_info == null) && (tree_node != null));
            
            // check that the info isn't already in the list
            for (count2=0; (count2<file_list.size()) && (cd_info != null); count2++)
            {
                cd_info2 = file_list.get(count2);
                if (cd_info.equals (cd_info2))
                    cd_info = null;
            }
            
            // add the file to the list
            if (cd_info != null) file_list.add (cd_info);
        }
        
        return file_list;
    }

    private void updateChildNodes (DefaultMutableTreeNode file_node, CDInfo cd_info)
    {
        String pub_date_string;
        DefaultMutableTreeNode header_node, data_node;
        ImagCDFileStats file_stats;
        boolean file_node_expanded, header_node_expanded, data_node_expanded;
        
        ((TreeNodeUserObject) file_node.getUserObject()).setCDInfo(cd_info);
        file_node_expanded = isNodeExpanded(file_node);
        header_node_expanded = data_node_expanded = false;

        file_stats = cd_info.getCDFileStats();
        if (file_stats == null)
        {
            removeAllChildNodes (file_node);
            header_node = data_node = null;
        }
        else
        {
            if (file_node.getChildCount() == 2)
            {
                header_node = (DefaultMutableTreeNode) file_node.getChildAt(0);
                data_node = (DefaultMutableTreeNode) file_node.getChildAt(1);
                header_node_expanded = isNodeExpanded(header_node);
                data_node_expanded = isNodeExpanded(data_node);
                if (file_stats.isHeaderError())
                    setNodeState (header_node, "Header (has warnings)", true);
                else
                    setNodeState (header_node, "Header", false);
                if (file_stats.isDataError())
                    setNodeState (data_node, "Data (has warnings)", true);
                else
                    setNodeState (data_node, "Data", true);
            }
            else
            {
                removeAllChildNodes (file_node);
                if (file_stats.isHeaderError())
                    header_node = new DefaultMutableTreeNode (new TreeNodeUserObject ("Header (has warnings)", true));
                else
                    header_node = new DefaultMutableTreeNode (new TreeNodeUserObject ("Header", false));
                if (file_stats.isDataError())
                    data_node = new DefaultMutableTreeNode (new TreeNodeUserObject ("Data (has warnings)", true));
                else
                    data_node = new DefaultMutableTreeNode (new TreeNodeUserObject ("Data", false));
                tree_model.insertNodeInto (header_node, file_node, file_node.getChildCount());
                tree_model.insertNodeInto (data_node, file_node, file_node.getChildCount());
            }

            if (file_stats.getPublicationDate() == null)
                pub_date_string = "";
            else
                pub_date_string = pub_date_format.format (file_stats.getPublicationDate());
            if (header_node.getChildCount() != 14)
            {
                removeAllChildNodes(header_node);
                tree_model.insertNodeInto (createNode ("Station", file_stats.getStationID(), file_stats.getStationIDErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Dates", (file_stats.getDateErrmsg() == null) ? "OK" : "Error", file_stats.getDateErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Colatitude", (double) (file_stats.getColatitude()) / 1000.0, "degrees", file_stats.getColatitudeErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Longitude", (double) (file_stats.getLongitude()) / 1000.0, "degrees", file_stats.getLongitudeErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Elevation", file_stats.getElevation(), "meters", file_stats.getElevationErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Data orientation", file_stats.getOrientation(), file_stats.getOrientationErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Institute", file_stats.getInstitute(), file_stats.getInstituteErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("D conversion", file_stats.getDConversion(), "(H/3438*10000 nT)", file_stats.getDConversionErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Quality code", file_stats.getQualityCode(), file_stats.getQualityCodeErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Instrument", file_stats.getInstrument(), file_stats.getInstrumentErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("K9 limit", file_stats.getK9(), "nT", file_stats.getK9Errmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Sample rate", file_stats.getSampRate(), "ms", file_stats.getSampRateErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Sensor orientation", file_stats.getSensorOrient(), file_stats.getSensorOrientErrmsg()), header_node, header_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Publication date", pub_date_string, file_stats.getPublicationDateErrmsg()), header_node, header_node.getChildCount());
            }
            else
            {
                setNodeState (header_node.getChildAt(0), "Station", file_stats.getStationID(), file_stats.getStationIDErrmsg());
                setNodeState (header_node.getChildAt(1), "Dates", (file_stats.getDateErrmsg() == null) ? "OK" : "Error", file_stats.getDateErrmsg());
                setNodeState (header_node.getChildAt(2), "Colatitude", (double) (file_stats.getColatitude()) / 1000.0, "degrees", file_stats.getColatitudeErrmsg());
                setNodeState (header_node.getChildAt(3), "Longitude", (double) (file_stats.getLongitude()) / 1000.0, "degrees", file_stats.getLongitudeErrmsg());
                setNodeState (header_node.getChildAt(4), "Elevation", file_stats.getElevation(), "meters", file_stats.getElevationErrmsg());
                setNodeState (header_node.getChildAt(5), "Data orientation", file_stats.getOrientation(), file_stats.getOrientationErrmsg());
                setNodeState (header_node.getChildAt(6), "Institute", file_stats.getInstitute(), file_stats.getInstituteErrmsg());
                setNodeState (header_node.getChildAt(7), "D conversion", file_stats.getDConversion(), "(H/3438*10000 nT)", file_stats.getDConversionErrmsg());
                setNodeState (header_node.getChildAt(8), "Quality code", file_stats.getQualityCode(), file_stats.getQualityCodeErrmsg());
                setNodeState (header_node.getChildAt(9), "Instrument", file_stats.getInstrument(), file_stats.getInstrumentErrmsg());
                setNodeState (header_node.getChildAt(10), "K9 limit", file_stats.getK9(), "nT", file_stats.getK9Errmsg());
                setNodeState (header_node.getChildAt(11), "Sample rate", file_stats.getSampRate(), "ms", file_stats.getSampRateErrmsg());
                setNodeState (header_node.getChildAt(12), "Sensor orientation", file_stats.getSensorOrient(), file_stats.getSensorOrientErrmsg());
                setNodeState (header_node.getChildAt(13), "Publication date", pub_date_string, file_stats.getPublicationDateErrmsg());
            }
        
            if (data_node.getChildCount() != 7)
            {
                removeAllChildNodes(data_node);
                tree_model.insertNodeInto (createNode ("Vector minute samples", file_stats.getNVectorMinuteSamples(), file_stats.getExpectedVectorMinuteSamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Scalar minute samples", file_stats.getNScalarMinuteSamples(), file_stats.getExpectedScalarMinuteSamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Vector hour samples", file_stats.getNVectorHourSamples(), file_stats.getExpectedVectorHourSamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Scalar hour samples", file_stats.getNScalarHourSamples(), file_stats.getExpectedScalarHourSamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Vector day samples", file_stats.getNVectorDaySamples(), file_stats.getExpectedVectorDaySamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("Scalar day samples", file_stats.getNScalarDaySamples(), file_stats.getExpectedScalarDaySamples()), data_node, data_node.getChildCount());
                tree_model.insertNodeInto (createNode ("K indices", file_stats.getNKIndices(), file_stats.getExpectedKIndices()), data_node, data_node.getChildCount());
            }
            else
            {
                setNodeState (data_node.getChildAt(0), "Vector minute samples", file_stats.getNVectorMinuteSamples(), file_stats.getExpectedVectorMinuteSamples());
                setNodeState (data_node.getChildAt(1), "Scalar minute samples", file_stats.getNScalarMinuteSamples(), file_stats.getExpectedScalarMinuteSamples());
                setNodeState (data_node.getChildAt(2), "Vector hour samples", file_stats.getNVectorHourSamples(), file_stats.getExpectedVectorHourSamples());
                setNodeState (data_node.getChildAt(3), "Scalar hour samples", file_stats.getNScalarHourSamples(), file_stats.getExpectedScalarHourSamples());
                setNodeState (data_node.getChildAt(4), "Vector day samples", file_stats.getNVectorDaySamples(), file_stats.getExpectedVectorDaySamples());
                setNodeState (data_node.getChildAt(5), "Scalar day samples", file_stats.getNScalarDaySamples(), file_stats.getExpectedScalarDaySamples());
                setNodeState (data_node.getChildAt(6), "K indices", file_stats.getNKIndices(), file_stats.getExpectedKIndices());
            }
        }
        
        tree_model.reload (file_node);
        expandNode (file_node, file_node_expanded);
        if (file_node_expanded)
        {
            if (header_node != null) expandNode (header_node, header_node_expanded);
            if (data_node != null) expandNode (data_node, data_node_expanded);
        }
    }

    private MutableTreeNode createNode (String title, int value, String units, String errmsg)
    {
        return createNode (title, Integer.toString (value) + " " + units, errmsg);
    }
    
    private MutableTreeNode createNode (String title, double value, String units, String errmsg)
    {
        return createNode (title, Double.toString (value) + " " + units, errmsg);
    }
    
    private MutableTreeNode createNode (String title, String value, String errmsg)
    {
        if (errmsg == null) return new DefaultMutableTreeNode (new TreeNodeUserObject (title + ": "+ value, false));
        return new DefaultMutableTreeNode (new TreeNodeUserObject (title + ": " + value + " (" + errmsg + ")", true));
    }

    private MutableTreeNode createNode (String title, int n_values, int expected_values)
    {
        int percent_complete;
        
        percent_complete = (n_values * 100) / expected_values;
        if (percent_complete == 100)
            return new DefaultMutableTreeNode (new TreeNodeUserObject (title + " " + Integer.toString(percent_complete) + "% complete", false));
        return new DefaultMutableTreeNode (new TreeNodeUserObject (title + " " + Integer.toString(percent_complete) + "% complete", true));
    }

    private void setNodeState (DefaultMutableTreeNode node, String title, boolean is_error)
    {
        TreeNodeUserObject user_object;
        
        user_object = (TreeNodeUserObject) node.getUserObject();
        user_object.setTitle(title);
        user_object.setError(is_error);
    }

    private void setNodeState (TreeNode node, String title, int value, String units, String errmsg)
    {
        setNodeState (node, title, Integer.toString(value) + " " + units, errmsg);
    }
    
    private void setNodeState (TreeNode node, String title, double value, String units, String errmsg)
    {
        setNodeState (node, title, Double.toString(value) + " " + units, errmsg);
    }
    
    private void setNodeState (TreeNode node, String title, String value, String errmsg)
    {
        if (errmsg == null) 
            setNodeState ((DefaultMutableTreeNode) node, title + ": "+ value, false);
        else
            setNodeState ((DefaultMutableTreeNode) node, title + ": " + value + " (" + errmsg + ")", true);
        
    }
    
    private void setNodeState (TreeNode node, String title, int n_values, int expected_values)
    {
        int percent_complete;
        
        percent_complete = (n_values * 100) / expected_values;
        if (percent_complete == 100)
            setNodeState ((DefaultMutableTreeNode) node, title + " " + Integer.toString(percent_complete) + "% complete", false);
        else
            setNodeState ((DefaultMutableTreeNode) node, title + " " + Integer.toString(percent_complete) + "% complete", true);
    }
    
    private void removeAllChildNodes (MutableTreeNode parent)
    {
        while (parent.getChildCount() > 0)
        {
            tree_model.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
        }
    }
    
    private boolean isNodeExpanded (TreeNode node)
    {
        TreePath tree_path;
        
        tree_path = findTreePath (node);
        if (tree_path == null) return false;
        return FilesTree.isExpanded(tree_path);
    }
    
    private void expandNode (TreeNode node, boolean node_expanded) 
    {
        TreePath tree_path;
        
        tree_path = findTreePath (node);
        if (tree_path != null)
        {
            if (node_expanded) FilesTree.expandPath(tree_path);
            else FilesTree.collapsePath(tree_path);
        }
    }
    
    private TreePath findTreePath (TreeNode node)
    {
        TreePath tree_path;
        TreeNode node_path [];
        
        if (node == null) node_path = null;
        else node_path = tree_model.getPathToRoot(node);
        
        if (node_path == null) tree_path = null;
        else if (node_path.length <= 0) tree_path = null;
        else tree_path = new TreePath (node_path);
        
        return tree_path;
    }
}
