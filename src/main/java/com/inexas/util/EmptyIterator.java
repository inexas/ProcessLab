package com.inexas.util;

import java.util.*;

/**
 * A simple class that can be used to return an iterator that has no entries.
 * 
 * @author keith
 *
 * @param <T>
 */
public class EmptyIterator<T> implements Iterator<T> {
	
	public boolean hasNext() {
	    return false;
    }

	public T next() {
		throw new NoSuchElementException();
    }

	public void remove() {
		throw new NotImplementedException("No elements to return");
    }

}
