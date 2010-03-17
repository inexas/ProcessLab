package com.inexas.pl.entity;

import com.inexas.util.*;

public class UniqueConstraint extends Constraint {
	public final static String NAME = "unique";
	public final static UniqueConstraint instance = new UniqueConstraint();
	
	private UniqueConstraint() {
		// hide the ctor
	}

	@Override
	public void check(Object toCheck) {
		throw new UnexpectedException("toCheck");
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void toWiki(StringBuilder sb) {
		sb.append("unique;");
	}

}
