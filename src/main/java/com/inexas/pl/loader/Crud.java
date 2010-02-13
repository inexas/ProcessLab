package com.inexas.pl.loader;


/**
 * Create read, update, delete. Classes that implement this interface
 * are persistent
 * 
 * @author keith
 *
 */
public interface Crud {
	int getId();

	/**
	 * Recursively create the object in the database
	 * 
	 * @param connection the transaction bound connection
	 */
	void create(DbConnection connection);
	/**
	 * Recursively create the object in the database
	 * 
	 * @param connection the transaction bound connection
	 */
	void update(DbConnection connection);
	/**
	 * Recursively create the object in the database
	 * 
	 * @param connection the transaction bound connection
	 */
	void delete(DbConnection connection);
}
