package me.opl.libs.tablib;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.json.JSONObject;

public class TabLib {
	private boolean instantResize;

	private AbstractPanel draggedPanel;
	private TabbedPanelContainer draggedPanelSource;

	private ArrayList<Window> windows;

	private VariableProvider variableProvider;

	public TabLib(VariableProvider variableProvider) {
		windows = new ArrayList<Window>();

		draggedPanel = null;
		draggedPanelSource = null;

		this.variableProvider = variableProvider;

		instantResize = true;
	}

	public void setInstantResize(boolean instantResize) {
		this.instantResize = instantResize;
	}

	public boolean getInstantResize() {
		return instantResize;
	}

	protected void setDraggedPanel(TabbedPanelContainer sourceTPC, AbstractPanel panel) {
		this.draggedPanelSource = sourceTPC;
		this.draggedPanel = panel;
	}

	protected TabbedPanelContainer getDraggedPanelSource() {
		return draggedPanelSource;
	}

	protected AbstractPanel getDraggedPanel() {
		return draggedPanel;
	}

	public VariableProvider getVariableProvider() {
		return variableProvider;
	}

	public Window openNewWindow(Window.WindowPopulator populator) {
		Window w = new Window(this, populator);
		windows.add(w);
		w.setVisible(true);
		return w;
	}

	public Dialog openNewDialog(Window window, Class<? extends AbstractPanel> panelClass) {
		return window.openNewDialog(panelClass);
	}

	public AbstractPanel createPanel(Class<? extends AbstractPanel> panelClass) {
		return createPanel(panelClass, null);
	}

	public AbstractPanel createPanel(Class<? extends AbstractPanel> panelClass, JSONObject state) {
		try {
			if (state == null) {
				try {
					return getConstructor(panelClass, VariableProvider.class).newInstance(variableProvider);
				} catch (NoSuchMethodException e) {
					return getConstructor(panelClass, VariableProvider.class, JSONObject.class).newInstance(variableProvider, state);
				}
			} else {
				try {
					return getConstructor(panelClass, VariableProvider.class, JSONObject.class).newInstance(variableProvider, state);
				} catch (NoSuchMethodException e) {
					return getConstructor(panelClass, VariableProvider.class).newInstance(variableProvider);
				}
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Given class doesn't have a valid constructor: " + panelClass.getName(), e);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Given class is abstract: " + panelClass.getName(), e);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public TabbedPanelContainer createTabbedPanelContainer() {
		return new TabbedPanelContainer(this);
	}

	public JSplitPane createSplitPane(int orientation, Component topOrLeft, Component bottomOrRight) {
		final JSplitPane splitPane = new JSplitPane(orientation, instantResize);

		splitPane.setDividerSize(3);
		splitPane.setResizeWeight(0.5d);

		splitPane.setTopComponent(topOrLeft);
		splitPane.setBottomComponent(bottomOrRight);

		((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(null);
		((BasicSplitPaneUI) splitPane.getUI()).getSplitPane().setBorder(null);

		return splitPane;
	}

	protected void onWindowClosed(Window window) {
		windows.remove(window);
	}

	@SuppressWarnings("unchecked")
	private Constructor<? extends AbstractPanel> getConstructor(Class<? extends AbstractPanel> panelClass, Class<?>... paramTypes) throws NoSuchMethodException {
		constructor: for (Constructor<?> c : panelClass.getConstructors()) {
			if (!AbstractPanel.class.isAssignableFrom(c.getDeclaringClass())) continue;

			Class<?>[] contructorParams = c.getParameterTypes();

			if (contructorParams.length != paramTypes.length) continue;

			for (int i = 0; i < contructorParams.length; i++) {
				if (!paramTypes[i].isAssignableFrom(contructorParams[i])) continue constructor;
			}

			return (Constructor<? extends AbstractPanel>) c;
		}

		String ex = null;
		for (Class<?> c : paramTypes) ex = (ex == null ? "" : ex + ", ") + c.getName();
		throw new NoSuchMethodException(ex);
	}
}
