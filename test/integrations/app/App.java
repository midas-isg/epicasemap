package integrations.app;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.io.File;
import java.util.Map;

import play.Configuration;
import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.test.FakeApplication;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {
	private FakeApplication fakeApp = null;

	public static App newWithTestDb() {
		return new App("test/resources/conf/test.conf");
	}

	public static App newWithInMemoryDb() {
		return new App("test/resources/conf/test_in_memory_DB.conf");
	}

	public static App doNotUseForBoostingupCoverageOnly(String path) {
		try {
			return new App(path);
		} catch (Exception e) {
			return null;
		}
	}

	private App(String testConfPathname) {
		Map<String, Object> configurationMap = readConf(testConfPathname);
		fakeApp = fakeApplication(configurationMap);
	}

	public void runWithTransaction(Callback0 callback) {
		running(fakeApp, withTransaction(callback));
	}

	private Map<String, Object> readConf(String pathname) {
		File file = new File(pathname);
		if (!file.exists())
			throw new RuntimeException(pathname + " is not found");
		Config config = ConfigFactory.parseFile(file);
		Configuration configuration = new Configuration(config);
		return configuration.asMap();
	}

	private Runnable withTransaction(Callback0 callback) {
		return () -> {
			JPA.withTransaction(callback);
		};
	}
}
