package integrations.app;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.io.File;
import java.util.Map;

import play.Configuration;
import play.libs.F.Callback0;
import play.libs.F.Function0;
import play.test.FakeApplication;
import suites.Helper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {
	private FakeApplication fakeApp = null;

	public static App newWithTestDb() {
		return new App("test/resources/conf/test.conf");
	}

	public static App newWithInMemoryDb() {
		return newWithInMemoryDb(false);
	}
	
	public static App newWithInMemoryDbWithDbOpen() {
		return newWithInMemoryDb(true);
	}

	public static App newWithInMemoryDb(boolean isKeptDatabaseOpen) {
		return new App("test/resources/conf/test_in_memory_DB.conf", isKeptDatabaseOpen);
	}

	public static App doNotUseForBoostingupCoverageOnly(String path) {
		try {
			return new App(path);
		} catch (Exception e) {
			return null;
		}
	}
	
	private App(String testConfPathname) {
		this(testConfPathname, false);
	}

	private App(String testConfPathname, boolean isKeptDatabaseOpen) {
		Map<String, Object> configurationMap = readConf(testConfPathname);
		if (isKeptDatabaseOpen)
			KeepDatabaseOpen(configurationMap);
		fakeApp = fakeApplication(configurationMap);
	}

	private void KeepDatabaseOpen(Map<String, Object> originalMap) {
		Map<String, Object> map = getMap(getMap(originalMap, "db"), "default");
		String url = (String) map.get("url");
		map.put("url", url + "-keep;DB_CLOSE_DELAY=-1");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMap(Map<String, Object> map, String key) {
		return (Map<String, Object>)map.get(key);
	}

	public void runWithTransaction(Callback0 callback) {
		Function0<Void> f = () -> {
			callback.invoke();
			return null;
		};
		
		running(fakeApp, () -> Helper.wrapTransaction(f));
	}

	private Map<String, Object> readConf(String pathname) {
		File file = new File(pathname);
		if (!file.exists())
			throw new RuntimeException(pathname + " is not found");
		Config config = ConfigFactory.parseFile(file);
		Configuration configuration = new Configuration(config);
		return configuration.asMap();
	}
}
