package com.inexas.pl.db;

import java.sql.*;
import java.util.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public class RelationalSubsystem implements Subsystem {
	private final int MAX_NAME_LENGTH = 10;
	private final Map<String, String> reservedNames = new HashMap<String, String>();
	private final BasicCache<String, BagOfTricks> bots = new BasicCache<String, BagOfTricks>();

	public RelationalSubsystem() {
		// Load the reserved names...
		final Db db = Db.reserveInstance();
		final String reservedNameList = db.getSql("ReservedNames");
		final StringTokenizer parser = new StringTokenizer(reservedNameList, ",");
		while(parser.hasMoreTokens()) {
			reservedNames.put(parser.nextToken(), null);
		}

		Db.releaseInstance(db);
	}

	public void create(EntityType entity) {
		final Db db = Db.reserveInstance();
		final Transaction transaction = db.startTransaction();
    	try {
    		// Create the entry in the EntityType table...
    		
    		// EntityTypeInsert=\
    		// INSERT INTO EntityType(Id,NextId,Relational,Markup)\
    		// VALUES({Id},1,'R',{Markup})
    		final String entityKey = entity.getKey();
    		transaction.update("EntityTypeInsert",
    				"{Id}", entityKey,
    				"{Markup}", entity.toString());
    		
    		// And create the TupleTableName records recursively
    		createTuple(transaction, entity, entityKey);
        } catch(final SQLException e) {
        	db.rollback(transaction);
        	throw new DbRuntimeException("Error creating EntityType: " + entity, e);
        }
    	Db.releaseInstance(db);
    }

	private void createTuple(Transaction transaction, TupleType tuple, String entityKey)
	throws SQLException {
		// Create the TupleTableName records by trying to insert
		// a candidate until we don't get a primary key violation...

		// Build the stem: "TUPLENAME_"
		final StringBuilder sb = new StringBuilder();
		final String tupleKey;
		final String tmp = tuple.getKey();
		final int colon = tmp.lastIndexOf(':');
		if(colon > 1) {
			// It's an entity key, get the tuple key part
			tupleKey = tmp.substring(colon + 1);
		} else {
			tupleKey = tmp;
		}
		sb.append(tupleKey.toUpperCase());
		if(sb.length() > MAX_NAME_LENGTH) {
			sb.setLength(MAX_NAME_LENGTH);
		}
		sb.append('_');
		final int stemLength = sb.length();
		int disambiguator = 0;
		final String tuplePath = tuple.getFullPath();
		while(true) {
			sb.append(disambiguator);
			// TupleTableNameInsert=\
			// INSERT INTO TupleTableName(Name,OwnerKey,Path)\
			// VALUES('{Name}','{OwnerKey}','{Path}')
			try {
				final String tableName = sb.toString();
	            transaction.update("TupleTableNameInsert",
	            		"{Name}", tableName,
	            		"{OwnerKey}", entityKey,
	            		"{Path}", tuplePath);
	            // If we get here we inserted the record so the table name
	            // must be unique
	            tuple.setTableName(tableName);
	            break;
            } catch(DbRuntimeException e) {
            	// todo Can we find a way of checking for PK violations
            	if(disambiguator > 1000) {
            		throw new DbRuntimeException(
            				"Too many attempts to find an unambiguouse name, change the tuple type name");
            	}
            	disambiguator ++;
            	sb.setLength(stemLength);
            }
		}
		
		// Now recurse through child tuple types...
		final Iterator<TupleType> childTuples = tuple.getTupleTypeMemberIterator();
		while(childTuples.hasNext()) {
			final TupleType childTuple = childTuples.next();
			createTuple(transaction, childTuple, entityKey);
		}
    }
	
	public void create(Entity entity) {
		// !todo Implement me
		throw new RuntimeException("How about implementing me?!");
	}

	public void delete(Entity entity) {
		// !todo Implement me
		throw new RuntimeException("How about implementing me?!");
	}

	public void install(Transaction transaction) {
		// Create the tables for all relational tuples...
//        final ResultSet rs = transaction.query("tupleTypeReadRelationalEntities");
//        while(rs.next()) {
//        	final String key = rs.getString("key");
//        	final EntityType entityType = entityTypes.get(key);
//        	final BagOfTricks bot = getBot(transaction, entityType);
//        	bot.createTables(entityType, transaction);
//        }
	}

	public Entity read(int id) {
		// !todo Implement me
		throw new RuntimeException("How about implementing me?!");
	}

	public void uninstall(Transaction transaction) {
		// !todo Implement me
		throw new RuntimeException("How about implementing me?!");
	}

	public void update(Entity entity) {
		// !todo Implement me
		throw new RuntimeException("How about implementing me?!");
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
	
	private String generateTableName(Transaction transaction, TupleType tupleType) throws SQLException {
		final StringBuilder sb = new StringBuilder();
		
		// Build the stem: "TUPLENAME_"
		sb.append(tupleType.getKey().toUpperCase());
		if(sb.length() > MAX_NAME_LENGTH) {
			sb.setLength(MAX_NAME_LENGTH);
		}
		sb.append('_');
		
		// Load the matching names...
		// SELECT TableName FROM TupleType WHERE TupleName LIKE '{TableNameStem}%'
        final ResultSet rs = transaction.query(
        		"TupleGetMatchingNames",
        		"{TableNameStem}", sb.toString());
        final Set<String> usedNames = new HashSet<String>();
        while(rs.next()) {
        	usedNames.add(rs.getString(1));
        }
		rs.close();
		
		// Now generate the _0 postfix...
		final int stemLength = sb.length();
		for(int i = 0; ; i++) {
			sb.setLength(stemLength);
			sb.append(Integer.toString(i));
			if(!usedNames.contains(sb.toString())) {
				break;
			}
		}
		
		return sb.toString();
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
				getValidColumnName(columnNameMap, truncate(ktcvKey, MAX_NAME_LENGTH));
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

		private String truncate(String string, int truncateLength) {
			final int currentLength = string.length();
			return string.substring(0, currentLength < truncateLength ? currentLength : truncateLength);
		}

		@SuppressWarnings("unused")
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
				final String dataType = transaction.getSql("SqlType_" + ktcv.getDataType().asText);
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

		private String getValidColumnName(Map<String, String> usedNames, String ktcvKey) {
			final StringBuilder sb = new StringBuilder();
			sb.append(ktcvKey.toUpperCase());
			if(sb.length() > MAX_NAME_LENGTH) {
				sb.setLength(MAX_NAME_LENGTH);
			}
			final int stemLength = sb.length();
			int unique = 0;
			while(reservedNames.containsKey(sb.toString()) || usedNames.containsValue(sb.toString())) {
				sb.setLength(stemLength);
				sb.append('_');
				sb.append(unique);
				unique++;
			}
			usedNames.put(ktcvKey, sb.toString());
			return sb.toString();
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
		@SuppressWarnings("unused")
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
