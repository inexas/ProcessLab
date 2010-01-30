package com.inexas.wikibi.scheduler;

public interface Schedulable {

	void run();

	String getPageName();

	int getPageVersion();

	String getDescription();

}
