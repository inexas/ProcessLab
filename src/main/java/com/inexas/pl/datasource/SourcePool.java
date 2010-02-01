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
/**
 * 
 */
package com.inexas.pl.datasource;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.servlet.http.*;
import com.ecyrd.jspwiki.*;
import com.inexas.pl.bi.*;
import com.inexas.pl.bi.RandomNames.*;

/**
 * The StatementPool manages a set of SQL statements. It's like a connection
 * pool but on the level of statements. Statements are for a given data source.
 * 
 * The statement pool is a singleton so that statements are reusable across all
 * sessions.
 * 
 * The "demo" data source is a built-in data source. If, and when retrieved a
 * memory-based demo database is created and stored in the session along with
 * the StatementFactory.
 */
public class SourcePool {
	private static SourcePool instance;
	private static boolean inMemory = true;
	private static final String SP = SourcePool.class.getName();
	private static final long DAY_MS = 1000 * 60 * 60 * 24;
	public static final String DEMO_DB = "demo";

	private static final String LEADER = "datastar.db.";
	private static final int LEADER_LENGTH = LEADER.length();
	private static final String DRIVER = ".driver";
	private static final int DRIVER_LENGTH = DRIVER.length();
	private static final String URL = ".url";
	private static final String USER = ".user";
	private static final String PASSWORD = ".password";
	private static final String VIEW_LEADERS = ".viewsql";
	private static final String UPDATE_LEADERS = ".updatesql";
	private static final String COMMENT = ".comment";
	private final String home;
	private final String pageHome;

	/**
	 * Sources represent an access point to a single data source
	 */
	public static class Source {
		@SuppressWarnings("unused")
		private final long created = new java.util.Date().getTime();
		private final Factory factory;
		private final Statement statement;

		private Source(Factory factory, Statement statement) {
			this.factory = factory;
			this.statement = statement;
		}

		public ResultSet query(String sql) {
			try {
				return statement.executeQuery(sql);
			} catch(final SQLException e) {
				vanish();
				throw new RuntimeException("Error performing query: " + sql, e);
			}
		}

		public void update(String sql) {
			try {
				statement.executeUpdate(sql);
			} catch(final SQLException e) {
				vanish();
				throw new RuntimeException("Error performing update: " + sql, e);
			}
		}

		public void release() {
			factory.release(this);
		}

		private void vanish() {
			factory.release(this);
		}
	}

	/**
	 * A Source factory for a single db
	 */
	public static class Factory {
		private final String name, comment, driver, url, user, password;
		private final String[] viewLeaders;
		private final String[] updateLeaders;
		private Connection connection;
		private Queue<Source> queue = new LinkedList<Source>();

		private Factory(String name, String comment, String driver, String url, String user, String password,
		        String[] viewLeaders, String[] updateLeaders) {
			this.name = name;
			this.comment = comment;
			this.driver = driver;
			this.url = url;
			this.user = user;
			this.password = password;
			this.viewLeaders = viewLeaders;
			this.updateLeaders = updateLeaders;
		}

		private Source getSource() {
			// todo Handle multithreading, garbage collection, errors
			Source result;
			if(queue.isEmpty()) {
				// Need a new source...
				try {
					if(connection == null) {
						Class.forName(driver);
						connection = DriverManager.getConnection(url, user, password);
					}
					result = new Source(this, connection.createStatement());
				} catch(final Exception e) {
					connection = null;
					throw new RuntimeException("Connecting to database: " + url, e);
				}
			} else {
				result = queue.remove();
			}
			return result;
		}

		private void release(Source source) {
			queue.add(source);
		}

		@Override
		public String toString() {
			return "SourcePool: " + url + ':' + queue.size();
		}

		public String[] getViewLeaders() {
			return viewLeaders;
		}

		public String[] getUpdateLeaders() {
			return updateLeaders;
		}

		public String getName() {
			return name;
		}

		public String getComment() {
			return comment;
		}
	}

	private final Map<String, Factory> factories = new HashMap<String, Factory>();

	/**
	 * This is called by the engine as it is being initialized.
	 * 
	 * @param engine
	 */
	public static void initialize(WikiEngine engine) {
		assert instance == null;
		instance = new SourcePool(engine);
	}
	
	public static SourcePool getInstance() {
		return instance;
	}

	private SourcePool(WikiEngine engine) {

		// Read in the properties from the Data* config page
		final Properties properties = new Properties();
		pageHome = (String)engine.getWikiProperties().get("jspwiki.fileSystemProvider.pageDir");
		home = engine.getRootPath();
		final String filename = pageHome + "/DataStar+configuration.txt";
		try {
			final BufferedReader in = new BufferedReader(new FileReader(filename));
			String line;
			boolean inConfig = false;
			while((line = in.readLine()) != null) {
				final String trimmed = line.trim();
				if(trimmed.length() == 0) {
					continue;
				}
				if(inConfig) {
					if(trimmed.startsWith("}}}")) {
						inConfig = false;
						continue;
					}
				} else { // Not inConfig
					if(trimmed.startsWith("{{{")) {
						inConfig = true;
					}
					continue;
				}
				final int equals = trimmed.indexOf('=');
				if(equals < 0) {
					continue;
				}

				final String key = trimmed.substring(0, equals).trim();
				final String value = trimmed.substring(equals + 1).trim();
				properties.put(key, value);
			}
			in.close();
		} catch(final IOException e) {
			throw new RuntimeException("Cannot read: " + filename, e);
		}

		for(final Object propertyObject : properties.keySet()) {
			final String property = propertyObject.toString();
			if(property.startsWith(LEADER) && property.endsWith(DRIVER)) {
				final int length = property.length();
				final String key = property.substring(LEADER_LENGTH, length - DRIVER_LENGTH);

				final String comment = properties.getProperty(LEADER + key + COMMENT);
				final String driver = properties.getProperty(LEADER + key + DRIVER);
				final String url = properties.getProperty(LEADER + key + URL);
				final String user = properties.getProperty(LEADER + key + USER);
				final String password = properties.getProperty(LEADER + key + PASSWORD);
				final String[] viewLeaders = getLeaders(properties, LEADER + key + VIEW_LEADERS);
				final String[] updateLeaders = getLeaders(properties, LEADER + key + UPDATE_LEADERS);
				final Factory generator = new Factory(key, comment, driver, url, user, password, viewLeaders,
				        updateLeaders);
				if(factories.put(key, generator) != null) {
					throw new RuntimeException("Duplicate database: " + key);
				}
			}
		}
	}

	public Map<String, Factory> getFactories() {
		return factories;
	}

	public Factory getFactory(HttpServletRequest request, String db) {
		final Factory result;
		if(db.equals(DEMO_DB)) {
			// The demo DB is handled differently...
			result = handleDemoDb(request);
		} else {
			result = factories.get(db);
			if(result == null) {
				throw new RuntimeException("No such db: " + db);
			}
		}
		return result;
	}

	public Source getSource(HttpServletRequest request, String db) {
		return getFactory(request, db).getSource();
	}

	private String[] getLeaders(Properties properties, String key) {
		final String viewlist = properties.getProperty(key);
		if(viewlist == null) {
			throw new RuntimeException("Missing property in Data* configuration: " + key);
		}
		return viewlist.toUpperCase().split(",");
	}

	private static final int[] prices = new int[] { 245, 319, 409, 475, 229 };

	private Factory handleDemoDb(HttpServletRequest request) {
		final HttpSession session = request.getSession();
		// Get the factory...
		Factory factory = (Factory)session.getAttribute(SP);
		if(factory == null) {
			// No factory for this session, create one...

			final String[] viewLeaders = { "SELECT" };
			final String[] updateLeaders = { "INSERT", "UPDATE" };
			factory = new Factory("demo", "Demo database", "org.hsqldb.jdbcDriver",
			        // "jdbc:hsqldb:mem:" + session.getId(),
			        inMemory ? "jdbc:hsqldb:mem:" + session.getId() : "jdbc:hsqldb:hsql://localhost/demo", "sa", "",
			        viewLeaders, updateLeaders);

			// Now create the database...
			final Source source = factory.getSource();

			// Execute the WEB-INF/createDemoDb.sql
			final String filename = home + "WEB-INF/CreateDemoDb.sql";
			try {
				final StringBuilder sb = new StringBuilder();
				final BufferedReader in = new BufferedReader(new FileReader(filename));
				while(true) {
					String s = in.readLine();
					if(s == null) {
						break;
					}
					final String trimmed = s.trim();
					if(trimmed.length() == 0 || trimmed.startsWith("--")) {
						continue;
					}
					sb.append(trimmed);
					if(trimmed.endsWith(";")) {
						final String sql = sb.toString();
						source.update(sql);
						sb.setLength(0);
					} else {
						sb.append(' ');
					}
				}
				in.close();
			} catch(final Exception e) {
				throw new RuntimeException("Error running createDemoDb.sql", e);
			}

			// Now generate a bunch of random orders for 2008 (a leap year: 366
			// days)...
			final Random random = new Random();
			final Calendar calendar = Calendar.getInstance();
			calendar.set(2008, 0, 1);
			final long jan1 = calendar.getTimeInMillis();

			for(int orderId = 1; orderId <= 100; orderId++) {
				final int employeeId = random.nextInt(5) + 1;
				final int customerId = random.nextInt(5) + 1;
				final java.sql.Date date = new Date(jan1 + random.nextInt(366) * DAY_MS);

				source.update("INSERT INTO orders(id,date,employeeId,customerId)VALUES(" + orderId + ",'" + date + "',"
				        + employeeId + ',' + customerId + ");");
				for(int p = 0; p < 5; p++) {
					final int quantity = random.nextInt(5);
					final int productId = random.nextInt(5) + 1;
					final int price = prices[productId - 1];
					if(quantity > 0) {
						source.update("INSERT INTO lineitems(orderId,productId,price,quantity)VALUES(" + orderId + ','
						        + productId + ',' + price + ',' + quantity + ");");
					}
				}
			}
			session.setAttribute(SP, factory);

			// Now fill the team table...
			// INSERT INTO team(id,firstName,lastName)VALUES(1,'Linda','Young');
			final Iterator<FullName> names = new RandomNames().iterator();
			for(int i = 0; i < 15; i++) {
				final StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO team(id,firstName,lastName)VALUES(");
				sb.append(i + 1);
				sb.append(",'");
				final FullName name = names.next();
				sb.append(name.firstName);
				sb.append("','");
				sb.append(name.lastName);
				sb.append("')");
				source.update(sb.toString());
			}

			// Now create a set of inquiries...
			// INSERT INTO
			// inquiries(id,received,category,state,customerName,assigneeId)VALUES(...)
			final java.util.Date today = new java.util.Date();
			final long lastMonth = new java.util.Date(today.getTime() - 31 * DAY_MS).getTime();
			for(int i = 0; i < 223; i++) {
				final String category = categories[random.nextInt(categories.length)];
				final String state = states[random.nextInt(states.length)];
				final FullName customerName = names.next();
				final int assigneedId = random.nextInt(15) + 1;
				final java.sql.Date received = new Date(lastMonth + random.nextInt(28) * DAY_MS);

				final StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO inquiries(id,received,category,state,customerName,assigneeId)VALUES(");
				sb.append(i + 1700);
				sb.append(",'");
				sb.append(received);
				sb.append("','");
				sb.append(category);
				sb.append("','");
				sb.append(state);
				sb.append("','");
				sb.append(customerName.firstName);
				sb.append(' ');
				sb.append(customerName.lastName);
				sb.append("',");
				sb.append(assigneedId);
				sb.append(')');
				source.update(sb.toString());
			}
		}
		return factory;
	}

	private static final String[] categories = { "Complaint", "Close account", "Open account", "Transfer account",
	        "Address change", "Other", };
	private static final String[] states = { "received", "received", "active", "active", "active", "cleared" };
}