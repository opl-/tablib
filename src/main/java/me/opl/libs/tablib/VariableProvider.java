package me.opl.libs.tablib;

public interface VariableProvider {
	public boolean isValueSet(String key);

	public void setObject(String key, Object value);

	public Object getObject(String key);
}
