package com.inexas.wikibi.datacache;

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
