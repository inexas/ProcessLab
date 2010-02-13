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
package com.ecyrd.jspwiki.plugin;

/*
 PageView - a plugin based on PageViewCountPlugin for:

 JSPWiki - a JSP-based WikiWiki clone.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import com.ecyrd.jspwiki.ReferenceManager;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.event.WikiEngineEvent;
import com.ecyrd.jspwiki.event.WikiEvent;
import com.ecyrd.jspwiki.event.WikiEventListener;
import com.ecyrd.jspwiki.plugin.InitializablePlugin;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.PluginManager;
import com.ecyrd.jspwiki.plugin.WikiPlugin;
import com.ecyrd.jspwiki.util.WikiBackgroundThread;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Counts the number of times each page has been viewed.
 * 
 * Parameters: count=yes|no show=none|count|list entries=max number of list
 * entries min=minimum page count to be listed sort=name|count
 * 
 * Default values are show=none and sort=name.
 * 
 * @author Andre van dalen
 * @version 0.5
 */
@SuppressWarnings("unchecked")
public class PageViewPlugin implements WikiPlugin, InitializablePlugin {

	/** The logger. */
	private static Logger log = Logger.getLogger(PageViewPlugin.class);

	/** The page view manager. */
	private static PageViewManager singleton = null;

	/** Constant for the 'count' parameter / value. */
	private static final String PARAM_COUNT = "count";

	/** Name of the 'entries' parameter. */
	private static final String PARAM_MAX_ENTRIES = "entries";

	/** Name of the 'exclude' parameter. */
	private static final String PARAM_EXCLUDE = "exclude";

	/** Name of the 'include' parameter. */
	private static final String PARAM_INCLUDE = "include";

	/** Name of the 'max' parameter. */
	private static final String PARAM_MAX_COUNT = "max";

	/** Name of the 'min' parameter. */
	private static final String PARAM_MIN_COUNT = "min";

	/** Name of the 'refer' parameter. */
	private static final String PARAM_REFER = "refer";

	/** Name of the 'show' parameter. */
	private static final String PARAM_SHOW = "show";

	/** Name of the 'sort' parameter. */
	private static final String PARAM_SORT = "sort";

	/** Constant for the 'none' parameter value. */
	private static final String STR_NONE = "none";

	/** Constant for the 'list' parameter value. */
	private static final String STR_LIST = "list";

	/** Constant for the 'name' parameter value. */
	// private static final String STR_NAME = "name";

	/** Constant for the 'yes' parameter value. */
	private static final String STR_YES = "yes";

	/** Constant for empty string. */
	private static final String STR_EMPTY = "";

	/** Constant for Wiki markup separator. */
	private static final String STR_SEPARATOR = "----";

	/** Constant for comma-separated list separator. */
	private static final String STR_COMMA = ",";

	/** Constant for no-op glob exression. */
	private static final String STR_GLOBSTAR = "*";

	/** Constant for file storage. */
	private static final String COUNTER_PAGE = "PageCount.txt";

	/** Constant for storage interval in seconds. */
	private static final int STORAGE_INTERVAL = 60;

	/**
	 * Initialize the PageViewPlugin and its singleton.
	 * 
	 * @param engine
	 *            The wiki engine.
	 */
	public void initialize(WikiEngine engine) {

		log.info("initialize");

		if(null == singleton) {

			singleton = new PageViewManager(log);

			singleton.initialize(engine);
		}
	}

	/**
	 * Cleanup the singleton reference.
	 */
	public void cleanup() {

		log.info("cleanup");

		singleton = null;
	}

	/**
	 * Count a page hit, present a pages' counter or output a list of
	 * pagecounts.
	 * 
	 * @param context
	 * @param params
	 * @throws com.ecyrd.jspwiki.plugin.PluginException
	 * @return String Wiki page snippet
	 * @concurrency concurrent
	 */
	public String execute(WikiContext context, Map params) throws PluginException {

		PageViewManager manager = singleton;
		String result = STR_EMPTY;

		if(null != manager) {

			result = manager.execute(context, params);
		}

		return result;
	}

	/**
	 * Page view manager, handling all storage.
	 */
	public final class PageViewManager implements WikiEventListener {

		/** The logger. */
		@SuppressWarnings("hiding")
		private final Logger log;

		/** Are we initialized? */
		private boolean initialized = false;

		/** The page counters. */
		private Map counters = null;

		/** The page counters in storage format. */
		private Properties storage = null;

		/** Are all changes stored? */
		private boolean dirty = false;

		/** The page count storage background thread. */
		private Thread pageCountSaveThread = null;

		/** The work directory. */
		private String workDir = null;

		/** Comparator for descending sort on page count. */
		private final Comparator compareCountDescending = new Comparator() {
			public int compare(Object o1, Object o2) {
				final int v1 = getCount(o1);
				final int v2 = getCount(o2);
				return (v1 == v2) ? ((String)o1).compareTo((String)o2) : (v1 < v2) ? 1 : -1;
			}
		};

		/**
		 * Create a page view manager.
		 * 
		 * @param logger
		 */
		public PageViewManager(Logger logger) {
			log = logger;
		}

		/**
		 * Initialize the page view manager.
		 * 
		 * @param engine
		 *            The wiki engine.
		 * @concurrency guarded
		 */
		public synchronized void initialize(WikiEngine engine) {

			log.info("initialize manager");

			initialized = true;
			workDir = engine.getWorkDir();

			engine.addWikiEventListener(this);

			if(null == counters) {

				// Load the counters into a collection
				storage = new Properties();
				counters = new TreeMap();

				loadCounters();
			}

			// backup counters every 5 minutes
			if(null == pageCountSaveThread) {
				pageCountSaveThread = new CounterSaveThread(engine, 5 * STORAGE_INTERVAL, this);
				pageCountSaveThread.start();
			}
		}

		/**
		 * Handle the shutdown event via the page counter thread.
		 * 
		 * @concurrency guarded
		 */
		private synchronized void handleShutdown() {

			log.info("handleShutdown: The counter store thread was shut down.");

			cleanup();

			if(null != counters) {

				dirty = true;
				storeCounters();

				counters.clear();
				counters = null;

				storage.clear();
				storage = null;
			}

			initialized = false;

			pageCountSaveThread = null;
		}

		/**
		 * Inspect wiki events for shutdown.
		 * 
		 * @param event
		 *            The wiki event to inspect.
		 */
		public void actionPerformed(WikiEvent event) {

			if(event instanceof WikiEngineEvent) {
				if(event.getType() == WikiEngineEvent.SHUTDOWN) {

					log.info("actionPerformed: Detected wiki engine shutdown");
					handleShutdown();
				}
			}
		}

		/**
		 * Count a page hit, present a pages' counter or output a list of
		 * pagecounts.
		 * 
		 * @param context
		 * @param params
		 * @throws com.ecyrd.jspwiki.plugin.PluginException
		 * @return String Wiki page snippet
		 * @throws PluginException
		 *             Malformed pattern parameter.
		 * @concurrency concurrent
		 */
		public String execute(WikiContext context, Map params) throws PluginException {
			WikiEngine engine = context.getEngine();
			WikiPage page = context.getPage();
			String result = STR_EMPTY;

			if(null != page) {
				// get parameters
				String pagename = page.getName();
				String count = (String)params.get(PARAM_COUNT);
				String show = (String)params.get(PARAM_SHOW);
				int entries = TextUtil.parseIntParameter((String)params.get(PARAM_MAX_ENTRIES), Integer.MAX_VALUE);
				final int max = TextUtil.parseIntParameter((String)params.get(PARAM_MAX_COUNT), Integer.MAX_VALUE);
				final int min = TextUtil.parseIntParameter((String)params.get(PARAM_MIN_COUNT), Integer.MIN_VALUE);
				String sort = (String)params.get(PARAM_SORT);
				String body = (String)params.get(PluginManager.PARAM_BODY);
				Pattern[] exclude = compileGlobs(PARAM_EXCLUDE, (String)params.get(PARAM_EXCLUDE));
				Pattern[] include = compileGlobs(PARAM_INCLUDE, (String)params.get(PARAM_INCLUDE));
				Pattern[] refer = compileGlobs(PARAM_REFER, (String)params.get(PARAM_REFER));
				PatternMatcher matcher = (null != exclude || null != include || null != refer) ? new Perl5Matcher()
				        : null;
				boolean increment = false;

				// increment counter?
				if(STR_YES.equals(count)) {
					increment = true;
				} else {
					count = null;
				}

				// default increment counter?
				if((null == show || STR_NONE.equals(show)) && null == count) {
					increment = true;
				}

				// filter on referring pages?
				Collection referrers = null;

				if(null != refer) {
					ReferenceManager refManager = engine.getReferenceManager();

					Iterator iter = refManager.findCreated().iterator();

					while(null != iter && iter.hasNext()) {

						String name = (String)iter.next();
						boolean use = false;

						for(int n = 0; !use && n < refer.length; n++) {
							use = matcher.matches(name, refer[n]);
						}

						if(use) {
							Collection refs = engine.getReferenceManager().findReferrers(name);

							if(null != refs && !refs.isEmpty()) {
								if(null == referrers) {
									referrers = new HashSet();
								}
								referrers.addAll(refs);
							}
						}
					}
				}

				synchronized(this) {
					Counter counter = (Counter)counters.get(pagename);

					// only count in view mode, keep storage values in sync
					if(increment && WikiContext.VIEW.equalsIgnoreCase(context.getRequestContext())) {
						if(null == counter) {
							counter = new Counter();
							counters.put(pagename, counter);
						}
						counter.increment();
						storage.setProperty(pagename, counter.toString());
						dirty = true;
					}

					if(null == show || STR_NONE.equals(show)) {
						// nothing to show

					} else if(PARAM_COUNT.equals(show)) {
						// show page count
						result = counter.toString();

					} else if(null != body && 0 < body.length() && STR_LIST.equals(show)) {
						// show list of counts
						String header = STR_EMPTY;
						String line = body;
						String footer = STR_EMPTY;
						int start = body.indexOf(STR_SEPARATOR);

						// split body into header, line, footer on ----
						// separator
						if(0 < start) {
							header = body.substring(0, start);

							start = skipWhitespace(start + STR_SEPARATOR.length(), body);

							int end = body.indexOf(STR_SEPARATOR, start);

							if(start >= end) {
								line = body.substring(start);

							} else {
								line = body.substring(start, end);

								end = skipWhitespace(end + STR_SEPARATOR.length(), body);

								footer = body.substring(end);
							}
						}

						// sort on name or count?
						Map sorted = counters;

						if(null != sort && PARAM_COUNT.equals(sort)) {
							sorted = new TreeMap(compareCountDescending);

							sorted.putAll(counters);
						}

						// build a messagebuffer with the list in wiki markup
						StringBuffer buf = new StringBuffer(header);
						MessageFormat fmt = new MessageFormat(line);
						Object[] args = new Object[] { pagename, STR_EMPTY, STR_EMPTY };
						Iterator iter = sorted.entrySet().iterator();

						while(null != iter && 0 < entries && iter.hasNext()) {

							Entry entry = (Entry)iter.next();
							String name = (String)entry.getKey();

							// check minimum count
							final int value = ((Counter)entry.getValue()).getValue();
							boolean use = min <= value && value <= max;

							// did we specify a refer-to page?
							if(use && null != referrers) {

								use = referrers.contains(name);
							}

							// did we specify what pages to include?
							if(use && null != include) {
								use = false;

								for(int n = 0; !use && n < include.length; n++) {

									use = matcher.matches(name, include[n]);
								}
							}

							// did we specify what pages to exclude?
							if(use && null != exclude) {
								for(int n = 0; use && n < exclude.length; n++) {

									use &= !matcher.matches(name, exclude[n]);
								}
							}

							if(use) {
								args[1] = engine.beautifyTitle(name);
								args[2] = entry.getValue();

								fmt.format(args, buf, null);

								entries--;
							}
						}
						buf.append(footer);

						// let the engine render the list
						result = engine.textToHTML(context, buf.toString());
					}
				}
			}
			return result;
		}

		/**
		 * Compile regexp parameter.
		 * 
		 * @param name
		 *            The name of the parameter.
		 * @param value
		 *            The parameter value.
		 * @return Pattern[] The compiled patterns, or <code>null</code>.
		 * @throws PluginException
		 *             On malformed patterns.
		 */
		private Pattern[] compileGlobs(String name, String value) throws PluginException {

			Pattern[] result = null;

			if(null != value && 0 < value.length() && !STR_GLOBSTAR.equals(value)) {
				try {
					PatternCompiler pc = new GlobCompiler();

					String[] ptrns = StringUtils.split(value, STR_COMMA);

					result = new Pattern[ptrns.length];

					for(int n = 0; n < ptrns.length; n++) {
						result[n] = pc.compile(ptrns[n]);
					}
				} catch(MalformedPatternException e) {
					throw new PluginException("Parameter " + name + " has a malformed pattern: " + e.getMessage());
				}
			}

			return result;
		}

		/**
		 * Adjust ofsset skipping whitespace.
		 * 
		 * @param offset
		 *            The offset in value to adjust.
		 * @param value
		 *            String in which offset points.
		 * @return int Adjusted offset into value.
		 */
		private int skipWhitespace(int off, String value) {
			int offset = off;
			while(Character.isWhitespace(value.charAt(offset))) {
				offset++;
			}
			return offset;
		}

		/**
		 * Retrieve a page count.
		 * 
		 * @return int The page count for the given key.
		 * @pre counters != null
		 * @pre counters.get(key) != null
		 * @param key
		 */
		protected int getCount(Object key) {
			return ((Counter)counters.get(key)).getValue();
		}

		/**
		 * Load the page view counters from file.
		 */
		private void loadCounters() {
			if(null != counters && null != storage) {

				if(log.isInfoEnabled()) {
					log.info("loadCounters");
				}
				synchronized(this) {

					InputStream fis = null;

					try {
						fis = new FileInputStream(new File(workDir, COUNTER_PAGE));

						storage.load(fis);

					} catch(IOException ioe) {
						log.error("loadCounters: Can't load page counter store, will create a new one: "
						        + ioe.getMessage());

					} finally {
						try {
							if(null != fis) {
								fis.close();
							}
						} catch(Exception ignore) {
							/** ignore */
						}
					}

					// Copy the collection into a sorted map
					Iterator iter = storage.entrySet().iterator();

					while(null != iter && iter.hasNext()) {
						Entry entry = (Entry)iter.next();

						counters.put(entry.getKey(), new Counter((String)entry.getValue()));
					}

					if(log.isInfoEnabled()) {
						log.info("loadCounters: counters.size=" + counters.size());
					}
				}
			}
		}

		/**
		 * Save the page view counters to file.
		 * 
		 * @concurrency $none
		 */
		protected void storeCounters() {
			if(null != counters && null != storage && dirty) {

				if(log.isInfoEnabled()) {
					log.info("storeCounters: counters.size=" + counters.size());
				}
				synchronized(this) {

					OutputStream fos = null;

					// Write out the collection of counters
					try {
						fos = new FileOutputStream(new File(workDir, COUNTER_PAGE));

						storage.store(fos, "\n# The number of times each page has been viewed.\n# Do not modify.\n");
						fos.flush();

						dirty = false;

					} catch(IOException ioe) {
						log.error("storeCounters: Can't store counters: " + ioe.getMessage());

					} finally {
						try {
							if(null != fos) {
								fos.close();
							}
						} catch(Exception ignore) {
							/** ignore */
						}
					}
				}
			}
		}

		/**
		 * Is the given thread still current?
		 * 
		 * @return boolean <code>true</code> iff the thread is still the current
		 *         background thread.
		 * @param thrd
		 */
		protected boolean isRunning(Thread thrd) {
			return initialized && thrd == pageCountSaveThread;
		}

	}

	/**
	 * Counter for page hits collection.
	 */
	static final class Counter {

		/** The count value. */
		private int count = 0;

		/**
		 * Create a new counter.
		 */
		public Counter() {
			// Nothing to do
		}

		/**
		 * Create and initialise a new counter.
		 * 
		 * @param value
		 *            Count value.
		 */
		public Counter(String value) {

			setValue(value);
		}

		/**
		 * Increment counter.
		 */
		public void increment() {
			count++;
		}

		/**
		 * Get the count value.
		 * 
		 * @return int
		 */
		public int getValue() {

			return count;
		}

		/**
		 * Set the count value.
		 * 
		 * @param value
		 *            String representation of the count.
		 */
		public void setValue(String value) {
			try {
				count = Integer.parseInt(value);

			} catch(Exception ignore) {
				count = 0;
			}
		}

		/**
		 * @return String String representation of the count.
		 */
		@Override
        public String toString() {
			return String.valueOf(count);
		}
	}

	/**
	 * Background thread storing the page counters.
	 */
	static final class CounterSaveThread extends WikiBackgroundThread {

		/** The page view manager. */
		private final PageViewManager manager;

		/**
		 * Create a wiki background thread to store the page counters.
		 * 
		 * @param engine
		 *            The wiki engine.
		 * @param interval
		 *            Delay in seconds between saves.
		 * @param pageViewManager
		 */
		public CounterSaveThread(WikiEngine engine, int interval, PageViewManager pageViewManager) {

			super(engine, interval);

			if(null == pageViewManager) {
				throw new IllegalArgumentException("Manager cannot be null");
			}

			manager = pageViewManager;
		}

		/**
		 * Save the page counters to file.
		 */
		@Override
        public void backgroundTask() {

			if(manager.isRunning(this)) {
				manager.storeCounters();
			}
		}
	}
}
