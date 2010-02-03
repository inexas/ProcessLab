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
package com.ecyrd.jspwiki.providers;

import java.io.*;
import java.util.Properties;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.*;

/**
 * Provides a simple directory based repository for Wiki pages.
 * <P>
 * All files have ".txt" appended to make life easier for those who insist on
 * using Windows or other software which makes assumptions on the files contents
 * based on its name.
 * <p>
 * This class functions as a superclass to all file based providers.
 * 
 * @since 2.1.21.
 * 
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFileProvider
        implements WikiPageProvider {
	private static final Logger log = Logger.getLogger(AbstractFileProvider.class);
	private String m_pageDirectory = "/tmp/";

	protected String m_encoding;

	protected WikiEngine m_engine;

	/**
	 * Name of the property that defines where page directories are.
	 */
	public static final String PROP_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";

	/**
	 * All files should have this extension to be recognized as JSPWiki files.
	 * We default to .txt, because that is probably easiest for Windows users,
	 * and guarantees correct handling.
	 */
	public static final String FILE_EXT = ".txt";

	/** The default encoding. */
	public static final String DEFAULT_ENCODING = "ISO-8859-1";

	private boolean m_windowsHackNeeded = false;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws FileNotFoundException
	 *             If the specified page directory does not exist.
	 * @throws IOException
	 *             In case the specified page directory is a file, not a
	 *             directory.
	 */
	public void initialize(WikiEngine engine, Properties properties)
	        throws NoRequiredPropertyException,
	        IOException, FileNotFoundException {
		log.debug("Initing FileSystemProvider");
		m_pageDirectory = WikiEngine.getRequiredProperty(properties, PROP_PAGEDIR);

		File f = new File(m_pageDirectory);

		if(!f.exists()) {
			if(!f.mkdirs()) {
				throw new IOException("Failed to create page directory " + f.getAbsolutePath()
				        + " , please check property "
				        + PROP_PAGEDIR);
			}
		} else {
			if(!f.isDirectory()) {
				throw new IOException("Page directory is not a directory: " + f.getAbsolutePath());
			}
			if(!f.canWrite()) {
				throw new IOException("Page directory is not writable: " + f.getAbsolutePath());
			}
		}

		m_engine = engine;

		m_encoding = properties.getProperty(WikiEngine.PROP_ENCODING,
		        DEFAULT_ENCODING);

		String os = System.getProperty("os.name").toLowerCase();

		if(os.startsWith("windows") || os.equals("nt")) {
			m_windowsHackNeeded = true;
		}

		log.info("Wikipages are read from '" + m_pageDirectory + "'");
	}

	String getPageDirectory() {
		return m_pageDirectory;
	}

	private static final String[] WINDOWS_DEVICE_NAMES =
	        {
	        "con", "prn", "nul", "aux", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9",
	        "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9"
	        };

	/**
	 * This makes sure that the queried page name is still readable by the file
	 * system. For example, all XML entities and slashes are encoded with the
	 * percent notation.
	 * 
	 * @param pagename
	 *            The name to mangle
	 * @return The mangled name.
	 */
	protected String mangleName(String pagenameParam) {
		String pagename = TextUtil.urlEncode(pagenameParam, m_encoding);

		pagename = TextUtil.replaceString(pagename, "/", "%2F");

		//
		// Names which start with a dot must be escaped to prevent problems.
		// Since we use URL encoding, this is invisible in our unescaping.
		//
		if(pagename.startsWith(".")) {
			pagename = "%2E" + pagename.substring(1);
		}

		if(m_windowsHackNeeded) {
			String pn = pagename.toLowerCase();
			for(int i = 0; i < WINDOWS_DEVICE_NAMES.length; i++) {
				if(WINDOWS_DEVICE_NAMES[i].equals(pn)) {
					pagename = "$$$" + pagename;
				}
			}
		}

		return pagename;
	}

	/**
	 * This makes the reverse of mangleName.
	 * 
	 * @param filename
	 *            The filename to unmangle
	 * @return The unmangled name.
	 */
	protected String unmangleName(String filenameParam) {
		// The exception should never happen.
		try {
			String filename;
			if(m_windowsHackNeeded && filenameParam.startsWith("$$$") && filenameParam.length() > 3) {
				filename = filenameParam.substring(3);
			} else {
				filename = filenameParam;
			}

			return TextUtil.urlDecode(filename, m_encoding);
		} catch(UnsupportedEncodingException e) {
			throw new InternalWikiException("Faulty encoding; should never happen");
		}
	}

	/**
	 * Finds a Wiki page from the page repository.
	 * 
	 * @param page
	 *            The name of the page.
	 * @return A File to the page. May be null.
	 */
	protected File findPage(String page) {
		return new File(m_pageDirectory, mangleName(page) + FILE_EXT);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean pageExists(String page) {
		File pagefile = findPage(page);

		return pagefile.exists();
	}

	/**
	 * This implementation just returns the current version, as filesystem does
	 * not provide versioning information for now.
	 * 
	 * @param page
	 *            {@inheritDoc}
	 * @param version
	 *            {@inheritDoc}
	 * @throws {@inheritDoc}
	 */
	public String getPageText(String page, int version)
	        throws ProviderException {
		return getPageText(page);
	}

	/**
	 * Read the text directly from the correct file.
	 */
	private String getPageText(String page) {
		String result = null;
		InputStream in = null;

		File pagedata = findPage(page);

		if(pagedata.exists()) {
			if(pagedata.canRead()) {
				try {
					in = new FileInputStream(pagedata);
					result = FileUtil.readContents(in, m_encoding);
				} catch(IOException e) {
					log.error("Failed to read", e);
				} finally {
					try {
						if(in != null)
							in.close();
					} catch(Exception e) {
						log.fatal("Closing failed", e);
					}
				}
			} else {
				log.warn("Failed to read page '" + page + "' from '" + pagedata.getAbsolutePath()
				        + "', possibly a permissions problem");
			}
		} else {
			// This is okay.
			log.info("New page '" + page + "'");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void putPageText(WikiPage page, String text)
	        throws ProviderException {
		File file = findPage(page.getName());
		PrintWriter out = null;

		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),
			        m_encoding));

			out.print(text);
		} catch(IOException e) {
			log.error("Saving failed");
		} finally {
			if(out != null)
				out.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAllPages()
	        throws ProviderException {
		log.debug("Getting all pages...");

		ArrayList<WikiPage> set = new ArrayList<WikiPage>();

		File wikipagedir = new File(m_pageDirectory);

		File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

		if(wikipages == null) {
			log.error("Wikipages directory '" + m_pageDirectory + "' does not exist! Please check " + PROP_PAGEDIR
			        + " in processlab.properties.");
			throw new InternalWikiException("Page directory does not exist");
		}

		for(int i = 0; i < wikipages.length; i++) {
			String wikiname = wikipages[i].getName();
			int cutpoint = wikiname.lastIndexOf(FILE_EXT);

			WikiPage page = getPageInfo(unmangleName(wikiname.substring(0, cutpoint)),
			        WikiPageProvider.LATEST_VERSION);
			if(page == null) {
				// This should not really happen.
				// FIXME: Should we throw an exception here?
				log.error("Page " + wikiname
				        + " was found in directory listing, but could not be located individually.");
				continue;
			}

			set.add(page);
		}

		return set;
	}

	/**
	 * Does not work.
	 * 
	 * @param date
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public Collection getAllChangedSince(Date date) {
		return new ArrayList(); // FIXME
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPageCount() {
		File wikipagedir = new File(m_pageDirectory);

		File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

		return wikipages.length;
	}

	/**
	 * Iterates through all WikiPages, matches them against the given query, and
	 * returns a Collection of SearchResult objects.
	 * 
	 * @param query
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public Collection findPages(QueryItem[] query) {
		File wikipagedir = new File(m_pageDirectory);
		TreeSet<SearchResult> res = new TreeSet<SearchResult>(new SearchResultComparator());
		SearchMatcher matcher = new SearchMatcher(m_engine, query);

		File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

		for(int i = 0; i < wikipages.length; i++) {
			FileInputStream input = null;

			// log.debug("Searching page "+wikipages[i].getPath() );

			String filename = wikipages[i].getName();
			int cutpoint = filename.lastIndexOf(FILE_EXT);
			String wikiname = filename.substring(0, cutpoint);

			wikiname = unmangleName(wikiname);

			try {
				input = new FileInputStream(wikipages[i]);
				String pagetext = FileUtil.readContents(input, m_encoding);
				SearchResult comparison = matcher.matchPageContent(wikiname, pagetext);
				if(comparison != null) {
					res.add(comparison);
				}
			} catch(IOException e) {
				log.error("Failed to read " + filename, e);
			} finally {
				try {
					if(input != null)
						input.close();
				} catch(IOException e) {
					// It's fine to fail silently.
				} 
			}
		}

		return res;
	}

	/**
	 * Always returns the latest version, since FileSystemProvider does not
	 * support versioning.
	 * 
	 * @param page
	 *            {@inheritDoc}
	 * @param version
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws {@inheritDoc}
	 */
	public WikiPage getPageInfo(String page, int version)
	        throws ProviderException {
		File file = findPage(page);

		if(!file.exists()) {
			return null;
		}

		WikiPage p = new WikiPage(m_engine, page);
		p.setLastModified(new Date(file.lastModified()));

		return p;
	}

	/**
	 * The FileSystemProvider provides only one version.
	 * 
	 * @param page
	 *            {@inheritDoc}
	 * @throws {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public List getVersionHistory(String page)
	        throws ProviderException {
		ArrayList<WikiPage> list = new ArrayList<WikiPage>();

		list.add(getPageInfo(page, WikiPageProvider.LATEST_VERSION));

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProviderInfo() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteVersion(String pageName, int version)
	        throws ProviderException {
		if(version == WikiProvider.LATEST_VERSION) {
			File f = findPage(pageName);

			f.delete();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deletePage(String pageName)
	        throws ProviderException {
		File f = findPage(pageName);

		f.delete();
	}

	/**
	 * A simple filter which filters only those filenames which correspond to
	 * the file extension used.
	 */
	public static class WikiFileFilter
	        implements FilenameFilter {
		/**
		 * {@inheritDoc}
		 */
		public boolean accept(File dir, String name) {
			return name.endsWith(FILE_EXT);
		}
	}
}
