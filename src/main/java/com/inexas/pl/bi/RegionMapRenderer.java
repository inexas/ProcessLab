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
