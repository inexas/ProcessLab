package com.inexas.pl.db;

import java.sql.*;
import java.util.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public class RelationalSubsystem implements Subsystem {
	private final int MAX_NAME_LENGTH = 10;
	private final Set<String> reservedNames = new HashSet<String>();
	private final BasicCache<String, BagOfTricks> bots = new BasicCache<String, BagOfTricks>();

	public RelationalSubsystem() {
		// Load the reserved names...
		final Db db = Db.reserveInstance();
		final String reservedNameList = db.getSql("ReservedNames");
		final StringTokenizer parser = new StringTokenizer(reservedNameList, ",");
		while(parser.hasMoreTokens()) {
			reservedNames.add(parser.nextToken());
		}

		Db.releaseInstance(db);
	}

	public void create(EntityType entity) {
		final Db db = Db.reserveInstance();
		final String entityKey = entity.getKey();
		db.update("EntityTypeInsert",
				"{Id}", entityKey,
				"{Markup}", entity.toString());
    	Db.releaseInstance(db);
//		final Transaction transaction = db.startTransaction();
//    	try {
//    		// Create the entry in the EntityType table...
//    		
//    		// EntityTypeInsert=\
//    		// INSERT INTO EntityType(Id,NextId,Relational,Markup)\
//    		// VALUES({Id},1,'R',{Markup})
//    		
//    		// And create the TupleTableName records recursively
////    		createTuple(transaction, entity, entityKey);
//    		
//    		db.commit(transaction);
//        } catch(final SQLException e) {
//        	db.rollback(transaction);
//        	throw new DbRuntimeException("Error creating EntityType: " + entity, e);
//        }
    }

	public void delete(EntityType entity) {
		final Db db = Db.reserveInstance();
		final Transaction transaction = db.startTransaction();
		// Delete any entities...
		
		// TupleTableNameRead=\
		// SELECT Name FROM TupleTableName WHERE Owner='{Owner}'
		final String entityKey = entity.getKey();
		final ResultSet rs = transaction.query("TupleTableNameRead", "{Owner}", entityKey);
		try {
			// Drop the entity tables (and their contents!)
	        while(rs.next()) {
	        	// EnityDropTable=\
	        	// DROP TABLE {TableName}
	        	transaction.update("EnityDropTable", "{TableName}", rs.getString(1));
	        }
	        
	        // Delete the table entries
	        // TupleTableNameDelete=\
	    	// DELETE FROM TupleTableName WHERE Owner='{Key}'
        	transaction.update("TupleTableNameDelete", "{Owner}", entityKey);
        	
        	// Drop the entity type record
        	// EntityTypeDelete=\
        	// DELETE FROM EntityType WHERE Id={Id}
        	transaction.update("EntityTypeDelete", "{Id}", entityKey);

	        db.commit(transaction);
        } catch(final SQLException e) {
	        db.rollback(transaction);
	        throw new DbRuntimeException("Error deleting: " + entity, e);
        }
		
		Db.releaseInstance(db);
    }

	public void create(Entity entity) {
		final Db db = Db.reserveInstance();
		final Transaction transaction = db.startTransaction();
		
		try {
			final EntityType entityType = entity.getEntityType();
			
	        // Might we need to create the tables for the entity?
	        if(entityType.getTableName() == null) {
	        	createTables(transaction, entityType, entityType.getKey());
	        }
	        
	        update(transaction, entity);
	        
	        db.commit(transaction);
        } catch(final Exception e) {
	        db.rollback(transaction);
	        throw new DbRuntimeException("Error creating entity: " + entity, e);
        } finally {
        	Db.releaseInstance(db);
        }
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
			try {
				// First create the table name record...
				// TupleTableNameInsert=\
				// INSERT INTO TupleTableName(Name,Owner,Path)\
				// VALUES('{Name}','{Owner}','{Path}')
				final String tableName = sb.toString();
	            transaction.update("TupleTableNameInsert",
	            		"{Name}", tableName,
	            		"{Owner}", entityKey,
	            		"{Path}", tuplePath);
	            // If we get here we inserted the record so the table name
	            // must be unique
	            tuple.setTableName(tableName);
	            
	            // Now create the table itself...
	            
	            
	            break;
            } catch(DbRuntimeException e) {
            	// todo Can we find a way of checking for PK violations
            	if(disambiguator > 1000) {
            		throw new DbRuntimeException(
            				"Too many attempts to find an unambiguous name, change the tuple type name");
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
	
	private void createTables(Transaction transaction, TupleType tuple, String entityKey) throws SQLException {
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
			try {
				// First create the table name record...
				// TupleTableNameInsert=\
				// INSERT INTO TupleTableName(Name,Owner,Path)\
				// VALUES('{Name}','{Owner}','{Path}')
				final String tableName = sb.toString();
	            transaction.update("TupleTableNameInsert",
	            		"{Name}", tableName,
	            		"{Owner}", entityKey,
	            		"{Path}", tuplePath);
	            // If we get here we inserted the record so the table name
	            // must be unique
	            tuple.setTableName(tableName);
	            
	            // Now create the table itself...
	            
	            
	            break;
            } catch(DbRuntimeException e) {
            	// todo Can we find a way of checking for PK violations
            	if(disambiguator > 1000) {
            		throw new DbRuntimeException(
            				"Too many attempts to find an unambiguous name, change the tuple type name");
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

		final String nonRootColumns;
		if(tuple instanceof EntityType) {
			nonRootColumns = "";
		} else {
			nonRootColumns = ",Rsvd_Pid INT NOT NULL,Rsvd_Ord INT NOT NULL";
		}
		final StringBuilder ktcvColumns = new StringBuilder();
		final Iterator<KtcvType<?>> ktcvs = tuple.getKtcvTypeMemberIterator();
		final Map<String, String> columnNameMap = getColumnNameMap(tuple);
		while(ktcvs.hasNext()) {
			final KtcvType<?> ktcv = ktcvs.next();
			ktcvColumns.append(',');
			ktcvColumns.append(columnNameMap.get(ktcv.getKey()));
			ktcvColumns.append(' ');
			final String dataType = transaction.getSql("SqlType_" + ktcv.getDataType().asText);
			ktcvColumns.append(dataType);
		}

		// TupleCreateTable=\
		// CREATE TABLE {TableName}(\
		// Rsvd_id INT PRIMARY KEY\
		// {NonRootColumns}\
		// {KtcvColumns}\
		transaction.update(
				"TupleCreateTable",
				"{TableName}", tuple.getTableName(),
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


	public void update(Entity entity) {
		// !todo Check entity has been created, throw if not
		
		final Db db = Db.reserveInstance();
		final Transaction transaction = db.startTransaction();
		
		try {
			// Delete any deleted tuples...
			final Collection<Tuple> deletedTuples = entity.getDeletedTuples();
			for(final Tuple tuple : deletedTuples) {
				assert tuple.isDeleted();
				assert tuple.getId() != 0;
				// TupleDelete=\
				// DELETE FROM {TableName} WHERE Id={Id}
				transaction.update("TupleDelete",
						"{TableName}", tuple.getTupleType().getTableName(),
						"{Id}", Integer.toString(tuple.getId()));
			}
			
	        update(transaction, entity);
	        
	        db.commit(transaction);
        } catch(final Exception e) {
	        db.rollback(transaction);
	        throw new DbRuntimeException("Error creating entity: " + entity, e);
        } finally {
        	Db.releaseInstance(db);
        }
	}

	private Map<String, String> getColumnNameMap(TupleType tupleType) {
		final Map<String, String> result = tupleType.getColumnNameMap();
		if(result.isEmpty()) {
			final Iterator<KtcvType<?>> ktcvs = tupleType.getKtcvTypeMemberIterator();
			while(ktcvs.hasNext()) {
				// Get the key, truncate it if it's too long...
				final KtcvType<?> ktcv = ktcvs.next();
				final String key = ktcv.getKey();
				final StringBuilder sb = new StringBuilder(key);
				if(sb.length() > MAX_NAME_LENGTH) {
					sb.setLength(MAX_NAME_LENGTH);
				}
				
				String uniqueName = sb.toString();
				if(reservedNames.contains(uniqueName) || result.containsValue(uniqueName)) {
					sb.append('_');
					final int stemLength = sb.length();
					int disambiguator = 0;
					while(true) {
						sb.append(disambiguator);
						uniqueName = sb.toString();
						if(reservedNames.contains(uniqueName) || result.containsValue(uniqueName)) {
							break;
						}
						disambiguator++;
						sb.setLength(stemLength);
					}
				}
				result.put(key, uniqueName);
			}
		}
		return result;
	}

	/**
	 * This method is called both to update a changed entity as well as
	 * to create (insert) a new entity. The entity's members are marked 
	 * with dirty flags to let us know what needs to be updated and 
	 * deleted.
	 * @throws SQLException 
	 */
	private void update(Transaction transaction, Tuple tuple) throws SQLException {

		// Initialize useful variables...
		final TupleType tupleType = tuple.getTupleType();
		final Map<String, String> columnNameMap = getColumnNameMap(tupleType);
		final String tableName = tupleType.getTableName();
		
		// If the ID is 0 then we need to insert else we do an update...
		if(tuple.getId() == 0) {
			// Generated the ID...
			
			// TupleGetNextId=\
			// SELECT NextId FROM EntityType WHERE Id={Id}
			final EntityType entityType = tuple.getEntity().getEntityType();
			final String entityTypeId = entityType.getKey();
			final ResultSet rs = transaction.query("TupleGetNextId", "{Id}", entityTypeId);
			rs.next();
			final int id = rs.getInt(1);
			// TupleUpdateNextId=\
			// UPDATE EntityType SET NextId={NextId} WHERE Id={Id}
			transaction.update("TupleUpdateNextId",
					"{Id}", entityTypeId,
					"{NextId}", Integer.toString(id + 1));
			tuple.setId(id);

			// Now insert a new tuple record...
			
			// INSERT INTO {TableName}(Rsvd_Id{NonRootFieldNames}{Names})
			// VALUES({Id}{NonRootFields}{Values})
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

			// TupleInsert=\
			// INSERT INTO {TableName}(Rsvd_Id{NonRootFieldNames}{Names})\
			// VALUES({Id}{NonRootFields}{Values})
			transaction.update(
					"TupleInsert",
					"{TableName}", tableName,
					"{NonRootFieldNames}", nonRootFieldNames,
					"{Names}", names.toString(),
					"{Id}", Integer.toString(id),
			        "{NonRootFields}", nonRootFields,
			        "{Values}", values.toString());
			tuple.setClean();

			// Now insert the child tuple lists recursively...
			final Iterator<TupleList> tupleLists = tuple.getTupleListIterator();
			while(tupleLists.hasNext()) {
				final TupleList tupleList = tupleLists.next();
				final BagOfTricks bot = getBot(transaction, tupleList.getType());
				for(final Tuple childTuple : tupleList) {
					bot.insertRecursively(childTuple, transaction);
				}
			}
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

	public void delete(Entity entity) {
		final Db db = Db.reserveInstance();
		final Transaction transaction = db.startTransaction();
		
		try {
	        delete(transaction, entity);
	        
	        db.commit(transaction);
        } catch(final Exception e) {
	        db.rollback(transaction);
	        throw new DbRuntimeException("Error deleting entity: " + entity, e);
        } finally {
        	Db.releaseInstance(db);
        }
	}

    private void delete(Transaction transaction, Tuple tuple) throws SQLException {
    	assert tuple.getId() != 0;

		// Women and children first...
		final Iterator<TupleList> tupleLists = tuple.getTupleListIterator();
		while(tupleLists.hasNext()) {
			final TupleList childTupleList = tupleLists.next();
			for(final Tuple childTuple : childTupleList) {
				delete(transaction, childTuple);
			}
		}

		// TupleDelete=\
		// DELETE FROM {TableName} WHERE Rsvd_Id={Id}
		transaction.update(
				"TupleDelete",
				"{TableName}", tuple.getTupleType().getTableName(),
				"{Id}", Integer.toString(tuple.getId()));
		
		tuple.setId(0);
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

			// TupleInsert=\
			// INSERT INTO {TableName}(Rsvd_Id{NonRootFieldNames}{Names})\
			// VALUES({Id}{NonRootFields}{Values})
			transaction.update(
					"TupleInsert",
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
			while(reservedNames.contains(sb.toString()) || usedNames.containsValue(sb.toString())) {
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
		
	}

}
