package com.inexas.pl.entity;

public class NotnullConstraint extends Constraint {
	public static final String NAME = "notnull";
	public static Constraint notnullConstraint = new NotnullConstraint();

	/**
	 * Use the preconstructed static variable, it's faster
	 * 
	 * @see NotnullConstraint#notnullConstraint
	 */
	private NotnullConstraint() {
		// prevent construction, 
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void check(Object toCheck) throws ConstraintViolationException {
		if(toCheck == null) {
			throw new ConstraintViolationException(this, "Cannot set to null");
		}
	}

	@Override
	public String toString() {
		return NAME;
	}

	@Override
	public void toWiki(StringBuilder sb) {
		sb.append("notnull;");
	}
	
}
