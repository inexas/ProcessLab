package com.inexas.pl.db;

public class DbRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2452781711932347039L;

    DbRuntimeException(String message) {
		super(message);
	}
	DbRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
