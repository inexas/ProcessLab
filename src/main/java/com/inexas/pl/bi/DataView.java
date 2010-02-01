/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.pl.bi;

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
