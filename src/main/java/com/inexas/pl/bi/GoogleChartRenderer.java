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

import java.text.*;
import java.util.*;
import com.ecyrd.jspwiki.*;
import com.inexas.pl.bi.Csv.*;
import com.inexas.pl.bi.Parameters.*;

abstract class GoogleChartRenderer extends Renderer {
	private static final char BASE_URL = 1; // "http://localhost:9080/processlab/"
	private static final char ID = 2; // "1"
	private static final char REQUEST_ID = 3; // "0"
	private static final char PAGE = 4;
	private static final char SELECT_HANDLER = 5; // See below
	private static final char PARAM_LIST = '4'; // "height:378,width:504"
	private static final char SELECT_LISTENER = '$'; // See below
	private static final char FUNCTION_NAME = '7'; // PieChart
	private static final char PACKAGE_NAME = '8'; // table
	private static final char LINK = '9'; // PageName&x={0}
	private static final char LINK_PARAMS = '¤'; // table
	private static final char[] fromSql = (
	        "function draw" + ID + "(){\n" +
	        "var query=new google.visualization.Query('" + BASE_URL + "jds?r=" + REQUEST_ID + "');\n" +
	        "query.send(doit" + ID + ");\n" +
	        "}\n" +
	        "var data" + ID + ";\n" +
	        "var chart" + ID + ";\n" +
	        "function doit" + ID + "(response){\n" +
	        "showWait(" + ID + ");\n" +
	        "if(response.isError()){\n" +
	        "alert('Error in query: '+response.getMessage()+response.getDetailedMessage());\n" +
	        "return;\n" +
	        "}\n" +
	        "data" + ID + " = response.getDataTable();\n" +
	        "chart" + ID + " = new google.visualization.7(document.getElementById('chartDiv" + ID + "'));\n" +
	        "chart" + ID + ".draw(data" + ID + ",{4});\n" +
	        "$" +
	        
	        // function refresh(id,page);
	        "refresh(" + ID + ",'" + PAGE + "');\n" +
	        
	        // !todo The hidewait has a JS error ???
	        "hideWait(" + ID + ");\n" +
	        "}\n" +
	        SELECT_HANDLER +
	        "queueChart('8',draw" + ID + ");\n").toCharArray();
	private static final char[] selectListener = (
	        "google.visualization.events.addListener(" +
	        "chart" + ID + ", 'select', selectHandler" + ID + ");\n").toCharArray();
	private static final char[] selectHandler = (
	        "var then=0;\n" +
	        "var selection;\n" +
	        "function selectHandler" + ID + "(){\n" +
	        "selection=getSelection(chart" + ID + ",selection);\n" +
	        "var now = (new Date()).getTime();\n" +
	        "if((now-then)<333){\n" +
	        "¤" +
	        "var target=\"" + BASE_URL + "Wiki.jsp?page=\"+encodeParameters(" +
	        "chart" + ID + ",data" + ID + ",selection,'9',pa);\n" +
	        "window.location=target;\n" +
	        "}\n" +
	        "then=now;\n" +
	        "}\n").toCharArray();
	private static final char[] spreadsheetHandler = (
			"<a href=" + BASE_URL + "ss?r=" + REQUEST_ID + ">" +
			"<img src=images/numbers-16x16.jpg title=\"Generate spreadsheet\" border=0>" +
			"</a>" +
    "\n").toCharArray();
	
	private static final DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat timeParser = new SimpleDateFormat("HH:mm:ss");
	private static final DateFormat dateTimeParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final String pluginId, baseUrl, requestId, pageName;
	protected Parameters parameters;

	GoogleChartRenderer(DataView plugin) {
		super(plugin);
		
		parameters = plugin.parameters;

		final WikiContext context = plugin.context;
		final RequestHandler requestHandler = RequestHandler.getInstance();
		pluginId = requestHandler.getPageUniquePluginId();

		if(parameters.getDb() != null) {
			// How many times can the request be used?
			final int lives = parameters.hasSpreadsheet() ? 2 : 1;
			requestId = requestHandler.publishRequest(plugin, lives);
			// !todo Figure out a way of killing the aged requests
			// !todo Make sure that CSV cannot have spreadsheet icons
		} else {
			requestId = null;
		}

		pageName = plugin.context.getPage().getName();
		
		baseUrl = context.getEngine().getBaseURL();
	}

	private void buildJavaScript(char[] input, String link) {
		final StringBuilder sb = plugin.sb;
		for(final char c : input) {
			switch(c) {
			case BASE_URL:
				sb.append(baseUrl);
				break;
			case ID:
				sb.append(pluginId);
				break;
			case REQUEST_ID:
				sb.append(requestId);
				break;
			case PARAM_LIST:
				renderParameters(sb);
				break;
			case FUNCTION_NAME:
				sb.append(getFunctionName());
				break;
			case PAGE:
				sb.append(pageName);
				break;
			case PACKAGE_NAME:
				sb.append(getPackageName());
				break;
			case SELECT_LISTENER:
				if(link != null) {
					buildJavaScript(selectListener, link);
				}
				break;
			case SELECT_HANDLER:
				if(link != null) {
					buildJavaScript(selectHandler, link);
				}
				break;
			case LINK:
				sb.append(link);
				break;
			case LINK_PARAMS:
				getParameters(sb, link);
				break;
			default:
				sb.append(c);
				break;
			}
		}
	}

	public void render() {
		final StringBuilder sb = plugin.sb;

		// Is there a drill-down link?..
		final String link = parameters.getLink();

		sb.append("<script type=\"text/javascript\">\n");

		if(parameters.getCsv() == null) {
			buildJavaScript(fromSql, link);
		} else {
			// Inlined data...

			checkCsv();

			// function draw1(){
			// . var data = new google.visualization.DataTable();
			// . data.addRows(6);
			// . data.addColumn('string', 'City');
			// . data.addColumn('number', 'Popularity');
			// . data.setValue(0, 0, 'New York');
			// . data.setValue(0, 1, 200);
			// . data.setValue(1, 0, 'Boston');
			// . data.setValue(1, 1, 300);
			// . ...
			// . var chart=new
			// .
			// google.visualization.GeoMap(document.getElementById('chartDiv1'));
			// . chart.draw(data,{height:378,width:504is3d:true});
			// }
			// queueChart('table',draw1);
			final Csv csv = parameters.getCsv();
			final int rowCount = csv.getRowCount();
			final int columnCount = csv.getColumnCount();
			sb.append("function draw");
			sb.append(pluginId);
			sb.append("(){\n" +
			        "var data = new google.visualization.DataTable();\n" +
			        "data.addRows(");
			sb.append(rowCount);
			sb.append(");\n");
			for(int c = 0; c < columnCount; c++) {
				sb.append("data.addColumn('");
				sb.append(csv.getDataTypeAsString(c));
				sb.append("','");
				sb.append(csv.getLabel(c));
				sb.append("');\n");
			}
			for(int r = 0; r < rowCount; r++) {
				final String rAsString = Integer.toString(r);
				for(int c = 0; c < columnCount; c++) {
					sb.append("data.setValue(");
					sb.append(rAsString);
					sb.append(',');
					sb.append(Integer.toString(c));
					final DataType dataType = csv.getDataType(c);
					final String value = csv.get(r, c);
					if(dataType == DataType.STRING) {
						sb.append(",'");
						sb.append(value);
						sb.append('\'');
					} else if(dataType == DataType.DATE) {
						sb.append(",new Date(");
						try {
							final Calendar calendar = Calendar.getInstance();
							switch(value.length()) {
							case 19: { // Date/time
								// new Date(1988,0,1,23,45,02)
								final Date date = dateTimeParser.parse(value);
								calendar.setTime(date);
								sb.append(calendar.get(Calendar.YEAR));
								sb.append(',');
								sb.append(calendar.get(Calendar.MONTH));
								sb.append(',');
								sb.append(calendar.get(Calendar.DATE));
								sb.append(' ');
								sb.append(calendar.get(Calendar.HOUR_OF_DAY));
								sb.append(',');
								sb.append(calendar.get(Calendar.MINUTE));
								sb.append(',');
								sb.append(calendar.get(Calendar.SECOND));
								break;
							}
							case 10: { // Date
								// new Date(23,05,09)
								final Date date = dateParser.parse(value);
								calendar.setTime(date);
								sb.append(calendar.get(Calendar.YEAR));
								sb.append(',');
								sb.append(calendar.get(Calendar.MONTH));
								sb.append(',');
								sb.append(calendar.get(Calendar.DATE));
								break;
							}
							case 8: { // Time
								// new Date(0,0,0,1988,0,1)
								final Date date = timeParser.parse(value);
								calendar.setTime(date);
								sb.append("0,0,0");
								sb.append(calendar.get(Calendar.HOUR_OF_DAY));
								sb.append(',');
								sb.append(calendar.get(Calendar.MINUTE));
								sb.append(',');
								sb.append(calendar.get(Calendar.SECOND));
								break;
							}
							default:
								throw new ParseException("Invalid length", 0);
							}
						} catch(final ParseException e) {
							throw new RuntimeException("Invalid date: " + value +
									", format should be YYYY-MM-DD HH:MM:SS", e);
						}
						sb.append(')');
					} else {
						sb.append(',');
						sb.append(value);
					}
					sb.append(");\n");
				}
			}
			sb.append("var chart=new google.visualization.");
			sb.append(getFunctionName());
			sb.append("(document.getElementById('chartDiv");
			sb.append(pluginId);
			sb.append("'));\n" +
			        "chart.draw(data,{");
			renderParameters(sb);
			sb.append("});\n" +
			        "}\n" +
			        "queueChart('");
			sb.append(getPackageName());
			sb.append("',draw");
			sb.append(pluginId);
			sb.append(");\n");
		}

		// google.setOnLoadCallback(init1);
		// </script>
		// <div id="chartDiv1"></div>
		// sb.append("google.setOnLoadCallback(init");
		// sb.append(pluginId);
		// sb.append(");\n" +
		sb.append("</script>\n" +
		        "<div id=\"chartDiv");
		sb.append(pluginId);
		if(this instanceof TimeLineChartRenderer) {
			// Special case as the as the size must be set here
			//  style="height:2500px;width:750px;"></div>
			sb.append("\" style=\"");
			parameters.renderSizeAsStyle(sb);
			sb.append("\"></div>\n");
		} else {
			sb.append("\"></div>\n");
		}
		// <div id='rf1_div'></div>
		sb.append("<div id=\"rf");
		sb.append(pluginId);
		sb.append("_div\"></div>");
		if(parameters.hasSpreadsheet()) {
			buildJavaScript(spreadsheetHandler, null);
		}
	}

	protected abstract String getPackageName();

	abstract void checkCsv();

	private void renderParameters(StringBuilder sb) {
		boolean first = true;
		for(final Parameter<?> parameter : parameters.getParameters()) {
			if(parameter instanceof SizeParameter) {
	            final SizeParameter sizeParameter = (SizeParameter)parameter;
	            sizeParameter.setLimits(getLimits());
            }
			if(first) {
				first = false;
			} else {
				sb.append(',');
			}
			parameter.toJavaScript(sb);
		}
	}

	protected abstract int[] getLimits();

	protected abstract String getFunctionName();

	/**
	 * Check the data type of a column. The concrete classes of know what they are 
	 * expecting and pass us an array of DataTypes to communicate this. If the 
	 * data type of the column is not known then it is set to the first
	 * DataType in the array. If the data type is known then it must match one of
	 * the DataTypes in the array.
	 * 
	 * @param c
	 * @param requiredDataTypes
	 */
	protected void checkCsvDataType(int c, DataType... requiredDataTypes) {
		final Csv csv = parameters.getCsv();
		final DataType actualDataType = csv.getDataType(c);
		if(actualDataType == null) {
			// No type set yet, use the first...
			csv.setDataType(c, requiredDataTypes[0]);
		} else {
			// Look for a match...
			boolean found = false;
			for(final DataType requiredDataType : requiredDataTypes) {
				if(actualDataType == requiredDataType) {
					found = true;
					break;
				}
			}
			if(!found) {
				final StringBuilder sb = new StringBuilder("Invalid data type in column: ");
				sb.append(c);
				sb.append(", it should be ");
				if(requiredDataTypes.length == 1) {
					sb.append("a ");
					sb.append(requiredDataTypes[0].text);
				} else {
					sb.append("one of the following types: ");
					String delimiter = "";
					for(final DataType requiredDataType : requiredDataTypes) {
						sb.append(delimiter);
						delimiter = ", ";
						sb.append(requiredDataType.text);
					}
					throw new RuntimeException(sb.toString());
				}
			}
		}
	}

	protected void checkCsvColumnCount(Csv csv, int minColumns, int maxColumns) {
		final int columnCount = csv.getColumnCount();
		if(columnCount < minColumns) {
			throw new RuntimeException("Not enough columns, expecting: " + minColumns + ".." + maxColumns);
		}
		if(columnCount > maxColumns) {
			throw new RuntimeException("Too many columns, expecting: " + minColumns + ".." + maxColumns);
		}
	}

	private void getParameters(StringBuilder sb, String link) {
		// link = 'Main+page&n1={1}';
		// output
		// . . var pa = [1];
		// . . pa[0] = 1;
		final char[] ca = link.toCharArray();
		final List<String> numbers = new ArrayList<String>();
		for(int i = 0; i < ca.length; i++) {
			final char c = ca[i];
			if(c == '{') {
				i++;
				final int start = i;
				while(i < ca.length && Character.isDigit(ca[i])) {
					i++;
				}
				final int length = i - start;
				if(i < ca.length && ca[i] == '}' && length > 0) {
					final String number = new String(ca, start, length);
					numbers.add(number);
					i++;
				} else {
					throw new RuntimeException("Invalid link: " + link);
				}
			}
		}
		final int count = numbers.size();
		sb.append("var pa=[");
		sb.append(count);
		sb.append("];\n");
		for(int i = 0; i < count; i++) {
			sb.append("pa[");
			sb.append(i);
			sb.append("]=");
			sb.append(numbers.get(i));
			sb.append(";\n");
		}
	}

}
