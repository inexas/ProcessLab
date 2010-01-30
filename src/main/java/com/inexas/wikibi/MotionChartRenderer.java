package com.inexas.wikibi;

import com.inexas.wikibi.Csv.*;

public class MotionChartRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
		200,800,	// Minimum / maximum height
		340,1360	// Minimum / maximum width
	};

	public MotionChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "motionchart";
	}
	
	@Override
	protected String getFunctionName() {
		return "MotionChart";
    }

	@Override
    void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 3, 7);
		for(int c = 0; c < columnCount; c++) {
			switch(c) {
			case 0:
				checkCsvDataType(c, DataType.STRING);
				break;
				
			case 1:
				// todo Should check for date only not time
				checkCsvDataType(c, DataType.DATE);
				break;
				
			default:
				// ?todo Could do a better guess by looking at the data
				checkCsvDataType(c, DataType.NUMBER, DataType.STRING);
				break;
			}
		}
    }

	@Override
    protected int[] getLimits() {
		return limits;
    }

}
