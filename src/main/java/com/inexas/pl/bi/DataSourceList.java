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

import java.util.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.plugin.*;
import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;

public class DataSourceList implements WikiPlugin {

	@SuppressWarnings("unchecked")
    public String execute(WikiContext context, Map parameters) throws PluginException {
		
		// Generate a sorted list of data sources (including demo)...
		final List<String[]> dataSources = new ArrayList<String[]>();
		final String[] demoDataSource = { "demo", "Demo data source" };
		dataSources.add(demoDataSource);
		for(final Factory factory : SourcePool.getInstance().getFactories().values()) {
			final String[] dataSource = {
					factory.getName(),
					factory.getComment()
			};
			dataSources.add(dataSource);
		}
		Collections.sort(dataSources, new Comparator<String[]>() {
            public int compare(String[] lhsDataSource, String[] rhsDataSource) {
				return lhsDataSource[0].compareTo(rhsDataSource[0]);
            }
		});
		
		// Generate the output...
		final String format = (String)parameters.get("format");
		final StringBuilder sb = new StringBuilder();
		if(format == null || format.equals("table")) {
			//	<table class=wikitable border=1>
			//		<tr>
			//			<th>Name</th>
			//			<th>Comment</th>
			//		</tr>
			//		<tr>
			//			<td>demo</td>
			//			<td>Demo database</td>
			//		</tr>
			//	</table>
			sb.append(
					"<table class=wikitable border=1>" +
					"<tr>" +
					"<th>Name</th>" +
					"<th>Comment</th>" +
					"</tr>");
			for(final String[] dataSource : dataSources) {
				sb.append("<tr><td>");
				sb.append(dataSource[0]);
				sb.append("</td><td>");
				sb.append(dataSource[1]);
				sb.append("</td></tr>");
			}
			sb.append("</table>");
		} else if(format.equals("comma")) {
			String delimiter = "";
			for(final String[] dataSource : dataSources) {
				sb.append(delimiter);
				delimiter = ", ";
				sb.append(dataSource[0]);
			}
		}
		return sb.toString();
    }

}
