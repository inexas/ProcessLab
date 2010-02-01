package com.inexas.pl.context;

import java.util.*;

public class Context extends Member {
	public static final Long ROOT_KEY = new Long(1); 
	// !todo Handle circularity
	private final Map<Long, Member> children = new LinkedHashMap<Long, Member>();
	
	public Context(Long key, String name) {
		super(key, name);
    }

	/**
	 * @param child to add
	 * @return true if child added, false if it was already a member
	 */
	public boolean add(Member child) {
		final boolean result;
		final Long childKey = child.key;
		if(children.containsKey(childKey)) {
			result = false;
		} else {
			children.put(childKey, child);
			result = true;
		}
		return result;
	}
	
	public Member remove(Member child) {
		assert child.parent != this : "You probably mean Member.setParent() or Member.delete()";
		return children.remove(child.key);
	}
	
}
