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
package com.ecyrd.jspwiki.dav.items;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.ecyrd.jspwiki.dav.DavPath;
import com.ecyrd.jspwiki.dav.DavProvider;

/**
 * 
 * @since
 */
@SuppressWarnings("unchecked")
public abstract class DavItem {
	protected DavProvider m_provider;
	protected ArrayList<DavItem> m_items = new ArrayList<DavItem>();
	protected DavPath m_path;

	protected DavItem(DavProvider provider, DavPath path) {
		m_provider = provider;
		m_path = path;
	}

	public DavPath getPath() {
		return m_path;
	}

	public abstract Collection getPropertySet();

	public abstract String getHref();

	public abstract InputStream getInputStream();

	public abstract long getLength();

	public abstract String getContentType();

	public Iterator iterator(int depth) {
		ArrayList<DavItem> list = new ArrayList<DavItem>();

		if(depth == 0) {
			list.add(this);
		} else if(depth == 1) {
			list.add(this);
			list.addAll(m_items);
		} else if(depth == -1) {
			list.add(this);

			for(Iterator i = m_items.iterator(); i.hasNext();) {
				DavItem di = (DavItem)i.next();

				for(Iterator j = di.iterator(-1); i.hasNext();) {
					list.add((DavItem)j.next());
				}
			}
		}

		return list.iterator();
	}
}
