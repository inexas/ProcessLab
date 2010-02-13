package com.inexas.pl.entity;

import java.util.*;

public interface EntityProvider {

	/**
	 * Create a new entity of a given type.
	 * 
	 * @param the newly created entity
	 */
	Entity createInstance(EntityType entityType);

	/**
	 * Write the current contents of an entity. New entries are inserted,
	 * existing entries are updated if they are dirty, deleted tuples are
	 * removed.
	 * 
	 * @param entity the entity to write
	 */
	void update(Entity entity);

	/**
	 * Given an empty tuple list and its ID fill it from the DB. 
	 * 
	 * @param entity
	 */
	Entity read(EntityType entityType, int id);

	void delete(Entity entity);

	void install(Map<String, EntityType> entityTypes);

}