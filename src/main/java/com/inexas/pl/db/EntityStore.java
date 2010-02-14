package com.inexas.pl.db;

import java.sql.*;
import com.inexas.pl.entity.*;

public class EntityStore {
	private static final EntityStore instance;
	static {
		instance = new EntityStore();
	}

	public static EntityStore getInstance() {
		return instance;
	}

	private RelationalSubsystem relationalSubsystem;
	private Subsystem accordionSubsystem;

	private EntityStore() {
		// Make sure the database is initialized. There must be a Sequence table
		// so if it exists then the database must be installed
		relationalSubsystem = new RelationalSubsystem();
		accordionSubsystem = new AccordionSubsystem();
		final Db db = Db.reserveInstance();
		try {
			final ResultSet rs = db.query("GetTable", "{TableName}", "Sequence");
			if(!rs.next()) {
				db.batchUpdate("InstallStore", false);
			}
		} catch(final SQLException e) {
			throw new DbRuntimeException("Error installing database", e);
		} finally {
			Db.releaseInstance(db);
		}

	}

	public void create(Entity entity) {
		final EntityType entityType = entity.getEntityType();
		if(entityType.isRelational()) {
			relationalSubsystem.create(entity);
		} else {
			accordionSubsystem.create(entity);
		}
	}

	public Entity read(EntityType entityType, int id) {
		final Entity result;
		if(entityType.isRelational()) {
			result = relationalSubsystem.read(id);
		} else {
			result = accordionSubsystem.read(id);
		}
		return result;
	}

	public void update(Entity entity) {
		final EntityType entityType = entity.getEntityType();
		if(entityType.isRelational()) {
			relationalSubsystem.update(entity);
		} else {
			accordionSubsystem.update(entity);
		}
	}

	public void delete(Entity entity) {
		final EntityType entityType = entity.getEntityType();
		if(entityType.isRelational()) {
			relationalSubsystem.delete(entity);
		} else {
			accordionSubsystem.delete(entity);
		}
	}

	public void create(EntityType entityType) {
		relationalSubsystem.create(entityType);
    }

	public void delete(EntityType entity) {
		relationalSubsystem.delete(entity);
	}

}
