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
package com.ecyrd.jspwiki.ui.admin.beans;

import javax.management.NotCompliantMBeanException;
import javax.servlet.http.HttpServletRequest;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.ui.admin.AdminBean;
import com.ecyrd.management.SimpleMBean;

/**
 * This class is still experimental.
 * 
 * 
 */
public class PlainEditorAdminBean
        extends SimpleMBean
        implements AdminBean {
	private static final String TEMPLATE =
	        "<div>" +
	        "<input type='checkbox' id='ajax' %checked/>Use AJAX?<br />" +
	        "<input type='submit' value='Submit'/>" +
	        "%messages" +
	        "</div>";

	private boolean m_checked = false;

	private static final String[] ATTRIBUTES = { "title", "checked" };
	private static final String[] METHODS = {};

	public PlainEditorAdminBean() throws NotCompliantMBeanException {
		//
	}

	public String doGet(WikiContext context) {
		HttpServletRequest req = context.getHttpRequest();

		if(req != null && req.getMethod().equals("POST") && getTitle().equals(req.getParameter("form"))) {
			return doPost(context);
		}
		String base = TEMPLATE;

		base = TextUtil.replaceString(base, "%checked", "checked='checked'");
		base = TextUtil.replaceString(base, "%messages", "");

		return base;
	}

	public String doPost(WikiContext context) {
		HttpServletRequest req = context.getHttpRequest();

		boolean checked = "checked".equals(req.getParameter("id"));

		// Make changes

		String base = TEMPLATE;

		base = TextUtil.replaceString(base, "%checked", checked ? "checked='checked'" : "");
		base = TextUtil
		        .replaceString(base, "%messages", "<br /><font color='red'>Your settings have been saved</font>");

		return base;
	}

	public String getTitle() {
		return "Plain editor";
	}

	public int getType() {
		return EDITOR;
	}

	public boolean isEnabled() {
		return true;
	}

	public String getId() {
		return "editor.plain";
	}

	public boolean getChecked() {
		return m_checked;
	}

	public String[] getAttributeNames() {
		return ATTRIBUTES;
	}

	public String[] getMethodNames() {
		return METHODS;
	}

	public void initialize(WikiEngine engine) {
		// XXX Auto-generated method stub

	}
}
