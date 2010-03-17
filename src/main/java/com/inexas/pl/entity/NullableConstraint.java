package com.inexas.pl.entity;

public class NullableConstraint extends Constraint {
	public final static String NAME = "nullable";

	@Override
    public void check(Object toCheck) {
		// Nothing to do
	}

	@Override
    public String getName() {
		return NAME;
	}

	public String getValue() {
		return null;
	}

	@Override
    public void toWiki(StringBuilder sb) {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
