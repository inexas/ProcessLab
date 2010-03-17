package com.inexas.pl.entity.render;

import java.util.*;

public class TextLookup {
	private final TextLookup parent;
	private final Map<String,String> map = new HashMap<String,String>();
	/**
	 * lowercase ISO 639 code  
	 */
	private final String language;

	public TextLookup(TextLookup parent, String language) {
		this.parent = parent;
		this.language = language;
	}

	public TextLookup() {
		parent = null;
		language = "en";
	}

	public String getString(String key) {
		String returnValue = map.get(key);
		if(returnValue == null && parent != null) {
			returnValue = parent.getString(key);
		}
		if(returnValue == null) {
			returnValue = key;
		}
		return returnValue;
	}

	/**
	 * @return a lowercase ISO 639 code
	 * @see java.util.Locale
	 */
	public String getLanguage() {
		return language;
	}

}
