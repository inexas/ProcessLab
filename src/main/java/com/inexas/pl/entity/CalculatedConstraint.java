package com.inexas.pl.entity;

import java.util.*;

/**
 * This constrains a KTCV to a calculated value,
 * 
 * @author keith
 * 
 */
public class CalculatedConstraint extends Constraint {
	public final static String NAME = "calculated";
	private final String expressionText;

	// private Expression expression;

	public CalculatedConstraint(String expressionText) {
		this.expressionText = expressionText;
	}

	/**
	 * Values cannot be set
	 */
	@Override
    public void check(Object toCheck) {
		throw new RuntimeException("Value is calculated and cannot be set");
	}

	@Override
    public String getName() {
		return NAME;
	}

	public String getValue() {
		return expressionText;
	}

	public void calculate() {
		// expression = new Expression(expressionText);
	}

	public Object getResult(@SuppressWarnings("unused") Instance homeAtom) {
		// return expression.getResult(homeAtom);
		return null;
	}

	public List<Instance> getDependencies() {
		// final DependencyVisitor visitor = new DependencyVisitor();
		// expression.accept(visitor);
		// return visitor.getDependencies();
		return null;
	}

	@Override
    public void toWiki(StringBuilder sb) {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
