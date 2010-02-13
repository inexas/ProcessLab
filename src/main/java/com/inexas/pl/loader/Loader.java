package com.inexas.pl.loader;

public class Loader implements ILoader {
	protected final int id;
	protected final String key;
	protected Crud host;
	private ILoader parent;
	
	protected Loader(int id, String key, Crud host, ILoader parent) {
		this.id = id;
		this.key = key;
		this.host = host;
		this.parent = parent;
	}

	public final int getId() {
    	return id;
    }

	public final String getKey() {
    	return key;
    }

	public void setHost(Crud host) {
    	this.host = host;
    }

	public Crud getHost() {
    	return host;
    }

	public void setParent(ILoader parent) {
    	this.parent = parent;
    }

	public ILoader getParent() {
		return parent;
	}
	
}
