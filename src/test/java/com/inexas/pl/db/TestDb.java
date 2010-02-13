package com.inexas.pl.db;

import org.junit.*;
import com.inexas.pl.*;
import static org.junit.Assert.*;

public class TestDb extends AbstractProcessLabTest {

	@Test 
	public void connect() {
		final Db db = Db.reserveInstance();
		assertNotNull(db);
		Db.releaseInstance(db);
	}

	@Test(expected = DbRuntimeException.class)
	public void exit() {
		final Db db = Db.reserveInstance();
		assertNotNull(db);
		Db.staticExitGracefully();
	}
}
