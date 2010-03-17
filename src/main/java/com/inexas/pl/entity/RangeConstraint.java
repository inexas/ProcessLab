package com.inexas.pl.entity;

import java.util.*;

/**
 * Check a number lies within a given range.
 * 
 * @author kgwhittingham
 *
 */
public class RangeConstraint extends Constraint {
	public static final String NAME = "range";
	private Object minimumObject, maximumObject;
	public static enum Type {
		INTEGER,
		SHORT,
		LONG,
		FLOAT,
		DOUBLE,
		DATE
	}
	private Type type;

	public RangeConstraint(Double minimumObject, Double maximumObject) {
		this.type = Type.DOUBLE;
		this.minimumObject = minimumObject;
		this.maximumObject = maximumObject;
	}
	
	public RangeConstraint(Float minimumObject, Float maximumObject) {
		this.type = Type.FLOAT;
		this.minimumObject = minimumObject;
		this.maximumObject = maximumObject;
	}
	
	public RangeConstraint(Integer minimumObject, Integer maximumObject) {
		this.type = Type.INTEGER;
		this.minimumObject = minimumObject;
		this.maximumObject = maximumObject;
	}
	
	public RangeConstraint(Long minimumObject, Long maximumObject) {
		this.type = Type.LONG;
		this.minimumObject = minimumObject;
		this.maximumObject = maximumObject;
	}
	
	public RangeConstraint(Short minimumObject, Short maximumObject) {
		this.type = Type.SHORT;
		this.minimumObject = minimumObject;
		this.maximumObject = maximumObject;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void check(Object toCheckObject) throws ConstraintViolationException {
		if(toCheckObject != null) {
			switch(type) {
				case INTEGER: {
					final int toCheck = ((Integer)toCheckObject).intValue();
					final int minimum = ((Integer)minimumObject).intValue();
					final int maximum = ((Integer)maximumObject).intValue();
					if(toCheck < minimum || toCheck > maximum) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
					
				case SHORT: {
					final short toCheck = ((Short)toCheckObject).shortValue();
					final short minimum = ((Short)minimumObject).shortValue();
					final short maximum = ((Short)maximumObject).shortValue();
					if(toCheck < minimum || toCheck > maximum) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
					
				case LONG: {
					final long toCheck = ((Long)toCheckObject).longValue();
					final long minimum = ((Long)minimumObject).longValue();
					final long maximum = ((Long)maximumObject).longValue();
					if(toCheck < minimum || toCheck > maximum) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
					
				case FLOAT: {
					final float toCheck = ((Float)toCheckObject).floatValue();
					final float minimum = ((Float)minimumObject).floatValue();
					final float maximum = ((Float)maximumObject).floatValue();
					if(toCheck < minimum || toCheck > maximum) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
					
				case DOUBLE: {
					final double toCheck = ((Double)toCheckObject).doubleValue();
					final double minimum = ((Double)minimumObject).doubleValue();
					final double maximum = ((Double)maximumObject).doubleValue();
					if(toCheck < minimum || toCheck > maximum) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
					
				case DATE: {
					final Date toCheck = ((Date)toCheckObject);
					final Date minimum = ((Date)minimumObject);
					final Date maximum = ((Date)maximumObject);
					if(toCheck.compareTo(minimum) < 0 || toCheck.compareTo(maximum) > 0) {
						throw new ConstraintViolationException(
								this,
								"Value out of range: " + minimum + " <= " + toCheck + " <= " + maximum);
					}
				}
				break;
			}
		}
	}
	
	public void setMinimum(Double minimumObject) {
		this.minimumObject = minimumObject;
	}
	
	public Double getMinimumDouble() {
		return (Double)minimumObject;
	}

	public void setMaximum(Double maximumObject) {
		this.maximumObject = maximumObject;
	}

	public Double getMaximumDouble() {
		return (Double)maximumObject;
	}

	public void setMinimum(Float minimumObject) {
		this.minimumObject = minimumObject;
	}
	
	public Float getMinimumFloat() {
		return (Float)minimumObject;
	}

	public void setMaximum(Float maximumObject) {
		this.maximumObject = maximumObject;
	}

	public Float getMaximumFloat() {
		return (Float)maximumObject;
	}

	public void setMinimum(Integer minimumObject) {
		this.minimumObject = minimumObject;
	}
	
	public Integer getMinimumInteger() {
		return (Integer)minimumObject;
	}

	public void setMaximum(Integer maximumObject) {
		this.maximumObject = maximumObject;
	}

	public Integer getMaximumInteger() {
		return (Integer)maximumObject;
	}

	public void setMinimum(Long minimumObject) {
		this.minimumObject = minimumObject;
	}
	
	public Long getMinimumLong() {
		return (Long)minimumObject;
	}

	public void setMaximum(Long maximumObject) {
		this.maximumObject = maximumObject;
	}

	public Long getMaximumLong() {
		return (Long)maximumObject;
	}

	public void setMinimum(Short minimumObject) {
		this.minimumObject = minimumObject;
	}
	
	public Short getMinimumShort() {
		return (Short)minimumObject;
	}

	public void setMaximum(Short maximumObject) {
		this.maximumObject = maximumObject;
	}

	public Short getMaximumShort() {
		return (Short)maximumObject;
	}

	@Override
	public int hashCode() {
		return NAME.hashCode();
	}

	@Override
	public boolean equals(Object rhsObject) {
		boolean returnValue;
		if(this == rhsObject) {			// try for a cheap true...
			returnValue = true;
		} else if(rhsObject == null) {	// try for a cheap false...
			returnValue = false;
		} else {
			try {
				// check we have the same types...
				final RangeConstraint rhs = (RangeConstraint)rhsObject;
				// both lhs and rhs are the same types, check for an exact match...
				returnValue = equal(minimumObject, rhs.minimumObject) && equal(maximumObject, rhs.maximumObject);
			} catch(ClassCastException e) {
				// not the same types: false...
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	private boolean equal(Object lhs, Object rhs) {
		if(lhs == null) {
			return rhs == null;
		}
		return lhs.equals(rhs);
	}

	@Override
	public String toString() {
		return "Range(" + minimumObject + ", " + maximumObject + ")";
	}

	@Override
	public void toWiki(StringBuilder sb) {
		sb.append("range ");
		sb.append(minimumObject.toString());
		sb.append("..");
		sb.append(maximumObject.toString());
	}
}
