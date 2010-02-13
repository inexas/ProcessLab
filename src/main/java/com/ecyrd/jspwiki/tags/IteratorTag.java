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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.PageContext;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiPage;

/**
 * Iterates through tags.
 * 
 * <P>
 * <B>Attributes</B>
 * </P>
 * <UL>
 * <LI>list - a collection.
 * </UL>
 * 
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public abstract class IteratorTag
        extends BodyTagSupport
        implements TryCatchFinally {

	/**
     * 
     */
    private static final long serialVersionUID = 446317000510214249L;
	protected String m_pageName;
	protected Iterator m_iterator;
	protected WikiContext m_wikiContext;

	private static Logger log = Logger.getLogger(IteratorTag.class);

	/**
	 * Sets the collection that is used to form the iteration.
	 * 
	 * @param arg
	 *            A Collection which will be iterated.
	 */
	public void setList(Collection arg) {
		if(arg != null)
			m_iterator = arg.iterator();
	}

	/**
	 * Sets the collection list, but using an array.
	 * 
	 * @param arg
	 *            An array of objects which will be iterated.
	 */
	public void setList(Object[] arg) {
		if(arg != null) {
			m_iterator = Arrays.asList(arg).iterator();
		}
	}

	/**
	 * Sets the iterator directly that is used to form the iteration.
	 */
	/*
	 * public void setList( Iterator arg ) { m_iterator = arg; }
	 */

	/**
	 * Clears the iterator away. After calling this method doStartTag() will
	 * always return SKIP_BODY
	 */
	public void clearList() {
		m_iterator = null;
	}

	/**
	 * Override this method to reset your own iterator.
	 */
	public void resetIterator() {
		// No operation here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public int doStartTag() {
		m_wikiContext = WikiContext.findContext(pageContext);

		resetIterator();

		if(m_iterator == null)
			return SKIP_BODY;

		if(m_iterator.hasNext()) {
			buildContext();
		}

		return EVAL_BODY_BUFFERED;
	}

	/**
	 * Arg, I hate globals.
	 */
	private void buildContext() {
		//
		// Build a clone of the current context
		//
		WikiContext context = (WikiContext)m_wikiContext.clone();

		Object o = m_iterator.next();

		if(o instanceof WikiPage)
			context.setPage((WikiPage)o);

		//
		// Push it to the iterator stack, and set the id.
		//
		pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT,
		        context,
		        PageContext.REQUEST_SCOPE);
		pageContext.setAttribute(getId(),
		        o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public int doEndTag() {
		// Return back to the original.
		pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT,
		        m_wikiContext,
		        PageContext.REQUEST_SCOPE);

		return EVAL_PAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public int doAfterBody() {
		if(bodyContent != null) {
			try {
				JspWriter out = getPreviousOut();
				out.print(bodyContent.getString());
				bodyContent.clearBody();
			} catch(IOException e) {
				log.error("Unable to get inner tag text", e);
				// FIXME: throw something?
			}
		}

		if(m_iterator != null && m_iterator.hasNext()) {
			buildContext();
			return EVAL_BODY_BUFFERED;
		}

		return SKIP_BODY;
	}

	/**
	 * In case your tag throws an exception at any point, you can override this
	 * method and implement a custom exception handler.
	 * <p>
	 * By default, this handler does nothing.
	 * 
	 * @param arg0
	 *            The Throwable that the tag threw
	 * 
	 * @throws Throwable
	 *             I have no idea why this would throw anything
	 */
	public void doCatch(Throwable arg0) throws Throwable {
		//
	}

	/**
	 * Executed after the tag has been finished. This is a great place to put
	 * any cleanup code. However you <b>must</b> call super.doFinally() if you
	 * override this method, or else some of the things may not work as
	 * expected.
	 */
	public void doFinally() {
		resetIterator();
		m_iterator = null;
		m_pageName = null;
		m_wikiContext = null;
	}

}
