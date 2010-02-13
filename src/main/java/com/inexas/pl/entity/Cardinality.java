package com.inexas.pl.entity;

import com.inexas.pl.pl.*;

/**
 * The cardinality of a tuple or KTCV.
 */
public class Cardinality {
	// Predefine the standard cardinalities to maximize reuse
	public static final int MANY = Integer.MAX_VALUE;
	public static final Cardinality ZERO2ONE = new Cardinality(0, 1);
	public static final Cardinality ZERO2MANY = new Cardinality(0, MANY);
	public static final Cardinality ONE2ONE = new Cardinality(1, 1);
	public static final Cardinality ONE2MANY = new Cardinality(1, MANY);
	private final int from;
	private final int to;
	// The following variables are used during parsing only
	private char ca[];
	private int count, index;

	/**
	 * Either from >= 0 and from <= to and to > 1 or runtime exception, take
	 * your pick.
	 * 
	 * @param from
	 *            minimum cardinality
	 * @param to
	 *            maximum cardinality
	 * @return a valid Cardinality
	 */
	public static Cardinality newInstance(int from, int to) throws InvalidCardinalityException {
		final Cardinality cardinality = new Cardinality(from, to);
		return getCardinality(cardinality);
	}

	/**
	 * I need a string containing a valid integer >= 0, ".." followed by either
	 * a '*' or another valid integer >= first integer and no spaces otherwise
	 * throw an exception
	 * 
	 * @param text
	 */
	public static Cardinality newInstance(String text) throws InvalidCardinalityException {
		final Cardinality cardinality = new Cardinality(text);
		return getCardinality(cardinality);
	}

	private static Cardinality getCardinality(Cardinality cardinality) throws InvalidCardinalityException {
		// Sanity check numbers...
		final int from = cardinality.from;
		final int to = cardinality.to;
		if(from < 0 || to < from || to < 1) {
			throw new InvalidCardinalityException(from, to);
		}

		final Cardinality returnValue;
		if(from == 0) {
			if(to == 1) {
				returnValue = ZERO2ONE;
			} else if(to == MANY) {
				returnValue = ZERO2MANY;
			} else {
				returnValue = cardinality;
			}
		} else if(from == 1) {
			if(to == 1) {
				returnValue = ONE2ONE;
			} else if(to == MANY) {
				returnValue = ONE2MANY;
			} else {
				returnValue = cardinality;
			}
		} else {
			returnValue = cardinality;
		}
		return returnValue;
	}

	/**
	 * Parse a string and return a cardinality
	 * 
	 * @param string
	 *            the string to parse
	 */
	private Cardinality(String string) throws InvalidCardinalityException {
		if(string == null) {
			throw new InvalidCardinalityException(string);
		}
		try {
			ca = string.toCharArray();
			count = ca.length;
			index = 0;

			// Get the first integer
			from = parseInteger();

			// Get a ".."
			if(!(ca[index++] == '.' && ca[index++] == '.')) {
				throw new InvalidCardinalityException(string);
			}

			// Get a '*' or another integer
			if(ca[index] == '*') {
				index++;
				to = MANY;
			} else {
				to = parseInteger();
			}
			ca = null;
		} catch(final Exception e) {
			throw new InvalidCardinalityException(string);
		}
		if(index != count) {
			throw new InvalidCardinalityException(string);
		}
	}

	private int parseInteger() {
		final StringBuilder sb = new StringBuilder();
		while(index < count) {
			final char c = ca[index];
			if(c >= '0' && c <= '9') {
				sb.append(c);
				index++;
			} else {
				break;
			}
		}
		return Integer.parseInt(sb.toString());
	}

	private Cardinality(int from, int to) throws InvalidCardinalityException {
		assert ONE2ONE == null || !(from == 1 && to == 1);

		if((from < 0) || (to < 1) || (from > to)) {
			throw new InvalidCardinalityException(from, to);
		}
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	@Override
	public boolean equals(Object rhsObject) {
		final boolean returnValue;

		if(this == rhsObject) {
			returnValue = true;
		} else if(rhsObject == null || !(rhsObject instanceof Cardinality)) {
			returnValue = false;
		} else {
			final Cardinality rhs = (Cardinality)rhsObject;
			returnValue = from == rhs.from && to == rhs.to;
		}

		return returnValue;
	}

	@Override
	public int hashCode() {
		return from * 100 + to;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(from);
		sb.append("..");
		sb.append(to == MANY ? "*" : Integer.toString(to));
		return sb.toString();
	}

	public boolean isFixed() {
		return from == to;
	}

	public boolean isValidCardinality(int candidate) {
		return candidate >= from && candidate <= to;
	}

}
