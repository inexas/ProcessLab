package com.inexas.pl.db;

import java.sql.*;
import java.util.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

/**
 * Divide and conquer! To simplify the logic here we deal with a group at a time
 * and set up a bag of tricks (BOTs) to perform all the operations on a that
 * group.
 */
public class RelationalEntityProvider implements EntityProvider {
	private final int MAX_NAME_LENGTH, PART1LENGTH, PART2LENGTH;
	private final Map<String, String> reservedNames = new HashMap<String, String>();
	private final Map<String, String> tableNameMap = new HashMap<String, String>();
	private final BasicCache<String, BagOfTricks> bots = new BasicCache<String, BagOfTricks>();

	public RelationalEntityProvider() {
		// Load the reserved names...
		final Db db = Db.reserveInstance();
		final String reservedNameList = db.getSql("reservedNames");
		final StringTokenizer parser = new StringTokenizer(reservedNameList, ",");
		while(parser.hasMoreTokens()) {
			reservedNames.put(parser.nextToken(), null);
		}

		MAX_NAME_LENGTH = Integer.parseInt(db.getSql("maximumNameLength"));
		PART1LENGTH = MAX_NAME_LENGTH / 2 -1;
		PART2LENGTH = MAX_NAME_LENGTH - PART1LENGTH - 1;
		
		Db.releaseInstance(db);
	}

	public void install(Map<String, EntityType> entityTypes) {
		// Create the tables for all relational tuples...
		final Db db = Db.reserveInstance();
		try {
	        final ResultSet rs = db.query("tupleTypeReadRelationalEntities");
	        while(rs.next()) {
	        	final String key = rs.getString("key");
	        	final EntityType entityType = entityTypes.get(key);
	        	final BagOfTricks bot = getBot(db, entityType);
	        	bot.createTables(entityType, db);
	        }
        } catch(final SQLException e) {
        	throw new RuntimeException("Error installing RelationalEntityProvider", e);
        } finally {
        	Db.releaseInstance(db);
        }
        
	}
	
	/**
	 * Create a new Entity
	 */
	public Entity createInstance(EntityType entityType) {
		final Entity result = new Entity(entityType);
		final Db db = Db.reserveInstance();
		try {
	        final Transaction transaction = db.startTransaction();
	        final BagOfTricks bot = getBot(db, entityType);
	        bot.insertRecursively(result, db);
	        db.commit(transaction);
        } catch(final SQLException e) {
        	throw new RuntimeException("Error creating entity type: " + entityType);
        } finally {
        	Db.releaseInstance(db);
        }
		return result;
	}

	public Entity read(EntityType entityType, int id) {
		final Entity result = new Entity(entityType);
		result.setId(id);
		final Db db = Db.reserveInstance();
		try {
	        getBot(db, entityType);
        } catch(SQLException e) {
        	throw new RuntimeException("Error reading entity type: " + id, e);
        } finally {
        	Db.releaseInstance(db);
        }
		return result;
	}

	public void update(Entity entity) {
		assert entity != null;

		final Db db = Db.reserveInstance();
		try {
	        final Transaction transaction = db.startTransaction();
	        
	        final BagOfTricks bot = getBot(db, entity.getEntityType());
	        bot.updateRecursively(entity, transaction);
	        
	        db.commit(transaction);
        } catch(final SQLException e) {
        	throw new RuntimeException("Error updating entity: " + entity, e);
        } finally {
        	Db.releaseInstance(db);
        }
	}

	public void delete(Entity entity) {
		assert entity != null;

		final Db db = Db.reserveInstance();
		try {
	        final Transaction transaction = db.startTransaction();
	        
	        final BagOfTricks bot = getBot(db, entity.getEntityType());
	        bot.deleteRecursively(entity, db);
	        
	        db.commit(transaction);
        } catch(final SQLException e) {
        	throw new RuntimeException("Error deleting entity: " + entity, e);
        }
		Db.releaseInstance(db);
	}

	private BagOfTricks getBot(Transaction transaction, TupleType tupleType) throws SQLException {
		final String key = tupleType.getFullPath();
		BagOfTricks result = bots.get(key);
		if(result == null) {
			// Not in the cache then build it...
			result = new BagOfTricks(transaction, tupleType);
			bots.put(key, result);
		}
		return result;
	}

	/**
	 * A bag of tricks class is built for each tuple. It holds a bunch of
	 * pre-initialized code and SQL statements ready to perform the persistence
	 * operations.
	 */
	private class BagOfTricks {
		private final String tableName;
		private final Map<String, String> columnNameMap = new HashMap<String, String>();

		/**
		 * @param tupleType
		 *            for this tuple type
		 * @param sqlNameLookup
		 *            the SQL name lookup map
		 * @throws SQLException 
		 * @throws SQLException
		 *             in the event of a SQL failure
		 */
		BagOfTricks(Transaction transaction, TupleType tupleType) throws SQLException {
			tableName = generateTableName(transaction, tupleType);

			// Create the column name map...
			final Iterator<KtcvType<?>> ktcvs = tupleType.getKtcvTypeMemberIterator();
			while(ktcvs.hasNext()) {
				final KtcvType<?> ktcv = ktcvs.next();
				final String ktcvKey = ktcv.getKey();
				unique(columnNameMap, truncate(ktcvKey, MAX_NAME_LENGTH));
			}
		}

		/**
		 * Recursively insert a tuple and it's children
		 * 
		 * @param tuple
		 * @throws SQLException 
		 */
		void insertRecursively(Tuple tuple, Transaction transaction) throws SQLException {
			// INSERT INTO {TableName}(Rsvd_id{NonRootFieldNames}{Names})
			// VALUES({Id}{NonRootFields}{Values})
			final int id = getNextId(transaction, tuple.getEntity().getEntityType());
			tuple.setId(id);
			tuple.setClean();

			final String nonRootFieldNames, nonRootFields;
			if(tuple instanceof Entity) {
				nonRootFieldNames = "";
				nonRootFields = "";
			} else {
				nonRootFieldNames = ",Rsvd_pid,Rsvd_ord";
				final StringBuilder sb = new StringBuilder();
				sb.append(',');
				sb.append(Integer.toString(tuple.getParentTuple().getId()));
				sb.append(',');
				sb.append(Integer.toString(tuple.getOrdinal()));
				nonRootFields = sb.toString();
			}

			final StringBuilder names = new StringBuilder();
			final StringBuilder values = new StringBuilder();
			final Iterator<Ktcv<?>> ktcvs = tuple.getKtcvIterator();
			while(ktcvs.hasNext()) {
				final Ktcv<?> ktcv = ktcvs.next();
				if(ktcv.isDirty()) {
					names.append(',');
					names.append(columnNameMap.get(ktcv.getKey()));
					values.append(',');
					values.append(getSqlValue(ktcv));
					ktcv.setClean();
				}
			}

			transaction.update(
					"tupleInsert",
					"{TableName}", tableName,
					"{NonRootFieldNames}", nonRootFieldNames,
					"{Names}", names.toString(),
					"{Id}", Integer.toString(id),
			        "{NonRootFields}", nonRootFields,
			        "{Values}", values.toString());

			// Now insert the child tuple lists recursively...
			final Iterator<TupleList> tupleLists = tuple.getTupleListIterator();
			while(tupleLists.hasNext()) {
				final TupleList tupleList = tupleLists.next();
				final BagOfTricks bot = getBot(transaction, tupleList.getType());
				for(final Tuple childTuple : tupleList) {
					bot.insertRecursively(childTuple, transaction);
				}
			}
		}

		void updateRecursively(Tuple tuple, Transaction transaction) throws SQLException {
			// If the ID is 0 then we need to insert else we do an update...
			if(tuple.getId() == 0) {
				insertRecursively(tuple, transaction);
			} else {
				// Write the dirty KTCVs if any...
				// UPDATE {TableName} SET {NameValueList} WHERE id={Id}
				final StringBuilder nameValueList = new StringBuilder();
				String delimiter;
				if(tuple.isDirty()) {
					nameValueList.append("Rsvd_ord=");
					nameValueList.append(tuple.getOrdinal());
					delimiter = ",";
				} else {
					delimiter = "";
				}
				final Iterator<Ktcv<?>> ktcvs = tuple.getKtcvIterator();
				while(ktcvs.hasNext()) {
					final Ktcv<?> ktcv = ktcvs.next();
					if(ktcv.isDirty()) {
						nameValueList.append(delimiter);
						delimiter = ",";
						nameValueList.append(columnNameMap.get(ktcv.getKey()));
						nameValueList.append("=");
						nameValueList.append(getSqlValue(ktcv));
						ktcv.setClean();
					}
				}
				if(nameValueList.length() > 0) {
					transaction.update(
							"tupleUpdate",
							"{Id}", Integer.toString(tuple.getId()),
							"{TableName}", tableName,
							"{NameValueList}", nameValueList.toString());
				}
				
				// Visit the tuple lists...
				final Iterator<TupleList> tupleLists = tuple.getTupleListIterator();
				while(tupleLists.hasNext()) {
					final TupleList tupleList = tupleLists.next();
					final BagOfTricks bot = getBot(transaction, tupleList.getType());
					bot.updateRecursively(tupleList, transaction);
				}
			}
        }

		private void updateRecursively(TupleList tupleList, Transaction transaction) throws SQLException {
			// Delete any deleted tuples...
			// DELETE FROM {TableName} WHERE id IN({TupleIdList})
			final List<Tuple> deletedTuples = tupleList.getDeletedTuples();
			if(deletedTuples.size() > 0) {
				final StringBuilder tupleIdList = new StringBuilder();
				String delimiter = "";
				for(final Tuple tuple : deletedTuples) {
					tupleIdList.append(delimiter);
					delimiter = ",";
					tupleIdList.append(tuple.getId());
				}
				transaction.update("tupleDeleteList",
						"{TableName}", tableName,
						"{TupleIdList}", tupleIdList.toString());
				deletedTuples.clear();
			}
			
			// Now visit the tuples...
			for(final Tuple tuple : tupleList) {
				updateRecursively(tuple, transaction);
			}
			
        }

		private String generateTableName(Transaction transaction, TupleType tupleType) throws SQLException {
			String result;
			// SELECT tableName FROM tupleType WHERE id={Id}
	        final ResultSet rs = transaction.query(
	        		"tupleGetTableName",
	        		"{Id}", Integer.toString(tupleType.getId()));
	        rs.next();
	        result = rs.getString("tableName");
			
			if(result == null) {
				// We need to generate one...
				final String path = tupleType.getFullPath();
				final String entityTypeKey = "XXXX";//tupleType.getDataType().getKey();
				if(tupleType instanceof EntityType) {
					result = entityTypeKey.toUpperCase();
				} else {
					final String tupleTypeKey = tupleType.getKey();
					result = 
						truncate(entityTypeKey, PART1LENGTH).toUpperCase() +
						'_' +
						truncate(tupleTypeKey, PART2LENGTH).toUpperCase();
				}
				int unique = 0;
				while(reservedNames.containsKey(result) || tableNameMap.containsValue(result)) {
					final String uniqueText = Integer.toString(unique);
					result = result.substring(0, MAX_NAME_LENGTH - uniqueText.length()) + uniqueText;
					unique++;
				}
				tableNameMap.put(path, result);
	    		// UPDATE tuple SET tableName={TableName} WHERE id={Id}
				transaction.update(
	    				"tupleSetTableName",
	    				"{Id}", Integer.toString(tupleType.getId()),
	    				"{TableName}", '\'' + result + '\'');
			}
			return result;
	    }

		private String truncate(String string, int truncateLength) {
			final int currentLength = string.length();
			return string.substring(0, currentLength < truncateLength ? currentLength : truncateLength);
		}

		void createTables(EntityType entityType, Transaction transaction) throws SQLException {
			createTables((TupleType)entityType, transaction);
		}
		
		private void createTables(TupleType tuple, Transaction transaction) throws SQLException {
			// CREATE TABLE {TableName}(\
			// Rsvd_id INT PRIMARY KEY\
			// {NonRootColumns}\
			// {KtcvColumns}\
			final String nonRootColumns;
			if(tuple instanceof EntityType) {
				nonRootColumns = "";
			} else {
				nonRootColumns = ",Rsvd_Pid INT NOT NULL,Rsvd_Ord INT NOT NULL";
			}
			final StringBuilder ktcvColumns = new StringBuilder();
			final Iterator<KtcvType<?>> ktcvs = tuple.getKtcvTypeMemberIterator();
			while(ktcvs.hasNext()) {
				final KtcvType<?> ktcv = ktcvs.next();
				ktcvColumns.append(',');
				ktcvColumns.append(columnNameMap.get(ktcv.getKey()));
				ktcvColumns.append(' ');
				final String dataType = transaction.getSql("sqlType_" + ktcv.getDataType().asText);
				ktcvColumns.append(dataType);
			}

			transaction.update(
					"tupleTypeCreateTable",
					"{TableName}", tableName,
					"{NonRootColumns}", nonRootColumns,
					"{KtcvColumns}", ktcvColumns.toString());

			// Now create the child tuples recursively...
			final Iterator<TupleType> tupleLists = tuple.getTupleTypeMemberIterator();
			while(tupleLists.hasNext()) {
				final TupleType childTuple = tupleLists.next();
				final BagOfTricks bot = getBot(transaction, childTuple);
				bot.createTables(childTuple, transaction);
			}
		}

		private String unique(Map<String, String> usedNameMap, String string) {
			String result = string.toUpperCase();
			int unique = 0;
			while(reservedNames.containsKey(result) || usedNameMap.containsValue(result)) {
				final String uniqueText = Integer.toString(unique);
				result = result.substring(0, MAX_NAME_LENGTH - uniqueText.length()) + uniqueText;
				unique++;
			}
			usedNameMap.put(string, result);
			return result;
		}

		/**
		 * Delete a single tuple. It's children have been deleted
		 */
		void delete(Tuple tuple, Transaction transaction) {
			transaction.update(
					"tupleDelete",
					"{TableName}", tableName,
					"{Id}", Integer.toString(tuple.getId()));
		}

		/**
		 * Depth first delete of a tuple and all its children
		 */
		private void deleteRecursively(Tuple tuple, Transaction transaction) throws SQLException {

			// Women and children first...
			final Iterator<TupleList> tupleLists = tuple.getTupleListIterator();
			while(tupleLists.hasNext()) {
				final TupleList childTupleList = tupleLists.next();
				for(final Tuple childTuple : childTupleList) {
					deleteRecursively(childTuple, transaction);
				}
			}

			final BagOfTricks bot = getBot(transaction, tuple.getTupleType());
			bot.delete(tuple, transaction);
		}

		private int getNextId(Transaction transaction, EntityType entity) throws SQLException {
			// SELECT nextId FROM entity WHERE id={Id}
			final ResultSet rs = transaction.query(
					"entityGetNextId",
					"{Id}", Integer.toString(entity.getId()));
			rs.next();
			final int result = rs.getInt(1);
			transaction.update("entitySetNextId", "{Id}", Integer.toString(entity.getId()),
			        "{NextId}", Integer.toString(result + 1));
			return result;
		}
		
		private String getSqlValue(Ktcv<?> ktcv) {
			final String result;
			final String string = ktcv.getValueAsString();
			if(string == null) {
				result = "NULL";
			} else if(ktcv.getType().getDataType() == DataType.STRING) {
				result = '\'' + string + '\'';
			} else {
				result = string;
			}
			return result;
		}

	}

}
