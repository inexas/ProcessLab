package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

public class TextLineRenderer extends KtcvRenderer {

	public TextLineRenderer(Ktcv<?> ktcv) {
		super(ktcv);
	}

    @Override
	public void renderField(String id) {
    	@SuppressWarnings("unchecked")
		final Ktcv<String> stringKtcv = ((Ktcv<String>)ktcv);
		final String value = stringKtcv.getValue();
		final KtcvType<String> type = stringKtcv.getType();
		final RegexpConstraint regexp = (RegexpConstraint)type.getConstraint(RegexpConstraint.NAME);

		// <input type=text name=fqn value="text"[ readonly] onchange="validate('fqn','regexp')">
		sb.append("<input type=text name=");
		sb.append(id + key);
		sb.append(" value=\"");
		if(value != null) {
			deQuote(value.toString());
		}
		sb.append('\"');
		if(!ktcv.isReadWrite()) {
			sb.append(" readonly");
		}
		if(regexp != null) {
			sb.append(" onchange=\"javascript:validate('");
			sb.append(id);
			sb.append("','");
			sb.append(regexp.getRegularExpression().toString());
			sb.append("')\">");
		} else {
			sb.append('>');
		}
	}
}
