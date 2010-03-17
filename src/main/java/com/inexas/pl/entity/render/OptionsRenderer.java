package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

/**
 * This is invoked to render an KTCV that has an options constraint
 */
public class OptionsRenderer extends KtcvRenderer {

	/**
	 * Constructor for OptionListRenderer.
	 * 
	 * @param rb resource bundle to translate the label
	 * @param ktcv the ktcv to render
	 */
	public OptionsRenderer(Ktcv<?> ktcv) {
		super(ktcv);
	}

	@Override
	public void renderField(String id) {
		// <select name=fqn>
		//   <option>Less than</option>
		//   <option selected>Equal to</option>
		//   <option>Greater than</option>
		// </select>
		final OptionsConstraint options = (OptionsConstraint)ktcv.getType().getConstraint(OptionsConstraint.NAME);
		final Object defaultOption = options.getDefaultOption();
		
		sb.append("<select name=");
		sb.append(id);
		sb.append('>');
		for(Object option : options) {
			sb.append(option == defaultOption ? "<option selected>" : "<option>");
			deQuote(option.toString());
			sb.append("</option>");
		}
		sb.append("</select>");
	}
}
