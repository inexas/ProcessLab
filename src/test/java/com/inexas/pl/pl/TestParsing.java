package com.inexas.pl.pl;
import static org.junit.Assert.*;
import org.junit.*;
import com.inexas.pl.entity.*;


public class TestParsing {

	private void test(String subject) {
		final EntityCompiler compiler = new EntityCompiler(subject);
		final TupleType entity = compiler.getEntity();
		final String actual = entity.toString();
		assertEquals(subject, actual);
    }

	@Test
	public void basicTesta() {
		test("Abc {\n    Def;\n}\n");
		test("Abc 1..1 {\n    Def : Integer;\n}\n");
	}

}
