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
/**
 * Copyright (C) Inexas 2009, All rights reserved.
 *
 * @author keith
 */
package com.inexas.pl.bi;

import java.util.regex.*;
import com.ecyrd.jspwiki.*;
import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;
import com.inexas.pl.scheduler.*;

class DataSchedulable implements Schedulable {
	private final static Pattern newlines = Pattern.compile("[\\r\\n]");
	private final String pageName, sql, description, db;
	private final int pageVersion;
	
	public DataSchedulable(AbstractPlugin plugin) {
		final Parameters parameters = plugin.getParameters();
		final WikiPage page = plugin.context.getPage();
		pageName = page.getName();
		pageVersion = page.getVersion();
		db = parameters.getDb();
		// !todo check no fields
		sql = newlines.matcher(parameters.getSql()).replaceAll(" ");
		description = db + ":" + sql;
    }

    public String getDescription() {
    	return description;
    }

    public String getPageName() {
    	return pageName;
    }

    public int getPageVersion() {
    	return pageVersion;
    }

    public void run() {
		final Source source = SourcePool.getInstance().getSource(null, db);
		try {
			source.update(sql);
			PageLogger.getInstance().log(
					pageName,
					// todo Add the scheduler's name here
					"Scheduled",
					db,
					sql,
					"OK");
		} catch(final Exception e) {
			PageLogger.getInstance().log(
					pageName,
					// todo Add the scheduler's name here
					"Scheduled",
					db,
					sql,
					"ERROR: " + e.getMessage());
		} finally {
			source.release();
		}
    }

}