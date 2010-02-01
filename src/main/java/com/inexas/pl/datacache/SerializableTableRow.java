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
package com.inexas.pl.datacache;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import com.google.visualization.datasource.datatable.*;

public class SerializableTableRow implements Serializable {
	private static final long serialVersionUID = -9027802620714823368L;
	public final List<SerializableTableCell> cells = new ArrayList<SerializableTableCell>();
	public final Map<String, String> customProperties;

    public SerializableTableRow(TableRow row) {
    	for(final TableCell cell : row.getCells()) {
    		cells.add(new SerializableTableCell(cell));
    	}
    	
    	customProperties = row.getCustomProperties();
    }

	public TableRow getTableRow() {
		final TableRow result = new TableRow();
		for(final SerializableTableCell cell : cells) {
			result.addCell(cell.getTableCell());
		}

		if(customProperties != null) {
			for(final Entry<String, String> entry : customProperties.entrySet())  {
				result.setCustomProperty(entry.getKey(), entry.getValue());
			}
		}

		return result;
    }


}
