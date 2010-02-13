package com.inexas.util;
/**
 * 
 * @author KeithWhittingham
 * @version $Revision: 1.1 $
 */
public class UnexpectedException extends RuntimeException {
	private static final long serialVersionUID = 8637197045486771888L;

	/**
	 * @param message
	 */
	public UnexpectedException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param chainedException
	 */
	public UnexpectedException(String message, Exception chainedException) {
		super(message, chainedException);
	}

}
