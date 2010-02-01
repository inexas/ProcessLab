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
package com.inexas.pl.bi;

import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;

/**
 * DataUpdate is a simple wiki plugin that creates a button that, if pressed,
 * will execute a stored procedure on a database.
 * 
 * Parameters * sql='EXECUTE sp_DataUpdateTest @id@,\'@name@\''
 * 
 * We generate a button and, if there are any parameters then an input field for
 * each one.
 * 
 * @author Keith Whittingham, Processwide
 */
public class DataUpdate extends AbstractPlugin {

	@Override
	protected void render() {

		final String db = parameters.getDb();
		final String sql = parameters.getSql();
		
		if(submitted) {
			final Source source = SourcePool.getInstance().getSource(request, db);
			try {
				source.update(sql);
				PageLogger.getInstance().log(
						context.getPage().getName(),
						context.getWikiSession().getLoginPrincipal().getName(),
						db,
						sql,
						"OK");
				sb.append("<p><b><font color=green>The update has been processed.</font></b></p>");
			} catch(final Exception exception) {
				// Convert the stack trace to HTML
				sb.append(
				        "SQL error: \"" + sql + "\", " + exception.getMessage() + "<br>" +
				        "You can edit the page to correct it or, if you don't have the rights," +
				        "ask an administrator to fix the problem.");
			} finally {
				source.release();
			}
		}
	}

}
