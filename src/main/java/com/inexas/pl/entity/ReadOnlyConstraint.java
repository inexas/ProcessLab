package com.inexas.pl.entity;

public class ReadOnlyConstraint implements Constraint {
	public final static String NAME = "readonly";

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
