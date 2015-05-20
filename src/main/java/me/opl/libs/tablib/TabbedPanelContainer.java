package me.opl.libs.tablib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class TabbedPanelContainer extends JTabbedPane {
	private static final long serialVersionUID = -7941557943234327825L;

	public static final int SPLIT_VERTICAL = JSplitPane.VERTICAL_SPLIT;
	public static final int SPLIT_HORIZONTAL = JSplitPane.HORIZONTAL_SPLIT;

	protected TabLib tabLib;

	private ArrayList<AbstractPanel> panels;

	public TabbedPanelContainer(TabLib tabLibInstance) {
		this.tabLib = tabLibInstance;

		panels = new ArrayList<AbstractPanel>();

		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setTabPlacement(JTabbedPane.TOP);

		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DragGestureListener() {
			@Override
			public void dragGestureRecognized(DragGestureEvent e) {
				Point origin = e.getDragOrigin();

				for (int i = 0; i < getTabCount(); i++) {
					Rectangle b = getBoundsAt(i);
					if (b != null) {
						if (b.getX() < origin.getX() && b.getY() < origin.getY() && b.getX() + b.getWidth() > origin.getX() && b.getY() + b.getHeight() > origin.getY()) {
							tabLib.setDraggedPanel(TabbedPanelContainer.this, getPanelAt(i));
							e.startDrag(null, new PanelTransferable());
							break;
						}
					}
				}
			}
		});

		new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {
				int newTabIndex = getTabIndex(e.getLocation());
				int panelSide = getPanelSide(e.getLocation());

				if (newTabIndex == -1 && panelSide == CustomGlassPane.CENTER) newTabIndex = getTabCount();

				e.acceptDrop(DnDConstants.ACTION_MOVE);

				AbstractPanel panel = tabLib.getDraggedPanel();
				TabbedPanelContainer containter = tabLib.getDraggedPanelSource();

				((CustomGlassPane) containter.getRootPane().getGlassPane()).resetDropPosition();
				((CustomGlassPane) getRootPane().getGlassPane()).resetDropPosition();

				if (newTabIndex != -1) {
					if (containter == TabbedPanelContainer.this) {
						int oldIndex = panels.indexOf(panel);
						if (oldIndex != newTabIndex && oldIndex != newTabIndex - 1) {
							movePanel(panel, newTabIndex);
							setSelectedIndex(newTabIndex > oldIndex ? newTabIndex - 1 : newTabIndex);
						}
					} else {
						containter.closePanel(panel);
						if (containter.getPanelCount() == 0) containter.remove();
						addPanel(panel, newTabIndex);
						setSelectedIndex(newTabIndex);
					}
				} else {
					TabbedPanelContainer newTPC = new TabbedPanelContainer(tabLib);

					containter.closePanel(panel);
					newTPC.addPanel(panel);

					Component topOrLeft = null, bottomOrRight = null;
					if (panelSide == CustomGlassPane.TOP) {
						topOrLeft = newTPC;
						bottomOrRight = TabbedPanelContainer.this;
					} else if (panelSide == CustomGlassPane.RIGHT) {
						topOrLeft = TabbedPanelContainer.this;
						bottomOrRight = newTPC;
					} else if (panelSide == CustomGlassPane.BOTTOM) {
						topOrLeft = TabbedPanelContainer.this;
						bottomOrRight = newTPC;
					} else if (panelSide == CustomGlassPane.LEFT) {
						topOrLeft = newTPC;
						bottomOrRight = TabbedPanelContainer.this;
					}

					Container parent = getParent();
					int index = -1;

					for (int i = 0; i < parent.getComponents().length; i++) {
						Component c = parent.getComponent(i);
						if (c == TabbedPanelContainer.this) {
							index = i;
							break;
						}
					}

					int dividerLocation = -1;
					if (parent instanceof JSplitPane) dividerLocation = ((JSplitPane) parent).getDividerLocation();

					parent.add(tabLib.createSplitPane(panelSide == CustomGlassPane.TOP || panelSide == CustomGlassPane.BOTTOM ? SPLIT_VERTICAL : SPLIT_HORIZONTAL, topOrLeft, bottomOrRight), index);
					if (containter.getPanelCount() == 0) containter.remove();

					if (dividerLocation != -1) ((JSplitPane) parent).setDividerLocation(dividerLocation);
				}

				e.dropComplete(true);
			}

			@Override
			public void dragOver(DropTargetDragEvent e) {
				if (!e.getTransferable().isDataFlavorSupported(PanelTransferable.PANEL_FLAVOR) || e.getDropAction() != DnDConstants.ACTION_MOVE) {
					e.rejectDrag();
					return;
				}

				if (tabLib.getDraggedPanelSource() == TabbedPanelContainer.this && getPanelCount() == 1) {
					e.rejectDrag();
					((CustomGlassPane) getRootPane().getGlassPane()).resetDropPosition();
					return;
				}

				e.acceptDrag(DnDConstants.ACTION_MOVE);

				int newTabIndex = getTabIndex(e.getLocation());
				int panelSide = getPanelSide(e.getLocation());

				if (newTabIndex == -1 && panelSide == CustomGlassPane.CENTER) newTabIndex = getTabCount();

				if (newTabIndex != -1) {
					e.acceptDrag(DnDConstants.ACTION_MOVE);
					((CustomGlassPane) getRootPane().getGlassPane()).setDropPosition(TabbedPanelContainer.this, CustomGlassPane.DropType.TAB_LIST, newTabIndex);
				} else {
					if (getPanelCount() == 0 && panelSide != CustomGlassPane.CENTER) {
						e.rejectDrag();
						((CustomGlassPane) getRootPane().getGlassPane()).resetDropPosition();
					} else {
						e.acceptDrag(DnDConstants.ACTION_MOVE);
						((CustomGlassPane) getRootPane().getGlassPane()).setDropPosition(TabbedPanelContainer.this, CustomGlassPane.DropType.PANEL, panelSide);
					}
				}
			}

			@Override
			public void dragExit(DropTargetEvent e) {
				((CustomGlassPane) getRootPane().getGlassPane()).resetDropPosition();
			}

			private int getTabIndex(Point point) {
				if (getPanelCount() == 0) return -1;

				Rectangle firstTabBounds = getBoundsAt(0);
				if (firstTabBounds == null) return -1;

				if (firstTabBounds.getY() <= point.getY() && firstTabBounds.getY() + firstTabBounds.getHeight() >= point.getY()) {
					boolean lastBoundaryNotNull = true;

					for (int i = 0; i < getTabCount(); i++) {
						Rectangle b = getBoundsAt(i);
						if (b != null) {
							if (point.getX() >= b.getX() && point.getX() <= b.getX() + b.getWidth() / 2) return i;
							else if (point.getX() >= b.getX() + b.getWidth() / 2 && point.getX() <= b.getX() + b.getWidth()) return i + 1;
							lastBoundaryNotNull = true;
						} else {
							lastBoundaryNotNull = false;
						}
					}

					if (lastBoundaryNotNull) return firstTabBounds.getX() >= point.getX() ? 0 : getTabCount();
				}

				return -1;
			}

			private int getPanelSide(Point point) {
				Dimension b = getSize();

				double x = point.getX() / b.getWidth();
				double y = point.getY() / b.getHeight();

				if (x > 0.35 && x < 0.65 && y > 0.35 && y < 0.65) return CustomGlassPane.CENTER;

				if (Math.pow(x - 0.5d, 2) < Math.pow(y - 0.5d, 2)) {
					if (y < 0.5d) return CustomGlassPane.TOP;
					else return CustomGlassPane.BOTTOM;
				} else {
					if (x < 0.5d) return CustomGlassPane.LEFT;
					else return CustomGlassPane.RIGHT;
				}
			}
		});
	}

	public void remove() {
		if (getParent() instanceof JSplitPane) {
			JSplitPane splitPane = (JSplitPane) getParent();
			Component otherComponent = null;

			if (splitPane.getTopComponent() == this) otherComponent = splitPane.getBottomComponent();
			else otherComponent = splitPane.getTopComponent();

			if (splitPane.getParent() instanceof JSplitPane) {
				JSplitPane splitPanesParent = (JSplitPane) splitPane.getParent();
				int dividerLocation = splitPanesParent.getDividerLocation();

				if (splitPanesParent.getTopComponent() == splitPane) splitPanesParent.setTopComponent(otherComponent);
				else splitPanesParent.setBottomComponent(otherComponent);

				splitPanesParent.setDividerLocation(dividerLocation);
			} else {
				Container c = splitPane.getParent();
				splitPane.getParent().add(otherComponent);
				splitPane.getParent().remove(splitPane);
				c.revalidate();
			}
		} else if (getRootPane().getParent() instanceof Dialog) {
			Dialog d = (Dialog) getRootPane().getParent();
			d.dispose();
		}
	}

	public void addPanel(AbstractPanel panel) {
		panels.add(panel);
		addTab(panel.getTitle(), panel.getPanel());
	}

	public void addPanel(AbstractPanel panel, int index) {
		if (index == panels.size()) {
			addPanel(panel);
		} else {
			panels.add(index, panel);
			insertTab(panel.getTitle(), null, panel.getPanel(), null, index);
		}
	}

	public void movePanel(AbstractPanel panel, int to) {
		movePanel(panels.indexOf(panel), to);
	}

	public void movePanel(int from, int to) {
		Component component = getComponentAt(from);
		String title = getTitleAt(from);
		String tooltip = getToolTipTextAt(from);
		Icon icon = getIconAt(from);
		Icon disabledIcon = getDisabledIconAt(from);
		int mnemonic = getMnemonicAt(from);

		if (from < to) to--;

		AbstractPanel p = panels.remove(from);
		panels.add(to, p);

		remove(from);
		insertTab(title, icon, component, tooltip, to);

		setDisabledIconAt(to, disabledIcon);
		setMnemonicAt(to, mnemonic);
	}

	public AbstractPanel closePanel(AbstractPanel panel) {
		int index = panels.indexOf(panel);
		if (index == -1) throw new IllegalArgumentException("panel doesn't belong to this container");
		return closePanel(panels.indexOf(panel));
	}

	public AbstractPanel closePanel(int index) {
		if (index < 0 || index >= panels.size()) throw new ArrayIndexOutOfBoundsException(index);
		AbstractPanel p = panels.remove(index);
		remove(index);
		return p;
	}

	public AbstractPanel getPanelAt(int index) {
		return panels.get(index);
	}

	public int getPanelIndex(AbstractPanel panel) {
		return panels.indexOf(panel);
	}

	public int getPanelCount() {
		return panels.size();
	}
}
