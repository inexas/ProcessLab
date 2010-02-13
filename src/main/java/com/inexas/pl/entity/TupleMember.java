package com.inexas.pl.entity;

public interface TupleMember extends Visited {

	String getKey();

	Tuple getParentTuple();

	void register();

	void deregister();

	void recalculate();

}
