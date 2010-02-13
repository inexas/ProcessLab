package com.inexas.pl.entity;

//import org.jdom.*;

public abstract class AbstractType {
	protected final DataType dataType;
	protected final String key;
	protected final Cardinality cardinality;
	protected TupleType parent;
	protected int id;

//	/**
//	 * Construct from JDOM element
//	 */
//	protected AbstractType(Element element, TupleType parent) {
//		// todo Move this code out to a loader
//		key = element.getAttributeValue("key");
//		dataType = DataType.getType(element.getAttributeValue("type"));
//		final String cardinalityText = element.getAttributeValue("cardinality");
//		if(cardinalityText == null) {
//			cardinality = this instanceof KtcvType<?> ?
//			        Cardinality.ONE2ONE : Cardinality.ZERO2MANY;
//		} else {
//			cardinality = Cardinality.newInstance(cardinalityText);
//		}
//		this.parent = parent;
//	}

	protected AbstractType(DataType dataType, String key, Cardinality cardinality) {
		this.dataType = dataType;
		this.key = key;
		this.cardinality = cardinality;
	}

//	protected AbstractType(Tuple tuple, TupleType parent) {
//		key = tuple.getKtcv("key").getValueAsString();
//		dataType = DataType.getType(tuple.getKtcv("type").getValueAsString());
//		cardinality = Cardinality.newInstance(tuple.getKtcv("cardinality").getValueAsString());
//		this.parent = parent;
//	}

//	protected AbstractType() {
//		//
//	}

	public String getKey() {
		return key;
	}

	public void setParent(TupleType parent) {
		this.parent = parent;
	}

	public TupleType getParent() {
		return parent;
	}

	public String getFullPath() {
		final StringBuilder sb = new StringBuilder();
		getFullPath(sb);
		return sb.toString();
	}

	abstract void toString(StringBuilder sb, StringBuilder indent);

	protected void getFullPath(StringBuilder sb) {
		if(parent != null) {
			parent.getFullPath(sb);
		}
		sb.append('/');
		sb.append(key);
	}

//	abstract void calculate();

	public Cardinality getCardinality() {
		return cardinality;
	}

	public abstract TupleMember newInstance(Tuple instanceParent);

//	public DataType getTypeAsInt() {
//		return dataType;
//	}

	public Constraint getConstraint(@SuppressWarnings("unused") String name) {
		// todo Auto-generated method stub
		return null;
	}

//	public boolean isCalcuated() {
//		// todo Auto-generated method stub
//		return false;
//	}
//
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Return the data type as text
	 * 
	 * @return
	 */
	public DataType getDataType() {
		return dataType;
	}

}
