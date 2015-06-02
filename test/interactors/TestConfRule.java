package interactors;

import static org.fest.assertions.Assertions.assertThat;
import gateways.configuration.ConfReader;

import org.junit.Test;

public class TestConfRule {
	@Test
	public void readAppName() throws Exception {
		ConfRule r = new ConfRule(new DummyConfReader());
		String key1 = "key1";
		String actual1 = r.readString(key1);
		assertThat(actual1).isEqualTo(key1);
		assertThat(r.readString("key2")).isNotEqualTo(actual1);
	}

	private static class DummyConfReader extends ConfReader {
		@Override
		public String readString(String key) {
			return key;
		}
	}
}

