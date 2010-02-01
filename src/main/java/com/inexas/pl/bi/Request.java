package com.inexas.pl.bi;

import java.sql.*;
import java.util.regex.*;
import javax.servlet.http.*;
import com.ecyrd.jspwiki.*;
import com.google.visualization.datasource.base.*;
import com.google.visualization.datasource.datatable.*;
import com.google.visualization.datasource.datatable.value.*;
import com.ibm.icu.util.*;
import com.inexas.pl.datacache.*;
import com.inexas.pl.datacache.DiskDataCache.*;
import com.inexas.pl.datasource.*;
import com.inexas.pl.datasource.SourcePool.*;
import com.inexas.pl.scheduler.*;
import com.inexas.util.*;


/**
 * This stores a single request that might be made to render
 * an object. It is stored in the requestMap until it is used
 * 
 * !todo Requests will need garbage collecting in case they are not used - how
 */
class Request implements Schedulable {
	private final static Pattern newlines = Pattern.compile("[\\r\\n]");
	private static final String AGE = "Age";
	private static final String CREATED = "Created";
	/**
	 * Requests can be given more than one life. For example if a
	 * request is used to generate a chart that has a spreadsheet
	 * download icon then the request can be used a second time.
	 */
	private int lives;
	final String db, sql, pageName, principleName;
	final int pageVersion;
	final int cached;
	final boolean isRefresh, isQuery;
	private String description;
	private Source source;

	public Request(AbstractPlugin plugin, int lives) {
		final Parameters parameters = plugin.getParameters();
		final WikiPage page = plugin.context.getPage();
		pageName = page.getName();
		pageVersion = page.getVersion();
		db = parameters.getDb();
		sql = newlines.matcher(parameters.getSql()).replaceAll(" ");
		description = db + ":" + sql;
		cached = plugin instanceof DataView ? parameters.getCached() : 0;
		isRefresh = plugin.isRefresh;
		isQuery = plugin instanceof DataView;
		principleName = plugin.context.getWikiSession().getLoginPrincipal().getName();
		this.lives = lives;
    }

    public String getDescription() {
    	// JIT
    	if(description == null) {
    		description = db + ":" + newlines.matcher(sql).replaceAll(" ");
    	}
    	return description;
    }

    public String getPageName() {
    	return pageName;
    }

    public int getPageVersion() {
    	return pageVersion;
    }


    /**
     * This is called by the scheduler. 
     */
    public void run() {
		source = SourcePool.getInstance().getSource(null, db);
		try {
			if(isQuery) {
				if(cached == 0) {
					throw new RuntimeException("Scheduled query without caching is nonsense: " + toString());
				}
				final DataTable dataTable = executeQuery();
				DiskDataCache.getInstance().put(pageName, pageVersion, sql, cached, dataTable);
			} else {
				executeUpdate();
			}
		} finally {
			source.release();
		}
    }

	private void executeUpdate() {
		source = SourcePool.getInstance().getSource(null, db);
		try {
			source.update(sql);
			PageLogger.getInstance().log(pageName, principleName, db, sql, "OK");
		} catch(final Exception e) {
			PageLogger.getInstance().log(pageName, principleName, db, sql, "ERROR: " + e.getMessage());
		} finally {
			source.release();
		}
    }

	public boolean isRefresh() {
		return isRefresh;
    }

	public String getDb() {
    	return db;
    }

	public String getSql() {
    	return sql;
    }

	@Override
    public String toString() {
		return getDescription();
    }

	public DataTable executeQuery() {
		final DataTable result = new DataTable();

		try {
			// Execute the query...
			final ResultSet rs = source.query(sql);
			final ResultSetMetaData metaData = rs.getMetaData();
			final int columnCount = metaData.getColumnCount();

			// Create the data table...

			// Do the column headers...
			for(int i = 1; i <= columnCount; i++) {
				final String name = metaData.getColumnName(i);

				final ValueType type;
				final int sqlType = metaData.getColumnType(i);
				switch(sqlType) {
				case Types.BOOLEAN:
					type = ValueType.BOOLEAN;
					break;

				case Types.DATE:
					type = ValueType.DATE;
					break;

				case Types.TIME:
					type = ValueType.TIMEOFDAY;
					break;

				case Types.TIMESTAMP:
					type = ValueType.DATETIME;
					break;

				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.REAL:
				case Types.SMALLINT:
				case Types.TINYINT:
					type = ValueType.NUMBER;
					break;

				case Types.VARCHAR:
				case Types.CHAR:
					type = ValueType.TEXT;
					break;

				default:
					throw new RuntimeException("Type not handled: " + sqlType);
				}

				final String label = AbstractPlugin.makeLabel(name);
				final ColumnDescription columnDescription = new ColumnDescription(name, type, label);
				result.addColumn(columnDescription);
			} // for each column

			// Process the table body...
			while(rs.next()) {
				final TableRow row = new TableRow();

				for(int i = 1; i <= columnCount; i++) {
					final int sqlType = metaData.getColumnType(i);
					final Value value;
					switch(sqlType) {
					case Types.BOOLEAN:
						final boolean b = rs.getBoolean(i);
						if(rs.wasNull()) {
							value = BooleanValue.getNullValue();
						} else {
							value = b ? BooleanValue.TRUE : BooleanValue.FALSE;
						}
						break;

					case Types.DATE:
						final Date date = rs.getDate(i);
						if(date == null) {
							value = DateValue.getNullValue();
						} else {
							final Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							value = new DateValue(
							        calendar.get(Calendar.YEAR),
							        calendar.get(Calendar.MONTH),
							        calendar.get(Calendar.DATE));
						}
						break;

					case Types.TIME:
						final Date time = rs.getDate(i);
						if(time == null) {
							value = TimeOfDayValue.getNullValue();
						} else {
							final Calendar calendar = Calendar.getInstance();
							calendar.setTime(time);
							value = new TimeOfDayValue(
							        calendar.get(Calendar.HOUR_OF_DAY),
							        calendar.get(Calendar.MINUTE),
							        calendar.get(Calendar.SECOND),
							        calendar.get(Calendar.MILLISECOND));
						}
						break;

					case Types.TIMESTAMP:
						final Date timeStamp = rs.getDate(i);
						if(timeStamp == null) {
							value = DateTimeValue.getNullValue();
						} else {
							final Calendar calendar = Calendar.getInstance();
							calendar.setTime(timeStamp);
							value = new DateTimeValue(
							        calendar.get(Calendar.YEAR),
							        calendar.get(Calendar.MONTH),
							        calendar.get(Calendar.DATE),
							        calendar.get(Calendar.HOUR_OF_DAY),
							        calendar.get(Calendar.MINUTE),
							        calendar.get(Calendar.SECOND),
							        calendar.get(Calendar.MILLISECOND));
						}
						break;

					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.DECIMAL:
					case Types.REAL:
						// todo Do I need to do this in a finer grain?
						final double d = rs.getDouble(i);
						if(rs.wasNull()) {
							value = NumberValue.getNullValue();
						} else {
							value = new NumberValue(d);
						}
						break;

					case Types.BIGINT:
					case Types.INTEGER:
					case Types.NUMERIC:
					case Types.SMALLINT:
					case Types.TINYINT:
						// todo Do I need to do this in a finer grain?
						final long l = rs.getLong(i);
						if(rs.wasNull()) {
							value = NumberValue.getNullValue();
						} else {
							value = new NumberValue(l);
						}
						break;

					case Types.VARCHAR:
					case Types.CHAR: {
						final String s = rs.getString(i);
						if(s == null) {
							value = TextValue.getNullValue();
						} else {
							value = new TextValue(s);
						}
						break;
					}

					default:
						throw new RuntimeException("Type not handled: " + sqlType);
					} // switch(sqlType)
					row.addCell(value);
				}

				result.addRow(row);
			}
		} catch(final SQLException e) {
			throw new RuntimeException("SQL error in statement: \"" + sql + "\", " + e.getMessage());
		} catch(final TypeMismatchException e) {
			throw new RuntimeException("Error writing result row to TableRow", e);
		} finally {
			source.release();
		}
		return result;
	}

	public DataTable getDataTable(HttpServletRequest servletRequest) {
		DataTable result;
		
		// Try the cache first...
		if(!isRefresh() && cached > 0) {
			final Cached cachedDataTable = DiskDataCache.getInstance().get(pageName, pageVersion, sql);
			if(cachedDataTable == null) {
				result = null;
			} else {
				result = cachedDataTable.getDataTable();
				final java.util.Date created = new java.util.Date(cachedDataTable.getCachedAtTime());
				final long now = new java.util.Date().getTime();
				final long age = now - cachedDataTable.getCachedAtTime();
				result.setCustomProperty(CREATED, DateUtilities.toDateTimeString(created));
				result.setCustomProperty(AGE, DateUtilities.humanReadableDuration(age));
			}
		} else {
			result = null;
		}

		if(result == null) {
			// No luck with the cache, make the query... 
			source = SourcePool.getInstance().getSource(servletRequest, db);
			result = executeQuery();
			
			// Cache the new result set if needs be...
			if(cached > 0) {
				DiskDataCache.getInstance().put(
						pageName,
						pageVersion,
						sql,
						cached,
						result);
			}
		}
		
		return result;
    }
	
	public void addLife() {
		lives++;
	}

	/**
	 * Use up a life
	 * 
	 * @return true if there are still some lives remaining
	 */
	public boolean useLife() {
		lives--;
    	return lives > 0;
    }

}