package com.inexas.pl.entity;

import java.text.*;
import java.util.*;

/**
 * The date format constraint is for use with date KTCVs only. By default date
 * parsing and formatting is set to the current locale.
 * 
 * @author keith
 */
public class DateOutputFormatConstraint implements Constraint {
	public static final String NAME = "date.output.format";
	private final DateFormat parser, formatter;
	private final String format;

	/**
	 * Create a new date format constraint with a default locale parsing and
	 * formatting pattern.
	 * 
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint(String)
	 * @see #DateFormatConstraint(String,String)
	 */
	public DateOutputFormatConstraint() {
		parser = formatter = new SimpleDateFormat();
		format = "simple";
	}

	/**
	 * Create a new date format constraint with a matching parsing and
	 * formatting pattern. The patterns are the same as the SimpleDateFormat
	 * class
	 * 
	 * @param pattern
	 *            the parsing and formatting pattern
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint()
	 * @see #DateFormatConstraint(String)
	 * @see #DateFormatConstraint(String,String)
	 */
	public DateOutputFormatConstraint(String pattern) {
		parser = formatter = new SimpleDateFormat(pattern);
		format = pattern;
	}

	/**
	 * Create a new date format constraint with a different parsing and
	 * formatting pattern. The patterns are the same as the SimpleDateFormat
	 * class
	 * 
	 * @param parsePattern
	 *            the parsing pattern
	 * @param formatPattern
	 *            the formatting pattern
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint()
	 * @see #DateFormatConstraint(String)
	 */
	public DateOutputFormatConstraint(String parsePattern, String formatPattern) {
		parser = new SimpleDateFormat(parsePattern);
		formatter = new SimpleDateFormat(formatPattern);
		format = parsePattern + ", " + formatPattern;
	}

	public Date parse(String input) throws ParseException {
		return parser.parse(input);
	}

	public String format(Date output) {
		return formatter.format(output);
	}

	public String getName() {
		return NAME;
	}

	public void check(Object toCheck) {
		// nothing to do, all Dates have valid format
	}

	/**
	 * Return true if this date format contains time. This can be combined with
	 * isDate() if the format contains both date and time
	 * 
	 * @return true if this date format contains time
	 * @see #isDate()
	 */
	public boolean isTime() {
		// todo implement me!
		throw new RuntimeException("Implement me!");
	}

	/**
	 * Return true if this date format contains the date. This can be combined
	 * with isTime() if the format contains both date and time
	 * 
	 * @return true if this date format contains time
	 * @see #isTime()
	 */
	public boolean isDate() {
		// todo implement me!
		throw new RuntimeException("Implement me!");
	}

	@Override
	public int hashCode() {
		return NAME.hashCode();
	}

	@Override
	public boolean equals(Object rhsObject) {
		// try for a cheap true...
		if(this == rhsObject) {
			return true;
		}
		// try for a cheap false...
		if(rhsObject == null) {
			return false;
		}
		try {
			// check we have the same types...
			final DateOutputFormatConstraint rhs = (DateOutputFormatConstraint)rhsObject;
			// both LHS and RHS are the same types, check for an exact match...
			return formatter.equals(formatter) && parser.equals(rhs.parser);
		} catch(ClassCastException e) {
			// not the same types: false...
			return false;
		}
	}

	@Override
	public String toString() {
		return "DateFormatConstrain(" + format + ")";
	}

	public void toWiki(StringBuilder sb) {
		sb.append("dateformat ");
		sb.append(format);
	}

	public String getValue() {
		// todo Auto-generated method stub
		return null;
	}
}
