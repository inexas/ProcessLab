package com.inexas.pl.entity;

import java.util.*;
import com.inexas.util.*;

/**
 * @author keith
 */
public class KtcvType<T> extends AbstractType {
//	private final static DateOutputFormatConstraint defaultDateFormat = new DateOutputFormatConstraint();
//	private final static DateOutputFormatConstraint formatter = defaultDateFormat;
	// private final Expression initialValue;
//	private final boolean calculated;
	private Map<String, Constraint> constraints;

	public static KtcvType<?> getKtcvType(DataType type, String key) {
		final KtcvType<?>  result;
		switch(type) {
		case BOOLEAN:
			result = new KtcvType<Boolean>(type, key);
			break;
		case DATETIME:
			result = new KtcvType<Date>(type, key);
			break;
		case DOUBLE:
			result = new KtcvType<Double>(type, key);
			break;
		case INTEGER:
			result = new KtcvType<Integer>(type, key);
			break;
		case LONG:
			result = new KtcvType<Long>(type, key);
			break;
		case SHORT:
			result = new KtcvType<Short>(type, key);
			break;
		case STRING:
			result = new KtcvType<String>(type, key);
			break;
		case TUPLE:
			throw new UnexpectedCallException();
			
		default:
			throw new UnexpectedCallException(type.toString());
		}
		return result;
	}
	
	private KtcvType(DataType type, String key) {
		super(type, key, Cardinality.ONE2ONE);
	}

	@Override
	public TupleMember newInstance(Tuple theParent) {
		if(cardinality == Cardinality.ONE2ONE) {
			return new Ktcv<T>(this, theParent);
		}
		return new Instances<T>(this, theParent);
	}

	public Ktcv<T> newInstance() {
		return new Ktcv<T>(this, null);
	}

//	@SuppressWarnings("unchecked")
//	public KtcvType(Element element, TupleType parent) {
//		super(element, Cardinalityparent);
//		
//		// todo Save a expression tree here so we parse and generate compile
//		// time errors
//		final String valueText = element.getAttributeValue("value");
//		if(valueText != null) {
//			final String trimmed = valueText.trim();
//			if(trimmed.length() > 1) {
//				// initialValue = new Expression(valueText);
//			} else {
//				// initialValue = null;
//			}
//		} else {
//			// initialValue = null;
//		}
//		
//		// Process any constraints...
//		final Iterator<Element> iterator = element.getChildren("constraint").iterator();
//		while(iterator.hasNext()) {
//			final Element constraintElement = iterator.next();
//			final String constraintType = constraintElement.getAttributeValue("type");
//			final String value = constraintElement.getAttributeValue("value");
//			final Constraint constraint = ConstraintFactory.getConstraint(constraintType, value);
//			if(constraints == null) {
//				constraints = new HashMap<String, Constraint>();
//			}
//			constraints.put(constraintType, constraint);
//		}
//		
//		calculated = getConstraint(CalculatedConstraint.NAME) != null;
//	}
//	
//	public KtcvType(IKtcvTypeLoader loader, TupleType parent) {
//		super(loader.getDataType(), loader.getKey(), parent);
//		setId(loader.getId());
//		// final String valueText = loader.getValue();
//		// initialValue = valueText == null ? null : new Expression(valueText);
//		for(final NameValue<String> nameValue : loader.getConstraints()) {
//			final String name = nameValue.getName();
//			if(constraints == null) {
//				constraints = new HashMap<String, Constraint>();
//			}
//			final Constraint constraint = ConstraintFactory.getConstraint(name, nameValue.getValue());
//			constraints.put(name, constraint);
//		}
//		calculated = getConstraint(CalculatedConstraint.NAME) != null;
//	}

//	@SuppressWarnings("unchecked")
//	public T parse(String value) {
//		final T returnValue;
//		if(value == null || value.trim().length() == 0) {
//			returnValue = null;
//		} else {
//			switch(dataType) {
//			case BOOLEAN:
//				returnValue = (T)(Boolean.parseBoolean(value) ? Boolean.TRUE : Boolean.FALSE);
//				break;
//			case DATE:
//				try {
//					returnValue = (T)formatter.parse(value);
//				} catch(ParseException e) {
//					throw new RuntimeException("Date format should be: 30 Apr 1957 14:21, not " + value, e);
//				}
//				break;
//			case DOUBLE:
//				returnValue = (T)new Double(value);
//				break;
//			case FLOAT:
//				returnValue = (T)new Float(value);
//				break;
//			case INTEGER:
//				returnValue = (T)new Integer(value);
//				break;
//			case LONG:
//				returnValue = (T)new Long(value);
//				break;
//			case SHORT:
//				returnValue = (T)new Short(value);
//				break;
//			case STRING:
//				returnValue = (T)value;
//				break;
//			default:
//				throw new RuntimeException("Unhandled type: " + dataType);
//			}
//		}
//		return returnValue;
//	}

	@Override
	public String toString() {
		return "KtcvType('" + getKey() + "', Type: " + dataType + ")";
	}
	
	@Override
    void toString(StringBuilder sb, StringBuilder indent) {
		
		// The key
		sb.append(indent);
		sb.append(key);

		// Optional cardinality
		if(cardinality != Cardinality.ONE2ONE) {
			sb.append(' ');
			sb.append(cardinality);
		}
		
		// Optional data type...
		if(dataType != DataType.STRING) {
			sb.append(" : ");
			sb.append(dataType.asText);
		}
		
		sb.append(";\n");
	}

	public T getInitialValue(@SuppressWarnings("unused") Instance homeAtom) {
//		return initialValue == null ? null : (T)initialValue.getResult(homeAtom);
		return null;
	}

//	@Override
//	public void calculate() {
//		final CalculatedConstraint constraint = (CalculatedConstraint)getConstraint("calculated");
//		if(constraint != null) {
//			constraint.calculate();
//		}
//	}
//
//	@Override
//	public boolean isCalcuated() {
//		return calculated;
//	}

	public void throwIfInvalid(String toCheck) {
		if(constraints != null) {
			for(final Constraint constraint : constraints.values()) {
				try {
					if(!(constraint instanceof CalculatedConstraint)) {
						constraint.check(toCheck);
					}
				} catch(Exception e) {
					throw new RuntimeException("Type: " + toString(), e);
				}
			}
		}
	}

	public Iterator<Constraint> getConstraintIterator() {
		return constraints == null ? new EmptyIterator<Constraint>() : constraints.values().iterator();
	}

	@Override
	public Constraint getConstraint(String constraintKey) {
		return constraints == null ? null : constraints.get(constraintKey);
	}

	/**
	 * Return the (initial) value as it was defined in the definition
	 * 
	 * @return
	 */
	public String getInitialValue() {
		// return initialValue == null ? null : initialValue.toString();
		return null;
	}

//	public void create(DbConnection connection) {
//		assert id == 0;
//		id = connection.getNextId("KtcvType");
//		// final String initialValueAsString = initialValue == null ? "NULL" :
//		// initialValue.toString();
//		connection.update(
//		        "ktcvTypeInsert",
//		        "{Id}", Integer.toString(id),
//		        "{TupleId}", Integer.toString(id),
//		        "{Key}", '\'' + key + '\'',
//		        "{Type}", '\'' + dataType.asText + '\'',
//		        "{Value}", null,// initialValueAsString,
//		        "{Cardinality}", '\'' + cardinality.toString() + '\'');
//
//		if(constraints != null) {
//			for(final Constraint constraint : constraints.values()) {
//				// INSERT INTO
//				// constraint(ktcvId,name,value)VALUES({KtcvId},{Name},{Value})
//				connection.update(
//				        "constraintInsert",
//				        "{KtcvId}", Integer.toString(id),
//				        "{Name}", '\'' + constraint.getName() + '\'',
//				        "{Value}", '\'' + constraint.getValue() + '\'');
//			}
//		}
//	}

}
