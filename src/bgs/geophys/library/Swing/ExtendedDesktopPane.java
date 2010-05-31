package bgs.geophys.library.Swing;

import javax.swing.*;
import java.awt.*;

/**
 * A desktop pane that supports tiling and cascading
 */
public class ExtendedDesktopPane extends JDesktopPane {
	// This method allows child frames to
	// be added with automatic cascading
	public void addCascaded(Component comp, Integer layer) {
		// First add the component in the correct layer
		this.add(comp, layer);
		
		// Now do the cascading
		if (comp instanceof JInternalFrame) {
			this.cascade(comp);
		}

		// Move it to the front
		this.moveToFront(comp);
	}
	
	// Layout all of the children of this container
	// so that they are cascaded.
	public void cascadeAll() {
		Component[] comps = getComponents();
		int count = comps.length;
		nextX = 0;
		nextY = 0;

		for (int i = count - 1; i >= 0; i--) {
			Component comp = comps[i];
			if (comp instanceof JInternalFrame && comp.isVisible()) {
				cascade(comp);
			}
		}
	}

	// Layout all of the children of this container
	// so that they are tiled.
	public void tileAll() {
		DesktopManager manager = getDesktopManager();
		if (manager == null) {
			// No desktop manager - do nothing
			return;
		}
			
		Component[] comps = getComponents();
		Component comp;
		int count = 0;

		// Count and handle only the internal frames
		for (int i = 0; i < comps.length; i++) {
			comp = comps[i];
			if (comp instanceof JInternalFrame && comp.isVisible()) {
				count++;
			}
		}

		if (count != 0) {
			double root = Math.sqrt((double)count);
			int rows = (int)root;
			int columns = count/rows;
			int spares = count - (columns * rows);

			Dimension paneSize = getSize();
			int columnWidth = paneSize.width/columns;

			// We leave some space at the bottom that doesn't get covered
			int availableHeight = paneSize.height - UNUSED_HEIGHT;
			int mainHeight = availableHeight/rows;
			int smallerHeight = availableHeight/(rows + 1);
			int rowHeight = mainHeight;
			int x = 0;
			int y = 0;
			int thisRow = rows;
			int normalColumns = columns - spares;

			for (int i = comps.length - 1; i >= 0; i--) {
				comp = comps[i];
				if (comp instanceof JInternalFrame && comp.isVisible()) {
					manager.setBoundsForFrame((JComponent)comp, x, y, columnWidth, rowHeight);
					y += rowHeight;
					if (--thisRow == 0) {
						// Filled the row
						y = 0;
						x += columnWidth;

						// Switch to smaller rows if necessary
						if (--normalColumns <= 0) {
							thisRow = rows + 1;
							rowHeight = smallerHeight;
						} else {
							thisRow = rows;
						}
					}
				}
			}
		}
	}

	public void setCascadeOffsets(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public void setCascadeOffsets(Point pt) {
		this.offsetX = pt.x;
		this.offsetY = pt.y;
	}

	public Point getCascadeOffsets() {
		return new Point(offsetX, offsetY);
	}

	// Place a component so that it is cascaded
	// relative to the previous one
	protected void cascade(Component comp) {		
		Dimension paneSize = getSize();
		int targetWidth = 3 * paneSize.width/4;
		int targetHeight = 3 * paneSize.height/4;
		
		DesktopManager manager = getDesktopManager();
		if (manager == null) {
			comp.setBounds(0, 0, targetWidth, targetHeight);
			return;
		}
		
		if (nextX + targetWidth > paneSize.width ||
			nextY + targetHeight > paneSize.height) {
			nextX = 0;
			nextY = 0;
		}

		manager.setBoundsForFrame((JComponent)comp, nextX, nextY, targetWidth, targetHeight);
		
		nextX += offsetX;
		nextY += offsetY;
	}

	
	protected int nextX;		// Next X position
	protected int nextY;		// Next Y position
	protected int offsetX = DEFAULT_OFFSETX;
	protected int offsetY = DEFAULT_OFFSETY;

	protected static final int DEFAULT_OFFSETX = 24;
	protected static final int DEFAULT_OFFSETY = 24;
	protected static final int UNUSED_HEIGHT = 48;
}
