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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;

/**
 * Base class for JSPWiki tags. You do not necessarily have to derive from this
 * class, since this does some initialization.
 * <P>
 * This tag is only useful if you're having an "empty" tag, with no body
 * content.
 * 
 * @since 2.0
 */
public abstract class WikiTagBase
        extends TagSupport
        implements TryCatchFinally {
	/**
     * 
     */
    private static final long serialVersionUID = 5608604081031310692L;

	public static final String ATTR_CONTEXT = "jspwiki.context";

	static Logger log = Logger.getLogger(WikiTagBase.class);

	protected WikiContext m_wikiContext;

	/**
	 * This method calls the parent setPageContext() but it also provides a way
	 * for a tag to initialize itself before any of the setXXX() methods are
	 * called.
	 */
	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);

		initTag();
	}

	/**
	 * This method is called when the tag is encountered within a new request,
	 * but before the setXXX() methods are called. The default implementation
	 * does nothing.
	 * 
	 * @since 2.3.92
	 */
	public void initTag() {
		m_wikiContext = null;
		return;
	}

	public int doStartTag()
	        throws JspException {
		try {
			m_wikiContext = (WikiContext)pageContext.getAttribute(ATTR_CONTEXT,
			        PageContext.REQUEST_SCOPE);

			if(m_wikiContext == null) {
				throw new JspException("WikiContext may not be NULL - serious internal problem!");
			}

			return doWikiStartTag();
		} catch(Exception e) {
			log.error("Tag failed", e);
			throw new JspException("Tag failed, check logs: " + e.getMessage());
		}
	}

	/**
	 * This method is allowed to do pretty much whatever he wants. We then catch
	 * all mistakes.
	 */
	public abstract int doWikiStartTag() throws Exception;

	public int doEndTag()
	        throws JspException {
		return EVAL_PAGE;
	}

	public void doCatch(Throwable arg0) throws Throwable {
		//
	}

	public void doFinally() {
		m_wikiContext = null;
	}

	public void setId(String id) {
		super.setId(TextUtil.replaceEntities(id));
	}

}
