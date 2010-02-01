package com.inexas.pl.datacache;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import com.google.visualization.datasource.base.*;
import com.google.visualization.datasource.datatable.*;
import com.ibm.icu.util.*;

public class SerializableDataTable implements Serializable {
    private static final long serialVersionUID = -3824976001967632990L;
	public final List<SerializableColumnDescription> columns = new ArrayList<SerializableColumnDescription>();
	public final List<SerializableTableRow> rows = new ArrayList<SerializableTableRow>();
	public final Map<String, String> customProperties;
	public final List<SerializableWarning> warnings = new ArrayList<SerializableWarning>();
	public final ULocale localeForUserMessages;

	public SerializableDataTable(DataTable dataTable) {
		
		for(final ColumnDescription columnDescription : dataTable.getColumnDescriptions()) {
			columns.add(new SerializableColumnDescription(columnDescription));
		}
		
		for(final TableRow row : dataTable.getRows()) {
			rows.add(new SerializableTableRow(row));
		}
		
		customProperties = dataTable.getCustomProperties();
		
		for(final Warning warning : dataTable.getWarnings()) {
			warnings.add(new SerializableWarning(warning));
		}
		
		localeForUserMessages = dataTable.getLocaleForUserMessages();
		
	}

	public DataTable getDataTable() {
		final DataTable result = new DataTable();
		for(final SerializableColumnDescription column : columns) {
			result.addColumn(column.getColumnDescription());
		}
		
		for(final SerializableTableRow row : rows) {
			try {
	            result.addRow(row.getTableRow());
            } catch(final TypeMismatchException e) {
            	throw new RuntimeException("Error deserializing", e);
            }
		}
		
		if(customProperties != null) {
			for(final Entry<String, String> entry : customProperties.entrySet())  {
				result.setCustomProperty(entry.getKey(), entry.getValue());
			}
		}

		for(final SerializableWarning warning : warnings) {
			result.addWarning(warning.getWarning());
		}
		
		result.setLocaleForUserMessages(localeForUserMessages);
		
		return result;
    }

}
