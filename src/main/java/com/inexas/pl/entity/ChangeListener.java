package com.inexas.pl.entity;

public interface ChangeListener {
	public class Event {
		public final static int DELETE = 0;
		public final static int CHANGE_VALUE = 1;
		public final int type;
		public final Ktcv<?> source;

		public Event(Ktcv<?> source, int type) {
			this.source = source;
			this.type = type;
		}
	}

	/**
	 * This event handler is called when a dependent field is changed and the
	 * new value of the dependee (the listener) needs to be recalcuated.
	 * 
	 * @param event
	 */
	void handle(Event event);

	/**
	 * Return true if this listener depends on (listens to) the candidate
	 * dependee
	 * 
	 * @param candidateDependee
	 * @return
	 */
	boolean dependsOn(ChangeListener candidateDependee);
}
