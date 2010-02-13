package com.inexas.pl.entity;

import java.util.*;

public class TupleList extends Instance implements TupleMember, Iterable<Tuple> {
	private static final long serialVersionUID = 5623139104711666541L;
	protected final TupleType type;
	private final List<Tuple> tuples = new ArrayList<Tuple>();
	private final List<Tuple> deletedTuples = new ArrayList<Tuple>();
	// todo Implement tuple list cardinalities
	private final Cardinality cardinality = Cardinality.ZERO2MANY;

	public TupleList(TupleType type) {
		super(type.getKey());
		this.type = type;
	}

	public TupleList(TupleType type, Tuple parent) {
		super(type.getKey(), parent);
		assert parent != null : "If ya pass me a parent it can't be null";
		this.type = type;
	}

	public TupleType getType() {
		return type;
	}

	public Tuple add(int at) {
		final Tuple result = new Tuple(type, parentTuple, this);
		result.recalculate();
		result.register();
		tuples.add(at, result);
		for(int i = at; i < tuples.size(); i++) {
			tuples.get(i).setOrdinal(i);
		}
		return result;
	}

	public void append(Tuple tuple) {
		tuple.register();
		tuple.setOrdinal(tuples.size());
		tuples.add(tuple);
	}

	public Tuple append() {
		final Tuple result = new Tuple(type, parentTuple, this);
		result.recalculate();
		append(result);
		return result;
	}

	@Override
	public String toString() {
		return "TupleList:" + getKey() + tuples;
	}

	public void accept(Visitor visitor) {
		visitor.enter(this);
		for(final Visited visited : tuples) {
			visited.accept(visitor);
		}
		visitor.exit(this);
	}

	public int size() {
		return tuples.size();
	}

	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

	/**
	 * @param tuple
	 * @return true if the element was removed
	 */
	public boolean remove(Tuple tuple) {
		final boolean result;
		if(tuples.contains(tuple)) {
			tuple.deregister();
			result = tuples.remove(tuple);
			deletedTuples.add(tuple);
		} else {
			result = false;
		}
		return result;
	}

	public Tuple get(int index) {
		return tuples.get(index);
	}

	public Cardinality getCardinality() {
		return cardinality;
	}

	public void register() {
		for(final Tuple tuple : tuples) {
			tuple.register();
		}
	}

	public void recalculate() {
		for(final Tuple tuple : tuples) {
			tuple.recalculate();
		}
	}

	public void deregister() {
		for(final Tuple tuple : tuples) {
			tuple.deregister();
		}
	}

	public List<Tuple> getDeletedTuples() {
		return deletedTuples;
	}

}
