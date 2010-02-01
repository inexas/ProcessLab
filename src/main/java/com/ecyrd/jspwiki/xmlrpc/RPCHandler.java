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
package com.ecyrd.jspwiki.xmlrpc;

import java.io.UnsupportedEncodingException;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;
import com.ecyrd.jspwiki.auth.permissions.PermissionFactory;

/**
 * Provides handlers for all RPC routines.
 * 
 * @since 1.6.6
 */
// We could use WikiEngine directly, but because of introspection it would
// show just too many methods to be safe.

public class RPCHandler
        extends AbstractRPCHandler {
	private static Logger log = Logger.getLogger(RPCHandler.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void initialize(WikiContext ctx) {
		super.initialize(ctx);
	}

	/**
	 * Converts Java string into RPC string.
	 */
	private String toRPCString(String src) {
		return TextUtil.urlEncodeUTF8(src);
	}

	/**
	 * Converts RPC string (UTF-8, url encoded) into Java string.
	 */
	private String fromRPCString(String src) {
		return TextUtil.urlDecodeUTF8(src);
	}

	/**
	 * Transforms a Java string into UTF-8.
	 */
	private byte[] toRPCBase64(String src) {
		try {
			return src.getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
			//
			// You shouldn't be running JSPWiki on a platform that does not
			// use UTF-8. We revert to platform default, so that the other
			// end might have a chance of getting something.
			//
			log.fatal("Platform does not support UTF-8, reverting to platform default");
			return src.getBytes();
		}
	}

	public String getApplicationName() {
		checkPermission(PagePermission.VIEW);
		return toRPCString(m_engine.getApplicationName());
	}

	@SuppressWarnings("unchecked")
    public Vector getAllPages() {
		checkPermission(PagePermission.VIEW);
		Collection pages = m_engine.getRecentChanges();
		Vector<String> result = new Vector<String>();

		for(Iterator i = pages.iterator(); i.hasNext();) {
			WikiPage p = (WikiPage)i.next();
			if(!(p instanceof Attachment)) {
				result.add(toRPCString(p.getName()));
			}
		}

		return result;
	}

	/**
	 * Encodes a single wiki page info into a Hashtable.
	 */
	@SuppressWarnings("boxing")
    @Override
    protected Hashtable<String, Object> encodeWikiPage(WikiPage page) {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();

		ht.put("name", toRPCString(page.getName()));

		Date d = page.getLastModified();

		//
		// Here we reset the DST and TIMEZONE offsets of the
		// calendar. Unfortunately, I haven't thought of a better
		// way to ensure that we're getting the proper date
		// from the XML-RPC thingy, except to manually adjust the date.
		//

		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MILLISECOND,
		        -(cal.get(Calendar.ZONE_OFFSET) +
		        (cal.getTimeZone().inDaylightTime(d) ? cal.get(Calendar.DST_OFFSET) : 0)));

		ht.put("lastModified", cal.getTime());
		ht.put("version", page.getVersion());

		if(page.getAuthor() != null) {
			ht.put("author", toRPCString(page.getAuthor()));
		}

		return ht;
	}

	@Override
    @SuppressWarnings("unchecked")
    public Vector getRecentChanges(Date since) {
		checkPermission(PagePermission.VIEW);
		Collection pages = m_engine.getRecentChanges();
		Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(since);

		//
		// Convert UTC to our time.
		//
		cal.add(Calendar.MILLISECOND,
		        (cal.get(Calendar.ZONE_OFFSET) +
		        (cal.getTimeZone().inDaylightTime(since) ? cal.get(Calendar.DST_OFFSET) : 0)));
		Date newSince = cal.getTime();

		for(Iterator i = pages.iterator(); i.hasNext();) {
			WikiPage page = (WikiPage)i.next();

			if(page.getLastModified().after(newSince) && !(page instanceof Attachment)) {
				result.add(encodeWikiPage(page));
			}
		}

		return result;
	}

	/**
	 * Simple helper method, turns the incoming page name into normal Java
	 * string, then checks page condition.
	 * 
	 * @param pagename
	 *            Page Name as an RPC string (URL-encoded UTF-8)
	 * @return Real page name, as Java string.
	 * @throws XmlRpcException
	 *             , if there is something wrong with the page.
	 */
	private String parsePageCheckCondition(String pagenameParam) throws XmlRpcException {
		String pagename = fromRPCString(pagenameParam);

		if(!m_engine.pageExists(pagename)) {
			throw new XmlRpcException(ERR_NOPAGE, "No such page '" + pagename + "' found, o master.");
		}

		WikiPage p = m_engine.getPage(pagename);

		checkPermission(PermissionFactory.getPagePermission(p, PagePermission.VIEW_ACTION));

		return pagename;
	}

	@SuppressWarnings("unchecked")
    public Hashtable getPageInfo(String pagenameParam) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);
		return encodeWikiPage(m_engine.getPage(pagename));
	}

	@SuppressWarnings("unchecked")
    public Hashtable getPageInfoVersion(String pagenameParam, int version) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		return encodeWikiPage(m_engine.getPage(pagename, version));
	}

	public byte[] getPage(String pagenameParam) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		String text = m_engine.getPureText(pagename, -1);

		return toRPCBase64(text);
	}

	public byte[] getPageVersion(String pagenameParam, int version) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		return toRPCBase64(m_engine.getPureText(pagename, version));
	}

	public byte[] getPageHTML(String pagenameParam) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		return toRPCBase64(m_engine.getHTML(pagename));
	}

	public byte[] getPageHTMLVersion(String pagenameParam, int version) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		return toRPCBase64(m_engine.getHTML(pagename, version));
	}

	@SuppressWarnings("unchecked")
    public Vector listLinks(String pagenameParam) throws XmlRpcException {
		String pagename = parsePageCheckCondition(pagenameParam);

		WikiPage page = m_engine.getPage(pagename);
		String pagedata = m_engine.getPureText(page);

		LinkCollector localCollector = new LinkCollector();
		LinkCollector extCollector = new LinkCollector();
		LinkCollector attCollector = new LinkCollector();

		WikiContext context = new WikiContext(m_engine, page);
		context.setVariable(WikiEngine.PROP_REFSTYLE, "absolute");

		m_engine.textToHTML(context,
		        pagedata,
		        localCollector,
		        extCollector,
		        attCollector);

		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

		//
		// Add local links.
		//
		for(Iterator i = localCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.put("page", toRPCString(link));
			ht.put("type", LINK_LOCAL);

			//
			// FIXME: This is a kludge. The link format should really be queried
			// from the TranslatorReader itself. Also, the link format should
			// probably
			// have information on whether the page exists or not.
			//

			//
			// FIXME: The current link collector interface is not very good,
			// since
			// it causes this.
			//

			if(m_engine.pageExists(link)) {
				ht.put("href", context.getURL(WikiContext.VIEW, link));
			} else {
				ht.put("href", context.getURL(WikiContext.EDIT, link));
			}

			result.add(ht);
		}

		//
		// Add links to inline attachments
		//
		for(Iterator i = attCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();

			Hashtable<String, String> ht = new Hashtable<String, String>();

			ht.put("page", toRPCString(link));
			ht.put("type", LINK_LOCAL);
			ht.put("href", context.getURL(WikiContext.ATTACH, link));

			result.add(ht);
		}

		//
		// External links don't need to be changed into XML-RPC strings,
		// simply because URLs are by definition ASCII.
		//

		for(Iterator i = extCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();

			Hashtable<String, String> ht = new Hashtable<String, String>();

			ht.put("page", link);
			ht.put("type", LINK_EXTERNAL);
			ht.put("href", link);

			result.add(ht);
		}

		return result;
	}
}
