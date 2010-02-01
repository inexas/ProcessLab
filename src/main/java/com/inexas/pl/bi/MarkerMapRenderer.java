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

import com.inexas.pl.bi.Csv.*;

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
