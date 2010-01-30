package com.inexas.wikibi;

import com.inexas.wikibi.Csv.*;

public class TableRenderer extends GoogleChartRenderer {

	public TableRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "table";
	}
	
	@Override
	protected String getFunctionName() {
		return "Table";
	}

	
	@Override
	void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 2, 20);
		for(int c = 0; c < columnCount; c++) {
			if(c == 0) {
				checkCsvDataType(c, DataType.STRING);
			} else {
				checkCsvDataType(c, DataType.STRING, DataType.NUMBER, DataType.DATE, DataType.BOOLEAN);
			}
		}
	}

	@Override
    protected int[] getLimits() {
		return null;
    }

}
