package com.inexas.pl.entity;

import java.util.*;

// todo Decide if I should implement different classes for bag, set, ordered set and list
/**
 * A model Key Type Constraints Value collection
 * 
 * @author keith
 * 
 */
public class Instances<T> extends Instance implements TupleMember, Iterable<T> {
	private final AbstractType type;
	private final int from, to;
	private List<T> values = new ArrayList<T>();

	/**
	 * Standalone ctor
	 * 
	 * @param type
	 */
	protected Instances(AbstractType type) {
		this(type, null);
	}

	/**
	 * Member of a tuple or model ctor
	 * 
	 * @param type
	 * @param parent
	 */
	public Instances(AbstractType type, Tuple parent) {
		super(type.getKey(), parent);
		this.type = type;
		from = type.getCardinality().getFrom();
		to = type.getCardinality().getTo();
	}

	public AbstractType getType() {
		return type;
	}

	public void dispose() {
		// nothing to do...
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public boolean set(int index, T newValue) {
		final boolean result;
		final T oldValue = values.get(index);
		if(checkValue(oldValue, newValue)) {
			values.set(index, newValue);
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	public String getValueAsString(int index) {
		final T returnValue = values.get(index);
		return returnValue == null ? null : returnValue.toString();
	}

	public boolean append(T newValue) {
		final int index = values.size();
		if(index > to) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		checkValue(null, newValue);
		values.add(newValue);
		return true;
	}

	/**
	 * Removes the element at the specified position in this list (optional
	 * operation). Shifts any subsequent elements to the left (subtracts one
	 * from their indices). Returns the element that was removed from the list.
	 * 
	 * @param index
	 * @return the element that was removed from the collection
	 */
	public T remove(int index) {
		final T returnValue = values.remove(index);
		return returnValue;
	}

	/**
	 * Removes the first occurrence in this list of the specified element. If
	 * this list does not contain the element, it is unchanged. More formally,
	 * removes the element with the lowest index i such that (o==null ?
	 * get(i)==null : o.equals(get(i))) (if such an element exists).
	 * 
	 * @param toRemove
	 *            element to remove
	 * @return true if an element was removed
	 */
	public boolean remove(T toRemove) {
		if(values.size() <= from) {
			throw new RuntimeException("Insufficient elements to remove(): " + getFullPath());
		}
		final boolean returnValue = values.remove(toRemove);
		return returnValue;
	}

	public boolean contains(T element) {
		return values.contains(element);
	}

	public int indexOf(T element) {
		return values.indexOf(element);
	}

	public int size() {
		return values.size();
	}

	@Override
	public final int hashCode() {
		return values.hashCode();
	}

	@Override
	public final String toString() {
		return getClass().getName() + "('" + values + "')";
	}

	public void clear() {
		if(from > 0) {
			throw new RuntimeException("Cannot clear KTCV collections with non-zero 'from' cardinality");
		}
		values.clear();
	}

	public T get(int index) {
		return values.get(index);
	}

	/**
	 * This method is called before the value of the KTCV is changed. We check
	 * that the new value is valid.
	 * 
	 * @param currentValue
	 * @param valueToCheck
	 * @return true if the new value is valid and changes the state of the KTCV
	 * @throws RuntimeException
	 *             if the valid is invalid
	 */
	private boolean checkValue(T currentValue, T valueToCheck) {
		final boolean returnValue;
		if(currentValue == null) {
			if(valueToCheck != null) {
				((KtcvType<?>)type).throwIfInvalid(valueToCheck.toString()); // Throws
																			 // if
																			 // error
				returnValue = true;
			} else {
				// It's debatable that we should throw if the type is not
				// nullable
				returnValue = false;
			}
		} else if(!currentValue.equals(valueToCheck)) {
			// Throws if error
			((KtcvType<?>)type).throwIfInvalid(valueToCheck == null ? null : valueToCheck.toString());
			returnValue = true;
		} else {
			returnValue = false;
		}
		return returnValue;
	}

	@Override
	public boolean equals(Object object) {
		return values.equals(object);
	}

	public Iterator<T> iterator() {
		// todo This should probably be a List iterator
		return values.iterator();
	}

	/**
	 * Replace the current values in the collection with the new values
	 * 
	 * @param values
	 */
	public void setValues(List<String> newValues) {

		// Check the cardinality is in range...
		final int size = newValues.size();
		if(size < from || size > to) {
			throw new RuntimeException("Can't replace. Size = " + size + ", cardinality is: " + type.getCardinality());
		}

		// Convert the strings to objects and check them against constraints...
		final List<T> objects = new ArrayList<T>();
//		for(final String newValue : newValues) {
//			@SuppressWarnings("unchecked")
//			final KtcvType<T> ktcvType = (KtcvType<T>)type;
//			final T object = ktcvType.parse(newValue);
//			// Throws if there's an error
//			ktcvType.throwIfInvalid(object == null ? null : object.toString());
//			objects.add(object);
//		}

		// todo Check for uniqueness here...

		// We got here so it's OK...
		values = objects;
	}

	public void recalculate() {
		// Nothing to do (I think)...
	}

	public void deregister() {
		throw new RuntimeException("How about implementing me?!");
	}

	public void register() {
		// Nothing to do (I think)...
	}

}
