package com.inexas.pl.bi;

public class VerticalBarChartRenderer extends BarChartRenderer {

	public VerticalBarChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "columnchart";
	}
	
	@Override
	protected String getFunctionName() {
		return "ColumnChart";
    }

}
