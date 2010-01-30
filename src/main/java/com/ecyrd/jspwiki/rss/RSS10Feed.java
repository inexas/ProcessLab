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
package com.ecyrd.jspwiki.rss;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import org.apache.ecs.xml.XML;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;

/**
 * Provides an implementation of an RSS 1.0 feed. In addition, this class is
 * capable of adding RSS 1.0 Wiki Extensions to the Feed, as defined in <A
 * HREF="http://usemod.com/cgi-bin/mb.pl?ModWiki">UseMod:ModWiki</A>.
 */
@SuppressWarnings("unchecked")
public class RSS10Feed extends Feed {
	/**
	 * Create an RSS 1.0 feed for a given context.
	 * 
	 * @param context
	 *            {@inheritDoc}
	 */
	public RSS10Feed(WikiContext context) {
		super(context);
	}

	private XML getRDFItems() {
		XML items = new XML("items");

		XML rdfseq = new XML("rdf:Seq");

		for(Iterator i = m_entries.iterator(); i.hasNext();) {
			Entry e = (Entry)i.next();

			String url = e.getURL();

			rdfseq.addElement(new XML("rdf:li").addAttribute("rdf:resource", url));
		}

		items.addElement(rdfseq);

		return items;
	}

	private void addItemList(XML root) {
		SimpleDateFormat iso8601fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		WikiEngine engine = m_wikiContext.getEngine();

		for(Iterator i = m_entries.iterator(); i.hasNext();) {
			Entry e = (Entry)i.next();

			String url = e.getURL();

			XML item = new XML("item");
			item.addAttribute("rdf:about", url);

			item.addElement(new XML("title").addElement(format(e.getTitle())));

			item.addElement(new XML("link").addElement(url));

			XML content = new XML("description");

			content.addElement(format(e.getContent()));

			item.addElement(content);

			WikiPage p = e.getPage();

			if(p.getVersion() != -1) {
				item.addElement(new XML("wiki:version").addElement(Integer.toString(p.getVersion())));
			}

			if(p.getVersion() > 1) {
				item.addElement(new XML("wiki:diff").addElement(engine.getURL(WikiContext.DIFF,
				        p.getName(),
				        "r1=-1",
				        true)));
			}

			//
			// Modification date.
			//
			Calendar cal = Calendar.getInstance();
			cal.setTime(p.getLastModified());
			cal.add(Calendar.MILLISECOND,
			        -(cal.get(Calendar.ZONE_OFFSET) +
			        (cal.getTimeZone().inDaylightTime(p.getLastModified()) ? cal.get(Calendar.DST_OFFSET) : 0)));

			item.addElement(new XML("dc:date").addElement(iso8601fmt.format(cal.getTime())));

			//
			// Author
			String author = e.getAuthor();
			if(author == null)
				author = "unknown";

			XML contributor = new XML("dc:creator");

			item.addElement(contributor);

			/*
			 * XML description = new XML("rdf:Description"); if(
			 * m_wikiContext.getEngine().pageExists(author) ) {
			 * description.addAttribute( "link", engine.getURL(
			 * WikiContext.VIEW, author, null, true ) ); }
			 * 
			 * description.addElement( new XML("value").addElement(
			 * format(author) ) ); contributor.addElement( description );
			 */

			// Not too many aggregators seem to like this. Therefore we're
			// just adding the name here.

			contributor.addElement(format(author));

			// PageHistory

			item.addElement(new XML("wiki:history").addElement(engine.getURL(WikiContext.INFO,
			        p.getName(),
			        null,
			        true)));

			//
			// Add to root
			//

			root.addElement(item);
		}
	}

	private XML getChannelElement() {
		XML channel = new XML("channel");

		channel.addAttribute("rdf:about", m_feedURL);

		channel.addElement(new XML("link").addElement(m_feedURL));

		if(m_channelTitle != null)
			channel.addElement(new XML("title").addElement(format(m_channelTitle)));

		if(m_channelDescription != null)
			channel.addElement(new XML("description").addElement(format(m_channelDescription)));

		if(m_channelLanguage != null)
			channel.addElement(new XML("dc:language").addElement(m_channelLanguage));

		channel.setPrettyPrint(true);

		channel.addElement(getRDFItems());

		return channel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString() {
		XML root = new XML("rdf:RDF");

		root.addAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		root.addAttribute("xmlns", "http://purl.org/rss/1.0/");
		root.addAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		root.addAttribute("xmlns:wiki", "http://purl.org/rss/1.0/modules/wiki/");

		root.addElement(getChannelElement());

		addItemList(root);

		root.setPrettyPrint(true);

		return root.toString();
	}

}
