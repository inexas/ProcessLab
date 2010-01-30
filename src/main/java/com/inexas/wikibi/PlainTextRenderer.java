package com.inexas.wikibi;

import java.sql.*;
import com.inexas.wikibi.datasource.*;
import com.inexas.wikibi.datasource.SourcePool.*;

public class PlainTextRenderer extends Renderer {

	public PlainTextRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	public void render() {
		final Parameters parameters = plugin.getParameters();
		final Source source = SourcePool.getInstance().getSource(plugin.request, parameters.getDb());
		final String sql = parameters.getSql();
		final ResultSet rs = source.query(sql);
		try {
			// Execute the query...
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();
			if(columnCount > 1) {
				throw new RuntimeException("More than one column");
			}
			if(!rs.next()) {
				throw new RuntimeException("No result from SQL query");
			}
			plugin.sb.append(rs.getString(1));
			if(rs.next()) {
				throw new RuntimeException("Too many rows in response");
			}
		} catch(final SQLException e) {
			throw new RuntimeException("SQL error in statement: \"" + sql + "\", " + e.getMessage());
		} finally {
			source.release();
			try {
	            rs.close();
            } catch(SQLException e) {
            	throw new RuntimeException("Error closing result set", e);
            }
		}
	}

}
