package com.inexas.pl.entity;

public class NullableConstraint implements Constraint {
	public final static String NAME = "nullable";

	public void check(Object toCheck) {
		// Nothing to do
	}

	public String getName() {
		return NAME;
	}

	public String getValue() {
		return null;
	}

}
