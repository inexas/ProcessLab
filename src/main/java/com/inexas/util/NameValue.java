package com.inexas.util;

/**
 * A simple utility class that stores a name value pair. The name
 * is a String, the value is a generic type.
 * 
 * @author KeithWhittingham
 */
public class NameValue<T> {
	private final String name;
	private final T value;
	
	public NameValue(String name, T value) {
		assert name != null && name.trim().length() > 0;
		this.name = name.trim();
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
