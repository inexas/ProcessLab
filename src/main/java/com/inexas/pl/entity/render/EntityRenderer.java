package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;


public class EntityRenderer extends KtcvRenderer {
	@SuppressWarnings("unused")
	private final Entity entity;

	public EntityRenderer(Entity entity) {
		super(null);
		this.entity = entity;
	}
	
	@Override
	public void renderField(String id) {
		sb.append("<table width=100%>");
		sb.append("</table>");
	}

}
