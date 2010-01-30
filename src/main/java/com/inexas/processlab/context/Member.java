package com.inexas.processlab.context;

import java.security.*;

public abstract class Member {
	protected final Long key;
	private String name, description;
	protected Member parent;
	private Principal owner;

	public Member(Long key, String name) {
		assert key != null;
		this.key = key;
		this.name = name;
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
		// todo should I check here or at a higher level
		assert Directory.isValidName(name);
    	this.name = name;
    }

	public String getDescription() {
    	return description;
    }

	public void setDescription(String description) {
    	this.description = description;
    }

	public Long getKey() {
    	return key;
    }

	/**
	 * Set the owner. The owner may be null implying that the
	 * ownership is inherited.
	 * 
	 * @param owner
	 */
	public void setOwner(Principal owner) {
		assert owner != null || parent != null : "Root must have a non-null owner";
	    this.owner = owner;
    }

	public Principal getOwner() {
	    return owner == null ? parent.getOwner() : owner;
    }

	public void setParent(Member parent) {
		assert parent != null || key.equals(Context.ROOT_KEY) : "Only the Root context can have a null parent";
	    this.parent = parent;
    }

	public Member getParent() {
	    return parent;
    }
	
	public String getPathname() {
		final StringBuilder sb = new StringBuilder();
		getPathname(sb);
		return sb.toString();
	}

	private void getPathname(StringBuilder sb) {
		if(parent != null) {
			getPathname(sb);
		}
		sb.append(name);
		if(this instanceof Context) {
			sb.append('/');
		}
    }
	
	public void delete() {
		assert parent != null : "Already deleted or this is root";
		parent = null;
	}
}
