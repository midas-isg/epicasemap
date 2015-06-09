package integrations.server;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import interactors.ConfRule;

import org.junit.Test;

import controllers.Factory;
import play.libs.F.Callback;
import play.test.TestBrowser;

public class TestLandingPage {
    @Test
    public void containsConextForJavaScript() {
        Callback<TestBrowser> block = browser -> {
        	String context = readContext();
            String url = "http://localhost:3333" + context;
            String actual = pageSource(browser, url);
			String expected = javaScriptContext(context);
			assertThat(actual).contains(expected);
        };
		running(testServer(3333), HTMLUNIT, block);
    }

	private String javaScriptContext(String context) {
		String template = "var CONTEXT = '%s';";
		String expected = String.format(template, context);
		return expected;
	}

	private String pageSource(TestBrowser browser, String url) {
		browser.goTo(url);
		return browser.pageSource();
	}

	private String readContext() {
		ConfRule conf = Factory.makeConfRule();
		return conf.readString("application.context");
	}
}
