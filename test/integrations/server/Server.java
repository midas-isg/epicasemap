package integrations.server;

import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.HashMap;
import java.util.Map;

import integrations.app.App;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import suites.Helper;

public class Server {
	private static String context = null;
	private static String cookieValue;

	public static String toCookieValue() {
		if (cookieValue == null) {
			WSResponse wsResponse = login();
			cookieValue = wsResponse.getHeader("Set-Cookie");
		    System.out.println(cookieValue);
		}
		return cookieValue;
	}

	private static WSResponse login() {
		Map<String, String> body = new HashMap<>();
		body.put("email", "public@test.com");
		body.put("password", "public");
		String url = makeTestUrl("/login");
		WSResponse wsResponse = WS.url(url).setFollowRedirects(false).post(Json.toJson(body)).get(100_000);
		return wsResponse;
	}

	public static String makeTestUrl(String path) {
		final String fullPath = getContext() + path;
		return "http://localhost:3333" + fullPath.replace("//", "/");
	}

	public static void run(Runnable... tests) {
		Runnable test = () -> runAll(tests);
		running(testServer(3333, App.newWithTestDb().getFakeApp()), test);
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
