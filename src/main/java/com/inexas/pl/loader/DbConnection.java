package com.inexas.pl.loader;

import java.sql.*;
import java.util.*;
import java.util.Date;
import com.inexas.util.*;

/**
 * @author KeithWhittingham
 */
public class DbConnection {
	private final TextBundle sqlBundle;
	private final Connection connection;
	private int transactionDepth;

	DbConnection(TextBundle sqlBundle, Connection connection) {
		this.sqlBundle = sqlBundle;
		this.connection = connection;
	}

	/**
	 * Create a new transaction
	 */
	public void start() {
		try {
			if(transactionDepth++ == 0) {
				connection.setAutoCommit(false);
			}
		} catch(SQLException e) {
			throw new RuntimeException("tx failure", e);
		}
	}

	/**
	 * Commit the current transaction.
	 */
	public void commit() {
		try {
			if(--transactionDepth == 0) {
				connection.commit();
				connection.setAutoCommit(true);
			}
		} catch(SQLException e) {
			throw new RuntimeException("tx failure", e);
		}
	}

	public void rollback() {
		try {
			connection.rollback();
			connection.setAutoCommit(true);
			transactionDepth = 0;
		} catch(SQLException e) {
			throw new RuntimeException("tx failure", e);
		}
	}

	/**
	 * Execute a batch of SQL statements loaded from the SQL TextBundle. The string
	 * in the file is a list of statements separated by and optionally terminated by
	 * semicolons. 
	 * 
	 * @param sqlKey the key of the SQL batch in the properties file
	 * @param ignoreExceptions if true then all the statements are executed regardless of exceptions
	 */
	public void executeBatch(String sqlKey, boolean ignoreExceptions) {
		final String batch = sqlBundle.getString(sqlKey);
		try {
			final Statement statement = connection.createStatement();
			final StringTokenizer st = new StringTokenizer(batch, ";");
			while(st.hasMoreTokens()) {
				final String sql = st.nextToken();
				try {
					if(Database.logging) {
						Database.logger.info(sql);
					}
					statement.executeUpdate(sql);
				} catch(final SQLException e) {
					if(!ignoreExceptions) {
						rollback();
						throw new RuntimeException("Error executing statement: " + sql, e);
					}
				}
			}
			statement.close();
		} catch(SQLException e) {
			throw new RuntimeException("Error executing batch: " + sqlKey, e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Execute a query that can contain arguments. The arguments are
	 * in pairs and contain strings that will be substituted into the
	 * SQL text.
	 * 
	 * selectRecord=SELECT * FROM {tableName} WHERE id={id}
	 * executeQuery("selectRecord", "{id}", "21", "{tableName}", "myTable")
	 * 
	 * @param sqlKey
	 * @param parameters
	 * @return
	 */
	public ResultSet query(String sqlKey, String...  parameters) {
		final String sql = loadAndParameterize(sqlKey, parameters);
        if(Database.logging) {
        	Database.logger.info("Qry (" + sqlKey + "): " + sql);
        }
		return executeSqlQuery(sql);
	}
	
	public ResultSet executeQuery(String sqlKey) {
        final String sql = sqlBundle.getString(sqlKey);
        if(Database.logging) {
        	Database.logger.info("Qry (" + sqlKey + "): " + sql);
        }
        return executeSqlQuery(sql);
	}
	
	public ResultSet executeSqlQuery(String sql) {
        final Statement statement = getStatement();
		try {
	        final ResultSet resultSet = statement.executeQuery(sql);
	        return resultSet;
		} catch(final SQLException e) {
			throw new RuntimeException("Error executing query: " + sql, e);
        } finally {
        	close(statement);
        }
	}

	/**
	 * Execute an update that can contain arguments. See String.format() for the 
	 * formatting controls
	 * 
	 * @param sqlKey
	 * @param args
	 * @return
	 */
	public void update(String sqlKey, String...  parameters) {
		final String sql = loadAndParameterize(sqlKey, parameters);
		executeSqlUpdate(sql);
	}
	
	public void executeUpdate(String sqlKey) {
		final String sql = sqlBundle.getString(sqlKey);
		executeSqlUpdate(sql);
	}

	public void executeSqlUpdate(String sql) {
		if(Database.logging) {
			Database.logger.info("Upd " + sql);
		}
        final Statement statement = getStatement();
		try {
	        statement.executeUpdate(sql);
		} catch(final SQLException e) {
			throw new RuntimeException("Error executing update: " + sql, e);
        } finally {
        	close(statement);
        }
	}

	public void createSequence(String key) throws Exception {
		update("sequenceCreateSequence", key);
	}
	
	/**
	 * This method gets the next sequence number and updates the nextId in the
	 * sequence table. It should be called in a transaction so that it can
	 * be rolled back if the ID is not used
	 * 
	 * @param key the name of the sequence - as a convention use the name
	 * from YourClass.class.getName()
	 * 
	 * @return the next ID
	 * @throws RuntimeException
	 */
	public synchronized int getNextId(String key) {
		assert key != null && key.length() > 0 && key.length() <= 256: "Invalid sequence name: " + key;
		
		try {
	        // SELECT nextId FROM sequence WHERE key={key}
	        final ResultSet rs = query(
	        		"sequenceSelect",
	        		"{key}",  '\'' + key + '\'');
	        rs.next();
	        final int nextId = rs.getInt(1);
	        
	        // UPDATE sequence SET nextId={nextId} WHERE key={key}
	        update(
	        		"sequenceUpdate",
	        		"{key}", '\'' + key + '\'',
	        		"{nextId}", Integer.toString(nextId+1));
	        return nextId;
        } catch(final SQLException e) {
        	throw new RuntimeException("Error generating sequence number for: " + key, e);
        }
	}
	
	private Statement getStatement() {
		try {
	        return connection.createStatement();
        } catch(final SQLException e) {
        	throw new RuntimeException("Error loading connection", e);
        }
    }

	private void close(Statement statement) {
		try {
	        statement.close();
        } catch(final SQLException e) {
        	throw new RuntimeException("Error closing statement", e);
        }
    }

	private String loadAndParameterize(String sqlKey, String[] parameters) {
		assert parameters.length > 0 && parameters.length % 2 == 0 : "Next 2, 4, 6... parameters";
		assert parametersExist(sqlKey, parameters);
		String result = sqlBundle.getString(sqlKey);
		for(int i = 0; i < parameters.length; ) {
			final String name = parameters[i++];
			final String value = parameters[i++];
			result = result.replace(name, value);
		}
		return result;
    }

	/**
	 * This method checks that there is at least one occurrence of each parameter
	 * in the SQL string. It is only called by an assert
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	private boolean parametersExist(String sqlKey, String[] parameters) {
		final String sql = sqlBundle.getString(sqlKey);
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < parameters.length; i += 2) {
			final String parameterName = parameters[i];
			if(!(parameterName.startsWith("{") && parameterName.endsWith("}"))) {
				throw new RuntimeException("Parameter name missing {Brackets}: " + parameterName);
			}
			if(sql.indexOf(parameterName) < 0) {
				if(sb.length() > 0) {
					sb.append(',');
				}
				sb.append(parameterName);
			}
		}
		if(sb.length() > 0) {
			throw new RuntimeException(
					"There are missing parameters in SQL template, (sqlKey:sqltemplate:missing-parameters): " +
					sqlKey + ':' + sql + ':' + sb.toString());
		}
	    return true;
    }

	public String getSql(String key) {
	    return sqlBundle.getString(key);
    }

	public static String formatAsString(String string) {
		return string == null ? "NULL" : '\'' + string + '\'';
    }

	public static String formatAsTimestamp(Date date) {
		final String result;
		if(date == null) {
			result = "NULL";
		} else {
			final Timestamp timestamp = new Timestamp(date.getTime());
		    result = '\'' + timestamp.toString() + '\'';
		}
		return result;
    }

	public static String formatAsCharacter(char c) {
		final StringBuilder sb = new StringBuilder();
		sb.append('\'');
		sb.append(c);
		sb.append('\'');
		return sb.toString();
    }

}
