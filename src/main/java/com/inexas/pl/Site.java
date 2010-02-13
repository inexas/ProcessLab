package com.inexas.pl;

import java.io.*;
import java.util.*;
import javax.servlet.*;

/**
 * Site holds the ProcessLab's global configuration.
 */
public class Site {
	public static final String PROCESSLAB_CONFIG = "/WEB-INF/processlab.properties";
	public static final String DEFAULT_SQL_BUNDLE_BASE_NAME = "sql";
	static final String DB_DRIVER_NAME = "db.driver.name";
	static final String DB_URL = "db.url";
	static final String DB_USER_ID = "db.user.id";
	static final String DB_PASSWORD = "db.password";
	static final String DB_SQL_BUNDLE_NAME = "db.sql.bundle.name";
	static final String DB_LOGGING = "db.logging";
	static final String DB_FILTERS = "db.filters";
	private static String dbDriverName, dbUrl, dbUserId, dbPassword, dbSqlBundleName, dbFilters;
	private static boolean dbLogging;
	private static boolean loaded = false;
	
	/**
	 * Initialize as web app
	 */
	public static void initialize(ServletContext context) {
		checkNotLoaded();
		try {
	        final InputStream is = context.getResourceAsStream(PROCESSLAB_CONFIG);
	        final Properties properties = new Properties(); 
	        properties.load(is);
	        initialize(properties);
	        is.close();
        } catch(final IOException e) {
        	throw new RuntimeException("Error loading properties file: " + PROCESSLAB_CONFIG, e);
        }
	}
	
	/**
	 * This can be called in unit tests or by the other initializers
	 * @param properties
	 */
	public static void initialize(Properties properties) {
		checkNotLoaded();
		dbDriverName = getMandatoryProperty(properties, DB_DRIVER_NAME);
		dbUrl = getMandatoryProperty(properties, DB_URL);
		dbUserId = getMandatoryProperty(properties, DB_USER_ID);
		dbPassword = getMandatoryProperty(properties, DB_PASSWORD);
		dbSqlBundleName = properties.getProperty(DB_SQL_BUNDLE_NAME, DEFAULT_SQL_BUNDLE_BASE_NAME);
		dbLogging = getBooleanProperty(properties, DB_LOGGING, "false");
		dbFilters = properties.getProperty(DB_FILTERS, null);
		loaded = true;
	}

	private static boolean getBooleanProperty(Properties properties, String key, String defaultValue) {
		final String stringValue = properties.getProperty(key, defaultValue);
		return Boolean.parseBoolean(stringValue);
    }

	private static String getMandatoryProperty(Properties properties, String key) {
		final String result = properties.getProperty(key);
		if(result == null) {
			throw new RuntimeException("Missing property: " + key + " in " + PROCESSLAB_CONFIG);
		}
		return result;
    }

	private static void checkNotLoaded() {
		if(loaded) {
			throw new RuntimeException("Site already loaded: ");
		}
    }

	public static String getDbDriverName() {
		return dbDriverName;
	}

	public static String getDbUrl() {
		return dbUrl;
	}

	public static String getDbUserId() {
		return dbUserId;
	}

	public static String getDbPassword() {
		return dbPassword;
	}

	public static String asString() {
		return dbDriverName + '/' + dbUrl + "; pw=" + dbUserId;
    }

	public static String getDbSqlBundleName() {
		return dbSqlBundleName;
    }

	public static boolean getDbLogging() {
		return dbLogging;
    }

	public static String getDbFilters() {
		return dbFilters;
    }

}
