package com.inexas.pl.loader;

import com.inexas.util.*;
import org.antlr.runtime.tree.*;

public class DependencyNode {
	public enum Type {
		REMOTE_ACTION,
		AND,
		OR,
		NOT,
		XOR;
	}
	public final Type type;
	public final DependencyNode lhs;
	public final DependencyNode rhs;
	public final String action;

	public DependencyNode(Type type, DependencyNode lhs, DependencyNode rhs) {
		this.type = type;
		this.lhs = lhs;
		this.rhs = rhs;
		action = null;
    }
	
	public DependencyNode(Tree tree) {
		this.type = Type.REMOTE_ACTION;
		this.lhs = null;
		this.rhs = null;
		action = tree.getChild(0).getText() + "." + tree.getChild(1).getText();
    }
	
	public boolean evaluate() {
		final boolean result;
		switch(type) {
			case AND:
				result = lhs.evaluate() || rhs.evaluate();
				break;
			case OR:
				result = lhs.evaluate() || rhs.evaluate();
				break;
			case XOR:
				result = lhs.evaluate() != rhs.evaluate();
				break;
			case NOT:
				result = !lhs.evaluate();
				break;
			case REMOTE_ACTION:
				// !todo Implement me: Get the activity  and return true if it's completed
                throw new RuntimeException("How about implementing me?!");
			default:
				throw new UnexpectedException("Case: " + type);
		}
		return result;
	}

	@Override
    public String toString() {
		final String result;
		switch(type) {
			case AND:
				result = lhs.toString() + " & " + rhs.toString();
				break;

			case OR:
				result = lhs.toString() + " | " + rhs.toString();
				break;

			case XOR:
				result = lhs.toString() + " ^ " + rhs.toString();
				break;

			case NOT:
				result = '!' + lhs.toString();
				break;

			case REMOTE_ACTION:
				result = action;
				break;

			default:
				throw new UnexpectedException("Case: " + type);
		}
		return result;
    }

	
}
