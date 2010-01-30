package com.inexas.wikibi;

import com.inexas.wikibi.Parameters.*;

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
