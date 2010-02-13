package com.inexas.pl.loader;

import java.lang.ref.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import com.inexas.pl.db.*;
import com.inexas.util.*;

/** 
 * A connection factory that does pooling.
 * 
 * The configuration is passed via a Config object, we expect the following
 * - home: the home directory to find files terminated with a /
 * - db.driver
 * - db.url
 * - db.user
 * - db.password
 * - db.sql: the TextBundle file to load SQL statements from
 *  
 * @author KeithWhittingham
 */
public class Database {
	static Logger logger = Logger.getLogger(Database.class.getName());
	public static boolean logging = false;
	private TextBundle sqlBundle;
	private static Database instance;
	private List<Connection> freePool = new ArrayList<Connection>(); 
	private List<ConnectionHolder> activePool = new ArrayList<ConnectionHolder>(); 
	private String url, user, password;
	private int ceiling;

	/**
	 * Get the singleton Database instance. 
	 * 
	 * @return the singleton Database instance
	 * @throws RuntimeException if for some reason the connection cannot be established
	 */
	public static Database getInstance() throws RuntimeException {
		if(instance == null) {
			instance = new Database();
		}
		return instance;
	}
	
	/**
	 * The next call to get instance will return a new instance (note you
	 * will need to call getInstance(Config) first
	 */
	public static void reset() {
		instance = null;
	}
	
	/** 
	 * Singleton constructor
	 * 
	 * @throws RuntimeException
	 */
	private Database() throws RuntimeException {
		// load config...
		final Config config = Config.instance;
		final String sqlFileName = config.getString("db.sql");
		final String driver = config.getString("db.driver");
		url = config.getString("db.url");
		user = config.getString("db.user");
		password = config.getString("db.password");
		logging = config.getBoolean("db.logging");
		final String filters = config.getString("db.logging.filters");
		if(!(filters == null || filters.trim().length() == 0)) {
			logger.setFilter(new GrepFilter(filters));
		}
		
		// load the driver...
		try {
			Class.forName(driver).newInstance();
			// drop a connection into the free pool to test the
			// rest of the parameters...
			freePool.add(DriverManager.getConnection(url, user, password));
			ceiling = 1;
		} catch(final Exception e) {
			throw new RuntimeException(
					"Error connecting to database: "
					+ driver + "," + url + "," + user, e);
		}
		
		// load the text bundle
		sqlBundle = new TextBundle(sqlFileName);
	}
	
	/** 
	 * Get a new connection
	 * 
	 * @return a new connection
	 */
	public synchronized DbConnection getDbConnection() {
		Connection connection;
		
		// try and free some connections up if there's none available...
		int size = freePool.size();
		if(size == 0) {
			// garbage collection to collect the free connections...
			System.gc();
			
			// remove freed up connections from the active pool...
			for(ConnectionHolder ch : activePool) {
				if(ch.get() == null) {
					try {
						// get and reset the connection...
						connection = ch.getConnection();
						connection.rollback();
						connection.setAutoCommit(true);
						
						// move it to the free pool...
						activePool.remove(ch);
						freePool.add(connection);
						break;
					} catch (SQLException e) {
						throw new RuntimeException("Error clearing autocommit", e);
					}
				}
			}
			size = freePool.size();
		}
		
		if(size == 0) {
			// still none then we need to construct a new one...
			try {
				connection = DriverManager.getConnection(url, user, password);
				ceiling++;
				assert connection.getAutoCommit() == true;
			} catch (SQLException e) {
				throw new RuntimeException("Error getting connection", e);
			}
		} else {
			// recycle connection... 
			size--;
			connection = freePool.get(size);
			freePool.remove(size);
		}
		
		DbConnection dbc = new DbConnection(sqlBundle, connection);
		activePool.add(new ConnectionHolder(dbc, connection));
		return dbc;
	}

	/** 
	 * Return the maximum number of connections allocated during the
	 * life of the program
	 * 
	 * @return the maximum number of connections allocated
	 */
	public int getCeiling() {
		return ceiling;
	}
	
	/**
	 * A class that associates a JDBC connection to a DbConnection so
	 * that it can be recycled
	 *   
	 * @author KeithWhittingham
	 */
	private class ConnectionHolder extends WeakReference<DbConnection> {
		private Connection connection;

		public ConnectionHolder(DbConnection dbConnection, Connection connection) {
			super(dbConnection);
			this.connection = connection;
		}
		
		public Connection getConnection() {
			return connection;
		}
	}

	@Override
    public String toString() {
		return "Database: " + url;
    }

}
