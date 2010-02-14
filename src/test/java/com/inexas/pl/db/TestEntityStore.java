package com.inexas.pl.db;

import java.util.*;
import org.junit.*;
import com.inexas.pl.*;
import com.inexas.pl.entity.*;
import static org.junit.Assert.*;

public class TestEntityStore extends AbstractProcessLabTest {
	private static EntityStore store;

	@BeforeClass 
	public static void install() {
		store = EntityStore.getInstance();
		assertNotNull(store);
	}
	
	@Test
	public void createLoadDeleteEntityType() {
		final List<AbstractType> members = new ArrayList<AbstractType>();
		final KtcvType<?> ktcv = KtcvType.getKtcvType(DataType.STRING, "String");
		members.add(ktcv);
		final EntityType entityType = new EntityType("TestPage:1:TestTuple", Cardinality.ONE2MANY, members );
		store.create(entityType);
		assertEquals("TESTTUPLE_0",  entityType.getTableName());
	}

}
