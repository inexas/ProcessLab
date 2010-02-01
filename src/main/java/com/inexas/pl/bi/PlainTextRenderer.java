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

import java.sql.*;
import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;

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
