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

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;

public class SpreadsheetServlet extends HttpServlet {
	private static final long serialVersionUID = 6804019256137314360L;
	private enum Type {
		IGNORE,
		STRING,
		NUMBER
	}

	public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	throws ServletException, IOException {
		
		final StringBuilder sb = new StringBuilder();

		// Get the variables needed to make the query...
		final RequestHandler requestHandler = RequestHandler.getInstance();
		final Request request = requestHandler.getRequest(httpRequest);

		final Source source = SourcePool.getInstance().getSource(httpRequest, request.db);
		try {
			// Make the query...
			final ResultSet rs = source.query(request.sql);
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();
			
			// Format the results
			
			final Type types[] = new Type[columnCount];
			for(int i = 0; i < columnCount; i++) {
				final Type type;
				switch(metaData.getColumnType(i + 1)) {
				case Types.BIGINT:
				case Types.BOOLEAN:
				case Types.DATE:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.LONGVARCHAR:
				case Types.NUMERIC:
				case Types.REAL:
				case Types.SMALLINT:
				case Types.TIME:
				case Types.TIMESTAMP:
				case Types.TINYINT:
					type = Type.NUMBER;
					break;
					
				case Types.CHAR:
				case Types.VARCHAR:
					type = Type.STRING;
					break;
					
				default:
					type = Type.IGNORE;
				}
				types[i] = type;

				if(type != Type.IGNORE) {
					final String columnName = metaData.getColumnName(i + 1);
					sb.append(columnName);
					sb.append('\t');
				}
			}
			sb.append('\n');

			while(rs.next()) {
				for(int i = 0; i < columnCount; i++) {
					switch(types[i]) {
					case IGNORE:
						break;
						
					case NUMBER:
						sb.append(rs.getString(i + 1));
						sb.append('\t');
						break;

					case STRING:
						sb.append('\"');
						sb.append(rs.getString(i + 1));
						sb.append("\"\t");
					}
				}
				sb.append('\n');
			}
			httpResponse.setContentType("application/vnd.ms-excel");
		} catch(final SQLException e) {
			sb.setLength(0);
			sb.append("Error: " + e.getMessage());
		} finally {
			source.release();
		}
		final PrintWriter out = httpResponse.getWriter();
		out.print(sb.toString());
	}
}