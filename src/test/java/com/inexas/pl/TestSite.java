package com.inexas.pl;

import org.junit.*;
import static org.junit.Assert.*;


public class TestSite extends AbstractProcessLabTest {
	
	@Test
	public void load() {
		assertEquals(DB_DRIVER, Site.getDbDriverName());
		assertEquals(DB_URL, Site.getDbUrl());
		assertEquals(DB_USERID, Site.getDbUserId());
		assertEquals(DB_PASSWORD, Site.getDbPassword());
	}
}
