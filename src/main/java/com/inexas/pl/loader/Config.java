package com.inexas.pl.loader;

import java.io.*;
import java.util.*;

/**
 * A generic config class. The main differences with a standard properties file
 * are:
 * <ul>
 * <li> It's pre-named to "config.properties" and located in the root of the 
 * 		classpath
 * <li> The class is a singleton
 * <li> It can either be constructed as a part of a java application or as as
 *		part of a web application
 * <li> Helper methods getString(), getBoolean(), getInteger() are provided to
 *		provide the data conversion and error handling from text
 * <li> The getXxx(String) methods are strict - if the key is not found then a
 *		runtime exception is thrown. You can still use getXxx(String, Xxx) methods if
 *		the key is optional
 * </ul>
 * 
 * It should really only be used to store the database connection details
 * so that the rest of the config can be loaded from there to work in
 * a distributed environment
 * 
 * @author kwhittingham
 */
public class Config extends Properties {
	private static final long serialVersionUID = -9099353760446369421L;
	public final static String NAME = "config.properties";
	static {
		instance = new Config();
	}
	public final static Config instance;
	
	private Config() {
		try {
			final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(NAME);
			if(in == null) {
				throw new RuntimeException("Failed to load " + NAME + " in root of classpath");
			}
			load(in);
			in.close();
		} catch(IOException e) {
			throw new RuntimeException("Error loading " + NAME + " in root of classpath", e);
		}
	}
		
	public String getString(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}
	
	public String getString(String key) {
		final String returnValue = super.getProperty(key);
		if(returnValue == null) {
			throw new RuntimeException("Missing value in config for key: " + key);
		}
		return returnValue;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		final boolean returnValue;
		final String string = super.getProperty(key);
		if(string == null) {
			returnValue = defaultValue;
		} else {
			returnValue = toBoolean(key, string);
		}
		return returnValue;
	}
	
	public boolean getBoolean(String key) {
		return Boolean.valueOf(getString(key)).booleanValue();
	}
	
	private boolean toBoolean(String key, String string) {
		final boolean returnValue;
		if("true".equals(string)) {
			returnValue = true;
		} else if("false".equals(string)) {
			returnValue = false;
		} else {
			throw new RuntimeException(
					"Invalid value in " + NAME + " for key: " + key + " = " + string + ", expected true or false");
		}
		return returnValue;
	}

	public int getInt(String key, int defaultValue) {
		final int returnValue;
		final String string = super.getProperty(key);
		if(string == null) {
			returnValue = defaultValue;
		} else {
			returnValue = toInt(key, string);
		}
		return returnValue;
	}
	
	public int getInt(String key) {
		return toInt(key, getString(key));
	}
	
	private int toInt(String key, String string) {
		try {
			return Integer.parseInt(string);
		} catch(NumberFormatException e) {
			throw new RuntimeException(
					"Invalid value in " + NAME + " for key: " + key + " = " + string + ", expected an integer", e);
		}
	}

	@Deprecated
	@Override
	public String getProperty(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}

	@Deprecated
	@Override
	public String getProperty(String key) {
		return super.getProperty(key);
	}

}
