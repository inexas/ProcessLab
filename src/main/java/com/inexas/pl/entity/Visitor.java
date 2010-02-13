package com.inexas.pl.entity;

public interface Visitor {
	void enter(Tuple tuple);

	void exit(Tuple tuple);

	void enter(TupleList tupleList);

	void exit(TupleList tupleList);

	void visit(Ktcv<? extends Object> ktcv);

	void dispose();

	void visit(Instances<? extends Object> ktcvArray);
}
