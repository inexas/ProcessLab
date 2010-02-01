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
package com.ecyrd.jspwiki.plugin;

import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.parser.Heading;
import com.ecyrd.jspwiki.parser.HeadingListener;
import com.ecyrd.jspwiki.parser.JSPWikiMarkupParser;
import java.util.*;
import java.io.StringReader;
import java.io.IOException;

/**
 * Provides a table of contents.
 * <p>
 * Parameters :
 * </p>
 * <ul>
 * <li><b>title</b> - The title of the table of contents.</li>
 * <li><b>numbered</b> - if true, generates automatically numbers for the
 * headings.</li>
 * <li><b>start</b> - If using a numbered list, sets the start number.</li>
 * <li><b>prefix</b> - If using a numbered list, sets the prefix used for the
 * list.</li>
 * </ul>
 * 
 * @since 2.2
 */
@SuppressWarnings("unchecked")
public class TableOfContents
        implements WikiPlugin, HeadingListener {
	private static Logger log = Logger.getLogger(TableOfContents.class);

	/** Parameter name for setting the title. */
	public static final String PARAM_TITLE = "title";

	/** Parameter name for setting whether the headings should be numbered. */
	public static final String PARAM_NUMBERED = "numbered";

	/** Parameter name for setting where the numbering should start. */
	public static final String PARAM_START = "start";

	/** Parameter name for setting what the prefix for the heading is. */
	public static final String PARAM_PREFIX = "prefix";

	private static final String VAR_ALREADY_PROCESSING = "__TableOfContents.processing";

	StringBuffer m_buf = new StringBuffer();
	private boolean m_usingNumberedList = false;
	private String m_prefix = "";
	private int m_starting = 0;
	private int m_level1Index = 0;
	private int m_level2Index = 0;
	private int m_level3Index = 0;
	private int m_lastLevel = 0;

	/**
	 * {@inheritDoc}
	 */
	public void headingAdded(WikiContext context, Heading hd) {
		log.debug("HD: " + hd.m_level + ", " + hd.m_titleText + ", " + hd.m_titleAnchor);

		switch (hd.m_level){
		case Heading.HEADING_SMALL:
			m_buf.append("<li class=\"toclevel-3\">");
			m_level3Index++;
			break;
		case Heading.HEADING_MEDIUM:
			m_buf.append("<li class=\"toclevel-2\">");
			m_level2Index++;
			break;
		case Heading.HEADING_LARGE:
			m_buf.append("<li class=\"toclevel-1\">");
			m_level1Index++;
			break;
		default:
			throw new InternalWikiException("Unknown depth in toc! (Please submit a bug report.)");
		}

		if(m_level1Index < m_starting) {
			// in case we never had a large heading ...
			m_level1Index++;
		}
		if((m_lastLevel == Heading.HEADING_SMALL) && (hd.m_level != Heading.HEADING_SMALL)) {
			m_level3Index = 0;
		}
		if(((m_lastLevel == Heading.HEADING_SMALL) || (m_lastLevel == Heading.HEADING_MEDIUM)) &&
		        (hd.m_level == Heading.HEADING_LARGE)) {
			m_level3Index = 0;
			m_level2Index = 0;
		}

		String titleSection = hd.m_titleSection.replace('%', '_');
		String pageName = context.getEngine().encodeName(context.getPage().getName()).replace('%', '_');

		String url = context.getURL(WikiContext.VIEW, context.getPage().getName());
		String sectref = "#section-" + pageName + "-" + titleSection;

		m_buf.append("<a class=\"wikipage\" href=\"" + url + sectref + "\">");
		if(m_usingNumberedList) {
			switch (hd.m_level){
			case Heading.HEADING_SMALL:
				m_buf.append(m_prefix + m_level1Index + "." + m_level2Index + "." + m_level3Index + " ");
				break;
			case Heading.HEADING_MEDIUM:
				m_buf.append(m_prefix + m_level1Index + "." + m_level2Index + " ");
				break;
			case Heading.HEADING_LARGE:
				m_buf.append(m_prefix + m_level1Index + " ");
				break;
			default:
				throw new InternalWikiException("Unknown depth in toc! (Please submit a bug report.)");
			}
		}
		m_buf.append(TextUtil.replaceEntities(hd.m_titleText) + "</a></li>\n");

		m_lastLevel = hd.m_level;
	}

	/**
	 * {@inheritDoc}
	 */
	public String execute(WikiContext context, Map params)
	        throws PluginException {
		WikiEngine engine = context.getEngine();
		WikiPage page = context.getPage();
		ResourceBundle rb = context.getBundle(WikiPlugin.CORE_PLUGINS_RESOURCEBUNDLE);

		if(context.getVariable(VAR_ALREADY_PROCESSING) != null)
			// return rb.getString("tableofcontents.title");
			return "<a href=\"#section-TOC\" class=\"toc\">" + rb.getString("tableofcontents.title") + "</a>";

		StringBuffer sb = new StringBuffer();

		sb.append("<div class=\"toc\">\n");
		sb.append("<div class=\"collapsebox\">\n");

		String title = (String)params.get(PARAM_TITLE);
		sb.append("<h4 id=\"section-TOC\">");
		if(title != null) {
			// sb.append("<h4>"+TextUtil.replaceEntities(title)+"</h4>\n");
			sb.append(TextUtil.replaceEntities(title));
		} else {
			// sb.append("<h4>"+rb.getString("tableofcontents.title")+"</h4>\n");
			sb.append(rb.getString("tableofcontents.title"));
		}
		sb.append("</h4>\n");

		// should we use an ordered list?
		m_usingNumberedList = false;
		if(params.containsKey(PARAM_NUMBERED)) {
			String numbered = (String)params.get(PARAM_NUMBERED);
			if(numbered.equalsIgnoreCase("true")) {
				m_usingNumberedList = true;
			} else if(numbered.equalsIgnoreCase("yes")) {
				m_usingNumberedList = true;
			}
		}

		// if we are using a numbered list, get the rest of the parameters (if
		// any) ...
		if(m_usingNumberedList) {
			int start = 0;
			String startStr = (String)params.get(PARAM_START);
			if((startStr != null) && (startStr.matches("^\\d+$"))) {
				start = Integer.parseInt(startStr);
			}
			if(start < 0)
				start = 0;

			m_starting = start;
			m_level1Index = start - 1;
			if(m_level1Index < 0)
				m_level1Index = 0;
			m_level2Index = 0;
			m_level3Index = 0;
			m_prefix = (String)params.get(PARAM_PREFIX);
			if(m_prefix == null)
				m_prefix = "";
			m_lastLevel = Heading.HEADING_LARGE;
		}

		try {
			String wikiText = engine.getPureText(page);
			boolean runFilters =
			        "true".equals(engine.getVariableManager().getValue(context, WikiEngine.PROP_RUNFILTERS, "true"));

			try {
				if(runFilters)
					wikiText = engine.getFilterManager().doPreTranslateFiltering(context, wikiText);
			} catch(Exception e) {
				log.error("Could not construct table of contents: Filter Error", e);
				throw new PluginException("Unable to construct table of contents (see logs)");
			}

			context.setVariable(VAR_ALREADY_PROCESSING, "x");
			JSPWikiMarkupParser parser = new JSPWikiMarkupParser(context,
			        new StringReader(wikiText));
			parser.addHeadingListener(this);

			parser.parse();

			sb.append("<ul>\n" + m_buf.toString() + "</ul>\n");
		} catch(IOException e) {
			log.error("Could not construct table of contents", e);
			throw new PluginException("Unable to construct table of contents (see logs)");
		}

		sb.append("</div>\n</div>\n");

		return sb.toString();
	}

}
