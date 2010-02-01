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
package com.ecyrd.jspwiki;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import com.ecyrd.jspwiki.parser.HeadingListener;
import com.ecyrd.jspwiki.parser.JSPWikiMarkupParser;
import com.ecyrd.jspwiki.parser.MarkupParser;
import com.ecyrd.jspwiki.render.WikiRenderer;
import com.ecyrd.jspwiki.render.XHTMLRenderer;

/**
 * Handles conversion from Wiki format into fully featured HTML. This is where
 * all the magic happens. It is CRITICAL that this class is tested, or all Wikis
 * might die horribly.
 * <P>
 * The output of the HTML has not yet been validated against the HTML DTD.
 * However, it is very simple.
 * <p>
 * This class is officially deprecated in 2.3, and will be replaced with a dummy
 * class later on. Please see MarkupParser.
 * 
 * @deprecated
 */
// FIXME3.0: Remove
@SuppressWarnings("unchecked")
public class TranslatorReader extends Reader {
	/**
	 * This property defines the inline image pattern. It's current value is
	 * jspwiki.translatorReader.inlinePattern
	 */
	public static final String PROP_INLINEIMAGEPTRN = "jspwiki.translatorReader.inlinePattern";

	/** If true, consider CamelCase hyperlinks as well. */
	public static final String PROP_CAMELCASELINKS = "jspwiki.translatorReader.camelCaseLinks";

	/**
	 * If true, all hyperlinks are translated as well, regardless whether they
	 * are surrounded by brackets.
	 */
	public static final String PROP_PLAINURIS = "jspwiki.translatorReader.plainUris";

	/**
	 * If true, all outward links (external links) have a small link image
	 * appended.
	 */
	public static final String PROP_USEOUTLINKIMAGE = "jspwiki.translatorReader.useOutlinkImage";

	/**
	 * If set to "true", allows using raw HTML within Wiki text. Be warned, this
	 * is a VERY dangerous option to set - never turn this on in a publicly
	 * allowable Wiki, unless you are absolutely certain of what you're doing.
	 */
	public static final String PROP_ALLOWHTML = "jspwiki.translatorReader.allowHTML";

	/** If set to "true", all external links are tagged with 'rel="nofollow"' */
	public static final String PROP_USERELNOFOLLOW = "jspwiki.translatorReader.useRelNofollow";

	/** If set to "true", enables plugins during parsing */
	public static final String PROP_RUNPLUGINS = "jspwiki.translatorReader.runPlugins";
	/**
	 * The default inlining pattern. Currently "*.png"
	 */
	public static final String DEFAULT_INLINEPATTERN = "*.png";

	private JSPWikiMarkupParser m_parser;
	private WikiContext m_context;

	/**
	 * Creates a TranslatorReader using the default HTML renderer.
	 * 
	 * @param context
	 *            WikiContext
	 * @param in
	 *            The reader from which to read.
	 */
	public TranslatorReader(WikiContext context, Reader in) {
		initialize(context, in);
	}

	/**
	 * Creates a TranslatorReader.
	 * 
	 * @param context
	 *            WikiContext
	 * @param in
	 *            Reader
	 * @param renderer
	 *            Unused.
	 */
	public TranslatorReader(WikiContext context, Reader in, @SuppressWarnings("unused") Object renderer) {
		initialize(context, in);
	}

	/**
	 * Replaces the current input character stream with a new one.
	 * 
	 * @param in
	 *            New source for input. If null, this method does nothing.
	 * @return the old stream
	 */
	public Reader setInputReader(Reader in) {
		return m_parser.setInputReader(in);
	}

	/**
	 * @param m_engine
	 *            The WikiEngine this reader is attached to. Is used to figure
	 *            out of a page exits.
	 */

	// FIXME: TranslatorReaders should be pooled for better performance.
	private void initialize(WikiContext context,
	        Reader in) {
		m_context = context;
		m_parser = new JSPWikiMarkupParser(context, in);
	}

	/**
	 * Does not work, don't try to use it.
	 * 
	 * @param renderer
	 *            Renderer.
	 */
	protected void setRenderer(@SuppressWarnings("unused") Object renderer) {
		throw new InternalWikiException("No longer functional - please use JSPWikiMarkupParser");
	}

	/**
	 * Adds a hook for processing link texts. This hook is called when the link
	 * text is written into the output stream, and you may use it to modify the
	 * text. It does not affect the actual link, only the user-visible text.
	 * 
	 * @param mutator
	 *            The hook to call. Null is safe.
	 */
	public void addLinkTransmutator(StringTransmutator mutator) {
		m_parser.addLinkTransmutator(mutator);
	}

	/**
	 * Adds a hook for processing local links. The engine transforms both
	 * non-existing and existing page links.
	 * 
	 * @param mutator
	 *            The hook to call. Null is safe.
	 */
	public void addLocalLinkHook(StringTransmutator mutator) {
		m_parser.addLocalLinkHook(mutator);
	}

	/**
	 * Adds a hook for processing external links. This includes all http://
	 * ftp://, etc. links, including inlined images.
	 * 
	 * @param mutator
	 *            The hook to call. Null is safe.
	 */
	public void addExternalLinkHook(StringTransmutator mutator) {
		m_parser.addExternalLinkHook(mutator);
	}

	/**
	 * Adds a hook for processing attachment links.
	 * 
	 * @param mutator
	 *            The hook to call. Null is safe.
	 */
	public void addAttachmentLinkHook(StringTransmutator mutator) {
		m_parser.addAttachmentLinkHook(mutator);
	}

	/**
	 * Adds a listener to headings.
	 * 
	 * @param listener
	 *            Listener to add.
	 */
	public void addHeadingListener(HeadingListener listener) {
		m_parser.addHeadingListener(listener);
	}

	/** Disables access rules parsing. */
	public void disableAccessRules() {
		m_parser.disableAccessRules();
	}

	/**
	 * Can be used to turn on plugin execution on a translator-reader basis
	 * 
	 * @param toggle
	 *            on or off
	 */
	public void enablePlugins(@SuppressWarnings("unused") boolean toggle) {
		throw new InternalWikiException("No longer supported.");
	}

	/**
	 * Use this to turn on or off image inlining.
	 * 
	 * @param toggle
	 *            If true, images are inlined (as per set in jspwiki.properties)
	 *            If false, then images won't be inlined; instead, they will be
	 *            treated as standard hyperlinks.
	 * @since 2.2.9
	 */
	public void enableImageInlining(boolean toggle) {
		m_parser.enableImageInlining(toggle);
	}

	/**
	 * Figure out which image suffixes should be inlined.
	 * 
	 * @return Collection of Strings with patterns.
	 * @param engine
	 *            WikiEngine
	 */

	protected static Collection getImagePatterns(@SuppressWarnings("unused") WikiEngine engine) {
		throw new InternalWikiException("No longer supported - please use JSPWikiMarkupParser");
	}

	/**
	 * Write a HTMLized link depending on its type. The link mutator chain is
	 * processed.
	 * 
	 * @param type
	 *            Type of the link.
	 * @param link
	 *            The actual link.
	 * @param text
	 *            The user-visible text for the link.
	 * @return link
	 */
	public String makeLink(@SuppressWarnings("unused") int type, @SuppressWarnings("unused") String link, @SuppressWarnings("unused") String text) {
		return null;
		/*
		 * if( text == null ) text = link;
		 * 
		 * text = callMutatorChain( m_linkMutators, text );
		 * 
		 * return m_renderer.makeLink( type, link, text );
		 */
	}

	/**
	 * Cleans a Wiki name.
	 * <P>
	 * [ This is a link ] -&gt; ThisIsALink
	 * 
	 * @param link
	 *            Link to be cleared. Null is safe, and causes this to return
	 *            null.
	 * @return A cleaned link.
	 * 
	 * @since 2.0
	 */
	public static String cleanLink(String link) {
		return MarkupParser.cleanLink(link);
	}

	private StringReader m_data;

	/** {@inheritDoc} */
	@Override
    public int read()
	        throws IOException {
		if(m_data == null) {
			WikiRenderer r = new XHTMLRenderer(m_context, m_parser.parse());

			String s = r.getString();

			m_data = new StringReader(s);
		}

		return m_data.read();
	}

	/** {@inheritDoc} */
	public int read(char[] buf, int off, int len)
	        throws IOException {
		return m_data.read(buf, off, len);
	}

	/** {@inheritDoc} */
	public boolean ready()
	        throws IOException {
		return m_data.ready();
	}

	/** {@inheritDoc} */
	public void close() {
		//
	}

}
