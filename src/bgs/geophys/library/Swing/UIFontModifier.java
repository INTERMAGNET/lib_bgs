/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A class that allow modification of font attributes for a Swing
 * pluggable look and feel.
 * 
 * Although this class should change all visible objects in a class
 * hierarchy, it doesn't - the only safe way to use it is to call
 * it to do its work before the main window is displayed.
 * 
 * @author smf
 */
public class UIFontModifier 
{

    private class KeyedFont
    {
        private String key;
        private Font font;
        private Font orig_font;
        public KeyedFont (String key, Font font)
        {
            this.key = key;
            this.font = font;
            orig_font = new Font (font.getFontName(), font.getStyle(), font.getSize());
        }
        public String getKey () { return key; }
        public Font getFont () { return font; }
        public void setFont (Font font) 
        {
            this.font = font; 
            UIManager.put (key, font);
        }
        public Font getOrigFont () { return orig_font; }
        @Override
        public String toString () { return key; }
    }
    
    private Vector<KeyedFont> font_list;
    
    public UIFontModifier ()
    {
        Enumeration en;
        Object o;
        String name;
        Font font;
        
        // find all font keys
        font_list = new Vector<KeyedFont> ();
        en = UIManager.getDefaults().keys(); 
        while (en.hasMoreElements())
        {
            o = en.nextElement();
            name = o.toString ();
            font = UIManager.getFont(name);
            if (font != null)
                font_list.add (new KeyedFont (name, font));
        }
    }
    
    /** change all font sizes relative to their original values
     * @param percent the amount to change by - changes are not
     *        cumulative - multiplier is always applied to the original
     *        font size
     * @param min the minimum value a font may take */
    public void changeFontSize (int percent, float min)
    {
        int count;
        float multiplier, size;
        KeyedFont keyed_font;
        Font font;

        multiplier = (float) percent / 100.0f;
        for (count=0; count<font_list.size(); count++)
        {
            keyed_font = font_list.get (count);
            font = keyed_font.getFont();
            size = (float) font.getSize() * multiplier;
            if (size < min) size = min;
            keyed_font.setFont (font.deriveFont(size));
        }
        
        updateAllUI ();
    }

    private void updateAllUI() 
    {
       Frame[] frames = Frame.getFrames();
       for(int f = 0; f < frames.length; f++) {
          SwingUtilities.updateComponentTreeUI(frames[f]);
          frames[f].validate();
          Window[] windows = frames[f].getOwnedWindows();
          for(int w = 0; w < windows.length; w++) {
             SwingUtilities.updateComponentTreeUI(windows[w]);
             windows[w].validate();
          }
       }
    }
    
}
