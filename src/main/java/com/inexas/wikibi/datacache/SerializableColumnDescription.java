package com.inexas.wikibi.datacache;

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
