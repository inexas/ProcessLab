/*
  Copyright (c) Inexas 2010

  Modifications licensed under the Inexas Software License V1.0. You
  may not use this file except in compliance with the License.

  The License is available at: http://www.inexas.com/ISL-V1.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  The original file and contents are licensed under a separate license:
  see below.
*/
/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package com.ecyrd.jspwiki.dav;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.dav.methods.DavMethod;
import com.ecyrd.jspwiki.dav.methods.GetMethod;
import com.ecyrd.jspwiki.dav.methods.PropFindMethod;

/**
 * 
 * @since
 */
public class WikiDavServlet extends WebdavServlet {
	private static final long serialVersionUID = 1L;

	private WikiEngine m_engine;
	Logger log = Logger.getLogger(this.getClass().getName());
	private DavProvider m_rawProvider;
	private DavProvider m_rootProvider;
	private DavProvider m_htmlProvider;

	@Override
    public void init(ServletConfig config)
	        throws ServletException {
		super.init(config);

		m_engine = WikiEngine.getInstance(config);

		m_rawProvider = new RawPagesDavProvider(m_engine);
		m_rootProvider = new WikiRootProvider(m_engine);
		m_htmlProvider = new HTMLPagesDavProvider(m_engine);
	}

	private DavProvider pickProvider(String context) {
		if(context.equals("raw"))
			return m_rawProvider;
		else if(context.equals("html"))
			return m_htmlProvider;

		return m_rootProvider;
	}

	@Override
    public void doPropFind(HttpServletRequest req, HttpServletResponse res)
	        throws IOException, ServletException {
		StopWatch sw = new StopWatch();
		sw.start();

		// Do the "sanitize url" trick
		String p = new String(req.getPathInfo().getBytes("ISO-8859-1"), "UTF-8");

		DavPath path = new DavPath(p);
		if(path.isRoot()) {
			DavMethod dm = new PropFindMethod(m_rootProvider);
			dm.execute(req, res, path);
		} else {
			String context = path.get(0);

			PropFindMethod m = new PropFindMethod(pickProvider(context));
			m.execute(req, res, path.subPath(1));
		}

		sw.stop();

		log.debug("Propfind done for path " + path + ", took " + sw);
	}

	@Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
		log.debug("DAV doOptions for path " + req.getPathInfo());

		res.setHeader("DAV", "1"); // We support only Class 1
		res.setHeader("Allow", "GET, PUT, POST, OPTIONS, PROPFIND, PROPPATCH, MOVE, COPY, DELETE");
		res.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
    public void doMkCol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getContentLength() > 0) {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Message may contain no body");
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");
		}
	}

	@Override
    public void doPropPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		// DavMethod dm = new PropPatchMethod( m_rawProvider );

		// dm.execute( request, response );
	}

	@Override
    public void doCopy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");
	}

	@Override
    public void doMove(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");

	}

	@Override
    protected void doDelete(HttpServletRequest arg0, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");
	}

	@Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");
	}

	@Override
    protected void doPut(HttpServletRequest arg0, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JSPWiki is read-only.");
	}

	/*
	 * GET /dav/raw/WikiPage.txt GET /dav/html/WikiPage.html GET
	 * /dav/pdf/WikiPage.pdf GET /dav/raw/WikiPage/attachment1.png
	 */
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
	        throws ServletException, IOException {
		// Do the "sanitize url" trick
		// String p = new String(req.getPathInfo().getBytes("ISO-8859-1"),
		// "UTF-8");

		String p = req.getPathInfo();

		DavPath path = new DavPath(p);

		if(path.isRoot()) {
			DavMethod dm = new GetMethod(m_rootProvider);
			dm.execute(req, res, path);
		} else {
			DavMethod dm = new GetMethod(pickProvider(path.get(0)));

			dm.execute(req, res, path.subPath(1));
		}
	}
}
