package com.inexas.pl.entity;

import java.util.*;

/**
 * Choice is a set of strings that are read in from the constraints that limit
 * the set of values
 */
public class ChoiceConstraint implements Constraint {
	public final static String NAME = "choice";
	private final String choice[];
	private final String value;

	public ChoiceConstraint(String value) {
		this.value = value;
		final StringTokenizer st = new StringTokenizer(value, ";");
		final int count = st.countTokens();
		choice = new String[count];
		for(int i = 0; i < count; i++) {
			choice[i] = st.nextToken();
		}
	}

	public void check(Object toCheck) throws Exception {
		boolean found = false;
		for(int i = choice.length; --i >= 0;) {
			if(choice[i].equals(toCheck)) {
				found = true;
				break;
			}
		}
		if(!found) {
			throw new RuntimeException(
			        "Invalid choice for value: '" + value +
			        "', choice: " + Arrays.asList(choice));
		}
	}

	public String getName() {
		return NAME;
	}

	public String getValue() {
		return value;
	}

	public String[] getChoice() {
		return choice;
	}

}
