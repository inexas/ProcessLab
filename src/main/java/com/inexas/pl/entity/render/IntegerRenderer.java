package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

public class IntegerRenderer extends KtcvRenderer {

	public IntegerRenderer(Ktcv<?> ktcv) {
		super(ktcv);
	}

    @Override
	public void renderField(String id) {
    	@SuppressWarnings("unchecked")
		final Ktcv<Integer> integerKtcv = ((Ktcv<Integer>)ktcv);
		final Integer value = integerKtcv.getValue();
		final KtcvType<Integer> type = integerKtcv.getType();
		final RangeConstraint range = (RangeConstraint)type.getConstraint(RangeConstraint.NAME);

		// <input type=text name=fqn value=1.00[ readonly] onchange="validate('fqn',min,max)">
		sb.append("<input type=text name=");
		sb.append(id);
		sb.append(" value=");
		if(value != null) {
			sb.append(value.toString());
		}
		if(!ktcv.isReadWrite()) {
			sb.append(" readonly");
		}
		if(range != null) {
			sb.append(" onchange=\"javascript:validate('");
			sb.append(id);
			sb.append("',");
			sb.append(range.getMinimumInteger().intValue());
			sb.append(',');
			sb.append(range.getMaximumInteger().intValue());
			sb.append(")\">");
		} else {
			sb.append('>');
		}
		
		// <script language=javascript>
		// function validate () {
		// var number = parseInt(document.getElementById('id').value);
		// var field = document.getElementById('id');
		//	
		// if (isNaN(number)) {
		// alert("Not a valid Integer");
		// field.value = "";
		// } else {
		// if (Math.round(number) != number) {
		// alert("Number is a Double, not an Integer");
		// field.value = "";
		// }
		// if (number > maxmimum) {
		// alert("Number exceeds maximum");
		// field.value = "";
		// }
		// if (number < minimum) {
		// alert("Number falls below minimum");
		// field.value = "";
		// }
	}
}
