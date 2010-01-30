package com.inexas.wikibi;

/**
 * @author Keith Whittingham, Processwide
 */
public class DataView extends AbstractPlugin {
	private boolean spreadsheet;
	
	@Override
	protected void render() {
		try {
			parameters.getFormat().rendererConstructor.newInstance(this).render();
			if(spreadsheet) {
				final SpreadsheetRenderer sheetRenderer = new SpreadsheetRenderer(this);
				sheetRenderer.render();
			}
		} catch(final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error calling renderer: " + e.getMessage(), e);
		}
	}

}
