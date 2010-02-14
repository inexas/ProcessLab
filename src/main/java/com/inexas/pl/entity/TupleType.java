package com.inexas.pl.entity;

import java.util.*;


public class TupleType extends AbstractType {
	private final List<AbstractType> members;
	private String tableName;
	private final Map<String, String> columnNameMap = new HashMap<String, String>();

//	/**
//	 * Load from an XML element
//	 * 
//	 * @param element
//	 * @param parent
//	 */
//	public TupleType(Element element, TupleType parent) {
//		super(element, parent);
//
//		@SuppressWarnings("unchecked")
//		final List<Element> childElements = element.getChildren();
//		final Iterator<Element> iterator = childElements.iterator();
//		while(iterator.hasNext()) {
//			final AbstractType member;
//			final Element childElement = iterator.next();
//			final String elementName = childElement.getName();
//			if(elementName.equals("tuple")) {
//				member = new TupleType(childElement, this);
//			} else {
//				assert elementName.equals("ktcv") : "Element not handled: " + elementName;
//				// It's a KTCV, figure out the type...
//				final String typeName = childElement.getAttributeValue("type");
//				if(typeName.equals("string")) {
//					member = new KtcvType<String>(childElement, this);
//				} else if(typeName.equals("integer")) {
//					member = new KtcvType<Integer>(childElement, this);
//				} else if(typeName.equals("boolean")) {
//					member = new KtcvType<Boolean>(childElement, this);
//				} else if(typeName.equals("float")) {
//					member = new KtcvType<Float>(childElement, this);
//				} else if(typeName.equals("double")) {
//					member = new KtcvType<Double>(childElement, this);
//				} else if(typeName.equals("date")) {
//					member = new KtcvType<Date>(childElement, this);
//				} else {
//					throw new RuntimeException("Type not handled: " + typeName);
//				}
//			}
//			members.put(member.getKey(), member);
//		}
//	}

//	/**
//	 * Load from a persistence source
//	 * 
//	 * @param key
//	 * @param id
//	 */
//	public TupleType(ITupleTypeLoader loader, TupleType parent) {
//		super(DataType.TUPLE, loader.getKey(), parent);
//		setId(loader.getId());
//
//		// Load the KTCVs
//		for(final IKtcvTypeLoader ktcvTypeLoader : loader.getKtcvTypeMembers()) {
//			final KtcvType<?> ktcvType = KtcvType.newType(ktcvTypeLoader, this);
//			add(ktcvType);
//		}
//
//		// Recursively load the child tuple types
//		for(final ITupleTypeLoader tupleTypeLoader : loader.getTupleTypeMembers()) {
//			final TupleType childTupleType = new TupleType(tupleTypeLoader, this);
//			add(childTupleType);
//		}
//
//	}
//
//	public TupleType(String key, AbstractType[] members) {
//		super(DataType.TUPLE, key);
//		this.members = members;
//	}
//
//	public TupleType getChildTupleType(String childKey) {
//		return (TupleType)members.get(childKey);
//	}
//
//	public AbstractType getChildKtcvType(String childKey) {
//		return members.get(childKey);
//	}
//
	public Iterator<KtcvType<?>> getKtcvTypeMemberIterator() {
		return new SelectiveIterator<KtcvType<?>, AbstractType>(members, KtcvType.class);
	}

	public Iterator<TupleType> getTupleTypeMemberIterator() {
		return new SelectiveIterator<TupleType, AbstractType>(members, TupleType.class);
	}

	public TupleType(String key, Cardinality cardinality, List<AbstractType> members) {
		super(DataType.TUPLE, key, cardinality);
		this.members = members;
	}

	@Override
	public TupleMember newInstance(Tuple theParent) {
		final TupleMember result = new TupleList(this, theParent);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder indent = new StringBuilder();
		toString(sb, indent);
		return sb.toString();
	}

	@Override
    void toString(StringBuilder sb, StringBuilder indent) {
		// The key
		final int orginalIndent = indent.length();
		sb.append(indent);
		sb.append(key);

		// Optional cardinality
		if(cardinality != Cardinality.ZERO2MANY) {
			sb.append(' ');
			sb.append(cardinality);
		}
		// Start of body, newline...
		sb.append(" {\n");
		
		// Contents...
		indent.append("    ");
		for(AbstractType child : members) {
			child.toString(sb, indent);
		}
		
		// End of body, newline...
		indent.setLength(orginalIndent);
		sb.append("}\n");
	}

//	@Override
//	void calculate() {
//		// for(final AbstractType member : members.values()) {
//		// member.calculate();
//		// if(member instanceof TupleTypeInstance) {
//		// list.add(member);
//		// }
//		// }
//	}

//	public void add(AbstractType member) {
//		members.add(member);
//    }
//
	public List<AbstractType> getMembers() {
		return members;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
    }
	
	public String getTableName() {
		return tableName;
	}

	public Map<String, String> getColumnNameMap() {
		return columnNameMap ;
    }
	
//	/**
//	 * Recursively insert the tuples
//	 * 
//	 * @param tuple
//	 * @param connection
//	 */
//	protected void create(DbConnection connection) {
//		// First insert the tuple type
//		// INSERT INTO tuple(id,parentId,key,cardinality,tableName)
//		// VALUES({Id},{ParentId},{Key},{Cardinality},{TableName})
//		final int parentId;
//		if(parent != null) {
//			// It's not the entity tuple so we need a new ID...
//			id = connection.getNextId("TupleType");
//			parentId = parent.getId();
//		} else {
//			parentId = 0;
//		}
//
//		connection.update(
//		        "tupleTypeInsert",
//		        "{Id}", Integer.toString(id),
//		        "{ParentId}", Integer.toString(parentId),
//		        "{Key}", '\'' + key + '\'',
//		        "{Cardinality}", '\'' + cardinality.toString() + '\'');
//
//		// Insert the KTCVs...
//		final Iterator<KtcvType<?>> ktcvTypes = getKtcvTypeMemberIterator();
//		while(ktcvTypes.hasNext()) {
//			final KtcvType<?> ktcvType = ktcvTypes.next();
//			ktcvType.create(connection);
//		}
//
//		// Insert the child tuples...
//		final Iterator<TupleType> tupleTypes = getTupleTypeMemberIterator();
//		while(tupleTypes.hasNext()) {
//			final TupleType child = tupleTypes.next();
//			child.create(connection);
//		}
//	}
}
