package com.inexas.pl.entity;

import java.util.*;
import java.util.logging.*;

public class GrepFilter implements Filter {
	private final int count;
	private final String[] tokens;

	public GrepFilter(String filters) {
		final StringTokenizer st = new StringTokenizer(filters, ",");
		count = st.countTokens();
		tokens = new String[count];
		int index = 0;
		while(st.hasMoreTokens()) {
			final String token = st.nextToken();
			tokens[index++] = token.trim().toUpperCase();
		}
    }

	public boolean isLoggable(LogRecord logRecord) {
		boolean result = false;
		final String message = logRecord.getMessage().toUpperCase();
		for(int i = 0; i < count; i++) {
			final String token = tokens[i];
			if(message.indexOf(token) >= 0) {
				result = true;
				break;
			}
		}
		return result;
	}

}
