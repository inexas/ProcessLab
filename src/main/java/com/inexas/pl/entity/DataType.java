/**
 * 
 */
package com.inexas.pl.entity;

import java.util.*;

public enum DataType {
	BOOLEAN("Boolean", 0, false),
	DOUBLE("Double", 1, false),
	DATETIME("DateTime", 2, false),
	INTEGER("Integer", 3, true),
	LONG("Long", 4, true),
	SHORT("Short", 5, true),
	STRING("String", 6, false),
	TUPLE("Tuple", 7, false);

	private static Map<String, DataType> typeLookup = new HashMap<String, DataType>();
	private static Class<?> classes[];
	static {
		typeLookup.put("Boolean", DataType.BOOLEAN);
		typeLookup.put("java.lang.Boolean", DataType.BOOLEAN);
		typeLookup.put("boolean", DataType.BOOLEAN);
		
		typeLookup.put("Date", DataType.DATETIME);
		
		typeLookup.put("Double", DataType.DOUBLE);
		typeLookup.put("java.lang.Double", DataType.DOUBLE);
		typeLookup.put("double", DataType.DOUBLE);
		
		typeLookup.put("Integer", DataType.INTEGER);
		typeLookup.put("java.lang.Integer", DataType.INTEGER);
		typeLookup.put("integer", DataType.INTEGER);

		typeLookup.put("Long", DataType.LONG);
		typeLookup.put("java.lang.Long", DataType.LONG);
		typeLookup.put("long", DataType.LONG);

		typeLookup.put("Short", DataType.SHORT);
		typeLookup.put("java.lang.Short", DataType.SHORT);
		typeLookup.put("short", DataType.SHORT);
		
		typeLookup.put("String", DataType.STRING);
		typeLookup.put("string", DataType.STRING);

		typeLookup.put("tuple", DataType.TUPLE);

		classes = new Class<?>[8];
		classes[DataType.BOOLEAN.asInt] = boolean.class;
		classes[DataType.DOUBLE.asInt] = double.class;
		classes[DataType.DATETIME.asInt] = Date.class;
		classes[DataType.INTEGER.asInt] = int.class;
		classes[DataType.LONG.asInt] = long.class;
		classes[DataType.SHORT.asInt] = short.class;
		classes[DataType.STRING.asInt] = String.class;
		classes[DataType.TUPLE.asInt] = Tuple.class;
	}

	public final boolean isShiftable;
	public final int asInt;
	public final String asText;

	DataType(String asText, int asInt, boolean isShiftable) {
		this.asText = asText;
		this.asInt = asInt;
		this.isShiftable = isShiftable;
	}

	public static DataType getType(String typeName) {
		return typeLookup.get(typeName);
	}

	public static Class<?> getClass(DataType type) {
		return classes[type.asInt];
	}

	public static DataType getType(Class<?> returnType) {
		try {
			final String name = returnType.getName();
			return typeLookup.get(name);
		} catch(final NullPointerException e) {
			throw new RuntimeException("Missing lookup for: " + returnType.getName());
		}
	}

}