package com.inexas.pl.entity;

import java.util.*;

public class Tuple extends Instance implements Visited {
	protected final Map<String, TupleMember> members = new HashMap<String, TupleMember>();
	private final TupleType tupleType;
	private final TupleList parentTupleList;
	private int id, ordinal;
	private boolean dirty, deleted;

	public Tuple(TupleType type) {
		this(type, null, null);
	}

	public Tuple(TupleType type, Tuple parentTuple) {
		this(type, parentTuple, null);
	}

	public Tuple(TupleType type, Tuple parentTuple, TupleList parentTupleList) {
		super(type.getKey(), parentTuple);
		this.tupleType = type;
		this.parentTupleList = parentTupleList;

		// Create all the members in the tuple...
		final List<TupleMember> tuplesAndTupleLists = new ArrayList<TupleMember>();
		for(final AbstractType tupleTypeMember : type.getMembers()) {
			final TupleMember member = tupleTypeMember.newInstance(this);
			if(!(member instanceof Ktcv<?>)) {
				tuplesAndTupleLists.add(member);
			}
			members.put(member.getKey(), member);
		}
	}

	@Override
	public String toString() {
		return "Tuple:" + tupleType.getKey() + ':' + members.toString();
	}

	@SuppressWarnings("unchecked")
	public Ktcv<? extends Object> getKtcv(String keyToGet) {
		return (Ktcv<? extends Object>)members.get(keyToGet);
	}

	public TupleList getTupleList(String keyToGet) {
		return (TupleList)members.get(keyToGet);
	}

	public TupleList getParentTupleList() {
		return parentTupleList;
	}

	public void accept(Visitor visitor) {
		visitor.enter(this);
		for(final TupleMember member : members.values()) {
			if(member instanceof TupleList) {
				((TupleList)member).accept(visitor);
			}
		}
		visitor.exit(this);
	}

	public Boolean getBoolean(String memberKey) {
		@SuppressWarnings("unchecked")
		final Ktcv<Boolean> ktcv = (Ktcv<Boolean>)members.get(memberKey);
		return ktcv.getValue();
	}

	public TupleMember getMember(String memberKey) {
		return members.get(memberKey);
	}

	public void recalculate() {
		for(final TupleMember member : members.values()) {
			member.recalculate();
		}
	}

	public void register() {
		for(final TupleMember member : members.values()) {
			member.register();
		}
	}

	public void deregister() {
		for(final TupleMember member : members.values()) {
			member.deregister();
		}
	}

	public Iterator<TupleList> getTupleListIterator() {
		return new SelectiveIterator<TupleList, TupleMember>(members.values(), TupleList.class);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Iterator<Ktcv<?>> getKtcvIterator() {
		return new SelectiveIterator<Ktcv<?>, TupleMember>(members.values(), Ktcv.class);
	}

	public TupleType getTupleType() {
		return tupleType;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setClean() {
		dirty = false;
	}

	public void setOrdinal(int ordinal) {
		if(this.ordinal != ordinal) {
			this.ordinal = ordinal;
			dirty = true;
		}
	}

	public int getOrdinal() {
		return ordinal;
	}

	public Entity getEntity() {
		final Entity result;
		if(parentTuple == null) {
			result = (Entity)this;
		} else {
			result = parentTuple.getEntity();
		}
		return result;
    }

	public boolean isDeleted() {
		return deleted || parentTuple.isDeleted();
    }

}
