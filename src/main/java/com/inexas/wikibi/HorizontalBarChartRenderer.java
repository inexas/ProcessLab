package com.inexas.wikibi;

public class HorizontalBarChartRenderer extends BarChartRenderer {

	public HorizontalBarChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "barchart";
	}
	
	@Override
    protected String getFunctionName() {
		return "BarChart";
    }

}
