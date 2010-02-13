package com.inexas.pl.pl;

import org.junit.*;
import com.inexas.pl.entity.*;
import static org.junit.Assert.*;


public class TestCardinality {

	private void doTest(String string, Cardinality expectedResult) {
		final Cardinality result = Cardinality.newInstance(string);
		assertEquals(result, expectedResult);
	}
	
	private void doReuseTest(String string, Cardinality expectedResult) {
		final Cardinality result = Cardinality.newInstance(string);
		assertTrue(result == expectedResult);
    }

	@Test
	public void parsing() {
		doTest("0..1", Cardinality.ZERO2ONE);
		doTest("0..*", Cardinality.ZERO2MANY);
		doTest("1..1", Cardinality.ONE2ONE);
		doTest("1..*", Cardinality.ONE2MANY);
	}

	@Test
	public void reuse() {
		doReuseTest("0..1", Cardinality.ZERO2ONE);
		doReuseTest("0..*", Cardinality.ZERO2MANY);
		doReuseTest("1..1", Cardinality.ONE2ONE);
		doReuseTest("1..*", Cardinality.ONE2MANY);
	}

	@Test(expected= InvalidCardinalityException.class)
	public void nullString() { 
		Cardinality.newInstance(null);
	}

	@Test(expected= InvalidCardinalityException.class)
	public void invalidString1() { 
		Cardinality.newInstance("2..");
	}

	@Test(expected= InvalidCardinalityException.class)
	public void invalidString2() { 
		Cardinality.newInstance("2..2s");
	}

	@Test(expected= InvalidCardinalityException.class)
	public void invalidCardinality1() { 
		Cardinality.newInstance("0..0");
	}

	@Test(expected= InvalidCardinalityException.class)
	public void invalidCardinality2() { 
		Cardinality.newInstance("1..0");
	}

	@Test(expected= InvalidCardinalityException.class)
	public void invalidCardinality3() { 
		Cardinality.newInstance("2..1");
	}

}
