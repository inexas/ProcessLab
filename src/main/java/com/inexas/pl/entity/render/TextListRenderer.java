package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

/**
 * Render a text area
 * 
 * @author <a href=mailto:keith@whittingham.com>Keith Whittingham</a>
 * @version 1.00
 */
public class TextListRenderer extends KtcvRenderer {

	/**
	 * Constructor for TextListRenderer.
	 * 
	 * @param rb resource bundle to translate the label
	 * @param ktcv the ktcv to render
	 */
	public TextListRenderer(Ktcv<?> ktcv) {
		super(ktcv);
	}

    @Override
	public void renderField(String id) {
    	@SuppressWarnings("unchecked")
		final String text = ((Ktcv<String>)ktcv).getValue();
		sb.append("<textarea name=");
		sb.append(id);
		if(text == null) {
			sb.append("></textarea>");
		} else {
			sb.append('>');
			deQuote(text);
			sb.append("</textarea>");
		}
	}

}
