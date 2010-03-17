package com.inexas.pl.entity;

/**
 * The date input format constraint is used to generate the control that the
 * user is presented to input a date/time. Any combination of "YYYY", "MM",
 * "DD", "HH", "MM", "SS" is valid
 * 
 * @author keith
 */
public class DateInputFormatConstraint extends Constraint {
	public static final String NAME = "date.input.format";
	private final String pattern;

	public DateInputFormatConstraint(String pattern) {
		this.pattern = pattern;
	}

	@Override
    public void check(Object toCheck) {
		// nothing to do
	}

	@Override
    public String getName() {
		return NAME;
	}

	public String getValue() {
		return pattern;
	}

	@Override
    public void toWiki(StringBuilder sb) {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
