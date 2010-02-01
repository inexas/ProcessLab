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

import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.parser.MarkupParser;
import com.ecyrd.jspwiki.render.RenderingManager;
import com.ecyrd.jspwiki.util.ClassUtil;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.events.*;

/**
 * Provides a caching page provider. This class rests on top of a real provider
 * class and provides a cache to speed things up. Only if the cache copy of the
 * page text has expired, we fetch it from the provider.
 * <p>
 * This class also detects if someone has modified the page externally, not
 * through JSPWiki routines, and throws the proper RepositoryModifiedException.
 * <p>
 * Heavily based on ideas by Chris Brooking.
 * <p>
 * Since 2.1.52 uses the OSCache library from OpenSymphony.
 * 
 * @since 1.6.4
 * @see RepositoryModifiedException
 */
// FIXME: Synchronization is a bit inconsistent in places.
// FIXME: A part of the stuff is now redundant, since we could easily use the
// text cache
// for a lot of things. RefactorMe.

@SuppressWarnings("unchecked")
public class CachingProvider
        implements WikiPageProvider, VersioningProvider {
	private static final Logger log = Logger.getLogger(CachingProvider.class);

	private WikiPageProvider m_provider;
	// FIXME: Find another way to the search engine to use instead of from
	// WikiEngine?
	private WikiEngine m_engine;

	private Cache m_cache;
	private Cache m_negCache; // Cache for holding non-existing pages

	private Cache m_textCache;
	private Cache m_historyCache;

	private long m_cacheMisses = 0;
	private long m_cacheHits = 0;

	private long m_historyCacheMisses = 0;
	private long m_historyCacheHits = 0;

	private int m_expiryPeriod = 30;

	/**
	 * This can be very long, as normally all modifications are noticed in an
	 * earlier stage.
	 */
	private int m_pageContentExpiryPeriod = 24 * 60 * 60;

	// FIXME: This MUST be cached somehow.

	private boolean m_gotall = false;

	private CacheItemCollector m_allCollector = new CacheItemCollector();

	/**
	 * Defines, in seconds, the amount of time a text will live in the cache at
	 * most before requiring a refresh.
	 */

	public static final String PROP_CACHECHECKINTERVAL = "jspwiki.cachingProvider.cacheCheckInterval";

	/**
	 * The capacity of the cache.
	 */
	public static final String PROP_CACHECAPACITY = "jspwiki.cachingProvider.capacity";

	private static final int DEFAULT_CACHECAPACITY = 1000; // Good most wikis

	private static final String OSCACHE_ALGORITHM = "com.opensymphony.oscache.base.algorithm.LRUCache";

	/**
	 * {@inheritDoc}
	 */
	public void initialize(WikiEngine engine, Properties properties)
	        throws NoRequiredPropertyException,
	        IOException {
		log.debug("Initing CachingProvider");

		// engine is used for getting the search engine
		m_engine = engine;

		//
		// Cache consistency checks
		//
		m_expiryPeriod = TextUtil.getIntegerProperty(properties,
		        PROP_CACHECHECKINTERVAL,
		        m_expiryPeriod);

		log.debug("Cache expiry period is " + m_expiryPeriod + " s");

		//
		// Text cache capacity
		//
		int capacity = TextUtil.getIntegerProperty(properties,
		        PROP_CACHECAPACITY,
		        DEFAULT_CACHECAPACITY);

		log.debug("Cache capacity " + capacity + " pages.");

		m_cache = new Cache(true, false, false);

		//
		// OSCache documentation sucks big time. The clazz-parameter is
		// completely
		// undefined; I had to read the source code to figure out that you need
		// to declare what type of a listener you are adding by sending the type
		// of the interface.
		//
		m_cache.addCacheEventListener(m_allCollector, CacheEntryEventListener.class);

		//
		// FIXME: There's an interesting issue here... It would probably be
		// possible to DOS a JSPWiki instance by bombarding it with names that
		// do not exist, as they would fill the negcache. Will need to
		// think about this some more...
		//
		m_negCache = new Cache(true, false, false);

		m_textCache = new Cache(true, false, false,
		        false,
		        OSCACHE_ALGORITHM,
		        capacity);

		m_historyCache = new Cache(true, false, false, false,
		        OSCACHE_ALGORITHM,
		        capacity);

		//
		// Find and initialize real provider.
		//
		String classname = WikiEngine.getRequiredProperty(properties,
		        PageManager.PROP_PAGEPROVIDER);

		try {
			Class providerclass = ClassUtil.findClass("com.ecyrd.jspwiki.providers",
			        classname);

			m_provider = (WikiPageProvider)providerclass.newInstance();

			log.debug("Initializing real provider class " + m_provider);
			m_provider.initialize(engine, properties);
		} catch(ClassNotFoundException e) {
			log.error("Unable to locate provider class " + classname, e);
			throw new IllegalArgumentException("no provider class");
		} catch(InstantiationException e) {
			log.error("Unable to create provider class " + classname, e);
			throw new IllegalArgumentException("faulty provider class");
		} catch(IllegalAccessException e) {
			log.error("Illegal access to provider class " + classname, e);
			throw new IllegalArgumentException("illegal provider class");
		}
	}

	private WikiPage getPageInfoFromCache(String name)
	        throws ProviderException,
	        RepositoryModifiedException {
		boolean wasUpdated = false;
		// Sanity check; seems to occur sometimes
		if(name == null)
			return null;

		try {
			WikiPage item = (WikiPage)m_cache.getFromCache(name, m_expiryPeriod);

			wasUpdated = true;

			if(item != null)
				return item;

			return null;
		} catch(NeedsRefreshException e) {
			WikiPage cached = (WikiPage)e.getCacheContent();

			// int version = (cached != null) ? cached.getVersion() :
			// WikiPageProvider.LATEST_VERSION;

			WikiPage refreshed;

			//
			// Just be careful that we don't accidentally leave the cache in a
			// hung state
			//

			refreshed = m_provider.getPageInfo(name, WikiPageProvider.LATEST_VERSION);

			if(refreshed == null && cached != null) {
				// Page has been removed evilly by a goon from outer space

				log.debug("Page " + name + " has been removed externally.");

				m_cache.putInCache(name, null);
				m_textCache.putInCache(name, null);
				m_historyCache.putInCache(name, null);
				// We cache a page miss
				m_negCache.putInCache(name, name);
				wasUpdated = true;

				throw new RepositoryModifiedException("Removed: " + name, name);
			} else if(cached == null) {
				// The page did not exist in the first place

				if(refreshed != null) {
					// We must now add it
					m_cache.putInCache(name, refreshed);
					// Requests for this page are now no longer denied
					m_negCache.putInCache(name, null);
					wasUpdated = true;

					throw new RepositoryModifiedException("Added: " + name, name);
				}

				// Cache page miss
				m_negCache.putInCache(name, name);
			} else if(refreshed != null && cached.getVersion() != refreshed.getVersion()) {
				// The newest version has been deleted, but older versions still
				// remain
				log.debug("Page " + cached.getName() + " newest version deleted, reloading...");

				m_cache.putInCache(name, refreshed);
				// Requests for this page are now no longer denied
				m_negCache.removeEntry(name);

				m_textCache.removeEntry(name);
				m_historyCache.removeEntry(name);
				wasUpdated = true;

				return refreshed;
			} else if(refreshed != null
			        && Math.abs(refreshed.getLastModified().getTime() - cached.getLastModified().getTime()) > 1000L) {
				// Yes, the page has been modified externally and nobody told us

				log.info("Page " + cached.getName() + " changed, reloading...");

				m_cache.putInCache(name, refreshed);
				// Requests for this page are now no longer denied
				m_negCache.removeEntry(name);
				m_textCache.removeEntry(name);
				m_historyCache.removeEntry(name);
				wasUpdated = true;

				throw new RepositoryModifiedException("Modified: " + name, name);
			} else {
				// Refresh the cache by putting the same object back
				m_cache.putInCache(name, cached);
				// Requests for this page are now no longer denied
				m_negCache.putInCache(name, null);
				wasUpdated = true;
			}

			return cached;
		} finally {
			if(!wasUpdated)
				m_cache.cancelUpdate(name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean pageExists(String pageName, int version) {
		if(pageName == null)
			return false;

		//
		// First, check the negative cache if we've seen it before
		//
		try {
			String isNonExistant = (String)m_negCache.getFromCache(pageName, m_expiryPeriod);

			if(isNonExistant != null)
				return false; // No such page
		} catch(NeedsRefreshException e) {
			m_negCache.cancelUpdate(pageName);
		}

		WikiPage p = null;

		try {
			p = getPageInfoFromCache(pageName);
		} catch(RepositoryModifiedException e) {
			// The repository was modified, we need to check now if the page was
			// removed or
			// added.
			// XXX: This information would be available in the exception, but
			// we would
			// need to subclass.

			try {
				p = getPageInfoFromCache(pageName);
			} catch(Exception ex) {
				// This should not happen
				return false;
			}
		} catch(ProviderException e) {
			log.info("Provider failed while trying to check if page exists: " + pageName);
			return false;
		}

		if(p != null) {
			int latestVersion = p.getVersion();

			if(version == latestVersion || version == LATEST_VERSION) {
				return true;
			}

			if(m_provider instanceof VersioningProvider)
				return ((VersioningProvider)m_provider).pageExists(pageName, version);
		}

		try {
			return getPageInfo(pageName, version) != null;
		} catch(ProviderException e) {
			//
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean pageExists(String pageName) {
		if(pageName == null)
			return false;

		//
		// First, check the negative cache if we've seen it before
		//
		try {
			String isNonExistant = (String)m_negCache.getFromCache(pageName, m_expiryPeriod);

			if(isNonExistant != null)
				return false; // No such page
		} catch(NeedsRefreshException e) {
			m_negCache.cancelUpdate(pageName);
		}

		WikiPage p = null;

		try {
			p = getPageInfoFromCache(pageName);
		} catch(RepositoryModifiedException e) {
			// The repository was modified, we need to check now if the page was
			// removed or
			// added.
			// XXX: This information would be available in the exception, but
			// we would
			// need to subclass.

			try {
				p = getPageInfoFromCache(pageName);
			} catch(Exception ex) {
				return false; // This should not happen
			}
		} catch(ProviderException e) {
			log.info("Provider failed while trying to check if page exists: " + pageName);
			return false;
		}

		//
		// A null item means that the page either does not
		// exist, or has not yet been cached; a non-null
		// means that the page does exist.
		//
		if(p != null) {
			return true;
		}

		//
		// If we have a list of all pages in memory, then any page
		// not in the cache must be non-existent.
		//
		// FIXME: There's a problem here; if someone modifies the
		// repository by adding a page outside JSPWiki,
		// we won't notice it.

		if(m_gotall) {
			return false;
		}

		//
		// We could add the page to the cache here as well,
		// but in order to understand whether that is a
		// good thing or not we would need to analyze
		// the JSPWiki calling patterns extensively. Presumably
		// it would be a good thing if pageExists() is called
		// many times before the first getPageText() is called,
		// and the whole page is cached.
		//
		return m_provider.pageExists(pageName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws RepositoryModifiedException
	 *             If the page has been externally modified.
	 */
	public String getPageText(String pageName, int version)
	        throws ProviderException,
	        RepositoryModifiedException {
		String result = null;

		if(pageName == null)
			return null;

		if(version == WikiPageProvider.LATEST_VERSION) {
			result = getTextFromCache(pageName);
		} else {
			WikiPage p = getPageInfoFromCache(pageName);

			//
			// Or is this the latest version fetched by version number?
			//
			if(p != null && p.getVersion() == version) {
				result = getTextFromCache(pageName);
			} else {
				result = m_provider.getPageText(pageName, version);
			}
		}

		return result;
	}

	/**
	 * @throws RepositoryModifiedException
	 *             If the page has been externally modified.
	 */
	private String getTextFromCache(String pageName)
	        throws ProviderException,
	        RepositoryModifiedException {
		String text;
		boolean wasUpdated = false;

		if(pageName == null)
			return null;

		WikiPage page = getPageInfoFromCache(pageName);

		try {
			text = (String)m_textCache.getFromCache(pageName,
			        m_pageContentExpiryPeriod);
			wasUpdated = true;

			if(text == null) {
				if(page != null) {
					text = m_provider.getPageText(pageName, WikiPageProvider.LATEST_VERSION);

					m_textCache.putInCache(pageName, text);

					m_cacheMisses++;
				} else {
					return null;
				}
			} else {
				m_cacheHits++;
			}
		} catch(NeedsRefreshException e) {
			if(pageExists(pageName)) {
				text = m_provider.getPageText(pageName, WikiPageProvider.LATEST_VERSION);

				m_textCache.putInCache(pageName, text);
				wasUpdated = true;

				m_cacheMisses++;
			} else {
				m_textCache.putInCache(pageName, null);
				wasUpdated = true;
				return null; // No page exists
			}
		} finally {
			if(!wasUpdated)
				m_textCache.cancelUpdate(pageName);
		}

		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	public void putPageText(WikiPage page, String text)
	        throws ProviderException {
		synchronized(this) {
			m_provider.putPageText(page, text);

			page.setLastModified(new Date());

			// Refresh caches properly

			m_cache.removeEntry(page.getName());
			m_textCache.removeEntry(page.getName());
			m_historyCache.removeEntry(page.getName());
			m_negCache.removeEntry(page.getName());

			// Refresh caches
			try {
				getPageInfoFromCache(page.getName());
			} catch(RepositoryModifiedException e) {
				//
			} // Expected
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAllPages()
	        throws ProviderException {
		Collection all;

		if(m_gotall == false) {
			all = m_provider.getAllPages();

			// Make sure that all pages are in the cache.

			synchronized(this) {
				for(Iterator i = all.iterator(); i.hasNext();) {
					WikiPage p = (WikiPage)i.next();

					m_cache.putInCache(p.getName(), p);
					// Requests for this page are now no longer denied
					m_negCache.putInCache(p.getName(), null);
				}

				m_gotall = true;
			}
		} else {
			all = m_allCollector.getAllItems();
		}

		return all;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAllChangedSince(Date date) {
		return m_provider.getAllChangedSince(date);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPageCount()
	        throws ProviderException {
		return m_provider.getPageCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection findPages(QueryItem[] query) {
		//
		// If the provider is a fast searcher, then
		// just pass this request through.
		//
		return m_provider.findPages(query);

		// FIXME: Does not implement fast searching
	}

	//
	// FIXME: Kludge: make sure that the page is also parsed and it gets all the
	// necessary variables.
	//

	private void refreshMetadata(WikiPage page) {
		if(page != null && !page.hasMetadata()) {
			RenderingManager mgr = m_engine.getRenderingManager();

			try {
				String data = m_provider.getPageText(page.getName(), page.getVersion());

				WikiContext ctx = new WikiContext(m_engine, page);
				MarkupParser parser = mgr.getParser(ctx, data);

				parser.parse();
			} catch(Exception ex) {
				log.debug("Failed to retrieve variables for wikipage " + page);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public WikiPage getPageInfo(String pageName, int version)
	        throws ProviderException,
	        RepositoryModifiedException {
		WikiPage page = null;
		WikiPage cached = getPageInfoFromCache(pageName);

		int latestcached = (cached != null) ? cached.getVersion() : Integer.MIN_VALUE;

		if(version == WikiPageProvider.LATEST_VERSION ||
		        version == latestcached) {
			if(cached == null) {
				WikiPage data = m_provider.getPageInfo(pageName, version);

				if(data != null) {
					m_cache.putInCache(pageName, data);
					// Requests for this page are now no longer denied
					m_negCache.putInCache(pageName, null);
				}
				page = data;
			} else {
				page = cached;
			}
		} else {
			// We do not cache old versions.
			page = m_provider.getPageInfo(pageName, version);
			// refreshMetadata( page );
		}

		refreshMetadata(page);

		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getVersionHistory(String pageName)
	        throws ProviderException {
		List history = null;
		boolean wasUpdated = false;

		if(pageName == null)
			return null;
		try {
			history = (List)m_historyCache.getFromCache(pageName,
			        m_expiryPeriod);

			log.debug("History cache hit for page " + pageName);
			m_historyCacheHits++;
			wasUpdated = true;
		} catch(NeedsRefreshException e) {
			history = m_provider.getVersionHistory(pageName);

			m_historyCache.putInCache(pageName, history);

			log.debug("History cache miss for page " + pageName);
			m_historyCacheMisses++;
			wasUpdated = true;
		} finally {
			if(!wasUpdated)
				m_historyCache.cancelUpdate(pageName);
		}

		return history;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized String getProviderInfo() {
		return "Real provider: " + m_provider.getClass().getName() +
		        ". Cache misses: " + m_cacheMisses +
		        ". Cache hits: " + m_cacheHits +
		        ". History cache hits: " + m_historyCacheHits +
		        ". History cache misses: " + m_historyCacheMisses +
		        ". Cache consistency checks: " + m_expiryPeriod + "s";
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteVersion(String pageName, int version)
	        throws ProviderException {
		//
		// Luckily, this is such a rare operation it is okay
		// to synchronize against the whole thing.
		//
		synchronized(this) {
			WikiPage cached = getPageInfoFromCache(pageName);

			int latestcached = (cached != null) ? cached.getVersion() : Integer.MIN_VALUE;

			//
			// If we have this version cached, remove from cache.
			//
			if(version == WikiPageProvider.LATEST_VERSION ||
			        version == latestcached) {
				m_cache.removeEntry(pageName);
				m_textCache.removeEntry(pageName);
				m_historyCache.removeEntry(pageName);
			}

			m_provider.deleteVersion(pageName, version);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deletePage(String pageName)
	        throws ProviderException {
		//
		// See note in deleteVersion().
		//
		synchronized(this) {
			m_cache.putInCache(pageName, null);
			m_textCache.putInCache(pageName, null);
			m_historyCache.putInCache(pageName, null);
			m_negCache.putInCache(pageName, pageName);
			m_provider.deletePage(pageName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void movePage(String from,
	        String to)
	        throws ProviderException {
		m_provider.movePage(from, to);

		synchronized(this) {
			// Clear any cached version of the old page
			log.debug("Removing from page " + from + " from cache");
			// m_cache.removeEntry( from );
			m_cache.putInCache(from, null);
			m_textCache.putInCache(from, null);
			m_historyCache.putInCache(from, null);
			m_negCache.putInCache(from, from);
			// Clear the cache for the to page, if that page already exists
			// if ( m_cache.get( to ) != null )
			// {
			log.debug("Removing to page " + to + " from cache");
			// m_cache.removeEntry( to );
			m_cache.putInCache(to, null);
			m_textCache.putInCache(to, null);
			m_historyCache.putInCache(to, null);
			m_negCache.putInCache(to, to);
			// }
		}
	}

	/**
	 * Returns the actual used provider.
	 * 
	 * @since 2.0
	 * @return The real provider.
	 */
	public WikiPageProvider getRealProvider() {
		return m_provider;
	}

	/**
	 * This is a simple class that keeps a list of all WikiPages that we have in
	 * memory. Because the OSCache cannot give us a list of all pages currently
	 * in cache, we'll have to check this ourselves.
	 * 
	 * 
	 * @since 2.4
	 */
	private static class CacheItemCollector
	        implements CacheEntryEventListener {
		private Map<String, WikiPage> m_allItems = new Hashtable<String, WikiPage>();

		/**
		 * Returns a clone of the set - you cannot manipulate this.
		 * 
		 * @return A Set of WikiPage objects.
		 */
		public Set getAllItems() {
			Set<WikiPage> ret = new TreeSet<WikiPage>();
			ret.addAll(m_allItems.values());

			return ret;
		}

		public void cacheEntryAdded(CacheEntryEvent arg0) {
			cacheEntryUpdated(arg0);
		}

		public void cachePatternFlushed(CachePatternEvent ev) {
			//
		}

		public void cacheGroupFlushed(CacheGroupEvent ev) {
			//
		}

		public void cacheFlushed(CachewideEvent ev) {
			//
		}

		public void cacheEntryFlushed(CacheEntryEvent arg0) {
			cacheEntryRemoved(arg0);
		}

		public void cacheEntryRemoved(CacheEntryEvent arg0) {
			if(arg0.getEntry() != null) {
				WikiPage item = (WikiPage)arg0.getEntry().getContent();

				if(item != null) {
					m_allItems.remove(item.getName());
				}
			}
		}

		public void cacheEntryUpdated(CacheEntryEvent arg0) {
			WikiPage item = (WikiPage)arg0.getEntry().getContent();

			if(item != null) {
				// Item added or replaced.
				m_allItems.put(item.getName(), item);
			} else {
				// Removed item
				// FIXME: If the page system is changed during this time, we'll
				// just fail gracefully

				m_allItems.remove(arg0.getKey());
			}
		}
	}
}
