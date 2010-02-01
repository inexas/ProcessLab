package com.inexas.pl.scheduler;

import java.util.*;

public class Parser {
	private final String input;
	private final boolean[] minutes, hours, days, dates, months, years;
	private final Record record;
	private final boolean isYear;
	private int nextToken, length, min, max, number;
	private Token[] tokenStream;
	private boolean[] ba;

	private static enum TokenType {
		STAR, MINUS, SLASH, COMMA, NUMBER
	}

	private static class Token {
		public final static Token STAR = new Token(TokenType.STAR);
		public final static Token MINUS = new Token(TokenType.MINUS);
		public final static Token SLASH = new Token(TokenType.SLASH);
		public final static Token COMMA = new Token(TokenType.COMMA);
		public final TokenType type;
		public final StringBuilder sb;

		public Token(TokenType type) {
			this.type = type;
			sb = type == TokenType.NUMBER ? new StringBuilder() : null;
		}

		public void add(char c) {
			sb.append(c);
		}

		public int getInt() {
			return Integer.parseInt(sb.toString());
		}

		@Override
		public String toString() {
			final String result;
			switch(type) {
			case STAR:
				result = "*";
				break;
			case MINUS:
				result = "-";
				break;
			case SLASH:
				result = "/";
				break;
			case COMMA:
				result = ",";
				break;
			case NUMBER:
				result = sb.toString();
				break;
			default:
				throw new RuntimeException("Should never happen: " + type);
			}
			return result;
		}

	}

	public final class ParsingException extends Exception {
		private static final long serialVersionUID = 2857322098237090265L;

		public ParsingException(String message) {
			super(message + ": '" + input + '\'');
		}
	}

	public Parser(String input) throws ParsingException {
		this.input = input;

		final StringTokenizer st = new StringTokenizer(input);
		final int tokenCount = st.countTokens();
		if(tokenCount == 1) {
			// Try for a schedule name
			final String name = st.nextToken();
			final String cron = Scheduler.getInstance().getNamedSchedule(name);
			if(cron == null) {
				throw new ParsingException("Named schedule not found: " + name);
			}
			final Parser subParser = new Parser(cron);
			minutes = subParser.minutes;
			hours = subParser.hours;
			days = subParser.days;
			dates = subParser.dates;
			months = subParser.months;
			isYear = subParser.isYear;
			years = subParser.years;
			record = subParser.record;
		} else if(tokenCount == 6) {
			minutes = parseList(st.nextToken(), 0, 59);
			hours = parseList(st.nextToken(), 0, 23);
			days = parseList(st.nextToken(), 0, 7);
			dates = parseList(st.nextToken(), 1, 31);
			months = parseList(st.nextToken(), 1, 12);
			isYear = true;
			years = parseList(st.nextToken(), 0, 49);
			if(days[0] || days[7]) {
				// Both 0 and 7 are Sunday
				days[0] = true;
				days[7] = true;
			}
			
			record = new Record(minutes, hours, days, dates, months, years);
		} else {
			throw new ParsingException("Invalid number of tokens");
		}
		ba = null;
		tokenStream = null;
	}

	public Record getRecord() {
    	return record;
    }

	@Override
	public String toString() {
		/*
		 * Reconstruct the string from the boolean arrays. We could, of course,
		 * have save the input string but what fun would that be? Besides this
		 * is very good for testing.
		 */
		final StringBuilder sb = new StringBuilder();
		toString(sb, minutes, 0);
		sb.append(' ');
		toString(sb, hours, 0);
		sb.append(' ');
		toString(sb, days, 0);
		sb.append(' ');
		toString(sb, dates, 1);
		sb.append(' ');
		toString(sb, months, 1);
		sb.append(' ');
		toString(sb, years, 0);
		return sb.toString();
	}

	private void toString(StringBuilder sb, boolean[] booleanArray, int from) {
		final int baLength = booleanArray.length;
		int correction = baLength == years.length ? 2000 : 0;
		// Try for a *...
		boolean soFarSoGood = true;
		for(int i = from; i < baLength && soFarSoGood; i++) {
			if(!booleanArray[i]) {
				soFarSoGood = false;
			}
		}
		if(soFarSoGood) {
			sb.append('*');
		} else {
			String delimiter = "";
			for(int i = from; i < baLength; i++) {
				if(booleanArray[i]) {
					sb.append(delimiter);
					delimiter = ",";
					sb.append(i + correction);
					// Try for a range...
					int count = 1;
					for(int j = i + 1; j < baLength && booleanArray[j]; j++) {
						count++;
					}
					if(count > 2) {
						sb.append('-');
						i += count - 1;
						sb.append(i + correction);
					}
				}
			}
		}
	}

	private boolean[] parseList(String subToken, int currentMin, int currentMax) throws ParsingException {
		final boolean[] result = ba = new boolean[currentMax + 1];

		lex(subToken);
		this.min = currentMin;
		this.max = currentMax;

		while(true) {
			if(consume(TokenType.STAR)) {
				number = 0;
				if(!handleStep()) {
					// It was a "*", fill with trues and done...
					Arrays.fill(result, true);
				}
			} else if(consume(TokenType.NUMBER)) {
				final int firstNumber = number;
				if(consume(TokenType.MINUS)) {
					// Must be range: "n-n", process it
					expect(TokenType.NUMBER);
					setRangeTrue(firstNumber, number);
				} else if(handleStep()) {
					// Nothing to do...
				} else {
					// It's a single number
					setRangeTrue(number, number);
				}
			} else {
				throw new ParsingException("Invalid input");
			}
			if(EOTS()) {
				break;
			}
			expect(TokenType.COMMA);
		}
		return result;
	}

	private boolean handleStep() throws ParsingException {
		final boolean result;
		final int firstNumber = number;
		if(consume(TokenType.SLASH)) {
			expect(TokenType.NUMBER);
			final int modulo = firstNumber + number;
			for(int i = min; i < max; i++) {
				if(((i + firstNumber) % modulo) == 0) {
					ba[i] = true;
				}
			}
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	private void expect(TokenType type) throws ParsingException {
		if(!consume(type)) {
			throw new ParsingException(
			        "Expecting '" + type + " but got '" +
			        (nextToken == length ? "end of string" : tokenStream[nextToken].toString()));
		}
	}

	private boolean consume(TokenType type) {
		final boolean result;
		if(nextToken < length) {
			final Token token = tokenStream[nextToken];
			if(token.type == type) {
				if(type == TokenType.NUMBER) {
					number = token.getInt();
				}
				result = true;
				nextToken++;
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Test for end of token stream
	 * 
	 * @return
	 */
	private boolean EOTS() {
		return nextToken == length;
	}

	private void setRangeTrue(int first, int last) throws ParsingException {
		final int f, l;
		if(isYear) {
			f = first - 2000;
			l = last - 2000;
		} else {
			f = first;
			l = last;
		}
		if(f < min || f > last || l > max) {
			throw new ParsingException("Invalid range: " + first + '-' + last);
		}
		for(int i = f; i <= l; i++) {
			ba[i] = true;
		}
	}

	/**
	 * Lex a sub token: e.g. something like "1,2,3-6" into a token string and
	 * set up ready for parsing
	 */
	private void lex(String subToken) throws ParsingException {
		// Lex a token stream...
		final List<Token> tokenStreamList = new ArrayList<Token>();
		final char[] ca = subToken.toCharArray();
		Token numberToken = null;
		for(final char c : ca) {
			switch(c) {
			case '*':
				tokenStreamList.add(Token.STAR);
				numberToken = null;
				break;
			case '-':
				tokenStreamList.add(Token.MINUS);
				numberToken = null;
				break;
			case '/':
				tokenStreamList.add(Token.SLASH);
				numberToken = null;
				break;
			case ',':
				tokenStreamList.add(Token.COMMA);
				numberToken = null;
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if(numberToken == null) {
					numberToken = new Token(TokenType.NUMBER);
					tokenStreamList.add(numberToken);
				}
				numberToken.add(c);
				break;
			default:
				throw new ParsingException("Invalid character '" + c + "' in token");
			}
		}

		// Set up ready for parser...
		tokenStream = tokenStreamList.toArray(new Token[tokenStreamList.size()]);
		nextToken = 0;
		length = tokenStream.length;
	}

}
