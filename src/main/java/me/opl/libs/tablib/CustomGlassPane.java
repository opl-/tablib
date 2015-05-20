package me.opl.libs.tablib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class CustomGlassPane extends JComponent {
	private static final long serialVersionUID = -509067454267461572L;

	public enum DropType {
		TAB_LIST,
		PANEL
	}

	public static final int TOP = 0;
	public static final int RIGHT = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 3;
	public static final int CENTER = 4;

	private TabbedPanelContainer tabbedPanelContainer;
	private DropType dropType;
	private int position;

	public CustomGlassPane() {
		resetDropPosition();
	}

	public void setDropPosition(TabbedPanelContainer tpc, DropType dt, int pos) {
		tabbedPanelContainer = tpc;
		dropType = dt;
		position = pos;

		if (tpc != null && tabbedPanelContainer != null) setVisible(true);

		repaint();
	}

	public void resetDropPosition() {
		tabbedPanelContainer = null;
		dropType = null;
		position = 0;

		setVisible(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (dropType == null || tabbedPanelContainer == null) return;

		g.setColor(new Color(0, 0, 0, 150));

		if (dropType == DropType.TAB_LIST) {
			if (position == 0) {
				if (tabbedPanelContainer.getPanelCount() > 0) {
					Rectangle b2 = SwingUtilities.convertRectangle(tabbedPanelContainer, tabbedPanelContainer.getBoundsAt(0), tabbedPanelContainer.getRootPane());
					g.fillRect((int) b2.getX() - 1, (int) b2.getY(), 3, (int) b2.getHeight());
				} else {
					Rectangle b1 = tabbedPanelContainer.getBounds();
					g.drawRect(b1.x + (int) (b1.width * 0.35f), b1.y + (int) (b1.height * 0.35f), (int) (b1.width * 0.3f), (int) (b1.height * 0.3f));
				}
			} else {
				Rectangle b = tabbedPanelContainer.getBoundsAt(position - 1);
				if (b != null) {
					b = SwingUtilities.convertRectangle(tabbedPanelContainer, b, tabbedPanelContainer.getRootPane());
					Rectangle tabbedPaneBounds = SwingUtilities.convertRectangle(tabbedPanelContainer, tabbedPanelContainer.getBounds(), tabbedPanelContainer.getRootPane());
					if (b.getX() + b.getWidth() > tabbedPaneBounds.getX() + tabbedPaneBounds.getWidth() - 3) {
						b.setRect(tabbedPaneBounds.getX() - 3, b.getY(), tabbedPaneBounds.getWidth(), b.getHeight());
					}
					g.fillRect((int) (b.getX() + b.getWidth() - 1), (int) b.getY(), 3, (int) b.getHeight());
				}
			}
		} else if (dropType == DropType.PANEL) {
			Dimension s = tabbedPanelContainer.getSize();
			Point l = SwingUtilities.convertPoint(tabbedPanelContainer.getParent(), tabbedPanelContainer.getLocation(), tabbedPanelContainer.getRootPane());

			if (position == TOP) g.drawRect((int) l.getX(), (int) l.getY(), (int) s.getWidth(), (int) (s.getHeight() / 2d));
			else if (position == RIGHT) g.drawRect((int) (s.getWidth() / 2 + l.getX()), (int) l.getY(), (int) s.getWidth() / 2, (int) s.getHeight());
			else if (position == BOTTOM) g.drawRect((int) l.getX(), (int) (s.getHeight() / 2 + l.getY()), (int) s.getWidth(), (int) s.getHeight() / 2);
			else if (position == LEFT) g.drawRect((int) l.getX(), (int) l.getY(), (int) s.getWidth() / 2, (int) s.getHeight());
		}
	}
}
