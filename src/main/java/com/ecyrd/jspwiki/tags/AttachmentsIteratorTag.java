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
import java.util.Collection;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.attachment.AttachmentManager;
import com.ecyrd.jspwiki.attachment.Attachment;

/**
 * Iterates through the list of attachments one has.
 * 
 * <P>
 * <B>Attributes</B>
 * </P>
 * <UL>
 * <LI>page - Page name to refer to. Default is the current page.
 * </UL>
 * 
 * @since 2.0
 */

// FIXME: Too much in common with IteratorTag - REFACTOR
@SuppressWarnings("unchecked")
public class AttachmentsIteratorTag
        extends IteratorTag {
	private static final long serialVersionUID = 0L;

	static Logger log = Logger.getLogger(AttachmentsIteratorTag.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int doStartTag() {
		m_wikiContext = (WikiContext)pageContext.getAttribute(WikiTagBase.ATTR_CONTEXT,
		        PageContext.REQUEST_SCOPE);

		WikiEngine engine = m_wikiContext.getEngine();
		AttachmentManager mgr = engine.getAttachmentManager();
		WikiPage page;

		page = m_wikiContext.getPage();

		if(!mgr.attachmentsEnabled()) {
			return SKIP_BODY;
		}

		try {
			if(page != null && engine.pageExists(page)) {
				Collection atts = mgr.listAttachments(page);

				if(atts == null) {
					log.debug("No attachments to display.");
					// There are no attachments included
					return SKIP_BODY;
				}

				m_iterator = atts.iterator();

				if(m_iterator.hasNext()) {
					Attachment att = (Attachment)m_iterator.next();

					WikiContext context = (WikiContext)m_wikiContext.clone();
					context.setPage(att);
					pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT,
					        context,
					        PageContext.REQUEST_SCOPE);

					pageContext.setAttribute(getId(), att);
				} else {
					return SKIP_BODY;
				}
			} else {
				return SKIP_BODY;
			}

			return EVAL_BODY_BUFFERED;
		} catch(ProviderException e) {
			log.fatal("Provider failed while trying to iterator through history", e);
			// FIXME: THrow something.
		}

		return SKIP_BODY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int doAfterBody() {
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
			Attachment att = (Attachment)m_iterator.next();

			WikiContext context = (WikiContext)m_wikiContext.clone();
			context.setPage(att);
			pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT,
			        context,
			        PageContext.REQUEST_SCOPE);

			pageContext.setAttribute(getId(), att);

			return EVAL_BODY_BUFFERED;
		}

		return SKIP_BODY;
	}
}
