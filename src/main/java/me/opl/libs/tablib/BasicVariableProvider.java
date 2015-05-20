package me.opl.libs.tablib;

import java.util.HashMap;

public class BasicVariableProvider implements VariableProvider {
	private HashMap<String, Object> values;
	private HashMap<String, Object> tempValues;

	public BasicVariableProvider() {
		values = new HashMap<String, Object>();
		tempValues = new HashMap<String, Object>();
	}

	@Override
	public boolean isValueSet(String key) {
		return values.containsKey(key) || tempValues.containsKey(key);
	}

	public void setString(String key, String value) {
		values.put(key, value);
	}

	public void setTempString(String key, String value) {
		tempValues.put(key, value);
	}

	public String getString(String key) {
		if (!isValueSet(key)) throw new IllegalArgumentException("Key \"" + key + "\" doesn't have a value assigned.");
		Object value = tempValues.get(key);
		if (value == null) value = values.get(key);
		if (!(value instanceof String)) throw new IllegalStateException("Key \"" + key + "\" doesn't point to String value.");
		return (String) value;
	}

	public void setInteger(String key, int value) {
		values.put(key, (Integer) value);
	}

	public void setTempInteger(String key, int value) {
		tempValues.put(key, (Integer) value);
	}

	public int getInteger(String key) {
		if (!isValueSet(key)) throw new IllegalArgumentException("Key \"" + key + "\" doesn't have a value assigned.");
		Object value = tempValues.get(key);
		if (value == null) value = values.get(key); 
		if (!(value instanceof Integer)) throw new IllegalStateException("Key \"" + key + "\" doesn't point to Integer value.");
		return ((Integer) value).intValue();
	}

	@Override
	public void setObject(String key, Object value) {
		values.put(key, value);
	}

	public void setTempObject(String key, Object value) {
		tempValues.put(key, value);
	}

	@Override
	public Object getObject(String key) {
		if (!isValueSet(key)) throw new IllegalArgumentException("Key \"" + key + "\" doesn't have a value assigned.");
		Object value = tempValues.get(key);
		if (value == null) value = values.get(key);
		return value;
	}

	public void clearTempValues() {
		tempValues.clear();
	}
}
