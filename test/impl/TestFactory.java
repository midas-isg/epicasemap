package impl;

import org.junit.Test;

import suites.TestCase;

public class TestFactory extends TestCase {
	@Test
	public void testConstructorPrivate() throws Exception {
		testPrivateDefaultConstructor(Factory.class);
	}
}
