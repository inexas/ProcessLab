package com.inexas.pl.loader;

import java.util.*;

public interface ITupleTypeLoader extends ILoader {

	Collection<IKtcvTypeLoader> getKtcvTypeMembers();
	
	Collection<ITupleTypeLoader> getTupleTypeMembers();

	boolean isRelational();

}
