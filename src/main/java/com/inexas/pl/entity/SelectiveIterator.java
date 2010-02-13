package com.inexas.pl.entity;

import java.util.*;
import com.inexas.util.*;

/**
 * This class will iterate over a heterogeneous collection of objects and stop
 * only at the members of a given type
 * 
 * @author keith
 * 
 * @param <T>
 *            the type of the class to return (which must implement/extend B)
 * @param <B>
 *            the base class
 */
public class SelectiveIterator<T, B> implements Iterator<T> {
	private final Iterator<B> baseIterator;
	private T next;
	private Class<? extends B> tClass;

	/**
	 * Create a new iterator
	 * 
	 * @param collection
	 *            the collection over which to iterator
	 * @param tClass
	 *            the class required
	 */
	public SelectiveIterator(Collection<B> collection, Class<? extends B> tClass) {
		this.baseIterator = collection.iterator();
		this.tClass = tClass;
	}

	public boolean hasNext() {
		getNext();
		return next != null;
	}

	public T next() {
		getNext();
		if(next == null) {
			throw new NoSuchElementException();
		}
		final T result = next;
		next = null;
		return result;
	}

	public void remove() {
		throw new NotImplementedException("SelectiveIterator.remove() not implemented");
	}

	@SuppressWarnings("unchecked")
	private void getNext() {
		while(next == null && baseIterator.hasNext()) {
			final B member = baseIterator.next();
			if(member.getClass() == tClass) {
				next = (T)member;
			}
		}
	}
}
