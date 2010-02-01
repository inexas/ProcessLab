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

import com.inexas.pl.bi.Parameters.*;

// !todo fix the target param
public class SpreadsheetRenderer extends Renderer {

	public SpreadsheetRenderer(DataView plugin) {
		super(plugin);
	}

	@Override
	public void render() {
		final StringBuilder sb = plugin.sb;
		final RequestHandler requestHandler = RequestHandler.getInstance();
		final String requestId = requestHandler.publishRequest(plugin, 1);
		final Parameter<?> parameter = plugin.parameters.getParameter(Parameters.TARGET_KEY);
		final String target = parameter == null ? null : parameter.getValue().toString();
		if(target == null) {
			// <a href=http://xxx/ss?r=nnn>
			// <img src=images/numbers.png alt="Generate spreadsheet" border=0>
			// </a>
			sb.append("<a href=");
			sb.append(getBaseURL());
			sb.append("ss?r=");
			sb.append(requestId);
			sb.append("><img src=images/");
			final String userAgent = plugin.context.getHttpRequest().getHeader("User-Agent");
			final boolean isOsx = userAgent.indexOf("Mac OS X") > 0;
			final String icon = isOsx ? "numbers-16x16.jpg" : "excel-16x16.gif";
			sb.append(icon);
			sb.append(" title=\"Click to download data in spreadsheet\" border=0></a>");
		} else if(target.endsWith(".png") || target.endsWith(".gif") || target.endsWith(".jpg")) {
			// <a href=http://xxx/ss?r=nnn>
			// <img src=image.png alt="Generate spreadsheet" border=0>
			// </a>
			sb.append("<a href=");
			sb.append(getBaseURL());
			sb.append("ss?r=");
			sb.append(requestId);
			sb.append("><img src=");
			sb.append(target);
			sb.append(" title=\"Click to download data in spreadsheet\" border=0></a>");
		} else {
			// <a href=http://xxx/ss?r=nnn>
			// The text here
			// </a>
			sb.append("<a href=");
			sb.append(getBaseURL());
			sb.append("ss?r=");
			sb.append(requestId);
			sb.append(">");
			sb.append(target);
			sb.append("</a>");
		}
	}

	private String getBaseURL() {
		return plugin.context == null ? "http://testbaseurl/" : plugin.context.getEngine().getBaseURL();
	}

}
