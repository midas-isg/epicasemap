package integrations.server;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import impl.Factory;
import interactors.ConfRule;

import org.junit.Test;

import play.libs.F.Callback;
import play.test.TestBrowser;
public class TestLandingPage {
    @Test
    public void landingPageShown() {
        Callback<TestBrowser> block = browser -> {
        	ConfRule conf = Factory.makeConfRule();
        	String context = conf.readString("application.context");
            browser.goTo("http://localhost:3333" + context);
            String expected = "Your new application is ready.";
			assertThat(browser.pageSource()).contains(expected);
        };
		running(testServer(3333), HTMLUNIT, block);
    }
}
