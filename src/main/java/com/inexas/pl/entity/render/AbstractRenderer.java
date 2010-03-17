package com.inexas.pl.entity.render;

/**
 * Renderer base class
 */
public abstract class AbstractRenderer {
	protected final StringBuilder sb = new StringBuilder();
	
	protected String getString(String key) {
		return key;
	}
	
	public void deQuote(@SuppressWarnings("unused") String string) {
		// todo Implement me
        throw new RuntimeException("How about implementing me?!");
	}

	public abstract void render();
	
	public abstract void renderLabel();
	
	public abstract void renderField(String id);
	
}
