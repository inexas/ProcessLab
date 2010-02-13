package com.inexas.pl.loader;

import java.util.*;
import org.jdom.*;

public class JdomTupleTypeLoader extends Loader implements ITupleTypeLoader {
	private final boolean relational;
	private final Collection<ITupleTypeLoader> tupleTypeMembers = new ArrayList<ITupleTypeLoader>();
	private final Collection<IKtcvTypeLoader> ktcvTypeMembers = new ArrayList<IKtcvTypeLoader>();

	@SuppressWarnings("unchecked")
    public JdomTupleTypeLoader(Element element, Crud host, ILoader parent) {
		super(0, element.getAttributeValue("key"), host, parent);
		
		relational = Boolean.parseBoolean(element.getAttributeValue("relational"));

		final Iterator<Element> tuples = element.getChildren("tuple").iterator();
		while(tuples.hasNext()) {
			final Element children = tuples.next();
			final ITupleTypeLoader childLoader = new JdomTupleTypeLoader(children, host, this);
			tupleTypeMembers.add(childLoader);
		}

		final Iterator<Element> ktcvs = element.getChildren("ktcv").iterator();
		while(ktcvs.hasNext()) {
			final Element children = ktcvs.next();
			final IKtcvTypeLoader childLoader = new JdomKtcvTypeLoader(children, host, this);
			ktcvTypeMembers.add(childLoader);
		}
    }

	public Collection<IKtcvTypeLoader> getKtcvTypeMembers() {
		return ktcvTypeMembers;
	}

	public Collection<ITupleTypeLoader> getTupleTypeMembers() {
		return tupleTypeMembers;
	}

	public boolean isRelational() {
		return relational;
	}

}
