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
package com.ecyrd.jspwiki.ui.admin;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.parser.WikiDocument;
import com.ecyrd.jspwiki.render.RenderingManager;

/**
 * This class is still experimental.
 * 
 */
@SuppressWarnings("unchecked")
public abstract class WikiFormAdminBean
        implements AdminBean {
	public abstract String getForm(WikiContext context);

	public abstract void handleResponse(WikiContext context, Map params);

	public String doGet(WikiContext context) {
		String result = "";

		String wikiMarkup = getForm(context);

		RenderingManager mgr = context.getEngine().getRenderingManager();

		WikiDocument doc;
		try {
			doc = mgr.getParser(context, wikiMarkup).parse();
			result = mgr.getHTML(context, doc);
		} catch(IOException e) {
			// XXX Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public String handlePost(@SuppressWarnings("unused") WikiContext context, @SuppressWarnings("unused") HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse resp) {
		return null;
		// FIXME: Not yet implemented
	}
}
