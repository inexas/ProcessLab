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
