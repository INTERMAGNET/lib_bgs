/*
 * FontTestPanel.java
 *
 * Created on 07 November 2002, 16:57
 */

package bgs.geophys.library.Swing;

import java.awt.*;
import javax.swing.*;


/**
 *
 * @author  fmc
 * @version 
 */

/****************************************************
 * FontTestPanel - a JPanel for displaying text.
 * Displays sample when selecting new fonts.
 ****************************************************/
public class FontTestPanel extends JPanel
{
  Graphics graphics;
  Font currentFont;
  FontMetrics fm;
  String sampleString = "AaBbCcXxYyZz";

  public void paintComponent (Graphics g)
  {
    super.paintComponent (g);
    int x, y;
    Font font;

    graphics = g;
    // get selected font
    font = getGraphicsFont();

    // if a font has not been set, get font metrics for
    // graphics area font
    if (font == null)
      fm = getFontMetrics (graphics.getFont());
    else
    {
      fm = getFontMetrics (font);
      graphics.setFont(font);
    }

    // calculate position for text to be centred using font
    // metrics
    x = (getSize().width - fm.stringWidth (sampleString)) / 2;
    y = (getSize().height + fm.getHeight ()) / 2;

    graphics.drawString (sampleString, x, y);
  }

/****************************************************
 * setGraphicsFont - set the font to be used on the
 *                   fontTestPanel
 ****************************************************/
  public void setGraphicsFont(Font f)
  {
    currentFont = f;
  }
  
/****************************************************
 * getGraphicsFont - get the font being used on the
 *                   fontTestPanel
 ****************************************************/
  public Font getGraphicsFont ()
  {
    return currentFont;
  }
}
