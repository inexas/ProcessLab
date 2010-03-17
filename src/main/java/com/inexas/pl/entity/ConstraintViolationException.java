package com.inexas.pl.entity;


public class ConstraintViolationException extends RuntimeException {
	private static final long serialVersionUID = 244331087473077572L;
	private final Constraint constraint;

	/**
	 * @param message
	 */
	public ConstraintViolationException(Constraint constraint, String message) {
		this(constraint, message, null);
	}

	/**
	 * @param message
	 * @param chainedException
	 */
	public ConstraintViolationException(Constraint constraint, String message, Throwable chainedException) {
		super("Constraint violation: " + constraint + ", " + message, chainedException);
		this.constraint = constraint;
	}

	protected Constraint getConstraint() {
		return constraint;
	}

}
