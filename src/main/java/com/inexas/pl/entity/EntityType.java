package com.inexas.pl.entity;

import java.util.*;

public class EntityType extends TupleType {
	private final String fullKey;

	public EntityType(String key, Cardinality cardinality, List<AbstractType> members) {
		super(getTupleKey(key), cardinality, members);
		fullKey = key;
	}
	
	/**
	 * The entity key is the the form: 'PageKey:PageVersion:TupleKey' so something
	 * like "/Site:1:Config". Return the tuple key
	 */
	private static String getTupleKey(String entityKey) {
		final String result;
		final int colon = entityKey.lastIndexOf(':');
		assert colon > 1 : "Invalid entity type key: " + entityKey;
		result = entityKey.substring(colon + 1);
		return result;
	}

	public boolean isRelational() {
		return true;
    }
	
	@Override
    public String getKey() {
		return fullKey;
	}

}
