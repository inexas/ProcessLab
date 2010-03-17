package com.inexas.pl.entity;

import java.text.*;
import java.util.*;

/**
 * The date format constraint is for use with date NTCVs only. 
 * By default date parsing and formatting is set to the current
 * locale.
 */
public class DateFormatConstraint extends Constraint {
	public static final String NAME = "dateformat";
	private final DateFormat parser, formatter;
	private final String format;
	
	/**
	 * Create a new date format constraint with a default locale
	 * parsing and formatting pattern.
	 * 
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint(String)
	 * @see #DateFormatConstraint(String,String)
	 */
	public DateFormatConstraint() {
		parser = formatter = new SimpleDateFormat();
		format = "simple";
	}
	
	/**
	 * Create a new date format constraint with a matching parsing
	 * and formatting pattern. The patterns are the same as the 
	 * SimpleDateFormat class
	 * 
	 * @param pattern the parsing and formatting pattern
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint()
	 * @see #DateFormatConstraint(String)
	 * @see #DateFormatConstraint(String,String)
	 */
	public DateFormatConstraint(String pattern) {
		parser = formatter = new SimpleDateFormat(pattern);
		format = pattern;
	}
	
	/**
	 * Create a new date format constraint with a different parsing
	 * and formatting pattern. The patterns are the same as the 
	 * SimpleDateFormat class
	 * 
	 * @param parsePattern the parsing pattern
	 * @param formatPattern the formatting pattern
	 * @see SimpleDateFormat
	 * @see #DateFormatConstraint()
	 * @see #DateFormatConstraint(String)
	 */
	public DateFormatConstraint(String parsePattern, String formatPattern) {
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

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void check(Object toCheck) {
		// nothing to do, all Dates have valid format
	}

	/**
	 * Return true if this date format contains time. This can
	 * be combined with isDate() if the format contains both
	 * date and time
	 * 
	 * @return true if this date format contains time
	 * @see #isDate()
	 */
	public boolean isTime() {
		// todo implement me!
		throw new RuntimeException("Implement me!");
	}

	/**
	 * Return true if this date format contains the date. This can
	 * be combined with isTime() if the format contains both
	 * date and time
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
			final DateFormatConstraint rhs = (DateFormatConstraint)rhsObject;
			// both lhs and rhs are the same types, check for an exact match...
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

	@Override
	public void toWiki(StringBuilder sb) {
		sb.append("dateformat ");
		sb.append(format);
	}
}
