package integrations.server;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;

import _imperfactcoverage.Helper;

public class TestLandingPage {
    @Test
    public void containsConextForJavaScript() {
        Runnable block = () -> {
        	String context = Helper.readContext();
        	String expected = javaScriptContext(context);
        	String url = "http://localhost:3333" + context;
            String actual = Helper.get(url).getBody();
			assertThat(actual).contains(expected);
        };
		running(testServer(3333), block);
    }

	private String javaScriptContext(String context) {
		String template = "var CONTEXT = '%s';";
		String expected = String.format(template, context);
		return expected;
	}
}