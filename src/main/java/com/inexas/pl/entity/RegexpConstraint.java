package com.inexas.pl.entity;

import java.util.regex.*;

public class RegexpConstraint extends Constraint {
	public static final String NAME = "regexp";
	private Pattern regularExpression;

	@Override
    public String getName() {
		return NAME;
	}
	
	public RegexpConstraint(String regexpText) {
		try {
			regularExpression = Pattern.compile(regexpText);
		} catch(PatternSyntaxException e) {
			throw new RuntimeException("Invalid regular expression: " + regexpText, e);
		}
	}
	
	public Pattern getRegularExpression() {
		return regularExpression;
	}

	@Override
	public void check(Object toCheck) throws ConstraintViolationException {
		if(toCheck != null) {
			final Matcher matcher = regularExpression.matcher(toCheck.toString());
			if(!matcher.matches()) {
				throw new ConstraintViolationException(
						this,
						"Value '" + toCheck + "' does not match regexp: '" + regularExpression.pattern() + "'");
			}
		}
	}
	
	@Override
	public String toString() {
		return NAME + " " + regularExpression.pattern();
	}

	@Override
	public void toWiki(StringBuilder sb) {
		sb.append("regexp ");
		sb.append(regularExpression.toString());
	}

}
