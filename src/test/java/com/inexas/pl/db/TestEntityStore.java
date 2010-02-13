package com.inexas.pl.db;

import org.junit.*;
import com.inexas.pl.*;
import static org.junit.Assert.*;

public class TestEntityStore extends AbstractProcessLabTest {

	@Test 
	public void install() {
		final EntityStore store = EntityStore.getInstance();
		assertNotNull(store);
	}

}
