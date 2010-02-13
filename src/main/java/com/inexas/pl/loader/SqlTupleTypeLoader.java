/**
 * 
 */
package com.inexas.pl.loader;

import java.sql.*;
import java.util.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public class SqlTupleTypeLoader extends Loader implements ITupleTypeLoader {
	private final String tableName;
	private final boolean relational;
	private Collection<IKtcvTypeLoader> ktcvTypeMembers = new ArrayList<IKtcvTypeLoader>();
	private Collection<ITupleTypeLoader> tupleTypeMembers = new ArrayList<ITupleTypeLoader>();

	private class SqlKtcvTypeLoader extends Loader implements IKtcvTypeLoader {
		private final String value;
		private final DataType dataType;
		private final Collection<NameValue<String>> constraints = new ArrayList<NameValue<String>>();

		public SqlKtcvTypeLoader(ResultSet rs, Crud host, ILoader parent) throws SQLException {
			super(rs.getInt("id"), rs.getString("key"), host, parent);
			value = rs.getString("value");
			dataType = DataType.getType(rs.getString("type"));
		}

		public String getValue() {
			return value;
		}

		public DataType getDataType() {
			return dataType;
		}

//		public void addConstraint(String name, String constraintValue) {
//			constraints.add(new NameValue<String>(name, constraintValue));
//		}

		public Collection<NameValue<String>> getConstraints() {
			return constraints;
		}

	}

	/**
	 * Recursively load a tuple
	 * 
	 * @param connection
	 * @param rs
	 * @throws SQLException 
	 */
	public SqlTupleTypeLoader(ResultSet rs, boolean relational, Crud host, ILoader parent) throws SQLException {
		super(rs.getInt("id"), rs.getString("key"), host, parent);
		tableName = rs.getString("tableName");
		this.relational = relational;

		// Read child KTCVs and tuples
		final ResultSet childKtcvs = Database.getInstance().getDbConnection().query(
				"tupleTypeReadKtcvs",
				"{TupleId}",
				Integer.toString(id));
		while(childKtcvs.next()) {
			ktcvTypeMembers.add(new SqlKtcvTypeLoader(childKtcvs, host, this));
		}
		childKtcvs.close();

		// Read the child tuples
		final ResultSet childTuplesResultSet = Database.getInstance().getDbConnection().query(
				"tupleTypeReadTuples",
				"{ParentId}",
		        Integer.toString(id));
		while(childTuplesResultSet.next()) {
			tupleTypeMembers.add(new SqlTupleTypeLoader(childTuplesResultSet, relational, host, this));
		}
		childTuplesResultSet.close();
	}

	public Collection<IKtcvTypeLoader> getKtcvTypeMembers() {
		return ktcvTypeMembers;
	}

	public Collection<ITupleTypeLoader> getTupleTypeMembers() {
		return tupleTypeMembers;
	}

	public boolean isRelational() {
		return relational;
	}

	public String getTableName() {
	    return tableName;
    }

}