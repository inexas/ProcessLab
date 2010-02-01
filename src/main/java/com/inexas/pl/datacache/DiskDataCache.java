/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.pl.datacache;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.util.*;
import com.google.visualization.datasource.datatable.*;

public class DiskDataCache {
	private final Logger log = Logger.getLogger("com.inexas.pl");
	private final static int HOUSEKEEP_INTERVAL = 15 * 60; // In seconds
	private final static String EXTENSION = ".cached";
	private final File home;
	private final String homePath;
	private long lastId = 0;
	private final Map<Key, Key> keys = new HashMap<Key, Key>();

	/**
	 * This is a thread that checks every HOUSEKEEP_INTERVAL seconds and deletes any expired
	 * cached data tables.
	 */
	private final class HousekeepThread extends WikiBackgroundThread {

		public HousekeepThread(WikiEngine engine) {
			super(engine, HOUSEKEEP_INTERVAL);
			setName("DiskDataCache housekeeper");
		}

		@Override
		public void backgroundTask() throws Exception {
			synchronized(keys) {
				for(final Key key : keys.values()) {
					if(key.expired()) {
						delete(key);
						keys.remove(key);
					}
				}
			}
		}
	}

	private static class Key implements Serializable {
		private static final long serialVersionUID = -3598765028370910526L;
		public final String pageName, query;
		public final int pageVersion;
		private final int hashCode;
		private final long expires;
		private String id;

		/**
		 * Keys constructed like this must only be used as keys and not as
		 * values in the keys map
		 * 
		 * @param pageName
		 * @param version
		 * @param query
		 */
		public Key(String pageName, int version, String query) {
			this(pageName, version, query, 0);
		}

		public Key(String pageName, int version, String query, int minutesToLive) {
			this.pageName = pageName;
			this.query = query;
			this.pageVersion = version;
			this.expires = minutesToLive == 0 ? 0 : new Date().getTime() + minutesToLive * 60 * 1000;
			// XORing the key fields seems reasonable but we might not have an
			// expires
			hashCode = pageName.hashCode() ^ pageVersion ^ query.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object rhsObject) {
			final boolean result;
			// Try for a cheap true...
			if(this == rhsObject) {
				result = true;
			} else {
				// Try for a cheap false...
				if(rhsObject == null || !(rhsObject instanceof Key)) {
					result = false;
				} else {
					final Key rhs = (Key)rhsObject;
					result =
					        pageName.equals(rhs.pageName) &&
					        pageVersion == rhs.pageVersion &&
					        query.equals(rhs.query);
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return "Key(" +
			        pageName + ':' +
			        pageVersion + ", " +
			        query + ", " +
			        new Date(expires).toString() + ")";
		}

		public boolean expired() {
			assert expires != 0;
			final long now = new Date().getTime();
			return expires < now;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			assert id != null : "Null ID";
			return id;
		}
	}

	public static class Cached implements Serializable {
		private static final long serialVersionUID = -5144196949178072280L;
		private final Key key;
		private final SerializableDataTable dataTable;
		private final long cachedAtTime;

		public Cached(Key key, DataTable dataTable) {
			this.key = key;
			this.dataTable = new SerializableDataTable(dataTable);
			cachedAtTime = new Date().getTime();
		}
		
		public DataTable getDataTable() {
			return dataTable.getDataTable();
		}
		
		
		public long getCachedAtTime() {
			return cachedAtTime;
		}

	}

	/**
	 * @param properties 
	 * @param workingDirectory
	 *            this will be something like /processlab/work/
	 */
	public DiskDataCache(WikiEngine wikiEngine, Properties properties) {
		final String tmp = properties.getProperty("jspwiki.workDir");
		final String workingDirectory = tmp.endsWith("/") ? tmp : tmp + '/';

		// Make sure we have a valid home directory...
		PropertyReader.getDefaultProperties();
		homePath = workingDirectory + "/cache/";
		log.info("Initializing DiskDataCache from " + homePath);
		home = new File(homePath);
		if(home.exists()) {
			if(!home.isDirectory()) {
				throw new RuntimeException(homePath + " is not a directory");
			}
		} else {
			if(!home.mkdir()) {
				throw new RuntimeException("Cannot create directory: " + homePath);
			}
		}
		// Check we can create files...
		try {
			final File test = new File(homePath + "create.test");
			if(test.exists()) {
				test.delete();
			}
			test.createNewFile();
			test.delete();
		} catch(final Exception e) {
			throw new RuntimeException("Cannot create files in " + homePath);
		}

		// Load the initial cache state...
		int loaded = 0, discarded = 0, ignored = 0;
		final String[] children = home.list();
		if(children == null) {
			throw new RuntimeException("Can't list files in " + homePath);
		}
		for(final String child : children) {
			// Ignore non-cache files...
			if(!child.endsWith(EXTENSION)) {
				ignored++;
				continue;
			}
			final String filename = homePath + child;
			final File file = new File(filename);
			if(file.isDirectory()) {
				ignored++;
				continue;
			}

			try {
				// Deserialize the file and save the key...
				final ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				final Cached value = (Cached)in.readObject();
				final Key key = value.key;
				in.close();

				if(key.expired()) {
					file.delete();
					discarded++;
				} else {
					keys.put(key, key);
					// Figure out the last ID...
					final String noExtension = child.substring(0, child.length() - EXTENSION.length());
					final long longValue = Long.parseLong(noExtension, 16);
					if(longValue > lastId) {
						lastId = longValue;
					}
					loaded++;
				}
			} catch(final Exception e) {
				log.warning("Cannot deserialize object from " + filename);
				file.delete();
				discarded++;
			}
		}
		log.info("Initialized cache (loaded/ignored/discarded): " + loaded + '/' + ignored + '/' + discarded);

		// Create and start the thread
		
		final HousekeepThread housekeepThread = new HousekeepThread(wikiEngine);
		housekeepThread.start();
		
		instance = this;
	}

	public void put(String pageName, int version, String query, int minutesToLive, DataTable dataTable) {
		assert minutesToLive > 0 : "Invalid minutes to live";

		synchronized(keys) {
			try {
				// Create the filename which is also the ID...
				final Key key = new Key(pageName, version, query, minutesToLive);
				lastId++;
				final String filename = Long.toHexString(lastId) + EXTENSION;
				final String path = homePath + filename;
				key.setId(filename);

				// Does it replace and existing one...
				final Key replaced = keys.get(key);
				if(replaced != null) {
					delete(replaced);
				}
				// Serialize the cached object...
				final File file = new File(path);
				final Cached cached = new Cached(key, dataTable);
				final ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(cached);
				out.close();

				// And add the key to the map...
				keys.put(key, key);
			} catch(final Exception e) {
				log.warning("Cannot write object to cache (probably not serializable) " + e.getMessage());
			}
		}
	}

	public Cached get(String pageName, int version, String query) {
		synchronized(keys) {
			Cached result;
			final Key keylet = new Key(pageName, version, query);
			final Key key = keys.get(keylet);
			if(key != null) {
				if(key.expired()) {
					delete(key);
					result = null;
				} else {
					final File file = getFile(key);
					try {
						final ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
						result = (Cached)in.readObject();
						in.close();
					} catch(final Exception e) {
						log.warning("Cannot deserialize object from " + file.getAbsolutePath() +
						        ": " + e.getMessage());
						result = null;
					}
				}
				if(result == null) {
					keys.remove(key);
				}
			} else {
				result = null;
			}
			return result;
		}
	}

	public void remove(String pageName, int version, String query) {
		final Key key = new Key(pageName, version, query);
		synchronized(keys) {
			if(keys.containsKey(key)) {
				keys.remove(key);
				delete(key);
			} else {
				throw new RuntimeException("DataSet not found: " + key);
			}
		}
	}

	private void delete(Key key) {
		final File file = getFile(key);
		if(!file.delete()) {
			log.warning("Cannot delete: " + file.getAbsolutePath());
		}

	}

	private File getFile(Key key) {
		assert key != null;
		final String filename = keys.get(key).getId();
		assert filename != null;
		return new File(homePath + filename);
	}

	private static DiskDataCache instance;

	public static DiskDataCache getInstance() {
		assert instance != null : "Initialize the data cache";
		return instance;
	}

}
