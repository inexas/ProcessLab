/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.pl.scheduler;

import java.util.*;
import com.inexas.pl.scheduler.Parser.*;

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
