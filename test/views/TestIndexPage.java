package views;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import org.junit.Test;

import play.twirl.api.Content;


public class TestIndexPage {
    @Test
    public void renderTemplate() {
        String message = "Test message";
		Content html = views.html.index.render(message);
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains(message);
    }
}
