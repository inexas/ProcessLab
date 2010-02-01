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
import java.util.*;
import au.com.bytecode.opencsv.*;

public class Csv {
	public static enum DataType {
		STRING("string"),
		NUMBER("number"),
		DATE("date"),
		BOOLEAN("boolean");
		public final String text;

		private DataType(String text) {
			this.text = text;
		}
		private static final Map<String, DataType> map = new HashMap<String, DataType>();
		static {
			map.put(STRING.text, STRING);
			map.put(NUMBER.text, NUMBER);
			map.put(DATE.text, DATE);
			map.put(BOOLEAN.text, BOOLEAN);
		}

		public static DataType getDataType(String text) {
			final DataType result = map.get(text);
			if(result == null) {
				throw new RuntimeException("Invalid data type: " + text +
						". Valid types are: " + map.keySet());
			}
			return result;
		}
	}
	private final DataType[] dataTypes;
	private final String[] labels;
	private final String[][] cells;
	private final int rowCount, columnCount;
	
    public Csv(String text) {
		try {
			// Strip out empty lines...
			final BufferedReader lineReader = new BufferedReader(new StringReader(text));
			final StringBuilder sb = new StringBuilder();
			while(true) {
				final String line = lineReader.readLine();
				if(line == null) {
					break;
				}
				final String trimmed = line.trim();
				if(trimmed.length() == 0) {
					continue;
				}
				sb.append(trimmed);
				sb.append('\n');
			}
			
			final CSVReader reader = new CSVReader(new StringReader(sb.toString()));
			@SuppressWarnings("unchecked")
			final List<String[]> rows = reader.readAll();
			
			rowCount = rows.size() - 1;	// Don't count headers
			if(rowCount < 1) {
				throw new RuntimeException("Body needs at least headers + 1 row");
			}
				
			int tmp = 0;
			for(int i = 0; i < rowCount; i++) {
				final String[] sa = rows.get(i);
				final int currentWidth = sa.length;
				if(currentWidth > tmp) {
					tmp = currentWidth;
				}
			}
			columnCount = tmp;
			
			dataTypes = new DataType[columnCount];
			labels = new String[columnCount];
			cells = new String[rowCount][columnCount];
			
			// Figure out the labels and column data types...
			final String[] headers = rows.get(0);
			if(headers.length < columnCount) {
				throw new RuntimeException("Not enough headers for data in body");
			}
			for(int c = 0; c < columnCount; c++) {
				final String header = headers[c];
				final String label;
				final DataType type;
				final int colon = header.indexOf(':');
				if(colon > 0) {
					label = header.substring(0, colon);
					type = DataType.getDataType(header.substring(colon + 1));
				} else {
					label = header;
					type = null;
				}
				labels[c] = AbstractPlugin.makeLabel(label);
				dataTypes[c] = type;
			}
			
			// Transfer the data....
			for(int r = 0; r < rowCount; r++) {
				final String[] rowSource = rows.get(r + 1);
				final String[] row = cells[r];
				final int width = rowSource.length;
				for(int c = 0; c < columnCount; c++) {
					row[c] = c < width ? rowSource[c].trim() : "";
				}
			}
		} catch(final IOException e) {
			throw new RuntimeException("Error parsing CSV", e);
		}
		
	}

	public int getRowCount() {
    	return rowCount;
    }

	public int getColumnCount() {
    	return columnCount;
    }

	public String getDataTypeAsString(int c) {
		return dataTypes[c].text;
    }

	public String getLabel(int c) {
		return labels[c];
    }

	public String get(int r, int c) {
		return cells[r][c];
    }

	public DataType getDataType(int c) {
		return dataTypes[c];
    }

	public void setDataType(int c, DataType dataType) {
		dataTypes[c] = dataType;
    }
}
