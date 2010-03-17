package com.inexas.pl.entity;

/**
 * A model Key Type Constraints Value
 * 
 * @author keith
 * 
 */
public class Ktcv<T> extends Instance implements TupleMember, ChangeListener {
	private final KtcvType<T> type;
	// private final CalculatedConstraint calculatedConstraint;
	private T value;
	private boolean dirty;

	/**
	 * Standalone KTCV ctor
	 * 
	 * @param type
	 */
	public Ktcv(KtcvType<T> type) {
		super(type.getKey());
		this.type = type;
		setValue(type.getInitialValue(this));
		// calculatedConstraint = (CalculatedConstraint)
		// type.getConstraint(CalculatedConstraint.NAME);
	}

	/**
	 * Member of a tuple ctor
	 * 
	 * @param type
	 * @param parent
	 */
	public Ktcv(KtcvType<T> type, Tuple parent) {
		super(type.getKey(), parent);
		this.type = type;
		setValue(type.getInitialValue(this));
		// calculatedConstraint = (CalculatedConstraint)
		// type.getConstraint(CalculatedConstraint.NAME);
	}

	@SuppressWarnings("unchecked")
    public Ktcv(String key, T initialValue) {
		super(key);
		this.type = (KtcvType<T>)KtcvType.getKtcvType(DataType.STRING, key);
		setValue(initialValue);
		// calculatedConstraint = null;
	}

	public KtcvType<T> getType() {
		return type;
	}

	public void dispose() {
		// nothing to do...
	}

	/**
	 * Set a new value. The value is checked to be valid.
	 * 
	 * @param newValue
	 *            the new value to set
	 * @return true if the value has changed
	 * @throws InvalidValueException
	 *             if the value was invalid
	 */
	public boolean setValue(T newValue) throws InvalidValueException {
		final boolean result;

		// Only do work if there is a change...
		if(value == null) {
			if(newValue == null) {
				result = false;
			} else {
				type.throwIfInvalid(newValue.toString());
				result = true;
			}
		} else { // ...old value is not null...
			if(value.equals(newValue)) {
				result = false;
			} else {
				type.throwIfInvalid(newValue == null ? null : newValue.toString());
				result = true;
			}
		}

		if(result) {
			value = newValue;
			fire(new Event(this, Event.CHANGE_VALUE));
			dirty = true;
		}

		return result;
	}

//	public void parseAndSetValue(String newValue) {
//		setValue(type.parse(newValue));
//	}

	public T getValue() {
		return value;
	}

	public String getValueAsString() {
		return value == null ? null : value.toString();
	}

	public void register() {
		// if(calculatedConstraint != null) {
		// final List<Instance> dependencies =
		// calculatedConstraint.getDependencies();
		// for(final Instance dependency : dependencies) {
		// dependency.addChangeListener(this);
		// }
		// }
	}

	public void deregister() {
		// final List<Instance> dependencies =
		// calculatedConstraint.getDependencies();
		// for(final Instance dependency : dependencies) {
		// dependency.addChangeListener(this);
		// }
	}

	public void recalculate() {
		// if(calculatedConstraint != null) {
		// @SuppressWarnings("unchecked")
		// final T newValue = (T)calculatedConstraint.getResult(this);
		// value = newValue;
		// }
	}

	/**
	 * We are called because the value is calculated and therefore we have
	 * registered for change events in our dependencies.
	 */
	public void handle(Event event) {
		if(event.type == Event.CHANGE_VALUE) {
			// @SuppressWarnings("unchecked")
			// final T newValue = (T)calculatedConstraint.getResult(this);
			// setValue(newValue);
		} else {
			throw new RuntimeException("How about implementing me?! " + event.type);
		}
	}

	@Override
	public final int hashCode() {
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public boolean equals(final Object rhsObject) {
		final boolean returnValue;
		// try for a cheap true...
		if(this == rhsObject) {
			returnValue = true;
			// try for a cheap false...
		} else if(rhsObject == null || !(rhsObject instanceof Ktcv<?>)) {
			returnValue = false;
		} else {
			// We know it's not null and the same type
			@SuppressWarnings("unchecked")
			final Ktcv<T> rhs = (Ktcv<T>)rhsObject;
			if(value == null) {
				returnValue = rhs.value == null;
			} else {
				returnValue = rhs.value != null && value.equals(rhs.value);
			}
		}
		return returnValue;
	}

	@Override
	public final String toString() {
		return getClass().getName() + "('" + key + ':' + value + "')";
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setClean() {
		dirty = false;
	}

	public boolean isReadWrite() {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
