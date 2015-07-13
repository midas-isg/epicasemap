package suites;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;

public class TestCase {
	protected <T> void testPrivateDefaultConstructor(Class<T> clazz)
			throws Exception {
		Constructor<T> constructor = clazz.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
