package com.inexas.pl.bi;

import com.inexas.pl.bi.Csv.*;

public class PieChartRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
	        90, 900, // Minimum / maximum height
	        120, 1200 // Minimum / maximum width
	};

	public PieChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "piechart";
	}
	
	@Override
	protected String getFunctionName() {
		return "PieChart";
	}

	@Override
	void checkCsv() {
		final Csv csv = parameters.getCsv();
		checkCsvColumnCount(csv, 2, 2);
		checkCsvDataType(0, DataType.STRING);
		checkCsvDataType(1, DataType.NUMBER);
	}

	@Override
    protected int[] getLimits() {
		return limits;
    }

}
