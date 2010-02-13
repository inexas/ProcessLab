package com.inexas.pl.entity;

public class ConstraintFactory {

	public static Constraint getConstraint(String name, String value) {
		final Constraint result;
		// todo Could improve this using a map and preconstructed constraints
		// where possible
		if(name.equals(DateInputFormatConstraint.NAME)) {
			result = new DateInputFormatConstraint(value);
		} else if(name.equals(ChoiceConstraint.NAME)) {
			result = new ChoiceConstraint(value);
		} else if(name.equals(ReadOnlyConstraint.NAME)) {
			result = new ReadOnlyConstraint();
		} else if(name.equals(NullableConstraint.NAME)) {
			result = new NullableConstraint();
		} else if(name.equals(CalculatedConstraint.NAME)) {
			result = new CalculatedConstraint(value);
		} else if(name.equals(RegexpConstraint.NAME)) {
			result = new RegexpConstraint(value);
		} else {
			throw new RuntimeException("Unhandled constraint type: " + name);
		}
		return result;
	}

}
