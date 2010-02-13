package com.inexas.pl.pl;

public class InvalidCardinalityException extends RuntimeException {
    private static final long serialVersionUID = 5520537819926741466L;

	public InvalidCardinalityException(String cardinality) {
		super("Invalid cardinality: " + cardinality == null ? "<null>" : cardinality);
	}

	public InvalidCardinalityException(int from, int to) {
		super("Invalid cardinality: " + from + ':' + to);
	}

}
