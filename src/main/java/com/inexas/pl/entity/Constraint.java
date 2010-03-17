package com.inexas.pl.entity;

import java.util.*;
import com.inexas.util.*;

public abstract class Constraint {
	
	public abstract String getName();

	/**
	 * Check the value and if it is not OK then throw a constraint
	 * violation exception
	 * 
	 * @param toCheck the object to check
	 * @throws ConstraintViolationException if toCheck is not valid
	 */
	public abstract void check(Object toCheck) throws ConstraintViolationException;
	
	public abstract void toWiki(StringBuilder sb);

	public static Constraint getConstraint(String constraintName) {
		final Constraint returnValue;
		if(constraintName.equals(NotnullConstraint.NAME)) {
			returnValue = NotnullConstraint.notnullConstraint;
		} else if(constraintName.equals(UniqueConstraint.NAME)) {
			returnValue = UniqueConstraint.instance;
		} else {
			throw new UnexpectedException(constraintName);
		}
		return returnValue;
	}

	@SuppressWarnings("unchecked")
	public static Constraint getConstraint(String constraintName, Object value) {
		final Constraint returnValue;
		if(constraintName.equals(DateFormatConstraint.NAME)) {
			returnValue = new DateFormatConstraint((String)value);
		} else if(constraintName.equals(OptionsConstraint.NAME)) {
			returnValue = new OptionsConstraint((List<Object>)value);
		} else if(constraintName.equals(RangeConstraint.NAME)) {
			// todo Implement me
			throw new RuntimeException("Implement me!");
		} else {
			throw new UnexpectedException(constraintName);
		}
		return returnValue;
	}
	
}
