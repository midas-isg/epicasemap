package integrations.server;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import suites.Helper;

public class TestLandingPage {
    @Test
    public void containsConextForJavaScript() {
        Runnable block = () -> {
        	String context = Server.getContext();
        	String expected = javaScriptContext(context);
        	String url = Server.makeTestUrl("");
            String actual = Helper.get(url).getBody();
			assertThat(actual).contains(expected);
        };
		Server.run(block);
    }

	private String javaScriptContext(String context) {
		String template = "var CONTEXT = '%s';";
		String expected = String.format(template, context);
		return expected;
	}
}