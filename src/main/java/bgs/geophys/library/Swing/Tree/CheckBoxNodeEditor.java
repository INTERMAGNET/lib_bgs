package bgs.geophys.library.Swing.Tree;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

/**
* A {@link TreeCellEditor} for check box tree nodes.
*/
public class CheckBoxNodeEditor extends AbstractCellEditor implements
        TreeCellEditor
{

        private final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        private final JTree theTree;
        private CheckBoxNodeData data;

        public CheckBoxNodeEditor(final JTree tree) {
                theTree = tree;
        }

        @Override
        public Object getCellEditorValue() {
            final CheckBoxNodePanel panel = renderer.getPanel();
            if (data == null) throw new NullPointerException ("Missing data");
            data.setSelected(panel.check.isSelected());
            return data;
        }

        @Override
        public boolean isCellEditable(final EventObject event) {
                if (!(event instanceof MouseEvent)) return false;
                final MouseEvent mouseEvent = (MouseEvent) event;

                final TreePath path =
                        theTree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (path == null) return false;

                final Object node = path.getLastPathComponent();
                if (!(node instanceof CheckBoxNodeData)) return false;
                return true;
        }

        @Override
        public Component getTreeCellEditorComponent(final JTree tree,
                final Object value, final boolean selected, final boolean expanded,
                final boolean leaf, final int row)
        {

                if (value instanceof CheckBoxNodeData)
                    data = (CheckBoxNodeData) value;
                else
                    data = null;

                final Component editor =
                        renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf,
                                row, true);

                // editor always selected / focused
                final ItemListener itemListener = new ItemListener() {

                        @Override
                        public void itemStateChanged(final ItemEvent itemEvent) {
                                if (stopCellEditing()) {
                                        fireEditingStopped();
                                }
                        }
                };
                if (editor instanceof CheckBoxNodePanel) {
                        final CheckBoxNodePanel panel = (CheckBoxNodePanel) editor;
                        panel.check.addItemListener(itemListener);
                }

                return editor;
                
        }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        super.addCellEditorListener(l);
    }
        
    @Override
    public void cancelCellEditing () {
        super.cancelCellEditing();
    }
        
    @Override
    public void fireEditingCanceled () {
        super.fireEditingCanceled();
    }

    @Override
    public void fireEditingStopped () {
        super.fireEditingStopped();
    }

    @Override
    public CellEditorListener [] getCellEditorListeners () {
        return super.getCellEditorListeners();
    }
    
    @Override
    public void removeCellEditorListener (CellEditorListener l) {
        super.removeCellEditorListener(l);
    }
    
    @Override
    public boolean shouldSelectCell (EventObject e) {
        return super.shouldSelectCell(e);
    }

    @Override
    public boolean stopCellEditing () {
        theTree.repaint();
        return super.stopCellEditing();
    }
}
