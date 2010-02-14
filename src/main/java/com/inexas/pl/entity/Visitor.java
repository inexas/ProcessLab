package com.inexas.pl.entity;

public interface Visitor {
	void enter(Tuple tuple);
	void exit(Tuple tuple);
}
