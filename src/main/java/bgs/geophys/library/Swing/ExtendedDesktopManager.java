package bgs.geophys.library.Swing;

import javax.swing.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;

public class ExtendedDesktopManager extends DefaultDesktopManager {
	public ExtendedDesktopManager(JDesktopPane targetPane) {
		ghostPanel = new JPanel();
		ghostPanel.setOpaque(false);
		ghostPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
		this.targetPane = targetPane;
	}

	public void beginDraggingFrame(JComponent f) {
		Rectangle r = f.getBounds();
		ghostPanel.setBounds(r);

		f.setVisible(false);
		targetPane.add(ghostPanel);
		targetPane.setLayer(ghostPanel, JLayeredPane.DRAG_LAYER.intValue());
		targetPane.setVisible(true);		
	}

	public void dragFrame(JComponent f, int newX, int newY) {
		setBoundsForFrame(ghostPanel, newX, newY, ghostPanel.getWidth(), ghostPanel.getHeight());		
	}

	public void endDraggingFrame(JComponent f) {
		Rectangle r = ghostPanel.getBounds();
		
		targetPane.remove(ghostPanel);
		f.setVisible(true);
		f.setBounds(r);
	}

	public void beginResizingFrame(JComponent f, int direction) {
		oldCursor = f.getCursor();
		super.beginResizingFrame(f, direction);
		Cursor cursor = f.getCursor();
		Rectangle r = f.getBounds();
		ghostPanel.setBounds(r);

		f.setVisible(false);
		targetPane.add(ghostPanel);
		targetPane.setLayer(ghostPanel, JLayeredPane.DRAG_LAYER.intValue());
		ghostPanel.setCursor(cursor);
		targetPane.setVisible(true);
	}

	public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		setBoundsForFrame(ghostPanel, newX, newY, newWidth, newHeight);
	}

	public void endResizingFrame(JComponent f) {
		Rectangle r = ghostPanel.getBounds();
		ghostPanel.setCursor(oldCursor);
		targetPane.remove(ghostPanel);
		f.setVisible(true);
		f.setBounds(r);
	}

	protected JPanel ghostPanel;
	protected JComponent targetComponent;
	protected JDesktopPane targetPane;
	protected Cursor oldCursor;


	protected static final Color BORDER_COLOR = Color.black;
	protected static final int BORDER_THICKNESS = 2;
}
