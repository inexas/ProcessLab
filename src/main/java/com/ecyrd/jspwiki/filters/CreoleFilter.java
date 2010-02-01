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
package com.ecyrd.jspwiki.filters;

import java.util.Properties;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.filters.BasicPageFilter;
import com.ecyrd.jspwiki.filters.FilterException;
import com.ecyrd.jspwiki.parser.CreoleToJSPWikiTranslator;

/**
 * <p>
 * Provides the Implementation for mixed mode creole: If you activate this
 * filter, it will translate all markup that was saved as creole markup to
 * JSPWiki markup. Therefore the files will be saved with mixed markup.
 * <p>
 * <b>WARNING</b>: There's no turning back after insalling this filter. Since
 * your wiki pages are saved in Creole markup you can not deactivate it
 * afterwards.
 * <p>
 * <b>WARNING</b>: This feature is completely experimental, and is known to be
 * broken. Use at your own risk.
 * <p>
 * <b>WARNING</b>: The CreoleFilter feature is deprecated. JSPWiki is likely to
 * implement a non-mixed mode Creole at some point, since turning on Creole will
 * make new pages obsolete.
 * 
 * @author Steffen Schramm
 * @author Hanno Eichelberger
 * @author Christoph Sauer
 * 
 * @see <a
 *      href="http://www.wikicreole.org/wiki/MixedMode">[[WikiCreole:MixedMode]]</a>
 */

public class CreoleFilter extends BasicPageFilter {
	/**
	 * {@inheritDoc}
	 */
	public void initialize(WikiEngine engine, Properties props) throws FilterException {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	public String preSave(WikiContext wikiContext, String content)
	        throws FilterException {
		try {
			String username = wikiContext.getCurrentUser().getName();
			Properties prop = wikiContext.getEngine().getWikiProperties();
			return new CreoleToJSPWikiTranslator().translateSignature(prop, content, username);
		} catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public String preTranslate(WikiContext wikiContext, String content)
	        throws FilterException {
		try {
			Properties prop = wikiContext.getEngine().getWikiProperties();
			return new CreoleToJSPWikiTranslator().translate(prop, content);

		} catch(Exception e) {
			e.printStackTrace();
			return content
			        + "\n \n %%error \n"
			        + "[CreoleFilterError]: This page was not translated by the CreoleFilter due to "
			        + "the following error: " + e.getMessage() + "\n \n"
			        + "%%\n \n";
		}
	}

}
