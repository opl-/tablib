package me.opl.libs.tablib;

import javax.swing.JPanel;

import org.json.JSONObject;

public abstract class AbstractPanel {
	public AbstractPanel(VariableProvider variableProvider) {}

	public AbstractPanel(VariableProvider variableProvider, JSONObject savedState) {}

	public abstract String getTitle();

	public abstract JPanel getPanel();

	public JSONObject saveState() {return null;}
}
