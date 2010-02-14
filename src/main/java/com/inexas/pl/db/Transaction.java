package com.inexas.pl.db;

import java.sql.*;
import com.inexas.pl.*;
import com.inexas.util.*;

public class Transaction {
	private final Transaction parent;
	private final Connection connection;
	private final TextBundle sqlBundle;

	/**
	 * Constructor used only by Db
	 */
	protected Transaction() {
		this.parent = null;
		try {
			Class.forName(Site.getDbDriverName()).newInstance();
			connection = DriverManager.getConnection(Site.getDbUrl(), Site.getDbUserId(), Site.getDbPassword());
			sqlBundle = new TextBundle(Site.getDbSqlBundleName());
		} catch(final Exception e) {
			throw new DbRuntimeException("Error creating connection: " + Site.asString(), e);
		}
	}

	/**
	 * Constructor used by nested transactions
	 * @param parent
	 */
	private Transaction(Transaction parent) {
		this.parent = parent;
		connection = parent.connection;
		sqlBundle = parent.sqlBundle;
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
        if(Db.logging) {
        	Db.logger.info("U: " + sqlKey);
        }
		doUpdate(loadAndParameterize(sqlKey, parameters));
	}
	
	void doUpdate(String sql) {
		if(Db.logging) {
			Db.logger.info("U: " + sql);
		}
		try {
			final Statement statement = connection.createStatement();
	        statement.executeUpdate(sql);
	        statement.close();
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error executing: " + sql, e);
        }
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
        if(Db.logging) {
        	Db.logger.info("Q: " + sqlKey);
        	Db.logger.info("Q: " + sql);
        }
		try {
	        final Statement statement = connection.createStatement();
	        return statement.executeQuery(sql);
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error executing: " + sql, e);
        }
	}

	ResultSet doQuery(String sql) {
		try {
			if(Db.logging) {
				Db.logger.info("Q: " + sql);
			}
	        final Statement statement = connection.createStatement();
	        return statement.executeQuery(sql);
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error executing: " + sql, e);
        }
	}

	public Transaction startTransaction() {
		try {
	        if(parent == null) {
				if(Db.logging) {
					Db.logger.info("X: Start");
				}
	        	connection.setAutoCommit(false);
	        } else {
				if(Db.logging) {
					Db.logger.info("X: Nest");
				}
	        }
	        return new Transaction(this);
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error starting transaction", e);
        }
	}
	
	/**
	 * Commit the transaction
	 * @param transaction the transaction to commit
	 */
	public void commit(Transaction transaction) {
		try {
			if(transaction.parent != this) {
				throw new DbRuntimeException("Transaction nesting error: " + transaction);
			}
			
	        // Only if we're the root transaction
	        if(parent == null) {
				if(Db.logging) {
					Db.logger.info("X: Commit");
				}
	        	connection.commit();
	        	connection.setAutoCommit(true);
	        } else {
				if(Db.logging) {
					Db.logger.info("X: Commit unnest");
				}
	        }
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error commiting transaction", e);
        }
	}
	
	/**
	 * Roll back a transaction
	 * @param transaction the transaction to commit
	 */
	public void rollback(Transaction transaction) {
		try {
			if(transaction.parent != this) {
				throw new DbRuntimeException("Transaction nesting error: " + transaction);
			}
			
	        // Only if we're the root transaction
	        if(parent == null) {
				if(Db.logging) {
					Db.logger.info("X: Rollback");
				}
	        	connection.rollback();
	        	connection.setAutoCommit(true);
	        }
        } catch(final SQLException e) {
        	throw new DbRuntimeException("Error rolling back transaction", e);
        }
	}

	private String loadAndParameterize(String sqlKey, String[] parameters) {
		assert parameters.length > 0 && parameters.length % 2 == 0 : "Next 2, 4, 6... parameters";
		assert parametersExist(sqlKey, parameters);
		String result = getSql(sqlKey);
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
		final String sql = getSql(sqlKey);
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < parameters.length; i += 2) {
			final String parameterName = parameters[i];
			if(!(parameterName.startsWith("{") && parameterName.endsWith("}"))) {
				throw new DbRuntimeException("Parameter name missing {Brackets}: " + parameterName);
			}
			if(sql.indexOf(parameterName) < 0) {
				if(sb.length() > 0) {
					sb.append(',');
				}
				sb.append(parameterName);
			}
		}
		if(sb.length() > 0) {
			throw new DbRuntimeException(
					"There are missing parameters in SQL template, (sqlKey:sqltemplate:missing-parameters): " +
					sqlKey + ':' + sql + ':' + sb.toString());
		}
	    return true;
    }

	public String getSql(String key) {
		return sqlBundle.getString(key);
    }

	public void exitGracefully() {
		try {
			// todo Recurse through children
	        connection.close();
        } catch(SQLException e) {
        	throw new DbRuntimeException("Error exiting", e);
        }
    }

}
