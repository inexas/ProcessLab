package com.inexas.wikibi;

import com.inexas.wikibi.Csv.*;

public class MarkerMapRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
	        150, 600, // Minimum / maximum height
	        270, 1080 // Minimum / maximum width
	};

	public MarkerMapRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "geomap";
	}
	
	@Override
	protected String getFunctionName() {
		return "GeoMap";
	}

	@Override
    void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		for(int c = 0; c < columnCount; c++) {
			checkCsvDataType(c, c == 3 ? DataType.STRING : DataType.NUMBER);
		}
    }
	
	@Override
    protected int[] getLimits() {
		return limits;
    }

}
