package com.inexas.pl;

import java.util.*;
import org.junit.*;

public class AbstractProcessLabTest {
	protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
	protected static final String DB_URL = "jdbc:hsqldb:mem:test";
	protected static final String DB_USERID = "sa";
	protected static final String DB_PASSWORD = "";
	protected static final String DB_LOGGING = "true";
	protected static final String DB_FILTERS = "";

	@BeforeClass
    public static void initialize() {
    	final Properties properties = new Properties();
    	properties.setProperty(Site.DB_DRIVER_NAME, DB_DRIVER);
    	properties.setProperty(Site.DB_URL, DB_URL);
    	properties.setProperty(Site.DB_USER_ID, DB_USERID);
    	properties.setProperty(Site.DB_PASSWORD, DB_PASSWORD);
    	properties.setProperty(Site.DB_LOGGING, DB_LOGGING);
    	properties.setProperty(Site.DB_FILTERS, DB_FILTERS);
    	Site.initialize(properties);
    }

}
