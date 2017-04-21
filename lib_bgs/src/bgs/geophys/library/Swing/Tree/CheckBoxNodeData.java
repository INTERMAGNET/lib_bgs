
package bgs.geophys.library.Swing.Tree;

import javax.swing.Icon;
import javax.swing.JTree;

/**
* An interface for a tree node user object, for use with a {@link JTree}, 
* that tracks whether it is checked. If you want to use check boxes in
* a JTree, you must make a class that implements this interface as well
* as the TreeNode interface, then create tree nodes using the object that 
* implements both these interfaces.
*
* @see CheckBoxNodeEditor
* @see CheckBoxNodeRenderer
*/
public interface CheckBoxNodeData 
{

        public boolean isSelected();
        public void setSelected(final boolean checked);
        public String getText();
        public Icon getIcon();
        
}
