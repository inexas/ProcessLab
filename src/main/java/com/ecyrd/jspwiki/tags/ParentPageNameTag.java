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
package com.ecyrd.jspwiki.tags;

import java.io.IOException;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;

/**
 * Returns the parent of the currently requested page. Weblog entries are
 * recognized as subpages of the weblog page.
 * 
 * @since 2.0
 */
public class ParentPageNameTag
        extends WikiTagBase {
	private static final long serialVersionUID = 0L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int doWikiStartTag()
	        throws IOException {
		WikiEngine engine = m_wikiContext.getEngine();
		WikiPage page = m_wikiContext.getPage();

		if(page != null) {
			if(page instanceof Attachment) {
				pageContext.getOut().print(engine.beautifyTitle(((Attachment)page).getParentName()));
			} else {
				String name = page.getName();

				int entrystart = name.indexOf("_blogentry_");

				if(entrystart != -1) {
					name = name.substring(0, entrystart);
				}

				int commentstart = name.indexOf("_comments_");

				if(commentstart != -1) {
					name = name.substring(0, commentstart);
				}

				pageContext.getOut().print(engine.beautifyTitle(name));
			}
		}

		return SKIP_BODY;
	}
}
