package com.inexas.pl.scheduler;

public interface Schedulable {

	void run();

	String getPageName();

	int getPageVersion();

	String getDescription();

}
