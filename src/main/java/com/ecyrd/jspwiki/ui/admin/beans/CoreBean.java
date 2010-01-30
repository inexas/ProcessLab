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
import com.ecyrd.jspwiki.Release;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.ui.admin.SimpleAdminBean;

/**
 * An AdminBean which manages the JSPWiki core operations.
 * 
 */
public class CoreBean
        extends SimpleAdminBean {
	private static final String[] ATTRIBUTES = { "pages", "version" };
	private static final String[] METHODS = {};
	private WikiEngine m_engine;

	public CoreBean(WikiEngine engine) throws NotCompliantMBeanException {
		m_engine = engine;
	}

	/**
	 * Return the page count in the Wiki.
	 * 
	 * @return the page content
	 */
	public int getPages() {
		return m_engine.getPageCount();
	}

	public String getPagesDescription() {
		return "The total number of pages in this wiki";
	}

	public String getVersion() {
		return Release.VERSTR;
	}

	public String getVersionDescription() {
		return "The JSPWiki engine version";
	}

	public String getTitle() {
		return "Core bean";
	}

	public int getType() {
		return CORE;
	}

	public String getId() {
		return "corebean";
	}

	public String[] getAttributeNames() {
		return ATTRIBUTES;
	}

	public String[] getMethodNames() {
		return METHODS;
	}

}
