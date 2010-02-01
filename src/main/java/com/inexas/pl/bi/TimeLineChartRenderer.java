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

public class TimeLineChartRenderer extends GoogleChartRenderer {
	private static final int[] limits = {
	        90, 900, // Minimum / maximum height
	        120, 1200 // Minimum / maximum width
	};

	public TimeLineChartRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	protected String getPackageName() {
		return "annotatedtimeline";
	}

	@Override
	protected String getFunctionName() {
		return "AnnotatedTimeLine";
	}

	@Override
	void checkCsv() {
		final Csv csv = parameters.getCsv();
		final int columnCount = csv.getColumnCount();
		checkCsvColumnCount(csv, 2, 20);
		for(int c = 0; c < columnCount; c++) {
			if(c == 0) {
				checkCsvDataType(c, DataType.DATE);
			} else if(((c - 1) % 3) == 0) {
				checkCsvDataType(c, DataType.NUMBER);
			} else {
				checkCsvDataType(c, DataType.STRING);
			}
		}
	}

	public int[] getLimits() {
		return limits;
	}

}
