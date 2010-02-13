package com.inexas.pl.entity;

public class InvalidValueException extends RuntimeException {
	private static final long serialVersionUID = -1851683470406495064L;
	public final Object culpritValue;

	public InvalidValueException(Object culpritValue) {
		this.culpritValue = culpritValue;
	}

}
