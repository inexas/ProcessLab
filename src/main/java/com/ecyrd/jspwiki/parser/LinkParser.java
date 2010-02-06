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

package com.ecyrd.jspwiki.parser;

import java.util.*;
import org.apache.log4j.Logger;
import org.jdom.Attribute;

/**
 * Parses JSPWiki-style "augmented" link markup into a Link object containing
 * the link text, link reference, and any optional link attributes (as JDOM
 * Attributes).
 * <p>
 * The parser recognizes three link forms:
 * </p>
 * <ol>
 * <li><tt> [Text] </tt></li>
 * <li><tt> [Text | Link] </tt></li>
 * <li><tt> [Text | Link | attributes] </tt></li>
 * </ol>
 * <p>
 * where the attributes are space-delimited, each in the form of
 * </p>
 * 
 * <pre>
 *      name1='value1' name2='value2' name3='value3' (etc.)
 * </pre>
 * <p>
 * If the attribute parsing fails, the parser will still return the basic link,
 * writing a warning to the log.
 * </p>
 * 
 * <h3>Permitted Attributes</h3>
 * <p>
 * Attributes that aren't declared on <tt>&lt;a&gt;</tt> or those that permit
 * scripting in HTML (as this is a security risk) are ignored and have no effect
 * on parsing, nor show up in the resulting attribute list). The 'href' and
 * 'name' attributes are also ignored as spurious. The permitted list is:
 * 'accesskey', 'charset', 'class', 'hreflang', 'id', 'lang', 'dir', 'rel',
 * 'rev', 'style' , 'tabindex', 'target' , 'title', and 'type'. The declared
 * attributes that will be ignored are: 'href', 'name', 'shape', 'coords',
 * 'onfocus', 'onblur', or any of the other 'on*' event attributes.
 * </p>
 * <p>
 * The permitted attributes and target attribute values are static String arrays
 * ({@link #PERMITTED_ATTRIBUTES} and {@link #PERMITTED_TARGET_VALUES} resp.)
 * that could be compile-time modified (i.e., predeclared).
 * </p>
 * 
 * <h3>Permitted Values on Target Attribute</h3>
 * <p>
 * The following target names are reserved in HTML 4 and have special meanings.
 * These are the only values permitted by the parser.
 * <dl>
 * <dt><b>_blank</b></dt>
 * <dd>The user agent should load the designated document in a new, unnamed
 * window.</dd>
 * <dt><b>_self</b></dt>
 * <dd>The user agent should load the document in the same frame as the element
 * that refers to this target.</dd>
 * <dt><b>_parent</b></dt>
 * <dd>The user agent should load the document into the immediate FRAMESET
 * parent of the current frame. This value is equivalent to _self if the current
 * frame has no parent.</dd>
 * <dt><b>_top</b></dt>
 * <dd>The user agent should load the document into the full, original window
 * (thus canceling all other frames). This value is equivalent to _self if the
 * current frame has no parent.</dd>
 * </dl>
 * 
 * <h3>Returned Value</h3>
 * <p>
 * This returns a <b>Link</b> object, a public inner class with methods:
 * <ul>
 * <li> <tt>getText()</tt> returns the link text.</li>
 * <li> <tt>getReference()</tt> returns the link reference value.</li>
 * <li> <tt>attributeCount()</tt> returns the number of declared attributes.</li>
 * <li> <tt>getAttributes()</tt> returns an iterator over any validated
 * XHTML-compliant attributes, returned as JDOM Attributes.</li>
 * </ul>
 * <p>
 * The <tt>attributeCount()</tt> method can be used to circumvent calling
 * <tt>getAttributes()</tt>, which will create an empty Iterator rather than
 * return a null.
 * </p>
 * 
 * <h3>Example: Link Form 1</h3>
 * <p>
 * From an incoming wikitext link of:
 * 
 * <pre>
 *     [Acme]
 * </pre>
 * 
 * returns:
 * 
 * <pre>
 *    getText():         "Acme"
 *    getReference():    "Acme"
 *    attributeCount():  0
 *    getAttributes():   an empty Iterator
 * </pre>
 * 
 * <h3>Example: Link Form 2</h3>
 * <p>
 * From an incoming wikitext link of:
 * 
 * <pre>
 *     [Acme | http://www.acme.com/]
 * </pre>
 * 
 * returns:
 * 
 * <pre>
 *    getText():         "Acme"
 *    getReference():    "http://www.acme.com/"
 *    attributeCount():  0
 *    getAttributes():   an empty Iterator
 * </pre>
 * 
 * <h3>Example: Link Form 3</h3>
 * <p>
 * From an incoming wikitext link of:
 * </p>
 * 
 * <pre>
 *    [Acme | http://www.acme.com/ | id='foo' rel='Next']
 * </pre>
 * 
 * returns:
 * 
 * <pre>
 *    getText():         "Acme"
 *    getReference():    "http://www.acme.com/"
 *    attributeCount():  2
 *    getAttributes():   an Iterator containing:
 *      JDOM Attribute:  id="foo"
 *      JDOM Attribute:  rel="Next"
 * </pre>
 * 
 * 
 * @author Murray Altheim
 * @since 2.5.10
 */
@SuppressWarnings("unchecked")
public class LinkParser {
	private static Logger log = Logger.getLogger(LinkParser.class);

	/** Permitted attributes on links. Keep this sorted. */
	private static final String[] PERMITTED_ATTRIBUTES = new String[] {
	        "accesskey", "charset", "class", "dir", "hreflang", "id", "lang",
	        "rel", "rev", "style", "tabindex", "target", "title", "type" };

	/** Permitted values on the 'target' attribute. */
	private static final String[] PERMITTED_TARGET_VALUES = new String[] {
	        "_blank", "_self", "_parent", "_top" };

	private static final String EQSQUO = "='";
	private static final String SQUO = "'";
	private static final String EQ = "=";
	private static final String TARGET = "target";
	private static final String DELIMS = " \t\n\r\f=";

	private static final List m_EMPTY = new ArrayList();

	// ............

	/**
	 * Processes incoming link text, separating out the link text, the link URI,
	 * and then any specified attributes.
	 * 
	 * @param linktext
	 *            the wiki link text to be parsed
	 * @return a Link object containing the link text, reference, and any valid
	 *         Attributes
	 * @throws ParseException
	 *             if the parameter is null
	 */
	public Link parse(String linktext) throws ParseException {
		if(linktext == null) {
			throw new ParseException("null value passed to link parser");
		}

		Link link = null;

		try {
			// establish link text and link ref
			int cut1 = linktext.indexOf('|');
			if(cut1 == -1) {
				// link form 1: [Acme]
				return new Link(linktext);
			}

			int cut2 = cut1 + 1 < linktext.length()
			        ? linktext.indexOf('|', cut1 + 1)
			        : -1;

			if(cut2 == -1) {
				// link form 2: [Acme | http://www.acme.com/]
				String text = linktext.substring(0, cut1).trim(); // to cut1
				String ref = linktext.substring(cut1 + 1).trim(); // cut1 to end
				return new Link(text, ref);
			}

			// otherwise: link form 3: [Acme | http://www.acme.com/ | id='foo'
			// rel='Next']
			String text = linktext.substring(0, cut1).trim(); // to cut1
			String ref = linktext.substring(cut1 + 1, cut2).trim(); // cut1 to
																	// cut2
			String attribs = linktext.substring(cut2 + 1).trim(); // cut2 to end

			link = new Link(text, ref);

			// parse attributes
			if(attribs.indexOf(EQSQUO) != -1) // contains "='" that looks like
											  // attrib spec
			{
				try {
					StringTokenizer tok = new StringTokenizer(attribs, DELIMS, true);
					while(tok.hasMoreTokens()) {
						String token = tok.nextToken(DELIMS).trim(); // get
																	 // attribute
																	 // name
																	 // token
						while(isSpace(token) && tok.hasMoreTokens()) {
							token = tok.nextToken(DELIMS).trim(); // eat any WS
						}

						require(tok, EQ); // eat '=', break after '='
						require(tok, SQUO); // eat opening delim
						String value = tok.nextToken(SQUO); // using existing
															// delim
						require(tok, SQUO); // eat closing delim

						if(token != null && value != null) {
							if(Arrays.binarySearch(PERMITTED_ATTRIBUTES, token) >= 0) {
								if(!token.equals(TARGET) // _blank _self _parent
														 // _top
								        || Arrays.binarySearch(PERMITTED_TARGET_VALUES, value) >= 0) {
									Attribute a = new Attribute(token, value);
									link.addAttribute(a);
								} else {
									throw new ParseException("unknown target attribute value='"
									        + value + "' on link");
								}
							} else {
								throw new ParseException("unknown attribute name '"
								        + token + "' on link");
							}
						} else {
							throw new ParseException("unable to parse link attributes '"
							        + attribs + "'");

						}
					}
				} catch(ParseException pe) {
					log.warn("syntax error parsing link attributes '" + attribs + "': " + pe.getMessage());
				} catch(NoSuchElementException nse) {
					log.warn("expected more tokens while parsing link attributes '" + attribs + "'");
				}
			}

		} catch(Exception e) {
			log.warn(e.getClass().getName() + " thrown by link parser: " + e.getMessage());
		}

		return link;
	}

	private String require(StringTokenizer tok, String required)
	        throws ParseException, NoSuchElementException {
		String s = tok.nextToken(required);
		if(!s.equals(required)) {
			throw new ParseException("expected '" + required + "' not '" + s + "'"); // I18N
		}
		return s;
	}

	/**
	 * Returns true if the String <tt>s</tt> is completely composed of
	 * whitespace.
	 * 
	 * @param s
	 *            The string to check
	 * @return True, if "s" is all XML whitespace.
	 */
	public static final boolean isSpace(String s) {
		for(int i = 0; i < s.length(); i++) {
			if(!isSpace(s.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if char <tt>c</tt> is a member of <tt>S</tt> (space) [XML
	 * 1.1 production 3].
	 * 
	 * @param c
	 *            Character to check.
	 * @return True, if the character is an XML space.
	 */
	public static final boolean isSpace(char c) {
		return 0x20 == c // SPACE
		        || 0x0A == c // LF
		        || 0x0D == c // CR
		        || 0x09 == c // TAB
		        || 0x85 == c // NEL
		        || 0x2028 == c; // LS (line separator)
	}

	// .........................................................................

	/**
	 * Inner class serving as a struct containing the parsed components of a
	 * link.
	 */
	public static class Link {
		private String m_text;
		private String m_ref = null;
		private int m_interwikiPoint = -1;
		private List<Attribute> m_attribs = null;

		/**
		 * Create a new Link with text but no reference.
		 * 
		 * @param text
		 *            The link text.
		 * @throws ParseException
		 *             If the link text is illegal.
		 */
		protected Link(String text) throws ParseException {
			setText(text);
		}

		/**
		 * Create a new link with a given text and hyperlink (reference).
		 * 
		 * @param text
		 *            The link text.
		 * @param ref
		 *            The hypertext reference.
		 * @throws ParseException
		 *             If the link text or reference are illegal.
		 */
		protected Link(String text, String ref) throws ParseException {
			setText(text);
			setReference(ref);
		}

		/**
		 * Sets the link text.
		 * 
		 * @param text
		 *            The link text.
		 * @throws ParseException
		 *             If the text is illegal (e.g. null).
		 */
		protected void setText(String text) throws ParseException {
			if(text == null) {
				throw new ParseException("null link text");
			}
			m_text = text;
		}

		/**
		 * Returns the link text.
		 * 
		 * @return Link text.
		 */
		public String getText() {
			return m_text;
		}

		/**
		 * Sets the hypertext reference. Typically, this is an URI or an
		 * interwiki link, or a wikilink.
		 * 
		 * @param ref
		 *            The reference.
		 * @throws ParseException
		 *             If the reference is illegal.
		 */
		protected void setReference(String ref) throws ParseException {
			if(ref == null) {
				throw new ParseException("null link reference value");
			}
			m_ref = ref;
		}

		/**
		 * Returns true, if there is a reference.
		 * 
		 * @return True, if there's a reference; false otherwise.
		 */
		public boolean hasReference() {
			return m_ref != null;
		}

		/**
		 * Returns the link reference, or the link text if null.
		 * 
		 * @return A link reference.
		 */
		public String getReference() {
			return m_ref != null
			        ? m_ref
			        : m_text;
		}

		/**
		 * Returns true, if this Link represents an InterWiki link (of the form
		 * wiki:page).
		 * 
		 * @return True, if this Link represents an InterWiki link.
		 */
		public boolean isInterwikiLink() {
			if(!hasReference())
				m_ref = m_text;

			m_interwikiPoint = m_ref.indexOf(':');

			return m_interwikiPoint != -1;
		}

		/**
		 * Returns the name of the wiki if this is an interwiki link.
		 * 
		 * <pre>
		 *    Link link = new Link("Foo","Wikipedia:Foobar");
		 *    assert( link.getExternalWikiPage(), "Wikipedia" );
		 * </pre>
		 * 
		 * @return Name of the wiki, or null, if this is not an interwiki link.
		 */
		public String getExternalWiki() {
			if(isInterwikiLink()) {
				return m_ref.substring(0, m_interwikiPoint);
			}

			return null;
		}

		/**
		 * Returns the wikiname part of an interwiki link. Used only with
		 * interwiki links.
		 * 
		 * <pre>
		 *    Link link = new Link("Foo","Wikipedia:Foobar");
		 *    assert( link.getExternalWikiPage(), "Foobar" );
		 * </pre>
		 * 
		 * @return Wikiname part, or null, if this is not an interwiki link.
		 */
		public String getExternalWikiPage() {
			if(isInterwikiLink()) {
				return m_ref.substring(m_interwikiPoint + 1);
			}

			return null;
		}

		/**
		 * Returns the number of attributes on this link.
		 * 
		 * @return The number of attributes.
		 */
		public int attributeCount() {
			return m_attribs != null
			        ? m_attribs.size()
			        : 0;
		}

		/**
		 * Adds another attribute to the link.
		 * 
		 * @param attr
		 *            A JDOM Attribute.
		 */
		public void addAttribute(Attribute attr) {
			if(m_attribs == null) {
				m_attribs = new ArrayList<Attribute>();
			}
			m_attribs.add(attr);
		}

		/**
		 * Returns an Iterator over the list of JDOM Attributes.
		 * 
		 * @return Iterator over the attributes.
		 */
		public Iterator getAttributes() {
			return m_attribs != null
			        ? m_attribs.iterator()
			        : m_EMPTY.iterator();
		}

		/**
		 * Returns a wikitext string representation of this Link.
		 * 
		 * @return WikiText.
		 */
		@Override
        public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append('[');
			sb.append(m_text);

			if(m_ref != null) {
				sb.append(' ');
				sb.append('|');
				sb.append(' ');
				sb.append(m_ref);
			}

			if(m_attribs != null) {
				sb.append(' ');
				sb.append('|');
				Iterator it = getAttributes();
				while(it.hasNext()) {
					Attribute a = (Attribute)it.next();
					sb.append(' ');
					sb.append(a.getName());
					sb.append('=');
					sb.append('\'');
					sb.append(a.getValue());
					sb.append('\'');
				}
			}
			sb.append(']');
			return sb.toString();
		}

	} // end inner class

}