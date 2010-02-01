package com.inexas.pl.bi;

import com.inexas.pl.bi.Csv.*;

abstract class BarChartRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
		90,900,		// Minimum / maximum height
		120,1200	// Minimum / maximum width
	};

	protected BarChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
    void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 2, 20);
		for(int c = 0; c < columnCount; c++) {
			switch(c) {
			case 0:
				checkCsvDataType(c, DataType.STRING);
				break;
				
			default:
				checkCsvDataType(c, DataType.NUMBER);
				break;
			}
		}
    }

	@Override
    protected int[] getLimits() {
		return limits;
    }

}
