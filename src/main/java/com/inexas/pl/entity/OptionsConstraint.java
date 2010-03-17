package com.inexas.pl.entity;

import java.util.*;

public class OptionsConstraint extends Constraint implements Iterable<Object> {
	public static final String NAME = "option";
	private final List<Object> options;
	private Object defaultOption;

	public OptionsConstraint(List<Object> options) {
		this.options = options;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	public void add(Object option) {
		options.add(option);
	}
	
	public Iterator<Object> iterator() {
		return options.iterator();
	}

	@Override
	public void check(Object toCheck) throws ConstraintViolationException {
		if(toCheck != null) {
			if(!options.contains(toCheck)) {
				throw new ConstraintViolationException(this, "Option does not exist: " + toCheck);
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for(Object value : options) {
			sb.append(delimiter);
			sb.append(NAME);
			if(value != null) {
				sb.append(' ');
				sb.append(value.toString());
			}
			delimiter = "; ";
		}
		return sb.toString();
	}

	public void setDefaultOption(Object defaultOption) {
		this.defaultOption = defaultOption;
	}
	
	public Object getDefaultOption() {
		return defaultOption;
	}

	@Override
	public void toWiki(StringBuilder sb) {
		// todo! Implement me! 
		throw new RuntimeException("Implement me!");
	}

}
