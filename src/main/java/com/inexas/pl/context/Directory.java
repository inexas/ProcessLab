package com.inexas.pl.context;

public class Directory {
	private final Context root;

	public Directory() {
		root = new Context(Context.ROOT_KEY, "");
    }
	
	public Context getRoot() {
	    return root;
    }
	
	public static boolean isValidName(String candidate) {
		// LETTER [[ALPHA | LETTER | '_'])+
		boolean result;
		if(candidate == null) {
			result = false;
		} else {
			final char[] ca = candidate.toCharArray();
			final int length = ca.length;
			if(length == 0) {
				result = false;
			} else {
				if(!Character.isLetter(ca[0])) {
					result = false;
				} else {
					result = true;
					for(int i = 1; i < length; i++) {
						final char c = ca[i];
						if(Character.isLetterOrDigit(c) && c != '_') {
							result = false;
							break;
						}
					}
				}
			}
		}
		return result;
	}
}
