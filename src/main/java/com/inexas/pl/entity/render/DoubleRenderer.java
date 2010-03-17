package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

public class DoubleRenderer extends KtcvRenderer {

	public DoubleRenderer(Ktcv<Double> ktcv) {
		super(ktcv);
	}

    @Override
	public void renderField(String id) {
    	@SuppressWarnings("unchecked")
		final Ktcv<Double> doubleKtcv = ((Ktcv<Double>)ktcv);
		final Double value = doubleKtcv.getValue();
		final KtcvType<Double> type = doubleKtcv.getType();
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
			sb.append(range.getMinimumDouble());
			sb.append(',');
			sb.append(range.getMaximumDouble());
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
		// alert("Not a valid Double");
		// field.value = "";
		// } else {
		//		
		// if (number > maxmimum) {
		// alert("Number exceeds maximum");
		// field.value = "";
		// }
		// if (number < minimum) {
		// alert("Number falls below minimum");
		// field.value = "";
		// }
		// }
		// }
		// </script>
	}

}
