package com.inexas.pl.entity;

/**
 * The date input format constraint is used to generate the control that the
 * user is presented to input a date/time. Any combination of "YYYY", "MM",
 * "DD", "HH", "MM", "SS" is valid
 * 
 * @author keith
 */
public class DateInputFormatConstraint implements Constraint {
	public static final String NAME = "date.input.format";
	private final String pattern;

	public DateInputFormatConstraint(String pattern) {
		this.pattern = pattern;
	}

	public void check(Object toCheck) throws Exception {
		// nothing to do
	}

	public String getName() {
		return NAME;
	}

	public String getValue() {
		return pattern;
	}

}
