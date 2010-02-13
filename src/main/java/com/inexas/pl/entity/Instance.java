package com.inexas.pl.entity;

import java.util.*;

/**
 * @author keith
 */
public abstract class Instance {
	// todo Remove key from class
	protected final String key;
	protected final Tuple parentTuple;
	private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	protected Instance(String key) {
		this.key = key;
		parentTuple = null;
	}

	protected Instance(String key, Tuple parent) {
		this.key = key;
		this.parentTuple = parent;
	}

	/**
	 * The key KTCV key is the same as the KtcvType key so this is a convenience
	 * method.
	 * 
	 * @return the KtcvType key
	 */
	public final String getKey() {
		return key;
	}

	public Tuple getParentTuple() {
		return parentTuple;
	}

	public String getFullPath() {
		final StringBuilder sb = new StringBuilder();
		getFullPath(sb);
		return sb.toString();
	}

	void getFullPath(StringBuilder sb) {
		if(parentTuple != null) {
			parentTuple.getFullPath(sb);
		}
		sb.append('/');
		sb.append(key);
	}

	public void addChangeListener(ChangeListener dependency) {
		// todo Check for circularity...
		listeners.add(dependency);
	}

	public void removeChangeListener(ChangeListener listener) {
		assert listeners.contains(listener);
		listeners.remove(listener);
	}

	void fire(ChangeListener.Event event) {
		for(final ChangeListener listener : listeners) {
			listener.handle(event);
		}
	}

	/**
	 * Recursively check to see if the candidate listener is already a listener
	 * 
	 * @param candidateDependee
	 * @return
	 */
	public boolean dependsOn(ChangeListener candidateDependee) {
		if(this == candidateDependee) {
			return true;
		}
		for(final ChangeListener listener : listeners) {
			if(((Instance)listener).dependsOn(candidateDependee)) {
				return true;
			}
		}
		return false;
	}

}
