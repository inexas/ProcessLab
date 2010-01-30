package com.inexas.util;

public class UnexpectedCallException extends RuntimeException {
    private static final long serialVersionUID = 3530558253532823373L;

	public UnexpectedCallException() {
		super("I should never have been called");
    }
}
