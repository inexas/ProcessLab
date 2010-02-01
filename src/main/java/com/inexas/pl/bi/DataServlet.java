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

import javax.servlet.http.*;
import com.google.visualization.datasource.*;
import com.google.visualization.datasource.datatable.*;
import com.google.visualization.datasource.query.*;

public class DataServlet extends DataSourceServlet {
	private static final long serialVersionUID = 1463015448309515544L;

	public DataTable generateDataTable(Query query, HttpServletRequest httpRequest) {
		final RequestHandler requestHandler = RequestHandler.getInstance();
		final Request request = requestHandler.getRequest(httpRequest);
		return request.getDataTable(httpRequest);
	}

	@Override
    protected boolean isRestrictedAccessMode() {
		// todo Make this configurable
		return false;
    }

}
