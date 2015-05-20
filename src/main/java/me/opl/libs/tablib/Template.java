package me.opl.libs.tablib;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JSplitPane;

import org.json.JSONArray;
import org.json.JSONObject;

// TODO: combine dialog and window?
// TODO: create them
// TODO: figure out something to make it possible to load modules with special values
//       - remove that stuff?
//       > add save state and an optional, additional constructor to it?
public class Template implements Window.WindowPopulator {
	protected TabLib tabLib;

	private JSONObject template;

	public Template(Window window) {
		this.tabLib = window.tabLib;

		template = jsonifyWindow(window);
	}

	public Template(TabLib tabLib, JSONObject template) {
		this.tabLib = tabLib;

		this.template = template;
	}

	public JSONObject getJSON() {
		return template;
	}

	private static JSONObject jsonifyWindow(Window window) {
		JSONObject json = processComponent(window.getRootPane().getComponent(1));
		json.put("templateFor", "window");

		JSONArray dialogs = new JSONArray();
		for (Dialog d : window.getDialogs()) dialogs.put(jsonifyDialog(d));
		json.put("dialogs", dialogs);

		return json;
	}

	private static JSONObject jsonifyDialog(Dialog dialog) {
		JSONObject json = processComponent(dialog.getRootPane().getComponent(1));
		json.put("templateFor", "dialog");

		JSONObject meta = new JSONObject();
		meta.put("x", dialog.getX());
		meta.put("y", dialog.getY());
		meta.put("width", dialog.getWidth());
		meta.put("height", dialog.getHeight());
		meta.put("title", dialog.getTitle());
		json.put("meta", meta);

		return json;
	}

	private static JSONObject processComponent(Component c) {
		JSONObject json = new JSONObject();
		if (c instanceof JSplitPane) {
			JSplitPane splitPane = (JSplitPane) c;
			json.put("type", "splitPane");
			json.put("divider", (float) splitPane.getDividerLocation() / (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? splitPane.getWidth() : splitPane.getHeight()));
			json.put("orientation", splitPane.getOrientation());
			json.put("top", processComponent(splitPane.getTopComponent()));
			json.put("bottom", processComponent(splitPane.getBottomComponent()));
		} else if (c instanceof TabbedPanelContainer) {
			TabbedPanelContainer tpc = (TabbedPanelContainer) c;
			json.put("type", "tpc");
			JSONArray panels = new JSONArray();
			for (int i = 0; i < tpc.getPanelCount(); i++) {
				JSONObject panel = new JSONObject();
				panel.put("class", tpc.getPanelAt(i).getClass().getName());
				panel.put("state", tpc.getPanelAt(i).saveState());
				panels.put(panel);
			}
			json.put("panels", panels);
		} else if (c instanceof Container) {
			Container container = (Container) c;
			for (int i = 0; i < container.getComponentCount(); i++) {
				JSONObject json2 = processComponent(container.getComponent(i));
				if (json2 != null) return json2;
			}
			return null;
		} else {
			return null;
		}
		return json;
	}

	/*private static void deeper(int level, Component c) {
		System.out.println("              ".substring(0, level) + c);
		if (c instanceof Container) for (Component c2 : ((Container) c).getComponents()) deeper(level+1, c2);
	}*/

	public void populate(Window window) {
		Component c = processJSON(template);

		if (c != null) {
			window.add(c);
		} else {
			window.add(tabLib.createTabbedPanelContainer());
		}

		// TODO: open dialogs
		// TODO: set dialog meta
	}

	private Component processJSON(final JSONObject json) {
		String componentType = json.getString("type");

		if (componentType.equals("splitPane")) {
			Component top = processJSON(json.getJSONObject("top"));
			Component bottom = processJSON(json.getJSONObject("bottom"));

			final JSplitPane splitPane = tabLib.createSplitPane(json.getInt("orientation"), top, bottom);

			splitPane.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					splitPane.removeComponentListener(this);
					splitPane.setDividerLocation(json.getDouble("divider"));
				}
			});

			return splitPane;
		} else if (componentType.equals("tpc")) {
			TabbedPanelContainer tpc = tabLib.createTabbedPanelContainer();

			JSONArray panels = json.getJSONArray("panels");
			for (int i = 0; i < panels.length(); i++) {
				JSONObject panelData = panels.getJSONObject(i);

				try {
					Class<? extends AbstractPanel> panelClass = Class.forName(panelData.getString("class")).asSubclass(AbstractPanel.class);

					AbstractPanel panel = tabLib.createPanel(panelClass, panelData.isNull("state") ? null : panelData.getJSONObject("state"));
					tpc.addPanel(panel);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Given template contains an unrecognizable panel: " + panelData.getString("class"));
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Given template contains a class that isn't a panel: " + panelData.getString("class"));
				}
			}

			if (tpc.getPanelCount() == 0) throw new IllegalArgumentException("Panel container with no panels.");

			return tpc;
		} else {
			System.err.println("[TabLib] Found unsupported component type: " + componentType);
			return null;
		}
	}

	/*private JSplitPane createSplitPane(JSONObject splitPaneJSON) {
		JSplitPane splitPane = tabLib.createSplitPane(splitPaneJSON.getInt("orientation"), , bottomOrRight);
	}*/
}
