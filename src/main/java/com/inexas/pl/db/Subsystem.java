package com.inexas.pl.db;

import com.inexas.pl.entity.*;

interface Subsystem {
	void install(Transaction transaction);
	void uninstall(Transaction transaction);
	void create(Entity entity);
	Entity read(int id);
	void update(Entity entity);
	void delete(Entity entity);
}
