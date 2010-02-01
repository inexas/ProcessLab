package com.inexas.pl.bi;

import com.inexas.pl.bi.Csv.*;

public class TimeLineChartRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
	        90, 900, // Minimum / maximum height
	        120, 1200 // Minimum / maximum width
	};

	public TimeLineChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "annotatedtimeline";
	}

	@Override
	protected String getFunctionName() {
		return "AnnotatedTimeLine";
	}

	@Override
	void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 2, 20);
		for(int c = 0; c < columnCount; c++) {
			if(c == 0) {
				checkCsvDataType(c, DataType.DATE);
			} else if(((c - 1) % 3) == 0) {
				checkCsvDataType(c, DataType.NUMBER);
			} else {
				checkCsvDataType(c, DataType.STRING);
			}
		}
	}

	public int[] getLimits() {
		return limits;
	}

}
