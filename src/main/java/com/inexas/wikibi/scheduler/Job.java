package com.inexas.wikibi.scheduler;

import java.util.*;
import com.inexas.wikibi.scheduler.Parser.*;

public class Job {
	private final Schedulable schedulable;
	private final Record record;
	private long nextRunTime;
	
	public Job(Schedulable schedulable, String cronString) {
		assert schedulable != null : "Null schedulable";
		assert cronString != null : "Null cron string";
		
		try {
	        this.schedulable = schedulable;
	        this.record = new Parser(cronString).getRecord();
        } catch(final ParsingException e) {
        	throw new RuntimeException("Error setting up task", e);
        }
    }

	public void run() {
		schedulable.run();
    }

	public boolean setNextRunTime() {
		nextRunTime = record.getNextRunTime();
		return nextRunTime > 0;
    }

	public long getDelay() {
		if(nextRunTime == 0) {
			setNextRunTime();
		}
		final long now = new Date().getTime();
		return nextRunTime - now;
    }

	public long getNextRunTime() {
    	return nextRunTime;
    }

	public String getPageName() {
		return schedulable.getPageName();
    }

	public int getPageVersion() {
		return schedulable.getPageVersion();
    }

	@Override
    public String toString() {
		return schedulable.getDescription();
    }
	
}
