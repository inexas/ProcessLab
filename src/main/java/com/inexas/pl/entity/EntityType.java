package com.inexas.pl.entity;

import java.util.*;
import com.inexas.pl.loader.*;

public class EntityType extends TupleType {

	public EntityType(String key, Cardinality cardinality, List<AbstractType> members) {
		super(key, cardinality, members);
	}

	public EntityType(@SuppressWarnings("unused") ITupleTypeLoader loader) {
		super(null, null, null);
		// todo Implement me
        throw new RuntimeException("How about implementing me?!");
	}

	public boolean isRelational() {
		return true;
    }

}
