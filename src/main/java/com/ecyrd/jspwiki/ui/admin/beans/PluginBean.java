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

import java.util.Collection;
import java.util.Iterator;
import javax.management.NotCompliantMBeanException;
import org.apache.ecs.xhtml.*;
import com.ecyrd.jspwiki.Release;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.plugin.PluginManager.WikiPluginInfo;
import com.ecyrd.jspwiki.ui.admin.SimpleAdminBean;

public class PluginBean extends SimpleAdminBean {
	private WikiEngine m_engine;

	public PluginBean(WikiEngine engine) throws NotCompliantMBeanException {
		m_engine = engine;
	}

	public String[] getAttributeNames() {
		return new String[0];
	}

	public String[] getMethodNames() {
		return new String[0];
	}

	public String getTitle() {
		return "Plugins";
	}

	public int getType() {
		return CORE;
	}

	@SuppressWarnings("unchecked")
	public String doGet(WikiContext context) {
		Collection<WikiPluginInfo> plugins = m_engine.getPluginManager().modules();

		div root = new div();

		root.addElement(new h4("Plugins"));

		table tb = new table().setBorder(1);
		root.addElement(tb);

		tr head = new tr();
		head.addElement(new th("Name"));
		head.addElement(new th("Alias"));
		head.addElement(new th("Author"));
		head.addElement(new th("Notes"));

		tb.addElement(head);

		for(Iterator<WikiPluginInfo> i = plugins.iterator(); i.hasNext();) {
			tr row = new tr();
			tb.addElement(row);

			WikiPluginInfo info = i.next();

			row.addElement(new td(info.getName()));
			row.addElement(new td(info.getAlias()));
			row.addElement(new td(info.getAuthor()));

			String verWarning = "";
			if(!(Release.isNewerOrEqual(info.getMinVersion()) && Release.isOlderOrEqual(info.getMaxVersion()))) {
				verWarning = "<span class='warning'>This module is not compatible with this version of JSPWiki.</span>";
			}

			row.addElement(new td(verWarning));
		}

		return root.toString();
	}

}
