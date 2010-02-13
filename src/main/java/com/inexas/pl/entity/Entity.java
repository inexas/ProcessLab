package com.inexas.pl.entity;

public class Entity extends Tuple {
	private EntityType entityType;

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

}
