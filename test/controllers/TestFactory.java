package controllers;

import org.junit.Test;

import controllers.Factory;
import suites.TestCase;

public class TestFactory extends TestCase {
	@Test
	public void testConstructorPrivate() throws Exception {
		testPrivateDefaultConstructor(Factory.class);
	}
}
