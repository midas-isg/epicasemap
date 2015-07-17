package integrations.server;

import static org.fest.assertions.Assertions.assertThat;
import suites.Helper;

public class LandingPageTester {
	public static Runnable containsConextForJavaScript() {
        Runnable block = () -> {
        	String context = Server.getContext();
        	String expected = new LandingPageTester().javaScriptContext(context);
        	String url = Server.makeTestUrl("");
            String actual = Helper.get(url).getBody();
			assertThat(actual).contains(expected);
        };
		return block;
    }

	private String javaScriptContext(String context) {
		String template = "var CONTEXT = '%s';";
		String expected = String.format(template, context);
		return expected;
	}
}