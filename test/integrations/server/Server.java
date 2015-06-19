package integrations.server;

import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import suites.Helper;

public class Server {
	private static String context = null;
	
	public static String makeTestUrl(String path) {
		return "http://localhost:3333" + getContext() + path;
	}

	public static void run(Runnable... tests) {
		Runnable test = () -> runAll(tests);
		running(testServer(3333), test);
	}

	private static void runAll(Runnable... tests) {
		for (Runnable test : tests)
			test.run();
	}

	public static String getContext() {
		if (context == null)
			context = Helper.readContext();
		return context;
	}
}
