package com.inexas.util;

import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * The default logger formatter for Java logging is a pain because 1) It
 * produces 2 lines instead of 1, and 2) because the source file name
 * needs to be there in a format recognizable by Eclipse
 */
public class LogFormatter extends Formatter {
	 private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	 private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
	 
	@Override
	public String format(LogRecord record) {
		final StringBuilder sb = new StringBuilder();
		sb.append(dateFormatter.format(new Date(record.getMillis())));
		sb.append(' ');
		sb.append(record.getLevel());
		sb.append(' ');
		sb.append(record.getMessage());
		// Note there was a space between the square brackets and the 'at', maybe this
		// is needed for Eclipse?
		sb.append(" [at ");
		final String sourceClassName = record.getSourceClassName();
		sb.append(sourceClassName);
		sb.append('.');
		sb.append(record.getSourceMethodName());
		sb.append('(');
		sb.append(sourceClassName.substring(sourceClassName.lastIndexOf('.') + 1));
		// Need a line number here for Eclipse to parse the message into a link
		// but the record doesn't contain a real one so use anything (i.e. :1)
		sb.append(".java:1)]");
		sb.append(LINE_SEPARATOR);
		return sb.toString();
	}
}
