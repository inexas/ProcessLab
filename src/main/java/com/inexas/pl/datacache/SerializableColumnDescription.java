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
import com.google.visualization.datasource.datatable.*;
import com.google.visualization.datasource.datatable.value.*;

public class SerializableColumnDescription implements Serializable {
	private static final long serialVersionUID = 7536022151082722932L;
	public final String id;
	public final ValueType type;
	public final String label;
	public final String pattern;
	public final Map<String, String> customProperties;

	public SerializableColumnDescription(ColumnDescription cd) {
		id = cd.getId();
		type = cd.getType();
		label = cd.getLabel();
		pattern = cd.getPattern();
		customProperties = cd.getCustomProperties();
	}

	public ColumnDescription getColumnDescription() {
		return new ColumnDescription(id, type, label);
    }

}
