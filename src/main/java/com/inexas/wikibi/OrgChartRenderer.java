package com.inexas.wikibi;

import com.inexas.wikibi.Csv.*;

public class OrgChartRenderer extends GoogleChartRenderer {

	public OrgChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "orgchart";
	}
	
	@Override
	protected String getFunctionName() {
		return "OrgChart";
	}

	@Override
    void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 2, 3);
		for(int c = 0; c < columnCount; c++) {
			checkCsvDataType(c, DataType.STRING);
		}
    }
	
	@Override
    protected int[] getLimits() {
		return null;
    }

}
