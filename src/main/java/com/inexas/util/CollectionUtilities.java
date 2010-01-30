package com.inexas.util;

import java.util.*;

public class CollectionUtilities {
	public static <T> T get(Set<T> set, T object) {
		for(final T t : set) {
			if(object.equals(t)) {
				return t;
			}
		}
		return null;
	}
}
