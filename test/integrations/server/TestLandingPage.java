package integrations.server;

import static org.fest.assertions.Assertions.assertThat;
import suites.Helper;

public class TestLandingPage {
	public static Runnable containsConextForJavaScript() {
        Runnable block = () -> {
        	String context = Server.getContext();
        	String expected = javaScriptContext(context);
        	String url = Server.makeTestUrl("");
            String actual = Helper.get(url).getBody();
			assertThat(actual).contains(expected);
        };
		return block;
    }

	private static String javaScriptContext(String context) {
		String template = "var CONTEXT = '%s';";
		String expected = String.format(template, context);
		return expected;
	}
}