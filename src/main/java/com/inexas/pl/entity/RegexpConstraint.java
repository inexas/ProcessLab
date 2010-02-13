package com.inexas.pl.entity;

public class RegexpConstraint implements Constraint {
	public final static String NAME = "regexp";
	public final String regexp;

	public RegexpConstraint(String regexp) {
		this.regexp = regexp;
	}

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
