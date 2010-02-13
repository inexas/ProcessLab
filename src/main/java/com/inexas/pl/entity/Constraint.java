package com.inexas.pl.entity;

public interface Constraint {
	String getName();

	/**
	 * Check the value and if it is not OK then throw a constraint violation
	 * exception
	 * 
	 * @param toCheck
	 *            the object to check
	 * @throws ConstraintViolationException
	 *             if toCheck is not valid
	 */
	void check(Object toCheck) throws Exception;

	String getValue();

}
