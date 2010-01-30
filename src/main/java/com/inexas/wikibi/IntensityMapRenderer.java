package com.inexas.wikibi;

import com.inexas.wikibi.Csv.*;

public class IntensityMapRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
		90,900,		// Minimum / maximum height
		120,1200	// Minimum / maximum width
	};

	public IntensityMapRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "intensitymap";
	}
	
	@Override
    protected String getFunctionName() {
		return "IntensityMap";
    }

	@Override
    void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		for(int c = 0; c < columnCount; c++) {
			checkCsvDataType(c, c == 0 ? DataType.STRING : DataType.NUMBER);
		}
    }

	@Override
    protected int[] getLimits() {
		return limits;
    }

}
