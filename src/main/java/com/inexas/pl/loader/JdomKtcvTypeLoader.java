package com.inexas.pl.loader;

import java.util.*;
import org.jdom.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public class JdomKtcvTypeLoader extends Loader implements IKtcvTypeLoader {
	private final DataType dataType;
	private final Collection<NameValue<String>> constraints = new ArrayList<NameValue<String>>();
	private final String value;

	@SuppressWarnings("unchecked")
    public JdomKtcvTypeLoader(Element element, Crud host, ILoader parent) {
		super(0, element.getAttributeValue("key"), host, parent);
		
		dataType = DataType.getType(element.getAttributeValue("type"));
		final Iterator<Element> constraintElements = element.getChildren("constraint").iterator();
		while(constraintElements.hasNext()) {
			final Element constraintElement = constraintElements.next();
			final String constraintType = constraintElement.getAttributeValue("type");
			final String constraintValue = constraintElement.getAttributeValue("value");
			constraints.add(new NameValue<String>(constraintType, constraintValue));
		}
		value = element.getAttributeValue("value");
    }

	public Collection<NameValue<String>> getConstraints() {
		return constraints;
	}

	public DataType getDataType() {
		return dataType;
	}

	public String getValue() {
		return value;
	}

}
