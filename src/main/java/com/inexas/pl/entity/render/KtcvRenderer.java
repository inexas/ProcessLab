package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

public abstract class KtcvRenderer extends AbstractRenderer {
	protected final Ktcv<?> ktcv;
	protected final String key;
	protected String label, field; // todo to private

	public KtcvRenderer(Ktcv<?> ktcv) {
		assert ktcv != null;
		
		this.ktcv = ktcv;
		key = ktcv.getKey();
	}

	/**
	 * if one of the tuples-ktcv ktcvhistory isn't empty, the following method
	 * returns ture. The ktcv-history button will show up.
	 */
	public boolean checkHistory(@SuppressWarnings("unused")
	Tuple tuple) {
		// final Iterator<Ktcv> i = tuple.getKtcvIterator();
		// while(i.hasNext()) {
		// if (!(p.getHistory().isEmpty())) {
		// return true;
		// }
		// }
		return false;
	}

	/**
	 * If a KTCV's ktcvhistory is empty, the following method returns false. The
	 * property history button won't show up
	 */
	public boolean checkHistory(@SuppressWarnings("unused")Ktcv<?> toCheck) {
		// if ((toCheck.getHistory() != null) &&
		// (!(toCheck.getHistory().isEmpty()))) {
		// return true;
		// } else {
		// return false;
		// }
		return false;
	}

	@Override
	public void renderLabel() {
		sb.append(getString("ktcv." + key));
	}

	@Override
	public void render() {
		sb.append("<tr><td>");
		renderLabel();
		sb.append("</td><td>");
		renderField(key);
		sb.append("</td></tr>");
	}
}
