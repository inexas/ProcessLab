package com.inexas.pl.pl;

import java.util.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public class EntityCompiler {
	private EntityType entity;

	public EntityCompiler(String input) {
		try {
			final EntityLexer lexer = new EntityLexer(new ANTLRStringStream(input));
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			
			final EntityParser parser = new EntityParser(tokens);
			final CommonTree tree = parser.parse().tree;
			walk(tree);
		} catch(final RecognitionException e) {
			throw new RuntimeException("Invalid expression: '" + input + "'", e);
		}
	}

	/**
	 * Walk the tree produced by ANTLR to prune it either to a constant a
	 * home-grown tree if there are elements that cannot be evaluated because a
	 * tuple is required.
	 * 
	 * @param tree
	 * @return
	 */
	private AbstractType walk(CommonTree tree) {
		AbstractType result = null;

		final int tokenType = tree.getToken().getType();
		switch(tokenType) {
		case EntityParser.ENTITY:
			// Entity has a single tuple child
			entity = (EntityType)walk((CommonTree)tree.getChild(0));
			break;

		case EntityParser.TUPLE: {
			String key = null;
			Cardinality cardinality = Cardinality.ZERO2MANY;
			final List<AbstractType> members = new ArrayList<AbstractType>();
			// key cardinality? children
			
			final int childCount = tree.getChildCount();
			for(int i = 0; i < childCount; i++) {
				final CommonTree childTree = (CommonTree)tree.getChild(i);
				final Token childToken = childTree.getToken();
				switch(childToken.getType()) {
				case EntityParser.Id:
					key = tree.getChild(0).getText();
					break;
					
				case EntityParser.Cardinality:
					cardinality = Cardinality.newInstance(childTree.getText());
					break;
					
				case EntityParser.TUPLE:
				case EntityParser.KTCV:
					final AbstractType member = walk(childTree);
					members.add(member);
					break;

				default:
					throw new UnexpectedCallException(childToken.getText() + '/' + childToken.getType());
				}
			}
			
			final TupleType tuple = new TupleType(key, cardinality, members);

			// Sort out the ancestry...
			for(final AbstractType child : members) {
				child.setParent(tuple);
			}
			
			result = tuple;
			break;
		}

		case EntityParser.KTCV: {
			String key = null;
			// !todo Cardinality cardinality = Cardinality.ONE2ONE;
			DataType dataType = DataType.STRING;
			
			final int childCount = tree.getChildCount();
			for(int i = 0; i < childCount; i++) {
				final CommonTree childTree = (CommonTree)tree.getChild(i);
				final Token childToken = childTree.getToken();
				switch(childToken.getType()) {
				case EntityParser.Id:
					key = tree.getChild(0).getText();
					break;

				case EntityParser.BOOLEAN:
					dataType = DataType.BOOLEAN;
					break;
					
				case EntityParser.DATE:
					dataType = DataType.DATE;
					break;
					
				case EntityParser.DOUBLE:
					dataType = DataType.DOUBLE;
					break;
					
				case EntityParser.INTEGER:
					dataType = DataType.INTEGER;
					break;
					
				case EntityParser.LONG:
					dataType = DataType.LONG;
					break;
					
				case EntityParser.SHORT:
					dataType = DataType.SHORT;
					break;
					
				case EntityParser.STRING:
					dataType = DataType.STRING;
					break;
					
//				case EntityParser.Cardinality:
//					cardinality = Cardinality.newInstance(childTree.getText());
//					break;
					
					// !todo Handle value, constraints
				default:
					throw new UnexpectedCallException(childToken.getText() + '/' + childToken.getType());
				}
			}

			result = KtcvType.getKtcvType(dataType, key);
			break;
		}

		default:
			throw new RuntimeException("Parser walker, unhandled type: " + tokenType);
		}
		
		return result;
	}

	public EntityType getEntity() {
		return entity;
	}

}
