package com.inexas.wikibi;

public class RegionMapRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
		150,600,	// Minimum / maximum height
		270,1080	// Minimum / maximum width
	};

	public RegionMapRenderer(DataView plugin) {
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
		// todo Implement me
        throw new RuntimeException("How about implementing me?!");
    }

	@Override
    protected int[] getLimits() {
		return limits;
    }

}
