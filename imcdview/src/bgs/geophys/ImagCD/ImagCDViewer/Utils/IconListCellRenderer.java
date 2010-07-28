/*
 * IconListCellRenderer.java
 *
 * Created on 06 June 2002, 22:14
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;

/**
 * A cell renderer for JList that will render Icons for default_selected / de-default_selected items
 * @author  Simon
 * @version 
 */
public class IconListCellRenderer extends JLabel implements ListCellRenderer
{
    /** an interface thant an object can support if it wants to
     * set its own icon */
    public interface IconTeller
    {
        public ImageIcon getIcon (boolean is_selected);
    }
  
  /** a class to tie an image to a data type */
  private class TypeImage
  {
    private Class type; 
    private ImageIcon selected;
    private ImageIcon de_selected;
    public TypeImage (Class type, ImageIcon selected, ImageIcon de_selected)
    {
      this.type = type;
      this.selected = selected;
      this.de_selected = de_selected;
    }
    public boolean typesMatch (Object test_obj) 
    { 
      if (type.equals(test_obj.getClass())) return true;
      return false;
    }
    public ImageIcon getImage (boolean is_selected)
    {
      if (is_selected) return selected;
      return de_selected;
    }
  }
    
  private ImageIcon default_selected;
  private ImageIcon default_de_selected;
  private Vector<TypeImage> type_image_list;
  
  /** create a new IconListCellRenderer, setting the default icons 
   * that will be used for default_selected and unselected list items - non
   * default icons can be added using the addTypeImage() method - as an
   * alternative, the values that the list holds can support the IconTeller
   * interface to allow them to set their own icons */
  public IconListCellRenderer (ImageIcon default_selected, ImageIcon default_de_selected)
  {
    super ();
    this.type_image_list = new Vector<TypeImage> ();
    this.default_selected = default_selected;
    this.default_de_selected = default_de_selected;
  }
  
  public void addTypeImage (Class type, ImageIcon selected, ImageIcon de_selected)
  {
    type_image_list.add (new TypeImage (type, selected, de_selected));
  }
  
  /** This is the only method defined by ListCellRenderer.
   * We just reconfigure the JLabel each time we're called.
   * @param list the list to render
   * @param value the object to display
   * @param index the index of the cell
   * @param isSelected true if the cell is default_selected
   * @param cellHasFocus true if the list/cell have the focus */
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
  {
    int count;
    ImageIcon icon;
    TypeImage type_image;
    
    setText (value.toString());
    setOpaque (true);
    if (isSelected) 
    {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
      icon = default_selected;
    }
    else 
    {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
      icon = default_de_selected;
    }
    if (value instanceof IconTeller)
      icon = ((IconTeller) value).getIcon(isSelected);
    else
    {
      for (count=0; count<type_image_list.size(); count++)
      {
        type_image = type_image_list.get(count);
        if (type_image.typesMatch(value))
          icon = type_image.getImage(isSelected);
      }
    }
    setIcon (icon);
    setEnabled (list.isEnabled());
    setFont (list.getFont());
    return this;
  }

}
