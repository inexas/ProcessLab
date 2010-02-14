package com.inexas.pl.entity;

import java.util.*;

public class Entity extends Tuple {
	private EntityType entityType;
	private Set<Tuple> deletedTuples = new HashSet<Tuple>(); 

	public Entity(EntityType entityType) {
	    super(entityType);
	    this.setEntityType(entityType);
    }

	public void setEntityType(EntityType entityType) {
	    this.entityType = entityType;
    }

	public EntityType getEntityType() {
	    return entityType;
    }

	public Collection<Tuple> getDeletedTuples() {
		return deletedTuples;
    }

}
