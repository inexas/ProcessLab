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
