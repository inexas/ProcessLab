package com.inexas.util;

import java.util.*;

/**
 * BasicCache implements a non-canonical LRU cache with a configurable ceiling
 * 
 * @author kgwhittingham
 */
public class BasicCache<Key, Referent> {
	private final int ceiling;
	private LinkedHashMap<Key, Referent> map = new LinkedHashMap<Key, Referent>();
	
	/**
	 * Construct a cache. The name of the cache is used to look
	 * up the value of the ceiling (maximum number of cached 
	 * objects) from Config in the form &lt;name&gt;.ceiling
	 * 
	 * @param name the name of the cache
	 */
	public BasicCache(String name) {
		ceiling = Config.instance.getInt(name + ".ceiling", 512);
	}

	public void put(Key key, Referent referent) {
		map.put(key, referent);
		if(map.size() > ceiling) {
			map.remove(map.keySet().iterator().next());
		}
	}
	
	public Referent get(Key key) {
		return map.get(key);
	}

	public int getCeiling() {
		return ceiling;
	}

	public int size() {
		return map.size();
	}
	
}
