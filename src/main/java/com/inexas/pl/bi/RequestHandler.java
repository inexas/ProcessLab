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

import java.util.*;
import javax.servlet.http.*;

/**
 * The RequestHandler provides an interface between the JavaScript in
 * the Google plugins and the {@link DataServlet}. When the plugin is
 * rendered is publishes a {@link Request} here so that it can be picked
 * up later when the DataServlet is called to get the data.
 * 
 * !todo How do requests that never get taken die?
 */
public class RequestHandler {
	private static final RequestHandler instance = new RequestHandler();
	public final static String PARAM_RID = "r";
	private int nextPluginId, nextRequestId;
	private final Map<String, Request> requestMap = new HashMap<String, Request>();

	public static RequestHandler getInstance() {
		return instance;
	}

	private RequestHandler() {
		// Hide myself...
	}

	public String getPageUniquePluginId() {
		nextPluginId++;
		if(nextPluginId >= 1000) {
			nextPluginId = 0;
		}
		return Integer.toString(nextPluginId++);
	}

	// todo Think about accepting a single parameter containing all the sub parameters
	public String publishRequest(AbstractPlugin plugin, int lives) {
		final Request request = new Request(plugin, lives);
		final String key = new Integer(nextRequestId++).toString();
		requestMap.put(key, request);
		return key;
	}

	public Request getRequest(HttpServletRequest httpRequest) {
		final Request result;
		final String requestId = httpRequest.getParameter(PARAM_RID);
		if(requestId == null) {
			throw new RuntimeException("Missing request ID parameter");
		}
		result = requestMap.remove(requestId);
		if(result == null) {
			throw new RuntimeException("No request for request ID: " + requestId);
		}
		// If there are still some lives left, leave it there
		if(result.useLife()) {
			requestMap.put(requestId, result);
		}
		return result;
	}

}
