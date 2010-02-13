package com.inexas.pl.db;

import java.util.*;
import java.util.logging.*;
import com.inexas.pl.*;
import com.inexas.util.*;

/** 
 * This class is the sole point of access into the storage that ProcessLab uses
 * @author kwhittingham
 */
public class Db extends Transaction {
	static final Logger logger;
	public static boolean logging = Site.getDbLogging();
	static {
		logger = Logger.getLogger(Db.class.getName());
		final String filters = Site.getDbFilters();
		if(!(filters == null || filters.trim().length() == 0)) {
			logger.setFilter(new GrepFilter(filters));
		}
		logger.setLevel(logging ? Level.INFO : Level.OFF);
		logger.setUseParentHandlers(false);
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new LogFormatter());
		logger.addHandler(handler);
	}
	// Static data
	private static Stack<Db> freePool = new Stack<Db>(); 
	private static Set<Db> activePool = new HashSet<Db>();
	private static int ceiling = 0;
	// Non-static data for each instance

	/**
	 * Reserve an instance of of the database. It must be released afterwards
	 * by calling {@link #releaseInstance(Db)}.
	 */
	public static Db reserveInstance() throws RuntimeException {
		final Db result;
		// !todo Synchronize
		
		// Get a free connection, create one if necessary...
		if(freePool.isEmpty()) {
			result = new Db();
		} else {
			result = freePool.pop();
		}
		
		// Add to to the active pool remembering the ceiling...
		activePool.add(result);
		if(activePool.size() > ceiling) {
			ceiling = activePool.size();
		}
		
		return result;
	}
	
	public static void releaseInstance(Db db) {
		// Return the instance to the free pool
		if(activePool.remove(db)) {
			freePool.add(db);
		} else {
			throw new RuntimeException("Db not in active pool" + db);
		}
	}
	
	/** 
	 * Constructor called just-in-time
	 */
	private Db() {
		//
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
	
	@Override
    public String toString() {
		return "Db: " + Site.asString() + ", ceiling: " + ceiling;
    }
	
	public static void staticExitGracefully() {
		for(final Transaction transaction : freePool) {
			transaction.exitGracefully();
		}
		if(!activePool.isEmpty()) {
			for(final Transaction transaction : activePool) {
				transaction.exitGracefully();
			}
			throw new DbRuntimeException("Transactions not released or still busy");
		}
	}

	public void batchUpdate(String sqlKey, boolean ignoreExceptions) {
		final Transaction transaction = startTransaction();
		final String batch = getSql(sqlKey);
		String lastStatement = null;
		try {
			final StringTokenizer st = new StringTokenizer(batch, ";");
			while(st.hasMoreTokens()) {
				lastStatement = st.nextToken();
				try {
					transaction.doUpdate(lastStatement);
				} catch(final Exception e) {
					if(!ignoreExceptions) {
						throw new RuntimeException("Error executing statement: " + lastStatement, e);
					}
				}
			}
			commit(transaction);
		} catch(final Throwable e) {
			rollback(transaction);
			throw new RuntimeException("Error executing batch, last statement: " + lastStatement, e);
		}
	}

}
