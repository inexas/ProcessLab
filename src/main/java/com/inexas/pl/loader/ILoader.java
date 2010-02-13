package com.inexas.pl.loader;


/**
 * The methods in the loader interface are used to avoid circularity and
 * so stack overflows when objects are loaded. For example a process 
 * definition needs to load the initiator role and the initiator role needs
 * to load the process definition.
 * 
 * @author keith
 *
 */
public interface ILoader {
	int getId();
	
	String getKey();
	
	void setHost(Crud host);
	
	void setParent(ILoader loader);
	
}
